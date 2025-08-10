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
import java.util.Arrays;

public class IntRecord implements Record, Serializable {
	private static final long serialVersionUID = 1L;

	private char pattern;
	private int[] array;
	
	public IntRecord() {
	}
	
	public IntRecord(char pattern, int[] array) {
		set(pattern, array);
	}
	
	public void set(char pattern, int[] array) {
		this.pattern = pattern;
		this.array = array;
	}
	
	public boolean exists(int pos) {
		if (pattern == '\uFFFF') {
			return true;
		}
		return (pattern & (char)(1 << (15 - pos))) != 0;
	}
	
	public long get(int pos) {
		if (pattern == '\uFFFF') {
			return array[pos];
		}
		return array[Integer.bitCount(pattern >> (16 - pos))];
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + pattern;
		result = prime * result + Arrays.hashCode(array);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IntRecord other = (IntRecord)obj;
		if (pattern != other.pattern) {
			return false;
		}
		if (!Arrays.equals(array, other.array)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 16; i++) {
			if (i > 0) {
				sb.append(",");
			}
			if (exists(i)) {
				sb.append(ByteUtils.hex(get(i), 4));
			} else {
				sb.append("    ");
			}
		}
		return sb.toString();
	}
}
