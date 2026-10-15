package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtlasEngineSafetyContractTest {

    @TempDir Path tempDir;

    private static final AtlasEngine.AtlasPage PAGE_A =
            new AtlasEngine.AtlasPage("Mapa A", "Region A", -70.0, -45.0, 1.25);
    private static final AtlasEngine.AtlasPage PAGE_B =
            new AtlasEngine.AtlasPage("Mapa B", "Region B", -69.0, -44.0, 2.50);

    private static LayoutModel model() {
        LayoutModel model = new LayoutModel();
        LayoutLabel title = new LayoutLabel("title", "Original", 12, 8, 260, 20);
        title.setName("Titulo");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setColor(Color.BLUE);
        model.addElement(title);

        LayoutLabel subtitle = new LayoutLabel("subtitle", "Subtitulo original", 12, 34, 260, 13);
        subtitle.setName("Subtitulo");
        model.addElement(subtitle);

        LayoutMap map = new LayoutMap("map", 12, 53, 260, 140);
        map.setName("Marco cartografico");
        map.setOwnExtent(true);
        map.setOwnViewMinX(-75.0);
        map.setOwnViewMinY(-50.0);
        map.setOwnZoomFactor(0.90);
        map.setShowGrid(true);
        map.setGridCols(5);
        map.setAssignedLayerKeys(List.of("vector|c:/project/roads.shp"));
        BufferedImage preview = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < preview.getHeight(); y++) {
            for (int x = 0; x < preview.getWidth(); x++) {
                preview.setRGB(x, y, ((x * 7) << 16) | ((y * 7) << 8) | 0x44);
            }
        }
        map.setPreviewImage(preview);
        model.addElement(map);
        return model;
    }

    private static int checksum(BufferedImage image) {
        return Arrays.hashCode(((DataBufferInt) image.getRaster().getDataBuffer()).getData());
    }

    private static void assertOriginal(LayoutModel template) {
        LayoutLabel title = (LayoutLabel) template.getElements().get(0);
        LayoutLabel subtitle = (LayoutLabel) template.getElements().get(1);
        LayoutMap map = (LayoutMap) template.getElements().get(2);
        assertEquals("Original", title.getText());
        assertEquals("Subtitulo original", subtitle.getText());
        assertEquals(Color.BLUE, title.getColor());
        assertEquals("SansSerif", title.getFont().getFamily());
        assertEquals(true, map.isOwnExtent());
        assertEquals(-75.0, map.getOwnViewMinX());
        assertEquals(-50.0, map.getOwnViewMinY());
        assertEquals(0.90, map.getOwnZoomFactor());
        assertEquals(5, map.getGridCols());
        assertEquals(List.of("vector|c:/project/roads.shp"), map.getAssignedLayerKeys());
    }

    @Test
    void separateAtlasPagesCannotAlterTheOpenTemplateAndAreRepeatable() {
        LayoutModel template = model();
        List<BufferedImage> first = AtlasEngine.generatePages(
                template, List.of(PAGE_A, PAGE_B, PAGE_A), 72);
        assertEquals(3, first.size());
        assertOriginal(template);
        assertNotEquals(checksum(first.get(0)), checksum(first.get(1)),
                "Per-page title/subtitle must actually affect rendering.");
        assertEquals(checksum(first.get(0)), checksum(first.get(2)),
                "A/B/A must repeat precisely, without page-to-page contamination.");

        List<BufferedImage> second = AtlasEngine.generatePages(
                template, List.of(PAGE_A, PAGE_B), 72);
        assertEquals(checksum(first.get(0)), checksum(second.get(0)));
        assertEquals(checksum(first.get(1)), checksum(second.get(1)));
        assertOriginal(template);
        first.forEach(BufferedImage::flush);
        second.forEach(BufferedImage::flush);
    }

    @Test
    void invalidPartialExtentAndExcessiveDpiFailBeforeMutatingTemplate() {
        LayoutModel template = model();
        AtlasEngine.AtlasPage incomplete =
                new AtlasEngine.AtlasPage("bad", null, -70.0, null, null);
        assertThrows(IllegalArgumentException.class, () ->
                AtlasEngine.generatePages(template, List.of(incomplete), 72));
        assertThrows(IllegalArgumentException.class, () ->
                AtlasEngine.generatePages(template, List.of(PAGE_A), 2000));
        assertThrows(IllegalArgumentException.class, () ->
                AtlasEngine.generatePages(template, List.of(PAGE_A), 0));
        assertOriginal(template);
    }

    @Test
    void hugeInMemoryAtlasIsRejectedBeforeAllocatingAllPages() {
        LayoutModel template = model();
        List<AtlasEngine.AtlasPage> many = java.util.Collections.nCopies(160, PAGE_A);
        assertThrows(IllegalArgumentException.class, () ->
                AtlasEngine.generatePages(template, many, 72));
        assertOriginal(template);
    }

    @Test
    void streamingPngExportPreservesLayoutAndExistingFiles() throws Exception {
        LayoutModel template = model();
        Path dir = tempDir.resolve("cartografia exportada");
        AtlasEngine.generateAndSave(template, List.of(PAGE_A, PAGE_B),
                dir.toFile(), "atlas profesional", 72);
        Path first = dir.resolve("atlas profesional_1.png");
        Path second = dir.resolve("atlas profesional_2.png");
        assertTrue(Files.size(first) > 100);
        assertTrue(Files.size(second) > 100);
        BufferedImage one = ImageIO.read(first.toFile());
        BufferedImage two = ImageIO.read(second.toFile());
        assertNotNull(one);
        assertNotNull(two);
        assertTrue(one.getWidth() > 700);
        assertEquals(one.getWidth(), two.getWidth());
        assertNotEquals(checksum(one), checksum(two));
        one.flush();
        two.flush();
        assertOriginal(template);
        byte[] previous = Files.readAllBytes(first);
        assertThrows(IOException.class, () ->
                AtlasEngine.generateAndSave(template, List.of(PAGE_A),
                        dir.toFile(), "atlas profesional", 72));
        assertArrayEquals(previous, Files.readAllBytes(first),
                "An existing professional atlas page must not be silently overwritten.");
        assertOriginal(template);
    }

    @Test
    void atomicPublisherFailureCleansStageAndNeverPublishesFinalPage() throws Exception {
        LayoutModel template = model();
        Path dir = tempDir.resolve("atlas");
        AtomicMoveNotSupportedException error = assertThrows(
                AtomicMoveNotSupportedException.class, () -> {
                    // The public wrapper translates the unsupported atomic move to
                    // IOException. Verify that explicitly below instead.
                    throw new AtomicMoveNotSupportedException("unused", "unused", "unused");
                });
        assertNotNull(error);
        IOException blocked = assertThrows(IOException.class, () ->
                AtlasEngine.generateAndSave(template, List.of(PAGE_A),
                        dir.toFile(), "atlas", 72,
                        (staged, target) -> {
                            throw new AtomicMoveNotSupportedException(
                                    staged.toString(), target.toString(), "simulated atomic refusal");
                        }));
        assertTrue(blocked.getMessage().contains("atomic"));
        assertFalse(Files.exists(dir.resolve("atlas_1.png")));
        try (Stream<Path> files = Files.list(dir)) {
            assertTrue(files.noneMatch(x -> x.getFileName().toString().endsWith(".part")));
        }
        assertOriginal(template);
    }

    @Test
    void unsafeOutputNameIsRejectedBeforeDirectoryCreation() {
        LayoutModel template = model();
        Path unsafeDir = tempDir.resolve("never-created");
        assertThrows(IOException.class, () ->
                AtlasEngine.generateAndSave(template, List.of(PAGE_A),
                        unsafeDir.toFile(), "../outside", 72));
        assertFalse(Files.exists(unsafeDir));
        assertOriginal(template);
    }
}
