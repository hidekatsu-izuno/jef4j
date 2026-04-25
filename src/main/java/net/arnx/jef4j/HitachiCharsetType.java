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
	EBCDIC("x-Hitachi-EBCDIC", 0, -1, -1, false),
	EBCDIK("x-Hitachi-EBCDIK", 1, -1, -1, false),
	KEIS78("x-Hitachi-KEIS78", -1, 0, -1, false),
	KEIS78_HD("x-Hitachi-KEIS78-HanyoDenshi", -1, 0, 0, false),
	KEIS78_AJ1("x-Hitachi-KEIS78-AdobeJapan1", -1, 0, 1, false),
	KEIS78_SSS("x-Hitachi-KEIS78-ShiftSpaceSingle", -1, 0, -1, true),
	KEIS78_SSS_HD("x-Hitachi-KEIS78-ShiftSpaceSingle-HanyoDenshi", -1, 0, 0, true),
	KEIS78_SSS_AJ1("x-Hitachi-KEIS78-ShiftSpaceSingle-AdobeJapan1", -1, 0, 1, true),
	KEIS83("x-Hitachi-KEIS83", -1, 1, -1, false),
	KEIS83_HD("x-Hitachi-KEIS83-HanyoDenshi", -1, 1, 0, false),
	KEIS83_AJ1("x-Hitachi-KEIS83-AdobeJapan1", -1, 1, 1, false),
	KEIS83_SSS("x-Hitachi-KEIS83-ShiftSpaceSingle", -1, 1, -1, true),
	KEIS83_SSS_HD("x-Hitachi-KEIS83-ShiftSpaceSingle-HanyoDenshi", -1, 1, 0, true),
	KEIS83_SSS_AJ1("x-Hitachi-KEIS83-ShiftSpaceSingle-AdobeJapan1", -1, 1, 1, true),
	KEIS78_EBCDIC("x-Hitachi-KEIS78-EBCDIC", 0, 0, -1, false),
	KEIS78_EBCDIK("x-Hitachi-KEIS78-EBCDIK", 1, 0, -1, false),
	KEIS78_HD_EBCDIC("x-Hitachi-KEIS78-HanyoDenshi-EBCDIC", 0, 0, 0, false),
	KEIS78_HD_EBCDIK("x-Hitachi-KEIS78-HanyoDenshi-EBCDIK", 1, 0, 0, false),
	KEIS78_AJ1_EBCDIC("x-Hitachi-KEIS78-AdobeJapan1-EBCDIC", 0, 0, 1, false),
	KEIS78_AJ1_EBCDIK("x-Hitachi-KEIS78-AdobeJapan1-EBCDIK", 1, 0, 1, false),
	KEIS78_SSS_EBCDIC("x-Hitachi-KEIS78-ShiftSpaceSingle-EBCDIC", 0, 0, -1, true),
	KEIS78_SSS_EBCDIK("x-Hitachi-KEIS78-ShiftSpaceSingle-EBCDIK", 1, 0, -1, true),
	KEIS78_SSS_HD_EBCDIC("x-Hitachi-KEIS78-ShiftSpaceSingle-HanyoDenshi-EBCDIC", 0, 0, 0, true),
	KEIS78_SSS_HD_EBCDIK("x-Hitachi-KEIS78-ShiftSpaceSingle-HanyoDenshi-EBCDIK", 1, 0, 0, true),
	KEIS78_SSS_AJ1_EBCDIC("x-Hitachi-KEIS78-ShiftSpaceSingle-AdobeJapan1-EBCDIC", 0, 0, 1, true),
	KEIS78_SSS_AJ1_EBCDIK("x-Hitachi-KEIS78-ShiftSpaceSingle-AdobeJapan1-EBCDIK", 1, 0, 1, true),
	KEIS83_EBCDIC("x-Hitachi-KEIS83-EBCDIC", 0, 1, -1, false),
	KEIS83_EBCDIK("x-Hitachi-KEIS83-EBCDIK", 1, 1, -1, false),
	KEIS83_HD_EBCDIC("x-Hitachi-KEIS83-HanyoDenshi-EBCDIC", 0, 1, 0, false),
	KEIS83_HD_EBCDIK("x-Hitachi-KEIS83-HanyoDenshi-EBCDIK", 1, 1, 0, false),
	KEIS83_AJ1_EBCDIC("x-Hitachi-KEIS83-AdobeJapan1-EBCDIC", 0, 1, 1, false),
	KEIS83_AJ1_EBCDIK("x-Hitachi-KEIS83-AdobeJapan1-EBCDIK", 1, 1, 1, false),
	KEIS83_SSS_EBCDIC("x-Hitachi-KEIS83-ShiftSpaceSingle-EBCDIC", 0, 1, -1, true),
	KEIS83_SSS_EBCDIK("x-Hitachi-KEIS83-ShiftSpaceSingle-EBCDIK", 1, 1, -1, true),
	KEIS83_SSS_HD_EBCDIC("x-Hitachi-KEIS83-ShiftSpaceSingle-HanyoDenshi-EBCDIC", 0, 1, 0, true),
	KEIS83_SSS_HD_EBCDIK("x-Hitachi-KEIS83-ShiftSpaceSingle-HanyoDenshi-EBCDIK", 1, 1, 0, true),
	KEIS83_SSS_AJ1_EBCDIC("x-Hitachi-KEIS83-ShiftSpaceSingle-AdobeJapan1-EBCDIC", 0, 1, 1, true),
	KEIS83_SSS_AJ1_EBCDIK("x-Hitachi-KEIS83-ShiftSpaceSingle-AdobeJapan1-EBCDIK", 1, 1, 1, true);

	private final String charsetName;
	private final int sbcsTableNo;
	private final int mbcsTableNo;
	private final int ivsTableNo;
	private final boolean shiftSpaceSingle;
	
	HitachiCharsetType(
		String charsetName, 
		int sbcsTableNo,
		int mbcsTableNo,
		int ivsTableNo,
		boolean shiftSpaceSingle
	) {
		this.charsetName = charsetName;
		this.sbcsTableNo = sbcsTableNo;
		this.mbcsTableNo = mbcsTableNo;
		this.ivsTableNo = ivsTableNo;
		this.shiftSpaceSingle = shiftSpaceSingle;
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

	boolean isShiftSpaceSingle() {
		return shiftSpaceSingle;
	}
}
