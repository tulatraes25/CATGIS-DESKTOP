package ar.com.catgis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Project {
    private String name;
    private String projectCRS;
    private File projectFile;
    private boolean modified;
    private String studyName = "";
    private String companyName = "";
    private String cartographerName = "";
    private String imageSource = "";
    private String coordinateReference = "";
    private String legendTitle = "Leyenda";
    private String legendSubtitle = "Capas visibles del mapa";
    private String logoPath = "";
    private String layoutImagePath = "";
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

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName != null ? studyName.trim() : "";
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName != null ? companyName.trim() : "";
    }

    public String getCartographerName() {
        return cartographerName;
    }

    public void setCartographerName(String cartographerName) {
        this.cartographerName = cartographerName != null ? cartographerName.trim() : "";
    }

    public String getImageSource() {
        return imageSource;
    }

    public void setImageSource(String imageSource) {
        this.imageSource = imageSource != null ? imageSource.trim() : "";
    }

    public String getCoordinateReference() {
        return coordinateReference;
    }

    public void setCoordinateReference(String coordinateReference) {
        this.coordinateReference = coordinateReference != null ? coordinateReference.trim() : "";
    }

    public String getLegendTitle() {
        return legendTitle;
    }

    public void setLegendTitle(String legendTitle) {
        this.legendTitle = legendTitle != null && !legendTitle.isBlank() ? legendTitle.trim() : "Leyenda";
    }

    public String getLegendSubtitle() {
        return legendSubtitle;
    }

    public void setLegendSubtitle(String legendSubtitle) {
        this.legendSubtitle = legendSubtitle != null && !legendSubtitle.isBlank()
                ? legendSubtitle.trim()
                : "Capas visibles del mapa";
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath != null ? logoPath.trim() : "";
    }

    public String getLayoutImagePath() {
        return layoutImagePath;
    }

    public void setLayoutImagePath(String layoutImagePath) {
        this.layoutImagePath = layoutImagePath != null ? layoutImagePath.trim() : "";
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
