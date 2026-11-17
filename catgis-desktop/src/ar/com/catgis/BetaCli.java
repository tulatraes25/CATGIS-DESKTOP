package ar.com.catgis;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Beta command-line contract for CATGIS/CATMAP:
 * {@code --version}, {@code --self-test} and {@code --catmap-standalone}.
 * Parsing and the headless self-test stay independent from Swing.
 */
public final class BetaCli {

    /** Parsed beta options plus the remaining (feature) arguments. */
    public record Options(boolean version, boolean selfTest,
                          boolean catmapStandalone, String[] remaining) {
        public Options {
            remaining = remaining == null ? new String[0] : remaining.clone();
        }
    }

    public static final String VERSION_FLAG = "--version";
    public static final String SELF_TEST_FLAG = "--self-test";
    public static final String CATMAP_STANDALONE_FLAG = "--catmap-standalone";

    private BetaCli() {}

    public static Options parse(String[] args) {
        boolean version = false;
        boolean selfTest = false;
        boolean standalone = false;
        List<String> rest = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                String normalized = arg == null ? "" : arg.trim().toLowerCase(Locale.ROOT);
                switch (normalized) {
                    case VERSION_FLAG, "-v", "--beta-version" -> version = true;
                    case SELF_TEST_FLAG, "--beta-check", "--selftest" -> selfTest = true;
                    case CATMAP_STANDALONE_FLAG, "--catmap-standalone=true" -> standalone = true;
                    default -> {
                        if (arg != null) {
                            rest.add(arg);
                        }
                    }
                }
            }
        }
        return new Options(version, selfTest, standalone, rest.toArray(new String[0]));
    }

    /** Print the `--version` contract (single CLI console surface). */
    public static void printVersion(boolean catmap) {
        for (String line : versionLines(catmap)) {
            System.out.println(line);
        }
    }

    /** Run and print the self-test summary; returns the process exit code. */
    public static int printSelfTest() {
        return runSelfTest();
    }

    /** Lines printed by `--version` (product and, for standalone, CATMAP). */
    public static List<String> versionLines(boolean catmap) {
        List<String> lines = new ArrayList<>();
        lines.add(catmap ? AppVersion.catmapVersionText() : AppVersion.versionText());
        if (!catmap) {
            lines.add(AppVersion.CATMAP_PRODUCT + ": usa --catmap-standalone para abrirlo.");
        }
        return lines;
    }

    /** Classes that must be present for the beta capability set. */
    static List<String> capabilityClasses() {
        return List.of(
                "ar.com.catgis.layout.LayoutModel",
                "ar.com.catgis.catmap.GoldenMaster",
                "ar.com.catgis.catmap.workspace.DataWorkspaceModel",
                "ar.com.catgis.catmap.OgcServiceSupport",
                "ar.com.catgis.catmap.IdwInterpolator",
                "ar.com.catgis.catmap.ViewshedRaster",
                "ar.com.catgis.catmap.VolumeCalculator",
                "ar.com.catgis.catmap.TerrainProfile",
                "ar.com.catgis.DrainageExtractionService",
                "ar.com.catgis.climate.RiskAnalysisEngine");
    }

    /**
     * Run the headless self-test. Prints one line per check plus the summary and
     * returns 0 (all checks passed) or 1 (any failure). Never opens a window.
     */
    public static int runSelfTest() {
        int checks = 0;
        int failed = 0;
        System.out.println("SELFTEST " + AppVersion.versionText());
        if (!AppVersion.VERSION.matches("\\d+\\.\\d+\\.\\d+-beta\\.\\d+")) {
            failed++;
            System.out.println("SELFTEST_FAIL version-format " + AppVersion.VERSION);
        } else {
            checks++;
            System.out.println("SELFTEST_OK version-format " + AppVersion.VERSION);
        }
        if (!AppVersion.isBeta()) {
            failed++;
            System.out.println("SELFTEST_FAIL channel " + AppVersion.CHANNEL);
        } else {
            checks++;
            System.out.println("SELFTEST_OK channel " + AppVersion.CHANNEL);
        }
        for (String className : capabilityClasses()) {
            try {
                Class.forName(className, false, BetaCli.class.getClassLoader());
                checks++;
                System.out.println("SELFTEST_OK class " + className);
            } catch (Throwable missing) {
                failed++;
                System.out.println("SELFTEST_FAIL class " + className + " ("
                        + missing.getClass().getSimpleName() + ")");
            }
        }
        checks++;
        System.out.println("SELFTEST_OK headless java.awt.headless="
                + Boolean.getBoolean("java.awt.headless"));
        if (failed == 0) {
            System.out.println("SELFTEST_OK checks=" + checks);
            return 0;
        }
        System.out.println("SELFTEST_FAIL checks=" + checks + " failed=" + failed);
        return 1;
    }
}
