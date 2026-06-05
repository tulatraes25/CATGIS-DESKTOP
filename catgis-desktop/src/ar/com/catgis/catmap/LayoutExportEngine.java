package ar.com.catgis.catmap;

import ar.com.catgis.MapPanel;
import ar.com.catgis.RasterLayer;
import ar.com.catgis.climate.WindRoseRenderer;
import ar.com.catgis.layout.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
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
     * Export layout to PDF using PDFBox.
     */
    public static void exportPdf(LayoutModel model, File file, int dpi) throws Exception {
        BufferedImage image = renderLayout(model, dpi);

        try (org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument()) {
            // A4 landscape: 297mm x 210mm
            // PDFBox PDRectangle.A4 is portrait (595.28 x 841.89), rotate to landscape
            org.apache.pdfbox.pdmodel.common.PDRectangle rect = new org.apache.pdfbox.pdmodel.common.PDRectangle(
                org.apache.pdfbox.pdmodel.common.PDRectangle.A4.getHeight(),
                org.apache.pdfbox.pdmodel.common.PDRectangle.A4.getWidth()
            );
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
