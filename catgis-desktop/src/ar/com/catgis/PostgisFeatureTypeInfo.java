package ar.com.catgis;

public class PostgisFeatureTypeInfo {

    private final String typeName;
    private final String schemaName;
    private final String tableName;
    private final String displayName;
    private final String geometryTypeLabel;
    private final String crsCode;
    private final boolean writable;
    private final boolean loadByDefault;

    public PostgisFeatureTypeInfo(String typeName,
                                  String schemaName,
                                  String tableName,
                                  String geometryTypeLabel,
                                  String crsCode) {
        this(typeName, schemaName, tableName, tableName, geometryTypeLabel, crsCode, false, false);
    }

    public PostgisFeatureTypeInfo(String typeName,
                                  String schemaName,
                                  String tableName,
                                  String geometryTypeLabel,
                                  String crsCode,
                                  boolean writable) {
        this(typeName, schemaName, tableName, tableName, geometryTypeLabel, crsCode, writable, false);
    }

    public PostgisFeatureTypeInfo(String typeName,
                                  String schemaName,
                                  String tableName,
                                  String displayName,
                                  String geometryTypeLabel,
                                  String crsCode,
                                  boolean writable,
                                  boolean loadByDefault) {
        this.typeName = typeName != null ? typeName : "";
        this.schemaName = schemaName != null ? schemaName : "";
        this.tableName = tableName != null ? tableName : "";
        this.displayName = displayName != null ? displayName : "";
        this.geometryTypeLabel = geometryTypeLabel != null ? geometryTypeLabel : "";
        this.crsCode = crsCode != null ? crsCode : "";
        this.writable = writable;
        this.loadByDefault = loadByDefault;
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

    public String getDisplayName() {
        return displayName;
    }

    public String getGeometryTypeLabel() {
        return geometryTypeLabel;
    }

    public String getCrsCode() {
        return crsCode;
    }

    public boolean isWritable() {
        return writable;
    }

    public boolean isLoadByDefault() {
        return loadByDefault;
    }

    public String getDisplayLabel() {
        StringBuilder sb = new StringBuilder();
        String qualifiedName = !schemaName.isBlank() ? schemaName + "." + tableName : tableName;
        String resolvedDisplayName = !displayName.isBlank() ? displayName : (qualifiedName.isBlank() ? typeName : qualifiedName);
        sb.append(resolvedDisplayName);
        if (!qualifiedName.isBlank() && !qualifiedName.equalsIgnoreCase(resolvedDisplayName)) {
            sb.append(" | ").append(qualifiedName);
        }
        if (!geometryTypeLabel.isBlank()) {
            sb.append(" | ").append(geometryTypeLabel);
        }
        if (!crsCode.isBlank()) {
            sb.append(" | ").append(crsCode);
        }
        if (loadByDefault) {
            sb.append(" | recomendado");
        }
        sb.append(" | ").append(writable ? "editable" : "solo lectura");
        return sb.toString();
    }

    @Override
    public String toString() {
        return getDisplayLabel();
    }
}
