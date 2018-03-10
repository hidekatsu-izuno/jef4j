package net.arnx.jef4j.util;

public interface Record {
	public boolean exists(int pos);
	public long get(int pos);
}
