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
public class HitachiCharsetEncoder extends CharsetEncoder {
	private static final byte[][] SBCS_MAP = new byte[2][];
	private static final LongObjMap<Record[]> MBCS_MAP;
	private static final ByteBuffer DUMMY = ByteBuffer.allocate(0);
	
	static {
		try (ObjectInputStream in = new ObjectInputStream(
				HitachiCharsetEncoder.class.getResourceAsStream("HitachiEncodeMap.dat"))) {
			SBCS_MAP[0] = (byte[])in.readObject();
			SBCS_MAP[1] = (byte[])in.readObject();
			MBCS_MAP = (LongObjMap<Record[]>)in.readObject();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

    private final HitachiCharsetType type;
	private final byte[] map;
	
	private boolean kshifted = false;
	private StringBuilder backup;

    public HitachiCharsetEncoder(Charset cs, HitachiCharsetType type) {
		super(cs, getAverageBytesPerChar(type), getMaxBytesPerChar(type), getReplacementChar(type));
		this.type = type;
		int sbcsTableNo = type.getSBCSTableNo();
		this.map = (sbcsTableNo != -1) ? SBCS_MAP[sbcsTableNo] : null;
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
				} else if (c <= '\u007F' || (
					map != null && (
						c <= '\u009F'
						|| c == '\u00A3' || c == '\u00A6' || c == '\u00AC'
						|| c == '\u203E' 
						|| (c >= '\uFF61' && c <= '\uFF9F')
					)
				)) {
					if (map == null) {
						return CoderResult.unmappableForLength(1);
					} else {
						if (c == '\u203E') {
							c = (char)(c - '\u203E' + '\u00B0');
						} else if (c >= '\uFF61' && c <= '\uFF9F') {
							c = (char)(c - '\uFF61' + '\u00C0');
						}
					}
					
					byte value = map[c];
					if (c != '\0' && value == 0) {
						return CoderResult.unmappableForLength(1);
					}
					
					if (kshifted) {
						if (out.remaining() < 2) {
							return CoderResult.OVERFLOW;
						}
						out.put((byte)0x01);
						out.put((byte)0x42);
						kshifted = false;
					}

					if (!out.hasRemaining()) {
						return CoderResult.OVERFLOW;
					}
					out.put(value);
					mark++;
				} else if (type.handleMBCS()) { // Double Bytes
					if (c >= '\uE000' && c <= '\uEE5D') { // Private Use Area
						out.put((byte)((0x81 + (c - 0xE000) / 94) & 0xFF));
						out.put((byte)((0xA1 + (c - 0xE000) % 94) & 0xFF));
						mark++;
					} else if (c >= '\uEE5E' && c <= '\uEEBA') { // Private Use Area
						out.put((byte)((0xA0 + (c - 0xEE5E) / 94) & 0xFF));
						out.put((byte)((0xA1 + (c - 0xEE5E) % 94) & 0xFF));
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
						
						Record[] records = MBCS_MAP.get(key & 0xFFFFFFF0);
						Record record = records != null ? records[type.getMBCSTableNo()] : null;
						if (record == null || !record.exists((int)(key & 0xF))) {
							return CoderResult.unmappableForLength(1);
						}
						
						if (type.handleSBCS() && type.handleMBCS() && !kshifted) {
							if (out.remaining() < 2) {
								return CoderResult.OVERFLOW;
							}
							out.put((byte)0x0A);
							out.put((byte)0x41);
							kshifted = true;
						}
						
						if (out.remaining() < 2) {
							return CoderResult.OVERFLOW;
						}

						int mc = -1;
						CoderResult cr = null;
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
								Record[] records2 = MBCS_MAP.get(key2 & 0xFFFFFFFFF0L);
								Record record2 = records2 != null ? records2[type.getMBCSTableNo()] : null;
								if (record2 != null && record2.exists((int)(key2 & 0xF))) {
									mc = (char)record2.get((int)(key2 & 0xF));
									progress++;
								} else {
									in.position(mark2);
								}
							} else if (type.handleIVS() && c3 == '\uDB40') {
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
										Record[] records2 = MBCS_MAP.get(key2 & 0xFFFFFFFFF0L);
										Record record2 = records2 != null ? records2[type.getMBCSTableNo()] : null;
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
							} else {
								in.position(mark2);
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

		if (type.handleSBCS() && type.handleMBCS() && kshifted) {
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
	}

	private boolean isEndOfInput() {
		try {
			return flush(DUMMY).isOverflow();
		} catch(IllegalStateException e) {
			return false;
		}
	}

	private static float getAverageBytesPerChar(HitachiCharsetType type) {
		return type.handleMBCS() ? 2 : 1;
	}
	
	private static float getMaxBytesPerChar(HitachiCharsetType type) {
		return type.handleIVS() ? 4 : type.handleMBCS() ? 2 : 1;
	}
	
	private static byte[] getReplacementChar(HitachiCharsetType type) {
		return type.handleMBCS() ? new byte[] { 0x40, 0x40 } : new byte[] { 0x40 };
	}
}
