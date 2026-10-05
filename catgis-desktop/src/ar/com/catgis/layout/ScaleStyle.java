package ar.com.catgis.layout;

/**
 * Scale bar visual style for layout rendering.
 */
public enum ScaleStyle {
    SEGMENTED_BAR("Barra segmentada"),
    SIMPLE_BAR("Barra simple"),
    NUMERIC("Escala numerica");

    private final String label;

    ScaleStyle(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
