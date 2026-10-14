package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Small real PNG fixtures + actual kernel-locked child JVM termination.
 * No external GDAL binary or user workstation path is needed.
 */
class GdalTileCrashJournalContractTest {

    @TempDir
    Path temp;

    private Path target() {
        return temp.resolve("XYZ with spaces");
    }

    private static void writeOwnedTile(Path root, Color color) throws IOException {
        Path folder = root.resolve("2").resolve("1");
        Files.createDirectories(folder);
        BufferedImage tile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        tile.setRGB(23, 42, color.getRGB());
        assertTrue(ImageIO.write(tile, "png", folder.resolve("0.png").toFile()));
        Files.writeString(root.resolve(GdalTileOutputTransaction.OWNER_FILE),
                GdalTileOutputTransaction.OWNER_VALUE);
    }

    private static int colorOf(Path root) throws IOException {
        return ImageIO.read(root.resolve("2/1/0.png").toFile()).getRGB(23, 42);
    }

    private Path preparedStage(Path target) throws Exception {
        Path stage = Files.createTempDirectory(temp, ".catgis-xyz-stage-");
        writeOwnedTile(stage.resolve("tiles"), Color.BLUE);
        GdalTileCrashJournal.prepare(target, stage);
        assertTrue(Files.isRegularFile(GdalTileCrashJournal.journalPath(target)));
        return stage;
    }

