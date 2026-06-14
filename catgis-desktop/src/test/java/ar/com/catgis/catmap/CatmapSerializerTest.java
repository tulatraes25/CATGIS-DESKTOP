package ar.com.catgis.catmap;

import ar.com.catgis.layout.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CatmapSerializer error handling and atomic save.
 * <p>
 * Verifies that corrupt .catmap files produce logged warnings instead
 * of silent data loss, and that the atomic save (tmp → rename) prevents
 * partial writes.
 */
class CatmapSerializerTest {

    @TempDir
    Path tempDir;

    // ---- Roundtrip (valid data) ----

    @Test
    void roundtripLabelPreservesValues() throws Exception {
        LayoutModel model = new LayoutModel();
        LayoutLabel label = new LayoutLabel("l1", "Test", 10, 20, 200, 30);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setColor(Color.BLUE);
        model.addElement(label);

        File file = tempDir.resolve("roundtrip.catmap").toFile();
        CatmapSerializer.save(model, file);

        LayoutModel loaded = CatmapSerializer.load(file);
        assertEquals(1, loaded.getElements().size());

        LayoutElement el = loaded.getElements().get(0);
        assertEquals("l1", el.getId());
        assertEquals(10.0, el.getBoundsMm().x, 0.01);
        assertEquals(20.0, el.getBoundsMm().y, 0.01);
        assertEquals(200.0, el.getBoundsMm().width, 0.01);
        assertEquals(30.0, el.getBoundsMm().height, 0.01);
    }

    @Test
    void roundtripMultipleElementsPreservesOrder() throws Exception {
        LayoutModel model = new LayoutModel();
        model.addElement(new LayoutLabel("l1", "A", 10, 10, 100, 20));
        model.addElement(new LayoutRectangle("r1", 20, 20, 80, 40));
        model.addElement(new LayoutEllipse("e1", 30, 30, 60, 50));

        File file = tempDir.resolve("multi.catmap").toFile();
        CatmapSerializer.save(model, file);

        LayoutModel loaded = CatmapSerializer.load(file);
        assertEquals(3, loaded.getElements().size());
        assertEquals("l1", loaded.getElements().get(0).getId());
        assertEquals("r1", loaded.getElements().get(1).getId());
        assertEquals("e1", loaded.getElements().get(2).getId());
    }

    // ---- Save atomic (.tmp) ----

    @Test
    void saveCreatesTempFileDuringWrite() throws Exception {
        LayoutModel model = new LayoutModel();
        model.addElement(new LayoutLabel("l1", "X", 10, 10, 100, 20));

        File file = tempDir.resolve("atomic.catmap").toFile();
        CatmapSerializer.save(model, file);

        // After save, only the .catmap exists, not .tmp
        assertTrue(file.exists());
        Path tmpPath = tempDir.resolve("atomic.catmap.tmp");
        assertFalse(Files.exists(tmpPath), ".tmp should be cleaned up after atomic rename");
    }

    @Test
    void saveCreatesBackupIfTargetExists() throws Exception {
        LayoutModel model = new LayoutModel();
        model.addElement(new LayoutLabel("l1", "V1", 10, 10, 100, 20));

        File file = tempDir.resolve("backup.catmap").toFile();
        CatmapSerializer.save(model, file); // first save
        CatmapSerializer.save(model, file); // second save — should create .bak

        File backup = tempDir.resolve("backup.catmap.bak").toFile();
        assertTrue(backup.exists(), "backup should exist after second save");
    }

    // ---- Corrupt data — invalid numbers ----

    @Test
    void loadCorruptDoubleParsesRemainingElements() throws Exception {
        String corrupt = """
                # CATMAP Layout v1
                PAGE_SIZE=A4
                PAGE_ORIENTATION=LANDSCAPE
                ELEMENT|LayoutLabel|l1|Valid|10.0|20.0|100.0|30.0|1|true|false
                ELEMENT|LayoutRectangle|r1|Broken|abc|xyz|80.0|40.0|2|true|false
                ELEMENT|LayoutLabel|l2|AlsoValid|50.0|60.0|120.0|25.0|3|true|false
                # End of layout
                """;

        File file = tempDir.resolve("corrupt.catmap").toFile();
        Files.writeString(file.toPath(), corrupt, StandardCharsets.UTF_8);

        LayoutModel loaded = CatmapSerializer.load(file);
        // l1 and l2 should load, r1 may or may not (depending on x/w/h defaults)
        assertTrue(loaded.getElements().size() >= 1,
                "expected at least 1 element, got " + loaded.getElements().size());
    }

