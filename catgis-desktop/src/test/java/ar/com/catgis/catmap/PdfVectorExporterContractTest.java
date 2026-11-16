package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Vector PDF: text is extractable, the map is the only raster, multi-page keeps the sheet. */
class PdfVectorExporterContractTest {

    @TempDir
    Path directory;

    private static LayoutModel modelWithLabel(String text) {
        LayoutModel model = new LayoutModel();
        LayoutLabel label = new LayoutLabel("lbl", text, 12, 10, 250, 14);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        model.addElement(label);
        return model;
    }

    private static String extractText(File file) throws Exception {
        try (PDDocument document = Loader.loadPDF(file)) {
            return new PDFTextStripper().getText(document);
        }
    }

    @Test
    void textIsRealExtractableContentOnTheDocumentSheet() throws Exception {
        File pdf = directory.resolve("vector.pdf").toFile();
        LayoutModel model = modelWithLabel("Titulo CATGIS - Area 2");
        PdfVectorExporter.export(model, pdf);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(1, document.getNumberOfPages());
            assertEquals(841.89f, document.getPage(0).getMediaBox().getWidth(), 0.6f);
            assertEquals(595.28f, document.getPage(0).getMediaBox().getHeight(), 0.6f);
        }
        assertTrue(extractText(pdf).contains("Titulo CATGIS - Area 2"),
                "PDF text must be extractable, not rasterized");
    }

    @Test
    void multipagePdfKeepsEveryPageAndItsText() throws Exception {
        LayoutModel model = new LayoutModel();
        model.addElement(new LayoutLabel("p1", "PAGINA UNO", 12, 10, 200, 14));
        model.addPage();
        model.addElement(new LayoutLabel("p2", "PAGINA DOS", 12, 10, 200, 14));
        model.addPage();
        model.addElement(new LayoutLabel("p3", "PAGINA TRES", 12, 10, 200, 14));

        File pdf = directory.resolve("multipagina.pdf").toFile();
        PdfVectorExporter.export(model, pdf);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(3, document.getNumberOfPages());
        }
        String text = extractText(pdf);
        assertTrue(text.contains("PAGINA UNO"));
        assertTrue(text.contains("PAGINA DOS"));
        assertTrue(text.contains("PAGINA TRES"));
    }

    @Test
    void mapFrameIsTheOnlyRasterImage() throws Exception {
        LayoutModel model = modelWithLabel("Mapa del proyecto");
        LayoutMap map = new LayoutMap("map", 12, 30, 260, 150);
        BufferedImage preview = new BufferedImage(30, 20, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < preview.getHeight(); y++) {
            for (int x = 0; x < preview.getWidth(); x++) {
                preview.setRGB(x, y, (x * 9) << 8 | y);
            }
        }
        map.setPreviewImage(preview);
        model.addElement(map);

        File pdf = directory.resolve("mapa.pdf").toFile();
        PdfVectorExporter.export(model, pdf);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            PDPage page = document.getPage(0);
            int images = 0;
            for (var name : page.getResources().getXObjectNames()) {
                if (page.getResources().getXObject(name) instanceof PDImageXObject) {
                    images++;
                }
            }
            assertEquals(1, images, "only the map frame is rasterized");
        }
        assertTrue(extractText(pdf).contains("Mapa del proyecto"));
    }

    @Test
    void a3PortraitSheetIsHonoured() throws Exception {
        LayoutModel model = new LayoutModel();
        model.setPageSpec(LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.PORTRAIT));
        model.addElement(new LayoutLabel("lbl", "A3 vertical", 12, 10, 200, 14));
        File pdf = directory.resolve("a3.pdf").toFile();
        PdfVectorExporter.export(model, pdf);
        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(841.89f, document.getPage(0).getMediaBox().getWidth(), 0.6f);
            assertEquals(1190.55f, document.getPage(0).getMediaBox().getHeight(), 0.6f);
        }
    }

    @Test
    void exportReplacesAnExistingFileAndRejectsMissingInputs() throws Exception {
        File pdf = directory.resolve("reemplazo.pdf").toFile();
        Files.writeString(pdf.toPath(), "basura anterior", StandardCharsets.UTF_8);
        PdfVectorExporter.export(modelWithLabel("Reemplazo"), pdf);
        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(1, document.getNumberOfPages());
        }
        try (var files = Files.list(directory)) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString().endsWith(".part")));
        }
        assertThrows(NullPointerException.class, () -> PdfVectorExporter.export(null, pdf));
        assertThrows(NullPointerException.class,
                () -> PdfVectorExporter.export(modelWithLabel("x"), null));
    }
}
