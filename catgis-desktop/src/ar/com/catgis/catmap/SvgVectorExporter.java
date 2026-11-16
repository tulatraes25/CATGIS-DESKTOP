package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutCartouche;
import ar.com.catgis.layout.LayoutElement;
import ar.com.catgis.layout.LayoutEllipse;
import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutLegend;
import ar.com.catgis.layout.LayoutLine;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutNorthArrow;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.LayoutRectangle;
import ar.com.catgis.layout.LayoutRenderContext;
import ar.com.catgis.layout.LayoutScaleBar;
import ar.com.catgis.layout.LayoutTable;

import javax.imageio.ImageIO;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Real vector SVG export for CATMAP pages: text and shapes are emitted as SVG
 * elements, while the map frame (and other raster-only elements) stay as an
 * embedded PNG. This is the counterpart of the raster wrapper in
 * {@link SvgExportEngine}.
 */
public final class SvgVectorExporter {

    private static final int RASTER_DPI = 150;
    private static final double PT_TO_MM = 25.4 / 72.0;
    private static final long MAX_EMBEDDED_PIXELS = 18_000_000L;

    private SvgVectorExporter() {}

    /** Export the active page as vector SVG; the destination is replaced atomically. */
    public static void exportPage(LayoutModel model, File file) throws Exception {
        Objects.requireNonNull(model, "CATMAP layout model");
        Objects.requireNonNull(file, "CATMAP SVG destination");
        LayoutPageSpec spec = model.getPageSpec();
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append(String.format(Locale.ROOT,
                "<svg xmlns=\"%s\" version=\"1.1\" width=\"%smm\" height=\"%smm\""
                        + " viewBox=\"0 0 %s %s\">\n",
                "http://www.w3.org/2000/svg",
                number(spec.widthMm()), number(spec.heightMm()),
                number(spec.widthMm()), number(spec.heightMm())));
        svg.append(String.format(Locale.ROOT,
                "  <rect x=\"0\" y=\"0\" width=\"%s\" height=\"%s\" fill=\"#ffffff\"/>\n",
                number(spec.widthMm()), number(spec.heightMm())));
        for (LayoutElement element : model.getVisibleElementsSortedByZ()) {
            appendElement(svg, element, spec);
        }
        svg.append("</svg>\n");
        byte[] bytes = svg.toString().getBytes(StandardCharsets.UTF_8);
        CatmapAtomicExport.publish(file,
                staged -> Files.write(staged, bytes),
                staged -> verifyVectorSvg(staged, spec));
    }

    private static void appendElement(StringBuilder svg, LayoutElement element,
                                      LayoutPageSpec spec) throws Exception {
        Rectangle2D.Double bounds = element.getBoundsMm();
        if (bounds.width <= 0 || bounds.height <= 0) {
            return;
        }
        if (element instanceof LayoutLabel label) {
            appendLabel(svg, label);
        } else if (element instanceof LayoutRectangle) {
            appendRectangle(svg, bounds);
        } else if (element instanceof LayoutEllipse ellipse) {
            appendEllipse(svg, ellipse);
        } else if (element instanceof LayoutLine line) {
            appendLine(svg, line);
        } else if (element instanceof LayoutScaleBar scaleBar) {
            appendScaleBar(svg, scaleBar);
        } else if (element instanceof LayoutLegend legend) {
            appendLegend(svg, legend);
        } else if (element instanceof LayoutCartouche cartouche) {
            appendCartouche(svg, cartouche);
        } else if (element instanceof LayoutTable table) {
            appendTable(svg, table);
        } else if (element instanceof LayoutNorthArrow) {
            appendNorthArrow(svg, bounds);
        } else {
            appendRasterElement(svg, element, spec);
        }
    }

