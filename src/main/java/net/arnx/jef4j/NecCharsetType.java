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
	EBCDIK("x-NEC-EBCDIK", 0, -1, false, false),
	JIPSJ("x-NEC-JIPSJ", -1, 0, false, false),
	JIPSE("x-NEC-JIPSE", -1, 0, true, false),
	JIPSJ_EBCDIK("x-NEC-JIPSJ-EBCDIK", 0, 0, false, false),
	JIPSE_EBCDIK("x-NEC-JIPSE-EBCDIK", 0, 0, true, false),
	JIPSJ_HD_EBCDIK("x-NEC-JIPSJ-HanyoDenshi-EBCDIK", 0, 0, false, true),
	JIPSE_HD_EBCDIK("x-NEC-JIPSE-HanyoDenshi-EBCDIK",  0, 0, true, true),
	JIPSJ_AJ1_EBCDIK("x-NEC-JIPSJ-AdobeJapan1-EBCDIK",  0, 1, false, true),
	JIPSE_AJ1_EBCDIK("x-NEC-JIPSE-AdobeJapan1-EBCDIK",  0, 1, true, true);
	
	private final String charsetName;
	private final int sbcsTableNo;
	private final int mbcsTableNo;
	private final boolean handleJIPSE;
	private final boolean handleIVS;
	
	NecCharsetType(
		String charsetName, 
		int sbcsTableNo,
		int mbcsTableNo,
		boolean handleJIPSE,
		boolean handleIVS
	) {
		this.charsetName = charsetName;
		this.sbcsTableNo = sbcsTableNo;
		this.mbcsTableNo = mbcsTableNo;
		this.handleJIPSE = handleJIPSE;
		this.handleIVS = handleIVS;
	}
	
	public String getCharsetName() {
		return charsetName;
	}

	boolean handleSBCS() {
		return sbcsTableNo != -1;
	}
	
	boolean handleMBCS() {
		return mbcsTableNo != -1;
	}

	boolean handleJIPSE() {
		return handleJIPSE;
	}
	
	boolean handleIVS() {
		return handleIVS;
	}

	int getSBCSTableNo() {
		return sbcsTableNo;
	}

	int getMBCSTableNo() {
		return mbcsTableNo;
	}
}
