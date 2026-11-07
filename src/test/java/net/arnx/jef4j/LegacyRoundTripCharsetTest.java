package net.arnx.jef4j;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LegacyRoundTripCharsetTest {
	@ParameterizedTest
	@CsvSource({
		"x-Hitachi-KEIS78-Roundtrip, x-Hitachi-KEIS78, hitachi_keis78_mapping.json, 9878, 16",
		"x-Hitachi-KEIS83-Roundtrip, x-Hitachi-KEIS83, hitachi_keis83_mapping.json, 9887, 3",
		"x-NEC-JIPSJ-Roundtrip, x-NEC-JIPSJ, nec_jips_mapping.json, 10947, 296",
		"x-NEC-JIPSE-Roundtrip, x-NEC-JIPSE, nec_jips_mapping.json, 10947, 296"
	})
	public void testAllBinaryCodes(String name, String baseName, String mapping, int expectedAccepted, int expectedExcluded)
			throws Exception {
		Charset charset = Charset.forName(name);
		Set<Integer> oneWay = oneWayCodes(name, mapping);
		CharsetDecoder decoder = charset.newDecoder();
		CharsetEncoder encoder = charset.newEncoder();
		Charset base = Charset.forName(baseName);
		CharsetDecoder baseDecoder = base.newDecoder();
		CharsetEncoder baseEncoder = base.newEncoder();
		ByteBuffer input = ByteBuffer.allocate(2);
		CharBuffer expectedText = CharBuffer.allocate(8);
		CharBuffer actualText = CharBuffer.allocate(8);
		ByteBuffer encoded = ByteBuffer.allocate(8);
		ByteArrayOutputStream joinedBytes = new ByteArrayOutputStream();
		StringBuilder joinedText = new StringBuilder();
		int accepted = 0;
		int excluded = 0;
		for (int code = 0; code <= 0xFFFF; code++) {
			input.clear();
			input.putShort((short) code).flip();
			expectedText.clear();
			CoderResult baseResult = baseDecoder.reset().decode(input, expectedText, true);
			boolean baseAccepted = baseResult.isUnderflow();
			if (baseAccepted) assertTrue(baseDecoder.flush(expectedText).isUnderflow());
			expectedText.flip();
			input.rewind();
			boolean reversible = false;
			if (baseAccepted) {
				encoded.clear();
				reversible = baseEncoder.reset().encode(expectedText, encoded, true).isUnderflow()
						&& baseEncoder.flush(encoded).isUnderflow();
				encoded.flip();
				reversible &= input.equals(encoded);
				reversible &= !oneWay.contains(code);
				expectedText.rewind();
			}
			actualText.clear();
			CoderResult result = decoder.reset().decode(input, actualText, true);
			String context = name + ": " + Integer.toHexString(code);
			assertEquals(reversible, result.isUnderflow(), context);
			if (reversible) {
				assertTrue(decoder.flush(actualText).isUnderflow(), context);
				actualText.flip();
				assertEquals(expectedText, actualText, context);
				assertFalse(actualText.toString().codePoints().anyMatch(cp -> cp >= 0xE0100 && cp <= 0xE01EF), context);
				joinedText.append(actualText);
				encoded.clear();
				assertTrue(encoder.reset().encode(actualText, encoded, true).isUnderflow(), context);
				assertTrue(encoder.flush(encoded).isUnderflow(), context);
				encoded.flip();
				input.rewind();
				assertEquals(input, encoded, context);
				joinedBytes.write(code >>> 8);
				joinedBytes.write(code);
				accepted++;
			} else if (baseAccepted) {
				assertTrue(result.isUnmappable(), context);
				assertEquals(2, result.length(), context);
				assertEquals(0, input.position(), context);
				assertEquals(0, actualText.position(), context);
				excluded++;
			} else {
				assertTrue(result.isError(), context);
			}
		}
		assertEquals(expectedAccepted, accepted, name);
		assertEquals(expectedExcluded, excluded, name);
		byte[] allBytes = joinedBytes.toByteArray();
		for (boolean direct : new boolean[] { false, true }) {
			ByteBuffer allInput = direct ? ByteBuffer.allocateDirect(allBytes.length) : ByteBuffer.allocate(allBytes.length);
			allInput.put(allBytes).flip();
			CharBuffer allText = decoder.decode(allInput.asReadOnlyBuffer());
			assertEquals(joinedText.toString(), allText.toString(), name);
			assertEquals(ByteBuffer.wrap(allBytes), encoder.encode(allText), name);
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"x-Hitachi-KEIS78-Roundtrip", "x-Hitachi-KEIS83-Roundtrip",
		"x-NEC-JIPSJ-Roundtrip", "x-NEC-JIPSE-Roundtrip"
	})
	public void testAllEncodableScalars(String name) throws Exception {
		Charset charset = Charset.forName(name);
		CharsetEncoder encoder = charset.newEncoder();
		CharsetDecoder decoder = charset.newDecoder();
		CharBuffer input = CharBuffer.allocate(2);
		ByteBuffer encoded = ByteBuffer.allocate(8);
		CharBuffer decoded = CharBuffer.allocate(8);
		int accepted = 0;
		for (int cp = 0; cp <= Character.MAX_CODE_POINT; cp++) {
			if (cp >= Character.MIN_SURROGATE && cp <= Character.MAX_SURROGATE) continue;
			input.clear();
			if (Character.isSupplementaryCodePoint(cp)) {
				input.put(Character.highSurrogate(cp)).put(Character.lowSurrogate(cp));
			} else {
				input.put((char) cp);
			}
			input.flip();
			encoded.clear();
			CoderResult result = encoder.reset().encode(input, encoded, true);
			if (result.isError()) continue;
			String context = name + ": U+" + Integer.toHexString(cp);
			assertTrue(result.isUnderflow(), context);
			assertTrue(encoder.flush(encoded).isUnderflow(), context);
			encoded.flip();
			decoded.clear();
			assertTrue(decoder.reset().decode(encoded, decoded, true).isUnderflow(), context);
			assertTrue(decoder.flush(decoded).isUnderflow(), context);
			input.rewind();
			decoded.flip();
			assertEquals(input, decoded, context);
			accepted++;
		}
		assertTrue(accepted > 9000, name);
	}

	@ParameterizedTest
	@CsvSource({
		"x-Hitachi-KEIS78-Roundtrip, 4040, A1A1",
		"x-Hitachi-KEIS78-Roundtrip, 4040, AFBF",
		"x-Hitachi-KEIS83-Roundtrip, 4040, 73FA",
		"x-NEC-JIPSJ-Roundtrip, 2121, 2921",
		"x-NEC-JIPSJ-Roundtrip, 2121, 2B74",
		"x-NEC-JIPSJ-Roundtrip, 2121, 303B",
		"x-NEC-JIPSJ-Roundtrip, 2121, D4B5",
		"x-NEC-JIPSE-Roundtrip, 4F4F, 5D4F",
		"x-NEC-JIPSE-Roundtrip, 4F4F, 4EA3"
	})
	public void testExcludedCodeRecovery(String name, String valid, String excluded) throws Exception {
		Charset charset = Charset.forName(name);
		ByteBuffer input = ByteBuffer.wrap(bytes(valid + excluded + valid));
		CharBuffer output = CharBuffer.allocate(8);
		CoderResult result = charset.newDecoder().decode(input, output, true);
		assertTrue(result.isUnmappable());
		assertEquals(2, result.length());
		assertEquals(2, input.position());
		assertEquals(1, output.position());
		input.rewind();
		assertEquals("\u3000\uFFFD\u3000", charset.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPLACE).decode(input).toString());
		input.rewind();
		assertEquals("\u3000\u3000", charset.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.IGNORE).decode(input).toString());

		CharsetDecoder decoder = charset.newDecoder();
		input = ByteBuffer.wrap(bytes(excluded));
		input.limit(1);
		output.clear();
		assertTrue(decoder.decode(input, output, false).isUnderflow());
		assertEquals(0, input.position());
		input.limit(2);
		assertTrue(decoder.decode(input, output, true).isUnmappable());
		assertEquals(0, input.position());
		decoder.reset();
		input.limit(1);
		assertTrue(decoder.decode(input, output, true).isMalformed());
	}

	private static Set<Integer> oneWayCodes(String name, String mapping) throws Exception {
		int[] translation = new int[256];
		for (int b = 0; b < translation.length; b++) translation[b] = b;
		if (name.startsWith("x-NEC-JIPSE")) {
			try (InputStream input = LegacyRoundTripCharsetTest.class.getResourceAsStream("/nec_jis8_ebcdik_mapping.json")) {
				for (JsonNode node : new ObjectMapper().readTree(input)) {
					translation[Integer.parseInt(node.get("jis8").asText(), 16)]
							= Integer.parseInt(node.get("ebcdik").asText(), 16);
				}
			}
		}
		Set<Integer> result = new HashSet<>();
		try (InputStream input = LegacyRoundTripCharsetTest.class.getResourceAsStream("/" + mapping)) {
			for (JsonNode node : new ObjectMapper().readTree(input)) {
				if (node.has("hd") || node.has("aj1")) continue;
				for (JsonNode option : node.path("options")) {
					if ("oneway".equals(option.asText())) {
						int code = Integer.parseInt(node.get("code").asText(), 16);
						result.add((translation[code >>> 8] << 8) | translation[code & 0xFF]);
					}
				}
			}
		}
		return result;
	}

	private static byte[] bytes(String hex) {
		byte[] result = new byte[hex.length() / 2];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
		}
		return result;
	}
}
