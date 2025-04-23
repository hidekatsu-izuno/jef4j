package net.arnx.jef4j.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class LongObjMapTest {

	@Test
	public void test() {
		LongObjMap<Character> map = new LongObjMap<>();
		map.put('\u0001', '\u0003');
		map.put('\u0002', '\u0100');
		map.put('\u0060', '\u0002');
		
		assertEquals((Character)'\u0003', map.get('\u0001'));
		assertEquals((Character)'\u0100', map.get('\u0002'));
		assertEquals((Character)'\u0002', map.get('\u0060'));
		assertEquals(null, map.get('\u0040'));
	}
}
