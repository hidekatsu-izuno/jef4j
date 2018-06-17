package net.arnx.jef4j.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Converter {
	public static void main(String[] args) throws Exception {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream("./base.txt"), 
				StandardCharsets.UTF_8))) {
			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("./base_result.txt"), 
					StandardCharsets.UTF_8))) {
				
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.isEmpty()) {
						continue;
					}
					
					String[] parts = line.split(" ");
					for (int i = 0; i < parts.length; i++) {
						if (i > 0) {
							writer.append(" ");
						}
						if (i == 2) {
							StringBuilder sb = new StringBuilder();
							for (String c : parts[0].split("_")) {
								sb.appendCodePoint(Integer.parseUnsignedInt(c, 16));
							}
							writer.append(sb.toString());
						} else {
							writer.append(parts[i]);
						}
					}
					writer.append("\n");
				}
			}
		}
		
	}
}
