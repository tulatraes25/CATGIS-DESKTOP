package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class CatserverLayerStyleSupport {

    private static final Color DEFAULT_LINE_COLOR = Color.RED;
    private static final float DEFAULT_LINE_WIDTH = 1.5f;
    private static final float EPSILON = 0.0001f;

    private CatserverLayerStyleSupport() {
    }

    public static void applyIfNeeded(Layer layer, ShapefileData data) {
        if (!(layer instanceof PostgisLayer postgisLayer) || data == null) {
            return;
        }
        if (!isCatserverConnection(postgisLayer)) {
            return;
        }
        applyInfraestructuraLineasSymbology(postgisLayer, data);
    }

    private static void applyInfraestructuraLineasSymbology(PostgisLayer layer, ShapefileData data) {
        if (!matchesOperativeLineasLayer(layer) || layer.getLineCategorizedSymbology().isConfigured()) {
            return;
        }
        if (!hasDefaultLineAppearance(layer)) {
            return;
        }

        String nombreField = resolveNombreField(data.getSchema());
        if (nombreField.isBlank()) {
            return;
        }

        Set<String> categories = collectCategories(data, nombreField);
        if (categories.isEmpty()) {
            return;
        }

        layer.setLineColor(new Color(88, 101, 122));
        layer.setLineWidth(1.45f);
        layer.setLineSymbolStyle(Layer.LineSymbolStyle.SOLID);

        CategorizedSymbology symbology = layer.getLineCategorizedSymbology();
        symbology.clearRules();
        symbology.setFieldName(nombreField);
        symbology.setLegendTitle("Infraestructura electrica");
        symbology.setLegendSubtitle("Lineas CATSERVER por nombre");

        for (String category : categories) {
            applyRuleStyle(symbology.getOrCreateRule(category), category);
        }
    }

    private static boolean isCatserverConnection(PostgisLayer layer) {
        return "catserver".equalsIgnoreCase(safe(layer.getDatabaseName()));
    }

    private static boolean matchesOperativeLineasLayer(PostgisLayer layer) {
        String schema = safe(layer.getSchemaName()).toLowerCase(Locale.ROOT);
        String table = safe(layer.getTableName()).toLowerCase(Locale.ROOT);
        String name = safe(layer.getName()).toLowerCase(Locale.ROOT);
        String typeName = safe(layer.getTypeName()).toLowerCase(Locale.ROOT);
        if ("gobierno_modernizacion".equals(schema) && "redes_servicios_publicos_lineas".equals(table)) {
            return true;
        }
        if ("ingenieria".equals(schema) && "lineas".equals(table)) {
            return true;
        }
        if ("infraestructura".equals(schema) && table.startsWith("lineas_shape_")) {
            return true;
        }
        if (name.contains("infraestructura - lineas")) {
            return true;
        }
        if (name.contains("redes y servicios publicos - lineas")) {
            return true;
        }
        if (name.contains("ingenieria - lineas")) {
            return true;
        }
        return typeName.contains("infraestructura.lineas_shape_")
                || typeName.contains("ingenieria.lineas")
                || typeName.contains("gobierno_modernizacion.redes_servicios_publicos_lineas");
    }

    private static boolean hasDefaultLineAppearance(Layer layer) {
        return colorsEqual(layer.getLineColor(), DEFAULT_LINE_COLOR)
                && Math.abs(layer.getLineWidth() - DEFAULT_LINE_WIDTH) < EPSILON
                && layer.getLineSymbolStyle() == Layer.LineSymbolStyle.SOLID;
    }

    private static String resolveNombreField(SimpleFeatureType schema) {
        if (schema == null) {
            return "";
        }
        String resolved = FeatureAttributeResolver.resolveFieldName(schema, "nombre");
        for (String attributeName : schema.getAttributeDescriptors().stream()
                .map(descriptor -> descriptor != null ? descriptor.getLocalName() : "")
                .toList()) {
            if (attributeName != null && attributeName.equalsIgnoreCase(resolved)) {
                return resolved;
            }
        }
        return "";
    }

    private static Set<String> collectCategories(ShapefileData data, String fieldName) {
        Set<String> categories = new LinkedHashSet<>();
        for (SimpleFeature feature : data.getFeatures()) {
            if (feature == null) {
                continue;
            }
            Object value = FeatureAttributeResolver.resolveAttribute(feature, fieldName);
            categories.add(CategorizedSymbology.valueKey(value != null ? String.valueOf(value) : null));
        }
        return categories;
    }

    private static void applyRuleStyle(CategoryStyleRule rule, String value) {
        if (rule == null) {
            return;
        }
        String normalized = safe(value).toUpperCase(Locale.ROOT);
        Color color;
        float width;
        Layer.LineSymbolStyle style;

        if (normalized.contains("FRANJA") && normalized.contains("I.P.A")) {
            color = new Color(153, 27, 27);
            width = 2.2f;
            style = Layer.LineSymbolStyle.DASH_DOT;
        } else if (normalized.contains("500 KV") || normalized.contains("TRANSPA")) {
            color = new Color(31, 41, 55);
            width = 3.6f;
            style = Layer.LineSymbolStyle.SOLID;
        } else if (normalized.contains("35 KV") || normalized.contains("33000")) {
            color = normalized.contains("OXI")
                    ? new Color(180, 83, 9)
                    : normalized.contains("TORDILLO")
                    ? new Color(146, 64, 14)
                    : new Color(136, 19, 55);
            width = 2.7f;
            style = normalized.contains("33000")
                    ? Layer.LineSymbolStyle.SOLID
                    : Layer.LineSymbolStyle.DASHED;
        } else if (normalized.contains("13200")) {
            color = new Color(30, 64, 175);
            width = 2.05f;
            style = Layer.LineSymbolStyle.SOLID;
        } else if (normalized.contains("10400")) {
            color = new Color(21, 128, 61);
            width = 1.8f;
            style = Layer.LineSymbolStyle.DASHED;
        } else if (normalized.contains("1100")) {
            color = new Color(180, 83, 9);
            width = 1.3f;
            style = Layer.LineSymbolStyle.DOTTED;
        } else if (normalized.contains("LINEAS ELECTRICAS")) {
            color = new Color(100, 116, 139);
            width = 1.1f;
            style = Layer.LineSymbolStyle.DASHED;
        } else {
            color = new Color(71, 85, 105);
            width = 1.45f;
            style = Layer.LineSymbolStyle.SOLID;
        }

        rule.setPrimaryColor(color);
        rule.setSecondaryColor(color);
        rule.setLineWidth(width);
        rule.setLineStyle(style);
    }

    private static boolean colorsEqual(Color left, Color right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.getRed() == right.getRed()
                && left.getGreen() == right.getGreen()
                && left.getBlue() == right.getBlue()
                && left.getAlpha() == right.getAlpha();
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
