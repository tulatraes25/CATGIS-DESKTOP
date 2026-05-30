/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.framework.util.StringTools
 *  org.deegree.framework.xml.XMLParsingException
 *  org.deegree.model.filterencoding.capabilities.FilterCapabilities
 *  org.deegree.model.metadata.iso19115.Keywords
 *  org.deegree.model.spatialschema.Envelope
 *  org.deegree.model.spatialschema.GeometryFactory
 *  org.deegree.ogcwebservices.getcapabilities.DCPType
 *  org.deegree.ogcwebservices.getcapabilities.HTTP
 *  org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException
 *  org.deegree.ogcwebservices.getcapabilities.MetadataURL
 *  org.deegree.ogcwebservices.getcapabilities.OGCCapabilitiesDocument
 *  org.deegree.ogcwebservices.getcapabilities.OperationsMetadata
 *  org.deegree.ogcwebservices.getcapabilities.Protocol
 *  org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList
 *  org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType
 *  org.deegree.ogcwebservices.wfs.capabilities.WFSOperationsMetadata
 */
package org.deegree.ogcwebservices.wfs.capabilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities100Fragment;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilitiesDocument;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.Protocol;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList;
import org.deegree.ogcwebservices.wfs.capabilities.Operation;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.deegree.ogcwebservices.wfs.capabilities.WFSOperationsMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class WFSCapabilitiesDocument_1_0_0
extends OGCCapabilitiesDocument {
    private static final long serialVersionUID = 4538469826043112486L;
    private static final String[] VALID_TYPES = new String[]{"TDC211", "FGDC"};
    private static final String[] VALID_FORMATS = new String[]{"XML", "SGML", "TXT"};

    public void createEmptyDocument() throws IOException, SAXException {
        Document doc = XMLTools.create();
        Element root = doc.createElementNS("http://www.opengis.net/wfs", "wfs:WFS_Capabilities");
        doc.importNode(root, false);
        this.setRootElement(root);
        root.setAttribute("version", "1.0.0");
        root.setAttribute("updateSequence", "0");
    }

    public OGCCapabilities parseCapabilities() throws InvalidCapabilitiesException {
        WFSCapabilities wfsCapabilities = null;
        try {
            wfsCapabilities = new WFSCapabilities(this.parseVersion(), this.parseUpdateSequence(), this.getService(), null, this.getCapability(), this.getFeatureTypeList(), null, null, null, this.getFilterCapabilities());
        }
        catch (XMLParsingException e) {
            throw new InvalidCapabilitiesException(String.valueOf(e.getMessage()) + "\n" + StringTools.stackTraceToString((Throwable)e));
        }
        return wfsCapabilities;
    }

    public ServiceIdentification getService() throws XMLParsingException {
        Element element = XMLTools.getRequiredElement(this.getRootElement(), "wfs:Service", nsContext);
        String name = XMLTools.getRequiredNodeAsString(element, "wfs:Name/text()", nsContext);
        String title = XMLTools.getRequiredNodeAsString(element, "wfs:Title/text()", nsContext);
        String abstract_ = XMLTools.getNodeAsString(element, "wfs:Abstract/text()", nsContext, null);
        String keywordsValue = XMLTools.getNodeAsString(element, "wfs:Keywords/text()", nsContext, null);
        Keywords[] keywords = null;
        if (keywordsValue != null) {
            keywords = new Keywords[]{new Keywords(new String[]{keywordsValue})};
        }
        String[] serviceTypeVersions = new String[]{"1.0.0"};
        String fees = XMLTools.getNodeAsString(element, "wfs:Fees/text()", nsContext, null);
        return new ServiceIdentification(name, null, serviceTypeVersions, title, abstract_, keywords, fees, null);
    }

    public OperationsMetadata getCapability() throws XMLParsingException {
        Element requestElement = XMLTools.getRequiredElement(this.getRootElement(), "wfs:Capability/wfs:Request", nsContext);
        org.deegree.ogcwebservices.getcapabilities.Operation getCapabilities = null;
        Element getCapabilitiesElement = XMLTools.getElement(requestElement, "wfs:GetCapabilities", nsContext);
        if (getCapabilitiesElement != null) {
            List<Element> dcpTypeElements = XMLTools.getElements(getCapabilitiesElement, "wfs:DCPType", nsContext);
            DCPType[] dcpTypes = new DCPType[dcpTypeElements.size()];
            int i = 0;
            while (i < dcpTypes.length) {
                Element dcpTypeElement = dcpTypeElements.get(i);
                dcpTypes[i] = this.getDCPTypeType(dcpTypeElement);
                ++i;
            }
            getCapabilities = new org.deegree.ogcwebservices.getcapabilities.Operation("GetCapabilities", dcpTypes);
        }
        org.deegree.ogcwebservices.getcapabilities.Operation describeFeatureType = null;
        Element describeFeatureTypeElement = XMLTools.getElement(requestElement, "wfs:DescribeFeatureType", nsContext);
        if (describeFeatureTypeElement != null) {
            List<Element> dcpTypeElements = XMLTools.getElements(describeFeatureTypeElement, "wfs:DCPType", nsContext);
            DCPType[] dcpTypes = new DCPType[dcpTypeElements.size()];
            int i = 0;
            while (i < dcpTypes.length) {
                Element dcpTypeElement = dcpTypeElements.get(i);
                dcpTypes[i] = this.getDCPTypeType(dcpTypeElement);
                ++i;
            }
            describeFeatureType = new org.deegree.ogcwebservices.getcapabilities.Operation("DescribeFeatureType", dcpTypes);
        }
        org.deegree.ogcwebservices.getcapabilities.Operation getFeature = null;
        Element getFeatureElement = XMLTools.getElement(requestElement, "wfs:GetFeature", nsContext);
        if (getFeatureElement != null) {
            List<Element> dcpTypeElements = XMLTools.getElements(getFeatureElement, "wfs:DCPType", nsContext);
            DCPType[] dcpTypes = new DCPType[dcpTypeElements.size()];
            int i = 0;
            while (i < dcpTypes.length) {
                Element dcpTypeElement = dcpTypeElements.get(i);
                dcpTypes[i] = this.getDCPTypeType(dcpTypeElement);
                ++i;
            }
            getFeature = new org.deegree.ogcwebservices.getcapabilities.Operation("GetFeature", dcpTypes);
        }
        org.deegree.ogcwebservices.getcapabilities.Operation getFeatureWithLock = null;
        Element getFeatureWithLockElement = XMLTools.getElement(requestElement, "wfs:GetFeatureWithLock", nsContext);
        if (getFeatureWithLockElement != null) {
            List<Element> dcpTypeElements = XMLTools.getElements(getFeatureWithLockElement, "wfs:DCPType", nsContext);
            DCPType[] dcpTypes = new DCPType[dcpTypeElements.size()];
            int i = 0;
            while (i < dcpTypes.length) {
                Element dcpTypeElement = dcpTypeElements.get(i);
                dcpTypes[i] = this.getDCPTypeType(dcpTypeElement);
                ++i;
            }
            getFeatureWithLock = new org.deegree.ogcwebservices.getcapabilities.Operation("GetFeatureWithLock", dcpTypes);
        }
        org.deegree.ogcwebservices.getcapabilities.Operation lockFeature = null;
        Element lockFeatureElement = XMLTools.getElement(requestElement, "wfs:LockFeature", nsContext);
        if (lockFeatureElement != null) {
            List<Element> dcpTypeElements = XMLTools.getElements(lockFeatureElement, "wfs:DCPType", nsContext);
            DCPType[] dcpTypes = new DCPType[dcpTypeElements.size()];
            int i = 0;
            while (i < dcpTypes.length) {
                Element dcpTypeElement = dcpTypeElements.get(i);
                dcpTypes[i] = this.getDCPTypeType(dcpTypeElement);
                ++i;
            }
            lockFeature = new org.deegree.ogcwebservices.getcapabilities.Operation("LockFeature", dcpTypes);
        }
        org.deegree.ogcwebservices.getcapabilities.Operation transaction = null;
        Element transactionElement = XMLTools.getElement(requestElement, "wfs:Transaction", nsContext);
        if (transactionElement != null) {
            List<Element> dcpTypeElements = XMLTools.getElements(transactionElement, "wfs:DCPType", nsContext);
            DCPType[] dcpTypes = new DCPType[dcpTypeElements.size()];
            int i = 0;
            while (i < dcpTypes.length) {
                Element dcpTypeElement = dcpTypeElements.get(i);
                dcpTypes[i] = this.getDCPTypeType(dcpTypeElement);
                ++i;
            }
            transaction = new org.deegree.ogcwebservices.getcapabilities.Operation("Transaction", dcpTypes);
        }
        return new WFSOperationsMetadata(getCapabilities, describeFeatureType, getFeature, getFeatureWithLock, null, lockFeature, transaction, null, null);
    }

    private DCPType getDCPTypeType(Element element) throws XMLParsingException {
        Element httpElement = XMLTools.getRequiredElement(element, "wfs:HTTP", nsContext);
        String[] gets = XMLTools.getNodesAsStrings(httpElement, "wfs:Get/@onlineResource", nsContext);
        URL[] getURLs = new URL[gets.length];
        int j = 0;
        while (j < gets.length) {
            try {
                getURLs[j] = new URL(gets[j]);
            }
            catch (MalformedURLException e) {
                throw new XMLParsingException("OnlineResource '" + gets[j] + "' is not a valid URL.");
            }
            ++j;
        }
        String[] posts = XMLTools.getNodesAsStrings(httpElement, "wfs:Post/@onlineResource", nsContext);
        URL[] postURLs = new URL[posts.length];
        int j2 = 0;
        while (j2 < posts.length) {
            try {
                postURLs[j2] = new URL(posts[j2]);
            }
            catch (MalformedURLException e) {
                throw new XMLParsingException("OnlineResource '" + posts[j2] + "' is not a valid URL.");
            }
            ++j2;
        }
        return new DCPType((Protocol)new HTTP(getURLs, postURLs));
    }

    public FeatureTypeList getFeatureTypeList() throws XMLParsingException {
        ArrayList<WFSFeatureType> wfsFeatureTypes = new ArrayList<WFSFeatureType>();
        FeatureTypeList featureTypeList = new FeatureTypeList(new Operation[0], wfsFeatureTypes);
        Element element = (Element)XMLTools.getNode(this.getRootElement(), "wfs:FeatureTypeList", nsContext);
        if (element != null) {
            List<Element> featureTypeElementList;
            Operation[] globalOperations = null;
            Element operationsTypeElement = (Element)XMLTools.getNode(element, "wfs:Operations", nsContext);
            if (operationsTypeElement != null) {
                globalOperations = this.getOperationsType(operationsTypeElement);
            }
            if ((featureTypeElementList = XMLTools.getElements(element, "wfs:FeatureType", nsContext)).size() < 1) {
                throw new XMLParsingException("A wfs:FeatureTypeListType must contain at least one wfs:FeatureType-element.");
            }
            int i = 0;
            while (i < featureTypeElementList.size()) {
                WFSFeatureType wfsFT = this.getFeatureTypeType(featureTypeElementList.get(i));
                wfsFeatureTypes.add(wfsFT);
                ++i;
            }
            featureTypeList = new FeatureTypeList(globalOperations, wfsFeatureTypes);
        }
        return featureTypeList;
    }

    public WFSFeatureType getFeatureTypeType(Element element) throws XMLParsingException {
        QualifiedName name = WFSCapabilitiesDocument_1_0_0.parseQualifiedName((Node)XMLTools.getRequiredNode(element, "wfs:Name/text()", nsContext));
        String title = XMLTools.getNodeAsString(element, "wfs:Title/text()", nsContext, null);
        String abstract_ = XMLTools.getNodeAsString(element, "wfs:Abstract/text()", nsContext, null);
        String keywordsValue = XMLTools.getNodeAsString(element, "wfs:Keywords/text()", nsContext, null);
        Keywords[] keywords = null;
        if (keywordsValue != null) {
            keywords = new Keywords[]{new Keywords(new String[]{keywordsValue})};
        }
        URI defaultSrs = XMLTools.getRequiredNodeAsURI(element, "wfs:SRS", nsContext);
        Operation[] operations = null;
        Element operationsTypeElement = (Element)XMLTools.getNode(element, "wfs:Operations", nsContext);
        if (operationsTypeElement != null) {
            operations = this.getOperationsType(operationsTypeElement);
        }
        List<Element> latLongBoundingBoxElements = XMLTools.getElements(element, "wfs:LatLongBoundingBox", nsContext);
        Envelope[] latLongBoundingBoxes = new Envelope[latLongBoundingBoxElements.size()];
        int i = 0;
        while (i < latLongBoundingBoxes.length) {
            latLongBoundingBoxes[i] = this.getLatLongBoundingBoxType(latLongBoundingBoxElements.get(i));
            ++i;
        }
        List<Element> metadataURLElementList = XMLTools.getElements(element, "wfs:MetadataURL", nsContext);
        MetadataURL[] metadataUrls = new MetadataURL[metadataURLElementList.size()];
        int i2 = 0;
        while (i2 < metadataUrls.length) {
            metadataUrls[i2] = this.getMetadataURL(metadataURLElementList.get(i2));
            ++i2;
        }
        return new WFSFeatureType(name, title, abstract_, keywords, defaultSrs, null, operations, null, latLongBoundingBoxes, metadataUrls);
    }

    private Envelope getLatLongBoundingBoxType(Element element) throws XMLParsingException {
        double minX = XMLTools.getRequiredNodeAsDouble(element, "@minx", nsContext);
        double minY = XMLTools.getRequiredNodeAsDouble(element, "@miny", nsContext);
        double maxX = XMLTools.getRequiredNodeAsDouble(element, "@maxx", nsContext);
        double maxY = XMLTools.getRequiredNodeAsDouble(element, "@maxy", nsContext);
        return GeometryFactory.createEnvelope((double)minX, (double)minY, (double)maxX, (double)maxY, null);
    }

    public MetadataURL getMetadataURL(Element element) throws XMLParsingException {
        URL onlineResource;
        String type = XMLTools.getRequiredNodeAsString(element, "@type", nsContext, VALID_TYPES);
        String format = XMLTools.getRequiredNodeAsString(element, "@format", nsContext, VALID_FORMATS);
        String url = XMLTools.getRequiredNodeAsString(element, "text()", nsContext);
        try {
            onlineResource = new URL(url);
        }
        catch (MalformedURLException e) {
            throw new XMLParsingException("A wfs:MetadataURLType must contain a valid URL: " + e.getMessage());
        }
        return new MetadataURL(type, format, onlineResource);
    }

    public Operation[] getOperationsType(Element element) throws XMLParsingException {
        String[] operationCodes = XMLTools.getNodesAsStrings(element, "wfs:Operation/text()", nsContext);
        Operation[] operations = new Operation[operationCodes.length];
        int i = 0;
        while (i < operations.length) {
            if ("GetGMLObject".equals(operationCodes[i])) {
                String msg = "Invalid WFS capabilities document. WFS 1.0.0 does not specify operation 'GetGMLObject.'";
                throw new XMLParsingException(msg);
            }
            try {
                operations[i] = new Operation(operationCodes[i]);
            }
            catch (InvalidParameterException e) {
                throw new XMLParsingException(e.getMessage());
            }
            ++i;
        }
        return operations;
    }

    public FilterCapabilities getFilterCapabilities() throws XMLParsingException {
        FilterCapabilities filterCapabilities = null;
        Element filterCapabilitiesElement = (Element)XMLTools.getNode(this.getRootElement(), "ogc:Filter_Capabilities", nsContext);
        if (filterCapabilitiesElement != null) {
            filterCapabilities = new FilterCapabilities100Fragment(filterCapabilitiesElement, this.getSystemId()).parseFilterCapabilities();
        }
        return filterCapabilities;
    }
}

