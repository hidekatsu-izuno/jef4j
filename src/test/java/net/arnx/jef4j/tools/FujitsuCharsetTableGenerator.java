package net.arnx.jef4j.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class FujitsuCharsetTableGenerator {
	public static void main(String[] args) throws IOException {
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper();

		try (BufferedWriter out = Files.newBufferedWriter(Paths.get("docs/mappings.html"), StandardCharsets.UTF_8)) {
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
			out.append(".charmap td { border: 1px solid black; text-align: center; vertical-align: middle; width: 32px; height: 22px; line-height: 1; }\n");
			out.append(".charmap th { font-weight: bold; background: #C1FFFF; font-family: monospace; }\n");
			out.append(".special { font-size: 12px; font-family: monospace; }\n");
			out.append(".nogriph { font-size: 9px; font-family: monospace; }\n");
			out.append(".unmapped { background: silver; }\n");
			out.append("</style>\n");
			out.append("</head>\n");
			out.append("<body>\n");
			
			for (String[] pair : new String[][] {
				{ "/fujitsu_ebcdic_mapping.json", "x-Fujitsu-EBCDIC" },
				{ "/fujitsu_ebcdik_mapping.json", "x-Fujitsu-EBCDIK" },
				{ "/fujitsu_ascii_mapping.json", "x-Fujitsu-ASCII" }
			}) {
				Map<Integer, String> map = new HashMap<>();
				
				try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
						FujitsuCharsetTableGenerator.class.getResourceAsStream(pair[0]), 
						StandardCharsets.UTF_8)))) {
					while (parser.nextToken() != JsonToken.END_ARRAY) {
						if (parser.currentToken() == JsonToken.START_OBJECT) {
		                    JsonNode node = mapper.readTree(parser);
							map.put(
								Integer.parseUnsignedInt(node.get("ebcdic").asText(), 16), 
								node.get("text").asText()
							);
						}
					}
				}
				
				out.append("<table class=\"charmap\">\n");
				out.append("<caption>" + pair[1] + "</caption>\n");
				out.append("<tr>");
				out.append("<th><sub>H</sub>&nbsp;<sup>L</sup></th>");
				for (int j = 0; j <= 0xF; j++) {
					out.append("<th>" + ByteUtils.hex(j, 1) + "</th>");
				}
				out.append("</tr>\n");
				for (int i = 0; i <= 0xF; i++) {
					out.append("<tr>");
					out.append("<th>" + ByteUtils.hex(i, 1) + "</th>");
					for (int j = 0; j <= 0xF; j++) {
						String value = map.get((i << 4) | j);
						if (value == null) {
							out.append("<td class=\"unmapped\">&nbsp;</td>");
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
	
			{
				Map<Integer, String> map = new HashMap<>();
				
				try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
						FujitsuCharsetTableGenerator.class.getResourceAsStream("/fujitsu_jef_mapping.json"), 
						StandardCharsets.UTF_8)))) {
					while (parser.nextToken() != JsonToken.END_ARRAY) {
						if (parser.currentToken() == JsonToken.START_OBJECT) {
		                    JsonNode node = mapper.readTree(parser);
							map.put(
								Integer.parseUnsignedInt(node.get("jef").asText(), 16), 
								node.get("text").asText()
							);
						}
					}
				}
				
				out.append("<table class=\"charmap\">\n");
				out.append("<caption>JEF標準漢字/標準非漢字</caption>\n");
				for (int i = 0xA1; i <= 0xFE; i++) {
					out.append("<tr>");
					out.append("<th><sub>H</sub>&nbsp;<sup>L</sup></th>");
					for (int j = 0x0; j <= 0xF; j++) {
						out.append("<th>" + ByteUtils.hex(j, 1) + "</th>");
					}
					out.append("</tr>\n");
					
					for (int i2 = 0xA; i2 <= 0xF; i2++) {
						out.append("<tr>");
						out.append("<th>" + ByteUtils.hex((i << 4) | i2, 3) + "</th>");
						for (int j = 0x0; j <= 0xF; j++) {
							String value = map.get((i << 8) | (i2 << 4) | j);
							if (value == null) {
								out.append("<td class=\"unmapped\">&nbsp;</td>");
							} else {
								out.append("<td class=\"char\">" + value + "</td>");
							}
						}
						out.append("</tr>\n");
					}
				}
				out.append("<table>\n");
				
				out.append("<table class=\"charmap\">\n");
				out.append("<caption>JEF拡張漢字/拡張非漢字</caption>\n");
				for (int i = 0x41; i <= 0x7F; i++) {
					out.append("<tr>");
					out.append("<th><sub>H</sub>&nbsp;<sup>L</sup></th>");
					for (int j = 0x0; j <= 0xF; j++) {
						out.append("<th>" + ByteUtils.hex(j, 1) + "</th>");
					}
					out.append("</tr>\n");
					
					for (int i2 = 0xA; i2 <= 0xF; i2++) {
						out.append("<tr>");
						out.append("<th>" + ByteUtils.hex((i << 4) | i2, 3) + "</th>");
						for (int j = 0x0; j <= 0xF; j++) {
							String value = map.get((i << 8) | (i2 << 4) | j);
							if (value == null) {
								out.append("<td class=\"unmapped\">&nbsp;</td>");
							} else if (value.length() > 1 && (value.startsWith("[") || value.startsWith("(") || value.startsWith("{"))) {
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
}
