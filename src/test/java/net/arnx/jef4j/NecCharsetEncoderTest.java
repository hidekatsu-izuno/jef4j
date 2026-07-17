package net.arnx.jef4j;

import static net.arnx.jef4j.util.ByteUtils.hex;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

public class NecCharsetEncoderTest {
	@Test
	public void testAllNecEncoders() {
		for (CharsetType type : CharsetType.values()) {
			if (type.getCharsetName().startsWith("x-NEC-")) {
				assertDoesNotThrow(() -> Charset.forName(type.getCharsetName()).newEncoder(), type.getCharsetName());
			}
		}
	}

	@Test
	public void testNecJipsjEncoder() {
		Charset charset = Charset.forName("x-NEC-JIPSJ");
		assertEquals("2121234124223021", hex("　Ａあ亜".getBytes(charset)));
	}

	@Test
	public void testNecJipseEncoder() {
		Charset charset = Charset.forName("x-NEC-JIPSE");
		assertEquals("4F4F7BC1E07FF04F", hex("　Ａあ亜".getBytes(charset)));
	}

	@Test
	public void testNecJipsjJis8Encoder() {
		Charset charset = Charset.forName("x-NEC-JIPSJ-JIS8");
		assertEquals("611A7024221A71621A7033241A7163", hex("aあb海c".getBytes(charset)));
	}

	@Test
	public void testNecJipseEbcdikEncoder() {
		Charset charset = Charset.forName("x-NEC-JIPSE-EBCDIK");
		assertEquals("593F75E07F3F76623F75F3E03F7663", hex("aあb海c".getBytes(charset)));
	}

	@Test
	public void testNecJipseHanyoDenshiEncoder() {
		Charset charset = Charset.forName("x-NEC-JIPSE-HanyoDenshi");
		assertEquals("F05E", hex("飴󠄃".getBytes(charset)));
	}

	@Test
	public void testNecJipseAdobeJapan1Encoder() {
		Charset charset = Charset.forName("x-NEC-JIPSE-AdobeJapan1");
		assertEquals("F05E", hex("飴󠄁".getBytes(charset)));
	}
}
