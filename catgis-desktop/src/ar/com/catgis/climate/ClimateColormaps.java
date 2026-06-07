package ar.com.catgis.climate;
import ar.com.catgis.core.model.Layer;

import ar.com.catgis.MapPanel;
import ar.com.catgis.RasterLayer;

import java.awt.Color;

/**
 * Predefined colormaps for climate and environmental variable visualization.
 * Each colormap is a 256-entry Color[] array optimized for the variable's typical range.
 */
public final class ClimateColormaps {

    public enum ClimateVariable {
        TEMPERATURE("Temperatura"),
        PRECIPITATION("Precipitación"),
        WIND_SPEED("Velocidad del viento"),
        PRESSURE("Presión atmosférica"),
        CUSTOM("Personalizado");

        private final String label;
        ClimateVariable(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private ClimateColormaps() {}

    /**
     * Temperature colormap: blue -> cyan -> green -> yellow -> red.
     * Optimal range: -10 to 45 degrees Celsius.
     */
    public static Color[] createTemperatureColormap() {
        Color[] cmap = new Color[256];
        for (int i = 0; i < 256; i++) {
            float t = i / 255f;
            float r, g, b;
            if (t < 0.25f) {
                // dark blue -> blue
                float u = t / 0.25f;
                r = 0f;
                g = 60f / 255f + u * (80f / 255f);
                b = 140f / 255f + u * (115f / 255f);
            } else if (t < 0.5f) {
                // blue -> cyan -> green
                float u = (t - 0.25f) / 0.25f;
                r = 0f;
                g = 140f / 255f + u * (115f / 255f);
                b = 255f / 255f * (1f - u);
            } else if (t < 0.75f) {
                // green -> yellow
                float u = (t - 0.5f) / 0.25f;
                r = u * 255f / 255f;
                g = 1f;
                b = 0f;
            } else {
                // yellow -> red
                float u = (t - 0.75f) / 0.25f;
                r = 1f;
                g = 1f - u;
                b = 0f;
            }
            cmap[i] = new Color(clamp(r), clamp(g), clamp(b));
        }
        return cmap;
    }

    /**
     * Precipitation colormap: white -> light blue -> blue -> dark blue -> purple.
     * Optimal range: 0 to 500 mm.
     */
    public static Color[] createPrecipitationColormap() {
        Color[] cmap = new Color[256];
        for (int i = 0; i < 256; i++) {
            float t = i / 255f;
            float r, g, b;
            if (t < 0.2f) {
                // white -> very light blue
                float u = t / 0.2f;
                r = 1f - u * 0.15f;
                g = 1f - u * 0.1f;
                b = 1f;
            } else if (t < 0.45f) {
                // light blue -> medium blue
                float u = (t - 0.2f) / 0.25f;
                r = 0.85f - u * 0.45f;
                g = 0.9f - u * 0.4f;
                b = 1f;
            } else if (t < 0.7f) {
                // medium blue -> dark blue
                float u = (t - 0.45f) / 0.25f;
                r = 0.4f - u * 0.3f;
                g = 0.5f - u * 0.35f;
                b = 1f - u * 0.1f;
            } else {
                // dark blue -> purple
                float u = (t - 0.7f) / 0.3f;
                r = 0.1f + u * 0.5f;
                g = 0.15f + u * 0.1f;
                b = 0.9f - u * 0.3f;
            }
            cmap[i] = new Color(clamp(r), clamp(g), clamp(b));
        }
        return cmap;
    }

    /**
     * Wind speed colormap: white -> light green -> yellow -> orange -> red.
     * Optimal range: 0 to 30 m/s.
     */
    public static Color[] createWindSpeedColormap() {
        Color[] cmap = new Color[256];
        for (int i = 0; i < 256; i++) {
            float t = i / 255f;
            float r, g, b;
            if (t < 0.25f) {
                // white -> light green
                float u = t / 0.25f;
                r = 1f - u * 0.2f;
                g = 1f;
                b = 0.9f - u * 0.1f;
            } else if (t < 0.5f) {
                // light green -> yellow
                float u = (t - 0.25f) / 0.25f;
                r = 0.8f + u * 0.2f;
                g = 1f;
                b = 0.8f - u * 0.3f;
            } else if (t < 0.75f) {
                // yellow -> orange
                float u = (t - 0.5f) / 0.25f;
                r = 1f;
                g = 1f - u * 0.35f;
                b = 0.5f - u * 0.2f;
            } else {
                // orange -> red
                float u = (t - 0.75f) / 0.25f;
                r = 1f;
                g = 0.65f - u * 0.55f;
                b = 0.3f - u * 0.2f;
            }
            cmap[i] = new Color(clamp(r), clamp(g), clamp(b));
        }
        return cmap;
    }

    /**
     * Pressure colormap: dark purple -> blue -> green -> yellow.
     * Optimal range: 960 to 1050 hPa.
     */
    public static Color[] createPressureColormap() {
        Color[] cmap = new Color[256];
        for (int i = 0; i < 256; i++) {
            float t = i / 255f;
            float r, g, b;
            if (t < 0.25f) {
                // dark purple -> purple-blue
                float u = t / 0.25f;
                r = 0.4f - u * 0.15f;
                g = 0.1f + u * 0.2f;
                b = 0.5f + u * 0.1f;
            } else if (t < 0.5f) {
                // purple-blue -> blue
                float u = (t - 0.25f) / 0.25f;
                r = 0.25f - u * 0.2f;
                g = 0.3f + u * 0.2f;
                b = 0.6f + u * 0.3f;
            } else if (t < 0.75f) {
                // blue -> green
                float u = (t - 0.5f) / 0.25f;
                r = 0.05f + u * 0.1f;
                g = 0.5f + u * 0.3f;
                b = 0.9f - u * 0.5f;
            } else {
                // green -> yellow
                float u = (t - 0.75f) / 0.25f;
                r = 0.15f + u * 0.7f;
                g = 0.8f + u * 0.15f;
                b = 0.4f - u * 0.35f;
            }
            cmap[i] = new Color(clamp(r), clamp(g), clamp(b));
        }
        return cmap;
    }

    /**
     * General-purpose terrain/elevation colormap: green -> brown -> white.
     */
    public static Color[] createTerrainColormap() {
        Color[] cmap = new Color[256];
        for (int i = 0; i < 256; i++) {
            float t = i / 255f;
            float r, g, b;
            if (t < 0.3f) {
                // dark green -> green
                float u = t / 0.3f;
                r = 0.1f + u * 0.2f;
                g = 0.3f + u * 0.4f;
                b = 0.1f + u * 0.1f;
            } else if (t < 0.6f) {
                // green -> brown
                float u = (t - 0.3f) / 0.3f;
                r = 0.3f + u * 0.4f;
                g = 0.7f - u * 0.3f;
                b = 0.2f - u * 0.1f;
            } else {
                // brown -> white
                float u = (t - 0.6f) / 0.4f;
                r = 0.7f + u * 0.3f;
                g = 0.4f + u * 0.5f;
                b = 0.1f + u * 0.8f;
            }
            cmap[i] = new Color(clamp(r), clamp(g), clamp(b));
        }
        return cmap;
    }

    /**
     * Apply the appropriate colormap to a RasterLayer based on the climate variable.
     * Sets the customColorMap on the layer's RasterStyle in MapPanel.
     */
    public static void applyToLayer(RasterLayer layer, ClimateVariable variable, MapPanel mapPanel) {
        if (layer == null || mapPanel == null) return;
        Color[] cmap;
        double minVal, maxVal;
        switch (variable) {
            case TEMPERATURE:
                cmap = createTemperatureColormap();
                minVal = -10; maxVal = 45;
                break;
            case PRECIPITATION:
                cmap = createPrecipitationColormap();
                minVal = 0; maxVal = 500;
                break;
            case WIND_SPEED:
                cmap = createWindSpeedColormap();
                minVal = 0; maxVal = 30;
                break;
            case PRESSURE:
                cmap = createPressureColormap();
                minVal = 960; maxVal = 1050;
                break;
            default:
                cmap = createTemperatureColormap();
                minVal = 0; maxVal = 100;
                break;
        }

        int bandCount = 1;
        var data = mapPanel.getRasterData(layer);
        if (data != null) bandCount = Math.max(1, data.getBandCount());

        MapPanel.RasterStyle style = mapPanel.getOrCreateRasterStyle(layer, bandCount);
        style.customColorMap = cmap;
        style.colorMapMin = minVal;
        style.colorMapMax = maxVal;
        style.grayscale = false;
        style.autoContrast = false;
        mapPanel.repaint();
    }

    /**
     * Suggest a climate variable type based on a NetCDF variable name.
     */
    public static ClimateVariable getSuggestedVariable(String variableName) {
        if (variableName == null) return ClimateVariable.CUSTOM;
        String v = variableName.toLowerCase();

        if (v.contains("temp") || v.contains("t2m") || v.contains("t_2m")
                || v.contains("skt") || v.contains("air") || v.contains("sst")
                || v.contains("tsoil") || v.contains("t_soil"))
            return ClimateVariable.TEMPERATURE;

        if (v.contains("precip") || v.contains("rain") || v.contains("tp")
                || v.contains("prate") || v.contains("prcp") || v.contains("total_precip")
                || v.contains("apcp") || v.contains("snow") || v.contains("lwe"))
            return ClimateVariable.PRECIPITATION;

        if (v.contains("wind") || v.contains("ws") || v.contains("wspd")
                || v.contains("u10") || v.contains("v10") || v.contains("ugrd")
                || v.contains("vgrd") || v.contains("si10"))
            return ClimateVariable.WIND_SPEED;

        if (v.contains("press") || v.contains("psfc") || v.contains("mslp")
                || v.contains("pmsl") || v.contains("sp") || v.contains("prmsl"))
            return ClimateVariable.PRESSURE;

        return ClimateVariable.CUSTOM;
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
