package ar.com.catgis.layout;

/**
 * Predefined layout page templates.
 */
public enum LayoutTemplate {
    TECHNICAL_RIGHT("Tecnica - leyenda derecha", LegendPlacement.RIGHT_PANEL),
    BOTTOM_REFERENCE("Referencia inferior", LegendPlacement.BOTTOM_PANEL),
    CLEAN_CENTERED("Limpia centrada", LegendPlacement.MAP_BOTTOM_RIGHT),
    STRONG_CARTOUCHE("Datos cartograficos enfatizados", LegendPlacement.RIGHT_PANEL);

    private final String label;
    private final LegendPlacement defaultLegendPlacement;

    LayoutTemplate(String label, LegendPlacement defaultLegendPlacement) {
        this.label = label;
        this.defaultLegendPlacement = defaultLegendPlacement;
    }

    public LegendPlacement defaultLegendPlacement() {
        return defaultLegendPlacement;
    }

    @Override
    public String toString() {
        return label;
    }
}
