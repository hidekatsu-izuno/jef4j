package net.arnx.jef4j;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import net.arnx.jef4j.util.ByteRecord;
import net.arnx.jef4j.util.IntObjMap;
import net.arnx.jef4j.util.IntRecord;
import net.arnx.jef4j.util.CharRecord;
import net.arnx.jef4j.util.Record;

public class FujitsuCharsetMapGenerator {
	public static void main(String[] args) throws IOException {
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
				values[Integer.parseUnsignedInt(unicode.substring(3), 16)] = ascii;
				
				prefix = ascii.substring(0, 1) + "0";
				values = ascii2unicodeMap.get(prefix);
				if (values == null) {
					values = new String[16];
					ascii2unicodeMap.put(prefix, values);
				}
				values[Integer.parseUnsignedInt(ascii.substring(1), 16)] = unicode;
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
				values[Integer.parseUnsignedInt(unicode.substring(3), 16)] = ebcdic;
				
				prefix = ebcdic.substring(0, 1) + "0";
				values = ebcdic2unicodeMap.get(prefix);
				if (values == null) {
					values = new String[16];
					ebcdic2unicodeMap.put(prefix, values);
				}
				values[Integer.parseUnsignedInt(ebcdic.substring(1), 16)] = unicode;
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
				values[Integer.parseUnsignedInt(unicode.substring(3), 16)] = ebcdik;
				
				prefix = ebcdik.substring(0, 1) + "0";
				values = ebcdik2unicodeMap.get(prefix);
				if (values == null) {
					values = new String[16];
					ebcdik2unicodeMap.put(prefix, values);
				}
				values[Integer.parseUnsignedInt(ebcdik.substring(1), 16)] = unicode;
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
				
				String prefix = unicode.substring(0, unicode.length()-1) + "0";
				String[] values = unicode2jefMap.get(prefix);
				if (values == null) {
					values = new String[16];
					unicode2jefMap.put(prefix, values);
				}
				values[Integer.parseUnsignedInt(unicode.substring(unicode.length()-1), 16)] = jef;
				
				prefix = jef.substring(0, jef.length()-1) + "0";
				values = jef2unicodeMap.get(prefix);
				if (values == null) {
					values = new String[16];
					jef2unicodeMap.put(prefix, values);
				}
				values[Integer.parseUnsignedInt(jef.substring(jef.length()-1), 16)] = unicode;
			}
		}
		
		IntObjMap<Record> asciiEncoder = new IntObjMap<>();
		for (Map.Entry<String, String[]> entry : unicode2asciiMap.entrySet()) {
			int key = Integer.parseUnsignedInt(entry.getKey(), 16);
			
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
					values[index++] = (byte)Integer.parseUnsignedInt(value, 16);
				}
			}
			
			asciiEncoder.put(key, new ByteRecord((char)pattern, values));
		}
		
		IntObjMap<Record> ebcdicEncoder = new IntObjMap<>();
		for (Map.Entry<String, String[]> entry : unicode2ebcdicMap.entrySet()) {
			int key = Integer.parseUnsignedInt(entry.getKey(), 16);
			
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
					values[index++] = (byte)Integer.parseUnsignedInt(value, 16);
				}
			}
			
			ebcdicEncoder.put(key, new ByteRecord((char)pattern, values));
		}
		
		IntObjMap<Record> ebcdikEncoder = new IntObjMap<>();
		for (Map.Entry<String, String[]> entry : unicode2ebcdikMap.entrySet()) {
			int key = Integer.parseUnsignedInt(entry.getKey(), 16);
			
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
					values[index++] = (byte)Integer.parseUnsignedInt(value, 16);
				}
			}
			
			ebcdikEncoder.put(key, new ByteRecord((char)pattern, values));
		}
		
		IntObjMap<Record> jefEncoder = new IntObjMap<>();
		for (Map.Entry<String, String[]> entry : unicode2jefMap.entrySet()) {
			int key = Integer.parseUnsignedInt(entry.getKey(), 16);
			
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
					values[index++] = (char)Integer.parseUnsignedInt(value, 16);
				}
			}
			
			jefEncoder.put(key, new CharRecord((char)pattern, values));
		}
		
		IntObjMap<Record> asciiDecoder = new IntObjMap<>();
		for (Map.Entry<String, String[]> entry : ascii2unicodeMap.entrySet()) {
			int key = Integer.parseUnsignedInt(entry.getKey(), 16);
			
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
					values[index++] = (char)Integer.parseUnsignedInt(value, 16);
				}
			}
			
			asciiDecoder.put(key, new CharRecord((char)pattern, values));
		}
		
		IntObjMap<Record> ebcdicDecoder = new IntObjMap<>();
		for (Map.Entry<String, String[]> entry : ebcdic2unicodeMap.entrySet()) {
			int key = Integer.parseUnsignedInt(entry.getKey(), 16);
			
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
					values[index++] = (char)Integer.parseUnsignedInt(value, 16);
				}
			}
			
			ebcdicDecoder.put(key, new CharRecord((char)pattern, values));
		}
		
		IntObjMap<Record> ebcdikDecoder = new IntObjMap<>();
		for (Map.Entry<String, String[]> entry : ebcdik2unicodeMap.entrySet()) {
			int key = Integer.parseUnsignedInt(entry.getKey(), 16);
			
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
					values[index++] = (char)Integer.parseUnsignedInt(value, 16);
				}
			}
			
			ebcdikDecoder.put(key, new CharRecord((char)pattern, values));
		}

		IntObjMap<Record> jefDecoder = new IntObjMap<>();
		for (Map.Entry<String, String[]> entry : jef2unicodeMap.entrySet()) {
			int key = Integer.parseUnsignedInt(entry.getKey(), 16);
			
			int len = 0;
			int pattern = 0;
			int size = 4;
			for (String value : entry.getValue()) {
				if (value != null) {
					len++;
					pattern = (pattern << 1) | 1;
					size = Math.max(size, value.length());
				} else {
					pattern = pattern << 1;
				}
			}
			
			if (size > 4) {
				int[] values = new int[len];
				int index = 0;
				for (String value : entry.getValue()) {
					if (value != null) {
						values[index++] = Integer.parseUnsignedInt(value, 16);
					}
				}
				jefDecoder.put(key, new IntRecord((char)pattern, values));
			} else {
				char[] values = new char[len];
				int index = 0;
				for (String value : entry.getValue()) {
					if (value != null) {
						values[index++] = (char)Integer.parseUnsignedInt(value, 16);
					}
				}				
				jefDecoder.put(key, new CharRecord((char)pattern, values));
			}
		}
		
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("src/main/resources/net/arnx/jef4j/FujitsuEncodeMap.dat"))) {
			out.writeObject(asciiEncoder);
			out.writeObject(ebcdicEncoder);
			out.writeObject(ebcdikEncoder);
			out.writeObject(jefEncoder);
		}
		
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("src/main/resources/net/arnx/jef4j/FujitsuDecodeMap.dat"))) {
			out.writeObject(asciiDecoder);
			out.writeObject(ebcdicDecoder);
			out.writeObject(ebcdikDecoder);
			out.writeObject(jefDecoder);
		}
	}

}
