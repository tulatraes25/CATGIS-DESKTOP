package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

/**
 * Contract for snap operations, extracted from MapPanel to reduce coupling.
 * SnapManager depends on this interface instead of the full MapPanel.
 */
interface SnapContext {

    // --- Editing state ---
    boolean isFeatureEditMode();
    boolean isMoveVertexEditOp();
    int getActiveEditVertexIndex();

    // --- Coordinate conversion ---
    double screenToWorldX(int screenX);
    double screenToWorldY(int screenY);
    int worldToScreenX(double worldX);
    int worldToScreenY(double worldY);

    // --- Layer / feature access ---
    ShapefileData getShapefileData(Layer layer);
    boolean isFeatureVisibleInLayer(Layer layer, SimpleFeature feature);
    boolean isLayerEffectivelyVisible(Layer layer);
    boolean sameFeatureId(SimpleFeature feature, String id);

    // --- Selection ---
    Layer getSelectedLayer();
    String getSelectedFeatureId();

    // --- Reprojection ---
    Geometry reprojectGeometryIfNeeded(Layer layer, Geometry geometry);

    // --- Snap candidates ---
    Layer getActiveVectorEditingLayer();
    List<Layer> getRenderOrderLayers();
    boolean hasShapefileLayer(Layer layer);

    // --- Projection ---
    LineSplitProjection findEditableSegmentProjection(
            Geometry displayGeometry, Coordinate target,
            int screenX, int screenY, double tolerancePx);
}
