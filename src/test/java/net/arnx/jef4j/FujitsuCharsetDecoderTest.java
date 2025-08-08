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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import net.arnx.jef4j.util.ByteUtils;

public class FujitsuCharsetDecoderTest {

	@Test
	public void testFujitsuEbcdicDecoder() throws IOException {
		Charset EBCDIC = Charset.forName("x-Fujitsu-EBCDIC");
		assertEquals("a\uFFFDus\uFFFDb\uFFFD\uFFFDu\uFFFDc", new String(new byte[] {
			(byte)0x81, (byte)0x28, (byte)0xA4, (byte)0xA2, (byte)0x29, (byte)0x82, (byte)0x28, (byte)0xB3, (byte)0xA4, (byte)0x29, (byte)0x83
		}, EBCDIC));

		Map<String, String> expected = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/ebcdic_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				expected.put(parts[1], parts[0]);
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = EBCDIC
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
		Charset EBCDIK = Charset.forName("x-Fujitsu-EBCDIK");
		assertEquals("ｱ\uFFFDﾏﾍ\uFFFDｲ\uFFFD\uFFFDﾏ\uFFFDｳ", new String(new byte[] {
			(byte)0x81, (byte)0x28, (byte)0xA4, (byte)0xA2, (byte)0x29, (byte)0x82, (byte)0x28, (byte)0xB3, (byte)0xA4, (byte)0x29, (byte)0x83
		}, EBCDIK));

		Map<String, String> expected = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/ebcdik_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				expected.put(parts[1], parts[0]);
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = EBCDIK
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
		Charset ASCII = Charset.forName("x-Fujitsu-ASCII");
		assertEquals("a\uFFFDus\uFFFDb\uFFFD\uFFFDu\uFFFDc", new String(new byte[] {
			(byte)0x81, (byte)0x28, (byte)0xA4, (byte)0xA2, (byte)0x29, (byte)0x82, (byte)0x28, (byte)0xB3, (byte)0xA4, (byte)0x29, (byte)0x83
		}, ASCII));

		Map<String, String> expected = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/ascii_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				expected.put(parts[1], parts[0]);
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = ASCII
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
		Charset JEF = Charset.forName("x-Fujitsu-JEF");
		assertEquals("\uFFFD\uFFFDあ\uFFFD\uFFFD\uFFFD海\uFFFD\uFFFD", new String(new byte[] {
			(byte)0x81, (byte)0x28, (byte)0xA4, (byte)0xA2, (byte)0x29, (byte)0x82, (byte)0x28, (byte)0xB3, (byte)0xA4, (byte)0x29, (byte)0x83
		}, JEF));

		Map<String, String> expected = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/jef_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) continue;
				
				String[] parts = line.split(" ");
				String unicode = toChars(parts[0], false);
				if (!unicode.equals("FFFD")) {
					expected.put(parts[1], unicode);
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = JEF
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
	public void testFujitsuJefHanyoDenshiDecoder() throws IOException {
		Map<String, String> expected = new TreeMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/jef_mapping.txt"), 
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) continue;
				
				String[] parts = line.split(" ");
				String unicode = toChars(parts[0], true);
				if (!unicode.equals("FFFD")) {
					expected.put(parts[1], unicode);
				}
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetDecoder cd = Charset.forName("x-Fujitsu-JEF-HanyoDenshi")
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
	public void testFujitsuJefUserDefinedSpaceDecoder() throws IOException {
		Map<String, String> actual = new TreeMap<>();

		CharsetDecoder cd = Charset.forName("x-Fujitsu-JEF")
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

		assertEquals("E000", actual.get("80A1"));
		assertEquals("E05D", actual.get("80FE"));
		assertEquals("E814", actual.get("96A1"));
		assertEquals("E871", actual.get("96FE"));
		assertEquals("EBC0", actual.get("A0A1"));
		assertEquals("EC1D", actual.get("A0FE"));
	}
	
	@Test
	public void testFujitsuJefEbcdicEncoder() throws IOException {
		Charset JEF_EBCDIC = Charset.forName("x-Fujitsu-JEF-EBCDIC");
		assertEquals("aあb海c", new String(new byte[] {
			(byte)0x81, (byte)0x28, (byte)0xA4, (byte)0xA2, (byte)0x29, (byte)0x82, (byte)0x28, (byte)0xB3, (byte)0xA4, (byte)0x29, (byte)0x83
		}, JEF_EBCDIC));
	}

	@Test
	public void testFujitsuJefEbcdikEncoder() throws IOException {
		Charset JEF_EBCDIK = Charset.forName("x-Fujitsu-JEF-EBCDIK");
		assertEquals("ｱあｲ海ｳ", new String(new byte[] {
			(byte)0x81, (byte)0x28, (byte)0xA4, (byte)0xA2, (byte)0x29, (byte)0x82, (byte)0x28, (byte)0xB3, (byte)0xA4, (byte)0x29, (byte)0x83
		}, JEF_EBCDIK));
	}

	@Test
	public void testFujitsuJefAsciiEncoder() throws IOException {
		Charset JEF_ASCII = Charset.forName("x-Fujitsu-JEF-ASCII");
		assertEquals("aあb海c", new String(new byte[] {
			(byte)0x81, (byte)0x28, (byte)0xA4, (byte)0xA2, (byte)0x29, (byte)0x82, (byte)0x28, (byte)0xB3, (byte)0xA4, (byte)0x29, (byte)0x83
		}, JEF_ASCII));
	}
	
	private static String toChars(String unicode, boolean useHanyoDenshi) {
		if (!useHanyoDenshi) {
			unicode = unicode.replaceAll("_E.*$", "");
		} else {
			unicode = unicode.replaceAll("/.*$", "");
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
