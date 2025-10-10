package net.arnx.jef4j;

import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import net.arnx.jef4j.util.LongObjMap;

@SuppressWarnings("unchecked")
public class HitachiCharsetDecoder extends CharsetDecoder {
	private static final byte[] EBCDIC_MAP;
	private static final byte[] EBCDIK_MAP;
	private static final LongObjMap<Record[]> KANJI_MAP;
	
	static {
		try (ObjectInputStream in = new ObjectInputStream(
				HitachiCharsetDecoder.class.getResourceAsStream("HitachiDecodeMap.dat"))) {
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

    public HitachiCharsetDecoder(Charset cs, HitachiCharsetType type) {
		super(cs, 1, getMaxCharsPerByte(type));
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
	protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
		int mark = in.position();
		try {
			while (in.hasRemaining()) {
				int b = in.get() & 0xFF;
			}
		} finally {
			in.position(mark);
		}
		return CoderResult.UNDERFLOW;
	}
	
	@Override
	protected void implReset() {
		kshifted = false;
	}

    private static float getMaxCharsPerByte(HitachiCharsetType type) {
		switch (type) {
		case KEIS78:
        case KEIS83:
		case KEIS78_EBCDIC:
		case KEIS78_EBCDIK:
		case KEIS83_EBCDIC:
		case KEIS83_EBCDIK:
			return 2.0F;
		default:
			return 1.0F;
		}
	}
}
