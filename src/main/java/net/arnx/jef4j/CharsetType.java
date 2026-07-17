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

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

enum CharsetType {
	FUJITSU_EBCDIC("x-Fujitsu-EBCDIC", 0, -1, -1, FujitsuCharset::new),
	FUJITSU_EBCDIK("x-Fujitsu-EBCDIK", 1, -1, -1, FujitsuCharset::new),
	FUJITSU_ASCII("x-Fujitsu-ASCII", 2, -1, -1, FujitsuCharset::new),
	FUJITSU_JEF("x-Fujitsu-JEF", -1, 0, -1, FujitsuCharset::new),
	FUJITSU_JEF_EBCDIC("x-Fujitsu-JEF-EBCDIC", 0, 0, -1, FujitsuCharset::new),
	FUJITSU_JEF_EBCDIK("x-Fujitsu-JEF-EBCDIK", 1, 0, -1, FujitsuCharset::new),
	FUJITSU_JEF_ASCII("x-Fujitsu-JEF-ASCII", 2, 0, -1, FujitsuCharset::new),
	FUJITSU_JEF_HD("x-Fujitsu-JEF-HanyoDenshi", -1, 0, 0, FujitsuCharset::new),
	FUJITSU_JEF_HD_EBCDIC("x-Fujitsu-JEF-HanyoDenshi-EBCDIC", 0, 0, 0, FujitsuCharset::new),
	FUJITSU_JEF_HD_EBCDIK("x-Fujitsu-JEF-HanyoDenshi-EBCDIK", 1, 0, 0, FujitsuCharset::new),
	FUJITSU_JEF_HD_ASCII("x-Fujitsu-JEF-HanyoDenshi-ASCII", 2, 0, 0, FujitsuCharset::new),
	FUJITSU_JEF_AJ1("x-Fujitsu-JEF-AdobeJapan1", -1, 0, 1, FujitsuCharset::new),
	FUJITSU_JEF_AJ1_EBCDIC("x-Fujitsu-JEF-AdobeJapan1-EBCDIC", 0, 0, 1, FujitsuCharset::new),
	FUJITSU_JEF_AJ1_EBCDIK("x-Fujitsu-JEF-AdobeJapan1-EBCDIK", 1, 0, 1, FujitsuCharset::new),
	FUJITSU_JEF_AJ1_ASCII("x-Fujitsu-JEF-AdobeJapan1-ASCII", 2, 0, 1, FujitsuCharset::new),
	FUJITSU_JEF_RT("x-Fujitsu-JEF-Roundtrip", -1, 0, 2, FujitsuCharset::new),

