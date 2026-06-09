package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

class SnapManager {

    private final MapPanel map;

    SnapManager(MapPanel map) {
        this.map = map;
    }

    static class SnapTarget {
        final Coordinate coordinate;
        final double distance;

        SnapTarget(Coordinate coordinate, double distance) {
            this.coordinate = coordinate;
            this.distance = distance;
        }
    }

    boolean shouldExcludeSelectedFeatureFromSnap() {
        return map.featureEditMode
                && MapPanel.EDIT_OP_MOVE_VERTEX.equals(map.featureEditOperation)
                && map.activeEditVertexIndex >= 0;
    }

    Coordinate findNearestSnapCoordinate(int screenX, int screenY, boolean excludeSelectedFeature) {
        if (!map.snapEnabled) {
            return null;
        }
        SnapTarget bestTarget = null;
        Coordinate target = new Coordinate(map.screenToWorldX(screenX), map.screenToWorldY(screenY));
        for (Layer layer : getSnapCandidateLayers()) {
            if (layer == null || !map.layerManager.isLayerEffectivelyVisible(layer)) {
                continue;
            }

            ShapefileData data = map.getShapefileData(layer);
            if (data == null || data.getFeatures() == null) {
                continue;
            }

            for (SimpleFeature feature : data.getFeatures()) {
                if (feature == null) {
                    continue;
                }
                if (!map.isFeatureVisibleInLayer(layer, feature)) {
                    continue;
                }
                if (excludeSelectedFeature && layer == map.selectedLayer
                        && map.sameFeatureId(feature, map.selectedFeature != null ? map.selectedFeature.getID() : null)) {
                    continue;
                }

                Object geomObj = feature.getDefaultGeometry();
                if (!(geomObj instanceof Geometry geometry)) {
                    continue;
                }

                Geometry displayGeometry = map.reprojectGeometryIfNeeded(layer, geometry);
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

            int vx = map.worldToScreenX(coordinate.x);
            int vy = map.worldToScreenY(coordinate.y);
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
            MapPanel.LineSplitProjection projection = map.findEditableSegmentProjection(
                    displayGeometry, target, screenX, screenY, MapPanel.SNAP_TOLERANCE_PX);
            if (projection != null && projection.projected != null
                    && (bestTarget == null || projection.distance < bestTarget.distance)) {
                bestTarget = new SnapTarget(new Coordinate(projection.projected), projection.distance);
            }
        }

        return bestTarget;
    }

    List<Layer> getSnapCandidateLayers() {
        List<Layer> candidates = new ArrayList<>();
        if (map.activeVectorEditingLayer != null && map.shapefileLayers.containsKey(map.activeVectorEditingLayer)) {
            candidates.add(map.activeVectorEditingLayer);
            return candidates;
        }
        if (map.selectedLayer != null && map.shapefileLayers.containsKey(map.selectedLayer)) {
            candidates.add(map.selectedLayer);
            return candidates;
        }
        for (Layer layer : map.getRenderOrderLayers()) {
            if (layer != null && map.shapefileLayers.containsKey(layer) && map.isLayerEffectivelyVisible(layer)) {
                candidates.add(layer);
            }
        }
        return candidates;
    }
}
