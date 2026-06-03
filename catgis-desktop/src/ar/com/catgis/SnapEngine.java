package ar.com.catgis;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.util.List;

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

            for (Object obj : data.getFeatures()) {
                if (obj instanceof org.geotools.api.feature.simple.SimpleFeature feature) {
                    Geometry geom = (Geometry) feature.getDefaultGeometry();
                    if (geom == null) continue;

                    if (snapToVertex) {
                        for (Coordinate c : geom.getCoordinates()) {
                            double dist = Math.hypot(c.x - worldX, c.y - worldY);
                            if (dist < bestDist && dist <= toleranceWorld) {
                                bestDist = dist;
                                bestSnap = c;
                            }
                        }
                    }

                    if (snapToEdge && geom instanceof LineString ls) {
                        Coordinate nearest = nearestPointOnLine(ls, worldX, worldY);
                        if (nearest != null) {
                            double dist = Math.hypot(nearest.x - worldX, nearest.y - worldY);
                            if (dist < bestDist && dist <= toleranceWorld) {
                                bestDist = dist;
                                bestSnap = nearest;
                            }
                        }
                    }
                }
            }
        }

        return bestSnap != null ? bestSnap : new Coordinate(worldX, worldY);
    }

    private Coordinate nearestPointOnLine(LineString line, double px, double py) {
        Coordinate[] coords = line.getCoordinates();
        Coordinate best = null;
        double bestDist = Double.MAX_VALUE;

        for (int i = 0; i < coords.length - 1; i++) {
            Coordinate a = coords[i];
            Coordinate b = coords[i + 1];
            double dx = b.x - a.x;
            double dy = b.y - a.y;
            double lenSq = dx * dx + dy * dy;
            if (lenSq == 0) continue;

            double t = Math.max(0, Math.min(1, ((px - a.x) * dx + (py - a.y) * dy) / lenSq));
            double projX = a.x + t * dx;
            double projY = a.y + t * dy;
            double dist = Math.hypot(projX - px, projY - py);

            if (dist < bestDist) {
                bestDist = dist;
                best = new Coordinate(projX, projY);
            }
        }
        return best;
    }

    private ShapefileData getShapefileData(Layer layer) {
        MapPanel mapPanel = CatgisDesktopApp.mapPanel;
        return mapPanel != null ? mapPanel.getShapefileData(layer) : null;
    }
}
