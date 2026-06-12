package ar.com.catgis.sld;

import ar.com.catgis.CategoryStyleRule;
import ar.com.catgis.RuleBasedSymbology;
import ar.com.catgis.core.model.Layer;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SLD (Styled Layer Descriptor) import/export for CATGIS.
 * <p>
 * Exports real layer symbology to SLD 1.1.0 XML and imports
 * basic SLD symbolizers into CATGIS style rules.
 * </p>
 */
public final class SldSupport {

    private SldSupport() {}

    // ─── Export ───────────────────────────────────────────────────────

    /**
     * Export a layer with its actual symbology to SLD XML.
     */
    public static String exportToSld(Layer layer) {
        if (layer == null) return "";

        RuleBasedSymbology symb = layer.getPolygonRuleBasedSymbology();
        if (symb == null || symb.getRules() == null || symb.getRules().isEmpty()) {
            symb = layer.getLineRuleBasedSymbology();
        }
        if (symb == null || symb.getRules() == null || symb.getRules().isEmpty()) {
            symb = layer.getPointRuleBasedSymbology();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<StyledLayerDescriptor version=\"1.1.0\"\n");
        sb.append("  xmlns=\"http://www.opengis.net/sld\"\n");
        sb.append("  xmlns:ogc=\"http://www.opengis.net/ogc\"\n");
        sb.append("  xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
        sb.append("  <NamedLayer>\n");
        sb.append("    <Name>").append(escapeXml(layer.getName())).append("</Name>\n");
        sb.append("    <UserStyle>\n");
        sb.append("      <Title>").append(escapeXml(layer.getName())).append("</Title>\n");
        sb.append("      <FeatureTypeStyle>\n");

        if (symb != null && symb.getRules() != null) {
            for (CategoryStyleRule rule : symb.getRules()) {
                exportRule(sb, rule);
            }
        } else {
            // Default style
            exportDefaultRule(sb);
        }

        sb.append("      </FeatureTypeStyle>\n");
        sb.append("    </UserStyle>\n");
        sb.append("  </NamedLayer>\n");
        sb.append("</StyledLayerDescriptor>\n");
        return sb.toString();
    }

    private static void exportRule(StringBuilder sb, CategoryStyleRule rule) {
        sb.append("        <Rule>\n");
        sb.append("          <Name>").append(escapeXml(rule.getValue())).append("</Name>\n");

        Color fill = rule.getPrimaryColor();
        Color stroke = rule.getSecondaryColor();
        float strokeWidth = rule.getLineWidth();
        int size = rule.getPointSize();

        if (fill != null) {
            sb.append("          <PolygonSymbolizer>\n");
            sb.append("            <Fill>\n");
            sb.append("              <CssParameter name=\"fill\">#")
                    .append(colorToHex(fill)).append("</CssParameter>\n");
            sb.append("              <CssParameter name=\"fill-opacity\">")
                    .append(fill.getAlpha() / 255.0).append("</CssParameter>\n");
            sb.append("            </Fill>\n");
            if (stroke != null && strokeWidth > 0) {
                sb.append("            <Stroke>\n");
                sb.append("              <CssParameter name=\"stroke\">#")
                        .append(colorToHex(stroke)).append("</CssParameter>\n");
                sb.append("              <CssParameter name=\"stroke-width\">")
                        .append(strokeWidth).append("</CssParameter>\n");
                sb.append("            </Stroke>\n");
            }
            sb.append("          </PolygonSymbolizer>\n");
        }

        if (stroke != null && strokeWidth > 0) {
            sb.append("          <LineSymbolizer>\n");
            sb.append("            <Stroke>\n");
            sb.append("              <CssParameter name=\"stroke\">#")
                    .append(colorToHex(stroke)).append("</CssParameter>\n");
            sb.append("              <CssParameter name=\"stroke-width\">")
                    .append(strokeWidth).append("</CssParameter>\n");
            sb.append("            </Stroke>\n");
            sb.append("          </LineSymbolizer>\n");
        }

        if (size > 0) {
            sb.append("          <PointSymbolizer>\n");
            sb.append("            <Graphic>\n");
            sb.append("              <Mark>\n");
            sb.append("                <WellKnownName>circle</WellKnownName>\n");
            if (fill != null) {
                sb.append("                <Fill><CssParameter name=\"fill\">#")
                        .append(colorToHex(fill)).append("</CssParameter></Fill>\n");
            }
            if (stroke != null) {
                sb.append("                <Stroke><CssParameter name=\"stroke\">#")
                        .append(colorToHex(stroke)).append("</CssParameter></Stroke>\n");
            }
            sb.append("              </Mark>\n");
            sb.append("              <Size>").append(size).append("</Size>\n");
            sb.append("            </Graphic>\n");
            sb.append("          </PointSymbolizer>\n");
        }

        sb.append("        </Rule>\n");
    }

    private static void exportDefaultRule(StringBuilder sb) {
        sb.append("        <Rule>\n");
        sb.append("          <PointSymbolizer>\n");
        sb.append("            <Graphic><Mark><WellKnownName>circle</WellKnownName></Mark><Size>6</Size></Graphic>\n");
        sb.append("          </PointSymbolizer>\n");
        sb.append("          <LineSymbolizer>\n");
        sb.append("            <Stroke><CssParameter name=\"stroke\">#3388ff</CssParameter></Stroke>\n");
        sb.append("          </LineSymbolizer>\n");
        sb.append("          <PolygonSymbolizer>\n");
        sb.append("            <Fill><CssParameter name=\"fill\">#aaddff</CssParameter></Fill>\n");
        sb.append("          </PolygonSymbolizer>\n");
        sb.append("        </Rule>\n");
    }

    // ─── Import ───────────────────────────────────────────────────────

    /**
     * Import basic SLD XML and return a list of style rules.
     */
    public static List<CategoryStyleRule> importFromSld(String sldXml) {
        List<CategoryStyleRule> rules = new ArrayList<>();
        if (sldXml == null || sldXml.isBlank()) return rules;

        int ruleIdx = 0;
        while (true) {
            int start = sldXml.indexOf("<Rule>", ruleIdx);
            if (start < 0) start = sldXml.indexOf("<Rule ", ruleIdx);
            if (start < 0) break;
            int end = sldXml.indexOf("</Rule>", start);
            if (end < 0) break;

            String ruleBlock = sldXml.substring(start, end + 7);
            String name = extractXmlContent(ruleBlock, "Name");
            Color fill = extractColor(ruleBlock, "PolygonSymbolizer", "fill");
            Color stroke = extractColor(ruleBlock, "LineSymbolizer", "stroke");
            float strokeWidth = extractFloat(ruleBlock, "stroke-width", 1.0f);
            int pointSize = (int) extractFloat(ruleBlock, "Size", 6.0f);

            if (name == null || name.isEmpty()) name = "Rule_" + (rules.size() + 1);

            CategoryStyleRule rule = new CategoryStyleRule(name);
            if (fill != null) rule.setPrimaryColor(fill);
            if (stroke != null) rule.setSecondaryColor(stroke);
            rule.setLineWidth(strokeWidth);
            rule.setPointSize(pointSize);
            rules.add(rule);
            ruleIdx = end + 7;
        }
        return rules;
    }

    /**
     * Import SLD from a file.
     */
    public static List<CategoryStyleRule> importFromFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return importFromSld(sb.toString());
    }

