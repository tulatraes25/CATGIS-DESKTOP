package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GdalSupportPortableResolverContractTest {

    @TempDir
    Path tempDir;

    @Test
    void nativeExecutableNamesWorkAcrossAllThreeDesktopPlatforms() {
        assertEquals("gdal_translate.exe",
                GdalSupport.nativeExecutableName("gdal_translate", "Windows 11"));
        assertEquals("gdalwarp.exe",
                GdalSupport.nativeExecutableName("gdalwarp.exe", "Windows 10"));
        assertEquals("gdal_translate",
                GdalSupport.nativeExecutableName("gdal_translate.exe", "Linux"));
        assertEquals("gdalinfo",
                GdalSupport.nativeExecutableName("gdalinfo.exe", "Mac OS X"));
        assertEquals("gdalwarp",
                GdalSupport.nativeExecutableName("gdalwarp.exe", "Darwin"));
        assertEquals("gdal",
                GdalSupport.nativeExecutableName("gdal.exe", "Linux"));
        assertEquals("gdal",
                GdalSupport.nativeExecutableName("gdal", "Mac OS X"));
        assertEquals("gdal.exe",
                GdalSupport.nativeExecutableName("gdal", "Windows 11"));
    }

    @Test
    void rejectsArbitraryExecutablesAndPathTraversalInsteadOfUsingShellOrPath() {
        for (String invalid : List.of("../gdalwarp", "gdalwarp.exe;touch /tmp/nope",
                "gdalinfo.exe -of GTiff", "cmd.exe", "/bin/sh",
                "python", "ogr2ogr", "../../usr/bin/gdalinfo")) {
            assertThrows(IllegalArgumentException.class,
                    () -> GdalSupport.nativeExecutableName(invalid, "Mac OS X"),
                    "Unreviewed external process name must be rejected: " + invalid);
        }
        assertThrows(IllegalArgumentException.class,
                () -> GdalSupport.nativeExecutableName(null, "Linux"));
    }

    @Test
    void defaultMacCandidatesIncludeBothHomebrewArchitecturesAndMacports() {
        List<File> dirs = GdalSupport.candidateDirectories(
                "Mac OS X", null, null, null);
        assertEquals(new File("/opt/homebrew/bin"), dirs.get(0));
        assertTrue(dirs.contains(new File("/usr/local/bin")));
        assertTrue(dirs.contains(new File("/opt/local/bin")));
        assertFalse(dirs.contains(new File("C:\\OSGeo4W\\bin")));
    }

    @Test
    void defaultLinuxCandidatesExcludeVolatileTmpAndUntrustedPath() {
        List<File> dirs = GdalSupport.candidateDirectories(
                "Linux Mint", null, null, null);
        assertEquals(new File("/usr/bin"), dirs.get(0));
        assertTrue(dirs.contains(new File("/usr/local/bin")));
        // File.isAbsolute() uses the HOST operating system's parser.
        // A Windows runner simulating Linux must not call it on "/usr/bin".
        // The candidate policy is verified by OS-neutral list membership.
        assertFalse(dirs.contains(new File("/tmp")));
        assertFalse(dirs.contains(new File(System.getProperty("java.io.tmpdir"))));
        assertFalse(dirs.contains(new File(".")));
    }

    @Test
    void windowsRetainsOsgeoOrderAndHonorsAbsoluteOverrides() {
        File osgeo = tempDir.resolve("OSGeo4W/bin").toFile();
        File osgeo64 = tempDir.resolve("OSGeo4W64/bin").toFile();
        List<File> dirs = GdalSupport.candidateDirectories(
                "Windows 11", null, osgeo.getAbsolutePath(), osgeo64.getAbsolutePath());
        assertEquals(List.of(osgeo, osgeo64), dirs);
    }

    @Test
    void configuredAbsoluteDirectoryOverridesDefaultAndRelativeDirectoryIsRejected() {
        File override = tempDir.resolve("gdal bin").toFile();
        List<File> dirs = GdalSupport.candidateDirectories(
                "Mac OS X", override.getAbsolutePath(), null, null);
        assertEquals(override, dirs.get(0));
        assertThrows(GdalSupport.GdalNotAvailableException.class,
                () -> GdalSupport.candidateDirectories("Linux", "relative/bin", null, null));
        assertThrows(GdalSupport.GdalNotAvailableException.class,
                () -> GdalSupport.candidateDirectories("Windows 11", null,
                        "relative/osgeo", null));
    }

    @Test
    void resolvesExistingNativeToolWithoutExecutingItOrLookingInPath() throws Exception {
        Path bin = Files.createDirectory(tempDir.resolve("native-bin"));
        Path linuxExe = bin.resolve("gdal_translate");
        Files.writeString(linuxExe, "not run during lookup", StandardCharsets.UTF_8);
        assertTrue(linuxExe.toFile().setExecutable(true, false));
        assertEquals(linuxExe.toFile().getAbsolutePath(),
                GdalSupport.resolveFromDirectories(
                        "gdal_translate.exe", "Mac OS X", List.of(bin.toFile())));
        assertEquals(linuxExe.toFile().getAbsolutePath(),
                GdalSupport.resolveFromDirectories(
                        "gdal_translate.exe", "Linux", List.of(bin.toFile())));

        Path windowsExe = bin.resolve("gdalwarp.exe");
        Files.writeString(windowsExe, "not run during lookup", StandardCharsets.UTF_8);
        assertTrue(windowsExe.toFile().setExecutable(true, false));
        assertEquals(windowsExe.toFile().getAbsolutePath(),
                GdalSupport.resolveFromDirectories(
                        "gdalwarp.exe", "Windows 11", List.of(bin.toFile())));
    }

    @Test
    void unavailableNativeToolFailsClosedWithActionablePlatformMessage() {
        File bin = tempDir.resolve("empty-bin").toFile();
        assertTrue(bin.mkdirs());
        GdalSupport.GdalNotAvailableException unix = assertThrows(
                GdalSupport.GdalNotAvailableException.class,
                () -> GdalSupport.resolveFromDirectories(
                        "gdalinfo.exe", "Mac OS X", List.of(bin)));
        assertTrue(unix.getMessage().contains("CATGIS_GDAL_BIN"));
        GdalSupport.GdalNotAvailableException windows = assertThrows(
                GdalSupport.GdalNotAvailableException.class,
                () -> GdalSupport.resolveFromDirectories(
                        "gdalinfo.exe", "Windows 11", List.of(bin)));
        assertTrue(windows.getMessage().contains("OSGeo4W"));
    }

    @Test
    void rejectsDirectoryNamedLikeExecutableAndUntrustedRelativeSearchRoot() throws Exception {
        Path dir = Files.createDirectory(tempDir.resolve("bin"));
        Files.createDirectory(dir.resolve("gdalinfo"));
        assertThrows(GdalSupport.GdalNotAvailableException.class,
                () -> GdalSupport.resolveFromDirectories(
                        "gdalinfo.exe", "Linux", List.of(dir.toFile())));
        assertThrows(GdalSupport.GdalNotAvailableException.class,
                () -> GdalSupport.resolveFromDirectories(
                        "gdalinfo.exe", "Linux", List.of(new File("."))));
    }
}
