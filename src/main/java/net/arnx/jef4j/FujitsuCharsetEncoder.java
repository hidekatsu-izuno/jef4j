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
	
	private boolean shiftin = false;

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
					
					if (shiftin) {
						if (!out.hasRemaining()) {
							return CoderResult.OVERFLOW;
						}
						out.put((byte)0x29);
						shiftin = false;
					}

					if (!out.hasRemaining()) {
						return CoderResult.OVERFLOW;
					}
					out.put(value);
					mark++;
				} else if (type.containsJEF()) { // Double Bytes
					Record record;
					int pos;
					
					int progress = 1;
					if (Character.isSurrogate(c)) {
						if (!Character.isHighSurrogate(c)) {
							return CoderResult.malformedForLength(1);
						}
						
						if (!in.hasRemaining()) {
							return CoderResult.UNDERFLOW;
						}
						char c2 = in.get();
						if (!Character.isLowSurrogate(c2)) {
							return CoderResult.malformedForLength(2);
						}
						
						record = JEF_MAP.get(Character.toCodePoint(c, c2) & 0xFFFF0);
						pos = c2 & 0xF;
						progress++;
					} else {
						record = JEF_MAP.get(c & 0xFFF0);
						pos = c & 0xF;
					}
					
					if (record == null || !record.exists(pos)) {
						return CoderResult.unmappableForLength(1);
					}
					
					if (in.hasRemaining()) {
						in.mark();
					}
					
					if (map != null && !shiftin) {
						if (!out.hasRemaining()) {
							return CoderResult.OVERFLOW;
						}
						out.put((byte)0x28);
						shiftin = true;
					}
					
					if (out.remaining() < 2) {
						return CoderResult.OVERFLOW;
					}
					
					char mc = (char)record.get(pos);
					out.put((byte)((mc >> 8) & 0xFF));
					out.put((byte)(mc & 0xFF));
					
					mark += progress;
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
		if (map != null && shiftin) {
			if (!out.hasRemaining()) {
				return CoderResult.OVERFLOW;
			}
			out.put((byte)0x28);
			shiftin = false;
		}
		return CoderResult.UNDERFLOW;
	}
	
	@Override
	protected void implReset() {
		shiftin = false;
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
