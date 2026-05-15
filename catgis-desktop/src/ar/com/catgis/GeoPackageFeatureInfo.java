package ar.com.catgis;

public class GeoPackageFeatureInfo {

    private final String tableName;
    private final String identifier;
    private final String description;
    private final String geometryTypeLabel;
    private final String crsCode;

    public GeoPackageFeatureInfo(String tableName,
                                 String identifier,
                                 String description,
                                 String geometryTypeLabel,
                                 String crsCode) {
        this.tableName = tableName != null ? tableName : "";
        this.identifier = identifier != null ? identifier : "";
        this.description = description != null ? description : "";
        this.geometryTypeLabel = geometryTypeLabel != null ? geometryTypeLabel : "";
        this.crsCode = CRSDefinitions.normalizeCode(crsCode);
    }

    public String getTableName() {
        return tableName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    public String getGeometryTypeLabel() {
        return geometryTypeLabel;
    }

    public String getCrsCode() {
        return crsCode;
    }

    public String getDisplayLabel() {
        String base = identifier != null && !identifier.isBlank() ? identifier : tableName;
        if (tableName != null && !tableName.isBlank() && !tableName.equals(identifier)) {
            base += " [" + tableName + "]";
        }
        if (geometryTypeLabel != null && !geometryTypeLabel.isBlank()) {
            base += " - " + geometryTypeLabel;
        }
        return base;
    }

    @Override
    public String toString() {
        return getDisplayLabel();
    }
}
