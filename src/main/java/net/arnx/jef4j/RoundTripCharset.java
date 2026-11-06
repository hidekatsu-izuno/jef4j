/*
 * Copyright 2026 Hidekatsu Izuno <hidekatsu.izuno@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.arnx.jef4j;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.function.IntUnaryOperator;

/**
 * Delegates to a base charset while rejecting explicitly supplied Unicode
 * characters and byte sequences that do not round-trip unchanged.
 */
public class RoundTripCharset extends Charset {
	private final Charset baseCharset;
	private final BitSet unmappable = new BitSet();
	private final BitSet unmappableByteStarts = new BitSet(256);
	private final IntUnaryOperator byteLength;
	private final int[] unmappableByteRanges;

	protected RoundTripCharset(CharsetType type, Charset baseCharset, char[] unmappableCharacters) {
		this(type, baseCharset, unmappableCharacters, new int[0], b -> 1);
	}

	/**
	 * @param unmappableCharacters Unicode characters that cannot round-trip
	 * @param unmappableByteRanges sorted, inclusive start/end pairs of byte codes
	 * @param byteLength encoded character length for an unsigned leading byte
	 */
	protected RoundTripCharset(CharsetType type, Charset baseCharset, char[] unmappableCharacters,
			int[] unmappableByteRanges, IntUnaryOperator byteLength) {
		super(type.getCharsetName(), type.getAliases());
		this.baseCharset = baseCharset;
		this.byteLength = byteLength;
		this.unmappableByteRanges = unmappableByteRanges.clone();
		for (char c : unmappableCharacters) {
			unmappable.set(c);
		}
		if ((unmappableByteRanges.length & 1) != 0) {
			throw new IllegalArgumentException("Byte ranges must contain start/end pairs");
		}
		for (int i = 0; i < unmappableByteRanges.length; i += 2) {
			int start = unmappableByteRanges[i];
			int end = unmappableByteRanges[i + 1];
			if (start < 0 || start > end || end > 0xFFFFFF
					|| (i > 0 && start <= unmappableByteRanges[i - 1])) {
				throw new IllegalArgumentException("Byte ranges must be sorted and disjoint");
			}
			unmappableByteStarts.set(firstByte(start), firstByte(end) + 1);
		}
	}

	private static int firstByte(int code) {
		return code <= 0xFF ? code : code <= 0xFFFF ? code >>> 8 : code >>> 16;
	}

	private boolean isUnmappableByteCode(int code) {
		int low = 0;
		int high = unmappableByteRanges.length / 2 - 1;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			if (code < unmappableByteRanges[mid * 2]) {
				high = mid - 1;
			} else if (code > unmappableByteRanges[mid * 2 + 1]) {
				low = mid + 1;
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean contains(Charset cs) {
		int first = unmappable.nextSetBit(0);
		int firstByte = unmappableByteStarts.nextSetBit(0);
		return equals(cs) || (StandardCharsets.US_ASCII.equals(cs)
				&& (first < 0 || first >= 0x80) && (firstByte < 0 || firstByte >= 0x80));
	}

	@Override
	public CharsetDecoder newDecoder() {
		return new Decoder(this, baseCharset.newDecoder());
	}

	@Override
	public CharsetEncoder newEncoder() {
		return new Encoder(this, baseCharset.newEncoder(), unmappable);
	}

	private static final class Decoder extends CharsetDecoder {
		private static final ByteBuffer EMPTY = ByteBuffer.allocate(0).asReadOnlyBuffer();
		private final RoundTripCharset charset;
		private final CharsetDecoder decoder;

		Decoder(RoundTripCharset cs, CharsetDecoder decoder) {
			super(cs, decoder.averageCharsPerByte(), decoder.maxCharsPerByte());
			this.charset = cs;
			this.decoder = decoder;
		}

		@Override
		protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
			int limit = in.limit();
			int end = in.position();
			int excludedLength = 0;
			// Each encoded character in the base charsets produces one BMP character.
			for (long scanned = 0; end < limit && scanned <= out.remaining(); scanned++) {
				int first = in.get(end) & 0xFF;
				int length = charset.byteLength.applyAsInt(first);
				if (limit - end < length) {
					break;
				}
				if (charset.unmappableByteStarts.get(first)) {
					int code = first;
					for (int i = 1; i < length; i++) {
						code = (code << 8) | (in.get(end + i) & 0xFF);
					}
					if (charset.isUnmappableByteCode(code)) {
						excludedLength = length;
						break;
					}
				}
				end += length;
			}
			if (excludedLength == 0) {
				return decoder.decode(in, out, false);
			}
			if (end == in.position()) {
				return CoderResult.unmappableForLength(excludedLength);
			}

			CoderResult result;
			try {
				in.limit(end);
				result = decoder.decode(in, out, false);
			} finally {
				in.limit(limit);
			}
			if (result.isUnderflow()) {
				// An invalid leading byte before the excluded code may need lookahead.
				return in.position() < end ? decoder.decode(in, out, false)
						: CoderResult.unmappableForLength(excludedLength);
			}
			return result;
		}

		@Override
		protected CoderResult implFlush(CharBuffer out) {
			CoderResult result = decoder.decode(EMPTY, out, true);
			return result.isUnderflow() ? decoder.flush(out) : result;
		}

		@Override
		protected void implReset() {
			decoder.reset();
		}
	}

	private static final class Encoder extends CharsetEncoder {
		private static final CharBuffer EMPTY = CharBuffer.allocate(0).asReadOnlyBuffer();
		private final CharsetEncoder encoder;
		private final BitSet unmappable;

		Encoder(Charset cs, CharsetEncoder encoder, BitSet unmappable) {
			super(cs, encoder.averageBytesPerChar(), encoder.maxBytesPerChar(), encoder.replacement());
			this.encoder = encoder;
			this.unmappable = unmappable;
		}

		@Override
		public boolean canEncode(char c) {
			return !unmappable.get(c) && encoder.canEncode(c);
		}

		@Override
		protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
			int limit = in.limit();
			int end = in.position();
			// The base charsets emit at least one byte per UTF-16 code unit.
			// Bound the scan so repeated OVERFLOW does not rescan the whole input.
			int scanLimit = (int) Math.min(limit, (long) end + out.remaining() + 1);
			while (end < scanLimit && !unmappable.get(in.get(end))) {
				end++;
			}
			if (end == scanLimit) {
				return encoder.encode(in, out, false);
			}
			if (end == in.position()) {
				return CoderResult.unmappableForLength(1);
			}

			CoderResult result;
			try {
				in.limit(end);
				result = encoder.encode(in, out, false);
			} finally {
				in.limit(limit);
			}
			if (result.isUnderflow() && end < limit) {
				if (in.position() < end) {
					// Let the base encoder see the character after a trailing high surrogate.
					return encoder.encode(in, out, false);
				}
				return CoderResult.unmappableForLength(1);
			}
			return result;
		}

		@Override
		protected CoderResult implFlush(ByteBuffer out) {
			CoderResult result = encoder.encode(EMPTY, out, true);
			return result.isUnderflow() ? encoder.flush(out) : result;
		}

		@Override
		protected void implReset() {
			encoder.reset();
		}
	}
}