    private void assertCleaned(Path target, Path stage) throws IOException {
        assertFalse(Files.exists(GdalTileCrashJournal.journalPath(target),
                LinkOption.NOFOLLOW_LINKS));
        assertFalse(Files.exists(stage, LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    void interruptedBeforeOldMovePreservesExistingTilesAndCleansPrivateStage() throws Exception {
        Path target = target();
        writeOwnedTile(target, Color.RED);
        byte[] old = Files.readAllBytes(target.resolve("2/1/0.png"));
        Path stage = preparedStage(target);
        assertTrue(GdalTileCrashJournal.recover(target));
        assertArrayEquals(old, Files.readAllBytes(target.resolve("2/1/0.png")));
        assertCleaned(target, stage);
    }

    @Test
    void interruptedAfterOldMoveRestoresOwnedBackupExactly() throws Exception {
        Path target = target();
        writeOwnedTile(target, Color.RED);
        byte[] old = Files.readAllBytes(target.resolve("2/1/0.png"));
        Path stage = preparedStage(target);
        Files.move(target, stage.resolve("previous"), StandardCopyOption.ATOMIC_MOVE);
        assertFalse(Files.exists(target));
        assertTrue(GdalTileCrashJournal.recover(target));
        assertArrayEquals(old, Files.readAllBytes(target.resolve("2/1/0.png")));
        assertCleaned(target, stage);
    }

    @Test
    void interruptedAfterNewPublicationKeepsNewAndRemovesOldBackup() throws Exception {
        Path target = target();
        writeOwnedTile(target, Color.RED);
        Path stage = preparedStage(target);
        Files.move(target, stage.resolve("previous"), StandardCopyOption.ATOMIC_MOVE);
        Files.move(stage.resolve("tiles"), target, StandardCopyOption.ATOMIC_MOVE);
        assertTrue(GdalTileCrashJournal.recover(target));
        assertEquals(Color.BLUE.getRGB(), colorOf(target));
        assertCleaned(target, stage);
    }

    @Test
    void interruptedFirstPublicationKeepsPublishedNewTiles() throws Exception {
        Path target = target();
        Path stage = preparedStage(target);
        Files.move(stage.resolve("tiles"), target, StandardCopyOption.ATOMIC_MOVE);
        assertTrue(GdalTileCrashJournal.recover(target));
        assertEquals(Color.BLUE.getRGB(), colorOf(target));
        assertCleaned(target, stage);
    }

    @Test
    void noPriorOutputAndUnpublishedStagePreservesAmbiguousCandidate() throws Exception {
        Path target = target();
        Path stage = preparedStage(target);
        assertFalse(GdalTileCrashJournal.recover(target),
                "No old output: do not silently discard an unpublished raster.");
        assertTrue(Files.isDirectory(stage.resolve("tiles")));
        assertTrue(Files.exists(GdalTileCrashJournal.journalPath(target)));
        assertFalse(Files.exists(target));
    }

    @Test
    void nonOwnedReplacementBlocksRecoveryWithoutRemovingPriorData() throws Exception {
        Path target = target();
        writeOwnedTile(target, Color.RED);
        Path stage = preparedStage(target);
        Files.move(target, stage.resolve("previous"), StandardCopyOption.ATOMIC_MOVE);
        Files.createDirectories(target);
        Files.writeString(target.resolve("user-data.txt"), "DO NOT DELETE");
        assertFalse(GdalTileCrashJournal.recover(target));
        assertEquals("DO NOT DELETE", Files.readString(target.resolve("user-data.txt")));
        assertTrue(Files.isRegularFile(stage.resolve("previous/2/1/0.png")));
        assertTrue(Files.exists(GdalTileCrashJournal.journalPath(target)));
    }

    @Test
    void tamperedWorkspaceMarkerBlocksRecoveryWithoutDeletingTiles() throws Exception {
        Path target = target();
        writeOwnedTile(target, Color.RED);
        Path stage = preparedStage(target);
        Path owner = stage.resolve(".catgis-xyz-workspace-owner");
        Files.writeString(owner, "SPOOFED");
        assertThrows(IOException.class, () -> GdalTileCrashJournal.recover(target));
        assertEquals(Color.RED.getRGB(), colorOf(target));
        assertTrue(Files.exists(stage.resolve("tiles/2/1/0.png")));
        assertTrue(Files.exists(GdalTileCrashJournal.journalPath(target)));
    }

    @Test
    void unrecognizedOrPathTraversalJournalRefusesRecovery() throws Exception {
        Path target = target();
        writeOwnedTile(target, Color.RED);
        Path stage = preparedStage(target);
        Path journal = GdalTileCrashJournal.journalPath(target);
        Files.writeString(journal, "CATGIS_XYZ_JOURNAL_V1\n../escape\nfake.lck\n");
        assertThrows(IOException.class, () -> GdalTileCrashJournal.recover(target));
        assertTrue(Files.exists(stage.resolve("tiles/2/1/0.png")));
        assertEquals(Color.RED.getRGB(), colorOf(target));
    }

    @Test
    void journalSymlinkCannotRedirectRecoveryOutsideWorkspaceWhenSupported()
            throws Exception {
        Path target = target();
        writeOwnedTile(target, Color.RED);
        Path arbitrary = temp.resolve("private-user-data.txt");
        Files.writeString(arbitrary, "KEEP");
        Path journal = GdalTileCrashJournal.journalPath(target);
        try {
            Files.createSymbolicLink(journal, arbitrary);
        } catch (IOException | UnsupportedOperationException unavailable) {
            return; // Windows test runner may lack symlink privileges.
        }
        assertThrows(IOException.class, () -> GdalTileCrashJournal.recover(target));
        assertEquals("KEEP", Files.readString(arbitrary));
        assertEquals(Color.RED.getRGB(), colorOf(target));
    }

    @Test
    void actualChildJvmHaltAfterBackupIsRecoveredByNextProcess() throws Exception {
        Path target = target();
        writeOwnedTile(target, Color.RED);
        byte[] old = Files.readAllBytes(target.resolve("2/1/0.png"));
        Path stage = preparedStage(target);

        Path testClasses = Path.of(CrashProbe.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
        Path mainClasses = Path.of(GdalTileCrashJournal.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
        String cp = testClasses + File.pathSeparator + mainClasses;
        Path java = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name", "").toLowerCase().contains("win")
                        ? "java.exe" : "java");

        Process child = new ProcessBuilder(java.toString(), "-cp", cp,
                CrashProbe.class.getName(), target.toString(), stage.toString())
                .redirectErrorStream(true).start();
        try {
            assertTrue(child.waitFor(15L, TimeUnit.SECONDS),
                    "The deliberately terminated child JVM must exit.");
            assertEquals(91, child.exitValue());
        } finally {
            if (child.isAlive()) {
                child.destroyForcibly();
                child.waitFor(5L, TimeUnit.SECONDS);
            }
        }
        assertFalse(Files.exists(target));
        try (GdalTileTargetLock lease = GdalTileTargetLock.tryAcquire(target)) {
            assertNotNull(lease, "Kernel lease must be released after Runtime.halt().");
            assertTrue(GdalTileCrashJournal.recover(target));
        }
        assertArrayEquals(old, Files.readAllBytes(target.resolve("2/1/0.png")));
        assertCleaned(target, stage);
    }

    /** Dedicated JVM stage: simulate hard shutdown between atomic renames. */
    public static final class CrashProbe {
        private CrashProbe() {}

        public static void main(String[] args) throws Exception {
            Path target = Path.of(args[0]);
            Path stage = Path.of(args[1]);
            try (GdalTileTargetLock lease = GdalTileTargetLock.tryAcquire(target)) {
                if (lease == null) {
                    System.exit(8);
                }
                Files.move(target, stage.resolve("previous"),
                        StandardCopyOption.ATOMIC_MOVE);
                Runtime.getRuntime().halt(91);
            }
        }
    }
}
