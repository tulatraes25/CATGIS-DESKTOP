package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Page geometry travels with the CATMAP document and reaches every real export:
 * PNG pixels, PDF MediaBox millimetres and SVG sheet dimensions.
 */
class CatmapPageGeometryContractTest {

    @TempDir
    Path directory;

    private static LayoutModel sample(LayoutPageSpec spec) {
        LayoutModel model = new LayoutModel();
        model.setPageSpec(spec);
        model.addElement(new LayoutLabel("title", "MAPA CATGIS", 12, 14, 120, 20));
        return model;
    }

    @Test
    void a3PortraitRoundTripsThroughTheDocument() throws Exception {
        LayoutPageSpec spec = LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.PORTRAIT);
        File layout = directory.resolve("a3.catmap").toFile();
        CatmapSerializer.save(sample(spec), layout);

        LayoutModel loaded = CatmapSerializer.load(layout);
        assertEquals(spec, loaded.getPageSpec());
        assertEquals(1, loaded.getElements().size());
        String text = Files.readString(layout.toPath(), StandardCharsets.UTF_8);
        assertTrue(text.contains("PAGE_SIZE=A3"));
        assertTrue(text.contains("PAGE_ORIENTATION=PORTRAIT"));
        assertTrue(text.contains("PAGE_WIDTH_MM=297.0"));
        assertTrue(text.contains("PAGE_HEIGHT_MM=420.0"));
    }

    @Test
    void customMillimetresAndMarginsRoundTrip() throws Exception {
        LayoutPageSpec spec = LayoutPageSpec.custom(200.0, 300.0,
                PageOrientation.LANDSCAPE, 10.0, 5.0, 10.0, 5.0);
        File layout = directory.resolve("custom.catmap").toFile();
        CatmapSerializer.save(sample(spec), layout);

        LayoutModel loaded = CatmapSerializer.load(layout);
        assertEquals(spec, loaded.getPageSpec());
        assertEquals(300.0, loaded.getPageSpec().widthMm(), 0.001);
        assertEquals(200.0, loaded.getPageSpec().heightMm(), 0.001);
        assertTrue(loaded.getPageSpec().hasMargins());
    }

    @Test
    void legacyHeaderWithoutExplicitDimensionsLoadsAsA4Landscape() throws Exception {
        String legacy = """
                # CATMAP Layout v1
                PAGE_SIZE=A4
                PAGE_ORIENTATION=LANDSCAPE
                ELEMENT|LayoutLabel|l1|Legacy|10|20|100|30|1|true|false
                """;
        File layout = directory.resolve("legacy.catmap").toFile();
        Files.writeString(layout.toPath(), legacy, StandardCharsets.UTF_8);

        LayoutModel loaded = CatmapSerializer.load(layout);
        assertEquals(LayoutPageSpec.A4_LANDSCAPE, loaded.getPageSpec());
        assertEquals(1, loaded.getElements().size());
    }

    @Test
    void corruptPageWidthFailsClosed() throws Exception {
        String corrupt = """
                # CATMAP Layout v1
                PAGE_SIZE=A4
                PAGE_ORIENTATION=LANDSCAPE
                PAGE_WIDTH_MM=not-a-number
                ELEMENT|LayoutLabel|l1|Broken|10|20|100|30|1|true|false
                """;
        File layout = directory.resolve("corrupt-page.catmap").toFile();
        Files.writeString(layout.toPath(), corrupt, StandardCharsets.UTF_8);

        IOException failure = assertThrows(IOException.class, () -> CatmapSerializer.load(layout));
        assertTrue(failure.getMessage().contains("page width"));
    }

    @Test
    void customPageWithoutDimensionsFailsClosed() throws Exception {
        String corrupt = """
                # CATMAP Layout v1
                PAGE_SIZE=CUSTOM
                PAGE_ORIENTATION=PORTRAIT
                ELEMENT|LayoutLabel|l1|Broken|10|20|100|30|1|true|false
                """;
        File layout = directory.resolve("custom-missing.catmap").toFile();
        Files.writeString(layout.toPath(), corrupt, StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> CatmapSerializer.load(layout));
    }

    @Test
    void unknownPresetFailsClosed() throws Exception {
        String corrupt = """
                # CATMAP Layout v1
                PAGE_SIZE=A2
                ELEMENT|LayoutLabel|l1|Broken|10|20|100|30|1|true|false
                """;
        File layout = directory.resolve("unknown-preset.catmap").toFile();
        Files.writeString(layout.toPath(), corrupt, StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> CatmapSerializer.load(layout));
    }

    @Test
    void pngExportUsesDocumentPage() throws Exception {
        LayoutPageSpec spec = LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.PORTRAIT);
        File png = directory.resolve("a3.png").toFile();
        LayoutExportEngine.exportPng(sample(spec), png, 100);

        int[] expected = CatmapAtomicExport.rasterSize(100, spec.widthMm(), spec.heightMm());
        BufferedImage image = ImageIO.read(png);
        assertEquals(expected[0], image.getWidth());
        assertEquals(expected[1], image.getHeight());
        image.flush();
    }

    @Test
    void pdfExportUsesDocumentPage() throws Exception {
        LayoutPageSpec spec = LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.PORTRAIT);
        File pdf = directory.resolve("a3.pdf").toFile();
        LayoutExportEngine.exportPdf(sample(spec), pdf, 72);

        try (org.apache.pdfbox.pdmodel.PDDocument parsed =
                     org.apache.pdfbox.Loader.loadPDF(pdf)) {
            org.apache.pdfbox.pdmodel.common.PDRectangle box =
                    parsed.getPage(0).getMediaBox();
            assertEquals(297.0 * 72.0 / 25.4, box.getWidth(), 0.5f);
            assertEquals(420.0 * 72.0 / 25.4, box.getHeight(), 0.5f);
        }
    }

    @Test
    void svgExportUsesDocumentPage() throws Exception {
        LayoutPageSpec spec = LayoutPageSpec.custom(200.0, 300.0, PageOrientation.LANDSCAPE);
        File svg = directory.resolve("custom.svg").toFile();
        SvgExportEngine.exportSvg(sample(spec), svg, 72);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        org.w3c.dom.Element root = factory.newDocumentBuilder()
                .parse(svg).getDocumentElement();
        assertEquals(300.0, mmAttribute(root.getAttribute("width")), 0.001);
        assertEquals(200.0, mmAttribute(root.getAttribute("height")), 0.001);
    }

    @Test
    void defaultModelStillExportsLegacyA4Landscape() throws Exception {
        File png = directory.resolve("a4.png").toFile();
        LayoutExportEngine.exportPng(sample(LayoutPageSpec.A4_LANDSCAPE), png, 100);

        int[] expected = CatmapAtomicExport.rasterSize(100, 297.0, 210.0);
        BufferedImage image = ImageIO.read(png);
        assertEquals(expected[0], image.getWidth());
        assertEquals(expected[1], image.getHeight());
        image.flush();
    }

    private static double mmAttribute(String value) {
        assertTrue(value.endsWith("mm"), "expected millimetre attribute: " + value);
        return Double.parseDouble(value.substring(0, value.length() - 2));
    }
}
