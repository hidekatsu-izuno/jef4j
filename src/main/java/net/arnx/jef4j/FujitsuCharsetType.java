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
	EBCDIC("x-Fujitsu-EBCDIC", false, false),
	EBCDIK("x-Fujitsu-EBCDIK", false, false),
	ASCII("x-Fujitsu-ASCII", false, false),
	JEF("x-Fujitsu-JEF", true, false),
	JEF_EBCDIC("x-Fujitsu-JEF-EBCDIC", true, false),
	JEF_EBCDIK("x-Fujitsu-JEF-EBCDIK", true, false),
	JEF_ASCII("x-Fujitsu-JEF-ASCII", true, false),
	JEF_HD("x-Fujitsu-JEF-HanyoDenshi", true, true),
	JEF_HD_EBCDIC("x-Fujitsu-JEF-HanyoDenshi-EBCDIC", true, true),
	JEF_HD_EBCDIK("x-Fujitsu-JEF-HanyoDenshi-EBCDIK", true, true),
	JEF_HD_ASCII("x-Fujitsu-JEF-HanyoDenshi-ASCII", true, true);
	
	private final String charsetName;
	private final boolean handleJEF;
	private final boolean handleHanyoDenshi;
	
	FujitsuCharsetType(String charsetName, 
			boolean handleJEF, boolean handleHanyoDenshi) {
		this.charsetName = charsetName;
		this.handleJEF = handleJEF;
		this.handleHanyoDenshi = handleHanyoDenshi;
	}
	
	public String getCharsetName() {
		return charsetName;
	}
	
	boolean handleJEF() {
		return handleJEF;
	}
	
	boolean handleHanyoDenshi() {
		return handleHanyoDenshi;
	}
}
