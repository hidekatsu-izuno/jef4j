package net.arnx.jef4j;

import static net.arnx.jef4j.util.ByteUtils.hex;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Map;
import java.util.TreeMap;

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

	private static boolean hasOption(JsonNode node, String option) {
		for (JsonNode value : node.path("options")) {
			if (option.equals(value.asText())) return true;
		}
		return false;
	}
}
