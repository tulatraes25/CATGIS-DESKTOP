package ar.com.catgis.data.online;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnlineTilePortableCacheRootContractTest {

    @TempDir
    Path tempDir;

    @Test
    void windowsUsesExistingAppDataPathRegardlessOfXdgEnvironment() {
        String appData = tempDir.resolve("app-data").toString();
        File expected = new File(appData, "CATGIS/online-tile-cache");
        File actual = OnlineTileCache.resolveCacheDirectory(
                "Windows 11", appData,
                tempDir.resolve("wrong-linux-xdg").toString(),
                tempDir.resolve("home").toString(), tempDir.resolve("tmp").toString());

        assertEquals(expected, actual, "Existing Windows cache directory must not move.");
    }

    @Test
    void linuxUsesAbsoluteXdgCacheHomeInsteadOfVolatileTmp() {
        String xdg = tempDir.resolve("xdg-cache").toString();
        File result = OnlineTileCache.resolveCacheDirectory(
                "Linux", tempDir.resolve("incorrect-win-appdata").toString(),
                xdg, tempDir.resolve("home").toString(), tempDir.resolve("tmp").toString());

        assertEquals(new File(xdg, "CATGIS/online-tile-cache"), result);
        assertNotEquals(new File(tempDir.resolve("tmp").toFile(), "catgis-online-tiles"), result,
                "A normal Linux user must not lose their offline tiles after reboot.");
    }

    @Test
    void linuxWithoutXdgUsesPersistentUserHomeAndRejectsRelativeXdg() {
        String home = tempDir.resolve("linux-home").toString();
        File expected = new File(home, ".cache/CATGIS/online-tile-cache");

        assertEquals(expected, OnlineTileCache.resolveCacheDirectory(
                "Linux Mint", null, null, home, tempDir.resolve("tmp").toString()));
        assertEquals(expected, OnlineTileCache.resolveCacheDirectory(
                "Linux Mint", null, "relative/path", home, tempDir.resolve("tmp").toString()));
        assertEquals(expected, OnlineTileCache.resolveCacheDirectory(
                "Linux Mint", null, "", home, tempDir.resolve("tmp").toString()));
    }

    @Test
    void windowsWithoutAppDataStillUsesUserHomeNotTemporaryStorage() {
        String home = tempDir.resolve("windows-home").toString();
        File expected = new File(home, "AppData/Roaming/CATGIS/online-tile-cache");
        assertEquals(expected, OnlineTileCache.resolveCacheDirectory(
                "Windows 10", null, null, home, tempDir.resolve("tmp").toString()));
    }

    @Test
    void macOsAvoidsBothWindowsAppDataAndLinuxTemporaryDirectory() {
        String home = tempDir.resolve("mac-home").toString();
        File expected = new File(home, "Library/Caches/CATGIS/online-tile-cache");
        assertEquals(expected, OnlineTileCache.resolveCacheDirectory(
                "Mac OS X", tempDir.resolve("app-data").toString(), null,
                home, tempDir.resolve("tmp").toString()));
        assertEquals(expected, OnlineTileCache.resolveCacheDirectory(
                "Darwin", null, null, home, tempDir.resolve("tmp").toString()));
    }

    @Test
    void headlessServiceWithoutHomeHasBoundedExplicitFallback() {
        String tmp = tempDir.resolve("tmp").toString();
        File actual = OnlineTileCache.resolveCacheDirectory(
                "Linux", null, "relative-xdg", null, tmp);
        assertEquals(new File(tmp, "catgis-online-tiles"), actual);
        assertTrue(actual.getPath().endsWith("catgis-online-tiles"));
    }
}
