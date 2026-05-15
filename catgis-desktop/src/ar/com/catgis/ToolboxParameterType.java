package ar.com.catgis;

public enum ToolboxParameterType {
    LAYER("Capa"),
    DISTANCE("Distancia"),
    TEXT("Texto"),
    BOOLEAN("Booleano"),
    OUTPUT_NAME("Nombre de salida");

    private final String displayName;

    ToolboxParameterType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
