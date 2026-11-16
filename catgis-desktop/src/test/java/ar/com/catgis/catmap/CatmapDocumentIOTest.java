package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutLegend;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.LayoutTemplateManager;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One document entry point: canonical line format with legacy composer-JSON migration. */
class CatmapDocumentIOTest {

    @TempDir
    Path directory;

    private static final String LEGACY_JSON = """
            {
              "format": "catmap",
              "version": 1,
              "page": { "widthMm": 297.0, "heightMm": 210.0, "orientation": "landscape" },
              "elements": [
                {"type": "LayoutLabel", "id": "lbl-1", "xMm": 12, "yMm": 10, "wMm": 270,
                 "hMm": 16, "zOrder": 1, "visible": true, "locked": false, "name": "Titulo",
                 "text": "Titulo de Prueba", "fontSize": 20, "fontFamily": "SansSerif",
                 "bold": true, "color": "#1B2638"},
                {"type": "LayoutMap", "id": "map-1", "xMm": 15, "yMm": 25, "wMm": 267,
                 "hMm": 160, "zOrder": 0, "visible": true, "locked": false, "name": "Mapa",
                 "showGrid": true, "gridCols": 5, "gridRows": 4, "gridByDistance": true,
                 "gridIntervalX": 250, "gridIntervalY": 250, "gridUnit": "m",
                 "gridColor": "#336699", "targetScale": 10000, "assignedLayers": "a;b"},
                {"type": "LayoutLegend", "id": "leg-1", "xMm": 155, "yMm": 55, "wMm": 75,
                 "hMm": 40, "zOrder": 2, "visible": true, "locked": false,
                 "name": "Leyenda", "title": "Leyenda de Prueba", "autoHeight": true,
                 "showBg": false, "showBorder": false, "titleFontSize": 12,
                 "itemFontSize": 9, "items": [{"label": "Capa 1", "included": true}]}
              ]
            }
            """;

    @Test
    void canonicalDocumentsRoundTripThroughTheSingleEntryPoint() throws Exception {
        LayoutModel model = new LayoutModel();
        model.setPageSpec(LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.PORTRAIT));
        model.addElement(new LayoutLabel("l1", "A3", 10, 10, 100, 20));

        File file = directory.resolve("canonical.catmap").toFile();
        CatmapDocumentIO.save(model, file);
        assertTrue(Files.readString(file.toPath(), StandardCharsets.UTF_8)
                .startsWith("# CATMAP Layout"));

