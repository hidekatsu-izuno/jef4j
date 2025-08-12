package net.arnx.jef4j;

import static net.arnx.jef4j.util.ByteUtils.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

import net.arnx.jef4j.tools.FujitsuCharsetIndexGenerator;
import net.arnx.jef4j.util.ByteUtils;

public class FujitsuCharsetEncoderTest {
	private JsonFactory factory = new JsonFactory();
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testFujitsuEbcdicEncoder() throws IOException {
		Charset EBCDIC = Charset.forName("x-Fujitsu-EBCDIC");
		assertEquals("8140824083", hex("aあb海c".getBytes(EBCDIC)));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/ebcdic_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					expected.put(
						node.get("unicode").asText(),
						node.get("ebcdic").asText()
					);
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
	public void testFujitsuEbcdikEncoder() throws IOException {
		Charset EBCDIK = Charset.forName("x-Fujitsu-EBCDIK");
		assertEquals("8140824083", hex("ｱあｲ海ｳ".getBytes(EBCDIK)));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/ebcdik_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					expected.put(
						node.get("unicode").asText(),
						node.get("ebcdic").asText()
					);
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
	public void testFujitsuAsciiEncoder() throws IOException {
		Charset ASCII = Charset.forName("x-Fujitsu-ASCII");
		assertEquals("8140824083", hex("aあb海c".getBytes(ASCII)));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/ascii_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					expected.put(
						node.get("unicode").asText(),
						node.get("ebcdic").asText()
					);
				}
			}
		}

		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = Charset.forName("x-Fujitsu-ASCII")
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
	public void testFujitsuJefEncoder() throws IOException {
		Charset JEF = Charset.forName("x-Fujitsu-JEF");
		assertEquals("4040A4A240404040B3A44040", hex("aあbc海d".getBytes(JEF)));

		Map<String, String> expected = new TreeMap<>();
		
		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/jef_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);
					
