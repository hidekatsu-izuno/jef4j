package net.arnx.jef4j;

import static net.arnx.jef4j.util.ByteUtils.hex;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IbmCharsetEncoderTest {
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testIbm8482Encoder() throws IOException {
		assertEncoderMapping("x-IBM-8482", "/ibm_8482_mapping.json");
	}

	@Test
	public void testIbm5123Encoder() throws IOException {
		assertEncoderMapping("x-IBM-5123", "/ibm_5123_mapping.json");
	}

	@Test
	public void testIbm11684Encoder() throws IOException {
		assertEncoderMapping("x-IBM-11684", "/ibm_11684_mapping.json");
		assertEquals("80A1A0FE", hex("\uE000\uEC1D".getBytes(Charset.forName("x-IBM-11684"))));
	}

	@Test
	public void testIbm1390Encoder() {
		assertEquals("C10E40400FC2", hex("A　B".getBytes(Charset.forName("x-IBM-1390"))));
		assertEquals("C10E80A10FC2", hex("A\uE000B".getBytes(Charset.forName("x-IBM-1390"))));
	}

	@Test
	public void testIbm1399Encoder() {
		assertEquals("C10E40400FC2", hex("A　B".getBytes(Charset.forName("x-IBM-1399"))));
		assertEquals("C10E80A10FC2", hex("A\uE000B".getBytes(Charset.forName("x-IBM-1399"))));
	}

	@Test
	public void testIbm8482Roundtrip() throws IOException {
		assertRoundtrip("x-IBM-8482", "/ibm_8482_mapping.json");
	}

	@Test
	public void testIbm5123Roundtrip() throws IOException {
		assertRoundtrip("x-IBM-5123", "/ibm_5123_mapping.json");
	}

	@Test
	public void testIbm11684Roundtrip() throws IOException {
		assertRoundtrip("x-IBM-11684", "/ibm_11684_mapping.json");
	}

	@Test
	public void testIbm1390Roundtrip() throws IOException {
		assertRoundtrip("x-IBM-1390", "/ibm_8482_mapping.json", "/ibm_11684_mapping.json");
	}

	@Test
	public void testIbm1399Roundtrip() throws IOException {
		assertRoundtrip("x-IBM-1399", "/ibm_5123_mapping.json", "/ibm_11684_mapping.json");
	}

	private void assertEncoderMapping(String charsetName, String resource) throws IOException {
		Map<String, String> expected = new TreeMap<>();
		try (InputStream input = getClass().getResourceAsStream(resource)) {
			for (JsonNode node : mapper.readTree(input)) {
				if (!hasOption(node, "decode_only")) {
					expected.put(node.get("unicode").asText(), node.get("code").asText());
				}
			}
		}

		CharsetEncoder encoder = Charset.forName(charsetName).newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		for (Map.Entry<String, String> entry : expected.entrySet()) {
			int codePoint = Integer.parseInt(entry.getKey(), 16);
			CharBuffer chars = CharBuffer.wrap(Character.toChars(codePoint));
			try {
				ByteBuffer bytes = encoder.encode(chars);
				assertEquals(entry.getValue(), hex(bytes), entry.getKey());
			} catch (CharacterCodingException e) {
				throw new AssertionError(entry.getKey(), e);
			}
			encoder.reset();
		}
	}

	private void assertRoundtrip(String charsetName, String... resources) throws IOException {
		Set<String> inputs = new TreeSet<>();
		for (String resource : resources) {
			try (InputStream input = getClass().getResourceAsStream(resource)) {
				for (JsonNode node : mapper.readTree(input)) {
					String unicode = node.get("unicode").asText();
					if (!hasOption(node, "decode_only")
							&& !(resources.length > 1 && ("000E".equals(unicode) || "000F".equals(unicode)))) {
						inputs.add(unicode);
					}
				}
			}
		}

		Charset charset = Charset.forName(charsetName);
		CharsetEncoder encoder = charset.newEncoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		CharsetDecoder decoder = charset.newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		for (String unicode : inputs) {
			int codePoint = Integer.parseInt(unicode, 16);
			CharBuffer input = CharBuffer.wrap(Character.toChars(codePoint));
			byte[] encoded;
			try {
				encoded = bytes(encoder.encode(input));
			} catch (CharacterCodingException e) {
				// Stateful charsets do not necessarily expose every mapping from their component charsets.
				continue;
			}
			try {
				CharBuffer decoded = decoder.decode(ByteBuffer.wrap(encoded));
				byte[] reencoded = bytes(encoder.encode(decoded));
				assertArrayEquals(encoded, reencoded, charsetName + ": " + unicode);
			} catch (CharacterCodingException e) {
				throw new AssertionError(charsetName + ": " + unicode, e);
			}
		}
	}

	private static byte[] bytes(ByteBuffer buffer) {
		byte[] result = new byte[buffer.remaining()];
		buffer.get(result);
		return result;
	}

	private static boolean hasOption(JsonNode node, String option) {
		for (JsonNode value : node.path("options")) {
			if (option.equals(value.asText())) return true;
		}
		return false;
	}
}
