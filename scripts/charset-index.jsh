import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.arnx.jef4j.util.*;

var mapper = new ObjectMapper();

boolean has(JsonNode node, String option) {
    var options = node.path("options");
    for (var value : options) if (option.equals(value.asText())) return true;
    return false;
}

String key(JsonNode node, String variant) {
    var unicode = node.get("unicode").asText();
    JsonNode sp = node.get("sp");
    if (sp == null && variant != null) sp = node.get(variant);
    if (sp == null) return unicode;
    return "0".repeat(5 - sp.asText().length()) + sp.asText()
        + "0".repeat(5 - unicode.length()) + unicode;
}

String groupKey(JsonNode node) {
    return node.get("code").asText() + ":" + key(node, null);
}

void sbcs(String file, List<Object> encoders, List<Object> decoders) throws IOException {
    var encoder = new byte[256];
    var decoder = new byte[256];
    for (var node : mapper.readTree(Files.newBufferedReader(Paths.get("src/test/resources", file)))) {
        if ("nec_jis8_ebcdik_mapping.json".equals(file)) {
            var jis8 = Integer.parseUnsignedInt(node.get("jis8").asText(), 16);
            var ebcdik = Integer.parseUnsignedInt(node.get("ebcdik").asText(), 16);
            encoder[jis8] = (byte) ebcdik;
            decoder[ebcdik] = (byte) jis8;
            continue;
        }
        var unicode = Integer.parseUnsignedInt(node.get("unicode").asText(), 16);
        var code = Integer.parseUnsignedInt(node.get("code").asText(), 16);
        if (unicode == 0x203e) unicode = 0x00b0;
        else if (unicode == 0x20ac) unicode = 0x00b1;
        else if (unicode >= 0xff61) unicode = unicode - 0xff61 + 0x00c0;
        if (!has(node, "decode_only")) encoder[unicode] = (byte) code;
        if (!has(node, "encode_only")) decoder[code] = (byte) unicode;
    }
    encoders.add(encoder);
    decoders.add(decoder);
}

void put(Map<String, String[][]> map, String value, int index, String code) {
    var prefix = value.substring(0, value.length() - 1) + "0";
    var variants = map.computeIfAbsent(prefix, ignored -> new String[3][]);
    if (variants[index] == null) variants[index] = new String[16];
    variants[index][Integer.parseUnsignedInt(value.substring(value.length() - 1), 16)] = code;
}

net.arnx.jef4j.util.Record[] records(String[][] variants, boolean encode) {
    var result = new net.arnx.jef4j.util.Record[variants.length];
    for (var i = 0; i < variants.length; i++) {
        var values = variants[i];
        if (values == null) continue;
        var pattern = 0;
        var size = 0;
        var count = 0;
        for (var value : values) {
            pattern <<= 1;
            if (value != null) {
                pattern |= 1;
                count++;
                size = Math.max(size, value.length());
            }
        }
        if (encode || size == 4) {
            var compact = new char[count];
            var offset = 0;
            for (var value : values) {
                if (value != null) compact[offset++] = (char) Integer.parseUnsignedInt(value, 16);
            }
            result[i] = new CharRecord((char) pattern, compact);
        } else if (size == 5) {
            var compact = new int[count];
            var offset = 0;
            for (var value : values) {
                if (value != null) compact[offset++] = Integer.parseUnsignedInt(value, 16);
            }
            result[i] = new IntRecord((char) pattern, compact);
        } else if (size == 10) {
            var compact = new long[count];
            var offset = 0;
            for (var value : values) {
                if (value != null) compact[offset++] = Long.parseUnsignedLong(value, 16);
            }
            result[i] = new LongRecord((char) pattern, compact);
        } else throw new IllegalStateException("Invalid Unicode key length: " + size);
    }
    for (var i = 0; i < result.length - 1; i++) {
        for (var j = i + 1; j < result.length; j++) {
            if (result[i] != null && result[i].equals(result[j])) result[j] = result[i];
        }
    }
    return result;
}

