package net.arnx.jef4j;

import static org.junit.Assert.*;
import static net.arnx.jef4j.util.ByteUtils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;

import net.arnx.jef4j.util.ByteUtils;

public class FujitsuCharsetDecoderTest {

	@Test
	public void testFujitsuEbcdicDecoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/ebcdic_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				expected.put(parts[1], parts[0]);
			}
			expected.put("28", "");
			expected.put("38", "");
			expected.put("29", "");
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = Charset.forName("x-Fujitsu-EBCDIC")
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);
	
		for (int i = 0; i < 0xFF; i++) {
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
		assertEquals(expected, actual);
	}

	@Test
	public void testFujitsuEbcdikDecoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/ebcdik_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				expected.put(parts[1], parts[0]);
			}
			expected.put("28", "");
			expected.put("38", "");
			expected.put("29", "");
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = Charset.forName("x-Fujitsu-EBCDIK")
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);
	
		for (int i = 0; i < 0xFF; i++) {
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
		assertEquals(expected, actual);
	}

	@Test
	public void testFujitsuAsciiDecoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/ascii_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				expected.put(parts[1], parts[0]);
			}
			expected.put("28", "");
			expected.put("38", "");
			expected.put("29", "");
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = Charset.forName("x-Fujitsu-ASCII")
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);
	
		for (int i = 0; i < 0xFF; i++) {
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
		assertEquals(expected, actual);
	}

	@Test
	public void testFujitsuJefDecoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/jef_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) continue;
				
				String[] parts = line.split(" ");
				String unicode = toChars(parts[0]);
				if (!unicode.equals("FFFD")) {
					expected.put(parts[1], unicode);
				}
			}
			expected.put("2828", "");
			expected.put("2829", "");
			expected.put("2838", "");
			expected.put("2928", "");
			expected.put("2929", "");
			expected.put("2938", "");
			expected.put("3828", "");
			expected.put("3829", "");
			expected.put("3838", "");
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = Charset.forName("x-Fujitsu-JEF")
				.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		ByteBuffer bb = ByteBuffer.allocate(2);
	
		for (int i = 0; i < 0xFFFF; i++) {
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
			assertEquals(key, expected.get(key), actual.get(key));
		}		
	}
	
	private String toChars(String unicode) {
		unicode = unicode.replaceAll("_.*$", "");
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
