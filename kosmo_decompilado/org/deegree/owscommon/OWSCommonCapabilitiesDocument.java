/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.Code
 *  org.deegree.datatypes.xlink.SimpleLink
 *  org.deegree.framework.util.StringTools
 *  org.deegree.framework.xml.ElementList
 *  org.deegree.framework.xml.XMLParsingException
 *  org.deegree.model.metadata.iso19115.Address
 *  org.deegree.model.metadata.iso19115.ContactInfo
 *  org.deegree.model.metadata.iso19115.Keywords
 *  org.deegree.model.metadata.iso19115.OnlineResource
 *  org.deegree.model.metadata.iso19115.Phone
 *  org.deegree.model.metadata.iso19115.TypeCode
 *  org.deegree.model.spatialschema.Envelope
 *  org.deegree.model.spatialschema.GeometryFactory
 *  org.deegree.ogcbase.CommonNamespaces
 *  org.deegree.ogcwebservices.getcapabilities.DCPType
 *  org.deegree.ogcwebservices.getcapabilities.HTTP
 *  org.deegree.ogcwebservices.getcapabilities.OGCCapabilitiesDocument
 *  org.deegree.ogcwebservices.getcapabilities.Protocol
 *  org.deegree.owscommon.OWSDomainType
 */
package org.deegree.owscommon;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.deegree.datatypes.Code;
import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.metadata.iso19115.Address;
import org.deegree.model.metadata.iso19115.ContactInfo;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.metadata.iso19115.Phone;
import org.deegree.model.metadata.iso19115.TypeCode;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilitiesDocument;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.Protocol;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.owscommon.OWSDomainType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class OWSCommonCapabilitiesDocument
extends OGCCapabilitiesDocument {
    public static final String ALL_NAME = "All";
    public static final String SERVICE_IDENTIFICATION_NAME = "ServiceIdentification";
    public static final String SERVICE_PROVIDER_NAME = "ServiceProvider";
    public static final String OPERATIONS_METADATA_NAME = "OperationsMetadata";
    public static final String CONTENTS_NAME = "Contents";
    protected static final URI OWSNS = CommonNamespaces.OWSNS;
    protected static final URI OGCNS = CommonNamespaces.OGCNS;

    public ServiceProvider getServiceProvider() throws XMLParsingException {
        Element element = XMLTools.getRequiredChildElement(SERVICE_PROVIDER_NAME, OWSNS, this.getRootElement());
        String providerName = XMLTools.getStringValue("ProviderName", OWSNS, element, "deegree");
        Element providerSiteElement = XMLTools.getChildElement("ProviderSite", OWSNS, element);
        SimpleLink providerSite = null;
        if (providerSiteElement != null) {
            providerSite = this.parseSimpleLink(providerSiteElement);
        }
        Element serviceContactElement = XMLTools.getRequiredChildElement("ServiceContact", OWSNS, element);
        String individualName = XMLTools.getStringValue("IndividualName", OWSNS, serviceContactElement, null);
        String positionName = XMLTools.getStringValue("PositionName", OWSNS, serviceContactElement, null);
        ContactInfo contactInfo = null;
        Element contactInfoElement = XMLTools.getChildElement("ContactInfo", OWSNS, serviceContactElement);
        if (contactInfoElement != null) {
            contactInfo = this.getContactInfo(contactInfoElement);
        }
        TypeCode role = null;
        Element roleElement = (Element)XMLTools.getNode(serviceContactElement, "ows:Role", nsContext);
        if (roleElement != null) {
            role = this.getCodeType(roleElement);
        }
        ServiceProvider serviceProvider = new ServiceProvider(providerName, providerSite, individualName, positionName, contactInfo, role);
        return serviceProvider;
    }

    public ServiceIdentification getServiceIdentification() throws XMLParsingException {
        Element element = XMLTools.getRequiredChildElement(SERVICE_IDENTIFICATION_NAME, OWSNS, this.getRootElement());
        Element serviceTypeElement = XMLTools.getRequiredChildElement("ServiceType", OWSNS, element);
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
        String title = XMLTools.getRequiredStringValue("Title", OWSNS, element);
        String serviceAbstract = XMLTools.getRequiredStringValue("Abstract", OWSNS, element);
        List<Element> keywordsList = XMLTools.getElements(element, "ows:Keywords", nsContext);
        Keywords[] keywords = this.getKeywords(keywordsList);
        String fees = XMLTools.getStringValue("Fees", OWSNS, element, null);
        String[] accessConstraints = XMLTools.getNodesAsStrings(element, "ows:AccessConstraints", nsContext);
        ServiceIdentification serviceIdentification = new ServiceIdentification(serviceType, serviceTypeVersions, title, serviceAbstract, keywords, fees, accessConstraints);
        return serviceIdentification;
    }

    protected Keywords getKeywords(Element element) throws XMLParsingException {
        TypeCode codeType = null;
        Element codeTypeElement = (Element)XMLTools.getNode(element, "ows:Type", nsContext);
        if (codeTypeElement != null) {
            codeType = this.getCodeType(codeTypeElement);
        }
        Keywords keywords = new Keywords(XMLTools.getNodesAsStrings(element, "ows:Keyword/text()", nsContext), null, codeType);
        return keywords;
    }

    public Keywords[] getKeywords(List nl) throws XMLParsingException {
        Keywords[] kws = null;
        if (nl.size() > 0) {
            kws = new Keywords[nl.size()];
            int i = 0;
            while (i < kws.length) {
                kws[i] = this.getKeywords((Element)nl.get(i));
                ++i;
            }
        }
        return kws;
    }

    protected DCPType getDCP(Element element) throws XMLParsingException {
        DCPType dcpType = null;
        try {
            Element elem = (Element)XMLTools.getRequiredNode(element, "ows:HTTP", nsContext);
            List<Node> nl = XMLTools.getNodes(elem, "ows:Get", nsContext);
            URL[] get = new URL[nl.size()];
            int i = 0;
            while (i < get.length) {
                String s = XMLTools.getNodeAsString(nl.get(i), "./@xlink:href", nsContext, null);
                if (s == null) {
                    s = XMLTools.getRequiredNodeAsString(nl.get(i), "./ows:OnlineResource/@xlink:href", nsContext);
                }
                get[i] = new URL(s);
                ++i;
            }
            nl = XMLTools.getNodes(elem, "ows:Post", nsContext);
            URL[] post = new URL[nl.size()];
            int i2 = 0;
            while (i2 < post.length) {
                String s = XMLTools.getNodeAsString(nl.get(i2), "./@xlink:href", nsContext, null);
                if (s == null) {
                    s = XMLTools.getRequiredNodeAsString(nl.get(i2), "./ows:OnlineResource/@xlink:href", nsContext);
                }
                post[i2] = new URL(s);
                ++i2;
            }
            HTTP protocol = new HTTP(get, post);
            dcpType = new DCPType((Protocol)protocol);
        }
        catch (MalformedURLException e) {
            throw new XMLParsingException("Couldn't parse DCPType onlineresource URL about: " + StringTools.stackTraceToString((Throwable)e));
        }
        return dcpType;
    }

    protected DCPType[] getDCPs(List<Element> el) throws XMLParsingException {
        DCPType[] dcpTypes = new DCPType[el.size()];
        int i = 0;
        while (i < dcpTypes.length) {
            dcpTypes[i] = this.getDCP(el.get(i));
            ++i;
        }
        return dcpTypes;
    }

    protected Operation getOperation(String name, boolean isMandatory, Map operations) throws XMLParsingException {
        Operation operation = null;
        Element operationElement = (Element)operations.get(name);
        if (operationElement == null) {
            if (isMandatory) {
                throw new XMLParsingException("Mandatory operation '" + name + "' not defined in 'OperationsMetadata'-section.");
            }
        } else {
            ElementList parameterElements = XMLTools.getChildElements("Parameter", OWSNS, operationElement);
            OWSDomainType[] parameters = new OWSDomainType[parameterElements.getLength()];
            int i = 0;
            while (i < parameters.length) {
                parameters[i] = this.getOWSDomainType(name, parameterElements.item(i));
                ++i;
            }
            DCPType[] dcps = this.getDCPs(XMLTools.getRequiredElements(operationElement, "ows:DCP", nsContext));
            operation = new Operation(name, dcps, parameters);
        }
        return operation;
    }

    protected OWSDomainType[] getContraints(Element root) throws XMLParsingException {
        OWSDomainType[] contraints = null;
        ElementList contraintElements = XMLTools.getChildElements("Constraint", OWSNS, root);
        contraints = new OWSDomainType[contraintElements.getLength()];
        int i = 0;
        while (i < contraints.length) {
            contraints[i] = this.getOWSDomainType(null, contraintElements.item(i));
            ++i;
        }
        return contraints;
    }

    protected OWSDomainType getOWSDomainType(String opname, Element element) throws XMLParsingException {
        String name = XMLTools.getRequiredNodeAsString(element, "@name", nsContext);
        String[] values = XMLTools.getNodesAsStrings(element, "ows:Value/text()", nsContext);
        if (values.length < 1) {
            throw new XMLParsingException("At least one 'ows:Value'-element must be defined in each element of type 'ows:DomainType'.");
        }
        OWSDomainType domainType = new OWSDomainType(name, values, null);
        return domainType;
    }

    protected TypeCode getCodeType(Element element) throws XMLParsingException {
        String code = XMLTools.getRequiredNodeAsString(element, "text()", nsContext);
        URI codeSpace = null;
        String codeSpaceString = XMLTools.getNodeAsString(element, "@codeSpace", nsContext, null);
        if (codeSpaceString != null) {
            try {
                codeSpace = new URI(codeSpaceString);
            }
            catch (URISyntaxException e) {
                throw new XMLParsingException("'" + codeSpaceString + "' does not denote a valid URI in: " + e.getMessage());
            }
        }
        return new TypeCode(code, codeSpace);
    }

    private ContactInfo getContactInfo(Element element) throws XMLParsingException {
        Phone phone = null;
        Element phoneElement = XMLTools.getChildElement("Phone", OWSNS, element);
        if (phoneElement != null) {
            try {
                phone = this.parsePhone(phoneElement, OWSNS);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        Address address = null;
        Element addressElement = XMLTools.getChildElement("Address", OWSNS, element);
        if (addressElement != null) {
            try {
                address = this.parseAddress(addressElement, OWSNS);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        OnlineResource onlineResource = null;
        Element onlineResourceElement = XMLTools.getChildElement("OnlineResource", OWSNS, element);
        if (onlineResourceElement != null) {
            try {
                onlineResource = this.parseOnLineResource(onlineResourceElement);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        String hoursOfService = XMLTools.getNodeAsString(element, "ows:HoursOfService/text()", nsContext, null);
        String contactInstructions = XMLTools.getNodeAsString(element, "ows:ContactInstructions/text()", nsContext, null);
        return new ContactInfo(address, contactInstructions, hoursOfService, onlineResource, phone);
    }

    protected Envelope getWGS84BoundingBoxType(Element element) throws XMLParsingException {
        double[] lowerCorner = XMLTools.getRequiredNodeAsDoubles(element, "ows:LowerCorner/text()", nsContext, " ");
        if (lowerCorner.length != 2) {
            throw new XMLParsingException("Element 'ows:LowerCorner' must contain exactly two double values.");
        }
        double[] upperCorner = XMLTools.getRequiredNodeAsDoubles(element, "ows:UpperCorner/text()", nsContext, " ");
        if (upperCorner.length != 2) {
            throw new XMLParsingException("Element 'ows:UpperCorner' must contain exactly two double values.");
        }
        return GeometryFactory.createEnvelope((double)lowerCorner[0], (double)lowerCorner[1], (double)upperCorner[0], (double)upperCorner[1], null);
    }
}

