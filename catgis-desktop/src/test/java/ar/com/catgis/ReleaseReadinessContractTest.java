package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Release readiness guard: version and channel must be declared consistently in
 * code, build, manifest and beta documentation. This does not certify A0.7/F3.
 */
class ReleaseReadinessContractTest {

    private static final String VERSION = "1.0.0-beta.2";

    @Test
    void versionIsDeclaredConsistentlyEverywhere() throws Exception {
        Path root = repoRoot();
        assertEquals(VERSION, AppVersion.VERSION);
        assertTrue(AppVersion.versionText().contains(VERSION), AppVersion.versionText());
        assertTrue(AppVersion.versionText().toLowerCase(java.util.Locale.ROOT).contains("beta"));

        String gradle = Files.readString(root.resolve("catgis-desktop/build.gradle"));
        assertTrue(gradle.contains("version = '" + VERSION + "'"),
                "build.gradle version drift");

        String manifest = Files.readString(root.resolve("docs/CAPABILITY_MANIFEST.md"));
        assertTrue(manifest.contains("| Versión | **" + VERSION + "**"),
                "manifest version drift");

        String readme = Files.readString(root.resolve("docs/BETA_README.md"));
        assertTrue(readme.contains(VERSION), "BETA_README version drift");
    }

    @Test
    void betaReleaseWorkflowPublishesPreReleaseWithAssets() throws Exception {
        Path workflow = repoRoot().resolve(".github/workflows/release-beta.yml");
        assertTrue(Files.isRegularFile(workflow), "release-beta workflow missing");
        String text = Files.readString(workflow);
        assertTrue(text.contains("gh release create"), "workflow must create a release");
        assertTrue(text.contains("--prerelease"), "beta channel must stay a pre-release");
        assertTrue(text.contains("release-assets/*"), "workflow must upload artifacts");
        assertTrue(text.contains("build/installer/linux/app-image")
                        && text.contains("build/installer/windows/app-image")
                        && text.contains("build/installer/macos/app-image"),
                "workflow must build all three app images");
    }

    @Test
    void manifestAndReadmeKeepPhysicalCertificationHonest() throws Exception {
        Path root = repoRoot();
        String manifest = Files.readString(root.resolve("docs/CAPABILITY_MANIFEST.md"));
        assertTrue(manifest.contains("NOT_CERTIFIED"),
                "manifest must not silently claim A0.7/F3 certification");
        String readme = Files.readString(root.resolve("docs/BETA_README.md"));
        assertTrue(readme.contains("NOT_CERTIFIED"),
                "beta README must keep the physical gate explicit");
    }

    @Test
    void performanceSmokeForTransactionalBatchStaysBounded() throws Exception {
        Path outputDir = Files.createTempDirectory("catgis-batch-perf-");
        try {
            java.util.List<java.io.File> inputs = new java.util.ArrayList<>();
            for (int i = 0; i < 40; i++) {
                Path input = Files.createTempFile("catgis-perf-input-", ".txt");
                Files.writeString(input, "payload-" + i);
                inputs.add(input.toFile());
            }
            var job = new BatchProcessor.BatchJob("perf", inputs, outputDir.toFile(), "copy");
            long start = System.nanoTime();
            var result = BatchTransactionalProcessor.process(job, (input, output) -> {
                try {
                    Files.copy(input.toPath(), output.toPath());
                } catch (java.io.IOException io) {
                    throw new RuntimeException(io);
                }
            }, null);
            long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
            assertEquals(40, result.success(), result.errors().toString());
            assertEquals(0, result.failed());
            assertTrue(elapsedMs < 120_000L,
                    "transactional batch performance smoke exceeded bound: " + elapsedMs + "ms");
            System.out.println("BATCH_TX_PERF files=40 ms=" + elapsedMs);
            for (java.io.File input : inputs) {
                Files.deleteIfExists(input.toPath());
            }
        } finally {
            deleteRecursively(outputDir);
        }
    }

    private static Path repoRoot() {
        Path cwd = Path.of("").toAbsolutePath().normalize();
        if (Files.isDirectory(cwd.resolve(".kiro"))) {
            return cwd;
        }
        Path parent = cwd.getParent();
        if (parent != null && Files.isDirectory(parent.resolve(".kiro"))) {
            return parent;
        }
        throw new AssertionError("No se encontro el checkout CATGIS desde " + cwd);
    }

    private static void deleteRecursively(Path root) throws Exception {
        try (var paths = Files.walk(root)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (java.io.IOException ignored) {
                    // best effort in test cleanup
                }
            });
        }
    }
}
