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

    private final UndoRedoContext ctx;
    private final Deque<LayerEditSnapshot> editUndoStack = new ArrayDeque<>();
    private final Deque<LayerEditSnapshot> editRedoStack = new ArrayDeque<>();

    UndoRedoManager(UndoRedoContext ctx) {
        this.ctx = ctx;
    }

    boolean canUndo() {
        return !editUndoStack.isEmpty() && ctx.getEditingLayer() != null;
    }

    boolean canRedo() {
        return !editRedoStack.isEmpty() && ctx.getEditingLayer() != null;
    }

    void clear() {
        editUndoStack.clear();
        editRedoStack.clear();
    }

    void pushUndoSnapshotForSelectedLayer() {
        if (ctx.getSelectedLayerForUndo() == null) {
            return;
        }
        pushUndoSnapshot(ctx.getSelectedLayerForUndo(), ctx.getSelectedFeatureForUndo() != null ? ctx.getSelectedFeatureForUndo().getID() : null);
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
        ShapefileData data = ctx.getShapefileData(layer);
        if (data == null) {
            return null;
        }
        return new LayerEditSnapshot(
                layer,
                ctx.cloneFeatureList(data.getFeatures()),
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
                ctx.computeEnvelope(snapshot.features),
                snapshot.sourceName,
                snapshot.features.size(),
                snapshot.message,
                snapshot.schema
        );
        ctx.addOrUpdateShapefileLayer(snapshot.layer, restoredData);

        ctx.setActiveVectorEditingLayer(snapshot.layer);
        ctx.setSelectedLayer(snapshot.layer);
        ctx.setSelectedFeature(ctx.findFeatureById(restoredData.getFeatures(), snapshot.selectedFeatureId));
        if (snapshot.selectedFeatureId != null && !snapshot.selectedFeatureId.isBlank()) {
            ctx.updateTableSelectionIds(snapshot.layer, List.of(snapshot.selectedFeatureId));
            OpenAttributeTableAction.syncSelectionFromMap(snapshot.layer, List.of(snapshot.selectedFeatureId));
        } else {
            ctx.clearTableSelectionIds(snapshot.layer);
            OpenAttributeTableAction.clearSelectionInOpenTables();
        }
        ctx.setFeatureEditMode(ctx.getSelectedFeatureForUndo() != null);
        ctx.setFeatureEditOriginalGeometry(ctx.extractFeatureGeometryCopy(ctx.getSelectedFeatureForUndo()));
        ctx.setFeatureEditDirty(true);
        ctx.clearFeatureEditSketchCoordinates();
        ctx.setActiveEditVertexIndex(-1);
        ctx.setJoinTargetVertexIndex(-1);
        ctx.clearAdjacentPolygonState();
        ctx.setFeatureEditOperation(MapPanel.EDIT_OP_MOVE_VERTEX);
        snapshot.layer.setFeatureCount(restoredData.getFeatureCount());

        ctx.markProjectDirty();
        if (statusMessage != null && !statusMessage.isBlank()) {
            ctx.showCopiedMessage(statusMessage);
        }
        ctx.refreshEditingUi();
    }

    void undoFeatureEdit() {
        if (!canUndo()) {
            return;
        }
        Layer layer = ctx.getEditingLayer();
        if (layer == null) {
            return;
        }

        editRedoStack.push(captureLayerSnapshot(layer, ctx.getSelectedFeatureForUndo() != null ? ctx.getSelectedFeatureForUndo().getID() : null));
        restoreLayerSnapshot(editUndoStack.pop(), "Deshacer aplicado.");
    }

    void redoFeatureEdit() {
        if (!canRedo()) {
            return;
        }
        Layer layer = ctx.getEditingLayer();
        if (layer == null) {
            return;
        }

        editUndoStack.push(captureLayerSnapshot(layer, ctx.getSelectedFeatureForUndo() != null ? ctx.getSelectedFeatureForUndo().getID() : null));
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