	HITACHI_EBCDIC("x-Hitachi-EBCDIC", 0, -1, -1, false, HitachiCharset::new),
	HITACHI_EBCDIK("x-Hitachi-EBCDIK", 1, -1, -1, false, HitachiCharset::new),
	HITACHI_KEIS78("x-Hitachi-KEIS78", -1, 0, -1, false, HitachiCharset::new),
	HITACHI_KEIS78_HD("x-Hitachi-KEIS78-HanyoDenshi", -1, 0, 0, false, HitachiCharset::new),
	HITACHI_KEIS78_AJ1("x-Hitachi-KEIS78-AdobeJapan1", -1, 0, 1, false, HitachiCharset::new),
	HITACHI_KEIS78_SSS("x-Hitachi-KEIS78-ShiftSpaceSingle", -1, 0, -1, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_HD("x-Hitachi-KEIS78-ShiftSpaceSingle-HanyoDenshi", -1, 0, 0, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_AJ1("x-Hitachi-KEIS78-ShiftSpaceSingle-AdobeJapan1", -1, 0, 1, true, HitachiCharset::new),
	HITACHI_KEIS83("x-Hitachi-KEIS83", -1, 1, -1, false, HitachiCharset::new),
	HITACHI_KEIS83_HD("x-Hitachi-KEIS83-HanyoDenshi", -1, 1, 0, false, HitachiCharset::new),
	HITACHI_KEIS83_AJ1("x-Hitachi-KEIS83-AdobeJapan1", -1, 1, 1, false, HitachiCharset::new),
	HITACHI_KEIS83_SSS("x-Hitachi-KEIS83-ShiftSpaceSingle", -1, 1, -1, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_HD("x-Hitachi-KEIS83-ShiftSpaceSingle-HanyoDenshi", -1, 1, 0, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_AJ1("x-Hitachi-KEIS83-ShiftSpaceSingle-AdobeJapan1", -1, 1, 1, true, HitachiCharset::new),
	HITACHI_KEIS78_EBCDIC("x-Hitachi-KEIS78-EBCDIC", 0, 0, -1, false, HitachiCharset::new),
	HITACHI_KEIS78_EBCDIK("x-Hitachi-KEIS78-EBCDIK", 1, 0, -1, false, HitachiCharset::new),
	HITACHI_KEIS78_HD_EBCDIC("x-Hitachi-KEIS78-HanyoDenshi-EBCDIC", 0, 0, 0, false, HitachiCharset::new),
	HITACHI_KEIS78_HD_EBCDIK("x-Hitachi-KEIS78-HanyoDenshi-EBCDIK", 1, 0, 0, false, HitachiCharset::new),
	HITACHI_KEIS78_AJ1_EBCDIC("x-Hitachi-KEIS78-AdobeJapan1-EBCDIC", 0, 0, 1, false, HitachiCharset::new),
	HITACHI_KEIS78_AJ1_EBCDIK("x-Hitachi-KEIS78-AdobeJapan1-EBCDIK", 1, 0, 1, false, HitachiCharset::new),
	HITACHI_KEIS78_SSS_EBCDIC("x-Hitachi-KEIS78-ShiftSpaceSingle-EBCDIC", 0, 0, -1, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_EBCDIK("x-Hitachi-KEIS78-ShiftSpaceSingle-EBCDIK", 1, 0, -1, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_HD_EBCDIC("x-Hitachi-KEIS78-ShiftSpaceSingle-HanyoDenshi-EBCDIC", 0, 0, 0, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_HD_EBCDIK("x-Hitachi-KEIS78-ShiftSpaceSingle-HanyoDenshi-EBCDIK", 1, 0, 0, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_AJ1_EBCDIC("x-Hitachi-KEIS78-ShiftSpaceSingle-AdobeJapan1-EBCDIC", 0, 0, 1, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_AJ1_EBCDIK("x-Hitachi-KEIS78-ShiftSpaceSingle-AdobeJapan1-EBCDIK", 1, 0, 1, true, HitachiCharset::new),
	HITACHI_KEIS83_EBCDIC("x-Hitachi-KEIS83-EBCDIC", 0, 1, -1, false, HitachiCharset::new),
	HITACHI_KEIS83_EBCDIK("x-Hitachi-KEIS83-EBCDIK", 1, 1, -1, false, HitachiCharset::new),
	HITACHI_KEIS83_HD_EBCDIC("x-Hitachi-KEIS83-HanyoDenshi-EBCDIC", 0, 1, 0, false, HitachiCharset::new),
	HITACHI_KEIS83_HD_EBCDIK("x-Hitachi-KEIS83-HanyoDenshi-EBCDIK", 1, 1, 0, false, HitachiCharset::new),
	HITACHI_KEIS83_AJ1_EBCDIC("x-Hitachi-KEIS83-AdobeJapan1-EBCDIC", 0, 1, 1, false, HitachiCharset::new),
	HITACHI_KEIS83_AJ1_EBCDIK("x-Hitachi-KEIS83-AdobeJapan1-EBCDIK", 1, 1, 1, false, HitachiCharset::new),
	HITACHI_KEIS83_SSS_EBCDIC("x-Hitachi-KEIS83-ShiftSpaceSingle-EBCDIC", 0, 1, -1, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_EBCDIK("x-Hitachi-KEIS83-ShiftSpaceSingle-EBCDIK", 1, 1, -1, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_HD_EBCDIC("x-Hitachi-KEIS83-ShiftSpaceSingle-HanyoDenshi-EBCDIC", 0, 1, 0, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_HD_EBCDIK("x-Hitachi-KEIS83-ShiftSpaceSingle-HanyoDenshi-EBCDIK", 1, 1, 0, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_AJ1_EBCDIC("x-Hitachi-KEIS83-ShiftSpaceSingle-AdobeJapan1-EBCDIC", 0, 1, 1, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_AJ1_EBCDIK("x-Hitachi-KEIS83-ShiftSpaceSingle-AdobeJapan1-EBCDIK", 1, 1, 1, true, HitachiCharset::new),

	NEC_JIS8("x-NEC-JIS8", 0, -1, -1, NecCharset::new),
	NEC_EBCDIK("x-NEC-EBCDIK", 1, -1, -1, NecCharset::new),
	NEC_JIPSJ("x-NEC-JIPSJ", -1, 0, -1, NecCharset::new),
	NEC_JIPSJ_HD("x-NEC-JIPSJ-HanyoDenshi", -1, 0, 0, NecCharset::new),
	NEC_JIPSJ_AJ1("x-NEC-JIPSJ-AdobeJapan1", -1, 0, 1, NecCharset::new),
	NEC_JIPSJ_JIS8("x-NEC-JIPSJ-JIS8", 0, 0, -1, NecCharset::new),
	NEC_JIPSJ_HD_JIS8("x-NEC-JIPSJ-HanyoDenshi-JIS8", 1, 0, 0, NecCharset::new),
	NEC_JIPSJ_AJ1_JIS8("x-NEC-JIPSJ-AdobeJapan1-JIS8", 1, 1, 1, NecCharset::new),
	NEC_JIPSE("x-NEC-JIPSE", -1, 1, -1, NecCharset::new),
	NEC_JIPSE_HD("x-NEC-JIPSE-HanyoDenshi", -1, 1, 0, NecCharset::new),
	NEC_JIPSE_AJ1("x-NEC-JIPSE-AdobeJapan1", -1, 1, 1, NecCharset::new),
	NEC_JIPSE_EBCDIK("x-NEC-JIPSE-EBCDIK", 1, 1, -1, NecCharset::new),
	NEC_JIPSE_HD_EBCDIK("x-NEC-JIPSE-HanyoDenshi-EBCDIK", 1, 1, 0, NecCharset::new),
	NEC_JIPSE_AJ1_EBCDIK("x-NEC-JIPSE-AdobeJapan1-EBCDIK", 1, 1, 1, NecCharset::new),

	IBM_8482("x-IBM-8482", 0, -1, -1, IbmCharset::new),
	IBM_5123("x-IBM-5123", 1, -1, -1, IbmCharset::new),
	IBM_11684("x-IBM-11684", -1, 0, -1, IbmCharset::new),
	IBM_1390("x-IBM-1390", 0, 0, -1, IbmCharset::new),
	IBM_1399("x-IBM-1399", 1, 0, -1, IbmCharset::new);

	private static final Map<String, CharsetType> TYPES_BY_NAME;

	static {
		Map<String, CharsetType> typesByName = new HashMap<>();
		for (CharsetType type : values()) {
			typesByName.put(type.charsetName.toLowerCase(Locale.ROOT), type);
		}
		TYPES_BY_NAME = Collections.unmodifiableMap(typesByName);
	}

	private final String charsetName;
	private final int sbcsTableNo;
	private final int mbcsTableNo;
	private final int ivsTableNo;
	private final boolean shiftSpaceSingle;
	private final Function<CharsetType, Charset> charsetFactory;

	CharsetType(String charsetName, int sbcsTableNo, int mbcsTableNo, int ivsTableNo,
			Function<CharsetType, Charset> charsetFactory) {
		this(charsetName, sbcsTableNo, mbcsTableNo, ivsTableNo, false, charsetFactory);
	}

	CharsetType(String charsetName, int sbcsTableNo, int mbcsTableNo, int ivsTableNo,
			boolean shiftSpaceSingle, Function<CharsetType, Charset> charsetFactory) {
		this.charsetName = charsetName;
		this.sbcsTableNo = sbcsTableNo;
		this.mbcsTableNo = mbcsTableNo;
		this.ivsTableNo = ivsTableNo;
		this.shiftSpaceSingle = shiftSpaceSingle;
		this.charsetFactory = charsetFactory;
	}

	String getCharsetName() {
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

	Charset newCharset() {
		return charsetFactory.apply(this);
	}

	static CharsetType forName(String charsetName) {
		return TYPES_BY_NAME.get(charsetName.toLowerCase(Locale.ROOT));
	}
}
