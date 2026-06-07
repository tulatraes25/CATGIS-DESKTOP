package ar.com.catgis;

import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class KmlExportEngine {

    private KmlExportEngine() {}

    public static void exportLayerWithDialog(Component parent, Layer layer) {
        if (layer == null) return;
        ar.com.catgis.data.vector.ShapefileData data = ar.com.catgis.data.vector.VectorLayerUtils.ensureVectorData(layer);
        if (data == null || data.getFeatures() == null || data.getFeatures().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "La capa no tiene entidades para exportar.", "KML Export", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar KML");
        chooser.setSelectedFile(new File(layer.getName().replaceAll("[^A-Za-z0-9]+", "_") + ".kml"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        try {
            exportToKml(chooser.getSelectedFile(), data.getFeatures(), layer.getName());
            JOptionPane.showMessageDialog(parent, "Exportado exitosamente.", "KML Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            AppErrorSupport.showErrorDialog(parent, "KML Export", "Error al exportar KML.", e);
        }
    }

    public static void exportToKml(File file, List<SimpleFeature> features, String layerName) throws IOException {
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            w.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
            w.println("<Document>");
            w.println("<name>" + escapeXml(layerName) + "</name>");
            w.println("<Folder>");
            w.println("<name>" + escapeXml(layerName) + "</name>");
            int idx = 1;
            for (SimpleFeature feature : features) {
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                if (geom == null || geom.isEmpty()) continue;
                writeFeature(w, geom, feature.getID(), idx++);
            }
            w.println("</Folder>");
            w.println("</Document>");
            w.println("</kml>");
        }
    }

    private static void writeFeature(PrintWriter w, org.locationtech.jts.geom.Geometry geom, String id, int idx) {
        w.println("<Placemark>");
        w.println("<name>" + escapeXml(id != null ? id : "Feature " + idx) + "</name>");
        w.println("<description>Feature " + idx + "</description>");
        writeGeometry(w, geom);
        w.println("</Placemark>");
    }

    private static void writeGeometry(PrintWriter w, org.locationtech.jts.geom.Geometry geom) {
        if (geom instanceof org.locationtech.jts.geom.Point) {
            w.println("<Point><coordinates>" + formatCoord(geom.getCoordinate()) + "</coordinates></Point>");
        } else if (geom instanceof org.locationtech.jts.geom.LineString) {
            w.println("<LineString><coordinates>" + formatCoordinates(geom.getCoordinates()) + "</coordinates></LineString>");
        } else if (geom instanceof org.locationtech.jts.geom.Polygon poly) {
            w.println("<Polygon>");
            w.println("<outerBoundaryIs><LinearRing><coordinates>" + formatCoordinates(poly.getExteriorRing().getCoordinates()) + "</coordinates></LinearRing></outerBoundaryIs>");
            for (int i = 0; i < poly.getNumInteriorRing(); i++) {
                w.println("<innerBoundaryIs><LinearRing><coordinates>" + formatCoordinates(poly.getInteriorRingN(i).getCoordinates()) + "</coordinates></LinearRing></innerBoundaryIs>");
            }
            w.println("</Polygon>");
        } else if (geom instanceof org.locationtech.jts.geom.MultiPoint || geom instanceof org.locationtech.jts.geom.MultiLineString || geom instanceof org.locationtech.jts.geom.MultiPolygon) {
            w.println("<MultiGeometry>");
            for (int i = 0; i < geom.getNumGeometries(); i++) writeGeometry(w, geom.getGeometryN(i));
            w.println("</MultiGeometry>");
        } else {
            w.println("<LineString><coordinates>" + formatCoordinates(geom.getCoordinates()) + "</coordinates></LineString>");
        }
    }

    private static String formatCoord(Coordinate c) {
        return String.format(java.util.Locale.ROOT, "%.8f,%.8f,0", c.getX(), c.getY());
    }

    private static String formatCoordinates(Coordinate[] coords) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < coords.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(formatCoord(coords[i]));
        }
        return sb.toString();
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}
