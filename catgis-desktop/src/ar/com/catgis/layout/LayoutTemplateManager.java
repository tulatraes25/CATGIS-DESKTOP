package ar.com.catgis.layout;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LayoutTemplateManager {

    public static void saveTemplate(File file, LayoutModel model, double pageW, double pageH, String orientation) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"format\": \"catmap\",\n");
        sb.append("  \"version\": 1,\n");
        sb.append("  \"page\": { \"widthMm\": ").append(pageW).append(", \"heightMm\": ").append(pageH);
        sb.append(", \"orientation\": \"").append(orientation).append("\" },\n");
        sb.append("  \"elements\": [\n");
        java.util.List<LayoutElement> elements = model.getElements();
        for (int i = 0; i < elements.size(); i++) {
            LayoutElement el = elements.get(i);
            sb.append("    {");
            sb.append("\"type\": \"").append(el.getClass().getSimpleName()).append("\", ");
            sb.append("\"id\": \"").append(esc(el.getId())).append("\", ");
            sb.append("\"xMm\": ").append(el.getBoundsMm().x).append(", ");
            sb.append("\"yMm\": ").append(el.getBoundsMm().y).append(", ");
            sb.append("\"wMm\": ").append(el.getBoundsMm().width).append(", ");
            sb.append("\"hMm\": ").append(el.getBoundsMm().height).append(", ");
            sb.append("\"zOrder\": ").append(el.getZOrder()).append(", ");
            sb.append("\"visible\": ").append(el.isVisible()).append(", ");
            sb.append("\"locked\": ").append(el.isLocked());

            if (el instanceof LayoutLegend) {
                LayoutLegend leg = (LayoutLegend) el;
                sb.append(serializeLegend(leg));
            } else if (el instanceof LayoutLabel) {
                LayoutLabel lab = (LayoutLabel) el;
                sb.append(", \"text\": \"").append(esc(lab.getText())).append("\"");
                sb.append(", \"fontSize\": ").append(lab.getFont().getSize2D());
                sb.append(", \"color\": ").append(colorHex(lab.getColor()));
            }

            sb.append("}");
            if (i < elements.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");

        try (FileWriter w = new FileWriter(file, StandardCharsets.UTF_8)) {
            w.write(sb.toString());
        }
    }

    private static String serializeLegend(LayoutLegend leg) {
        StringBuilder s = new StringBuilder();
        s.append(", \"title\": \"").append(esc(leg.getTitle())).append("\"");
        s.append(", \"autoHeight\": ").append(leg.isAutoHeight());
        s.append(", \"showBg\": ").append(leg.isShowBackground());
        s.append(", \"showBorder\": ").append(leg.isShowBorder());
        s.append(", \"titleFontSize\": ").append(leg.getTitleFont().getSize2D());
        s.append(", \"itemFontSize\": ").append(leg.getItemFont().getSize2D());
        s.append(", \"items\": [");
        for (int i = 0; i < leg.getItems().size(); i++) {
            LayoutLegend.LegendItem item = leg.getItems().get(i);
            s.append("{\"label\": \"").append(esc(item.label)).append("\", ");
            s.append("\"included\": ").append(item.included).append("}");
            if (i < leg.getItems().size() - 1) s.append(", ");
        }
        s.append("]");
        return s.toString();
    }

    public static void loadTemplate(File file, LayoutModel model) throws IOException {
        String content;
        try (FileReader r = new FileReader(file, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int len;
            while ((len = r.read(buf)) > 0) sb.append(buf, 0, len);
            content = sb.toString();
        }
        // Simple JSON parsing - just extract elements array
        int elementsStart = content.indexOf("\"elements\"");
        if (elementsStart < 0) return;
        int arrStart = content.indexOf('[', elementsStart);
        int arrEnd = content.lastIndexOf(']');
        if (arrStart < 0 || arrEnd < 0) return;
        String arr = content.substring(arrStart + 1, arrEnd);

        String[] parts = arr.split("\\},\\s*\\{");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.startsWith("{")) part = part.substring(1);
            if (part.endsWith("}")) part = part.substring(0, part.length() - 1);
            parseElement(part, model);
        }
    }

    private static void parseElement(String json, LayoutModel model) {
        String type = extractStr(json, "type");
        String id = extractStr(json, "id");
        double x = extractDouble(json, "xMm");
        double y = extractDouble(json, "yMm");
        double w = extractDouble(json, "wMm");
        double h = extractDouble(json, "hMm");
        int z = extractInt(json, "zOrder");
        boolean vis = extractBool(json, "visible", true);
        boolean locked = extractBool(json, "locked", false);

        LayoutElement el = null;
        if ("LayoutLabel".equals(type)) {
            String text = extractStr(json, "text");
            el = new LayoutLabel(id, text, x, y, w, h);
        } else if ("LayoutLegend".equals(type)) {
            el = new LayoutLegend(id, x, y, w, h);
            LayoutLegend leg = (LayoutLegend) el;
            leg.setTitle(extractStr(json, "title"));
            leg.setAutoHeight(extractBool(json, "autoHeight", true));
            leg.setShowBackground(extractBool(json, "showBg", false));
            leg.setShowBorder(extractBool(json, "showBorder", false));
            double tfs = extractDouble(json, "titleFontSize");
            double ifs = extractDouble(json, "itemFontSize");
            if (tfs > 0) leg.setTitleFont(new Font("SansSerif", Font.BOLD, (int) tfs));
            if (ifs > 0) leg.setItemFont(new Font("SansSerif", Font.PLAIN, (int) ifs));
        }
        if (el != null) {
            el.setZOrder(z);
            el.setVisible(vis);
            el.setLocked(locked);
            model.addElement(el);
        }
    }

    private static String extractStr(String json, String key) {
        int ki = json.indexOf("\"" + key + "\"");
        if (ki < 0) return "";
        int vi = json.indexOf(':', ki) + 1;
        while (vi < json.length() && Character.isWhitespace(json.charAt(vi))) vi++;
        if (vi < json.length() && json.charAt(vi) == '"') {
            int end = json.indexOf('"', vi + 1);
            return end > vi ? json.substring(vi + 1, end).replace("\\\"", "\"") : "";
        }
        int end = vi;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
        return json.substring(vi, end).trim();
    }

    private static double extractDouble(String json, String key) {
        try { return Double.parseDouble(extractStr(json, key)); } catch (Exception e) { return 0; }
    }

    private static int extractInt(String json, String key) {
        try { return Integer.parseInt(extractStr(json, key)); } catch (Exception e) { return 0; }
    }

    private static boolean extractBool(String json, String key, boolean def) {
        String v = extractStr(json, key);
        if (v.isEmpty()) return def;
        return "true".equalsIgnoreCase(v);
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String colorHex(Color c) {
        if (c == null) return "0";
        return "\"#" + String.format("%06X", c.getRGB() & 0xFFFFFF) + "\"";
    }
}
