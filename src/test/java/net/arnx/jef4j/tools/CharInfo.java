package net.arnx.jef4j.tools;

import java.util.Set;
import java.util.TreeSet;

public class CharInfo {
    public String unicode;
    public String sp;
    public String hd;
    public String aj1;
    public String ebcdic;
    public String jef;
    public String text;
    public Set<String> options = new TreeSet<>();

    public CharInfo() {
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        if (jef != null) {
            sb.append("\"unicode\": ")
                .append(" ".repeat(5 - unicode.length()))
                .append("\"").append(unicode.replace("\"", "\\\"")).append("\", ");
            if (sp != null) {
                sb.append("\"sp\": ")
                    .append(" ".repeat(5 - sp.length()))
                    .append("\"").append(sp.replace("\"", "\\\"")).append("\", ");
            } else {
                sb.append(" ".repeat(15));
            }
            if (hd != null) {
                sb.append("\"hd\": ")
                    .append(" ".repeat(5 - hd.length()))
                    .append("\"").append(hd.replace("\"", "\\\"")).append("\", ");
            } else {
                sb.append(" ".repeat(15));
            }
            if (aj1 != null) {
                sb.append("\"aj1\": ")
                    .append(" ".repeat(5 - aj1.length()))
                    .append("\"").append(aj1.replace("\"", "\\\"")).append("\", ");
            } else {
                sb.append(" ".repeat(16));
            }
            sb.append("\"jef\": \"").append(jef.replace("\"", "\\\"")).append("\", ");
        } else {
            sb.append("\"unicode\": ")
                .append(" ".repeat(5 - unicode.length()))
                .append("\"").append(unicode.replace("\"", "\\\"")).append("\", ");
            sb.append("\"ebcdic\": \"").append(ebcdic.replace("\"", "\\\"")).append("\", ");
        }
        sb.append("\"text\": \"").append(text.replace("\\", "\\\\").replace("\"", "\\\"")).append("\", ");
        sb.append("\"options\": [");
        int pos = 0;
        for (String option : options) {
            if (pos > 0) {
                sb.append(", ");
            }
            sb.append("\"").append(option.replace("\"", "\\\"")).append("\"");
            pos++;
        }
        sb.append("]");
        sb.append(" }");
        return sb.toString();
    }
}
