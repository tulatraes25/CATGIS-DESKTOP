package ar.com.catgis.climate;

import ar.com.catgis.I18n;

/**
 * Open-Meteo historical API datasets.
 * Free, no API key required. Provides point-based weather data
 * that CATGIS converts into raster grids.
 *
 * @see <a href="https://open-meteo.com/">Open-Meteo</a>
 */
public enum OpenMeteoDataset implements ClimateDatasetOption {

    TEMPERATURE_2M("Temperatura 2m (media diaria)",
            "Open-Meteo histórico", "EPSG:4326", "temp_2m",
            "Temperatura del aire a 2 metros, media diaria en °C. Open-Meteo Historical API, sin clave. CATGIS construye un raster interpolando datos puntuales.",
            "temperature_2m_mean",
            "https://archive-api.open-meteo.com/v1/archive"),

    TEMPERATURE_MAX("Temperatura 2m (máxima diaria)",
            "Open-Meteo histórico", "EPSG:4326", "temp_max",
            "Temperatura máxima diaria a 2 metros en °C.",
            "temperature_2m_max",
            "https://archive-api.open-meteo.com/v1/archive"),

    TEMPERATURE_MIN("Temperatura 2m (mínima diaria)",
            "Open-Meteo histórico", "EPSG:4326", "temp_min",
            "Temperatura mínima diaria a 2 metros en °C.",
            "temperature_2m_min",
            "https://archive-api.open-meteo.com/v1/archive"),

    PRECIPITATION("Precipitación (total diario)",
            "Open-Meteo histórico", "EPSG:4326", "precip",
            "Precipitación total diaria en mm.",
            "precipitation_sum",
            "https://archive-api.open-meteo.com/v1/archive"),

    PRECIPITATION_HOURS("Horas de precipitación",
            "Open-Meteo histórico", "EPSG:4326", "precip_hours",
            "Duración de precipitación en horas.",
            "precipitation_hours",
            "https://archive-api.open-meteo.com/v1/archive"),

    WIND_SPEED_10M("Velocidad del viento 10m (media)",
            "Open-Meteo histórico", "EPSG:4326", "wind_10m",
            "Velocidad media del viento a 10 metros en km/h.",
            "wind_speed_10m_max",
            "https://archive-api.open-meteo.com/v1/archive"),

    WIND_GUSTS_10M("Ráfagas de viento 10m (máxima)",
            "Open-Meteo histórico", "EPSG:4326", "wind_gusts",
            "Ráfagas máximas de viento a 10 metros en km/h.",
            "wind_gusts_10m_max",
            "https://archive-api.open-meteo.com/v1/archive"),

    PRESSURE("Presión en superficie (media)",
            "Open-Meteo histórico", "EPSG:4326", "pressure",
            "Presión atmosférica media en superficie en hPa.",
            "surface_pressure_mean",
            "https://archive-api.open-meteo.com/v1/archive"),

    CLOUD_COVER("Cobertura nubosa (media)",
            "Open-Meteo histórico", "EPSG:4326", "cloud_cover",
            "Cobertura nubosa media en porcentaje.",
            "cloud_cover_mean",
            "https://archive-api.open-meteo.com/v1/archive"),

    SUNSHINE_DURATION("Duración de insolación",
            "Open-Meteo histórico", "EPSG:4326", "sunshine",
            "Duración de insolación en horas.",
            "sunshine_duration",
            "https://archive-api.open-meteo.com/v1/archive"),

    SHORTWAVE_RADIATION("Radiación solar de onda corta",
            "Open-Meteo histórico", "EPSG:4326", "shortwave_rad",
            "Radiación solar media de onda corta en MJ/m².",
            "shortwave_radiation_sum",
            "https://archive-api.open-meteo.com/v1/archive"),

    DEWPOINT_2M("Punto de rocío 2m (media)",
            "Open-Meteo histórico", "EPSG:4326", "dewpoint",
            "Temperatura de punto de rocío a 2 metros en °C.",
            "dewpoint_2m_mean",
            "https://archive-api.open-meteo.com/v1/archive");

    private final String displayName;
    private final String sourceLabel;
    private final String sourceCrsCode;
    private final String outputCode;
    private final String technicalSummary;
    private final String apiVariableCode;
    private final String downloadUrlPattern;

    OpenMeteoDataset(String displayName, String sourceLabel, String sourceCrsCode,
                     String outputCode, String technicalSummary,
                     String apiVariableCode, String downloadUrlPattern) {
        this.displayName = displayName;
        this.sourceLabel = sourceLabel;
        this.sourceCrsCode = sourceCrsCode;
        this.outputCode = outputCode;
        this.technicalSummary = technicalSummary;
        this.apiVariableCode = apiVariableCode;
        this.downloadUrlPattern = downloadUrlPattern;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSourceLabel() {
        return sourceLabel;
    }

    @Override
    public String getSourceCrsCode() {
        return sourceCrsCode;
    }

    @Override
    public String getOutputCode() {
        return outputCode;
    }

    @Override
    public String getTechnicalSummary() {
        return technicalSummary;
    }

    @Override
    public String getApiVariableCode() {
        return apiVariableCode;
    }

    @Override
    public String getDownloadUrlPattern() {
        return downloadUrlPattern;
    }

    @Override
    public String toString() {
        return I18n.t(displayName);
    }
}
