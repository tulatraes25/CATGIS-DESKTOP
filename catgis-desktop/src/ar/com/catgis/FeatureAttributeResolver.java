package ar.com.catgis;

import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;

public final class FeatureAttributeResolver {

    private FeatureAttributeResolver() {
    }

    public static Object resolveAttribute(SimpleFeature feature, String fieldName) {
        if (feature == null || fieldName == null || fieldName.isBlank()) {
            return null;
        }

        Object direct = safeGetAttribute(feature, fieldName);
        if (direct != null) {
            return direct;
        }

        AttributeDescriptor descriptor = findDescriptor(feature.getFeatureType(), fieldName);
        if (descriptor != null) {
            Object byDescriptor = safeGetAttribute(feature, descriptor.getLocalName());
            if (byDescriptor != null) {
                return byDescriptor;
            }
        }

        for (Property property : feature.getProperties()) {
            if (property == null || property.getName() == null) {
                continue;
            }
            String candidate = property.getName().getLocalPart();
            if (matches(candidate, fieldName)) {
                return property.getValue();
            }
        }
        return null;
    }

    public static String resolveFieldName(SimpleFeatureType schema, String requestedName) {
        if (schema == null || requestedName == null || requestedName.isBlank()) {
            return requestedName != null ? requestedName.trim() : "";
        }
        AttributeDescriptor descriptor = findDescriptor(schema, requestedName);
        return descriptor != null ? descriptor.getLocalName() : requestedName.trim();
    }

    private static AttributeDescriptor findDescriptor(SimpleFeatureType schema, String requestedName) {
        if (schema == null || requestedName == null || requestedName.isBlank()) {
            return null;
        }
        for (AttributeDescriptor descriptor : schema.getAttributeDescriptors()) {
            if (descriptor == null) {
                continue;
            }
            if (matches(descriptor.getLocalName(), requestedName)) {
                return descriptor;
            }
            if (descriptor.getName() != null && matches(descriptor.getName().getLocalPart(), requestedName)) {
                return descriptor;
            }
        }
        return null;
    }

    private static Object safeGetAttribute(SimpleFeature feature, String fieldName) {
        try {
            return feature.getAttribute(fieldName);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean matches(String candidate, String requestedName) {
        if (candidate == null || requestedName == null) {
            return false;
        }
        String normalizedCandidate = normalize(candidate);
        String normalizedRequested = normalize(requestedName);
        return !normalizedCandidate.isBlank() && normalizedCandidate.equalsIgnoreCase(normalizedRequested);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        int colon = trimmed.lastIndexOf(':');
        if (colon >= 0 && colon < trimmed.length() - 1) {
            trimmed = trimmed.substring(colon + 1);
        }
        return trimmed;
    }
}
