package ar.com.catgis.layout;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class LayoutTemplateManager {

    public static Map<String, String> getTemplateList() {
        Map<String, String> list = new LinkedHashMap<>();
        list.put("A4_AMBIENTAL", "A4 horizontal - Informe ambiental");
        list.put("A4_TECNICO", "A4 horizontal - Tecnico con leyenda");
        list.put("A3_TECNICO", "A3 horizontal - Mapa grande");
        list.put("A4_MUESTREO", "A4 horizontal - Muestreo");
        list.put("A4_SATELITAL", "A4 horizontal - Imagen satelital");
        list.put("A4_VERTICAL", "A4 vertical - Informe");
        list.put("A4_REFERENCIA", "A4 - Referencia / Accesibilidad");
        list.put("A4_ACCESIBILIDAD", "A4 - Accesibilidad");
        list.put("A4_EMPLAZAMIENTO", "A4 - Emplazamiento");
        list.put("A4_PERFIL", "A4 - Perfil / Altimetria");
        return list;
    }

    public static void applyTemplate(String key, LayoutModel model) {
        model.clearSelection();
        java.util.List<LayoutElement> rm = new java.util.ArrayList<>(model.getElements());
        for (LayoutElement e : rm) model.removeElement(e.getId());
        switch (key) {
            case "A4_AMBIENTAL": buildAmbiental(model); break;
            case "A4_TECNICO": buildTecnico(model); break;
            case "A3_TECNICO": buildA3Tecnico(model); break;
            case "A4_MUESTREO": buildMuestreo(model); break;
            case "A4_SATELITAL": buildSatelital(model); break;
            case "A4_VERTICAL": buildVertical(model); break;
            case "A4_REFERENCIA": buildReferencia(model); break;
            case "A4_ACCESIBILIDAD": buildAccesibilidad(model); break;
            case "A4_EMPLAZAMIENTO": buildEmplazamiento(model); break;
            case "A4_PERFIL": buildPerfil(model); break;
        }
    }

    private static void addLabel(LayoutModel m, String id, String txt, int x, int y, int w, int h, Font f, Color c, int[] z) { LayoutLabel l = new LayoutLabel(id, txt, x, y, w, h); l.setFont(f); l.setColor(c); l.setZOrder(z[0]++); l.setName(id); m.addElement(l); }
    private static void addMap(LayoutModel m, String id, int x, int y, int w, int h, int[] z) { LayoutMap mp = new LayoutMap(id, x, y, w, h); mp.setZOrder(z[0]++); mp.setName(id); m.addElement(mp); }
    private static void addLegend(LayoutModel m, String id, int x, int y, int w, int h, int[] z, boolean bg) { LayoutLegend lg = new LayoutLegend(id, x, y, w, h); lg.setZOrder(z[0]++); lg.setAutoHeight(true); lg.setName(id); if (bg) { lg.setShowBackground(true); lg.setBgOpacity(0.9f); } m.addElement(lg); }
    private static void addScale(LayoutModel m, String id, int x, int y, int w, int h, int[] z) { LayoutScaleBar sb = new LayoutScaleBar(id, x, y, w, h); sb.setZOrder(z[0]++); sb.setName(id); m.addElement(sb); }
    private static void addNorth(LayoutModel m, String id, int x, int y, int w, int h, int[] z) { LayoutNorthArrow na = new LayoutNorthArrow(id, x, y, w, h); na.setZOrder(z[0]++); na.setName(id); m.addElement(na); }

    private static void buildAmbiental(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Mapa Ambiental", 15, 8, 267, 14, new Font("SansSerif", Font.BOLD, 16), new Color(0x1A2434), z);
        addMap(m, "Mapa principal", 15, 25, 267, 150, z);
        addLegend(m, "Leyenda", 15, 178, 130, 40, z, false);
        addScale(m, "Escala", 15, 215, 100, 10, z);
        addNorth(m, "Norte", 260, 165, 18, 18, z); }
    private static void buildTecnico(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Mapa Tecnico", 15, 8, 267, 14, new Font("SansSerif", Font.BOLD, 18), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Salida cartografica del proyecto", 15, 23, 267, 10, new Font("SansSerif", Font.PLAIN, 9), new Color(0x5B6778), z);
        addMap(m, "Mapa principal", 15, 25, 175, 165, z);
        addLegend(m, "Leyenda", 195, 25, 85, 40, z, true);
        addScale(m, "Escala", 15, 193, 120, 10, z);
        addNorth(m, "Norte", 175, 180, 16, 16, z); }
    private static void buildA3Tecnico(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Mapa Tecnico A3", 20, 10, 380, 18, new Font("SansSerif", Font.BOLD, 22), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Salida cartografica del proyecto", 20, 29, 380, 10, new Font("SansSerif", Font.PLAIN, 10), new Color(0x5B6778), z);
        addMap(m, "Mapa principal", 20, 32, 380, 244, z);
        addLegend(m, "Leyenda", 20, 280, 250, 40, z, false);
        addScale(m, "Escala", 280, 280, 120, 10, z);
        addNorth(m, "Norte", 370, 270, 22, 22, z); }
    private static void buildMuestreo(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Mapa de Muestreo", 15, 8, 267, 14, new Font("SansSerif", Font.BOLD, 16), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Planilla de puntos de muestreo", 15, 23, 267, 10, new Font("SansSerif", Font.PLAIN, 9), new Color(0x5B6778), z);
        addMap(m, "Mapa principal", 15, 25, 267, 155, z);
        addLegend(m, "Leyenda", 15, 183, 140, 40, z, false);
        addScale(m, "Escala", 155, 183, 120, 10, z);
        addNorth(m, "Norte", 268, 168, 16, 16, z); }
    private static void buildSatelital(LayoutModel m) { int[] z = {0};
        addMap(m, "Mapa principal", 15, 8, 267, 178, z);
        addLabel(m, "Titulo", "Imagen Satelital", 15, 190, 200, 10, new Font("SansSerif", Font.BOLD, 12), Color.WHITE, z);
        addScale(m, "Escala", 170, 190, 110, 10, z);
        addNorth(m, "Norte", 268, 8, 14, 14, z); }
    private static void buildVertical(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Informe", 12, 8, 186, 14, new Font("SansSerif", Font.BOLD, 16), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Documento tecnico", 12, 23, 186, 10, new Font("SansSerif", Font.PLAIN, 9), new Color(0x5B6778), z);
        addMap(m, "Mapa principal", 12, 25, 186, 210, z);
        addLegend(m, "Leyenda", 12, 238, 100, 40, z, false);
        addScale(m, "Escala", 115, 238, 80, 10, z);
        addNorth(m, "Norte", 180, 220, 14, 14, z); }

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
            sb.append("\"locked\": ").append(el.isLocked()).append(", ");
            sb.append("\"name\": \"").append(esc(el.getName() != null ? el.getName() : el.getId())).append("\"");

            if (el instanceof LayoutLegend) {
                LayoutLegend leg = (LayoutLegend) el;
                sb.append(serializeLegend(leg));
            } else if (el instanceof LayoutMap) {
                LayoutMap map = (LayoutMap) el;
                sb.append(", \"showGrid\": ").append(map.isShowGrid())
                  .append(", \"gridCols\": ").append(map.getGridCols())
                  .append(", \"gridRows\": ").append(map.getGridRows())
                  .append(", \"gridByDistance\": ").append(map.isGridByDistance())
                  .append(", \"gridIntervalX\": ").append(map.getGridIntervalX())
                  .append(", \"gridIntervalY\": ").append(map.getGridIntervalY())
                  .append(", \"gridUnit\": \"").append(esc(map.getGridUnit())).append("\"")
                  .append(", \"gridColor\": ").append(colorHex(map.getGridColor()))
                  .append(", \"targetScale\": ").append(map.getTargetScaleDenominator());
            } else if (el instanceof LayoutLabel) {
                LayoutLabel lab = (LayoutLabel) el;
                sb.append(", \"text\": \"").append(esc(lab.getText())).append("\"");
                sb.append(", \"fontSize\": ").append(lab.getFont().getSize2D());
                sb.append(", \"fontFamily\": \"").append(esc(lab.getFont().getFamily())).append("\"");
                sb.append(", \"bold\": ").append(lab.getFont().isBold());
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
            LayoutLabel lab = (LayoutLabel) el;
            double fs = extractDouble(json, "fontSize");
            String ff = extractStr(json, "fontFamily");
            boolean bold = extractBool(json, "bold", false);
            if (fs > 0 && !ff.isEmpty()) {
                lab.setFont(new Font(ff, bold ? Font.BOLD : Font.PLAIN, (int) fs));
            } else if (fs > 0) {
                lab.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, (int) fs));
            }
            String c = extractStr(json, "color");
            if (!c.isEmpty()) lab.setColor(parseColor(c));
            String nm = extractStr(json, "name");
            if (!nm.isEmpty()) lab.setName(nm);
        } else if ("LayoutMap".equals(type)) {
            el = new LayoutMap(id, x, y, w, h);
            LayoutMap map = (LayoutMap) el;
            map.setShowGrid(extractBool(json, "showGrid", false));
            map.setGridCols(extractInt(json, "gridCols", 3));
            map.setGridRows(extractInt(json, "gridRows", 3));
            map.setGridByDistance(extractBool(json, "gridByDistance", false));
            map.setGridIntervalX(extractDouble(json, "gridIntervalX", 100));
            map.setGridIntervalY(extractDouble(json, "gridIntervalY", 100));
            String gu = extractStr(json, "gridUnit");
            if (!gu.isEmpty()) map.setGridUnit(gu);
            String gc = extractStr(json, "gridColor");
            if (!gc.isEmpty()) map.setGridColor(parseColor(gc));
            map.setTargetScaleDenominator(extractDouble(json, "targetScale", 0));
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
            String nm = extractStr(json, "name");
            if (!nm.isEmpty()) el.setName(nm);
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

    private static double extractDouble(String json, String key, double def) {
        try { return Double.parseDouble(extractStr(json, key)); } catch (Exception e) { return def; }
    }

    private static int extractInt(String json, String key, int def) {
        try { return Integer.parseInt(extractStr(json, key)); } catch (Exception e) { return def; }
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

    /** Serialize a single element to a compact JSON fragment for copy/paste */
    public static String elementToJson(LayoutElement el) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"").append(esc(el.getClass().getSimpleName())).append("\",");
        sb.append("\"id\":\"").append(esc(el.getId())).append("\",");
        sb.append("\"name\":\"").append(esc(el.getName() != null ? el.getName() : "")).append("\",");
        sb.append("\"xMm\":").append(el.getBoundsMm().x).append(",");
        sb.append("\"yMm\":").append(el.getBoundsMm().y).append(",");
        sb.append("\"wMm\":").append(el.getBoundsMm().width).append(",");
        sb.append("\"hMm\":").append(el.getBoundsMm().height);
        if (el instanceof LayoutLabel) {
            LayoutLabel lab = (LayoutLabel) el;
            sb.append(",\"text\":\"").append(esc(lab.getText())).append("\"");
            sb.append(",\"fontSize\":").append(lab.getFont().getSize2D());
            sb.append(",\"fontFamily\":\"").append(esc(lab.getFont().getFamily())).append("\"");
            sb.append(",\"bold\":").append(lab.getFont().isBold());
            sb.append(",\"color\":").append(colorHex(lab.getColor()));
        }
        if (el instanceof LayoutCartouche) {
            LayoutCartouche lc = (LayoutCartouche) el;
            sb.append(",\"fields\":{");
            boolean first = true;
            for (java.util.Map.Entry<String, String> e : lc.getFields().entrySet()) {
                if (!first) sb.append(","); first = false;
                sb.append("\"").append(esc(e.getKey())).append("\":\"").append(esc(e.getValue())).append("\"");
            }
            sb.append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    /** Deserialize from elementToJson output, with optional position offset */
    public static LayoutElement jsonToElement(String type, String json, double offsetX, double offsetY) {
        double x = extractDouble(json, "xMm") + offsetX;
        double y = extractDouble(json, "yMm") + offsetY;
        double w = extractDouble(json, "wMm");
        double h = extractDouble(json, "hMm");
        String id = extractStr(json, "id") + "-copy";
        String nm = extractStr(json, "name");
        LayoutElement el = null;
        if ("LayoutLabel".equals(type)) {
            String text = extractStr(json, "text");
            el = new LayoutLabel(id, text, x, y, w, h);
            LayoutLabel lab = (LayoutLabel) el;
            double fs = extractDouble(json, "fontSize");
            String ff = extractStr(json, "fontFamily");
            boolean bold = extractBool(json, "bold", false);
            if (fs > 0 && !ff.isEmpty()) lab.setFont(new Font(ff, bold ? Font.BOLD : Font.PLAIN, (int)fs));
            String c = extractStr(json, "color");
            if (!c.isEmpty()) lab.setColor(parseColor(c));
        } else if ("LayoutCartouche".equals(type)) {
            el = new LayoutCartouche(id, x, y, w, h);
            LayoutCartouche lc = (LayoutCartouche) el;
            // parse fields manually
            int fi = json.indexOf("\"fields\"");
            if (fi >= 0) {
                int objStart = json.indexOf('{', fi);
                int objEnd = json.indexOf('}', objStart);
                if (objStart >= 0 && objEnd > objStart) {
                    String fieldsStr = json.substring(objStart + 1, objEnd);
                    String[] pairs = fieldsStr.split(",");
                    for (String pair : pairs) {
                        String[] kv = pair.split(":");
                        if (kv.length == 2) {
                            String key = kv[0].trim().replace("\"", "");
                            String val = kv[1].trim().replace("\"", "");
                            lc.setField(key, val);
                        }
                    }
                }
            }
        } else {
            // Generic: create a LayoutRectangle as fallback
            el = new LayoutRectangle(id, x, y, w, h);
        }
        if (el != null && !nm.isEmpty()) el.setName(nm);
        return el;
    }

    private static Color parseColor(String hex) {
        if (hex == null || hex.isEmpty()) return Color.BLACK;
        try {
            if (hex.startsWith("#")) hex = hex.substring(1);
            return new Color(Integer.parseInt(hex, 16));
        } catch (Exception e) { return Color.BLACK; }
    }

    // ---- New professional templates ----

    private static void buildReferencia(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Referencia de Accesibilidad", 12, 8, 273, 14,
            new Font("SansSerif", Font.BOLD, 16), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Mapa de ubicacion general - Plano de conjunto", 12, 24, 273, 10,
            new Font("SansSerif", Font.PLAIN, 9), new Color(0x5B6778), z);
        addMap(m, "Mapa principal", 12, 36, 273, 130, z);
        addLegend(m, "Leyenda", 12, 172, 130, 40, z, false);
        addScale(m, "Escala grafica", 12, 208, 100, 10, z);
        addNorth(m, "Norte", 265, 155, 16, 16, z);
        LayoutCartouche cc = new LayoutCartouche("Datos cartograficos", 148, 172, 137, 48);
        cc.setZOrder(z[0]++); cc.setName("Datos cartograficos"); m.addElement(cc);
    }

    private static void buildAccesibilidad(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Accesibilidad al Proyecto", 12, 8, 273, 14,
            new Font("SansSerif", Font.BOLD, 16), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Rutas de acceso y caminos principales", 12, 24, 273, 10,
            new Font("SansSerif", Font.PLAIN, 9), new Color(0x5B6778), z);
        addMap(m, "Mapa principal", 12, 36, 200, 135, z);
        addLegend(m, "Leyenda", 218, 36, 67, 40, z, true);
        addScale(m, "Escala grafica", 12, 175, 130, 10, z);
        addNorth(m, "Norte", 270, 160, 16, 16, z);
        LayoutCartouche ac = new LayoutCartouche("Datos cartograficos", 12, 190, 273, 22);
        ac.setZOrder(z[0]++); ac.setName("Datos cartograficos"); m.addElement(ac);
    }

    private static void buildEmplazamiento(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Emplazamiento del Proyecto", 12, 8, 273, 14,
            new Font("SansSerif", Font.BOLD, 16), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Area de proyecto - Planta general", 12, 24, 273, 10,
            new Font("SansSerif", Font.PLAIN, 9), new Color(0x5B6778), z);
        addMap(m, "Mapa principal", 12, 36, 273, 140, z);
        addLegend(m, "Leyenda", 12, 180, 160, 30, z, false);
        addScale(m, "Escala grafica", 175, 180, 110, 10, z);
        addNorth(m, "Norte", 265, 175, 16, 16, z);
        LayoutCartouche ec = new LayoutCartouche("Datos cartograficos", 12, 193, 273, 19);
        ec.setZOrder(z[0]++); ec.setName("Datos cartograficos"); m.addElement(ec);
    }

    private static void buildPerfil(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Perfil de Altimetria", 12, 8, 273, 14,
            new Font("SansSerif", Font.BOLD, 16), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Trazado longitudinal - Progresivas y cotas", 12, 24, 273, 10,
            new Font("SansSerif", Font.PLAIN, 9), new Color(0x5B6778), z);
        addMap(m, "Mapa de traza", 12, 36, 273, 95, z);
        LayoutTable t = new LayoutTable("Tabla progresivas", 12, 134, 273, 55);
        t.setZOrder(z[0]++); t.setName("Progresivas / Altimetria");
        t.setShowBorders(true); t.setAlternateRows(true);
        t.setFirstRowIsHeader(true); t.setMaxVisibleRows(8);
        m.addElement(t);
        addScale(m, "Escala grafica", 12, 192, 130, 10, z);
        addNorth(m, "Norte", 268, 120, 14, 14, z);
        LayoutCartouche pc = new LayoutCartouche("Datos cartograficos", 148, 192, 137, 20);
        pc.setZOrder(z[0]++); pc.setName("Datos cartograficos"); m.addElement(pc);
    }
}
