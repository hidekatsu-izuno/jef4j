package net.arnx.jef4j;

import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import net.arnx.jef4j.FujitsuJefCharset.SingleByteEncoding;
import net.arnx.jef4j.util.CharObjMap;
import net.arnx.jef4j.util.CharRecord;

@SuppressWarnings("unchecked")
class FujitsuJefCharsetDecoder extends CharsetDecoder {
	private static final CharObjMap<CharRecord> ASCII_MAP;
	private static final CharObjMap<CharRecord> EBCDIC_MAP;
	private static final CharObjMap<CharRecord> EBCDIK_MAP;
	private static final CharObjMap<CharRecord> JEF_MAP;
	
	static {
		try (ObjectInputStream in = new ObjectInputStream(
				FujitsuJefCharsetEncoder.class.getResourceAsStream("JefDecodeMap.dat"))) {
			ASCII_MAP = (CharObjMap<CharRecord>)in.readObject();
			EBCDIC_MAP = (CharObjMap<CharRecord>)in.readObject();
			EBCDIK_MAP = (CharObjMap<CharRecord>)in.readObject();
			JEF_MAP = (CharObjMap<CharRecord>)in.readObject();
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
				
				if (!shiftin && encoding != SingleByteEncoding.NONE) {
					char c = (char)b;					
					CharRecord record;
					switch (encoding) {
					case ASCII:
						record = ASCII_MAP.get((char)(c & 0xFFF0));
						break;
					case EBCDIC:
						record = EBCDIC_MAP.get((char)(c & 0xFFF0));
						break;
					case EBCDIK:
						record = EBCDIK_MAP.get((char)(c & 0xFFF0));
						break;
					default:
						return CoderResult.unmappableForLength(1);
					}
					int pos = c & 0xF;
					if (record == null || !record.exists(pos)) {
						return CoderResult.malformedForLength(1);
					}
					
					if (!out.hasRemaining()) {
						return CoderResult.OVERFLOW;
					}
					out.put(record.get(pos));
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
						char c = (char)(b << 8 | b2);
						CharRecord record = JEF_MAP.get((char)(c & 0xFFF0));
						int pos = c & 0xF;
						if (record == null || !record.exists(pos)) {
							return CoderResult.malformedForLength(2);
						}
						
						if (!out.hasRemaining()) {
							return CoderResult.OVERFLOW;
						}
						out.put(record.get(pos));
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
