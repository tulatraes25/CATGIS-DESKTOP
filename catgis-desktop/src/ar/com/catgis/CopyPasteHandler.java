package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.List;

class CopyPasteHandler {

    private final MapPanel map;

    private SimpleFeature copiedFeature = null;
    private final List<SimpleFeature> copiedFeatures = new ArrayList<>();

    CopyPasteHandler(MapPanel map) {
        this.map = map;
    }

    boolean hasCopiedFeature() {
        return copiedFeature != null || !copiedFeatures.isEmpty();
    }

    void copySelectedFeature() {
        copySelectedFeatures();
    }

    void copySelectedFeatures() {
        captureSelectedFeaturesForCopy(false);
    }

    boolean cutSelectedFeatures() {
        if (map.selectedLayer == null) {
            return false;
        }

        List<String> selectedIds = map.getSelectedFeatureIdsForLayer(map.selectedLayer);
        if (selectedIds.isEmpty()) {
            return false;
        }

        copySelectedFeatures();
        boolean deleted = map.deleteSelectedFeatures();
        if (deleted) {
            map.showCopiedMessage(selectedIds.size() == 1 ? "Entidad cortada." : selectedIds.size() + " entidades cortadas.");
        }
        return deleted;
    }

    boolean pasteCopiedFeatures() {
        Layer targetLayer = map.getEditingLayerRef();
        if ((!hasCopiedFeature()) || targetLayer == null) {
            return false;
        }
        if (map.isReadOnlyVectorLayer(targetLayer)) {
            NotificationManager.warn(map, "Pegar entidades", map.getReadOnlyLayerMessage(targetLayer));
            return false;
        }

        ShapefileData targetData = map.getShapefileData(targetLayer);
        if (targetData == null) {
            return false;
        }

        List<SimpleFeature> sources = copiedFeatures.isEmpty() && copiedFeature != null
                ? List.of(copiedFeature)
                : new ArrayList<>(copiedFeatures);
        List<SimpleFeature> features = new ArrayList<>(targetData.getFeatures());
        List<String> pastedIds = new ArrayList<>();
        for (SimpleFeature sourceFeature : sources) {
            SimpleFeature pasted = buildPastedFeature(sourceFeature, targetLayer, features, true);
            if (pasted == null) {
                continue;
            }
            features.add(pasted);
            pastedIds.add(pasted.getID());
        }

        if (pastedIds.isEmpty()) {
            NotificationManager.warn(map, "Pegar entidades",
                    "Por ahora solo se puede pegar en una capa compatible con la estructura de los elementos copiados.");
            return false;
        }

        map.pushUndoSnapshot(targetLayer, null);
        map.replaceLayerFeatures(targetLayer, features, pastedIds.get(0), pastedIds.size() == 1, null);
        map.applyFeatureSelection(targetLayer, pastedIds, pastedIds.size() == 1, true, false,
                pastedIds.size() == 1 ? "Entidad pegada." : pastedIds.size() + " entidades pegadas.");
        return true;
    }

    boolean pasteCopiedFeature() {
        return pasteCopiedFeatures();
    }

    boolean copySelectedFeaturesToEditingLayer() {
        if (map.selectedLayer == null) {
            return false;
        }

        Layer targetLayer = map.getEditingLayerRef();
        if (targetLayer == null && map.selectedLayer != null && !(map.selectedLayer instanceof RasterLayer)) {
            map.prepareLayerForEditing(map.selectedLayer);
            targetLayer = map.getEditingLayerRef();
        }
        if (targetLayer == null) {
            return false;
        }
        if (map.isReadOnlyVectorLayer(targetLayer)) {
            NotificationManager.warn(map, "Pegar entidades", map.getReadOnlyLayerMessage(targetLayer));
            return false;
        }

        captureSelectedFeaturesForCopy(true);
        if (!hasCopiedFeature()) {
            return false;
        }

        return pasteCopiedFeatures();
    }

