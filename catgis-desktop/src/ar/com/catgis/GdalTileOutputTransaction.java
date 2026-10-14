package ar.com.catgis;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fail-closed XYZ tile publication: GDAL never writes into the visible target.
 * This filesystem contract has no direct GDAL/OS-specific dependencies.
 */
final class GdalTileOutputTransaction {

    static final String OWNER_FILE = ".catgis-xyz-owner";
    private static final String OWNER_VALUE = "CATGIS_XYZ_TILES_V1\n";
    private static final Set<Path> ACTIVE_TARGETS = ConcurrentHashMap.newKeySet();
    private static final int MAX_ENTRIES = 2_000_000;
    private static final ThreadLocal<String> LAST_FAILURE = new ThreadLocal<>();

    static String lastFailureReason() {
        return LAST_FAILURE.get() == null ? "No transactional failure recorded"
                : LAST_FAILURE.get();
    }

    private static void failure(String message) {
        LAST_FAILURE.set(message.length() <= 400 ? message : message.substring(0, 400));
    }

    @FunctionalInterface
    interface TileProducer {
        boolean generate(Path outputPath) throws Exception;
    }

    @FunctionalInterface
    interface AtomicMover {
        void move(Path from, Path to) throws IOException;
    }

    private GdalTileOutputTransaction() {}

    static boolean execute(File input, File output, int zoomMin, int zoomMax,
                           TileProducer producer) {
        return execute(input, output, zoomMin, zoomMax, producer,
                (from, to) -> Files.move(from, to, StandardCopyOption.ATOMIC_MOVE));
    }

