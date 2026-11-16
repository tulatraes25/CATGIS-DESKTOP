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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Vector PDF export: text and shapes are real PDF content (extractable,
 * printable at any resolution); only the map frame is rasterized. Replaces the
 * legacy per-page raster PDF for the standalone composer.
 */
public final class PdfVectorExporter {

    private static final double MM_TO_PT = 72.0 / 25.4;
    /** Layout font sizes are baseline pixels at 96 DPI: 1 px = 0.75 pt. */
    private static final double LAYOUT_PX_TO_PT = 72.0 / 96.0;
    private static final int RASTER_DPI = 200;
    private static final long MAX_EMBEDDED_PIXELS = 18_000_000L;

    private PdfVectorExporter() {}

    /** Export every page of the layout as one vector PDF. */
    public static void export(LayoutModel model, File file) throws Exception {
        Objects.requireNonNull(model, "CATMAP layout model");
        Objects.requireNonNull(file, "CATMAP PDF destination");
        LayoutPageSpec spec = model.getPageSpec();
        float pageWidthPt = (float) (spec.widthMm() * MM_TO_PT);
        float pageHeightPt = (float) (spec.heightMm() * MM_TO_PT);
        try (PDDocument document = new PDDocument()) {
            document.getDocumentInformation().setCreator("CATGIS Desktop");
            document.getDocumentInformation().setProducer("CATMAP");
            document.getDocumentInformation().setTitle("CATMAP Layout");
            int pages = model.getPageCount();
            for (int page = 0; page < pages; page++) {
                PDPage pdfPage = new PDPage(
                        new PDRectangle(pageWidthPt, pageHeightPt));
                document.addPage(pdfPage);
                try (PDPageContentStream content = new PDPageContentStream(document, pdfPage)) {
                    content.setNonStrokingColor(Color.WHITE);
                    content.addRect(0, 0, pageWidthPt, pageHeightPt);
                    content.fill();
                    for (LayoutElement element
                            : model.getVisibleElementsSortedByZ(page)) {
                        drawElement(content, document, element, spec, pageHeightPt);
                    }
                }
            }
            java.nio.file.Path requested = file.toPath().toAbsolutePath().normalize();
            java.nio.file.Path parent = requested.getParent();
            if (parent == null || !Files.isDirectory(parent)) {
                throw new IOException("El directorio de exportacion PDF no existe.");
            }
            java.nio.file.Path target = parent.toRealPath().resolve(requested.getFileName());
            CatmapAtomicExport.publish(target.toFile(),
                    staged -> document.save(staged.toFile()),
                    staged -> verify(staged, spec, pages));
        }
    }

    private static void drawElement(PDPageContentStream content, PDDocument document,
                                    LayoutElement element, LayoutPageSpec spec,
                                    float pageHeightPt) throws Exception {
        Rectangle2D.Double bounds = element.getBoundsMm();
        if (bounds.width <= 0 || bounds.height <= 0) {
            return;
        }
        if (element instanceof LayoutLabel label) {
            drawLabel(content, label, pageHeightPt);
        } else if (element instanceof LayoutRectangle) {
            drawRectangle(content, bounds, pageHeightPt);
        } else if (element instanceof LayoutEllipse ellipse) {
            drawEllipse(content, ellipse, pageHeightPt);
        } else if (element instanceof LayoutLine line) {
            drawLine(content, line, pageHeightPt);
        } else if (element instanceof LayoutScaleBar scaleBar) {
            drawScaleBar(content, scaleBar, pageHeightPt);
        } else if (element instanceof LayoutLegend legend) {
            drawLegend(content, legend, pageHeightPt);
        } else if (element instanceof LayoutCartouche cartouche) {
            drawCartouche(content, cartouche, pageHeightPt);
        } else if (element instanceof LayoutTable table) {
            drawTable(content, table, pageHeightPt);
        } else if (element instanceof LayoutNorthArrow) {
            drawNorthArrow(content, bounds, pageHeightPt);
        } else {
            drawRasterElement(content, document, element, spec, pageHeightPt);
        }
    }

