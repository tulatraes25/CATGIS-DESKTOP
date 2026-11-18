package ar.com.catgis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T1/#155: shapefile family publication is journaled and crash-recoverable. */
class VectorFamilyJournalContractTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void resetSeam() {
        VectorExportPublisher.crashAfterMovesForTesting(-1);
    }

    private void createPreviousDataset(String base) throws IOException {
        write(base + ".shp", "SHP-VIEJO");
        write(base + ".shx", "SHX-VIEJO");
        write(base + ".dbf", "DBF-VIEJO");
        write(base + ".prj", "PRJ-VIEJO");
    }

    private void writeNewFamily(Path staging, String base) throws IOException {
        Path directory = staging.getParent();
        Files.writeString(directory.resolve(base + ".shp"), "SHP-NUEVO", StandardCharsets.UTF_8);
        Files.writeString(directory.resolve(base + ".shx"), "SHX-NUEVO", StandardCharsets.UTF_8);
        Files.writeString(directory.resolve(base + ".dbf"), "DBF-NUEVO", StandardCharsets.UTF_8);
    }

    private void write(String name, String content) throws IOException {
        Files.writeString(tempDir.resolve(name), content, StandardCharsets.UTF_8);
    }

    private String read(String name) throws IOException {
        return Files.readString(tempDir.resolve(name), StandardCharsets.UTF_8);
    }

    private void assertPreviousIntact(String base) throws IOException {
        assertEquals("SHP-VIEJO", read(base + ".shp"));
        assertEquals("SHX-VIEJO", read(base + ".shx"));
        assertEquals("DBF-VIEJO", read(base + ".dbf"));
        assertEquals("PRJ-VIEJO", read(base + ".prj"));
    }

    private void assertNoJournalOrLock(String base) throws IOException {
        assertFalse(Files.exists(tempDir.resolve("." + base + ".catgis-publish.journal")),
                "journal must not survive a completed flow");
        assertFalse(Files.exists(tempDir.resolve("." + base + ".catgis-publish.lock")),
                "lock must not survive a completed flow");
        try (Stream<Path> entries = Files.list(tempDir)) {
            List<String> leftovers = entries.map(path -> path.getFileName().toString())
                    .filter(name -> name.contains(".catgis-old-")
                            || name.contains(".catgis-publish.journal")
                            || name.endsWith(".journal.tmp"))
                    .toList();
            assertTrue(leftovers.isEmpty(), "leftovers: " + leftovers);
        }
    }

    @Test
    void successfulReplaceLeavesNoJournalOrBackups() throws Exception {
        createPreviousDataset("pozos");
        VectorExportPublisher.publishShapefile(tempDir.resolve("pozos.shp").toFile(),
                staging -> {
                    writeNewFamily(staging.toPath(), "pozos");
                });
        assertEquals("SHP-NUEVO", read("pozos.shp"));
        assertEquals("DBF-NUEVO", read("pozos.dbf"));
        assertFalse(Files.exists(tempDir.resolve("pozos.prj")),
                "stale sidecars must disappear with the previous family");
        assertNoJournalOrLock("pozos");
    }

    @Test
    void crashAfterBackupsRestoresPreviousFamily() throws Exception {
        createPreviousDataset("pozos");
        VectorExportPublisher.crashAfterMovesForTesting(2);

        assertThrows(Error.class, () -> VectorExportPublisher.publishShapefile(
                tempDir.resolve("pozos.shp").toFile(),
                staging -> writeNewFamily(staging.toPath(), "pozos")));

        Path journal = tempDir.resolve(".pozos.catgis-publish.journal");
        assertTrue(Files.exists(journal), "interrupted publish must leave a journal");

        VectorExportPublisher.recoverPendingPublish(tempDir, "pozos");

        assertPreviousIntact("pozos");
        assertNoJournalOrLock("pozos");
    }

    @Test
    void crashDuringNewFamilyPublicationRemovesPartialOutput() throws Exception {
        VectorExportPublisher.crashAfterMovesForTesting(1);

        assertThrows(Error.class, () -> VectorExportPublisher.publishShapefile(
                tempDir.resolve("nuevos.shp").toFile(),
                staging -> writeNewFamily(staging.toPath(), "nuevos")));

        assertTrue(Files.exists(tempDir.resolve(".nuevos.catgis-publish.journal")));
        VectorExportPublisher.recoverPendingPublish(tempDir, "nuevos");

        assertFalse(Files.exists(tempDir.resolve("nuevos.shp")),
                "a crashed first publication must not leave a partial family");
        assertNoJournalOrLock("nuevos");
    }

    @Test
    void nextPublicationRecoversInterruptedOneBeforeWriting() throws Exception {
        createPreviousDataset("pozos");
        VectorExportPublisher.crashAfterMovesForTesting(2);
        assertThrows(Error.class, () -> VectorExportPublisher.publishShapefile(
                tempDir.resolve("pozos.shp").toFile(),
                staging -> writeNewFamily(staging.toPath(), "pozos")));

        VectorExportPublisher.publishShapefile(tempDir.resolve("pozos.shp").toFile(),
                staging -> writeNewFamily(staging.toPath(), "pozos"));

        assertEquals("SHP-NUEVO", read("pozos.shp"));
        assertNoJournalOrLock("pozos");
    }

    @Test
    void freshLockBlocksConcurrentPublication() throws Exception {
        createPreviousDataset("pozos");
        Path lock = tempDir.resolve(".pozos.catgis-publish.lock");
        Files.writeString(lock, "1234|" + System.currentTimeMillis(), StandardCharsets.UTF_8);

        IOException failure = assertThrows(IOException.class,
                () -> VectorExportPublisher.publishShapefile(
                        tempDir.resolve("pozos.shp").toFile(),
                        staging -> writeNewFamily(staging.toPath(), "pozos")));

        assertTrue(failure.getMessage().contains("bloqueada"), failure.getMessage());
        assertPreviousIntact("pozos");
        Files.deleteIfExists(lock);
    }

    @Test
    void staleLockIsRecovered() throws Exception {
        createPreviousDataset("pozos");
        Path lock = tempDir.resolve(".pozos.catgis-publish.lock");
        Files.writeString(lock, "1234|" + (System.currentTimeMillis() - 20 * 60 * 1000L),
                StandardCharsets.UTF_8);

        VectorExportPublisher.publishShapefile(tempDir.resolve("pozos.shp").toFile(),
                staging -> writeNewFamily(staging.toPath(), "pozos"));

        assertEquals("SHP-NUEVO", read("pozos.shp"));
        assertNoJournalOrLock("pozos");
    }

    @Test
    void invalidJournalFailsClosedWithoutTouchingData() throws Exception {
        createPreviousDataset("pozos");
        Files.writeString(tempDir.resolve(".pozos.catgis-publish.journal"),
                "NOT-A-JOURNAL\n", StandardCharsets.UTF_8);

        assertThrows(IOException.class,
                () -> VectorExportPublisher.recoverPendingPublish(tempDir, "pozos"));

        assertPreviousIntact("pozos");
        assertTrue(Files.exists(tempDir.resolve(".pozos.catgis-publish.journal")),
                "a corrupt journal must stay for manual inspection");
    }
}
