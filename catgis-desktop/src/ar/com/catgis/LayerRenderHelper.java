package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;

/**
 * Shared rendering logic for MapPanel and MapFrameRenderer.
 * Eliminates code duplication between Swing and standalone render paths.
 */
public final class LayerRenderHelper {

    private LayerRenderHelper() {}

    /**
     * Resolve graduated symbology rule for a feature.
     */
    public static GraduatedRangeRule resolveGraduatedRule(GraduatedSymbology symbology, SimpleFeature feature) {
        if (symbology == null || !symbology.isConfigured() || feature == null) return null;
        try {
            String fieldName = symbology.getFieldName();
            if (fieldName == null || fieldName.isBlank()) return null;
            Object v = resolveAttribute(feature, fieldName);
            if (v == null) return null;
            double value = Double.parseDouble(String.valueOf(v));
            return symbology.getRuleForValue(value);
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Resolve categorized symbology rule for a feature.
     */
    public static CategoryStyleRule resolveCategoryRule(CategorizedSymbology symbology, SimpleFeature feature) {
        if (symbology == null || !symbology.isConfigured() || feature == null) return null;
        String fieldName = symbology.getFieldName();
        if (fieldName == null || fieldName.isBlank()) return null;
        Object value = resolveAttribute(feature, fieldName);
        return symbology.getRule(CategorizedSymbology.valueKey(value != null ? String.valueOf(value) : null));
    }

    /**
     * Resolve best matching style rule: graduated takes priority over categorized.
     */
    public static CategoryStyleRule resolveBestRule(Layer layer, SimpleFeature feature, String geomType) {
        if (layer == null || feature == null) return null;
        if ("point".equals(geomType) || "multipoint".equals(geomType)) {
            GraduatedRangeRule g = resolveGraduatedRule(layer.getPointGraduatedSymbology(), feature);
            if (g != null) return g;
            return resolveCategoryRule(layer.getPointCategorizedSymbology(), feature);
        }
        if ("line".equals(geomType) || "multilinestring".equals(geomType)) {
            GraduatedRangeRule g = resolveGraduatedRule(layer.getLineGraduatedSymbology(), feature);
            if (g != null) return g;
            return resolveCategoryRule(layer.getLineCategorizedSymbology(), feature);
        }
        if ("polygon".equals(geomType) || "multipolygon".equals(geomType)) {
            GraduatedRangeRule g = resolveGraduatedRule(layer.getPolygonGraduatedSymbology(), feature);
            if (g != null) return g;
            return resolveCategoryRule(layer.getPolygonCategorizedSymbology(), feature);
        }
        return null;
    }

    /**
     * Resolve point size including proportional symbols.
     */
    public static int resolveProportionalSize(Layer layer, SimpleFeature feature, int defaultSize) {
        ProportionalSymbols ps = layer != null ? layer.getProportionalSymbols() : null;
        if (ps == null || !ps.isConfigured() || feature == null) return Math.max(4, defaultSize);
        try {
            Object v = resolveAttribute(feature, ps.getFieldName());
            if (v == null) return Math.max(4, defaultSize);
            double value = Double.parseDouble(String.valueOf(v));
            return Math.max(4, ps.getSizeForValue(value));
        } catch (Exception ignored) {}
        return Math.max(4, defaultSize);
    }

    /**
     * Generic attribute resolution, compatible with both MapPanel and standalone contexts.
     * Delegates to FeatureAttributeResolver for robust attribute matching.
     */
    private static Object resolveAttribute(SimpleFeature feature, String fieldName) {
        if (feature == null || fieldName == null || fieldName.isBlank()) return null;
        // Hacemos catch a nivel local para evitar propagar excepciones del FeatureAttributeResolver
        try {
            return FeatureAttributeResolver.resolveAttribute(feature, fieldName);
        } catch (Exception ignored) {
            return null;
        }
    }
}
