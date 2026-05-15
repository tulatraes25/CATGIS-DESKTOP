package ar.com.catgis;

public class OnlineWmsLayer extends RasterLayer {
    private String sourceId = "";
    private String providerName = "";
    private String serviceUrl = "";
    private String layerNames = "";
    private String styleNames = "";
    private String requestCrs = "";
    private String version = "1.3.0";
    private String imageFormat = "image/png";
    private boolean transparent = true;
    private String attribution = "";
    private String termsUrl = "";
    private String extentCrs = "";
    private double extentMinX = Double.NaN;
    private double extentMinY = Double.NaN;
    private double extentMaxX = Double.NaN;
    private double extentMaxY = Double.NaN;

    public OnlineWmsLayer(String name) {
        super(name, "");
        setType("ONLINE_WMS");
    }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId != null ? sourceId : ""; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName != null ? providerName : ""; }
    public String getServiceUrl() { return serviceUrl; }
    public void setServiceUrl(String serviceUrl) { this.serviceUrl = serviceUrl != null ? serviceUrl : ""; }
    public String getLayerNames() { return layerNames; }
    public void setLayerNames(String layerNames) { this.layerNames = layerNames != null ? layerNames : ""; }
    public String getStyleNames() { return styleNames; }
    public void setStyleNames(String styleNames) { this.styleNames = styleNames != null ? styleNames : ""; }
    public String getRequestCrs() { return requestCrs; }
    public void setRequestCrs(String requestCrs) { this.requestCrs = requestCrs != null ? requestCrs : ""; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version != null ? version : "1.3.0"; }
    public String getImageFormat() { return imageFormat; }
    public void setImageFormat(String imageFormat) { this.imageFormat = imageFormat != null ? imageFormat : "image/png"; }
    public boolean isTransparent() { return transparent; }
    public void setTransparent(boolean transparent) { this.transparent = transparent; }
    public String getAttribution() { return attribution; }
    public void setAttribution(String attribution) { this.attribution = attribution != null ? attribution : ""; }
    public String getTermsUrl() { return termsUrl; }
    public void setTermsUrl(String termsUrl) { this.termsUrl = termsUrl != null ? termsUrl : ""; }
    public String getExtentCrs() { return extentCrs; }
    public void setExtentCrs(String extentCrs) { this.extentCrs = extentCrs != null ? extentCrs : ""; }
    public double getExtentMinX() { return extentMinX; }
    public double getExtentMinY() { return extentMinY; }
    public double getExtentMaxX() { return extentMaxX; }
    public double getExtentMaxY() { return extentMaxY; }
    public void setExtent(double minX, double minY, double maxX, double maxY, String crs) {
        this.extentMinX = minX;
        this.extentMinY = minY;
        this.extentMaxX = maxX;
        this.extentMaxY = maxY;
        this.extentCrs = crs != null ? crs : "";
    }
}
