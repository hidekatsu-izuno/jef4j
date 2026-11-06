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
	FUJITSU_EBCDIC_LOWER("x-Fujitsu-EBCDIC-Lower", new String[] { "x-Fujitsu-EBCDIC" }, 0, -1, -1, FujitsuCharset::new),
	FUJITSU_EBCDIC_KANA("x-Fujitsu-EBCDIC-Kana", new String[] { "x-Fujitsu-EBCDIK" }, 1, -1, -1, FujitsuCharset::new),
	FUJITSU_EBCDIC_ASCII("x-Fujitsu-EBCDIC-Ascii", new String[] { "x-Fujitsu-ASCII" }, 2, -1, -1, FujitsuCharset::new),
	FUJITSU_JEF("x-Fujitsu-JEF", new String[0], -1, 0, -1, FujitsuCharset::new),
	FUJITSU_EBCDIC_LOWER_PLUS_JEF("x-Fujitsu-EBCDIC-Lower+JEF", new String[] { "x-Fujitsu-EBCDIC+JEF", "x-Fujitsu-JEF-EBCDIC" }, 0, 0, -1, FujitsuCharset::new),
	FUJITSU_JEF_PLUS_EBCDIC_LOWER("x-Fujitsu-JEF+EBCDIC-Lower", new String[] { "x-Fujitsu-JEF+EBCDIC" }, 0, 0, -1, true, false, FujitsuCharset::new),
	FUJITSU_EBCDIC_KANA_PLUS_JEF("x-Fujitsu-EBCDIC-Kana+JEF", new String[] { "x-Fujitsu-EBCDIK+JEF", "x-Fujitsu-JEF-EBCDIK" }, 1, 0, -1, FujitsuCharset::new),
	FUJITSU_JEF_PLUS_EBCDIC_KANA("x-Fujitsu-JEF+EBCDIC-Kana", new String[] { "x-Fujitsu-JEF+EBCDIK" }, 1, 0, -1, true, false, FujitsuCharset::new),
	FUJITSU_EBCDIC_ASCII_PLUS_JEF("x-Fujitsu-EBCDIC-Ascii+JEF", new String[] { "x-Fujitsu-ASCII+JEF", "x-Fujitsu-JEF-ASCII" }, 2, 0, -1, FujitsuCharset::new),
	FUJITSU_JEF_PLUS_EBCDIC_ASCII("x-Fujitsu-JEF+EBCDIC-Ascii", new String[] { "x-Fujitsu-JEF+ASCII" }, 2, 0, -1, true, false, FujitsuCharset::new),
	FUJITSU_JEF_HD("x-Fujitsu-JEF-HanyoDenshi", new String[0], -1, 0, 0, FujitsuCharset::new),
	FUJITSU_EBCDIC_LOWER_PLUS_JEF_HD("x-Fujitsu-EBCDIC-Lower+JEF-HanyoDenshi", new String[] { "x-Fujitsu-EBCDIC+JEF-HanyoDenshi", "x-Fujitsu-JEF-HanyoDenshi-EBCDIC" }, 0, 0, 0, FujitsuCharset::new),
	FUJITSU_JEF_HD_PLUS_EBCDIC_LOWER("x-Fujitsu-JEF-HanyoDenshi+EBCDIC-Lower", new String[] { "x-Fujitsu-JEF-HanyoDenshi+EBCDIC" }, 0, 0, 0, true, false, FujitsuCharset::new),
	FUJITSU_EBCDIC_KANA_PLUS_JEF_HD("x-Fujitsu-EBCDIC-Kana+JEF-HanyoDenshi", new String[] { "x-Fujitsu-EBCDIK+JEF-HanyoDenshi", "x-Fujitsu-JEF-HanyoDenshi-EBCDIK" }, 1, 0, 0, FujitsuCharset::new),
	FUJITSU_JEF_HD_PLUS_EBCDIC_KANA("x-Fujitsu-JEF-HanyoDenshi+EBCDIC-Kana", new String[] { "x-Fujitsu-JEF-HanyoDenshi+EBCDIK" }, 1, 0, 0, true, false, FujitsuCharset::new),
	FUJITSU_EBCDIC_ASCII_PLUS_JEF_HD("x-Fujitsu-EBCDIC-Ascii+JEF-HanyoDenshi", new String[] { "x-Fujitsu-ASCII+JEF-HanyoDenshi", "x-Fujitsu-JEF-HanyoDenshi-ASCII" }, 2, 0, 0, FujitsuCharset::new),
	FUJITSU_JEF_HD_PLUS_EBCDIC_ASCII("x-Fujitsu-JEF-HanyoDenshi+EBCDIC-Ascii", new String[] { "x-Fujitsu-JEF-HanyoDenshi+ASCII" }, 2, 0, 0, true, false, FujitsuCharset::new),
	FUJITSU_JEF_AJ1("x-Fujitsu-JEF-AdobeJapan1", new String[0], -1, 0, 1, FujitsuCharset::new),
	FUJITSU_EBCDIC_LOWER_PLUS_JEF_AJ1("x-Fujitsu-EBCDIC-Lower+JEF-AdobeJapan1", new String[] { "x-Fujitsu-EBCDIC+JEF-AdobeJapan1", "x-Fujitsu-JEF-AdobeJapan1-EBCDIC" }, 0, 0, 1, FujitsuCharset::new),
	FUJITSU_JEF_AJ1_PLUS_EBCDIC_LOWER("x-Fujitsu-JEF-AdobeJapan1+EBCDIC-Lower", new String[] { "x-Fujitsu-JEF-AdobeJapan1+EBCDIC" }, 0, 0, 1, true, false, FujitsuCharset::new),
	FUJITSU_EBCDIC_KANA_PLUS_JEF_AJ1("x-Fujitsu-EBCDIC-Kana+JEF-AdobeJapan1", new String[] { "x-Fujitsu-EBCDIK+JEF-AdobeJapan1", "x-Fujitsu-JEF-AdobeJapan1-EBCDIK" }, 1, 0, 1, FujitsuCharset::new),
	FUJITSU_JEF_AJ1_PLUS_EBCDIC_KANA("x-Fujitsu-JEF-AdobeJapan1+EBCDIC-Kana", new String[] { "x-Fujitsu-JEF-AdobeJapan1+EBCDIK" }, 1, 0, 1, true, false, FujitsuCharset::new),
	FUJITSU_EBCDIC_ASCII_PLUS_JEF_AJ1("x-Fujitsu-EBCDIC-Ascii+JEF-AdobeJapan1", new String[] { "x-Fujitsu-ASCII+JEF-AdobeJapan1", "x-Fujitsu-JEF-AdobeJapan1-ASCII" }, 2, 0, 1, FujitsuCharset::new),
	FUJITSU_JEF_AJ1_PLUS_EBCDIC_ASCII("x-Fujitsu-JEF-AdobeJapan1+EBCDIC-Ascii", new String[] { "x-Fujitsu-JEF-AdobeJapan1+ASCII" }, 2, 0, 1, true, false, FujitsuCharset::new),
	FUJITSU_JEF_RT("x-Fujitsu-JEF-Roundtrip", new String[0], -1, 0, 2, FujitsuCharset::new),

	HITACHI_EBCDIC("x-Hitachi-EBCDIC", new String[0], 0, -1, -1, false, HitachiCharset::new),
	HITACHI_EBCDIK("x-Hitachi-EBCDIK", new String[0], 1, -1, -1, false, HitachiCharset::new),
	HITACHI_KEIS78("x-Hitachi-KEIS78", new String[0], -1, 0, -1, false, HitachiCharset::new),
	HITACHI_KEIS78_HD("x-Hitachi-KEIS78-HanyoDenshi", new String[0], -1, 0, 0, false, HitachiCharset::new),
	HITACHI_KEIS78_AJ1("x-Hitachi-KEIS78-AdobeJapan1", new String[0], -1, 0, 1, false, HitachiCharset::new),
	HITACHI_KEIS78_SSS("x-Hitachi-KEIS78-ShiftSpaceSingle", new String[0], -1, 0, -1, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_HD("x-Hitachi-KEIS78-ShiftSpaceSingle-HanyoDenshi", new String[0], -1, 0, 0, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_AJ1("x-Hitachi-KEIS78-ShiftSpaceSingle-AdobeJapan1", new String[0], -1, 0, 1, true, HitachiCharset::new),
	HITACHI_KEIS83("x-Hitachi-KEIS83", new String[0], -1, 1, -1, false, HitachiCharset::new),
	HITACHI_KEIS83_HD("x-Hitachi-KEIS83-HanyoDenshi", new String[0], -1, 1, 0, false, HitachiCharset::new),
	HITACHI_KEIS83_AJ1("x-Hitachi-KEIS83-AdobeJapan1", new String[0], -1, 1, 1, false, HitachiCharset::new),
	HITACHI_KEIS83_SSS("x-Hitachi-KEIS83-ShiftSpaceSingle", new String[0], -1, 1, -1, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_HD("x-Hitachi-KEIS83-ShiftSpaceSingle-HanyoDenshi", new String[0], -1, 1, 0, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_AJ1("x-Hitachi-KEIS83-ShiftSpaceSingle-AdobeJapan1", new String[0], -1, 1, 1, true, HitachiCharset::new),
	HITACHI_EBCDIC_PLUS_KEIS78("x-Hitachi-EBCDIC+KEIS78", new String[] { "x-Hitachi-KEIS78-EBCDIC" }, 0, 0, -1, false, HitachiCharset::new),
	HITACHI_KEIS78_PLUS_EBCDIC("x-Hitachi-KEIS78+EBCDIC", new String[0], 0, 0, -1, true, false, HitachiCharset::new),
	HITACHI_EBCDIK_PLUS_KEIS78("x-Hitachi-EBCDIK+KEIS78", new String[] { "x-Hitachi-KEIS78-EBCDIK" }, 1, 0, -1, false, HitachiCharset::new),
	HITACHI_KEIS78_PLUS_EBCDIK("x-Hitachi-KEIS78+EBCDIK", new String[0], 1, 0, -1, true, false, HitachiCharset::new),
	HITACHI_EBCDIC_PLUS_KEIS78_HD("x-Hitachi-EBCDIC+KEIS78-HanyoDenshi", new String[] { "x-Hitachi-KEIS78-HanyoDenshi-EBCDIC" }, 0, 0, 0, false, HitachiCharset::new),
	HITACHI_KEIS78_HD_PLUS_EBCDIC("x-Hitachi-KEIS78-HanyoDenshi+EBCDIC", new String[0], 0, 0, 0, true, false, HitachiCharset::new),
	HITACHI_EBCDIK_PLUS_KEIS78_HD("x-Hitachi-EBCDIK+KEIS78-HanyoDenshi", new String[] { "x-Hitachi-KEIS78-HanyoDenshi-EBCDIK" }, 1, 0, 0, false, HitachiCharset::new),
	HITACHI_KEIS78_HD_PLUS_EBCDIK("x-Hitachi-KEIS78-HanyoDenshi+EBCDIK", new String[0], 1, 0, 0, true, false, HitachiCharset::new),
	HITACHI_EBCDIC_PLUS_KEIS78_AJ1("x-Hitachi-EBCDIC+KEIS78-AdobeJapan1", new String[] { "x-Hitachi-KEIS78-AdobeJapan1-EBCDIC" }, 0, 0, 1, false, HitachiCharset::new),
	HITACHI_KEIS78_AJ1_PLUS_EBCDIC("x-Hitachi-KEIS78-AdobeJapan1+EBCDIC", new String[0], 0, 0, 1, true, false, HitachiCharset::new),
	HITACHI_EBCDIK_PLUS_KEIS78_AJ1("x-Hitachi-EBCDIK+KEIS78-AdobeJapan1", new String[] { "x-Hitachi-KEIS78-AdobeJapan1-EBCDIK" }, 1, 0, 1, false, HitachiCharset::new),
	HITACHI_KEIS78_AJ1_PLUS_EBCDIK("x-Hitachi-KEIS78-AdobeJapan1+EBCDIK", new String[0], 1, 0, 1, true, false, HitachiCharset::new),
	HITACHI_EBCDIC_PLUS_KEIS78_SSS("x-Hitachi-EBCDIC+KEIS78-ShiftSpaceSingle", new String[] { "x-Hitachi-KEIS78-ShiftSpaceSingle-EBCDIC" }, 0, 0, -1, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_PLUS_EBCDIC("x-Hitachi-KEIS78-ShiftSpaceSingle+EBCDIC", new String[0], 0, 0, -1, true, true, HitachiCharset::new),
	HITACHI_EBCDIK_PLUS_KEIS78_SSS("x-Hitachi-EBCDIK+KEIS78-ShiftSpaceSingle", new String[] { "x-Hitachi-KEIS78-ShiftSpaceSingle-EBCDIK" }, 1, 0, -1, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_PLUS_EBCDIK("x-Hitachi-KEIS78-ShiftSpaceSingle+EBCDIK", new String[0], 1, 0, -1, true, true, HitachiCharset::new),
	HITACHI_EBCDIC_PLUS_KEIS78_SSS_HD("x-Hitachi-EBCDIC+KEIS78-ShiftSpaceSingle-HanyoDenshi", new String[] { "x-Hitachi-KEIS78-ShiftSpaceSingle-HanyoDenshi-EBCDIC" }, 0, 0, 0, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_HD_PLUS_EBCDIC("x-Hitachi-KEIS78-ShiftSpaceSingle-HanyoDenshi+EBCDIC", new String[0], 0, 0, 0, true, true, HitachiCharset::new),
	HITACHI_EBCDIK_PLUS_KEIS78_SSS_HD("x-Hitachi-EBCDIK+KEIS78-ShiftSpaceSingle-HanyoDenshi", new String[] { "x-Hitachi-KEIS78-ShiftSpaceSingle-HanyoDenshi-EBCDIK" }, 1, 0, 0, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_HD_PLUS_EBCDIK("x-Hitachi-KEIS78-ShiftSpaceSingle-HanyoDenshi+EBCDIK", new String[0], 1, 0, 0, true, true, HitachiCharset::new),
	HITACHI_EBCDIC_PLUS_KEIS78_SSS_AJ1("x-Hitachi-EBCDIC+KEIS78-ShiftSpaceSingle-AdobeJapan1", new String[] { "x-Hitachi-KEIS78-ShiftSpaceSingle-AdobeJapan1-EBCDIC" }, 0, 0, 1, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_AJ1_PLUS_EBCDIC("x-Hitachi-KEIS78-ShiftSpaceSingle-AdobeJapan1+EBCDIC", new String[0], 0, 0, 1, true, true, HitachiCharset::new),
	HITACHI_EBCDIK_PLUS_KEIS78_SSS_AJ1("x-Hitachi-EBCDIK+KEIS78-ShiftSpaceSingle-AdobeJapan1", new String[] { "x-Hitachi-KEIS78-ShiftSpaceSingle-AdobeJapan1-EBCDIK" }, 1, 0, 1, true, HitachiCharset::new),
	HITACHI_KEIS78_SSS_AJ1_PLUS_EBCDIK("x-Hitachi-KEIS78-ShiftSpaceSingle-AdobeJapan1+EBCDIK", new String[0], 1, 0, 1, true, true, HitachiCharset::new),
	HITACHI_EBCDIC_PLUS_KEIS83("x-Hitachi-EBCDIC+KEIS83", new String[] { "x-Hitachi-KEIS83-EBCDIC" }, 0, 1, -1, false, HitachiCharset::new),
	HITACHI_KEIS83_PLUS_EBCDIC("x-Hitachi-KEIS83+EBCDIC", new String[0], 0, 1, -1, true, false, HitachiCharset::new),
	HITACHI_EBCDIK_PLUS_KEIS83("x-Hitachi-EBCDIK+KEIS83", new String[] { "x-Hitachi-KEIS83-EBCDIK" }, 1, 1, -1, false, HitachiCharset::new),
	HITACHI_KEIS83_PLUS_EBCDIK("x-Hitachi-KEIS83+EBCDIK", new String[0], 1, 1, -1, true, false, HitachiCharset::new),
	HITACHI_EBCDIC_PLUS_KEIS83_HD("x-Hitachi-EBCDIC+KEIS83-HanyoDenshi", new String[] { "x-Hitachi-KEIS83-HanyoDenshi-EBCDIC" }, 0, 1, 0, false, HitachiCharset::new),
	HITACHI_KEIS83_HD_PLUS_EBCDIC("x-Hitachi-KEIS83-HanyoDenshi+EBCDIC", new String[0], 0, 1, 0, true, false, HitachiCharset::new),
	HITACHI_EBCDIK_PLUS_KEIS83_HD("x-Hitachi-EBCDIK+KEIS83-HanyoDenshi", new String[] { "x-Hitachi-KEIS83-HanyoDenshi-EBCDIK" }, 1, 1, 0, false, HitachiCharset::new),
	HITACHI_KEIS83_HD_PLUS_EBCDIK("x-Hitachi-KEIS83-HanyoDenshi+EBCDIK", new String[0], 1, 1, 0, true, false, HitachiCharset::new),
	HITACHI_EBCDIC_PLUS_KEIS83_AJ1("x-Hitachi-EBCDIC+KEIS83-AdobeJapan1", new String[] { "x-Hitachi-KEIS83-AdobeJapan1-EBCDIC" }, 0, 1, 1, false, HitachiCharset::new),
	HITACHI_KEIS83_AJ1_PLUS_EBCDIC("x-Hitachi-KEIS83-AdobeJapan1+EBCDIC", new String[0], 0, 1, 1, true, false, HitachiCharset::new),
	HITACHI_EBCDIK_PLUS_KEIS83_AJ1("x-Hitachi-EBCDIK+KEIS83-AdobeJapan1", new String[] { "x-Hitachi-KEIS83-AdobeJapan1-EBCDIK" }, 1, 1, 1, false, HitachiCharset::new),
	HITACHI_KEIS83_AJ1_PLUS_EBCDIK("x-Hitachi-KEIS83-AdobeJapan1+EBCDIK", new String[0], 1, 1, 1, true, false, HitachiCharset::new),
	HITACHI_EBCDIC_PLUS_KEIS83_SSS("x-Hitachi-EBCDIC+KEIS83-ShiftSpaceSingle", new String[] { "x-Hitachi-KEIS83-ShiftSpaceSingle-EBCDIC" }, 0, 1, -1, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_PLUS_EBCDIC("x-Hitachi-KEIS83-ShiftSpaceSingle+EBCDIC", new String[0], 0, 1, -1, true, true, HitachiCharset::new),
	HITACHI_EBCDIK_PLUS_KEIS83_SSS("x-Hitachi-EBCDIK+KEIS83-ShiftSpaceSingle", new String[] { "x-Hitachi-KEIS83-ShiftSpaceSingle-EBCDIK" }, 1, 1, -1, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_PLUS_EBCDIK("x-Hitachi-KEIS83-ShiftSpaceSingle+EBCDIK", new String[0], 1, 1, -1, true, true, HitachiCharset::new),
	HITACHI_EBCDIC_PLUS_KEIS83_SSS_HD("x-Hitachi-EBCDIC+KEIS83-ShiftSpaceSingle-HanyoDenshi", new String[] { "x-Hitachi-KEIS83-ShiftSpaceSingle-HanyoDenshi-EBCDIC" }, 0, 1, 0, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_HD_PLUS_EBCDIC("x-Hitachi-KEIS83-ShiftSpaceSingle-HanyoDenshi+EBCDIC", new String[0], 0, 1, 0, true, true, HitachiCharset::new),
	HITACHI_EBCDIK_PLUS_KEIS83_SSS_HD("x-Hitachi-EBCDIK+KEIS83-ShiftSpaceSingle-HanyoDenshi", new String[] { "x-Hitachi-KEIS83-ShiftSpaceSingle-HanyoDenshi-EBCDIK" }, 1, 1, 0, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_HD_PLUS_EBCDIK("x-Hitachi-KEIS83-ShiftSpaceSingle-HanyoDenshi+EBCDIK", new String[0], 1, 1, 0, true, true, HitachiCharset::new),
	HITACHI_EBCDIC_PLUS_KEIS83_SSS_AJ1("x-Hitachi-EBCDIC+KEIS83-ShiftSpaceSingle-AdobeJapan1", new String[] { "x-Hitachi-KEIS83-ShiftSpaceSingle-AdobeJapan1-EBCDIC" }, 0, 1, 1, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_AJ1_PLUS_EBCDIC("x-Hitachi-KEIS83-ShiftSpaceSingle-AdobeJapan1+EBCDIC", new String[0], 0, 1, 1, true, true, HitachiCharset::new),
	HITACHI_EBCDIK_PLUS_KEIS83_SSS_AJ1("x-Hitachi-EBCDIK+KEIS83-ShiftSpaceSingle-AdobeJapan1", new String[] { "x-Hitachi-KEIS83-ShiftSpaceSingle-AdobeJapan1-EBCDIK" }, 1, 1, 1, true, HitachiCharset::new),
	HITACHI_KEIS83_SSS_AJ1_PLUS_EBCDIK("x-Hitachi-KEIS83-ShiftSpaceSingle-AdobeJapan1+EBCDIK", new String[0], 1, 1, 1, true, true, HitachiCharset::new),

	NEC_JIS8("x-NEC-JIS8", new String[0], 0, -1, -1, NecCharset::new),
	NEC_EBCDIK("x-NEC-EBCDIK", new String[0], 1, -1, -1, NecCharset::new),
	NEC_JIPSJ("x-NEC-JIPSJ", new String[0], -1, 0, -1, NecCharset::new),
	NEC_JIPSJ_HD("x-NEC-JIPSJ-HanyoDenshi", new String[0], -1, 0, 0, NecCharset::new),
	NEC_JIPSJ_AJ1("x-NEC-JIPSJ-AdobeJapan1", new String[0], -1, 0, 1, NecCharset::new),
	NEC_JIS8_PLUS_JIPSJ("x-NEC-JIS8+JIPSJ", new String[] { "x-NEC-JIPSJ-JIS8" }, 0, 0, -1, NecCharset::new),
	NEC_JIPSJ_PLUS_JIS8("x-NEC-JIPSJ+JIS8", new String[0], 0, 0, -1, true, false, NecCharset::new),
	NEC_JIS8_PLUS_JIPSJ_HD("x-NEC-JIS8+JIPSJ-HanyoDenshi", new String[] { "x-NEC-JIPSJ-HanyoDenshi-JIS8" }, 0, 0, 0, NecCharset::new),
	NEC_JIPSJ_HD_PLUS_JIS8("x-NEC-JIPSJ-HanyoDenshi+JIS8", new String[0], 0, 0, 0, true, false, NecCharset::new),
	NEC_JIS8_PLUS_JIPSJ_AJ1("x-NEC-JIS8+JIPSJ-AdobeJapan1", new String[] { "x-NEC-JIPSJ-AdobeJapan1-JIS8" }, 0, 0, 1, NecCharset::new),
	NEC_JIPSJ_AJ1_PLUS_JIS8("x-NEC-JIPSJ-AdobeJapan1+JIS8", new String[0], 0, 0, 1, true, false, NecCharset::new),
	NEC_JIPSE("x-NEC-JIPSE", new String[0], -1, 1, -1, NecCharset::new),
	NEC_JIPSE_HD("x-NEC-JIPSE-HanyoDenshi", new String[0], -1, 1, 0, NecCharset::new),
	NEC_JIPSE_AJ1("x-NEC-JIPSE-AdobeJapan1", new String[0], -1, 1, 1, NecCharset::new),
	NEC_EBCDIK_PLUS_JIPSE("x-NEC-EBCDIK+JIPSE", new String[] { "x-NEC-JIPSE-EBCDIK" }, 1, 1, -1, NecCharset::new),
	NEC_JIPSE_PLUS_EBCDIK("x-NEC-JIPSE+EBCDIK", new String[0], 1, 1, -1, true, false, NecCharset::new),
	NEC_EBCDIK_PLUS_JIPSE_HD("x-NEC-EBCDIK+JIPSE-HanyoDenshi", new String[] { "x-NEC-JIPSE-HanyoDenshi-EBCDIK" }, 1, 1, 0, NecCharset::new),
	NEC_JIPSE_HD_PLUS_EBCDIK("x-NEC-JIPSE-HanyoDenshi+EBCDIK", new String[0], 1, 1, 0, true, false, NecCharset::new),
	NEC_EBCDIK_PLUS_JIPSE_AJ1("x-NEC-EBCDIK+JIPSE-AdobeJapan1", new String[] { "x-NEC-JIPSE-AdobeJapan1-EBCDIK" }, 1, 1, 1, NecCharset::new),
	NEC_JIPSE_AJ1_PLUS_EBCDIK("x-NEC-JIPSE-AdobeJapan1+EBCDIK", new String[0], 1, 1, 1, true, false, NecCharset::new),

	IBM_8482("x-IBM-8482", new String[0], 0, -1, -1, IbmCharset::new),
	IBM_5123("x-IBM-5123", new String[0], 1, -1, -1, IbmCharset::new),
	IBM_11684("x-IBM-11684", new String[0], -1, 0, -1, IbmCharset::new),
	IBM_8482_PLUS_11684("x-IBM-8482+11684", new String[] { "x-IBM-1390" }, 0, 0, -1, IbmCharset::new),
	IBM_11684_PLUS_8482("x-IBM-11684+8482", new String[0], 0, 0, -1, true, false, IbmCharset::new),
	IBM_5123_PLUS_11684("x-IBM-5123+11684", new String[] { "x-IBM-1399" }, 1, 0, -1, IbmCharset::new),
	IBM_11684_PLUS_5123("x-IBM-11684+5123", new String[0], 1, 0, -1, true, false, IbmCharset::new),

	WINDOWS_31J_RT("x-Windows-31J-RoundTrip", new String[0], -1, -1, -1,
			type -> new RoundTripCharset(type, Charset.forName("Windows-31J"),
					new char[] {
						'\u00A2', '\u00A3', '\u00A5', '\u00AB', '\u00AC', '\u00AF',
						'\u00B5', '\u00B7', '\u00B8', '\u00BB', '\u203E', '\u3094'
					},
					new int[] {
						0x8790, 0x8792, 0x8795, 0x8797, 0x879A, 0x879C,
						0xED40, 0xED7E, 0xED80, 0xEDFC, 0xEE40, 0xEE7E,
						0xEE80, 0xEEEC, 0xEEEF, 0xEEFC, 0xFA4A, 0xFA54, 0xFA58, 0xFA5B
					},
					b -> (b >= 0x81 && b <= 0x9F) || (b >= 0xE0 && b <= 0xFC) ? 2 : 1)),
	EUC_JP_OPEN_RT("x-eucJP-Open-RoundTrip", new String[0], -1, -1, -1,
			type -> new RoundTripCharset(type, Charset.forName("x-eucJP-open"),
					new char[] { '\u00A5', '\u203E' },
					new int[] {
						0xADE2, 0xADE2, 0xADF0, 0xADF2, 0xADF5, 0xADF7, 0xADFA, 0xADFC,
						0xF9A1, 0xF9FE, 0xFAA1, 0xFAFE, 0xFBA1, 0xFBFE,
						0xFCA1, 0xFCEE, 0xFCF1, 0xFCFE,
						0x8FF3FD, 0x8FF3FE, 0x8FF4A1, 0x8FF4A8, 0x8FF4AB, 0x8FF4AD
					},
					b -> b < 0x80 ? 1 : b == 0x8F ? 3 : 2));

	private static final Map<String, CharsetType> TYPES_BY_NAME;

	static {
		Map<String, CharsetType> typesByName = new HashMap<>();
		for (CharsetType type : values()) {
			typesByName.put(type.charsetName.toLowerCase(Locale.ROOT), type);
			for (String alias : type.aliases) {
				typesByName.put(alias.toLowerCase(Locale.ROOT), type);
			}
		}
		TYPES_BY_NAME = Collections.unmodifiableMap(typesByName);
	}

	private final String charsetName;
	private final String[] aliases;
	private final int sbcsTableNo;
	private final int mbcsTableNo;
	private final int ivsTableNo;
	private final boolean mbcsPreferred;
	private final boolean shiftSpaceSingle;
	private final Function<CharsetType, Charset> charsetFactory;

	CharsetType(String charsetName, String[] aliases, int sbcsTableNo, int mbcsTableNo, int ivsTableNo,
			Function<CharsetType, Charset> charsetFactory) {
		this(charsetName, aliases, sbcsTableNo, mbcsTableNo, ivsTableNo, false, false, charsetFactory);
	}

	CharsetType(String charsetName, String[] aliases, int sbcsTableNo, int mbcsTableNo, int ivsTableNo,
			boolean shiftSpaceSingle, Function<CharsetType, Charset> charsetFactory) {
		this(charsetName, aliases, sbcsTableNo, mbcsTableNo, ivsTableNo, false, shiftSpaceSingle, charsetFactory);
	}

	CharsetType(String charsetName, String[] aliases, int sbcsTableNo, int mbcsTableNo, int ivsTableNo,
			boolean mbcsPreferred, boolean shiftSpaceSingle, Function<CharsetType, Charset> charsetFactory) {
		this.charsetName = charsetName;
		this.aliases = aliases.clone();
		this.sbcsTableNo = sbcsTableNo;
		this.mbcsTableNo = mbcsTableNo;
		this.ivsTableNo = ivsTableNo;
		this.mbcsPreferred = mbcsPreferred;
		this.shiftSpaceSingle = shiftSpaceSingle;
		this.charsetFactory = charsetFactory;
	}

	String getCharsetName() {
		return charsetName;
	}

	String[] getAliases() {
		return aliases.clone();
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

	boolean isMBCSPreferred() {
		return mbcsPreferred;
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
