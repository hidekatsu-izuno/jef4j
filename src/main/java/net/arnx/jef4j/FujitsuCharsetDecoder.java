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
	private static final LongObjMap<Record> ASCII_MAP;
	private static final LongObjMap<Record> EBCDIC_MAP;
	private static final LongObjMap<Record> EBCDIK_MAP;
	private static final LongObjMap<Record> JEF_MAP;
	
	static {
		try (ObjectInputStream in = new ObjectInputStream(
				FujitsuCharsetEncoder.class.getResourceAsStream("FujitsuDecodeMap.dat"))) {
			ASCII_MAP = (LongObjMap<Record>)in.readObject();
			EBCDIC_MAP = (LongObjMap<Record>)in.readObject();
			EBCDIK_MAP = (LongObjMap<Record>)in.readObject();
			JEF_MAP = (LongObjMap<Record>)in.readObject();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private final FujitsuCharsetType type;
	private final LongObjMap<Record> map;
	
	private boolean shiftin = false;
	
	public FujitsuCharsetDecoder(Charset cs, FujitsuCharsetType type) {
		super(cs, 1, 1);
		this.type = type;
		
		switch (type) {
		case ASCII:
		case JEF_ASCII:
			map = ASCII_MAP;
			break;
		case EBCDIC:
		case JEF_EBCDIC:
			map = EBCDIC_MAP;
			break;
		case EBCDIK:
		case JEF_EBCDIK:
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
				
				if (b == 0x28 || b == 0x38) {
					shiftin = true;
					mark++;
					continue;
				} else if (b == 0x29) {
					shiftin = false;
					mark++;
					continue;
				}
				
				if (!shiftin && map != null) {
					Record record = map.get(b & 0xFFF0);
					int pos = b & 0xF;
					if (record == null || !record.exists(pos)) {
						return CoderResult.unmappableForLength(1);
					}
					
					if (!out.hasRemaining()) {
						return CoderResult.OVERFLOW;
					}
					out.put((char)record.get(pos));
					mark++;
				} else if (containsJef(type) && b >= 0x40 && b <= 0xFE) {
					if (!in.hasRemaining()) {
						return CoderResult.UNDERFLOW;
					}
					
					int b2 = in.get() & 0xFF;
					if ((b == 0x40 && b2 == 0x40)
							|| (b != 0x40 && b2 >= 0xA1 && b2 <= 0xFE)) {
						Record record = JEF_MAP.get((b << 8) | (b2 & 0xF0));
						int pos = b2 & 0xF;
						if (record == null || !record.exists(pos)) {
							return CoderResult.unmappableForLength(2);
						}
						
						if (!out.hasRemaining()) {
							return CoderResult.OVERFLOW;
						}
						long mc = record.get(pos);
						int combi = (int)((mc >> 20) & 0xFFFFF);
						int base = (int)(mc & 0xFFFFF);
						
						if (Character.isSupplementaryCodePoint(base)) {
							out.put(Character.highSurrogate(base));
							out.put(Character.lowSurrogate(base));
						} else {
							out.put((char)base);
						}
						mark += 2;
					} else {
						return CoderResult.unmappableForLength(2);
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
		shiftin = false;
	}
	
	private static boolean containsJef(FujitsuCharsetType type) {
		switch (type) {
		case JEF:
		case JEF_EBCDIC:
		case JEF_EBCDIK:
		case JEF_ASCII:
			return true;
		default:
			return false;
		}
	}
}
