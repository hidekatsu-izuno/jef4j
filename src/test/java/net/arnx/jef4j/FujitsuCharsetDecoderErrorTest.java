package net.arnx.jef4j;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class FujitsuCharsetDecoderErrorTest {
	@ParameterizedTest
	@ValueSource(strings = {
		"x-Fujitsu-JEF", "x-Fujitsu-JEF-HanyoDenshi",
		"x-Fujitsu-JEF-AdobeJapan1", "x-Fujitsu-JEF-Roundtrip",
		"x-Fujitsu-EBCDIC-Lower+JEF", "x-Fujitsu-JEF+EBCDIC-Lower"
	})
	public void testUnmappablePairPreservesFollowingCharacter(String name) throws Exception {
		Charset charset = Charset.forName(name);
		boolean mixed = name.contains("EBCDIC");
		for (int lead : new int[] { 0x00, 0x3F, 0xFF, 0x80 }) {
			ByteBuffer input = ByteBuffer.allocate(8);
			if (mixed) {
				input.put((byte) 0x28);
			}
			int errorPosition = input.position();
			input.put((byte) lead).put((byte) 0x40);
			input.put((byte) 0xA4).put((byte) 0xA2); // あ
			if (mixed) {
				input.put((byte) 0x29).put((byte) 0x81); // a
			}
			input.flip();

			CoderResult result = charset.newDecoder().decode(input, CharBuffer.allocate(8), true);
			assertTrue(result.isUnmappable(), name);
			assertEquals(2, result.length(), name);
			assertEquals(errorPosition, input.position(), name);

			input.rewind();
			assertEquals(mixed ? "あa" : "あ", charset.newDecoder()
					.onUnmappableCharacter(CodingErrorAction.IGNORE).decode(input).toString(), name);
			input.rewind();
			assertEquals(mixed ? "\uFFFDあa" : "\uFFFDあ", charset.newDecoder()
					.onUnmappableCharacter(CodingErrorAction.REPLACE).decode(input).toString(), name);
		}
	}

	@ParameterizedTest
	@ValueSource(ints = { 0x00, 0x28, 0x29, 0x38, 0x3F, 0xFF, 0xA4 })
	public void testIncompletePairWaitsForSecondByte(int lead) {
		CharsetDecoder decoder = Charset.forName("x-Fujitsu-JEF").newDecoder();
		ByteBuffer input = ByteBuffer.wrap(new byte[] { (byte) lead, (byte) 0x40 });
		input.limit(1);
		CharBuffer output = CharBuffer.allocate(4);
		assertTrue(decoder.decode(input, output, false).isUnderflow());
		assertEquals(0, input.position());

		input.limit(2);
		CoderResult result = decoder.decode(input, output, true);
		assertTrue(result.isUnmappable());
		assertEquals(2, result.length());
		assertEquals(0, input.position());

		decoder.reset();
		input.limit(1);
		result = decoder.decode(input, output, true);
		assertTrue(result.isMalformed());
		assertEquals(1, result.length());
	}

	@ParameterizedTest
	@ValueSource(ints = { 0x28, 0x29, 0x38 })
	public void testShiftByteInPureJefIsPartOfPair(int shift) {
		Charset charset = Charset.forName("x-Fujitsu-JEF");
		assertEquals("\uFFFD海", new String(new byte[] {
			(byte) shift, (byte) 0x40, (byte) 0xB3, (byte) 0xA4
		}, charset));
		assertEquals("\uFFFD海", new String(new byte[] {
			(byte) 0xA4, (byte) shift, (byte) 0xB3, (byte) 0xA4
		}, charset));
	}
}
