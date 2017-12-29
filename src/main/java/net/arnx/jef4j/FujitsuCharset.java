package net.arnx.jef4j;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class FujitsuCharset extends Charset {
	private FujitsuCharsetType type;
	
	protected FujitsuCharset(FujitsuCharsetType type) {
		super(type.getCharsetName(), new String[0]);
		this.type = type;
	}

	@Override
	public boolean contains(Charset cs) {
		return false;
	}

	@Override
	public CharsetDecoder newDecoder() {
		return new FujitsuCharsetDecoder(this, type);
	}

	@Override
	public CharsetEncoder newEncoder() {
		return new FujitsuCharsetEncoder(this, type);
	}
}
