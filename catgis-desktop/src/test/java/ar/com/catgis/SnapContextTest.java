package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies SnapContext is properly decoupled — any implementation works,
 * no dependency on MapPanel.
 */
class SnapContextTest {

    @Test
    void minimalImplementationDoesNotThrow() {
        SnapContext ctx = new SnapContext() {
            @Override public boolean isFeatureEditMode() { return false; }
            @Override public boolean isMoveVertexEditOp() { return false; }
            @Override public int getActiveEditVertexIndex() { return -1; }
            @Override public double screenToWorldX(int sx) { return sx; }
            @Override public double screenToWorldY(int sy) { return sy; }
            @Override public int worldToScreenX(double wx) { return (int) wx; }
            @Override public int worldToScreenY(double wy) { return (int) wy; }
            @Override public ShapefileData getShapefileData(Layer l) { return null; }
            @Override public boolean isFeatureVisibleInLayer(Layer l, SimpleFeature f) { return false; }
            @Override public boolean isLayerEffectivelyVisible(Layer l) { return false; }
            @Override public boolean sameFeatureId(SimpleFeature f, String id) { return false; }
            @Override public Layer getSelectedLayer() { return null; }
            @Override public String getSelectedFeatureId() { return null; }
            @Override public Geometry reprojectGeometryIfNeeded(Layer l, Geometry g) { return g; }
            @Override public Layer getActiveVectorEditingLayer() { return null; }
            @Override public List<Layer> getRenderOrderLayers() { return Collections.emptyList(); }
            @Override public boolean hasShapefileLayer(Layer l) { return false; }
            @Override public LineSplitProjection findEditableSegmentProjection(
                    Geometry g, Coordinate c, int sx, int sy, double tol) { return null; }
        };

        assertFalse(ctx.isFeatureEditMode());
        assertFalse(ctx.isMoveVertexEditOp());
        assertEquals(-1, ctx.getActiveEditVertexIndex());
        assertEquals(42.0, ctx.screenToWorldX(42), 0.01);
        assertEquals(42, ctx.worldToScreenY(42.0));
    }

    @Test
    void unconfiguredContextHasNoSelection() {
        SnapContext ctx = new SnapContext() {
            @Override public boolean isFeatureEditMode() { return false; }
            @Override public boolean isMoveVertexEditOp() { return false; }
            @Override public int getActiveEditVertexIndex() { return -1; }
            @Override public double screenToWorldX(int sx) { return 0; }
            @Override public double screenToWorldY(int sy) { return 0; }
            @Override public int worldToScreenX(double wx) { return 0; }
            @Override public int worldToScreenY(double wy) { return 0; }
            @Override public ShapefileData getShapefileData(Layer l) { return null; }
            @Override public boolean isFeatureVisibleInLayer(Layer l, SimpleFeature f) { return false; }
            @Override public boolean isLayerEffectivelyVisible(Layer l) { return false; }
            @Override public boolean sameFeatureId(SimpleFeature f, String id) { return false; }
            @Override public Layer getSelectedLayer() { return null; }
            @Override public String getSelectedFeatureId() { return null; }
            @Override public Geometry reprojectGeometryIfNeeded(Layer l, Geometry g) { return g; }
            @Override public Layer getActiveVectorEditingLayer() { return null; }
            @Override public List<Layer> getRenderOrderLayers() { return Collections.emptyList(); }
            @Override public boolean hasShapefileLayer(Layer l) { return false; }
            @Override public LineSplitProjection findEditableSegmentProjection(
                    Geometry g, Coordinate c, int sx, int sy, double tol) { return null; }
        };

        assertNull(ctx.getSelectedLayer());
        assertNull(ctx.getSelectedFeatureId());
        assertNull(ctx.getActiveVectorEditingLayer());
        assertTrue(ctx.getRenderOrderLayers().isEmpty());
    }
}
