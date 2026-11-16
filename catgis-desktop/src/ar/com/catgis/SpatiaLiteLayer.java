package ar.com.catgis;

import ar.com.catgis.core.model.Layer;

/**
 * Layer model for one spatial table inside a SQLite / SpatiaLite database.
 * The inherited path is always the real database file path; table identity is
 * stored separately so file I/O never receives a pseudo URI.
 */
public class SpatiaLiteLayer extends Layer {

    private String tableName = "";
    private String geometryTypeLabel = "";
    private String resolvedCrs = "";

    public SpatiaLiteLayer(String name, String path) {
        super(name, extractDatabasePath(path), "SPATIALITE");
        this.tableName = extractLegacyTable(path);
    }

    public String getTableName() { return tableName; }

    public void setTableName(String tableName) {
        this.tableName = tableName != null ? tableName.trim() : "";
    }

    public String getGeometryTypeLabel() { return geometryTypeLabel; }

    public void setGeometryTypeLabel(String label) {
        this.geometryTypeLabel = label != null ? label : "";
    }

    public String getResolvedCrs() { return resolvedCrs; }

    public void setResolvedCrs(String crs) {
        this.resolvedCrs = crs != null ? crs : "";
    }

    private static String extractDatabasePath(String path) {
        String value = path != null ? path.trim() : "";
        if (!value.regionMatches(true, 0, "spatialite://", 0, "spatialite://".length())) {
            return value;
        }
        String legacy = value.substring("spatialite://".length());
        int tableSeparator = legacy.lastIndexOf('#');
        return tableSeparator >= 0 ? legacy.substring(0, tableSeparator) : legacy;
    }

    private static String extractLegacyTable(String path) {
        String value = path != null ? path.trim() : "";
        if (!value.regionMatches(true, 0, "spatialite://", 0, "spatialite://".length())) {
            return "";
        }
        String legacy = value.substring("spatialite://".length());
        int tableSeparator = legacy.lastIndexOf('#');
        return tableSeparator >= 0 && tableSeparator + 1 < legacy.length()
                ? legacy.substring(tableSeparator + 1).trim()
                : "";
    }
}
