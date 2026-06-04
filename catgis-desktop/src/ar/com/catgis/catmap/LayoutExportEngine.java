package ar.com.catgis.catmap;

import ar.com.catgis.layout.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Export engine for CATMAP layouts.
 * Renders layout to PDF or image formats.
 */
public final class LayoutExportEngine {

    private LayoutExportEngine() {}

    /**
     * Export layout to PNG image.
     */
    public static void exportPng(LayoutModel model, File file, int dpi) throws Exception {
        BufferedImage image = renderLayout(model, dpi);
        ImageIO.write(image, "png", file);
    }

    /**
     * Export layout to JPG image.
     */
    public static void exportJpg(LayoutModel model, File file, int dpi) throws Exception {
        BufferedImage image = renderLayout(model, dpi);
        // Convert to RGB for JPG
        BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g.drawImage(image, 0, 0, null);
        g.dispose();
        ImageIO.write(rgb, "jpg", file);
    }

    /**
     * Export layout to PDF using PDFBox.
     */
    public static void exportPdf(LayoutModel model, File file, int dpi) throws Exception {
        BufferedImage image = renderLayout(model, dpi);

        try (org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument()) {
            // A4 landscape: 297mm x 210mm
            org.apache.pdfbox.pdmodel.common.PDRectangle rect = org.apache.pdfbox.pdmodel.common.PDRectangle.A4;
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(rect);
            document.addPage(page);

            // Convert to RGB
            BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            g.drawImage(image, 0, 0, null);
            g.dispose();

            // Create PDF image
            org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject pdfImg =
                    org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory.createFromImage(document, rgb);

            // Draw image on page
            try (org.apache.pdfbox.pdmodel.PDPageContentStream cs =
                    new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page)) {
                cs.drawImage(pdfImg, 0, 0, rect.getWidth(), rect.getHeight());
            }

            document.save(file);
        }
    }

    /**
     * Render layout to BufferedImage at specified DPI.
     */
    public static BufferedImage renderLayout(LayoutModel model, int dpi) {
        // A4 landscape: 297mm x 210mm
        double pageWidthMm = 297;
        double pageHeightMm = 210;

        int widthPx = (int) ((pageWidthMm / 25.4) * dpi);
        int heightPx = (int) ((pageHeightMm / 25.4) * dpi);

        LayoutRenderContext ctx = new LayoutRenderContext(
                LayoutRenderContext.Mode.EXPORT_IMAGE, dpi, pageWidthMm, pageHeightMm);

        BufferedImage image = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        try {
            // White page background
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, widthPx, heightPx);

            // Render all visible elements
            for (LayoutElement element : model.getVisibleElementsSortedByZ()) {
                element.render(g2, ctx);
            }
        } finally {
            g2.dispose();
        }

        return image;
    }
}
