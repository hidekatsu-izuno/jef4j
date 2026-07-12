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

enum IbmCharsetType {
	IBM8482("x-IBM-8482", 0, -1, -1),
	IBM5123("x-IBM-5123", 1, -1, -1),
	IBM11684("x-IBM-11684", -1, 0, -1),
	IBM1390("x-IBM-1390", 0, 0, -1),
	IBM1399("x-IBM-1399", 1, 0, -1);
	
	private final String charsetName;
	private final int sbcsTableNo;
	private final int mbcsTableNo;
	private final int ivsTableNo;
	
	IbmCharsetType(
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
