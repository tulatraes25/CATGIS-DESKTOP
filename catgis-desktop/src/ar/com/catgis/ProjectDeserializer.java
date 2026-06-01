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

        try { layer.setPointColor(parseColor(parts[3])); } catch (Exception ignored) {}
        try { layer.setLineColor(parseColor(parts[4])); } catch (Exception ignored) {}
        try { layer.setFillColor(parseColor(parts[5])); } catch (Exception ignored) {}
        try { layer.setLineWidth(Float.parseFloat(parts[6])); } catch (Exception ignored) {}
        try { layer.setPointSize(Integer.parseInt(parts[7])); } catch (Exception ignored) {}
        try { layer.setVisible(Boolean.parseBoolean(parts[8])); } catch (Exception ignored) {}
        try { layer.setOpacity(Float.parseFloat(parts[9])); } catch (Exception ignored) {}
        if (parts.length > 10) {
            try { layer.setPointSymbolStyle(Layer.PointSymbolStyle.fromValue(parts[10])); } catch (Exception ignored) {}
        }
        if (parts.length > 11) {
            layer.setCatalogSymbolId(parts[11].trim());
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
