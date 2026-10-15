package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Existing entry-point behavior; does not claim a displayed/native GUI was tested. */
class GeoParquetOpenFlowContractTest {
    @TempDir Path temp;

    @Test
    void silentOpenDoesNotReadMetadataOrCreateAProjectOrImportALayer() throws Exception {
        Object originalProject = AppContext.project();
        File mustNotRead = new File("silent.parquet") {
            @Override public Path toPath() { throw new AssertionError("Silent metadata open must not perform file I/O."); }
        };
        SwingUtilities.invokeAndWait(() ->
                assertFalse(OpenFileAction.openSelectedFile(mustNotRead, "parquet", null, false)));
        assertSame(originalProject, AppContext.project());
    }

    @Test
    void realValidAndCorruptFilesRemainUnchangedAndDoNotCountAsLoadedLayers() throws Exception {
        Object originalProject = AppContext.project();
        Path valid = GeoParquetTestFixtures.empty(temp.resolve("inspeccion \u00f1.geoparquet"));
        Path corrupt = temp.resolve("broken.parquet");
        Files.writeString(corrupt, "invalid");
        byte[] before = Files.readAllBytes(valid);
        SwingUtilities.invokeAndWait(() -> assertFalse(OpenFileAction.openSelectedFiles(
                new File[] {valid.toFile(), null, corrupt.toFile()}, "all", null, false)));
        assertArrayEquals(before, Files.readAllBytes(valid));
        assertEquals("invalid", Files.readString(corrupt));
        assertSame(originalProject, AppContext.project());
        Files.delete(valid);
        Files.delete(corrupt);
    }

    @Test
    void headlessPresentationIsExplicitlyAbsentEvenWhenDialogsAreRequested() throws Exception {
        assertTrue(GraphicsEnvironment.isHeadless(), "This contract requires the normal headless CI suite.");
        File mustNotRead = new File("headless.geoparquet") {
            @Override public Path toPath() { throw new AssertionError("Headless UI adapter must not inspect a file."); }
        };
        SwingUtilities.invokeAndWait(() -> assertFalse(GeoParquetInspection.open(mustNotRead, null, true)));
        assertFalse(GeoParquetInspection.open(null, null, true));
    }

    @Test
    void existingOpenEntryPointDelegatesWithoutInlineMetadataIoOrUnconditionalDialogs() throws Exception {
        String source = Files.readString(Path.of("src/ar/com/catgis/OpenFileAction.java"));
        String start = "} else if (lowerName.endsWith(\".parquet\") || lowerName.endsWith(\".geoparquet\")) {";
        int from = source.indexOf(start);
        assertTrue(from >= 0);
        int to = source.indexOf("\n            }", from);
        assertTrue(to > from);
        String branch = source.substring(from, to);
        assertTrue(branch.contains("return GeoParquetInspection.open(file, parent, showDialogs);"));
        assertFalse(branch.contains("GeoParquetReader"));
        assertFalse(branch.contains("NotificationManager"));
        assertFalse(branch.contains("AppErrorSupport"));
    }
}
