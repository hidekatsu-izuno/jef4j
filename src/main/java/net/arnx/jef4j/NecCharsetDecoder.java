package net.arnx.jef4j;

import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.List;

import net.arnx.jef4j.util.LongObjMap;
import net.arnx.jef4j.util.Record;

@SuppressWarnings("unchecked")
public class NecCharsetDecoder extends CharsetDecoder {
	private static final byte[] EBCDIK_JIS8_MAP = new byte[] {
		(byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x9C, (byte)0x09, (byte)0x86, (byte)0x7F, // 00-07
		(byte)0x97, (byte)0x8D, (byte)0x8E, (byte)0x0B, (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F, // 08-0F
		(byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13, (byte)0x9D, (byte)0x0A, (byte)0x08, (byte)0x87, // 10-17
		(byte)0x18, (byte)0x19, (byte)0x92, (byte)0x8F, (byte)0x1C, (byte)0x1D, (byte)0x1E, (byte)0x1F, // 18-1F
		(byte)0x80, (byte)0x81, (byte)0x82, (byte)0x83, (byte)0x84, (byte)0x85, (byte)0x17, (byte)0x1B, // 20-27
		(byte)0x88, (byte)0x89, (byte)0x8A, (byte)0x8B, (byte)0x8C, (byte)0x05, (byte)0x06, (byte)0x07, // 28-2F
		(byte)0x90, (byte)0x91, (byte)0x16, (byte)0x93, (byte)0x94, (byte)0x95, (byte)0x96, (byte)0x04, // 30-37
		(byte)0x98, (byte)0x99, (byte)0x9A, (byte)0x9B, (byte)0x14, (byte)0x15, (byte)0x9E, (byte)0x1A, // 38-3F
		(byte)0x20, (byte)0xA0, (byte)0xA1, (byte)0xA2, (byte)0xA3, (byte)0xA4, (byte)0xA5, (byte)0xA6, // 40-47
		(byte)0xA7, (byte)0xA8, (byte)0x5B, (byte)0x2E, (byte)0x3C, (byte)0x28, (byte)0x2B, (byte)0x21, // 48-4F
		(byte)0x26, (byte)0xA9, (byte)0xAA, (byte)0xAB, (byte)0xAC, (byte)0xAD, (byte)0xAE, (byte)0xAF, // 50-57
		(byte)0xB0, (byte)0xB1, (byte)0x5D, (byte)0x5C, (byte)0x2A, (byte)0x29, (byte)0x3B, (byte)0x5E, // 58-5F
		(byte)0x2D, (byte)0x2F, (byte)0xB2, (byte)0xB3, (byte)0xB4, (byte)0xB5, (byte)0xB6, (byte)0xB7, // 60-67
		(byte)0xB8, (byte)0xB9, (byte)0x7C, (byte)0x2C, (byte)0x25, (byte)0x5F, (byte)0x3E, (byte)0x3F, // 68-6F
		(byte)0xBA, (byte)0xBB, (byte)0xBC, (byte)0xBD, (byte)0xBE, (byte)0xBF, (byte)0xC0, (byte)0xC1, // 70-77
		(byte)0xC2, (byte)0x60, (byte)0x3A, (byte)0x23, (byte)0x40, (byte)0x27, (byte)0x3D, (byte)0x22, // 78-7F
		(byte)0xC3, (byte)0x61, (byte)0x62, (byte)0x63, (byte)0x64, (byte)0x65, (byte)0x66, (byte)0x67, // 80-87
		(byte)0x68, (byte)0x69, (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC7, (byte)0xC8, (byte)0xC9, // 88-8F
		(byte)0xCA, (byte)0x6A, (byte)0x6B, (byte)0x6C, (byte)0x6D, (byte)0x6E, (byte)0x6F, (byte)0x70, // 90-97
		(byte)0x71, (byte)0x72, (byte)0xCB, (byte)0xCC, (byte)0xCD, (byte)0xCE, (byte)0xCF, (byte)0xD0, // 98-9F
		(byte)0xD1, (byte)0x7E, (byte)0x73, (byte)0x74, (byte)0x75, (byte)0x76, (byte)0x77, (byte)0x78, // A0-A7
		(byte)0x79, (byte)0x7A, (byte)0xD2, (byte)0xD3, (byte)0xD4, (byte)0xD5, (byte)0xD6, (byte)0xD7, // A8-AF
		(byte)0xD8, (byte)0xD9, (byte)0xDA, (byte)0xDB, (byte)0xDC, (byte)0xDD, (byte)0xDE, (byte)0xDF, // B0-B7
		(byte)0xE0, (byte)0xE1, (byte)0xE2, (byte)0xE3, (byte)0xE4, (byte)0xE5, (byte)0xE6, (byte)0xE7, // B8-BF
		(byte)0x7B, (byte)0x41, (byte)0x42, (byte)0x43, (byte)0x44, (byte)0x45, (byte)0x46, (byte)0x47, // C0-C7
		(byte)0x48, (byte)0x49, (byte)0xE8, (byte)0xE9, (byte)0xEA, (byte)0xEB, (byte)0xEC, (byte)0xED, // C8-CF
		(byte)0x7D, (byte)0x4A, (byte)0x4B, (byte)0x4C, (byte)0x4D, (byte)0x4E, (byte)0x4F, (byte)0x50, // D0-D7
		(byte)0x51, (byte)0x52, (byte)0xEE, (byte)0xEF, (byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3, // D8-DF
		(byte)0x24, (byte)0x9F, (byte)0x53, (byte)0x54, (byte)0x55, (byte)0x56, (byte)0x57, (byte)0x58, // E0-E7
		(byte)0x59, (byte)0x5A, (byte)0xF4, (byte)0xF5, (byte)0xF6, (byte)0xF7, (byte)0xF8, (byte)0xF9, // E8-EF
		(byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37, // F0-F7
		(byte)0x38, (byte)0x39, (byte)0xFA, (byte)0xFB, (byte)0xFC, (byte)0xFD, (byte)0xFE, (byte)0xFF  // F8-FF
	};

	private static final List<byte[]> SBCS_MAP = new ArrayList<>();
	private static final List<LongObjMap<Record[]>> MBCS_MAP = new ArrayList<>();

	static {
		try (ObjectInputStream in = new ObjectInputStream(
				NecCharsetDecoder.class.getResourceAsStream("NecDecodeMap.dat"))) {
			SBCS_MAP.add((byte[])in.readObject());
			SBCS_MAP.add((byte[])in.readObject());
			MBCS_MAP.add((LongObjMap<Record[]>)in.readObject());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

    private final NecCharsetType type;
	private final byte[] map;
	private final LongObjMap<Record[]> mmap;
	
	private boolean kshifted = false;

    public NecCharsetDecoder(Charset cs, NecCharsetType type) {
		super(cs, 1, getMaxCharsPerByte(type));
		this.type = type;
		int sbcsTableNo = type.getSBCSTableNo();
		this.map = (sbcsTableNo != -1) ? SBCS_MAP.get(sbcsTableNo) : null;
		int mbcsTableNo = type.getMBCSTableNo();
		this.mmap = (mbcsTableNo != -1) ? MBCS_MAP.get(0) : null;
	}

	@Override
	protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
		int mark = in.position();
		try {
			while (in.hasRemaining()) {
				int b = in.get() & 0xFF;
				if (type.getSBCSTableNo() != -1 && type.getMBCSTableNo() != -1
					&& b == (type.getMBCSTableNo() == 1 ? 0x3F : 0x1A)) {
					if (!in.hasRemaining()) {
						return CoderResult.UNDERFLOW;
					}
					int b2 = in.get() & 0xFF;
					if (b2 == (type.getMBCSTableNo() == 1 ? 0x75 : 0x70)) {
						kshifted = true;
						mark += 2;
						continue;
					} else if (b2 == (type.getMBCSTableNo() == 1 ? 0x76 : 0x71)) {
						kshifted = false;
						mark += 2;
						continue;
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
				} else if (type.getMBCSTableNo() != -1 && (b >= 0x21 && b <= 0x7E) || (b >= 0xA1 && b <= 0xFE)) {
					if (!in.hasRemaining()) {
						return CoderResult.UNDERFLOW;
					}
					
					int b2 = in.get() & 0xFF;
					if (type.getMBCSTableNo() == 1) {
						b = EBCDIK_JIS8_MAP[b & 0xFF];
						b2 = EBCDIK_JIS8_MAP[b2 & 0xFF];
					}

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
						Record[] records = mmap.get((b << 8) | (b2 & 0xF0));
						Record record = records != null ? records[Math.max(type.getIVSTableNo(), 0)] : null;
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
							if (type.getIVSTableNo() != -1) {
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
		return type.getMBCSTableNo() != -1 ? 2.0F : 1.0F;
	}
}
