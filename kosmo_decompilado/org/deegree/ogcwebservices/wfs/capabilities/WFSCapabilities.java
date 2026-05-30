/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.model.filterencoding.capabilities.FilterCapabilities
 *  org.deegree.ogcwebservices.getcapabilities.Contents
 *  org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException
 *  org.deegree.ogcwebservices.getcapabilities.OperationsMetadata
 *  org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList
 *  org.deegree.ogcwebservices.wfs.capabilities.GMLObject
 */
package org.deegree.ogcwebservices.wfs.capabilities;

import java.io.IOException;
import java.net.URL;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.ogcwebservices.getcapabilities.Contents;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList;
import org.deegree.ogcwebservices.wfs.capabilities.GMLObject;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.owscommon.OWSCommonCapabilities;
import org.xml.sax.SAXException;

public class WFSCapabilities
extends OWSCommonCapabilities {
    private static final long serialVersionUID = -8126209663124432256L;
    private FeatureTypeList featureTypeList;
    private GMLObject[] servesGMLObjectTypeList;
    private GMLObject[] supportsGMLObjectTypeList;
    private FilterCapabilities filterCapabilities;

    public static OGCCapabilities createCapabilities(URL url) throws IOException, SAXException, InvalidCapabilitiesException {
        WFSCapabilitiesDocument capabilitiesDoc = new WFSCapabilitiesDocument();
        capabilitiesDoc.load(url);
        return capabilitiesDoc.parseCapabilities();
    }

    public WFSCapabilities(String version, String updateSequence, ServiceIdentification serviceIdentification, ServiceProvider serviceProvider, OperationsMetadata operationsMetadata, FeatureTypeList featureTypeList, GMLObject[] servesGMLObjectTypeList, GMLObject[] supportsGMLObjectTypeList, Contents contents, FilterCapabilities filterCapabilities) {
        super(version, updateSequence, serviceIdentification, serviceProvider, operationsMetadata, contents);
        this.featureTypeList = featureTypeList;
        this.servesGMLObjectTypeList = servesGMLObjectTypeList;
        this.supportsGMLObjectTypeList = supportsGMLObjectTypeList;
        this.filterCapabilities = filterCapabilities;
    }

    public FilterCapabilities getFilterCapabilities() {
        return this.filterCapabilities;
    }

    public FeatureTypeList getFeatureTypeList() {
        return this.featureTypeList;
    }

    public void setFeatureTypeList(FeatureTypeList featureTypeList) {
        this.featureTypeList = featureTypeList;
    }

    public GMLObject[] getServesGMLObjectTypeList() {
        return this.servesGMLObjectTypeList;
    }

    public void setServesGMLObjectTypeList(GMLObject[] servesGMLObjectTypeList) {
        this.servesGMLObjectTypeList = servesGMLObjectTypeList;
    }

    public GMLObject[] getSupportsGMLObjectTypeList() {
        return this.supportsGMLObjectTypeList;
    }

    public void setSupportsGMLObjectTypeList(GMLObject[] supportsGMLObjectTypeList) {
        this.supportsGMLObjectTypeList = supportsGMLObjectTypeList;
    }
}

