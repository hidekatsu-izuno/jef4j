package net.arnx.jef4j.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public final class ByteUtils {
	private static final String HEX = "0123456789ABCDEF";
	
	public static String hex(int n, int len) {
		StringBuilder sb = new StringBuilder();
		if (len > 7) {
			sb.append(HEX.charAt((n >> 28) & 0xF));
		}
		if (len > 6) {
			sb.append(HEX.charAt((n >> 24) & 0xF));
		}
		if (len > 5) {
			sb.append(HEX.charAt((n >> 20) & 0xF));
		}
		if (len > 4) {
			sb.append(HEX.charAt((n >> 16) & 0xF));
		}
		if (len > 3) {
			sb.append(HEX.charAt((n >> 12) & 0xF));
		}
		if (len > 2) {
			sb.append(HEX.charAt((n >> 8) & 0xF));
		}
		if (len > 1) {
			sb.append(HEX.charAt((n >> 4) & 0xF));
		}
		if (len > 0) {
			sb.append(HEX.charAt(n & 0xF));
		}
		return sb.toString();
	}
	
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
