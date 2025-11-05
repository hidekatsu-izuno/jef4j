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

enum FujitsuCharsetType {
	EBCDIC("x-Fujitsu-EBCDIC", 0, -1, false),
	EBCDIK("x-Fujitsu-EBCDIK", 1, -1, false),
	ASCII("x-Fujitsu-ASCII", 2, -1, false),
	JEF("x-Fujitsu-JEF", -1, 0, false),
	JEF_EBCDIC("x-Fujitsu-JEF-EBCDIC", 0, 0, false),
	JEF_EBCDIK("x-Fujitsu-JEF-EBCDIK", 1, 0, false),
	JEF_ASCII("x-Fujitsu-JEF-ASCII", 2, 0, false),
	JEF_HD("x-Fujitsu-JEF-HanyoDenshi", -1, 0, true),
	JEF_HD_EBCDIC("x-Fujitsu-JEF-HanyoDenshi-EBCDIC", 0, 0, true),
	JEF_HD_EBCDIK("x-Fujitsu-JEF-HanyoDenshi-EBCDIK", 1, 0, true),
	JEF_HD_ASCII("x-Fujitsu-JEF-HanyoDenshi-ASCII", 2, 0, true),
	JEF_AJ1("x-Fujitsu-JEF-AdobeJapan1", -1, 1, true),
	JEF_AJ1_EBCDIC("x-Fujitsu-JEF-AdobeJapan1-EBCDIC", 0, 1, true),
	JEF_AJ1_EBCDIK("x-Fujitsu-JEF-AdobeJapan1-EBCDIK", 1, 1, true),
	JEF_AJ1_ASCII("x-Fujitsu-JEF-AdobeJapan1-ASCII",  2, 1, true),
	JEF_R("x-Fujitsu-JEF-Reversible", -1, 2, false);
	
	private final String charsetName;
	private final int sbcsTableNo;
	private final int mbcsTableNo;
	private final boolean handleIVS;
	
	FujitsuCharsetType(
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
