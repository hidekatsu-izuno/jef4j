package net.arnx.jef4j;

import static org.junit.Assert.*;

import java.nio.charset.Charset;

import org.junit.Test;

import net.arnx.jef4j.util.ByteUtils;

public class Jef4jCharsetProviderTest {
	@Test
	public void testEncoder() {
		assertEquals(
				"A4A2A4A4A4A6A4A8A4AAC4D4",
				ByteUtils.hex("あいうえお辻".getBytes(Charset.forName("x-Fujitsu-JEF"))));
	
		assertEquals(
				"C128A4A229C3",
				ByteUtils.hex("AあC".getBytes(Charset.forName("x-Fujitsu-JEF-ASCII"))));
		
		assertEquals(
				"77A941E0",
				ByteUtils.hex("🄀𠆤".getBytes(Charset.forName("x-Fujitsu-JEF"))));
		
		assertEquals(
				"71ABA1AB",
				ByteUtils.hex("\uD82C\uDC19\u3099".getBytes(Charset.forName("x-Fujitsu-JEF"))));
		
		assertEquals(
			"F2CDB0B3E4C6CFB6",
			ByteUtils.hex("鯵鰺篭籠".getBytes(Charset.forName("x-Fujitsu-JEF"))));
	}

	@Test
	public void testDecoder() {
		assertEquals(
				"　辻辻岧𭏾𨦸",
				new String(new byte[] {
						(byte)0x40, (byte)0x40,
						(byte)0x67, (byte)0xA5,
						(byte)0xC4, (byte)0xD4,
						(byte)0x4A, (byte)0xF2,
						(byte)0x48, (byte)0xC2,
						(byte)0x6A, (byte)0xC6
				}, Charset.forName("x-Fujitsu-JEF")));
		
		assertEquals(
				"はばぱ",
				new String(new byte[] {
						(byte)0xA4, (byte)0xCF,
						(byte)0xA4, (byte)0xD0,
						(byte)0xA4, (byte)0xD1
				}, Charset.forName("x-Fujitsu-JEF")));

		assertEquals(
			"鯵鰺篭籠",
			new String(new byte[] {
					(byte)0xF2, (byte)0xCD,
					(byte)0xB0, (byte)0xB3,
					(byte)0xE4, (byte)0xC6,
					(byte)0xCF, (byte)0xB6
			}, Charset.forName("x-Fujitsu-JEF-HanyoDenshi")));
	}
}
