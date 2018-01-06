package net.arnx.jef4j;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

import org.junit.Test;

public class Jef4jCharsetProviderTest {
	@Test
	public void test() {
		assertArrayEquals(
				new byte[] {
						(byte)0xA4, (byte)0xA2, 
						(byte)0xA4, (byte)0xA4, 
						(byte)0xA4, (byte)0xA6, 
						(byte)0xA4, (byte)0xA8,
						(byte)0xA4, (byte)0xAA
				}, 
				"あいうえお".getBytes(Charset.forName("x-Fujitsu-JEF")));
	
		assertArrayEquals(
				new byte[] {
						(byte)0xC1,
						(byte)0x28,
						(byte)0xA4, (byte)0xA2,
						(byte)0x29,
						(byte)0xC3
				}, 
				"AあC".getBytes(Charset.forName("x-Fujitsu-JEF-ASCII")));
		
		assertArrayEquals(
				new byte[] {
						(byte)0x77, (byte)0xA9
				}, 
				"🄀".getBytes(Charset.forName("x-Fujitsu-JEF")));
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
