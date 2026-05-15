package ar.com.catgis;

public enum OnlineSoilProvider {
    SOILGRIDS("SoilGrids global (ISRIC)", false);

    private final String displayName;
    private final boolean requiresApiKey;

    OnlineSoilProvider(String displayName, boolean requiresApiKey) {
        this.displayName = displayName;
        this.requiresApiKey = requiresApiKey;
    }

    public boolean requiresApiKey() {
        return requiresApiKey;
    }

    public OnlineSoilDatasetOption[] getDatasets() {
        return switch (this) {
            case SOILGRIDS -> SoilGridsDataset.values();
        };
    }

    @Override
    public String toString() {
        return I18n.t(displayName);
    }
}
