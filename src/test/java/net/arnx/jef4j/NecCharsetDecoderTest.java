package net.arnx.jef4j;

import static net.arnx.jef4j.util.ByteUtils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
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

public class NecCharsetDecoderTest {
	private JsonFactory factory = new JsonFactory();
	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testNecJis8Decoder() throws IOException {
		Charset JIS8 = Charset.forName("x-NEC-JIS8");
		assertEquals("ｱ\u008E｡ﾏﾍ\u008E｢ｲ\u008E｡\uFFFDﾏ\u008E｢ｳ", new String(new byte[] {
			(byte)0x81, 
			(byte)0x0A, (byte)0x41, 
			(byte)0xA4, 
			(byte)0xA2, 
			(byte)0x0A, (byte)0x42, 
			(byte)0x82, 
			(byte)0x0A, (byte)0x41, 
			(byte)0xB3, 
			(byte)0xA4, 
			(byte)0x0A, (byte)0x42, 
			(byte)0x83
		}, JIS8));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/nec_jis8_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
					JsonNode code = node.get("code");
					JsonNode unicode = node.get("unicode");
					boolean encodeOnly = false;
					
					JsonNode optionsNode = node.get("options");
					if (optionsNode != null && optionsNode.isArray()) {
						for (JsonNode child : optionsNode) {
							if ("encode_only".equals(child.asText())) {
								encodeOnly = true;
							}
						}
					}

					if (code != null && unicode != null && !"FFFD".equals(unicode.asText()) && !encodeOnly) {
						expected.put(code.asText(), unicode.asText());
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = JIS8
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);
	
		for (int i = 0; i <= 0xFF; i++) {
			bb.clear();
			bb.put((byte)(i & 0xFF));
			bb.flip();
			try {
				CharBuffer cb = cd.decode(bb);
				bb.flip();
				
				actual.put(hex(bb), hex(cb));
			} catch (CharacterCodingException e) {
			}
			
			cd.reset();
		}

		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), key);
		}
	}

	@Test
	public void testNecEbcdikDecoder() throws IOException {
		Charset EBCDIK = Charset.forName("x-NEC-EBCDIK");
		assertEquals("ｱ\u008E｡ﾏﾍ\u008E｢ｲ\u008E｡\uFFFDﾏ\u008E｢ｳ", new String(new byte[] {
			(byte)0x81, 
			(byte)0x0A, (byte)0x41, 
			(byte)0xA4, 
			(byte)0xA2, 
			(byte)0x0A, (byte)0x42, 
			(byte)0x82, 
			(byte)0x0A, (byte)0x41, 
			(byte)0xB3, 
			(byte)0xA4, 
			(byte)0x0A, (byte)0x42, 
			(byte)0x83
		}, EBCDIK));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/nec_ebcdik_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
					JsonNode code = node.get("code");
					JsonNode unicode = node.get("unicode");
					boolean encodeOnly = false;
					
					JsonNode optionsNode = node.get("options");
					if (optionsNode != null && optionsNode.isArray()) {
						for (JsonNode child : optionsNode) {
							if ("encode_only".equals(child.asText())) {
								encodeOnly = true;
							}
						}
					}

					if (code != null && unicode != null && !"FFFD".equals(unicode.asText()) && !encodeOnly) {
						expected.put(code.asText(), unicode.asText());
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = EBCDIK
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);
	
		for (int i = 0; i <= 0xFF; i++) {
			bb.clear();
			bb.put((byte)(i & 0xFF));
			bb.flip();
			try {
				CharBuffer cb = cd.decode(bb);
				bb.flip();
				
				actual.put(hex(bb), hex(cb));
			} catch (CharacterCodingException e) {
			}
			
			cd.reset();
		}

		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), key);
		}
	}

	@Test
	public void testNecJipsjDecoder() throws IOException {
		Charset JIPSJ = Charset.forName("x-NEC-JIPSJ");
		assertEquals("\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD", new String(new byte[] {
			(byte)0x81, 
			(byte)0x0A, (byte)0x41, 
			(byte)0xA4, 
			(byte)0xA2, 
			(byte)0x0A, (byte)0x42,  
			(byte)0x82, 
			(byte)0x0A, (byte)0x41, 
			(byte)0xB3, 
			(byte)0xA4, 
			(byte)0x0A, (byte)0x42,  
			(byte)0x83
		}, JIPSJ));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/hitachi_keis78_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
					boolean encodeOnly = false;
					
					JsonNode optionsNode = node.get("options");
					if (optionsNode != null && optionsNode.isArray()) {
						for (JsonNode child : optionsNode) {
							if ("encode_only".equals(child.asText())) {
								encodeOnly = true;
							}
						}
					}
					
					String unicode = toChars(node, true, false, false);
					if (!unicode.equals("FFFD") && !encodeOnly) {
						expected.put(node.get("code").asText(), unicode);
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = JIPSJ
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);
	
		for (int i = 0; i < 0xFFFF; i++) {
			if (i >= 0x80A0 && i <= 0xA0FF) {
				continue;
			}
			
			bb.clear();
			bb.put((byte)((i >> 8) & 0xFF));
			bb.put((byte)(i & 0xFF));
			bb.flip();
			try {
				CharBuffer cb = cd.decode(bb);
				bb.flip();
				
				actual.put(hex(bb), hex(cb));
			} catch (CharacterCodingException e) {
			}
			
			cd.reset();
		}
		
		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), key);
		}
	}

	@Test
	public void testNecJipseDecoder() throws IOException {
		Charset JIPSE = Charset.forName("x-NEC-JIPSE");
		assertEquals("\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD", new String(new byte[] {
			(byte)0x81, 
			(byte)0x0A, (byte)0x41, 
			(byte)0xA4, 
			(byte)0xA2, 
			(byte)0x0A, (byte)0x42,  
			(byte)0x82, 
			(byte)0x0A, (byte)0x41, 
			(byte)0xB3, 
			(byte)0xA4, 
			(byte)0x0A, (byte)0x42,  
			(byte)0x83
		}, JIPSE));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/hitachi_keis83_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
					boolean encodeOnly = false;
					
					JsonNode optionsNode = node.get("options");
					if (optionsNode != null && optionsNode.isArray()) {
						for (JsonNode child : optionsNode) {
							if ("encode_only".equals(child.asText())) {
								encodeOnly = true;
							}
						}
					}
					
					String unicode = toChars(node, true, false, false);
					if (!unicode.equals("FFFD") && !encodeOnly) {
						expected.put(node.get("code").asText(), unicode);
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = JIPSE
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);
	
		for (int i = 0; i < 0xFFFF; i++) {
			if (i >= 0x80A0 && i <= 0xA0FF) {
				continue;
			}
			
			bb.clear();
			bb.put((byte)((i >> 8) & 0xFF));
			bb.put((byte)(i & 0xFF));
			bb.flip();
			try {
				CharBuffer cb = cd.decode(bb);
				bb.flip();
				
				actual.put(hex(bb), hex(cb));
			} catch (CharacterCodingException e) {
			}
			
			cd.reset();
		}
		
		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), key);
		}
	}

	@Test
	public void testNecJipsjHanyoDenshiDecoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/hitachi_keis78_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					String unicode = toChars(node, true, true, false);
					if (!unicode.equals("FFFD")) {
						expected.put(node.get("code").asText(), unicode);
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = Charset.forName("x-NEC-JIPSJ-HanyoDenshi")
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);
	
		for (int i = 0; i < 0xFFFF; i++) {
			if (i >= 0x80A0 && i <= 0xA0FF) {
				continue;
			}

			bb.clear();
			bb.put((byte)((i >> 8) & 0xFF));
			bb.put((byte)(i & 0xFF));
			bb.flip();
			try {
				CharBuffer cb = cd.decode(bb);
				bb.flip();
				
				actual.put(hex(bb), hex(cb));
			} catch (CharacterCodingException e) {
			}
			
			cd.reset();
		}
		
		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), key);
		}
	}

	@Test
	public void testNecJipseHanyoDenshiDecoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/nec_jipsj_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					String unicode = toChars(node, true, true, false);
					if (!unicode.equals("FFFD")) {
						expected.put(node.get("code").asText(), unicode);
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = Charset.forName("x-NEC-JIPSE-HanyoDenshi")
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);
	
		for (int i = 0; i < 0xFFFF; i++) {
			if (i >= 0x80A0 && i <= 0xA0FF) {
				continue;
			}

			bb.clear();
			bb.put((byte)((i >> 8) & 0xFF));
			bb.put((byte)(i & 0xFF));
			bb.flip();
			try {
				CharBuffer cb = cd.decode(bb);
				bb.flip();
				
				actual.put(hex(bb), hex(cb));
			} catch (CharacterCodingException e) {
			}
			
			cd.reset();
		}
		
		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), key);
		}
	}

	@Test
	public void testNecJipsjAdobeJapan1Decoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/nec_jipsj_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					String unicode = toChars(node, true, false, true);
					if (!unicode.equals("FFFD")) {
						expected.put(node.get("code").asText(), unicode);
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = Charset.forName("x-NEC-JIPSJ-AdobeJapan1")
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);
	
		for (int i = 0; i < 0xFFFF; i++) {
			if (i >= 0x80A0 && i <= 0xA0FF) {
				continue;
			}

			bb.clear();
			bb.put((byte)((i >> 8) & 0xFF));
			bb.put((byte)(i & 0xFF));
			bb.flip();
			try {
				CharBuffer cb = cd.decode(bb);
				bb.flip();
				
				actual.put(hex(bb), hex(cb));
			} catch (CharacterCodingException e) {
			}
			
			cd.reset();
		}
		
		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), key);
		}
	}
		
	@Test
	public void testNecJipseAdobeJapan1Decoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/nec_jipsj_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					String unicode = toChars(node, true, false, true);
					if (!unicode.equals("FFFD")) {
						expected.put(node.get("code").asText(), unicode);
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = Charset.forName("x-NEC-JIPSE-AdobeJapan1")
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);
	
		for (int i = 0; i < 0xFFFF; i++) {
			if (i >= 0x80A0 && i <= 0xA0FF) {
				continue;
			}

			bb.clear();
			bb.put((byte)((i >> 8) & 0xFF));
			bb.put((byte)(i & 0xFF));
			bb.flip();
			try {
				CharBuffer cb = cd.decode(bb);
				bb.flip();
				
				actual.put(hex(bb), hex(cb));
			} catch (CharacterCodingException e) {
			}
			
			cd.reset();
		}
		
		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), key);
		}
	}

	@Test
	public void testNecJipsjUserDefinedSpaceDecoder() throws IOException {
		Map<String, String> actual = new TreeMap<>();

		CharsetDecoder cd = Charset.forName("x-NEC-JIPSJ")
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);

		for (int b1 = 0x80; b1 <= 0xA0; b1++) {
			for (int b2 = 0xA1; b2 <= 0xFE; b2++) {
				bb.clear();
				bb.put((byte)(b1 & 0xFF));
				bb.put((byte)(b2 & 0xFF));
				bb.flip();
				try {
					CharBuffer cb = cd.decode(bb);
					bb.flip();
					
					actual.put(hex(bb), hex(cb));
				} catch (CharacterCodingException e) {
				}
			}
		}

		assertEquals("E000", actual.get("81A1"));
		assertEquals("E05D", actual.get("81FE"));
		assertEquals("E7B6", actual.get("96A1"));
		assertEquals("E813", actual.get("96FE"));
		assertEquals("EB62", actual.get("A0A1"));
		assertEquals("EBBF", actual.get("A0FE"));
	}

	@Test
	public void testNecJipseUserDefinedSpaceDecoder() throws IOException {
		Map<String, String> actual = new TreeMap<>();

		CharsetDecoder cd = Charset.forName("x-NEC-JIPSE")
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);

		for (int b1 = 0x80; b1 <= 0xA0; b1++) {
			for (int b2 = 0xA1; b2 <= 0xFE; b2++) {
				bb.clear();
				bb.put((byte)(b1 & 0xFF));
				bb.put((byte)(b2 & 0xFF));
				bb.flip();
				try {
					CharBuffer cb = cd.decode(bb);
					bb.flip();
					
					actual.put(hex(bb), hex(cb));
				} catch (CharacterCodingException e) {
				}
			}
		}

		assertEquals("E000", actual.get("81A1"));
		assertEquals("E05D", actual.get("81FE"));
		assertEquals("E7B6", actual.get("96A1"));
		assertEquals("E813", actual.get("96FE"));
		assertEquals("EB62", actual.get("A0A1"));
		assertEquals("EBBF", actual.get("A0FE"));
	}
	
	@Test
	public void testNecJipsjJis8Encoder() throws IOException {
		Charset JIPSJ_JIS8 = Charset.forName("x-NEC-JIPSJ-JIS8");
		assertEquals("aあb海c", new String(new byte[] {
			(byte)0x81, 
			(byte)0x0A, (byte)0x42, 
			(byte)0xA4, (byte)0xA2, 
			(byte)0x0A, (byte)0x41, 
			(byte)0x82, 
			(byte)0x0A, (byte)0x42, 
			(byte)0xB3, (byte)0xA4, 
			(byte)0x0A, (byte)0x41, 
			(byte)0x83
		}, JIPSJ_JIS8));
	}

	@Test
	public void testNecJipsjEbcdikEncoder() throws IOException {
		Charset JIPSJ_EBCDIK = Charset.forName("x-NEC-JIPSJ-EBCDIK");
		assertEquals("aあb海c", new String(new byte[] {
			(byte)0x81, 
			(byte)0x0A, (byte)0x42, 
			(byte)0xA4, (byte)0xA2, 
			(byte)0x0A, (byte)0x41, 
			(byte)0x82, 
			(byte)0x0A, (byte)0x42, 
			(byte)0xB3, (byte)0xA4, 
			(byte)0x0A, (byte)0x41, 
			(byte)0x83
		}, JIPSJ_EBCDIK));
	}

	@Test
	public void testNecJipseJis8Encoder() throws IOException {
		Charset JIPSE_JIS8 = Charset.forName("x-NEC-JIPSE-JIS8");
		assertEquals("ｱあｲ海ｳ", new String(new byte[] {
			(byte)0x81, 
			(byte)0x0A, (byte)0x42, 
			(byte)0xA4, (byte)0xA2, 
			(byte)0x0A, (byte)0x41, 
			(byte)0x82, 
			(byte)0x0A, (byte)0x42, 
			(byte)0xB3, (byte)0xA4, 
			(byte)0x0A, (byte)0x41, 
			(byte)0x83
		}, JIPSE_JIS8));
	}

	@Test
	public void testNecJipseEbcdikEncoder() throws IOException {
		Charset JIPSE_EBCDIK = Charset.forName("x-NEC-JIPSE-EBCDIK");
		assertEquals("ｱあｲ海ｳ", new String(new byte[] {
			(byte)0x81, 
			(byte)0x0A, (byte)0x42, 
			(byte)0xA4, (byte)0xA2, 
			(byte)0x0A, (byte)0x41, 
			(byte)0x82, 
			(byte)0x0A, (byte)0x42, 
			(byte)0xB3, (byte)0xA4, 
			(byte)0x0A, (byte)0x41, 
			(byte)0x83
		}, JIPSE_EBCDIK));
	}

	private static String toChars(JsonNode node, boolean useSP, boolean useHanyoDenshi, boolean useAdobeJapan1) {
		List<String> parts = new ArrayList<>(); 
		parts.add(node.get("unicode").asText());
		if (useSP) {
			JsonNode spNode = node.get("sp");
			if (spNode != null) {
				parts.add(spNode.asText());
			}
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
	
	@Test
	public void testDecodeStep() throws IOException {
		Charset JEF = Charset.forName("x-Fujitsu-JEF");
		{
			new String(new byte[] {
				(byte)0x72, (byte)0xAE
			}, JEF);
		}
	}
}
