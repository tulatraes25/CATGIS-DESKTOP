package ar.com.catgis;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Atomic publication of vector exports (DC-1).
 *
 * <p>Exporting over an existing file used to delete the previous dataset before
 * writing the new one: any failure during the export left the user without the
 * source shapefile. Every export is now written to a staging location inside the
 * destination directory (same volume, so moves are cheap) and only published
 * once the writer finished. If anything fails, the previous dataset is restored
 * and the staging area is removed.</p>
 *
 * <p>Shapefiles are multi-file datasets, so publication backs up the existing
 * parts, moves the staged parts into place, and restores the backup on failure.</p>
 */
final class VectorExportPublisher {

    /** Sidecar extensions that belong to a shapefile dataset, in publish order. */
    static final List<String> SHAPEFILE_EXTENSIONS =
            List.of(".shp", ".shx", ".dbf", ".prj", ".fix", ".qix", ".cpg");

    private static final int MAX_STAGING_PREFIX_LENGTH = 40;

    @FunctionalInterface
    interface TargetWriter {
        void write(File target) throws Exception;
    }

    private VectorExportPublisher() {
    }

    /**
     * Writes a single-file export (dxf/geojson/kml/kmz/gpx) through staging and
     * replaces the target only after the writer succeeded.
     */
    static void publishSingleFile(File target, TargetWriter writer) throws Exception {
        Path targetPath = absoluteTarget(target);
        Path directory = targetPath.getParent();
        Files.createDirectories(directory);

        String fileName = targetPath.getFileName().toString();
        Path staging = Files.createTempFile(
                directory,
                stagingPrefix(baseNameOf(fileName)),
                extensionOf(fileName));
        try {
            writer.write(staging.toFile());
            moveReplacing(staging, targetPath);
        } finally {
            deleteQuietly(staging);
        }
    }

    /**
     * Writes a shapefile dataset through staging and publishes the whole set
     * atomically: on failure the previously published parts are restored.
     */
    static void publishShapefile(File target, TargetWriter writer) throws Exception {
        Path requested = absoluteTarget(target);
        Path directory = requested.getParent();
        Files.createDirectories(directory);

        String baseName = baseNameOf(requested.getFileName().toString());
        String shapefileName = baseName + ".shp";
        Path stagingDirectory = Files.createTempDirectory(directory, stagingPrefix(baseName));
        Path journal = journalPath(directory, baseName);

        // Heal any publish interrupted by a crash before starting a new one, then
        // remove old backups orphaned after a committed publish.
        recoverPendingPublish(directory, baseName);
        deleteOrphanBackups(directory, baseName);
        Path lock = acquirePublishLock(directory, baseName);
        List<Path> published = new ArrayList<>();
        Map<Path, Path> backups = new LinkedHashMap<>();
        boolean committed = false;
        movesSinceStart = 0;
        try {
            writer.write(stagingDirectory.resolve(shapefileName).toFile());

            List<Path> stagedParts = collectParts(stagingDirectory, baseName);
            requirePart(stagedParts, baseName, ".shp");
            requirePart(stagedParts, baseName, ".shx");

            backups = planBackups(directory, baseName);
            writeJournal(directory, baseName, stagingDirectory, backups, stagedParts);
            for (Map.Entry<Path, Path> entry : backups.entrySet()) {
                injectedMove(entry.getKey(), entry.getValue(), false);
            }
            for (Path staged : stagedParts) {
                Path destination = directory.resolve(staged.getFileName().toString());
                injectedMove(staged, destination, true);
                published.add(destination);
            }
            committed = true;
        } catch (Exception publicationFailure) {
            rollBack(published, backups, publicationFailure);
            deleteQuietly(journal);
            throw publicationFailure;
        } finally {
            deleteRecursively(stagingDirectory);
            if (committed) {
                // Journal deletion is the commit point: recovery must not restore
                // backups once the new family is completely published.
                deleteQuietly(journal);
                deleteAll(backups.values());
            }
            releasePublishLock(lock);
        }
    }

    /** Test seam: throw a crash after the Nth file move, leaving the journal. */
    private static volatile int crashAfterMoves = -1;

    static void crashAfterMovesForTesting(int moves) {
        crashAfterMoves = moves;
    }

    private static int movesSinceStart;

    /** Simulated process crash (not caught by the normal rollback path). */
    static final class SimulatedPublishCrash extends Error {
        SimulatedPublishCrash(int moves) {
            super("crash simulado de publicacion despues de " + moves + " movimientos");
        }
    }

