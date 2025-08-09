package net.arnx.jef4j.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Converter {
	public static void main(String[] args) throws Exception {
		convertEBCDIC("src/test/resources/ascii_mapping.txt", "src/test/resources/ascii_mapping.json");
		convertEBCDIC("src/test/resources/ebcdic_mapping.txt", "src/test/resources/ebcdic_mapping.json");
		convertEBCDIC("src/test/resources/ebcdik_mapping.txt", "src/test/resources/ebcdik_mapping.json");
		convertJEF("src/test/resources/jef_mapping.txt", "src/test/resources/jef_mapping.json");
	}

	private static void convertEBCDIC(String from, String to) throws IOException {
		List<CharInfo> lines = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(from), StandardCharsets.UTF_8));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(to), StandardCharsets.UTF_8))
		) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}

				String[] parts = line.split(" ");
				CharInfo ci = new CharInfo();
				if (parts.length > 0) {
					ci.unicode = parts[0];
				}
				if (parts.length > 1) {
					ci.ebcdic = parts[1];
				}
				if (parts.length > 2) {
					ci.text = parts[2];
				}

				lines.add(ci);
			}

			writer.append("[\n");
			boolean first = true;
			for (CharInfo ci : lines) {
				if (!first) {
					writer.append(",\n");
				}
				writer.append(ci.toString());
				first = false;
			}
			writer.append("\n]\n");
		}
	}

	private static void convertJEF(String from, String to) throws IOException {
		Charset cs = Charset.forName("x-Fujitsu-JEF");
		List<CharInfo> lines = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(from), StandardCharsets.UTF_8));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(to), StandardCharsets.UTF_8))
		) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				
				String[] parts = line.split(" ");

				CharInfo ci = new CharInfo();
				if (parts.length > 0) {
					String[] unicode = parts[0].split("[_/]");
					if (unicode.length > 0) {
						ci.unicode = unicode[0];
					}
					if (unicode.length > 1) {
						if (unicode[1].startsWith("E")) {
							ci.hd = unicode[1];						
						} else {
							ci.sp = unicode[1];
						}
					}
					if (unicode.length > 2) {
						ci.aj1 = unicode[2];
					}
				}
				if (parts.length > 1) {
					ci.jef = parts[1];
				}
				if (parts.length > 2) {
					ci.text = parts[2];
				}
				if (parts.length > 3) {
					String[] options = parts[3].split(",");
					for (String option : options) {
						if (option.isEmpty()) {
							continue;
						}

						if ("SUB".equals(option) || "IVS".equals(option)) {
							option = "substitution";
						} else if ("NCM".equals(option)) {
							option = "unmappable";
						} else if ("CI".equals(option)) {
							option = "cjk_ci";
						}
						ci.options.add(option);
					}
				}

				lines.add(ci);
			}

			for (CharInfo ci : lines) {
				if (ci.options.contains("no_mapping")) {
					continue;
				}

				int j = Integer.parseUnsignedInt(ci.jef, 16);
				int u = Integer.parseUnsignedInt(ci.unicode, 16);
				byte[] in = new byte[] { (byte)((j & 0xFF00) >>> 8), (byte)(j & 0xFF) }; 
				String text;
				try {
					text = new String(in, cs);
					byte[] out = text.getBytes(cs);
					if (!Arrays.equals(in, out) || u != text.codePointAt(0)) {
						if (j >= 0xA1A2 && j <= 0xFEFE) {
							ci.options.add("irreversible");
						} else {
							ci.options.add("irreversible");
						}
					}
				} catch (Exception e) {
					ci.options.add("error: " + e.getMessage());
				}
			}

			writer.append("[\n");
			boolean first = true;
			for (CharInfo ci : lines) {
				if (!first) {
					writer.append(",\n");
				}
				writer.append(ci.toString());
				first = false;
			}
			writer.append("\n]\n");
		}
	}
}
