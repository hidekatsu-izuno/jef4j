package net.arnx.jef4j;

import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import net.arnx.jef4j.util.LongObjMap;

@SuppressWarnings("unchecked")
public class HitachiCharsetEncoder extends CharsetEncoder {
	private static final byte[] EBCDIC_MAP;
	private static final byte[] EBCDIK_MAP;
	private static final LongObjMap<Record[]> KANJI_MAP;
	private static final ByteBuffer DUMMY = ByteBuffer.allocate(0);
	
	static {
		try (ObjectInputStream in = new ObjectInputStream(
				HitachiCharsetEncoder.class.getResourceAsStream("HitachiEncodeMap.dat"))) {
			EBCDIC_MAP = (byte[])in.readObject();
			EBCDIK_MAP = (byte[])in.readObject();
			KANJI_MAP = (LongObjMap<Record[]>)in.readObject();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

    private final HitachiCharsetType type;
	private final byte[] map;
	
	private boolean kshifted = false;

    public HitachiCharsetEncoder(Charset cs, HitachiCharsetType type) {
		super(cs, getAverageBytesPerChar(type), getMaxBytesPerChar(type), getReplacementChar(type));
		this.type = type;
		
		switch (type) {
		case EBCDIC:
		case KEIS78_EBCDIC:
		case KEIS83_EBCDIC:
			map = EBCDIC_MAP;
			break;
		case EBCDIK:
		case KEIS78_EBCDIK:
		case KEIS83_EBCDIK:
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

	private static float getAverageBytesPerChar(HitachiCharsetType type) {
		switch (type) {
		case EBCDIC:
		case EBCDIK:
			return 1;
		default:
			return 2;
		}
	}
	
	private static float getMaxBytesPerChar(HitachiCharsetType type) {
		switch (type) {
		case EBCDIC:
		case EBCDIK:
			return 1;
		default:
			return 2;
		}
	}
	
	private static byte[] getReplacementChar(HitachiCharsetType type) {
		switch (type) {
		case EBCDIC:
		case EBCDIK:
			return new byte[] { 0x40 };
		default:
			return new byte[] { 0x40, 0x40 };
		}
	}
}
