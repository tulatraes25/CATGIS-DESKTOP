package ar.com.catgis.catmap;

import ar.com.catgis.layout.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Atlas engine for batch layout generation.
 * Generates multiple pages from a layout template.
 */
public final class AtlasEngine {

    private AtlasEngine() {}

    /**
     * Generate atlas pages from a layout model.
     * Each page can have different content based on features or pages.
     */
    public static List<BufferedImage> generatePages(
            LayoutModel template,
            List<AtlasPage> pages,
            int dpi) {

        List<BufferedImage> results = new ArrayList<>();
        LayoutRenderContext ctx = new LayoutRenderContext(
                LayoutRenderContext.Mode.EXPORT_IMAGE, dpi, 297, 210);

        for (AtlasPage page : pages) {
            // Apply page-specific overrides to the template
            applyPageOverrides(template, page);

            // Render
            int widthPx = (int) ctx.mmToPx(297);
            int heightPx = (int) ctx.mmToPx(210);
            BufferedImage image = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            try {
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, widthPx, heightPx);
                for (LayoutElement element : template.getVisibleElementsSortedByZ()) {
                    element.render(g2, ctx);
                }
            } finally {
                g2.dispose();
            }
            results.add(image);
        }
        return results;
    }

    /**
     * Generate atlas and save to files.
     */
    public static void generateAndSave(
            LayoutModel template,
            List<AtlasPage> pages,
            File outputDir,
            String baseName,
            int dpi) throws Exception {

        if (!outputDir.exists()) outputDir.mkdirs();

        List<BufferedImage> images = generatePages(template, pages, dpi);
        for (int i = 0; i < images.size(); i++) {
            String fileName = baseName + "_" + (i + 1) + ".png";
            File file = new File(outputDir, fileName);
            ImageIO.write(images.get(i), "png", file);
        }
    }

    /**
     * Apply page-specific overrides to the template.
     */
    private static void applyPageOverrides(LayoutModel template, AtlasPage page) {
        // Update title if present
        for (LayoutElement el : template.getElements()) {
            if (el instanceof LayoutLabel label) {
                if ("Titulo".equals(label.getName()) && page.title() != null) {
                    label.setText(page.title());
                }
                if ("Subtitulo".equals(label.getName()) && page.subtitle() != null) {
                    label.setText(page.subtitle());
                }
            }
            // Update map extent if present
            if (el instanceof LayoutMap map && page.extentMinX() != null) {
                map.setOwnExtent(true);
                map.setOwnViewMinX(page.extentMinX());
                map.setOwnViewMinY(page.extentMinY());
                map.setOwnViewMinX(page.extentMinX());
                map.setOwnZoomFactor(page.zoomFactor());
            }
        }
    }

    /**
     * Atlas page data.
     */
    public record AtlasPage(
            String title,
            String subtitle,
            Double extentMinX,
            Double extentMinY,
            Double zoomFactor
    ) {}
}
