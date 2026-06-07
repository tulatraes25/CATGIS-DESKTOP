package ar.com.catgis;

import java.awt.Color;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Serializes a Project to .catgis text format.
 * Extracted from SaveProjectAction for testability and reuse.
 */
public final class ProjectSerializer {

    private ProjectSerializer() {}

    public static void serialize(Project project, File file) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            w.write("# CATGIS Project v1");
            w.newLine();
            w.write("# Layers section");
            w.newLine();
            for (Layer layer : project.getLayers()) {
                if (layer == null) continue;
                w.write(formatLayer(layer));
                w.newLine();
            }
            w.write("# Legend section");
            w.newLine();
            w.write("# End of project data");
            w.newLine();
        }
    }

    private static String formatLayer(Layer layer) {
        StringBuilder sb = new StringBuilder();
        String path = layer.getPath() != null ? layer.getPath() : "";
        String name = safe(layer.getName());
        sb.append("LAYER|").append(name).append("|").append(path).append("|");
        sb.append(layer.getPointColor() != null ? colorHex(layer.getPointColor()) : "0,0,255").append("|");
        sb.append(layer.getLineColor() != null ? colorHex(layer.getLineColor()) : "255,0,0").append("|");
        sb.append(layer.getFillColor() != null ? colorHex(layer.getFillColor()) : "200,200,255").append("|");
        sb.append(layer.getLineWidth()).append("|");
        sb.append(layer.getPointSize()).append("|");
        sb.append(layer.isVisible()).append("|");
        sb.append(layer.getOpacity()).append("|");
        sb.append(layer.getPointSymbolStyle().name()).append("|");
        sb.append(layer.getCatalogSymbolId() != null ? layer.getCatalogSymbolId() : "").append("|");
        // Extended fields: heatmap, clustering, proportional
        sb.append(layer.isHeatmapEnabled()).append("|");
        sb.append(layer.getHeatmapRadius()).append("|");
        sb.append(layer.getHeatmapOpacity()).append("|");
        sb.append(layer.isClusteringEnabled()).append("|");
        sb.append(layer.getClusterRadius()).append("|");
        ProportionalSymbols ps = layer.getProportionalSymbols();
        sb.append(ps != null && ps.getFieldName() != null ? safe(ps.getFieldName()) : "").append("|");
        sb.append(ps != null ? ps.getMinSize() : 4).append("|");
        sb.append(ps != null ? ps.getMaxSize() : 40).append("|");
        sb.append(ps != null ? ps.isScaleByArea() : false).append("|");
        sb.append(ps != null && ps.isEnabled());
        // Label fields
        sb.append("|").append(layer.isLabelsVisible());
        sb.append("|").append(safe(layer.getLabelField()));
        sb.append("|").append(safe(layer.getLabelExpression()));
        sb.append("|").append(safe(layer.getLabelFontFamily()));
        sb.append("|").append(layer.getLabelFontSize());
        sb.append("|").append(layer.isLabelBold());
        sb.append("|").append(layer.isLabelItalic());
        sb.append("|").append(layer.isLabelUnderline());
        sb.append("|").append(colorHex(layer.getLabelColor()));
        sb.append("|").append(layer.isLabelHaloEnabled());
        sb.append("|").append(colorHex(layer.getLabelHaloColor()));
        sb.append("|").append(layer.getLabelHaloWidth());
        sb.append("|").append(layer.getLabelOffsetX());
        sb.append("|").append(layer.getLabelOffsetY());
        sb.append("|").append(layer.getLabelPlacementMode().name());
        sb.append("|").append(layer.getLabelPriority());
        sb.append("|").append(layer.isLabelCollisionAvoid());
        sb.append("|").append(layer.isLabelBackgroundEnabled());
        sb.append("|").append(colorHex(layer.getLabelBackgroundColor()));
        sb.append("|").append(layer.getLabelMinScale());
        sb.append("|").append(layer.getLabelMaxScale());
        return sb.toString();
    }

    private static String colorHex(Color c) {
        return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
    }

    private static String safe(String s) { return s != null ? s : ""; }
}
