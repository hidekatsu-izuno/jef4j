package net.arnx.jef4j;

import static org.junit.Assert.*;

import java.nio.charset.Charset;

import org.junit.Test;

public class Jef4jCharsetProviderTest {

	@Test
	public void test() {
		Charset fujitsuJef = Charset.forName("x-Fujitsu-JEF");
		
		assertEquals(new byte[] {}, "あいうえお".getBytes(fujitsuJef));
	}

}
