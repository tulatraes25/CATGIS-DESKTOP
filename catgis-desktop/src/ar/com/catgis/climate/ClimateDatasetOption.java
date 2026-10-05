package ar.com.catgis.climate;

/**
 * Interface for climate dataset options, following the same pattern as
 * {@link ar.com.catgis.OnlineDemDatasetOption} and {@link ar.com.catgis.OnlineSoilDatasetOption}.
 */
public interface ClimateDatasetOption {

    /** Human-readable display name (e.g. "Temperatura media anual") */
    String getDisplayName();

    /** Source provider label (e.g. "WorldClim v2.1 / UCAR") */
    String getSourceLabel();

    /** Source CRS code (e.g. "EPSG:4326") */
    String getSourceCrsCode();

    /** Short output code used for filenames (e.g. "bio_1", "tavg") */
    String getOutputCode();

    /** Full technical note or description for the dialog */
    String getTechnicalSummary();

    /** Variable name/code used in remote API calls */
    String getApiVariableCode();

    /** URL pattern for downloading the global GeoTIFF for this variable */
    String getDownloadUrlPattern();
}
