package ar.com.catgis.catmap;

import ar.com.catgis.CatgisLogger;

import ar.com.catgis.layout.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Serializer/deserializer for .catmap layout files.
 * Format: simple key-value text format.
 */
public final class CatmapSerializer {

    private CatmapSerializer() {}

    private static final int MAX_PAGES = 200;
    private static final int MAX_ELEMENTS = 25000;
    private static final long MAX_LAYOUT_FILE_BYTES = 128L * 1024L * 1024L;
    private static final int MAX_EMBEDDED_PNG_BYTES = 24 * 1024 * 1024;
    private static final long MAX_EMBEDDED_IMAGE_PIXELS = 32_000_000L;

    @FunctionalInterface
    interface EmbeddedImageEncoder {
        byte[] encode(BufferedImage image) throws IOException;
    }

    @FunctionalInterface
    interface AtomicLayoutPublisher {
        void publish(java.nio.file.Path staging, java.nio.file.Path target) throws IOException;
    }

    private static void publishAtomically(java.nio.file.Path staging,
                                          java.nio.file.Path target) throws IOException {
        java.nio.file.Files.move(staging, target,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private static void validateLayoutTarget(java.nio.file.Path path) throws IOException {
        if (java.nio.file.Files.isSymbolicLink(path)
                || (java.nio.file.Files.exists(path, java.nio.file.LinkOption.NOFOLLOW_LINKS)
                    && !java.nio.file.Files.isRegularFile(path, java.nio.file.LinkOption.NOFOLLOW_LINKS))) {
            throw new IOException("CATMAP no puede sobrescribir un enlace simbolico o un destino no regular: "
                    + path.getFileName());
        }
    }

    /**
     * Fail-closed atomic layout persistence. Never rename away the previous
     * target before the new layout is completely written and staged. The
     * existing backup is replaced atomically with a copy of the last valid
     * target and the new target is only published with ATOMIC_MOVE.
     */
    public static void save(LayoutModel model, File file) throws IOException {
        save(model, file, CatmapSerializer::publishAtomically);
    }

    /**
     * Test seam for unsupported atomic moves and injected publication failure.
     * This also verifies that owned .part files are removed without touching
     * a previous good .catmap or blindly deleting a previous backup.
     */
    static void save(LayoutModel model, File file, AtomicLayoutPublisher publisher) throws IOException {
        save(model, file, publisher, CatmapSerializer::encodeEmbeddedPng);
    }

    /** Failure injection for embedded-image encoding before target publication. */
    static void save(LayoutModel model, File file, AtomicLayoutPublisher publisher,
                     EmbeddedImageEncoder imageEncoder) throws IOException {
        java.util.Objects.requireNonNull(model, "CATMAP layout model");
        java.util.Objects.requireNonNull(file, "CATMAP layout destination");
        java.util.Objects.requireNonNull(publisher, "CATMAP atomic publisher");
        java.util.Objects.requireNonNull(imageEncoder, "CATMAP embedded PNG encoder");
        int pageCount = model.getPageCount();
        if (pageCount < 1 || pageCount > MAX_PAGES) {
            throw new IOException("CATMAP page count must be between 1 and " + MAX_PAGES);
        }
        int count = 0;
        for (int page = 0; page < pageCount; page++) {
            count += model.getPageElements(page).size();
            if (count > MAX_ELEMENTS) {
                throw new IOException("CATMAP layout has too many elements.");
            }
        }

        java.nio.file.Path requested = file.toPath().toAbsolutePath().normalize();
        java.nio.file.Path parent = requested.getParent();
        if (parent == null || !java.nio.file.Files.isDirectory(parent)) {
            throw new IOException("No existe el directorio donde guardar CATMAP.");
        }
        parent = parent.toRealPath();
        java.nio.file.Path target = parent.resolve(requested.getFileName());
        java.nio.file.Path backup = parent.resolve(requested.getFileName().toString() + ".bak");
        validateLayoutTarget(target);
        validateLayoutTarget(backup);

        java.nio.file.Path staged = java.nio.file.Files.createTempFile(parent,
                "." + target.getFileName() + "-", ".part");
        java.nio.file.Path stagedBackup = null;
        try {
            try (BufferedWriter w = java.nio.file.Files.newBufferedWriter(staged, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                    java.nio.file.StandardOpenOption.WRITE)) {
                boolean multipage = pageCount > 1;
                w.write(multipage ? "# CATMAP Layout v2" : "# CATMAP Layout v1");
                w.newLine();
                LayoutPageSpec pageSpec = model.getPageSpec();
                w.write("PAGE_SIZE=" + pageSpec.presetName());
                w.newLine();
                w.write("PAGE_ORIENTATION=" + pageSpec.orientation().name());
                w.newLine();
                if (!LayoutPageSpec.A4_LANDSCAPE.equals(pageSpec)) {
                    writePageGeometry(w, pageSpec);
                }
                if (model.getAtlasCoverageLayer() != null) {
                    w.write("ATLAS_COVERAGE_LAYER=" + model.getAtlasCoverageLayer());
                    w.newLine();
                    w.write("ATLAS_NAME_FIELD="
                            + (model.getAtlasNameField() != null ? model.getAtlasNameField() : ""));
                    w.newLine();
                    w.write("ATLAS_BUFFER_PERCENT=" + model.getAtlasBufferPercent());
                    w.newLine();
                    w.write("ATLAS_ORDER=" + (model.isAtlasOrderAscending() ? "ASC" : "DESC"));
                    w.newLine();
                }
                if (multipage) {
                    w.write("PAGE_COUNT=" + pageCount);
                    w.newLine();
                    w.write("CURRENT_PAGE=" + model.getCurrentPage());
                    w.newLine();
                    for (int page = 0; page < pageCount; page++) {
                        w.write("PAGE_BEGIN=" + page);
                        w.newLine();
                        for (LayoutElement element : model.getPageElements(page)) {
                            w.write(serializeElement(element, imageEncoder));
                            w.newLine();
                        }
                        w.write("PAGE_END=" + page);
                        w.newLine();
                    }
                } else {
                    // Remain byte-compatible with existing v1 single-page files.
                    for (LayoutElement element : model.getElements()) {
                        w.write(serializeElement(element, imageEncoder));
                        w.newLine();
                    }
                }
                w.write("# End of layout");
                w.newLine();
            }
            // Flush staged bytes before publication. Directory fsync and
            // real power-cut recovery are separate cross-platform gates.
            try (java.nio.channels.FileChannel channel = java.nio.channels.FileChannel.open(
                    staged, java.nio.file.StandardOpenOption.WRITE)) {
                channel.force(true);
            }

            validateLayoutTarget(target);
            validateLayoutTarget(backup);
            if (java.nio.file.Files.exists(target, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
                stagedBackup = java.nio.file.Files.createTempFile(parent,
                        "." + target.getFileName() + "-backup-", ".part");
                java.nio.file.Files.copy(target, stagedBackup,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.LinkOption.NOFOLLOW_LINKS);
                try (java.nio.channels.FileChannel channel = java.nio.channels.FileChannel.open(
                        stagedBackup, java.nio.file.StandardOpenOption.WRITE)) {
                    channel.force(true);
                }
                validateLayoutTarget(backup);
                publisher.publish(stagedBackup, backup);
                stagedBackup = null;
            }
            validateLayoutTarget(target);
            publisher.publish(staged, target);
            staged = null;
        } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
            throw new IOException("El sistema de archivos no permite guardar CATMAP de forma atomica; "
                    + "el diseno anterior se conservo.", unsupported);
        } finally {
            if (staged != null) {
                java.nio.file.Files.deleteIfExists(staged);
            }
            if (stagedBackup != null) {
                java.nio.file.Files.deleteIfExists(stagedBackup);
            }
        }
    }

    /**
     * Load layout from .catmap file.
     */
    public static LayoutModel load(File file) throws IOException {
        java.util.Objects.requireNonNull(file, "CATMAP layout source");
        if (java.nio.file.Files.size(file.toPath()) > MAX_LAYOUT_FILE_BYTES) {
            throw new IOException("CATMAP layout exceeds the maximum supported file size.");
        }
        LayoutModel model = new LayoutModel();
        PageMetadata pageMetadata = new PageMetadata();
        boolean v2 = false;
        int expectedPages = -1;
        int selectedPage = 0;
        int activePage = -1;
        int completedPages = 0;
        int elementCount = 0;

        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if ("# CATMAP Layout v2".equals(trimmed)) {
                    v2 = true;
                    continue;
                }
                if (trimmed.startsWith("#")) {
                    continue;
                }
                if (!v2) {
                    if (trimmed.startsWith("PAGE_BEGIN=") || trimmed.startsWith("PAGE_END=")
                            || trimmed.startsWith("PAGE_COUNT=")) {
                        throw new IOException("CATMAP multi-page document is missing its v2 header.");
                    }
                    if (applyAtlasMetadata(model, trimmed)) {
                        continue;
                    }
                    if (pageMetadata.parse(trimmed)) {
                        continue;
                    }
                    if (trimmed.startsWith("PAGE_")) {
                        continue; // Backward-compatible v1 page metadata.
                    }
                    LayoutElement element = parseElement(trimmed);
                    if (element != null) {
                        model.addElement(element);
                        if (++elementCount > MAX_ELEMENTS) {
                            throw new IOException("CATMAP layout exceeds element count limit.");
                        }
                    }
                    continue;
                }
                if (trimmed.startsWith("PAGE_COUNT=")) {
                    if (expectedPages >= 0 || activePage != -1) {
                        throw new IOException("CATMAP v2 page count repeated or out of order.");
                    }
                    expectedPages = parseStrictPageNumber(trimmed.substring(11), MAX_PAGES);
                    if (expectedPages < 1) {
                        throw new IOException("CATMAP v2 must contain at least one page.");
                    }
                } else if (trimmed.startsWith("CURRENT_PAGE=")) {
                    if (activePage != -1 || completedPages > 0) {
                        throw new IOException("CATMAP v2 current page out of order.");
                    }
                    selectedPage = parseStrictPageNumber(trimmed.substring(13), MAX_PAGES);
                } else if (trimmed.startsWith("PAGE_BEGIN=")) {
                    int page = parseStrictPageNumber(trimmed.substring(11), MAX_PAGES);
                    if (expectedPages < 1 || activePage != -1 || page != completedPages
                            || page >= expectedPages) {
                        throw new IOException("CATMAP v2 page begin sequence is invalid.");
                    }
                    if (page > 0) {
                        model.addPage();
                    }
                    model.setCurrentPage(page);
                    activePage = page;
                } else if (trimmed.startsWith("PAGE_END=")) {
                    int page = parseStrictPageNumber(trimmed.substring(9), MAX_PAGES);
                    if (activePage != page) {
                        throw new IOException("CATMAP v2 page end does not match page begin.");
                    }
                    activePage = -1;
                    completedPages++;
                } else if (applyAtlasMetadata(model, trimmed)) {
                } else if (pageMetadata.parse(trimmed)) {
                    if (activePage != -1) {
                        throw new IOException("CATMAP v2 page metadata inside element section.");
                    }
                } else if (trimmed.startsWith("ELEMENT|")) {
                    if (activePage < 0) {
                        throw new IOException("CATMAP v2 element outside a page section.");
                    }
                    LayoutElement element = parseElement(trimmed);
                    if (element == null) {
                        throw new IOException("CATMAP v2 has an unsupported or corrupt element.");
                    }
                    if (element instanceof LayoutImage image
                            && trimmed.contains("|IMAGE_DATA=")
                            && !trimmed.endsWith("|IMAGE_DATA=")
                            && image.getImage() == null) {
                        throw new IOException("CATMAP v2 contains undecodable embedded image data.");
                    }
                    model.getCurrentPageElements().add(element);
                    if (++elementCount > MAX_ELEMENTS) {
                        throw new IOException("CATMAP layout exceeds element count limit.");
                    }
                } else {
                    throw new IOException("CATMAP v2 contains unrecognized layout metadata.");
                }
            }
        }

        if (v2) {
            if (expectedPages < 1 || activePage != -1 || completedPages != expectedPages
                    || selectedPage >= expectedPages) {
                throw new IOException("CATMAP v2 is truncated or has an invalid page count.");
            }
            model.setCurrentPage(selectedPage);
        }
        model.setPageSpec(pageMetadata.toSpec());
        return model;
    }

