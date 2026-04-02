package ar.com.catgis;

public class FieldConfig {

    private final String fieldName;
    private String typeName;
    private String publicName;
    private boolean visible = true;
    private boolean editable = true;

    public FieldConfig(String fieldName, String typeName) {
        this.fieldName = fieldName;
        this.typeName = normalizeTypeName(typeName);
        this.publicName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getTypeName() {
        return normalizeTypeName(typeName);
    }

    public void setTypeName(String typeName) {
        this.typeName = normalizeTypeName(typeName);
    }

    public String getPublicName() {
        return publicName != null && !publicName.isBlank() ? publicName : fieldName;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName != null ? publicName.trim() : fieldName;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public static String normalizeTypeName(String typeName) {
        if (typeName == null || typeName.isBlank()) {
            return "String";
        }

        String t = typeName.trim();
        switch (t.toLowerCase()) {
            case "string":
            case "java.lang.string":
                return "String";
            case "integer":
            case "int":
            case "java.lang.integer":
                return "Integer";
            case "long":
            case "java.lang.long":
                return "Long";
            case "double":
            case "java.lang.double":
                return "Double";
            case "float":
            case "java.lang.float":
                return "Float";
            case "boolean":
            case "bool":
            case "java.lang.boolean":
                return "Boolean";
            case "date":
            case "java.util.date":
            case "java.sql.date":
                return "Date";
            case "timestamp":
            case "java.sql.timestamp":
                return "Timestamp";
            default:
                return t;
        }
    }
}