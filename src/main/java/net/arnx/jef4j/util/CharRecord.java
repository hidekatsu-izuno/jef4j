package net.arnx.jef4j.util;
import java.io.Serializable;

public class CharRecord implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private char pattern;
	private char[] array;
	
	public CharRecord() {
	}
	
	public CharRecord(char pattern, char[] array) {
		set(pattern, array);
	}
	
	public void set(char pattern, char[] array) {
		this.pattern = pattern;
		this.array = array;
	}
	
	public boolean exists(int pos) {
		if (pos == '\uFFFF') {
			return true;
		}
		return (pattern & (char)(1 << (15 - pos))) != 0;
	}
	
	public char get(int pos) {
		if (pos == '\uFFFF') {
			return array[pos];
		}
		return array[Integer.bitCount(pattern >> (16 - pos))];
	}
	
	public int size() {
		return 16;
	}
}
