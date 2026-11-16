package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutElement;
import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutMap;
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
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bounded CATMAP atlas renderer. Atlas-specific changes are applied only to
 * independent label/map frame instances, never the editor's own elements.
 */
public final class AtlasEngine {

    private static final int MIN_DPI = 36;
    private static final int MAX_DPI = 450;
    private static final long MAX_PAGE_BYTES = 96L * 1024L * 1024L;
    private static final long MAX_BATCH_BYTES = 256L * 1024L * 1024L;
    private static final int MAX_PAGES = 200;
    private static final int MAX_PDF_PAGES = 200;

    private AtlasEngine() {}

    @FunctionalInterface
    interface AtomicAtlasPublisher {
        void publish(Path staging, Path target) throws IOException;
    }

    private record PageSetup(LayoutRenderContext context, int width, int height,
                             long bytes) {}

    private static PageSetup setup(int dpi, LayoutPageSpec pageSpec) {
        if (dpi < MIN_DPI || dpi > MAX_DPI) {
            throw new IllegalArgumentException("CATMAP atlas: DPI must be between 36 and 450.");
        }
        LayoutRenderContext context = new LayoutRenderContext(
                LayoutRenderContext.Mode.EXPORT_IMAGE, dpi,
                pageSpec.widthMm(), pageSpec.heightMm());
        int width = context.mmToPxInt(pageSpec.widthMm());
        int height = context.mmToPxInt(pageSpec.heightMm());
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("CATMAP atlas: invalid page dimensions.");
        }
        long bytes = (long) width * height * 4L;
        if (bytes > MAX_PAGE_BYTES) {
            throw new IllegalArgumentException("CATMAP atlas page exceeds the memory safety limit.");
        }
        return new PageSetup(context, width, height, bytes);
    }

    private static List<LayoutElement> snapshotForPage(LayoutModel template,
                                                        AtlasPage page,
                                                        PageSetup setup,
                                                        int pageIndex) {
        Objects.requireNonNull(page, "CATMAP atlas page");
        boolean anyExtent = page.extentMinX() != null
                || page.extentMinY() != null
                || page.extentMaxX() != null
                || page.extentMaxY() != null;
        if (anyExtent) {
            if (page.extentMinX() == null || page.extentMinY() == null
                    || page.extentMaxX() == null || page.extentMaxY() == null
                    || !Double.isFinite(page.extentMinX())
                    || !Double.isFinite(page.extentMinY())
                    || !Double.isFinite(page.extentMaxX())
                    || !Double.isFinite(page.extentMaxY())
                    || page.extentMaxX() <= page.extentMinX()
                    || page.extentMaxY() <= page.extentMinY()) {
                throw new IllegalArgumentException(
                        "CATMAP atlas page requires a finite extent with max > min.");
            }
        }
        double bufferPercent = page.bufferPercent() != null ? page.bufferPercent() : 10.0;

        List<LayoutElement> pageElements = new ArrayList<>();
        for (LayoutElement element : template.getVisibleElementsSortedByZ()) {
            if (element instanceof LayoutLabel original) {
                LayoutLabel copy = original.copyForAtlas();
                if ("Titulo".equals(copy.getName()) && page.title() != null) {
                    copy.setText(page.title());
                } else if ("Subtitulo".equals(copy.getName()) && page.subtitle() != null) {
                    copy.setText(page.subtitle());
                }
                String text = copy.getText();
                String dynamic = copy.getDynamicExpression();
                if (text != null && (text.contains("{page}") || text.contains("{feature}"))) {
                    copy.setText(replaceAtlasTokens(text, pageIndex + 1, page.title()));
                }
                if (dynamic != null
                        && (dynamic.contains("{page}") || dynamic.contains("{feature}"))) {
                    copy.setDynamicExpression(
                            replaceAtlasTokens(dynamic, pageIndex + 1, page.title()));
                }
                pageElements.add(copy);
            } else if (element instanceof LayoutMap original) {
                LayoutMap copy = original.copyForAtlas();
                if (anyExtent) {
                    int frameWidthPx = Math.max(1, setup.context().mmToPxInt(copy.getBoundsMm().width));
                    int frameHeightPx = Math.max(1, setup.context().mmToPxInt(copy.getBoundsMm().height));
                    AtlasFraming.Framing framing = AtlasFraming.forFeature(
                            new org.locationtech.jts.geom.Envelope(
                                    page.extentMinX(), page.extentMaxX(),
                                    page.extentMinY(), page.extentMaxY()),
                            frameWidthPx, frameHeightPx, bufferPercent);
                    copy.setOwnExtent(true);
                    copy.setOwnViewMinX(framing.minX());
                    copy.setOwnViewMinY(framing.minY());
                    copy.setOwnZoomFactor(framing.worldPerPixel(frameWidthPx));
                }
                pageElements.add(copy);
            } else {
                // Other layout elements receive NO atlas overrides. Their
                // own renderers remain the existing layout engine's contract.
                pageElements.add(element);
            }
        }
        return pageElements;
    }

    private static String replaceAtlasTokens(String text, int pageNumber, String feature) {
        String result = text.replace("{page}", Integer.toString(pageNumber));
        return result.replace("{feature}", feature != null ? feature : "");
    }

    private static BufferedImage renderPage(LayoutModel template, AtlasPage page,
                                            PageSetup setup, int pageIndex) {
        List<LayoutElement> pageElements = snapshotForPage(template, page, setup, pageIndex);
        BufferedImage image = new BufferedImage(
                setup.width(), setup.height(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, setup.width(), setup.height());
            for (LayoutElement element : pageElements) {
                element.render(g2, setup.context());
            }
        } finally {
            g2.dispose();
        }
        return image;
    }

    /**
     * Compatibility API for callers requesting in-memory pages. Reject
     * batches whose uncompressed images would exceed 256 MiB.
     */
    public static List<BufferedImage> generatePages(
            LayoutModel template, List<AtlasPage> pages, int dpi) {
        Objects.requireNonNull(template, "CATMAP atlas template");
        Objects.requireNonNull(pages, "CATMAP atlas pages");
        PageSetup setup = setup(dpi, template.getPageSpec());
        if (setup.bytes() * pages.size() > MAX_BATCH_BYTES) {
            throw new IllegalArgumentException(
                    "CATMAP atlas batch exceeds the in-memory image budget; export pages to files.");
        }
        if (pages.size() > MAX_PAGES) {
            throw new IllegalArgumentException("CATMAP atlas supports a maximum of "
                    + MAX_PAGES + " pages.");
        }
        List<BufferedImage> results = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            results.add(renderPage(template, pages.get(i), setup, i));
        }
        return results;
    }

    /**
     * Bounded streaming export: render and atomically save just one page at
     * a time. Existing exported pages must not be silently overwritten.
     */
    public static void generateAndSave(LayoutModel template, List<AtlasPage> pages,
                                       File outputDir, String baseName, int dpi)
            throws Exception {
        generateAndSave(template, pages, outputDir, baseName, dpi,
                (stage, target) -> Files.move(stage, target,
                        StandardCopyOption.ATOMIC_MOVE));
    }

    /** Injection seam for testing unsupported atomic move and cleanup. */
    static void generateAndSave(LayoutModel template, List<AtlasPage> pages,
                                File outputDir, String baseName, int dpi,
                                AtomicAtlasPublisher publisher) throws Exception {
        Objects.requireNonNull(template, "CATMAP atlas template");
        Objects.requireNonNull(pages, "CATMAP atlas pages");
        Objects.requireNonNull(outputDir, "CATMAP atlas output directory");
        Objects.requireNonNull(publisher, "CATMAP atlas publisher");
        if (baseName == null || baseName.isBlank() || ".".equals(baseName)
                || "..".equals(baseName) || baseName.contains("..")
                || baseName.indexOf('/') >= 0 || baseName.indexOf('\\') >= 0
                || baseName.chars().anyMatch(Character::isISOControl)) {
            throw new IOException("CATMAP atlas output name is invalid.");
        }

        PageSetup setup = setup(dpi, template.getPageSpec());
        Path supplied = outputDir.toPath().toAbsolutePath().normalize();
        if (Files.isSymbolicLink(supplied)) {
            throw new IOException("CATMAP atlas output directory cannot be a symbolic link.");
        }
        Files.createDirectories(supplied);
        Path parent = supplied.toRealPath();
        if (!Files.isDirectory(parent)) {
            throw new IOException("CATMAP atlas output path is not a directory.");
        }
        for (int i = 0; i < pages.size(); i++) {
            Path target = parent.resolve(baseName + "_" + (i + 1) + ".png");
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("CATMAP atlas output already exists: " + target.getFileName());
            }
            Path stage = Files.createTempFile(parent, "." + baseName + "-", ".part");
            BufferedImage image = null;
            try {
                image = renderPage(template, pages.get(i), setup, i);
                if (!ImageIO.write(image, "png", stage.toFile())) {
                    throw new IOException("No PNG encoder available for CATMAP atlas.");
                }
                BufferedImage decoded = ImageIO.read(stage.toFile());
                if (decoded == null || decoded.getWidth() != setup.width()
                        || decoded.getHeight() != setup.height()) {
                    throw new IOException("CATMAP atlas PNG validation failed.");
                }
                decoded.flush();
                try (FileChannel channel = FileChannel.open(stage, StandardOpenOption.WRITE)) {
                    channel.force(true);
                }
                if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                    throw new IOException("CATMAP atlas output appeared during rendering: "
                            + target.getFileName());
                }
                publisher.publish(stage, target);
                stage = null;
            } catch (AtomicMoveNotSupportedException unsupported) {
                throw new IOException("CATMAP atlas requires an atomic filesystem rename; "
                        + "previous output was preserved.", unsupported);
            } finally {
                if (image != null) {
                    image.flush();
                }
                if (stage != null) {
                    Files.deleteIfExists(stage);
                }
            }
        }
    }

    /**
     * Export the atlas as a multi-page PDF (one page per atlas page) using the
     * document sheet size, with atomic publication and independent validation.
     */
    public static void exportPdf(LayoutModel template, List<AtlasPage> pages,
                                 File file, int dpi) throws Exception {
        Objects.requireNonNull(template, "CATMAP atlas template");
        Objects.requireNonNull(pages, "CATMAP atlas pages");
        Objects.requireNonNull(file, "CATMAP atlas destination");
        if (pages.isEmpty() || pages.size() > MAX_PDF_PAGES) {
            throw new IllegalArgumentException("CATMAP atlas PDF requires between 1 and "
                    + MAX_PDF_PAGES + " pages.");
        }
        PageSetup setup = setup(dpi, template.getPageSpec());
        LayoutPageSpec spec = template.getPageSpec();
        double widthMm = spec.widthMm();
        double heightMm = spec.heightMm();
        try (org.apache.pdfbox.pdmodel.PDDocument document =
                     new org.apache.pdfbox.pdmodel.PDDocument()) {
            document.getDocumentInformation().setCreator("CATGIS Desktop");
            document.getDocumentInformation().setTitle("CATMAP Atlas");
            for (int i = 0; i < pages.size(); i++) {
                BufferedImage pageImage = renderPage(template, pages.get(i), setup, i);
                try {
                    BufferedImage rgb = new BufferedImage(
                            pageImage.getWidth(), pageImage.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = rgb.createGraphics();
                    try {
                        g.setColor(Color.WHITE);
                        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
                        g.drawImage(pageImage, 0, 0, null);
                    } finally {
                        g.dispose();
                    }
                    org.apache.pdfbox.pdmodel.common.PDRectangle rect =
                            new org.apache.pdfbox.pdmodel.common.PDRectangle(
                                    (float) (widthMm * 72.0 / 25.4),
                                    (float) (heightMm * 72.0 / 25.4));
                    org.apache.pdfbox.pdmodel.PDPage page =
                            new org.apache.pdfbox.pdmodel.PDPage(rect);
                    document.addPage(page);
                    org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject pdfImage =
                            org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
                                    .createFromImage(document, rgb);
                    try (org.apache.pdfbox.pdmodel.PDPageContentStream cs =
                                 new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page)) {
                        cs.drawImage(pdfImage, 0, 0, rect.getWidth(), rect.getHeight());
                    }
                    rgb.flush();
                } finally {
                    pageImage.flush();
                }
            }
            Path requested = file.toPath().toAbsolutePath().normalize();
            Path parent = requested.getParent();
            if (parent == null || !Files.isDirectory(parent)) {
                throw new IOException("El directorio de exportacion CATMAP no existe.");
            }
            Path target = parent.toRealPath().resolve(requested.getFileName());
            CatmapAtomicExport.publish(target.toFile(),
                    staged -> document.save(staged.toFile()),
                    staged -> CatmapAtomicExport.verifyPdf(staged, widthMm, heightMm,
                            pages.size()));
        }
    }

    public record AtlasPage(
            String title,
            String subtitle,
            Double extentMinX,
            Double extentMinY,
            Double extentMaxX,
            Double extentMaxY,
            Double bufferPercent
    ) {
        /** Atlas page without a feature extent (numbered fallback page). */
        public AtlasPage(String title, String subtitle) {
            this(title, subtitle, null, null, null, null, null);
        }

        /** Atlas page framed from a feature extent with the default buffer. */
        public AtlasPage(String title, String subtitle,
                         Double extentMinX, Double extentMinY,
                         Double extentMaxX, Double extentMaxY) {
            this(title, subtitle, extentMinX, extentMinY, extentMaxX, extentMaxY, null);
        }
    }
}