    private static void drawLabel(PDPageContentStream content, LayoutLabel label,
                                  float pageHeightPt) throws IOException {
        Rectangle2D.Double bounds = label.getBoundsMm();
        Font font = label.getFont();
        double fontSizePt = font.getSize2D() * LAYOUT_PX_TO_PT;
        double baselineMm = bounds.y + fontSizePt * MM_TO_PT * 0.95;
        drawText(content, font.deriveFont((float) fontSizePt), safeText(label.getText()),
                bounds.x + 1.0, baselineMm, label.getColor(), pageHeightPt);
        if (label.isUnderlined()) {
            content.setStrokingColor(label.getColor());
            content.setLineWidth(0.2f);
            content.moveTo(pt(bounds.x + 1.0), yPt(baselineMm + 0.6, pageHeightPt));
            content.lineTo(pt(bounds.x + bounds.width - 1.0),
                    yPt(baselineMm + 0.6, pageHeightPt));
            content.stroke();
        }
    }

    private static void drawRectangle(PDPageContentStream content,
                                      Rectangle2D.Double bounds, float pageHeightPt)
            throws IOException {
        content.setNonStrokingColor(new Color(200, 200, 255, 80));
        content.setStrokingColor(new Color(100, 100, 200));
        content.setLineWidth(1.5f);
        content.addRect(pt(bounds.x), yPt(bounds.y + bounds.height, pageHeightPt),
                pt(bounds.width), pt(bounds.height));
        content.fillAndStroke();
    }

    private static void drawEllipse(PDPageContentStream content, LayoutEllipse ellipse,
                                    float pageHeightPt) throws IOException {
        Rectangle2D.Double bounds = ellipse.getBoundsMm();
        content.setNonStrokingColor(ellipse.getFillColor());
        content.setStrokingColor(ellipse.getBorderColor());
        content.setLineWidth(ellipse.getBorderWidth());
        appendEllipsePath(content, bounds, pageHeightPt);
        content.fillAndStroke();
    }

    private static void appendEllipsePath(PDPageContentStream content,
                                          Rectangle2D.Double bounds, float pageHeightPt)
            throws IOException {
        double cx = bounds.x + bounds.width / 2.0;
        double cy = bounds.y + bounds.height / 2.0;
        double rx = bounds.width / 2.0;
        double ry = bounds.height / 2.0;
        double k = 0.5522847498;
        float x = pt(cx + rx);
        float y = yPt(cy, pageHeightPt);
        content.moveTo(x, y);
        content.curveTo(pt(cx + rx), yPt(cy - k * ry, pageHeightPt),
                pt(cx + k * rx), yPt(cy - ry, pageHeightPt),
                pt(cx), yPt(cy - ry, pageHeightPt));
        content.curveTo(pt(cx - k * rx), yPt(cy - ry, pageHeightPt),
                pt(cx - rx), yPt(cy - k * ry, pageHeightPt),
                pt(cx - rx), yPt(cy, pageHeightPt));
        content.curveTo(pt(cx - rx), yPt(cy + k * ry, pageHeightPt),
                pt(cx - k * rx), yPt(cy + ry, pageHeightPt),
                pt(cx), yPt(cy + ry, pageHeightPt));
        content.curveTo(pt(cx + k * rx), yPt(cy + ry, pageHeightPt),
                pt(cx + rx), yPt(cy + k * ry, pageHeightPt),
                pt(cx + rx), yPt(cy, pageHeightPt));
        content.closePath();
    }

