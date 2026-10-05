package ar.com.catgis;

public enum OnlineDemProvider {
    PUBLIC_TERRAIN_TILES("Terrain Tiles publicos (sin clave)", false),
    OPEN_TOPOGRAPHY("OpenTopography global (requiere clave)", true);

    private final String displayName;
    private final boolean requiresApiKey;

    OnlineDemProvider(String displayName, boolean requiresApiKey) {
        this.displayName = displayName;
        this.requiresApiKey = requiresApiKey;
    }

    public boolean requiresApiKey() {
        return requiresApiKey;
    }

    public OnlineDemDatasetOption[] getDatasets() {
        return switch (this) {
            case PUBLIC_TERRAIN_TILES -> TerrainTilesDataset.values();
            case OPEN_TOPOGRAPHY -> OpenTopographyDataset.values();
        };
    }

    @Override
    public String toString() {
        return I18n.t(displayName);
    }
}
