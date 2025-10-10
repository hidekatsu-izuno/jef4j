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

enum HitachiCharsetType {
	EBCDIC("x-Hitachi-EBCDIC", false),
	EBCDIK("x-Hitachi-EBCDIK", false),
	KEIS78("x-Hitachi-KEIS78", false),
	KEIS83("x-Hitachi-KEIS83", false),
	KEIS78_EBCDIC("x-Hitachi-KEIS78-EBCDIC", true),
	KEIS78_EBCDIK("x-Hitachi-KEIS78-EBCDIK", true),
	KEIS83_EBCDIC("x-Hitachi-KEIS83-EBCDIC", true),
	KEIS83_EBCDIK("x-Hitachi-KEIS83-EBCDIK", true);
	
	private final String charsetName;
	private final boolean handleShift;
	
	HitachiCharsetType(
		String charsetName, 
		boolean handleShift
	) {
		this.charsetName = charsetName;
		this.handleShift = handleShift;
	}
	
	public String getCharsetName() {
		return charsetName;
	}

	boolean handleShift() {
		return handleShift;
	}
}
