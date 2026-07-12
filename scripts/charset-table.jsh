import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

var mapper = new ObjectMapper();

String hex(int value, int width) {
    return String.format("%0" + width + "X", value);
}

record CharsetInfo(String name, String mapping, int start1, int end1, int start2, int end2) {}

Map<Integer, String> mapping(CharsetInfo info) throws IOException {
    Map<Integer, String> result = new HashMap<>();
    for (var node : mapper.readTree(Files.newBufferedReader(Paths.get("src/test/resources", info.mapping())))) {
        var text = node.get("text").asText();
        if ("(Undefined)".equals(text) || "(Reserved)".equals(text)) text = "U+" + node.get("unicode").asText();
        result.put(Integer.parseUnsignedInt(node.get("code").asText(), 16), text);
    }
    return result;
}

void cell(BufferedWriter out, String value, boolean sbcs) throws IOException {
    if (value == null) out.append("<td class=\"unmapped\">&nbsp;</td>");
    else if (value.startsWith("U+") || (!sbcs && (value.startsWith("[") || value.startsWith("(") || value.startsWith("{")))) out.append("<td class=\"nogriph\">").append(value).append("</td>");
    else if (sbcs && value.length() > 1 && !Character.isSurrogate(value.charAt(0))) out.append("<td class=\"special\">").append(value).append("</td>");
    else out.append("<td class=\"char\">").append(value).append("</td>");
}

void table(String title, String file, CharsetInfo[] sbcs, CharsetInfo[] mbcs) throws IOException {
    try (var out = Files.newBufferedWriter(Paths.get(file), StandardCharsets.UTF_8)) {
        out.append("<!doctype html>\n<html lang=\"ja\">\n<head>\n<meta charset=\"UTF-8\">\n<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n<link href=\"https://fonts.googleapis.com/css2?family=Noto+Sans+JP:wght@100..900&family=Noto+Serif+Hentaigana:wght@200..900&display=swap\" rel=\"stylesheet\">\n<style>\nbody { font-family: \"Noto Sans JP\", \"Noto Serif Hentaigana\"; }\n.charmap { table-layout: fixed; border-collapse: collapse; font-size: 16px; margin-bottom: 16px; }\n.charmap caption { line-height: 1.4; font-family: sans-serif; }\n.charmap th,\n.charmap td { border: 1px solid black; text-align: center; vertical-align: middle; width: 48px; height: 22px; line-height: 1; }\n.charmap th { font-weight: bold; background: #C1FFFF; font-family: monospace; }\n.special { font-size: 12px; font-family: monospace; }\n.nogriph { font-size: 9px; font-family: monospace; }\n.unmapped { background: silver; }\n</style>\n</head>\n<body>\n<h1>").append(title).append("</h1>\n");
        for (var info : sbcs) {
            var map = mapping(info);
            out.append("<table class=\"charmap\">\n<caption>").append(info.name()).append("</caption>\n<tr><th><sub>H</sub>&nbsp;<sup>L</sup></th>");
            for (var j = info.start2(); j <= info.end2(); j++) out.append("<th>").append(hex(j, 1)).append("</th>");
            out.append("</tr>\n");
            for (var i = info.start1(); i <= info.end1(); i++) {
                out.append("<tr><th>").append(hex(i, 1)).append("</th>");
                for (var j = info.start2(); j <= info.end2(); j++) cell(out, map.get((i << 4) | j), true);
                out.append("</tr>\n");
            }
            out.append("<table>\n");
        }
        for (var info : mbcs) {
            var map = mapping(info);
            out.append("<table class=\"charmap\">\n<caption>").append(info.name()).append("</caption>\n");
            for (var i = info.start1(); i <= info.end1(); i++) {
                out.append("<tr><th><sub>H</sub>&nbsp;<sup>L</sup></th>");
                for (var j = 0; j <= 0xf; j++) out.append("<th>").append(hex(j, 1)).append("</th>");
                out.append("</tr>\n");
                for (var i2 = info.start2(); i2 <= info.end2(); i2++) {
                    out.append("<tr><th>").append(hex((i << 4) | i2, 3)).append("</th>");
                    for (var j = 0; j <= 0xf; j++) cell(out, map.get((i << 8) | (i2 << 4) | j), false);
                    out.append("</tr>\n");
                }
            }
            out.append("</table>\n");
        }
        out.append("</body>\n</html>\n");
    }
}

