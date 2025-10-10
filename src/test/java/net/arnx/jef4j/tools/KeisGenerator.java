package net.arnx.jef4j.tools;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import net.arnx.jef4j.util.ByteUtils;

public class KeisGenerator {
    public static void main(String[] args) throws IOException {
        Charset cs = Charset.forName("EUC-JP");

        try (Writer out = Files.newBufferedWriter(Path.of("src/test/resources/hitachi_keis_mapping.json"), StandardCharsets.UTF_8)) {
            out.write("[\n");
            for (int hb = 0xA1; hb <= 0xFE; hb++) {
                for (int lb = 0xA1; lb <= 0xFE; lb++) {
                    String c = new String(new byte[] { (byte)hb, (byte)lb }, cs);
                    if (c.charAt(0) != '?' && c.charAt(0) != '\uFFFD') {
                        out.write("{ \"unicode\":  \"" + ByteUtils.hex(c.charAt(0), 4) + "\"," 
                            + " \"keis\": \"" + ByteUtils.hex(new byte[] { (byte)hb, (byte)lb })+ "\"," 
                            + " \"text\": \"" + c + "\"," 
                            + " \"options\": [] },\n");
                    }
                }
            }
            out.write("]\n");
        }

    }
}