    private static boolean applyAtlasMetadata(LayoutModel model, String line) throws IOException {
        if (!line.startsWith("ATLAS_")) {
            return false;
        }
        int eq = line.indexOf('=');
        if (eq < 0) {
            throw new IOException("CATMAP atlas metadata is malformed: " + line);
        }
        String key = line.substring(0, eq);
        String value = line.substring(eq + 1).trim();
        switch (key) {
            case "ATLAS_COVERAGE_LAYER" -> model.setAtlasCoverageLayer(value);
            case "ATLAS_NAME_FIELD" -> model.setAtlasNameField(value);
            case "ATLAS_BUFFER_PERCENT" -> {
                try {
                    model.setAtlasBufferPercent(Double.parseDouble(value));
                } catch (IllegalArgumentException invalid) {
                    throw new IOException("CATMAP atlas buffer is invalid: " + value, invalid);
                }
            }
            case "ATLAS_ORDER" -> {
                if ("ASC".equalsIgnoreCase(value)) {
                    model.setAtlasOrderAscending(true);
                } else if ("DESC".equalsIgnoreCase(value)) {
                    model.setAtlasOrderAscending(false);
                } else {
                    throw new IOException("CATMAP atlas order is invalid: " + value);
                }
            }
            default -> throw new IOException("CATMAP atlas metadata is unknown: " + key);
        }
        return true;
    }

