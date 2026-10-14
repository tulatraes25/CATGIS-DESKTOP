package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic filesystem/rollback safety independent of native GDAL.
 * The real GDAL CLI tests exercise the public path on actual Linux/Mac hosts.
 */
class GdalTileOutputTransactionContractTest {

    @TempDir
    Path temporary;

    private Path source() throws IOException {
        Path file = temporary.resolve("source raster.tif");
        Files.writeString(file, "synthetic-command-input");
        return file;
    }

    private static boolean writeTile(Path tiles, Color color) throws IOException {
        Path directory = tiles.resolve("2").resolve("1");
        Files.createDirectories(directory);
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(21, 42, color.getRGB());
        ImageIO.write(image, "png", directory.resolve("0.png").toFile());
        return true;
    }

    private void assertNoStagingLeft() throws IOException {
        try (Stream<Path> children = Files.list(temporary)) {
            assertEquals(0, children.filter(p -> p.getFileName().toString()
                    .startsWith(".catgis-xyz-stage-")).count(),
                    "Every normal completion/failure must remove CATGIS-owned staging.");
        }
    }

    @Test
    void newOutputIsPublishedOnlyAfterValidatingRealPngAndOwnership() throws Exception {
        Path src = source();
        Path target = temporary.resolve("XYZ result with spaces");
        assertTrue(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> writeTile(staged, Color.RED)));
        assertTrue(Files.isRegularFile(target.resolve("2/1/0.png")));
        assertEquals("CATGIS_XYZ_TILES_V1\n",
                Files.readString(target.resolve(GdalTileOutputTransaction.OWNER_FILE)));
        assertEquals(Color.RED.getRGB(),
                ImageIO.read(target.resolve("2/1/0.png").toFile()).getRGB(21, 42));
        assertNoStagingLeft();
    }

    @Test
    void ownedExistingOutputIsReplacedWithoutMixingOldAndNewTiles() throws Exception {
        Path src = source();
        Path target = temporary.resolve("tiles");
        assertTrue(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> writeTile(staged, Color.RED)));
        byte[] oldBytes = Files.readAllBytes(target.resolve("2/1/0.png"));
        assertTrue(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> writeTile(staged, Color.BLUE)));
        assertEquals(Color.BLUE.getRGB(),
                ImageIO.read(target.resolve("2/1/0.png").toFile()).getRGB(21, 42));
        assertFalse(java.util.Arrays.equals(oldBytes, Files.readAllBytes(target.resolve("2/1/0.png"))));
        assertNoStagingLeft();
    }

    @Test
    void childFailureOrTimeoutCannotDamageExistingOutput() throws Exception {
        Path src = source();
        Path target = temporary.resolve("tiles");
        assertTrue(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> writeTile(staged, Color.RED)));
        byte[] old = Files.readAllBytes(target.resolve("2/1/0.png"));
        assertFalse(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> {
                    writeTile(staged, Color.BLUE);
                    return false; // Simulate GDAL process nonzero exit or deadline.
                }));
        assertArrayEquals(old, Files.readAllBytes(target.resolve("2/1/0.png")));
        assertNoStagingLeft();
    }

    @Test
    void failingSecondAtomicRenameRestoresPreviousOutput() throws Exception {
        Path src = source();
        Path target = temporary.resolve("tiles");
        assertTrue(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> writeTile(staged, Color.RED)));
        byte[] old = Files.readAllBytes(target.resolve("2/1/0.png"));
        AtomicBoolean previousWasMoved = new AtomicBoolean(false);
        assertFalse(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> writeTile(staged, Color.BLUE), (from, to) -> {
                    if (to.getFileName().toString().equals("previous")) {
                        previousWasMoved.set(true);
                    }
                    if (from.getFileName().toString().equals("tiles")
                            && from.getParent().getFileName().toString()
                                    .startsWith(".catgis-xyz-stage-")) {
                        throw new IOException("Injected SECOND rename failure");
                    }
                    Files.move(from, to, StandardCopyOption.ATOMIC_MOVE);
                }));
        assertTrue(previousWasMoved.get(), "The old output must actually enter backup.");
        assertArrayEquals(old, Files.readAllBytes(target.resolve("2/1/0.png")));
        assertNoStagingLeft();
    }

    @Test
    void unsupportedAtomicMovesFailClosedAndPreserveOriginal() throws Exception {
        Path src = source();
        Path target = temporary.resolve("tiles");
        assertTrue(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> writeTile(staged, Color.RED)));
        byte[] old = Files.readAllBytes(target.resolve("2/1/0.png"));
        assertFalse(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> writeTile(staged, Color.BLUE), (from, to) -> {
                    throw new AtomicMoveNotSupportedException(
                            from.toString(), to.toString(), "simulated FS");
                }));
        assertArrayEquals(old, Files.readAllBytes(target.resolve("2/1/0.png")));
        assertNoStagingLeft();
    }

    @Test
    void invalidOrEmptyOutputIsNeverPublished() throws Exception {
        Path src = source();
        Path target = temporary.resolve("tiles");
        assertFalse(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> {
                    Files.createDirectories(staged);
                    return true;
                }));
        assertFalse(Files.exists(target));
        assertFalse(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> {
                    Files.createDirectories(staged.resolve("2/1"));
                    Files.writeString(staged.resolve("2/1/0.png"), "not-a-png");
                    return true;
                }));
        assertFalse(Files.exists(target));
        assertFalse(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> {
                    writeTile(staged.resolve("99"), Color.RED);
                    return true;
                }));
        assertFalse(Files.exists(target));
        assertNoStagingLeft();
    }

    @Test
    void neverReplaceUnownedDirectoriesOrFiles() throws Exception {
        Path src = source();
        Path target = temporary.resolve("user map tiles");
        Files.createDirectories(target);
        Path userFile = target.resolve("keep-important.txt");
        Files.writeString(userFile, "USER DATA");
        assertFalse(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> writeTile(staged, Color.RED)));
        assertEquals("USER DATA", Files.readString(userFile));
        Path fileTarget = temporary.resolve("other-file");
        Files.writeString(fileTarget, "DO NOT ERASE");
        assertFalse(GdalTileOutputTransaction.execute(src.toFile(), fileTarget.toFile(),
                2, 2, staged -> writeTile(staged, Color.RED)));
        assertEquals("DO NOT ERASE", Files.readString(fileTarget));
        assertNoStagingLeft();
    }

    @Test
    void symlinkTargetAndSymlinkedParentAreRejectedIfSupported() throws Exception {
        Path src = source();
        Path existing = temporary.resolve("real-target");
        Files.createDirectories(existing);
        Path link = temporary.resolve("link-tiles");
        try {
            Files.createSymbolicLink(link, existing);
        } catch (UnsupportedOperationException | IOException unavailable) {
            // Windows runners may lack symlink privileges. Linux/Mac execute
            // the assertion; no extra silent JUnit skip is introduced.
            return;
        }
        assertFalse(GdalTileOutputTransaction.execute(src.toFile(), link.toFile(),
                2, 2, staged -> writeTile(staged, Color.RED)));
        assertFalse(GdalTileOutputTransaction.execute(src.toFile(),
                link.resolve("nested").toFile(),
                2, 2, staged -> writeTile(staged, Color.RED)));
        assertNoStagingLeft();
    }

    @Test
    void twoConcurrentWritersCannotInterfereOrUnlockEachOther() throws Exception {
        Path src = source();
        Path target = temporary.resolve("same destination");
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService workers = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> first = workers.submit(() ->
                    GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                            2, 2, staged -> {
                                entered.countDown();
                                if (!release.await(10, TimeUnit.SECONDS)) {
                                    return false;
                                }
                                return writeTile(staged, Color.RED);
                            }));
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            assertFalse(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                    2, 2, staged -> writeTile(staged, Color.BLUE)),
                    "A second writer must not steal the first writer's active lock.");
            assertFalse(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                    2, 2, staged -> writeTile(staged, Color.BLUE)),
                    "Rejected attempts must not release another writer's lock.");
            release.countDown();
            assertTrue(first.get(10, TimeUnit.SECONDS));
            assertEquals(Color.RED.getRGB(),
                    ImageIO.read(target.resolve("2/1/0.png").toFile()).getRGB(21, 42));
            assertNoStagingLeft();
        } finally {
            release.countDown();
            workers.shutdownNow();
        }
    }

    @Test
    void failedRollbackPreservesRecoveryBackupInsteadOfDeletingUserData() throws Exception {
        Path src = source();
        Path target = temporary.resolve("tiles");
        assertTrue(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> writeTile(staged, Color.RED)));
        byte[] old = Files.readAllBytes(target.resolve("2/1/0.png"));
        AtomicBoolean previousWasMoved = new AtomicBoolean(false);

        assertFalse(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, staged -> writeTile(staged, Color.BLUE), (from, to) -> {
                    String sourceName = from.getFileName().toString();
                    if (to.getFileName().toString().equals("previous")) {
                        previousWasMoved.set(true);
                    }
                    if (sourceName.equals("previous")
                            || (sourceName.equals("tiles")
                                    && from.getParent().getFileName().toString()
                                            .startsWith(".catgis-xyz-stage-"))) {
                        throw new IOException("Simulated publish AND rollback failure");
                    }
                    Files.move(from, to, StandardCopyOption.ATOMIC_MOVE);
                }));

        assertTrue(previousWasMoved.get(), "The original folder must reach backup.");
        assertFalse(Files.exists(target),
                "An extreme failure can temporarily remove the final pathname.");
        try (Stream<Path> roots = Files.list(temporary)) {
            Path retained = roots.filter(p -> p.getFileName().toString()
                    .startsWith(".catgis-xyz-stage-")).findFirst().orElseThrow();
            assertArrayEquals(old, Files.readAllBytes(
                    retained.resolve("previous/2/1/0.png")),
                    "Never delete the only remaining previous tiles after failed recovery.");
        }
    }

    @Test
    void oversizedPngDimensionsAreRejectedBeforePixelDecode() throws Exception {
        Path src = source();
        Path target = temporary.resolve("image bomb guard");
        assertFalse(GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                2, 2, stage -> {
                    Path zoom = stage.resolve("2/1");
                    Files.createDirectories(zoom);
                    BufferedImage nonTile = new BufferedImage(
                            1024, 1024, BufferedImage.TYPE_INT_ARGB);
                    ImageIO.write(nonTile, "png", zoom.resolve("0.png").toFile());
                    return true;
                }));
        assertFalse(Files.exists(target));
        assertNoStagingLeft();
    }

    @Test
    void knownGdalMapMlMetadataIsAllowedButUnknownRootFilesStayBlocked() throws Exception {
        Path src = source();
        Path published = temporary.resolve("XYZ MapML output");
        assertTrue(GdalTileOutputTransaction.execute(src.toFile(), published.toFile(),
                2, 2, staged -> {
                    writeTile(staged, Color.RED);
                    Files.writeString(staged.resolve("mapml.mapml"), "<mapml/>");
                    return true;
                }));
        assertTrue(Files.isRegularFile(published.resolve("mapml.mapml")));
        // Existing CATGIS-owned output with this legitimate GDAL metadata
        // must be eligible for later replacement without deleting user data.
        assertTrue(GdalTileOutputTransaction.execute(src.toFile(), published.toFile(),
                2, 2, staged -> writeTile(staged, Color.BLUE)));
        Path suspicious = temporary.resolve("unknown-metadata");
        assertFalse(GdalTileOutputTransaction.execute(src.toFile(), suspicious.toFile(),
                2, 2, staged -> {
                    writeTile(staged, Color.RED);
                    Files.writeString(staged.resolve("unknown-file.js"), "unexpected");
                    return true;
                }));
        assertFalse(Files.exists(suspicious));
        assertNoStagingLeft();
    }

    @Test
    void neverBlockSwingEventDispatchThreadOrWriteToAncestorOfInput() throws Exception {
        Path src = source();
        Path target = temporary.resolve("tiles");
        AtomicBoolean attempted = new AtomicBoolean(true);
        SwingUtilities.invokeAndWait(() -> attempted.set(
                GdalTileOutputTransaction.execute(src.toFile(), target.toFile(),
                        2, 2, staged -> writeTile(staged, Color.RED))));
        assertFalse(attempted.get());
        assertFalse(Files.exists(target));
        // Output directory encompasses the source raster: publishing would
        // otherwise rename or destroy the active source file.
        assertFalse(GdalTileOutputTransaction.execute(src.toFile(), temporary.toFile(),
                2, 2, staged -> writeTile(staged, Color.RED)));
    }
}
