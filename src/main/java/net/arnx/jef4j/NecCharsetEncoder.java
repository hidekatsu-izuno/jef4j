package net.arnx.jef4j;

import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import net.arnx.jef4j.util.LongObjMap;

@SuppressWarnings("unchecked")
public class NecCharsetEncoder extends CharsetEncoder {
	private static final byte[] EBCDIK_MAP;
	private static final LongObjMap<Record[]> KANJI_MAP;
	private static final ByteBuffer DUMMY = ByteBuffer.allocate(0);
	
	static {
		try (ObjectInputStream in = new ObjectInputStream(
				NecCharsetEncoder.class.getResourceAsStream("NecEncodeMap.dat"))) {
			EBCDIK_MAP = (byte[])in.readObject();
			KANJI_MAP = (LongObjMap<Record[]>)in.readObject();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

    private final NecCharsetType type;
	private final byte[] map;
	
	private boolean kshifted = false;

    public NecCharsetEncoder(Charset cs, NecCharsetType type) {
		super(cs, getAverageBytesPerChar(type), getMaxBytesPerChar(type), getReplacementChar(type));
		this.type = type;
		
		switch (type) {
		case EBCDIK:
		case JIPS_J_EBCDIK:
		case JIPS_E_EBCDIK:
			map = EBCDIK_MAP;
			break;
		default:
			map = null;
		}
	}

	@Override
	protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
        int mark = in.position();
		try {
			while (in.hasRemaining()) {
				char c = in.get();
			}
		} finally {
			in.position(mark);
		}
		return CoderResult.UNDERFLOW;
	}
	
	@Override
	protected CoderResult implFlush(ByteBuffer out) {
		if (out == DUMMY) {
			return CoderResult.OVERFLOW;
		}

		if (type.handleShift() && kshifted) {
			if (!out.hasRemaining()) {
				return CoderResult.OVERFLOW;
			}
			out.put((byte)0x28);
			kshifted = false;
		}
		return CoderResult.UNDERFLOW;
	}
	
	@Override
	protected void implReset() {
		kshifted = false;
	}

	private static float getAverageBytesPerChar(NecCharsetType type) {
		switch (type) {
		case EBCDIK:
			return 1;
		default:
			return 2;
		}
	}
	
	private static float getMaxBytesPerChar(NecCharsetType type) {
		switch (type) {
		case EBCDIK:
			return 1;
		default:
			return 2;
		}
	}
	
	private static byte[] getReplacementChar(NecCharsetType type) {
		switch (type) {
		case EBCDIK:
			return new byte[] { 0x40 };
		default:
			return new byte[] { 0x40, 0x40 };
		}
	}
}