    private static void writePageGeometry(BufferedWriter writer, LayoutPageSpec pageSpec)
            throws IOException {
        writer.write("PAGE_WIDTH_MM=" + pageSpec.portraitWidthMm());
        writer.newLine();
        writer.write("PAGE_HEIGHT_MM=" + pageSpec.portraitHeightMm());
        writer.newLine();
        if (pageSpec.hasMargins()) {
            writer.write("PAGE_MARGINS_MM=" + pageSpec.marginTopMm() + ","
                    + pageSpec.marginRightMm() + "," + pageSpec.marginBottomMm() + ","
                    + pageSpec.marginLeftMm());
            writer.newLine();
        }
    }

    /**
     * Parses the page-geometry keys of the document header. Legacy files only
     * carry PAGE_SIZE/PAGE_ORIENTATION and load as their preset; new files also
     * carry explicit portrait millimetres and optional margins.
     */
    private static final class PageMetadata {
        private String preset;
        private PageOrientation orientation;
        private Double widthMm;
        private Double heightMm;
        private double[] margins;
        private boolean marginSeen;

        boolean parse(String line) throws IOException {
            if (line.startsWith("PAGE_SIZE=")) {
                if (preset != null) {
                    throw new IOException("CATMAP page size is repeated.");
                }
                preset = line.substring("PAGE_SIZE=".length()).trim().toUpperCase(java.util.Locale.ROOT);
                if (!LayoutPageSpec.CUSTOM_PRESET.equals(preset)) {
                    try {
                        PageSizePreset.valueOf(preset);
                    } catch (IllegalArgumentException invalid) {
                        throw new IOException("CATMAP page size is not supported: " + preset, invalid);
                    }
                }
                return true;
            }
            if (line.startsWith("PAGE_ORIENTATION=")) {
                if (orientation != null) {
                    throw new IOException("CATMAP page orientation is repeated.");
                }
                String value = line.substring("PAGE_ORIENTATION=".length())
                        .trim().toUpperCase(java.util.Locale.ROOT);
                try {
                    orientation = PageOrientation.valueOf(value);
                } catch (IllegalArgumentException invalid) {
                    throw new IOException("CATMAP page orientation is not supported: " + value, invalid);
                }
                return true;
            }
            if (line.startsWith("PAGE_WIDTH_MM=")) {
                widthMm = parseMillimetres(line.substring("PAGE_WIDTH_MM=".length()), "page width");
                return true;
            }
            if (line.startsWith("PAGE_HEIGHT_MM=")) {
                heightMm = parseMillimetres(line.substring("PAGE_HEIGHT_MM=".length()), "page height");
                return true;
            }
            if (line.startsWith("PAGE_MARGINS_MM=")) {
                margins = parseMargins(line.substring("PAGE_MARGINS_MM=".length()));
                marginSeen = true;
                return true;
            }
            return false;
        }