void mbcs(String file, List<Object> encoders, List<Object> decoders) throws IOException {
    Map<String, String[][]> unicodeToCode = new TreeMap<>();
    Map<String, String[][]> codeToUnicode = new TreeMap<>();
    var nodes = mapper.readTree(Files.newBufferedReader(Paths.get("src/test/resources", file)));
    Map<String, Integer> groups = new HashMap<>();
    for (var node : nodes) {
        var variant = node.has("hd") ? 1 : node.has("aj1") ? 2 : 4;
        if (!has(node, "decode_only")) variant |= 8;
        groups.merge(groupKey(node), variant, (left, right) -> left | right);
    }
    for (var node : nodes) {
        var unicode = key(node, null);
        if ("FFFD".equals(unicode)) continue;

        var variants = new String[3];
        if (node.has("hd")) variants[0] = key(node, "hd");
        if (node.has("aj1")) variants[1] = key(node, "aj1");
        var group = groups.get(groupKey(node));
        if ((!node.has("hd") && !node.has("aj1")) || (group & 4) == 0) {
            if ((group & 1) == 0) variants[0] = unicode;
            if ((group & 2) == 0) variants[1] = unicode;
            if (!has(node, "oneway")) variants[2] = unicode;
        }
        var code = node.get("code").asText();
        var variantOnly = has(node, "variant_only");
        for (var i = 0; i < variants.length; i++) {
            if (variants[i] == null) continue;
            if (!has(node, "decode_only") || ((!node.has("hd") && !node.has("aj1")) && (group & 8) != 0)) {
                if (!variantOnly) {
                    put(unicodeToCode, unicode, i, code);
                }
                if (!unicode.equals(variants[i])) {
                    put(unicodeToCode, variants[i], i, code);
                }
            }
            if (!has(node, "encode_only")) {
                put(codeToUnicode, code, i, variants[i]);
            }
        }
    }
    var encodeMap = new LongObjMap<net.arnx.jef4j.util.Record[]>();
    for (var entry : unicodeToCode.entrySet()) {
        encodeMap.put(Long.parseUnsignedLong(entry.getKey(), 16), records(entry.getValue(), true));
    }
    var decodeMap = new LongObjMap<net.arnx.jef4j.util.Record[]>();
    for (var entry : codeToUnicode.entrySet()) {
        decodeMap.put(Long.parseUnsignedLong(entry.getKey(), 16), records(entry.getValue(), false));
    }
    encoders.add(encodeMap);
    decoders.add(decodeMap);
}

void generate(String encodeFile, String decodeFile, String[] sbcsFiles, String[] mbcsFiles) throws IOException {
    List<Object> encoders = new ArrayList<>(), decoders = new ArrayList<>();
    for (var file : sbcsFiles) sbcs(file, encoders, decoders);
    for (var file : mbcsFiles) mbcs(file, encoders, decoders);
    try (var out = new ObjectOutputStream(Files.newOutputStream(Paths.get(encodeFile)))) {
        for (var map : encoders) out.writeObject(map);
    }
    try (var out = new ObjectOutputStream(Files.newOutputStream(Paths.get(decodeFile)))) {
        for (var map : decoders) out.writeObject(map);
    }
}

generate(
    "src/main/resources/net/arnx/jef4j/FujitsuEncodeMap.dat",
    "src/main/resources/net/arnx/jef4j/FujitsuDecodeMap.dat",
    new String[] {
        "fujitsu_ebcdic_lower_mapping.json",
        "fujitsu_ebcdic_kana_mapping.json",
        "fujitsu_ebcdic_ascii_mapping.json"
    }, 
    new String[] {
        "fujitsu_jef_mapping.json"
    }
);
generate(
    "src/main/resources/net/arnx/jef4j/HitachiEncodeMap.dat",
    "src/main/resources/net/arnx/jef4j/HitachiDecodeMap.dat",
    new String[] {
        "hitachi_ebcdic_mapping.json",
        "hitachi_ebcdik_mapping.json"
    }, 
    new String[] {
        "hitachi_keis78_mapping.json",
        "hitachi_keis83_mapping.json"
    }
);
generate(
    "src/main/resources/net/arnx/jef4j/NecEncodeMap.dat", 
    "src/main/resources/net/arnx/jef4j/NecDecodeMap.dat",
    new String[] {
        "nec_jis8_mapping.json",
        "nec_ebcdik_mapping.json",
        "nec_jis8_ebcdik_mapping.json"
    }, 
    new String[] {
        "nec_jips_mapping.json"
    }
);
generate(
    "src/main/resources/net/arnx/jef4j/IbmEncodeMap.dat",
    "src/main/resources/net/arnx/jef4j/IbmDecodeMap.dat",
    new String[] {
        "ibm_8482_mapping.json",
        "ibm_5123_mapping.json"
    }, 
    new String[] {
        "ibm_11684_mapping.json"
    }
);

/exit
