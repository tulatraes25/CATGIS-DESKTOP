package ar.com.catgis.climate;

import ar.com.catgis.I18n;

/**
 * Supported online climate data providers.
 * Follows the same pattern as {@link ar.com.catgis.OnlineDemProvider}.
 */
public enum OnlineClimateProvider {

    WORLDCLIM("WorldClim v2.1 (global, sin clave)", false) {
        @Override
        public ClimateDatasetOption[] getDatasets() {
            return WorldClimDataset.values();
        }
    },
    OPEN_METEO("Open-Meteo histórico (sin clave)", false) {
        @Override
        public ClimateDatasetOption[] getDatasets() {
            return OpenMeteoDataset.values();
        }
    };

    private final String displayName;
    private final boolean requiresApiKey;

    OnlineClimateProvider(String displayName, boolean requiresApiKey) {
        this.displayName = displayName;
        this.requiresApiKey = requiresApiKey;
    }

    public boolean requiresApiKey() {
        return requiresApiKey;
    }

    public abstract ClimateDatasetOption[] getDatasets();

    @Override
    public String toString() {
        return I18n.t(displayName);
    }
}
