package ar.com.catgis;

public class ShapefileInfo {

    private final String sourceName;
    private final int featureCount;
    private final String message;

    public ShapefileInfo(String sourceName, int featureCount, String message) {
        this.sourceName = sourceName;
        this.featureCount = featureCount;
        this.message = message;
    }

    public String getSourceName() {
        return sourceName;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public String getMessage() {
        return message;
    }
}