    private static void appendLabel(StringBuilder svg, LayoutLabel label) {
        Rectangle2D.Double bounds = label.getBoundsMm();
        Font font = label.getFont();
        double fontSizeMm = font.getSize2D() * PT_TO_MM;
        double baseline = bounds.y + fontSizeMm * 0.95;
        svg.append(String.format(Locale.ROOT,
                "  <text x=\"%s\" y=\"%s\" font-family=\"%s\" font-size=\"%s\""
                        + " font-weight=\"%s\" fill=\"%s\">%s</text>\n",
                number(bounds.x + 1.0), number(baseline),
                escape(font.getFamily()), number(fontSizeMm),
                font.isBold() ? "bold" : "normal", hex(label.getColor()),
                escape(label.getText())));
        if (label.isUnderlined()) {
            svg.append(String.format(Locale.ROOT,
                    "  <line x1=\"%s\" y1=\"%s\" x2=\"%s\" y2=\"%s\""
                            + " stroke=\"%s\" stroke-width=\"0.2\"/>\n",
                    number(bounds.x + 1.0), number(baseline + 0.6),
                    number(bounds.x + bounds.width - 1.0), number(baseline + 0.6),
                    hex(label.getColor())));
        }
    }

    private static void appendRectangle(StringBuilder svg, Rectangle2D.Double bounds) {
        svg.append(String.format(Locale.ROOT,
                "  <rect x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\""
                        + " fill=\"%s\" fill-opacity=\"%s\" stroke=\"%s\" stroke-width=\"1.5\"/>\n",
                number(bounds.x), number(bounds.y), number(bounds.width), number(bounds.height),
                "#c8c8ff", "0.31", "#6464c8"));
    }

    private static void appendEllipse(StringBuilder svg, LayoutEllipse ellipse) {
        Rectangle2D.Double bounds = ellipse.getBoundsMm();
        svg.append(String.format(Locale.ROOT,
                "  <ellipse cx=\"%s\" cy=\"%s\" rx=\"%s\" ry=\"%s\""
                        + " fill=\"%s\" fill-opacity=\"%s\" stroke=\"%s\" stroke-width=\"%s\"/>\n",
                number(bounds.x + bounds.width / 2.0),
                number(bounds.y + bounds.height / 2.0),
                number(bounds.width / 2.0), number(bounds.height / 2.0),
                hex(ellipse.getFillColor()), opacity(ellipse.getFillColor()),
                hex(ellipse.getBorderColor()), number(ellipse.getBorderWidth())));
    }

    private static void appendLine(StringBuilder svg, LayoutLine line) {
        Rectangle2D.Double bounds = line.getBoundsMm();
        String dash = line.isDashed() ? " stroke-dasharray=\"2 1.2\"" : "";
        svg.append(String.format(Locale.ROOT,
                "  <line x1=\"%s\" y1=\"%s\" x2=\"%s\" y2=\"%s\" stroke=\"%s\""
                        + " stroke-width=\"%s\"%s/>\n",
                number(bounds.x), number(bounds.y),
                number(bounds.x + bounds.width), number(bounds.y + bounds.height),
                hex(line.getColor()), number(line.getLineWidth()), dash));
    }

    private static void appendScaleBar(StringBuilder svg, LayoutScaleBar scaleBar) {
        Rectangle2D.Double bounds = scaleBar.getBoundsMm();
        double maxMeters = (bounds.width / 1000.0) * scaleBar.getMapScaleDenominator();
        double[] steps = {1, 2, 5, 10, 25, 50, 100, 250, 500, 1000, 2500, 5000, 10000, 25000,
                50000, 100000, 250000, 500000, 1000000};
        double step = steps[steps.length - 1];
        for (double candidate : steps) {
            if (candidate >= maxMeters / 5.0) {
                step = candidate;
                break;
            }
        }
        int segments = Math.max(2, Math.min(scaleBar.getSegments(), 6));
        double barWidthMm = bounds.width;
        double segmentWidth = barWidthMm / segments;
        double y = bounds.y;
        double height = Math.max(2.0, bounds.height * 0.35);
        Font font = scaleBar.getFont();
        double fontSizeMm = font.getSize2D() * PT_TO_MM;
        svg.append(String.format(Locale.ROOT,
                "  <g stroke=\"%s\" stroke-width=\"0.3\">", hex(scaleBar.getColor())));
        svg.append(String.format(Locale.ROOT,
                "<line x1=\"%s\" y1=\"%s\" x2=\"%s\" y2=\"%s\"/>",
                number(bounds.x), number(y), number(bounds.x + barWidthMm), number(y)));
        for (int i = 0; i <= segments; i++) {
            double x = bounds.x + i * segmentWidth;
            svg.append(String.format(Locale.ROOT,
                    "<line x1=\"%s\" y1=\"%s\" x2=\"%s\" y2=\"%s\"/>",
                    number(x), number(y), number(x), number(y + height)));
            String label = formatDistance(i * step, scaleBar.getUnitLabel());
            svg.append(String.format(Locale.ROOT,
                    "<text x=\"%s\" y=\"%s\" font-family=\"%s\" font-size=\"%s\""
                            + " fill=\"%s\" stroke=\"none\">%s</text>",
                    number(x - 2.0), number(y + height + fontSizeMm + 0.8),
                    escape(font.getFamily()), number(fontSizeMm),
                    hex(scaleBar.getColor()), escape(label)));
        }
        svg.append("</g>\n");
    }

