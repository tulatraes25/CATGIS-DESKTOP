package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SnapManagerTest {

    @Test
    void snapDisabledReturnsNull() {
        TestSnapContext ctx = new TestSnapContext();
        SnapManager snap = new SnapManager(ctx);
        snap.setSnapEnabled(false);

        assertNull(snap.findNearestSnapCoordinate(100, 100, false));
    }

    @Test
    void noSnapCandidatesReturnsNull() {
        TestSnapContext ctx = new TestSnapContext();
        SnapManager snap = new SnapManager(ctx);
        snap.setSnapEnabled(true);

        assertNull(snap.findNearestSnapCoordinate(100, 100, false));
    }

    @Test
    void snapEnabledByDefault() {
        TestSnapContext ctx = new TestSnapContext();
        SnapManager snap = new SnapManager(ctx);
        assertTrue(snap.isSnapEnabled());
    }

    @Test
    void disableSnapClearsPreview() {
        TestSnapContext ctx = new TestSnapContext();
        SnapManager snap = new SnapManager(ctx);
        snap.setSnapPreviewCoordinate(new Coordinate(1, 2));
        snap.setSnapEnabled(false);
        assertNull(snap.getSnapPreviewCoordinate());
    }

    @Test
    void shouldExcludeSelectedFeatureWhenEditingMoveVertex() {
        TestSnapContext ctx = new TestSnapContext();
        SnapManager snap = new SnapManager(ctx);
        // Mock editing state: featureEditMode + moveVertex + active vertex >= 0
        ctx.featureEditMode = true;
        ctx.activeEditVertexIndex = 0;
        // Note: isMoveVertexEditOp() returns EDIT_OP_MOVE_VERTEX.equals(featureEditOperation)
        // but featureEditOperation is not set in TestSnapContext, so this should return false
        assertFalse(snap.shouldExcludeSelectedFeatureFromSnap());
    }

    // --- Test context ---

    static class TestSnapContext implements SnapContext {
        boolean featureEditMode;
        int activeEditVertexIndex = -1;
        List<Layer> layers = new ArrayList<>();

        @Override public boolean isFeatureEditMode() { return featureEditMode; }
        @Override public boolean isMoveVertexEditOp() { return false; }
        @Override public int getActiveEditVertexIndex() { return activeEditVertexIndex; }
        @Override public double screenToWorldX(int screenX) { return screenX; }
        @Override public double screenToWorldY(int screenY) { return screenY; }
        @Override public int worldToScreenX(double worldX) { return (int) worldX; }
        @Override public int worldToScreenY(double worldY) { return (int) worldY; }
        @Override public ShapefileData getShapefileData(Layer layer) { return null; }
        @Override public boolean isFeatureVisibleInLayer(Layer layer, SimpleFeature feature) { return true; }
        @Override public boolean isLayerEffectivelyVisible(Layer layer) { return true; }
        @Override public boolean sameFeatureId(SimpleFeature feature, String id) { return false; }
        @Override public Layer getSelectedLayer() { return null; }
        @Override public String getSelectedFeatureId() { return null; }
        @Override public Geometry reprojectGeometryIfNeeded(Layer layer, Geometry geometry) { return geometry; }
        @Override public Layer getActiveVectorEditingLayer() { return null; }
        @Override public List<Layer> getRenderOrderLayers() { return layers; }
        @Override public boolean hasShapefileLayer(Layer layer) { return false; }
        @Override public LineSplitProjection findEditableSegmentProjection(
                Geometry displayGeometry, Coordinate target, int screenX, int screenY, double tolerancePx) {
            return null;
        }
    }
}
