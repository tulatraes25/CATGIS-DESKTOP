package ar.com.catgis;

public interface OnlineDemDatasetOption {

    String getDisplayName();

    String getSourceLabel();

    String getSourceCrsCode();

    String getOutputCode();

    default String getTechnicalSummary() {
        return getSourceLabel();
    }
}
