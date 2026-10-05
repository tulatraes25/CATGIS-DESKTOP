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

    // --- Template tests ---

    @Test
    void templateRoundTripPreservesFields() throws Exception {
        BatchProcessor.BatchTemplate template = new BatchProcessor.BatchTemplate(
                "NDVI Batch", "Compute NDVI on all TIFFs", "spectral_index",
                ".tif", true,
                java.util.Map.of("indexId", "ndvi", "bandA", "3", "bandB", "4")
        );

        String json = template.toJson();
        BatchProcessor.BatchTemplate restored = BatchProcessor.BatchTemplate.fromJson(json);

        assertEquals(template.name(), restored.name());
        assertEquals(template.description(), restored.description());
        assertEquals(template.operation(), restored.operation());
        assertEquals(template.extensionFilter(), restored.extensionFilter());
        assertEquals(template.includeSubdirs(), restored.includeSubdirs());
        assertEquals(template.settings().size(), restored.settings().size());
        assertEquals("ndvi", restored.settings().get("indexId"));
    }

    @Test
    void saveAndLoadTemplateWorks(@TempDir java.nio.file.Path tmpDir) throws Exception {
        // Override TEMPLATES_DIR for test
        BatchProcessor.BatchTemplate template = new BatchProcessor.BatchTemplate(
                "Test Template", "A test", "copy", ".shp", false, java.util.Map.of()
        );

        BatchProcessor.saveTemplate(template);
        var templates = BatchProcessor.loadAllTemplates();

        assertFalse(templates.isEmpty());
        assertTrue(templates.stream().anyMatch(t -> t.name().equals("Test Template")));

        // Cleanup
        BatchProcessor.deleteTemplate("Test Template");
    }

    @Test
    void deleteTemplateRemovesFile() throws Exception {
        BatchProcessor.BatchTemplate template = new BatchProcessor.BatchTemplate(
                "Delete Me", "", "copy", ".shp", false, java.util.Map.of()
        );
        BatchProcessor.saveTemplate(template);

        boolean deleted = BatchProcessor.deleteTemplate("Delete Me");
        assertTrue(deleted);

        var templates = BatchProcessor.loadAllTemplates();
        assertTrue(templates.stream().noneMatch(t -> t.name().equals("Delete Me")));
    }

    @Test
    void templateWithEmptySettingsStillWorks() {
        BatchProcessor.BatchTemplate template = new BatchProcessor.BatchTemplate(
                "Minimal", null, "convert", null, false, null
        );
        String json = template.toJson();
        assertTrue(json.contains("Minimal"));
        BatchProcessor.BatchTemplate restored = BatchProcessor.BatchTemplate.fromJson(json);
        assertEquals("Minimal", restored.name());
    }
}
