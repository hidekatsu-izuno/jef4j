package net.arnx.jef4j;

import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import net.arnx.jef4j.FujitsuJefCharset.SingleByteEncoding;
import net.arnx.jef4j.util.CharObjMap;

@SuppressWarnings("unchecked")
class FujitsuJefCharsetEncoder extends CharsetEncoder {
	private static final byte[] CTRL_MAP = new byte[] {
		0x00, 0x01, 0x02, 0x03, 0x37, 0x2D, 0x00, 0x2F,
		0x16, 0x05, 0x15, 0x0B, 0x0C, 0x0D, 0x00, 0x00,
		0x10, 0x00, 0x00, 0x00, 0x00, 0x3D, 0x32, 0x26,
		0x00, 0x19, 0x3F, 0x27, 0x00, 0x00, 0x00, 0x00, 
		0x40
	};
	private static final byte[] ASCII_MAP = new byte[] {
		0x4F, 0x7F, 0x7B, 0x5B, 0x6C, 0x50, 0x7D, 
		0x4D, 0x5D, 0x5C, 0x4E, 0x6B, 0x60, 0x4B, 0x61, 
		(byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3, (byte)0xF4, (byte)0xF5, (byte)0xF6, (byte)0xF7, 
		(byte)0xF8, (byte)0xF9, 0x7A, 0x5E, 0x4C, 0x7E, 0x6E, 0x6F,
		0x7C, (byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC7, 
		(byte)0xC8, (byte)0xC9, (byte)0xD1, (byte)0xD2, (byte)0xD3, (byte)0xD4, (byte)0xD5, (byte)0xD6,
		(byte)0xD7, (byte)0xD8, (byte)0xD9, (byte)0xE2, (byte)0xE3, (byte)0xE4, (byte)0xE5, (byte)0xE6,
		(byte)0xE7, (byte)0xE8, (byte)0xE9, (byte)0x4A, (byte)0xE0, (byte)0x5A, 0x5F, 0x6D,
		0x79, (byte)0x81, (byte)0x82, (byte)0x83, (byte)0x84, (byte)0x85, (byte)0x86, (byte)0x87,
		(byte)0x88, (byte)0x89, (byte)0x91, (byte)0x92, (byte)0x93, (byte)0x94, (byte)0x95, (byte)0x96,
		(byte)0x97, (byte)0x98, (byte)0x99, (byte)0xA2, (byte)0xA3, (byte)0xA4, (byte)0xA5, (byte)0xA6,
		(byte)0xA7, (byte)0xA8, (byte)0xA9, (byte)0xC0, (byte)0x6A, (byte)0xD0, (byte)0xA1, 0x00
	};
	private static final byte[] EBCDIC_MAP = new byte[] {
		0x5A, 0x7F, 0x7B, (byte)0xE0, 0x6C, 0x50, 0x7D,
		0x4D, 0x5D, 0x5C, 0x4E, 0x6B, 0x60, 0x4B, 0x61,
		(byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3, (byte)0xF4, (byte)0xF5, (byte)0xF6, (byte)0xF7, 
		(byte)0xF8, (byte)0xF9, 0x7A, 0x5E, 0x4C, 0x7E, 0x6E, 0x6F,
		0x7C, (byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC7, 		
		(byte)0xC8, (byte)0xC9, (byte)0xD1, (byte)0xD2, (byte)0xD3, (byte)0xD4, (byte)0xD5, (byte)0xD6,
		(byte)0xD7, (byte)0xD8, (byte)0xD9, (byte)0xE2, (byte)0xE3, (byte)0xE4, (byte)0xE5, (byte)0xE6,
		(byte)0xE7, (byte)0xE8, (byte)0xE9, 0x00, 0x5B, 0x00, 0x00, 0x6D,
		0x79, (byte)0x81, (byte)0x82, (byte)0x83, (byte)0x84, (byte)0x85, (byte)0x86, (byte)0x87,
		(byte)0x88, (byte)0x89, (byte)0x91, (byte)0x92, (byte)0x93, (byte)0x94, (byte)0x95, (byte)0x96,
		(byte)0x97, (byte)0x98, (byte)0x99, (byte)0xA2, (byte)0xA3, (byte)0xA4, (byte)0xA5, (byte)0xA6,
		(byte)0xA7, (byte)0xA8, (byte)0xA9, (byte)0xC0, 0x4F, (byte)0xD0, (byte)0xA1, 0x00 
	};
	private static final byte[] EBCDIK_MAP = new byte[] {
		0x5A, 0x7F, 0x7B, (byte)0xE0, 0x6C, 0x50, 0x7D,
		0x4D, 0x5D, 0x5C, 0x4E, 0x6B, 0x60, 0x4B, 0x61,
		(byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3, (byte)0xF4, (byte)0xF5, (byte)0xF6, (byte)0xF7, 
		(byte)0xF8, (byte)0xF9, 0x7A, 0x5E, 0x4C, 0x7E, 0x6E, 0x6F,
		0x7C, (byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC7, 		
		(byte)0xC8, (byte)0xC9, (byte)0xD1, (byte)0xD2, (byte)0xD3, (byte)0xD4, (byte)0xD5, (byte)0xD6,
		(byte)0xD7, (byte)0xD8, (byte)0xD9, (byte)0xE2, (byte)0xE3, (byte)0xE4, (byte)0xE5, (byte)0xE6,
		(byte)0xE7, (byte)0xE8, (byte)0xE9, 0x00, 0x5B, 0x00, 0x00, 0x6D,
		0x79, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, (byte)0xC0, 0x4F, (byte)0xD0, (byte)0xA1, 0x00 
	};
	private static final byte[] KANA_MAP = new byte[] {
		0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 
		0x48, 0x49, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 
		0x58, (byte)0x81, (byte)0x82, (byte)0x83, (byte)0x84, (byte)0x85, (byte)0x86, (byte)0x86, 
		(byte)0x87, (byte)0x88, (byte)0x89, (byte)0x8A, (byte)0x8C, (byte)0x8D, (byte)0x8E, (byte)0x8F, 
		(byte)0x90, (byte)0x91, (byte)0x92, (byte)0x93, (byte)0x94, (byte)0x95, (byte)0x96, (byte)0x96, 
		(byte)0x97, (byte)0x98, (byte)0x99, (byte)0x9A, (byte)0x9D, (byte)0x9E, (byte)0x9F, (byte)0xA2,
		(byte)0xA3, (byte)0xA4, (byte)0xA5, (byte)0xA6, (byte)0xA7, (byte)0xA8, (byte)0xA9, (byte)0xAA,
		(byte)0xAC, (byte)0xAD, (byte)0xAE, (byte)0xAF, (byte)0xBA, (byte)0xBB, (byte)0xBC, (byte)0xBD, 
		(byte)0xBE, (byte)0xBF
	};
	
	private static final CharObjMap<char[]> JEF_MAP;
	
	static {
		try (ObjectInputStream in = new ObjectInputStream(
				FujitsuJefCharsetEncoder.class.getResourceAsStream("net/arnx/jef4j/JefMap.dat"))) {
			JEF_MAP = (CharObjMap<char[]>)in.readObject();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private final SingleByteEncoding encoding;
	
	private boolean shiftin = false;

	public FujitsuJefCharsetEncoder(Charset cs, SingleByteEncoding encoding) {
		super(cs, 2, 2, getReplacementChar(encoding));
		this.encoding = encoding;
	}
	
	private static byte[] getReplacementChar(SingleByteEncoding encoding) {
		if (encoding == SingleByteEncoding.NONE) {
			return new byte[] { (byte)0xA1, (byte)0xA9 };
		}
		return new byte[] { 0x6F };
	}
	
	@Override
	protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
		int mark = in.position();
		try {
			while (in.hasRemaining()) {
				char c = in.get();
				if (Character.isSurrogate(c)) {
					if (Character.isHighSurrogate(c)) {
						if (!in.hasRemaining()) {
							return CoderResult.UNDERFLOW;
						}
						char c2 = in.get();
						if (Character.isLowSurrogate(c2)) {
							char mc = '\uFFFE';
							if (c == '\uD83C' && c2 == '\uDD00') {
								mc = '\u77A9';
							}
							
							if (mc != '\uFFFE') {
								if (encoding != SingleByteEncoding.NONE && !shiftin) {
									if (!out.hasRemaining()) {
										return CoderResult.OVERFLOW;
									}
									out.put((byte)0x29);
									shiftin = true;
								}
								
								if (out.remaining() < 2) {
									return CoderResult.OVERFLOW;
								}
								out.put((byte)((mc >> 8) & 0xFF));
								out.put((byte)(mc & 0xFF));
								mark++;
								continue;
							}
							return CoderResult.unmappableForLength(2);
						} else {
							return CoderResult.malformedForLength(1);
						}
					} else {
						return CoderResult.malformedForLength(1);
					}
				} else if (c >= '\uFFFE') {
					return CoderResult.unmappableForLength(1);
				} else if (c <= '\u007F' || c == '\u00A3' || c == '\u00A6' || c == '\u00AC') { // Single Byte
					if (encoding == SingleByteEncoding.NONE) {
						return CoderResult.unmappableForLength(1);
					}
					if (shiftin) {
						if (!out.hasRemaining()) {
							return CoderResult.OVERFLOW;
						}
						out.put((byte)0x28);
						shiftin = false;
					}
					
					byte b = 0;
					if (c <= '\u0020') {
						b = CTRL_MAP[c];
					} else {
						switch (encoding) {
						case ASCII:
							if (c <= '\u007F') {
								b = ASCII_MAP[c - 0x21];
							}
							break;
						case EBCDIC:
							if (c <= '\u007F') {
								b = EBCDIC_MAP[c - 0x21];
							} else if (c == '\u00A3') {
								b = (byte)0x4A;
							} else if (c == '\u00A6') {
								b = (byte)0x6A;
							} else if (c == '\u00AC') {
								b = (byte)0x5F;
							}
							break;
						case EBCDIK:
							if (c <= '\u007F') {
								b = EBCDIK_MAP[c - 0x21];
							} else if (c == '\u00A3') {
								b = (byte)0x4A;
							} else if (c == '\u00AC') {
								b = (byte)0x5F;
							}
							break;
						default:
							return CoderResult.unmappableForLength(1);
						}
					}
					if (b == 0 && c != '\0') {
						return CoderResult.unmappableForLength(1);
					}

					if (!out.hasRemaining()) {
						return CoderResult.OVERFLOW;
					}
					out.put((byte)b);
					mark++;
				} else if (c >= '\uFF61' && c <= '\uFF9F') { // Harfwidth katakana
					if (encoding != SingleByteEncoding.EBCDIK) {
						return CoderResult.unmappableForLength(1);
					}
					if (shiftin) {
						if (!out.hasRemaining()) {
							return CoderResult.OVERFLOW;
						}
						out.put((byte)0x28);
						shiftin = false;
					}
					
					byte b = KANA_MAP[c - 0xFF61];
					if (b == 0) {
						return CoderResult.unmappableForLength(1);
					}
					if (!out.hasRemaining()) {
						return CoderResult.OVERFLOW;
					}
					out.put(b);
					mark++;
				} else { // Double Bytes
					char[] pattern = JEF_MAP.get((char)(c & 0xFFF0));
					if (pattern != null) {
						int pos = c & 0xF;
						if ((pattern[0] & (char)(1 << (16 - pos))) > 0) {
							char mc = pattern[Integer.bitCount(pattern[0] >> (15 - pos))];
							
							if (encoding != SingleByteEncoding.NONE && !shiftin) {
								if (!out.hasRemaining()) {
									return CoderResult.OVERFLOW;
								}
								out.put((byte)0x29);
								shiftin = true;
							}
							
							if (out.remaining() < 2) {
								return CoderResult.OVERFLOW;
							}
							out.put((byte)((mc >> 8) & 0xFF));
							out.put((byte)(mc & 0xFF));
							mark++;
							continue;
						}
					}
					
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
		if (encoding != SingleByteEncoding.NONE && shiftin) {
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
}