    private static void injectedMove(Path source, Path destination, boolean replace)
            throws IOException {
        if (replace) {
            moveReplacing(source, destination);
        } else {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        movesSinceStart++;
        int limit = crashAfterMoves;
        if (limit >= 0 && movesSinceStart >= limit) {
            crashAfterMoves = -1;
            throw new SimulatedPublishCrash(movesSinceStart);
        }
    }

    private static void deleteOrphanBackups(Path directory, String baseName) throws IOException {
        String prefix = "." + baseName + ".catgis-old-";
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory)) {
            for (Path entry : entries) {
                if (entry.getFileName().toString().startsWith(prefix)) {
                    deleteQuietly(entry);
                }
            }
        }
    }

    private static Path journalPath(Path directory, String baseName) {
        return directory.resolve("." + baseName + ".catgis-publish.journal");
    }

    private static Path lockPath(Path directory, String baseName) {
        return directory.resolve("." + baseName + ".catgis-publish.lock");
    }

    private static Path acquirePublishLock(Path directory, String baseName) throws IOException {
        Path lock = lockPath(directory, baseName);
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                Files.writeString(lock,
                        ProcessHandle.current().pid() + "|" + System.currentTimeMillis(),
                        StandardCharsets.UTF_8,
                        java.nio.file.StandardOpenOption.CREATE_NEW,
                        java.nio.file.StandardOpenOption.WRITE);
                return lock;
            } catch (FileAlreadyExistsException busy) {
                if (isStaleLock(lock)) {
                    deleteQuietly(lock);
                    continue;
                }
                throw new IOException("La publicacion de la familia " + baseName
                        + " ya esta bloqueada por otro proceso.");
            }
        }
        throw new IOException("No se pudo bloquear la publicacion de " + baseName + ".");
    }

    private static boolean isStaleLock(Path lock) {
        try {
            String text = Files.readString(lock, StandardCharsets.UTF_8).trim();
            String[] parts = text.split("\\|", -1);
            if (parts.length < 2) {
                return true;
            }
            long timestamp = Long.parseLong(parts[1]);
            return System.currentTimeMillis() - timestamp > 15 * 60 * 1000L;
        } catch (Exception unreadable) {
            return true;
        }
    }

    private static void releasePublishLock(Path lock) {
        if (lock != null) {
            deleteQuietly(lock);
        }
    }

    private static Map<Path, Path> planBackups(Path directory, String baseName) throws IOException {
        Map<Path, Path> backups = new LinkedHashMap<>();
        String token = UUID.randomUUID().toString().replace("-", "");
        for (String extension : SHAPEFILE_EXTENSIONS) {
            Path existing = findIgnoringCase(directory, baseName + extension);
            if (existing == null) {
                continue;
            }
            Path backup = directory.resolve("." + baseName + ".catgis-old-" + token + extension);
            backups.put(existing, backup);
        }
        return backups;
    }

    private static void writeJournal(Path directory, String baseName, Path stagingDirectory,
                                     Map<Path, Path> backups, List<Path> stagedParts)
            throws IOException {
        StringBuilder text = new StringBuilder();
        text.append("CATGIS_VECTOR_PUBLISH|1\n");
        text.append("STAGING|").append(stagingDirectory.getFileName()).append('\n');
        for (Map.Entry<Path, Path> entry : backups.entrySet()) {
            text.append("BACKUP|").append(entry.getKey().getFileName())
                    .append('|').append(entry.getValue().getFileName()).append('\n');
        }
        for (Path staged : stagedParts) {
            text.append("PUBLISH|")
                    .append(directory.resolve(staged.getFileName().toString()).getFileName())
                    .append('\n');
        }
        Path journal = journalPath(directory, baseName);
        Path temp = directory.resolve("." + baseName + ".catgis-publish.journal.tmp");
        Files.writeString(temp, text.toString(), StandardCharsets.UTF_8);
        moveReplacing(temp, journal);
    }

    /**
     * Restores the previous family (or its absence) from an interrupted publish.
     * Never deletes a target that was not backed up unless it is a published part.
     */
    static void recoverPendingPublish(Path directory, String baseName) throws IOException {
        Path journal = journalPath(directory, baseName);
        if (!Files.exists(journal)) {
            return;
        }
        List<String> lines;
        try (BufferedReader reader = Files.newBufferedReader(journal, StandardCharsets.UTF_8)) {
            lines = reader.lines().toList();
        }
        if (lines.isEmpty() || !lines.get(0).trim().equals("CATGIS_VECTOR_PUBLISH|1")) {
            throw new IOException("Journal de publicacion invalido para " + baseName
                    + "; no se modifica el dataset.");
        }
        Map<String, String> backups = new LinkedHashMap<>();
        Set<String> publishTargets = new LinkedHashSet<>();
        String stagingName = null;
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).trim().split("\\|", -1);
            if (parts.length < 2) {
                continue;
            }
            switch (parts[0]) {
                case "STAGING" -> stagingName = parts[1];
                case "BACKUP" -> {
                    if (parts.length >= 3) {
                        backups.put(parts[1], parts[2]);
                    }
                }
                case "PUBLISH" -> publishTargets.add(parts[1]);
                default -> {
                    // Unknown future records are ignored; no filesystem action.
                }
            }
        }
        for (Map.Entry<String, String> entry : backups.entrySet()) {
            Path backup = directory.resolve(entry.getValue());
            Path target = directory.resolve(entry.getKey());
            if (Files.exists(backup)) {
                Files.move(backup, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        for (String publishTarget : publishTargets) {
            boolean hadBackup = backups.keySet().stream()
                    .anyMatch(target -> target.equalsIgnoreCase(publishTarget));
            if (!hadBackup) {
                deleteQuietly(directory.resolve(publishTarget));
            }
        }
        if (stagingName != null && stagingName.startsWith("." + baseName + "-")) {
            deleteRecursively(directory.resolve(stagingName));
        }
        deleteQuietly(journal);
        deleteQuietly(lockPath(directory, baseName));
    }

    private static Path absoluteTarget(File target) {
        if (target == null) {
            throw new IllegalArgumentException("El destino de exportacion no puede ser nulo.");
        }
        return target.getAbsoluteFile().toPath();
    }

    private static List<Path> collectParts(Path directory, String baseName) throws IOException {
        List<Path> parts = new ArrayList<>();
        for (String extension : SHAPEFILE_EXTENSIONS) {
            Path candidate = findIgnoringCase(directory, baseName + extension);
            if (candidate != null) {
                parts.add(candidate);
            }
        }
        return parts;
    }

    private static void requirePart(List<Path> parts, String baseName, String extension) throws IOException {
        boolean present = parts.stream()
                .anyMatch(part -> part.getFileName().toString().toLowerCase(java.util.Locale.ROOT)
                        .endsWith(extension));
        if (!present) {
            throw new IOException("La exportacion no genero el archivo requerido "
                    + baseName + extension + "; se conserva el dataset anterior.");
        }
    }

    private static Map<Path, Path> backUpExistingParts(Path directory, String baseName) throws IOException {
        Map<Path, Path> backups = new LinkedHashMap<>();
        String token = UUID.randomUUID().toString().replace("-", "");
        for (String extension : SHAPEFILE_EXTENSIONS) {
            Path existing = findIgnoringCase(directory, baseName + extension);
            if (existing == null) {
                continue;
            }
            Path backup = directory.resolve("." + baseName + ".catgis-old-" + token + extension);
            Files.move(existing, backup);
            backups.put(existing, backup);
        }
        return backups;
    }

    private static Path findIgnoringCase(Path directory, String fileName) throws IOException {
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory)) {
            for (Path entry : entries) {
                if (entry.getFileName().toString().equalsIgnoreCase(fileName)) {
                    return entry;
                }
            }
        }
        return null;
    }

    private static void rollBack(List<Path> published, Map<Path, Path> backups, Exception failure) {
        for (int index = published.size() - 1; index >= 0; index--) {
            deleteQuietly(published.get(index));
        }
        for (Map.Entry<Path, Path> entry : backups.entrySet()) {
            try {
                Files.move(entry.getValue(), entry.getKey(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException restoreFailure) {
                failure.addSuppressed(restoreFailure);
                CatgisLogger.warn("No se pudo restaurar " + entry.getKey(), restoreFailure);
            }
        }
    }

    private static void moveReplacing(Path staging, Path target) throws IOException {
        try {
            Files.move(staging, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(staging, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException atomicFailure) {
            // ATOMIC_MOVE with an existing destination may be rejected by some
            // filesystems; retry with an explicit replace before giving up.
            if (!Files.exists(staging)) {
                throw atomicFailure;
            }
            Files.move(staging, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String stagingPrefix(String baseName) {
        String safe = baseName == null || baseName.isBlank() ? "export" : baseName;
        if (safe.length() > MAX_STAGING_PREFIX_LENGTH) {
            safe = safe.substring(0, MAX_STAGING_PREFIX_LENGTH);
        }
        return "." + safe + "-";
    }

    private static String baseNameOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(dot) : "";
    }

    private static void deleteAll(Collection<Path> paths) {
        for (Path path : paths) {
            deleteQuietly(path);
        }
    }

    private static void deleteRecursively(Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            List<Path> ordered = paths.sorted(Comparator.reverseOrder()).toList();
            for (Path path : ordered) {
                deleteQuietly(path);
            }
        } catch (IOException failure) {
            CatgisLogger.warn("No se pudo limpiar el staging de exportacion " + directory, failure);
        }
    }

    private static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException failure) {
            CatgisLogger.warn("No se pudo eliminar " + path, failure);
        }
    }
}
