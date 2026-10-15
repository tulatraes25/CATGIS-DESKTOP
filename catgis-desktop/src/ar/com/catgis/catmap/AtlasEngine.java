package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutElement;
import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
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

    private AtlasEngine() {}

    @FunctionalInterface
    interface AtomicAtlasPublisher {
        void publish(Path staging, Path target) throws IOException;
    }

    private record PageSetup(LayoutRenderContext context, int width, int height,
                             long bytes) {}

    private static PageSetup setup(int dpi) {
        if (dpi < MIN_DPI || dpi > MAX_DPI) {
            throw new IllegalArgumentException("CATMAP atlas: DPI must be between 36 and 450.");
        }
        LayoutRenderContext context = new LayoutRenderContext(
                LayoutRenderContext.Mode.EXPORT_IMAGE, dpi, 297, 210);
        int width = context.mmToPxInt(297);
        int height = context.mmToPxInt(210);
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
                                                        AtlasPage page) {
        Objects.requireNonNull(page, "CATMAP atlas page");
        boolean anyExtent = page.extentMinX() != null
                || page.extentMinY() != null || page.zoomFactor() != null;
        if (anyExtent) {
            if (page.extentMinX() == null || page.extentMinY() == null
                    || page.zoomFactor() == null
                    || !Double.isFinite(page.extentMinX())
                    || !Double.isFinite(page.extentMinY())
                    || !Double.isFinite(page.zoomFactor())
                    || page.zoomFactor() <= 0) {
                throw new IllegalArgumentException(
                        "CATMAP atlas page requires finite X, Y and positive zoom together.");
            }
        }

        List<LayoutElement> pageElements = new ArrayList<>();
        for (LayoutElement element : template.getVisibleElementsSortedByZ()) {
            if (element instanceof LayoutLabel original) {
                LayoutLabel copy = original.copyForAtlas();
                if ("Titulo".equals(copy.getName()) && page.title() != null) {
                    copy.setText(page.title());
                } else if ("Subtitulo".equals(copy.getName()) && page.subtitle() != null) {
                    copy.setText(page.subtitle());
                }
                pageElements.add(copy);
            } else if (element instanceof LayoutMap original) {
                LayoutMap copy = original.copyForAtlas();
                if (anyExtent) {
                    copy.setOwnExtent(true);
                    copy.setOwnViewMinX(page.extentMinX());
                    copy.setOwnViewMinY(page.extentMinY());
                    copy.setOwnZoomFactor(page.zoomFactor());
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

    private static BufferedImage renderPage(LayoutModel template, AtlasPage page,
                                            PageSetup setup) {
        List<LayoutElement> pageElements = snapshotForPage(template, page);
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
        PageSetup setup = setup(dpi);
        if (setup.bytes() * pages.size() > MAX_BATCH_BYTES) {
            throw new IllegalArgumentException(
                    "CATMAP atlas batch exceeds the in-memory image budget; export pages to files.");
        }
        List<BufferedImage> results = new ArrayList<>();
        for (AtlasPage page : pages) {
            results.add(renderPage(template, page, setup));
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

        PageSetup setup = setup(dpi);
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
                image = renderPage(template, pages.get(i), setup);
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

    public record AtlasPage(
            String title,
            String subtitle,
            Double extentMinX,
            Double extentMinY,
            Double zoomFactor
    ) {}
}
