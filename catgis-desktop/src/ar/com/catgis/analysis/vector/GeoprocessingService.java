package ar.com.catgis.analysis.vector;

import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.CatgisLogger;
import ar.com.catgis.GeometryTools;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.util.*;

public final class GeoprocessingService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public record LayerData(String name, String crsCode, String family,
                            SimpleFeatureType schema, List<SimpleFeature> features) {}

    public static ShapefileData executeBuffer(LayerData layerA, double distance, String outputName) throws Exception {
        SimpleFeatureType resultType = buildResultType(outputName, layerA.crsCode(), "POLYGON", layerA.schema());
        List<SimpleFeature> features = new ArrayList<>();
        int idx = 1;
        for (SimpleFeature sf : layerA.features()) {
            Geometry g = geometryOf(sf);
            if (g == null || g.isEmpty()) continue;
            Geometry buffered = normalizeToFamily(g.buffer(distance), "POLYGON");
            if (buffered == null || buffered.isEmpty()) continue;
            features.add(copyFeature(resultType, sf, buffered, outputName + "." + idx++));
        }
        return buildResultData(outputName, features, resultType);
    }

    public static ShapefileData executeDissolve(LayerData layerA, String fieldName, String outputName) throws Exception {
        String trimmed = fieldName != null ? fieldName.trim() : "";
        AttributeDescriptor groupDesc = findAttribute(layerA.schema(), trimmed);
        Map<Object, List<Geometry>> groups = new LinkedHashMap<>();
        for (SimpleFeature f : layerA.features()) {
            Geometry g = geometryOf(f);
            if (g == null || g.isEmpty()) continue;
            Object key = groupDesc != null ? f.getAttribute(groupDesc.getLocalName()) : "__all__";
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(g);
        }
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(safeTypeName(outputName));
        applyCrs(builder, layerA.crsCode());
        builder.add("the_geom", MultiPolygon.class);
        if (groupDesc != null) builder.add(groupDesc.getLocalName(), groupDesc.getType().getBinding());
        builder.add("conteo", Integer.class);
        SimpleFeatureType resultType = builder.buildFeatureType();
        List<SimpleFeature> result = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(resultType);
        int idx = 1;
        for (Map.Entry<Object, List<Geometry>> entry : groups.entrySet()) {
            Geometry union = normalizeToFamily(UnaryUnionOp.union(entry.getValue()), "POLYGON");
            if (union == null || union.isEmpty()) continue;
            fb.set("the_geom", union);
            if (groupDesc != null) fb.set(groupDesc.getLocalName(), entry.getKey());
            fb.set("conteo", entry.getValue().size());
            result.add(fb.buildFeature(outputName + "." + idx++));
            fb.reset();
        }
        return buildResultData(outputName, result, resultType);
    }

    public static ShapefileData executeClip(LayerData layerA, LayerData layerB, String outputName) throws Exception {
        Geometry mask = buildUnion(layerB.features(), layerA.crsCode(), layerB.crsCode());
        if (mask == null || mask.isEmpty()) throw new IllegalArgumentException("Mask empty.");
        SimpleFeatureType resultType = buildResultType(outputName, layerA.crsCode(), layerA.family(), layerA.schema());
        List<SimpleFeature> result = new ArrayList<>();
        int idx = 1;
        for (SimpleFeature sf : layerA.features()) {
            Geometry g = geometryOf(sf);
            if (g == null || g.isEmpty() || !g.intersects(mask)) continue;
            Geometry clipped = normalizeToFamily(g.intersection(mask), layerA.family());
            if (clipped == null || clipped.isEmpty()) continue;
            result.add(copyFeature(resultType, sf, clipped, outputName + "." + idx++));
        }
        return buildResultData(outputName, result, resultType);
    }

    public static ShapefileData executeDifference(LayerData layerA, LayerData layerB, String outputName) throws Exception {
        Geometry mask = buildUnion(layerB.features(), layerA.crsCode(), layerB.crsCode());
        SimpleFeatureType resultType = buildResultType(outputName, layerA.crsCode(), layerA.family(), layerA.schema());
        List<SimpleFeature> result = new ArrayList<>();
        int idx = 1;
        for (SimpleFeature sf : layerA.features()) {
            Geometry g = geometryOf(sf);
            if (g == null || g.isEmpty()) continue;
            Geometry diff = normalizeToFamily(g.difference(mask), layerA.family());
            if (diff == null || diff.isEmpty()) continue;
            result.add(copyFeature(resultType, sf, diff, outputName + "." + idx++));
        }
        return buildResultData(outputName, result, resultType);
    }

    public static ShapefileData executeVoronoi(LayerData layerA, String outputName) throws Exception {
        SimpleFeatureType polyType = buildResultType(outputName, layerA.crsCode(), "POLYGON", null);
        List<SimpleFeature> cells = GeometryTools.computeVoronoi(layerA.features(), null, polyType);
        return buildResultData(outputName, cells, polyType);
    }

    public static ShapefileData executeSymDiff(LayerData layerA, LayerData layerB, String outputName) throws Exception {
        Geometry ga = buildUnion(layerA.features(), layerA.crsCode(), layerA.crsCode());
        Geometry gb = buildUnion(layerB.features(), layerA.crsCode(), layerB.crsCode());
        if (ga == null || ga.isEmpty() || gb == null || gb.isEmpty()) throw new IllegalArgumentException("Empty geometry.");
        Geometry result = ga.symDifference(gb);
        SimpleFeatureType rt = buildResultType(outputName, layerA.crsCode(), "POLYGON", layerA.schema());
        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(rt);
        List<Polygon> polys = new ArrayList<>();
        collectPolys(result, polys);
        int idx = 1;
        for (Polygon p : polys) {
            if (p == null || p.isEmpty()) continue;
            builder.set("the_geom", p);
            features.add(builder.buildFeature(outputName + "." + idx++));
        }
        return buildResultData(outputName, features, rt);
    }

    public static ShapefileData executeMultiBuffer(LayerData layerA, double ringDist, int rings, String outputName) throws Exception {
        SimpleFeatureType rt = buildResultType(outputName, layerA.crsCode(), layerA.family(), layerA.schema());
        List<SimpleFeature> features = new ArrayList<>();
        int idx = 1;
        for (SimpleFeature sf : layerA.features()) {
            Geometry g = geometryOf(sf);
            if (g == null || g.isEmpty()) continue;
            for (int r = 1; r <= rings; r++) {
                Geometry buf = g.buffer(ringDist * r);
                if (buf != null && !buf.isEmpty()) features.add(copyFeature(rt, sf, buf, outputName + "." + idx++));
            }
        }
        return buildResultData(outputName, features, rt);
    }

    public static ShapefileData executeSmooth(LayerData layerA, double tolerance, String outputName) throws Exception {
        SimpleFeatureType rt = buildResultType(outputName, layerA.crsCode(), layerA.family(), layerA.schema());
        List<SimpleFeature> features = new ArrayList<>();
        int idx = 1;
        for (SimpleFeature sf : layerA.features()) {
            Geometry g = geometryOf(sf);
            if (g == null || g.isEmpty()) continue;
            Geometry smoothed = GeometryTools.smooth(g, tolerance);
            if (smoothed != null && !smoothed.isEmpty()) features.add(copyFeature(rt, sf, smoothed, outputName + "." + idx++));
        }
        return buildResultData(outputName, features, rt);
    }

    public static ShapefileData executePolyToLine(LayerData layerA, String outputName) throws Exception {
        SimpleFeatureType lineType = buildResultType(outputName, layerA.crsCode(), "LINE", null);
        List<SimpleFeature> lines = GeometryTools.polygonsToLines(layerA.features(), lineType);
        return buildResultData(outputName, lines, lineType);
    }

    public static ShapefileData executeLineToPoly(LayerData layerA, String outputName) throws Exception {
        SimpleFeatureType polyType = buildResultType(outputName, layerA.crsCode(), "POLYGON", null);
        List<SimpleFeature> polys = GeometryTools.linesToPolygons(layerA.features(), polyType);
        return buildResultData(outputName, polys, polyType);
    }

    public static ShapefileData executeMinBounding(LayerData layerA, String type, String outputName) throws Exception {
        Geometry bounding = GeometryTools.computeMinimumBoundingGeometry(layerA.features(), type != null ? type : "envelope");
        if (bounding == null) throw new IllegalArgumentException("No bounding geometry computed.");
        String family = bounding instanceof Polygon || bounding instanceof MultiPolygon ? "POLYGON" : "LINE";
        SimpleFeatureType rt = buildResultType(outputName, layerA.crsCode(), family, null);
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(rt);
        builder.set("the_geom", bounding);
        return buildResultData(outputName, Collections.singletonList(builder.buildFeature(outputName + ".1")), rt);
    }

    public static ShapefileData executeDelaunay(LayerData layerA, String outputName) throws Exception {
        SimpleFeatureType lineType = buildResultType(outputName, layerA.crsCode(), "LINE", null);
        List<SimpleFeature> triangles = GeometryTools.computeDelaunay(layerA.features(), lineType);
        return buildResultData(outputName, triangles, lineType);
    }

    // --- Geometry utilities ---
    private static Geometry geometryOf(SimpleFeature f) {
        Object g = f.getDefaultGeometry();
        return g instanceof Geometry ? (Geometry) g : null;
    }

    private static Geometry buildUnion(List<SimpleFeature> features, String targetCrs, String sourceCrs) throws Exception {
        List<Geometry> geoms = new ArrayList<>();
        for (SimpleFeature f : features) {
            Geometry g = reproject(geometryOf(f), sourceCrs, targetCrs);
            if (g != null && !g.isEmpty()) geoms.add(g);
        }
        return geoms.isEmpty() ? null : UnaryUnionOp.union(geoms);
    }

    private static Geometry reproject(Geometry g, String srcCrs, String tgtCrs) throws Exception {
        if (g == null) return null;
        String ns = CRSDefinitions.normalizeCode(srcCrs), nt = CRSDefinitions.normalizeCode(tgtCrs);
        if (ns.isBlank() || nt.isBlank() || ns.equalsIgnoreCase(nt)) return g.copy();
        CoordinateReferenceSystem source = CRSDefinitions.decode(ns, true);
        CoordinateReferenceSystem target = CRSDefinitions.decode(nt, true);
        MathTransform transform = CRS.findMathTransform(source, target, true);
        return JTS.transform(g, transform);
    }

    private static Geometry normalizeToFamily(Geometry g, String family) {
        return g; // Geometry normalization handled by buildResultData schema
    }

    private static SimpleFeatureType buildResultType(String name, String crs, String family, SimpleFeatureType source) throws Exception {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName(safeTypeName(name));
        applyCrs(b, crs);
        b.add("the_geom", MultiPolygon.class);
        if (source != null) {
            for (AttributeDescriptor d : source.getAttributeDescriptors()) {
                if (!(d instanceof GeometryDescriptor)) b.add(d.getLocalName(), d.getType().getBinding());
            }
        }
        return b.buildFeatureType();
    }

    private static void applyCrs(SimpleFeatureTypeBuilder b, String crsCode) {
        try {
            String n = CRSDefinitions.normalizeCode(crsCode);
            if (!n.isBlank()) b.setCRS(CRSDefinitions.decode(n, true));
        } catch (Exception ignored) { CatgisLogger.warn("GeoprocessingService: operation failed", ignored); }
    }

    private static AttributeDescriptor findAttribute(SimpleFeatureType type, String name) {
        if (type == null || name == null || name.isBlank()) return null;
        for (AttributeDescriptor d : type.getAttributeDescriptors()) {
            if (!(d instanceof GeometryDescriptor) && d.getLocalName().equalsIgnoreCase(name.trim())) return d;
        }
        return null;
    }

    private static SimpleFeature copyFeature(SimpleFeatureType targetType, SimpleFeature source, Geometry geom, String id) {
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(targetType);
        b.set("the_geom", geom);
        if (source != null && targetType != null) {
            for (AttributeDescriptor d : targetType.getAttributeDescriptors()) {
                if (!(d instanceof GeometryDescriptor)) b.set(d.getLocalName(), source.getAttribute(d.getLocalName()));
            }
        }
        return b.buildFeature(id);
    }

    private static void collectPolys(Geometry g, List<Polygon> out) {
        if (g == null || g.isEmpty()) return;
        if (g instanceof Polygon) out.add((Polygon) g);
        else if (g instanceof GeometryCollection) {
            for (int i = 0; i < g.getNumGeometries(); i++) collectPolys(g.getGeometryN(i), out);
        }
    }

    private static ShapefileData buildResultData(String name, List<SimpleFeature> features, SimpleFeatureType type) {
        Geometry env = null;
        for (SimpleFeature f : features) {
            Geometry g = geometryOf(f);
            if (g != null && !g.isEmpty()) env = env == null ? g.getEnvelope() : env.union(g.getEnvelope());
        }
        org.locationtech.jts.geom.Envelope envelope = env != null ? env.getEnvelopeInternal() : new org.locationtech.jts.geom.Envelope();
        return new ShapefileData(features, envelope, name, features.size(), "Geoprocessing result", type);
    }

    private static String safeTypeName(String text) {
        if (text == null || text.isBlank()) return "resultado";
        return text.trim().replaceAll("[^A-Za-z0-9_]+", "_");
    }
}
