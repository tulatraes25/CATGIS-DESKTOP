/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.Code
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.framework.log.ILogger
 *  org.deegree.framework.log.LoggerFactory
 *  org.deegree.framework.util.StringTools
 *  org.deegree.framework.xml.XMLParsingException
 *  org.deegree.model.filterencoding.capabilities.FilterCapabilities
 *  org.deegree.model.filterencoding.capabilities.FilterCapabilities110Fragment
 *  org.deegree.model.metadata.iso19115.Keywords
 *  org.deegree.model.spatialschema.Envelope
 *  org.deegree.ogcbase.CommonNamespaces
 *  org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException
 *  org.deegree.ogcwebservices.getcapabilities.MetadataURL
 *  org.deegree.ogcwebservices.getcapabilities.OperationsMetadata
 *  org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList
 *  org.deegree.ogcwebservices.wfs.capabilities.FormatType
 *  org.deegree.ogcwebservices.wfs.capabilities.GMLObject
 *  org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType
 *  org.deegree.ogcwebservices.wfs.capabilities.WFSOperationsMetadata
 *  org.deegree.owscommon.OWSDomainType
 */
package org.deegree.ogcwebservices.wfs.capabilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.deegree.datatypes.Code;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities110Fragment;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.deegree.ogcwebservices.wfs.capabilities.GMLObject;
import org.deegree.ogcwebservices.wfs.capabilities.Operation;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.deegree.ogcwebservices.wfs.capabilities.WFSOperationsMetadata;
import org.deegree.owscommon.OWSCommonCapabilitiesDocument;
import org.deegree.owscommon.OWSDomainType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class WFSCapabilitiesDocument_1_1_0
extends OWSCommonCapabilitiesDocument {
    private static final long serialVersionUID = 6664839532969382269L;
    private static ILogger LOG = LoggerFactory.getLogger(WFSCapabilitiesDocument_1_1_0.class);
    public static final String FEATURE_TYPE_LIST_NAME = "FeatureTypeList";
    public static final String SERVES_GML_OBJECT_TYPE_LIST_NAME = "ServesGMLObjectTypeList";
    public static final String SUPPORTS_GML_OBJECT_TYPE_LIST_NAME = "SupportsGMLObjectTypeList";
    public static final String FILTER_CAPABILITIES_NAME = "FilterCapabilities";
    protected static final URI WFSNS = CommonNamespaces.WFSNS;
    private static final String PRE_OWS = "ows:";
    protected static final URI OGCNS = CommonNamespaces.OGCNS;
    protected static final URI DEEGREEWFSNS = CommonNamespaces.DEEGREEWFS;
    private static final String XML_TEMPLATE = "WFSCapabilitiesTemplate.xml";
    private static final String[] VALID_TYPES = new String[]{"TC211", "FGDC", "19115", "19139"};
    private static final String[] VALID_FORMATS = new String[]{"text/xml", "text/html", "text/sgml", "text/plain", "XML"};

    public void createEmptyDocument() throws IOException, SAXException {
        URL url = WFSCapabilitiesDocument_1_1_0.class.getResource(XML_TEMPLATE);
        if (url == null) {
            throw new IOException("The resource 'WFSCapabilitiesTemplate.xml could not be found.");
        }
        this.load(url);
    }

    public void createEmptyDocument(String version) {
        Document doc = XMLTools.create();
        Element root = doc.createElementNS("http://www.opengis.net/wfs", "WFS_Capabilities");
        doc.importNode(root, false);
        this.setRootElement(root);
        root.setAttribute("version", version);
        root.setAttribute("updateSequence", "0");
    }

    public OGCCapabilities parseCapabilities() throws InvalidCapabilitiesException {
        WFSCapabilities wfsCapabilities = null;
        try {
            wfsCapabilities = new WFSCapabilities(this.parseVersion(), this.parseUpdateSequence(), this.getServiceIdentification(), this.getServiceProvider(), this.getOperationsMetadata(), this.getFeatureTypeList(), this.getServesGMLObjectTypeList(), this.getSupportsGMLObjectTypeList(), null, this.getFilterCapabilities());
        }
        catch (XMLParsingException e) {
            throw new InvalidCapabilitiesException(String.valueOf(e.getMessage()) + "\n" + StringTools.stackTraceToString((Throwable)e));
        }
        return wfsCapabilities;
    }

    @Override
    public ServiceIdentification getServiceIdentification() throws XMLParsingException {
        Element element = XMLTools.getRequiredElement(this.getRootElement(), "ows:ServiceIdentification", nsContext);
        Element serviceTypeElement = XMLTools.getRequiredElement(element, "ows:ServiceType", nsContext);
        Code serviceType = null;
        try {
            String codeSpace = XMLTools.getAttrValue(serviceTypeElement, OWSNS, "codeSpace", null);
            URI uri = codeSpace != null ? new URI(codeSpace) : null;
            serviceType = new Code(XMLTools.getStringValue(serviceTypeElement), uri);
        }
        catch (URISyntaxException e) {
            throw new XMLParsingException("Given value '" + XMLTools.getAttrValue(serviceTypeElement, OWSNS, "codeSpace", null) + "' in attribute 'codeSpace' of element 'ServiceType' " + "(namespace: '" + OWSNS + "') is not a valid URI.");
        }
        String[] serviceTypeVersions = XMLTools.getRequiredNodeAsStrings(element, "ows:ServiceTypeVersion", nsContext, ",;");
        if (serviceTypeVersions.length == 0) {
            String msg = "No version specified in 'ows:ServiceTypeVersion' element.";
            throw new XMLParsingException(msg);
        }
        String fees = XMLTools.getStringValue("Fees", OWSNS, element, null);
        String[] accessConstraints = XMLTools.getNodesAsStrings(element, "ows:AccessConstraints", nsContext);
        String title = XMLTools.getNodeAsString(element, "ows:Title", nsContext, null);
        String name = XMLTools.getNodeAsString(element, "ows:Name", nsContext, title);
        String abs = XMLTools.getNodeAsString(element, "ows:Abstract", nsContext, null);
        Keywords[] kws = this.getKeywords(XMLTools.getElements(element, "ows:Keywords", nsContext));
        ServiceIdentification serviceIdentification = new ServiceIdentification(name, serviceType, serviceTypeVersions, title, abs, kws, fees, accessConstraints);
        return serviceIdentification;
    }

    public OperationsMetadata getOperationsMetadata() throws XMLParsingException {
        List<Node> operationElementList = XMLTools.getNodes(this.getRootElement(), "ows:OperationsMetadata/ows:Operation", nsContext);
        HashMap<String, Node> operations = new HashMap<String, Node>();
        int i = 0;
        while (i < operationElementList.size()) {
            operations.put(XMLTools.getRequiredNodeAsString(operationElementList.get(i), "@name", nsContext), operationElementList.get(i));
            ++i;
        }
        org.deegree.ogcwebservices.getcapabilities.Operation getCapabilities = this.getOperation("GetCapabilities", true, operations);
        org.deegree.ogcwebservices.getcapabilities.Operation describeFeatureType = this.getOperation("DescribeFeatureType", true, operations);
        org.deegree.ogcwebservices.getcapabilities.Operation getFeature = this.getOperation("GetFeature", false, operations);
        org.deegree.ogcwebservices.getcapabilities.Operation getFeatureWithLock = this.getOperation("GetFeatureWithLock", false, operations);
        org.deegree.ogcwebservices.getcapabilities.Operation getGMLObject = this.getOperation("GetGMLObject", false, operations);
        org.deegree.ogcwebservices.getcapabilities.Operation lockFeature = this.getOperation("LockFeature", false, operations);
        org.deegree.ogcwebservices.getcapabilities.Operation transaction = this.getOperation("Transaction", false, operations);
        List<Element> parameterElementList = XMLTools.getElements(this.getRootElement(), "ows:OperationsMetadata/ows:Parameter", nsContext);
        OWSDomainType[] parameters = new OWSDomainType[parameterElementList.size()];
        int i2 = 0;
        while (i2 < parameters.length) {
            parameters[i2] = this.getOWSDomainType(null, parameterElementList.get(i2));
            ++i2;
        }
        List<Element> constraintElementList = XMLTools.getElements(this.getRootElement(), "ows:OperationsMetadata/ows:Constraint", nsContext);
        OWSDomainType[] constraints = new OWSDomainType[constraintElementList.size()];
        int i3 = 0;
        while (i3 < constraints.length) {
            constraints[i3] = this.getOWSDomainType(null, constraintElementList.get(i3));
            ++i3;
        }
        WFSOperationsMetadata metadata = new WFSOperationsMetadata(getCapabilities, describeFeatureType, getFeature, getFeatureWithLock, getGMLObject, lockFeature, transaction, parameters, constraints);
        return metadata;
    }

    public FeatureTypeList getFeatureTypeList() throws XMLParsingException {
        ArrayList<WFSFeatureType> wfsFeatureTypes = new ArrayList<WFSFeatureType>();
        FeatureTypeList featureTypeList = new FeatureTypeList(new Operation[0], wfsFeatureTypes);
        Element element = (Element)XMLTools.getNode(this.getRootElement(), "wfs:FeatureTypeList", nsContext);
        if (element != null) {
            Operation[] globalOperations = null;
            Element operationsTypeElement = (Element)XMLTools.getNode(element, "wfs:Operations", nsContext);
            if (operationsTypeElement != null) {
                globalOperations = this.getOperationsType(operationsTypeElement);
            }
            List<Element> featureTypeElementList = XMLTools.getElements(element, "wfs:FeatureType", nsContext);
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

    public GMLObject[] getServesGMLObjectTypeList() throws XMLParsingException {
        GMLObject[] gmlObjectTypes = null;
        Element element = (Element)XMLTools.getNode(this.getRootElement(), "wfs:ServesGMLObjectTypeList", nsContext);
        if (element != null) {
            List<Node> nodeList = XMLTools.getRequiredNodes(element, "wfs:GMLObjectType", nsContext);
            gmlObjectTypes = new GMLObject[nodeList.size()];
            int i = 0;
            while (i < gmlObjectTypes.length) {
                gmlObjectTypes[i] = this.getGMLObjectType((Element)nodeList.get(i));
                ++i;
            }
        }
        return gmlObjectTypes;
    }

    public GMLObject[] getSupportsGMLObjectTypeList() throws XMLParsingException {
        GMLObject[] gmlObjectTypes = null;
        Element element = (Element)XMLTools.getNode(this.getRootElement(), "wfs:SupportsGMLObjectTypeList", nsContext);
        if (element != null) {
            List<Node> nodeList = XMLTools.getRequiredNodes(element, "wfs:GMLObjectType", nsContext);
            gmlObjectTypes = new GMLObject[nodeList.size()];
            int i = 0;
            while (i < gmlObjectTypes.length) {
                gmlObjectTypes[i] = this.getGMLObjectType((Element)nodeList.get(i));
                ++i;
            }
        }
        return gmlObjectTypes;
    }

    public GMLObject getGMLObjectType(Element element) throws XMLParsingException {
        QualifiedName name = WFSCapabilitiesDocument_1_1_0.parseQualifiedName((Node)XMLTools.getRequiredNode(element, "wfs:Name/text()", nsContext));
        String title = XMLTools.getNodeAsString(element, "wfs:Title/text()", nsContext, null);
        String abstract_ = XMLTools.getNodeAsString(element, "wfs:Abstract/text()", nsContext, null);
        Keywords[] keywords = this.getKeywords(XMLTools.getNodes(element, "ows:Keywords", nsContext));
        List<Element> formatElementList = XMLTools.getElements(element, "wfs:OutputFormats/wfs:Format", nsContext);
        FormatType[] outputFormats = new FormatType[formatElementList.size()];
        int i = 0;
        while (i < outputFormats.length) {
            outputFormats[i] = this.getFormatType(formatElementList.get(i));
            ++i;
        }
        return new GMLObject(name, title, abstract_, keywords, outputFormats);
    }

    public WFSFeatureType getFeatureTypeType(Element element) throws XMLParsingException {
        QualifiedName name = WFSCapabilitiesDocument_1_1_0.parseQualifiedName((Node)XMLTools.getRequiredNode(element, "wfs:Name/text()", nsContext));
        String title = XMLTools.getRequiredNodeAsString(element, "wfs:Title/text()", nsContext);
        String abstract_ = XMLTools.getNodeAsString(element, "wfs:Abstract/text()", nsContext, null);
        Keywords[] keywords = this.getKeywords(XMLTools.getNodes(element, "ows:Keywords", nsContext));
        URI defaultSrs = null;
        URI[] otherSrs = null;
        Node noSrsElement = XMLTools.getNode(element, "wfs:NoSRS", nsContext);
        if (noSrsElement == null) {
            defaultSrs = XMLTools.getNodeAsURI(element, "wfs:DefaultSRS/text()", nsContext, null);
            if (defaultSrs == null) {
                String msg = "A 'wfs:FeatureType' element must always contain a 'wfs:NoSRS' element  or a 'wfs:DefaultSRS' element";
                throw new XMLParsingException(msg);
            }
            otherSrs = XMLTools.getNodesAsURIs(element, "wfs:OtherSRS/text()", nsContext);
        }
        Operation[] operations = null;
        Element operationsTypeElement = (Element)XMLTools.getNode(element, "wfs:Operations", nsContext);
        if (operationsTypeElement != null) {
            operations = this.getOperationsType(operationsTypeElement);
        }
        List<Element> formatElementList = XMLTools.getElements(element, "wfs:OutputFormats/wfs:Format", nsContext);
        FormatType[] formats = new FormatType[formatElementList.size()];
        int i = 0;
        while (i < formats.length) {
            formats[i] = this.getFormatType(formatElementList.get(i));
            ++i;
        }
        List<Element> wgs84BoundingBoxElements = XMLTools.getElements(element, "ows:WGS84BoundingBox", nsContext);
        if (wgs84BoundingBoxElements.size() < 1) {
            throw new XMLParsingException("A 'wfs:FeatureTypeType' must contain at least one 'ows:WGS84BoundingBox'-element.");
        }
        Envelope[] wgs84BoundingBoxes = new Envelope[wgs84BoundingBoxElements.size()];
        int i2 = 0;
        while (i2 < wgs84BoundingBoxes.length) {
            wgs84BoundingBoxes[i2] = this.getWGS84BoundingBoxType(wgs84BoundingBoxElements.get(i2));
            ++i2;
        }
        List<Element> metadataURLElementList = XMLTools.getElements(element, "wfs:MetadataURL", nsContext);
        MetadataURL[] metadataUrls = new MetadataURL[metadataURLElementList.size()];
        int i3 = 0;
        while (i3 < metadataUrls.length) {
            metadataUrls[i3] = this.getMetadataURL(metadataURLElementList.get(i3));
            ++i3;
        }
        WFSFeatureType featureType = new WFSFeatureType(name, title, abstract_, keywords, defaultSrs, otherSrs, operations, formats, wgs84BoundingBoxes, metadataUrls);
        return featureType;
    }

    public FormatType getFormatType(Element element) throws XMLParsingException {
        String[] tmp = new String[3];
        URI[] uris = new URI[3];
        tmp[0] = XMLTools.getNodeAsString(element, "@deegreewfs:inFilter", nsContext, null);
        tmp[1] = XMLTools.getNodeAsString(element, "@deegreewfs:outFilter", nsContext, null);
        tmp[2] = XMLTools.getNodeAsString(element, "@deegreewfs:schemaLocation", nsContext, null);
        int i = 0;
        while (i < tmp.length) {
            try {
                if (tmp[i] != null && !"".equals(tmp[i].trim())) {
                    if (!tmp[i].toLowerCase().startsWith("file:/")) {
                        tmp[i] = this.resolve(tmp[i]).toExternalForm();
                        LOG.logDebug("Found format " + (i == 0 ? "inFilter" : (i == 1 ? "outFilter" : "schemaLocation")) + " at location: " + tmp[i]);
                    }
                    uris[i] = new URI(tmp[i]);
                }
            }
            catch (MalformedURLException e) {
                throw new XMLParsingException("Could not resolve relative path:" + tmp[i]);
            }
            catch (URISyntaxException e) {
                throw new XMLParsingException("Not a valid URI:" + tmp[i]);
            }
            ++i;
        }
        String value = XMLTools.getRequiredNodeAsString(element, "text()", nsContext);
        return new FormatType(uris[0], uris[1], uris[2], value);
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
            filterCapabilities = new FilterCapabilities110Fragment(filterCapabilitiesElement, this.getSystemId()).parseFilterCapabilities();
        }
        return filterCapabilities;
    }
}

