package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Atlas v2: framed pages, dynamic page tokens, PDF output and persisted config. */
class AtlasV2ContractTest {

    @TempDir
    Path directory;

    private static final double[] EXTENT_A = {-70.0, -45.0, -68.0, -43.0};
    private static final double[] EXTENT_B = {-69.0, -44.0, -66.0, -41.0};

    private static AtlasEngine.AtlasPage page(String title, double[] extent, Double buffer) {
        return new AtlasEngine.AtlasPage(title, "Hoja", extent[0], extent[1],
                extent[2], extent[3], buffer);
    }

    private static LayoutModel template() {
        LayoutModel model = new LayoutModel();
        model.setPageSpec(LayoutPageSpec.preset(PageSizePreset.A3,
                PageOrientation.LANDSCAPE));
        LayoutLabel title = new LayoutLabel("title", "{feature} - pagina {page}",
                12, 8, 260, 20);
        title.setName("Titulo");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setDynamicExpression("{feature} - pagina {page}");
        model.addElement(title);
        LayoutMap map = new LayoutMap("map", 12, 40, 260, 140);
        map.setName("Marco");
        model.addElement(map);
        return model;
    }

    private static int checksum(BufferedImage image) {
        return Arrays.hashCode(image.getRGB(0, 0, image.getWidth(), image.getHeight(),
                null, 0, image.getWidth()));
    }

    @Test
    void pageTokensAndFramedExtentsRenderDistinctRepeatablePages() {
        LayoutModel model = template();
        List<AtlasEngine.AtlasPage> twoPages = List.of(
                page("Norte", EXTENT_A, 10.0), page("Sur", EXTENT_B, 10.0));
        List<BufferedImage> first = AtlasEngine.generatePages(model, twoPages, 72);
        List<BufferedImage> second = AtlasEngine.generatePages(model, twoPages, 72);
        assertEquals(2, first.size());
        assertNotEquals(checksum(first.get(0)), checksum(first.get(1)),
                "different features must frame and label differently");
        assertEquals(checksum(first.get(0)), checksum(second.get(0)),
                "the same atlas must repeat precisely");
        assertEquals(checksum(first.get(1)), checksum(second.get(1)));
        first.forEach(BufferedImage::flush);
        second.forEach(BufferedImage::flush);
        // Template elements stay untouched.
        LayoutLabel title = (LayoutLabel) model.getElements().get(0);
        assertEquals("{feature} - pagina {page}", title.getText());
        assertEquals("{feature} - pagina {page}", title.getDynamicExpression());
    }

    @Test
    void sameExtentStillChangesWithThePageToken() {
        LayoutModel model = template();
        List<BufferedImage> pages = AtlasEngine.generatePages(model,
                List.of(page("Norte", EXTENT_A, 0.0), page("Norte", EXTENT_A, 0.0)), 72);
        assertNotEquals(checksum(pages.get(0)), checksum(pages.get(1)),
                "page token must differ between pages even with the same extent");
        pages.forEach(BufferedImage::flush);
    }

    @Test
    void pdfExportHasOnePagePerAtlasPageWithTheDocumentSheet() throws Exception {
        LayoutModel model = template();
        File pdf = directory.resolve("atlas.pdf").toFile();
        AtlasEngine.exportPdf(model,
                List.of(page("Norte", EXTENT_A, 5.0), page("Sur", EXTENT_B, 5.0)),
                pdf, 72);

        try (org.apache.pdfbox.pdmodel.PDDocument document =
                     org.apache.pdfbox.Loader.loadPDF(pdf)) {
            assertEquals(2, document.getNumberOfPages());
            float expectedWidth = (float) (420.0 * 72.0 / 25.4);
            float expectedHeight = (float) (297.0 * 72.0 / 25.4);
            for (int i = 0; i < 2; i++) {
                org.apache.pdfbox.pdmodel.common.PDRectangle box =
                        document.getPage(i).getMediaBox();
                assertEquals(expectedWidth, box.getWidth(), 0.5f);
                assertEquals(expectedHeight, box.getHeight(), 0.5f);
            }
        }
    }

    @Test
    void pdfExportRejectsAnEmptyAtlas() {
        File pdf = directory.resolve("empty.pdf").toFile();
        assertThrows(IllegalArgumentException.class,
                () -> AtlasEngine.exportPdf(template(), List.of(), pdf, 72));
    }

    @Test
    void atlasConfigurationRoundTripsWithTheDocument() throws Exception {
        LayoutModel model = template();
        model.setAtlasCoverageLayer("barrios");
        model.setAtlasNameField("nombre");
        model.setAtlasBufferPercent(15.0);
        model.setAtlasOrderAscending(false);
        File file = directory.resolve("config.catmap").toFile();
        CatmapSerializer.save(model, file);

        LayoutModel loaded = CatmapSerializer.load(file);
        assertEquals("barrios", loaded.getAtlasCoverageLayer());
        assertEquals("nombre", loaded.getAtlasNameField());
        assertEquals(15.0, loaded.getAtlasBufferPercent(), 1.0e-9);
        assertTrue(!loaded.isAtlasOrderAscending());
    }

    @Test
    void documentsWithoutAtlasConfigurationUseSafeDefaults() throws Exception {
        String legacy = """
                # CATMAP Layout v1
                PAGE_SIZE=A4
                PAGE_ORIENTATION=LANDSCAPE
                ELEMENT|LayoutLabel|l1|Legacy|10|20|100|30|1|true|false
                """;
        File file = directory.resolve("legacy.catmap").toFile();
        Files.writeString(file.toPath(), legacy, StandardCharsets.UTF_8);

        LayoutModel loaded = CatmapSerializer.load(file);
        assertNull(loaded.getAtlasCoverageLayer());
        assertNull(loaded.getAtlasNameField());
        assertEquals(10.0, loaded.getAtlasBufferPercent(), 1.0e-9);
        assertTrue(loaded.isAtlasOrderAscending());
    }

    @Test
    void corruptAtlasConfigurationFailsClosed() throws Exception {
        String corrupt = """
                # CATMAP Layout v1
                PAGE_SIZE=A4
                PAGE_ORIENTATION=LANDSCAPE
                ATLAS_COVERAGE_LAYER=barrios
                ATLAS_BUFFER_PERCENT=250
                ELEMENT|LayoutLabel|l1|Legacy|10|20|100|30|1|true|false
                """;
        File file = directory.resolve("corrupt.catmap").toFile();
        Files.writeString(file.toPath(), corrupt, StandardCharsets.UTF_8);
        assertThrows(java.io.IOException.class, () -> CatmapSerializer.load(file));
    }

    @Test
    void invalidBufferSetterIsRejected() {
        LayoutModel model = new LayoutModel();
        assertThrows(IllegalArgumentException.class,
                () -> model.setAtlasBufferPercent(-1));
        assertThrows(IllegalArgumentException.class,
                () -> model.setAtlasBufferPercent(101));
    }
}
