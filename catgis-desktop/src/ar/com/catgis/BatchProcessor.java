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

        /** Serialize with the shared strict codec; retain the legacy public API. */
        public String toJson() {
            return BatchTemplateCodec.encode(this);
        }

        /** Decode valid templates without altering paths or processing parameters. */
        public static BatchTemplate fromJson(String json) {
            return BatchTemplateCodec.decode(json);
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
                } catch (Exception ignored) { CatgisLogger.warn("BatchProcessor: operation failed", ignored); }
            }
        } catch (Exception ignored) { CatgisLogger.warn("BatchProcessor: operation failed", ignored); }
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
        // Snapshot and validate the WHOLE plan before invoking a callback.
        // A late invalid target must not be discovered after earlier writes.
        List<File> inputs = List.copyOf(job.inputFiles());
        int total = inputs.size();
        List<BatchOutputPlan.Entry> plan;
        try {
            plan = BatchOutputPlan.build(inputs, job.outputDir());
        } catch (IOException | SecurityException unsafePlan) {
            return new BatchResult(0, total,
                    List.of("Lote rechazado antes de ejecutar: " + unsafePlan.getMessage()));
        }

        for (int i = 0; i < total; i++) {
            File input = plan.get(i).input();
            File output = plan.get(i).output();

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
        if (image == null) throw new IOException("No se pudo leer: " + input.getName());
        if (SpectralIndexEngine.requiredBandCount(indexId) != 2) {
            throw new IllegalArgumentException("El indice " + indexId
                    + " requiere mas de dos bandas y no puede ejecutarse por esta ruta.");
        }

        // Batch/UI band numbers are human-facing and one-based (B1..Bn).
        java.awt.image.BufferedImage bandAImage = RasterBandExtractor.extractOneBased(image, bandA);
        java.awt.image.BufferedImage bandBImage = RasterBandExtractor.extractOneBased(image, bandB);

        java.awt.image.BufferedImage result = SpectralIndexEngine.computeIndex(
                bandAImage, bandBImage, indexId);
        writeTiffOrThrow(result, output);
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
        writeTiffOrThrow(result, output);
    }

    private static void writeTiffOrThrow(java.awt.image.BufferedImage image, File output)
            throws IOException {
        if (image == null) {
            throw new IOException("La operacion raster no produjo un resultado.");
        }
        if (!javax.imageio.ImageIO.write(image, "tif", output)) {
            throw new IOException("No hay un escritor TIFF disponible para publicar el resultado.");
        }
    }
}
