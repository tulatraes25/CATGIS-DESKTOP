package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutModel;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Real exported PDF glyph positions and pixels, not only header/text presence. */
class CatmapPdfMultilineContractTest {

    @TempDir
    Path directory;

    private record Glyph(String text, float x, float y) {}

    private static final class Positions extends PDFTextStripper {
        private final List<Glyph> glyphs = new ArrayList<>();

        Positions() throws IOException {
            super();
        }

        @Override
        protected void processTextPosition(TextPosition text) {
            glyphs.add(new Glyph(text.getUnicode(), text.getXDirAdj(), text.getYDirAdj()));
            super.processTextPosition(text);
        }
    }

    @Test
    void multilineVectorGlyphsKeepAbsoluteXAndDescendingBaselinesAtDifferentDpi() throws Exception {
        for (int dpi : new int[] {72, 300}) {
            LayoutModel model = model("A\nB\nC", 20, 40);
            Path pdf = export(model, "positions-" + dpi + ".pdf", dpi);
            try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
                List<Glyph> glyphs = positions(document);
                assertEquals(List.of("A", "B", "C"), glyphs.stream().map(Glyph::text).toList());
                for (int i = 0; i < glyphs.size(); i++) {
                    assertPosition(glyphs.get(i), 20, 40, i);
                    assertTrue(glyphs.get(i).y() > 0
                            && glyphs.get(i).y() < document.getPage(0).getMediaBox().getHeight());
                }
            }
            assertNoStages();
            Files.delete(pdf);
        }
    }

    @Test
    void crlfCrLfAndLogicalBlankLinesArePreserved() throws Exception {
        Path pdf = export(model("A\r\n\r\nB\rC\nD", 20, 40), "logical lines.pdf", 72);
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            List<Glyph> glyphs = positions(document);
            assertEquals(List.of("A", "B", "C", "D"), glyphs.stream().map(Glyph::text).toList());
            int[] rows = {0, 2, 3, 4};
            for (int i = 0; i < rows.length; i++) {
                assertPosition(glyphs.get(i), 20, 40, rows[i]);
            }
        }
        assertNoStages();
    }

    @Test
    void consecutiveLabelsHaveIndependentTextMatricesAndKeepSingleLinePlacement() throws Exception {
        LayoutModel model = model("A\nB", 20, 40);
        LayoutLabel second = label("second", "Z", 100, 90);
        second.setZOrder(10);
        model.addElement(second);
        Path pdf = export(model, "independent labels.pdf", 72);
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            List<Glyph> glyphs = positions(document);
            assertEquals(3, glyphs.size());
            Glyph a = glyphs.stream().filter(g -> g.text().equals("A")).findFirst().orElseThrow();
            Glyph b = glyphs.stream().filter(g -> g.text().equals("B")).findFirst().orElseThrow();
            Glyph z = glyphs.stream().filter(g -> g.text().equals("Z")).findFirst().orElseThrow();
            assertPosition(a, 20, 40, 0);
            assertPosition(b, 20, 40, 1);
            assertPosition(z, 100, 90, 0);
        }
        assertNoStages();
    }

    @Test
    void everyLineIsActuallyVisibleInRenderedPdfAtItsExpectedLocation() throws Exception {
        Path pdf = export(model("A\nB\nC", 20, 40), "visible lines.pdf", 72);
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            BufferedImage image = new PDFRenderer(document).renderImageWithDPI(0, 72);
            try {
                for (int row = 0; row < 3; row++) {
                    int left = (int) Math.floor(mmToPt(20) + 2);
                    double baseline = mmToPt(40) + 3 + 12 * 0.2 + row * 12 * 1.2;
                    int top = (int) Math.floor(baseline - 11);
                    int bottom = (int) Math.ceil(baseline + 1);
                    int darkPixels = 0;
                    for (int y = top; y <= bottom; y++) {
                        for (int x = left; x <= left + 20; x++) {
                            int rgb = image.getRGB(x, y);
                            if (((rgb >> 16) & 255) < 160
                                    && ((rgb >> 8) & 255) < 160
                                    && (rgb & 255) < 160) {
                                darkPixels++;
                            }
                        }
                    }
                    assertTrue(darkPixels > 3,
                            "PDF row " + row + " must be visible at the requested label coordinates.");
                }
            } finally {
                image.flush();
            }
        }
        assertNoStages();
        Files.delete(pdf);
    }

    @Test
    void unsupportedGlyphAfterFirstLineCannotDestroyPreviousProfessionalPdf() throws Exception {
        Path pdf = export(model("SAVED", 20, 40), "previous professional output.pdf", 72);
        byte[] previous = Files.readAllBytes(pdf);
        LayoutModel unencodable = model("First line\n\uD83D\uDE80", 20, 40);
        assertThrows(Exception.class, () -> LayoutExportEngine.exportPdf(unencodable, pdf.toFile(), 72));
        assertArrayEquals(previous, Files.readAllBytes(pdf));
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            assertTrue(new PDFTextStripper().getText(document).contains("SAVED"));
        }
        assertNoStages();
    }

    private static LayoutModel model(String text, double x, double y) {
        LayoutModel model = new LayoutModel();
        model.addElement(label("label", text, x, y));
        return model;
    }

    private static LayoutLabel label(String id, String text, double x, double y) {
        LayoutLabel label = new LayoutLabel(id, text, x, y, 70, 50);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return label;
    }

    private Path export(LayoutModel model, String name, int dpi) throws Exception {
        Path pdf = directory.resolve(name);
        LayoutExportEngine.exportPdf(model, pdf.toFile(), dpi);
        return pdf;
    }

    private static List<Glyph> positions(PDDocument document) throws IOException {
        Positions positions = new Positions();
        positions.getText(document);
        return positions.glyphs;
    }

    private static void assertPosition(Glyph glyph, double xMm, double yMm, int row) {
        assertEquals(mmToPt(xMm) + 3, glyph.x(), 0.05,
                "Every line must keep the same absolute X, not accumulate Td offsets.");
        assertEquals(mmToPt(yMm) + 3 + 12 * 0.2 + row * 12 * 1.2, glyph.y(), 0.05,
                "Top-origin Y must advance by one line height per logical row.");
    }

    private static double mmToPt(double millimetres) {
        return millimetres * 72.0 / 25.4;
    }

    private void assertNoStages() throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            assertFalse(paths.anyMatch(path -> path.getFileName().toString().endsWith(".part")));
        }
    }
}
