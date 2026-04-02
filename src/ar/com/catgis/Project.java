package ar.com.catgis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Project {
    private String name;
    private String projectCRS;
    private File projectFile;
    private boolean modified;
    private final List<Layer> layers = new ArrayList<>();

    public Project() {
        this.name = "Proyecto sin nombre";
        this.projectCRS = "EPSG:4326";
        this.modified = false;
    }

    public Project(String name) {
        this.name = name != null ? name : "Proyecto sin nombre";
        this.projectCRS = "EPSG:4326";
        this.modified = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "Proyecto sin nombre";
    }

    public String getProjectCRS() {
        return projectCRS;
    }

    public void setProjectCRS(String projectCRS) {
        String normalized = CRSDefinitions.normalizeCode(projectCRS);
        if (!normalized.isBlank()) {
            this.projectCRS = normalized;
        }
    }

    public File getProjectFile() {
        return projectFile;
    }

    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void addLayer(Layer layer) {
        if (layer != null) {
            layers.add(layer);
        }
    }

    public void removeLayer(Layer layer) {
        layers.remove(layer);
    }

    public void setLayerOrder(List<Layer> orderedLayers) {
        if (orderedLayers == null || orderedLayers.isEmpty()) {
            return;
        }

        List<Layer> reordered = new ArrayList<>();
        for (Layer layer : orderedLayers) {
            if (layer != null && layers.contains(layer) && !reordered.contains(layer)) {
                reordered.add(layer);
            }
        }

        for (Layer layer : layers) {
            if (layer != null && !reordered.contains(layer)) {
                reordered.add(layer);
            }
        }

        layers.clear();
        layers.addAll(reordered);
    }

    public void clearLayers() {
        layers.clear();
    }
}
