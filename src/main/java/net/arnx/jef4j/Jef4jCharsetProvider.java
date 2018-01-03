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
package net.arnx.jef4j;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Jef4jCharsetProvider extends CharsetProvider {
	private final ConcurrentMap<String, Charset> map = new ConcurrentHashMap<>();
	
	public Jef4jCharsetProvider() {
	}

	@Override
	public Iterator<Charset> charsets() {
		return map.values().iterator();
	}

	@Override
	public Charset charsetForName(String charsetName) {
		return map.computeIfAbsent(charsetName, cn -> {
			for (FujitsuCharsetType type : FujitsuCharsetType.values()) {
				if (type.getCharsetName().equalsIgnoreCase(cn)) {
					return new FujitsuCharset(type);
				}
			}
			return null;
		});
	}
}
