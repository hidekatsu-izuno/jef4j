package net.arnx.jef4j;

enum FujitsuCharsetType {
	ASCII("x-Fujitsu-ASCII"),
	EBCDIC("x-Fujitsu-EBCDIC"),
	EBCDIK("x-Fujitsu-EBCDIK"),
	JEF("x-Fujitsu-JEF"),
	JEF_ASCII("x-Fujitsu-JEF-ASCII"),
	JEF_EBCDIC("x-Fujitsu-JEF-EBCDIC"),
	JEF_EBCDIK("x-Fujitsu-JEF-EBCDIK");
	
	private final String charsetName;
	
	FujitsuCharsetType(String charsetName) {
		this.charsetName = charsetName;
	}
	
	public String getCharsetName() {
		return charsetName;
	}
}
