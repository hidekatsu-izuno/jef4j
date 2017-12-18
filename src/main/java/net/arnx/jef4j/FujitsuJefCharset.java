package net.arnx.jef4j;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class FujitsuJefCharset extends Charset {
	public static enum SingleByteEncoding {
		NONE,
		ASCII,
		EBCDIC,
		EBCDIK
	}
	
	private SingleByteEncoding encoding;
	
	protected FujitsuJefCharset(SingleByteEncoding encoding) {
		super(createName(encoding), new String[0]);
		this.encoding = encoding;
	}
	
	private static String createName(SingleByteEncoding encoding) {
		String name = "x-Fujitsu-JEF";
		if (encoding != SingleByteEncoding.NONE) {
			name += "-" + encoding.name();
		}
		return name;
	}

	@Override
	public boolean contains(Charset cs) {
		return false;
	}

	@Override
	public CharsetDecoder newDecoder() {
		return new FujitsuJefCharsetDecoder(this, encoding);
	}

	@Override
	public CharsetEncoder newEncoder() {
		return new FujitsuJefCharsetEncoder(this, encoding);
	}
}
