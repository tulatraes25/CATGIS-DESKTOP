package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T31/A0.2: golden-master scenes are deterministic, survive a document
 * roundtrip and keep preview == export at the same DPI.
 */
class GoldenMasterContractTest {

    @TempDir
    Path directory;

    @Test
    void everySceneIsDeterministic() {
        for (GoldenMaster.Scene scene : GoldenMaster.scenes()) {
            GoldenMaster.Entry first = GoldenMaster.capture(scene.name(), scene.model(), 96);
            GoldenMaster.Entry second = GoldenMaster.capture(scene.name(), scene.model(), 96);
            assertEquals(first.width(), second.width());
            assertEquals(first.height(), second.height());
            assertEquals(first.sha256(), second.sha256(),
                    "scene " + scene.name() + " must render deterministically");
            assertFalse(first.sha256().isBlank());
        }
    }

    @Test
    void documentRoundtripPreservesTheRenderedHash() throws Exception {
        for (GoldenMaster.Scene scene : GoldenMaster.scenes()) {
            boolean hasLiveMap = scene.model().getElements().stream()
                    .anyMatch(element -> element instanceof ar.com.catgis.layout.LayoutMap);
            if (hasLiveMap) {
                // The map frame content comes from the live project/MapPanel and
                // is intentionally not embedded in the .catmap document.
                continue;
            }
            File file = directory.resolve(scene.name() + ".catmap").toFile();
            CatmapDocumentIO.save(scene.model(), file);
            LayoutModel loaded = CatmapDocumentIO.load(file);
            assertEquals(GoldenMaster.capture(scene.name(), scene.model(), 96).sha256(),
                    GoldenMaster.capture(scene.name(), loaded, 96).sha256(),
                    "roundtrip changed the render of " + scene.name());
        }
    }

    @Test
    void mapPreviewIsRuntimeOnlyNotPersisted() throws Exception {
        GoldenMaster.Scene scene = GoldenMaster.scenes().stream()
                .filter(candidate -> candidate.name().equals("a4_map_preview"))
                .findFirst().orElseThrow();
        java.io.File file = directory.resolve("map-preview.catmap").toFile();
        CatmapDocumentIO.save(scene.model(), file);
        LayoutModel loaded = CatmapDocumentIO.load(file);
        ar.com.catgis.layout.LayoutMap map = loaded.getElements().stream()
                .filter(element -> element instanceof ar.com.catgis.layout.LayoutMap)
                .map(element -> (ar.com.catgis.layout.LayoutMap) element)
                .findFirst().orElseThrow();
        assertEquals(null, map.getPreviewImage(),
                "map previews are runtime caches, not document state");
    }

    @Test
    void previewAndPngExportMatchAtTheSameDpi() throws Exception {
        for (GoldenMaster.Scene scene : GoldenMaster.scenes()) {
            BufferedImage preview = LayoutExportService.renderPage(
                    scene.model(), scene.model().getCurrentPage(), 96);
            File png = directory.resolve(scene.name() + ".png").toFile();
            LayoutExportEngine.exportPng(scene.model(), png, 96);
            BufferedImage exported = ImageIO.read(png);
            assertEquals(GoldenMaster.sha256(preview), GoldenMaster.sha256(exported),
                    "preview/export mismatch for " + scene.name());
            preview.flush();
            if (exported != null) {
                exported.flush();
            }
        }
    }

    @Test
    void mastersReportListsEveryScene() {
        List<GoldenMaster.Entry> entries = new ArrayList<>();
        for (GoldenMaster.Scene scene : GoldenMaster.scenes()) {
            entries.add(GoldenMaster.capture(scene.name(), scene.model(), 96));
        }
        String report = GoldenMaster.reportMarkdown(entries);
        assertTrue(report.startsWith("# Golden masters A0.2"));
        for (GoldenMaster.Scene scene : GoldenMaster.scenes()) {
            assertTrue(report.contains("| " + scene.name() + " |"),
                    "report missing scene " + scene.name());
        }
        assertTrue(entries.stream().allMatch(entry -> entry.width() > 100));
    }

    @Test
    void hashCoversDimensionsAndPixels() {
        BufferedImage image = GoldenMaster.syntheticMapPreview(20, 10);
        String first = GoldenMaster.sha256(image);
        image.setRGB(3, 3, 0xFF00FF00);
        String second = GoldenMaster.sha256(image);
        assertFalse(first.equals(second), "a pixel change must change the hash");

        BufferedImage other = new BufferedImage(20, 10, BufferedImage.TYPE_INT_ARGB);
        assertFalse(GoldenMaster.sha256(other).equals(first),
                "a different image must have a different hash");
    }
}
