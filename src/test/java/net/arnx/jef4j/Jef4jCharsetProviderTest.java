package net.arnx.jef4j;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
public class Jef4jCharsetProviderTest {

	@Test
	public void test() {
		Charset fujitsuJef = Charset.forName("x-Fujitsu-JEF");
		
		assertArrayEquals(
				new byte[] {
						(byte)0xA4, (byte)0xA2, 
						(byte)0xA4, (byte)0xA4, 
						(byte)0xA4, (byte)0xA6, 
						(byte)0xA4, (byte)0xA8,
						(byte)0xA4, (byte)0xAA
				}, 
				"あいうえお".getBytes(fujitsuJef));
	}
}
