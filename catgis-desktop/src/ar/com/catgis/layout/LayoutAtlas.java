package ar.com.catgis.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic atlas/map series support.
 * Generates one layout per feature in a coverage layer.
 */
public class LayoutAtlas {

    private String coverageLayerName;
    private String pageNameField;
    private boolean enabled = false;
    private int currentPage = 0;
    private final List<String> pageNames = new ArrayList<>();

    public void setCoverageLayer(String name, String nameField) {
        this.coverageLayerName = name;
        this.pageNameField = nameField;
    }

    public String getCoverageLayerName() { return coverageLayerName; }
    public String getPageNameField() { return pageNameField; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { enabled = e; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int p) { currentPage = Math.max(0, p); }
    public List<String> getPageNames() { return pageNames; }

    public int getPageCount() { return pageNames.size(); }
    public void setPageNames(List<String> names) { pageNames.clear(); if (names != null) pageNames.addAll(names); }

    public boolean hasNext() { return enabled && currentPage < pageNames.size() - 1; }
    public boolean hasPrev() { return enabled && currentPage > 0; }

    public void nextPage() { if (hasNext()) currentPage++; }
    public void prevPage() { if (hasPrev()) currentPage--; }

    public String getCurrentPageName() {
        if (!enabled || pageNames.isEmpty() || currentPage >= pageNames.size()) return "";
        return pageNames.get(currentPage);
    }
}
