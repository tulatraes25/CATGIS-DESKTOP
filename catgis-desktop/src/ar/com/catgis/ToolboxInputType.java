package ar.com.catgis;

public enum ToolboxInputType {
    VECTOR_ANY("Vectorial"),
    VECTOR_POINT("Puntos"),
    VECTOR_LINE("Lineas"),
    VECTOR_POLYGON("Poligonos"),
    NONE("Sin entrada");

    private final String displayName;

    ToolboxInputType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
