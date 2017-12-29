package net.arnx.jef4j.util;

import java.io.Serializable;

public class ByteRecord implements Serializable {
	private static final long serialVersionUID = 1L;

	private char pattern;
	private byte[] array;
	
	public ByteRecord() {
	}
	
	public ByteRecord(char pattern, byte[] array) {
		set(pattern, array);
	}
	
	public void set(char pattern, byte[] array) {
		this.pattern = pattern;
		this.array = array;
	}
	
	public boolean exists(int pos) {
		if (pattern == '\uFFFF') {
			return true;
		}
		return (pattern & (char)(1 << (15 - pos))) != 0;
	}
	
	public byte get(int pos) {
		if (pattern == '\uFFFF') {
			return array[pos];
		}
		return array[Integer.bitCount(pattern >> (16 - pos))];
	}
	
	public int size() {
		return 16;
	}
}
