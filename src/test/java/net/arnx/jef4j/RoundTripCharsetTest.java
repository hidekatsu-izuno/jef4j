package net.arnx.jef4j;

import static net.arnx.jef4j.util.ByteUtils.hex;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class RoundTripCharsetTest {
	@Test
	public void testExclusionsAreSuppliedByCaller() {
		Charset base = Charset.forName("Windows-31J");
		char[] characters = { 'A' };
		Charset charset = new RoundTripCharset(CharsetType.WINDOWS_31J_RT, base, characters);
		characters[0] = 'B';
		assertFalse(charset.newEncoder().canEncode('A'));
		assertTrue(charset.newEncoder().canEncode('\u00A5'));
		assertEquals("3F5C", hex("A\u00A5".getBytes(charset)));
		assertEquals("A", new String(new byte[] { 0x41 }, charset));
		assertFalse(charset.contains(StandardCharsets.US_ASCII));

		Charset unfiltered = new RoundTripCharset(CharsetType.WINDOWS_31J_RT, base, new char[0]);
		assertTrue(unfiltered.newEncoder().canEncode('\u00A5'));
		assertEquals("415C", hex("A\u00A5".getBytes(unfiltered)));
		assertTrue(unfiltered.contains(StandardCharsets.US_ASCII));
	}

	@ParameterizedTest
	@CsvSource({
		"x-Windows-31J-RoundTrip, Windows-31J",
		"x-eucJP-Open-RoundTrip, x-eucJP-Open"
	})
	public void testRegistration(String name, String baseName) {
		Charset charset = Charset.forName(name.toLowerCase(Locale.ROOT));
		assertInstanceOf(RoundTripCharset.class, charset);
		assertEquals(name, charset.name());
		assertEquals(charset, Charset.forName(name.toUpperCase(Locale.ROOT)));
		assertEquals(charset, charset.newDecoder().charset());
		assertEquals(charset, charset.newEncoder().charset());
		assertTrue(charset.contains(charset));
		assertTrue(charset.contains(StandardCharsets.US_ASCII));
		assertFalse(charset.contains(StandardCharsets.UTF_8));
	}

	@ParameterizedTest
	@CsvSource({
		"x-Windows-31J-RoundTrip, Aあｱ繊, 4182A0B19140",
		"x-Windows-31J-RoundTrip, \uE000\uE757, F040F9FC",
		"x-eucJP-Open-RoundTrip, Aあｱ丂, 41A4A28EB18FB0A1",
		"x-eucJP-Open-RoundTrip, 纊, 8FD4E3"
	})
	public void testReversibleMappings(String name, String text, String expected) throws Exception {
		Charset charset = Charset.forName(name);
		assertEquals(expected, hex(text.getBytes(charset)));
		assertEquals(text, charset.newDecoder().decode(ByteBuffer.wrap(bytes(expected))).toString());
		assertTrue(charset.newEncoder().canEncode(text));
	}

	@ParameterizedTest
	@ValueSource(strings = { "x-Windows-31J-RoundTrip", "x-eucJP-Open-RoundTrip" })
	public void testNonReversibleCharacters(String name) throws Exception {
		Charset charset = Charset.forName(name);
		CharsetEncoder encoder = charset.newEncoder();
		assertFalse(encoder.canEncode('\u00A5'));
		assertFalse(encoder.canEncode('\u203E'));
		assertTrue(encoder.canEncode("\\~"));

		String text = "A\u00A5\u203EB";
		CharBuffer input = CharBuffer.wrap(text);
		ByteBuffer output = ByteBuffer.allocate(16);
		CoderResult result = encoder.encode(input, output, true);
		assertTrue(result.isUnmappable());
		assertEquals(1, result.length());
		assertEquals(1, input.position());
		assertEquals(1, output.position());
		assertEquals("413F3F42", hex(text.getBytes(charset)));
		assertEquals("4142", hex(encoder.reset().onUnmappableCharacter(CodingErrorAction.IGNORE)
				.encode(CharBuffer.wrap(text))));
		assertEquals("41212142", hex(encoder.reset().onUnmappableCharacter(CodingErrorAction.REPLACE)
				.replaceWith(new byte[] { 0x21 }).encode(CharBuffer.wrap(text))));
	}

	@ParameterizedTest
	@CsvSource({
		"x-Windows-31J-RoundTrip, ED40, FA5C, 纊",
		"x-Windows-31J-RoundTrip, EEEF, FA40, ⅰ",
		"x-eucJP-Open-RoundTrip, F9A1, 8FD4E3, 纊",
		"x-eucJP-Open-RoundTrip, 8FF3FD, ADB5, Ⅰ",
		"x-eucJP-Open-RoundTrip, 8FF4A1, ADB7, Ⅲ",
		"x-eucJP-Open-RoundTrip, 8FF4AB, ADEA, ㈱"
	})
	public void testRejectNonRoundTripBytes(String name, String duplicate, String canonical, String text) throws Exception {
		Charset charset = Charset.forName(name);
		CharsetDecoder decoder = charset.newDecoder();
		ByteBuffer input = ByteBuffer.wrap(bytes("41" + duplicate + "42"));
		CharBuffer output = CharBuffer.allocate(8);
		CoderResult result = decoder.decode(input, output, true);
		assertTrue(result.isUnmappable());
		assertEquals(duplicate.length() / 2, result.length());
		assertEquals(1, input.position());
		assertEquals(1, output.position());
		assertEquals(input.capacity(), input.limit());
		assertEquals("A\uFFFDB", new String(input.array(), charset));
		input.rewind();
		assertEquals("AB", decoder.reset().onUnmappableCharacter(CodingErrorAction.IGNORE).decode(input).toString());
		input.rewind();
		assertEquals("A!B", decoder.reset().onUnmappableCharacter(CodingErrorAction.REPLACE)
				.replaceWith("!").decode(input).toString());
		assertFalse(charset.newEncoder().isLegalReplacement(bytes(duplicate)));
		assertTrue(charset.newEncoder().isLegalReplacement(bytes(canonical)));
		assertEquals(canonical, hex(text.getBytes(charset)));
		assertEquals(text, new String(bytes(canonical), charset));
	}

	@Test
	public void testByteExclusionsAreSuppliedByCaller() {
		int[] ranges = { 0x41, 0x41 };
		Charset charset = new RoundTripCharset(CharsetType.WINDOWS_31J_RT,
				Charset.forName("Windows-31J"), new char[0], ranges, b -> 1);
		ranges[0] = ranges[1] = 0x42;
		CoderResult result = charset.newDecoder().decode(ByteBuffer.wrap(new byte[] { 0x41 }), CharBuffer.allocate(1), true);
		assertTrue(result.isUnmappable());
		assertEquals(1, result.length());
		assertEquals("B", new String(new byte[] { 0x42 }, charset));
		assertEquals("41", hex("A".getBytes(charset)));
		assertFalse(charset.contains(StandardCharsets.US_ASCII));
	}

	@ParameterizedTest
	@CsvSource({
		"x-Windows-31J-RoundTrip, 82ED40",
		"x-eucJP-Open-RoundTrip, A4ADE2A1"
	})
	public void testExcludedBytePatternsAcrossCharacterBoundaries(String name, String encoded) throws Exception {
		Charset charset = Charset.forName(name);
		ByteBuffer original = ByteBuffer.wrap(bytes(encoded));
		CharBuffer decoded = charset.newDecoder().decode(original);
		assertEquals(encoded, hex(charset.newEncoder().encode(decoded)));
	}

	@ParameterizedTest
	@CsvSource({
		"x-Windows-31J-RoundTrip, ED40",
		"x-eucJP-Open-RoundTrip, F9A1",
		"x-eucJP-Open-RoundTrip, 8FF3FD"
	})
	public void testSplitExcludedByteSequence(String name, String excluded) {
		Charset charset = Charset.forName(name);
		int length = excluded.length() / 2;
		for (boolean direct : new boolean[] { false, true }) {
			for (int split = 1; split < length; split++) {
				ByteBuffer input = direct ? ByteBuffer.allocateDirect(16) : ByteBuffer.allocate(16);
				input.position(1);
				input.put(bytes("41" + excluded + "42")).flip();
				input.position(1);
				input = input.slice().asReadOnlyBuffer();
				input.limit(1 + split);
				CharBuffer output = CharBuffer.allocate(8);
				CharsetDecoder decoder = charset.newDecoder();
				assertTrue(decoder.decode(input, output, false).isUnderflow());
				assertEquals(1, input.position());
				assertEquals(1, output.position());
				input.limit(length + 2);
				CoderResult result = decoder.decode(input, output, true);
				assertTrue(result.isUnmappable());
				assertEquals(length, result.length());
				assertEquals(1, input.position());
				assertEquals(1, output.position());
				assertEquals(length + 2, input.limit());
				decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
				assertTrue(decoder.decode(input, output, true).isUnderflow());
				assertTrue(decoder.flush(output).isUnderflow());
				output.flip();
				assertEquals("A\uFFFDB", output.toString());

				input.position(1).limit(1 + split);
				result = decoder.reset().decode(input, CharBuffer.allocate(8), true);
				assertTrue(result.isMalformed());
				assertEquals(split, result.length());
				assertEquals(1, input.position());
			}
		}
	}

	@ParameterizedTest
	@CsvSource({
		"x-Windows-31J-RoundTrip, ED40",
		"x-eucJP-Open-RoundTrip, 8FF3FD"
	})
	public void testSmallOutputBuffersAroundExcludedBytes(String name, String excluded) {
		CharsetDecoder decoder = Charset.forName(name).newDecoder().onUnmappableCharacter(CodingErrorAction.REPLACE);
		ByteBuffer input = ByteBuffer.wrap(bytes("4142" + excluded + "43" + excluded + "44")).asReadOnlyBuffer();
		int limit = input.limit();
		CharBuffer output = CharBuffer.allocate(1);
		StringBuilder actual = new StringBuilder();
		CoderResult result;
		do {
			result = decoder.decode(input, output, true);
			assertFalse(result.isError());
			assertEquals(limit, input.limit());
			output.flip();
			actual.append(output);
			output.clear();
		} while (result.isOverflow());
		assertTrue(result.isUnderflow());
		assertTrue(decoder.flush(output).isUnderflow());
		assertEquals("AB\uFFFDC\uFFFDD", actual.toString());
	}

	@ParameterizedTest
	@ValueSource(strings = { "80", "A0", "FD", "FF" })
	public void testInvalidLeadingByteBeforeExcludedSequence(String invalid) throws Exception {
		Charset charset = Charset.forName("x-Windows-31J-RoundTrip");
		CharsetDecoder decoder = charset.newDecoder();
		ByteBuffer input = ByteBuffer.wrap(bytes("41" + invalid + "ED4042"));
		CharBuffer output = CharBuffer.allocate(8);
		CoderResult result = decoder.decode(input, output, true);
		assertTrue(result.isMalformed());
		assertEquals(1, result.length());
		assertEquals(1, input.position());
		assertEquals(1, output.position());
		input.rewind();
		assertEquals("A\uFFFD\uFFFDB", decoder.reset().onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE).decode(input).toString());
		input.rewind();
		assertEquals("AB", decoder.reset().onMalformedInput(CodingErrorAction.IGNORE)
				.onUnmappableCharacter(CodingErrorAction.IGNORE).decode(input).toString());
	}

	@ParameterizedTest
	@CsvSource({
		"x-Windows-31J-RoundTrip, 8130, true, 1, 0",
		"x-Windows-31J-RoundTrip, 81FD, false, 2, ''",
		"x-Windows-31J-RoundTrip, 81, true, 1, ''",
		"x-eucJP-Open-RoundTrip, 8EE0, false, 2, ''",
		"x-eucJP-Open-RoundTrip, 8FA1A1, false, 3, ''",
		"x-eucJP-Open-RoundTrip, 8F, true, 1, ''",
		"x-eucJP-Open-RoundTrip, 8FB0, true, 2, ''"
	})
	public void testInvalidBytes(String name, String invalid, boolean malformed, int length, String remainder) throws Exception {
		CharsetDecoder decoder = Charset.forName(name).newDecoder();
		ByteBuffer input = ByteBuffer.wrap(bytes(invalid));
		CharBuffer output = CharBuffer.allocate(8);
		CoderResult result = decoder.decode(input, output, true);
		assertTrue(result.isError());
		assertEquals(malformed, result.isMalformed());
		assertEquals(length, result.length());
		assertEquals(0, input.position());
		assertEquals(0, output.position());
		assertEquals("\uFFFD" + remainder, decoder.reset()
				.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE)
				.decode(input).toString());
		input.rewind();
		assertEquals(remainder, decoder.reset()
				.onMalformedInput(CodingErrorAction.IGNORE).onUnmappableCharacter(CodingErrorAction.IGNORE)
				.decode(input).toString());
	}

	@ParameterizedTest
	@ValueSource(strings = { "x-Windows-31J-RoundTrip", "x-eucJP-Open-RoundTrip" })
	public void testSurrogates(String name) throws Exception {
		CharsetEncoder encoder = Charset.forName(name).newEncoder();
		ByteBuffer output = ByteBuffer.allocate(16);
		for (String text : new String[] { "\uDC00", "\uD800A", "\uD800" }) {
			CharBuffer input = CharBuffer.wrap(text);
			CoderResult result = encoder.reset().encode(input, output, true);
			assertTrue(result.isMalformed());
			assertEquals(1, result.length());
			assertEquals(0, input.position());
			assertEquals(0, output.position());
		}

		CharBuffer input = CharBuffer.wrap("\uD83D\uDE00B");
		input.limit(1);
		assertTrue(encoder.reset().encode(input, output, false).isUnderflow());
		assertEquals(0, input.position());
		input.limit(3);
		CoderResult result = encoder.encode(input, output, true);
		assertTrue(result.isUnmappable());
		assertEquals(2, result.length());
		assertEquals(0, input.position());
		assertEquals(0, output.position());
		assertEquals("3F42", hex(encoder.reset().onUnmappableCharacter(CodingErrorAction.REPLACE).encode(input)));
		assertEquals("3F42", hex(encoder.reset().onMalformedInput(CodingErrorAction.REPLACE)
				.encode(CharBuffer.wrap("\uD800B"))));
	}

	@ParameterizedTest
	@ValueSource(strings = { "x-Windows-31J-RoundTrip", "x-eucJP-Open-RoundTrip" })
	public void testMalformedSurrogateBeforeExcludedCharacter(String name) throws Exception {
		CharsetEncoder encoder = Charset.forName(name).newEncoder();
		CharBuffer input = CharBuffer.wrap("A\uD800\u00A5B");
		ByteBuffer output = ByteBuffer.allocate(8);
		CoderResult result = encoder.encode(input, output, true);
		assertTrue(result.isMalformed());
		assertEquals(1, result.length());
		assertEquals(1, input.position());
		assertEquals(1, output.position());
		assertEquals(4, input.limit());
		input.rewind();
		assertEquals("413F3F42", hex(encoder.reset().onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE).encode(input)));
		input.rewind();
		assertEquals("4142", hex(encoder.reset().onMalformedInput(CodingErrorAction.IGNORE)
				.onUnmappableCharacter(CodingErrorAction.IGNORE).encode(input)));
	}

	@ParameterizedTest
	@ValueSource(strings = { "x-Windows-31J-RoundTrip", "x-eucJP-Open-RoundTrip" })
	public void testSmallOutputBuffersAroundExcludedCharacters(String name) {
		CharsetEncoder encoder = Charset.forName(name).newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPLACE);
		CharBuffer input = CharBuffer.wrap("AB\u00A5C\u203ED").asReadOnlyBuffer();
		ByteBuffer output = ByteBuffer.allocate(1);
		StringBuilder actual = new StringBuilder();
		CoderResult result;
		do {
			result = encoder.encode(input, output, true);
			assertFalse(result.isError());
			assertEquals(6, input.limit());
			output.flip();
			actual.append(hex(output));
			output.clear();
		} while (result.isOverflow());
		assertTrue(result.isUnderflow());
		assertTrue(encoder.flush(output).isUnderflow());
		assertEquals("41423F433F44", actual.toString());
	}

	@ParameterizedTest
	@CsvSource({
		"x-Windows-31J-RoundTrip, あ, 82A0",
		"x-eucJP-Open-RoundTrip, 丂, 8FB0A1"
	})
	public void testStreamingBuffers(String name, String text, String encoded) {
		Charset charset = Charset.forName(name);
		int length = encoded.length() / 2;
		for (boolean direct : new boolean[] { false, true }) {
			CharBuffer inputChars = direct ? ByteBuffer.allocateDirect(16).asCharBuffer() : CharBuffer.allocate(8);
			inputChars.position(1);
			inputChars.put("A" + text + "B").flip();
			inputChars.position(1);
			inputChars = inputChars.slice().asReadOnlyBuffer();
			ByteBuffer outputBytes = direct ? ByteBuffer.allocateDirect(16) : ByteBuffer.allocate(16);
			outputBytes.position(1);
			outputBytes.limit(2);
			CharsetEncoder encoder = charset.newEncoder();
			assertTrue(encoder.encode(inputChars, outputBytes, false).isOverflow());
			assertEquals(1, inputChars.position());
			assertEquals(2, outputBytes.position());
			outputBytes.limit(1 + length + 1);
			assertTrue(encoder.encode(inputChars, outputBytes, false).isOverflow());
			assertEquals(2, inputChars.position());
			assertEquals(2 + length, outputBytes.position());
			outputBytes.limit(16);
			assertTrue(encoder.encode(inputChars, outputBytes, true).isUnderflow());
			assertTrue(encoder.flush(outputBytes).isUnderflow());
			outputBytes.flip().position(1);
			assertEquals("41" + encoded + "42", hex(outputBytes));

			for (int split = 0; split < length; split++) {
				ByteBuffer inputBytes = direct ? ByteBuffer.allocateDirect(16) : ByteBuffer.allocate(16);
				inputBytes.position(1);
				inputBytes.put(bytes("41" + encoded + "42")).flip();
				inputBytes.position(1);
				inputBytes = inputBytes.slice().asReadOnlyBuffer();
				inputBytes.limit(1 + split);
				CharBuffer outputChars = direct ? ByteBuffer.allocateDirect(16).asCharBuffer() : CharBuffer.allocate(8);
				outputChars.position(1);
				CharsetDecoder decoder = charset.newDecoder();
				assertTrue(decoder.decode(inputBytes, outputChars, false).isUnderflow());
				assertEquals(1, inputBytes.position());
				assertEquals(2, outputChars.position());
				inputBytes.limit(2 + length);
				outputChars.limit(2);
				assertTrue(decoder.decode(inputBytes, outputChars, false).isOverflow());
				assertEquals(1, inputBytes.position());
				assertEquals(2, outputChars.position());
				outputChars.limit(8);
				assertTrue(decoder.decode(inputBytes, outputChars, true).isUnderflow());
				assertTrue(decoder.flush(outputChars).isUnderflow());
				outputChars.flip().position(1);
				assertEquals("A" + text + "B", outputChars.toString());
				inputBytes.position(1).limit(1 + split);
				CoderResult result = decoder.reset().decode(inputBytes, CharBuffer.allocate(8), true);
				assertEquals(split == 0, result.isUnderflow());
				if (split != 0) {
					assertTrue(result.isMalformed());
					assertEquals(split, result.length());
					assertEquals(1, inputBytes.position());
				}
			}
		}
	}

	@ParameterizedTest
	@CsvSource({
		"x-Windows-31J-RoundTrip, Windows-31J",
		"x-eucJP-Open-RoundTrip, x-eucJP-Open"
	})
	public void testAllBmpCharacters(String name, String baseName) {
		Charset charset = Charset.forName(name);
		Charset base = Charset.forName(baseName);
		CharsetEncoder baseEncoder = base.newEncoder();
		CharsetDecoder baseDecoder = base.newDecoder();
		CharsetEncoder encoder = charset.newEncoder();
		CharsetDecoder decoder = charset.newDecoder();
		CharBuffer input = CharBuffer.allocate(1);
		CharBuffer decoded = CharBuffer.allocate(2);
		ByteBuffer baseBytes = ByteBuffer.allocate(8);
		ByteBuffer actualBytes = ByteBuffer.allocate(8);
		int accepted = 0;
		for (int cp = 0; cp <= 0xFFFF; cp++) {
			input.clear();
			input.put((char) cp).flip();
			baseBytes.clear();
			boolean reversible = baseEncoder.reset().encode(input, baseBytes, true).isUnderflow();
			if (reversible) {
				baseBytes.flip();
				decoded.clear();
				reversible = baseDecoder.reset().decode(baseBytes, decoded, true).isUnderflow()
						&& decoded.position() == 1 && decoded.get(0) == cp;
			}
			input.rewind();
			actualBytes.clear();
			assertEquals(reversible, encoder.canEncode((char) cp), "U+" + Integer.toHexString(cp));
			CoderResult result = encoder.reset().encode(input, actualBytes, true);
			assertEquals(reversible, result.isUnderflow(), "U+" + Integer.toHexString(cp));
			if (reversible) {
				accepted++;
				assertTrue(encoder.flush(actualBytes).isUnderflow());
				baseBytes.rewind();
				actualBytes.flip();
				assertEquals(baseBytes, actualBytes);
				decoded.clear();
				assertTrue(decoder.reset().decode(actualBytes, decoded, true).isUnderflow());
				assertTrue(decoder.flush(decoded).isUnderflow());
				assertEquals(1, decoded.position());
				assertEquals(cp, decoded.get(0));
			} else {
				assertEquals(0, input.position());
				assertEquals(0, actualBytes.position());
				assertEquals(1, result.length());
				assertEquals(Character.isSurrogate((char) cp), result.isMalformed());
			}
		}
		assertTrue(accepted > 7000);
	}

	@ParameterizedTest
	@CsvSource({
		"x-Windows-31J-RoundTrip, Windows-31J, 2, 9397, 398",
		"x-eucJP-Open-RoundTrip, x-eucJP-Open, 3, 13303, 397"
	})
	public void testAllBinaryMappingsRoundTrip(String name, String baseName, int maxLength,
			int expectedAccepted, int expectedExcluded) throws Exception {
		Charset charset = Charset.forName(name);
		CharsetDecoder decoder = charset.newDecoder();
		CharsetEncoder encoder = charset.newEncoder();
		Charset base = Charset.forName(baseName);
		CharsetDecoder baseDecoder = base.newDecoder();
		CharsetEncoder baseEncoder = base.newEncoder();
		ByteBuffer input = ByteBuffer.allocate(3);
		ByteBuffer expectedBytes = ByteBuffer.allocate(16);
		ByteBuffer actualBytes = ByteBuffer.allocate(16);
		CharBuffer expectedChars = CharBuffer.allocate(3);
		CharBuffer actualChars = CharBuffer.allocate(3);
		ByteArrayOutputStream joinedBytes = new ByteArrayOutputStream();
		StringBuilder joinedChars = new StringBuilder();
		int accepted = 0;
		int excluded = 0;
		for (int length = 1; length <= maxLength; length++) {
			for (int code = 0; code <= (length == 1 ? 0xFF : 0xFFFF); code++) {
				input.clear();
				if (length == 3) {
					input.put((byte) 0x8F);
				}
				if (length >= 2) {
					input.put((byte) (code >> 8));
				}
				input.put((byte) code).flip();
				expectedChars.clear();
				CoderResult baseResult = baseDecoder.reset().decode(input, expectedChars, true);
				int basePosition = input.position();
				expectedChars.flip();
				boolean reversible = baseResult.isUnderflow();
				input.rewind();
				if (reversible) {
					expectedBytes.clear();
					reversible = baseEncoder.reset().encode(expectedChars, expectedBytes, true).isUnderflow();
					expectedChars.rewind();
					expectedBytes.flip();
					reversible &= input.equals(expectedBytes);
				}
				actualChars.clear();
				CoderResult result = decoder.reset().decode(input, actualChars, true);
				String context = "length=" + length + ", code=" + Integer.toHexString(code);
				assertEquals(reversible, result.isUnderflow(), context);
				actualChars.flip();
				if (reversible) {
					assertTrue(decoder.flush(CharBuffer.allocate(1)).isUnderflow(), context);
					assertEquals(expectedChars, actualChars, context);
					actualBytes.clear();
					assertTrue(encoder.reset().encode(actualChars, actualBytes, true).isUnderflow(), context);
					assertTrue(encoder.flush(actualBytes).isUnderflow(), context);
					actualBytes.flip();
					input.rewind();
					assertEquals(input, actualBytes, context);
					if (expectedChars.remaining() == 1) {
						accepted++;
						joinedBytes.write(input.array(), 0, length);
						joinedChars.append(expectedChars.get(0));
					}
				} else if (baseResult.isUnderflow()) {
					excluded++;
					assertTrue(result.isUnmappable(), context);
					assertEquals(length, result.length(), context);
					assertEquals(0, input.position(), context);
					assertEquals(0, actualChars.remaining(), context);
				} else {
					assertEquals(baseResult.toString(), result.toString(), context);
					assertEquals(basePosition, input.position(), context);
					assertEquals(expectedChars, actualChars, context);
				}
			}
		}
		assertEquals(expectedAccepted, accepted);
		assertEquals(expectedExcluded, excluded);
		byte[] allBytes = joinedBytes.toByteArray();
		for (boolean direct : new boolean[] { false, true }) {
			ByteBuffer allInput = direct ? ByteBuffer.allocateDirect(allBytes.length) : ByteBuffer.allocate(allBytes.length);
			allInput.put(allBytes).flip();
			CharBuffer allChars = decoder.reset().decode(allInput.asReadOnlyBuffer());
			assertEquals(joinedChars.toString(), allChars.toString());
			assertEquals(ByteBuffer.wrap(allBytes), encoder.reset().encode(allChars));
		}
	}

	private static byte[] bytes(String hex) {
		byte[] result = new byte[hex.length() / 2];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
		}
		return result;
	}
}
