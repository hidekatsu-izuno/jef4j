package net.arnx.jef4j;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

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
				"77A941E0C4B9",
				ByteUtils.hex("🄀𠆤長".getBytes(Charset.forName("x-Fujitsu-JEF"))));
		
		assertEquals(
				"DBEB",
				ByteUtils.hex("\u689B".getBytes(Charset.forName("x-Fujitsu-JEF"))));

		assertEquals(
				"71AC",
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
				"はばぱ゛゜",
				new String(new byte[] {
						(byte)0xA4, (byte)0xCF,
						(byte)0xA4, (byte)0xD0,
						(byte)0xA4, (byte)0xD1,
						(byte)0xA1, (byte)0xAB,
						(byte)0xA1, (byte)0xAC
				}, Charset.forName("x-Fujitsu-JEF")));

		assertEquals(
				"長長",
				new String(new byte[] {
						(byte)0x73, (byte)0xFB,
						(byte)0xC4, (byte)0xB9
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

	@Test
	public void testIbmPrivateUseArea() {
		Charset dbcs = Charset.forName("x-IBM-11684");
		assertEquals("80A1A0FE", ByteUtils.hex("\uE000\uEC1D".getBytes(dbcs)));
		assertEquals("\uE000\uEC1D", new String(new byte[] {
				(byte)0x80, (byte)0xA1, (byte)0xA0, (byte)0xFE
		}, dbcs));

		Charset mixed = Charset.forName("x-IBM-1390");
		assertEquals("C10E80A10FC2", ByteUtils.hex("A\uE000B".getBytes(mixed)));
		assertEquals("A\uE000B", new String(new byte[] {
				(byte)0xC1, (byte)0x0E, (byte)0x80, (byte)0xA1, (byte)0x0F, (byte)0xC2
		}, mixed));
	}
}