    private static String formatDistance(double meters, String unitLabel) {
        if (meters >= 1000.0) {
            double km = meters / 1000.0;
            return (km == Math.rint(km) ? Long.toString((long) km)
                    : String.format(Locale.ROOT, "%.1f", km)) + " km";
        }
        String unit = unitLabel == null || unitLabel.isBlank() ? "m" : unitLabel;
        return String.format(Locale.ROOT, "%.0f %s", meters, unit);
    }

    private static void appendLegend(StringBuilder svg, LayoutLegend legend) {
        Rectangle2D.Double bounds = legend.getBoundsMm();
        if (legend.isShowBackground()) {
            svg.append(String.format(Locale.ROOT,
                    "  <rect x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" fill=\"%s\""
                            + " fill-opacity=\"%s\"%s/>\n",
                    number(bounds.x), number(bounds.y),
                    number(bounds.width), number(bounds.height),
                    hex(legend.getBgColor()), opacity(legend.getBgColor()),
                    legend.isShowBorder()
                            ? " stroke=\"#666666\" stroke-width=\"0.3\"" : ""));
        }
        double titleSize = legend.getTitleFont().getSize2D() * PT_TO_MM;
        double itemSize = legend.getItemFont().getSize2D() * PT_TO_MM;
        double y = bounds.y + titleSize;
        if (legend.getTitle() != null && !legend.getTitle().isBlank()) {
            svg.append(String.format(Locale.ROOT,
                    "  <text x=\"%s\" y=\"%s\" font-family=\"%s\" font-size=\"%s\""
                            + " font-weight=\"bold\" fill=\"%s\">%s</text>\n",
                    number(bounds.x + 1.0), number(y),
                    escape(legend.getTitleFont().getFamily()), number(titleSize),
                    hex(legend.getTitleColor()), escape(legend.getTitle())));
            y += titleSize * 1.4;
        }
        List<LayoutLegend.LegendItem> items = legend.getIncludedItems();
        for (LayoutLegend.LegendItem item : items) {
            if (y > bounds.y + bounds.height - itemSize) {
                break;
            }
            double symbol = Math.max(1.5, itemSize * 0.8);
            svg.append(String.format(Locale.ROOT,
                    "  <rect x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" fill=\"%s\"/>\n",
                    number(bounds.x + 1.0), number(y - symbol * 0.8),
                    number(symbol), number(symbol), hex(item.color)));
            svg.append(String.format(Locale.ROOT,
                    "  <text x=\"%s\" y=\"%s\" font-family=\"%s\" font-size=\"%s\""
                            + " fill=\"%s\">%s</text>\n",
                    number(bounds.x + 1.0 + symbol + 1.5), number(y),
                    escape(legend.getItemFont().getFamily()), number(itemSize),
                    hex(legend.getItemColor()), escape(item.label)));
            y += itemSize * 1.5;
        }
    }

