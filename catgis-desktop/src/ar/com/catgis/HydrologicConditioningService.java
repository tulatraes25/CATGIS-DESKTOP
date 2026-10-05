package ar.com.catgis;

import ar.com.catgis.data.raster.LocalRasterData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Hydrologic conditioning using WhiteboxTools.
 * Fill depressions, breach depressions, and flow routing.
 */
public final class HydrologicConditioningService {

    private static final Path WORK_DIR = Path.of(System.getProperty("user.home"), ".catgis", "hydro");

    private HydrologicConditioningService() {}

    /**
     * Fill single-cell depressions in a DEM (breach-first, then fill).
     * WhiteboxTools: FillDepressions
     */
    public static boolean fillDepressions(File demFile, File outputFile) throws IOException {
        ensureWorkDir();
        String wbt = ExternalToolService.findWhiteboxTools();
        if (wbt == null) return false;

        ExternalToolService.ToolResult r = ExternalToolService.execute(
                wbt, "--run=FillDepressions",
                "--input=" + demFile.getAbsolutePath(),
                "--output=" + outputFile.getAbsolutePath()
        );
        return r.success() && outputFile.exists();
    }

    /**
     * Breach depressions (carve channels through barriers).
     * WhiteboxTools: BreachDepressions
     */
    public static boolean breachDepressions(File demFile, File outputFile) throws IOException {
        ensureWorkDir();
        String wbt = ExternalToolService.findWhiteboxTools();
        if (wbt == null) return false;

        ExternalToolService.ToolResult r = ExternalToolService.execute(
                wbt, "--run=BreachDepressions",
                "--input=" + demFile.getAbsolutePath(),
                "--output=" + outputFile.getAbsolutePath()
        );
        return r.success() && outputFile.exists();
    }

    /**
     * D8 flow accumulation.
     * WhiteboxTools: D8FlowAccumulation
     */
    public static boolean d8FlowAccumulation(File demFile, File outputFile) throws IOException {
        ensureWorkDir();
        String wbt = ExternalToolService.findWhiteboxTools();
        if (wbt == null) return false;

        ExternalToolService.ToolResult r = ExternalToolService.execute(
                wbt, "--run=D8FlowAccumulation",
                "--input=" + demFile.getAbsolutePath(),
                "--output=" + outputFile.getAbsolutePath()
        );
        return r.success() && outputFile.exists();
    }

    /**
     * D-Infinity flow accumulation (multiple flow directions).
     * WhiteboxTools: DInfFlowAccumulation
     */
    public static boolean dInfFlowAccumulation(File demFile, File outputFile) throws IOException {
        ensureWorkDir();
        String wbt = ExternalToolService.findWhiteboxTools();
        if (wbt == null) return false;

        ExternalToolService.ToolResult r = ExternalToolService.execute(
                wbt, "--run=DInfFlowAccumulation",
                "--input=" + demFile.getAbsolutePath(),
                "--output=" + outputFile.getAbsolutePath()
        );
        return r.success() && outputFile.exists();
    }

    /**
     * Check if WhiteboxTools is available.
     */
    public static boolean isAvailable() {
        return ExternalToolService.findWhiteboxTools() != null;
    }

    // ─── Geomorphons ─────────────────────────────────────────────────

    /**
     * Automatic landform classification using the Geomorphons algorithm.
     * WhiteboxTools: Geomorphons
     */
    public static boolean geomorphons(File demFile, File outputFile, int searchRadius) throws IOException {
        ensureWorkDir();
        String wbt = ExternalToolService.findWhiteboxTools();
        if (wbt == null) return false;

        ExternalToolService.ToolResult r = ExternalToolService.execute(
                wbt, "--run=Geomorphons",
                "--input=" + demFile.getAbsolutePath(),
                "--output=" + outputFile.getAbsolutePath(),
                "--search=" + Math.max(3, searchRadius)
        );
        return r.success() && outputFile.exists();
    }

    private static void ensureWorkDir() throws IOException {
        Files.createDirectories(WORK_DIR);
    }
}
