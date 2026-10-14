package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Portable real kernel file locks. No Whitebox/GDAL native runtime required.
 */
class GdalTileTargetLockContractTest {

    @TempDir
    Path sandbox;

    private Path target() {
        return sandbox.resolve("XYZ tiles with spaces");
    }

    @Test
    void sameTargetIsExclusiveAndLeaseSurvivesNormalReopen() throws Exception {
        Path target = target();
        Path control = GdalTileTargetLock.lockPath(target);
        try (GdalTileTargetLock first = GdalTileTargetLock.tryAcquire(target)) {
            assertNotNull(first);
            assertTrue(Files.isRegularFile(control, LinkOption.NOFOLLOW_LINKS));
            assertNull(GdalTileTargetLock.tryAcquire(target),
                    "Same-JVM duplicate leases must be rejected explicitly.");
        }
        assertTrue(Files.exists(control),
                "Never unlink the lock file: another process could lock a new inode.");
        try (GdalTileTargetLock second = GdalTileTargetLock.tryAcquire(target)) {
            assertNotNull(second);
        }
    }

    @Test
    void caseInsensitivePlatformsShareOneLeaseForDifferentPathCasing() throws Exception {
        Path upper = sandbox.resolve("XYZ Tiles");
        Path lower = sandbox.resolve("xyz tiles");
        assertEquals(GdalTileTargetLock.lockPath(upper, "Windows 11"),
                GdalTileTargetLock.lockPath(lower, "Windows 11"));
        assertEquals(GdalTileTargetLock.lockPath(upper, "Mac OS X"),
                GdalTileTargetLock.lockPath(lower, "Mac OS X"));
        assertFalse(GdalTileTargetLock.lockPath(upper, "Linux").equals(
                GdalTileTargetLock.lockPath(lower, "Linux")),
                "Distinct case-sensitive Linux targets need no shared lease.");
    }

    @Test
    void corruptedOrUntrustedLockFileFailsWithoutOverwritingIt() throws Exception {
        Path target = target();
        Path control = GdalTileTargetLock.lockPath(target);
        Files.writeString(control, "UNTRUSTED");
        assertThrows(IOException.class, () -> GdalTileTargetLock.tryAcquire(target));
        assertEquals("UNTRUSTED", Files.readString(control));
    }

    @Test
    void symlinkedLockDoesNotFollowOutsideParentWhenSupported() throws Exception {
        Path target = target();
        Path external = sandbox.resolve("user-files");
        Files.writeString(external, "DO NOT TOUCH");
        Path control = GdalTileTargetLock.lockPath(target);
        try {
            Files.createSymbolicLink(control, external);
        } catch (IOException | UnsupportedOperationException missingPermission) {
            return; // Symlink privileges depend on Windows runner policy.
        }
        assertThrows(IOException.class, () -> GdalTileTargetLock.tryAcquire(target));
        assertEquals("DO NOT TOUCH", Files.readString(external));
    }

    @Test
    void separateJvmCannotAcquireWhileOriginalProcessHoldsTheLease() throws Exception {
        Path target = target();
        // Gradle test workers may not publish the test runtime classpath in
        // java.class.path; resolve actual test/main CodeSource directories.
        Path testClasses = Path.of(ProbeProcess.class
                .getProtectionDomain().getCodeSource().getLocation().toURI());
        Path mainClasses = Path.of(GdalTileTargetLock.class
                .getProtectionDomain().getCodeSource().getLocation().toURI());
        String cp = testClasses + File.pathSeparator + mainClasses;
        Path java = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name", "").toLowerCase().contains("win")
                        ? "java.exe" : "java");
        Process child = new ProcessBuilder(java.toString(), "-cp", cp,
                ProbeProcess.class.getName(), target.toString())
                .redirectErrorStream(true).start();
        try {
            try (BufferedReader response = new BufferedReader(new InputStreamReader(
                    child.getInputStream(), StandardCharsets.UTF_8))) {
                String hello = assertTimeoutPreemptively(Duration.ofSeconds(15),
                        response::readLine);
                assertEquals("ACQUIRED", hello,
                        "Child JVM must acquire the actual OS lock before test assertions.");
                assertNull(GdalTileTargetLock.tryAcquire(target),
                        "A different JVM must not acquire the target while child owns it.");
                child.getOutputStream().write('\n');
                child.getOutputStream().flush();
                assertTrue(child.waitFor(15L, TimeUnit.SECONDS));
                assertEquals(0, child.exitValue());
            }
        } finally {
            if (child.isAlive()) {
                child.destroyForcibly();
                child.waitFor(5L, TimeUnit.SECONDS);
            }
        }
        try (GdalTileTargetLock after = GdalTileTargetLock.tryAcquire(target)) {
            assertNotNull(after, "Crash/exit must release kernel lock.");
        }
    }

    // Invoked as an isolated OS process by the test above, without JUnit APIs.
    public static final class ProbeProcess {
        private ProbeProcess() {}

        public static void main(String[] argv) throws Exception {
            try (GdalTileTargetLock lease =
                         GdalTileTargetLock.tryAcquire(Path.of(argv[0]))) {
                if (lease == null) {
                    System.out.println("NOT_ACQUIRED");
                    System.out.flush();
                    System.exit(8);
                    return;
                }
                System.out.println("ACQUIRED");
                System.out.flush();
                System.in.read();
            }
        }
    }
}
