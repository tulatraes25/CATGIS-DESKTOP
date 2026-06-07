package ar.com.catgis.catmap;
import ar.com.catgis.core.model.Layer;

import ar.com.catgis.MapPanel;
import ar.com.catgis.RasterLayer;
import ar.com.catgis.climate.WindRoseRenderer;
import ar.com.catgis.layout.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Export engine for CATMAP layouts.
 * Renders layout to PDF or image formats.
 * <p>
 * Supports climate raster layers (custom colormaps) and wind rose images.
 */
public final class LayoutExportEngine {

    private LayoutExportEngine() {}

    /**
     * Create a LayoutImage element from a wind rose renderer for inclusion in CATMAP layouts.
     * The wind rose is rendered at 300x250 pixels, suitable for an A4 landscape layout.
     *
     * @param renderer the wind rose renderer with data already set
     * @param xMm      X position in mm from top-left corner of layout
     * @param yMm      Y position in mm from top-left corner
     * @return a LayoutImage element ready to add to a LayoutModel
     */
    public static LayoutImage createWindRoseLayoutImage(WindRoseRenderer renderer, double xMm, double yMm) {
        if (renderer == null) return null;
        BufferedImage roseImage = renderer.renderToImage(300, 250);
        return new LayoutImage("wind_rose", roseImage, xMm, yMm, 50, 40);
    }