    private static void appendCartouche(StringBuilder svg, LayoutCartouche cartouche) {
        Rectangle2D.Double bounds = cartouche.getBoundsMm();
        svg.append(String.format(Locale.ROOT,
                "  <rect x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" fill=\"%s\""
                        + " fill-opacity=\"%s\"%s/>\n",
                number(bounds.x), number(bounds.y),
                number(bounds.width), number(bounds.height),
                hex(cartouche.getBgColor()), opacity(cartouche.getBgColor()),
                cartouche.isShowBorder()
                        ? " stroke=\"" + hex(cartouche.getBorderColor())
                                + "\" stroke-width=\"" + number(cartouche.getBorderWidth()) + "\""
                        : ""));
        double y = bounds.y + 4.0;
        double labelSize = cartouche.getLabelFont().getSize2D() * PT_TO_MM;
        double valueSize = cartouche.getValueFont().getSize2D() * PT_TO_MM;
        for (var field : cartouche.getFields().entrySet()) {
            if (y > bounds.y + bounds.height - 2.0) {
                break;
            }
            svg.append(String.format(Locale.ROOT,
                    "  <text x=\"%s\" y=\"%s\" font-family=\"%s\" font-size=\"%s\""
                            + " font-weight=\"bold\" fill=\"#333333\">%s:</text>\n",
                    number(bounds.x + cartouche.getPaddingMm()), number(y),
                    escape(cartouche.getLabelFont().getFamily()), number(labelSize),
                    escape(field.getKey())));
            svg.append(String.format(Locale.ROOT,
                    "  <text x=\"%s\" y=\"%s\" font-family=\"%s\" font-size=\"%s\""
                            + " fill=\"#222222\">%s</text>\n",
                    number(bounds.x + cartouche.getPaddingMm() + 22.0), number(y),
                    escape(cartouche.getValueFont().getFamily()), number(valueSize),
                    escape(field.getValue())));
            y += Math.max(labelSize, valueSize) * 1.6;
        }
    }

    private static void appendTable(StringBuilder svg, LayoutTable table) {
        Rectangle2D.Double bounds = table.getBoundsMm();
        svg.append(String.format(Locale.ROOT,
                "  <rect x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" fill=\"#ffffff\""
                        + " stroke=\"#666666\" stroke-width=\"0.3\"/>\n",
                number(bounds.x), number(bounds.y),
                number(bounds.width), number(bounds.height)));
        List<String[]> rows = table.getRows();
        if (rows == null || rows.isEmpty()) {
            return;
        }
        int maxRows = Math.max(1, table.getMaxVisibleRows());
        int columns = 0;
        for (String[] row : rows) {
            columns = Math.max(columns, row == null ? 0 : row.length);
        }
        if (columns == 0) {
            return;
        }
        double rowHeight = bounds.height / Math.min(rows.size(), maxRows + 1);
        double fontMm = Math.min(3.0, rowHeight * 0.55);
        double columnWidth = bounds.width / columns;
        int visible = Math.min(rows.size(), maxRows + 1);
        for (int r = 0; r < visible; r++) {
            String[] row = rows.get(r);
            double y = bounds.y + rowHeight * (r + 1) - fontMm * 0.35;
            for (int c = 0; c < columns; c++) {
                String value = row != null && c < row.length ? row[c] : "";
                svg.append(String.format(Locale.ROOT,
                        "  <text x=\"%s\" y=\"%s\" font-family=\"SansSerif\" font-size=\"%s\""
                                + " fill=\"#222222\">%s</text>\n",
                        number(bounds.x + columnWidth * c + 1.0), number(y),
                        number(fontMm), escape(value)));
            }
            if (r < visible - 1) {
                svg.append(String.format(Locale.ROOT,
                        "  <line x1=\"%s\" y1=\"%s\" x2=\"%s\" y2=\"%s\""
                                + " stroke=\"#999999\" stroke-width=\"0.2\"/>\n",
                        number(bounds.x), number(bounds.y + rowHeight * (r + 1)),
                        number(bounds.x + bounds.width), number(bounds.y + rowHeight * (r + 1))));
            }
        }
    }

    private static void appendNorthArrow(StringBuilder svg, Rectangle2D.Double bounds) {
        double cx = bounds.x + bounds.width / 2.0;
        double cy = bounds.y + bounds.height / 2.0;
        double radius = Math.min(bounds.width, bounds.height) / 2.0 - 2.0;
        double third = radius / 3.0;
        svg.append(String.format(Locale.ROOT,
                "  <polygon points=\"%s,%s %s,%s %s,%s\" fill=\"#37474f\"/>\n",
                number(cx), number(cy - radius),
                number(cx - third), number(cy),
                number(cx + third), number(cy)));
        svg.append(String.format(Locale.ROOT,
                "  <polygon points=\"%s,%s %s,%s %s,%s\" fill=\"#90a4ae\"/>\n",
                number(cx), number(cy + radius),
                number(cx - third), number(cy),
                number(cx + third), number(cy)));
        svg.append(String.format(Locale.ROOT,
                "  <text x=\"%s\" y=\"%s\" font-family=\"SansSerif\" font-size=\"%s\""
                        + " font-weight=\"bold\" fill=\"#37474f\" text-anchor=\"middle\">N</text>\n",
                number(cx), number(cy - radius - 1.0), number(radius * 0.6)));
    }

