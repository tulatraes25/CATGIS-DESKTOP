package ar.com.catgis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CatmapLegendSupport {

    private CatmapLegendSupport() {
    }

    public static List<CatmapLegendItem> buildAutomaticEntries(List<Layer> layers) {
        List<CatmapLegendItem> items = new ArrayList<>();
        if (layers == null) {
            return items;
        }

        for (Layer layer : layers) {
            if (layer == null) {
                continue;
            }
            if (layer.getPointCategorizedSymbology().isConfigured()) {
                for (CategoryStyleRule rule : layer.getPointCategorizedSymbology().getRules().values()) {
                    items.add(new CatmapLegendItem(
                            buildKey(layer, rule, "POINT"),
                            rule.getValue(),
                            layer.getName(),
                            isLegendVisibleByDefault(layer)
                    ));
                }
                continue;
            }
            if (layer.getLineCategorizedSymbology().isConfigured()) {
                for (CategoryStyleRule rule : layer.getLineCategorizedSymbology().getRules().values()) {
                    items.add(new CatmapLegendItem(
                            buildKey(layer, rule, "LINE"),
                            rule.getValue(),
                            layer.getName(),
                            isLegendVisibleByDefault(layer)
                    ));
                }
                continue;
            }
            if (layer.getPolygonCategorizedSymbology().isConfigured()) {
                for (CategoryStyleRule rule : layer.getPolygonCategorizedSymbology().getRules().values()) {
                    items.add(new CatmapLegendItem(
                            buildKey(layer, rule, "POLYGON"),
                            rule.getValue(),
                            layer.getName(),
                            isLegendVisibleByDefault(layer)
                    ));
                }
                continue;
            }
            items.add(new CatmapLegendItem(
                    buildKey(layer, null, resolveLegendGeometryType(layer)),
                    layer.getName(),
                    resolveLayerTypeLabel(layer),
                    isLegendVisibleByDefault(layer)
            ));
        }
        return items;
    }

    public static boolean isLegendVisibleByDefault(Layer layer) {
        return !(layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer);
    }

    public static List<CatmapLegendItem> mergeEntries(List<CatmapLegendItem> automaticEntries, List<CatmapLegendItem> overrides) {
        List<CatmapLegendItem> merged = new ArrayList<>();
        Map<String, CatmapLegendItem> automaticByKey = new LinkedHashMap<>();
        if (automaticEntries != null) {
            for (CatmapLegendItem item : automaticEntries) {
                if (item != null && !item.getKey().isBlank()) {
                    automaticByKey.put(item.getKey(), new CatmapLegendItem(item));
                }
            }
        }
        if (overrides != null) {
            for (CatmapLegendItem override : overrides) {
                if (override == null || override.getKey().isBlank()) {
                    continue;
                }
                CatmapLegendItem automatic = automaticByKey.remove(override.getKey());
                if (automatic == null) {
                    continue;
                }
                CatmapLegendItem item = new CatmapLegendItem(override);
                if (item.getLabel().isBlank()) {
                    item.setLabel(automatic.getLabel());
                }
                if (item.getSubtitle().isBlank()) {
                    item.setSubtitle(automatic.getSubtitle());
                }
                merged.add(item);
            }
        }
        merged.addAll(automaticByKey.values());
        return merged;
    }

    public static String buildKey(Layer layer, CategoryStyleRule categoryRule, String geometryType) {
        String type = normalizeToken(geometryType);
        String layerType = normalizeToken(layer != null ? layer.getType() : "");
        String path = normalizeToken(layer != null ? layer.getPath() : "");
        String name = normalizeToken(layer != null ? layer.getName() : "");
        String rule = normalizeToken(categoryRule != null ? categoryRule.getValue() : "");
        return type + "|" + layerType + "|" + path + "|" + name + "|" + rule;
    }

    public static String resolveLegendGeometryType(Layer layer) {
        if (layer instanceof RasterLayer || layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer) {
            return "RASTER";
        }
        if (layer instanceof GpxLayer gpxLayer) {
            return switch (gpxLayer.getContentKind().getGeometryFamily()) {
                case "POINT" -> "POINT";
                case "LINE" -> "LINE";
                default -> "VECTOR";
            };
        }
        ShapefileData data = CatgisDesktopApp.mapPanel != null ? CatgisDesktopApp.mapPanel.getShapefileData(layer) : null;
        String family = VectorLayerUtils.resolveGeometryFamily(data);
        if (family != null && !family.isBlank()) {
            return family.toUpperCase();
        }
        String type = layer != null ? layer.getType() : "";
        return type != null && !type.isBlank() ? type.toUpperCase() : "VECTOR";
    }

    public static String resolveLayerTypeLabel(Layer layer) {
        if (layer == null) {
            return "Capa";
        }
        if (layer instanceof RasterLayer) {
            return "Raster";
        }
        if (layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer) {
            return "Mapa base online";
        }
        if (layer instanceof GpxLayer gpxLayer) {
            return "GPX " + gpxLayer.getContentKind().getLabel();
        }
        String geometryFamily = VectorLayerUtils.resolveGeometryFamily(
                CatgisDesktopApp.mapPanel != null ? CatgisDesktopApp.mapPanel.getShapefileData(layer) : null
        );
        if ("POINT".equalsIgnoreCase(geometryFamily)) {
            return "Punto";
        }
        if ("LINE".equalsIgnoreCase(geometryFamily)) {
            return "Linea";
        }
        if ("POLYGON".equalsIgnoreCase(geometryFamily)) {
            return "Poligono";
        }
        String type = layer.getType();
        return type != null && !type.isBlank() ? type : "Vector";
    }

    private static String normalizeToken(String value) {
        if (value == null || value.isBlank()) {
            return "_";
        }
        return value.trim().replace('|', '_');
    }
}
