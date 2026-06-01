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
        sb.append(layer.getCatalogSymbolId() != null ? layer.getCatalogSymbolId() : "");
        return sb.toString();
    }

    private static String colorHex(Color c) {
        return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
    }

    private static String safe(String s) { return s != null ? s : ""; }
}
