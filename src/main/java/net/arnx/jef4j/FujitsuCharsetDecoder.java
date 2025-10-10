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
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import net.arnx.jef4j.util.LongObjMap;
import net.arnx.jef4j.util.Record;

@SuppressWarnings("unchecked")
class FujitsuCharsetDecoder extends CharsetDecoder {
	private static final byte[] ASCII_MAP;
	private static final byte[] EBCDIC_MAP;
	private static final byte[] EBCDIK_MAP;
	private static final LongObjMap<Record[]> KANJI_MAP;
	
	static {
		try (ObjectInputStream in = new ObjectInputStream(
				FujitsuCharsetDecoder.class.getResourceAsStream("FujitsuDecodeMap.dat"))) {
			ASCII_MAP = (byte[])in.readObject();
			EBCDIC_MAP = (byte[])in.readObject();
			EBCDIK_MAP = (byte[])in.readObject();
			KANJI_MAP = (LongObjMap<Record[]>)in.readObject();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private final FujitsuCharsetType type;
	private final byte[] map;
	
	private boolean kshifted = false;
	
	public FujitsuCharsetDecoder(Charset cs, FujitsuCharsetType type) {
		super(cs, 1, getMaxCharsPerByte(type));
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
	protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
		int mark = in.position();
		try {
			while (in.hasRemaining()) {
				int b = in.get() & 0xFF;
				
				if (type.handleShift()) {
					if (b == 0x28 || b == 0x38) {
						kshifted = true;
						mark++;
						continue;
					} else if (b == 0x29) {
						kshifted = false;
						mark++;
						continue;
					}
				} else if (b == 0x28 || b == 0x38 || b == 0x29) {
					return CoderResult.unmappableForLength(1);
				}
				
				if (!kshifted && map != null) {
					char c = b != 0 ? (char)(map[b] & 0xFF) : '\0';
					if (c >= '\u00B0' && c <= '\u00FE') {
						if (c == '\u00B0') {
							c = '\u203E';
						} else if (c >= '\u00C0') {
							c = (char)(c - '\u00C0' + '\uFF61');
						}
					}
					
					if (b != 0 && c == 0) {
						return CoderResult.unmappableForLength(1);
					}
					
					if (!out.hasRemaining()) {
						return CoderResult.OVERFLOW;
					}
					out.put(c);
					mark++;
				} else if (type.handleJEF() && b >= 0x40 && b <= 0xFE) {
					if (!in.hasRemaining()) {
						return CoderResult.UNDERFLOW;
					}
					
					int b2 = in.get() & 0xFF;
					if (b == 0x40 && b2 == 0x40) {
						out.put('\u3000');
						mark += 2;
					} else if (b2 == 0x28 || b2 == 0x38 || b2 == 0x29) {
						return CoderResult.unmappableForLength(1);
					} else if (b >= 0x80 && b <= 0xA0) {
						if (b2 >= 0xA1 && b2 <= 0xFE) {
							out.put((char)(0xE000 + (b - 0x80) * 94 + (b2 - 0xA1)));
							mark += 2;
						} else {
							return CoderResult.unmappableForLength(2);
						}
					} else {
						Record[] records = KANJI_MAP.get((b << 8) | (b2 & 0xF0));
						Record record = records != null ? records[type.getJEFTableNo()] : null;
						int pos = b2 & 0xF;
						if (record == null || !record.exists(pos)) {
							return CoderResult.unmappableForLength(2);
						}
						
						long mc = record.get(pos);
						int base = (int)(mc & 0xFFFFF);
						int combi = (int)((mc >> 20) & 0xFFFFF);
						
						int baseLen;
						if (Character.isSupplementaryCodePoint(base)) {
							baseLen = 2;
						} else {
							baseLen = 1;
						}
						
						int combiLen;
						if (combi == 0) {
							combiLen = 0;
						} else if (Character.isSupplementaryCodePoint(combi)) {
							if (type.handleIVS()) {
								combiLen = 2;
							} else {
								combiLen = 0;
							}
						} else {
							combiLen = 1;
						}
						
						if (out.remaining() < (baseLen + combiLen)) {
							return CoderResult.OVERFLOW;
						}
						
						if (baseLen == 2) {
							out.put(Character.highSurrogate(base));
							out.put(Character.lowSurrogate(base));
						} else if (baseLen == 1) {
							out.put((char)base);
						} else {
							return CoderResult.unmappableForLength(2);
						}
						
						if (combiLen == 2) {
							out.put(Character.highSurrogate(combi));
							out.put(Character.lowSurrogate(combi));
						} else if (combiLen == 1) {
							out.put((char)combi);
						}
						
						mark += 2;
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
	protected void implReset() {
		kshifted = false;
	}
	
	private static float getMaxCharsPerByte(FujitsuCharsetType type) {
		switch (type) {
		case JEF:
		case JEF_ASCII:
		case JEF_EBCDIC:
		case JEF_EBCDIK:
		case JEF_HD:
		case JEF_HD_ASCII:
		case JEF_HD_EBCDIC:
		case JEF_HD_EBCDIK:
			return 2.0F;
		default:
			return 1.0F;
		}
	}
}
