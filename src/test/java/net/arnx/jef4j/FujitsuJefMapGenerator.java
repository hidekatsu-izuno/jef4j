package net.arnx.jef4j;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import net.arnx.jef4j.util.ByteRecord;
import net.arnx.jef4j.util.CharObjMap;
import net.arnx.jef4j.util.CharRecord;

public class FujitsuJefMapGenerator {

	@Test
	public void generateMap() throws IOException {
		Map<String, String[]> unicode2asciiMap = new TreeMap<>();
		Map<String, String[]> ascii2unicodeMap = new TreeMap<>();
		
		try (BufferedReader reader = Files.newBufferedReader(Paths.get("./src/test/resources/ascii_mapping.txt"), StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				String unicode = parts[0];
				String ascii = parts[1];
				
				String prefix = unicode.substring(0, 3) + "0";
				String[] values = unicode2asciiMap.get(prefix);
				if (values == null) {
					values = new String[16];
					unicode2asciiMap.put(prefix, values);
				}
				values[Integer.parseInt(unicode.substring(3), 16)] = ascii;
				
				prefix = ascii.substring(0, 1) + "0";
				values = ascii2unicodeMap.get(prefix);
				if (values == null) {
					values = new String[16];
					ascii2unicodeMap.put(prefix, values);
				}
				values[Integer.parseInt(ascii.substring(1), 16)] = unicode;
			}
		}
		
		Map<String, String[]> unicode2ebcdicMap = new TreeMap<>();
		Map<String, String[]> ebcdic2unicodeMap = new TreeMap<>();
		
		try (BufferedReader reader = Files.newBufferedReader(Paths.get("./src/test/resources/ebcdic_mapping.txt"), StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				
				String[] parts = line.split(" ");
				String unicode = parts[0];
				String ebcdic = parts[1];
				
				String prefix = unicode.substring(0, 3) + "0";
				String[] values = unicode2ebcdicMap.get(prefix);
				if (values == null) {
					values = new String[16];
					unicode2ebcdicMap.put(prefix, values);
				}
				values[Integer.parseInt(unicode.substring(3), 16)] = ebcdic;
				
				prefix = ebcdic.substring(0, 1) + "0";
				values = ebcdic2unicodeMap.get(prefix);
				if (values == null) {
					values = new String[16];
					ebcdic2unicodeMap.put(prefix, values);
				}
				values[Integer.parseInt(ebcdic.substring(1), 16)] = unicode;
			}
		}
		
		Map<String, String[]> unicode2ebcdikMap = new TreeMap<>();
		Map<String, String[]> ebcdik2unicodeMap = new TreeMap<>();
		
		try (BufferedReader reader = Files.newBufferedReader(Paths.get("./src/test/resources/ebcdik_mapping.txt"), StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				String unicode = parts[0];
				String ebcdik = parts[1];
				
				String prefix = unicode.substring(0, 3) + "0";
				String[] values = unicode2ebcdikMap.get(prefix);
				if (values == null) {
					values = new String[16];
					unicode2ebcdikMap.put(prefix, values);
				}
				values[Integer.parseInt(unicode.substring(3), 16)] = ebcdik;
				
				prefix = ebcdik.substring(0, 1) + "0";
				values = ebcdik2unicodeMap.get(prefix);
				if (values == null) {
					values = new String[16];
					ebcdik2unicodeMap.put(prefix, values);
				}
				values[Integer.parseInt(ebcdik.substring(1), 16)] = unicode;
			}
		}
		
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
		
		CharObjMap<ByteRecord> asciiEncoder = new CharObjMap<>();
		for (Map.Entry<String, String[]> entry : unicode2asciiMap.entrySet()) {
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
			
			byte[] values = new byte[len];
			int index = 0;
			for (String value : entry.getValue()) {
				if (value != null) {
					values[index++] = (byte)Integer.parseInt(entry.getKey(), 16);
				}
			}
			
			asciiEncoder.put(key, new ByteRecord((char)pattern, values));
		}
		
		CharObjMap<ByteRecord> ebcdicEncoder = new CharObjMap<>();
		for (Map.Entry<String, String[]> entry : unicode2ebcdicMap.entrySet()) {
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
			
			byte[] values = new byte[len];
			int index = 0;
			for (String value : entry.getValue()) {
				if (value != null) {
					values[index++] = (byte)Integer.parseInt(entry.getKey(), 16);
				}
			}
			
			ebcdicEncoder.put(key, new ByteRecord((char)pattern, values));
		}
		
		CharObjMap<ByteRecord> ebcdikEncoder = new CharObjMap<>();
		for (Map.Entry<String, String[]> entry : unicode2ebcdikMap.entrySet()) {
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
			
			byte[] values = new byte[len];
			int index = 0;
			for (String value : entry.getValue()) {
				if (value != null) {
					values[index++] = (byte)Integer.parseInt(entry.getKey(), 16);
				}
			}
			
			ebcdikEncoder.put(key, new ByteRecord((char)pattern, values));
		}
		
		CharObjMap<CharRecord> jefEncoder = new CharObjMap<>();
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
			
			char[] values = new char[len];
			int index = 0;
			for (String value : entry.getValue()) {
				if (value != null) {
					values[index++] = (char)Integer.parseInt(entry.getKey(), 16);
				}
			}
			
			jefEncoder.put(key, new CharRecord((char)pattern, values));
		}
		
