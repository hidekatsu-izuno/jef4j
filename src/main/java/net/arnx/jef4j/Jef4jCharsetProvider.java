package net.arnx.jef4j;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.arnx.jef4j.FujitsuJefCharset.SingleByteEncoding;

public class Jef4jCharsetProvider extends CharsetProvider {
	private final ConcurrentMap<String, Charset> map = new ConcurrentHashMap<>();
	
	public Jef4jCharsetProvider() {
	}

	@Override
	public Iterator<Charset> charsets() {
		return map.values().iterator();
	}

	@Override
	public Charset charsetForName(String charsetName) {
		return map.computeIfAbsent(charsetName.toUpperCase(Locale.ENGLISH), upper -> {
			switch (upper) {
			case "X-FUJITSU-JEF-ASCII":
				return new FujitsuJefCharset(SingleByteEncoding.ASCII);
			case "X-FUJITSU-JEF-EBCDIC":
				return new FujitsuJefCharset(SingleByteEncoding.EBCDIC);
			case "X-FUJITSU-JEF-EBCDIK":
				return new FujitsuJefCharset(SingleByteEncoding.EBCDIK);
			case "X-FUJITSU-JEF":
				return new FujitsuJefCharset(SingleByteEncoding.NONE);
			}
			return null;
		});
	}
}
