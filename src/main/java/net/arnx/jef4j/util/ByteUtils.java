package net.arnx.jef4j.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public final class ByteUtils {
	private static final String HEX = "0123456789ABCDEF";
	
	public static String hex(long n, int len) {
		StringBuilder sb = new StringBuilder();
		if (len > 15) {
			sb.append(HEX.charAt((int)(n >> 60) & 0xF));
		}
		if (len > 14) {
			sb.append(HEX.charAt((int)(n >> 56) & 0xF));
		}
		if (len > 13) {
			sb.append(HEX.charAt((int)(n >> 52) & 0xF));
		}
		if (len > 12) {
			sb.append(HEX.charAt((int)(n >> 48) & 0xF));
		}
		if (len > 11) {
			sb.append(HEX.charAt((int)(n >> 44) & 0xF));
		}
		if (len > 10) {
			sb.append(HEX.charAt((int)(n >> 40) & 0xF));
		}
		if (len > 9) {
			sb.append(HEX.charAt((int)(n >> 36) & 0xF));
		}
		if (len > 8) {
			sb.append(HEX.charAt((int)(n >> 32) & 0xF));
		}
		if (len > 7) {
			sb.append(HEX.charAt((int)(n >> 28) & 0xF));
		}
		if (len > 6) {
			sb.append(HEX.charAt((int)(n >> 24) & 0xF));
		}
		if (len > 5) {
			sb.append(HEX.charAt((int)(n >> 20) & 0xF));
		}
		if (len > 4) {
			sb.append(HEX.charAt((int)(n >> 16) & 0xF));
		}
		if (len > 3) {
			sb.append(HEX.charAt((int)(n >> 12) & 0xF));
		}
		if (len > 2) {
			sb.append(HEX.charAt((int)(n >> 8) & 0xF));
		}
		if (len > 1) {
			sb.append(HEX.charAt((int)(n >> 4) & 0xF));
		}
		if (len > 0) {
			sb.append(HEX.charAt((int)n & 0xF));
		}
		return sb.toString();
	}
	
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
	
	public static String hex(byte[] ba) {
		StringBuilder sb = new StringBuilder();
		for (byte b : ba) {
			sb.append(HEX.charAt((b >> 4) & 0xF));
			sb.append(HEX.charAt(b & 0xF));
		}
		return sb.toString();
	}
	
	private ByteUtils() {
	}
}