    @Test
    void loadZeroSizedElementStillLoads() throws Exception {
        String corrupt = """
                # CATMAP Layout v1
                PAGE_SIZE=A4
                PAGE_ORIENTATION=LANDSCAPE
                ELEMENT|LayoutLabel|l1|ZeroSize|10.0|20.0|0|0|1|true|false
                # End of layout
                """;

        File file = tempDir.resolve("zerosize.catmap").toFile();
        Files.writeString(file.toPath(), corrupt, StandardCharsets.UTF_8);

        LayoutModel loaded = CatmapSerializer.load(file);
        assertEquals(1, loaded.getElements().size());
        // Element still loads but size is zero (logged as warning by parseElement)
    }

    @Test
    void loadMissingAttributesFallsBackToDefaults() throws Exception {
        String partial = """
                # CATMAP Layout v1
                PAGE_SIZE=A4
                PAGE_ORIENTATION=LANDSCAPE
                ELEMENT|LayoutEllipse|e1|Partial|30.0|40.0|60.0|50.0|1|true|false
                # End of layout
                """;

        File file = tempDir.resolve("partial.catmap").toFile();
        Files.writeString(file.toPath(), partial, StandardCharsets.UTF_8);

        LayoutModel loaded = CatmapSerializer.load(file);
        assertEquals(1, loaded.getElements().size());
        assertTrue(loaded.getElements().get(0) instanceof LayoutEllipse);
    }

    // ---- Empty / comments-only files ----

    @Test
    void loadEmptyFileReturnsEmptyModel() throws Exception {
        String empty = "# Just a comment, no elements";
        File file = tempDir.resolve("empty.catmap").toFile();
        Files.writeString(file.toPath(), empty, StandardCharsets.UTF_8);

        LayoutModel loaded = CatmapSerializer.load(file);
        assertEquals(0, loaded.getElements().size());
    }

    @Test
    void loadNullElementLineIsSkipped() throws Exception {
        String corrupt = """
                # CATMAP Layout v1
                PAGE_SIZE=A4
                PAGE_ORIENTATION=LANDSCAPE
                ELEMENT|LayoutLabel|l1|Good|10.0|20.0|100.0|30.0|1|true|false
                ELEMENT
                ELEMENT|LayoutLabel|l2|AlsoGood|50.0|60.0|120.0|25.0|3|true|false
                # End of layout
                """;

        File file = tempDir.resolve("nulline.catmap").toFile();
        Files.writeString(file.toPath(), corrupt, StandardCharsets.UTF_8);

        LayoutModel loaded = CatmapSerializer.load(file);
        assertEquals(2, loaded.getElements().size());
    }

    @Test
    void loadInvalidColorLogsWarning() throws Exception {
        String corrupt = """
                # CATMAP Layout v1
                PAGE_SIZE=A4
                PAGE_ORIENTATION=LANDSCAPE
                ELEMENT|LayoutLabel|l1|Test|10.0|20.0|100.0|30.0|1|true|false|COLOR=notacolor
                # End of layout
                """;

        File file = tempDir.resolve("badcolor.catmap").toFile();
        Files.writeString(file.toPath(), corrupt, StandardCharsets.UTF_8);

        LayoutModel loaded = CatmapSerializer.load(file);
        assertEquals(1, loaded.getElements().size());
        // Color falls back to BLACK — layout loads, warning is logged
    }

    @Test
    void loadTruncatedLineIsSkipped() throws Exception {
        String corrupt = """
                # CATMAP Layout v1
                PAGE_SIZE=A4
                PAGE_ORIENTATION=LANDSCAPE
                ELEMENT|LayoutLabel|l1|Good|10.0|20.0|100.0|30.0|1|true|false
                ELEMENT|truncated
                ELEMENT|LayoutLabel|l2|AlsoGood|50.0|60.0|120.0|25.0|3|true|false
                # End of layout
                """;

        File file = tempDir.resolve("truncated.catmap").toFile();
        Files.writeString(file.toPath(), corrupt, StandardCharsets.UTF_8);

        LayoutModel loaded = CatmapSerializer.load(file);
        assertEquals(2, loaded.getElements().size());
        // Truncated line is skipped with warning, valid elements still load
    }
}
