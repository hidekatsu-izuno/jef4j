package net.arnx.jef4j.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ByteRecord implements Externalizable {
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
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeChar(pattern);
		out.writeObject(array);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		pattern = in.readChar();
		array = (byte[])in.readObject();
	}
}