    private static void appendRasterElement(StringBuilder svg, LayoutElement element,
                                            LayoutPageSpec spec) throws Exception {
        Rectangle2D.Double bounds = element.getBoundsMm();
        int[] dimensions = CatmapAtomicExport.rasterSize(RASTER_DPI,
                bounds.width, bounds.height);
        int width = dimensions[0];
        int height = dimensions[1];
        if ((long) width * height > MAX_EMBEDDED_PIXELS) {
            throw new IOException("CATMAP SVG: elemento raster excede el presupuesto.");
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(-bounds.x * RASTER_DPI / 25.4, -bounds.y * RASTER_DPI / 25.4);
            element.render(g2, new LayoutRenderContext(
                    LayoutRenderContext.Mode.EXPORT_IMAGE, RASTER_DPI,
                    spec.widthMm(), spec.heightMm()));
        } finally {
            g2.dispose();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", output)) {
            image.flush();
            throw new IOException("CATMAP SVG: no hay codificador PNG disponible.");
        }
        image.flush();
        String encoded = Base64.getEncoder().encodeToString(output.toByteArray());
        svg.append(String.format(Locale.ROOT,
                "  <image x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\""
                        + " href=\"data:image/png;base64,%s\"/>\n",
                number(bounds.x), number(bounds.y),
                number(bounds.width), number(bounds.height), encoded));
    }

    /** Independent re-open: vector SVG with the document sheet and no foreign content. */
    static void verifyVectorSvg(java.nio.file.Path file, LayoutPageSpec spec) throws IOException {
        if (!Files.isRegularFile(file) || Files.size(file) == 0L) {
            throw new IOException("CATMAP SVG vacio o ilegible.");
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document document = factory.newDocumentBuilder().parse(file.toFile());
            Element root = document.getDocumentElement();
            String expectedWidth = number(spec.widthMm()) + "mm";
            String expectedHeight = number(spec.heightMm()) + "mm";
            String expectedViewBox = "0 0 " + number(spec.widthMm()) + " "
                    + number(spec.heightMm());
            if (!"svg".equals(root.getLocalName())
                    || !expectedWidth.equals(root.getAttribute("width"))
                    || !expectedHeight.equals(root.getAttribute("height"))
                    || !expectedViewBox.equals(root.getAttribute("viewBox"))) {
                throw new IOException("CATMAP SVG vectorial tiene hoja o raiz inesperadas.");
            }
            if (document.getElementsByTagNameNS("*", "script").getLength() != 0
                    || document.getElementsByTagNameNS("*", "foreignObject").getLength() != 0) {
                throw new IOException("CATMAP SVG vectorial contiene contenido no permitido.");
            }
            var images = document.getElementsByTagNameNS(
                    "http://www.w3.org/2000/svg", "image");
            for (int i = 0; i < images.getLength(); i++) {
                Element image = (Element) images.item(i);
                String href = image.getAttribute("href");
                if (!href.startsWith("data:image/png;base64,")) {
                    throw new IOException("CATMAP SVG vectorial tiene una imagen externa.");
                }
            }
            if (document.getElementsByTagNameNS(
                    "http://www.w3.org/2000/svg", "text").getLength() == 0
                    && document.getElementsByTagNameNS(
                            "http://www.w3.org/2000/svg", "rect").getLength() == 0) {
                throw new IOException("CATMAP SVG vectorial no contiene contenido dibujable.");
            }
        } catch (IOException io) {
            throw io;
        } catch (Exception invalid) {
            throw new IOException("CATMAP SVG vectorial no supera la validacion XML.", invalid);
        }
    }

    private static String number(double value) {
        String text = String.format(Locale.ROOT, "%.3f", value);
        while (text.contains(".") && (text.endsWith("0") || text.endsWith("."))) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private static String hex(Color color) {
        Color safe = color == null ? Color.BLACK : color;
        return String.format(Locale.ROOT, "#%02x%02x%02x",
                safe.getRed(), safe.getGreen(), safe.getBlue());
    }

    private static String opacity(Color color) {
        Color safe = color == null ? Color.BLACK : color;
        return String.format(Locale.ROOT, "%.3f", safe.getAlpha() / 255.0);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