        LayoutModel loaded = CatmapDocumentIO.load(file);
        assertEquals(1, loaded.getElements().size());
        assertEquals(LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.PORTRAIT),
                loaded.getPageSpec());
    }

    @Test
    void composerSaveNowWritesTheCanonicalFormat() throws Exception {
        LayoutModel model = new LayoutModel();
        model.addElement(new LayoutLabel("l1", "Titulo", 10, 10, 100, 20));

        File file = directory.resolve("composer.catmap").toFile();
        LayoutTemplateManager.saveTemplate(file, model, 297.0, 210.0, "landscape");

        String saved = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertTrue(saved.startsWith("# CATMAP Layout"), "composer must not write legacy JSON");
        LayoutModel loaded = CatmapDocumentIO.load(file);
        assertEquals(LayoutPageSpec.A4_LANDSCAPE, loaded.getPageSpec());
        assertEquals(1, loaded.getElements().size());
    }

    @Test
    void legacyComposerJsonIsMigratedWithPageAndElements() throws Exception {
        File file = directory.resolve("legacy-json.catmap").toFile();
        Files.writeString(file.toPath(), LEGACY_JSON, StandardCharsets.UTF_8);

        LayoutModel loaded = CatmapDocumentIO.load(file);
        assertEquals(3, loaded.getElements().size());
        assertEquals(LayoutPageSpec.A4_LANDSCAPE,
                loaded.getPageSpec());

        boolean label = false;
        boolean map = false;
        boolean legend = false;
        for (var element : loaded.getElements()) {
            if (element instanceof LayoutLabel text) {
                label = true;
                assertEquals("Titulo de Prueba", text.getText());
                assertEquals(20.0, text.getFont().getSize2D(), 0.1);
                assertTrue(text.getFont().isBold());
                assertEquals(0x1B2638, text.getColor().getRGB() & 0xFFFFFF);
            } else if (element instanceof LayoutMap grid) {
                map = true;
                assertTrue(grid.isShowGrid());
                assertTrue(grid.isGridByDistance());
                assertEquals(5, grid.getGridCols());
                assertEquals(10000.0, grid.getTargetScaleDenominator(), 0.001);
                assertEquals(2, grid.getAssignedLayerKeys().size());
            } else if (element instanceof LayoutLegend title) {
                legend = true;
                assertEquals("Leyenda de Prueba", title.getTitle());
            }
        }
        assertTrue(label && map && legend, "all legacy element types must migrate");
    }

    @Test
    void legacyJsonRoundTripsIntoTheCanonicalFormat() throws Exception {
        File legacy = directory.resolve("legacy.catmap").toFile();
        Files.writeString(legacy.toPath(), LEGACY_JSON, StandardCharsets.UTF_8);
        File migrated = directory.resolve("migrated.catmap").toFile();
        CatmapDocumentIO.save(CatmapDocumentIO.load(legacy), migrated);

        LayoutModel reloaded = CatmapDocumentIO.load(migrated);
        assertEquals(3, reloaded.getElements().size());
        assertEquals(LayoutPageSpec.A4_LANDSCAPE,
                reloaded.getPageSpec());
    }

    @Test
    void legacyPortraitPageIsMigratedWithEffectiveDimensions() throws Exception {
        String portrait = LEGACY_JSON
                .replace("\"widthMm\": 297.0, \"heightMm\": 210.0", "\"widthMm\": 210.0, \"heightMm\": 297.0")
                .replace("\"orientation\": \"landscape\"", "\"orientation\": \"portrait\"");
        File file = directory.resolve("portrait.catmap").toFile();
        Files.writeString(file.toPath(), portrait, StandardCharsets.UTF_8);

        LayoutModel loaded = CatmapDocumentIO.load(file);
        assertEquals(LayoutPageSpec.preset(PageSizePreset.A4, PageOrientation.PORTRAIT),
                loaded.getPageSpec());
        assertEquals(210.0, loaded.getPageSpec().widthMm(), 0.001);
        assertEquals(297.0, loaded.getPageSpec().heightMm(), 0.001);
    }

    @Test
    void unknownFormatFailsClosed() throws Exception {
        File file = directory.resolve("unknown.catmap").toFile();
        Files.writeString(file.toPath(), "esto no es un documento CATMAP", StandardCharsets.UTF_8);

        java.io.IOException failure = assertThrows(java.io.IOException.class,
                () -> CatmapDocumentIO.load(file));
        assertTrue(failure.getMessage().contains("not recognized"));
    }

    @Test
    void jsonWithoutCatmapFormatFailsClosed() throws Exception {
        File file = directory.resolve("foreign.catmap").toFile();
        Files.writeString(file.toPath(), "{\"format\":\"other\",\"elements\":[]}", StandardCharsets.UTF_8);

        assertThrows(java.io.IOException.class, () -> CatmapDocumentIO.load(file));
    }

    @Test
    void legacyUnsupportedElementTypeFailsClosed() throws Exception {
        String unsupported = LEGACY_JSON.replace("\"type\": \"LayoutLabel\"",
                "\"type\": \"LayoutRectangle\"");
        File file = directory.resolve("unsupported.catmap").toFile();
        Files.writeString(file.toPath(), unsupported, StandardCharsets.UTF_8);

        java.io.IOException failure = assertThrows(java.io.IOException.class,
                () -> CatmapDocumentIO.load(file));
        assertTrue(failure.getMessage().contains("unsupported element type"));
    }

    @Test
    void legacyElementWithoutCoordinatesFailsClosed() throws Exception {
        String corrupt = LEGACY_JSON.replace("\"xMm\": 12, ", "");
        File file = directory.resolve("corrupt.catmap").toFile();
        Files.writeString(file.toPath(), corrupt, StandardCharsets.UTF_8);

        assertThrows(java.io.IOException.class, () -> CatmapDocumentIO.load(file));
    }

    @Test
    void managerLoadTemplateReadsLegacyJson() throws Exception {
        File file = directory.resolve("legacy-manager.catmap").toFile();
        Files.writeString(file.toPath(), LEGACY_JSON, StandardCharsets.UTF_8);

        LayoutModel model = new LayoutModel();
        LayoutTemplateManager.loadTemplate(file, model);
        assertEquals(3, model.getElements().size());
        assertEquals(LayoutPageSpec.A4_LANDSCAPE,
                model.getPageSpec());
    }
}
