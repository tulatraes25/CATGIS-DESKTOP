package ar.com.catgis.layout;

/**
 * Where the legend is placed relative to the map frame.
 */
public enum LegendPlacement {
    RIGHT_PANEL("Panel derecho"),
    BOTTOM_PANEL("Franja inferior"),
    MAP_TOP_RIGHT("Dentro del mapa - arriba derecha"),
    MAP_BOTTOM_RIGHT("Dentro del mapa - abajo derecha"),
    MAP_BOTTOM_LEFT("Dentro del mapa - abajo izquierda");

    private final String label;

    LegendPlacement(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isInsideMap() {
        return this == MAP_TOP_RIGHT || this == MAP_BOTTOM_RIGHT || this == MAP_BOTTOM_LEFT;
    }
}
