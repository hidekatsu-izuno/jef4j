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
	JIS8("x-NEC-JIS8", 0, -1, -1),
	EBCDIK("x-NEC-EBCDIK", 1, -1, -1),
	JIPSJ("x-NEC-JIPSJ", -1, 0, -1),
	JIPSJ_HD("x-NEC-JIPSJ-HanyoDenshi", -1, 0, 0),
	JIPSJ_AJ1("x-NEC-JIPSJ-AdobeJapan1", -1, 0, 1),
	JIPSJ_JIS8("x-NEC-JIPSJ-JIS8", 0, 0, -1),
	JIPSJ_HD_JIS8("x-NEC-JIPSJ-HanyoDenshi-JIS8", 1, 0, 0),
	JIPSJ_AJ1_JIS8("x-NEC-JIPSJ-AdobeJapan1-JIS8",  1, 1, 1),
	JIPSE("x-NEC-JIPSE", -1, 1, -1),
	JIPSE_HD("x-NEC-JIPSJ-HanyoDenshi", -1, 1, 0),
	JIPSE_AJ1("x-NEC-JIPSJ-AdobeJapan1", -1, 1, 1),
	JIPSE_EBCDIK("x-NEC-JIPSE-EBCDIK", 1, 1, -1),
	JIPSE_HD_EBCDIK("x-NEC-JIPSE-HanyoDenshi-EBCDIK",  1, 1, 0),
	JIPSE_AJ1_EBCDIK("x-NEC-JIPSE-AdobeJapan1-EBCDIK",  1, 1, 1);
	
	private final String charsetName;
	private final int sbcsTableNo;
	private final int mbcsTableNo;
	private final int ivsTableNo;
	
	NecCharsetType(
		String charsetName, 
		int sbcsTableNo,
		int mbcsTableNo,
		int ivsTableNo
	) {
		this.charsetName = charsetName;
		this.sbcsTableNo = sbcsTableNo;
		this.mbcsTableNo = mbcsTableNo;
		this.ivsTableNo = ivsTableNo;
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
}
