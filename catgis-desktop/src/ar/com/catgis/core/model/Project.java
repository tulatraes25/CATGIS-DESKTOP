package ar.com.catgis.core.model;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.LayerGroup;

import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.CatmapLegendItem;
import ar.com.catgis.CatmapLayoutItem;
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
    private String catmapNorthStyle = "CLASSIC";
    private boolean catmapShowNorth = true;
    private final List<Layer> layers = new ArrayList<>();
    private final List<LayerGroup> layerGroups = new ArrayList<>();
    private final List<CatmapLayoutItem> catmapItems = new ArrayList<>();
    private final List<CatmapLegendItem> catmapLegendItems = new ArrayList<>();

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

    public String getCatmapNorthStyle() {
        return catmapNorthStyle;
    }

    public void setCatmapNorthStyle(String catmapNorthStyle) {
        this.catmapNorthStyle = catmapNorthStyle != null && !catmapNorthStyle.isBlank()
                ? catmapNorthStyle.trim()
                : "CLASSIC";
    }

    public boolean isCatmapShowNorth() {
        return catmapShowNorth;
    }

    public void setCatmapShowNorth(boolean catmapShowNorth) {
        this.catmapShowNorth = catmapShowNorth;
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

    public List<LayerGroup> getLayerGroups() {
        return layerGroups;
    }

    public void clearLayerGroups() {
        layerGroups.clear();
        for (Layer layer : layers) {
            if (layer != null) {
                layer.setGroupName("");
            }
        }
    }

    public LayerGroup getLayerGroup(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (LayerGroup group : layerGroups) {
            if (group != null && name.trim().equalsIgnoreCase(group.getName())) {
                return group;
            }
        }
        return null;
    }

    public LayerGroup addLayerGroup(String name) {
        String unique = buildUniqueGroupName(name);
        LayerGroup group = new LayerGroup(unique);
        layerGroups.add(group);
        return group;
    }

    public void addLayerGroup(LayerGroup group) {
        if (group == null) {
            return;
        }
        String unique = buildUniqueGroupName(group.getName());
        LayerGroup copy = new LayerGroup(group);
        copy.setName(unique);
        layerGroups.add(copy);
    }

    public String buildUniqueGroupName(String requestedName) {
        String base = requestedName != null ? requestedName.trim() : "";
        if (base.isBlank()) {
            base = "Grupo";
        }
        String candidate = base;
        int suffix = 2;
        while (getLayerGroup(candidate) != null) {
            candidate = base + " " + suffix++;
        }
        return candidate;
    }

    public boolean renameLayerGroup(String currentName, String newName) {
        LayerGroup group = getLayerGroup(currentName);
        if (group == null) {
            return false;
        }
        String previous = group.getName();
        String requested = newName != null ? newName.trim() : "";
        String unique;
        if (requested.equalsIgnoreCase(previous)) {
            unique = previous;
        } else {
            unique = buildUniqueGroupName(requested);
        }
        group.setName(unique);
        for (Layer layer : layers) {
            if (layer != null && previous.equalsIgnoreCase(layer.getGroupName())) {
                layer.setGroupName(unique);
            }
        }
        return true;
    }

    public boolean removeLayerGroup(String name, boolean keepLayersUngrouped) {
        LayerGroup group = getLayerGroup(name);
        if (group == null) {
            return false;
        }
        layerGroups.remove(group);
        if (keepLayersUngrouped) {
            for (Layer layer : layers) {
                if (layer != null && group.getName().equalsIgnoreCase(layer.getGroupName())) {
                    layer.setGroupName("");
                }
            }
        }
        return true;
    }

    public void assignLayerToGroup(Layer layer, String groupName) {
        if (layer == null) {
            return;
        }
        if (groupName == null || groupName.isBlank()) {
            layer.setGroupName("");
            return;
        }
        LayerGroup group = getLayerGroup(groupName);
        if (group == null) {
            group = addLayerGroup(groupName);
        }
        layer.setGroupName(group.getName());
    }

    public List<Layer> getLayersForGroup(String groupName) {
        List<Layer> result = new ArrayList<>();
        String normalized = groupName != null ? groupName.trim() : "";
        for (Layer layer : layers) {
            if (layer != null && normalized.equalsIgnoreCase(layer.getGroupName())) {
                result.add(layer);
            }
        }
        return result;
    }

    public List<Layer> getUngroupedLayers() {
        List<Layer> result = new ArrayList<>();
        for (Layer layer : layers) {
            if (layer != null && !layer.isInGroup()) {
                result.add(layer);
            }
        }
        return result;
    }

    public boolean isLayerEffectivelyVisible(Layer layer) {
        if (layer == null || !layer.isVisible()) {
            return false;
        }
        if (!layer.isInGroup()) {
            return true;
        }
        LayerGroup group = getLayerGroup(layer.getGroupName());
        return group == null || group.isVisible();
    }

    public void setLayerGroupOrder(List<String> orderedGroupNames) {
        if (orderedGroupNames == null || orderedGroupNames.isEmpty()) {
            return;
        }

        List<LayerGroup> reordered = new ArrayList<>();
        for (String name : orderedGroupNames) {
            LayerGroup group = getLayerGroup(name);
            if (group != null && !reordered.contains(group)) {
                reordered.add(group);
            }
        }
        for (LayerGroup group : layerGroups) {
            if (group != null && !reordered.contains(group)) {
                reordered.add(group);
            }
        }
        layerGroups.clear();
        layerGroups.addAll(reordered);
    }

    public List<CatmapLayoutItem> getCatmapItems() {
        return catmapItems;
    }

    public void clearCatmapItems() {
        catmapItems.clear();
    }

    public void addCatmapItem(CatmapLayoutItem item) {
        if (item != null) {
            catmapItems.add(new CatmapLayoutItem(item));
        }
    }

    public void setCatmapItems(List<CatmapLayoutItem> items) {
        catmapItems.clear();
        if (items == null) {
            return;
        }
        for (CatmapLayoutItem item : items) {
            if (item != null) {
                catmapItems.add(new CatmapLayoutItem(item));
            }
        }
    }

    public List<CatmapLegendItem> getCatmapLegendItems() {
        return catmapLegendItems;
    }

    public void clearCatmapLegendItems() {
        catmapLegendItems.clear();
    }

    public void addCatmapLegendItem(CatmapLegendItem item) {
        if (item != null) {
            catmapLegendItems.add(new CatmapLegendItem(item));
        }
    }

    public void setCatmapLegendItems(List<CatmapLegendItem> items) {
        catmapLegendItems.clear();
        if (items == null) {
            return;
        }
        for (CatmapLegendItem item : items) {
            if (item != null) {
                catmapLegendItems.add(new CatmapLegendItem(item));
            }
        }
    }
}
