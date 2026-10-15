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
 * Portable real kernel file locks for WCS output paths. No GIS native runtime required.
 */
class WcsTargetLockContractTest {

    @TempDir
    Path sandbox;

    private Path target() {
        return sandbox.resolve("WCS tiles with spaces");
    }

    @Test
    void aliasedParentSpellingsShareTheSameKernelLease() throws Exception {
        Path actual = sandbox.resolve("canonical WCS coverage.tif");
        Path withDot = sandbox.resolve(".").resolve("canonical WCS coverage.tif");
        assertEquals(WcsTargetLock.lockPath(actual), WcsTargetLock.lockPath(withDot),
                "The lock identity must canonicalize a parent's path spelling.");
        try (WcsTargetLock first = WcsTargetLock.tryAcquire(withDot)) {
            assertNotNull(first);
            assertNull(WcsTargetLock.tryAcquire(actual),
                    "A differently spelled parent must not acquire a second inode.");
        }

        Path realFolder = Files.createDirectory(sandbox.resolve("real WCS folder"));
        Path linkFolder = sandbox.resolve("WCS folder alias");
        try {
            Files.createSymbolicLink(linkFolder, realFolder.getFileName());
        } catch (IOException | UnsupportedOperationException | SecurityException unavailable) {
            return; // Windows symlink privileges vary; the dot regression is mandatory.
        }
        Path directTarget = realFolder.resolve("raster.tif");
        Path aliasedTarget = linkFolder.resolve("raster.tif");
        assertEquals(WcsTargetLock.lockPath(directTarget),
                WcsTargetLock.lockPath(aliasedTarget),
                "A symlinked parent must not produce a different lease inode.");
        try (WcsTargetLock held = WcsTargetLock.tryAcquire(directTarget)) {
            assertNotNull(held);
            assertNull(WcsTargetLock.tryAcquire(aliasedTarget));
        }
    }

    @Test
    void sameTargetIsExclusiveAndLeaseSurvivesNormalReopen() throws Exception {
        Path target = target();
        Path control = WcsTargetLock.lockPath(target);
        try (WcsTargetLock first = WcsTargetLock.tryAcquire(target)) {
            assertNotNull(first);
            assertTrue(Files.isRegularFile(control, LinkOption.NOFOLLOW_LINKS));
            assertNull(WcsTargetLock.tryAcquire(target),
                    "Same-JVM duplicate leases must be rejected explicitly.");
        }
        assertTrue(Files.exists(control),
                "Never unlink the lock file: another process could lock a new inode.");
        try (WcsTargetLock second = WcsTargetLock.tryAcquire(target)) {
            assertNotNull(second);
        }
    }

    @Test
    void caseInsensitivePlatformsShareOneLeaseForDifferentPathCasing() throws Exception {
        Path upper = sandbox.resolve("WCS Tiles");
        Path lower = sandbox.resolve("wcs tiles");
        assertEquals(WcsTargetLock.lockPath(upper, "Windows 11"),
                WcsTargetLock.lockPath(lower, "Windows 11"));
        assertEquals(WcsTargetLock.lockPath(upper, "Mac OS X"),
                WcsTargetLock.lockPath(lower, "Mac OS X"));
        assertFalse(WcsTargetLock.lockPath(upper, "Linux").equals(
                WcsTargetLock.lockPath(lower, "Linux")),
                "Distinct case-sensitive Linux targets need no shared lease.");
    }

    @Test
    void corruptedOrUntrustedLockFileFailsWithoutOverwritingIt() throws Exception {
        Path target = target();
        Path control = WcsTargetLock.lockPath(target);
        Files.writeString(control, "UNTRUSTED");
        assertThrows(IOException.class, () -> WcsTargetLock.tryAcquire(target));
        assertEquals("UNTRUSTED", Files.readString(control));
    }

    @Test
    void symlinkedLockDoesNotFollowOutsideParentWhenSupported() throws Exception {
        Path target = target();
        Path external = sandbox.resolve("user-files");
        Files.writeString(external, "DO NOT TOUCH");
        Path control = WcsTargetLock.lockPath(target);
        try {
            Files.createSymbolicLink(control, external);
        } catch (IOException | UnsupportedOperationException missingPermission) {
            return; // Symlink privileges depend on Windows runner policy.
        }
        assertThrows(IOException.class, () -> WcsTargetLock.tryAcquire(target));
        assertEquals("DO NOT TOUCH", Files.readString(external));
    }

    @Test
    void separateJvmCannotAcquireWhileOriginalProcessHoldsTheLease() throws Exception {
        Path target = target();
        // Gradle test workers may not publish the test runtime classpath in
        // java.class.path; resolve actual test/main CodeSource directories.
        Path testClasses = Path.of(ProbeProcess.class
                .getProtectionDomain().getCodeSource().getLocation().toURI());
        Path mainClasses = Path.of(WcsTargetLock.class
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
                assertNull(WcsTargetLock.tryAcquire(target),
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
        try (WcsTargetLock after = WcsTargetLock.tryAcquire(target)) {
            assertNotNull(after, "Crash/exit must release kernel lock.");
        }
    }

    // Invoked as an isolated OS process by the test above, without JUnit APIs.
    public static final class ProbeProcess {
        private ProbeProcess() {}

        public static void main(String[] argv) throws Exception {
            try (WcsTargetLock lease =
                         WcsTargetLock.tryAcquire(Path.of(argv[0]))) {
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