		CharObjMap<CharRecord> asciiDecoder = new CharObjMap<>();
		for (Map.Entry<String, String[]> entry : ascii2unicodeMap.entrySet()) {
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
			
			char[] values = new char[len];
			int index = 0;
			for (String value : entry.getValue()) {
				if (value != null) {
					values[index++] = (char)Integer.parseInt(entry.getKey(), 16);
				}
			}
			
			asciiDecoder.put(key, new CharRecord((char)pattern, values));
		}
		
		CharObjMap<CharRecord> ebcdicDecoder = new CharObjMap<>();
		for (Map.Entry<String, String[]> entry : ebcdic2unicodeMap.entrySet()) {
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
			
			char[] values = new char[len];
			int index = 0;
			for (String value : entry.getValue()) {
				if (value != null) {
					values[index++] = (char)Integer.parseInt(entry.getKey(), 16);
				}
			}
			
			ebcdicDecoder.put(key, new CharRecord((char)pattern, values));
		}
		
		CharObjMap<CharRecord> ebcdikDecoder = new CharObjMap<>();
		for (Map.Entry<String, String[]> entry : ebcdik2unicodeMap.entrySet()) {
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
			
			char[] values = new char[len];
			int index = 0;
			for (String value : entry.getValue()) {
				if (value != null) {
					values[index++] = (char)Integer.parseInt(entry.getKey(), 16);
				}
			}
			
			ebcdikDecoder.put(key, new CharRecord((char)pattern, values));
		}

		CharObjMap<CharRecord> jefDecoder = new CharObjMap<>();
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
			
			char[] values = new char[len];
			int index = 0;
			for (String value : entry.getValue()) {
				if (value != null) {
					values[index++] = (char)Integer.parseInt(entry.getKey(), 16);
				}
			}
			
			jefDecoder.put(key, new CharRecord((char)pattern, values));
		}
		
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("src/main/resources/net/arnx/jef4j/JefEncodeMap.dat"))) {
			out.writeObject(asciiEncoder);
			out.writeObject(ebcdicEncoder);
			out.writeObject(ebcdikEncoder);
			out.writeObject(jefEncoder);
		}
		
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("src/main/resources/net/arnx/jef4j/JefDecodeMap.dat"))) {
			out.writeObject(asciiDecoder);
			out.writeObject(ebcdicDecoder);
			out.writeObject(ebcdikDecoder);
			out.writeObject(jefDecoder);
		}
	}

}
