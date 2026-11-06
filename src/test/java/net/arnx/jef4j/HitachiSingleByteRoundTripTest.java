package net.arnx.jef4j;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class HitachiSingleByteRoundTripTest {
	@Test
	public void testExplicitMappings() throws Exception {
		Charset charset = Charset.forName("x-Hitachi-EBCDIC");
		byte[] bytes = { 0x5B, 0x7F, (byte) 0x8E, (byte) 0x8F };
		String text = "\\\"ﾈﾉ";
		assertEquals(text, charset.newDecoder().decode(ByteBuffer.wrap(bytes)).toString());
		assertEquals(ByteBuffer.wrap(bytes), charset.newEncoder().encode(CharBuffer.wrap(text)));
	}

	@ParameterizedTest
	@ValueSource(strings = { "x-Hitachi-EBCDIC", "x-Hitachi-EBCDIK" })
	public void testAllBytesRoundTrip(String name) throws Exception {
		Charset charset = Charset.forName(name);
		CharsetDecoder decoder = charset.newDecoder();
		CharsetEncoder encoder = charset.newEncoder();
		ByteBuffer input = ByteBuffer.allocate(1);
		CharBuffer chars = CharBuffer.allocate(1);
		ByteBuffer encoded = ByteBuffer.allocate(1);
		ByteArrayOutputStream joinedBytes = new ByteArrayOutputStream();
		StringBuilder joinedChars = new StringBuilder();
		int accepted = 0;
		for (int b = 0; b <= 0xFF; b++) {
			input.clear();
			input.put((byte) b).flip();
			chars.clear();
			CoderResult result = decoder.reset().decode(input, chars, true);
			if (result.isError()) {
				continue;
			}
			String context = name + ": " + Integer.toHexString(b);
			assertTrue(result.isUnderflow(), context);
			assertTrue(decoder.flush(chars).isUnderflow(), context);
			assertEquals(1, chars.position(), context);
			chars.flip();
			joinedChars.append(chars.get(0));
			encoded.clear();
			assertTrue(encoder.reset().encode(chars, encoded, true).isUnderflow(), context);
			assertTrue(encoder.flush(encoded).isUnderflow(), context);
			assertEquals(1, encoded.position(), context);
			assertEquals(b, encoded.get(0) & 0xFF, context);
			joinedBytes.write(b);
			accepted++;
		}
		assertEquals(223, accepted);
		byte[] allBytes = joinedBytes.toByteArray();
		CharBuffer allChars = decoder.decode(ByteBuffer.wrap(allBytes));
		assertEquals(joinedChars.toString(), allChars.toString());
		assertEquals(ByteBuffer.wrap(allBytes), encoder.encode(allChars));
	}

	@ParameterizedTest
	@ValueSource(strings = { "x-Hitachi-EBCDIC", "x-Hitachi-EBCDIK" })
	public void testAllEncodableCharactersRoundTrip(String name) {
		Charset charset = Charset.forName(name);
		CharsetEncoder encoder = charset.newEncoder();
		CharsetDecoder decoder = charset.newDecoder();
		CharBuffer input = CharBuffer.allocate(1);
		ByteBuffer encoded = ByteBuffer.allocate(1);
		CharBuffer decoded = CharBuffer.allocate(1);
		int accepted = 0;
		for (int cp = 0; cp <= 0xFFFF; cp++) {
			input.clear();
			input.put((char) cp).flip();
			encoded.clear();
			CoderResult result = encoder.reset().encode(input, encoded, true);
			if (result.isError()) {
				continue;
			}
			String context = name + ": U+" + Integer.toHexString(cp);
			assertTrue(result.isUnderflow(), context);
			assertTrue(encoder.flush(encoded).isUnderflow(), context);
			assertEquals(1, encoded.position(), context);
			encoded.flip();
			decoded.clear();
			assertTrue(decoder.reset().decode(encoded, decoded, true).isUnderflow(), context);
			assertTrue(decoder.flush(decoded).isUnderflow(), context);
			assertEquals(1, decoded.position(), context);
			assertEquals(cp, decoded.get(0), context);
			accepted++;
		}
		assertEquals(223, accepted);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"x-Hitachi-EBCDIC+KEIS78", "x-Hitachi-EBCDIC+KEIS83",
		"x-Hitachi-KEIS78+EBCDIC", "x-Hitachi-KEIS83+EBCDIC"
	})
	public void testCorrectedCharactersInMixedCharsets(String name) throws Exception {
		Charset charset = Charset.forName(name);
		String text = "\\\"ﾈﾉあ";
		ByteBuffer encoded = charset.newEncoder().encode(CharBuffer.wrap(text));
		assertEquals(text, charset.newDecoder().decode(encoded).toString());
	}
}
