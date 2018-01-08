/*
 * Copyright 2018 Hidekatsu Izuno <hidekatsu.izuno@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.arnx.jef4j.util;

import java.io.Serializable;

/**
 * Based on https://github.com/mikvor/hashmapTest
 * This code is licensed by The Unlicense (http://unlicense.org)
 */
public class IntObjMap<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	private int[] keys;
	private T[] values;
	private int size;

	private int mask;
	private float fillFactor;
	private int threshold;
	
	private T zeroValue;

	public IntObjMap() {
		this(16, 0.75F);
	}
	
	public IntObjMap(int size) {
		this(size, 0.75F);
	}
	
	public IntObjMap(int size, float loadFactor) {
		if (size <= 0) {
			throw new IllegalArgumentException("size must be positive!");
		}
		if (loadFactor <= 0.0F || loadFactor >= 1.0F) {
			throw new IllegalArgumentException("loadFactor must be in (0, 1)");
		}
		int capacity = calcCapacity(size, loadFactor);
		this.keys = new int[capacity];
		this.values = newArray(keys.length);
		this.mask = capacity - 1;
		this.fillFactor = loadFactor;
		this.threshold = (int) (capacity * loadFactor);
	}

	public T put(int key, T value) {
		if (key == 0) {
			T oldValue = zeroValue;
			zeroValue = value;
			return oldValue;
		}

		int ptr = hash(key);
		do {
			int k = keys[ptr];
			if (k == 0) {
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

	public T get(int key) {
		if (key == 0) {
			return zeroValue;
		}
		
		int pos = hash(key);
		do {
			int k = keys[pos];
			if (k == 0) {
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
		long x = (long) Math.ceil(size / f);
		if (x != 0) {
			x--;
			x |= x >> 1;
			x |= x >> 2;
			x |= x >> 4;
			x |= x >> 8;
			x |= x >> 16;
			x |= x >> 32;
			x++;
			if (x > (1 << 30)) {
				throw new IllegalArgumentException(
						"Too large (" + size + " expected elements with load factor " + f + ")");
			}
		}
		return Math.max(2, (int) x);
	}

	private int hash(int x) {
		int h = x * 0x9E3779B9;
		return ((h ^ (h >> 16)) & this.mask);
	}

	private void rehash(int newCapacity) {
		this.threshold = (int) (newCapacity * this.fillFactor);
		this.mask = newCapacity - 1;

		int[] oldKeys = this.keys;
		T[] oldValues = this.values;

		this.keys = new int[newCapacity];
		this.values = newArray(newCapacity);
		this.size = 0;

		for (int i = 0; i < oldKeys.length; i++) {
			int oldKey = oldKeys[i];
			if (oldKey != 0) {
				put(oldKey, oldValues[i]);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T[] newArray(int size) {
		return (T[]) new Object[size];
	}
}
