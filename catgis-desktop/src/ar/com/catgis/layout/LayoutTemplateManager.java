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
        for (TemplateRegistry.Entry e : TemplateRegistry.getAll()) {
            list.put(e.key, e.displayName);
        }
        return list;
    }

    public static void applyTemplate(String key, LayoutModel model) {
        model.clearSelection();
        java.util.List<LayoutElement> rm = new java.util.ArrayList<>(model.getElements());
        for (LayoutElement e : rm) model.removeElement(e.getId());
        internalApply(key, model);
        // Auto-populate legend from project layers
        for (LayoutElement el : model.getElements()) {
            if (el instanceof LayoutLegend && ((LayoutLegend)el).getItems().isEmpty()) {
                ar.com.catgis.Project p = ar.com.catgis.AppContext.get().getProject();
                if (p == null) p = ar.com.catgis.CatgisDesktopApp.currentProject;
                if (p != null && p.getLayers() != null) {
                    for (ar.com.catgis.Layer l : p.getLayers()) {
                        if (l == null || !l.isVisible() || LayoutLegend.isBasemapName(l.getName())) continue;
                        java.awt.Color c = resolveColor(l);
                        ((LayoutLegend)el).getItems().add(new LayoutLegend.LegendItem(l.getName(), c, resolveType(l)));
                    }
                }
            }
        }
    }

    private static java.awt.Color resolveColor(ar.com.catgis.Layer l) {
        if (l.getPointColor() != null && !l.getPointColor().equals(java.awt.Color.BLUE)) return l.getPointColor();
        if (l.getLineColor() != null && !l.getLineColor().equals(java.awt.Color.RED)) return l.getLineColor();
        if (l.getFillColor() != null) return l.getFillColor();
        return new java.awt.Color(0x1976D2);
    }

    private static String resolveType(ar.com.catgis.Layer l) {
        try {
            ar.com.catgis.MapPanel mp = ar.com.catgis.CatgisDesktopApp.mapPanel;
            if (mp != null) {
                ar.com.catgis.ShapefileData d = mp.getShapefileData(l);
                if (d != null) { String f = ar.com.catgis.VectorLayerUtils.resolveGeometryFamily(d); if (f != null) return f; }
            }
        } catch (Exception ignored) {}
        return "VECTOR";
    }

    private static void internalApply(String key, LayoutModel model) {

        // Existing templates
        switch (key) {
            case "A4_AMBIENTAL": buildAmbiental(model); return;
            case "A4_TECNICO": buildTecnico(model); return;
            case "A4_TECNICO_INFERIOR": buildTecnicoInferior(model); return;
            case "A4_CATASTRAL": buildCatastralA4(model); return;
            case "A4_HIDROLOGIA": buildHidrologiaA4(model); return;
            case "A4_TOPOGRAFIA": buildTopografiaA4(model); return;
            case "A4_URBANO": buildUrbanoA4(model); return;
            case "A4_PARCELARIO": buildParcelarioA4(model); return;
            case "A4_INFRAESTRUCTURA": buildInfraestructuraA4(model); return;
            case "A4_VERTICAL": buildVertical(model); return;
            case "A4_MUESTREO": buildMuestreo(model); return;
            case "A4_SATELITAL": buildSatelital(model); return;
            case "A4_REFERENCIA": buildReferencia(model); return;
            case "A4_ACCESIBILIDAD": buildAccesibilidad(model); return;
            case "A4_EMPLAZAMIENTO": buildEmplazamiento(model); return;
            case "A4_PERFIL": buildPerfil(model); return;
            case "A3_TECNICO": buildA3Tecnico(model); return;
            case "A3_AMBIENTAL": buildA3Ambiental(model); return;
            case "A3_CATASTRAL": buildA3Catastral(model); return;
            case "A3_SATELITAL": buildA3Satelital(model); return;
            case "A3_PARCELARIO": buildA3Parcelario(model); return;
            case "A3_HIDROLOGIA": buildA3Hidrologia(model); return;
            case "A3_TOPOGRAFIA": buildA3Topografia(model); return;
            case "A3_PRESENTACION": buildA3Presentacion(model); return;
        }

        // Parametric templates - auto-build from key pattern
        TemplateRegistry.Entry entry = TemplateRegistry.get(key);
        if (entry == null) { buildDefault(model, 297, 210); return; }

        String cat = entry.category.name();
        boolean isA3 = key.startsWith("A3_");
        double w = isA3 ? 420 : 297, h = isA3 ? 297 : 210; // A3 landscape
        if (key.contains("VERTICAL")) { double t = w; w = h; h = t; }

        // Determine layout style from key
        boolean showSubtitle = !key.contains("LIMPIA") && !key.contains("MINIMO");
        boolean legendRight = key.contains("DERECHA") || key.contains("LEYENDA_LATERAL");
        boolean legendBelow = key.contains("INFERIOR");
        boolean hasCartouche = !key.contains("LIMPIA") && !key.contains("MINIMO");
        boolean hasTable = key.contains("TABLA") || key.contains("PARCELARIO") || key.contains("PROGRESIVAS") || key.contains("PUNTOS");
        boolean isSatelital = cat.equals("SATELITALES");
        boolean isHidrologia = cat.equals("HIDROLOGIA");
        boolean isTopografia = cat.equals("TOPOGRAFIA");
        boolean isInstitucional = cat.equals("INSTITUCIONAL");
        boolean isCatastral = cat.equals("CATASTRALES");
        boolean isReferencia = cat.equals("REFERENCIA");
        boolean hasInset = key.contains("INSET") || key.contains("MAPA_UBICACION") || key.contains("DOBLE_MAPA");

        buildParametric(model, w, h, entry.displayName, showSubtitle, legendRight, legendBelow,
            hasCartouche, hasTable, isSatelital, isHidrologia, isTopografia, isInstitucional, isCatastral,
            isReferencia, hasInset);
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

    private static void buildDefault(LayoutModel m, double w, double h) {
        int[] z = {0};
        addLabel(m, "Titulo", "Mapa", 12, 8, (int)w - 24, 14, new Font("SansSerif", Font.BOLD, 16), new Color(0x1A2434), z);
        addMap(m, "Mapa principal", 12, 24, (int)w - 24, (int)h - 50, z);
        addScale(m, "Escala", 12, (int)h - 22, 100, 10, z);
        addNorth(m, "Norte", (int)w - 30, (int)h - 50, 16, 16, z);
    }

    private static void buildParametric(LayoutModel m, double w, double h, String titleText,
            boolean subtitle, boolean legendRight, boolean legendBelow,
            boolean cartouche, boolean table, boolean satelital, boolean hidrologia,
            boolean topografia, boolean institucional, boolean catastral, boolean referencia,
            boolean inset) {
        int[] z = {0};
        Font titleFont = institucional ? new Font("SansSerif", Font.BOLD, 24) : new Font("SansSerif", Font.BOLD, 18);
        Color titleColor = hidrologia ? new Color(0x0D47A1) : (satelital ? Color.WHITE : new Color(0x1A2434));
        int margin = 12, titleH = institucional ? 22 : 16, subH = subtitle ? 10 : 0;
        int titleY = satelital ? (int)h - 30 : 8;
        if (satelital) margin = 8;

        addLabel(m, "Titulo", titleText, margin, titleY, (int)w - margin*2, titleH, titleFont, titleColor, z);
        if (subtitle) {
            addLabel(m, "Subtitulo", "Salida cartografica profesional", margin, titleY + titleH + 2,
                (int)w - margin*2, subH, new Font("SansSerif", Font.PLAIN, 9), new Color(0x5B6778), z);
        }
        int contentTop = satelital ? margin : (titleY + titleH + subH + 4);

        int mapW = (int)w - margin*2, mapH;
        int legendW = 0, legendX = 0;

        if (legendRight) { legendW = Math.min(100, (int)w/3); mapW = (int)w - margin*2 - legendW - 4; legendX = margin + mapW + 4; }
        if (inset) { mapW = (int)(mapW * 0.7); }

        int footerH = 50;
        if (cartouche) footerH += 24;
        if (table) footerH += 40;
        mapH = (int)h - contentTop - footerH - 4;

        addMap(m, "Mapa principal", margin, contentTop, mapW, Math.max(20, mapH), z);
        if (legendRight && legendW > 0) {
            addLegend(m, "Leyenda", legendX, contentTop, legendW, Math.max(20, mapH), z, !satelital);
        } else if (legendBelow) {
            addLegend(m, "Leyenda", margin, contentTop + mapH + 2, (int)w - margin*2, 20, z, false);
        }

        if (inset) {
            int insetW = (int)w - margin*2 - mapW - 4;
            addMap(m, "Mapa ubicacion", margin + mapW + 4, contentTop, insetW, mapH/2, z);
        }

        int footY = contentTop + mapH + 4;
        if (legendBelow) footY += 24;

        addScale(m, "Escala grafica", margin, footY, 120, 10, z);
        addNorth(m, "Norte", (int)w - 30, contentTop + 4, 16, 16, z);

        if (cartouche) {
            LayoutCartouche c = new LayoutCartouche("Datos", margin, footY + 12, (int)w - margin*2, 28);
            c.setZOrder(z[0]++); c.setName("Datos cartograficos"); m.addElement(c);
            footY += 32;
        }
        if (table) {
            LayoutTable t = new LayoutTable("Tabla", margin, footY, (int)w - margin*2, 30);
            t.setZOrder(z[0]++); t.setShowBorders(true); t.setAlternateRows(true);
            t.setMaxVisibleRows(5); t.setName("Datos"); m.addElement(t);
        }
    }

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

    // ---- A4: Tecnico Inferior ----
    private static void buildTecnicoInferior(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Mapa Tecnico", 12, 8, 273, 14, new Font("SansSerif", Font.BOLD, 18), new Color(0x1A2434), z);
        addMap(m, "Mapa principal", 12, 24, 273, 148, z);
        addLegend(m, "Leyenda", 12, 176, 273, 20, z, false);
        addScale(m, "Escala", 12, 198, 130, 10, z);
        addNorth(m, "Norte", 270, 190, 14, 14, z);
    }

    // ---- A4: Catastral ----
    private static void buildCatastralA4(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Plano Catastral", 12, 8, 273, 14, new Font("SansSerif", Font.BOLD, 16), new Color(0x1A2434), z);
        addMap(m, "Plano catastral", 12, 24, 210, 155, z);
        addLegend(m, "Leyenda", 228, 24, 57, 40, z, true);
        addScale(m, "Escala", 12, 184, 130, 10, z);
        addNorth(m, "Norte", 270, 168, 14, 14, z);
    }

    // ---- A4: Hidrologia ----
    private static void buildHidrologiaA4(LayoutModel m) { int[] z = {0};
        Color wb = new Color(0x0D47A1);
        addLabel(m, "Titulo", "Mapa Hidrologico", 12, 8, 273, 14, new Font("SansSerif", Font.BOLD, 16), wb, z);
        addMap(m, "Mapa de cuencas", 12, 24, 273, 148, z);
        addLegend(m, "Leyenda", 12, 176, 160, 30, z, false);
        addScale(m, "Escala", 175, 176, 110, 10, z);
        addNorth(m, "Norte", 268, 168, 14, 14, z);
    }

    // ---- A4: Topografia ----
    private static void buildTopografiaA4(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Plano Topografico", 12, 8, 273, 14, new Font("SansSerif", Font.BOLD, 16), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Curvas de nivel", 12, 24, 273, 9, new Font("SansSerif", Font.PLAIN, 8), new Color(0x5B6778), z);
        addMap(m, "Mapa topografico", 12, 34, 273, 138, z);
        addLegend(m, "Leyenda", 12, 176, 150, 30, z, false);
        addScale(m, "Escala", 165, 176, 120, 10, z);
        addNorth(m, "Norte", 268, 166, 14, 14, z);
    }

    // ---- A4: Urbano ----
    private static void buildUrbanoA4(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Plano Urbano", 12, 8, 273, 14, new Font("SansSerif", Font.BOLD, 16), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Zonificacion y equipamiento", 12, 24, 273, 9, new Font("SansSerif", Font.PLAIN, 8), new Color(0x5B6778), z);
        addMap(m, "Mapa urbano", 12, 34, 200, 140, z);
        addLegend(m, "Zonificacion", 218, 34, 67, 40, z, true);
        addScale(m, "Escala", 12, 178, 130, 10, z);
        addNorth(m, "Norte", 270, 168, 14, 14, z);
    }

    // ---- A4: Parcelario ----
    private static void buildParcelarioA4(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Plano Parcelario", 12, 8, 273, 14, new Font("SansSerif", Font.BOLD, 16), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Nomenclatura catastral", 12, 24, 273, 9, new Font("SansSerif", Font.PLAIN, 8), new Color(0x5B6778), z);
        addMap(m, "Mapa parcelario", 12, 34, 273, 128, z);
        LayoutTable t1 = new LayoutTable("Tabla parcelas", 12, 166, 273, 28); t1.setZOrder(z[0]++);
        t1.setShowBorders(true); t1.setAlternateRows(true); t1.setMaxVisibleRows(4); t1.setName("Parcelas"); m.addElement(t1);
        addScale(m, "Escala", 12, 196, 130, 10, z);
        addNorth(m, "Norte", 268, 158, 14, 14, z);
    }

    // ---- A4: Infraestructura ----
    private static void buildInfraestructuraA4(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Plano de Infraestructura", 12, 8, 273, 14, new Font("SansSerif", Font.BOLD, 16), new Color(0x1A2434), z);
        addMap(m, "Mapa infraestructura", 12, 24, 273, 140, z);
        addLegend(m, "Infraestructura", 12, 168, 160, 30, z, false);
        addScale(m, "Escala", 175, 168, 110, 10, z);
        addNorth(m, "Norte", 268, 158, 14, 14, z);
    }

    // ---- A3: Ambiental ----
    private static void buildA3Ambiental(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Mapa Ambiental A3", 20, 10, 380, 18, new Font("SansSerif", Font.BOLD, 22), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Estudio de impacto ambiental", 20, 30, 380, 10, new Font("SansSerif", Font.PLAIN, 10), new Color(0x5B6778), z);
        addMap(m, "Mapa principal", 20, 42, 380, 228, z);
        addLegend(m, "Leyenda", 20, 276, 200, 40, z, false);
        addScale(m, "Escala", 230, 276, 170, 10, z);
        addNorth(m, "Norte", 375, 258, 20, 20, z);
    }

    // ---- A3: Catastral ----
    private static void buildA3Catastral(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Plano Catastral A3", 20, 10, 380, 18, new Font("SansSerif", Font.BOLD, 22), new Color(0x1A2434), z);
        addMap(m, "Plano catastral", 20, 32, 300, 238, z);
        addLegend(m, "Nomenclatura", 328, 32, 72, 40, z, true);
        addScale(m, "Escala", 20, 276, 180, 10, z);
        addNorth(m, "Norte", 375, 258, 18, 18, z);
    }

    // ---- A3: Satelital ----
    private static void buildA3Satelital(LayoutModel m) { int[] z = {0};
        addMap(m, "Imagen satelital", 20, 8, 380, 276, z);
        addLabel(m, "Titulo", "Imagen Satelital A3", 20, 288, 250, 14, new Font("SansSerif", Font.BOLD, 16), Color.WHITE, z);
        addScale(m, "Escala", 280, 288, 120, 10, z);
        addNorth(m, "Norte", 375, 8, 20, 20, z);
    }

    // ---- A3: Parcelario ----
    private static void buildA3Parcelario(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Plano Parcelario A3", 20, 10, 380, 18, new Font("SansSerif", Font.BOLD, 22), new Color(0x1A2434), z);
        addMap(m, "Mapa parcelario", 20, 32, 380, 208, z);
        LayoutTable t2 = new LayoutTable("Tabla", 20, 244, 380, 40); t2.setZOrder(z[0]++);
        t2.setShowBorders(true); t2.setAlternateRows(true); t2.setMaxVisibleRows(6); t2.setName("Parcelas"); m.addElement(t2);
        addScale(m, "Escala", 20, 288, 200, 10, z);
        addNorth(m, "Norte", 375, 238, 18, 18, z);
    }

    // ---- A3: Hidrologia ----
    private static void buildA3Hidrologia(LayoutModel m) { int[] z = {0};
        Color wb2 = new Color(0x0D47A1);
        addLabel(m, "Titulo", "Mapa Hidrologico A3", 20, 10, 380, 18, new Font("SansSerif", Font.BOLD, 22), wb2, z);
        addLabel(m, "Subtitulo", "Cuencas y red de drenaje", 20, 30, 380, 10, new Font("SansSerif", Font.PLAIN, 10), new Color(0x5B6778), z);
        addMap(m, "Mapa de cuencas", 20, 42, 380, 228, z);
        addLegend(m, "Hidrologia", 20, 276, 200, 40, z, false);
        addScale(m, "Escala", 230, 276, 170, 10, z);
        addNorth(m, "Norte", 375, 258, 18, 18, z);
    }

    // ---- A3: Topografia ----
    private static void buildA3Topografia(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "Plano Topografico A3", 20, 10, 380, 18, new Font("SansSerif", Font.BOLD, 22), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Curvas de nivel y relieve", 20, 30, 380, 10, new Font("SansSerif", Font.PLAIN, 10), new Color(0x5B6778), z);
        addMap(m, "Mapa topografico", 20, 42, 260, 228, z);
        addLegend(m, "Altimetria", 288, 42, 112, 40, z, true);
        addScale(m, "Escala", 20, 276, 180, 10, z);
        addNorth(m, "Norte", 375, 258, 18, 18, z);
    }

    // ---- A3: Presentacion ----
    private static void buildA3Presentacion(LayoutModel m) { int[] z = {0};
        addLabel(m, "Titulo", "CATGIS Desktop", 20, 10, 380, 22, new Font("SansSerif", Font.BOLD, 28), new Color(0x1A2434), z);
        addLabel(m, "Subtitulo", "Salida cartografica profesional", 20, 36, 380, 12, new Font("SansSerif", Font.PLAIN, 12), new Color(0x5B6778), z);
        addMap(m, "Mapa principal", 20, 54, 380, 216, z);
        addLegend(m, "Leyenda", 20, 274, 200, 40, z, true);
        addScale(m, "Escala", 230, 274, 170, 10, z);
        addNorth(m, "Norte", 375, 256, 20, 20, z);
    }
}
