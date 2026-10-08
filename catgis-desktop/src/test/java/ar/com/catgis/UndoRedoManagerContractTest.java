package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UndoRedoManagerContractTest {

    @Test
    void undoAndRedoRestoreGeometryAttributesAndSelection() throws Exception {
        TestContext ctx = TestContext.create();
        UndoRedoManager manager = new UndoRedoManager(ctx);

        manager.pushUndoSnapshot(ctx.layer, ctx.selectedFeature.getID());
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());

        ctx.selectedFeature.setAttribute("name", "Pozo B");
        ctx.selectedFeature.setDefaultGeometry(
                ReleaseTestSupport.GEOMETRY_FACTORY.createPoint(new Coordinate(-67.50, -31.25)));

        manager.undoFeatureEdit();

        assertEquals("Pozo A", ctx.currentFeature().getAttribute("name"));
        assertEquals(-68.85, ((Geometry) ctx.currentFeature().getDefaultGeometry()).getCoordinate().x, 0.0001);
        assertEquals(ctx.currentFeature().getID(), ctx.selectedFeature.getID());
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        assertTrue(ctx.projectDirty);
        assertEquals("Deshacer aplicado.", ctx.lastMessage);

        manager.redoFeatureEdit();

        assertEquals("Pozo B", ctx.currentFeature().getAttribute("name"));
        assertEquals(-67.50, ((Geometry) ctx.currentFeature().getDefaultGeometry()).getCoordinate().x, 0.0001);
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        assertEquals("Rehacer aplicado.", ctx.lastMessage);
    }

    @Test
    void pushingANewEditAfterUndoInvalidatesRedoHistory() throws Exception {
        TestContext ctx = TestContext.create();
        UndoRedoManager manager = new UndoRedoManager(ctx);

        manager.pushUndoSnapshot(ctx.layer, null);
        ctx.currentFeature().setAttribute("name", "Pozo B");
        manager.undoFeatureEdit();
        assertTrue(manager.canRedo());

        manager.pushUndoSnapshot(ctx.layer, null);

        assertFalse(manager.canRedo());
        assertTrue(manager.canUndo());
    }

    @Test
    void historyIsBoundedToTwentySnapshots() throws Exception {
        TestContext ctx = TestContext.create();
        UndoRedoManager manager = new UndoRedoManager(ctx);

        for (int i = 0; i < 25; i++) {
            manager.pushUndoSnapshot(ctx.layer, null);
            ctx.currentFeature().setAttribute("codigo", i + 1);
        }

        int undoCount = 0;
        while (manager.canUndo()) {
            manager.undoFeatureEdit();
            undoCount++;
        }

        assertEquals(20, undoCount);
    }

    private static final class TestContext implements UndoRedoContext {
        private final Layer layer;
        private ShapefileData data;
        private SimpleFeature selectedFeature;
        private Layer editingLayer;
        private boolean projectDirty;
        private String lastMessage;

        private TestContext(Layer layer, ShapefileData data) {
            this.layer = layer;
            this.data = data;
            this.selectedFeature = data.getFeatures().get(0);
            this.editingLayer = layer;
        }

        static TestContext create() throws Exception {
            ShapefileData data = ReleaseTestSupport.buildPointData(
                    "undo_contract",
                    "EPSG:4326",
                    new Coordinate(-68.85, -32.89),
                    "Pozo A",
                    7
            );
            Layer layer = ReleaseTestSupport.buildVectorLayer(
                    "Pozos", Path.of("undo_contract.shp"), "EPSG:4326");
            return new TestContext(layer, data);
        }

        SimpleFeature currentFeature() {
            return data.getFeatures().get(0);
        }

        @Override public Layer getEditingLayer() { return editingLayer; }
        @Override public Layer getSelectedLayerForUndo() { return layer; }
        @Override public SimpleFeature getSelectedFeatureForUndo() { return selectedFeature; }
        @Override public ShapefileData getShapefileData(Layer requested) { return requested == layer ? data : null; }
        @Override public List<SimpleFeature> cloneFeatureList(List<SimpleFeature> features) {
            return FeatureBuilder.cloneFeatureList(features);
        }
        @Override public Envelope computeEnvelope(List<SimpleFeature> features) {
            Envelope envelope = new Envelope();
            if (features != null) {
                for (SimpleFeature feature : features) {
                    Object geometry = feature != null ? feature.getDefaultGeometry() : null;
                    if (geometry instanceof Geometry g) {
                        envelope.expandToInclude(g.getEnvelopeInternal());
                    }
                }
            }
            return envelope;
        }
        @Override public SimpleFeature findFeatureById(List<SimpleFeature> features, String id) {
            return FeatureBuilder.findFeatureById(features, id);
        }
        @Override public Geometry extractFeatureGeometryCopy(SimpleFeature feature) {
            return MapGeometryUtils.extractFeatureGeometryCopy(feature);
        }
        @Override public void addOrUpdateShapefileLayer(Layer requested, ShapefileData replacement) {
            if (requested == layer) {
                data = replacement;
            }
        }
        @Override public void setActiveVectorEditingLayer(Layer value) { editingLayer = value; }
        @Override public void setSelectedLayer(Layer value) { }
        @Override public void setSelectedFeature(SimpleFeature value) { selectedFeature = value; }
        @Override public void setFeatureEditMode(boolean mode) { }
        @Override public void setFeatureEditOriginalGeometry(Geometry geometry) { }
        @Override public void setFeatureEditDirty(boolean dirty) { }
        @Override public void clearFeatureEditSketchCoordinates() { }
        @Override public void setActiveEditVertexIndex(int index) { }
        @Override public void setJoinTargetVertexIndex(int index) { }
        @Override public void clearAdjacentPolygonState() { }
        @Override public void setFeatureEditOperation(String operation) { }
        @Override public void markProjectDirty() { projectDirty = true; }
        @Override public void showCopiedMessage(String message) { lastMessage = message; }
        @Override public void refreshEditingUi() { }
        @Override public void updateTableSelectionIds(Layer layerKey, List<String> featureIds) { }
        @Override public void clearTableSelectionIds(Layer layerKey) { }
    }
}
