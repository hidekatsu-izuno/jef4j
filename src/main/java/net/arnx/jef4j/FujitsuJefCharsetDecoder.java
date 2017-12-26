package net.arnx.jef4j;

import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import net.arnx.jef4j.FujitsuJefCharset.SingleByteEncoding;
import net.arnx.jef4j.util.CharObjMap;

@SuppressWarnings("unchecked")
class FujitsuJefCharsetDecoder extends CharsetDecoder {
	private static final CharObjMap<char[]> ASCII_MAP;
	private static final CharObjMap<char[]> EBCDIC_MAP;
	private static final CharObjMap<char[]> EBCDIK_MAP;
	private static final CharObjMap<char[]> JEF_MAP;
	
	static {
		try (ObjectInputStream in = new ObjectInputStream(
				FujitsuJefCharsetEncoder.class.getResourceAsStream("net/arnx/jef4j/JefDecodeMap.dat"))) {
			ASCII_MAP = (CharObjMap<char[]>)in.readObject();
			EBCDIC_MAP = (CharObjMap<char[]>)in.readObject();
			EBCDIK_MAP = (CharObjMap<char[]>)in.readObject();
			JEF_MAP = (CharObjMap<char[]>)in.readObject();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private final SingleByteEncoding encoding;
	
	private boolean shiftin = false;
	
	protected FujitsuJefCharsetDecoder(Charset cs, SingleByteEncoding encoding) {
		super(cs, 2, 2);
		this.encoding = encoding;
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
				
				if (!shiftin && encoding == SingleByteEncoding.NONE) {
					char c = (char)b;
					char[] pattern;
					switch (encoding) {
					case ASCII:
						pattern = ASCII_MAP.get((char)(c & 0xFFF0));
						break;
					case EBCDIC:
						pattern = EBCDIC_MAP.get((char)(c & 0xFFF0));
						break;
					case EBCDIK:
						pattern = EBCDIK_MAP.get((char)(c & 0xFFF0));
						break;
					default:
						return CoderResult.unmappableForLength(1);
					}
					if (pattern == null) {
						return CoderResult.malformedForLength(1);
					}
					
					int pos = c & 0xF;
					if ((pattern[0] & (char)(1 << (16 - pos))) == 0) {
						return CoderResult.malformedForLength(1);
					}
					
					char mc = pattern[Integer.bitCount(pattern[0] >> (15 - pos))];
					
					if (!out.hasRemaining()) {
						return CoderResult.OVERFLOW;
					}
					
					out.put(mc);
					mark++;
				} else if (b >= 0x40 && b <= 0xFE) {
					if (!in.hasRemaining()) {
						return CoderResult.UNDERFLOW;
					}
					
					int b2 = in.get() & 0xFF;
					if (b == 0x77 && b2 == 0xA9) {
						if (out.remaining() < 2) {
							return CoderResult.OVERFLOW;
						}
						out.put('\uD83C');
						out.put('\uDD00');
						mark += 2;
					} else if (b2 >= 0xA1 && b2 <= 0xFE) {
						char c = (char)(b2 << 8 | b2);
						char[] pattern = JEF_MAP.get((char)(c & 0xFFF0));
						if (pattern == null) {
							return CoderResult.malformedForLength(2);
						}
						
						int pos = c & 0xF;
						if ((pattern[0] & (char)(1 << (16 - pos))) == 0) {
							return CoderResult.malformedForLength(2);
						}
						
						char mc = pattern[Integer.bitCount(pattern[0] >> (15 - pos))];
						
						if (!out.hasRemaining()) {
							return CoderResult.OVERFLOW;
						}
						
						out.put(mc);
						mark += 2;
					} else {
						return CoderResult.malformedForLength(2);
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
}
