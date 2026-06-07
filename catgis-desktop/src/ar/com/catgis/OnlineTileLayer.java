package ar.com.catgis;
import ar.com.catgis.data.online.OnlineRasterSource;

public class OnlineTileLayer extends RasterLayer {
    private String sourceId = "";
    private String providerName = "";
    private String urlTemplate = "";
    private int minZoom = 0;
    private int maxZoom = 19;
    private String attribution = "";
    private String termsUrl = "";
    private String serviceType = OnlineServiceType.XYZ.name();
    private boolean requiresApiKey = false;

    public OnlineTileLayer(String name) {
        super(name, "");
        setType("ONLINE_TILE");
        setSourceCRS("EPSG:3857");
    }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId != null ? sourceId : ""; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName != null ? providerName : ""; }
    public String getUrlTemplate() { return urlTemplate; }
    public void setUrlTemplate(String urlTemplate) { this.urlTemplate = urlTemplate != null ? urlTemplate : ""; }
    public int getMinZoom() { return minZoom; }
    public void setMinZoom(int minZoom) { this.minZoom = Math.max(0, minZoom); }
    public int getMaxZoom() { return maxZoom; }
    public void setMaxZoom(int maxZoom) { this.maxZoom = Math.max(this.minZoom, maxZoom); }
    public String getAttribution() { return attribution; }
    public void setAttribution(String attribution) { this.attribution = attribution != null ? attribution : ""; }
    public String getTermsUrl() { return termsUrl; }
    public void setTermsUrl(String termsUrl) { this.termsUrl = termsUrl != null ? termsUrl : ""; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType != null ? serviceType : OnlineServiceType.XYZ.name(); }
    public boolean isRequiresApiKey() { return requiresApiKey; }
    public void setRequiresApiKey(boolean requiresApiKey) { this.requiresApiKey = requiresApiKey; }

    public void applySource(OnlineRasterSource source) {
        if (source == null) {
            return;
        }
        setName(source.getName());
        setSourceName(source.getName());
        setSourceId(source.getId());
        setProviderName(source.getProvider());
        setServiceType(source.getServiceType().name());
        setUrlTemplate(source.getUrlTemplate());
        setMinZoom(source.getMinZoom());
        setMaxZoom(source.getMaxZoom());
        setAttribution(source.getAttribution());
        setTermsUrl(source.getTermsUrl());
        setRequiresApiKey(source.isRequiresApiKey());
        setSourceCRS(source.getSourceCRS());
        setFeatureCount(1);
        setVisible(true);
    }

    public OnlineRasterSource toSourceDescriptor() {
        return new OnlineRasterSource(
                sourceId,
                getName(),
                providerName,
                OnlineServiceType.XYZ,
                urlTemplate,
                minZoom,
                maxZoom,
                256,
                attribution,
                termsUrl,
                requiresApiKey,
                getSourceCRS()
        );
    }
}
