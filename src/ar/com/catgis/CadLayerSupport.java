package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class CadLayerSupport {

    private CadLayerSupport() {
    }

    public static boolean isCadLayer(Layer layer) {
        return layer != null && isCadPath(layer.getPath());
    }

    public static boolean isCadFile(File file) {
        return file != null && isCadPath(file.getName());
    }

    public static boolean isCadPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String lower = path.trim().toLowerCase(Locale.ROOT);
        return lower.endsWith(".dxf") || lower.endsWith(".dwg");
    }

    public static String formatSourceCrsLabel(String code) {
        String normalized = CRSDefinitions.normalizeCode(code);
        if (normalized == null || normalized.isBlank()) {
            return "Sin CRS definido";
        }
        return CRSDefinitions.getLabelForCode(normalized);
    }

    public static String describeCadReference(File file) {
        if (file == null) {
            return "Referencia CAD";
        }
        String path = file.getAbsolutePath();
        return file.getName() + "  [" + path + "]";
    }

    public static String buildCadAdjustmentLabel(Layer layer) {
        return CadPlacementSupport.buildPlacementSummary(layer);
    }

    public static String buildCadGeoreferenceLabel(Layer layer) {
        return CadGeoreferenceSupport.buildDetailedSummary(layer);
    }

    public static String buildCadInternalLayerFilterLabel(Layer layer) {
        if (layer == null || !isCadLayer(layer)) {
            return "No aplica";
        }
        if (!layer.hasCadInternalLayerFilter()) {
            return "Todas las capas CAD internas visibles";
        }
        int hiddenCount = layer.getCadHiddenInternalLayers().size();
        return hiddenCount == 1
                ? "1 capa CAD interna oculta"
                : hiddenCount + " capas CAD internas ocultas";
    }

    public static String extractCadLayerName(SimpleFeature feature) {
        if (feature == null) {
            return "";
        }
        Object value = feature.getAttribute("cad_layer");
        return normalizeCadLayerName(value != null ? value.toString() : "");
    }

    public static String extractCadEntityType(SimpleFeature feature) {
        if (feature == null) {
            return "";
        }
        Object value = feature.getAttribute("entity_type");
        return value != null ? value.toString().trim() : "";
    }

    public static boolean isCadFeatureVisible(Layer layer, SimpleFeature feature) {
        if (!isCadLayer(layer) || feature == null) {
            return true;
        }
        return layer.isCadInternalLayerVisible(extractCadLayerName(feature));
    }

    public static String encodeCadLayerNames(Collection<String> names) {
        if (names == null || names.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            if (name == null) {
                continue;
            }
            String trimmed = name.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append(trimmed);
        }
        if (sb.isEmpty()) {
            return "";
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static List<String> decodeCadLayerNames(String encoded) {
        List<String> names = new ArrayList<>();
        if (encoded == null || encoded.isBlank()) {
            return names;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(encoded.trim()), StandardCharsets.UTF_8);
            for (String line : decoded.split("\\R")) {
                if (line != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isBlank() && !names.contains(trimmed)) {
                        names.add(trimmed);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return names;
    }

    public static List<InternalCadLayerInfo> buildInternalLayerInfo(ShapefileData data, Layer layer) {
        Map<String, MutableCadLayerInfo> stats = new LinkedHashMap<>();
        if (data == null || data.getFeatures() == null) {
            return List.of();
        }
        for (SimpleFeature feature : data.getFeatures()) {
            if (feature == null) {
                continue;
            }
            String cadLayerName = extractCadLayerName(feature);
            MutableCadLayerInfo info = stats.computeIfAbsent(cadLayerName, key -> new MutableCadLayerInfo());
            info.featureCount++;

            String entityType = extractCadEntityType(feature);
            if (!entityType.isBlank()) {
                info.entityTypes.add(entityType);
                if ("TEXT".equalsIgnoreCase(entityType) || "MTEXT".equalsIgnoreCase(entityType)) {
                    info.textCount++;
                }
            }
        }

        List<InternalCadLayerInfo> result = new ArrayList<>();
        for (Map.Entry<String, MutableCadLayerInfo> entry : stats.entrySet()) {
            MutableCadLayerInfo info = entry.getValue();
            String entitySummary = info.entityTypes.isEmpty() ? "-" : String.join(", ", info.entityTypes);
            result.add(new InternalCadLayerInfo(
                    entry.getKey(),
                    info.featureCount,
                    info.textCount,
                    entitySummary,
                    layer == null || layer.isCadInternalLayerVisible(entry.getKey())
            ));
        }
        return result;
    }

    private static String normalizeCadLayerName(String raw) {
        String trimmed = raw != null ? raw.trim() : "";
        return trimmed.isBlank() ? "(sin capa CAD)" : trimmed;
    }

    public record InternalCadLayerInfo(String name,
                                       int featureCount,
                                       int textCount,
                                       String entityTypes,
                                       boolean visible) {
    }

    private static final class MutableCadLayerInfo {
        private int featureCount = 0;
        private int textCount = 0;
        private final Set<String> entityTypes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    }
}
