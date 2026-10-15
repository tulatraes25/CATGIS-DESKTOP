package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Real filesystem integrity contracts for CATMAP layout persistence. */
class CatmapAtomicSaveContractTest {

    @TempDir
    Path dir;

    private static LayoutModel model(String title) {
        LayoutModel layout = new LayoutModel();
        layout.addElement(new LayoutLabel("title", title, 12, 16, 190, 22));
        return layout;
    }

    private void assertNoOwnedStagingFiles() throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString().endsWith(".part")),
                    "Owned temporary layout/backup files must be deleted after every operation.");
        }
    }

    private static void atomicMove(Path staged, Path destination) throws IOException {
        Files.move(staged, destination, StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    void failedFinalAtomicRenamePreservesPreviousBytesAndBackup() throws Exception {
        Path output = dir.resolve("mapa final con espacios.catmap");
        CatmapSerializer.save(model("original"), output.toFile());
        byte[] previous = Files.readAllBytes(output);

        IOException error = assertThrows(IOException.class, () ->
                CatmapSerializer.save(model("new unfinished version"), output.toFile(),
                        (staged, destination) -> {
                            if (destination.equals(output)) {
                                throw new AtomicMoveNotSupportedException(
                                        staged.toString(), destination.toString(),
                                        "simulated unsupported atomic rename");
                            }
                            atomicMove(staged, destination);
                        }));

        assertTrue(error.getMessage().contains("atomica"));
        assertArrayEquals(previous, Files.readAllBytes(output),
                "A failed new publication must not rename away the last good layout.");
        assertArrayEquals(previous, Files.readAllBytes(dir.resolve("mapa final con espacios.catmap.bak")));
        assertEquals(1, CatmapSerializer.load(output.toFile()).getElements().size());
        assertNoOwnedStagingFiles();
    }

    @Test
    void failedBackupPublicationLeavesDestinationAndExistingBackupUntouched() throws Exception {
        Path output = dir.resolve("backup-safety.catmap");
        CatmapSerializer.save(model("old"), output.toFile());
        byte[] old = Files.readAllBytes(output);
        CatmapSerializer.save(model("current"), output.toFile());
        byte[] current = Files.readAllBytes(output);
        Path backup = dir.resolve("backup-safety.catmap.bak");
        assertArrayEquals(old, Files.readAllBytes(backup));

        IOException error = assertThrows(IOException.class, () ->
                CatmapSerializer.save(model("pending"), output.toFile(),
                        (staged, destination) -> {
                            if (destination.equals(backup)) {
                                throw new AtomicMoveNotSupportedException(
                                        staged.toString(), destination.toString(),
                                        "simulated unsupported backup rename");
                            }
                            atomicMove(staged, destination);
                        }));

        assertTrue(error.getMessage().contains("atomica"));
        assertArrayEquals(current, Files.readAllBytes(output));
        assertArrayEquals(old, Files.readAllBytes(backup));
        assertNoOwnedStagingFiles();
    }

    @Test
    void regularDoubleSavePreservesPriorVersionAndDoesNotClobberLegacyTemp() throws Exception {
        Path output = dir.resolve("atlas profesional.catmap");
        Path legacyTemp = dir.resolve("atlas profesional.catmap.tmp");
        Files.writeString(legacyTemp, "recoverable old stage", StandardCharsets.UTF_8);
        CatmapSerializer.save(model("A"), output.toFile());
        byte[] first = Files.readAllBytes(output);
        CatmapSerializer.save(model("B"), output.toFile());

        assertArrayEquals(first, Files.readAllBytes(dir.resolve("atlas profesional.catmap.bak")));
        assertEquals("recoverable old stage", Files.readString(legacyTemp, StandardCharsets.UTF_8));
        assertEquals(1, CatmapSerializer.load(output.toFile()).getElements().size());
        assertNoOwnedStagingFiles();
    }

    @Test
    void directoryOutputIsRejectedWithoutCreatingStaging() throws Exception {
        Path destination = Files.createDirectory(dir.resolve("directory.catmap"));
        IOException blocked = assertThrows(IOException.class,
                () -> CatmapSerializer.save(model("not written"), destination.toFile()));
        assertTrue(blocked.getMessage().contains("no regular"));
        assertTrue(Files.isDirectory(destination));
        assertNoOwnedStagingFiles();
    }

    @Test
    void symbolicLinkTargetAndBackupAreRejectedWithoutTouchingTheirVictims() throws Exception {
        Path original = dir.resolve("valuable-layout.catmap");
        CatmapSerializer.save(model("sensitive data"), original.toFile());
        byte[] originalBytes = Files.readAllBytes(original);
        Path link = dir.resolve("linked.catmap");
        try {
            Files.createSymbolicLink(link, original.getFileName());
        } catch (IOException | UnsupportedOperationException | SecurityException unavailable) {
            // Windows runners without symlink privileges cannot run this one
            // subcase. The mandatory directory/atomic-failure tests still run.
            return;
        }
        assertThrows(IOException.class, () -> CatmapSerializer.save(model("bad"), link.toFile()));
        assertArrayEquals(originalBytes, Files.readAllBytes(original));
        assertTrue(Files.isSymbolicLink(link));

        Path output = dir.resolve("normal.catmap");
        CatmapSerializer.save(model("normal"), output.toFile());
        byte[] outputBytes = Files.readAllBytes(output);
        Path backupLink = dir.resolve("normal.catmap.bak");
        Files.createSymbolicLink(backupLink, original.getFileName());
        assertThrows(IOException.class, () -> CatmapSerializer.save(model("bad"), output.toFile()));
        assertArrayEquals(outputBytes, Files.readAllBytes(output));
        assertArrayEquals(originalBytes, Files.readAllBytes(original));
        assertFalse(Files.isSymbolicLink(output));
        assertNoOwnedStagingFiles();
    }
}
