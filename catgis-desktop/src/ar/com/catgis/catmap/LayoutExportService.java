package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutElement;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.LayoutRenderContext;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Multi-page render/export for CATMAP documents.
 *
 * <p>Every page of the document is rendered with the document page geometry
 * (A3/A4/custom, orientation, margins) through one code path, so PDF, PNG pages
 * and future print/atlas output cannot drift from the preview.</p>
 */
public final class LayoutExportService {

    private LayoutExportService() {}

    /** Render one document page to a bounded raster at the requested DPI. */
    public static BufferedImage renderPage(LayoutModel model, int pageIndex, int dpi) {
        Objects.requireNonNull(model, "CATMAP layout model");
        if (pageIndex < 0 || pageIndex >= model.getPageCount()) {
            throw new IllegalArgumentException("CATMAP page index out of range: " + pageIndex);
        }
        LayoutPageSpec pageSpec = model.getPageSpec();
        int[] dimensions = CatmapAtomicExport.rasterSize(
                dpi, pageSpec.widthMm(), pageSpec.heightMm());
        int widthPx = dimensions[0];
        int heightPx = dimensions[1];

        LayoutRenderContext context = new LayoutRenderContext(
                LayoutRenderContext.Mode.EXPORT_IMAGE, dpi,
                pageSpec.widthMm(), pageSpec.heightMm());
        BufferedImage image = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        try {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, widthPx, heightPx);
            for (LayoutElement element : model.getVisibleElementsSortedByZ(pageIndex)) {
                ar.com.catgis.layout.ScaleResolver
                        .withResolvedScale(element, model, dpi)
                        .render(g2, context);
            }
        } finally {
            g2.dispose();
        }
        return image;
    }

    /** Export the whole document as a multi-page PDF (one page per layout page). */
    public static void exportPdf(LayoutModel model, File file, int dpi) throws Exception {
        LayoutExportEngine.exportPdf(model, file, dpi);
    }

    /**
     * Export one atomic PNG per document page as {@code <base>_<n>.png}.
     * No existing page is overwritten silently: staging validation and the
     * atomic move come from {@link CatmapAtomicExport}.
     */
    public static List<File> exportPngPages(LayoutModel model, File outputDirectory,
                                            String baseName, int dpi) throws Exception {
        Objects.requireNonNull(model, "CATMAP layout model");
        Objects.requireNonNull(outputDirectory, "CATMAP PNG output directory");
        String safeBase = requireSafeBaseName(baseName);
        Path directory = outputDirectory.toPath().toAbsolutePath().normalize();
        if (!Files.isDirectory(directory)) {
            throw new IOException("El directorio de exportacion CATMAP no existe.");
        }
        Path realDirectory = directory.toRealPath();
        LayoutPageSpec pageSpec = model.getPageSpec();
        int[] dimensions = CatmapAtomicExport.rasterSize(
                dpi, pageSpec.widthMm(), pageSpec.heightMm());
        List<File> written = new ArrayList<>();
        for (int page = 0; page < model.getPageCount(); page++) {
            BufferedImage image = renderPage(model, page, dpi);
            Path target = realDirectory.resolve(safeBase + "_" + (page + 1) + ".png");
            File targetFile = target.toFile();
            CatmapAtomicExport.publish(targetFile,
                    staged -> {
                        if (!ImageIO.write(image, "png", staged.toFile())) {
                            throw new IOException("CATMAP no dispone de codificador PNG.");
                        }
                    },
                    staged -> CatmapAtomicExport.verifyImage(staged, "png",
                            dimensions[0], dimensions[1]));
            written.add(targetFile);
        }
        return written;
    }

    private static String requireSafeBaseName(String baseName) {
        if (baseName == null || baseName.isBlank() || ".".equals(baseName)
                || "..".equals(baseName) || baseName.contains("..")
                || baseName.indexOf('/') >= 0 || baseName.indexOf('\\') >= 0
                || baseName.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("CATMAP output name is invalid: " + baseName);
        }
        return baseName;
    }
}
