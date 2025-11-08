package net.arnx.jef4j.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.arnx.jef4j.util.ByteUtils;

public class CharsetTableGenerator {
	public static void main(String[] args) throws IOException {
		writeCharsetTable(
			"富士通文字コード表",
			"docs/fujitsu_mappings.html",
			new CharsetInfo[] {
				new CharsetInfo("x-Fujitsu-EBCDIC: 富士通 EBCDIC (英小文字)", "/fujitsu_ebcdic_mapping.json", 0x0, 0xF, 0x0, 0xF),
				new CharsetInfo("x-Fujitsu-EBCDIK: 富士通 EBCDIC (カナ文字)", "/fujitsu_ebcdik_mapping.json", 0x0, 0xF, 0x0, 0xF),
				new CharsetInfo("x-Fujitsu-ASCII: 富士通 EBCDIC (ASCII)", "/fujitsu_ascii_mapping.json", 0x0, 0xF, 0x0, 0xF)
			},
			new CharsetInfo[] {
				new CharsetInfo("x-Fujitsu-JEF: 富士通 JEF 標準漢字/標準非漢字", "/fujitsu_jef_mapping.json", 0xA1, 0xFE, 0x0, 0xF),
				new CharsetInfo("x-Fujitsu-JEF: 富士通 JEF 拡張漢字/拡張非漢字", "/fujitsu_jef_mapping.json", 0x41, 0x7F, 0x0, 0xF)
			}
		);

		writeCharsetTable(
			"日立文字コード表",
			"docs/hitachi_mappings.html",
			new CharsetInfo[] {
				new CharsetInfo("x-Hitachi-EBCDIC: 日立 EBCDIC", "/hitachi_ebcdic_mapping.json", 0x0, 0xF, 0x0, 0xF),
				new CharsetInfo("x-Hitachi-EBCDIK: 日立 EBCDIK", "/hitachi_ebcdik_mapping.json", 0x0, 0xF, 0x0, 0xF),
			},
			new CharsetInfo[] {
				new CharsetInfo("x-Hitachi-KEIS78: 日立 KEIS78 基本文字セット(非漢字)", "/hitachi_keis78_mapping.json", 0xA1, 0xAC, 0x0, 0xF),
				new CharsetInfo("x-Hitachi-KEIS78: 日立 KEIS78 基本文字セット(漢字)", "/hitachi_keis78_mapping.json", 0xB0, 0xCE, 0x0, 0xF),
				new CharsetInfo("x-Hitachi-KEIS78: 日立 KEIS78 拡張文字セット1", "/hitachi_keis78_mapping.json", 0xD1, 0xFE, 0x0, 0xF),
				new CharsetInfo("x-Hitachi-KEIS78: 日立 KEIS78 拡張文字セット3", "/hitachi_keis78_mapping.json", 0x59, 0x80, 0x0, 0xF),
				new CharsetInfo("x-Hitachi-KEIS83: 日立 KEIS83/90 基本文字セット(非漢字)", "/hitachi_keis83_mapping.json", 0xA1, 0xAC, 0x0, 0xF),
				new CharsetInfo("x-Hitachi-KEIS83: 日立 KEIS83/90 基本文字セット(漢字)", "/hitachi_keis83_mapping.json", 0xB0, 0xCE, 0x0, 0xF),
				new CharsetInfo("x-Hitachi-KEIS83: 日立 KEIS83/90 拡張文字セット1", "/hitachi_keis83_mapping.json", 0xD1, 0xFE, 0x0, 0xF),
				new CharsetInfo("x-Hitachi-KEIS83: 日立 KEIS83/90 拡張文字セット3", "/hitachi_keis83_mapping.json", 0x59, 0x80, 0x0, 0xF),
			}
		);

		writeCharsetTable(
			"NEC文字コード表",
			"docs/nec_mappings.html",
			new CharsetInfo[] {
				new CharsetInfo("x-NEC-EBCDIK: NEC EBCDIC カタカナ", "/nec_ebcdik_mapping.json", 0x0, 0xF, 0x0, 0xF)
			},
			new CharsetInfo[] {
				new CharsetInfo("x-NEC-JIPSJ: JIPS(J) G0集合", "/nec_jips_mapping.json", 0x21, 0x73, 0x0, 0x7),
				new CharsetInfo("x-NEC-JIPSJ: JIPS(J) G1集合", "/nec_jips_mapping.json", 0xA1, 0xDF, 0x8, 0xF),
				new CharsetInfo("x-NEC-JIPSJ: JIPS(J) G2集合", "/nec_jips_mapping.json", 0xA1, 0xFE, 0x8, 0xF)
			}
		);
	}

