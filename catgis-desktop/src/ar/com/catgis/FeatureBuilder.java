package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Pure computational feature clone/build/merge utilities.
 * All methods are static and stateless — no dependency on MapPanel fields.
 */
class FeatureBuilder {

    private FeatureBuilder() {
    }

    // -----------------------------------------------------------------------
    // clone / build
    // -----------------------------------------------------------------------

    static SimpleFeature cloneFeature(SimpleFeature sourceFeature, Geometry geometry, String featureId) {
        if (sourceFeature == null) {
            return null;
        }

        SimpleFeatureType featureType = sourceFeature.getFeatureType();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
        int attributeCount = sourceFeature.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            Object value = sourceFeature.getAttribute(i);
            if (value instanceof Geometry) {
                builder.add(geometry != null ? geometry : ((Geometry) value).copy());
            } else {
                builder.add(value);
            }
        }
        return builder.buildFeature(featureId != null ? featureId : sourceFeature.getID());
    }

    static List<SimpleFeature> cloneFeatureList(List<SimpleFeature> features) {
        List<SimpleFeature> clones = new ArrayList<>();
        if (features == null) {
            return clones;
        }
        for (SimpleFeature feature : features) {
            if (feature != null) {
                clones.add(cloneFeature(feature,
                        MapGeometryUtils.extractFeatureGeometryCopy(feature),
                        feature.getID()));
            }
        }
        return clones;
    }

    static SimpleFeature buildNewFeatureForLayer(ShapefileData targetData,
                                                  Geometry geometry,
                                                  List<SimpleFeature> existingFeatures) {
        if (targetData == null || targetData.getSchema() == null || geometry == null) {
            return null;
        }

        SimpleFeatureType targetType = targetData.getSchema();
        Geometry adaptedGeometry = adaptGeometryForFeatureSchema(geometry, targetType);
        if (adaptedGeometry == null || adaptedGeometry.isEmpty()) {
            return null;
        }

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(targetType);
        for (int i = 0; i < targetType.getAttributeCount(); i++) {
            String attrName = targetType.getDescriptor(i).getLocalName();
            if (targetType.getDescriptor(i).equals(targetType.getGeometryDescriptor())) {
                builder.add(adaptedGeometry);
            } else {
                builder.add(defaultValueForAttribute(targetType, attrName));
            }
        }

        return builder.buildFeature(MapGeometryUtils.buildNextFeatureId(existingFeatures));
    }

    static SimpleFeature buildDerivedFeatureForLayer(ShapefileData targetData,
                                                      Geometry geometry,
                                                      List<SimpleFeature> existingFeatures,
                                                      SimpleFeature sourceFeature) {
        if (targetData == null || targetData.getSchema() == null || geometry == null) {
            return null;
        }

        Geometry adaptedGeometry = adaptGeometryForFeatureSchema(geometry, targetData.getSchema());
        if (adaptedGeometry == null || adaptedGeometry.isEmpty()) {
            return null;
        }

        if (sourceFeature != null && sourceFeature.getFeatureType() != null
                && targetData.getSchema().equals(sourceFeature.getFeatureType())) {
            return cloneFeature(sourceFeature, adaptedGeometry,
                    MapGeometryUtils.buildNextFeatureId(existingFeatures));
        }

        return buildNewFeatureForLayer(targetData, adaptedGeometry, existingFeatures);
    }

    static Object defaultValueForAttribute(SimpleFeatureType featureType, String attributeName) {
        if (featureType == null || attributeName == null
                || featureType.getDescriptor(attributeName) == null) {
            return null;
        }

        Class<?> binding = featureType.getDescriptor(attributeName).getType() != null
                ? featureType.getDescriptor(attributeName).getType().getBinding()
                : null;

        if (binding == null) {
            return null;
        }
        if (String.class.isAssignableFrom(binding)) {
            return "";
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // replace / merge
    // -----------------------------------------------------------------------

    static List<SimpleFeature> buildReplacementFeatures(SimpleFeature sourceFeature,
                                                         List<Geometry> replacementParts) {
        List<SimpleFeature> features = new ArrayList<>();
        if (sourceFeature == null || replacementParts == null) {
            return features;
        }
        int index = 0;
        for (Geometry part : replacementParts) {
            Geometry adapted = adaptGeometryForFeatureSchema(part, sourceFeature.getFeatureType());
            if (adapted == null || adapted.isEmpty()) {
                continue;
            }
            String featureId = index == 0
                    ? sourceFeature.getID()
                    : sourceFeature.getID() + "_part_" + index;
            features.add(cloneFeature(sourceFeature, adapted, featureId));
            index++;
        }
        return features;
    }

    static Geometry adaptGeometryForFeatureSchema(Geometry geometry,
                                                   SimpleFeatureType featureType) {
        if (geometry == null || featureType == null
                || featureType.getGeometryDescriptor() == null) {
            return null;
        }

        Class<?> binding = featureType.getGeometryDescriptor().getType().getBinding();
        if (binding == null || binding.isInstance(geometry)) {
            return geometry;
        }

        GeometryFactory factory = geometry.getFactory();
        if (LineString.class.isAssignableFrom(binding)
                && geometry instanceof MultiLineString multiLine
                && multiLine.getNumGeometries() > 0) {
            return (Geometry) multiLine.getGeometryN(0).copy();
        }
        if (MultiLineString.class.isAssignableFrom(binding)
                && geometry instanceof LineString lineString) {
            return factory.createMultiLineString(
                    new LineString[]{(LineString) lineString.copy()});
        }
        if (Polygon.class.isAssignableFrom(binding)
                && geometry instanceof MultiPolygon multiPolygon
                && multiPolygon.getNumGeometries() > 0) {
            return (Geometry) multiPolygon.getGeometryN(0).copy();
        }
        if (MultiPolygon.class.isAssignableFrom(binding)
                && geometry instanceof Polygon polygon) {
            return factory.createMultiPolygon(
                    new Polygon[]{(Polygon) polygon.copy()});
        }
        if (Point.class.isAssignableFrom(binding)
                && geometry instanceof MultiPoint multiPoint
                && multiPoint.getNumGeometries() > 0) {
            return (Geometry) multiPoint.getGeometryN(0).copy();
        }
        if (MultiPoint.class.isAssignableFrom(binding)
                && geometry instanceof Point point) {
            return factory.createMultiPoint(new Point[]{(Point) point.copy()});
        }
        return geometry;
    }

    // -----------------------------------------------------------------------
    // query
    // -----------------------------------------------------------------------

    static SimpleFeature findFeatureById(List<SimpleFeature> features, String featureId) {
        if (features == null || featureId == null) {
            return null;
        }
        for (SimpleFeature feature : features) {
            if (feature != null && sameFeatureId(feature, featureId)) {
                return feature;
            }
        }
        return null;
    }

    static boolean sameFeatureId(SimpleFeature feature, SimpleFeature otherFeature) {
        return feature != null && otherFeature != null
                && sameFeatureId(feature, otherFeature.getID());
    }

    static boolean sameFeatureId(SimpleFeature feature, String featureId) {
        return feature != null && featureId != null
                && featureId.equals(feature.getID());
    }

    static String resolveGeometryFamily(SimpleFeatureType featureType) {
        if (featureType == null || featureType.getGeometryDescriptor() == null) {
            return "";
        }

        Class<?> binding = featureType.getGeometryDescriptor().getType().getBinding();
        if (binding == null) {
            return "";
        }
        if (Point.class.isAssignableFrom(binding)
                || MultiPoint.class.isAssignableFrom(binding)) {
            return "POINT";
        }
        if (LineString.class.isAssignableFrom(binding)
                || MultiLineString.class.isAssignableFrom(binding)) {
            return "LINE";
        }
        if (Polygon.class.isAssignableFrom(binding)
                || MultiPolygon.class.isAssignableFrom(binding)) {
            return "POLYGON";
        }
        return "";
    }

    // -----------------------------------------------------------------------
    // selection helpers
    // -----------------------------------------------------------------------

    static List<SimpleFeature> collectSelectedFeatures(List<SimpleFeature> features,
                                                        List<String> selectedIds) {
        List<SimpleFeature> selected = new ArrayList<>();
        if (features == null || selectedIds == null || selectedIds.isEmpty()) {
            return selected;
        }

        LinkedHashSet<String> orderedIds = new LinkedHashSet<>(selectedIds);
        for (SimpleFeature feature : features) {
            if (feature != null && orderedIds.contains(feature.getID())) {
                selected.add(feature);
            }
        }
        return selected;
    }

    static List<String> extractFeatureIds(List<SimpleFeature> features) {
        List<String> ids = new ArrayList<>();
        if (features == null) {
            return ids;
        }

        for (SimpleFeature feature : features) {
            if (feature != null && feature.getID() != null
                    && !feature.getID().isBlank()) {
                ids.add(feature.getID());
            }
        }
        return ids;
    }

    static List<SimpleFeature> replaceFeaturesBySelection(
            List<SimpleFeature> sourceFeatures,
            List<String> selectedIds,
            List<SimpleFeature> replacementFeatures) {
        List<SimpleFeature> updated = new ArrayList<>();
        if (sourceFeatures == null) {
            updated.addAll(replacementFeatures);
            return updated;
        }

        LinkedHashSet<String> idsToReplace = new LinkedHashSet<>(selectedIds);
        boolean inserted = false;
        for (SimpleFeature feature : sourceFeatures) {
            if (feature == null) {
                continue;
            }
            if (idsToReplace.contains(feature.getID())) {
                if (!inserted) {
                    updated.addAll(replacementFeatures);
                    inserted = true;
                }
                continue;
            }
            updated.add(feature);
        }

        if (!inserted) {
            updated.addAll(replacementFeatures);
        }
        return updated;
    }

    // -----------------------------------------------------------------------
    // geometry merge
    // -----------------------------------------------------------------------

    static List<SimpleFeature> buildFeaturesForMergedGeometry(
            SimpleFeature sourceFeature,
            Geometry mergedGeometry,
            SimpleFeatureType targetType) {
        if (sourceFeature == null || mergedGeometry == null) {
            return new ArrayList<>();
        }

        Class<?> binding = targetType != null
                && targetType.getGeometryDescriptor() != null
                ? targetType.getGeometryDescriptor().getType().getBinding()
                : null;
        List<Geometry> parts;
        if (binding != null && (binding.isInstance(mergedGeometry)
                || MultiLineString.class.isAssignableFrom(binding)
                || MultiPolygon.class.isAssignableFrom(binding)
                || MultiPoint.class.isAssignableFrom(binding))) {
            parts = List.of(mergedGeometry);
        } else {
            parts = MapGeometryUtils.collectGeometryParts(mergedGeometry);
        }
        return buildReplacementFeatures(sourceFeature, parts);
    }

    static Geometry buildMergedGeometry(List<SimpleFeature> selectedFeatures, String family) {
        List<Geometry> geometries = new ArrayList<>();
        for (SimpleFeature feature : selectedFeatures) {
            Geometry geometry = MapGeometryUtils.extractFeatureGeometryCopy(feature);
            if (geometry != null && !geometry.isEmpty()) {
                geometries.add(geometry);
            }
        }

        if (geometries.size() < 2) {
            return null;
        }

        GeometryFactory factory = geometries.get(0).getFactory();
        if ("LINE".equals(family)) {
            Geometry unioned = UnaryUnionOp.union(geometries);
            LineMerger merger = new LineMerger();
            merger.add(unioned);
            Collection<?> mergedLines = merger.getMergedLineStrings();
            List<LineString> lines = new ArrayList<>();
            for (Object candidate : mergedLines) {
                if (candidate instanceof LineString lineString && !lineString.isEmpty()) {
                    lines.add((LineString) lineString.copy());
                }
            }

            if (lines.isEmpty()) {
                List<Geometry> parts = MapGeometryUtils.collectGeometryParts(unioned);
                for (Geometry part : parts) {
                    if (part instanceof LineString lineString && !lineString.isEmpty()) {
                        lines.add((LineString) lineString.copy());
                    }
                }
            }

            if (lines.isEmpty()) {
                return unioned;
            }
            if (lines.size() == 1) {
                return lines.get(0);
            }
            return factory.createMultiLineString(lines.toArray(new LineString[0]));
        }

        if ("POLYGON".equals(family)) {
            Geometry unioned = UnaryUnionOp.union(geometries);
            if (unioned == null || unioned.isEmpty()) {
                return null;
            }
            Geometry cleaned = unioned.buffer(0);
            return cleaned == null || cleaned.isEmpty() ? unioned : cleaned;
        }

        return null;
    }

    static int geometryPartCount(Geometry geometry) {
        return MapGeometryUtils.collectGeometryParts(geometry).size();
    }

    // -----------------------------------------------------------------------
    // envelope
    // -----------------------------------------------------------------------

    static Envelope computeEnvelope(List<SimpleFeature> features) {
        Envelope envelope = new Envelope();
        if (features == null) {
            return envelope;
        }
        for (SimpleFeature feature : features) {
            if (feature == null) {
                continue;
            }
            Object geomObj = feature.getDefaultGeometry();
            if (geomObj instanceof Geometry) {
                envelope.expandToInclude(((Geometry) geomObj).getEnvelopeInternal());
            }
        }
        return envelope;
    }

    // -----------------------------------------------------------------------
    // polygon
    // -----------------------------------------------------------------------

    static Polygon buildPolygonFromCoordinates(List<Coordinate> coordinates,
                                                GeometryFactory factory) {
        return MapGeometryUtils.buildPolygonFromCoordinates(coordinates, factory);
    }
}
