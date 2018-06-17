package net.arnx.jef4j.tools;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.arnx.dartsclone.DoubleArrayTrie;
import net.arnx.jef4j.util.CharRecord;
import net.arnx.jef4j.util.IntRecord;
import net.arnx.jef4j.util.LongObjMap;
import net.arnx.jef4j.util.LongRecord;
import net.arnx.jef4j.util.Record;

public class FujitsuCharsetIndexGenerator {
	public static void main(String[] args) throws IOException {
		List<Object> encoders = new ArrayList<>();
		List<Object> decoders = new ArrayList<>();
		
		generateAsciiIndex(encoders, decoders);
		generateEbcdicIndex(encoders, decoders);
		generateEbcdikIndex(encoders, decoders);
		generateJefIndex(encoders, decoders);
		
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("src/main/resources/net/arnx/jef4j/FujitsuEncodeMap.dat"))) {
			for (Object encoder : encoders) {
				out.writeObject(encoder);
			}
		}
		
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("src/main/resources/net/arnx/jef4j/FujitsuDecodeMap.dat"))) {
			for (Object decoder : decoders) {
				out.writeObject(decoder);
			}
		}
	}
	
	private static void generateAsciiIndex(List<Object> encoders, List<Object> decoders) throws IOException {
		byte[] encoderMap = new byte[256];
		byte[] decoderMap = new byte[256];
		
		Arrays.fill(encoderMap, (byte)0xFF);
		Arrays.fill(decoderMap, (byte)0xFF);
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/ascii_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				
				String[] parts = line.split(" ");
				String unicode = parts[0];
				String ascii = parts[1];
				
				encoderMap[Integer.parseUnsignedInt(unicode, 16)] = (byte)Integer.parseUnsignedInt(ascii, 16);
				decoderMap[Integer.parseUnsignedInt(ascii, 16)] = (byte)Integer.parseUnsignedInt(unicode, 16);
			}
		}
		
		encoders.add(encoderMap);
		decoders.add(decoderMap);
	}
	
	private static void generateEbcdicIndex(List<Object> encoders, List<Object> decoders) throws IOException {
		byte[] encoderMap = new byte[256];
		byte[] decoderMap = new byte[256];
		
		Arrays.fill(encoderMap, (byte)0xFF);
		Arrays.fill(decoderMap, (byte)0xFF);
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/ebcdic_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				
				String[] parts = line.split(" ");
				String unicode = parts[0];
				String ebcdic = parts[1];
				
				encoderMap[Integer.parseUnsignedInt(unicode, 16)] = (byte)Integer.parseUnsignedInt(ebcdic, 16);
				decoderMap[Integer.parseUnsignedInt(ebcdic, 16)] = (byte)Integer.parseUnsignedInt(unicode, 16);
			}
		}
		
		encoders.add(encoderMap);
		decoders.add(decoderMap);
	}
	
	private static void generateEbcdikIndex(List<Object> encoders, List<Object> decoders) throws IOException {
		byte[] encoderMap = new byte[256];
		byte[] decoderMap = new byte[256];
		
		Arrays.fill(encoderMap, (byte)0xFF);
		Arrays.fill(decoderMap, (byte)0xFF);
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/ebcdik_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				
				String[] parts = line.split(" ");
				String unicode = parts[0];
				String ebcdik = parts[1];
				
				int iUnicode = Integer.parseUnsignedInt(unicode, 16);
				int iEbcdik = Integer.parseUnsignedInt(ebcdik, 16);
				
				if (iUnicode >= '\uFF61') {
					iUnicode = iUnicode -'\uFF61' + '\u00C0';
				}
				
				encoderMap[iUnicode] = (byte)iEbcdik;
				decoderMap[iEbcdik] = (byte)iUnicode;
			}
		}
		
		encoders.add(encoderMap);
		decoders.add(decoderMap);
	}
	
	private static void generateJefIndex(List<Object> encoders, List<Object> decoders) throws IOException {
		Map<String, String[]> unicode2jefMap = new TreeMap<>();
		Map<String, String[]> jef2unicodeMap = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/jef_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				
				String[] parts = line.split(" ");
				String sunicode = toSimpleKey(parts[0]);
				String cunicode = toComplexKey(parts[0]);
				String jef = parts[1];
				
				if (sunicode.equals("FFFD")) {
					continue;
				}
				
				String prefix = sunicode.substring(0, sunicode.length()-1) + "0";
				String[] values = unicode2jefMap.get(prefix);
				if (values == null) {
					values = new String[16];
					unicode2jefMap.put(prefix, values);
				}
				values[Integer.parseUnsignedInt(sunicode.substring(sunicode.length()-1), 16)] = jef;
				
				prefix = jef.substring(0, jef.length()-1) + "0";
				values = jef2unicodeMap.get(prefix);
				if (values == null) {
					values = new String[16];
					jef2unicodeMap.put(prefix, values);
				}
				values[Integer.parseUnsignedInt(jef.substring(jef.length()-1), 16)] = cunicode;
			}
			
			LongObjMap<Record> jefEncoder = new LongObjMap<>();
			for (Map.Entry<String, String[]> entry : unicode2jefMap.entrySet()) {
				long key = Long.parseUnsignedLong(entry.getKey(), 16);
				
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

			LongObjMap<Record> jefDecoder = new LongObjMap<>();
			for (Map.Entry<String, String[]> entry : jef2unicodeMap.entrySet()) {
				long key = Long.parseUnsignedLong(entry.getKey(), 16);
				
				int len = 0;
				int pattern = 0;
				int size = 0;
				for (String value : entry.getValue()) {
					if (value != null) {
						len++;
						pattern = (pattern << 1) | 1;
						size = Math.max(size, value.length());
					} else {
						pattern = pattern << 1;
					}
				}
				
				if (size == 10) {
					long[] values = new long[len];
					int index = 0;
					for (String value : entry.getValue()) {
						if (value != null) {
							values[index++] = Long.parseUnsignedLong(value, 16);
						}
					}
					jefDecoder.put(key, new LongRecord((char)pattern, values));
				} else if (size == 5) {
					int[] values = new int[len];
					int index = 0;
					for (String value : entry.getValue()) {
						if (value != null) {
							values[index++] = Integer.parseUnsignedInt(value, 16);
						}
					}
					jefDecoder.put(key, new IntRecord((char)pattern, values));
				} else if (size == 4) {
					char[] values = new char[len];
					int index = 0;
					for (String value : entry.getValue()) {
						if (value != null) {
							values[index++] = (char)Integer.parseUnsignedInt(value, 16);
						}
					}				
					jefDecoder.put(key, new CharRecord((char)pattern, values));
				} else {
					throw new IllegalStateException("size is invalid: " + size);
				}
			}
			
			encoders.add(jefEncoder);
			decoders.add(jefDecoder);
		}
	}
	
	private static void generateJefIndex2(List<Object> encoders, List<Object> decoders) throws IOException {
		DoubleArrayTrie.Builder encoderMap = new DoubleArrayTrie.Builder();
		DoubleArrayTrie.Builder decoderMap = new DoubleArrayTrie.Builder();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/jef_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				
				String[] parts = line.split(" ");
				String sunicode = toSimpleKey(parts[0]);
				String cunicode = toComplexKey(parts[0]);
				String jef = parts[1];
				if (sunicode.equals("FFFD")) {
					continue;
				}
				
				
				
			}
		}
		
		encoders.add(encoderMap.toArray());
		decoders.add(decoderMap);
	}
	
	
	private static String toSimpleKey(String unicode) {
		String[] parts = unicode.split("_");
		if (parts.length == 2 && !Character.isSupplementaryCodePoint(Integer.parseUnsignedInt(parts[1], 16))) {
			StringBuilder sb = new StringBuilder(6);
			for (int i = 0; i < (5 - parts[1].length()); i++) {
				sb.append("0");
			}
			sb.append(parts[1]);
			for (int i = 0; i < (5 - parts[0].length()); i++) {
				sb.append("0");
			}
			sb.append(parts[0]);
			return sb.toString();
		}
		return parts[0];
	}
	
	private static String toComplexKey(String unicode) {
		String[] parts = unicode.split("_");
		if (parts.length > 1) {
			StringBuilder sb = new StringBuilder(6);
			for (int i = 0; i < (5 - parts[1].length()); i++) {
				sb.append("0");
			}
			sb.append(parts[1]);
			for (int i = 0; i < (5 - parts[0].length()); i++) {
				sb.append("0");
			}
			sb.append(parts[0]);
			return sb.toString();
		}
		return parts[0];
	}
}
