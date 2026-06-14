package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BiConsumer;

public class SelectionManager2 {

    private final MapPanel panel;

    public SelectionManager2(MapPanel panel) {
        this.panel = panel;
    }

    List<String> getSelectedFeatureIdsForLayer(Layer layer) {
        if (layer == null) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> ids = new LinkedHashSet<>();
        List<String> storedIds = panel.tableSelectionIds.get(layer);
        if (storedIds != null) {
            for (String id : storedIds) {
                if (id != null && !id.isBlank()) {
                    ids.add(id);
                }
            }
        }
        if (layer == panel.selectedLayer && panel.selectedFeature != null && panel.selectedFeature.getID() != null) {
            ids.add(panel.selectedFeature.getID());
        }
        return new ArrayList<>(ids);
    }

    List<String> normalizeSelectionIds(Layer layer, List<String> featureIds) {
        List<String> normalized = new ArrayList<>();
        if (layer == null || featureIds == null || featureIds.isEmpty()) {
            return normalized;
        }

        ShapefileData data = panel.getShapefileData(layer);
        if (data == null || data.getFeatures() == null) {
            return normalized;
        }

        LinkedHashSet<String> orderedIds = new LinkedHashSet<>(featureIds);
        for (String featureId : orderedIds) {
            if (panel.findFeatureById(data.getFeatures(), featureId) != null) {
                normalized.add(featureId);
            }
        }
        return normalized;
    }

    boolean applyFeatureSelection(Layer layer,
                                  List<String> featureIds,
                                  boolean activateEditForSingle,
                                  boolean syncOpenTables,
                                  boolean promptOnDirty,
                                  String statusMessage) {
        if (layer == null) {
            return false;
        }

        List<String> normalizedIds = normalizeSelectionIds(layer, featureIds);
        if (normalizedIds.isEmpty()) {
            if (promptOnDirty && !panel.confirmPendingFeatureEdit("cambiar la selecci\u00f3n")) {
                return false;
            }
            clearSelectedFeatureInternal(syncOpenTables);
            if (statusMessage != null && !statusMessage.isBlank()) {
                panel.showCopiedMessage(statusMessage);
            }
            return true;
        }

        ShapefileData data = panel.getShapefileData(layer);
        SimpleFeature primaryFeature = data != null ? panel.findFeatureById(data.getFeatures(), normalizedIds.get(0)) : null;
        if (activateEditForSingle && normalizedIds.size() == 1 && primaryFeature != null) {
            panel.enableFeatureEdit(layer, primaryFeature);
            if (statusMessage != null && !statusMessage.isBlank()) {
                panel.showCopiedMessage(statusMessage);
            }
            return true;
        }

        if (promptOnDirty && !panel.confirmPendingFeatureEdit("cambiar la selecci\u00f3n")) {
            return false;
        }

        panel.selectionFlashGeometry = null;
        panel.selectionFlashTimer.stop();
        panel.tableSelectionIds.clear();
        panel.tableSelectionIds.put(layer, new ArrayList<>(normalizedIds));
        panel.selectedLayer = layer;
        panel.selectedFeature = primaryFeature;
        panel.featureEditMode = false;
        panel.featureEditOriginalGeometry = null;
        panel.featureEditDirty = false;
        panel.featureEditSketchCoordinates.clear();
        panel.activeEditVertexIndex = -1;
        panel.joinTargetVertexIndex = -1;
        panel.clearAdjacentPolygonState();
        panel.undoRedoManager.clear();
        if (!MapPanel.EDIT_OP_MOVE_FEATURE.equals(panel.featureEditOperation)) {
            panel.featureEditOperation = MapPanel.EDIT_OP_MOVE_VERTEX;
        }

        if (syncOpenTables) {
            OpenAttributeTableAction.syncSelectionFromMap(layer, normalizedIds);
        }
        if (primaryFeature != null) {
            startSelectionFlash(layer, primaryFeature);
        }
        if (statusMessage != null && !statusMessage.isBlank()) {
            panel.showCopiedMessage(statusMessage);
        }
        panel.refreshEditingUi();
        return true;
    }

    List<String> mergeSelectionIds(Layer layer, List<String> currentIds, List<String> candidateIds, boolean toggleSingleCandidate) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (currentIds != null) {
            merged.addAll(currentIds);
        }
        if (candidateIds == null || candidateIds.isEmpty()) {
            return new ArrayList<>(merged);
        }

