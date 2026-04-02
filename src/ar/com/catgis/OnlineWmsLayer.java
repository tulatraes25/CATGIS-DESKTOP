package ar.com.catgis;

public class OnlineWmsLayer extends RasterLayer {
    private String serviceUrl = "";
    private String layerName = "";

    public OnlineWmsLayer(String name) {
        super(name, "");
    }

    public String getServiceUrl() { return serviceUrl; }
    public void setServiceUrl(String serviceUrl) { this.serviceUrl = serviceUrl; }
    public String getLayerName() { return layerName; }
    public void setLayerName(String layerName) { this.layerName = layerName; }
}