        LayoutPageSpec toSpec() throws IOException {
            if (preset == null) {
                if (orientation != null || widthMm != null || heightMm != null || marginSeen) {
                    throw new IOException("CATMAP page geometry is missing PAGE_SIZE.");
                }
                return LayoutPageSpec.A4_LANDSCAPE;
            }
            PageOrientation actualOrientation = orientation != null
                    ? orientation : PageOrientation.LANDSCAPE;
            try {
                LayoutPageSpec spec;
                if (LayoutPageSpec.CUSTOM_PRESET.equals(preset)) {
                    if (widthMm == null || heightMm == null) {
                        throw new IOException(
                                "CATMAP custom page requires PAGE_WIDTH_MM and PAGE_HEIGHT_MM.");
                    }
                    spec = LayoutPageSpec.custom(widthMm, heightMm, actualOrientation);
                } else {
                    PageSizePreset size = PageSizePreset.valueOf(preset);
                    if (widthMm != null && Math.abs(widthMm - size.widthMm) > 0.01) {
                        throw new IOException("CATMAP page width does not match PAGE_SIZE=" + preset);
                    }
                    if (heightMm != null && Math.abs(heightMm - size.heightMm) > 0.01) {
                        throw new IOException("CATMAP page height does not match PAGE_SIZE=" + preset);
                    }
                    spec = LayoutPageSpec.preset(size, actualOrientation);
                }
                if (marginSeen) {
                    spec = spec.withMargins(margins[0], margins[1], margins[2], margins[3]);
                }
                return spec;
            } catch (IllegalArgumentException invalid) {
                throw new IOException("CATMAP page geometry is invalid: " + invalid.getMessage(), invalid);
            }
        }

        private static double parseMillimetres(String text, String label) throws IOException {
            try {
                return Double.parseDouble(text.trim());
            } catch (NumberFormatException invalid) {
                throw new IOException("CATMAP " + label + " is not a valid number: " + text, invalid);
            }
        }

