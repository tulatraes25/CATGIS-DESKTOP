package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class VectorAttributeSupport {

    private VectorAttributeSupport() {
    }

    static String normalizeFieldName(String requestedName) {
        String base = requestedName != null ? requestedName.trim() : "";
        if (base.isBlank()) {
            return "";
        }
        String normalized = base
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}_]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");
        if (normalized.isBlank()) {
            normalized = "campo";
        }
        if (Character.isDigit(normalized.charAt(0))) {
            normalized = "f_" + normalized;
        }
        return normalized;
    }

    static boolean hasAttribute(ShapefileData data, String fieldName) {
        if (data == null || data.getSchema() == null || fieldName == null || fieldName.isBlank()) {
            return false;
        }
        return data.getSchema().getDescriptor(fieldName) != null;
    }

    static ShapefileData addField(ShapefileData data, String fieldName, String typeName) throws Exception {
        if (data == null || data.getSchema() == null) {
            throw new IllegalArgumentException("La capa no tiene esquema disponible.");
        }
        if (fieldName == null || fieldName.isBlank()) {
            throw new IllegalArgumentException("El nombre del campo no puede estar vacio.");
        }
        if (hasAttribute(data, fieldName)) {
            throw new IllegalArgumentException("Ya existe un campo llamado '" + fieldName + "'.");
        }

        SimpleFeatureType sourceType = data.getSchema();
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(sourceType.getTypeName() != null ? sourceType.getTypeName() : "layer");
        if (sourceType.getCoordinateReferenceSystem() != null) {
            builder.setCRS(sourceType.getCoordinateReferenceSystem());
        }

        for (AttributeDescriptor descriptor : sourceType.getAttributeDescriptors()) {
            if (descriptor instanceof GeometryDescriptor geometryDescriptor) {
                Class<?> geometryBinding = geometryDescriptor.getType() != null
                        ? geometryDescriptor.getType().getBinding()
                        : Object.class;
                builder.add(geometryDescriptor.getLocalName(), geometryBinding);
            } else {
                Class<?> binding = descriptor.getType() != null
                        ? descriptor.getType().getBinding()
                        : String.class;
                builder.add(descriptor.getLocalName(), binding != null ? binding : String.class);
            }
        }

        Class<?> newBinding = DrawFeatureBuilder.resolveAttributeClass(typeName);
        builder.add(fieldName, newBinding != null ? newBinding : String.class);
        SimpleFeatureType targetType = builder.buildFeatureType();

        List<SimpleFeature> rebuilt = new ArrayList<>();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(targetType);
        for (SimpleFeature feature : data.getFeatures()) {
            for (AttributeDescriptor descriptor : sourceType.getAttributeDescriptors()) {
                featureBuilder.set(descriptor.getLocalName(), feature.getAttribute(descriptor.getLocalName()));
            }
            featureBuilder.set(fieldName, null);
            rebuilt.add(featureBuilder.buildFeature(feature.getID()));
            featureBuilder.reset();
        }

        return new ShapefileData(
                rebuilt,
                data.getEnvelope(),
                data.getSourceName(),
                rebuilt.size(),
                data.getMessage(),
                targetType
        );
    }

    static List<String> resolveVisibleAttributeNames(Layer layer, ShapefileData data) {
        List<String> names = new ArrayList<>();
        if (data == null || data.getSchema() == null) {
            return names;
        }

        for (AttributeDescriptor descriptor : data.getSchema().getAttributeDescriptors()) {
            if (descriptor instanceof GeometryDescriptor) {
                continue;
            }
            String name = descriptor.getLocalName();
            FieldConfig config = layer != null
                    ? layer.getOrCreateFieldConfig(name, descriptor.getType() != null && descriptor.getType().getBinding() != null
                    ? descriptor.getType().getBinding().getSimpleName()
                    : "String")
                    : null;
            if (config == null || config.isVisible()) {
                names.add(name);
            }
        }

        return names;
    }

    static String buildUniqueFieldName(ShapefileData data, String requestedName) {
        String normalized = normalizeFieldName(requestedName);
        if (normalized.isBlank()) {
            normalized = "campo";
        }
        if (!hasAttribute(data, normalized)) {
            return normalized;
        }

        String base = normalized;
        int suffix = 2;
        while (hasAttribute(data, base + "_" + suffix)) {
            suffix++;
        }
        return base + "_" + suffix;
    }
}
