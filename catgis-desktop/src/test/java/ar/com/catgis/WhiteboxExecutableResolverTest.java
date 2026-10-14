package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhiteboxExecutableResolverTest {
    @TempDir
    Path temporary;

    private Path makeExecutable(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, "#!/bin/sh\nexit 0\n");
        file.toFile().setExecutable(true);
        assertTrue(Files.isExecutable(file));
        return file;
    }

    @Test
    void windowsAndUnixNamesRemainStrictAndExplicit() {
        assertEquals(List.of("whitebox_tools.exe", "wbt.exe"),
                WhiteboxExecutableResolver.nativeNames("Windows 11"));
        assertEquals(List.of("whitebox_tools", "wbt"),
                WhiteboxExecutableResolver.nativeNames("Linux"));
        assertEquals(List.of("whitebox_tools", "wbt"),
                WhiteboxExecutableResolver.nativeNames("Mac OS X"));
    }

    @Test
    void localDirectoryWithSpacesWorksOnWindowsPolicyWithoutSpawningWhere() throws Exception {
        Path tools = temporary.resolve("CATGIS Tools with spaces");
        Path executable = makeExecutable(tools.resolve("whitebox_tools.exe"));
        assertEquals(executable.toRealPath().toString(),
                WhiteboxExecutableResolver.resolve("Windows 11", null, List.of(tools)));
    }

    @Test
    void unixNamedExecutablesWorkWithinApprovedLocations() throws Exception {
        Path bin = temporary.resolve("usr/local/bin");
        Path first = makeExecutable(bin.resolve("whitebox_tools"));
        assertEquals(first.toRealPath().toString(),
                WhiteboxExecutableResolver.resolve("Linux", null, List.of(bin)));
        assertEquals(first.toRealPath().toString(),
                WhiteboxExecutableResolver.resolve("Mac OS X", null, List.of(bin)));
    }

    @Test
    void explicitBinaryOverridesOtherLocationsAndRejectsUnknownNames() throws Exception {
        Path bin = temporary.resolve("trusted");
        Path executable = makeExecutable(bin.resolve("whitebox_tools"));
        Path other = temporary.resolve("fallback");
        makeExecutable(other.resolve("whitebox_tools"));
        assertEquals(executable.toRealPath().toString(),
                WhiteboxExecutableResolver.resolve("Linux", executable.toString(),
                        List.of(other)));
        assertNull(WhiteboxExecutableResolver.resolve("Linux",
                makeExecutable(bin.resolve("other_tool")).toString(), List.of(other)));
    }

    @Test
    void explicitDirectoryIsAuthoritativeEvenWhenMissingExecutable() throws Exception {
        Path trusted = temporary.resolve("whitebox dir");
        Files.createDirectories(trusted);
        Path fallback = temporary.resolve("fallback");
        makeExecutable(fallback.resolve("whitebox_tools"));
        assertNull(WhiteboxExecutableResolver.resolve("Linux", trusted.toString(),
                List.of(fallback)), "Explicit incomplete configuration must fail closed.");
    }

    @Test
    void rejectsRelativeOverrideAndPathTraversalWithoutFallback() throws Exception {
        Path fallback = temporary.resolve("fallback");
        makeExecutable(fallback.resolve("whitebox_tools"));
        assertNull(WhiteboxExecutableResolver.resolve("Linux",
                "bin/whitebox_tools", List.of(fallback)));
        assertNull(WhiteboxExecutableResolver.resolve("Linux",
                temporary.resolve("missing/../fallback/whitebox_tools").toString(),
                List.of(fallback)));
    }

    @Test
    void refusesNonExecutableFileDirectoryAndUnknownRootName() throws Exception {
        Path root = temporary.resolve("trusted");
        Files.createDirectories(root.resolve("whitebox_tools"));
        assertNull(WhiteboxExecutableResolver.resolve("Linux", null, List.of(root)));
        Files.delete(root.resolve("whitebox_tools"));
        Path binary = root.resolve("whitebox_tools");
        Files.writeString(binary, "no-execute");
        binary.toFile().setExecutable(false, false);
        if (!Files.isExecutable(binary)) {
            assertNull(WhiteboxExecutableResolver.resolve("Linux", null, List.of(root)));
        }
        assertNull(WhiteboxExecutableResolver.resolve("Linux", null,
                List.of(temporary.resolve("missing"))));
    }

    @Test
    void followsHomebrewStyleSymlinkOnlyWhenItPointsToExecutableFile() throws Exception {
        Path cellar = temporary.resolve("Cellar/whitebox/3/bin/whitebox_tools");
        makeExecutable(cellar);
        Path brewBin = temporary.resolve("homebrew/bin");
        Files.createDirectories(brewBin);
        Path link = brewBin.resolve("whitebox_tools");
        try {
            Files.createSymbolicLink(link, cellar);
        } catch (IOException | UnsupportedOperationException unavailable) {
            return; // Windows runner may lack symbolic link privileges.
        }
        assertEquals(cellar.toRealPath().toString(),
                WhiteboxExecutableResolver.resolve("Mac OS X", null, List.of(brewBin)));
        Files.delete(cellar);
        assertNull(WhiteboxExecutableResolver.resolve("Mac OS X", null, List.of(brewBin)),
                "Dangling Homebrew symlink must never return an executable.");
    }

    @Test
    void platformDirectoryPoliciesPreferUserToolsAndIgnoreWildcardPath() {
        Path local = temporary.resolve("my tools");
        List<Path> linux = WhiteboxExecutableResolver.candidateDirectories(
                "Linux", local, null, null);
        List<Path> mac = WhiteboxExecutableResolver.candidateDirectories(
                "Mac OS X", local, null, null);
        List<Path> win = WhiteboxExecutableResolver.candidateDirectories(
                "Windows 11", local, temporary.toString(), null);
        assertEquals(local, linux.get(0));
        // Unix paths are native absolute only on POSIX JVMs.
        // Windows validates its own Program Files policy below; macOS and
        // Linux runners independently validate these absolute NIO paths.
        if (!System.getProperty("os.name", "").toLowerCase().contains("windows")) {
            assertTrue(linux.contains(Path.of("/usr/bin")));
            assertTrue(mac.contains(Path.of("/opt/homebrew/bin")));
            assertTrue(mac.contains(Path.of("/opt/local/bin")));
        }
        assertTrue(win.contains(temporary.resolve("WhiteboxTools")));
        assertFalse(linux.contains(temporary.resolve("untrusted PATH entry")));
    }

    @Test
    void optionalGisToolNamesAreStrictlyAllowlisted() {
        assertFalse(WhiteboxExecutableResolver.isOtherApprovedToolPresent(
                "evil;curl https://invalid.example"));
        assertFalse(WhiteboxExecutableResolver.isOtherApprovedToolPresent("../../../evil"));
        assertFalse(ExternalToolService.isToolAvailable("unknown_tool_12345"));
    }

    @Test
    void availabilityProbeNeverBlocksOrAccessesDiskFromSwingEdt() throws Exception {
        java.util.concurrent.atomic.AtomicReference<String> found =
                new java.util.concurrent.atomic.AtomicReference<>("incomplete");
        java.util.concurrent.atomic.AtomicBoolean available =
                new java.util.concurrent.atomic.AtomicBoolean(true);
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            found.set(ExternalToolService.findWhiteboxTools());
            available.set(ExternalToolService.isToolAvailable("whitebox_tools"));
        });
        assertNull(found.get());
        assertFalse(available.get());
    }

    @Test
    void executeWhiteboxRejectsInvalidRegistryIdentifierWithoutLaunchingAnything() {
        ExternalToolService.ToolResult error =
                ExternalToolService.executeWhitebox("--help");
        assertFalse(error.success());
        assertTrue(error.error().contains("invalido"));
        assertEquals(-1, error.exitCode());
    }
}
