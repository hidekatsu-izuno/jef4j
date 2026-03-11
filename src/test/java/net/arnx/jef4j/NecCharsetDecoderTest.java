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

import org.junit.jupiter.api.BeforeEach;
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

	private Map<String, String> jeMap = new TreeMap<>();
	private Map<String, String> ejMap = new TreeMap<>();

	@BeforeEach
	public void setUp() throws IOException {
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/nec_jis8_ebcdik_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					String jis8 = null;
					String ebcdik = null;
					while (parser.nextToken() != JsonToken.END_OBJECT) {
						String fieldName = parser.currentName();
						parser.nextToken();
						if ("jis8".equals(fieldName)) {
							jis8 = parser.getText();
						} else if ("ebcdik".equals(fieldName)) {
							ebcdik = parser.getText();
						}
					}
					if (jis8 != null && ebcdik != null) {
						jeMap.put(jis8, ebcdik);
						ejMap.put(ebcdik, jis8);
					}
				}
			}
		}
	}

	@Test
	public void testNecJis8Decoder() throws IOException {
		Charset JIS8 = Charset.forName("x-NEC-JIS8");
		assertEquals("A\u001Ap$\"\u001AqB\u001Ap%\"\u001Aqｱ", new String(new byte[] {
			(byte)0x41, //
			(byte)0x1A, (byte)0x70, //
			(byte)0x24, (byte)0x22, //
			(byte)0x1A, (byte)0x71, //
			(byte)0x42, //
			(byte)0x1A, (byte)0x70, //
			(byte)0x25, (byte)0x22, //
			(byte)0x1A, (byte)0x71, //
			(byte)0xB1, //
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
		assertEquals("\u3000Ａあ亜", new String(new byte[] {
			(byte)0x21, (byte)0x21, //
			(byte)0x23, (byte)0x41, //
			(byte)0x24, (byte)0x22, //
			(byte)0x30, (byte)0x21  //
		}, JIPSJ));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/nec_jips_mapping.json"), 
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
			if ((i >= 0x7421 && i <= 0x7E7E) || (i >= 0xE0A1 && i <= 0xFEFE)) {
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
		assertEquals("\u3000Ａあ亜", new String(new byte[] {
			(byte)0x4F, (byte)0x4F, //
			(byte)0x7B, (byte)0xC1, //
			(byte)0xE0, (byte)0x7F, //
			(byte)0xF0, (byte)0x4F  //
		}, JIPSE));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				CharsetIndexGenerator.class.getResourceAsStream("/nec_jips_mapping.json"), 
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
						String code = node.get("code").asText();
						String eCode = jeMap.get(code.substring(0, 2)) + jeMap.get(code.substring(2, 4));
						expected.put(eCode, unicode);
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
	
		for (int i = 0; i <= 0xFFFF; i++) {
			String hex = ByteUtils.hex(i, 4);
			int ei = Integer.parseInt(ejMap.get(hex.substring(0, 2)) + ejMap.get(hex.substring(2, 4)), 16);
			if ((ei >= 0x7421 && ei <= 0x7E7E) || (ei >= 0xE0A1 && ei <= 0xFEFE)) {
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
				CharsetIndexGenerator.class.getResourceAsStream("/nec_jips_mapping.json"), 
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
	
		for (int i = 0; i <= 0xFFFF; i++) {
			if ((i >= 0x7421 && i <= 0x7E7E) || (i >= 0xE0A1 && i <= 0xFEFE)) {
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
				CharsetIndexGenerator.class.getResourceAsStream("/nec_jips_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					String unicode = toChars(node, true, true, false);
					if (!unicode.equals("FFFD")) {
						String code = node.get("code").asText();
						String eCode = jeMap.get(code.substring(0, 2)) + jeMap.get(code.substring(2, 4));
						expected.put(eCode, unicode);
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
	
		for (int i = 0; i <= 0xFFFF; i++) {
			String hex = ByteUtils.hex(i, 4);
			int ei = Integer.parseInt(ejMap.get(hex.substring(0, 2)) + ejMap.get(hex.substring(2, 4)), 16);
			if ((ei >= 0x7421 && ei <= 0x7E7E) || (ei >= 0xE0A1 && ei <= 0xFEFE)) {
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
				CharsetIndexGenerator.class.getResourceAsStream("/nec_jips_mapping.json"), 
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
			if ((i >= 0x7421 && i <= 0x7E7E) || (i >= 0xE0A1 && i <= 0xFEFE)) {
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
				CharsetIndexGenerator.class.getResourceAsStream("/nec_jips_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					String unicode = toChars(node, true, false, true);
					if (!unicode.equals("FFFD")) {
						String code = node.get("code").asText();
						String eCode = jeMap.get(code.substring(0, 2)) + jeMap.get(code.substring(2, 4));
						expected.put(eCode, unicode);
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
			String hex = ByteUtils.hex(i, 4);
			int ei = Integer.parseInt(ejMap.get(hex.substring(0, 2)) + ejMap.get(hex.substring(2, 4)), 16);
			if ((ei >= 0x7421 && ei <= 0x7E7E) || (ei >= 0xE0A1 && ei <= 0xFEFE)) {
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

		for (int b1 = 0x74; b1 <= 0x7E; b1++) {
			for (int b2 = 0x21; b2 <= 0x7E; b2++) {
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

		for (int b1 = 0xE0; b1 <= 0xFE; b1++) {
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

		assertEquals("E000", actual.get("7421"));
		assertEquals("E409", actual.get("7E7E"));
		assertEquals("E40A", actual.get("E0A1"));
		assertEquals("EF6B", actual.get("FEFE"));
	}

	@Test
	public void testNecJipseUserDefinedSpaceDecoder() throws IOException {
		Map<String, String> actual = new TreeMap<>();

		CharsetDecoder cd = Charset.forName("x-NEC-JIPSE")
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);

		for (int b1 = 0x74; b1 <= 0x7E; b1++) {
			for (int b2 = 0x21; b2 <= 0x7E; b2++) {
				int be1 = Integer.parseInt(jeMap.get(ByteUtils.hex(b1, 2)), 16);
				int be2 = Integer.parseInt(jeMap.get(ByteUtils.hex(b2, 2)), 16);
				if (b1 == 0x74 && b2 == 0x21) {
					System.out.println(ByteUtils.hex(be1, 2) + ByteUtils.hex(be2, 2));
				}
				bb.clear();
				bb.put((byte)(be1 & 0xFF));
				bb.put((byte)(be2 & 0xFF));
				bb.flip();
				try {
					CharBuffer cb = cd.decode(bb);
					bb.flip();
					
					actual.put(hex(bb), hex(cb));
				} catch (CharacterCodingException e) {
				}
			}
		}

		for (int b1 = 0xE0; b1 <= 0xFE; b1++) {
			for (int b2 = 0xA1; b2 <= 0xFE; b2++) {
				int be1 = Integer.parseInt(jeMap.get(ByteUtils.hex(b1, 2)), 16);
				int be2 = Integer.parseInt(jeMap.get(ByteUtils.hex(b2, 2)), 16);
				//System.out.println(ByteUtils.hex(be1, 2) + ByteUtils.hex(be2, 2));

				bb.clear();
				bb.put((byte)(be1 & 0xFF));
				bb.put((byte)(be2 & 0xFF));
				bb.flip();
				try {
					CharBuffer cb = cd.decode(bb);
					bb.flip();
					
					actual.put(hex(bb), hex(cb));
				} catch (CharacterCodingException e) {
				}
			}
		}

		assertEquals("E000", actual.get("A34F"));
		assertEquals("E409", actual.get("A1A1"));
		assertEquals("E40A", actual.get("B842"));
		assertEquals("EF6B", actual.get("FEFE"));
	}
	
	@Test
	public void testNecJipsjJis8Encoder() throws IOException {
		Charset JIPSJ_JIS8 = Charset.forName("x-NEC-JIPSJ-JIS8");
		assertEquals("aあb海c", new String(new byte[] {
			(byte)0x61, 
			(byte)0x1A, (byte)0x70, 
			(byte)0x24, (byte)0x22, 
			(byte)0x1A, (byte)0x71, 
			(byte)0x62, 
			(byte)0x1A, (byte)0x70, 
			(byte)0x33, (byte)0x24, 
			(byte)0x1A, (byte)0x71,
			(byte)0x63
		}, JIPSJ_JIS8));
	}

	@Test
	public void testNecJipseEbcdikEncoder() throws IOException {
		Charset JIPSJ_EBCDIK = Charset.forName("x-NEC-JIPSE-EBCDIK");
		assertEquals("aあb海c", new String(new byte[] {
			(byte)0x59, 
			(byte)0x3F, (byte)0x75, 
			(byte)0xE0, (byte)0x7F, 
			(byte)0x3F, (byte)0x76, 
			(byte)0x62, 
			(byte)0x3F, (byte)0x75, 
			(byte)0xF3, (byte)0xE0, 
			(byte)0x3F, (byte)0x76, 
			(byte)0x63
		}, JIPSJ_EBCDIK));
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