        private static double[] parseMargins(String text) throws IOException {
            String[] parts = text.trim().split(",");
            if (parts.length != 4) {
                throw new IOException("CATMAP page margins require four comma-separated values.");
            }
            double[] values = new double[4];
            for (int i = 0; i < 4; i++) {
                values[i] = parseMillimetres(parts[i], "page margin");
            }
            return values;
        }
    }

    private static int parseStrictPageNumber(String text, int max) throws IOException {
        try {
            int value = Integer.parseInt(text.trim());
            if (value < 0 || value > max) {
                throw new IOException("CATMAP page number is outside allowed range.");
            }
            return value;
        } catch (NumberFormatException invalid) {
            throw new IOException("CATMAP page number is malformed.", invalid);
        }
    }

    // Public entry for undo/redo in LayoutModel.
    public static String serializeElementRaw(LayoutElement el) {
        try {
            return serializeElement(el, CatmapSerializer::encodeEmbeddedPng);
        } catch (IOException failure) {
            throw new UncheckedIOException("CATMAP image could not be serialized.", failure);
        }
    }

    public static LayoutElement parseElementRaw(String line) {
        return parseElement(line);
    }

    private static String serializeElement(LayoutElement el, EmbeddedImageEncoder encoder)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("ELEMENT|");
        sb.append(el.getClass().getSimpleName()).append("|");
        sb.append(el.getId()).append("|");
        sb.append(el.getName()).append("|");
        sb.append(el.getBoundsMm().x).append("|");
        sb.append(el.getBoundsMm().y).append("|");
        sb.append(el.getBoundsMm().width).append("|");
        sb.append(el.getBoundsMm().height).append("|");
        sb.append(el.getZOrder()).append("|");
        sb.append(el.isVisible()).append("|");
        sb.append(el.isLocked());

        // Type-specific properties
        if (el instanceof LayoutLabel label) {
            sb.append("|TEXT=").append(encodeText(label.getText()));
            sb.append("|FONT_FAMILY=").append(label.getFont().getFamily());
            sb.append("|FONT_SIZE=").append(label.getFont().getSize());
            sb.append("|FONT_STYLE=").append(label.getFont().getStyle());
            sb.append("|COLOR=").append(colorToHex(label.getColor()));
        } else if (el instanceof LayoutMap map) {
            sb.append("|SHOW_GRID=").append(map.isShowGrid());
            sb.append("|FRAME_COLOR=").append(colorToHex(map.getFrameColor()));
            sb.append("|FRAME_WIDTH=").append(map.getFrameWidth());
            sb.append("|ASSIGNED_LAYERS=").append(encodeText(String.join(";", map.getAssignedLayerKeys())));
            sb.append("|GRID_COLS=").append(map.getGridCols());
            sb.append("|GRID_ROWS=").append(map.getGridRows());
            sb.append("|GRID_BY_DISTANCE=").append(map.isGridByDistance());
            sb.append("|GRID_INTERVAL_X=").append(map.getGridIntervalX());
            sb.append("|GRID_INTERVAL_Y=").append(map.getGridIntervalY());
            sb.append("|GRID_UNIT=").append(encodeText(map.getGridUnit()));
            sb.append("|GRID_COLOR=").append(colorToHex(map.getGridColor()));
            sb.append("|TARGET_SCALE=").append(map.getTargetScaleDenominator());
            sb.append("|OWN_EXTENT=").append(map.isOwnExtent());
            if (map.isOwnExtent()) {
                sb.append("|VIEW_MIN_X=").append(map.getOwnViewMinX());
                sb.append("|VIEW_MIN_Y=").append(map.getOwnViewMinY());
                sb.append("|ZOOM_FACTOR=").append(map.getOwnZoomFactor());
            }
        } else if (el instanceof LayoutLegend legend) {
            sb.append("|AUTO_HEIGHT=").append(legend.isAutoHeight());
            sb.append("|SHOW_BACKGROUND=").append(legend.isShowBackground());
            sb.append("|SHOW_BORDER=").append(legend.isShowBorder());
            sb.append("|TITLE=").append(encodeText(legend.getTitle()));
            sb.append("|TITLE_FONT_SIZE=").append(legend.getTitleFont().getSize2D());
            sb.append("|ITEM_FONT_SIZE=").append(legend.getItemFont().getSize2D());
        } else if (el instanceof LayoutScaleBar scale) {
            sb.append("|SCALE_DENOMINATOR=").append(scale.getMapScaleDenominator());
            sb.append("|SEGMENTS=").append(scale.getSegments());
        } else if (el instanceof LayoutNorthArrow) {
            // No extra properties needed
        } else if (el instanceof LayoutCartouche cartouche) {
            for (var entry : cartouche.getFields().entrySet()) {
                sb.append("|FIELD_").append(entry.getKey()).append("=").append(encodeText(entry.getValue()));
            }
        } else if (el instanceof LayoutImage image) {
            BufferedImage bi = image.getImage();
            if (bi != null) {
                byte[] bytes = encoder.encode(bi);
                validateEncodedPng(bytes);
                sb.append("|IMAGE_DATA=").append(Base64.getEncoder().encodeToString(bytes));
            } else {
                sb.append("|IMAGE_DATA=");
            }
        } else if (el instanceof LayoutTable table) {
            sb.append("|MAX_ROWS=").append(table.getMaxVisibleRows());
        }

