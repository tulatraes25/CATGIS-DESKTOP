package ar.com.catgis.climate;

import ar.com.catgis.I18n;

/**
 * WorldClim v2.1 bioclimatic variables.
 * Global coverage at 30 arc-seconds (~1 km), EPSG:4326.
 * Data available from UCAR/WorldClim as global GeoTIFFs.
 *
 * @see <a href="https://www.worldclim.org/">WorldClim</a>
 */
public enum WorldClimDataset implements ClimateDatasetOption {

    // --- BIO variables ---
    BIO1("Temperatura media anual (BIO1)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_1",
            "Temperatura media anual en °C * 10. Variable bioclimática BIO1 del conjunto WorldClim v2.1 a 30 arcseg. Util para caracterización climática general del área de estudio.",
            "bio_1",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_1.tif"),

    BIO2("Rango diurno medio (BIO2)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_2",
            "Rango diurno medio (media mensual de temperatura máxima - mínima) en °C * 10.",
            "bio_2",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_2.tif"),

    BIO3("Isotermalidad (BIO3)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_3",
            "Isotermalidad: BIO2/BIO7 * 100 (adimensional).",
            "bio_3",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_3.tif"),

    BIO4("Estacionalidad de temperatura (BIO4)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_4",
            "Desvío estándar de temperatura mensual * 100 (adimensional).",
            "bio_4",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_4.tif"),

    BIO5("Temperatura máxima del mes más cálido (BIO5)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_5",
            "Temperatura máxima del mes más cálido en °C * 10.",
            "bio_5",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_5.tif"),

    BIO6("Temperatura mínima del mes más frío (BIO6)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_6",
            "Temperatura mínima del mes más frío en °C * 10.",
            "bio_6",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_6.tif"),

    BIO7("Rango anual de temperatura (BIO7)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_7",
            "Rango anual de temperatura: BIO5 - BIO6 en °C * 10.",
            "bio_7",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_7.tif"),

    BIO8("Temperatura media del trimestre más húmedo (BIO8)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_8",
            "Temperatura media del trimestre más húmedo en °C * 10.",
            "bio_8",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_8.tif"),

    BIO9("Temperatura media del trimestre más seco (BIO9)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_9",
            "Temperatura media del trimestre más seco en °C * 10.",
            "bio_9",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_9.tif"),

    BIO10("Temperatura media del trimestre más cálido (BIO10)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_10",
            "Temperatura media del trimestre más cálido en °C * 10.",
            "bio_10",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_10.tif"),

    BIO11("Temperatura media del trimestre más frío (BIO11)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_11",
            "Temperatura media del trimestre más frío en °C * 10.",
            "bio_11",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_11.tif"),

    BIO12("Precipitación anual (BIO12)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_12",
            "Precipitación anual en mm.",
            "bio_12",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_12.tif"),

    BIO13("Precipitación del mes más húmedo (BIO13)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_13",
            "Precipitación del mes más húmedo en mm.",
            "bio_13",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_13.tif"),

    BIO14("Precipitación del mes más seco (BIO14)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_14",
            "Precipitación del mes más seco en mm.",
            "bio_14",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_14.tif"),

    BIO15("Estacionalidad de precipitación (BIO15)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_15",
            "Coeficiente de variación de precipitación mensual (adimensional).",
            "bio_15",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_15.tif"),

    BIO16("Precipitación del trimestre más húmedo (BIO16)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_16",
            "Precipitación del trimestre más húmedo en mm.",
            "bio_16",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_16.tif"),

    BIO17("Precipitación del trimestre más seco (BIO17)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_17",
            "Precipitación del trimestre más seco en mm.",
            "bio_17",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_17.tif"),

    BIO18("Precipitación del trimestre más cálido (BIO18)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_18",
            "Precipitación del trimestre más cálido en mm.",
            "bio_18",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_18.tif"),

    BIO19("Precipitación del trimestre más frío (BIO19)",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "bio_19",
            "Precipitación del trimestre más frío en mm.",
            "bio_19",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_bio_19.tif"),

    // --- Monthly averages ---
    TAVG_ANNUAL("Temperatura media anual",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tavg",
            "Temperatura media anual en °C * 10. Promedio de temperaturas medias mensuales (1970-2000).",
            "tavg",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tavg.tif"),

    PREC_ANNUAL("Precipitación anual",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "prec",
            "Precipitación anual total en mm. Suma de precipitación mensual (1970-2000).",
            "prec",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_prec.tif"),

    TMIN_ANNUAL("Temperatura mínima anual",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tmin",
            "Temperatura mínima anual en °C * 10. Promedio de mínimas mensuales (1970-2000).",
            "tmin",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tmin.tif"),

    TMAX_ANNUAL("Temperatura máxima anual",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tmax",
            "Temperatura máxima anual en °C * 10. Promedio de máximas mensuales (1970-2000).",
            "tmax",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tmax.tif"),

    // --- Monthly variables (January = 01 through December = 12) ---
    TAVG_01("Temperatura media - Enero",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tavg_01",
            "Temperatura media de enero en °C * 10.",
            "tavg01",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tavg_01.tif"),

    TAVG_02("Temperatura media - Febrero",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tavg_02",
            "Temperatura media de febrero en °C * 10.",
            "tavg02",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tavg_02.tif"),

    TAVG_03("Temperatura media - Marzo",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tavg_03",
            "Temperatura media de marzo en °C * 10.",
            "tavg03",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tavg_03.tif"),

    TAVG_04("Temperatura media - Abril",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tavg_04",
            "Temperatura media de abril en °C * 10.",
            "tavg04",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tavg_04.tif"),

    TAVG_05("Temperatura media - Mayo",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tavg_05",
            "Temperatura media de mayo en °C * 10.",
            "tavg05",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tavg_05.tif"),

    TAVG_06("Temperatura media - Junio",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tavg_06",
            "Temperatura media de junio en °C * 10.",
            "tavg06",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tavg_06.tif"),

    TAVG_07("Temperatura media - Julio",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tavg_07",
            "Temperatura media de julio en °C * 10.",
            "tavg07",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tavg_07.tif"),

    TAVG_08("Temperatura media - Agosto",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tavg_08",
            "Temperatura media de agosto en °C * 10.",
            "tavg08",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tavg_08.tif"),

    TAVG_09("Temperatura media - Septiembre",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tavg_09",
            "Temperatura media de septiembre en °C * 10.",
            "tavg09",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tavg_09.tif"),

    TAVG_10("Temperatura media - Octubre",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tavg_10",
            "Temperatura media de octubre en °C * 10.",
            "tavg10",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tavg_10.tif"),

    TAVG_11("Temperatura media - Noviembre",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tavg_11",
            "Temperatura media de noviembre en °C * 10.",
            "tavg11",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tavg_11.tif"),

    TAVG_12("Temperatura media - Diciembre",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "tavg_12",
            "Temperatura media de diciembre en °C * 10.",
            "tavg12",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_tavg_12.tif"),

    PREC_01("Precipitación - Enero",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "prec_01",
            "Precipitación total de enero en mm.",
            "prec01",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_prec_01.tif"),

    PREC_02("Precipitación - Febrero",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "prec_02",
            "Precipitación total de febrero en mm.",
            "prec02",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_prec_02.tif"),

    PREC_03("Precipitación - Marzo",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "prec_03",
            "Precipitación total de marzo en mm.",
            "prec03",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_prec_03.tif"),

    PREC_04("Precipitación - Abril",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "prec_04",
            "Precipitación total de abril en mm.",
            "prec04",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_prec_04.tif"),

    PREC_05("Precipitación - Mayo",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "prec_05",
            "Precipitación total de mayo en mm.",
            "prec05",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_prec_05.tif"),

    PREC_06("Precipitación - Junio",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "prec_06",
            "Precipitación total de junio en mm.",
            "prec06",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_prec_06.tif"),

    PREC_07("Precipitación - Julio",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "prec_07",
            "Precipitación total de julio en mm.",
            "prec07",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_prec_07.tif"),

    PREC_08("Precipitación - Agosto",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "prec_08",
            "Precipitación total de agosto en mm.",
            "prec08",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_prec_08.tif"),

    PREC_09("Precipitación - Septiembre",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "prec_09",
            "Precipitación total de septiembre en mm.",
            "prec09",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_prec_09.tif"),

    PREC_10("Precipitación - Octubre",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "prec_10",
            "Precipitación total de octubre en mm.",
            "prec10",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_prec_10.tif"),

    PREC_11("Precipitación - Noviembre",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "prec_11",
            "Precipitación total de noviembre en mm.",
            "prec11",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_prec_11.tif"),

    PREC_12("Precipitación - Diciembre",
            "WorldClim v2.1 / UCAR", "EPSG:4326", "prec_12",
            "Precipitación total de diciembre en mm.",
            "prec12",
            "https://biogeo.ucar.edu/data/worldclim/v2.1/base/wc2.1_30s_prec_12.tif");

    private final String displayName;
    private final String sourceLabel;
    private final String sourceCrsCode;
    private final String outputCode;
    private final String technicalSummary;
    private final String apiVariableCode;
    private final String downloadUrlPattern;

    WorldClimDataset(String displayName, String sourceLabel, String sourceCrsCode,
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
