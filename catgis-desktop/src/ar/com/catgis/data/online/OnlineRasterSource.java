package ar.com.catgis.data.online;
import ar.com.catgis.core.model.Layer;

import ar.com.catgis.OnlineTileLayer;
import ar.com.catgis.OnlineServiceType;
public class OnlineRasterSource {

    private final String id;
    private final String name;
    private final String provider;
    private final OnlineServiceType serviceType;
    private final String urlTemplate;
    private final int minZoom;
    private final int maxZoom;
    private final int tileSize;
    private final String attribution;
    private final String termsUrl;
    private final boolean requiresApiKey;
    private final String sourceCRS;

    public OnlineRasterSource(String id,
                              String name,
                              String provider,
                              OnlineServiceType serviceType,
                              String urlTemplate,
                              int minZoom,
                              int maxZoom,
                              int tileSize,
                              String attribution,
                              String termsUrl,
                              boolean requiresApiKey,
                              String sourceCRS) {
        this.id = id != null ? id : "";
        this.name = name != null ? name : "";
        this.provider = provider != null ? provider : "";
        this.serviceType = serviceType != null ? serviceType : OnlineServiceType.XYZ;
        this.urlTemplate = urlTemplate != null ? urlTemplate : "";
        this.minZoom = Math.max(0, minZoom);
        this.maxZoom = Math.max(this.minZoom, maxZoom);
        this.tileSize = tileSize > 0 ? tileSize : 256;
        this.attribution = attribution != null ? attribution : "";
        this.termsUrl = termsUrl != null ? termsUrl : "";
        this.requiresApiKey = requiresApiKey;
        this.sourceCRS = (sourceCRS != null && !sourceCRS.isBlank()) ? sourceCRS : "EPSG:3857";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProvider() {
        return provider;
    }

    public OnlineServiceType getServiceType() {
        return serviceType;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public int getTileSize() {
        return tileSize;
    }

    public String getAttribution() {
        return attribution;
    }

    public String getTermsUrl() {
        return termsUrl;
    }

    public boolean isRequiresApiKey() {
        return requiresApiKey;
    }

    public String getSourceCRS() {
        return sourceCRS;
    }

    public OnlineTileLayer createLayer() {
        OnlineTileLayer layer = new OnlineTileLayer(name);
        layer.applySource(this);
        return layer;
    }
}