        return sb.toString();
    }

    private static byte[] encodeEmbeddedPng(BufferedImage image) throws IOException {
        if (image.getWidth() <= 0 || image.getHeight() <= 0
                || (long) image.getWidth() * image.getHeight() > MAX_EMBEDDED_IMAGE_PIXELS) {
            throw new IOException("CATMAP embedded image has unsupported dimensions.");
        }
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", output)) {
            throw new IOException("CATMAP PNG image encoder is unavailable.");
        }
        byte[] bytes = output.toByteArray();
        validateEncodedPng(bytes);
        return bytes;
    }

    private static void validateEncodedPng(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length < 24 || bytes.length > MAX_EMBEDDED_PNG_BYTES) {
            throw new IOException("CATMAP embedded PNG is missing or exceeds size limits.");
        }
        byte[] magic = {(byte) 137, 80, 78, 71, 13, 10, 26, 10};
        for (int i = 0; i < magic.length; i++) {
            if (bytes[i] != magic[i]) {
                throw new IOException("CATMAP embedded image is not PNG.");
            }
        }
        if (bytes[12] != 'I' || bytes[13] != 'H' || bytes[14] != 'D' || bytes[15] != 'R') {
            throw new IOException("CATMAP embedded image has no PNG header.");
        }
        int width = java.nio.ByteBuffer.wrap(bytes, 16, 4).getInt();
        int height = java.nio.ByteBuffer.wrap(bytes, 20, 4).getInt();
        if (width <= 0 || height <= 0 || (long) width * height > MAX_EMBEDDED_IMAGE_PIXELS) {
            throw new IOException("CATMAP embedded PNG dimensions exceed safety limits.");
        }
    }

    private static LayoutElement parseElement(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 10) {
            CatgisLogger.warn("CatmapSerializer: linea malformada (partes=" + parts.length
                    + "), omitiendo: " + (line.length() > 80 ? line.substring(0, 80) + "..." : line), null);
            return null;
        }

        String type = parts[1];
        String id = parts[2];
        String name = parts[3];
        double x = parseDouble(parts[4]);
        double y = parseDouble(parts[5]);
        double w = parseDouble(parts[6]);
        double h = parseDouble(parts[7]);
        int zOrder = parseInt(parts[8]);
        boolean visible = parseBoolean(parts[9]);
        boolean locked = parts.length > 10 ? parseBoolean(parts[10]) : false;

        if (w <= 0 || h <= 0) {
            CatgisLogger.warn("CatmapSerializer: elemento '" + id + "' (tipo=" + type
                    + ") tiene tamanio invalido w=" + w + " h=" + h + ", posible corrupcion de archivo", null);
        }

        LayoutElement element = switch (type) {
            case "LayoutLabel" -> {
                LayoutLabel label = new LayoutLabel(id, "", x, y, w, h);
                yield label;
            }
            case "LayoutMap" -> {
                LayoutMap map = new LayoutMap(id, x, y, w, h);
                yield map;
            }
            case "LayoutLegend" -> {
                LayoutLegend legend = new LayoutLegend(id, x, y, w, h);
                legend.setAutoHeight(true);
                yield legend;
            }
            case "LayoutScaleBar" -> {
                LayoutScaleBar scale = new LayoutScaleBar(id, x, y, w, h);
                yield scale;
            }
            case "LayoutNorthArrow" -> {
                LayoutNorthArrow north = new LayoutNorthArrow(id, x, y, w, h);
                yield north;
            }
            case "LayoutCartouche" -> {
                LayoutCartouche cartouche = new LayoutCartouche(id, x, y, w, h);
                yield cartouche;
            }
            case "LayoutRectangle" -> {
                LayoutRectangle rect = new LayoutRectangle(id, x, y, w, h);
                yield rect;
            }
            case "LayoutEllipse" -> {
                LayoutEllipse ellipse = new LayoutEllipse(id, x, y, w, h);
                yield ellipse;
            }
            case "LayoutLine" -> {
                LayoutLine ln = new LayoutLine(id, x, y, w, h);
                yield ln;
            }
            case "LayoutImage" -> {
                LayoutImage img = new LayoutImage(id, null, x, y, w, h);
                yield img;
            }
            case "LayoutTable" -> {
                LayoutTable tbl = new LayoutTable(id, x, y, w, h);
                yield tbl;
            }
            default -> null;
        };

        if (element == null) return null;

        element.setName(name);
        element.setZOrder(zOrder);
        element.setVisible(visible);
        element.setLocked(locked);

        // Parse type-specific properties
        for (int i = 11; i < parts.length; i++) {
            String part = parts[i];
            if (part.startsWith("TEXT=")) {
                if (element instanceof LayoutLabel label) {
                    label.setText(decodeText(part.substring(5)));
                }
            } else if (part.startsWith("FONT_FAMILY=")) {
                if (element instanceof LayoutLabel label) {
                    String family = part.substring(12);
                    label.setFont(new Font(family, label.getFont().getStyle(), label.getFont().getSize()));
                }
            } else if (part.startsWith("FONT_SIZE=")) {
                if (element instanceof LayoutLabel label) {
                    int size = parseInt(part.substring(10));
                    label.setFont(new Font(label.getFont().getFamily(), label.getFont().getStyle(), size));
                }
            } else if (part.startsWith("FONT_STYLE=")) {
                if (element instanceof LayoutLabel label) {
                    int style = parseInt(part.substring(11));
                    label.setFont(new Font(label.getFont().getFamily(), style, label.getFont().getSize()));
                }
            } else if (part.startsWith("COLOR=")) {
                if (element instanceof LayoutLabel label) {
                    label.setColor(parseColor(part.substring(6)));
                }
            } else if (part.startsWith("SHOW_GRID=")) {
                if (element instanceof LayoutMap map) {
                    map.setShowGrid(parseBoolean(part.substring(10)));
                }
            } else if (part.startsWith("FRAME_COLOR=")) {
                if (element instanceof LayoutMap map) {
                    map.setFrameColor(parseColor(part.substring(12)));
                }
            } else if (part.startsWith("FRAME_WIDTH=")) {
                if (element instanceof LayoutMap map) {
                    map.setFrameWidth((float) parseDouble(part.substring(12)));
                }
            } else if (part.startsWith("ASSIGNED_LAYERS=")) {
                if (element instanceof LayoutMap map) {
                    String encoded = decodeText(part.substring(16));
                    java.util.List<String> layerKeys = new java.util.ArrayList<>();
                    if (!encoded.isBlank()) {
                        for (String token : encoded.split(";")) {
                            if (token != null) {
                                String trimmed = token.trim();
                                if (!trimmed.isBlank()) {
                                    layerKeys.add(trimmed);
                                }
                            }
                        }
                    }
                    map.setAssignedLayerKeys(layerKeys);
                }
            } else if (part.startsWith("OWN_EXTENT=")) {
                if (element instanceof LayoutMap map) {
                    map.setOwnExtent(parseBoolean(part.substring(11)));
                }
            } else if (part.startsWith("GRID_COLS=")) {
                if (element instanceof LayoutMap map) {
                    map.setGridCols(parseInt(part.substring(10)));
                }
            } else if (part.startsWith("GRID_ROWS=")) {
                if (element instanceof LayoutMap map) {
                    map.setGridRows(parseInt(part.substring(10)));
                }
            } else if (part.startsWith("GRID_BY_DISTANCE=")) {
                if (element instanceof LayoutMap map) {
                    map.setGridByDistance(parseBoolean(part.substring(17)));
                }
            } else if (part.startsWith("GRID_INTERVAL_X=")) {
                if (element instanceof LayoutMap map) {
                    map.setGridIntervalX(parseDouble(part.substring(16)));
                }
            } else if (part.startsWith("GRID_INTERVAL_Y=")) {
                if (element instanceof LayoutMap map) {
                    map.setGridIntervalY(parseDouble(part.substring(16)));
                }
            } else if (part.startsWith("GRID_UNIT=")) {
                if (element instanceof LayoutMap map) {
                    map.setGridUnit(decodeText(part.substring(10)));
                }
            } else if (part.startsWith("GRID_COLOR=")) {
                if (element instanceof LayoutMap map) {
                    map.setGridColor(parseColor(part.substring(11)));
                }
            } else if (part.startsWith("TARGET_SCALE=")) {
                if (element instanceof LayoutMap map) {
                    map.setTargetScaleDenominator(parseDouble(part.substring(13)));
                }
            } else if (part.startsWith("SHOW_BORDER=")) {
                if (element instanceof LayoutLegend legend) {
                    legend.setShowBorder(parseBoolean(part.substring(12)));
                }
            } else if (part.startsWith("TITLE_FONT_SIZE=")) {
                if (element instanceof LayoutLegend legend) {
                    int size = parseInt(part.substring(16));
                    legend.setTitleFont(new Font(legend.getTitleFont().getFamily(),
                            legend.getTitleFont().getStyle(), size));
                }
            } else if (part.startsWith("ITEM_FONT_SIZE=")) {
                if (element instanceof LayoutLegend legend) {
                    int size = parseInt(part.substring(15));
                    legend.setItemFont(new Font(legend.getItemFont().getFamily(),
                            legend.getItemFont().getStyle(), size));
                }
            } else if (part.startsWith("VIEW_MIN_X=")) {
                if (element instanceof LayoutMap map) {
                    map.setOwnViewMinX(parseDouble(part.substring(11)));
                }
            } else if (part.startsWith("VIEW_MIN_Y=")) {
                if (element instanceof LayoutMap map) {
                    map.setOwnViewMinY(parseDouble(part.substring(11)));
                }
            } else if (part.startsWith("ZOOM_FACTOR=")) {
                if (element instanceof LayoutMap map) {
                    map.setOwnZoomFactor(parseDouble(part.substring(12)));
                }
            } else if (part.startsWith("AUTO_HEIGHT=")) {
                if (element instanceof LayoutLegend legend) {
                    legend.setAutoHeight(parseBoolean(part.substring(12)));
                }
            } else if (part.startsWith("SHOW_BACKGROUND=")) {
                if (element instanceof LayoutLegend legend) {
                    legend.setShowBackground(parseBoolean(part.substring(15)));
                }
            } else if (part.startsWith("TITLE=")) {
                if (element instanceof LayoutLegend legend) {
                    legend.setTitle(decodeText(part.substring(6)));
                }
            } else if (part.startsWith("SCALE_DENOMINATOR=")) {
                if (element instanceof LayoutScaleBar scale) {
                    scale.setMapScaleDenominator(parseDouble(part.substring(18)));
                }
            } else if (part.startsWith("SEGMENTS=")) {
                if (element instanceof LayoutScaleBar scale) {
                    scale.setSegments(parseInt(part.substring(9)));
                }
            } else if (part.startsWith("IMAGE_DATA=")) {
                if (element instanceof LayoutImage img) {
                    String data = part.substring(11);
                    if (!data.isEmpty()) {
                        try {
                            if (data.length() > ((MAX_EMBEDDED_PNG_BYTES + 2L) / 3L) * 4L) {
                                throw new IOException("CATMAP base64 image exceeds configured budget.");
                            }
                            byte[] bytes = Base64.getDecoder().decode(data);
                            validateEncodedPng(bytes);
                            BufferedImage bi = ImageIO.read(new java.io.ByteArrayInputStream(bytes));
                            if (bi == null) {
                                throw new IOException("CATMAP PNG decoder returned no image.");
                            }
                            img.setImage(bi);
                        } catch (Exception failed) {
                            // Legacy v1 remains tolerant. The v2 page loader
                            // rejects nonempty but undecodable IMAGE_DATA.
                            CatgisLogger.warn("CatmapSerializer: no se pudo decodificar IMAGE_DATA", failed);
                        }
                    }
                }
            } else if (part.startsWith("FIELD_")) {
                if (element instanceof LayoutCartouche cartouche) {
                    int eq = part.indexOf('=');
                    if (eq > 6) {
                        String key = part.substring(6, eq);
                        String value = decodeText(part.substring(eq + 1));
                        cartouche.setField(key, value);
                    }
                }
            }
        }

        return element;
    }

    // --- Utility methods ---

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) {
            CatgisLogger.warn("CatmapSerializer: valor decimal invalido '" + s + "', usando 0", null);
            return 0;
        }
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) {
            CatgisLogger.warn("CatmapSerializer: valor entero invalido '" + s + "', usando 0", null);
            return 0;
        }
    }

    private static boolean parseBoolean(String s) {
        String trimmed = s.trim();
        if ("true".equalsIgnoreCase(trimmed)) return true;
        if ("false".equalsIgnoreCase(trimmed)) return false;
        CatgisLogger.warn("CatmapSerializer: valor booleano invalido '" + s + "', usando false", null);
        return false;
    }

    private static Color parseColor(String s) {
        try {
            String[] parts = s.split(",");
            if (parts.length >= 3) {
                return new Color(
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim())
                );
            }
        } catch (Exception ignored) {
            CatgisLogger.warn("CatmapSerializer: color invalido '" + s + "', usando negro", null);
        }
        return Color.BLACK;
    }

    private static String colorToHex(Color c) {
        if (c == null) return "0,0,0";
        return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
    }

    private static String encodeText(String text) {
        if (text == null) return "";
        return text.replace("%", "%25").replace("|", "%7C").replace("\n", "%0A");
    }

    private static String decodeText(String text) {
        if (text == null) return "";
        return text.replace("%7C", "|").replace("%0A", "\n").replace("%25", "%");
    }
}
