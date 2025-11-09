package net.arnx.jef4j.tools;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

public class CharsetIndexGenerator {
	public static void main(String[] args) throws IOException {
		writeCharsetIndex(
			"src/main/resources/net/arnx/jef4j/FujitsuEncodeMap.dat",
			"src/main/resources/net/arnx/jef4j/FujitsuDecodeMap.dat",
			new String[] {
				"fujitsu_ebcdic_mapping.json",
				"fujitsu_ebcdik_mapping.json",
				"fujitsu_ascii_mapping.json"
			},
			new String[] {
				"fujitsu_jef_mapping.json"
			}
		);

		writeCharsetIndex(
			"src/main/resources/net/arnx/jef4j/HitachiEncodeMap.dat",
			"src/main/resources/net/arnx/jef4j/HitachiDecodeMap.dat",
			new String[] {
				"hitachi_ebcdic_mapping.json",
				"hitachi_ebcdik_mapping.json"
			},
			new String[] {
				"hitachi_keis78_mapping.json",
				"hitachi_keis83_mapping.json"
			}
		);

		writeCharsetIndex(
			"src/main/resources/net/arnx/jef4j/NecEncodeMap.dat",
			"src/main/resources/net/arnx/jef4j/NecDecodeMap.dat",
			new String[] {
				"nec_ebcdik_mapping.json"
			},
			new String[] {
				"nec_jips_mapping.json"
			}
		);
	}

