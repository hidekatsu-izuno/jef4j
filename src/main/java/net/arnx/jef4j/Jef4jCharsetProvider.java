package net.arnx.jef4j;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
		return map.computeIfAbsent(charsetName, cn -> {
			for (FujitsuCharsetType type : FujitsuCharsetType.values()) {
				if (type.getCharsetName().equalsIgnoreCase(cn)) {
					return new FujitsuCharset(type);
				}
			}
			return null;
		});
	}
}
