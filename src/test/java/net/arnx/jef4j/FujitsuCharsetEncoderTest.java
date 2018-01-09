package net.arnx.jef4j;

import static org.junit.Assert.*;

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
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

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
	
		for (int cp = 0; cp <= Character.MAX_CODE_POINT; cp++) {
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
	
		for (int cp = 0; cp <= Character.MAX_CODE_POINT; cp++) {
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
	
		for (int cp = 0; cp <= Character.MAX_CODE_POINT; cp++) {
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
				String[] parts = line.split(" ");
				expected.put(parts[0], parts[1]);
			}
		}
		
		Map<String, String> actual = new TreeMap<>();
		
		CharsetEncoder ce = Charset.forName("x-Fujitsu-JEF")
				.newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		CharBuffer cb = CharBuffer.allocate(2);
	
		for (int cp = 0; cp <= Character.MAX_CODE_POINT; cp++) {
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
	public void testEncoder() {
		for (Charset cs : Arrays.asList(
				Charset.forName("x-Fujitsu-EBCDIC"),
				Charset.forName("x-Fujitsu-EBCDIK"),
				Charset.forName("x-Fujitsu-ASCII"),
				Charset.forName("x-Fujitsu-JEF-EBCDIC"),
				Charset.forName("x-Fujitsu-JEF-EBCDIK"),
				Charset.forName("x-Fujitsu-JEF-ASCII"),
				Charset.forName("x-Fujitsu-JEF"))) {
			
			CharsetEncoder ce = cs.newEncoder()
					.onUnmappableCharacter(CodingErrorAction.REPORT)
					.onMalformedInput(CodingErrorAction.REPORT);
			CharBuffer cb = CharBuffer.allocate(2);
		
			for (int cp = 0; cp <= Character.MAX_CODE_POINT; cp++) {
				cb.clear();
				cb.put(Character.toChars(cp));
				cb.flip();
				try {
					ByteBuffer bb = ce.encode(cb);
					cb.flip();
					System.out.println(hex(cb) + " " + hex(bb));
				} catch (CharacterCodingException e) {
				}
				
				ce.reset();
			}
		}
	}
	
	@Test
	public void testDecoder() {
		for (Charset cs : Arrays.asList(
				Charset.forName("x-Fujitsu-EBCDIC"),
				Charset.forName("x-Fujitsu-EBCDIK"),
				Charset.forName("x-Fujitsu-ASCII"))) {
			
			CharsetDecoder cd = cs.newDecoder()
					.onUnmappableCharacter(CodingErrorAction.REPORT)
					.onMalformedInput(CodingErrorAction.REPORT);
			ByteBuffer bb = ByteBuffer.allocate(2);
			
			for (int b = 0; b < 0xFF; b++) {
				bb.clear();
				bb.put((byte)b);
				bb.flip();
				try {
					CharBuffer cb = cd.decode(bb);
					bb.flip();
					System.out.println(hex(bb) + " " + hex(cb));
				} catch (CharacterCodingException e) {
				}
				cd.reset();
			}
		}
		
		for (Charset cs : Arrays.asList(
				Charset.forName("x-Fujitsu-JEF-EBCDIC"),
				Charset.forName("x-Fujitsu-JEF-EBCDIK"),
				Charset.forName("x-Fujitsu-JEF-ASCII"))) {
			
			CharsetDecoder cd = cs.newDecoder()
					.onUnmappableCharacter(CodingErrorAction.REPORT)
					.onMalformedInput(CodingErrorAction.REPORT);
			ByteBuffer bb = ByteBuffer.allocate(4);
			
			for (int b = 0; b < 0xFF; b++) {
				bb.clear();
				bb.put((byte)b);
				bb.flip();
				try {
					CharBuffer cb = cd.decode(bb);
					bb.flip();
					System.out.println(hex(bb) + " " + hex(cb));
				} catch (CharacterCodingException e) {
				}
				cd.reset();
			}
			
			for (int b1 = 0; b1 <= 0xFF; b1++) {
				for (int b2 = 0; b2 <= 0xFF; b2++) {
					bb.clear();
					bb.put((byte)0x28);
					bb.put((byte)b1);
					bb.put((byte)b2);
					bb.put((byte)0x29);
					bb.flip();
					try {
						CharBuffer cb = cd.decode(bb);
						bb.flip();
						System.out.println(hex(bb) + " " + hex(cb));
					} catch (CharacterCodingException e) {
					}
					cd.reset();
				}
			}
		}
		
		for (Charset cs : Arrays.asList(
				Charset.forName("x-Fujitsu-JEF"))) {
			
			CharsetDecoder cd = cs.newDecoder()
					.onUnmappableCharacter(CodingErrorAction.REPORT)
					.onMalformedInput(CodingErrorAction.REPORT);
			ByteBuffer bb = ByteBuffer.allocate(2);
			
			for (int b1 = 0; b1 <= 0xFF; b1++) {
				for (int b2 = 0; b2 <= 0xFF; b2++) {
					bb.clear();
					bb.put((byte)b1);
					bb.put((byte)b2);
					bb.flip();
					try {
						CharBuffer cb = cd.decode(bb);
						bb.flip();
						System.out.println(hex(bb) + " " + hex(cb));
					} catch (CharacterCodingException e) {
					}
					cd.reset();
				}
			}
		}
	}
	
	private static final String HEX = "0123456789ABCDEF";
	
	private static String hex(CharBuffer cb) {
		StringBuilder sb = new StringBuilder();
		while (cb.hasRemaining()) {
			char c = cb.get();
			sb.append(HEX.charAt((c >> 12) & 0xF));
			sb.append(HEX.charAt((c >> 8) & 0xF));
			sb.append(HEX.charAt((c >> 4) & 0xF));
			sb.append(HEX.charAt(c & 0xF));
		}
		return sb.toString();
	}
	
	private static String hex(ByteBuffer bb) {
		StringBuilder sb = new StringBuilder();
		while (bb.hasRemaining()) {
			byte b = bb.get();
			sb.append(HEX.charAt((b >> 4) & 0xF));
			sb.append(HEX.charAt(b & 0xF));
		}
		return sb.toString();
	}

}
