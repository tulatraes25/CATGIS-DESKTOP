package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutElement;
import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutLegend;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.PageOrientation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Single entry point for CATMAP documents.
 *
 * <p>The canonical on-disk format is the versioned line format written by
 * {@link CatmapSerializer} (full element fidelity, atomic write, backup).
 * Documents produced by the legacy embedded composer were JSON; those files
 * are still readable through {@link #load(File)} and are migrated to the
 * canonical format the next time the document is saved.</p>
 */
public final class CatmapDocumentIO {

    private static final long MAX_DOCUMENT_BYTES = 128L * 1024L * 1024L;
    private static final int MAX_ELEMENTS = 25000;
    private static final int HEAD_BYTES = 4096;
    private static final ObjectMapper JSON = new ObjectMapper();

    private CatmapDocumentIO() {}

    /** Load a canonical CATMAP document or a legacy composer JSON document. */
    public static LayoutModel load(File file) throws IOException {
        java.util.Objects.requireNonNull(file, "CATMAP document source");
        if (!file.isFile()) {
            throw new IOException("CATMAP document does not exist: " + file);
        }
        if (Files.size(file.toPath()) > MAX_DOCUMENT_BYTES) {
            throw new IOException("CATMAP document exceeds the maximum supported file size.");
        }
        String head = readHead(file);
        if (head.startsWith("{")) {
            return loadLegacyJson(file);
        }
        if (head.startsWith("# CATMAP Layout") || head.startsWith("PAGE_")
                || head.startsWith("ELEMENT|")) {
            return CatmapSerializer.load(file);
        }
        throw new IOException("CATMAP document format is not recognized: " + file.getName());
    }

    /** Save using the canonical atomic, versioned line format. */
    public static void save(LayoutModel model, File file) throws IOException {
        CatmapSerializer.save(model, file);
    }

    private static String readHead(File file) throws IOException {
        try (java.io.InputStream in = Files.newInputStream(file.toPath())) {
            byte[] buffer = in.readNBytes(HEAD_BYTES);
            String text = new String(buffer, StandardCharsets.UTF_8);
            if (text.startsWith("\uFEFF")) {
                text = text.substring(1);
            }
            return text.stripLeading();
        }
    }

    private static LayoutModel loadLegacyJson(File file) throws IOException {
        JsonNode root = JSON.readTree(file);
        if (root == null || !root.isObject()) {
            throw new IOException("CATMAP legacy document is not a JSON object.");
        }
        String format = root.path("format").asText("");
        if (!"catmap".equals(format)) {
            throw new IOException("CATMAP JSON document is not a layout (format=" + format + ").");
        }
        LayoutModel model = new LayoutModel();
        model.setPageSpec(readLegacyPage(root.path("page")));

        JsonNode elements = root.path("elements");
        if (!elements.isArray()) {
            throw new IOException("CATMAP legacy document has no elements array.");
        }
        if (elements.size() > MAX_ELEMENTS) {
            throw new IOException("CATMAP legacy document exceeds the element count limit.");
        }
        for (JsonNode element : elements) {
            model.addElement(readLegacyElement(element));
        }
        return model;
    }

    private static LayoutPageSpec readLegacyPage(JsonNode page) throws IOException {
        if (!page.isObject()) {
            return LayoutPageSpec.A4_LANDSCAPE;
        }
        double widthMm = page.path("widthMm").asDouble(0);
        double heightMm = page.path("heightMm").asDouble(0);
        if (!(widthMm > 0) || !(heightMm > 0)) {
            throw new IOException("CATMAP legacy page requires positive widthMm/heightMm.");
        }
        String rawOrientation = page.path("orientation").asText("landscape");
        PageOrientation orientation;
        String normalized = rawOrientation.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.equals("landscape") || normalized.equals("horizontal")
                || normalized.equals("h")) {
            orientation = PageOrientation.LANDSCAPE;
        } else if (normalized.equals("portrait") || normalized.equals("vertical")
                || normalized.equals("v")) {
            orientation = PageOrientation.PORTRAIT;
        } else {
            throw new IOException("CATMAP legacy page orientation is not supported: "
                    + rawOrientation);
        }
        // Legacy JSON stored the effective (already oriented) sheet size.
        double portraitWidth = orientation == PageOrientation.LANDSCAPE ? heightMm : widthMm;
        double portraitHeight = orientation == PageOrientation.LANDSCAPE ? widthMm : heightMm;
        try {
            return LayoutPageSpec.fromPortraitDimensions(
                    portraitWidth, portraitHeight, orientation);
        } catch (IllegalArgumentException invalid) {
            throw new IOException("CATMAP legacy page is invalid: " + invalid.getMessage(), invalid);
        }
    }

    private static LayoutElement readLegacyElement(JsonNode node) throws IOException {
        if (node == null || !node.isObject()) {
            throw new IOException("CATMAP legacy document contains a non-object element.");
        }
        String type = node.path("type").asText("");
        String id = node.path("id").asText("");
        if (id.isBlank()) {
            throw new IOException("CATMAP legacy document contains an element without id.");
        }
        double x = number(node, "xMm");
        double y = number(node, "yMm");
        double width = number(node, "wMm");
        double height = number(node, "hMm");

        LayoutElement element;
        switch (type) {
            case "LayoutLabel" -> element = readLegacyLabel(node, id, x, y, width, height);
            case "LayoutMap" -> element = readLegacyMap(node, id, x, y, width, height);
            case "LayoutLegend" -> element = readLegacyLegend(node, id, x, y, width, height);
            default -> throw new IOException(
                    "CATMAP legacy document contains unsupported element type: " + type);
        }
        element.setZOrder(node.path("zOrder").asInt(0));
        element.setVisible(node.path("visible").asBoolean(true));
        element.setLocked(node.path("locked").asBoolean(false));
        String name = node.path("name").asText("");
        if (!name.isBlank()) {
            element.setName(name);
        }
        return element;
    }

    private static LayoutLabel readLegacyLabel(JsonNode node, String id,
                                               double x, double y, double w, double h)
            throws IOException {
        LayoutLabel label = new LayoutLabel(id, node.path("text").asText(""), x, y, w, h);
        double fontSize = node.path("fontSize").asDouble(0);
        String family = node.path("fontFamily").asText("");
        boolean bold = node.path("bold").asBoolean(false);
        if (fontSize > 0) {
            String useFamily = family.isBlank() ? "SansSerif" : family;
            label.setFont(new Font(useFamily, bold ? Font.BOLD : Font.PLAIN, (int) fontSize));
        }
        label.setColor(readLegacyColor(node.path("color").asText("")));
        return label;
    }

    private static LayoutMap readLegacyMap(JsonNode node, String id,
                                           double x, double y, double w, double h)
            throws IOException {
        LayoutMap map = new LayoutMap(id, x, y, w, h);
        map.setShowGrid(node.path("showGrid").asBoolean(false));
        map.setGridCols(node.path("gridCols").asInt(3));
        map.setGridRows(node.path("gridRows").asInt(3));
        map.setGridByDistance(node.path("gridByDistance").asBoolean(false));
        map.setGridIntervalX(node.path("gridIntervalX").asDouble(100));
        map.setGridIntervalY(node.path("gridIntervalY").asDouble(100));
        String unit = node.path("gridUnit").asText("");
        if (!unit.isBlank()) {
            map.setGridUnit(unit);
        }
        String gridColor = node.path("gridColor").asText("");
        if (!gridColor.isBlank()) {
            map.setGridColor(readLegacyColor(gridColor));
        }
        map.setTargetScaleDenominator(node.path("targetScale").asDouble(0));
        String assigned = node.path("assignedLayers").asText("");
        if (!assigned.isBlank()) {
            map.setAssignedLayerKeys(java.util.Arrays.stream(assigned.split(";"))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .toList());
        }
        return map;
    }

    private static LayoutLegend readLegacyLegend(JsonNode node, String id,
                                                 double x, double y, double w, double h)
            throws IOException {
        LayoutLegend legend = new LayoutLegend(id, x, y, w, h);
        String title = node.path("title").asText("");
        if (!title.isBlank()) {
            legend.setTitle(title);
        }
        legend.setAutoHeight(node.path("autoHeight").asBoolean(true));
        legend.setShowBackground(node.path("showBg").asBoolean(false));
        legend.setShowBorder(node.path("showBorder").asBoolean(false));
        double titleFontSize = node.path("titleFontSize").asDouble(0);
        double itemFontSize = node.path("itemFontSize").asDouble(0);
        if (titleFontSize > 0) {
            legend.setTitleFont(new Font("SansSerif", Font.BOLD, (int) titleFontSize));
        }
        if (itemFontSize > 0) {
            legend.setItemFont(new Font("SansSerif", Font.PLAIN, (int) itemFontSize));
        }
        return legend;
    }

    private static Color readLegacyColor(String value) throws IOException {
        if (value == null || value.isBlank() || "0".equals(value)) {
            return Color.BLACK;
        }
        String hex = value.startsWith("#") ? value.substring(1) : value;
        try {
            return new Color(Integer.parseInt(hex, 16));
        } catch (NumberFormatException invalid) {
            throw new IOException("CATMAP legacy color is not valid: " + value, invalid);
        }
    }

    private static double number(JsonNode node, String field) throws IOException {
        JsonNode value = node.get(field);
        if (value == null || !value.isNumber() || !Double.isFinite(value.asDouble())) {
            throw new IOException("CATMAP legacy element has an invalid " + field + ".");
        }
        return value.asDouble();
    }
}
