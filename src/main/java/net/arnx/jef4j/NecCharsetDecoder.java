package net.arnx.jef4j;

import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import net.arnx.jef4j.util.LongObjMap;

@SuppressWarnings("unchecked")
public class NecCharsetDecoder extends CharsetDecoder {
	private static final byte[] EBCDIK_MAP;
	private static final LongObjMap<Record[]> KANJI_MAP;
	
	static {
		try (ObjectInputStream in = new ObjectInputStream(
				NecCharsetDecoder.class.getResourceAsStream("NecDecodeMap.dat"))) {
			EBCDIK_MAP = (byte[])in.readObject();
			KANJI_MAP = (LongObjMap<Record[]>)in.readObject();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

    private final NecCharsetType type;
	private final byte[] map;
	
	private boolean kshifted = false;

    public NecCharsetDecoder(Charset cs, NecCharsetType type) {
		super(cs, 1, getMaxCharsPerByte(type));
		this.type = type;
		
		switch (type) {
		case EBCDIK:
		case JIPS_E_EBCDIK:
		case JIPS_J_EBCDIK:
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

    private static float getMaxCharsPerByte(NecCharsetType type) {
		switch (type) {
		case JIPS_E:
		case JIPS_J:
		case JIPS_J_JIS8:
		case JIPS_E_EBCDIK:
		case JIPS_J_EBCDIK:
			return 2.0F;
		default:
			return 1.0F;
		}
	}
}
