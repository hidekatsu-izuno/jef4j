package net.arnx.jef4j;

enum FujitsuCharsetType {
	ASCII("x-Fujitsu-ASCII", false),
	EBCDIC("x-Fujitsu-EBCDIC", false),
	EBCDIK("x-Fujitsu-EBCDIK", false),
	JEF("x-Fujitsu-JEF", true),
	JEF_ASCII("x-Fujitsu-JEF-ASCII", true),
	JEF_EBCDIC("x-Fujitsu-JEF-EBCDIC", true),
	JEF_EBCDIK("x-Fujitsu-JEF-EBCDIK", true);
	
	private final String charsetName;
	private final boolean containsJEF;
	
	FujitsuCharsetType(String charsetName, boolean containsJEF) {
		this.charsetName = charsetName;
		this.containsJEF = containsJEF;
	}
	
	public String getCharsetName() {
		return charsetName;
	}
	
	public boolean containsJEF() {
		return containsJEF;
	}
}
