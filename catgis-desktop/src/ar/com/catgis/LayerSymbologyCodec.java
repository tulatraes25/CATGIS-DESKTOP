package ar.com.catgis;

import ar.com.catgis.core.model.Layer;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Encode/decode symbology configuration to/from text strings for project file persistence.
 * <p>
 * Supports categorized, graduated, and rule-based symbology types.
 * Values are Base64-encoded to safely handle special characters in field names and expressions.
 */
public final class LayerSymbologyCodec {

    private LayerSymbologyCodec() {}

    // ─── Backward-compatible aliases ───────────────────────────────
    /** @deprecated Use {@link #encodeCategorized(CategorizedSymbology)} */
    @Deprecated public static String encodeCategorizedSymbology(CategorizedSymbology s) { return encodeCategorized(s); }
    /** @deprecated Use {@link #decodeCategorized(String)} */
    @Deprecated public static CategorizedSymbology decodeCategorizedSymbology(String s) { return decodeCategorized(s); }
    /** @deprecated Use {@link #encodeGraduated(GraduatedSymbology)} */
    @Deprecated public static String encodeGraduatedSymbology(GraduatedSymbology s) { return encodeGraduated(s); }
    /** @deprecated Use {@link #decodeGraduated(String)} */
    @Deprecated public static GraduatedSymbology decodeGraduatedSymbology(String s) { return decodeGraduated(s); }

    // ─── Categorized ─────────────────────────────────────────────────────

