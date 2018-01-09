package net.arnx.jef4j;

import static org.junit.Assert.*;

import java.nio.charset.Charset;

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

}