	public static void writeCharsetTable(
		String title,
		String outFile,
		CharsetInfo[] sbcsList,
		CharsetInfo[] mbcsList
	) throws IOException {
		try (BufferedWriter out = Files.newBufferedWriter(Paths.get(outFile), StandardCharsets.UTF_8)) {
			out.append("<!doctype html>\n");
			out.append("<html lang=\"ja\">\n");
			out.append("<head>\n");
			out.append("<meta charset=\"UTF-8\">\n");
			out.append("<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n");
			out.append("<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n");
			out.append("<link href=\"https://fonts.googleapis.com/css2?family=Noto+Sans+JP:wght@100..900&family=Noto+Serif+Hentaigana:wght@200..900&display=swap\" rel=\"stylesheet\">\n");
			out.append("<style>\n");
			out.append("body { font-family: \"Noto Sans JP\", \"Noto Serif Hentaigana\"; }\n");
			out.append(".charmap { table-layout: fixed; border-collapse: collapse; font-size: 16px; margin-bottom: 16px; }\n");
			out.append(".charmap caption { line-height: 1.4; font-family: sans-serif; }\n");
			out.append(".charmap th,\n");
			out.append(".charmap td { border: 1px solid black; text-align: center; vertical-align: middle; width: 48px; height: 22px; line-height: 1; }\n");
			out.append(".charmap th { font-weight: bold; background: #C1FFFF; font-family: monospace; }\n");
			out.append(".special { font-size: 12px; font-family: monospace; }\n");
			out.append(".nogriph { font-size: 9px; font-family: monospace; }\n");
			out.append(".unmapped { background: silver; }\n");
			out.append("</style>\n");
			out.append("</head>\n");
			out.append("<body>\n");
			out.append("<h1>" + title + "</h1>\n");
			
			for (CharsetInfo csInfo : sbcsList) {
				Map<Integer, String> map = csInfo.getMapping();
				
				out.append("<table class=\"charmap\">\n");
				out.append("<caption>" + csInfo.name + "</caption>\n");
				out.append("<tr>");
				out.append("<th><sub>H</sub>&nbsp;<sup>L</sup></th>");
				for (int j = 0; j <= 0xF; j++) {
					out.append("<th>" + ByteUtils.hex(j, 1) + "</th>");
				}
				out.append("</tr>\n");
				for (int i = csInfo.start; i <= csInfo.end; i++) {
					out.append("<tr>");
					out.append("<th>" + ByteUtils.hex(i, 1) + "</th>");
					for (int j = csInfo.start2; j <= csInfo.end2; j++) {
						String value = map.get((i << 4) | j);
						if (value == null) {
							out.append("<td class=\"unmapped\">&nbsp;</td>");
						} else if (value.startsWith("U+")) {
							out.append("<td class=\"nogriph\">" + value + "</td>");
						} else if (value.length() > 1 && !Character.isSurrogate(value.charAt(0))) {
							out.append("<td class=\"special\">" + value + "</td>");
						} else {
							out.append("<td class=\"char\">" + value + "</td>");
						}
					}
					out.append("</tr>\n");
				}
				out.append("<table>\n");
			}

			for (CharsetInfo csInfo : mbcsList) {
				Map<Integer, String> map = csInfo.getMapping();

				out.append("<table class=\"charmap\">\n");
				out.append("<caption>" + csInfo.name + "</caption>\n");
				for (int i = csInfo.start; i <= csInfo.end; i++) {
					out.append("<tr>");
					out.append("<th><sub>H</sub>&nbsp;<sup>L</sup></th>");
					for (int j = 0x0; j <= 0xF; j++) {
						out.append("<th>" + ByteUtils.hex(j, 1) + "</th>");
					}
					out.append("</tr>\n");
					
					for (int i2 = csInfo.start2; i2 <= csInfo.end2; i2++) {
						out.append("<tr>");
						out.append("<th>" + ByteUtils.hex((i << 4) | i2, 3) + "</th>");
						for (int j = 0x0; j <= 0xF; j++) {
							String value = map.get((i << 8) | (i2 << 4) | j);
							if (value == null) {
								out.append("<td class=\"unmapped\">&nbsp;</td>");
							} else if (value.startsWith("U+")
								|| value.startsWith("[")
								|| value.startsWith("(")
								|| value.startsWith("{")
							) {
								out.append("<td class=\"nogriph\">" + value + "</td>");
							} else {
								out.append("<td class=\"char\">" + value + "</td>");
							}
						}
						out.append("</tr>\n");
					}
				}
				out.append("</table>\n");
			}
			
			out.append("</body>\n");
			out.append("</html>\n");
		}
	}

	private static class CharsetInfo {
		static JsonFactory factory = new JsonFactory();
		static ObjectMapper mapper = new ObjectMapper();

		String name;
		String mapping;
		int start;
		int end;
		int start2;
		int end2;
		Map<Integer, String> map;

		public CharsetInfo(String name, String mapping, int start, int end, int start2, int end2) {
			this.name = name;
			this.mapping = mapping;
			this.start = start;
			this.end = end;
			this.start2 = start2;
			this.end2 = end2;
		}

		private Map<Integer, String> getMapping() {
			if (map != null) {
				return map;
			}

			map = new HashMap<>();
			try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
					CharsetTableGenerator.class.getResourceAsStream(mapping), 
					StandardCharsets.UTF_8)))) {
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					if (parser.currentToken() == JsonToken.START_OBJECT) {
						JsonNode node = mapper.readTree(parser);
						boolean decodeOnly = false;

						JsonNode optionsNode = node.get("options");
						if (optionsNode != null && optionsNode.isArray()) {
							for (JsonNode child : optionsNode) {
								if ("decode_only".equals(child.asText())) {
									decodeOnly = false;
								}
							}
						}

						if (!decodeOnly) {
							map.put(
								Integer.parseUnsignedInt(node.get("code").asText(), 16), 
								replaceText(node.get("text").asText(), node.get("unicode").asText())
							);
						}
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			return map;
		}

		private static String replaceText(String text, String code) {
			if ("(Undefined)".equals(text) || "(Reserved)".equals(text)) {
				return "U+" + code;
			}
			return text;
		}
	}
}
