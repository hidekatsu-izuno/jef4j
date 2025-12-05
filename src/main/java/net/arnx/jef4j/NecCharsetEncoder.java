package net.arnx.jef4j;

import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.List;

import net.arnx.jef4j.util.LongObjMap;
import net.arnx.jef4j.util.Record;

@SuppressWarnings("unchecked")
public class NecCharsetEncoder extends CharsetEncoder {
	private static final byte[] JIS8_EBCDIC_MAP = new byte[] {
		(byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x37, (byte)0x2D, (byte)0x2E, (byte)0x2F, // 00-07
		(byte)0x16, (byte)0x05, (byte)0x15, (byte)0x0B, (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F, // 08-0F
		(byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13, (byte)0x3C, (byte)0x3D, (byte)0x32, (byte)0x26, // 10-17
		(byte)0x18, (byte)0x19, (byte)0x3F, (byte)0x27, (byte)0x1C, (byte)0x1D, (byte)0x1E, (byte)0x1F, // 18-1F
		(byte)0x40, (byte)0x4F, (byte)0x7F, (byte)0x7B, (byte)0xE0, (byte)0x6C, (byte)0x50, (byte)0x7D, // 20-27
		(byte)0x4D, (byte)0x5D, (byte)0x5C, (byte)0x4E, (byte)0x6B, (byte)0x60, (byte)0x4B, (byte)0x61, // 28-2F
		(byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3, (byte)0xF4, (byte)0xF5, (byte)0xF6, (byte)0xF7, // 30-37
		(byte)0xF8, (byte)0xF9, (byte)0x7A, (byte)0x5E, (byte)0x4C, (byte)0x7E, (byte)0x6E, (byte)0x6F, // 38-3F
		(byte)0x7C, (byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC7, // 40-47
		(byte)0xC8, (byte)0xC9, (byte)0xD1, (byte)0xD2, (byte)0xD3, (byte)0xD4, (byte)0xD5, (byte)0xD6, // 48-4F
		(byte)0xD7, (byte)0xD8, (byte)0xD9, (byte)0xE2, (byte)0xE3, (byte)0xE4, (byte)0xE5, (byte)0xE6, // 50-57
		(byte)0xE7, (byte)0xE8, (byte)0xE9, (byte)0x4A, (byte)0x5B, (byte)0x5A, (byte)0x5F, (byte)0x6D, // 58-5F
		(byte)0x79, (byte)0x81, (byte)0x82, (byte)0x83, (byte)0x84, (byte)0x85, (byte)0x86, (byte)0x87, // 60-67
		(byte)0x88, (byte)0x89, (byte)0x91, (byte)0x92, (byte)0x93, (byte)0x94, (byte)0x95, (byte)0x96, // 68-6F
		(byte)0x97, (byte)0x98, (byte)0x99, (byte)0xA2, (byte)0xA3, (byte)0xA4, (byte)0xA5, (byte)0xA6, // 70-77
		(byte)0xA7, (byte)0xA8, (byte)0xA9, (byte)0xC0, (byte)0x6A, (byte)0xD0, (byte)0xA1, (byte)0x07, // 78-7F
		(byte)0x20, (byte)0x21, (byte)0x22, (byte)0x23, (byte)0x24, (byte)0x25, (byte)0x06, (byte)0x17, // 80-87
		(byte)0x28, (byte)0x29, (byte)0x2A, (byte)0x2B, (byte)0x2C, (byte)0x09, (byte)0x0A, (byte)0x1B, // 88-8F
		(byte)0x30, (byte)0x31, (byte)0x1A, (byte)0x33, (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x08, // 90-97
		(byte)0x38, (byte)0x39, (byte)0x3A, (byte)0x3B, (byte)0x04, (byte)0x14, (byte)0x3E, (byte)0xE1, // 98-9F
		(byte)0x41, (byte)0x42, (byte)0x43, (byte)0x44, (byte)0x45, (byte)0x46, (byte)0x47, (byte)0x48, // A0-A7
		(byte)0x49, (byte)0x51, (byte)0x52, (byte)0x53, (byte)0x54, (byte)0x55, (byte)0x56, (byte)0x57, // A8-AF
		(byte)0x58, (byte)0x59, (byte)0x62, (byte)0x63, (byte)0x64, (byte)0x65, (byte)0x66, (byte)0x67, // B0-B7
		(byte)0x68, (byte)0x69, (byte)0x70, (byte)0x71, (byte)0x72, (byte)0x73, (byte)0x74, (byte)0x75, // B8-BF
		(byte)0x76, (byte)0x77, (byte)0x78, (byte)0x80, (byte)0x8A, (byte)0x8B, (byte)0x8C, (byte)0x8D, // C0-C7
		(byte)0x8E, (byte)0x8F, (byte)0x90, (byte)0x9A, (byte)0x9B, (byte)0x9C, (byte)0x9D, (byte)0x9E, // C8-CF
		(byte)0x9F, (byte)0xA0, (byte)0xAA, (byte)0xAB, (byte)0xAC, (byte)0xAD, (byte)0xAE, (byte)0xAF, // D0-D7
		(byte)0xB0, (byte)0xB1, (byte)0xB2, (byte)0xB3, (byte)0xB4, (byte)0xB5, (byte)0xB6, (byte)0xB7, // D8-DF
		(byte)0xB8, (byte)0xB9, (byte)0xBA, (byte)0xBB, (byte)0xBC, (byte)0xBD, (byte)0xBE, (byte)0xBF, // E0-E7
		(byte)0xCA, (byte)0xCB, (byte)0xCC, (byte)0xCD, (byte)0xCE, (byte)0xCF, (byte)0xDA, (byte)0xDB, // E8-EF
		(byte)0xDC, (byte)0xDD, (byte)0xDE, (byte)0xDF, (byte)0xEA, (byte)0xEB, (byte)0xEC, (byte)0xED, // F0-F7
		(byte)0xEE, (byte)0xEF, (byte)0xFA, (byte)0xFB, (byte)0xFC, (byte)0xFD, (byte)0xFE, (byte)0xFF  // F8-FF
	};

	private static final List<byte[]> SBCS_MAP = new ArrayList<>();
	private static final List<LongObjMap<Record[]>> MBCS_MAP = new ArrayList<>();
	private static final ByteBuffer DUMMY = ByteBuffer.allocate(0);

	static {
		try (ObjectInputStream in = new ObjectInputStream(
				NecCharsetEncoder.class.getResourceAsStream("NecEncodeMap.dat"))) {
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
	private StringBuilder backup;

    public NecCharsetEncoder(Charset cs, NecCharsetType type) {
		super(cs, getAverageBytesPerChar(type), getMaxBytesPerChar(type), getReplacementChar(type));
		this.type = type;
		int sbcsTableNo = type.getSBCSTableNo();
		this.map = (sbcsTableNo != -1) ? SBCS_MAP.get(sbcsTableNo) : null;
		int mbcsTableNo = type.getMBCSTableNo();
		this.mmap = (mbcsTableNo != -1) ? MBCS_MAP.get(mbcsTableNo) : null;
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
						if (type.getMBCSTableNo() == 1) {
							out.put((byte)0x3F);
							out.put((byte)0x76);
						} else {
							out.put((byte)0x1A);
							out.put((byte)0x71);
						}
						kshifted = false;
					}

					if (!out.hasRemaining()) {
						return CoderResult.OVERFLOW;
					}
					out.put(value);
					mark++;
				} else if (type.getMBCSTableNo() != -1) { // Double Bytes
					if (c >= '\uE000' && c <= '\uE409') { // Private Use Area
						byte b1 = (byte)((0x74 + (c - 0xE000) / 94) & 0xFF);
						byte b2 = (byte)((0x21 + (c - 0xE000) % 94) & 0xFF);
						if (type.getMBCSTableNo() == 1) {
							b1 = JIS8_EBCDIC_MAP[b1 & 0xFF];
							b2 = JIS8_EBCDIC_MAP[b2 & 0xFF];
						}
						out.put(b1);
						out.put(b2);
						mark++;
					} else if (c >= '\uE40A' && c <= '\uEF6B') { // Private Use Area
						byte b1 = (byte)((0xE0 + (c - 0xE40A) / 94) & 0xFF);
						byte b2 = (byte)((0xA1 + (c - 0xE40A) % 94) & 0xFF);
						if (type.getMBCSTableNo() == 1) {
							b1 = JIS8_EBCDIC_MAP[b1 & 0xFF];
							b2 = JIS8_EBCDIC_MAP[b2 & 0xFF];
						}
						out.put(b1);
						out.put(b2);
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
						
						Record[] records = mmap.get(key & 0xFFFFFFF0);
						Record record = records != null ? records[Math.max(type.getIVSTableNo(), 0)] : null;
						if (record == null || !record.exists((int)(key & 0xF))) {
							return CoderResult.unmappableForLength(1);
						}
						
						if (type.getSBCSTableNo() != -1 && type.getMBCSTableNo() != -1 && !kshifted) {
							if (out.remaining() < 2) {
								return CoderResult.OVERFLOW;
							}
							if (type.getMBCSTableNo() == 1) {
								out.put((byte)0x3F);
								out.put((byte)0x75);
							} else {
								out.put((byte)0x1A);
								out.put((byte)0x70);
							}
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
							if (c3 == '\u3099' || c3 == '\u309A') {
								long key2 = ((long)c3) << 20 | key;
								Record[] records2 = mmap.get(key2 & 0xFFFFFFFFF0L);
								Record record2 = records2 != null ? records2[Math.max(type.getIVSTableNo(), 0)] : null;
								if (record2 != null && record2.exists((int)(key2 & 0xF))) {
									mc = (char)record2.get((int)(key2 & 0xF));
									progress++;
								} else {
									in.position(mark2);
								}
							} else if (type.getIVSTableNo() != -1 && c3 == '\uDB40') {
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
										Record[] records2 = mmap.get(key2 & 0xFFFFFFFFF0L);
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

						byte b1 = (byte)((mc >> 8) & 0xFF);
						byte b2 = (byte)(mc & 0xFF);
						if (type.getMBCSTableNo() == 1) {
							b1 = JIS8_EBCDIC_MAP[b1 & 0xFF];
							b2 = JIS8_EBCDIC_MAP[b2 & 0xFF];
						}
						out.put(b1);
						out.put(b2);
						
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

		if (type.getSBCSTableNo() != -1 && type.getMBCSTableNo() != -1 && kshifted) {
			if (out.remaining() < 2) {
				return CoderResult.OVERFLOW;
			}
			if (type.getMBCSTableNo() == 1) {
				out.put((byte)0x3F);
				out.put((byte)0x76);
			} else {
				out.put((byte)0x1A);
				out.put((byte)0x71);
			}
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

	private static float getAverageBytesPerChar(NecCharsetType type) {
		float size = type.getMBCSTableNo() != -1 ? 2 : 1;
		if (type.getSBCSTableNo() != -1 && type.getMBCSTableNo() != -1) {
			size += 1;
		}
		return size;
	}
	
	private static float getMaxBytesPerChar(NecCharsetType type) {
		float size = type.getIVSTableNo() != -1 ? 4 : type.getMBCSTableNo() != -1 ? 2 : 1;
		if (type.getSBCSTableNo() != -1 && type.getMBCSTableNo() != -1) {
			size += 2;
		}
		return size;
	}
	
	private static byte[] getReplacementChar(NecCharsetType type) {
		return type.getMBCSTableNo() != -1 && type.getSBCSTableNo() == -1 ? 
			(type.getMBCSTableNo() == 1 ? new byte[] { 0x4F, 0x4F } : new byte[] { 0x21, 0x21 }) : 
			(type.getSBCSTableNo() == 1 ? new byte[] { 0x40 } : new byte[] { 0x20 });
	}
}
