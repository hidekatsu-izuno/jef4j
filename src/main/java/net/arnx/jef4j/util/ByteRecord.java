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
		return (pattern & (char)(1 << (16 - pos))) != 0;
	}
	
	public byte get(int pos) {
		return array[Integer.bitCount(pattern >> (16 - pos - 1))];
	}
	
	public int size() {
		return 16;
	}
}
