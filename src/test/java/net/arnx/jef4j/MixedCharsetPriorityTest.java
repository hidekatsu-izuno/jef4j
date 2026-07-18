package net.arnx.jef4j;

import static net.arnx.jef4j.util.ByteUtils.hex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

public class MixedCharsetPriorityTest {
	@Test
	public void testAllMixedCharsetPriorities() {
		int sbcsFirst = 0;
		int mbcsFirst = 0;
		for (CharsetType type : CharsetType.values()) {
			if (type.getSBCSTableNo() < 0 || type.getMBCSTableNo() < 0) {
				continue;
			}

			Charset charset = Charset.forName(type.getCharsetName());
			assertTrue(charset.name().contains("+"), charset.name());
			byte[] sbcs = "A".getBytes(charset);
			byte[] mbcs = "あ".getBytes(charset);
			if (type.isMBCSPreferred()) {
				mbcsFirst++;
				assertTrue(sbcs.length > 1, charset.name());
				assertEquals(2, mbcs.length, charset.name());
			} else {
				sbcsFirst++;
				assertEquals(1, sbcs.length, charset.name());
				assertTrue(mbcs.length > 2, charset.name());
			}
			assertEquals("A", new String(sbcs, charset), charset.name());
			assertEquals("あ", new String(mbcs, charset), charset.name());
		}
		assertEquals(41, sbcsFirst);
		assertEquals(41, mbcsFirst);
	}

	@Test
	public void testFujitsuPriority() {
		assertEncoding(
				"x-Fujitsu-EBCDIC-Lower+JEF-HanyoDenshi",
				"AあB", "C128A4A229C2");
		assertEncoding(
				"x-Fujitsu-JEF-HanyoDenshi+EBCDIC-Lower",
				"あA海󠄂", "A4A229C128B3A4");
	}

	@Test
	public void testFujitsuLegacyAliases() {
		assertAliases("x-Fujitsu-EBCDIC-Lower", "x-Fujitsu-EBCDIC");
		assertAliases("x-Fujitsu-EBCDIC-Kana", "x-Fujitsu-EBCDIK");
		assertAliases("x-Fujitsu-EBCDIC-Ascii", "x-Fujitsu-ASCII");

		assertAliases("x-Fujitsu-EBCDIC-Lower+JEF",
				"x-Fujitsu-EBCDIC+JEF", "x-Fujitsu-JEF-EBCDIC");
		assertAliases("x-Fujitsu-JEF+EBCDIC-Lower", "x-Fujitsu-JEF+EBCDIC");
		assertAliases("x-Fujitsu-EBCDIC-Lower+JEF-HanyoDenshi",
				"x-Fujitsu-EBCDIC+JEF-HanyoDenshi", "x-Fujitsu-JEF-HanyoDenshi-EBCDIC");
		assertAliases("x-Fujitsu-JEF-HanyoDenshi+EBCDIC-Lower",
				"x-Fujitsu-JEF-HanyoDenshi+EBCDIC");
		assertAliases("x-Fujitsu-EBCDIC-Lower+JEF-AdobeJapan1",
				"x-Fujitsu-EBCDIC+JEF-AdobeJapan1", "x-Fujitsu-JEF-AdobeJapan1-EBCDIC");
		assertAliases("x-Fujitsu-JEF-AdobeJapan1+EBCDIC-Lower",
				"x-Fujitsu-JEF-AdobeJapan1+EBCDIC");

		assertAliases("x-Fujitsu-EBCDIC-Kana+JEF",
				"x-Fujitsu-EBCDIK+JEF", "x-Fujitsu-JEF-EBCDIK");
		assertAliases("x-Fujitsu-JEF+EBCDIC-Kana", "x-Fujitsu-JEF+EBCDIK");
		assertAliases("x-Fujitsu-EBCDIC-Kana+JEF-HanyoDenshi",
				"x-Fujitsu-EBCDIK+JEF-HanyoDenshi", "x-Fujitsu-JEF-HanyoDenshi-EBCDIK");
		assertAliases("x-Fujitsu-JEF-HanyoDenshi+EBCDIC-Kana",
				"x-Fujitsu-JEF-HanyoDenshi+EBCDIK");
		assertAliases("x-Fujitsu-EBCDIC-Kana+JEF-AdobeJapan1",
				"x-Fujitsu-EBCDIK+JEF-AdobeJapan1", "x-Fujitsu-JEF-AdobeJapan1-EBCDIK");
		assertAliases("x-Fujitsu-JEF-AdobeJapan1+EBCDIC-Kana",
				"x-Fujitsu-JEF-AdobeJapan1+EBCDIK");

		assertAliases("x-Fujitsu-EBCDIC-Ascii+JEF",
				"x-Fujitsu-ASCII+JEF", "x-Fujitsu-JEF-ASCII");
		assertAliases("x-Fujitsu-JEF+EBCDIC-Ascii", "x-Fujitsu-JEF+ASCII");
		assertAliases("x-Fujitsu-EBCDIC-Ascii+JEF-HanyoDenshi",
				"x-Fujitsu-ASCII+JEF-HanyoDenshi", "x-Fujitsu-JEF-HanyoDenshi-ASCII");
		assertAliases("x-Fujitsu-JEF-HanyoDenshi+EBCDIC-Ascii",
				"x-Fujitsu-JEF-HanyoDenshi+ASCII");
		assertAliases("x-Fujitsu-EBCDIC-Ascii+JEF-AdobeJapan1",
				"x-Fujitsu-ASCII+JEF-AdobeJapan1", "x-Fujitsu-JEF-AdobeJapan1-ASCII");
		assertAliases("x-Fujitsu-JEF-AdobeJapan1+EBCDIC-Ascii",
				"x-Fujitsu-JEF-AdobeJapan1+ASCII");
	}

