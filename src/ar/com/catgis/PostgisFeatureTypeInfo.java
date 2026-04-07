package ar.com.catgis;

public class PostgisFeatureTypeInfo {

    private final String typeName;
    private final String schemaName;
    private final String tableName;
    private final String geometryTypeLabel;
    private final String crsCode;

    public PostgisFeatureTypeInfo(String typeName,
                                  String schemaName,
                                  String tableName,
                                  String geometryTypeLabel,
                                  String crsCode) {
        this.typeName = typeName != null ? typeName : "";
        this.schemaName = schemaName != null ? schemaName : "";
        this.tableName = tableName != null ? tableName : "";
        this.geometryTypeLabel = geometryTypeLabel != null ? geometryTypeLabel : "";
        this.crsCode = crsCode != null ? crsCode : "";
    }

    public String getTypeName() {
        return typeName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getGeometryTypeLabel() {
        return geometryTypeLabel;
    }

    public String getCrsCode() {
        return crsCode;
    }

    public String getDisplayLabel() {
        StringBuilder sb = new StringBuilder();
        String qualifiedName = !schemaName.isBlank() ? schemaName + "." + tableName : tableName;
        sb.append(qualifiedName.isBlank() ? typeName : qualifiedName);
        if (!geometryTypeLabel.isBlank()) {
            sb.append(" | ").append(geometryTypeLabel);
        }
        if (!crsCode.isBlank()) {
            sb.append(" | ").append(crsCode);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getDisplayLabel();
    }
}
