package ar.com.catgis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BatchProcessorTest {

    @TempDir
    java.nio.file.Path tempDir;

    private File inputDir;
    private File outputDir;

    @BeforeEach
    void setUp() {
        inputDir = tempDir.resolve("input").toFile();
        outputDir = tempDir.resolve("output").toFile();
        inputDir.mkdirs();
        outputDir.mkdirs();
    }

    @Test
    void findFilesReturnsMatchingExtension() throws IOException {
        Files.createFile(inputDir.toPath().resolve("test.shp"));
        Files.createFile(inputDir.toPath().resolve("test.geojson"));
        Files.createFile(inputDir.toPath().resolve("test.txt"));
        List<File> shpFiles = BatchProcessor.findFiles(inputDir, ".shp");
        assertEquals(1, shpFiles.size());
        assertEquals("test.shp", shpFiles.get(0).getName());
    }

    @Test
    void findAllGisFilesFindsMultipleFormats() throws IOException {
        Files.createFile(inputDir.toPath().resolve("a.shp"));
        Files.createFile(inputDir.toPath().resolve("b.geojson"));
        Files.createFile(inputDir.toPath().resolve("c.tif"));
        Files.createFile(inputDir.toPath().resolve("d.txt"));
        List<File> gisFiles = BatchProcessor.findAllGisFiles(inputDir);
        assertEquals(3, gisFiles.size());
    }

    @Test
    void processBatchExecutesForAllFiles() throws IOException {
        Files.createFile(inputDir.toPath().resolve("a.txt"));
        Files.createFile(inputDir.toPath().resolve("b.txt"));
        Files.createFile(inputDir.toPath().resolve("c.txt"));
        List<File> files = BatchProcessor.findFiles(inputDir, ".txt");

        BatchProcessor.BatchJob job = new BatchProcessor.BatchJob("copy", files, outputDir, "copy");
        var result = BatchProcessor.processBatch(job, (input, output) -> {
            try { Files.copy(input.toPath(), output.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
            catch (IOException e) { throw new RuntimeException(e); }
        });

        assertEquals(3, result.success());
        assertEquals(0, result.failed());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void processBatchReportsErrors() {
        try {
            Files.createFile(inputDir.toPath().resolve("a.txt"));
        } catch (IOException e) { fail(e); }
        List<File> files = BatchProcessor.findFiles(inputDir, ".txt");

        BatchProcessor.BatchJob job = new BatchProcessor.BatchJob("fail", files, outputDir, "fail");
        var result = BatchProcessor.processBatch(job, (input, output) -> {
            throw new RuntimeException("Test error");
        });

        assertEquals(0, result.success());
        assertEquals(1, result.failed());
        assertEquals(1, result.errors().size());
    }

    @Test
    void processBatchReportsProgress() {
        try {
            Files.createFile(inputDir.toPath().resolve("a.txt"));
            Files.createFile(inputDir.toPath().resolve("b.txt"));
        } catch (IOException e) { fail(e); }
        List<File> files = BatchProcessor.findFiles(inputDir, ".txt");

        List<Integer> progressValues = new ArrayList<>();
        BatchProcessor.BatchJob job = new BatchProcessor.BatchJob("progress", files, outputDir, "copy");
        BatchProcessor.processBatch(job, (input, output) -> {
            try {
                Files.copy(input.toPath(), output.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) { throw new RuntimeException(e); }
        }, progress -> progressValues.add(progress));

        assertTrue(progressValues.size() > 0);
        assertEquals(100, progressValues.get(progressValues.size() - 1).intValue());
    }

    @Test
    void findFilesReturnsEmptyForNonexistentDir() {
        List<File> files = BatchProcessor.findFiles(new File("/nonexistent"), ".shp");
        assertTrue(files.isEmpty());
    }

    @Test
    void findFilesReturnsEmptyForNullDir() {
        List<File> files = BatchProcessor.findFiles(null, ".shp");
        assertTrue(files.isEmpty());
    }
}
