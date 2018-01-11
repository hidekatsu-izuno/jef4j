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

import net.arnx.jef4j.util.ByteUtils;

public class FujitsuCharsetTableGenerator {
	public static void main(String[] args) throws IOException {
		try (BufferedWriter out = Files.newBufferedWriter(Paths.get("docs/mappings.html"), StandardCharsets.UTF_8)) {
			out.append("<!doctype html>\n");
			out.append("<html lang=\"ja\">\n");
			out.append("<head>\n");
			out.append("<meta charset=\"UTF-8\">\n");
			out.append("<style>\n");
			out.append(".charmap { table-layout: fixed; border-collapse: collapse; font-size: 14px; font-family: monospace; margin-bottom: 16px; }\n");
			out.append(".charmap caption { line-height: 1.4; font-family: sans-serif; }\n");
			out.append(".charmap th,\n");
			out.append(".charmap td { border: 1px solid black; text-align: center; vertical-align: middle; width: 20px; height: 20px; line-height: 1; }\n");
			out.append(".charmap th { font-weight: bold; background: #C1FFFF; }\n");
			out.append(".special { font-size: 8px; }\n");
			out.append(".unmapped { background: silver; }\n");
			out.append("</style>\n");
			out.append("</head>\n");
			out.append("<body>\n");
			
			for (String[] pair : new String[][] {
				{ "/ebcdic_mapping.txt", "x-Fujitsu-EBCDIC" },
				{ "/ebcdik_mapping.txt", "x-Fujitsu-EBCDIK" },
				{ "/ascii_mapping.txt", "x-Fujitsu-ASCII" }
			}) {
				Map<Integer, String> map = new HashMap<>();
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(
						FujitsuCharsetMapGenerator.class.getResourceAsStream(pair[0]), 
						StandardCharsets.UTF_8))) {
					String line;
					while ((line = reader.readLine()) != null) {
						if (line.isEmpty()) continue;
						
						String[] parts = line.split(" ");
						map.put(Integer.parseUnsignedInt(parts[1], 16), parts[2]);
					}
				}
				
				out.append("<table class=\"charmap\">\n");
				out.append("<caption>" + pair[1] + "</caption>\n");
				out.append("<tr>");
				out.append("<th><sub>H</sub><sup>L</sup></th>");
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
						} else if (value.length() > 1) {
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
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(
						FujitsuCharsetMapGenerator.class.getResourceAsStream("/jef_mapping.txt"), 
						StandardCharsets.UTF_8))) {
					String line;
					while ((line = reader.readLine()) != null) {
						if (line.isEmpty()) continue;
						
						String[] parts = line.split(" ");
						map.put(Integer.parseUnsignedInt(parts[1], 16), parts[2]);
					}
				}
				
				out.append("<table class=\"charmap\">\n");
				out.append("<caption>JEF標準漢字/標準非漢字</caption>\n");
				out.append("<tr>");
				out.append("<th><sub>H</sub><sup>L</sup></th>");
				for (int j = 0x0; j <= 0xF; j++) {
					out.append("<th>" + ByteUtils.hex(j, 1) + "</th>");
				}
				out.append("</tr>\n");
				for (int i = 0xA1; i <= 0xFE; i++) {
					for (int i2 = 0xA; i2 <= 0xF; i2++) {
						out.append("<tr>");
						out.append("<th>" + ByteUtils.hex((i << 4) | i2, 3) + "</th>");
						for (int j = 0x0; j <= 0xF; j++) {
							String value = map.get((i << 8) | (i2 << 4) | j);
							if (value == null) {
								out.append("<td class=\"unmapped\">&nbsp;</td>");
							} else if (value.length() > 1) {
								out.append("<td class=\"special\">" + value + "</td>");
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
				out.append("<tr>");
				out.append("<th><sub>H</sub><sup>L</sup></th>");
				for (int j = 0x0; j <= 0xF; j++) {
					out.append("<th>" + ByteUtils.hex(j, 1) + "</th>");
				}
				out.append("</tr>\n");
				for (int i = 0x41; i <= 0x7F; i++) {
					for (int i2 = 0xA; i2 <= 0xF; i2++) {
						out.append("<tr>");
						out.append("<th>" + ByteUtils.hex((i << 4) | i2, 3) + "</th>");
						for (int j = 0x0; j <= 0xF; j++) {
							String value = map.get((i << 8) | (i2 << 4) | j);
							if (value == null) {
								out.append("<td class=\"unmapped\">&nbsp;</td>");
							} else if (value.length() > 1) {
								out.append("<td class=\"special\">" + value + "</td>");
							} else {
								out.append("<td class=\"char\">" + value + "</td>");
							}
						}
						out.append("</tr>\n");
					}
				}
				out.append("<table>\n");
			}
			
			out.append("</body>\n");
			out.append("</html>\n");
		}
	}
}