    boolean copySelectedFeaturesFromLayerToEditingLayer(Layer sourceLayer) {
        if (sourceLayer == null || sourceLayer instanceof RasterLayer) {
            NotificationManager.warn(map, "Copiar a capa en edicion",
                    "Selecciona una capa vectorial con entidades seleccionadas.");
            return false;
        }

        Layer targetLayer = map.getEditingLayerRef();
        if (targetLayer == null) {
            NotificationManager.warn(map, "Copiar a capa en edicion",
                    "No hay una capa vectorial en edicion activa. Primero activa Editar vector en la capa destino.");
            return false;
        }
        if (sourceLayer == targetLayer) {
            NotificationManager.warn(map, "Copiar a capa en edicion",
                    "La capa seleccionada ya es la capa en edicion. Usa copiar/pegar normal para duplicar dentro de la misma capa.");
            return false;
        }
        if (map.isReadOnlyVectorLayer(targetLayer)) {
            NotificationManager.warn(map, "Pegar entidades", map.getReadOnlyLayerMessage(targetLayer));
            return false;
        }

        ShapefileData sourceData = map.getShapefileData(sourceLayer);
        ShapefileData targetData = map.getShapefileData(targetLayer);
        if (sourceData == null || sourceData.getSchema() == null || targetData == null || targetData.getSchema() == null) {
            NotificationManager.warn(map, "Copiar a capa en edicion",
                    "La capa fuente o destino no tiene esquema vectorial disponible.");
            return false;
        }

        String sourceFamily = map.resolveGeometryFamily(sourceData.getSchema());
        String targetFamily = map.resolveGeometryFamily(targetData.getSchema());
        if (sourceFamily.isBlank() || !sourceFamily.equals(targetFamily)) {
            NotificationManager.warn(map, "Copiar a capa en edicion",
                    "La geometria no es compatible. Fuente: " + sourceFamily + " | Destino: " + targetFamily + ".");
            return false;
        }

        List<String> selectedIds = map.getSelectedFeatureIdsForLayer(sourceLayer);
        if (selectedIds.isEmpty()) {
            NotificationManager.warn(map, "Copiar a capa en edicion",
                    "No hay entidades seleccionadas en la capa fuente.");
            return false;
        }

        List<SimpleFeature> targetFeatures = new ArrayList<>(targetData.getFeatures());
        List<String> pastedIds = new ArrayList<>();
        for (SimpleFeature sourceFeature : collectSelectedFeatures(sourceData.getFeatures(), selectedIds)) {
            SimpleFeature pasted = buildPastedFeature(sourceFeature, targetLayer, targetFeatures, false);
            if (pasted == null) {
                continue;
            }
            targetFeatures.add(pasted);
            pastedIds.add(pasted.getID());
        }

        if (pastedIds.isEmpty()) {
            NotificationManager.warn(map, "Copiar a capa en edicion",
                    "No se pudieron copiar las entidades seleccionadas a la capa en edicion.");
            return false;
        }

        map.pushUndoSnapshot(targetLayer, null);
        map.replaceLayerFeatures(targetLayer, targetFeatures, pastedIds.get(0), pastedIds.size() == 1, null);
        map.applyFeatureSelection(targetLayer, pastedIds, pastedIds.size() == 1, true, false,
                pastedIds.size() == 1
                        ? "Entidad copiada a la capa en edicion."
                        : pastedIds.size() + " entidades copiadas a la capa en edicion.");
        return true;
    }

    private void captureSelectedFeaturesForCopy(boolean silent) {
        if (map.selectedLayer == null) {
            return;
        }

        ShapefileData data = map.getShapefileData(map.selectedLayer);
        List<String> selectedIds = map.getSelectedFeatureIdsForLayer(map.selectedLayer);
        if (data == null || selectedIds.isEmpty()) {
            return;
        }

        copiedFeatures.clear();
        for (String featureId : selectedIds) {
            SimpleFeature feature = map.findFeatureById(data.getFeatures(), featureId);
            if (feature != null) {
                copiedFeatures.add(map.cloneFeature(feature, map.extractFeatureGeometryCopy(feature), feature.getID()));
            }
        }
        copiedFeature = copiedFeatures.isEmpty() ? null : copiedFeatures.get(0);
        if (!silent) {
            map.showCopiedMessage(copiedFeatures.size() == 1 ? "Entidad copiada." : copiedFeatures.size() + " entidades copiadas.");
        }
        map.refreshEditingUi();
    }

    private SimpleFeature buildPastedFeature(SimpleFeature sourceFeature,
                                             Layer targetLayer,
                                             List<SimpleFeature> existingFeatures,
                                             boolean offsetGeometry) {
        if (sourceFeature == null || targetLayer == null) {
            return null;
        }

        List<SimpleFeature> targetFeatures = existingFeatures != null ? existingFeatures : new ArrayList<>();
        ShapefileData targetData = map.getShapefileData(targetLayer);
        SimpleFeatureType targetType = targetData != null ? targetData.getSchema() : null;
        if (targetType == null && !targetFeatures.isEmpty()) {
            targetType = targetFeatures.get(0).getFeatureType();
        }
        if (targetType == null) {
            return null;
        }

        Geometry pastedGeometry = map.extractFeatureGeometryCopy(sourceFeature);
        if (pastedGeometry == null) {
            return null;
        }

        if (offsetGeometry) {
            offsetGeometryForPaste(pastedGeometry);
        }
        Geometry adaptedGeometry = map.adaptGeometryForFeatureSchema(pastedGeometry, targetType);
        if (adaptedGeometry == null) {
            return null;
        }

        org.geotools.feature.simple.SimpleFeatureBuilder builder = new org.geotools.feature.simple.SimpleFeatureBuilder(targetType);
        int attributeCount = targetType.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            String attrName = targetType.getDescriptor(i).getLocalName();
            Object sourceValue = null;
            if (targetType.getDescriptor(i).equals(targetType.getGeometryDescriptor())) {
                sourceValue = adaptedGeometry;
            } else if (sourceFeature.getFeatureType().getDescriptor(attrName) != null) {
                sourceValue = sourceFeature.getAttribute(attrName);
            }
            builder.add(sourceValue);
        }

        return builder.buildFeature(map.buildNextFeatureId(targetFeatures));
    }

    private void offsetGeometryForPaste(Geometry geometry) {
        MapGeometryUtils.offsetGeometryForPaste(geometry);
    }

    private List<SimpleFeature> collectSelectedFeatures(List<SimpleFeature> features, List<String> selectedIds) {
        List<SimpleFeature> selected = new ArrayList<>();
        if (features == null || selectedIds == null || selectedIds.isEmpty()) {
            return selected;
        }

        java.util.LinkedHashSet<String> orderedIds = new java.util.LinkedHashSet<>(selectedIds);
        for (SimpleFeature feature : features) {
            if (feature != null && orderedIds.contains(feature.getID())) {
                selected.add(feature);
            }
        }
        return selected;
    }
}
