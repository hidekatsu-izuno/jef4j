package net.arnx.jef4j.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class FujitsuCharsetChecker {
	public static void main(String[] args) throws IOException {
		// For Checking
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				FujitsuCharsetMapGenerator.class.getResourceAsStream("/jef_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			
			Map<String, String> unicodeMap = new TreeMap<>();
			Set<String> jefMap = new TreeSet<>();
			
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				
				String[] parts = line.split(" ");
				String unicode = parts[0].replaceFirst("_.*$", "");
				String jef = parts[1];
				String chars = parts[2];
				String option = (parts.length > 3) ? parts[3] : "";
				
				if (!"FFFD".equals(unicode)) {
					if (unicodeMap.containsKey(unicode)) {
						if (unicodeMap.get(unicode).compareTo(jef) > 0) {
							System.err.println("Duplicate(U): " + unicode + " " + jef);
						}
					} else {
						unicodeMap.put(unicode, jef);
					}					 
				}
				
				if (!"CI".equals(option)) {
					if (jefMap.contains(jef)) {
						System.err.println("Duplicate(J):" + jef + " " + chars);
					} else {
						jefMap.add(jef);
					}
				}
			}
		}
	}
}
