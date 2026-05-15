package ar.com.catgis;

public class ProDatasetDescriptor {

    private String datasetId = "";
    private String family = "";
    private String provider = "";
    private String platform = "";
    private String instrument = "";
    private String processingLevel = "";
    private String acquisitionStart = "";
    private String acquisitionEnd = "";

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = sanitize(datasetId);
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = sanitize(family);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = sanitize(provider);
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = sanitize(platform);
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = sanitize(instrument);
    }

    public String getProcessingLevel() {
        return processingLevel;
    }

    public void setProcessingLevel(String processingLevel) {
        this.processingLevel = sanitize(processingLevel);
    }

    public String getAcquisitionStart() {
        return acquisitionStart;
    }

    public void setAcquisitionStart(String acquisitionStart) {
        this.acquisitionStart = sanitize(acquisitionStart);
    }

    public String getAcquisitionEnd() {
        return acquisitionEnd;
    }

    public void setAcquisitionEnd(String acquisitionEnd) {
        this.acquisitionEnd = sanitize(acquisitionEnd);
    }

    private static String sanitize(String value) {
        return value != null ? value.trim() : "";
    }
}
