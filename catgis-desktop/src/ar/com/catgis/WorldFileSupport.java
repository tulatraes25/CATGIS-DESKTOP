package ar.com.catgis;

import java.io.*;

public final class WorldFileSupport {

    private WorldFileSupport() {}

    public record WorldFileParams(double pixelX, double rotationX, double rotationY, double pixelY, double topLeftX, double topLeftY) {}

    public static void writeWorldFile(File imageFile, WorldFileParams params) {
        String base = imageFile.getAbsolutePath();
        String prefix = base.substring(0, base.lastIndexOf('.'));
        String ext = getWorldFileExtension(imageFile.getName());
        File wf = new File(prefix + "." + ext);
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(wf), java.nio.charset.StandardCharsets.US_ASCII))) {
            w.printf(java.util.Locale.ROOT, "%.10f%n", params.pixelX());
            w.printf(java.util.Locale.ROOT, "%.10f%n", params.rotationX());
            w.printf(java.util.Locale.ROOT, "%.10f%n", params.rotationY());
            w.printf(java.util.Locale.ROOT, "%.10f%n", params.pixelY());
            w.printf(java.util.Locale.ROOT, "%.10f%n", params.topLeftX());
            w.printf(java.util.Locale.ROOT, "%.10f%n", params.topLeftY());
        } catch (IOException e) {
            CatgisLogger.warn("Failed to write world file: " + wf, e);
        }
    }

    private static String getWorldFileExtension(String imageName) {
        if (imageName == null) return "tfw";
        String lower = imageName.toLowerCase();
        if (lower.endsWith(".tif") || lower.endsWith(".tiff")) return "tfw";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "jgw";
        if (lower.endsWith(".png")) return "pgw";
        if (lower.endsWith(".gif")) return "gfw";
        if (lower.endsWith(".bmp")) return "bpw";
        return "wld";
    }
}
