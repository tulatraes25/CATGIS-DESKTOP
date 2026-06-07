package ar.com.catgis;

import java.awt.Color;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Deserializes a Project from .catgis text format.
 * Extracted from LoadProjectAction for testability and reuse.
 */
public final class ProjectDeserializer {

    public static class Result {
        public final Project project;
        public final List<String> warnings = new ArrayList<>();

        Result(Project p) { this.project = p; }
    }

    private ProjectDeserializer() {}

    public static Result deserialize(File file) throws IOException {
        Project project = new Project(file.getName().replace(".catgis", ""));
        List<String> warnings = new ArrayList<>();
        List<String> lines = new ArrayList<>();

        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                lines.add(trimmed);
            }
        }

        for (String line : lines) {
            if (line.startsWith("LAYER|")) {
                try {
                    Layer layer = parseLayer(line);
                    if (layer != null) project.addLayer(layer);
                } catch (Exception e) {
                    warnings.add("Linea de capa invalida: " + line.substring(0, Math.min(80, line.length())));
                }
            }
        }

        Result result = new Result(project);
        result.warnings.addAll(warnings);
        return result;
    }

    private static Layer parseLayer(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 10) return null;
        String name = parts[1].trim();
        String path = parts[2].trim();
        Layer layer = new Layer(name, path, "VECTOR");
        if (!path.isEmpty()) layer.setPath(path);

        try { layer.setPointColor(parseColor(parts[3])); } catch (Exception ignored) { /* fallback value */ }
        try { layer.setLineColor(parseColor(parts[4])); } catch (Exception ignored) { /* fallback value */ }
        try { layer.setFillColor(parseColor(parts[5])); } catch (Exception ignored) { /* fallback value */ }
        try { layer.setLineWidth(Float.parseFloat(parts[6])); } catch (Exception ignored) { /* fallback value */ }
        try { layer.setPointSize(Integer.parseInt(parts[7])); } catch (Exception ignored) { /* fallback value */ }
        try { layer.setVisible(Boolean.parseBoolean(parts[8])); } catch (Exception ignored) { /* fallback value */ }
        try { layer.setOpacity(Float.parseFloat(parts[9])); } catch (Exception ignored) { /* fallback value */ }
        if (parts.length > 10) {
            try { layer.setPointSymbolStyle(Layer.PointSymbolStyle.fromValue(parts[10])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 11) {
            layer.setCatalogSymbolId(parts[11].trim());
        }
        // Extended fields (v2): heatmap, clustering, proportional
        if (parts.length > 12) {
            try { layer.setHeatmapEnabled(Boolean.parseBoolean(parts[12])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 13) {
            try { layer.setHeatmapRadius(Integer.parseInt(parts[13])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 14) {
            try { layer.setHeatmapOpacity(Float.parseFloat(parts[14])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 15) {
            try { layer.setClusteringEnabled(Boolean.parseBoolean(parts[15])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 16) {
            try { layer.setClusterRadius(Integer.parseInt(parts[16])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 17) {
            String propField = parts[17].trim();
            if (!propField.isEmpty()) {
                layer.getProportionalSymbols().setFieldName(propField);
            }
        }
        if (parts.length > 18) {
            try { layer.getProportionalSymbols().setMinSize(Integer.parseInt(parts[18])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 19) {
            try { layer.getProportionalSymbols().setMaxSize(Integer.parseInt(parts[19])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 20) {
            try { layer.getProportionalSymbols().setScaleByArea(Boolean.parseBoolean(parts[20])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 21) {
            try { layer.getProportionalSymbols().setEnabled(Boolean.parseBoolean(parts[21])); } catch (Exception ignored) { /* fallback value */ }
        }
        // Label fields (v3)
        if (parts.length > 22) {
            try { layer.setLabelsVisible(Boolean.parseBoolean(parts[22])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 23) {
            layer.setLabelField(parts[23].trim());
        }
        if (parts.length > 24) {
            layer.setLabelExpression(parts[24].trim());
        }
        if (parts.length > 25) {
            String v = parts[25].trim();
            if (!v.isEmpty()) layer.setLabelFontFamily(v);
        }
        if (parts.length > 26) {
            try { layer.setLabelFontSize(Integer.parseInt(parts[26])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 27) {
            try { layer.setLabelBold(Boolean.parseBoolean(parts[27])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 28) {
            try { layer.setLabelItalic(Boolean.parseBoolean(parts[28])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 29) {
            try { layer.setLabelUnderline(Boolean.parseBoolean(parts[29])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 30) {
            try { layer.setLabelColor(parseColor(parts[30])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 31) {
            try { layer.setLabelHaloEnabled(Boolean.parseBoolean(parts[31])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 32) {
            try { layer.setLabelHaloColor(parseColor(parts[32])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 33) {
            try { layer.setLabelHaloWidth(Float.parseFloat(parts[33])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 34) {
            try { layer.setLabelOffsetX(Integer.parseInt(parts[34])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 35) {
            try { layer.setLabelOffsetY(Integer.parseInt(parts[35])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 36) {
            try { layer.setLabelPlacementMode(Layer.LabelPlacementMode.fromValue(parts[36])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 37) {
            try { layer.setLabelPriority(Integer.parseInt(parts[37])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 38) {
            try { layer.setLabelCollisionAvoid(Boolean.parseBoolean(parts[38])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 39) {
            try { layer.setLabelBackgroundEnabled(Boolean.parseBoolean(parts[39])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 40) {
            try { layer.setLabelBackgroundColor(parseColor(parts[40])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 41) {
            try { layer.setLabelMinScale(Double.parseDouble(parts[41])); } catch (Exception ignored) { /* fallback value */ }
        }
        if (parts.length > 42) {
            try { layer.setLabelMaxScale(Double.parseDouble(parts[42])); } catch (Exception ignored) { /* fallback value */ }
        }
        return layer;
    }

    private static Color parseColor(String s) {
        String[] rgb = s.split(",");
        if (rgb.length >= 3) {
            return new Color(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim()));
        }
        return Color.BLUE;
    }
}

