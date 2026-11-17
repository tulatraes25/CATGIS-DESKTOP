package ar.com.catgis;

/** Product identity and release channel for the first usable beta. */
public final class AppVersion {

    /** Semantic version of the beta (X.Y.Z-beta.N). */
    public static final String VERSION = "1.0.0-beta.1";
    /** Release channel shown in CLI and documentation. */
    public static final String CHANNEL = "beta";
    public static final String PRODUCT = "CATGIS Desktop";
    public static final String CATMAP_PRODUCT = "CATMAP";
    public static final String VENDOR = "CATGIS";
    public static final int YEAR = 2026;

    private AppVersion() {}

    public static boolean isBeta() {
        return "beta".equals(CHANNEL);
    }

    /** One-line CATGIS identity, used by `--version`. */
    public static String versionText() {
        return PRODUCT + " " + VERSION + " (" + CHANNEL + ")";
    }

    /** One-line CATMAP identity, used by the standalone launcher. */
    public static String catmapVersionText() {
        return CATMAP_PRODUCT + " " + VERSION + " (" + CHANNEL + ")"
                + " — parte de " + PRODUCT;
    }

    /** About dialog body for CATMAP (no hard-coded version literals). */
    public static String catmapAboutText() {
        return CATMAP_PRODUCT + " — Cartographic Layout Composer\n"
                + "Version " + VERSION + " (" + CHANNEL + ")\n\n"
                + "Parte de " + PRODUCT + "\n"
                + "Copyright " + YEAR + "\n\n"
                + "Herramienta de composicion cartografica\n"
                + "para mapas tecnicos y ambientales.";
    }
}
