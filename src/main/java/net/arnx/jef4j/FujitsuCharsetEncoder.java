/*
 * Copyright 2018 Hidekatsu Izuno <hidekatsu.izuno@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.arnx.jef4j;

import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import net.arnx.jef4j.util.LongObjMap;
import net.arnx.jef4j.util.Record;

@SuppressWarnings("unchecked")
class FujitsuCharsetEncoder extends CharsetEncoder {
	private static final byte[] ASCII_MAP;
	private static final byte[] EBCDIC_MAP;
	private static final byte[] EBCDIK_MAP;
	private static final LongObjMap<Record> JEF_MAP;
	private static final ByteBuffer DUMMY = ByteBuffer.allocate(0);
	
	static {
		try (ObjectInputStream in = new ObjectInputStream(
				FujitsuCharsetEncoder.class.getResourceAsStream("FujitsuEncodeMap.dat"))) {
			ASCII_MAP = (byte[])in.readObject();
			EBCDIC_MAP = (byte[])in.readObject();
			EBCDIK_MAP = (byte[])in.readObject();
			JEF_MAP = (LongObjMap<Record>)in.readObject();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private final FujitsuCharsetType type;
	private final byte[] map;
	
	private boolean kshifted = false;
	private StringBuilder backup;

	public FujitsuCharsetEncoder(Charset cs, FujitsuCharsetType type) {
		super(cs, getAverageBytesPerChar(type), getMaxBytesPerChar(type), getReplacementChar(type));
		this.type = type;
		
		switch (type) {
		case ASCII:
		case JEF_ASCII:
		case JEF_HD_ASCII:
			map = ASCII_MAP;
			break;
		case EBCDIC:
		case JEF_EBCDIC:
		case JEF_HD_EBCDIC:
			map = EBCDIC_MAP;
			break;
		case EBCDIK:
		case JEF_EBCDIK:
		case JEF_HD_EBCDIK:
			map = EBCDIK_MAP;
			break;
		default:
			map = null;
		}
	}

	@Override
	protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
		if (backup != null) {
			if (out.remaining() < 2) {
				return CoderResult.OVERFLOW;
			}

			if (!in.hasRemaining()) {
				return CoderResult.UNDERFLOW;
			}
			int mark = in.position();
			char c = in.get();
			if (Character.isHighSurrogate(c)) {
				if (!in.hasRemaining()) {
					if (isEndOfInput()) {
						backup.append(c);
					} else {
						in.position(mark);
						return CoderResult.UNDERFLOW;
					}
				} else {
					char c2 = in.get();
					if (Character.isLowSurrogate(c2)) {
						backup.append(c);
						backup.append(c2);
					} else {
						if (isEndOfInput()) {
							backup.append(c);
							backup.append(c2);
						} else {
							in.position(mark);
							return CoderResult.malformedForLength(1);
						}
					}
				}
			} else {
				backup.append(c);
			}

			CharBuffer in2 = CharBuffer.wrap(backup);
			backup = null;

			CoderResult cr = encodeLoop(in2, out, true);
			if (cr.isMalformed()) {
				int delta = in.position() - mark;
				in.position(mark);
				return CoderResult.malformedForLength(delta);
			} else if (cr.isUnmappable()) {
				int delta = in.position() - mark;
				in.position(mark);
				return CoderResult.unmappableForLength(delta);
			} else if (cr.isOverflow()) {
				in.position(mark);
				return CoderResult.OVERFLOW;
			}
		}
		return encodeLoop(in, out, false);
	}

	private CoderResult encodeLoop(CharBuffer in, ByteBuffer out, boolean restored) {
		int mark = in.position();
		try {
			while (in.hasRemaining()) {
				char c = in.get();
				if (c >= '\uFFFE') {
					return CoderResult.unmappableForLength(1);
				} else if (c <= '\u007F'
						|| (map == EBCDIC_MAP && (c == '\u00A3' || c == '\u00A6' || c == '\u00AC'))
						|| (map == EBCDIK_MAP && (c == '\u00A3' || c == '\u00AC' || (c >= '\uFF61' && c <= '\uFF9F')))) {
					
					if (map == null) {
						return CoderResult.unmappableForLength(1);
					} else if (map == EBCDIK_MAP && c >= '\uFF61' && c <= '\uFF9F') {
						c = (char)(c - '\uFF61' + '\u00C0');
					}
					
					byte value = map[c];
					if (value == -1) {
						return CoderResult.unmappableForLength(1);
					}
					
					if (kshifted) {
						if (!out.hasRemaining()) {
							return CoderResult.OVERFLOW;
						}
						out.put((byte)0x29);
						kshifted = false;
					}

					if (!out.hasRemaining()) {
						return CoderResult.OVERFLOW;
					}
					out.put(value);
					mark++;
				} else if (type.handleJEF()) { // Double Bytes
					if (c >= '\uE000' && c <= '\uEC1D') { // Private Use Area
						out.put((byte)((0x80 + (c - 0xE000) / 94) & 0xFF));
						out.put((byte)((0xA1 + (c - 0xE000) % 94) & 0xFF));
						mark++;
					} else {
						long key;
						
						int progress = 1;
						if (Character.isSurrogate(c)) {
							if (!Character.isHighSurrogate(c)) {
								return CoderResult.malformedForLength(1);
							}
							
							if (!in.hasRemaining()) {
								return CoderResult.UNDERFLOW;
							}
							char c2 = in.get();
							if (Character.isLowSurrogate(c2)) {
								progress++;
							} else {
								return CoderResult.malformedForLength(2);
							}
							
							key = Character.toCodePoint(c, c2);
						} else {
							key = c;
						}
						
						Record record = JEF_MAP.get(key & 0xFFFFFFF0);
						if (record == null || !record.exists((int)(key & 0xF))) {
							return CoderResult.unmappableForLength(1);
						}
						
						if (type.handleShift() && !kshifted) {
							if (!out.hasRemaining()) {
								return CoderResult.OVERFLOW;
							}
							out.put((byte)0x28);
							kshifted = true;
						}
						
						if (out.remaining() < 2) {
							return CoderResult.OVERFLOW;
						}

						int mc = -1;
						CoderResult cr = null;
						if (type.handleHanyoDenshi()) {
							if (!in.hasRemaining()) {
								if (!restored) {
									backup = new StringBuilder().appendCodePoint((int)key);
									mark += progress;
									return CoderResult.UNDERFLOW;	
								}
							} else {
								int mark2 = in.position();
								char c3 = in.get();
								if (c3 == '\u3099') {
									long key2 = ((long)c3) << 20 | key;
									Record record2 = JEF_MAP.get(key2 & 0xFFFFFFFFF0L);
									if (record2 != null && record2.exists((int)(key2 & 0xF))) {
										mc = (char)record2.get((int)(key2 & 0xF));
										progress++;
									} else {
										in.position(mark2);
									}
								} else if (c3 == '\uDB40') {
									if (!in.hasRemaining()) {
										if (isEndOfInput()) {
											cr = CoderResult.malformedForLength(1);
										} else if (!restored) {
											backup = new StringBuilder().appendCodePoint((int)key);
											mark += progress;
											return CoderResult.UNDERFLOW;
										} else {
											in.position(mark2);
										}
									} else {
										char c4 = in.get();
										if (Character.isLowSurrogate(c4)) {
											long key2 = ((long)Character.toCodePoint(c3, c4)) << 20 | key;
											Record record2 = JEF_MAP.get(key2 & 0xFFFFFFFFF0L);
											if (record2 != null && record2.exists((int)(key2 & 0xF))) {
												mc = (char)record2.get((int)(key2 & 0xF));
												progress += 2;
											} else {
												cr = CoderResult.unmappableForLength(2);
											}
										} else {
											cr = CoderResult.malformedForLength(2);
										}
									}
								}
							}
						}
						if (mc == -1) {
							mc = (char)record.get((int)(key & 0xF));
						}
						
						out.put((byte)((mc >> 8) & 0xFF));
						out.put((byte)(mc & 0xFF));
						
						mark += progress;
						if (cr != null) {
							return cr;
						}
					}
				} else {
					return CoderResult.unmappableForLength(1);
				}
			}
		} finally {
			in.position(mark);
		}
		return CoderResult.UNDERFLOW;
	}
	
	@Override
	protected CoderResult implFlush(ByteBuffer out) {
		if (out == DUMMY) {
			return CoderResult.OVERFLOW;
		}
		
		if (backup != null) {
			CharBuffer in = CharBuffer.wrap(backup);
			backup = null;

			CoderResult cr = encodeLoop(in, out, true);
			if (cr.isError()) {
				throw new IllegalStateException();
			} else if (cr.isOverflow()) {
				return CoderResult.OVERFLOW;
			}
		}

		if (type.handleShift() && kshifted) {
			if (!out.hasRemaining()) {
				return CoderResult.OVERFLOW;
			}
			out.put((byte)0x28);
			kshifted = false;
		}
		return CoderResult.UNDERFLOW;
	}
	
	@Override
	protected void implReset() {
		kshifted = false;
		backup = null;
	}

	private boolean isEndOfInput() {
		try {
			return flush(DUMMY).isOverflow();
		} catch(IllegalStateException e) {
			return false;
		}
	}
	
	private static float getAverageBytesPerChar(FujitsuCharsetType type) {
		switch (type) {
		case ASCII:
		case EBCDIC:
		case EBCDIK:
			return 1;
		default:
			return 2;
		}
	}
	
	private static float getMaxBytesPerChar(FujitsuCharsetType type) {
		switch (type) {
		case ASCII:
		case EBCDIC:
		case EBCDIK:
			return 1;
		case JEF:
			return 2;
		default:
			return 4;
		}
	}
	
	private static byte[] getReplacementChar(FujitsuCharsetType type) {
		switch (type) {
		case ASCII:
		case EBCDIC:
		case EBCDIK:
			return new byte[] { 0x40 };
		default:
			return new byte[] { 0x40, 0x40 };
		}
	}
}