    /**
     * Export SLD to a file.
     */
    public static void exportToFile(Layer layer, File outputFile) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(outputFile))) {
            pw.print(exportToSld(layer));
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────

    private static String colorToHex(Color c) {
        return String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String extractXmlContent(String xml, String tag) {
        int start = xml.indexOf("<" + tag + ">");
        if (start < 0) return null;
        start += tag.length() + 2;
        int end = xml.indexOf("</" + tag + ">", start);
        if (end < 0) return null;
        return xml.substring(start, end).trim();
    }

    private static Color extractColor(String xml, String symbolizer, String param) {
        int symIdx = xml.indexOf("<" + symbolizer + ">");
        if (symIdx < 0) return null;
        int endIdx = xml.indexOf("</" + symbolizer + ">", symIdx);
        if (endIdx < 0) return null;
        String block = xml.substring(symIdx, endIdx);

        String hex = extractCssParam(block, param);
        if (hex != null && hex.startsWith("#")) {
            try {
                return Color.decode(hex);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private static String extractCssParam(String xml, String paramName) {
        String search = "name=\"" + paramName + "\"";
        int idx = xml.indexOf(search);
        if (idx < 0) return null;
        int start = xml.indexOf(">", idx) + 1;
        int end = xml.indexOf("<", start);
        if (end < 0) return null;
        return xml.substring(start, end).trim();
    }

    private static float extractFloat(String xml, String paramName, float defaultVal) {
        String val = extractCssParam(xml, paramName);
        if (val == null) {
            // Try direct tag
            val = extractXmlContent(xml, paramName);
        }
        if (val != null) {
            try { return Float.parseFloat(val); } catch (NumberFormatException ignored) {}
        }
        return defaultVal;
    }
}
