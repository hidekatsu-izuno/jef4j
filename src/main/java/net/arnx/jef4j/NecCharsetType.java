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
	EBCDIK("x-NEC-EBCDIK", 0, -1, -1, false),
	JIS8("x-NEC-JIS8", 1, -1, -1, false),
	JIPSJ("x-NEC-JIPSJ", -1, 0, -1, false),
	JIPSE("x-NEC-JIPSE", -1, 0, -1, true),
	JIPSJ_EBCDIK("x-NEC-JIPSJ-EBCDIK", 0, 0, -1, false),
	JIPSJ_HD_EBCDIK("x-NEC-JIPSJ-HanyoDenshi-EBCDIK", 0, 0, 0, false),
	JIPSJ_AJ1_EBCDIK("x-NEC-JIPSJ-AdobeJapan1-EBCDIK",  0, 1, 1, false),
	JIPSJ_JIS8("x-NEC-JIPSJ-JIS8", 1, 0, -1, false),
	JIPSJ_HD_JIS8("x-NEC-JIPSJ-HanyoDenshi-JIS8", 1, 0, 0, false),
	JIPSJ_AJ1_JIS8("x-NEC-JIPSJ-AdobeJapan1-JIS8",  1, 1, 1, false),
	JIPSE_EBCDIK("x-NEC-JIPSE-EBCDIK", 0, 0, -1, false),
	JIPSE_HD_EBCDIK("x-NEC-JIPSE-HanyoDenshi-EBCDIK",  0, 0, 0, true),
	JIPSE_AJ1_EBCDIK("x-NEC-JIPSE-AdobeJapan1-EBCDIK",  0, 1, 1, true);
	
	private final String charsetName;
	private final int sbcsTableNo;
	private final int mbcsTableNo;
	private final boolean handleJIPSE;
	private final int ivsTableNo;
	
	NecCharsetType(
		String charsetName, 
		int sbcsTableNo,
		int mbcsTableNo,
		int ivsTableNo,
		boolean handleJIPSE
	) {
		this.charsetName = charsetName;
		this.sbcsTableNo = sbcsTableNo;
		this.mbcsTableNo = mbcsTableNo;
		this.ivsTableNo = ivsTableNo;
		this.handleJIPSE = handleJIPSE;
	}
	
	public String getCharsetName() {
		return charsetName;
	}

	int getSBCSTableNo() {
		return sbcsTableNo;
	}

	int getMBCSTableNo() {
		return mbcsTableNo;
	}
	
	int getIVSTableNo() {
		return ivsTableNo;
	}

	boolean handleJIPSE() {
		return handleJIPSE;
	}
}
