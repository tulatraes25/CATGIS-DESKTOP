package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.*;
import java.awt.Color;
import java.io.*;
import java.util.*;

/**
 * Export layers to DXF R12 ASCII format.
 * Supports POINT, LINE, LWPOLYLINE entities with colors.
 */
public final class DxfExportEngine {

    private DxfExportEngine() {}

    /**
     * Export a layer's features to DXF file.
     */
    public static void exportLayer(File file, Layer layer) throws Exception {
        ShapefileData data = VectorLayerUtils.ensureVectorData(layer);
        if (data == null || data.getFeatureCollection() == null) {
            throw new IllegalArgumentException("No se pudieron cargar los datos de la capa: " + layer.getName());
        }
        Color lineColor = layer.getLineColor() != null ? layer.getLineColor() : Color.BLACK;
        Color fillColor = layer.getFillColor() != null ? layer.getFillColor() : new Color(200, 200, 200);
        Color pointColor = layer.getPointColor() != null ? layer.getPointColor() : Color.RED;
        export(file, data.getFeatureCollection(), layer.getName(), lineColor, fillColor, pointColor);
    }

    /**
     * Full export with per-geometry-type colors.
     */
    public static void export(File file, SimpleFeatureCollection features,
                              String layerName,
                              Color lineColor, Color fillColor, Color pointColor) throws Exception {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), "ISO-8859-1"))) {

            writeHeader(pw);
            writeTables(pw, layerName);
            writeEntities(pw, features, layerName, lineColor, fillColor, pointColor);
            writeFooter(pw);
        }
    }

    /**
     * Overloaded export with graduated/proportional per-feature color extractor.
     * Uses a function to extract the per-feature color (e.g., from graduated symbology).
     */
    public static void exportWithFeatureColors(File file, SimpleFeatureCollection features,
                                                String layerName,
                                                Color defaultLineColor,
                                                Color defaultFillColor,
                                                Color defaultPointColor,
                                                java.util.function.Function<SimpleFeature, Color> colorExtractor) throws Exception {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), "ISO-8859-1"))) {

            writeHeader(pw);
            writeTables(pw, layerName);
            writeEntitiesWithFeatureColors(pw, features, layerName,
                    defaultLineColor, defaultFillColor, defaultPointColor, colorExtractor);
            writeFooter(pw);
        }
    }

    /**
     * Legacy export (single default color for all geometry types).
     */
    @Deprecated
    public static void export(File file, SimpleFeatureCollection features,
                              String layerName, Color defaultColor) throws Exception {
        export(file, features, layerName, defaultColor, defaultColor, defaultColor);
    }

    private static void writeHeader(PrintWriter pw) {
        pw.println("0");
        pw.println("SECTION");
        pw.println("2");
        pw.println("HEADER");
        pw.println("9");
        pw.println("$ACADVER");
        pw.println("1");
        pw.println("AC1006"); // R12
        pw.println("9");
        pw.println("$INSBASE");
        pw.println("10");
        pw.println("0.0");
        pw.println("20");
        pw.println("0.0");
        pw.println("30");
        pw.println("0.0");
        pw.println("9");
        pw.println("$EXTMIN");
        pw.println("10");
        pw.println("-1e20");
        pw.println("20");
        pw.println("-1e20");
        pw.println("9");
        pw.println("$EXTMAX");
        pw.println("10");
        pw.println("1e20");
        pw.println("20");
        pw.println("1e20");
        pw.println("0");
        pw.println("ENDSEC");
    }

    private static void writeTables(PrintWriter pw, String layerName) {
        // LTYPE table
        pw.println("0");
        pw.println("SECTION");
        pw.println("2");
        pw.println("TABLES");
        pw.println("0");
        pw.println("TABLE");
        pw.println("2");
        pw.println("LTYPE");
        pw.println("70");
        pw.println("1");
        pw.println("0");
        pw.println("LTYPE");
        pw.println("2");
        pw.println("CONTINUOUS");
        pw.println("70");
        pw.println("0");
        pw.println("3");
        pw.println("Solid line");
        pw.println("72");
        pw.println("65");
        pw.println("73");
        pw.println("0");
        pw.println("40");
        pw.println("0.0");
        pw.println("0");
        pw.println("ENDTAB");

        // LAYER table
        pw.println("0");
        pw.println("TABLE");
        pw.println("2");
        pw.println("LAYER");
        pw.println("70");
        pw.println("1");
        pw.println("0");
        pw.println("LAYER");
        pw.println("2");
        pw.println(sanitizeLayerName(layerName));
        pw.println("70");
        pw.println("0");      // thawed
        pw.println("62");
        pw.println("7");      // white/black
        pw.println("6");
        pw.println("CONTINUOUS");
        pw.println("0");
        pw.println("ENDTAB");

        pw.println("0");
        pw.println("ENDSEC");
    }

    private static void writeEntities(PrintWriter pw, SimpleFeatureCollection features,
                                      String layerName,
                                      Color lineColor, Color fillColor, Color pointColor) throws Exception {
        writeEntitiesWithFeatureColors(pw, features, layerName, lineColor, fillColor, pointColor, null);
    }

    private static void writeEntitiesWithFeatureColors(PrintWriter pw, SimpleFeatureCollection features,
                                                        String layerName,
                                                        Color defaultLineColor, Color defaultFillColor,
                                                        Color defaultPointColor,
                                                        java.util.function.Function<SimpleFeature, Color> colorExtractor) throws Exception {
        pw.println("0");
        pw.println("SECTION");
        pw.println("2");
        pw.println("ENTITIES");

        String safeLayer = sanitizeLayerName(layerName);

        try (FeatureIterator<SimpleFeature> it = features.features()) {
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                Object geomObj = f.getDefaultGeometry();
                if (!(geomObj instanceof Geometry geom) || geom.isEmpty()) continue;

                // Per-feature color override (for graduated/proportional symbology)
                Color featureColor = colorExtractor != null ? colorExtractor.apply(f) : null;

                String geomType = geom.getGeometryType().toUpperCase();

                switch (geomType) {
                    case "POINT":
                        writePoint(pw, (Point) geom, safeLayer,
                                featureColor != null ? featureColor : defaultPointColor);
                        break;
                    case "MULTIPOINT":
                        for (int i = 0; i < geom.getNumGeometries(); i++) {
                            writePoint(pw, (Point) geom.getGeometryN(i), safeLayer,
                                    featureColor != null ? featureColor : defaultPointColor);
                        }
                        break;
                    case "LINESTRING":
                        writePolyline(pw, (LineString) geom, safeLayer,
                                featureColor != null ? featureColor : defaultLineColor, false);
                        break;
                    case "MULTILINESTRING":
                        MultiLineString mls = (MultiLineString) geom;
                        for (int i = 0; i < mls.getNumGeometries(); i++) {
                            writePolyline(pw, (LineString) mls.getGeometryN(i), safeLayer,
                                    featureColor != null ? featureColor : defaultLineColor, false);
                        }
                        break;
                    case "POLYGON":
                        writePolygon(pw, (Polygon) geom, safeLayer,
                                featureColor != null ? featureColor : defaultLineColor,
                                featureColor != null ? featureColor : defaultFillColor);
                        break;
                    case "MULTIPOLYGON":
                        MultiPolygon mp = (MultiPolygon) geom;
                        for (int i = 0; i < mp.getNumGeometries(); i++) {
                            writePolygon(pw, (Polygon) mp.getGeometryN(i), safeLayer,
                                    featureColor != null ? featureColor : defaultLineColor,
                                    featureColor != null ? featureColor : defaultFillColor);
                        }
                        break;
                }
            }
        }

        pw.println("0");
        pw.println("ENDSEC");
    }

    private static void writePoint(PrintWriter pw, Point pt, String layer, Color color) {
        pw.println("0");
        pw.println("POINT");
        pw.println("8");
        pw.println(layer);
        pw.println("62");
        pw.println(rgbToAci(color));
        pw.println("10");
        pw.println(formatCoord(pt.getX()));
        pw.println("20");
        pw.println(formatCoord(pt.getY()));
        pw.println("30");
        pw.println("0.0");
    }

    private static void writePolyline(PrintWriter pw, LineString line, String layer,
                                       Color color, boolean closed) throws Exception {
        Coordinate[] coords = line.getCoordinates();
        if (coords.length < 2) return;

        pw.println("0");
        pw.println("LWPOLYLINE");
        pw.println("8");
        pw.println(layer);
        pw.println("62");
        pw.println(rgbToAci(color));
        pw.println("90");
        pw.println(coords.length);
        pw.println("70");
        pw.println(closed ? "1" : "0");
        pw.println("43");
        pw.println("0.0"); // constant width

        for (Coordinate c : coords) {
            pw.println("10");
            pw.println(formatCoord(c.x));
            pw.println("20");
            pw.println(formatCoord(c.y));
        }
    }

    private static void writePolygon(PrintWriter pw, Polygon polygon, String layer, Color color) throws Exception {
        writePolygon(pw, polygon, layer, color, color);
    }

    private static void writePolygon(PrintWriter pw, Polygon polygon, String layer,
                                      Color lineColor, Color fillColor) throws Exception {
        // Exterior ring as closed polyline
        LineString shell = polygon.getExteriorRing();
        writePolyline(pw, shell, layer, lineColor, true);

        // Solid fill using SOLID entity (R12 compatible)
        Coordinate[] coords = shell.getCoordinates();
        if (coords.length >= 3) {
            pw.println("0");
            pw.println("SOLID");
            pw.println("8");
            pw.println(layer);
            pw.println("62");
            pw.println(rgbToAci(fillColor));
            // SOLID uses up to 4 corners; use first 3 for a triangle fill
            pw.println("10");
            pw.println(formatCoord(coords[0].x));
            pw.println("20");
            pw.println(formatCoord(coords[0].y));
            pw.println("11");
            pw.println(formatCoord(coords[1].x));
            pw.println("21");
            pw.println(formatCoord(coords[1].y));
            pw.println("12");
            pw.println(formatCoord(coords[2].x));
            pw.println("22");
            pw.println(formatCoord(coords[2].y));
            pw.println("13");
            pw.println(formatCoord(coords[2].x));
            pw.println("23");
            pw.println(formatCoord(coords[2].y));
        }

        // Interior rings (holes) - outline only
        for (int r = 0; r < polygon.getNumInteriorRing(); r++) {
            LineString hole = polygon.getInteriorRingN(r);
            writePolyline(pw, hole, layer, lineColor, true);
        }
    }

    private static void writeFooter(PrintWriter pw) {
        pw.println("0");
        pw.println("EOF");
    }

    // ========== Helpers ==========

    private static String sanitizeLayerName(String name) {
        if (name == null) return "0";
        // DXF layer names: max 31 chars, no special chars
        String clean = name.replaceAll("[^a-zA-Z0-9_\\- ]", "_");
        return clean.length() > 31 ? clean.substring(0, 31) : clean;
    }

    private static String formatCoord(double v) {
        // DXF uses '.' as decimal separator regardless of locale
        return String.format(Locale.US, "%.6f", v);
    }

    /**
     * Convert Java AWT Color to DXF ACI (AutoCAD Color Index).
     * Maps to the closest of 255 standard ACI colors.
     * Simple mapping: uses RGB intensity to pick from basic colors.
     */
    private static int rgbToAci(Color color) {
        if (color == null) return 7; // white/black

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        // Basic ACI colors (1-9)
        if (r == 0 && g == 0 && b == 0) return 7;   // black
        if (r == 255 && g == 0 && b == 0) return 1;  // red
        if (r == 255 && g == 255 && b == 0) return 2; // yellow
        if (r == 0 && g == 255 && b == 0) return 3;  // green
        if (r == 0 && g == 255 && b == 255) return 4; // cyan
        if (r == 0 && g == 0 && b == 255) return 5;  // blue
        if (r == 255 && g == 0 && b == 255) return 6; // magenta
        if (r == 255 && g == 255 && b == 255) return 7; // white
        if (r >= 190 && g >= 190 && b >= 190) return 9; // light gray

        // Use the hue to map to ACI 10-249
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        int hueIdx = Math.round(hsb[0] * 30f) % 30;
        int satIdx = Math.round(hsb[1] * 5f);
        int valIdx = Math.round(hsb[2] * 5f);

        // ACI 10-249: 6 saturation levels × 30 hues, 5 value levels
        int aci = 10 + (satIdx * 30 + hueIdx) % 240;
        return Math.max(1, Math.min(255, aci));
    }

    /**
     * Export a layer to DXF via file chooser dialog.
     */
    public static void exportLayerWithDialog(java.awt.Component parent, Layer layer) {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle("Exportar capa a DXF");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("DXF (*.dxf)", "dxf"));
        chooser.setSelectedFile(new java.io.File(layer.getName() + ".dxf"));

        if (chooser.showSaveDialog(parent) == javax.swing.JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".dxf")) {
                file = new File(file.getAbsolutePath() + ".dxf");
            }
            try {
                exportLayer(file, layer);
                NotificationManager.info(parent,
                    "Exportar DXF",
                    "Exportado a DXF correctamente:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                NotificationManager.error(parent,
                    "Error",
                    "Error al exportar DXF:\n" + ex.getMessage());
            }
        }
    }
}
