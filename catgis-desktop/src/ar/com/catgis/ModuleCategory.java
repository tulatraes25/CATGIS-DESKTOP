package ar.com.catgis;

public enum ModuleCategory {
    CAD("CAD"),
    VALIDATION("Validacion"),
    EXPORT("Exportacion"),
    DATA_SOURCE("Origen de datos"),
    ONLINE_MAPS("Mapas base online"),
    GEOPROCESSING("Geoprocesamiento"),
    COMPOSITION("Composicion / impresion"),
    TOOLBOX("Toolbox"),
    ADVANCED("Herramientas avanzadas"),
    STYLING("Simbologia");

    private final String displayName;

    ModuleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return I18n.t(displayName);
    }
}
