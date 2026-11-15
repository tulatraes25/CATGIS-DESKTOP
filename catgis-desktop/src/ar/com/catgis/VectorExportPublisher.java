package ar.com.catgis;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
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
        Path targetPath = directory.resolve(shapefileName);
        Path stagingDirectory = Files.createTempDirectory(directory, stagingPrefix(baseName));
        try {
            writer.write(stagingDirectory.resolve(shapefileName).toFile());

            List<Path> stagedParts = collectParts(stagingDirectory, baseName);
            requirePart(stagedParts, baseName, ".shp");
            requirePart(stagedParts, baseName, ".shx");

            Map<Path, Path> backups = backUpExistingParts(directory, baseName);
            List<Path> published = new ArrayList<>();
            try {
                for (Path staged : stagedParts) {
                    Path destination = directory.resolve(staged.getFileName().toString());
                    Files.move(staged, destination, StandardCopyOption.REPLACE_EXISTING);
                    published.add(destination);
                }
            } catch (Exception publicationFailure) {
                rollBack(published, backups, publicationFailure);
                throw publicationFailure;
            }
            deleteAll(backups.values());
        } finally {
            deleteRecursively(stagingDirectory);
        }
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
