package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class UndoRedoManager {

    private static final int MAX_EDIT_HISTORY = 20;

    private final MapPanel map;
    private final Deque<LayerEditSnapshot> editUndoStack = new ArrayDeque<>();
    private final Deque<LayerEditSnapshot> editRedoStack = new ArrayDeque<>();

    UndoRedoManager(MapPanel map) {
        this.map = map;
    }

    boolean canUndo() {
        return !editUndoStack.isEmpty() && map.getEditingLayerRef() != null;
    }

    boolean canRedo() {
        return !editRedoStack.isEmpty() && map.getEditingLayerRef() != null;
    }

    void clear() {
        editUndoStack.clear();
        editRedoStack.clear();
    }

    void pushUndoSnapshotForSelectedLayer() {
        if (map.selectedLayer == null) {
            return;
        }
        pushUndoSnapshot(map.selectedLayer, map.selectedFeature != null ? map.selectedFeature.getID() : null);
    }

    void pushUndoSnapshot(Layer layer, String selectedFeatureId) {
        LayerEditSnapshot snapshot = captureLayerSnapshot(layer, selectedFeatureId);
        if (snapshot == null) {
            return;
        }
        editUndoStack.push(snapshot);
        while (editUndoStack.size() > MAX_EDIT_HISTORY) {
            editUndoStack.removeLast();
        }
        editRedoStack.clear();
    }

    private LayerEditSnapshot captureLayerSnapshot(Layer layer, String selectedFeatureId) {
        if (layer == null) {
            return null;
        }
        ShapefileData data = map.getShapefileData(layer);
        if (data == null) {
            return null;
        }
        return new LayerEditSnapshot(
                layer,
                map.cloneFeatureList(data.getFeatures()),
                data.getSourceName(),
                data.getMessage(),
                data.getSchema(),
                selectedFeatureId
        );
    }

    private void restoreLayerSnapshot(LayerEditSnapshot snapshot, String statusMessage) {
        if (snapshot == null || snapshot.layer == null) {
            return;
        }

        ShapefileData restoredData = new ShapefileData(
                snapshot.features,
                map.computeEnvelope(snapshot.features),
                snapshot.sourceName,
                snapshot.features.size(),
                snapshot.message,
                snapshot.schema
        );
        map.addOrUpdateShapefileLayer(snapshot.layer, restoredData);

        map.activeVectorEditingLayer = snapshot.layer;
        map.selectedLayer = snapshot.layer;
        map.selectedFeature = map.findFeatureById(restoredData.getFeatures(), snapshot.selectedFeatureId);
        if (snapshot.selectedFeatureId != null && !snapshot.selectedFeatureId.isBlank()) {
            map.tableSelectionIds.put(snapshot.layer, new ArrayList<>(List.of(snapshot.selectedFeatureId)));
            OpenAttributeTableAction.syncSelectionFromMap(snapshot.layer, List.of(snapshot.selectedFeatureId));
        } else {
            map.tableSelectionIds.remove(snapshot.layer);
            OpenAttributeTableAction.clearSelectionInOpenTables();
        }
        map.featureEditMode = map.selectedFeature != null;
        map.featureEditOriginalGeometry = map.extractFeatureGeometryCopy(map.selectedFeature);
        map.featureEditDirty = true;
        map.featureEditSketchCoordinates.clear();
        map.activeEditVertexIndex = -1;
        map.joinTargetVertexIndex = -1;
        map.clearAdjacentPolygonState();
        map.featureEditOperation = MapPanel.EDIT_OP_MOVE_VERTEX;
        snapshot.layer.setFeatureCount(restoredData.getFeatureCount());

        CatgisDesktopApp.markProjectDirty();
        if (statusMessage != null && !statusMessage.isBlank()) {
            map.showCopiedMessage(statusMessage);
        }
        map.refreshEditingUi();
    }

    void undoFeatureEdit() {
        if (!canUndo()) {
            return;
        }
        Layer layer = map.getEditingLayerRef();
        if (layer == null) {
            return;
        }

        editRedoStack.push(captureLayerSnapshot(layer, map.selectedFeature != null ? map.selectedFeature.getID() : null));
        restoreLayerSnapshot(editUndoStack.pop(), "Deshacer aplicado.");
    }

    void redoFeatureEdit() {
        if (!canRedo()) {
            return;
        }
        Layer layer = map.getEditingLayerRef();
        if (layer == null) {
            return;
        }

        editUndoStack.push(captureLayerSnapshot(layer, map.selectedFeature != null ? map.selectedFeature.getID() : null));
        restoreLayerSnapshot(editRedoStack.pop(), "Rehacer aplicado.");
    }

    static class LayerEditSnapshot {
        private final Layer layer;
        private final List<SimpleFeature> features;
        private final String sourceName;
        private final String message;
        private final SimpleFeatureType schema;
        private final String selectedFeatureId;

        private LayerEditSnapshot(Layer layer,
                                  List<SimpleFeature> features,
                                  String sourceName,
                                  String message,
                                  SimpleFeatureType schema,
                                  String selectedFeatureId) {
            this.layer = layer;
            this.features = features != null ? features : new ArrayList<>();
            this.sourceName = sourceName;
            this.message = message;
            this.schema = schema;
            this.selectedFeatureId = selectedFeatureId;
        }
    }
}
