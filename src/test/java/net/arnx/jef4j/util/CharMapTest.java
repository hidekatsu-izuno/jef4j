package net.arnx.jef4j.util;

import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class CharMapTest {

	@Test
	public void test() {
		CharObjMap<Character> map = new CharObjMap<>();
		map.put('\u0001', '\u0003');
		map.put('\u0002', '\u0100');
		map.put('\u0060', '\u0002');
		
		assertEquals((Character)'\u0003', map.get('\u0001'));
		assertEquals((Character)'\u0100', map.get('\u0002'));
		assertEquals((Character)'\u0002', map.get('\u0060'));
		assertEquals(null, map.get('\u0040'));
	}

	@Test
	public void generateJefMap() throws IOException {
		Map<String, String[]> unicode2asciiMap = new TreeMap<>();
		Map<String, String[]> ascii2unicodeMap = new TreeMap<>();
		
		try (BufferedReader reader = Files.newBufferedReader(Paths.get("./src/test/resources/ascii_mapping.txt"), StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				String unicode = parts[0];
				String ascii = parts[1];
				
				if (!unicode.startsWith("00")) {
					continue;
				}
			}
		}
		
		Map<String, String[]> unicode2ebcdicMap = new TreeMap<>();
		Map<String, String[]> ebcdic2unicodeMap = new TreeMap<>();
		
		Map<String, String[]> unicode2ebcdikMap = new TreeMap<>();
		Map<String, String[]> ebcdik2unicodeMap = new TreeMap<>();
		
		Map<String, String[]> unicode2jefMap = new TreeMap<>();
		Map<String, String[]> jef2unicodeMap = new TreeMap<>();
		
		try (BufferedReader reader = Files.newBufferedReader(Paths.get("./src/test/resources/jef_mapping.txt"), StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				String unicode = parts[0];
				String jef = parts[1];
				
				String prefix = unicode.substring(0, 3) + "0";
				String[] values = unicode2jefMap.get(prefix);
				if (values == null) {
					values = new String[16];
					unicode2jefMap.put(prefix, values);
				}
				values[Integer.parseInt(unicode.substring(3), 16)] = jef;
				
				prefix = jef.substring(0, 3) + "0";
				values = jef2unicodeMap.get(prefix);
				if (values == null) {
					values = new String[16];
					jef2unicodeMap.put(prefix, values);
				}
				values[Integer.parseInt(jef.substring(3), 16)] = unicode;
			}
		}
		
		CharObjMap<char[]> encoder = new CharObjMap<>();
		for (Map.Entry<String, String[]> entry : unicode2jefMap.entrySet()) {
			char key = (char)Integer.parseInt(entry.getKey(), 16);
			
			int len = 0;
			int pattern = 0;
			for (String value : entry.getValue()) {
				if (value != null) {
					pattern = (pattern << 1) | 1;
					len++;
				} else {
					pattern = pattern << 1;
				}
			}
			
			char[] values = new char[len + 1];
			values[0] = (char)pattern;
			int index = 1;
			for (String value : entry.getValue()) {
				if (value != null) {
					values[index++] = (char)Integer.parseInt(entry.getKey(), 16);
				}
			}
			
			encoder.put(key, values);
		}

		CharObjMap<char[]> decoder = new CharObjMap<>();
		for (Map.Entry<String, String[]> entry : jef2unicodeMap.entrySet()) {
			char key = (char)Integer.parseInt(entry.getKey(), 16);
			
			int len = 0;
			int pattern = 0;
			for (String value : entry.getValue()) {
				if (value != null) {
					pattern = (pattern << 1) | 1;
					len++;
				} else {
					pattern = pattern << 1;
				}
			}
			
			char[] values = new char[len + 1];
			values[0] = (char)pattern;
			int index = 1;
			for (String value : entry.getValue()) {
				if (value != null) {
					values[index++] = (char)Integer.parseInt(entry.getKey(), 16);
				}
			}
			
			decoder.put(key, values);
		}
		
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("src/main/resources/net/arnx/jef4j/JefEncodeMap.dat"))) {
			out.writeObject(encoder);
		}
		
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("src/main/resources/net/arnx/jef4j/JefDecodeMap.dat"))) {
			out.writeObject(decoder);
		}
	}
}