        if (toggleSingleCandidate && candidateIds.size() == 1) {
            String candidate = candidateIds.get(0);
            if (merged.contains(candidate)) {
                merged.remove(candidate);
            } else {
                merged.add(candidate);
            }
        } else {
            merged.addAll(candidateIds);
        }
        return new ArrayList<>(merged);
    }

    void identifyFeature(int screenX, int screenY) {
        List<IdentifyResultItem> hits = collectIdentifyResults(screenX, screenY);
        if (panel.activeVectorEditingLayer != null) {
            List<IdentifyResultItem> filteredHits = new ArrayList<>();
            for (IdentifyResultItem item : hits) {
                if (item != null && item.getLayer() == panel.activeVectorEditingLayer) {
                    filteredHits.add(item);
                }
            }
            hits = filteredHits;
        }

        if (hits.isEmpty()) {
            clearSelectedFeature();
            panel.showCopiedMessage("No se identific\u00f3 ninguna entidad.");
            NotificationManager.warn(panel, null, "No se identific\u00f3 ninguna entidad.");
            panel.repaint();
            return;
        }

        if (hits.size() == 1) {
            IdentifyResultItem hit = hits.get(0);
            highlightIdentifiedFeature(hit.getLayer(), hit.getFeature());
            panel.showCopiedMessage("1 entidad identificada en capa: " + (hit.getLayer() != null ? hit.getLayer().getName() : "-"));
            panel.showFeatureInfo(hit.getFeature(), hit.getLayer());
            return;
        }

        highlightIdentifiedFeature(hits.get(0).getLayer(), hits.get(0).getFeature());
        panel.showCopiedMessage(hits.size() + " entidades identificadas. Eleg\u00ed una en la ventana de resultados.");
        IdentifyResultsDialog.open(panel, hits);
    }

    void selectFeatureForEditing(int screenX, int screenY, boolean additiveSelection) {
        List<IdentifyResultItem> hits = collectSelectableResults(screenX, screenY);

        if (hits.isEmpty()) {
            if (!additiveSelection) {
                clearSelectedFeature();
                panel.showCopiedMessage("No se seleccion\u00f3 ninguna entidad.");
            }
            panel.repaint();
            return;
        }

        IdentifyResultItem hit = hits.get(0);
        Layer targetLayer = hit.getLayer();
        String featureId = hit.getFeature() != null ? hit.getFeature().getID() : null;
        if (targetLayer == null || featureId == null) {
            return;
        }

        List<String> selectionIds = additiveSelection
                ? mergeSelectionIds(targetLayer, getSelectedFeatureIdsForLayer(targetLayer), List.of(featureId), true)
                : List.of(featureId);

        String message = additiveSelection
                ? (selectionIds.size() == 1
                ? "1 entidad seleccionada."
                : selectionIds.size() + " entidades seleccionadas.")
                : "Entidad seleccionada para edici\u00f3n: " + targetLayer.getName();
        applyFeatureSelection(targetLayer, selectionIds, !additiveSelection, true, true, message);
    }

    void selectFeatureForEditing(Rectangle selectionBounds, boolean additiveSelection) {
        List<IdentifyResultItem> hits = collectSelectableResults(selectionBounds);

        if (hits.isEmpty()) {
            if (!additiveSelection) {
                clearSelectedFeature();
                panel.showCopiedMessage("No se encontr\u00f3 ninguna entidad dentro del rect\u00e1ngulo.");
            }
            panel.repaint();
            return;
        }

        Layer targetLayer = hits.get(0).getLayer();
        List<String> selectionIds = new ArrayList<>();
        for (IdentifyResultItem item : hits) {
            if (item != null && item.getLayer() == targetLayer && item.getFeature() != null && item.getFeature().getID() != null) {
                selectionIds.add(item.getFeature().getID());
            }
        }
        if (additiveSelection) {
            selectionIds = mergeSelectionIds(targetLayer, getSelectedFeatureIdsForLayer(targetLayer), selectionIds, false);
        }

        String message = selectionIds.size() == 1
                ? "Entidad seleccionada con ventana de captura."
                : selectionIds.size() + " entidades seleccionadas con ventana de captura.";
        applyFeatureSelection(targetLayer, selectionIds, !additiveSelection && selectionIds.size() == 1, true, true, message);
    }

    List<IdentifyResultItem> collectIdentifyResults(int screenX, int screenY) {
        List<IdentifyResultItem> hits = new ArrayList<>();

        double tolerancePixels = 6.0;
        double toleranceWorld = tolerancePixels / panel.zoomFactor;

        double worldX = panel.screenToWorldX(screenX);
        double worldY = panel.screenToWorldY(screenY);

        Point clickPoint = new org.locationtech.jts.geom.GeometryFactory()
                .createPoint(new Coordinate(worldX, worldY));

        collectPointProbeHits(panel.layerManager.getHitTestLayers(false), clickPoint, toleranceWorld, hits, "Error durante identificacion puntual sobre capa ");

        return hits;
    }

    List<IdentifyResultItem> collectSelectableResults(int screenX, int screenY) {
        List<IdentifyResultItem> hits = new ArrayList<>();

        double tolerancePixels = 6.0;
        double toleranceWorld = tolerancePixels / panel.zoomFactor;

        double worldX = panel.screenToWorldX(screenX);
        double worldY = panel.screenToWorldY(screenY);

        Point clickPoint = panel.selectionGeometryFactory.createPoint(new Coordinate(worldX, worldY));

        collectPointProbeHits(panel.layerManager.getHitTestLayers(true), clickPoint, toleranceWorld, hits, "Error durante seleccion puntual sobre capa ");

        sortSelectableHits(hits, clickPoint);
        return hits;
    }

    List<IdentifyResultItem> collectSelectableResults(Rectangle selectionBounds) {
        List<IdentifyResultItem> hits = new ArrayList<>();
        if (selectionBounds == null || selectionBounds.width <= 0 || selectionBounds.height <= 0) {
            return hits;
        }

        double minWorldX = panel.screenToWorldX(selectionBounds.x);
        double maxWorldX = panel.screenToWorldX(selectionBounds.x + selectionBounds.width);
        double maxWorldY = panel.screenToWorldY(selectionBounds.y);
        double minWorldY = panel.screenToWorldY(selectionBounds.y + selectionBounds.height);
        Envelope envelope = new Envelope(
                Math.min(minWorldX, maxWorldX),
                Math.max(minWorldX, maxWorldX),
                Math.min(minWorldY, maxWorldY),
                Math.max(minWorldY, maxWorldY)
        );
        Geometry selectionArea = panel.selectionGeometryFactory.toGeometry(envelope);

        forEachVisibleFeatureGeometry(panel.layerManager.getHitTestLayers(true), "Error durante seleccion por rectangulo sobre capa ", (layer, featureGeometry) -> {
            if (featureGeometry.geometry().intersects(selectionArea) || selectionArea.contains(featureGeometry.geometry())) {
                hits.add(new IdentifyResultItem(layer, featureGeometry.feature(), featureGeometry.geometry()));
            }
        });

        sortSelectableHits(hits, selectionArea.getCentroid());
        return hits;
    }

    private void collectPointProbeHits(List<Layer> layers,
                                       Point clickPoint,
                                       double toleranceWorld,
                                       List<IdentifyResultItem> hits,
                                       String failurePrefix) {
        forEachVisibleFeatureGeometry(layers, failurePrefix, (layer, featureGeometry) -> {
            Geometry geometry = featureGeometry.geometry();
            boolean hit = geometry instanceof Point || geometry instanceof org.locationtech.jts.geom.MultiPoint
                    ? geometry.isWithinDistance(clickPoint, toleranceWorld)
                    : geometry.buffer(toleranceWorld).contains(clickPoint);
            if (hit) {
                hits.add(new IdentifyResultItem(layer, featureGeometry.feature(), geometry));
            }
        });
    }

    void forEachVisibleFeatureGeometry(List<Layer> layers,
                                       String failurePrefix,
                                       BiConsumer<Layer, SelectionManager2.FeatureGeometryRef> consumer) {
        if (layers == null || consumer == null) {
            return;
        }
        for (Layer layer : layers) {
            if (!panel.layerManager.isLayerEffectivelyVisible(layer)) {
                continue;
            }

            ShapefileData data = panel.shapefileLayers.get(layer);
            if (data == null || data.getFeatureCollection() == null) {
                continue;
            }

            try (org.geotools.feature.FeatureIterator<SimpleFeature> iterator = data.getFeatureCollection().features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    if (!panel.isFeatureVisibleInLayer(layer, feature)) {
                        continue;
                    }
                    Object geomObj = feature.getDefaultGeometry();
                    if (!(geomObj instanceof Geometry)) {
                        continue;
                    }

                    Geometry geometry = panel.reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
                    if (geometry == null || geometry.isEmpty()) {
                        continue;
                    }

                    consumer.accept(layer, new SelectionManager2.FeatureGeometryRef(feature, geometry));
                }
            } catch (Exception ex) {
                AppErrorSupport.logFailure((failurePrefix != null ? failurePrefix : "Error de capa ") + layer.getName(), ex);
            }
        }
    }

    private void sortSelectableHits(List<IdentifyResultItem> hits, Geometry referenceGeometry) {
        if (hits == null || hits.size() < 2 || referenceGeometry == null) {
            return;
        }
        hits.sort((left, right) -> Double.compare(
                selectableHitDistance(left, referenceGeometry),
                selectableHitDistance(right, referenceGeometry)
        ));
    }

    private double selectableHitDistance(IdentifyResultItem item, Geometry referenceGeometry) {
        if (item == null || item.getGeometry() == null || referenceGeometry == null) {
            return Double.MAX_VALUE;
        }

        double distance = item.getGeometry().distance(referenceGeometry);
        if (panel.selectedLayer != null
                && panel.selectedFeature != null
                && item.getLayer() == panel.selectedLayer
                && panel.sameFeatureId(item.getFeature(), panel.selectedFeature)) {
            distance += 0.000001d;
        }
        return distance;
    }

    boolean isHitOnCurrentSelection(int screenX, int screenY) {
        if (panel.selectedLayer == null || !panel.hasFeatureSelection()) {
            return false;
        }
        List<String> selectedIds = getSelectedFeatureIdsForLayer(panel.selectedLayer);
        if (selectedIds.isEmpty()) {
            return false;
        }

        List<IdentifyResultItem> hits = collectSelectableResults(screenX, screenY);
        for (IdentifyResultItem hit : hits) {
            if (hit != null
                    && hit.getLayer() == panel.selectedLayer
                    && hit.getFeature() != null
                    && selectedIds.contains(hit.getFeature().getID())) {
                return true;
            }
        }
        return false;
    }

    void highlightIdentifiedFeature(Layer layer, SimpleFeature feature) {
        panel.selectedLayer = layer;
        panel.selectedFeature = feature;
        if (layer != null && feature != null) {
            panel.tableSelectionIds.clear();
            panel.tableSelectionIds.put(layer, new ArrayList<>(List.of(feature.getID())));
            OpenAttributeTableAction.syncSelectionFromMap(layer, List.of(feature.getID()));
            startSelectionFlash(layer, feature);
        }
        CatgisDesktopApp.syncFloatingVectorEditToolbar();
        panel.repaint();
    }

    void clearSelectedFeatureInternal(boolean syncOpenTables) {
        panel.selectionFlashGeometry = null;
        panel.selectionFlashTimer.stop();
        panel.tableSelectionIds.clear();
        if (syncOpenTables) {
            OpenAttributeTableAction.clearSelectionInOpenTables();
        }
        panel.selectedLayer = panel.activeVectorEditingLayer;
        panel.selectedFeature = null;
        panel.featureEditMode = false;
        panel.featureEditOperation = MapPanel.EDIT_OP_MOVE_VERTEX;
        panel.featureEditSketchCoordinates.clear();
        panel.featureEditOriginalGeometry = null;
        panel.featureEditDirty = false;
        panel.activeEditVertexIndex = -1;
        panel.joinTargetVertexIndex = -1;
        panel.clearAdjacentPolygonState();
        panel.clearCadConstructionState();
        panel.movingSelectedFeatures = false;
        panel.moveSelectionLastProjectX = Double.NaN;
        panel.moveSelectionLastProjectY = Double.NaN;
        panel.undoRedoManager.clear();
        panel.refreshEditingUi();
    }

    public void clearSelectedFeature() {
        if (!panel.confirmPendingFeatureEdit("limpiar la selecci\u00f3n")) {
            return;
        }
        clearSelectedFeatureInternal(true);
    }

    void syncSelectionFromAttributeTable(Layer layer, List<String> featureIds) {
        if (layer == null) {
            return;
        }

        panel.tableSelectionIds.clear();
        if (featureIds == null || featureIds.isEmpty()) {
            if (!panel.featureEditMode && layer == panel.selectedLayer) {
                panel.selectedFeature = null;
            }
        } else {
            panel.tableSelectionIds.put(layer, new ArrayList<>(featureIds));
            if (!panel.featureEditMode) {
                panel.selectedLayer = layer;
                ShapefileData data = panel.getShapefileData(layer);
                panel.selectedFeature = data != null ? panel.findFeatureById(data.getFeatures(), featureIds.get(0)) : null;
                if (panel.selectedFeature != null) {
                    startSelectionFlash(layer, panel.selectedFeature);
                }
            }
        }

        CatgisDesktopApp.syncFloatingVectorEditToolbar();
        panel.repaint();
    }

    void startSelectionFlash(Layer layer, SimpleFeature feature) {
        if (layer == null || feature == null) {
            return;
        }

        Object geomObj = feature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return;
        }

        Geometry geometry = panel.reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        panel.selectionFlashGeometry = geometry;
        panel.selectionFlashStartedAt = System.currentTimeMillis();
        if (!panel.selectionFlashTimer.isRunning()) {
            panel.selectionFlashTimer.start();
        }
        panel.repaint();
    }

    List<Layer> getHitTestLayers(boolean preferEditingLayer) {
        return panel.layerManager.getHitTestLayers(preferEditingLayer);
    }

    record FeatureGeometryRef(SimpleFeature feature, Geometry geometry) {
    }
}
