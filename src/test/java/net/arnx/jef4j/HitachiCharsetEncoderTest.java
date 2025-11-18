package net.arnx.jef4j;

import static net.arnx.jef4j.util.ByteUtils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.arnx.jef4j.tools.CharsetIndexGenerator;
import net.arnx.jef4j.util.ByteUtils;

public class HitachiCharsetEncoderTest {
	private JsonFactory factory = new JsonFactory();
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testHitachiEbcdicEncoder() throws IOException {
		Charset EBCDIC = Charset.forName("x-Hitachi-EBCDIC");
		assertEquals("8140824083", hex("aあb海c".getBytes(EBCDIC)));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/hitachi_ebcdic_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
					JsonNode unicode = node.get("unicode");
					JsonNode code = node.get("code");
					boolean decodeOnly = false;
					
					JsonNode optionsNode = node.get("options");
					if (optionsNode != null && optionsNode.isArray()) {
						for (JsonNode child : optionsNode) {
							if ("decode_only".equals(child.asText())) {
								decodeOnly = true;
							}
						}
					}

					if (unicode != null && code != null && !"FFFD".equals(unicode.asText()) && !decodeOnly) {
						expected.put(unicode.asText(), code.asText());
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = EBCDIC
				.newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		CharBuffer cb = CharBuffer.allocate(2);
	
		for (int cp = 0; cp <= 0x2FFFF; cp++) {
			cb.clear();
			cb.put(Character.toChars(cp));
			cb.flip();
			try {
				ByteBuffer bb = ce.encode(cb);
				cb.flip();
				
				actual.put(hex(cb), hex(bb));
			} catch (CharacterCodingException e) {
			}
			
			ce.reset();
		}
		assertEquals(expected, actual);
	}
	
	@Test
	public void testHitachiEbcdikEncoder() throws IOException {
		Charset EBCDIK = Charset.forName("x-Hitachi-EBCDIK");
		assertEquals("8140824083", hex("ｱあｲ海ｳ".getBytes(EBCDIK)));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/hitachi_ebcdik_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
					JsonNode unicode = node.get("unicode");
					JsonNode code = node.get("code");
					boolean decodeOnly = false;
					
					JsonNode optionsNode = node.get("options");
					if (optionsNode != null && optionsNode.isArray()) {
						for (JsonNode child : optionsNode) {
							if ("decode_only".equals(child.asText())) {
								decodeOnly = true;
							}
						}
					}

					if (unicode != null && code != null && !"FFFD".equals(unicode.asText()) && !decodeOnly) {
						expected.put(unicode.asText(), code.asText());
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = EBCDIK
				.newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		CharBuffer cb = CharBuffer.allocate(2);
	
		for (int cp = 0; cp <= 0x2FFFF; cp++) {
			cb.clear();
			cb.put(Character.toChars(cp));
			cb.flip();
			try {
				ByteBuffer bb = ce.encode(cb);
				cb.flip();
				
				actual.put(hex(cb), hex(bb));
			} catch (CharacterCodingException e) {
			}
			
			ce.reset();
		}
		assertEquals(expected, actual);
	}
	
	@Test
	public void testHitachiKEIS78Encoder() throws IOException {
		Charset KEIS78 = Charset.forName("x-Hitachi-KEIS78");
		assertEquals("4040A4A240404040B3A44040", hex("aあbc海d".getBytes(KEIS78)));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/hitachi_keis78_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
					boolean decodeOnly = false;
					
					JsonNode optionsNode = node.get("options");
					if (optionsNode != null && optionsNode.isArray()) {
						for (JsonNode child : optionsNode) {
							if ("decode_only".equals(child.asText())) {
								decodeOnly = true;
							}
						}
					}
					
					String unicode = toChars(node, false, false);
					if (!unicode.equals("FFFD") && !decodeOnly) {
						expected.put(unicode, node.get("code").asText());
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = KEIS78
				.newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		CharBuffer cb = CharBuffer.allocate(3);
	
		for (int cp = 0; cp <= 0x2FFFF; cp++) {
			if (cp >= 0xE000 && cp <= 0xF8FF) {
				continue;
			}

			boolean success = false;
			cb.clear();
			cb.put(Character.toChars(cp));
			cb.flip();
			try {
				ByteBuffer bb = ce.encode(cb);
				cb.flip();
				
				actual.put(hex(cb), hex(bb));
				success = true;
			} catch (CharacterCodingException e) {
			}

			ce.reset();
			if (success) {
				if (useSp3099(cp)) {
					cb.clear();
					cb.put(Character.toChars(cp));
					cb.put('\u3099');
					cb.flip();
					try {
						ByteBuffer bb = ce.encode(cb);
						cb.flip();
						actual.put(hex(cb), hex(bb));
					} catch (CharacterCodingException e) {
					}
					ce.reset();
				}
			}
			
			ce.reset();
		}
		
		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), key);
		}
	}

