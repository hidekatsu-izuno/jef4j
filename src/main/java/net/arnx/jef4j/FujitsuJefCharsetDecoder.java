package net.arnx.jef4j;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import net.arnx.jef4j.FujitsuJefCharset.SingleByteEncoding;

class FujitsuJefCharsetDecoder extends CharsetDecoder {
	protected FujitsuJefCharsetDecoder(Charset cs, SingleByteEncoding type) {
		super(cs, 2, 2);
	}

	@Override
	protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
		return null;
	}
}
