import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;
import java.util.Locale;

/**
 * Simple steady-state comparison with the JDK charsets; not a JMH benchmark.
 * Compile the project first, then run with Java 11 or later:
 *
 * java -Xms256m -Xmx256m -cp target/classes scripts/RoundTripCharsetBenchmark.java
 * java -Xms256m -Xmx256m -cp target/classes scripts/RoundTripCharsetBenchmark.java strings
 *
 * The default reuses coder/input/output buffers; "strings" measures String APIs,
 * including their allocations. Results exclude charset initialization, use
 * 32,768-character inputs, and report the median of seven samples after warmup.
 */
public class RoundTripCharsetBenchmark {
	private static final int CHARS = 32768;
	private static final int ROUNDS = 80;
	private static final int SAMPLES = 7;
	private static volatile long checksum;

	public static void main(String[] args) {
		if (args.length > 1 || (args.length == 1 && !"strings".equals(args[0]))) {
			throw new IllegalArgumentException("Usage: RoundTripCharsetBenchmark [strings]");
		}
		boolean strings = args.length == 1;
		System.out.printf("Java %s, %s, %,d characters, %d rounds, %d samples%n",
				System.getProperty("java.version"), strings ? "String APIs" : "reused buffers", CHARS, ROUNDS, SAMPLES);
		for (String base : new String[] { "Windows-31J", "x-eucJP-Open" }) {
			for (boolean ascii : new boolean[] { true, false }) {
				String unit = ascii ? "ABC xyz 0123456789 /\\~[]\r\n"
						: "日本語の文字コード変換あいうえおアイウエオｱｲｳｴｵ纊";
				StringBuilder builder = new StringBuilder(CHARS + unit.length());
				while (builder.length() < CHARS) {
					builder.append(unit);
				}
				builder.setLength(CHARS);
				String text = builder.toString();
				String roundTrip = base.equals("Windows-31J") ? "x-Windows-31J-RoundTrip" : "x-eucJP-Open-RoundTrip";
				for (String name : new String[] { base, roundTrip }) {
					Charset charset = Charset.forName(name);
					byte[] bytes = text.getBytes(charset);
					if (!text.equals(new String(bytes, charset))) {
						throw new AssertionError("Input does not round-trip: " + name);
					}
					Runnable encode;
					Runnable decode;
					if (strings) {
						encode = () -> {
							byte[] result = text.getBytes(charset);
							checksum += result.length + result[result.length / 2];
						};
						decode = () -> {
							String result = new String(bytes, charset);
							checksum += result.length() + result.charAt(result.length() / 2);
						};
					} else {
						CharsetEncoder encoder = charset.newEncoder();
						CharsetDecoder decoder = charset.newDecoder();
						CharBuffer inputChars = CharBuffer.wrap(text.toCharArray());
						ByteBuffer outputBytes = ByteBuffer.allocate(CHARS * 3);
						ByteBuffer inputBytes = ByteBuffer.wrap(bytes);
						CharBuffer outputChars = CharBuffer.allocate(CHARS);
						encode = () -> {
							inputChars.rewind();
							outputBytes.clear();
							CoderResult result = encoder.reset().encode(inputChars, outputBytes, true);
							if (!result.isUnderflow() || !encoder.flush(outputBytes).isUnderflow()) {
								throw new AssertionError(result);
							}
							checksum += outputBytes.position() + outputBytes.get(outputBytes.position() / 2);
						};
						decode = () -> {
							inputBytes.rewind();
							outputChars.clear();
							CoderResult result = decoder.reset().decode(inputBytes, outputChars, true);
							if (!result.isUnderflow() || !decoder.flush(outputChars).isUnderflow()) {
								throw new AssertionError(result);
							}
							checksum += outputChars.position() + outputChars.get(outputChars.position() / 2);
						};
					}
					for (int i = 0; i < 160; i++) {
						encode.run();
						decode.run();
					}
					double encodeRate = measure(encode);
					double decodeRate = measure(decode);
					System.out.printf(Locale.ROOT, "%s %-28s encode=%8.1f Mchar/s decode=%8.1f Mchar/s%n",
							ascii ? "ASCII" : "日本語", name, encodeRate, decodeRate);
				}
			}
		}
		System.out.println("checksum=" + checksum);
	}

	private static double measure(Runnable task) {
		long[] samples = new long[SAMPLES];
		for (int sample = 0; sample < SAMPLES; sample++) {
			long start = System.nanoTime();
			for (int round = 0; round < ROUNDS; round++) {
				task.run();
			}
			samples[sample] = System.nanoTime() - start;
		}
		Arrays.sort(samples);
		return (double) CHARS * ROUNDS * 1000 / samples[SAMPLES / 2];
	}
}
