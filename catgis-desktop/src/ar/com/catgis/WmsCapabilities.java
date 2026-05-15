package ar.com.catgis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WmsCapabilities {
    private final String serviceTitle;
    private final String serviceAbstract;
    private final String version;
    private final List<String> formats;
    private final List<WmsLayerInfo> layers;

    public WmsCapabilities(String serviceTitle, String serviceAbstract, String version, List<String> formats, List<WmsLayerInfo> layers) {
        this.serviceTitle = serviceTitle != null ? serviceTitle : "";
        this.serviceAbstract = serviceAbstract != null ? serviceAbstract : "";
        this.version = version != null ? version : "1.3.0";
        this.formats = formats != null ? new ArrayList<>(formats) : new ArrayList<>();
        this.layers = layers != null ? new ArrayList<>(layers) : new ArrayList<>();
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public String getServiceAbstract() {
        return serviceAbstract;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getFormats() {
        return Collections.unmodifiableList(formats);
    }

    public List<WmsLayerInfo> getLayers() {
        return Collections.unmodifiableList(layers);
    }
}
