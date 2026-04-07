package ar.com.catgis;

import java.util.prefs.Preferences;

public final class OpenTopographySettings {

    private static final Preferences ROOT = Preferences.userNodeForPackage(OpenTopographySettings.class).node("opentopography");
    private static final String API_KEY = "apiKey";

    private OpenTopographySettings() {
    }

    public static String getApiKey() {
        return ROOT.get(API_KEY, "").trim();
    }

    public static void setApiKey(String apiKey) {
        ROOT.put(API_KEY, apiKey != null ? apiKey.trim() : "");
    }
}
