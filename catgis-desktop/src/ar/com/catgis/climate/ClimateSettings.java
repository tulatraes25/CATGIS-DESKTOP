package ar.com.catgis.climate;

import java.util.prefs.Preferences;

/**
 * Climate download settings, stored via Preferences.
 * Follows the same pattern as {@link ar.com.catgis.OpenTopographySettings}.
 * Prepared for future providers that may require API keys.
 */
public final class ClimateSettings {

    private static final Preferences ROOT = Preferences.userNodeForPackage(ClimateSettings.class).node("climate");
    private static final String WORLDCLEAN_API_KEY = "worldcleanApiKey";
    private static final String NASA_EARTHDATA_TOKEN = "nasaEarthdataToken";

    private ClimateSettings() {
    }

    /** WorldClean API key (not currently required, prepared for future use) */
    public static String getWorldCleanApiKey() {
        return ROOT.get(WORLDCLEAN_API_KEY, "").trim();
    }

    public static void setWorldCleanApiKey(String apiKey) {
        ROOT.put(WORLDCLEAN_API_KEY, apiKey != null ? apiKey.trim() : "");
    }

    /** NASA Earthdata token (prepared for future GIBS/LP DAAC access) */
    public static String getNasaEarthdataToken() {
        return ROOT.get(NASA_EARTHDATA_TOKEN, "").trim();
    }

    public static void setNasaEarthdataToken(String token) {
        ROOT.put(NASA_EARTHDATA_TOKEN, token != null ? token.trim() : "");
    }
}
