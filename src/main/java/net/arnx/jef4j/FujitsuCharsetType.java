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
	EBCDIC("x-Fujitsu-EBCDIC", false, false, -1),
	EBCDIK("x-Fujitsu-EBCDIK", false, false, -1),
	ASCII("x-Fujitsu-ASCII", false, false, -1),
	JEF("x-Fujitsu-JEF", false, false, 0),
	JEF_EBCDIC("x-Fujitsu-JEF-EBCDIC", true, false, 0),
	JEF_EBCDIK("x-Fujitsu-JEF-EBCDIK", true, false, 0),
	JEF_ASCII("x-Fujitsu-JEF-ASCII", true, false, 0),
	JEF_HD("x-Fujitsu-JEF-HanyoDenshi", false, true, 0),
	JEF_HD_EBCDIC("x-Fujitsu-JEF-HanyoDenshi-EBCDIC", true, true, 0),
	JEF_HD_EBCDIK("x-Fujitsu-JEF-HanyoDenshi-EBCDIK", true, true, 0),
	JEF_HD_ASCII("x-Fujitsu-JEF-HanyoDenshi-ASCII", true, true, 0),
	JEF_AJ1("x-Fujitsu-JEF-AdobeJapan1", false, true, 1),
	JEF_AJ1_EBCDIC("x-Fujitsu-JEF-AdobeJapan1-EBCDIC", true, true, 1),
	JEF_AJ1_EBCDIK("x-Fujitsu-JEF-AdobeJapan1-EBCDIK", true, true, 1),
	JEF_AJ1_ASCII("x-Fujitsu-JEF-AdobeJapan1-ASCII", true, true, 1),
	JEF_R("x-Fujitsu-JEF-Reversible", false, false, 2);
	
	private final String charsetName;
	private final boolean handleShift;
	private final boolean handleIVS;
	private final int jefTableNo;
	
	FujitsuCharsetType(
		String charsetName, 
		boolean handleShift,
		boolean handleIVS,
		int ivsTableNo
	) {
		this.charsetName = charsetName;
		this.handleShift = handleShift;
		this.handleIVS = handleIVS;
		this.jefTableNo = ivsTableNo;
	}
	
	public String getCharsetName() {
		return charsetName;
	}

	boolean handleShift() {
		return handleShift;
	}
	
	boolean handleJEF() {
		return jefTableNo != -1;
	}
	
	boolean handleIVS() {
		return handleIVS;
	}

	int getJEFTableNo() {
		return jefTableNo;
	}
}