	public static void writeCharsetIndex(
		String outEncodeFile,
		String outDecodeFile,
		String[] sbcsList,
		String[] mbcsList
	) throws IOException {
		List<Object> encoders = new ArrayList<>();
		List<Object> decoders = new ArrayList<>();
		
		CharsetIndexGenerator generator = new CharsetIndexGenerator();
		for (String csInfo : sbcsList) {
			generator.generateSBCSIndex(csInfo, encoders, decoders);
		}
		for (String csInfo : mbcsList) {
			generator.generateMBCSIndex(csInfo, encoders, decoders);
		}
		
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outEncodeFile))) {
			for (Object encoder : encoders) {
				out.writeObject(encoder);
			}
		}
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outDecodeFile))) {
			for (Object decoder : decoders) {
				out.writeObject(decoder);
			}
		}
	}

	private JsonFactory factory = new JsonFactory();
	private ObjectMapper mapper = new ObjectMapper();
	
	private void generateSBCSIndex(String filename, List<Object> encoders, List<Object> decoders) throws IOException {
		byte[] encoderMap = new byte[256];
		byte[] decoderMap = new byte[256];
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/" + filename), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
				
					String unicode = node.get("unicode").asText();
					String code = node.get("code").asText();
					boolean decodeOnly = false;
					boolean encodeOnly = false;

					JsonNode optionsNode = node.get("options");
					if (optionsNode != null && optionsNode.isArray()) {
						for (JsonNode child : optionsNode) {
							if ("decode_only".equals(child.asText())) {
								decodeOnly = true;
							} else if ("encode_only".equals(child.asText())) {
								encodeOnly = true;
							}
						}
					}

					int iUnicode = Integer.parseUnsignedInt(unicode, 16);
					int iEbcdic = Integer.parseUnsignedInt(code, 16);
					if (iUnicode == '\u203E') {
						iUnicode = iUnicode - '\u203E' + '\u00B0';
					} else if (iUnicode >= '\uFF61') {
						iUnicode = iUnicode - '\uFF61' + '\u00C0';
					}
					
					if (!decodeOnly) {
						encoderMap[iUnicode] = (byte)iEbcdic;
					}
					if (!encodeOnly) {
						decoderMap[iEbcdic] = (byte)iUnicode;
					}
				}
			}
		}
		
		encoders.add(encoderMap);
		decoders.add(decoderMap);
	}
	
	private void generateMBCSIndex(String filename, List<Object> encoders, List<Object> decoders) throws IOException {
		Map<String, String[][]> unicode2codeMap = new TreeMap<>();
		Map<String, String[][]> code2hdunicodeMap = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/" + filename), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					String sunicode = toKey(node, null);
					if (sunicode.equals("FFFD")) {
						continue;
					}

					String hdunicode = toKey(node, "hd");
					String aj1unicode = toKey(node, "aj1");
					String code = node.get("code").asText();
					boolean decodeOnly = false;
					boolean encodeOnly = false;
					boolean reversible = true;

					JsonNode optionsNode = node.get("options");
					if (optionsNode != null && optionsNode.isArray()) {
						for (JsonNode child : optionsNode) {
							if ("decode_only".equals(child.asText())) {
								decodeOnly = true;
							} else if ("encode_only".equals(child.asText())) {
								encodeOnly = true;
							} else if ("irreversible".equals(child.asText())) {
								reversible = false;
							}
						}
					}

					String[] ivsUnicode = new String[] {
						hdunicode,
						aj1unicode,
						reversible ? sunicode : null
					};

					for (int i = 0; i < ivsUnicode.length; i++) {
						if (ivsUnicode[i] == null) {
							continue;
						}
						String prefix = sunicode.substring(0, sunicode.length()-1) + "0";
						String[][] array = unicode2codeMap.computeIfAbsent(prefix, key -> new String[3][]);
						if (array[i] == null) {
							array[i] = new String[16];
						}
						if (!decodeOnly) {
							array[i][Integer.parseUnsignedInt(sunicode.substring(sunicode.length()-1), 16)] = code;
						}
					}

					for (int i = 0; i < ivsUnicode.length; i++) {
						if (ivsUnicode[i] == null) {
							continue;
						}
						if (!sunicode.equals(ivsUnicode[i])) {
							String prefix = ivsUnicode[i].substring(0, ivsUnicode[i].length()-1) + "0";
							String[][] array = unicode2codeMap.computeIfAbsent(prefix, key -> new String[3][]);
							if (array[i] == null) {
								array[i] = new String[16];
							}
							if (!decodeOnly) {
								array[i][Integer.parseUnsignedInt(ivsUnicode[i].substring(ivsUnicode[i].length()-1), 16)] = code;
							}
						}
					}
					
					for (int i = 0; i < ivsUnicode.length; i++) {
						if (ivsUnicode[i] == null) {
							continue;
						}
						String prefix = code.substring(0, code.length()-1) + "0";
						String[][] array = code2hdunicodeMap.computeIfAbsent(prefix, key -> new String[3][]);
						if (array[i] == null) {
							array[i] = new String[16];
						}
						if (!encodeOnly) {
							array[i][Integer.parseUnsignedInt(code.substring(code.length()-1), 16)] = ivsUnicode[i];
						}
					}
				}
			}
			
			LongObjMap<Record[]> encodeMap = new LongObjMap<>();
			for (Map.Entry<String, String[][]> entry : unicode2codeMap.entrySet()) {
				long key = Long.parseUnsignedLong(entry.getKey(), 16);
				String[][] array = entry.getValue();
				Record[] records = new Record[array.length];
				for (int i = 0; i < array.length; i++) {
					if (array[i] == null) {
						continue;
					}
					int len = 0;
					int pattern = 0;
					for (String value : array[i]) {
						if (value != null) {
							pattern = (pattern << 1) | 1;
							len++;
						} else {
							pattern = pattern << 1;
						}
					}
					
					char[] values = new char[len];
					int index = 0;
					for (String value : array[i]) {
						if (value != null) {
							values[index++] = (char)Integer.parseUnsignedInt(value, 16);
						}
					}

					records[i] = new CharRecord((char)pattern, values);
				}
				for (int i = 0; i < records.length - 1; i++) {
					for (int j = i + 1; j < records.length; j++) {
						if (records[j] != null && records[j].equals(records[i])) {
							records[j] = records[i];
						}
					}
				}
				encodeMap.put(key, records);
			}

			LongObjMap<Record[]> decodeMap = new LongObjMap<>();
			for (Map.Entry<String, String[][]> entry : code2hdunicodeMap.entrySet()) {
				long key = Long.parseUnsignedLong(entry.getKey(), 16);
				String[][] array = entry.getValue();
				Record[] records = new Record[array.length];
				for (int i = 0; i < array.length; i++) {
					if (array[i] == null) {
						continue;
					}

					int len = 0;
					int pattern = 0;
					int size = 0;
					for (String value : array[i]) {
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
						for (String value : array[i]) {
							if (value != null) {
								values[index++] = Long.parseUnsignedLong(value, 16);
							}
						}
						records[i] = new LongRecord((char)pattern, values);
					} else if (size == 5) {
						int[] values = new int[len];
						int index = 0;
						for (String value : array[i]) {
							if (value != null) {
								values[index++] = Integer.parseUnsignedInt(value, 16);
							}
						}
						records[i] = new IntRecord((char)pattern, values);
					} else if (size == 4) {
						char[] values = new char[len];
						int index = 0;
						for (String value : array[i]) {
							if (value != null) {
								values[index++] = (char)Integer.parseUnsignedInt(value, 16);
							}
						}				
						records[i] = new CharRecord((char)pattern, values);
					} else {
						throw new IllegalStateException("size is invalid: " + size);
					}
				}
				for (int i = 0; i < records.length - 1; i++) {
					for (int j = i + 1; j < records.length; j++) {
						if (records[j] != null && records[j].equals(records[i])) {
							records[j] = records[i];
						}
					}
				}
				decodeMap.put(key, records);
			}
			
			encoders.add(encodeMap);
			decoders.add(decodeMap);
		}
	}
	
	private static String toKey(JsonNode node, String ivs) {
		String unicode = node.get("unicode").asText();
		JsonNode spNode = node.get("sp");
		String sp = null;
		
		if (spNode != null) {
			sp = spNode.asText();
		} else if (ivs != null) {
			JsonNode ivsNode = node.get(ivs);
			sp = ivsNode != null ? ivsNode.asText() : null;
		}

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
}