	@Test
	public void testHitachiKEIS83Encoder() throws IOException {
		Charset KEIS83 = Charset.forName("x-Hitachi-KEIS83");
		assertEquals("4040A4A240404040B3A44040", hex("aあbc海d".getBytes(KEIS83)));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/hitachi_keis83_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
					boolean decodeOnly = false;
					
					JsonNode optionsNode = node.get("options");
					if (optionsNode != null && optionsNode.isArray()) {
						for (JsonNode child : optionsNode) {
							if ("decode_only".equals(child.asText())) {
								decodeOnly = true;
							}
						}
					}
					
					String unicode = toChars(node, false, false);
					if (!unicode.equals("FFFD") && !decodeOnly) {
						expected.put(unicode, node.get("code").asText());
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = KEIS83
				.newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		CharBuffer cb = CharBuffer.allocate(3);
	
		for (int cp = 0; cp <= 0x2FFFF; cp++) {
			if (cp >= 0xE000 && cp <= 0xF8FF) {
				continue;
			}

			boolean success = false;
			cb.clear();
			cb.put(Character.toChars(cp));
			cb.flip();
			try {
				ByteBuffer bb = ce.encode(cb);
				cb.flip();
				
				actual.put(hex(cb), hex(bb));
				success = true;
			} catch (CharacterCodingException e) {
			}

			ce.reset();
			if (success) {
				if (useSp3099(cp)) {
					cb.clear();
					cb.put(Character.toChars(cp));
					cb.put('\u3099');
					cb.flip();
					try {
						ByteBuffer bb = ce.encode(cb);
						cb.flip();
						actual.put(hex(cb), hex(bb));
					} catch (CharacterCodingException e) {
					}
					ce.reset();
				}
			}
			
			ce.reset();
		}
		
		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), key);
		}
	}

	@Test
	public void testHitachiKEIS83HanyoDenshiEncoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();

		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/hitachi_keis83_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
					boolean decodeOnly = false;

					JsonNode optionsNode = node.get("options");
					if (optionsNode != null && optionsNode.isArray()) {
						for (JsonNode child : optionsNode) {
							if ("decode_only".equals(child.asText())) {
								decodeOnly = true;
							}
						}
					}

					String unicode = toChars(node, false, false);
					if (!unicode.equals("FFFD") && !decodeOnly) {
						expected.put(unicode, node.get("code").asText());
					}

					unicode = toChars(node, true, false);
					if (!unicode.equals("FFFD") && !decodeOnly) {
						expected.put(unicode, node.get("code").asText());
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = Charset.forName("x-Hitachi-KEIS83-HanyoDenshi")
				.newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		CharBuffer cb = CharBuffer.allocate(4);
	
		for (int cp = 0; cp <= 0x2FFFF; cp++) {
			if (cp >= 0xE000 && cp <= 0xF8FF) {
				continue;
			}

			boolean success = false;
			cb.clear();
			cb.put(Character.toChars(cp));
			cb.flip();
			try {
				ByteBuffer bb = ce.encode(cb);
				cb.flip();
				
				actual.put(hex(cb), hex(bb));
				success = true;
			} catch (CharacterCodingException e) {
			}
			
			ce.reset();

			if (success) {
				if (useSp3099(cp)) {
					cb.clear();
					cb.put(Character.toChars(cp));
					cb.put('\u3099');
					cb.flip();
					try {
						ByteBuffer bb = ce.encode(cb);
						cb.flip();
						actual.put(hex(cb), hex(bb));
					} catch (CharacterCodingException e) {
					}
					ce.reset();
				} else {
					for (int ivs = 0x0; ivs <= 0xF; ivs++) {
						StringBuilder sb = new StringBuilder();
						sb.appendCodePoint(cp);
						sb.appendCodePoint(0xE0100 + ivs);
						cb.clear();
						cb.append(sb);
						cb.flip();
						try {
							ByteBuffer bb = ce.encode(cb);
							cb.flip();
							actual.put(hex(cb), hex(bb));
						} catch (CharacterCodingException e) {
						}
						ce.reset();
					}	
				}
			}
		}
		
		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), "key = " + key);
		}
	}
	
	@Test
	public void testHitachiKEIS78AdobeJapan1Encoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();

		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/hitachi_keis78_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
					boolean decodeOnly = false;

					JsonNode optionsNode = node.get("options");
					if (optionsNode != null && optionsNode.isArray()) {
						for (JsonNode child : optionsNode) {
							if ("decode_only".equals(child.asText())) {
								decodeOnly = true;
							}
						}
					}

					String unicode = toChars(node, false, false);
					if (!unicode.equals("FFFD") && !decodeOnly) {
						expected.put(unicode, node.get("code").asText());
					}

					unicode = toChars(node, false, true);
					if (!unicode.equals("FFFD") && !decodeOnly) {
						expected.put(unicode, node.get("code").asText());
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = Charset.forName("x-Hitachi-KEIS78-AdobeJapan1")
				.newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		CharBuffer cb = CharBuffer.allocate(4);
	
		for (int cp = 0; cp <= 0x2FFFF; cp++) {
			if (cp >= 0xE000 && cp <= 0xF8FF) {
				continue;
			}

			boolean success = false;
			cb.clear();
			cb.put(Character.toChars(cp));
			cb.flip();
			try {
				ByteBuffer bb = ce.encode(cb);
				cb.flip();
				
				actual.put(hex(cb), hex(bb));
				success = true;
			} catch (CharacterCodingException e) {
			}
			
			ce.reset();

			if (success) {
				if (useSp3099(cp)) {
					cb.clear();
					cb.put(Character.toChars(cp));
					cb.put('\u3099');
					cb.flip();
					try {
						ByteBuffer bb = ce.encode(cb);
						cb.flip();
						actual.put(hex(cb), hex(bb));
					} catch (CharacterCodingException e) {
					}
					ce.reset();
				} else {
					for (int ivs = 0x0; ivs <= 0xF; ivs++) {
						StringBuilder sb = new StringBuilder();
						sb.appendCodePoint(cp);
						sb.appendCodePoint(0xE0100 + ivs);
						cb.clear();
						cb.append(sb);
						cb.flip();
						try {
							ByteBuffer bb = ce.encode(cb);
							cb.flip();
							actual.put(hex(cb), hex(bb));
						} catch (CharacterCodingException e) {
						}
						ce.reset();
					}	
				}
			}
		}
		
		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), "key = " + key);
		}
	}

	@Test
	public void testHitachiKEIS83AdobeJapan1Encoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();

		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/hitachi_keis83_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
					boolean decodeOnly = false;

					JsonNode optionsNode = node.get("options");
					if (optionsNode != null && optionsNode.isArray()) {
						for (JsonNode child : optionsNode) {
							if ("decode_only".equals(child.asText())) {
								decodeOnly = true;
							}
						}
					}

					String unicode = toChars(node, false, false);
					if (!unicode.equals("FFFD") && !decodeOnly) {
						expected.put(unicode, node.get("code").asText());
					}

					unicode = toChars(node, false, true);
					if (!unicode.equals("FFFD") && !decodeOnly) {
						expected.put(unicode, node.get("code").asText());
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = Charset.forName("x-Hitachi-KEIS83-AdobeJapan1")
				.newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		CharBuffer cb = CharBuffer.allocate(4);
	
		for (int cp = 0; cp <= 0x2FFFF; cp++) {
			if (cp >= 0xE000 && cp <= 0xF8FF) {
				continue;
			}

			boolean success = false;
			cb.clear();
			cb.put(Character.toChars(cp));
			cb.flip();
			try {
				ByteBuffer bb = ce.encode(cb);
				cb.flip();
				
				actual.put(hex(cb), hex(bb));
				success = true;
			} catch (CharacterCodingException e) {
			}
			
			ce.reset();

			if (success) {
				if (useSp3099(cp)) {
					cb.clear();
					cb.put(Character.toChars(cp));
					cb.put('\u3099');
					cb.flip();
					try {
						ByteBuffer bb = ce.encode(cb);
						cb.flip();
						actual.put(hex(cb), hex(bb));
					} catch (CharacterCodingException e) {
					}
					ce.reset();
				} else {
					for (int ivs = 0x0; ivs <= 0xF; ivs++) {
						StringBuilder sb = new StringBuilder();
						sb.appendCodePoint(cp);
						sb.appendCodePoint(0xE0100 + ivs);
						cb.clear();
						cb.append(sb);
						cb.flip();
						try {
							ByteBuffer bb = ce.encode(cb);
							cb.flip();
							actual.put(hex(cb), hex(bb));
						} catch (CharacterCodingException e) {
						}
						ce.reset();
					}	
				}
			}
		}
		
		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), "key = " + key);
		}
	}

	@Test
	public void testHitachiKEIS78UserDefinedSpaceEncoder() throws IOException {
		Map<String, String> actual = new TreeMap<>();

		CharsetEncoder ce = Charset.forName("x-Hitachi-KEIS78")
				.newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		CharBuffer cb = CharBuffer.allocate(2);

		for (char c = '\uE000'; c <= '\uF8FF'; c++) {
			cb.clear();
			cb.put(c);
			cb.flip();
			try {
				ByteBuffer bb = ce.encode(cb);
				cb.flip();
				
				actual.put(hex(cb), hex(bb));
			} catch (CharacterCodingException e) {
			}
			
			ce.reset();
		}

		assertEquals("81A1", actual.get("E000"));
		assertEquals("81FE", actual.get("E05D"));
		assertEquals("96A1", actual.get("E7B6"));
		assertEquals("96FE", actual.get("E813"));
		assertEquals("A0A1", actual.get("EB62"));
		assertEquals("A0FE", actual.get("EBBF"));
		assertNull(actual.get("EBC0"));
		assertNull(actual.get("F8FF"));
	}


	@Test
	public void testHitachiKEIS83UserDefinedSpaceEncoder() throws IOException {
		Map<String, String> actual = new TreeMap<>();

		CharsetEncoder ce = Charset.forName("x-Hitachi-KEIS83")
				.newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		CharBuffer cb = CharBuffer.allocate(2);

		for (char c = '\uE000'; c <= '\uF8FF'; c++) {
			cb.clear();
			cb.put(c);
			cb.flip();
			try {
				ByteBuffer bb = ce.encode(cb);
				cb.flip();
				
				actual.put(hex(cb), hex(bb));
			} catch (CharacterCodingException e) {
			}
			
			ce.reset();
		}

		assertEquals("81A1", actual.get("E000"));
		assertEquals("81FE", actual.get("E05D"));
		assertEquals("96A1", actual.get("E7B6"));
		assertEquals("96FE", actual.get("E813"));
		assertEquals("A0A1", actual.get("EB62"));
		assertEquals("A0FE", actual.get("EBBF"));
		assertNull(actual.get("EBC0"));
		assertNull(actual.get("F8FF"));
	}

	@Test
	public void testHitachiKEIS78EbcdicEncoder() throws IOException {
		Charset KEIS78_EBCDIC = Charset.forName("x-Hitachi-KEIS78-EBCDIC");
		assertEquals("810A42A4A20A41820A42B3A40A4183", hex("aあb海c".getBytes(KEIS78_EBCDIC)));
	}

	@Test
	public void testHitachiKEIS83EbcdicEncoder() throws IOException {
		Charset KEIS78_EBCDIC = Charset.forName("x-Hitachi-KEIS83-EBCDIC");
		assertEquals("810A42A4A20A41820A42B3A40A4183", hex("aあb海c".getBytes(KEIS78_EBCDIC)));
	}

	@Test
	public void testHitachiKEIS78EbcdikEncoder() throws IOException {
		Charset KEIS78_EBCDIK = Charset.forName("x-Hitachi-KEIS78-EBCDIK");
		assertEquals("810A42A4A20A41820A42B3A40A4183", hex("ｱあｲ海ｳ".getBytes(KEIS78_EBCDIK)));
	}

	@Test
	public void testHitachiKEIS83EbcdikEncoder() throws IOException {
		Charset KEIS83_EBCDIK = Charset.forName("x-Hitachi-KEIS83-EBCDIK");
		assertEquals("810A42A4A20A41820A42B3A40A4183", hex("ｱあｲ海ｳ".getBytes(KEIS83_EBCDIK)));
	}
		
	private static String toChars(JsonNode node, boolean useHanyoDenshi, boolean useAdobeJapan1) {
		List<String> parts = new ArrayList<>(); 
		parts.add(node.get("unicode").asText());
		JsonNode spNode = node.get("sp");
		if (spNode != null) {
			parts.add(spNode.asText());
		}
		if (useHanyoDenshi) {
			JsonNode hdNode = node.get("hd");
			if (hdNode != null) {
				parts.add(hdNode.asText());
			}
		} else if (useAdobeJapan1) {
			JsonNode aj1Node = node.get("aj1");
			if (aj1Node != null) {
				parts.add(aj1Node.asText());
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for (String c : parts) {
			int cp = Integer.parseUnsignedInt(c, 16);
			if (Character.isSupplementaryCodePoint(cp)) {
				sb.append(ByteUtils.hex(Character.highSurrogate(cp), 4));
				sb.append(ByteUtils.hex(Character.lowSurrogate(cp), 4));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private static boolean useSp3099(int cp) {
		return cp == 0x1B025
				|| cp == 0x1B02A
				|| cp == 0x1B02C
				|| cp == 0x1B048
				|| cp == 0x1B05F
				|| cp == 0x1B0BA
				|| cp == 0x1B019
				|| cp == 0x1B034
				|| cp == 0x1B038
				|| cp == 0x1B03D
				|| cp == 0x1B03F
				|| cp == 0x1B041
				|| cp == 0x1B048
				|| cp == 0x1B04E
				|| cp == 0x1B04F
				|| cp == 0x1B055
				|| cp == 0x1B05B
				|| cp == 0x1B05F
				|| cp == 0x1B066
				|| cp == 0x1B06A
				|| cp == 0x1B06B
				|| cp == 0x1B06D
				|| cp == 0x1B072
				|| cp == 0x1B0A3
				|| cp == 0x1B0A9
				|| cp == 0x1B0AF
				|| cp == 0x1B0B1
				|| cp == 0x1B0B6;
	}
}
