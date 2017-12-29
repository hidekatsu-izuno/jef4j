package net.arnx.jef4j.util;

import java.io.Serializable;

/**
 * Based on https://github.com/mikvor/hashmapTest
 * 
 * This code is licensed by The Unlicense <http://unlicense.org>
 */
public class CharObjMap<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	private char[] keys;
	private T[] values;
	private int size;

	private int mask;
	private float fillFactor;
	private int threshold;
	
	private T zeroValue;

	public CharObjMap() {
		this(16, 0.75F);
	}
	
	public CharObjMap(int size) {
		this(size, 0.75F);
	}
	
	public CharObjMap(int size, float loadFactor) {
		if (size <= 0) {
			throw new IllegalArgumentException("size must be positive!");
		}
		if (loadFactor <= 0.0F || loadFactor >= 1.0F) {
			throw new IllegalArgumentException("loadFactor must be in (0, 1)");
		}
		int capacity = calcCapacity(size, loadFactor);
		this.keys = new char[capacity];
		this.values = newArray(keys.length);
		this.mask = capacity - 1;
		this.fillFactor = loadFactor;
		this.threshold = (int) (capacity * loadFactor);
	}

	public T put(char key, T value) {
		if (key == '\u0000') {
			T oldValue = zeroValue;
			zeroValue = value;
			return oldValue;
		}

		int ptr = hash(key);
		do {
			int k = keys[ptr];
			if (k == '\u0000') {
				keys[ptr] = key;
				values[ptr] = value;
				if (size >= threshold) {
					rehash(keys.length * 2);
				} else {
					size++;
				}
				return null;
			} else if (k == key) {
				T ret = values[ptr];
				values[ptr] = value;
				return ret;
			}
			ptr = (ptr + 1) & mask;
		} while (true);
	}

	public T get(char key) {
		if (key == '\u0000') {
			return zeroValue;
		}
		
		int pos = hash(key);
		do {
			char k = keys[pos];
			if (k == '\u0000') {
				return null;
			} else if (k == key) {
				return (T) values[pos];
			}
			pos = (pos + 1) & mask;
		} while (true);
	}

	public int size() {
		return size + (zeroValue != null ? 1 : 0);
	}

	private static int calcCapacity(int size, float f) {
		long capacity;
		long x = (long) Math.ceil(size / f);
		if (x == 0) {
			capacity = 2;
		} else {
			x--;
			x |= x >> 1;
			x |= x >> 2;
			x |= x >> 4;
			x |= x >> 8;
			x |= x >> 16;
			capacity = Math.max(2, (x | x >> 32) + 1);
		}
		if (capacity > (1 << 30)) {
			throw new IllegalArgumentException(
					"Too large (" + size + " expected elements with load factor " + f + ")");
		}
		return (int) capacity;
	}

	private int hash(int x) {
		int h = x * 0x9E3779B9;
		return ((h ^ (h >> 16)) & this.mask);
	}

	private void rehash(int newCapacity) {
		this.threshold = (int) (newCapacity * this.fillFactor);
		this.mask = newCapacity - 1;

		char[] oldKeys = this.keys;
		T[] oldValues = this.values;

		this.keys = new char[newCapacity];
		this.values = newArray(newCapacity);
		this.size = 0;

		for (int i = 0; i < oldKeys.length; i++) {
			char oldKey = oldKeys[i];
			if (oldKey != '\u0000') {
				put(oldKey, oldValues[i]);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T[] newArray(int size) {
		return (T[]) new Object[size];
	}
}
