package ar.com.catgis;

public enum ToolboxOutputType {
    VECTOR_LAYER("Nueva capa vectorial"),
    REPORT("Reporte"),
    TABLE("Tabla");

    private final String displayName;

    ToolboxOutputType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
