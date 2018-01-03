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

public class CharRecord implements Record, Serializable {
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
	
	public int get(int pos) {
		if (pos == '\uFFFF') {
			return array[pos];
		}
		return array[Integer.bitCount(pattern >> (16 - pos))];
	}
}
