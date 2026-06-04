package ar.com.catgis.catmap;

import ar.com.catgis.layout.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Simple SVG export engine for CATMAP layouts.
 * Renders layout to SVG using Java2D SVG generation.
 */
public final class SvgExportEngine {

    private SvgExportEngine() {}

    /**
     * Export layout to SVG file.
     * Uses a simple approach: render to BufferedImage and embed as base64 in SVG.
     */
    public static void exportSvg(LayoutModel model, File file, int dpi) throws Exception {
        // Render to image first
        BufferedImage image = LayoutExportEngine.renderLayout(model, dpi);

        // Convert to base64
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "png", baos);
        String base64 = java.util.Base64.getEncoder().encodeToString(baos.toByteArray());

        // Calculate dimensions in SVG units (mm)
        double pageWidthMm = 297;
        double pageHeightMm = 210;

        // Write SVG
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
            pw.println("     width=\"" + pageWidthMm + "mm\" height=\"" + pageHeightMm + "mm\"");
            pw.println("     viewBox=\"0 0 " + pageWidthMm + " " + pageHeightMm + "\">");

            // Page background
            pw.println("  <rect width=\"" + pageWidthMm + "\" height=\"" + pageHeightMm + "\" fill=\"white\"/>");

            // Layout content as embedded image
            pw.println("  <image x=\"0\" y=\"0\" width=\"" + pageWidthMm + "\" height=\"" + pageHeightMm + "\"");
            pw.println("         href=\"data:image/png;base64," + base64 + "\"/>");

            pw.println("</svg>");
        }
    }
}
