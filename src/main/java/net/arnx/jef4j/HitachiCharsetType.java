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
	EBCDIC("x-Hitachi-EBCDIC", 0, -1, false),
	EBCDIK("x-Hitachi-EBCDIK", 1, -1, false),
	KEIS78("x-Hitachi-KEIS78", -1, 0, false),
	KEIS90("x-Hitachi-KEIS90", -1, 1, false),
	KEIS78_EBCDIC("x-Hitachi-KEIS78-EBCDIC", 0, 0, false),
	KEIS78_EBCDIK("x-Hitachi-KEIS78-EBCDIK", 1, 0, false),
	KEIS78_HD_EBCDIC("x-Hitachi-KEIS78-HanyoDenshi-EBCDIC", 0, 0, true),
	KEIS78_HD_EBCDIK("x-Hitachi-KEIS78-HanyoDenshi-EBCDIK", 1, 0, true),
	KEIS78_AJ1_EBCDIC("x-Hitachi-KEIS78-AdobeJapan1-EBCDIC", 0, 0, true),
	KEIS78_AJ1_EBCDIK("x-Hitachi-KEIS78-AdobeJapan1-EBCDIK", 1, 0, true),
	KEIS90_EBCDIC("x-Hitachi-KEIS90-EBCDIC", 0, 1, false),
	KEIS90_EBCDIK("x-Hitachi-KEIS90-EBCDIK", 1, 1, false),
	KEIS90_HD_EBCDIC("x-Hitachi-KEIS90-HanyoDenshi-EBCDIC", 0, 1, true),
	KEIS90_HD_EBCDIK("x-Hitachi-KEIS90-HanyoDenshi-EBCDIK", 1, 1, true),
	KEIS90_AJ1_EBCDIC("x-Hitachi-KEIS90-AdobeJapan1-EBCDIC", 0, 1, true),
	KEIS90_AJ1_EBCDIK("x-Hitachi-KEIS90-AdobeJapan1-EBCDIK", 1, 1, true);

	private final String charsetName;
	private final int sbcsTableNo;
	private final int mbcsTableNo;
	private final boolean handleIVS;
	
	HitachiCharsetType(
		String charsetName, 
		int sbcsTableNo,
		int mbcsTableNo,
		boolean handleIVS
	) {
		this.charsetName = charsetName;
		this.sbcsTableNo = sbcsTableNo;
		this.handleIVS = handleIVS;
		this.mbcsTableNo = mbcsTableNo;
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
