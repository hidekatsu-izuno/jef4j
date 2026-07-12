package net.arnx.jef4j;

import static net.arnx.jef4j.util.ByteUtils.hex;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IbmCharsetDecoderTest {
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testIbm8482Decoder() throws IOException {
		assertDecoderMapping("x-IBM-8482", "/ibm_8482_mapping.json");
	}

	@Test
	public void testIbm5123Decoder() throws IOException {
		assertDecoderMapping("x-IBM-5123", "/ibm_5123_mapping.json");
	}

	@Test
	public void testIbm11684Decoder() throws IOException {
		assertDecoderMapping("x-IBM-11684", "/ibm_11684_mapping.json");
		assertEquals("\uE000\uEC1D", new String(new byte[] {
				(byte) 0x80, (byte) 0xA1, (byte) 0xA0, (byte) 0xFE
		}, Charset.forName("x-IBM-11684")));
	}

	@Test
	public void testIbm1390Decoder() {
		assertEquals("A　B", new String(bytes("C10E40400FC2"), Charset.forName("x-IBM-1390")));
		assertEquals("A\uE000B", new String(bytes("C10E80A10FC2"), Charset.forName("x-IBM-1390")));
	}

	@Test
	public void testIbm1399Decoder() {
		assertEquals("A　B", new String(bytes("C10E40400FC2"), Charset.forName("x-IBM-1399")));
		assertEquals("A\uE000B", new String(bytes("C10E80A10FC2"), Charset.forName("x-IBM-1399")));
	}

	private void assertDecoderMapping(String charsetName, String resource) throws IOException {
		Map<String, String> expected = new TreeMap<>();
		try (InputStream input = getClass().getResourceAsStream(resource)) {
			for (JsonNode node : mapper.readTree(input)) {
				if (!hasOption(node, "encode_only")) {
					expected.put(node.get("code").asText(), unicodeHex(node.get("unicode").asText()));
				}
			}
		}

		CharsetDecoder decoder = Charset.forName(charsetName).newDecoder()
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.onMalformedInput(CodingErrorAction.REPORT);
		for (Map.Entry<String, String> entry : expected.entrySet()) {
			try {
				CharBuffer chars = decoder.decode(ByteBuffer.wrap(bytes(entry.getKey())));
				assertEquals(entry.getValue(), hex(chars), entry.getKey());
			} catch (CharacterCodingException e) {
				throw new AssertionError(entry.getKey(), e);
			}
			decoder.reset();
		}
	}

	private static boolean hasOption(JsonNode node, String option) {
		for (JsonNode value : node.path("options")) {
			if (option.equals(value.asText())) return true;
		}
		return false;
	}

	private static byte[] bytes(String value) {
		byte[] bytes = new byte[value.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(value.substring(i * 2, i * 2 + 2), 16);
		}
		return bytes;
	}

	private static String unicodeHex(String value) {
		return hex(CharBuffer.wrap(Character.toChars(Integer.parseInt(value, 16))));
	}
}
