package ar.com.catgis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Batch processor for applying operations to multiple files.
 */
public final class BatchProcessor {

    private BatchProcessor() {}

    public record BatchJob(String name, List<File> inputFiles, File outputDir, String operation) {}
    public record BatchResult(int success, int failed, List<String> errors) {}

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

        // For multiband images, extract bands from the composite
        java.awt.image.BufferedImage bandAImage = extractBand(image, bandA);
        java.awt.image.BufferedImage bandBImage = extractBand(image, bandB);

        java.awt.image.BufferedImage result = SpectralIndexEngine.computeIndex(bandAImage, bandBImage, indexId);
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
