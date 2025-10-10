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

enum NecCharsetType {
	EBCDIK("x-NEC-EBCDIK", false),
	JIPS_J("x-NEC-JIPS-J", false),
	JIPS_E("x-NEC-JIPS-E", false),
	JIPS_J_JIS8("x-NEC-JIPS(J)-JIS8", true),
	JIPS_J_EBCDIK("x-NEC-JIPS(J)-EBCDIK", true),
	JIPS_E_EBCDIK("x-NEC-JIPS(E)-EBCDIK", true);
	
	private final String charsetName;
	private final boolean handleShift;
	
	NecCharsetType(
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
