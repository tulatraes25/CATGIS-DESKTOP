package ar.com.catgis;

/**
 * Describes one spatial table in a SpatiaLite database.
 */
public class SpatiaLiteFeatureTypeInfo {

    private final String tableName;
    private final String geometryTypeLabel;
    private final String crsCode;
    private final int srid;
    private final boolean writable;

    public SpatiaLiteFeatureTypeInfo(String tableName, String geometryTypeLabel,
                                      String crsCode, int srid, boolean writable) {
        this.tableName = tableName != null ? tableName : "";
        this.geometryTypeLabel = geometryTypeLabel != null ? geometryTypeLabel : "";
        this.crsCode = crsCode != null ? crsCode : "";
        this.srid = srid;
        this.writable = writable;
    }

    public String getTableName() { return tableName; }
    public String getGeometryTypeLabel() { return geometryTypeLabel; }
    public String getCrsCode() { return crsCode; }
    public int getSrid() { return srid; }
    public boolean isWritable() { return writable; }

    public String getDisplayLabel() {
        StringBuilder sb = new StringBuilder(tableName);
        if (!geometryTypeLabel.isBlank()) sb.append(" | ").append(geometryTypeLabel);
        if (!crsCode.isBlank()) sb.append(" | ").append(crsCode);
        sb.append(" | ").append(writable ? "editable" : "solo lectura");
        return sb.toString();
    }

    @Override
    public String toString() { return getDisplayLabel(); }
}
