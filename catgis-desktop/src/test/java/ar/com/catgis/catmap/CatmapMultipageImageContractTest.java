package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutImage;
import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Real CATMAP v1/v2 filesystem and PNG payload integrity contracts. */
class CatmapMultipageImageContractTest {

    @TempDir Path tempDir;

    private static BufferedImage tinyImage() {
        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, 0xff000000 | ((x * 48) << 16)
                        | ((y * 57) << 8) | (x * y));
            }
        }
        return image;
    }

    private static void assertNoStages(Path parent) throws IOException {
        try (Stream<Path> files = Files.list(parent)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().endsWith(".part")));
        }
    }

    @Test
    void multipageSaveLoadPreservesEveryPageIncludingEmptyPageAndSelectedPage() throws Exception {
        LayoutModel source = new LayoutModel();
        source.addElement(new LayoutLabel("cover-title", "Hoja 1", 11, 12, 180, 20));
        source.addPage();
        source.getCurrentPageElements().add(
                new LayoutLabel("second-title", "Hoja 2", 11, 12, 180, 20));
        LayoutMap map = new LayoutMap("map-2", 12, 30, 160, 110);
        map.setOwnExtent(true);
        map.setOwnViewMinX(-70.125);
        map.setOwnViewMinY(-45.25);
        map.setOwnZoomFactor(2.5);
        map.setAssignedLayerKeys(List.of("vector|C:/gis/capas con espacios.shp"));
        source.getCurrentPageElements().add(map);
        source.getCurrentPageElements().add(new LayoutImage("logo-2", tinyImage(),
                14, 150, 45, 22));
        source.addPage(); // an empty third page must survive.
        source.setCurrentPage(1);

        Path output = tempDir.resolve("proyecto multipagina.catmap");
        CatmapSerializer.save(source, output.toFile());
        String disk = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(disk.startsWith("# CATMAP Layout v2"));
        assertTrue(disk.contains("PAGE_COUNT=3"));
        assertTrue(disk.contains("CURRENT_PAGE=1"));

        LayoutModel loaded = CatmapSerializer.load(output.toFile());
        assertEquals(3, loaded.getPageCount());
        assertEquals(1, loaded.getCurrentPage());
        assertEquals(1, loaded.getPageElements(0).size());
        assertEquals(3, loaded.getPageElements(1).size());
        assertTrue(loaded.getPageElements(2).isEmpty());
        assertEquals("Hoja 1", ((LayoutLabel) loaded.getPageElements(0).get(0)).getText());
        assertEquals("Hoja 2", ((LayoutLabel) loaded.getPageElements(1).get(0)).getText());
        LayoutMap readMap = assertInstanceOf(LayoutMap.class, loaded.getPageElements(1).get(1));
        assertTrue(readMap.isOwnExtent());
        assertEquals(-70.125, readMap.getOwnViewMinX());
        assertEquals(-45.25, readMap.getOwnViewMinY());
        assertEquals(2.5, readMap.getOwnZoomFactor());
        assertEquals(map.getAssignedLayerKeys(), readMap.getAssignedLayerKeys());
        LayoutImage readImage = assertInstanceOf(LayoutImage.class,
                loaded.getPageElements(1).get(2));
        assertNotNull(readImage.getImage());
        assertEquals(4, readImage.getImage().getWidth());
        assertEquals(3, readImage.getImage().getHeight());
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 4; x++) {
                assertEquals(tinyImage().getRGB(x, y), readImage.getImage().getRGB(x, y));
            }
        }
        assertEquals(1, source.getCurrentPage(), "Saving must not change the open editor page.");
    }

    @Test
    void existingOnePageProjectsStayOnCompatibleV1DiskFormat() throws Exception {
        LayoutModel layout = new LayoutModel();
        layout.addElement(new LayoutLabel("legacy-title", "V1 compatible", 8, 12, 140, 18));
        Path file = tempDir.resolve("single page.catmap");
        CatmapSerializer.save(layout, file.toFile());
        String encoded = Files.readString(file);
        assertTrue(encoded.startsWith("# CATMAP Layout v1"));
        assertFalse(encoded.contains("PAGE_BEGIN="));
        LayoutModel loaded = CatmapSerializer.load(file.toFile());
        assertEquals(1, loaded.getPageCount());
        assertEquals("V1 compatible", ((LayoutLabel) loaded.getElements().get(0)).getText());
    }

    @Test
    void embeddedPngEncoderErrorCannotOverwriteSavedProjectOrBackup() throws Exception {
        LayoutModel initial = new LayoutModel();
        initial.addElement(new LayoutLabel("old", "Documento seguro", 10, 10, 120, 20));
        Path file = tempDir.resolve("trabajo seguro.catmap");
        CatmapSerializer.save(initial, file.toFile());
        CatmapSerializer.save(initial, file.toFile());
        byte[] previous = Files.readAllBytes(file);
        byte[] backupBefore = Files.readAllBytes(tempDir.resolve("trabajo seguro.catmap.bak"));

        LayoutModel changed = new LayoutModel();
        changed.addElement(new LayoutLabel("title", "Se perderia", 10, 10, 120, 20));
        changed.addElement(new LayoutImage("new-logo", tinyImage(), 10, 30, 30, 20));
        IOException rejected = assertThrows(IOException.class, () ->
                CatmapSerializer.save(changed, file.toFile(),
                        (staged, target) -> Files.move(staged, target,
                                StandardCopyOption.ATOMIC_MOVE,
                                StandardCopyOption.REPLACE_EXISTING),
                        image -> { throw new IOException("simulated PNG writer failure"); }));
        assertTrue(rejected.getMessage().contains("simulated"));
        assertArrayEquals(previous, Files.readAllBytes(file));
        assertArrayEquals(backupBefore,
                Files.readAllBytes(tempDir.resolve("trabajo seguro.catmap.bak")));
        assertNoStages(tempDir);
    }

    @Test
    void malformedV2PageBoundariesAndUnparseableImagesFailClosed() throws Exception {
        String malformed = """
                # CATMAP Layout v2
                PAGE_SIZE=A4
                PAGE_ORIENTATION=LANDSCAPE
                PAGE_COUNT=2
                CURRENT_PAGE=0
                PAGE_BEGIN=0
                ELEMENT|LayoutLabel|first|First|10|10|100|20|0|true|false
                PAGE_END=0
                PAGE_BEGIN=1
                ELEMENT|LayoutLabel|second|Second|10|10|100|20|0|true|false
                # End of layout
                """;
        Path file = tempDir.resolve("truncated pages.catmap");
        Files.writeString(file, malformed);
        IOException fail = assertThrows(IOException.class,
                () -> CatmapSerializer.load(file.toFile()));
        assertTrue(fail.getMessage().contains("truncated"));

        String invalidImage = """
                # CATMAP Layout v2
                PAGE_COUNT=1
                CURRENT_PAGE=0
                PAGE_BEGIN=0
                ELEMENT|LayoutImage|logo|logo|1|2|20|20|0|true|false|IMAGE_DATA=garbage
                PAGE_END=0
                """;
        Files.writeString(file, invalidImage);
        IOException imageFailure = assertThrows(IOException.class,
                () -> CatmapSerializer.load(file.toFile()));
        assertTrue(imageFailure.getMessage().contains("undecodable"));
    }

    @Test
    void v2RejectsInconsistentPageCountAndUnknownElementInsteadOfDroppingData() throws Exception {
        String badPage = """
                # CATMAP Layout v2
                PAGE_COUNT=2
                CURRENT_PAGE=0
                PAGE_BEGIN=1
                PAGE_END=1
                """;
        Path file = tempDir.resolve("bad page order.catmap");
        Files.writeString(file, badPage);
        assertThrows(IOException.class, () -> CatmapSerializer.load(file.toFile()));

        String unknownElement = """
                # CATMAP Layout v2
                PAGE_COUNT=1
                PAGE_BEGIN=0
                ELEMENT|LayoutExternalPlugin|a|a|1|2|20|20|0|true|false
                PAGE_END=0
                """;
        Files.writeString(file, unknownElement);
        assertThrows(IOException.class, () -> CatmapSerializer.load(file.toFile()));
    }
}