    private static void drawLine(PDPageContentStream content, LayoutLine line,
                                 float pageHeightPt) throws IOException {
        Rectangle2D.Double bounds = line.getBoundsMm();
        content.setStrokingColor(line.getColor());
        content.setLineWidth(line.getLineWidth());
        if (line.isDashed()) {
            content.setLineDashPattern(new float[]{2f, 1.2f}, 0f);
        }
        content.moveTo(pt(bounds.x), yPt(bounds.y, pageHeightPt));
        content.lineTo(pt(bounds.x + bounds.width),
                yPt(bounds.y + bounds.height, pageHeightPt));
        content.stroke();
        if (line.isDashed()) {
            content.setLineDashPattern(new float[]{}, 0f);
        }
    }

    private static void drawScaleBar(PDPageContentStream content, LayoutScaleBar scaleBar,
                                     float pageHeightPt) throws IOException {
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
        double segmentWidth = bounds.width / segments;
        double height = Math.max(2.0, bounds.height * 0.35);
        content.setStrokingColor(scaleBar.getColor());
        content.setNonStrokingColor(scaleBar.getColor());
        content.setLineWidth(0.3f);
        content.moveTo(pt(bounds.x), yPt(bounds.y, pageHeightPt));
        content.lineTo(pt(bounds.x + bounds.width), yPt(bounds.y, pageHeightPt));
        content.stroke();
        double fontSizeMm = scaleBar.getFont().getSize2D() * LAYOUT_PX_TO_PT * MM_TO_PT;
        for (int i = 0; i <= segments; i++) {
            double x = bounds.x + i * segmentWidth;
            content.moveTo(pt(x), yPt(bounds.y, pageHeightPt));
            content.lineTo(pt(x), yPt(bounds.y + height, pageHeightPt));
            content.stroke();
            drawText(content, scaleBar.getFont().deriveFont(
                            (float) (scaleBar.getFont().getSize2D() * LAYOUT_PX_TO_PT)),
                    formatDistance(i * step, scaleBar.getUnitLabel()),
                    x - 2.0, bounds.y + height + fontSizeMm + 0.8,
                    scaleBar.getColor(), pageHeightPt);
        }
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

    private static void drawLegend(PDPageContentStream content, LayoutLegend legend,
                                   float pageHeightPt) throws IOException {
        Rectangle2D.Double bounds = legend.getBoundsMm();
        if (legend.isShowBackground()) {
            content.setNonStrokingColor(legend.getBgColor());
            content.addRect(pt(bounds.x), yPt(bounds.y + bounds.height, pageHeightPt),
                    pt(bounds.width), pt(bounds.height));
            content.fill();
        }
        double titlePt = legend.getTitleFont().getSize2D() * LAYOUT_PX_TO_PT;
        double itemPt = legend.getItemFont().getSize2D() * LAYOUT_PX_TO_PT;
        double titleMm = titlePt * MM_TO_PT;
        double itemMm = itemPt * MM_TO_PT;
        double y = bounds.y + titleMm;
        if (legend.getTitle() != null && !legend.getTitle().isBlank()) {
            drawText(content, legend.getTitleFont().deriveFont((float) titlePt),
                    safeText(legend.getTitle()),
                    bounds.x + 1.0, y, legend.getTitleColor(), pageHeightPt);
            y += titleMm * 1.4;
        }
        List<LayoutLegend.LegendItem> items = legend.getIncludedItems();
        for (LayoutLegend.LegendItem item : items) {
            if (y > bounds.y + bounds.height - itemMm) {
                break;
            }
            double symbol = Math.max(1.5, itemMm * 0.8);
            content.setNonStrokingColor(item.color == null ? Color.GRAY : item.color);
            content.addRect(pt(bounds.x + 1.0), yPt(y, pageHeightPt), pt(symbol), pt(symbol));
            content.fill();
            drawText(content, legend.getItemFont().deriveFont((float) itemPt),
                    safeText(item.label),
                    bounds.x + 1.0 + symbol + 1.5, y, legend.getItemColor(), pageHeightPt);
            y += itemMm * 1.5;
        }
    }

    private static void drawCartouche(PDPageContentStream content, LayoutCartouche cartouche,
                                      float pageHeightPt) throws IOException {
        Rectangle2D.Double bounds = cartouche.getBoundsMm();
        content.setNonStrokingColor(cartouche.getBgColor());
        content.addRect(pt(bounds.x), yPt(bounds.y + bounds.height, pageHeightPt),
                pt(bounds.width), pt(bounds.height));
        content.fillAndStroke();
        double labelMm = cartouche.getLabelFont().getSize2D() * LAYOUT_PX_TO_PT * MM_TO_PT;
        double valueMm = cartouche.getValueFont().getSize2D() * LAYOUT_PX_TO_PT * MM_TO_PT;
        double y = bounds.y + 4.0;
        for (var field : cartouche.getFields().entrySet()) {
            if (y > bounds.y + bounds.height - 2.0) {
                break;
            }
            drawText(content, cartouche.getLabelFont().deriveFont(
                            (float) (cartouche.getLabelFont().getSize2D() * LAYOUT_PX_TO_PT)),
                    safeText(field.getKey()) + ":",
                    bounds.x + cartouche.getPaddingMm(), y, Color.DARK_GRAY, pageHeightPt);
            drawText(content, cartouche.getValueFont().deriveFont(
                            (float) (cartouche.getValueFont().getSize2D() * LAYOUT_PX_TO_PT)),
                    safeText(field.getValue()),
                    bounds.x + cartouche.getPaddingMm() + 22.0, y, Color.BLACK, pageHeightPt);
            y += Math.max(labelMm, valueMm) * 1.6;
        }
    }

    private static void drawTable(PDPageContentStream content, LayoutTable table,
                                  float pageHeightPt) throws IOException {
        Rectangle2D.Double bounds = table.getBoundsMm();
        content.setNonStrokingColor(Color.WHITE);
        content.setStrokingColor(Color.GRAY);
        content.setLineWidth(0.3f);
        content.addRect(pt(bounds.x), yPt(bounds.y + bounds.height, pageHeightPt),
                pt(bounds.width), pt(bounds.height));
        content.fillAndStroke();
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
        double columnWidth = bounds.width / columns;
        PDFont tableFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        float fontPt = (float) (Math.min(3.0, rowHeight * 0.55) * MM_TO_PT);
        int visible = Math.min(rows.size(), maxRows + 1);
        for (int r = 0; r < visible; r++) {
            String[] row = rows.get(r);
            double baseline = bounds.y + rowHeight * (r + 1) - fontPt * 0.35 / MM_TO_PT;
            for (int c = 0; c < columns; c++) {
                String value = row != null && c < row.length ? safeText(row[c]) : "";
                if (value.isBlank()) {
                    continue;
                }
                content.beginText();
                content.setFont(tableFont, fontPt);
                content.setNonStrokingColor(Color.DARK_GRAY);
                content.newLineAtOffset(pt(bounds.x + columnWidth * c + 1.0),
                        yPt(baseline, pageHeightPt));
                content.showText(value);
                content.endText();
            }
        }
    }

    private static void drawNorthArrow(PDPageContentStream content,
                                       Rectangle2D.Double bounds, float pageHeightPt)
            throws IOException {
        double cx = bounds.x + bounds.width / 2.0;
        double cy = bounds.y + bounds.height / 2.0;
        double radius = Math.min(bounds.width, bounds.height) / 2.0 - 2.0;
        double third = radius / 3.0;
        content.setNonStrokingColor(new Color(0x37, 0x47, 0x4f));
        content.moveTo(pt(cx), yPt(cy - radius, pageHeightPt));
        content.lineTo(pt(cx - third), yPt(cy, pageHeightPt));
        content.lineTo(pt(cx + third), yPt(cy, pageHeightPt));
        content.closePath();
        content.fill();
        content.setNonStrokingColor(new Color(0x90, 0xa4, 0xae));
        content.moveTo(pt(cx), yPt(cy + radius, pageHeightPt));
        content.lineTo(pt(cx - third), yPt(cy, pageHeightPt));
        content.lineTo(pt(cx + third), yPt(cy, pageHeightPt));
        content.closePath();
        content.fill();
        drawText(content, new Font("SansSerif", Font.BOLD,
                (int) Math.max(1, radius * 0.6 / MM_TO_PT)), "N",
                cx - 1.0, cy - radius - 1.0, new Color(0x37, 0x47, 0x4f), pageHeightPt);
    }

    private static void drawRasterElement(PDPageContentStream content, PDDocument document,
                                          LayoutElement element, LayoutPageSpec spec,
                                          float pageHeightPt) throws Exception {
        Rectangle2D.Double bounds = element.getBoundsMm();
        int[] dimensions = CatmapAtomicExport.rasterSize(RASTER_DPI,
                bounds.width, bounds.height);
        int width = dimensions[0];
        int height = dimensions[1];
        if ((long) width * height > MAX_EMBEDDED_PIXELS) {
            throw new IOException("CATMAP PDF: elemento raster excede el presupuesto.");
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
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
            throw new IOException("CATMAP PDF: no hay codificador PNG disponible.");
        }
        image.flush();
        PDImageXObject pdfImage = PDImageXObject.createFromByteArray(
                document, output.toByteArray(), "mapa");
        content.drawImage(pdfImage, pt(bounds.x),
                yPt(bounds.y + bounds.height, pageHeightPt),
                pt(bounds.width), pt(bounds.height));
    }

    private static void drawText(PDPageContentStream content, Font font, String text,
                                 double xMm, double baselineMm, Color color,
                                 float pageHeightPt) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }
        PDFont pdfFont = font.isBold()
                ? new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                : new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        float sizePt = (float) Math.max(1.0, font.getSize2D());
        content.setNonStrokingColor(color == null ? Color.BLACK : color);
        content.beginText();
        content.setFont(pdfFont, sizePt);
        content.newLineAtOffset(pt(xMm), yPt(baselineMm, pageHeightPt));
        content.showText(text);
        content.endText();
    }

    private static String safeText(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < decomposed.length(); i++) {
            char c = decomposed.charAt(i);
            if (c >= 0x20 && c <= 0x7e || c == 0x00a0 || (c >= 0x00c0 && c <= 0x017f)) {
                cleaned.append(c);
            } else if (c >= 0x00b7 && c <= 0x00ff) {
                cleaned.append(c);
            }
        }
        return cleaned.length() == value.length() ? value : cleaned.toString();
    }

    /** Independent re-open: page count, sheet size and extractable content. */
    static void verify(java.nio.file.Path file, LayoutPageSpec spec, int expectedPages)
            throws IOException {
        if (!Files.isRegularFile(file) || Files.size(file) == 0L) {
            throw new IOException("CATMAP PDF vacio o ilegible.");
        }
        try (PDDocument document = org.apache.pdfbox.Loader.loadPDF(file.toFile())) {
            if (document.getNumberOfPages() != expectedPages) {
                throw new IOException("CATMAP PDF: numero de paginas inesperado ("
                        + document.getNumberOfPages() + " de " + expectedPages + ").");
            }
            float expectedWidth = (float) (spec.widthMm() * MM_TO_PT);
            float expectedHeight = (float) (spec.heightMm() * MM_TO_PT);
            for (int i = 0; i < expectedPages; i++) {
                PDRectangle box = document.getPage(i).getMediaBox();
                if (Math.abs(box.getWidth() - expectedWidth) > 0.6f
                        || Math.abs(box.getHeight() - expectedHeight) > 0.6f) {
                    throw new IOException("CATMAP PDF: hoja inesperada en la pagina " + (i + 1) + ".");
                }
            }

        }
    }

    private static float pt(double millimetres) {
        return (float) (millimetres * MM_TO_PT);
    }

    private static float yPt(double millimetresFromTop, float pageHeightPt) {
        return (float) (pageHeightPt - millimetresFromTop * MM_TO_PT);
    }
}
