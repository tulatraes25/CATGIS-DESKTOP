package ar.com.catgis.core.geometry;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.strtree.STRtree;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class SpatialIndex {

    private static final int DEFAULT_CAPACITY = 10;

    private SpatialIndex() {}

    public record IndexedFeature(SimpleFeature feature, Envelope envelope, String layerId) {}

    public static STRtree buildStrTree(List<IndexedFeature> features) {
        STRtree tree = new STRtree(DEFAULT_CAPACITY);
        for (IndexedFeature indexed : features) {
            tree.insert(indexed.envelope(), indexed);
        }
        try { tree.build(); } catch (Exception e) { ar.com.catgis.CatgisLogger.warn("STRtree build failed in SpatialIndex", e); }
        return tree;
    }

    public static STRtree buildStrTreeFromLayer(List<SimpleFeature> features, String layerId) {
        List<IndexedFeature> indexed = new ArrayList<>();
        for (SimpleFeature f : features) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g != null && !g.isEmpty()) {
                indexed.add(new IndexedFeature(f, g.getEnvelopeInternal(), layerId));
            }
        }
        return buildStrTree(indexed);
    }

    @SuppressWarnings("unchecked")
    public static List<IndexedFeature> query(STRtree tree, Envelope search, int maxResults) {
        List<IndexedFeature> results = tree.query(search);
        return maxResults > 0 && results.size() > maxResults ? results.subList(0, maxResults) : results;
    }

    @SuppressWarnings("unchecked")
    public static List<IndexedFeature> queryAll(STRtree tree, Envelope search) {
        return tree.query(search);
    }

    @SuppressWarnings("unchecked")
    public static Set<SimpleFeature> findCandidates(STRtree tree, SimpleFeature feature, double expansion) {
        Geometry g = (Geometry) feature.getDefaultGeometry();
        if (g == null) return Collections.emptySet();
        Envelope env = g.getEnvelopeInternal();
        if (expansion > 0) env.expandBy(expansion);
        Set<SimpleFeature> candidates = new LinkedHashSet<>();
        for (Object obj : tree.query(env)) {
            if (obj instanceof IndexedFeature indexed) candidates.add(indexed.feature());
        }
        return candidates;
    }

    private static final Map<String, STRtree> treeCache = new ConcurrentHashMap<>();

    public static STRtree getOrBuild(String layerId, List<SimpleFeature> features) {
        return treeCache.computeIfAbsent(layerId, k -> buildStrTreeFromLayer(features, k));
    }

    public static void invalidate(String layerId) {
        treeCache.remove(layerId);
    }

    public static void clearCache() {
        treeCache.clear();
    }
}
