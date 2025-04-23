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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import net.arnx.jef4j.util.ByteUtils;

public class FujitsuCharsetEncoderTest {

	
	@Test
	public void testFujitsuEbcdicEncoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/ebcdic_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				expected.put(parts[0], parts[1]);
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = Charset.forName("x-Fujitsu-EBCDIC")
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
		Map<String, String> expected = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/ebcdik_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				expected.put(parts[0], parts[1]);
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = Charset.forName("x-Fujitsu-EBCDIK")
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
		Map<String, String> expected = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/ascii_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				expected.put(parts[0], parts[1]);
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
		Map<String, String> expected = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/jef_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) continue;

				String[] parts = line.split(" ");
				String unicode = toChars(parts[0].replaceAll("_.*$", ""), false);
				if (!unicode.equals("FFFD")) {
					expected.put(unicode, parts[1]);
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = Charset.forName("x-Fujitsu-JEF")
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
	
	private static String toChars(String unicode, boolean useHanyoDenshi) {
		if (!useHanyoDenshi) {
			unicode = unicode.replaceAll("_E.*$", "");
		}
		
		StringBuilder sb = new StringBuilder();
		for (String c : unicode.split("_")) {
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

}
