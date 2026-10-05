package ar.com.catgis.layout;

/**
 * North arrow rendering style for layout maps.
 */
public enum NorthStyle {
    SIMPLE("Simple"),
    CLASSIC("Clasico"),
    MODERN("Moderno"),
    ROSE("Rosa"),
    TECHNICAL("Tecnico");

    private final String label;

    NorthStyle(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
