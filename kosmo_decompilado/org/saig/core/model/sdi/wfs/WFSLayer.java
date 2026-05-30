/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.sdi.wfs;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import de.latlon.deejump.wfs.client.AbstractWFSWrapper;
import es.kosmo.core.model.sdi.BasicAuthentificationData;
import java.awt.Color;
import java.util.List;
import org.saig.core.filter.Filter;
import org.saig.core.model.sdi.wfs.WFSFeatureCollection;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;

public class WFSLayer
extends Layer {
    private String version;
    private String url;
    private String wfsFeatureType;
    private List<String> selectedAttributes;
    private String geomField;
    private int maxFeatures;
    private String format;
    private String selectedSrs;
    private Filter filter;
    protected BasicAuthentificationData basicAuthData;

    public WFSLayer(String version, String url, String wfsFeatureType, List<String> selectedAttributes, String geomField, String format, String selectedSrs, Filter filter, int maxFeatures, Color fillColor, BasicAuthentificationData basicAuthData, WFSFeatureCollection dataset, LayerManager layerManager) {
        super(dataset.getInfo().getTitle(), fillColor, (FeatureCollection)dataset, layerManager);
        this.version = version;
        this.url = url;
        this.wfsFeatureType = wfsFeatureType;
        this.selectedAttributes = selectedAttributes;
        this.geomField = geomField;
        this.maxFeatures = maxFeatures;
        this.format = format;
        this.selectedSrs = selectedSrs;
        this.filter = filter;
        this.basicAuthData = basicAuthData;
    }

    public WFSLayer() {
    }

    public AbstractWFSWrapper getService() {
        if (this.getUltimateFeatureCollectionWrapper() != null) {
            return ((WFSFeatureCollection)this.getUltimateFeatureCollectionWrapper()).getService();
        }
        return null;
    }

    public WFSFeatureTypeInfo getInfo() {
        if (this.getUltimateFeatureCollectionWrapper() != null) {
            return ((WFSFeatureCollection)this.getUltimateFeatureCollectionWrapper()).getInfo();
        }
        return null;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWfsFeatureType() {
        return this.wfsFeatureType;
    }

    public void setWfsFeatureType(String wfsFeatureType) {
        this.wfsFeatureType = wfsFeatureType;
    }

    @Override
    public boolean hasReadableDataSource() {
        return true;
    }

    public List<String> getSelectedAttributes() {
        return this.selectedAttributes;
    }

    public void setSelectedAttributes(List<String> selectedAttributes) {
        this.selectedAttributes = selectedAttributes;
    }

    public String getGeomField() {
        return this.geomField;
    }

    public void setGeomField(String geomField) {
        this.geomField = geomField;
    }

    public int getMaxFeatures() {
        return this.maxFeatures;
    }

    public void setMaxFeatures(int maxFeatures) {
        this.maxFeatures = maxFeatures;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSelectedSrs() {
        return this.selectedSrs;
    }

    public void setSelectedSrs(String selectedSrs) {
        this.selectedSrs = selectedSrs;
    }

    public Filter getFilter() {
        return this.filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public boolean isTransactional() {
        if (this.getService() != null) {
            return this.getService().isTransactional();
        }
        return false;
    }

    public BasicAuthentificationData getBasicAuthData() {
        return this.basicAuthData;
    }

    public void setBasicAuthData(BasicAuthentificationData basicAuthData) {
        this.basicAuthData = basicAuthData;
        if (this.getService() != null) {
            this.getService().setBasicAuthData(basicAuthData);
        }
    }
}

