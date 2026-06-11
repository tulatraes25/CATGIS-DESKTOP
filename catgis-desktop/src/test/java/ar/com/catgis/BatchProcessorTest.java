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

    @Test
    void processBatchWithEmptyInputReturnsZeroCounts() {
        BatchProcessor.BatchJob job = new BatchProcessor.BatchJob("empty", List.of(), outputDir, "copy");
        var result = BatchProcessor.processBatch(job, (input, output) -> {});

        assertEquals(0, result.success());
        assertEquals(0, result.failed());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void processBatchWithEmptyInputCallsProgressZeroTimes() {
        List<Integer> progressValues = new ArrayList<>();
        BatchProcessor.BatchJob job = new BatchProcessor.BatchJob("empty", List.of(), outputDir, "copy");
        BatchProcessor.processBatch(job, (input, output) -> {}, progress -> progressValues.add(progress));

        assertTrue(progressValues.isEmpty());
    }

    @Test
    void processBatchWithNullInputFilesThrowsNPE() {
        BatchProcessor.BatchJob job = new BatchProcessor.BatchJob("null", null, outputDir, "copy");
        assertThrows(NullPointerException.class,
                () -> BatchProcessor.processBatch(job, (input, output) -> {}));
    }

    @Test
    void processBatchWithNullCallbackStillProcesses() throws IOException {
        Files.createFile(inputDir.toPath().resolve("a.txt"));
        List<File> files = BatchProcessor.findFiles(inputDir, ".txt");
        BatchProcessor.BatchJob job = new BatchProcessor.BatchJob("null-cb", files, outputDir, "copy");

        BatchProcessor.BatchResult result = BatchProcessor.processBatch(job,
                (in, out) -> { try { java.nio.file.Files.copy(in.toPath(), out.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING); } catch (Exception ignored) {} }, null);
        assertNotNull(result);
        assertEquals(1, result.success());
    }

    @Test
    void processBatchWithNullProgressCallbackStillProcesses() throws IOException {
        Files.createFile(inputDir.toPath().resolve("a.txt"));
        Files.createFile(inputDir.toPath().resolve("b.txt"));
        List<File> files = BatchProcessor.findFiles(inputDir, ".txt");

        BatchProcessor.BatchJob job = new BatchProcessor.BatchJob("no-progress", files, outputDir, "copy");
        var result = BatchProcessor.processBatch(job, (input, output) -> {
            try {
                Files.copy(input.toPath(), output.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) { throw new RuntimeException(e); }
        }, null);

        assertEquals(2, result.success());
        assertEquals(0, result.failed());
    }

    @Test
    void processBatchProgressValuesAreMonotonicallyNonDecreasingAndEndAt100() throws IOException {
        for (int i = 0; i < 5; i++) {
            Files.createFile(inputDir.toPath().resolve("f" + i + ".txt"));
        }
        List<File> files = BatchProcessor.findFiles(inputDir, ".txt");

        List<Integer> progressValues = new ArrayList<>();
        BatchProcessor.BatchJob job = new BatchProcessor.BatchJob("progress-range", files, outputDir, "copy");
        BatchProcessor.processBatch(job, (input, output) -> {
            try {
                Files.copy(input.toPath(), output.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) { throw new RuntimeException(e); }
        }, progress -> progressValues.add(progress));

        assertFalse(progressValues.isEmpty());
        for (int p : progressValues) {
            assertTrue(p >= 0 && p <= 100, "Progress out of range: " + p);
        }
        for (int i = 1; i < progressValues.size(); i++) {
            assertTrue(progressValues.get(i) >= progressValues.get(i - 1),
                    "Progress decreased from " + progressValues.get(i - 1) + " to " + progressValues.get(i));
        }
        assertEquals(100, progressValues.get(progressValues.size() - 1).intValue());
    }

    @Test
    void processBatchPartialFailureReportsCorrectCounts() throws IOException {
        Files.createFile(inputDir.toPath().resolve("ok.txt"));
        Files.createFile(inputDir.toPath().resolve("fail.txt"));
        List<File> files = BatchProcessor.findFiles(inputDir, ".txt");

        BatchProcessor.BatchJob job = new BatchProcessor.BatchJob("partial", files, outputDir, "copy");
        var result = BatchProcessor.processBatch(job, (input, output) -> {
            if (input.getName().equals("fail.txt")) {
                throw new RuntimeException("Simulated failure");
            }
            try {
                Files.copy(input.toPath(), output.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(1, result.success());
        assertEquals(1, result.failed());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("fail.txt"));
    }
}
