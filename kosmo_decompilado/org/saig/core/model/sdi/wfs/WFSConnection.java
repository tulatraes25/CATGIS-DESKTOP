/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.saig.core.model.sdi.wfs;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import es.kosmo.core.model.sdi.BasicAuthentificationData;
import java.util.ArrayList;
import java.util.List;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactoryImpl;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.model.sdi.wfs.WFSFeatureCollection;
import org.saig.jump.plugin.sdi.wfs.WfsLayerBuilder;
import org.saig.jump.plugin.sdi.wfs.WfsLoadingErrorException;

public class WFSConnection {
    private String version;
    private String url;
    private String wfsFeatureType;
    private List<String> selectedAttributes;
    private String geomField;
    private String format;
    private String selectedSrs;
    private BasicAuthentificationData basicAuthData;

    public WFSConnection(String version, String url, String wfsFeatureType, List<String> selectedAttributes, String geomField, String format, String selectedSrs, BasicAuthentificationData basicAuthentificationData) {
        this.version = version;
        this.url = url;
        this.wfsFeatureType = wfsFeatureType;
        this.selectedAttributes = selectedAttributes;
        this.geomField = geomField;
        this.format = format;
        this.selectedSrs = selectedSrs;
        this.basicAuthData = basicAuthentificationData;
    }

    public List<Feature> query(Filter filter, int geomType, int maxFeatures) throws WfsLoadingErrorException {
        WfsLayerBuilder builder = new WfsLayerBuilder();
        List<WFSFeatureCollection> collections = builder.createCollections(this.version, this.url, this.wfsFeatureType, this.selectedAttributes, this.geomField, this.format, this.selectedSrs, filter, maxFeatures, this.basicAuthData);
        WFSFeatureCollection featFind = null;
        for (WFSFeatureCollection feat : collections) {
            if (feat.getFeatureSchema().getGeometryType() != geomType) continue;
            featFind = feat;
        }
        if (featFind != null) {
            List<Feature> features = featFind.getFeatures();
            return features;
        }
        return new ArrayList<Feature>();
    }

    public List<Feature> query(Envelope env, int geomType, int maxFeatures) throws WfsLoadingErrorException {
        FilterFactoryImpl filterFactory = (FilterFactoryImpl)FilterFactoryImpl.createFilterFactory();
        try {
            Filter filter = filterFactory.createBBoxFilter(this.geomField, env);
            return this.query(filter, geomType, maxFeatures);
        }
        catch (IllegalFilterException e) {
            throw new WfsLoadingErrorException(e);
        }
    }
}