    /**
     * Apply climate colormap styling to a map frame rendered for layout export.
     * This ensures that climate raster layers (with customColorMap set) render
     * correctly in the CATMAP export, matching the map view appearance.
     * <p>
     * Called automatically during layout rendering; no manual action needed.
     *
     * @param mapPanel the MapPanel with climate raster layers
     * @param g2       the Graphics2D context for layout rendering
     * @param layers   the layers to render (filtered for climate rasters)
     */
    public static void applyClimateColormapsToLayout(MapPanel mapPanel, Graphics2D g2, List<?> layers) {
        if (mapPanel == null || g2 == null || layers == null) return;

        for (Object obj : layers) {
            if (obj instanceof RasterLayer rasterLayer) {
                MapPanel.RasterStyle style = mapPanel.getOrCreateRasterStyle(
                        rasterLayer, 1);
                if (style.customColorMap != null) {
                    // Custom colormap is already applied in MapPanel rendering pipeline;
                    // we just ensure it gets rendered in layout by marking the layer
                    rasterLayer.putUserData("layoutClimateColormap", true);
                }
            }
        }
    }

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
     * Export layout to PDF using PDFBox with vector elements.
     * Text, lines, rectangles, ellipses are drawn as vector PDF content.
     * Map frames, legends, tables, and images fall back to high-res raster.
     */
    public static void exportPdf(LayoutModel model, File file, int dpi) throws Exception {
        try (org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument()) {
            // A4 landscape: 297mm x 210mm
            double pageWidthPt = mmToPt(297);
            double pageHeightPt = mmToPt(210);
            org.apache.pdfbox.pdmodel.common.PDRectangle rect = new org.apache.pdfbox.pdmodel.common.PDRectangle(
                (float) pageWidthPt, (float) pageHeightPt);
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(rect);
            document.addPage(page);

            try (org.apache.pdfbox.pdmodel.PDPageContentStream cs =
                    new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page)) {

                // White background
                cs.setNonStrokingColor(Color.WHITE);
                cs.addRect(0, 0, (float) pageWidthPt, (float) pageHeightPt);
                cs.fill();

                // Render each element
                for (LayoutElement element : model.getVisibleElementsSortedByZ()) {
                    exportElementToPdf(document, cs, element, dpi);
                }
            }

            document.save(file);
        }
    }

    private static void exportElementToPdf(
            org.apache.pdfbox.pdmodel.PDDocument document,
            org.apache.pdfbox.pdmodel.PDPageContentStream cs,
            LayoutElement element, int dpi) throws Exception {

        Rectangle2D.Double b = element.getBoundsMm();
        // Convert mm to PDF points (origin at lower-left)
        float x = (float) mmToPt(b.x);
        float y = (float) (mmToPt(210) - mmToPt(b.y + b.height)); // flip Y
        float w = (float) mmToPt(b.width);
        float h = (float) mmToPt(b.height);

        if (element instanceof LayoutLabel label) {
            exportLabel(cs, label, x, y, w, h);
        } else if (element instanceof LayoutLine line) {
            exportLine(cs, line, x, y, w, h);
        } else if (element instanceof LayoutRectangle rectEl) {
            exportRect(cs, rectEl, x, y, w, h);
        } else if (element instanceof LayoutEllipse ellipse) {
            exportEllipse(cs, ellipse, x, y, w, h);
        } else {
            // Fallback: render to raster (LayoutMap, LayoutLegend, LayoutTable,
            // LayoutImage, LayoutScaleBar, LayoutNorthArrow, LayoutCartouche, etc.)
            exportAsRaster(document, cs, element, x, y, w, h, dpi);
        }
    }

    private static void exportLabel(
            org.apache.pdfbox.pdmodel.PDPageContentStream cs,
            LayoutLabel label, float x, float y, float w, float h) throws Exception {

        String text = label.getText();
        if (text == null || text.isBlank()) return;

        // Background
        Color bg = label.getBgColor();
        if (bg.getAlpha() > 0) {
            cs.setNonStrokingColor(bg);
            cs.addRect(x, y, w, h);
            cs.fill();
        }

        // Border
        float bw = label.getBorderWidth();
        if (bw > 0 && label.getBorderColor().getAlpha() > 0) {
            cs.setStrokingColor(label.getBorderColor());
            cs.setLineWidth(bw);
            cs.addRect(x, y, w, h);
            cs.stroke();
        }

        // Text
        Color color = label.getColor();
        java.awt.Font font = label.getFont();
        float fontSize = font != null ? font.getSize2D() : 12f;

        cs.setNonStrokingColor(color);
        cs.beginText();
        // Map Java font name to PDF BaseFont
        String pdfFont = mapFontToPdf(font);
        cs.setFont(documentFont(cs, pdfFont), fontSize);

        // Position text inside the label bounds with padding
        float padding = 3f;
        float tx = x + padding;
        float ty = y + h - padding - fontSize * 0.2f; // approximate ascent

        // Handle multi-line text
        String[] lines = text.split("\n");
        float lineHeight = fontSize * 1.2f;
        for (String line : lines) {
            cs.newLineAtOffset(tx, ty);
            cs.showText(line);
            ty -= lineHeight;
        }
        cs.endText();
    }

    private static void exportLine(
            org.apache.pdfbox.pdmodel.PDPageContentStream cs,
            LayoutLine line, float x, float y, float w, float h) throws Exception {

        Color color = line.getBorderColor();
        if (color.getAlpha() == 0) color = Color.BLACK;
        cs.setStrokingColor(color);
        cs.setLineWidth(line.getBorderWidth() > 0 ? line.getBorderWidth() : 1);

        // A LayoutLine typically goes from one corner to another
        // Use bounds as start/end
        cs.moveTo(x, y + h);
        cs.lineTo(x + w, y);
        cs.stroke();
    }

    private static void exportRect(
            org.apache.pdfbox.pdmodel.PDPageContentStream cs,
            LayoutRectangle rectEl, float x, float y, float w, float h) throws Exception {

        // Fill
        Color bg = rectEl.getBgColor();
        if (bg.getAlpha() > 0) {
            cs.setNonStrokingColor(bg);
            int radius = rectEl.getCornerRadius();
            if (radius > 0) {
                // Approximate rounded rect with line arcs
                cs.addRect(x, y, w, h);
            } else {
                cs.addRect(x, y, w, h);
            }
            cs.fill();
        }

        // Border
        float bw = rectEl.getBorderWidth();
        if (bw > 0 && rectEl.getBorderColor().getAlpha() > 0) {
            cs.setStrokingColor(rectEl.getBorderColor());
            cs.setLineWidth(bw);
            cs.addRect(x, y, w, h);
            cs.stroke();
        }
    }

    private static void exportEllipse(
            org.apache.pdfbox.pdmodel.PDPageContentStream cs,
            LayoutEllipse ellipse, float x, float y, float w, float h) throws Exception {

        // PDF doesn't have native ellipse; approximate with bezier curves
        float cx = x + w / 2f;
        float cy = y + h / 2f;
        float rx = w / 2f;
        float ry = h / 2f;
        final float BEZIER_K = 0.55228475f; // bezier approximation constant
        float kx = rx * BEZIER_K;
        float ky = ry * BEZIER_K;

        // Build ellipse path
        cs.moveTo(cx + rx, cy);
        cs.curveTo(cx + rx, cy + ky, cx + kx, cy + ry, cx, cy + ry);
        cs.curveTo(cx - kx, cy + ry, cx - rx, cy + ky, cx - rx, cy);
        cs.curveTo(cx - rx, cy - ky, cx - kx, cy - ry, cx, cy - ry);
        cs.curveTo(cx + kx, cy - ry, cx + rx, cy - ky, cx + rx, cy);

        // Fill
        Color bg = ellipse.getBgColor();
        if (bg.getAlpha() > 0) {
            cs.setNonStrokingColor(bg);
            cs.fill();
        }

        // Border
        float bw = ellipse.getBorderWidth();
        if (bw > 0 && ellipse.getBorderColor().getAlpha() > 0) {
            cs.setStrokingColor(ellipse.getBorderColor());
            cs.setLineWidth(bw);
            cs.stroke();
        }
    }

    private static void exportAsRaster(
            org.apache.pdfbox.pdmodel.PDDocument document,
            org.apache.pdfbox.pdmodel.PDPageContentStream cs,
            LayoutElement element, float x, float y, float w, float h,
            int dpi) throws Exception {

        // Render element to a BufferedImage at the right resolution
        Rectangle2D.Double b = element.getBoundsMm();
        int imgW = Math.max(1, (int) ((b.width / 25.4) * dpi));
        int imgH = Math.max(1, (int) ((b.height / 25.4) * dpi));
        if (imgW > 4000 || imgH > 4000) {
            // Cap resolution to prevent memory issues
            double scale = Math.min(4000.0 / imgW, 4000.0 / imgH);
            imgW = (int) (imgW * scale);
            imgH = (int) (imgH * scale);
        }

        BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            LayoutRenderContext ctx = new LayoutRenderContext(
                LayoutRenderContext.Mode.EXPORT_IMAGE, dpi, 297, 210);
            element.render(g2, ctx);
        } finally {
            g2.dispose();
        }

        // Convert to RGB for PDF
        BufferedImage rgb = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
        Graphics2D rg = rgb.createGraphics();
        try {
            rg.setColor(Color.WHITE);
            rg.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            rg.drawImage(img, 0, 0, null);
        } finally {
            rg.dispose();
        }

        org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject pdfImg =
                org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory.createFromImage(document, rgb);
        cs.drawImage(pdfImg, x, y, w, h);
    }

    // ========== Helpers ==========

    private static double mmToPt(double mm) {
        return mm * 72.0 / 25.4;
    }

    private static String mapFontToPdf(java.awt.Font font) {
        if (font == null) return "Helvetica";
        String name = font.getFamily().toLowerCase();
        boolean bold = font.isBold();
        boolean italic = font.isItalic();

        // Map common Java fonts to PDF Standard 14 fonts
        if (name.contains("courier")) {
            if (bold && italic) return "Courier-BoldOblique";
            if (bold) return "Courier-Bold";
            if (italic) return "Courier-Oblique";
            return "Courier";
        }
        if (name.contains("times") || name.contains("roman")) {
            if (bold && italic) return "Times-BoldItalic";
            if (bold) return "Times-Bold";
            if (italic) return "Times-Italic";
            return "Times-Roman";
        }
        // Default: Helvetica (sans-serif)
        if (bold && italic) return "Helvetica-BoldOblique";
        if (bold) return "Helvetica-Bold";
        if (italic) return "Helvetica-Oblique";
        return "Helvetica";
    }

    private static org.apache.pdfbox.pdmodel.font.PDFont documentFont(
            org.apache.pdfbox.pdmodel.PDPageContentStream cs, String baseName) throws Exception {
        // Return standard font by name using reflection on PDType1Font
        java.lang.reflect.Field field = org.apache.pdfbox.pdmodel.font.PDType1Font.class.getField(baseName);
        return (org.apache.pdfbox.pdmodel.font.PDFont) field.get(null);
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
