package ar.com.catgis;

public class OnlineTileLayer extends RasterLayer {
    private String providerId = "";
    private String styleId = "";

    public OnlineTileLayer(String name) {
        super(name, "");
    }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public String getStyleId() { return styleId; }
    public void setStyleId(String styleId) { this.styleId = styleId; }
}