	@Test
	public void testHitachiPriority() {
		assertEncoding(
				"x-Hitachi-EBCDIC+KEIS78-HanyoDenshi",
				"aあb", "810A42A4A20A4182");
		assertEncoding(
				"x-Hitachi-KEIS78-HanyoDenshi+EBCDIC",
				"あa海", "A4A20A41810A42B3A4");
		assertAlias(
				"x-Hitachi-EBCDIC+KEIS78-HanyoDenshi",
				"x-Hitachi-KEIS78-HanyoDenshi-EBCDIC");
	}

	@Test
	public void testNecPriority() {
		assertEncoding(
				"x-NEC-JIS8+JIPSJ-HanyoDenshi",
				"aあb", "611A7024221A7162");
		assertEncoding(
				"x-NEC-JIPSJ-HanyoDenshi+JIS8",
				"あa海", "24221A71611A703324");
		assertEncoding(
				"x-NEC-JIPSE+EBCDIK",
				"あa海", "E07F3F76593F75F3E0");
		assertAlias(
				"x-NEC-JIS8+JIPSJ-HanyoDenshi",
				"x-NEC-JIPSJ-HanyoDenshi-JIS8");
	}

	@Test
	public void testIbmPriority() {
		assertEncoding(
				"x-IBM-8482+11684",
				"A　B", "C10E40400FC2");
		assertEncoding(
				"x-IBM-11684+8482",
				"　A海", "40400FC10E45A7");
		assertAlias("x-IBM-8482+11684", "x-IBM-1390");
	}

	private static void assertEncoding(String charsetName, String text, String expectedBytes) {
		Charset charset = Charset.forName(charsetName);
		byte[] bytes = text.getBytes(charset);
		assertEquals(expectedBytes, hex(bytes), charsetName);
		assertEquals(text, new String(bytes, charset), charsetName);
	}

	private static void assertAlias(String canonicalName, String alias) {
		Charset charset = Charset.forName(alias);
		assertEquals(canonicalName, charset.name());
		assertTrue(charset.aliases().contains(alias));
	}

	private static void assertAliases(String canonicalName, String... aliases) {
		for (String alias : aliases) {
			assertAlias(canonicalName, alias);
		}
	}
}
