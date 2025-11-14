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
	EBCDIC("x-Fujitsu-EBCDIC", 0, -1, -1),
	EBCDIK("x-Fujitsu-EBCDIK", 1, -1, -1),
	ASCII("x-Fujitsu-ASCII", 2, -1, -1),
	JEF("x-Fujitsu-JEF", -1, 0, -1),
	JEF_EBCDIC("x-Fujitsu-JEF-EBCDIC", 0, 0, 0),
	JEF_EBCDIK("x-Fujitsu-JEF-EBCDIK", 1, 0, 0),
	JEF_ASCII("x-Fujitsu-JEF-ASCII", 2, 0, 0),
	JEF_HD("x-Fujitsu-JEF-HanyoDenshi", -1, 0, 0),
	JEF_HD_EBCDIC("x-Fujitsu-JEF-HanyoDenshi-EBCDIC", 0, 0, 0),
	JEF_HD_EBCDIK("x-Fujitsu-JEF-HanyoDenshi-EBCDIK", 1, 0, 0),
	JEF_HD_ASCII("x-Fujitsu-JEF-HanyoDenshi-ASCII", 2, 0, 0),
	JEF_AJ1("x-Fujitsu-JEF-AdobeJapan1", -1, 0, 1),
	JEF_AJ1_EBCDIC("x-Fujitsu-JEF-AdobeJapan1-EBCDIC", 0, 0, 1),
	JEF_AJ1_EBCDIK("x-Fujitsu-JEF-AdobeJapan1-EBCDIK", 1, 0, 1),
	JEF_AJ1_ASCII("x-Fujitsu-JEF-AdobeJapan1-ASCII",  2, 0, 1),
	JEF_R("x-Fujitsu-JEF-Roundtrip", -1, 0, 2);
	
	private final String charsetName;
	private final int sbcsTableNo;
	private final int mbcsTableNo;
	private final int ivsTableNo;
	
	FujitsuCharsetType(
		String charsetName, 
		int sbcsTableNo,
		int mbcsTableNo,
		int ivsTableNo
	) {
		this.charsetName = charsetName;
		this.sbcsTableNo = sbcsTableNo;
		this.ivsTableNo = ivsTableNo;
		this.mbcsTableNo = mbcsTableNo;
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
