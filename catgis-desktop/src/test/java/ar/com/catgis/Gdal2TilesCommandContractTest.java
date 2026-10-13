package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Gdal2TilesCommandContractTest {

    @TempDir
    Path tempDir;

    @Test
    void autoZoomMustNeverLeaveDanglingMinusZ() {
        File raster = tempDir.resolve("dem.tif").toFile();
        File tiles = tempDir.resolve("xyz").toFile();
        List<String> args = Gdal2TilesService.buildCommand(
                "/usr/bin/python3", raster, tiles, 0, -1);
        assertEquals(List.of("/usr/bin/python3", "-m", "gdal2tiles",
                        "--processes=4", "--resampling=bilinear",
                        raster.getAbsolutePath(), tiles.getAbsolutePath()), args);
        assertFalse(args.contains("-z"),
                "Auto zoom has no explicit -z; old implementation left a dangling -z.");
    }

    @Test
    void explicitZoomSuppliesExactlyOneRangeAndNoImplicitOptions() {
        File raster = tempDir.resolve("dem.tif").toFile();
        File tiles = tempDir.resolve("xyz").toFile();
        List<String> args = Gdal2TilesService.buildCommand(
                "C:\\OSGeo4W64\\bin\\python.exe", raster, tiles, 3, 8);
        assertEquals("C:\\OSGeo4W64\\bin\\python.exe", args.get(0));
        assertEquals(1, args.stream().filter("-z"::equals).count());
        assertEquals("3-8", args.get(args.indexOf("-z") + 1));
        assertFalse(args.contains("--processes=4"));
        assertFalse(args.contains("--resampling=bilinear"));
    }

    @Test
    void treatsRasterAndOutputPathsWithSpacesAsSingleArguments() {
        File raster = tempDir.resolve("GIS Data/Survey With Spaces.tif").toFile();
        File tiles = tempDir.resolve("GIS Results/XYZ Tiles").toFile();
        List<String> args = Gdal2TilesService.buildCommand(
                "/opt/homebrew/bin/python3", raster, tiles, 1, 4);
        assertEquals(7, args.size(), "One list member per ProcessBuilder argument.");
        assertEquals(raster.getAbsolutePath(), args.get(5));
        assertEquals(tiles.getAbsolutePath(), args.get(6));
    }

    @Test
    void rejectsIncompleteAndInvertedZoomArguments() {
        File raster = tempDir.resolve("dem.tif").toFile();
        File tiles = tempDir.resolve("xyz").toFile();
        assertThrows(IllegalArgumentException.class,
                () -> Gdal2TilesService.buildCommand("python3", raster, tiles, -1, -1));
        assertThrows(IllegalArgumentException.class,
                () -> Gdal2TilesService.buildCommand("python3", raster, tiles, 7, 3));
        assertThrows(IllegalArgumentException.class,
                () -> Gdal2TilesService.buildCommand(null, raster, tiles, 1, 3));
        assertThrows(IllegalArgumentException.class,
                () -> Gdal2TilesService.buildCommand("  ", raster, tiles, 1, 3));
        assertThrows(IllegalArgumentException.class,
                () -> Gdal2TilesService.buildCommand("python3", null, tiles, 1, 3));
        assertThrows(IllegalArgumentException.class,
                () -> Gdal2TilesService.buildCommand("python3", raster, null, 1, 3));
    }

    @Test
    void fixedZoomWithSameBoundsDoesNotTurnIntoAutoZoom() {
        File raster = tempDir.resolve("dem.tif").toFile();
        File tiles = tempDir.resolve("xyz").toFile();
        List<String> args = Gdal2TilesService.buildCommand(
                "python3", raster, tiles, 4, 4);
        assertTrue(args.contains("-z"));
        assertTrue(args.contains("4-4"));
    }
}
