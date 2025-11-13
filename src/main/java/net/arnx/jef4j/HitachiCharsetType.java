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
	EBCDIC("x-Hitachi-EBCDIC", 0, -1, -1),
	EBCDIK("x-Hitachi-EBCDIK", 1, -1, -1),
	KEIS78("x-Hitachi-KEIS78", -1, 0, -1),
	KEIS78_HD("x-Hitachi-KEIS78-HanyoDenshi", -1, 0, 0),
	KEIS78_AJ1("x-Hitachi-KEIS78-AdobeJapan1", -1, 0, 1),
	KEIS83("x-Hitachi-KEIS83", -1, 1, -1),
	KEIS83_HD("x-Hitachi-KEIS83-HanyoDenshi", -1, 1, 0),
	KEIS83_AJ1("x-Hitachi-KEIS83-AdobeJapan1", -1, 1, 1),
	KEIS78_EBCDIC("x-Hitachi-KEIS78-EBCDIC", 0, 0, -1),
	KEIS78_EBCDIK("x-Hitachi-KEIS78-EBCDIK", 1, 0, -1),
	KEIS78_HD_EBCDIC("x-Hitachi-KEIS78-HanyoDenshi-EBCDIC", 0, 0, 0),
	KEIS78_HD_EBCDIK("x-Hitachi-KEIS78-HanyoDenshi-EBCDIK", 1, 0, 0),
	KEIS78_AJ1_EBCDIC("x-Hitachi-KEIS78-AdobeJapan1-EBCDIC", 0, 0, 1),
	KEIS78_AJ1_EBCDIK("x-Hitachi-KEIS78-AdobeJapan1-EBCDIK", 1, 0, 1),
	KEIS83_EBCDIC("x-Hitachi-KEIS83-EBCDIC", 0, 1, -1),
	KEIS83_EBCDIK("x-Hitachi-KEIS83-EBCDIK", 1, 1, -1),
	KEIS83_HD_EBCDIC("x-Hitachi-KEIS83-HanyoDenshi-EBCDIC", 0, 1, 0),
	KEIS83_HD_EBCDIK("x-Hitachi-KEIS83-HanyoDenshi-EBCDIK", 1, 1, 0),
	KEIS83_AJ1_EBCDIC("x-Hitachi-KEIS83-AdobeJapan1-EBCDIC", 0, 1, 1),
	KEIS83_AJ1_EBCDIK("x-Hitachi-KEIS83-AdobeJapan1-EBCDIK", 1, 1, 1);

	private final String charsetName;
	private final int sbcsTableNo;
	private final int mbcsTableNo;
	private final int ivsTableNo;
	
	HitachiCharsetType(
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
