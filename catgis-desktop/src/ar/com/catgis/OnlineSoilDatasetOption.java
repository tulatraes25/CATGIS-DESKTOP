package ar.com.catgis;

public interface OnlineSoilDatasetOption {

    String getDisplayName();

    String getSourceLabel();

    String getSourceCrsCode();

    String getOutputCode();

    String getTechnicalSummary();

    String getResolutionSummary();

    String getRiskUseHint();
}
