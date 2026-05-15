package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class TopologyValidationService {

    private static final double AREA_EPS = 1e-9;
    private static final double LENGTH_EPS = 1e-9;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private TopologyValidationService() {
    }

    public static List<TopologyCheckResult> validateLayer(Layer layer, ShapefileData data) {
        return validateLayer(layer, data, new TopologyValidationOptions());
    }

    public static List<TopologyCheckResult> validateLayer(Layer layer,
                                                          ShapefileData data,
                                                          TopologyValidationOptions options) {
        List<TopologyCheckResult> results = new ArrayList<>();
        if (layer == null || data == null || data.getFeatures() == null) {
            return results;
        }

        TopologyValidationOptions safeOptions = options != null ? options : new TopologyValidationOptions();
        String family = VectorLayerUtils.resolveGeometryFamily(data);
        String sourceCrs = VectorLayerUtils.pickLayerCrs(layer, data);

        Map<String, List<String>> duplicates = new LinkedHashMap<>();
        Map<String, Integer> endpointUsage = new HashMap<>();
        Map<String, Geometry> geometryIndex = new HashMap<>();
        List<SimpleFeature> polygonFeatures = new ArrayList<>();
        List<SimpleFeature> lineFeatures = new ArrayList<>();
        List<EndpointRecord> endpointRecords = new ArrayList<>();

        for (SimpleFeature feature : data.getFeatures()) {
            if (feature == null) {
                continue;
            }

            Geometry geometry = geometryOf(feature);
            if (geometry == null || geometry.isEmpty()) {
                results.add(result("Error", "Geometria vacia", "La entidad no contiene geometria util.", List.of(feature.getID()), null, sourceCrs));
                continue;
            }

            geometryIndex.put(feature.getID(), geometry);

            if (safeOptions.isInvalidGeometries()) {
                IsValidOp validOp = new IsValidOp(geometry);
                TopologyValidationError error = validOp.getValidationError();
                if (error != null) {
                    Geometry focus = error.getCoordinate() != null ? GEOMETRY_FACTORY.createPoint(error.getCoordinate()) : geometry;
                    results.add(result("Error", "Geometria invalida", error.getMessage(), List.of(feature.getID()), focus, sourceCrs));
                }
            }

            if (geometry instanceof LineString || geometry instanceof MultiLineString) {
                if (safeOptions.isSelfIntersections() && !geometry.isSimple()) {
                    results.add(result("Error", "Auto-interseccion", "La linea presenta cruces sobre si misma.", List.of(feature.getID()), geometry, sourceCrs));
                }
                if (geometry.getLength() <= LENGTH_EPS) {
                    results.add(result("Error", "Linea degenerada", "La longitud de la linea es nula o despreciable.", List.of(feature.getID()), geometry, sourceCrs));
                }
                lineFeatures.add(feature);
                registerLineEndpoints(feature, geometry, endpointUsage, endpointRecords);
            }

            if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
                if (geometry.getArea() <= AREA_EPS) {
                    results.add(result("Error", "Poligono degenerado", "La superficie del poligono es nula o despreciable.", List.of(feature.getID()), geometry, sourceCrs));
                }
                if (safeOptions.isSlivers() && geometry.getArea() > AREA_EPS && geometry.getArea() <= safeOptions.getSliverAreaThreshold()) {
                    results.add(result("Advertencia", "Sliver", "La entidad poligonal tiene un area muy pequena para el umbral configurado.", List.of(feature.getID()), geometry, sourceCrs));
                }
                polygonFeatures.add(feature);
            }

            if (safeOptions.isProblematicMultiparts()
                    && geometry instanceof GeometryCollection collection
                    && collection.getNumGeometries() > 1
                    && isProblematicMultipart(collection)) {
                results.add(result("Advertencia", "Multiparte para revision", "La entidad multiparte contiene partes desconectadas o superpuestas.", List.of(feature.getID()), geometry, sourceCrs));
            }

            if (safeOptions.isDuplicates()) {
                String normalizedKey = normalizedGeometryKey(geometry);
                duplicates.computeIfAbsent(normalizedKey, key -> new ArrayList<>()).add(feature.getID());
            }
        }

        if (safeOptions.isDuplicates()) {
            for (Map.Entry<String, List<String>> entry : duplicates.entrySet()) {
                if (entry.getValue().size() > 1) {
                    Geometry focus = geometryIndex.get(entry.getValue().get(0));
                    results.add(result("Error", "Duplicado geometrico", "Se encontraron entidades con la misma geometria.", entry.getValue(), focus, sourceCrs));
                }
            }
        }

        if ("LINE".equals(family) && (safeOptions.isDanglingEndpoints() || safeOptions.isNearMissEndpoints())) {
            detectLineConnectionIssues(results, lineFeatures, endpointUsage, endpointRecords, safeOptions, sourceCrs);
        }

        if ("POLYGON".equals(family)) {
            if (safeOptions.isOverlaps() || safeOptions.isSlivers()) {
                detectPolygonOverlaps(results, polygonFeatures, safeOptions, sourceCrs);
            }
            if (safeOptions.isHoles() || safeOptions.isSlivers()) {
                detectPolygonVoids(results, polygonFeatures, safeOptions, sourceCrs);
            }
        }

        return results;
    }

    public static void exportReport(List<TopologyCheckResult> results, File file) throws Exception {
        if (file == null) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("severidad,tipo_error,detalle,feature_ids,con_foco");
            writer.newLine();
            if (results != null) {
                for (TopologyCheckResult result : results) {
                    writer.write(csv(result.getSeverity()));
                    writer.write(",");
                    writer.write(csv(result.getCheckType()));
                    writer.write(",");
                    writer.write(csv(result.getDetail()));
                    writer.write(",");
                    writer.write(csv(String.join("|", result.getFeatureIds())));
                    writer.write(",");
                    writer.write(csv(result.hasFocusGeometry() ? "si" : "no"));
                    writer.newLine();
                }
            }
        }
    }

    private static void detectLineConnectionIssues(List<TopologyCheckResult> results,
                                                   List<SimpleFeature> lineFeatures,
                                                   Map<String, Integer> endpointUsage,
                                                   List<EndpointRecord> endpointRecords,
                                                   TopologyValidationOptions options,
                                                   String sourceCrs) {
        for (EndpointRecord record : endpointRecords) {
            if (record == null || record.coordinate == null) {
                continue;
            }
            if (endpointUsage.getOrDefault(endpointKey(record.coordinate), 0) > 1) {
                continue;
            }

            double nearestEndpointDistance = Double.MAX_VALUE;
            double nearestSegmentDistance = Double.MAX_VALUE;
            String nearestEndpointFeatureId = null;
            String nearestSegmentFeatureId = null;
            Point endpointPoint = GEOMETRY_FACTORY.createPoint(record.coordinate);

            for (SimpleFeature feature : lineFeatures) {
                if (feature == null || feature.getID().equals(record.featureId)) {
                    continue;
                }
                Geometry geometry = geometryOf(feature);
                if (geometry == null || geometry.isEmpty()) {
                    continue;
                }

                for (Coordinate otherEndpoint : getLineEndpoints(geometry)) {
                    if (otherEndpoint == null) {
                        continue;
                    }
                    double distance = otherEndpoint.distance(record.coordinate);
                    if (distance < nearestEndpointDistance) {
                        nearestEndpointDistance = distance;
                        nearestEndpointFeatureId = feature.getID();
                    }
                }

                try {
                    double distanceToLine = geometry.distance(endpointPoint);
                    if (distanceToLine < nearestSegmentDistance) {
                        nearestSegmentDistance = distanceToLine;
                        nearestSegmentFeatureId = feature.getID();
                    }
                } catch (TopologyException ignored) {
                }
            }

            if (options.isNearMissEndpoints()
                    && nearestEndpointFeatureId != null
                    && nearestEndpointDistance > 0d
                    && nearestEndpointDistance <= options.getConnectionTolerance()) {
                results.add(result(
                        "Advertencia",
                        "Undershoot",
                        "Un extremo queda muy cerca de otro extremo sin llegar a conectar.",
                        List.of(record.featureId, nearestEndpointFeatureId),
                        endpointPoint,
                        sourceCrs
                ));
                continue;
            }

            if (options.isNearMissEndpoints()
                    && nearestSegmentFeatureId != null
                    && nearestSegmentDistance > 0d
                    && nearestSegmentDistance <= options.getConnectionTolerance()) {
                results.add(result(
                        "Advertencia",
                        "Overshoot",
                        "Un extremo se acerca a otra linea sin resolver correctamente la conexion.",
                        List.of(record.featureId, nearestSegmentFeatureId),
                        endpointPoint,
                        sourceCrs
                ));
                continue;
            }

            if (options.isDanglingEndpoints()) {
                results.add(result(
                        "Advertencia",
                        "Extremo colgante",
                        "La linea tiene al menos un extremo sin conexion con otra entidad.",
                        List.of(record.featureId),
                        endpointPoint,
                        sourceCrs
                ));
            }
        }
    }

    private static void detectPolygonOverlaps(List<TopologyCheckResult> results,
                                              List<SimpleFeature> polygonFeatures,
                                              TopologyValidationOptions options,
                                              String sourceCrs) {
        for (int i = 0; i < polygonFeatures.size(); i++) {
            Geometry a = geometryOf(polygonFeatures.get(i));
            if (a == null) {
                continue;
            }
            for (int j = i + 1; j < polygonFeatures.size(); j++) {
                Geometry b = geometryOf(polygonFeatures.get(j));
                if (b == null || !a.getEnvelopeInternal().intersects(b.getEnvelopeInternal())) {
                    continue;
                }
                Geometry overlap;
                try {
                    overlap = a.intersection(b);
                } catch (TopologyException ex) {
                    continue;
                }
                if (overlap == null || overlap.isEmpty() || overlap.getArea() <= AREA_EPS) {
                    continue;
                }

                List<String> ids = List.of(polygonFeatures.get(i).getID(), polygonFeatures.get(j).getID());
                if (options.isSlivers() && overlap.getArea() <= options.getSliverAreaThreshold()) {
                    results.add(result("Advertencia", "Sliver", "Dos poligonos generan una superposicion muy pequena.", ids, overlap, sourceCrs));
                } else if (options.isOverlaps()) {
                    results.add(result("Error", "Superposicion", "Dos poligonos se superponen en superficie.", ids, overlap, sourceCrs));
                }
            }
        }
    }

    private static void detectPolygonVoids(List<TopologyCheckResult> results,
                                           List<SimpleFeature> polygonFeatures,
                                           TopologyValidationOptions options,
                                           String sourceCrs) {
        List<Geometry> geometries = new ArrayList<>();
        for (SimpleFeature feature : polygonFeatures) {
            Geometry geometry = geometryOf(feature);
            if (geometry != null && !geometry.isEmpty()) {
                geometries.add(geometry);
            }
        }
        if (geometries.isEmpty()) {
            return;
        }

        Geometry union = UnaryUnionOp.union(geometries);
        if (union == null || union.isEmpty()) {
            return;
        }

        List<Polygon> polygons = new ArrayList<>();
        collectPolygons(union, polygons);
        for (Polygon polygon : polygons) {
            if (polygon == null) {
                continue;
            }
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                Coordinate[] coords = polygon.getInteriorRingN(i).getCoordinates();
                if (coords == null || coords.length < 4) {
                    continue;
                }
                Polygon hole = GEOMETRY_FACTORY.createPolygon(new CoordinateList(coords).toCoordinateArray());
                if (hole.isEmpty() || hole.getArea() <= AREA_EPS) {
                    continue;
                }
                if (options.isSlivers() && hole.getArea() <= options.getSliverAreaThreshold()) {
                    results.add(result("Advertencia", "Sliver", "Se detecto un hueco muy fino dentro de la cobertura poligonal.", List.of(), hole, sourceCrs));
                } else if (options.isHoles()) {
                    results.add(result("Error", "Hueco interno", "La union de poligonos deja un hueco interno.", List.of(), hole, sourceCrs));
                }
            }
        }
    }

    private static void registerLineEndpoints(SimpleFeature feature,
                                              Geometry geometry,
                                              Map<String, Integer> endpointUsage,
                                              List<EndpointRecord> endpointRecords) {
        for (Coordinate endpoint : getLineEndpoints(geometry)) {
            if (endpoint == null) {
                continue;
            }
            String key = endpointKey(endpoint);
            endpointUsage.put(key, endpointUsage.getOrDefault(key, 0) + 1);
            endpointRecords.add(new EndpointRecord(feature != null ? feature.getID() : "", new Coordinate(endpoint)));
        }
    }

    private static Coordinate[] getLineEndpoints(Geometry geometry) {
        if (geometry instanceof LineString line) {
            Coordinate[] coordinates = line.getCoordinates();
            return coordinates.length >= 2
                    ? new Coordinate[]{coordinates[0], coordinates[coordinates.length - 1]}
                    : new Coordinate[0];
        }
        if (geometry instanceof MultiLineString multiLine && multiLine.getNumGeometries() > 0) {
            Geometry first = multiLine.getGeometryN(0);
            Geometry last = multiLine.getGeometryN(multiLine.getNumGeometries() - 1);
            Coordinate start = first instanceof LineString line ? line.getCoordinateN(0) : null;
            Coordinate end = last instanceof LineString line ? line.getCoordinateN(line.getNumPoints() - 1) : null;
            return new Coordinate[]{start, end};
        }
        return new Coordinate[0];
    }

    private static boolean isProblematicMultipart(GeometryCollection collection) {
        if (collection == null || collection.getNumGeometries() <= 1) {
            return false;
        }
        for (int i = 0; i < collection.getNumGeometries(); i++) {
            Geometry a = collection.getGeometryN(i);
            if (a == null || a.isEmpty()) {
                return true;
            }
            for (int j = i + 1; j < collection.getNumGeometries(); j++) {
                Geometry b = collection.getGeometryN(j);
                if (b == null || b.isEmpty()) {
                    return true;
                }
                if (a.intersects(b) && !a.touches(b)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void collectPolygons(Geometry geometry, List<Polygon> polygons) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }
        if (geometry instanceof Polygon polygon) {
            polygons.add(polygon);
            return;
        }
        if (geometry instanceof MultiPolygon multiPolygon) {
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Geometry child = multiPolygon.getGeometryN(i);
                if (child instanceof Polygon polygon) {
                    polygons.add(polygon);
                }
            }
            return;
        }
        if (geometry instanceof GeometryCollection collection) {
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                collectPolygons(collection.getGeometryN(i), polygons);
            }
        }
    }

    private static String endpointKey(Coordinate coordinate) {
        return String.format(Locale.US, "%.6f|%.6f", coordinate.x, coordinate.y);
    }

    private static String normalizedGeometryKey(Geometry geometry) {
        Geometry copy = geometry.copy();
        copy.normalize();
        return copy.toText();
    }

    private static Geometry geometryOf(SimpleFeature feature) {
        return feature != null && feature.getDefaultGeometry() instanceof Geometry geometry ? geometry : null;
    }

    private static TopologyCheckResult result(String severity,
                                              String type,
                                              String detail,
                                              List<String> featureIds,
                                              Geometry focusGeometry,
                                              String sourceCrs) {
        return new TopologyCheckResult(severity, type, detail, featureIds, focusGeometry, sourceCrs);
    }

    private static String csv(String value) {
        String safe = value != null ? value : "";
        safe = safe.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private static final class EndpointRecord {
        private final String featureId;
        private final Coordinate coordinate;

        private EndpointRecord(String featureId, Coordinate coordinate) {
            this.featureId = featureId != null ? featureId : "";
            this.coordinate = coordinate;
        }
    }
}
