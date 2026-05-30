/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.framework.log.ILogger
 *  org.deegree.framework.log.LoggerFactory
 *  org.deegree.framework.xml.XMLFragment
 *  org.deegree.framework.xml.XMLParsingException
 *  org.deegree.framework.xml.schema.ComplexTypeDeclaration
 *  org.deegree.framework.xml.schema.ElementDeclaration
 *  org.deegree.framework.xml.schema.SimpleTypeDeclaration
 *  org.deegree.framework.xml.schema.TypeDeclaration
 *  org.deegree.framework.xml.schema.TypeReference
 *  org.deegree.framework.xml.schema.XMLSchema
 *  org.deegree.framework.xml.schema.XMLSchemaException
 */
package org.deegree.framework.xml.schema;

import java.net.URI;
import java.util.List;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.framework.xml.schema.ComplexTypeDeclaration;
import org.deegree.framework.xml.schema.ElementDeclaration;
import org.deegree.framework.xml.schema.SimpleTypeDeclaration;
import org.deegree.framework.xml.schema.TypeDeclaration;
import org.deegree.framework.xml.schema.TypeReference;
import org.deegree.framework.xml.schema.XMLSchema;
import org.deegree.framework.xml.schema.XMLSchemaException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XSDocument
extends XMLFragment {
    private static final long serialVersionUID = 4371672452129797159L;
    private URI targetNamespace;
    private final ILogger LOG = LoggerFactory.getLogger(XSDocument.class);

    public XMLSchema parseXMLSchema() throws XMLParsingException, XMLSchemaException {
        SimpleTypeDeclaration[] simpleTypes = this.extractSimpleTypeDeclarations();
        ComplexTypeDeclaration[] complexTypes = this.extractComplexTypeDeclarations();
        ElementDeclaration[] elementDeclarations = this.extractElementDeclarations();
        return new XMLSchema(this.getTargetNamespace(), simpleTypes, complexTypes, elementDeclarations);
    }

    public synchronized URI getTargetNamespace() throws XMLParsingException {
        if (this.targetNamespace == null) {
            this.targetNamespace = XMLTools.getNodeAsURI(this.getRootElement(), "@targetNamespace", nsContext, null);
        }
        return this.targetNamespace;
    }

    public SimpleTypeDeclaration[] extractSimpleTypeDeclarations() throws XMLParsingException {
        List<Element> simpleTypeElements = XMLTools.getElements(this.getRootElement(), this.getFullName("simpleType"), nsContext);
        this.LOG.logDebug("Found " + simpleTypeElements.size() + " simple type declarations.");
        SimpleTypeDeclaration[] simpleTypeDeclarations = new SimpleTypeDeclaration[simpleTypeElements.size()];
        int i = 0;
        while (i < simpleTypeDeclarations.length) {
            simpleTypeDeclarations[i] = this.parseSimpleTypeDeclaration(simpleTypeElements.get(i));
            ++i;
        }
        return simpleTypeDeclarations;
    }

    public ComplexTypeDeclaration[] extractComplexTypeDeclarations() throws XMLParsingException {
        List<Element> complexTypeElements = XMLTools.getElements(this.getRootElement(), this.getFullName("complexType"), nsContext);
        this.LOG.logDebug("Found " + complexTypeElements.size() + " complex type declarations.");
        ComplexTypeDeclaration[] complexTypeDeclarations = new ComplexTypeDeclaration[complexTypeElements.size()];
        int i = 0;
        while (i < complexTypeDeclarations.length) {
            complexTypeDeclarations[i] = this.parseComplexTypeDeclaration(complexTypeElements.get(i));
            ++i;
        }
        return complexTypeDeclarations;
    }

    public ElementDeclaration[] extractElementDeclarations() throws XMLParsingException {
        List<Element> complexTypeElements = XMLTools.getElements(this.getRootElement(), this.getFullName("element"), nsContext);
        this.LOG.logDebug("Found " + complexTypeElements.size() + " element declarations.");
        ElementDeclaration[] elementDeclarations = new ElementDeclaration[complexTypeElements.size()];
        int i = 0;
        while (i < elementDeclarations.length) {
            elementDeclarations[i] = this.parseElementDeclaration(complexTypeElements.get(i));
            ++i;
        }
        return elementDeclarations;
    }

    public Element getComplexTypeDeclaration(String name) {
        String xPath = String.valueOf(this.getFullName("complexType[name=\"]")) + name + "\"]";
        Element element = null;
        try {
            element = (Element)XMLTools.getNode(this.getRootElement(), xPath, nsContext);
        }
        catch (XMLParsingException xMLParsingException) {
            // empty catch block
        }
        return element;
    }

    protected ElementDeclaration parseElementDeclaration(Element element) throws XMLParsingException {
        QualifiedName name = new QualifiedName(XMLTools.getRequiredNodeAsString(element, "@name", nsContext), this.getTargetNamespace());
        if (name.getLocalName().length() == 0) {
            String msg = "Error in schema document. Empty name (\"\") in element declaration found.";
            throw new XMLSchemaException(msg);
        }
        this.LOG.logDebug("Parsing element declaration '" + name + "'.");
        boolean isAbstract = XMLTools.getNodeAsBoolean(element, "@abstract", nsContext, false);
        TypeReference typeReference = null;
        Node typeNode = XMLTools.getNode(element, "@type|xs:simpleType/xs:restriction/@base|xs:simpleType/xs:extension/@base", nsContext);
        if (typeNode != null) {
            typeReference = new TypeReference(XSDocument.parseQualifiedName((Node)typeNode));
        } else {
            Element elem = (Element)XMLTools.getRequiredNode(element, this.getFullName("complexType"), nsContext);
            ComplexTypeDeclaration type = this.parseComplexTypeDeclaration(elem);
            typeReference = new TypeReference((TypeDeclaration)type);
        }
        int minOccurs = XMLTools.getNodeAsInt(element, "@minOccurs", nsContext, 1);
        int maxOccurs = -1;
        String maxOccursString = XMLTools.getNodeAsString(element, "@maxOccurs", nsContext, "1");
        if (!"unbounded".equals(maxOccursString)) {
            try {
                maxOccurs = Integer.parseInt(maxOccursString);
            }
            catch (NumberFormatException e) {
                throw new XMLParsingException("Invalid value ('" + maxOccursString + "') in 'maxOccurs' attribute. " + "Must be a valid integer value or 'unbounded'.");
            }
        }
        QualifiedName substitutionGroup = null;
        Node substitutionGroupNode = XMLTools.getNode(element, "@substitutionGroup", nsContext);
        if (substitutionGroupNode != null) {
            substitutionGroup = XSDocument.parseQualifiedName((Node)substitutionGroupNode);
        }
        return new ElementDeclaration(name, isAbstract, typeReference, minOccurs, maxOccurs, substitutionGroup);
    }

    protected SimpleTypeDeclaration parseSimpleTypeDeclaration(Element element) throws XMLParsingException {
        QualifiedName name = null;
        String localName = XMLTools.getNodeAsString(element, "@name", nsContext, null);
        if (localName != null) {
            name = new QualifiedName(localName, this.getTargetNamespace());
            if (localName.length() == 0) {
                String msg = "Error in schema document. Empty name (\"\") in simpleType declaration found.";
                throw new XMLSchemaException(msg);
            }
        }
        this.LOG.logDebug("Parsing simple type declaration '" + name + "'.");
        Node restrictionBaseNode = XMLTools.getRequiredNode(element, this.getFullName("restriction/@base"), nsContext);
        TypeReference restrictionBase = new TypeReference(XSDocument.parseQualifiedName((Node)restrictionBaseNode));
        return new SimpleTypeDeclaration(name, restrictionBase);
    }

    protected ComplexTypeDeclaration parseComplexTypeDeclaration(Element element) throws XMLParsingException {
        QualifiedName name = null;
        String localName = XMLTools.getNodeAsString(element, "@name", nsContext, null);
        if (localName != null) {
            name = new QualifiedName(localName, this.getTargetNamespace());
            if (localName.length() == 0) {
                String msg = "Error in schema document. Empty name (\"\") for complexType declaration found.";
                throw new XMLSchemaException(msg);
            }
        }
        this.LOG.logDebug("Parsing complex type declaration '" + name + "'.");
        List<Element> subElementList = null;
        TypeReference extensionBase = null;
        Node extensionBaseNode = XMLTools.getNode(element, String.valueOf(this.getFullName("complexContent/")) + this.getFullName("extension/@base"), nsContext);
        if (extensionBaseNode != null) {
            extensionBase = new TypeReference(XSDocument.parseQualifiedName((Node)extensionBaseNode));
            subElementList = XMLTools.getElements(element, String.valueOf(this.getFullName("complexContent/")) + this.getFullName("extension/") + this.getFullName("sequence/") + this.getFullName("element"), nsContext);
        } else {
            subElementList = XMLTools.getRequiredElements(element, String.valueOf(this.getFullName("sequence/")) + this.getFullName("element"), nsContext);
        }
        ElementDeclaration[] subElements = new ElementDeclaration[subElementList.size()];
        int i = 0;
        while (i < subElements.length) {
            Element subElement = subElementList.get(i);
            subElements[i] = this.parseElementDeclaration(subElement);
            ++i;
        }
        return new ComplexTypeDeclaration(name, extensionBase, subElements);
    }

    protected String getFullName(String localName) {
        String ret;
        Element root = this.getRootElement();
        String prefix = root.getPrefix();
        if (prefix != null && prefix.length() > 0) {
            URI uri = nsContext.getURI(prefix);
            if (uri == null) {
                String nsUri = root.lookupNamespaceURI(prefix);
                try {
                    nsContext.addNamespace(prefix, new URI(nsUri));
                }
                catch (Exception exc) {
                    this.LOG.logError("failed to add namespace: " + nsUri, (Throwable)exc);
                }
            }
            ret = String.valueOf(prefix) + ':' + localName;
        } else {
            ret = "xs:" + localName;
        }
        return ret;
    }
}

