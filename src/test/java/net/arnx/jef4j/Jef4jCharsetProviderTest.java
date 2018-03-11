package net.arnx.jef4j;

import static org.junit.Assert.*;

import java.nio.charset.Charset;

import org.junit.Test;

import net.arnx.jef4j.util.ByteUtils;

public class Jef4jCharsetProviderTest {
	@Test
	public void testEncoder() {
		assertArrayEquals(
				new byte[] {
						(byte)0xA4, (byte)0xA2, 
						(byte)0xA4, (byte)0xA4, 
						(byte)0xA4, (byte)0xA6, 
						(byte)0xA4, (byte)0xA8,
						(byte)0xA4, (byte)0xAA,
						(byte)0xC4, (byte)0xD4
				}, 
				"あいうえお辻".getBytes(Charset.forName("x-Fujitsu-JEF")));
	
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
						(byte)0x77, (byte)0xA9,
						(byte)0x41, (byte)0xE0
				}, 
				"🄀𠆤".getBytes(Charset.forName("x-Fujitsu-JEF")));
		
		
		assertEquals(
				"71ABA1AB",
				ByteUtils.hex("\uD82C\uDC19\u3099".getBytes(Charset.forName("x-Fujitsu-JEF"))));
	}

	@Test
	public void testDecoder() {
		assertEquals(
				"　辻辻岧",
				new String(new byte[] {
						(byte)0x40, (byte)0x40,
						(byte)0x67, (byte)0xA5,
						(byte)0xC4, (byte)0xD4,
						(byte)0x4A, (byte)0xF2
				}, Charset.forName("x-Fujitsu-JEF")));
		
		assertEquals(
				"はばぱ",
				new String(new byte[] {
						(byte)0xA4, (byte)0xCF,
						(byte)0xA4, (byte)0xD0,
						(byte)0xA4, (byte)0xD1
				}, Charset.forName("x-Fujitsu-JEF")));
	}
}
