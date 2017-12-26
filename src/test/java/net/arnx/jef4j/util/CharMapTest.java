package net.arnx.jef4j.util;

import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class CharMapTest {

	@Test
	public void test() {
		CharObjMap<Character> map = new CharObjMap<>();
		map.put('\u0001', '\u0003');
		map.put('\u0002', '\u0100');
		map.put('\u0060', '\u0002');
		
		assertEquals((Character)'\u0003', map.get('\u0001'));
		assertEquals((Character)'\u0100', map.get('\u0002'));
		assertEquals((Character)'\u0002', map.get('\u0060'));
		assertEquals(null, map.get('\u0040'));
	}
}
