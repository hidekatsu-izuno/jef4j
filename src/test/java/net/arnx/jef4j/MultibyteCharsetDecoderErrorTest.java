package net.arnx.jef4j;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class MultibyteCharsetDecoderErrorTest {
	@ParameterizedTest
	@CsvSource({
		"x-IBM-11684, '', 4040, ''",
		"x-IBM-1390, 0E, 4040, 0FC1",
		"x-IBM-1399, 0E, 4040, 0FC1",
		"x-IBM-11684+8482, '', 4040, 0FC1",
		"x-IBM-11684+5123, '', 4040, 0FC1",
		"x-Hitachi-KEIS78, '', 4040, ''",
		"x-Hitachi-KEIS83, '', 4040, ''",
		"x-Hitachi-EBCDIC+KEIS78, 0A42, 4040, 0A41C1",
		"x-Hitachi-EBCDIC+KEIS83, 0A42, 4040, 0A41C1",
		"x-Hitachi-KEIS78+EBCDIC, '', 4040, 0A41C1",
		"x-Hitachi-KEIS83+EBCDIC, '', 4040, 0A41C1",
		"x-NEC-JIPSJ, '', 2121, ''",
		"x-NEC-JIPSE, '', 4F4F, ''",
		"x-NEC-JIS8+JIPSJ, 1A70, 2121, 1A7141",
		"x-NEC-EBCDIK+JIPSE, 3F75, 4F4F, 3F76C1",
		"x-NEC-JIPSJ+JIS8, '', 2121, 1A7141",
		"x-NEC-JIPSE+EBCDIK, '', 4F4F, 3F76C1"
	})
	public void testInvalidPairRecovery(String name, String prefix, String valid, String suffix) throws Exception {
		Charset charset = Charset.forName(name);
		for (String invalid : new String[] { "0040", "FF40" }) {
			ByteBuffer input = ByteBuffer.wrap(bytes(prefix + invalid + valid + suffix));
			CharBuffer output = CharBuffer.allocate(8);
			CoderResult result = charset.newDecoder().decode(input, output, true);
			assertTrue(result.isUnmappable());
			assertEquals(2, result.length());
			assertEquals(prefix.length() / 2, input.position());
			assertEquals(0, output.position());

			String expected = suffix.isEmpty() ? "\u3000" : "\u3000A";
			input.rewind();
			assertEquals(expected, charset.newDecoder().onUnmappableCharacter(CodingErrorAction.IGNORE)
					.decode(input).toString());
			input.rewind();
			assertEquals("\uFFFD" + expected, charset.newDecoder().onUnmappableCharacter(CodingErrorAction.REPLACE)
					.decode(input).toString());

			CharsetDecoder decoder = charset.newDecoder();
			input.rewind();
			int limit = input.limit();
			input.limit(prefix.length() / 2 + 1);
			assertTrue(decoder.decode(input, output, false).isUnderflow());
			assertEquals(prefix.length() / 2, input.position());
			input.limit(limit);
			result = decoder.decode(input, output, true);
			assertTrue(result.isUnmappable());
			assertEquals(2, result.length());

			decoder.reset();
			input.rewind();
			input.limit(prefix.length() / 2 + 1);
			result = decoder.decode(input, output, true);
			assertTrue(result.isMalformed());
			assertEquals(1, result.length());
		}
	}

	@ParameterizedTest
	@CsvSource({
		"x-IBM-11684, 0E404040, \uFFFD\u3000",
		"x-IBM-11684, 0F404040, \uFFFD\u3000",
		"x-IBM-11684, 40284040, \uFFFD\u3000",
		"x-IBM-1390, 0E402840400FC1, \uFFFD\u3000A",
		"x-IBM-1390, 0E403840400FC1, \uFFFD\u3000A",
		"x-IBM-1390, 0E402940400FC1, \uFFFD\u3000A"
	})
	public void testInvalidControlPair(String name, String input, String expected) {
		assertEquals(expected, new String(bytes(input), Charset.forName(name)));
	}

	@ParameterizedTest
	@CsvSource({
		"x-Fujitsu-JEF, 2829, A4A2, あ",
		"x-Fujitsu-JEF, 4038, A4A2, あ",
		"x-Fujitsu-JEF, 30E2, A4A2, あ",
		"x-IBM-11684, 0E0F, 4040, '\u3000'",
		"x-IBM-11684, 400F, 4040, '\u3000'",
		"x-Hitachi-KEIS78, 0A42, 4040, '\u3000'",
		"x-Hitachi-KEIS83, 400A, 4040, '\u3000'",
		"x-NEC-JIPSJ, 1A70, 2121, '\u3000'",
		"x-NEC-JIPSJ, 211A, 2121, '\u3000'",
		"x-NEC-JIPSE, 3F75, 4F4F, '\u3000'",
		"x-NEC-JIPSE, 4F3F, 4F4F, '\u3000'"
	})
	public void testPureDoubleByteModeDoesNotInterpretShifts(String name, String invalid, String valid, String expected) throws Exception {
		Charset charset = Charset.forName(name);
		ByteBuffer input = ByteBuffer.wrap(bytes(invalid + valid));
		CoderResult result = charset.newDecoder().decode(input, CharBuffer.allocate(8), true);
		assertTrue(result.isUnmappable());
		assertEquals(2, result.length());
		assertEquals(0, input.position());
		assertEquals("\uFFFD" + expected, charset.newDecoder().onUnmappableCharacter(CodingErrorAction.REPLACE)
				.decode(input).toString());
		input.rewind();
		assertEquals(expected, charset.newDecoder().onUnmappableCharacter(CodingErrorAction.IGNORE)
				.decode(input).toString());
	}

	private static byte[] bytes(String hex) {
		byte[] result = new byte[hex.length() / 2];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
		}
		return result;
	}
}
