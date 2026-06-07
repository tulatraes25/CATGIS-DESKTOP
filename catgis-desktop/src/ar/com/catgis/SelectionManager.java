package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.util.*;

/**
 * Manages feature selection state on the map.
 * Extracted from MapPanel to reduce its responsibilities.
 */
public class SelectionManager {

    private final Set<String> selectedFeatureIds = new LinkedHashSet<>();
    private Layer selectedLayer = null;
    private SimpleFeature selectedFeature = null;
    private List<SimpleFeature> selectedFeatures = new ArrayList<>();

    private Runnable selectionChangeCallback;

    // --- Getters ---

    public Set<String> getSelectedFeatureIds() { return Collections.unmodifiableSet(selectedFeatureIds); }
    public Layer getSelectedLayer() { return selectedLayer; }
    public SimpleFeature getSelectedFeature() { return selectedFeature; }
    public List<SimpleFeature> getSelectedFeatures() { return Collections.unmodifiableList(selectedFeatures); }
    public boolean hasSelection() { return !selectedFeatureIds.isEmpty(); }

    // --- Setters ---

    public void setSelectionChangeCallback(Runnable callback) { this.selectionChangeCallback = callback; }

    // --- Selection operations ---

    public void selectFeature(Layer layer, SimpleFeature feature) {
        clearSelection();
        if (layer == null || feature == null) return;
        selectedLayer = layer;
        selectedFeature = feature;
        String id = extractFeatureId(feature);
        if (id != null) selectedFeatureIds.add(id);
        selectedFeatures.add(feature);
        notifyChange();
    }

    public void selectFeatures(Layer layer, List<SimpleFeature> features) {
        clearSelection();
        if (layer == null || features == null || features.isEmpty()) return;
        selectedLayer = layer;
        selectedFeatures = new ArrayList<>(features);
        for (SimpleFeature f : features) {
            String id = extractFeatureId(f);
            if (id != null) selectedFeatureIds.add(id);
        }
        selectedFeature = features.get(0);
        notifyChange();
    }

    public void addToSelection(Layer layer, SimpleFeature feature) {
        if (feature == null) return;
        if (selectedLayer != null && selectedLayer != layer) {
            clearSelection();
        }
        selectedLayer = layer;
        String id = extractFeatureId(feature);
        if (id != null && !selectedFeatureIds.contains(id)) {
            selectedFeatureIds.add(id);
            selectedFeatures.add(feature);
            selectedFeature = feature;
            notifyChange();
        }
    }

    public void clearSelection() {
        selectedFeatureIds.clear();
        selectedLayer = null;
        selectedFeature = null;
        selectedFeatures = new ArrayList<>();
        notifyChange();
    }

    // --- Box selection ---

    public void selectByEnvelope(Layer layer, Envelope envelope, MapPanel mapPanel) {
        if (layer == null || envelope == null) return;
        ShapefileData data = mapPanel.getShapefileData(layer);
        if (data == null) return;

        List<SimpleFeature> found = new ArrayList<>();
        for (SimpleFeature feature : data.getFeatures()) {
            Object geomObj = feature.getDefaultGeometry();
            if (geomObj instanceof Geometry geom) {
                if (geom.getEnvelopeInternal().intersects(envelope)) {
                    found.add(feature);
                }
            }
        }
        if (!found.isEmpty()) {
            selectFeatures(layer, found);
        }
    }

    // --- Utility ---

    private String extractFeatureId(SimpleFeature feature) {
        if (feature == null) return null;
        try {
            Object id = feature.getID();
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void notifyChange() {
        if (selectionChangeCallback != null) selectionChangeCallback.run();
    }
}
