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
import java.nio.charset.UnmappableCharacterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NecJipsMappingTest {
	@ParameterizedTest
	@ValueSource(strings = {
		"x-NEC-JIPSJ", "x-NEC-JIPSJ-HanyoDenshi", "x-NEC-JIPSJ-AdobeJapan1",
		"x-NEC-JIPSE", "x-NEC-JIPSE-HanyoDenshi", "x-NEC-JIPSE-AdobeJapan1"
	})
	public void testDistinctCharacters(String name) throws Exception {
		Charset charset = Charset.forName(name);
		ByteBuffer expected = name.startsWith("x-NEC-JIPSE")
				? ByteBuffer.wrap(bytes(0x4D4E, 0x4D7C, 0x5CC5, 0x5CC6, 0x5C6D))
				: ByteBuffer.wrap(bytes(0x282B, 0x2840, 0x2A45, 0x2A46, 0x2A5F));
		String text = "┼╂ﾅﾆﾟ";
		assertEquals(text, charset.newDecoder().decode(expected).toString());
		expected.rewind();
		assertEquals(expected, charset.newEncoder().encode(CharBuffer.wrap(text)));
	}

	@ParameterizedTest
	@ValueSource(strings = { "x-NEC-JIPSJ", "x-NEC-JIPSE" })
	public void testJisBoxDrawingBlock(String name) throws Exception {
		Charset charset = Charset.forName(name);
		String text = "─│┌┐┘└├┬┤┴┼━┃┏┓┛┗┣┳┫┻╋┠┯┨┷┿┝┰┥┸╂";
		ByteBuffer expected = nativeCodes(charset, block(0x2821, 32));
		assertEquals(text, charset.newDecoder().decode(expected).toString());
		expected.rewind();
		assertEquals(expected, charset.newEncoder().encode(CharBuffer.wrap(text)));
	}

	@ParameterizedTest
	@ValueSource(strings = { "x-NEC-JIPSJ", "x-NEC-JIPSE" })
	public void testAsciiBlockIsDecodeOnly(String name) throws Exception {
		Charset charset = Charset.forName(name);
		StringBuilder text = new StringBuilder();
		for (int cp = 0x21; cp <= 0x7E; cp++) {
			text.append((char) cp);
		}
		assertEquals(text.toString(), charset.newDecoder().decode(nativeCodes(charset, block(0x2921, 94))).toString());
		assertThrows(UnmappableCharacterException.class,
				() -> charset.newEncoder().encode(CharBuffer.wrap(text)));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"x-NEC-JIPSJ", "x-NEC-JIPSJ-HanyoDenshi", "x-NEC-JIPSJ-AdobeJapan1",
		"x-NEC-JIPSE", "x-NEC-JIPSE-HanyoDenshi", "x-NEC-JIPSE-AdobeJapan1"
	})
	public void testBracketsPreferStandardCodes(String name) throws Exception {
		Charset charset = Charset.forName(name);
		String text = "〔〕〈〉《》『』【】";
		ByteBuffer standard = nativeCodes(charset,
				bytes(0x214C, 0x214D, 0x2152, 0x2153, 0x2154, 0x2155, 0x2158, 0x2159, 0x215A, 0x215B));
		assertEquals(text, charset.newDecoder().decode(standard).toString());
		assertEquals(text, charset.newDecoder().decode(nativeCodes(charset, block(0x2B74, 10))).toString());
		standard.rewind();
		assertEquals(standard, charset.newEncoder().encode(CharBuffer.wrap(text)));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"x-NEC-JIPSJ", "x-NEC-JIPSJ-HanyoDenshi", "x-NEC-JIPSJ-AdobeJapan1",
		"x-NEC-JIPSE", "x-NEC-JIPSE-HanyoDenshi", "x-NEC-JIPSE-AdobeJapan1"
	})
	public void testAllReversibleCodesRoundTrip(String name) throws Exception {
		Charset charset = Charset.forName(name);
		Set<Integer> decodeOnly = decodeOnlyCodes(charset);
		CharsetDecoder decoder = charset.newDecoder();
		CharsetEncoder encoder = charset.newEncoder();
		ByteBuffer input = ByteBuffer.allocate(2);
		CharBuffer text = CharBuffer.allocate(8);
		ByteBuffer encoded = ByteBuffer.allocate(8);
		ByteArrayOutputStream joinedBytes = new ByteArrayOutputStream();
		StringBuilder joinedText = new StringBuilder();
		int accepted = 0;
		for (int code = 0; code <= 0xFFFF; code++) {
			if (decodeOnly.contains(code)) continue;
			input.clear();
			input.putShort((short) code).flip();
			text.clear();
			CoderResult result = decoder.reset().decode(input, text, true);
			if (result.isError()) continue;
			String context = name + ": " + Integer.toHexString(code);
			assertTrue(result.isUnderflow(), context);
			assertTrue(decoder.flush(text).isUnderflow(), context);
			text.flip();
			joinedText.append(text);
			encoded.clear();
			assertTrue(encoder.reset().encode(text, encoded, true).isUnderflow(), context);
			assertTrue(encoder.flush(encoded).isUnderflow(), context);
			input.rewind();
			encoded.flip();
			assertEquals(input, encoded, context);
			joinedBytes.write(code >>> 8);
			joinedBytes.write(code);
			accepted++;
		}
		assertTrue(accepted > 10000, name);
		ByteBuffer allBytes = ByteBuffer.wrap(joinedBytes.toByteArray());
		CharBuffer allText = decoder.decode(allBytes);
		assertEquals(joinedText.toString(), allText.toString(), name);
		allBytes.rewind();
		assertEquals(allBytes, encoder.encode(allText), name);
	}

	@ParameterizedTest
	@ValueSource(strings = { "x-NEC-JIPSJ", "x-NEC-JIPSE" })
	public void testHalfWidthKanaBlock(String name) throws Exception {
		Charset charset = Charset.forName(name);
		StringBuilder text = new StringBuilder();
		for (int cp = 0xFF61; cp <= 0xFF9F; cp++) {
			text.append((char) cp);
		}
		ByteBuffer expected = nativeCodes(charset, block(0x2A21, 63));
		assertEquals(text.toString(), charset.newDecoder().decode(expected).toString());
		expected.rewind();
		assertEquals(expected, charset.newEncoder().encode(CharBuffer.wrap(text)));
	}

	@ParameterizedTest
	@ValueSource(strings = { "x-NEC-JIPSJ", "x-NEC-JIPSE" })
	public void testKanaExtensionBounds(String name) throws Exception {
		Charset charset = Charset.forName(name);
		ByteBuffer input = nativeCodes(charset, bytes(0x2A60, 0x2A7E));
		assertEquals("ヰポ", charset.newDecoder().decode(input).toString());
	}

	@Test
	public void testG0CodesAreUnique() throws Exception {
		Set<String> codes = new HashSet<>();
		try (InputStream input = getClass().getResourceAsStream("/nec_jips_mapping.json")) {
			for (JsonNode node : new ObjectMapper().readTree(input)) {
				String code = node.get("code").asText();
				if (Integer.parseInt(code, 16) < 0x3000) {
					assertTrue(codes.add(code), "Duplicate G0 code: " + code);
				}
			}
		}
	}

	private static Set<Integer> decodeOnlyCodes(Charset charset) throws Exception {
		String variant = charset.name().endsWith("HanyoDenshi") ? "hd"
				: charset.name().endsWith("AdobeJapan1") ? "aj1" : null;
		Map<String, Boolean> baseRows = new HashMap<>();
		Map<String, Boolean> variantRows = new HashMap<>();
		try (InputStream input = NecJipsMappingTest.class.getResourceAsStream("/nec_jips_mapping.json")) {
			for (JsonNode node : new ObjectMapper().readTree(input)) {
				boolean decodeOnly = false;
				for (JsonNode option : node.path("options")) {
					decodeOnly |= "decode_only".equals(option.asText());
				}
				String code = node.get("code").asText();
				if (variant != null && node.has(variant)) {
					variantRows.merge(code, decodeOnly, (left, right) -> left && right);
				} else if (!node.has("hd") && !node.has("aj1")) {
					baseRows.merge(code, decodeOnly, (left, right) -> left && right);
				}
			}
		}
		// An IVS row can be reversible even when its base-character row is decode-only.
		baseRows.putAll(variantRows);
		int[] codes = baseRows.entrySet().stream().filter(Map.Entry::getValue)
				.mapToInt(entry -> Integer.parseInt(entry.getKey(), 16)).toArray();
		ByteBuffer nativeCodes = nativeCodes(charset, bytes(codes));
		Set<Integer> result = new HashSet<>();
		while (nativeCodes.hasRemaining()) result.add(nativeCodes.getShort() & 0xFFFF);
		return result;
	}

	private static byte[] block(int first, int count) {
		int[] codes = new int[count];
		for (int i = 0; i < count; i++) {
			codes[i] = first + i;
		}
		return bytes(codes);
	}

	private static byte[] bytes(int... codes) {
		byte[] result = new byte[codes.length * 2];
		for (int i = 0; i < codes.length; i++) {
			result[i * 2] = (byte) (codes[i] >>> 8);
			result[i * 2 + 1] = (byte) codes[i];
		}
		return result;
	}

	private static ByteBuffer nativeCodes(Charset charset, byte[] jipsJ) throws Exception {
		if (charset.name().startsWith("x-NEC-JIPSE")) {
			// JIPS(E) translates byte values, independently of the Unicode SBCS mapping.
			int[] translation = new int[256];
			Arrays.fill(translation, -1);
			try (InputStream input = NecJipsMappingTest.class.getResourceAsStream("/nec_jis8_ebcdik_mapping.json")) {
				for (JsonNode node : new ObjectMapper().readTree(input)) {
					translation[Integer.parseInt(node.get("jis8").asText(), 16)]
							= Integer.parseInt(node.get("ebcdik").asText(), 16);
				}
			}
			byte[] result = new byte[jipsJ.length];
			for (int i = 0; i < jipsJ.length; i++) {
				int code = translation[jipsJ[i] & 0xFF];
				assertTrue(code >= 0);
				result[i] = (byte) code;
			}
			return ByteBuffer.wrap(result);
		}
		return ByteBuffer.wrap(jipsJ);
	}
}