    public static String encodeCategorized(CategorizedSymbology s) {
        if (s == null || !s.isConfigured()) return "";
        StringBuilder rules = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, CategoryStyleRule> e : s.getRules().entrySet()) {
            if (!first) rules.append(';');
            first = false;
            CategoryStyleRule r = e.getValue();
            rules.append(encode(e.getKey())).append('>')
                 .append(colorToText(r.getPrimaryColor())).append('>')
                 .append(colorToText(r.getSecondaryColor())).append('>')
                 .append(r.getLineStyle().name()).append('>')
                 .append(r.getLineWidth()).append('>')
                 .append(r.getPolygonFillStyle().name()).append('>')
                 .append(r.getPointSymbolStyle().name()).append('>')
                 .append(r.getPointSize());
        }
        return encode(s.getFieldName()) + "~" + encode(s.getLegendTitle()) + "~" + encode(s.getLegendSubtitle()) + "~" + rules;
    }

    public static CategorizedSymbology decodeCategorized(String text) {
        CategorizedSymbology s = new CategorizedSymbology();
        if (text == null || text.isBlank()) return s;
        String[] p = text.split("~", 4);
        if (p.length < 4) return s;
        s.setFieldName(decode(p[0]));
        s.setLegendTitle(decode(p[1]));
        s.setLegendSubtitle(decode(p[2]));
        if (p[3].isBlank()) return s;
        for (String rp : p[3].split(";")) {
            String[] f = rp.split(">", -1);
            if (f.length < 5) continue;
            CategoryStyleRule r = s.getOrCreateRule(decode(f[0]));
            Color c1 = parseColor(f[1]), c2 = parseColor(f[2]);
            if (c1 != null) r.setPrimaryColor(c1);
            if (c2 != null) r.setSecondaryColor(c2);
            r.setLineStyle(Layer.LineSymbolStyle.fromValue(f[3]));
            if (f.length >= 6) { try { r.setLineWidth(Float.parseFloat(f[4])); } catch (Exception ignored) {} }
            r.setPolygonFillStyle(Layer.PolygonFillStyle.fromValue(f[5]));
            if (f.length >= 7) r.setPointSymbolStyle(Layer.PointSymbolStyle.fromValue(f[6]));
            if (f.length >= 8) { try { r.setPointSize(Integer.parseInt(f[7])); } catch (Exception ignored) {} }
        }
        return s;
    }

    // ─── Graduated ───────────────────────────────────────────────────────

    public static String encodeGraduated(GraduatedSymbology s) {
        if (s == null || !s.isConfigured()) return "";
        StringBuilder rules = new StringBuilder();
        boolean first = true;
        for (GraduatedRangeRule r : s.getRules()) {
            if (!first) rules.append(';');
            first = false;
            rules.append(r.getMinValue()).append('>').append(r.getMaxValue()).append('>')
                 .append(encode(r.getLabel())).append('>')
                 .append(colorToText(r.getPrimaryColor())).append('>')
                 .append(colorToText(r.getSecondaryColor())).append('>')
                 .append(r.getLineStyle().name()).append('>')
                 .append(r.getLineWidth()).append('>')
                 .append(r.getPolygonFillStyle().name()).append('>')
                 .append(r.getPointSymbolStyle().name()).append('>')
                 .append(r.getPointSize());
        }
        return encode(s.getFieldName()) + "~" + encode(s.getLegendTitle()) + "~" + encode(s.getLegendSubtitle())
             + "~" + s.getNumClasses() + "~" + rules;
    }

    public static GraduatedSymbology decodeGraduated(String text) {
        GraduatedSymbology s = new GraduatedSymbology();
        if (text == null || text.isBlank()) return s;
        String[] p = text.split("~", -1);
        if (p.length < 5) return s;
        s.setFieldName(decode(p[0]));
        s.setLegendTitle(decode(p[1]));
        s.setLegendSubtitle(decode(p[2]));
        if (p.length >= 4) { try { s.setNumClasses(Integer.parseInt(p[3])); } catch (Exception ignored) {} }
        if (p.length < 5 || p[4].isBlank()) return s;
        for (String rp : p[4].split(";")) {
            String[] f = rp.split(">", -1);
            if (f.length < 4) continue;
            try {
                double min = Double.parseDouble(f[0]), max = Double.parseDouble(f[1]);
                String label = decode(f[2]);
                GraduatedRangeRule r = new GraduatedRangeRule(min, max, label);
                Color c1 = parseColor(f[3]), c2 = f.length >= 5 ? parseColor(f[4]) : null;
                if (c1 != null) r.setPrimaryColor(c1);
                if (c2 != null) r.setSecondaryColor(c2);
                if (f.length >= 6) r.setLineStyle(Layer.LineSymbolStyle.fromValue(f[5]));
                if (f.length >= 7) { try { r.setLineWidth(Float.parseFloat(f[6])); } catch (Exception ignored) {} }
                if (f.length >= 8) r.setPolygonFillStyle(Layer.PolygonFillStyle.fromValue(f[7]));
                if (f.length >= 9) r.setPointSymbolStyle(Layer.PointSymbolStyle.fromValue(f[8]));
                if (f.length >= 10) { try { r.setPointSize(Integer.parseInt(f[9])); } catch (Exception ignored) {} }
                s.getRules().add(r);
            } catch (Exception ignored) {}
        }
        return s;
    }

    // ─── Rule-based ──────────────────────────────────────────────────────

    public static String encodeRuleBased(RuleBasedSymbology s) {
        if (s == null || !s.isConfigured()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(encode(s.getDescription())).append('~');
        encodeRuleList(sb, s.getRules());
        return sb.toString();
    }

    private static void encodeRuleList(StringBuilder sb, java.util.List<RuleBasedStyleRule> rules) {
        boolean first = true;
        for (RuleBasedStyleRule r : rules) {
            if (!first) sb.append('|');
            first = false;
            sb.append(encode(r.getDescription())).append('>')
              .append(encode(r.getFilterExpression())).append('>')
              .append(r.getScaleMin()).append('>')
              .append(r.getScaleMax()).append('>')
              .append(r.isElseRule() ? "1" : "0").append('>')
              .append(colorToText(r.getPrimaryColor())).append('>')
              .append(colorToText(r.getSecondaryColor())).append('>')
              .append(r.getLineStyle().name()).append('>')
              .append(r.getLineWidth()).append('>')
              .append(r.getPolygonFillStyle().name()).append('>')
              .append(r.getPointSymbolStyle().name()).append('>')
              .append(r.getPointSize()).append('>')
              .append(encode(r.getCatalogSymbolId()));
            if (r.hasChildren()) {
                sb.append('[');
                encodeRuleList(sb, r.getChildren());
                sb.append(']');
            }
        }
    }

    public static RuleBasedSymbology decodeRuleBased(String text) {
        RuleBasedSymbology s = new RuleBasedSymbology();
        if (text == null || text.isBlank()) return s;
        s.setEnabled(true);
        String[] p = text.split("~", 2);
        if (p.length < 2) return s;
        s.setDescription(decode(p[0]));
        decodeRuleList(p[1], s.getRules());
        return s;
    }

    private static int decodeRuleList(String text, java.util.List<RuleBasedStyleRule> rules) {
        if (text == null || text.isBlank()) return 0;
        int pos = 0;
        while (pos < text.length()) {
            if (text.charAt(pos) == ']' || text.charAt(pos) == '|') { pos++; continue; }
            if (text.charAt(pos) == '[') { pos++; continue; }
            int end = indexOfAny(text, pos, '>', '|', '[');
            if (end < 0) break;
            String desc = decode(text.substring(pos, end));
            pos = end + 1; // skip >

            end = indexOfAny(text, pos, '>', '|', '[');
            if (end < 0) break;
            String filter = decode(text.substring(pos, end));
            pos = end + 1;

            end = indexOfAny(text, pos, '>', '|', '['); if (end < 0) break;
            double scaleMin = tryParseDouble(text.substring(pos, end), 0); pos = end + 1;

            end = indexOfAny(text, pos, '>', '|', '['); if (end < 0) break;
            double scaleMax = tryParseDouble(text.substring(pos, end), 0); pos = end + 1;

            end = indexOfAny(text, pos, '>', '|', '['); if (end < 0) break;
            boolean elseRule = "1".equals(text.substring(pos, end)); pos = end + 1;

            end = indexOfAny(text, pos, '>', '|', '['); if (end < 0) break;
            Color primary = parseColor(text.substring(pos, end)); pos = end + 1;

            end = indexOfAny(text, pos, '>', '|', '['); if (end < 0) break;
            Color secondary = parseColor(text.substring(pos, end)); pos = end + 1;

            end = indexOfAny(text, pos, '>', '|', '['); if (end < 0) break;
            String lineStyle = text.substring(pos, end); pos = end + 1;

            end = indexOfAny(text, pos, '>', '|', '['); if (end < 0) break;
            float lineWidth = (float) tryParseDouble(text.substring(pos, end), 1.5f); pos = end + 1;

            end = indexOfAny(text, pos, '>', '|', '['); if (end < 0) break;
            String polyFill = text.substring(pos, end); pos = end + 1;

            end = indexOfAny(text, pos, '>', '|', '['); if (end < 0) break;
            String pointStyle = text.substring(pos, end); pos = end + 1;

            end = indexOfAny(text, pos, '>', '|', '['); if (end < 0) break;
            int pointSize = (int) tryParseDouble(text.substring(pos, end), 9); pos = end + 1;

            end = indexOfAny(text, pos, '>', '|', '['); if (end < 0) break;
            String catId = decode(text.substring(pos, end)); pos = end + 1;

            RuleBasedStyleRule rule = new RuleBasedStyleRule(desc);
            rule.setFilterExpression(filter);
            rule.setScaleMin(scaleMin);
            rule.setScaleMax(scaleMax);
            rule.setElseRule(elseRule);
            if (primary != null) rule.setPrimaryColor(primary);
            if (secondary != null) rule.setSecondaryColor(secondary);
            rule.setLineStyle(Layer.LineSymbolStyle.fromValue(lineStyle));
            rule.setLineWidth(lineWidth);
            rule.setPolygonFillStyle(Layer.PolygonFillStyle.fromValue(polyFill));
            rule.setPointSymbolStyle(Layer.PointSymbolStyle.fromValue(pointStyle));
            rule.setPointSize(pointSize);
            rule.setCatalogSymbolId(catId);

            // Check for children block
            if (pos < text.length() && text.charAt(pos) == '[') {
                pos++; // skip [
                int childEnd = findMatchingBracket(text, pos);
                if (childEnd > pos) {
                    decodeRuleList(text.substring(pos, childEnd), rule.getChildren());
                    pos = childEnd + 1; // skip ]
                }
            }

            rules.add(rule);
        }
        return pos;
    }

    // ─── Utilities ───────────────────────────────────────────────────────

    private static int indexOfAny(String s, int from, char... chars) {
        for (int i = from; i < s.length(); i++) {
            for (char c : chars) {
                if (s.charAt(i) == c) return i;
            }
        }
        return -1;
    }

    private static int findMatchingBracket(String s, int from) {
        int depth = 1;
        for (int i = from; i < s.length(); i++) {
            if (s.charAt(i) == '[') depth++;
            else if (s.charAt(i) == ']') { depth--; if (depth == 0) return i; }
        }
        return -1;
    }

    private static double tryParseDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((value != null ? value : "").getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        if (value == null || value.isBlank()) return "";
        try { return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8); }
        catch (Exception e) { return ""; }
    }

    private static String colorToText(Color c) {
        if (c == null) return "";
        return c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha();
    }

    private static Color parseColor(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            String[] p = text.split(",");
            if (p.length < 3) return null;
            return new Color(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()),
                             Integer.parseInt(p[2].trim()), p.length >= 4 ? Integer.parseInt(p[3].trim()) : 255);
        } catch (Exception e) { return null; }
    }
}
