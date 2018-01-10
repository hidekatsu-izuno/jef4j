package net.arnx.jef4j.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public final class ByteUtils {
	private static final String HEX = "0123456789ABCDEF";
	
	public static String hex(CharBuffer cb) {
		StringBuilder sb = new StringBuilder();
		while (cb.hasRemaining()) {
			char c = cb.get();
			sb.append(HEX.charAt((c >> 12) & 0xF));
			sb.append(HEX.charAt((c >> 8) & 0xF));
			sb.append(HEX.charAt((c >> 4) & 0xF));
			sb.append(HEX.charAt(c & 0xF));
		}
		return sb.toString();
	}
	
	public static String hex(ByteBuffer bb) {
		StringBuilder sb = new StringBuilder();
		while (bb.hasRemaining()) {
			byte b = bb.get();
			sb.append(HEX.charAt((b >> 4) & 0xF));
			sb.append(HEX.charAt(b & 0xF));
		}
		return sb.toString();
	}
	
	private ByteUtils() {
	}
}
