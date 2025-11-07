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
public class HitachiCharsetDecoder extends CharsetDecoder {
	private static final byte[][] SBCS_MAP = new byte[2][];
	private static final LongObjMap<Record[]> MBCS_MAP;
	
	static {
		try (ObjectInputStream in = new ObjectInputStream(
				HitachiCharsetDecoder.class.getResourceAsStream("HitachiDecodeMap.dat"))) {
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

    public HitachiCharsetDecoder(Charset cs, HitachiCharsetType type) {
		super(cs, 1, getMaxCharsPerByte(type));
		this.type = type;
		int sbcsTableNo = type.getSBCSTableNo();
		this.map = (sbcsTableNo != -1) ? SBCS_MAP[sbcsTableNo] : null;
	}

	@Override
	protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
		int mark = in.position();
		try {
			while (in.hasRemaining()) {
				int b = in.get() & 0xFF;
				if (type.handleSBCS() && type.handleMBCS() && b == 0x0A) {
					if (!in.hasRemaining()) {
						return CoderResult.UNDERFLOW;
					}
					int b2 = in.get() & 0xFF;
					if (b2 == 0x41) {
						kshifted = true;
						mark += 2;
					} else if (b2 == 0x42) {
						kshifted = false;
						mark += 2;
					} else {
						return CoderResult.unmappableForLength(1);
					}
				}

				if (!kshifted && map != null) {
					char c = b != 0 ? (char)(map[b] & 0xFF) : '\0';
					if (c >= '\u00C0') {
						c = (char)(c - '\u00C0' + '\uFF61');
					} else if (c == '\u00B0') {
						c = '\u203E';
					}
					if (b != 0 && c == 0) {
						return CoderResult.unmappableForLength(1);
					}
					
					if (!out.hasRemaining()) {
						return CoderResult.OVERFLOW;
					}
					out.put(c);
					mark++;
				} else if (type.handleMBCS() && b >= 0x40 && b <= 0xFE) {
					if (!in.hasRemaining()) {
						return CoderResult.UNDERFLOW;
					}
					
					int b2 = in.get() & 0xFF;
					if (b == 0x40 && b2 == 0x40) {
						out.put('\u3000');
						mark += 2;
					} else if (b >= 0x81 && b <= 0xA0) { // Private Use Area
						if (b2 >= 0xA1 && b2 <= 0xFE) {
							out.put((char)(0xE000 + (b - 0x81) * 94 + (b2 - 0xA1)));
							mark += 2;
						} else {
							return CoderResult.unmappableForLength(2);
						}
					} else {
						Record[] records = MBCS_MAP.get((b << 8) | (b2 & 0xF0));
						Record record = records != null ? records[type.getMBCSTableNo()] : null;
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

    private static float getMaxCharsPerByte(HitachiCharsetType type) {
		return type.handleMBCS() ? 2.0F : 1.0F;
	}
}
