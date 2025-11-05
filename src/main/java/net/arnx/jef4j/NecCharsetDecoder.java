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
public class NecCharsetDecoder extends CharsetDecoder {
	private static final byte[][] SBCS_MAP = new byte[1][];
	private static final LongObjMap<Record[]> MBCS_MAP;
	
	static {
		try (ObjectInputStream in = new ObjectInputStream(
				NecCharsetDecoder.class.getResourceAsStream("NecDecodeMap.dat"))) {
			SBCS_MAP[0] = (byte[])in.readObject();
			MBCS_MAP = (LongObjMap<Record[]>)in.readObject();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

    private final NecCharsetType type;
	private final byte[] map;
	
	private boolean kshifted = false;

    public NecCharsetDecoder(Charset cs, NecCharsetType type) {
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
				if (type.handleSBCS() && type.handleMBCS() && b == (type.handleJIPSE() ? 0x3F : 0x1A)) {
					if (!in.hasRemaining()) {
						return CoderResult.UNDERFLOW;
					}
					int b2 = in.get() & 0xFF;
					if (b2 == (type.handleJIPSE() ? 0x75 : 0x70)) {
						kshifted = true;
						mark += 2;
					} else if (b2 == (type.handleJIPSE() ? 0x76 : 0x71)) {
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
				} else if (type.handleMBCS() && (b >= 0x21 && b <= 0x7E) || (b >= 0xA1 && b <= 0xFE)) {
					if (!in.hasRemaining()) {
						return CoderResult.UNDERFLOW;
					}
					
					int b2 = in.get() & 0xFF;
					if (b >= 0x74 && b <= 0x7E) { // Private Use Area
						if (b2 >= 0x21 && b2 <= 0x7E) {
							out.put((char)(0xE000 + (b - 0x74) * 94 + (b2 - 0x21)));
							mark += 2;
						} else {
							return CoderResult.unmappableForLength(2);
						}
					} else if (b >= 0xE0 && b <= 0xFE) { // Private Use Area
						if (b2 >= 0xA1 && b2 <= 0xFE) {
							out.put((char)(0xE40A + (b - 0xE0) * 94 + (b2 - 0xA1)));
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

    private static float getMaxCharsPerByte(NecCharsetType type) {
		return type.handleMBCS() ? 2.0F : 1.0F;
	}
}
