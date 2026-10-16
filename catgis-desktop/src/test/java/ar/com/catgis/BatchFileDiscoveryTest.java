package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchFileDiscoveryTest {

    @TempDir
    Path directory;

    @Test
    void rootOnlyScanDoesNotInventRecursiveBehavior() throws Exception {
        Files.createFile(directory.resolve("root.tif"));
        Path nested = Files.createDirectory(directory.resolve("nested"));
        Files.createFile(nested.resolve("child.shp"));

        List<java.io.File> files = BatchFileDiscovery.find(directory.toFile(), false);

        assertEquals(1, files.size());
        assertEquals("root.tif", files.get(0).getName());
    }

    @Test
    void recursiveScanFindsSupportedFilesAndIgnoresUnsupportedFiles() throws Exception {
        Files.createFile(directory.resolve("root.tif"));
        Files.createFile(directory.resolve("notes.txt"));
        Path nested = Files.createDirectories(directory.resolve("a").resolve("b"));
        Files.createFile(nested.resolve("roads.geojson"));
        Files.createFile(nested.resolve("ignore.tmp"));

        List<java.io.File> files = BatchFileDiscovery.find(directory.toFile(), true);

        assertEquals(2, files.size());
        assertTrue(files.stream().anyMatch(file -> file.getName().equals("root.tif")));
        assertTrue(files.stream().anyMatch(file -> file.getName().equals("roads.geojson")));
    }

    @Test
    void resultsHaveStableAbsolutePathOrder() throws Exception {
        Path nested = Files.createDirectory(directory.resolve("nested"));
        Files.createFile(directory.resolve("z.tif"));
        Files.createFile(nested.resolve("a.shp"));
        Files.createFile(directory.resolve("m.gpkg"));

        List<java.io.File> files = BatchFileDiscovery.find(directory.toFile(), true);
        List<String> paths = files.stream().map(java.io.File::getAbsolutePath).toList();
        List<String> sorted = paths.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        assertEquals(sorted, paths);
    }

    @Test
    void invalidDirectoryReturnsAnEmptyImmutableResult() throws Exception {
        assertTrue(BatchFileDiscovery.find(null, true).isEmpty());
        assertTrue(BatchFileDiscovery.find(directory.resolve("missing").toFile(), true).isEmpty());
        List<java.io.File> result = BatchFileDiscovery.find(directory.resolve("missing").toFile(), false);
        assertTrue(result.isEmpty());
    }
}
