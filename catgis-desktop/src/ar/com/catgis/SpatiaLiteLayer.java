package ar.com.catgis;

import ar.com.catgis.core.model.Layer;

/**
 * Layer model for SpatiaLite data sources.
 * SpatiaLite is a spatial extension for SQLite databases.
 */
public class SpatiaLiteLayer extends Layer {

    private String tableName = "";
    private String geometryTypeLabel = "";
    private String resolvedCrs = "";

    public SpatiaLiteLayer(String name, String path) {
        super(name, path, "SPATIALITE");
    }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName != null ? tableName.trim() : ""; refreshPath(); }

    public String getGeometryTypeLabel() { return geometryTypeLabel; }
    public void setGeometryTypeLabel(String label) { this.geometryTypeLabel = label != null ? label : ""; }

    public String getResolvedCrs() { return resolvedCrs; }
    public void setResolvedCrs(String crs) { this.resolvedCrs = crs != null ? crs : ""; }

    private void refreshPath() {
        String path = getPath();
        if (path == null) path = "";
        String base = path.contains("spatialite://") ? path.substring(path.indexOf("spatialite://") + 13) : path;
        setPath("spatialite://" + base + "#" + tableName);
    }
}
