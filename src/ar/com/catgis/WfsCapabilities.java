package ar.com.catgis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WfsCapabilities {

    private final String serviceTitle;
    private final String serviceAbstract;
    private final String version;
    private final List<WfsFeatureTypeInfo> featureTypes;

    public WfsCapabilities(String serviceTitle, String serviceAbstract, String version, List<WfsFeatureTypeInfo> featureTypes) {
        this.serviceTitle = serviceTitle != null ? serviceTitle : "";
        this.serviceAbstract = serviceAbstract != null ? serviceAbstract : "";
        this.version = version != null ? version : "";
        this.featureTypes = featureTypes != null ? new ArrayList<>(featureTypes) : new ArrayList<>();
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

    public List<WfsFeatureTypeInfo> getFeatureTypes() {
        return Collections.unmodifiableList(featureTypes);
    }
}
