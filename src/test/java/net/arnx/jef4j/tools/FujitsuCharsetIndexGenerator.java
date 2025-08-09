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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.arnx.jef4j.util.CharRecord;
import net.arnx.jef4j.util.IntRecord;
import net.arnx.jef4j.util.LongObjMap;
import net.arnx.jef4j.util.LongRecord;
import net.arnx.jef4j.util.Record;

public class FujitsuCharsetIndexGenerator {
	public static void main(String[] args) throws IOException {
		List<Object> encoders = new ArrayList<>();
		List<Object> decoders = new ArrayList<>();
		
		FujitsuCharsetIndexGenerator generator = new FujitsuCharsetIndexGenerator();
		generator.generateAsciiIndex(encoders, decoders);
		generator.generateEbcdicIndex(encoders, decoders);
		generator.generateEbcdikIndex(encoders, decoders);
		generator.generateJefIndex(encoders, decoders);
		
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

		System.out.println("Finish!");
	}

	private JsonFactory factory = new JsonFactory();
	private ObjectMapper mapper = new ObjectMapper();
	
	private void generateAsciiIndex(List<Object> encoders, List<Object> decoders) throws IOException {
		byte[] encoderMap = new byte[256];
		byte[] decoderMap = new byte[256];
		
		Arrays.fill(encoderMap, (byte)0xFF);
		Arrays.fill(decoderMap, (byte)0xFF);
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/ascii_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
				
					String unicode = node.get("unicode").asText();
					String ascii = node.get("ebcdic").asText();
					
					encoderMap[Integer.parseUnsignedInt(unicode, 16)] = (byte)Integer.parseUnsignedInt(ascii, 16);
					decoderMap[Integer.parseUnsignedInt(ascii, 16)] = (byte)Integer.parseUnsignedInt(unicode, 16);
				}
			}
		}
		
		encoders.add(encoderMap);
		decoders.add(decoderMap);
	}
	
	private void generateEbcdicIndex(List<Object> encoders, List<Object> decoders) throws IOException {
		byte[] encoderMap = new byte[256];
		byte[] decoderMap = new byte[256];
		
		Arrays.fill(encoderMap, (byte)0xFF);
		Arrays.fill(decoderMap, (byte)0xFF);
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/ebcdic_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
					
					String unicode = node.get("unicode").asText();
					String ebcdic = node.get("ebcdic").asText();
					
					encoderMap[Integer.parseUnsignedInt(unicode, 16)] = (byte)Integer.parseUnsignedInt(ebcdic, 16);
					decoderMap[Integer.parseUnsignedInt(ebcdic, 16)] = (byte)Integer.parseUnsignedInt(unicode, 16);
				}
			}
		}
		
		encoders.add(encoderMap);
		decoders.add(decoderMap);
	}
	
	private void generateEbcdikIndex(List<Object> encoders, List<Object> decoders) throws IOException {
		byte[] encoderMap = new byte[256];
		byte[] decoderMap = new byte[256];
		
		Arrays.fill(encoderMap, (byte)0xFF);
		Arrays.fill(decoderMap, (byte)0xFF);
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/ebcdik_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
				
					String unicode = node.get("unicode").asText();
					String ebcdik = node.get("ebcdic").asText();
					
					int iUnicode = Integer.parseUnsignedInt(unicode, 16);
					int iEbcdik = Integer.parseUnsignedInt(ebcdik, 16);
					
					if (iUnicode >= '\uFF61') {
						iUnicode = iUnicode -'\uFF61' + '\u00C0';
					}
					
					encoderMap[iUnicode] = (byte)iEbcdik;
					decoderMap[iEbcdik] = (byte)iUnicode;
				}
			}
		}
		
		encoders.add(encoderMap);
		decoders.add(decoderMap);
	}
	
	private void generateJefIndex(List<Object> encoders, List<Object> decoders) throws IOException {
		Map<String, String[]> unicode2jefMap = new TreeMap<>();
		Map<String, String[]> jef2hdunicodeMap = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/jef_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					String sunicode = toSimpleKey(node);
					String hdunicode = toHanyoDenshiKey(node);
					String jef = node.get("jef").asText();

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

					if (!sunicode.equals(hdunicode)) {
						prefix = hdunicode.substring(0, hdunicode.length()-1) + "0";
						values = unicode2jefMap.get(prefix);
						if (values == null) {
							values = new String[16];
							unicode2jefMap.put(prefix, values);
						}
						values[Integer.parseUnsignedInt(hdunicode.substring(hdunicode.length()-1), 16)] = jef;
					}
					
					prefix = jef.substring(0, jef.length()-1) + "0";
					values = jef2hdunicodeMap.get(prefix);
					if (values == null) {
						values = new String[16];
						jef2hdunicodeMap.put(prefix, values);
					}
					values[Integer.parseUnsignedInt(jef.substring(jef.length()-1), 16)] = hdunicode;
				}
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
			for (Map.Entry<String, String[]> entry : jef2hdunicodeMap.entrySet()) {
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
	
	private static String toSimpleKey(JsonNode node) {
		String unicode = node.get("unicode").asText();
		JsonNode spNode = node.get("sp");
		String sp = spNode != null ? spNode.asText() : null;

		if (sp != null) {
			StringBuilder sb = new StringBuilder(6);
			for (int i = 0; i < (5 - sp.length()); i++) {
				sb.append("0");
			}
			sb.append(sp);
			for (int i = 0; i < (5 - unicode.length()); i++) {
				sb.append("0");
			}
			sb.append(unicode);
			return sb.toString();
		}
		return unicode;
	}
	
	private static String toHanyoDenshiKey(JsonNode node) {
		String unicode = node.get("unicode").asText();
		JsonNode spNode = node.get("sp");
		String sp = spNode != null ? spNode.asText() : null;
		JsonNode hdNode = node.get("hd");
		String hd = hdNode != null ? hdNode.asText() : null;

		if (sp != null || hd != null) {
			StringBuilder sb = new StringBuilder(6);
			for (int i = 0; i < (5 - (sp != null ? sp.length() : hd.length())); i++) {
				sb.append("0");
			}
			sb.append(sp != null ? sp : hd);
			for (int i = 0; i < (5 - unicode.length()); i++) {
				sb.append("0");
			}
			sb.append(unicode);
			return sb.toString();
		}
		return unicode;
	}
}
