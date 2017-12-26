package net.arnx.jef4j.util;

import java.io.Serializable;

public class CharRecord implements Serializable {
	private static final long serialVersionUID = 3694134637170525319L;
	
	private final char pattern;
	private final char[] array;
	
	public CharRecord(char pattern, char[] array) {
		this.pattern = pattern;
		this.array = array;
	}
	
	public boolean exists(int pos) {
		return (pattern & (char)(1 << (16 - pos))) != 0;
	}
	
	public char get(int pos) {
		return array[Integer.bitCount(pattern >> (16 - pos - 1))];
	}
	
	public int size() {
		return 16;
	}
}
