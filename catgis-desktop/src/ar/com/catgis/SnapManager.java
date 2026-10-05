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

    private final SnapContext ctx;
    private boolean snapEnabled = true;
    private Coordinate snapPreviewCoordinate = null;

    SnapManager(SnapContext ctx) {
        this.ctx = ctx;
    }

    boolean isSnapEnabled() {
        return snapEnabled;
    }

    void setSnapEnabled(boolean enabled) {
        this.snapEnabled = enabled;
        if (!enabled) {
            snapPreviewCoordinate = null;
        }
    }

    Coordinate getSnapPreviewCoordinate() {
        return snapPreviewCoordinate;
    }

    void setSnapPreviewCoordinate(Coordinate coordinate) {
        this.snapPreviewCoordinate = coordinate;
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
        return ctx.isFeatureEditMode()
                && ctx.isMoveVertexEditOp()
                && ctx.getActiveEditVertexIndex() >= 0;
    }

    Coordinate findNearestSnapCoordinate(int screenX, int screenY, boolean excludeSelectedFeature) {
        if (!snapEnabled) {
            return null;
        }
        SnapTarget bestTarget = null;
        Coordinate target = new Coordinate(ctx.screenToWorldX(screenX), ctx.screenToWorldY(screenY));
        for (Layer layer : getSnapCandidateLayers()) {
            if (layer == null || !ctx.isLayerEffectivelyVisible(layer)) {
                continue;
            }

            ShapefileData data = ctx.getShapefileData(layer);
            if (data == null || data.getFeatures() == null) {
                continue;
            }

            for (SimpleFeature feature : data.getFeatures()) {
                if (feature == null) {
                    continue;
                }
                if (!ctx.isFeatureVisibleInLayer(layer, feature)) {
                    continue;
                }
                if (excludeSelectedFeature && layer == ctx.getSelectedLayer()
                        && ctx.sameFeatureId(feature, ctx.getSelectedFeatureId())) {
                    continue;
                }

                Object geomObj = feature.getDefaultGeometry();
                if (!(geomObj instanceof Geometry geometry)) {
                    continue;
                }

                Geometry displayGeometry = ctx.reprojectGeometryIfNeeded(layer, geometry);
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

            int vx = ctx.worldToScreenX(coordinate.x);
            int vy = ctx.worldToScreenY(coordinate.y);
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
            LineSplitProjection projection = ctx.findEditableSegmentProjection(
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
        Layer activeEditing = ctx.getActiveVectorEditingLayer();
        if (activeEditing != null && ctx.hasShapefileLayer(activeEditing)) {
            candidates.add(activeEditing);
            return candidates;
        }
        Layer selected = ctx.getSelectedLayer();
        if (selected != null && ctx.hasShapefileLayer(selected)) {
            candidates.add(selected);
            return candidates;
        }
        for (Layer layer : ctx.getRenderOrderLayers()) {
            if (layer != null && ctx.hasShapefileLayer(layer) && ctx.isLayerEffectivelyVisible(layer)) {
                candidates.add(layer);
            }
        }
        return candidates;
    }
}