table("富士通文字コード表", "docs/fujitsu_mappings.html",
    new CharsetInfo[] { 
        new CharsetInfo("x-Fujitsu-EBCDIC: 富士通 EBCDIC (英小文字)", "fujitsu_ebcdic_mapping.json", 0, 0xf, 0, 0xf), 
        new CharsetInfo("x-Fujitsu-EBCDIK: 富士通 EBCDIC (カナ文字)", "fujitsu_ebcdik_mapping.json", 0, 0xf, 0, 0xf), 
        new CharsetInfo("x-Fujitsu-ASCII: 富士通 EBCDIC (ASCII)", "fujitsu_ascii_mapping.json", 0, 0xf, 0, 0xf)
    },
    new CharsetInfo[] {
        new CharsetInfo("x-Fujitsu-JEF: 富士通 JEF 標準漢字/標準非漢字", "fujitsu_jef_mapping.json", 0xa1, 0xfe, 0xa, 0xf), 
        new CharsetInfo("x-Fujitsu-JEF: 富士通 JEF 拡張漢字/拡張非漢字", "fujitsu_jef_mapping.json", 0x41, 0x7f, 0xa, 0xf)
    }
);
table("日立文字コード表", "docs/hitachi_mappings.html",
    new CharsetInfo[] {
        new CharsetInfo("x-Hitachi-EBCDIC: 日立 EBCDIC", "hitachi_ebcdic_mapping.json", 0, 0xf, 0, 0xf), 
        new CharsetInfo("x-Hitachi-EBCDIK: 日立 EBCDIK", "hitachi_ebcdik_mapping.json", 0, 0xf, 0, 0xf) 
    },
    new CharsetInfo[] {
        new CharsetInfo("x-Hitachi-KEIS78: 日立 KEIS78 基本文字セット(非漢字)", "hitachi_keis78_mapping.json", 0xa1, 0xac, 0xa, 0xf),
        new CharsetInfo("x-Hitachi-KEIS78: 日立 KEIS78 基本文字セット(漢字)", "hitachi_keis78_mapping.json", 0xb0, 0xce, 0xa, 0xf),
        new CharsetInfo("x-Hitachi-KEIS78: 日立 KEIS78 拡張文字セット1", "hitachi_keis78_mapping.json", 0xd1, 0xfe, 0xa, 0xf),
        new CharsetInfo("x-Hitachi-KEIS78: 日立 KEIS78 拡張文字セット3", "hitachi_keis78_mapping.json", 0x59, 0x80, 0xa, 0xf),
        new CharsetInfo("x-Hitachi-KEIS83: 日立 KEIS83/90 基本文字セット(非漢字)", "hitachi_keis83_mapping.json", 0xa1, 0xac, 0xa, 0xf),
        new CharsetInfo("x-Hitachi-KEIS83: 日立 KEIS83/90 基本文字セット(漢字)", "hitachi_keis83_mapping.json", 0xb0, 0xce, 0xa, 0xf),
        new CharsetInfo("x-Hitachi-KEIS83: 日立 KEIS83/90 拡張文字セット1", "hitachi_keis83_mapping.json", 0xd1, 0xfe, 0xa, 0xf),
        new CharsetInfo("x-Hitachi-KEIS83: 日立 KEIS83/90 拡張文字セット3", "hitachi_keis83_mapping.json", 0x59, 0x80, 0xa, 0xf)
    }
);
table("NEC文字コード表", "docs/nec_mappings.html",
    new CharsetInfo[] {
        new CharsetInfo("x-NEC-EBCDIK: NEC EBCDIC カタカナ", "nec_ebcdik_mapping.json", 0, 0xf, 0, 0xf),
        new CharsetInfo("x-NEC-JIS8: NEC JIS8 (JIS X0201)", "nec_jis8_mapping.json", 0, 0xf, 0, 0xf)
    },
    new CharsetInfo[] {
        new CharsetInfo("x-NEC-JIPSJ: JIPS(J) G0集合", "nec_jips_mapping.json", 0x21, 0x73, 0x2, 0x7),
        new CharsetInfo("x-NEC-JIPSJ: JIPS(J) G1集合", "nec_jips_mapping.json", 0xa1, 0xdf, 0xa, 0xf),
        new CharsetInfo("x-NEC-JIPSJ: JIPS(J) G2集合", "nec_jips_mapping.json", 0xa1, 0xfe, 0xa, 0xf)
    }
);
table("IBM文字コード表", "docs/ibm_mappings.html",
    new CharsetInfo[] {
        new CharsetInfo("x-IBM-8482: IBM EBCDIC 日本語カタカナ（ユーロ対応）", "ibm_8482_mapping.json", 0, 0xf, 0, 0xf),
        new CharsetInfo("x-IBM-5123: IBM EBCDIC 日本語ラテン（ユーロ対応）", "ibm_5123_mapping.json", 0, 0xf, 0, 0xf),
    },
    new CharsetInfo[] {
        new CharsetInfo("x-IBM-11684: IBM漢字（4040–68FF）", "ibm_11684_mapping.json", 0x40, 0x68, 0x4, 0xf),
        new CharsetInfo("x-IBM-11684: IBM漢字（B300–ECFF）", "ibm_11684_mapping.json", 0xb3, 0xec, 0x4, 0xf),
    }
);

/exit
