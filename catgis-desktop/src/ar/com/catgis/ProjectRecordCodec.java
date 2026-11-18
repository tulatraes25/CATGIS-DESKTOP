package ar.com.catgis;

/**
 * Reversible, versioned encoding for the line-oriented .catgis project format.
 *
 * <p>v1 replaced '|' with a space and broke paths/names with pipes or newlines.
 * v2 writes a {@code PROJECT_FORMAT|2} marker and escapes the only characters
 * that are structurally significant in a line record: {@code %}, {@code |},
 * CR and LF. Unicode and backslashes are preserved verbatim; no lossy trim is
 * applied to the decoded text.</p>
 */
public final class ProjectRecordCodec {

    /** Current on-disk project format version. */
    public static final int VERSION = 2;

    /** Marker line written right after the CATGIS_PROJECT header. */
    public static final String FORMAT_LINE = "PROJECT_FORMAT|" + VERSION;

    private ProjectRecordCodec() {}

    public static String escape(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '%' -> out.append("%25");
                case '|' -> out.append("%7C");
                case '\r' -> out.append("%0D");
                case '\n' -> out.append("%0A");
                default -> out.append(ch);
            }
        }
        return out.toString();
    }

    public static String unescape(String value) {
        if (value == null || value.isEmpty() || value.indexOf('%') < 0) {
            return value == null ? "" : value;
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch != '%') {
                out.append(ch);
                continue;
            }
            if (i + 2 >= value.length()) {
                throw new IllegalArgumentException("Secuencia escapada incompleta en el proyecto.");
            }
            int high = Character.digit(value.charAt(i + 1), 16);
            int low = Character.digit(value.charAt(i + 2), 16);
            if (high < 0 || low < 0) {
                throw new IllegalArgumentException("Secuencia escapada invalida en el proyecto.");
            }
            out.append((char) ((high << 4) + low));
            i += 2;
        }
        return out.toString();
    }

    /** True when the first non-comment line declares a format version. */
    public static boolean isV2Header(String line) {
        return line != null && line.trim().equals(FORMAT_LINE);
    }
}
