package ar.com.catgis.sld;

import ar.com.catgis.core.model.Layer;

import java.awt.*;
import java.io.*;

/**
 * SLD (Styled Layer Descriptor) writer for CATGIS layers.
 * Exports basic SLD 1.1.0 styling from layer properties.
 */
public final class SldSupport {

    private SldSupport() {}

    /**
     * Export a layer's basic styling to SLD format.
     */
    public static String exportToSld(Layer layer) {
        if (layer == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<StyledLayerDescriptor version=\"1.1.0\"\n");
        sb.append("  xmlns=\"http://www.opengis.net/sld\"\n");
        sb.append("  xmlns:ogc=\"http://www.opengis.net/ogc\"\n");
        sb.append("  xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
        sb.append("  <NamedLayer>\n");
        sb.append("    <Name>").append(escapeXml(layer.getName())).append("</Name>\n");
        sb.append("    <UserStyle>\n");
        sb.append("      <FeatureTypeStyle>\n");
        sb.append("        <Rule>\n");
        sb.append("          <PointSymbolizer>\n");
        sb.append("            <Fill><CssParameter name=\"fill\">#3388ff</CssParameter></Fill>\n");
        sb.append("            <Mark><WellKnownName>circle</WellKnownName><Size>6</Size></Mark>\n");
        sb.append("          </PointSymbolizer>\n");
        sb.append("          <LineSymbolizer>\n");
        sb.append("            <Stroke><CssParameter name=\"stroke\">#3388ff</CssParameter><CssParameter name=\"stroke-width\">1</CssParameter></Stroke>\n");
        sb.append("          </LineSymbolizer>\n");
        sb.append("          <PolygonSymbolizer>\n");
        sb.append("            <Fill><CssParameter name=\"fill\">#aaddff</CssParameter></Fill>\n");
        sb.append("            <Stroke><CssParameter name=\"stroke\">#3388ff</CssParameter><CssParameter name=\"stroke-width\">1</CssParameter></Stroke>\n");
        sb.append("          </PolygonSymbolizer>\n");
        sb.append("        </Rule>\n");
        sb.append("      </FeatureTypeStyle>\n");
        sb.append("    </UserStyle>\n");
        sb.append("  </NamedLayer>\n");
        sb.append("</StyledLayerDescriptor>\n");
        return sb.toString();
    }

    /**
     * Export SLD to a file.
     */
    public static void exportToFile(Layer layer, File outputFile) throws IOException {
        String sld = exportToSld(layer);
        try (PrintWriter pw = new PrintWriter(new FileWriter(outputFile))) {
            pw.print(sld);
        }
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
