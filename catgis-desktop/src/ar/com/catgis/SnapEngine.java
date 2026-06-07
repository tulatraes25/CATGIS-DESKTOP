package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.geometry.SpatialIndex;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Snapping engine for vertex/edge snapping during editing.
 * Extracted from MapPanel to reduce its responsibilities.
 */
public class SnapEngine {

    private boolean snapEnabled = false;
    private double snapTolerancePx = 10;
    private boolean snapToVertex = true;
    private boolean snapToEdge = true;

    // --- Getters/Setters ---

    public boolean isSnapEnabled() { return snapEnabled; }
    public void setSnapEnabled(boolean enabled) { this.snapEnabled = enabled; }
    public double getSnapTolerancePx() { return snapTolerancePx; }
    public void setSnapTolerancePx(double tolerance) { this.snapTolerancePx = tolerance; }
    public boolean isSnapToVertex() { return snapToVertex; }
    public void setSnapToVertex(boolean snap) { this.snapToVertex = snap; }
    public boolean isSnapToEdge() { return snapToEdge; }
    public void setSnapToEdge(boolean snap) { this.snapToEdge = snap; }

    /**
     * Find the nearest snap point to the given screen coordinates.
     * Returns the snapped coordinate, or the original if no snap found.
     */
    public Coordinate snap(int screenX, int screenY, List<Layer> layers,
                           MapViewController viewController) {
        if (!snapEnabled) return new Coordinate(viewController.screenToWorldX(screenX), viewController.screenToWorldY(screenY));

        double worldX = viewController.screenToWorldX(screenX);
        double worldY = viewController.screenToWorldY(screenY);
        double toleranceWorld = snapTolerancePx / viewController.getZoomFactor();

        Coordinate bestSnap = null;
        double bestDist = Double.MAX_VALUE;

        for (Layer layer : layers) {
            if (layer == null || !layer.isVisible()) continue;
            ShapefileData data = getShapefileData(layer);
            if (data == null) continue;

            String layerId = layer.getName() + "@" + System.identityHashCode(layer);
            STRtree index = indexCache.computeIfAbsent(layerId, k -> buildLayerIndex(data));
            if (index == null) continue;

            Envelope search = new Envelope(worldX - toleranceWorld, worldX + toleranceWorld, worldY - toleranceWorld, worldY + toleranceWorld);
            for (Object obj : index.query(search)) {
                if (obj instanceof Coordinate c) {
                    double dist = Math.hypot(c.x - worldX, c.y - worldY);
                    if (dist < bestDist && dist <= toleranceWorld) { bestDist = dist; bestSnap = c; }
                } else if (obj instanceof LineSegment seg) {
                    double t = Math.max(0, Math.min(1, ((worldX - seg.x1) * seg.dx + (worldY - seg.y1) * seg.dy) / seg.lenSq));
                    double px = seg.x1 + t * seg.dx;
                    double py = seg.y1 + t * seg.dy;
                    double dist = Math.hypot(px - worldX, py - worldY);
                    if (dist < bestDist && dist <= toleranceWorld) { bestDist = dist; bestSnap = new Coordinate(px, py); }
                }
            }
        }

        return bestSnap != null ? bestSnap : new Coordinate(worldX, worldY);
    }

    private static final Map<String, STRtree> indexCache = new ConcurrentHashMap<>();

    private STRtree buildLayerIndex(ShapefileData data) {
        STRtree tree = new STRtree(10);
        for (Object obj : data.getFeatures()) {
            if (!(obj instanceof org.geotools.api.feature.simple.SimpleFeature feature)) continue;
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            if (geom == null) continue;
            for (Coordinate c : geom.getCoordinates()) tree.insert(new Envelope(c.x, c.x, c.y, c.y), c);
            if (geom instanceof LineString ls && snapToEdge) {
                Coordinate[] coords = ls.getCoordinates();
                for (int i = 0; i < coords.length - 1; i++) {
                    Coordinate a = coords[i], b = coords[i + 1];
                    double dx = b.x - a.x, dy = b.y - a.y, lenSq = dx * dx + dy * dy;
                    if (lenSq == 0) continue;
                    Envelope segEnv = new Envelope(Math.min(a.x, b.x), Math.max(a.x, b.x), Math.min(a.y, b.y), Math.max(a.y, b.y));
                    tree.insert(segEnv, new LineSegment(a.x, a.y, dx, dy, lenSq));
                }
            }
        }
        try { tree.build(); } catch (Exception ignored) {}
        return tree;
    }

    private record LineSegment(double x1, double y1, double dx, double dy, double lenSq) {}

    private ShapefileData getShapefileData(Layer layer) {
        MapPanel mapPanel = AppContext.get().getMapPanel();
        return mapPanel != null ? mapPanel.getShapefileData(layer) : null;
    }
}
