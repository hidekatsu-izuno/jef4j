package net.arnx.jef4j.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class CharRecord implements Externalizable {
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
		return (pattern & (char)(1 << (16 - pos))) != 0;
	}
	
	public char get(int pos) {
		return array[Integer.bitCount(pattern >> (16 - pos - 1))];
	}
	
	public int size() {
		return 16;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeChar(pattern);
		out.writeObject(array);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		pattern = in.readChar();
		array = (char[])in.readObject();
	}

}
