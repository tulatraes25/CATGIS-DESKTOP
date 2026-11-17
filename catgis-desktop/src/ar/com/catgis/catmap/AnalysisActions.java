package ar.com.catgis.catmap;

/**
 * Analysis entry points for CATMAP standalone (REQ-BETA-4). Thin facade over the
 * already certified CATGIS dialogs so the menu stays declarative and testable.
 */
public final class AnalysisActions {

    private AnalysisActions() {}

    public static void openContours() {
        ar.com.catgis.ContourGenerationDialog.open();
    }

    public static void openDrainage() {
        ar.com.catgis.DrainageExtractionDialog.open();
    }

    public static void openBasins() {
        ar.com.catgis.BasinFromOutletDialog.open();
    }

    public static void openFlood() {
        ar.com.catgis.FloodScenarioDialog.open();
    }

    public static void openRisk() {
        ar.com.catgis.BooleanRiskDialog.open();
    }

    public static void openViewshed() {
        ar.com.catgis.ViewshedDialog.open();
    }

    public static void openProfile() {
        ar.com.catgis.TopographicProfileDialog.open();
    }
}
