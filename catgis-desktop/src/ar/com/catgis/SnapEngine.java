package ar.com.catgis;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import org.geotools.api.feature.simple.SimpleFeature;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;

import java.util.ArrayList;
import java.util.List;

public class SnapEngine {

    private final MapPanel panel;

    public SnapEngine(MapPanel panel) {
        this.panel = panel;
    }

    // --- Inner class ---

    static class SnapTarget {
        final Coordinate coordinate;
        final double distance;

        SnapTarget(Coordinate coordinate, double distance) {
            this.coordinate = coordinate;
            this.distance = distance;
        }
    }

    // --- Methods moved from MapPanel ---

    boolean shouldExcludeSelectedFeatureFromSnap() {
        return panel.featureEditMode
                && MapPanel.EDIT_OP_MOVE_VERTEX.equals(panel.featureEditOperation)
                && panel.activeEditVertexIndex >= 0;
    }

    Coordinate findNearestSnapCoordinate(int screenX, int screenY, boolean excludeSelectedFeature) {
        if (!panel.snapEnabled) {
            return null;
        }
        SnapTarget bestTarget = null;
        Coordinate target = new Coordinate(panel.screenToWorldX(screenX), panel.screenToWorldY(screenY));
        for (Layer layer : getSnapCandidateLayers()) {
            if (layer == null || !panel.isLayerEffectivelyVisible(layer)) {
                continue;
            }

            ShapefileData data = panel.getShapefileData(layer);
            if (data == null || data.getFeatures() == null) {
                continue;
            }

            for (SimpleFeature feature : data.getFeatures()) {
                if (feature == null) {
                    continue;
                }
                if (!panel.isFeatureVisibleInLayer(layer, feature)) {
                    continue;
                }
                if (excludeSelectedFeature && layer == panel.selectedLayer && panel.sameFeatureId(feature, panel.selectedFeature != null ? panel.selectedFeature.getID() : null)) {
                    continue;
                }

                Object geomObj = feature.getDefaultGeometry();
                if (!(geomObj instanceof Geometry geometry)) {
                    continue;
                }

                Geometry displayGeometry = panel.reprojectGeometryIfNeeded(layer, geometry);
                if (displayGeometry == null || displayGeometry.isEmpty()) {
                    continue;
                }

                SnapTarget candidate = findNearestSnapTarget(displayGeometry, target, screenX, screenY);
                if (candidate != null && (bestTarget == null || candidate.distance < bestTarget.distance)) {
                    bestTarget = candidate;
                }
            }
        }
        return bestTarget != null ? bestTarget.coordinate : null;
    }

    SnapTarget findNearestSnapTarget(Geometry displayGeometry, Coordinate target, int screenX, int screenY) {
        if (displayGeometry == null || displayGeometry.isEmpty() || target == null) {
            return null;
        }

        SnapTarget bestTarget = null;
        for (Coordinate coordinate : displayGeometry.getCoordinates()) {
            if (coordinate == null) {
                continue;
            }

            int vx = panel.worldToScreenX(coordinate.x);
            int vy = panel.worldToScreenY(coordinate.y);
            double distance = Math.hypot(screenX - vx, screenY - vy);
            if (distance > MapPanel.SNAP_TOLERANCE_PX) {
                continue;
            }

            if (bestTarget == null || distance < bestTarget.distance) {
                bestTarget = new SnapTarget(new Coordinate(coordinate), distance);
            }
        }

        if (displayGeometry instanceof LineString
                || displayGeometry instanceof MultiLineString
                || displayGeometry instanceof Polygon
                || displayGeometry instanceof MultiPolygon) {
            MapPanel.LineSplitProjection projection = panel.findEditableSegmentProjection(displayGeometry, target, screenX, screenY, MapPanel.SNAP_TOLERANCE_PX);
            if (projection != null && projection.projected != null
                    && (bestTarget == null || projection.distance < bestTarget.distance)) {
                bestTarget = new SnapTarget(new Coordinate(projection.projected), projection.distance);
            }
        }

        return bestTarget;
    }

    List<Layer> getSnapCandidateLayers() {
        List<Layer> candidates = new ArrayList<>();
        if (panel.activeVectorEditingLayer != null && panel.shapefileLayers.containsKey(panel.activeVectorEditingLayer)) {
            candidates.add(panel.activeVectorEditingLayer);
            return candidates;
        }
        if (panel.selectedLayer != null && panel.shapefileLayers.containsKey(panel.selectedLayer)) {
            candidates.add(panel.selectedLayer);
            return candidates;
        }
        for (Layer layer : panel.getRenderOrderLayers()) {
            if (layer != null && panel.shapefileLayers.containsKey(layer) && panel.isLayerEffectivelyVisible(layer)) {
                candidates.add(layer);
            }
        }
        return candidates;
    }
}
