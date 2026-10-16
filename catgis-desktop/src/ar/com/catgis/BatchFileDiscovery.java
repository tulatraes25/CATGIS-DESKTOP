package ar.com.catgis;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/** File discovery for batch workflows, kept outside Swing. */
final class BatchFileDiscovery {
    private static final int MAX_FILES = 100_000;

    private BatchFileDiscovery() {}

    static List<File> find(File directory, boolean includeSubdirs) throws IOException {
        if (directory == null || !directory.isDirectory()) {
            return List.of();
        }

        List<File> files = new ArrayList<>();
        if (!includeSubdirs) {
            files.addAll(BatchProcessor.findAllGisFiles(directory));
            ensureLimit(files.size());
        } else {
            try (Stream<Path> paths = Files.walk(directory.toPath())) {
                Iterator<Path> directories = paths
                        .filter(path -> Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
                        .iterator();
                while (directories.hasNext()) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new IOException("Escaneo cancelado.");
                    }
                    files.addAll(BatchProcessor.findAllGisFiles(directories.next().toFile()));
                    ensureLimit(files.size());
                }
            } catch (UncheckedIOException failure) {
                throw failure.getCause();
            }
        }

        files.sort(Comparator.comparing(File::getAbsolutePath, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(files);
    }

    private static void ensureLimit(int count) throws IOException {
        if (count > MAX_FILES) {
            throw new IOException("El escaneo excede el limite de " + MAX_FILES
                    + " archivos GIS. Reduce el directorio de entrada.");
        }
    }
}