					String unicode = toChars(node, false, false, false);
					if (!unicode.equals("FFFD")) {
						expected.put(unicode, node.get("jef").asText());
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = JEF
				.newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		CharBuffer cb = CharBuffer.allocate(2);
	
		for (int cp = 0; cp <= 0x2FFFF; cp++) {
			if (cp >= 0xE000 && cp <= 0xF8FF) {
				continue;
			}

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
		
		Set<String> keys = new TreeSet<>();
		keys.addAll(expected.keySet());
		keys.addAll(actual.keySet());
		for (String key : keys) {
			assertEquals(expected.get(key), actual.get(key), key);
		}
	}

	@Test
	public void testFujitsuJefReversibleEncoder() throws IOException {
		Charset JEF = Charset.forName("x-Fujitsu-JEF-Reversible");
		assertEquals("4040A4A240404040B3A44040", hex("aあbc海d".getBytes(JEF)));
		assertEquals("79DF", hex("\u00A1".getBytes(JEF)));

		Map<String, String> expected = new TreeMap<>();

		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/jef_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					String unicode = toChars(node, true, false, false);
					JsonNode optionsNode = node.get("options");
					boolean reversible = true;
					if (optionsNode != null && optionsNode.isArray()) {
						Set<String> options = mapper.convertValue(
							optionsNode,
							mapper.getTypeFactory().constructCollectionType(Set.class, String.class)
						);
						reversible = !options.contains("irreversible");
					}

					if (!unicode.equals("FFFD") && reversible) {
						expected.put(unicode, node.get("jef").asText());
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = JEF
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
				if (cp == 0x1B025
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
					|| cp == 0x1B0B6
				) {
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
	public void testFujitsuJefHanyoDenshiEncoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();

		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/jef_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					String unicode = toChars(node, true, false, false);
					if (!unicode.equals("FFFD")) {
						expected.put(unicode, node.get("jef").asText());
					}

					unicode = toChars(node, true, true, false);
					if (!unicode.equals("FFFD")) {
						expected.put(unicode, node.get("jef").asText());
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = Charset.forName("x-Fujitsu-JEF-HanyoDenshi")
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
				if (cp == 0x1B025
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
					|| cp == 0x1B0B6
				) {
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
	public void testFujitsuJefAdobeJapan1Encoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();

		try (JsonParser parser = factory.createParser(new BufferedReader(new InputStreamReader(
				FujitsuCharsetIndexGenerator.class.getResourceAsStream("/jef_mapping.json"), 
				StandardCharsets.UTF_8)))) {
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.currentToken() == JsonToken.START_OBJECT) {
					JsonNode node = mapper.readTree(parser);

					String unicode = toChars(node, true, false, false);
					if (!unicode.equals("FFFD")) {
						expected.put(unicode, node.get("jef").asText());
					}

					unicode = toChars(node, true, false, true);
					if (!unicode.equals("FFFD")) {
						expected.put(unicode, node.get("jef").asText());
					}
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = Charset.forName("x-Fujitsu-JEF-AdobeJapan1")
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
				if (cp == 0x1B025
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
					|| cp == 0x1B0B6
				) {
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
	public void testEncodeStepHanyoDenshi() throws IOException {
		Charset JEF_HD = Charset.forName("x-Fujitsu-JEF-HanyoDenshi");

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\u00A1");
			writer.close();
			assertEquals("79DF", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\u00A1\uDB40");
			writer.close();
			assertEquals("79DF4040", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\u00A1\uDB40\uFFFF");
			writer.close();
			assertEquals("79DF4040", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\u00A1\uDB40\uDD01");
			writer.close();
			assertEquals("79DF4040", hex(buf.toByteArray()));	
		}
				
		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\uD82C\uDC19");
			writer.close();
			assertEquals("71AB", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\uD82C\uDC19\u3099");
			writer.close();
			assertEquals("71AC", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\u3404\uDB40\uDD01");
			writer.close();
			assertEquals("41AE", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\u3404\uDB40\uDDFF");
			writer.close();
			assertEquals("41AE4040", hex(buf.toByteArray()));	
		}

				{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\u3404\uDB40\uDD01");
			writer.close();
			assertEquals("41AE", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\uD840");
			writer.close();
			assertEquals("4040", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\uD840\uDC0B");
			writer.close();
			assertEquals("41A6", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\uD840");
			writer.flush();
			writer.write("\uDC0B");
			writer.close();
			assertEquals("41A6", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\uD840");
			writer.flush();
			writer.write("\uDC0B");
			writer.flush();
			writer.write("\uDB40");
			writer.close();
			assertEquals("41A64040", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\uD840\uDC0B\uDB40\uDD01");
			writer.close();
			assertEquals("41A6", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\uD840");
			writer.flush();
			writer.write("\uDC0B");
			writer.flush();
			writer.write("\uDB40");
			writer.flush();
			writer.write("\uDD01");
			writer.close();
			assertEquals("41A6", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\uD840\uDC0B\uDB40\uDDFF");
			writer.close();
			assertEquals("41A64040", hex(buf.toByteArray()));	
		}

		{
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(buf, JEF_HD);
			writer.write("\uD840");
			writer.flush();
			writer.write("\uDC0B");
			writer.flush();
			writer.write("\uDB40");
			writer.flush();
			writer.write("\uDDFF");
			writer.close();
			assertEquals("41A64040", hex(buf.toByteArray()));	
		}
	}


	@Test
	public void testFujitsuJefUserDefinedSpaceEncoder() throws IOException {
		Map<String, String> actual = new TreeMap<>();

		CharsetEncoder ce = Charset.forName("x-Fujitsu-JEF")
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

		assertEquals("80A1", actual.get("E000"));
		assertEquals("80FE", actual.get("E05D"));
		assertEquals("96A1", actual.get("E814"));
		assertEquals("96FE", actual.get("E871"));
		assertEquals("A0A1", actual.get("EBC0"));
		assertEquals("A0FE", actual.get("EC1D"));
		assertNull(actual.get("EC1E"));
		assertNull(actual.get("F8FF"));
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
	public void testFujitsuJefEbcdicDecoder() throws IOException {
		Charset JEF_EBCDIC = Charset.forName("x-Fujitsu-JEF-EBCDIC");
		assertEquals("8128A4A2298228B3A42983", hex("aあb海c".getBytes(JEF_EBCDIC)));
	}

	@Test
	public void testFujitsuJefEbcdikDecoder() throws IOException {
		Charset JEF_EBCDIK = Charset.forName("x-Fujitsu-JEF-EBCDIK");
		assertEquals("8128A4A2298228B3A42983", hex("ｱあｲ海ｳ".getBytes(JEF_EBCDIK)));
	}

	@Test
	public void testFujitsuJefAsciiDecoder() throws IOException {
		Charset JEF_ASCII = Charset.forName("x-Fujitsu-JEF-ASCII");
		assertEquals("8128A4A2298228B3A42983", hex("aあb海c".getBytes(JEF_ASCII)));
	}
}
