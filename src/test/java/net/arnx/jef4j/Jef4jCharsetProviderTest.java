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
		
		assertEquals(new byte[] {}, "あいうえお".getBytes(fujitsuJef));
	}
}