    // Deterministic test seam: inject failure in either of the atomic renames.
    static boolean execute(File input, File output, int zoomMin, int zoomMax,
                           TileProducer producer, AtomicMover mover) {
        LAST_FAILURE.remove();
        if (SwingUtilities.isEventDispatchThread() || input == null || output == null
                || producer == null || mover == null || zoomMin < 0
                || (zoomMax > 0 && zoomMax < zoomMin)) {
            failure("Invalid input/output, zoom or Swing EDT invocation.");
            return false;
        }
        Path workspace = null;
        Path targetKey = null;
        boolean locked = false;
        boolean preserveBackup = false;
        try {
            Path inputReal = input.toPath().toRealPath();
            if (!Files.isRegularFile(inputReal, LinkOption.NOFOLLOW_LINKS)) {
                return false;
            }
            Path requestedTarget = output.toPath().toAbsolutePath().normalize();
            Path requestedParent = requestedTarget.getParent();
            if (requestedTarget.getFileName() == null || requestedParent == null
                    || !Files.isDirectory(requestedParent, LinkOption.NOFOLLOW_LINKS)
                    || Files.isSymbolicLink(requestedTarget)
                    || Files.isSymbolicLink(requestedParent)) {
                return false;
            }
            // Canonicalize macOS /var -> /private/var and other ancestor aliases.
            Path physicalParent = requestedParent.toRealPath();
            targetKey = physicalParent.resolve(requestedTarget.getFileName()).normalize();
            if (inputReal.startsWith(targetKey)) {
                return false;
            }
            if (Files.exists(targetKey, LinkOption.NOFOLLOW_LINKS)) {
                if (!Files.isDirectory(targetKey, LinkOption.NOFOLLOW_LINKS)
                        || !isOwnedCompleteDirectory(targetKey)) {
                    return false;
                }
            }
            if (!ACTIVE_TARGETS.add(targetKey)) {
                return false;
            }
            locked = true;

            // GDAL receives a nonexistent output path under an owned sibling,
            // preserving compatibility with modern tools requiring a new path.
            workspace = Files.createTempDirectory(physicalParent, ".catgis-xyz-stage-");
            Path stagedTiles = workspace.resolve("tiles");
            if (!producer.generate(stagedTiles)) {
                failure("GDAL tile command failed or timed out before complete staging.");
                return false;
            }
            if (!isValidTiles(stagedTiles, zoomMin, zoomMax)) {
                return false;
            }
            Files.writeString(stagedTiles.resolve(OWNER_FILE), OWNER_VALUE);
            // Recheck destination immediately before publication; never replace
            // an unrelated directory someone introduced during generation.
            if (Files.exists(targetKey, LinkOption.NOFOLLOW_LINKS)
                    && !isOwnedCompleteDirectory(targetKey)) {
                return false;
            }

            Path backup = workspace.resolve("previous");
            if (Files.exists(targetKey, LinkOption.NOFOLLOW_LINKS)) {
                mover.move(targetKey, backup);
                try {
                    mover.move(stagedTiles, targetKey);
                } catch (IOException publishFailure) {
                    try {
                        mover.move(backup, targetKey);
                    } catch (IOException restoreFailure) {
                        preserveBackup = true;
                        publishFailure.addSuppressed(restoreFailure);
                    }
                    throw publishFailure;
                }
            } else {
                mover.move(stagedTiles, targetKey);
            }
            LAST_FAILURE.remove();
            return true;
        } catch (Exception error) {
            failure("GDAL tile transaction: " + error.getClass().getSimpleName()
                    + ": " + error.getMessage());
            if (error instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            CatgisLogger.warn("GDAL XYZ transactional publication failed", error);
            return false;
        } finally {
            if (workspace != null) {
                Path backup = workspace.resolve("previous");
                if (preserveBackup && Files.exists(backup, LinkOption.NOFOLLOW_LINKS)) {
                    // Failed rollback: retain the sole surviving old dataset.
                    CatgisLogger.warn("GDAL XYZ old tiles preserved for recovery at " + backup,
                            new IOException("Atomic rollback could not complete"));
                } else {
                    try {
                        deleteTreeNoFollow(workspace);
                    } catch (IOException cleanupError) {
                        CatgisLogger.warn("GDAL XYZ staging cleanup failed", cleanupError);
                    }
                }
            }
            if (locked) {
                ACTIVE_TARGETS.remove(targetKey);
            }
        }
    }

    private static boolean isOwnedCompleteDirectory(Path target) throws IOException {
        Path marker = target.resolve(OWNER_FILE);
        if (!Files.isRegularFile(marker, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(marker)
                || Files.size(marker) != OWNER_VALUE.length()
                || !OWNER_VALUE.equals(Files.readString(marker))) {
            return false;
        }
        return isValidTiles(target, 0, -1);
    }

    static boolean isValidTiles(Path directory, int minZoom, int maxZoom) throws IOException {
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(directory)) {
            failure("GDAL output path missing, non-directory or symlink: " + directory);
            return false;
        }
        AtomicReference<Path> firstPng = new AtomicReference<>();
        int[] count = {0};
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    if (Files.isSymbolicLink(dir) || ++count[0] > MAX_ENTRIES) {
                        throw new IOException("Invalid XYZ directory or entry budget");
                    }
                    Path rel = directory.relativize(dir);
                    if (!dir.equals(directory) && (rel.getNameCount() > 2
                            || !numericIndex(rel.getFileName().toString()))) {
                        throw new IOException("Unexpected XYZ directory: " + rel);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    if (attrs.isSymbolicLink() || !attrs.isRegularFile()
                            || ++count[0] > MAX_ENTRIES) {
                        throw new IOException("Invalid XYZ file entry");
                    }
                    Path rel = directory.relativize(file);
                    if (rel.getNameCount() == 1) {
                        String rootFile = rel.getFileName().toString();
                        if (!rootFile.equals(OWNER_FILE)
                                && !Set.of("tilemapresource.xml", "openlayers.html",
                                        "leaflet.html", "googlemaps.html", "index.html",
                                        "metadata.json", "tilejson.json", "mapml.mapml").contains(rootFile)) {
                            throw new IOException("Unexpected XYZ root file: " + rootFile);
                        }
                        if (Files.size(file) > 4L * 1024 * 1024) {
                            throw new IOException("Oversized XYZ metadata file");
                        }
                        return FileVisitResult.CONTINUE;
                    }
                    if (rel.getNameCount() != 3) {
                        throw new IOException("Unexpected XYZ file depth: " + rel);
                    }
                    int z = parseIndex(rel.getName(0).toString());
                    if (z > 30) {
                        throw new IOException("XYZ zoom exceeds maximum supported level");
                    }
                    long limit = 1L << z;
                    int x = parseIndex(rel.getName(1).toString());
                    String yName = rel.getName(2).toString();
                    if (!yName.endsWith(".png")) {
                        throw new IOException("XYZ tile must be PNG: " + rel);
                    }
                    int y = parseIndex(yName.substring(0, yName.length() - 4));
                    if (x >= limit || y >= limit
                            || (maxZoom > 0 && (z < minZoom || z > maxZoom))) {
                        throw new IOException("XYZ coordinate/zoom outside range: " + rel);
                    }
                    if (firstPng.get() == null) {
                        firstPng.set(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException invalid) {
            failure("GDAL tile tree invalid: " + invalid.getMessage());
            return false;
        }
        if (firstPng.get() == null) {
            failure("GDAL generated no XYZ PNG tile files.");
            return false;
        }
        BufferedImage image = ImageIO.read(firstPng.get().toFile());
        if (image == null || image.getWidth() != 256 || image.getHeight() != 256) {
            failure("GDAL tile is not a readable 256x256 PNG: " + firstPng.get());
            return false;
        }
        return true;
    }

    private static int parseIndex(String raw) throws IOException {
        if (!numericIndex(raw)) {
            throw new IOException("Invalid XYZ numeric index: " + raw);
        }
        try {
            int value = Integer.parseInt(raw);
            if (value < 0 || value > (1 << 30)) {
                throw new IOException("XYZ coordinate outside range");
            }
            return value;
        } catch (NumberFormatException error) {
            throw new IOException("Oversized XYZ index", error);
        }
    }

    private static boolean numericIndex(String raw) {
        if (raw.isEmpty()) {
            return false;
        }
        for (int i = 0; i < raw.length(); i++) {
            if (raw.charAt(i) < '0' || raw.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }

    private static void deleteTreeNoFollow(Path root) throws IOException {
        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException problem)
                    throws IOException {
                if (problem != null) {
                    throw problem;
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
