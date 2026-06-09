package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;

import java.util.List;

/**
 * Callback interface for extracted classes to interact with MapPanel.
 * All methods are write operations (repaint, show messages, modify state).
 */
public interface MapPanelCallbacks {
    void repaint();
    void setTool(String tool);
    void showCopiedMessage(String message);
    void showScrollableInfoDialog(String title, String content);
    void pushUndoSnapshotForSelectedLayer();
    void pushUndoSnapshot(Layer layer, String description);
    void replaceLayerFeatures(Layer layer, List<SimpleFeature> features, String selectId, boolean showSuccess, String successMessage);
    void appendGeometriesToLayer(Layer layer, List<org.locationtech.jts.geom.Geometry> geometries, String successMessage);
    void applyFeatureSelection(Layer layer, List<String> featureIds, boolean additive, boolean showFlash, boolean tableSync, String message);
    void refreshEditingUi();
    void clearCadConstructionState();
    void updateStatusCoordinates(double x, double y);
    Layer resolveDrawingTargetLayer();
}
