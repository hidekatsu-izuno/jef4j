package net.arnx.jef4j;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import net.arnx.jef4j.FujitsuJefCharset.SingleByteEncoding;

class FujitsuJefCharsetDecoder extends CharsetDecoder {
	private static final String CTRL_MAP = ""
			+ "";
	
	private static final String ASCII_MAP = ""
			+ "";
	
	private static final String EBCDIC_MAP = ""
			+ "";
	
	private static final String EBCDIK_MAP = ""
			+ "";
	
	private static final String[][][] JEF_MAP = new String[][][] {
		
	};
	
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
				
				if (!out.hasRemaining()) {
					return CoderResult.OVERFLOW;
				}
				
				if (!shiftin && encoding == SingleByteEncoding.NONE) {
					char c = '\uFFFD';
					if (b <= 0x40) {
						c = CTRL_MAP.charAt(b);
					} else {
						switch (encoding) {
						case ASCII:
							c = ASCII_MAP.charAt(b - 0x41);
							break;
						case EBCDIC:
							c = EBCDIC_MAP.charAt(b - 0x41);
							break;
						case EBCDIK:
							c = EBCDIK_MAP.charAt(b - 0x41);
							break;
						default:
							return CoderResult.unmappableForLength(1);
						}
					}
					if (c == '\uFFFD') {
						return CoderResult.unmappableForLength(1);
					}
					
					out.put(c);
					mark++;
				} else if (b >= 0x40 && b <= 0xFE) {
					if (!in.hasRemaining()) {
						return CoderResult.UNDERFLOW;
					}
					
					int b2 = in.get() & 0xFF;
					if (b2 >= 0xA1 && b2 <= 0xFE) {
						int n1 = ((b - 0x40) >> 4) & 0xF;
						int n2 = (b - 0x40) & 0xF;
						int n3 = ((b2 - 0xA1) >> 4) & 0xF;
						int n4 = (b2 - 0xA1) & 0xF;
						
						String[][] m2 = JEF_MAP[n1];
						if (m2 != null && n2 < m2.length) {
							String[] m3 = m2[n2];
							if (m3 != null && n3 < m3.length) {
								String m4 = m3[n3];
								if (m4 != null && n4 < m4.length()) {
									char mc = m4.charAt(n4);
									if (mc != '\uFFFE') {
										if (out.remaining() < 2) {
											return CoderResult.OVERFLOW;
										}
										out.put(mc);
										mark++;
										continue;
									}
								}
							}
						}
					} else {
						return CoderResult.malformedForLength(1);
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
