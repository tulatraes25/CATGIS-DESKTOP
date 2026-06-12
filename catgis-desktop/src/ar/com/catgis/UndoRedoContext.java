package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

/**
 * Contract for undo/redo operations on the editing layer state.
 * UndoRedoManager depends on this instead of the full MapPanel.
 */
interface UndoRedoContext {

    // --- Read ---
    Layer getEditingLayer();
    Layer getSelectedLayerForUndo();
    SimpleFeature getSelectedFeatureForUndo();
    ShapefileData getShapefileData(Layer layer);
    List<SimpleFeature> cloneFeatureList(List<SimpleFeature> features);
    Envelope computeEnvelope(List<SimpleFeature> features);
    SimpleFeature findFeatureById(List<SimpleFeature> features, String id);
    Geometry extractFeatureGeometryCopy(SimpleFeature feature);

    // --- Write ---
    void addOrUpdateShapefileLayer(Layer layer, ShapefileData data);
    void setActiveVectorEditingLayer(Layer layer);
    void setSelectedLayer(Layer layer);
    void setSelectedFeature(SimpleFeature feature);
    void setFeatureEditMode(boolean mode);
    void setFeatureEditOriginalGeometry(Geometry geometry);
    void setFeatureEditDirty(boolean dirty);
    void clearFeatureEditSketchCoordinates();
    void setActiveEditVertexIndex(int index);
    void setJoinTargetVertexIndex(int index);
    void clearAdjacentPolygonState();
    void setFeatureEditOperation(String operation);
    void markProjectDirty();
    void showCopiedMessage(String message);
    void refreshEditingUi();
    void updateTableSelectionIds(Layer layerKey, List<String> featureIds);
    void clearTableSelectionIds(Layer layerKey);
}

