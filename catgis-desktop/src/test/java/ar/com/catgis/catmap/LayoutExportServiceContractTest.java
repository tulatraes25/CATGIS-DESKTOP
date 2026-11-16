package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One render/export path for every document page, with a parameterized PDF verifier. */
class LayoutExportServiceContractTest {

    @TempDir
    Path directory;

    private static final LayoutPageSpec A3_PORTRAIT =
            LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.PORTRAIT);

    private static LayoutModel pageModel(LayoutPageSpec spec, int pages) {
        LayoutModel model = new LayoutModel();
        model.setPageSpec(spec);
        for (int page = 0; page < pages; page++) {
            if (page > 0) {
                model.addPage();
            }
            model.addElement(new LayoutLabel("p" + page, "PAGINA " + (page + 1),
                    12, 14 + page * 40, 200, 20));
        }
        return model;
    }

    private static byte[] digest(BufferedImage image) throws Exception {
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0,
                image.getWidth());
        ByteBuffer buffer = ByteBuffer.allocate(pixels.length * 4);
        for (int pixel : pixels) {
            buffer.putInt(pixel);
        }
        return MessageDigest.getInstance("SHA-256").digest(buffer.array());
    }

    private void noPartialStages() throws Exception {
        try (Stream<Path> files = Files.list(directory)) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString().endsWith(".part")));
        }
    }

    @Test
    void multiPagePdfExportsEveryDocumentPageWithTheDocumentSheet() throws Exception {
        LayoutModel model = pageModel(A3_PORTRAIT, 3);
        File pdf = directory.resolve("atlas.pdf").toFile();
        LayoutExportService.exportPdf(model, pdf, 72);

        try (org.apache.pdfbox.pdmodel.PDDocument parsed =
                     org.apache.pdfbox.Loader.loadPDF(pdf)) {
            assertEquals(3, parsed.getNumberOfPages());
            float expectedWidth = (float) (297.0 * 72.0 / 25.4);
            float expectedHeight = (float) (420.0 * 72.0 / 25.4);
            for (int page = 0; page < 3; page++) {
                org.apache.pdfbox.pdmodel.common.PDRectangle box =
                        parsed.getPage(page).getMediaBox();
                assertEquals(expectedWidth, box.getWidth(), 0.5f);
                assertEquals(expectedHeight, box.getHeight(), 0.5f);
            }
            String text = new org.apache.pdfbox.text.PDFTextStripper().getText(parsed);
            assertTrue(text.contains("PAGINA 1"));
            assertTrue(text.contains("PAGINA 2"));
            assertTrue(text.contains("PAGINA 3"));
        }
    }

    @Test
    void singlePageDocumentStillExportsOnePdfPage() throws Exception {
        LayoutModel model = pageModel(LayoutPageSpec.A4_LANDSCAPE, 1);
        File pdf = directory.resolve("single.pdf").toFile();
        LayoutExportService.exportPdf(model, pdf, 72);

        try (org.apache.pdfbox.pdmodel.PDDocument parsed =
                     org.apache.pdfbox.Loader.loadPDF(pdf)) {
            assertEquals(1, parsed.getNumberOfPages());
        }
    }

    @Test
    void renderPageUsesTheRequestedPageAndDocumentSize() throws Exception {
        LayoutModel model = pageModel(A3_PORTRAIT, 2);
        int[] expected = CatmapAtomicExport.rasterSize(72, 297.0, 420.0);

        BufferedImage first = LayoutExportService.renderPage(model, 0, 72);
        BufferedImage second = LayoutExportService.renderPage(model, 1, 72);
        assertEquals(expected[0], first.getWidth());
        assertEquals(expected[1], first.getHeight());
        assertEquals(expected[0], second.getWidth());
        assertEquals(expected[1], second.getHeight());
        assertFalse(java.util.Arrays.equals(digest(first), digest(second)),
                "each page must render its own elements");
        first.flush();
        second.flush();
    }

    @Test
    void renderPageRejectsOutOfRangeIndex() {
        LayoutModel model = pageModel(A4(), 1);
        assertThrows(IllegalArgumentException.class,
                () -> LayoutExportService.renderPage(model, 1, 72));
        assertThrows(IllegalArgumentException.class,
                () -> LayoutExportService.renderPage(model, -1, 72));
    }

    private static LayoutPageSpec A4() {
        return LayoutPageSpec.A4_LANDSCAPE;
    }

    @Test
    void pngPagesExportOneAtomicFilePerDocumentPage() throws Exception {
        LayoutModel model = pageModel(A3_PORTRAIT, 3);
        List<File> files = LayoutExportService.exportPngPages(
                model, directory.toFile(), "pagina", 72);

        assertEquals(3, files.size());
        int[] expected = CatmapAtomicExport.rasterSize(72, 297.0, 420.0);
        for (int page = 0; page < files.size(); page++) {
            File file = files.get(page);
            assertEquals("pagina_" + (page + 1) + ".png", file.getName());
            BufferedImage image = ImageIO.read(file);
            assertEquals(expected[0], image.getWidth());
            assertEquals(expected[1], image.getHeight());
            image.flush();
        }
        noPartialStages();
    }

    @Test
    void pngPagesRejectUnsafeBaseNames() {
        LayoutModel model = pageModel(LayoutPageSpec.A4_LANDSCAPE, 1);
        assertThrows(IllegalArgumentException.class,
                () -> LayoutExportService.exportPngPages(model, directory.toFile(), "../evil", 72));
        assertThrows(IllegalArgumentException.class,
                () -> LayoutExportService.exportPngPages(model, directory.toFile(), "a/b", 72));
        assertThrows(IllegalArgumentException.class,
                () -> LayoutExportService.exportPngPages(model, directory.toFile(), "..", 72));
    }

    @Test
    void pdfVerifierIsParameterizedByPageCountAndSheet() throws Exception {
        LayoutModel model = pageModel(A3_PORTRAIT, 3);
        File pdf = directory.resolve("verified.pdf").toFile();
        LayoutExportService.exportPdf(model, pdf, 72);

        CatmapAtomicExport.verifyPdf(pdf.toPath(), 297.0, 420.0, 3);
        assertThrows(java.io.IOException.class,
                () -> CatmapAtomicExport.verifyPdf(pdf.toPath(), 297.0, 420.0, 2));
        assertThrows(java.io.IOException.class,
                () -> CatmapAtomicExport.verifyPdf(pdf.toPath(), 210.0, 297.0, 3));
    }
}
