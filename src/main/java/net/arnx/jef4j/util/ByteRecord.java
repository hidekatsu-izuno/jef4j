package net.arnx.jef4j.util;

import java.io.Serializable;

public class ByteRecord implements Serializable {
	private static final long serialVersionUID = -8979025926760081050L;
	
	private final char pattern;
	private final byte[] array;
	
	public ByteRecord(char pattern, byte[] array) {
		this.pattern = pattern;
		this.array = array;
	}
	
	public boolean exists(int pos) {
		return (pattern & (char)(1 << (16 - pos))) != 0;
	}
	
	public byte get(int pos) {
		return array[Integer.bitCount(pattern >> (16 - pos - 1))];
	}
	
	public int size() {
		return 16;
	}
}
