package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Batch processor for applying operations to multiple files.
 * Supports template save/load for reusable workflow configurations.
 */
public final class BatchProcessor {

    private BatchProcessor() {}

    /** Default directory for batch templates. */
    private static final String TEMPLATES_DIR = "batch_templates";

    public record BatchJob(String name, List<File> inputFiles, File outputDir, String operation) {}
    public record BatchResult(int success, int failed, List<String> errors) {}

    /**
     * A reusable batch processing template.
     */
    public record BatchTemplate(
            String name,
            String description,
            String operation,
            String extensionFilter,
            boolean includeSubdirs,
            Map<String, String> settings
    ) {
        public BatchTemplate {
            if (settings == null) settings = new LinkedHashMap<>();
        }

        /** Serialize to a simple key=value JSON-like format. */
        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"name\": \"").append(escape(name)).append("\",\n");
            sb.append("  \"description\": \"").append(escape(description != null ? description : "")).append("\",\n");
            sb.append("  \"operation\": \"").append(escape(operation != null ? operation : "")).append("\",\n");
            sb.append("  \"extensionFilter\": \"").append(escape(extensionFilter != null ? extensionFilter : "")).append("\",\n");
            sb.append("  \"includeSubdirs\": ").append(includeSubdirs).append(",\n");
            sb.append("  \"settings\": {\n");
            int i = 0;
            for (var entry : settings.entrySet()) {
                sb.append("    \"").append(escape(entry.getKey())).append("\": \"")
                        .append(escape(entry.getValue())).append("\"");
                if (++i < settings.size()) sb.append(",");
                sb.append("\n");
            }
            sb.append("  }\n");
            sb.append("}");
            return sb.toString();
        }

        /** Deserialize from JSON string. */
        public static BatchTemplate fromJson(String json) {
            String name = extractJsonString(json, "name");
            String desc = extractJsonString(json, "description");
            String op = extractJsonString(json, "operation");
            String ext = extractJsonString(json, "extensionFilter");
            boolean subdirs = json.contains("\"includeSubdirs\": true");
            Map<String, String> settings = extractJsonMap(json, "settings");
            return new BatchTemplate(name, desc, op, ext, subdirs, settings);
        }

        private static String escape(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r");
        }

        private static String extractJsonString(String json, String key) {
            int idx = json.indexOf("\"" + key + "\"");
            if (idx < 0) return "";
            int colon = json.indexOf(':', idx);
            if (colon < 0) return "";
            int start = json.indexOf('"', colon);
            if (start < 0) return "";
            int end = start + 1;
            while (end < json.length() && json.charAt(end) != '"') {
                if (json.charAt(end) == '\\') end++;
                end++;
            }
            if (end >= json.length()) return "";
            return unescape(json.substring(start + 1, end));
        }

        private static String unescape(String s) {
            if (s == null) return "";
            return s.replace("\\\"", "\"").replace("\\\\", "\\")
                    .replace("\\n", "\n").replace("\\r", "\r");
        }

        private static Map<String, String> extractJsonMap(String json, String key) {
            Map<String, String> map = new LinkedHashMap<>();
            int idx = json.indexOf("\"" + key + "\"");
            if (idx < 0) return map;
            int brace = json.indexOf('{', idx);
            if (brace < 0) return map;
            int endBrace = json.indexOf('}', brace);
            if (endBrace < 0) return map;
            String content = json.substring(brace + 1, endBrace);
            // Parse key: "value" pairs
            int pos = 0;
            while (pos < content.length()) {
                int keyStart = content.indexOf('"', pos);
                if (keyStart < 0) break;
                int keyEnd = content.indexOf('"', keyStart + 1);
                if (keyEnd < 0) break;
                String k = content.substring(keyStart + 1, keyEnd);
                int valColon = content.indexOf(':', keyEnd);
                if (valColon < 0) break;
                int valStart = content.indexOf('"', valColon);
                if (valStart < 0) break;
                int valEnd = valStart + 1;
                while (valEnd < content.length() && content.charAt(valEnd) != '"') {
                    if (content.charAt(valEnd) == '\\') valEnd++;
                    valEnd++;
                }
                if (valEnd >= content.length()) break;
                String v = unescape(content.substring(valStart + 1, valEnd));
                map.put(k, v);
                pos = valEnd + 1;
            }
            return map;
        }
    }

    /**
     * Save a template to the templates directory.
     */
    public static void saveTemplate(BatchTemplate template) throws IOException {
        Path dir = Paths.get(TEMPLATES_DIR);
        Files.createDirectories(dir);
        String fileName = sanitizeFileName(template.name()) + ".json";
        Files.writeString(dir.resolve(fileName), template.toJson());
    }

    /**
     * Load all templates from the templates directory.
     */
    public static List<BatchTemplate> loadAllTemplates() {
        List<BatchTemplate> templates = new ArrayList<>();
        Path dir = Paths.get(TEMPLATES_DIR);
        if (!Files.isDirectory(dir)) return templates;
        try {
            for (File f : dir.toFile().listFiles((d, n) -> n.endsWith(".json"))) {
                try {
                    String json = Files.readString(f.toPath());
                    templates.add(BatchTemplate.fromJson(json));
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return templates;
    }

    /**
     * Delete a template by name.
     */
    public static boolean deleteTemplate(String name) {
        Path file = Paths.get(TEMPLATES_DIR, sanitizeFileName(name) + ".json");
        try {
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            return false;
        }
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-.]", "_");
    }

    public static BatchResult processBatch(BatchJob job, BiConsumer<File, File> operation) {
        return processBatch(job, operation, null);
    }

    public static BatchResult processBatch(BatchJob job, BiConsumer<File, File> operation,
                                           Consumer<Integer> progressCallback) {
        List<String> errors = new ArrayList<>();
        int success = 0;
        int failed = 0;
        int total = job.inputFiles().size();

        for (int i = 0; i < total; i++) {
            File input = job.inputFiles().get(i);
            File output = new File(job.outputDir(), input.getName());

            try {
                operation.accept(input, output);
                success++;
            } catch (Exception e) {
                failed++;
                errors.add(input.getName() + ": " + e.getMessage());
            }

            if (progressCallback != null) {
                progressCallback.accept((int) ((i + 1.0) / total * 100));
            }
        }

        return new BatchResult(success, failed, errors);
    }

    public static List<File> findFiles(File directory, String extension) {
        List<File> files = new ArrayList<>();
        if (directory == null || !directory.isDirectory()) return files;
        File[] found = directory.listFiles((dir, name) ->
                name.toLowerCase().endsWith(extension.toLowerCase()));
        if (found != null) {
            for (File f : found) files.add(f);
        }
        return files;
    }

    public static List<File> findAllGisFiles(File directory) {
        List<File> files = new ArrayList<>();
        if (directory == null || !directory.isDirectory()) return files;
        String[] extensions = {".shp", ".geojson", ".json", ".gpkg", ".kml", ".kmz",
                ".dxf", ".dwg", ".gpx", ".fgb", ".tif", ".tiff", ".asc", ".img"};
        File[] found = directory.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            for (String ext : extensions) {
                if (lower.endsWith(ext)) return true;
            }
            return false;
        });
        if (found != null) {
            for (File f : found) files.add(f);
        }
        return files;
    }

    /**
     * Apply a spectral index to a raster file.
     */
    public static void applySpectralIndex(File input, File output, String indexId, int bandA, int bandB) throws Exception {
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(input);
        if (image == null) throw new Exception("No se pudo leer: " + input.getName());

        java.awt.image.BufferedImage bandAImage = extractBand(image, bandA);
        java.awt.image.BufferedImage bandBImage = extractBand(image, bandB);

        java.awt.image.BufferedImage result = SpectralIndexEngine.computeIndex(bandAImage, bandBImage, indexId);
        if (result != null) {
            javax.imageio.ImageIO.write(result, "tif", output);
        }
    }

    /**
     * Apply a raster reclassification to a file.
     */
    public static void applyReclassify(File input, File output, String rulesJson) throws Exception {
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(input);
        if (image == null) throw new Exception("No se pudo leer: " + input.getName());

        // Parse simple rules from format "from1-to1=new1;from2-to2=new2"
        List<RasterReclassifyEngine.ReclassRule> rules = new ArrayList<>();
        for (String ruleStr : rulesJson.split(";")) {
            String[] parts = ruleStr.split("-");
            if (parts.length >= 2) {
                String[] right = parts[1].split("=");
                if (right.length >= 2) {
                    rules.add(new RasterReclassifyEngine.ReclassRule(
                            Double.parseDouble(parts[0]),
                            Double.parseDouble(right[0]),
                            Double.parseDouble(right[1]),
                            right.length > 2 ? right[2] : ""));
                }
            }
        }

        java.awt.image.BufferedImage result = RasterReclassifyEngine.reclassify(image, rules, -1, false);
        if (result != null) {
            javax.imageio.ImageIO.write(result, "tif", output);
        }
    }

    private static java.awt.image.BufferedImage extractBand(java.awt.image.BufferedImage composite, int bandIndex) {
        int w = composite.getWidth();
        int h = composite.getHeight();
        java.awt.image.BufferedImage band = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
        java.awt.image.Raster src = composite.getRaster();
        java.awt.image.WritableRaster dst = band.getRaster();
        double[] pixel = new double[src.getNumBands()];
        double[] out = new double[1];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                src.getPixel(x, y, pixel);
                int idx = Math.min(bandIndex, pixel.length - 1);
                out[0] = pixel[idx];
                dst.setPixel(x, y, out);
            }
        }
        return band;
    }
}
