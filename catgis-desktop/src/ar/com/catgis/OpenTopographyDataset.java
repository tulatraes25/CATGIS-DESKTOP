package ar.com.catgis;

public enum OpenTopographyDataset implements OnlineDemDatasetOption {
    NASADEM("NASADEM", "NASADEM 30 m", "OpenTopography / NASADEM", "EPSG:4326"),
    COP30("COP30", "Copernicus GLO-30", "OpenTopography / Copernicus DEM", "EPSG:4326"),
    SRTMGL1("SRTMGL1", "SRTM GL1 30 m", "OpenTopography / SRTM", "EPSG:4326");

    private final String apiCode;
    private final String displayName;
    private final String sourceLabel;
    private final String sourceCrsCode;

    OpenTopographyDataset(String apiCode, String displayName, String sourceLabel, String sourceCrsCode) {
        this.apiCode = apiCode;
        this.displayName = displayName;
        this.sourceLabel = sourceLabel;
        this.sourceCrsCode = sourceCrsCode;
    }

    public String getApiCode() {
        return apiCode;
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
        return apiCode.toLowerCase();
    }

    @Override
    public String toString() {
        return I18n.t(displayName);
    }
}
