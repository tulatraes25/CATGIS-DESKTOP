/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.framework.log.ILogger
 *  org.deegree.framework.log.LoggerFactory
 *  org.deegree.framework.util.StringTools
 *  org.deegree.framework.xml.ElementList
 *  org.deegree.framework.xml.NamespaceContext
 *  org.deegree.framework.xml.XMLFragment
 *  org.deegree.framework.xml.XMLParsingException
 *  org.deegree.ogcbase.CommonNamespaces
 *  org.jaxen.JaxenException
 *  org.jaxen.NamespaceContext
 *  org.jaxen.dom.DOMXPath
 */
package org.deegree.framework.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.ogcbase.CommonNamespaces;
import org.jaxen.JaxenException;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class XMLTools {
    private static final ILogger LOG = LoggerFactory.getLogger(XMLTools.class);

    private XMLTools() {
    }

    public static Node getNode(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        Node node;
        block7: {
            node = null;
            try {
                DOMXPath xpath = new DOMXPath(xPathQuery);
                xpath.setNamespaceContext((org.jaxen.NamespaceContext)nsContext);
                node = (Node)xpath.selectSingleNode((Object)contextNode);
                if (!xPathQuery.endsWith("text()")) break block7;
                List nl = xpath.selectNodes((Object)contextNode);
                int pos = xPathQuery.lastIndexOf("/");
                xPathQuery = pos > 0 ? xPathQuery.substring(0, pos) : ".";
                xpath = new DOMXPath(xPathQuery);
                xpath.setNamespaceContext((org.jaxen.NamespaceContext)nsContext);
                List nl_ = xpath.selectNodes((Object)contextNode);
                ArrayList<String> tmp = new ArrayList<String>(nl_.size());
                int i = 0;
                while (i < nl_.size()) {
                    tmp.add(XMLTools.getStringValue((Node)nl_.get(i)));
                    ++i;
                }
                i = 0;
                while (i < nl.size()) {
                    try {
                        ((Node)nl.get(i)).getParentNode().removeChild((Node)nl.get(i));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    ++i;
                }
                Document doc = contextNode.getOwnerDocument();
                int i2 = 0;
                while (i2 < tmp.size()) {
                    Text text = doc.createTextNode((String)tmp.get(i2));
                    ((Node)nl_.get(i2)).appendChild(text);
                    node = text;
                    ++i2;
                }
            }
            catch (JaxenException e) {
                throw new XMLParsingException("Error evaluating XPath-expression '" + xPathQuery + "' from context node '" + contextNode.getNodeName() + "': " + e.getMessage(), (Throwable)e);
            }
        }
        return node;
    }

    public static Element getElement(Node contextNode, String xpath, NamespaceContext nsContext) throws XMLParsingException {
        Node node = XMLTools.getNode(contextNode, xpath, nsContext);
        return (Element)node;
    }

    public static String getNodeAsString(Node contextNode, String xPathQuery, NamespaceContext nsContext, String defaultValue) throws XMLParsingException {
        String value = defaultValue;
        Node node = XMLTools.getNode(contextNode, xPathQuery, nsContext);
        if (node != null) {
            value = XMLTools.getStringValue(node);
        }
        return value;
    }

    public static boolean getNodeAsBoolean(Node contextNode, String xPathQuery, NamespaceContext nsContext, boolean defaultValue) throws XMLParsingException {
        boolean value = defaultValue;
        Node node = XMLTools.getNode(contextNode, xPathQuery, nsContext);
        if (node != null) {
            String stringValue = XMLTools.getStringValue(node);
            if ("true".equals(stringValue) || "yes".equals(stringValue) || "1".equals(stringValue)) {
                value = true;
            } else if ("false".equals(stringValue) || "no".equals(stringValue) || "0".equals(stringValue)) {
                value = false;
            } else {
                throw new XMLParsingException("XPath-expression '" + xPathQuery + " ' from context node '" + contextNode.getNodeName() + "' has an invalid value ('" + stringValue + "'). Valid values are: 'true', 'yes', '1' " + "'false', 'no' and '0'.");
            }
        }
        return value;
    }

    public static int getNodeAsInt(Node contextNode, String xPathQuery, NamespaceContext nsContext, int defaultValue) throws XMLParsingException {
        int value = defaultValue;
        Node node = XMLTools.getNode(contextNode, xPathQuery, nsContext);
        if (node != null) {
            String stringValue = XMLTools.getStringValue(node);
            try {
                value = Integer.parseInt(stringValue);
            }
            catch (NumberFormatException e) {
                throw new XMLParsingException("Result '" + stringValue + "' of XPath-expression '" + xPathQuery + "' from context node '" + contextNode.getNodeName() + "' does not denote a valid integer value.");
            }
        }
        return value;
    }

    public static double getNodeAsDouble(Node contextNode, String xPathQuery, NamespaceContext nsContext, double defaultValue) throws XMLParsingException {
        double value = defaultValue;
        Node node = XMLTools.getNode(contextNode, xPathQuery, nsContext);
        if (node != null) {
            String stringValue = XMLTools.getStringValue(node);
            try {
                value = Double.parseDouble(stringValue);
            }
            catch (NumberFormatException e) {
                throw new XMLParsingException("Result '" + stringValue + "' of XPath-expression '" + xPathQuery + "' from context node '" + contextNode.getNodeName() + "' does not denote a valid double value.");
            }
        }
        return value;
    }

    public static URI getNodeAsURI(Node contextNode, String xPathQuery, NamespaceContext nsContext, URI defaultValue) throws XMLParsingException {
        URI value = defaultValue;
        Node node = XMLTools.getNode(contextNode, xPathQuery, nsContext);
        if (node != null) {
            String stringValue = XMLTools.getStringValue(node);
            try {
                value = new URI(stringValue);
            }
            catch (URISyntaxException e) {
                throw new XMLParsingException("Result '" + stringValue + "' of XPath-expression '" + xPathQuery + "' from context node '" + contextNode.getNodeName() + "' does not denote a valid URI.");
            }
        }
        return value;
    }

    public static QualifiedName getNodeAsQualifiedName(Node contextNode, String xPathQuery, NamespaceContext nsContext, QualifiedName defaultValue) throws XMLParsingException {
        QualifiedName value = defaultValue;
        Node node = XMLTools.getNode(contextNode, xPathQuery, nsContext);
        if (node != null) {
            value = XMLTools.getQualifiedNameValue(node);
        }
        return value;
    }

    public static List<Node> getNodes(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        List nl;
        block7: {
            nl = null;
            try {
                DOMXPath xpath = new DOMXPath(xPathQuery);
                xpath.setNamespaceContext((org.jaxen.NamespaceContext)nsContext);
                nl = xpath.selectNodes((Object)contextNode);
                if (!xPathQuery.endsWith("text()")) break block7;
                int pos = xPathQuery.lastIndexOf("/");
                xPathQuery = pos > 0 ? xPathQuery.substring(0, pos) : ".";
                xpath = new DOMXPath(xPathQuery);
                xpath.setNamespaceContext((org.jaxen.NamespaceContext)nsContext);
                List nl_ = xpath.selectNodes((Object)contextNode);
                ArrayList<String> tmp = new ArrayList<String>(nl_.size());
                int i = 0;
                while (i < nl_.size()) {
                    tmp.add(XMLTools.getStringValue((Node)nl_.get(i)));
                    ++i;
                }
                i = 0;
                while (i < nl.size()) {
                    try {
                        ((Node)nl.get(i)).getParentNode().removeChild((Node)nl.get(i));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    ++i;
                }
                nl.clear();
                Document doc = contextNode.getOwnerDocument();
                int i2 = 0;
                while (i2 < tmp.size()) {
                    Text text = doc.createTextNode((String)tmp.get(i2));
                    ((Node)nl_.get(i2)).appendChild(text);
                    nl.add(text);
                    ++i2;
                }
            }
            catch (JaxenException e) {
                throw new XMLParsingException("Error evaluating XPath-expression '" + xPathQuery + "' from context node '" + contextNode.getNodeName() + "': " + e.getMessage(), (Throwable)e);
            }
        }
        return nl;
    }

    public static String[] getNodesAsStrings(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        String[] values = null;
        List<Node> nl = XMLTools.getNodes(contextNode, xPathQuery, nsContext);
        if (nl != null) {
            values = new String[nl.size()];
            int i = 0;
            while (i < nl.size()) {
                values[i] = XMLTools.getStringValue(nl.get(i));
                ++i;
            }
        } else {
            values = new String[]{};
        }
        return values;
    }

    public static List<String> getNodesAsStringList(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        ArrayList<String> result = new ArrayList<String>();
        List<Node> nl = XMLTools.getNodes(contextNode, xPathQuery, nsContext);
        if (nl != null) {
            result = new ArrayList(nl.size());
            int i = 0;
            while (i < nl.size()) {
                result.add(XMLTools.getStringValue(nl.get(i)));
                ++i;
            }
        }
        return result;
    }

    public static URI[] getNodesAsURIs(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        String[] values = XMLTools.getNodesAsStrings(contextNode, xPathQuery, nsContext);
        URI[] uris = new URI[values.length];
        int i = 0;
        while (i < uris.length) {
            try {
                String currentValue = values[i];
                if (currentValue.startsWith(">")) {
                    currentValue = values[i].substring(1, values[i].length());
                }
                uris[i] = new URI(currentValue);
            }
            catch (URISyntaxException e) {
                throw new XMLParsingException("Result '" + values[i] + "' of XPath-expression '" + xPathQuery + "' from context node '" + contextNode.getNodeName() + "' does not denote a valid URI.");
            }
            ++i;
        }
        return uris;
    }

    public static QualifiedName[] getNodesAsQualifiedNames(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        QualifiedName[] values = null;
        List<Node> nl = XMLTools.getNodes(contextNode, xPathQuery, nsContext);
        if (nl != null) {
            values = new QualifiedName[nl.size()];
            int i = 0;
            while (i < nl.size()) {
                values[i] = XMLTools.getQualifiedNameValue(nl.get(i));
                ++i;
            }
        } else {
            values = new QualifiedName[]{};
        }
        return values;
    }

    public static Node getRequiredNode(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        Node node = XMLTools.getNode(contextNode, xPathQuery, nsContext);
        if (node == null) {
            throw new XMLParsingException("XPath-expression '" + xPathQuery + "' from context node '" + contextNode.getNodeName() + "' yields no result!");
        }
        return node;
    }

    public static Element getRequiredElement(Node contextNode, String xpath, NamespaceContext nsContext) throws XMLParsingException {
        Node node = XMLTools.getRequiredNode(contextNode, xpath, nsContext);
        return (Element)node;
    }

    public static String getRequiredNodeAsString(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        Node node = XMLTools.getRequiredNode(contextNode, xPathQuery, nsContext);
        return XMLTools.getStringValue(node);
    }

    public static String getRequiredNodeAsString(Node contextNode, String xPathQuery, NamespaceContext nsContext, String[] validValues) throws XMLParsingException {
        String value = XMLTools.getRequiredNodeAsString(contextNode, xPathQuery, nsContext);
        boolean found = false;
        int i = 0;
        while (i < validValues.length) {
            if (value.equals(validValues[i])) {
                found = true;
                break;
            }
            ++i;
        }
        if (!found) {
            StringBuffer sb = new StringBuffer("XPath-expression '" + xPathQuery + " ' from context node '" + contextNode.getNodeName() + "' has an invalid value. Valid values are: ");
            int i2 = 0;
            while (i2 < validValues.length) {
                sb.append("'").append(validValues[i2]).append("'");
                if (i2 != validValues.length - 1) {
                    sb.append(", ");
                } else {
                    sb.append(".");
                }
                ++i2;
            }
            throw new XMLParsingException(sb.toString());
        }
        return value;
    }

    public static String[] getRequiredNodeAsStrings(Node contextNode, String xPathQuery, NamespaceContext nsContext, String regex) throws XMLParsingException {
        Node node = XMLTools.getRequiredNode(contextNode, xPathQuery, nsContext);
        return StringTools.toArray((String)XMLTools.getStringValue(node), (String)regex, (boolean)false);
    }

    public static boolean getRequiredNodeAsBoolean(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        boolean value = false;
        Node node = XMLTools.getRequiredNode(contextNode, xPathQuery, nsContext);
        String stringValue = XMLTools.getStringValue(node);
        if ("true".equals(stringValue) || "yes".equals(stringValue)) {
            value = true;
        } else if ("false".equals(stringValue) || "no".equals(stringValue)) {
            value = false;
        } else {
            throw new XMLParsingException("XPath-expression '" + xPathQuery + " ' from context node '" + contextNode.getNodeName() + "' has an invalid value ('" + stringValue + "'). Valid values are: 'true', 'yes', 'false' and 'no'.");
        }
        return value;
    }

    public static int getRequiredNodeAsInt(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        int value = 0;
        String stringValue = XMLTools.getRequiredNodeAsString(contextNode, xPathQuery, nsContext);
        try {
            value = Integer.parseInt(stringValue);
        }
        catch (NumberFormatException e) {
            throw new XMLParsingException("Result '" + stringValue + "' of XPath-expression '" + xPathQuery + "' from context node '" + contextNode.getNodeName() + "' does not denote a valid integer value.");
        }
        return value;
    }

    public static double getRequiredNodeAsDouble(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        double value = 0.0;
        String stringValue = XMLTools.getRequiredNodeAsString(contextNode, xPathQuery, nsContext);
        try {
            value = Double.parseDouble(stringValue);
        }
        catch (NumberFormatException e) {
            throw new XMLParsingException("Result '" + stringValue + "' of XPath-expression '" + xPathQuery + "' from context node '" + contextNode.getNodeName() + "' does not denote a valid double value.");
        }
        return value;
    }

    public static double[] getRequiredNodeAsDoubles(Node contextNode, String xPathQuery, NamespaceContext nsContext, String regex) throws XMLParsingException {
        String[] parts = XMLTools.getRequiredNodeAsStrings(contextNode, xPathQuery, nsContext, regex);
        double[] doubles = new double[parts.length];
        int i = 0;
        while (i < parts.length) {
            try {
                doubles[i] = Double.parseDouble(parts[i]);
            }
            catch (NumberFormatException e) {
                throw new XMLParsingException("Value '" + parts[i] + "' does not denote a valid double value.");
            }
            ++i;
        }
        return doubles;
    }

    public static URI getRequiredNodeAsURI(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        URI uri = null;
        String stringValue = XMLTools.getRequiredNodeAsString(contextNode, xPathQuery, nsContext);
        try {
            uri = new URI(stringValue);
        }
        catch (URISyntaxException e) {
            throw new XMLParsingException("Result '" + stringValue + "' of XPath-expression '" + xPathQuery + "' from context node '" + contextNode.getNodeName() + "' does not denote a valid URI.");
        }
        return uri;
    }

    public static QualifiedName getRequiredNodeAsQualifiedName(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        Node node = XMLTools.getRequiredNode(contextNode, xPathQuery, nsContext);
        return XMLTools.getQualifiedNameValue(node);
    }

    public static List<Node> getRequiredNodes(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        List<Node> nl = XMLTools.getNodes(contextNode, xPathQuery, nsContext);
        if (nl.size() == 0) {
            throw new XMLParsingException("XPath-expression: '" + xPathQuery + "' from context node '" + contextNode.getNodeName() + "' does not yield a result.");
        }
        return nl;
    }

    public static List<Element> getRequiredElements(Node contextNode, String xpath, NamespaceContext nsContext) throws XMLParsingException {
        List<Node> nodes = XMLTools.getRequiredNodes(contextNode, xpath, nsContext);
        ArrayList<Element> list = new ArrayList<Element>(nodes.size());
        for (Node n : nodes) {
            list.add((Element)n);
        }
        return list;
    }

    public static List<Element> getElements(Node contextNode, String xpath, NamespaceContext nsContext) throws XMLParsingException {
        List<Node> nodes = XMLTools.getNodes(contextNode, xpath, nsContext);
        ArrayList<Element> list = new ArrayList<Element>(nodes.size());
        for (Node n : nodes) {
            list.add((Element)n);
        }
        return list;
    }

    public static String[] getRequiredNodesAsStrings(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        List<Node> nl = XMLTools.getRequiredNodes(contextNode, xPathQuery, nsContext);
        String[] values = new String[nl.size()];
        int i = 0;
        while (i < nl.size()) {
            values[i] = XMLTools.getStringValue(nl.get(i));
            ++i;
        }
        return values;
    }

    public static QualifiedName[] getRequiredNodesAsQualifiedNames(Node contextNode, String xPathQuery, NamespaceContext nsContext) throws XMLParsingException {
        List<Node> nl = XMLTools.getRequiredNodes(contextNode, xPathQuery, nsContext);
        QualifiedName[] values = new QualifiedName[nl.size()];
        int i = 0;
        while (i < nl.size()) {
            values[i] = XMLTools.getQualifiedNameValue(nl.get(i));
            ++i;
        }
        return values;
    }

    public static void checkValue(String value, String[] validValues) throws XMLParsingException {
        int i = 0;
        while (i < validValues.length) {
            if (validValues[i].equals(value)) {
                return;
            }
            ++i;
        }
        StringBuffer sb = new StringBuffer("Value '").append(value).append("' is invalid. Valid values are: ");
        int i2 = 0;
        while (i2 < validValues.length) {
            sb.append("'").append(validValues[i2]).append("'");
            if (i2 != validValues.length - 1) {
                sb.append(", ");
            } else {
                sb.append(".");
            }
            ++i2;
        }
        throw new XMLParsingException(sb.toString());
    }

    public static Element appendElement(Element element, URI namespaceURI, String name) {
        return XMLTools.appendElement(element, namespaceURI, name, null);
    }

    public static void appendNSBinding(Element element, String prefix, URI namespace) {
        Attr attribute = element.getOwnerDocument().createAttributeNS(CommonNamespaces.XMLNS.toASCIIString(), "xmlns:" + prefix);
        attribute.setNodeValue(namespace.toASCIIString());
        element.getAttributes().setNamedItemNS(attribute);
    }

    public static void appendNSDefaultBinding(Element element, URI namespace) {
        Attr attribute = element.getOwnerDocument().createAttributeNS(CommonNamespaces.XMLNS.toASCIIString(), "xmlns");
        attribute.setNodeValue(namespace.toASCIIString());
        element.getAttributes().setNamedItemNS(attribute);
    }

    public static void appendNSBindings(Element element, NamespaceContext nsContext) {
        Map namespaceMap = nsContext.getNamespaceMap();
        for (String prefix : namespaceMap.keySet()) {
            if ("xmlns".equals(prefix)) continue;
            URI namespace = (URI)namespaceMap.get(prefix);
            XMLTools.appendNSBinding(element, prefix, namespace);
        }
    }

    public static String getStringValue(Node node) {
        NodeList children = node.getChildNodes();
        StringBuffer sb = new StringBuffer(children.getLength() * 500);
        if (node.getNodeValue() != null) {
            sb.append(node.getNodeValue().trim());
        }
        if (node.getNodeType() != 2) {
            int i = 0;
            while (i < children.getLength()) {
                if (children.item(i).getNodeType() == 3 || children.item(i).getNodeType() == 4) {
                    sb.append(children.item(i).getNodeValue());
                }
                ++i;
            }
        }
        return sb.toString();
    }

    public static String getStringValue(String name, URI namespace, Node node, String defaultValue) {
        String value = defaultValue;
        Element element = XMLTools.getChildElement(name, namespace, node);
        if (element != null) {
            value = XMLTools.getStringValue(element);
        }
        if (value == null || value.equals("")) {
            value = defaultValue;
        }
        return value;
    }

    public static String getRequiredStringValue(String name, URI namespace, Node node) throws XMLParsingException {
        Element element = XMLTools.getRequiredChildElement(name, namespace, node);
        return XMLTools.getStringValue(element);
    }

    public static String getRequiredAttrValue(String name, URI namespaceURI, Node node) throws XMLParsingException {
        String namespace = namespaceURI == null ? null : namespaceURI.toString();
        String value = null;
        NamedNodeMap atts = node.getAttributes();
        if (atts != null) {
            Attr attribute = null;
            attribute = namespace == null ? (Attr)atts.getNamedItem(name) : (Attr)atts.getNamedItemNS(namespace, name);
            if (attribute != null) {
                value = attribute.getValue();
            }
        }
        if (value == null) {
            throw new XMLParsingException("Required attribute " + name + '(' + namespaceURI + ") of element " + node.getNodeName() + " is missing.");
        }
        return value;
    }

    public static QualifiedName getQualifiedNameValue(Node node) throws XMLParsingException {
        String name = node.getTextContent().trim();
        QualifiedName qName = null;
        if (name.indexOf(58) > -1) {
            String[] tmp = StringTools.toArray((String)name, (String)":", (boolean)false);
            try {
                qName = new QualifiedName(tmp[0], tmp[1], XMLTools.getNamespaceForPrefix(tmp[0], node));
            }
            catch (URISyntaxException e) {
                throw new XMLParsingException(e.getMessage(), (Throwable)e);
            }
        } else {
            qName = new QualifiedName(name);
        }
        return qName;
    }

    public static URI getNamespaceForPrefix(String prefix, Node node) throws URISyntaxException {
        if (node == null) {
            return null;
        }
        if (node.getNodeType() == 1) {
            NamedNodeMap nnm = node.getAttributes();
            if (nnm != null) {
                int i = 0;
                while (i < nnm.getLength()) {
                    Attr a = (Attr)nnm.item(i);
                    if (a.getName().startsWith("xmlns:") && a.getName().endsWith(String.valueOf(':') + prefix)) {
                        return new URI(a.getValue());
                    }
                    if (prefix == null && a.getName().equals("xmlns")) {
                        return new URI(a.getValue());
                    }
                    ++i;
                }
            }
        } else if (node.getNodeType() == 2) {
            return XMLTools.getNamespaceForPrefix(prefix, ((Attr)node).getOwnerElement());
        }
        return XMLTools.getNamespaceForPrefix(prefix, node.getParentNode());
    }

    @Deprecated
    public static Element getRequiredChildElement(String name, URI namespaceURI, Node node) throws XMLParsingException {
        String namespace = namespaceURI == null ? null : namespaceURI.toString();
        NodeList nl = node.getChildNodes();
        Element element = null;
        Element childElement = null;
        if (nl != null && nl.getLength() > 0) {
            int i = 0;
            while (i < nl.getLength()) {
                String s;
                if (nl.item(i) instanceof Element && ((s = (element = (Element)nl.item(i)).getNamespaceURI()) == null && namespace == null || namespace != null && namespace.equals(s)) && element.getLocalName().equals(name)) {
                    childElement = element;
                    break;
                }
                ++i;
            }
        }
        if (childElement == null) {
            throw new XMLParsingException("Required child-element " + name + '(' + namespaceURI + ") of element " + node.getNodeName() + " is missing.");
        }
        return childElement;
    }

    @Deprecated
    public static Element getChildElement(String name, URI namespaceURI, Node node) {
        String namespace = namespaceURI == null ? null : namespaceURI.toString();
        NodeList nl = node.getChildNodes();
        Element element = null;
        Element childElement = null;
        if (nl != null && nl.getLength() > 0) {
            int i = 0;
            while (i < nl.getLength()) {
                String s;
                if (nl.item(i) instanceof Element && ((s = (element = (Element)nl.item(i)).getNamespaceURI()) == null && namespace == null || namespace != null && namespace.equals(s)) && element.getLocalName().equals(name)) {
                    childElement = element;
                    break;
                }
                ++i;
            }
        }
        return childElement;
    }

    @Deprecated
    public static ElementList getChildElements(String name, URI namespaceURI, Node node) {
        String namespace = namespaceURI == null ? null : namespaceURI.toString();
        NodeList nl = node.getChildNodes();
        Element element = null;
        ElementList elementList = new ElementList();
        if (nl != null && nl.getLength() > 0) {
            int i = 0;
            while (i < nl.getLength()) {
                String s;
                if (nl.item(i) instanceof Element && ((s = (element = (Element)nl.item(i)).getNamespaceURI()) == null && namespace == null || namespace != null && namespace.equals(s)) && element.getLocalName().equals(name)) {
                    elementList.addElement(element);
                }
                ++i;
            }
        }
        return elementList;
    }

    public static Document create() {
        return XMLTools.getDocumentBuilder().newDocument();
    }

    public static synchronized DocumentBuilder getDocumentBuilder() {
        DocumentBuilder builder = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setExpandEntityReferences(false);
            factory.setIgnoringElementContentWhitespace(false);
            factory.setValidating(false);
            try {
                factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
            builder = factory.newDocumentBuilder();
        }
        catch (Exception ex) {
            LOG.logError(ex.getMessage(), (Throwable)ex);
        }
        return builder;
    }

    @Deprecated
    public static String getAttrValue(Node node, String attrName) {
        NamedNodeMap atts = node.getAttributes();
        if (atts == null) {
            return null;
        }
        Attr a = (Attr)atts.getNamedItem(attrName);
        if (a != null) {
            return a.getValue();
        }
        return null;
    }

    public static String getAttrValue(Node node, URI namespaceURI, String attrName, String defaultVal) {
        if (node == null) {
            return null;
        }
        String namespace = namespaceURI == null ? null : namespaceURI.toString();
        NamedNodeMap atts = node.getAttributes();
        if (atts == null) {
            return defaultVal;
        }
        Attr a = null;
        a = namespace == null ? (Attr)atts.getNamedItem(attrName) : (Attr)atts.getNamedItemNS(namespace, attrName);
        if (a != null) {
            return a.getValue();
        }
        return defaultVal;
    }

    public static Document parse(Reader reader) throws IOException, SAXException {
        DocumentBuilder parser = null;
        Document doc = null;
        try {
            try {
                DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
                fac.setNamespaceAware(true);
                fac.setValidating(false);
                fac.setIgnoringElementContentWhitespace(false);
                fac.setValidating(false);
                try {
                    fac.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
                parser = fac.newDocumentBuilder();
                doc = parser.parse(new InputSource(reader));
            }
            catch (ParserConfigurationException ex) {
                throw new IOException("Unable to initialize DocumentBuilder: " + ex.getMessage());
            }
            catch (Exception e) {
                throw new SAXException(e.getMessage());
            }
        }
        finally {
            reader.close();
        }
        return doc;
    }

    @Deprecated
    public static Document parse(InputStream is) throws IOException, SAXException {
        return XMLTools.parse(new InputStreamReader(is));
    }

    public static Node copyNode(Node source, Node dest) {
        if (source.getNodeType() == 3) {
            Text tn = dest.getOwnerDocument().createTextNode(XMLTools.getStringValue(source));
            return tn;
        }
        NamedNodeMap attr = source.getAttributes();
        if (attr != null) {
            int i = 0;
            while (i < attr.getLength()) {
                ((Element)dest).setAttribute(attr.item(i).getNodeName(), attr.item(i).getNodeValue());
                ++i;
            }
        }
        NodeList list = source.getChildNodes();
        int i = 0;
        while (i < list.getLength()) {
            if (!(list.item(i) instanceof Text)) {
                if (!(list.item(i) instanceof Comment)) {
                    Element en = dest.getOwnerDocument().createElementNS(list.item(i).getNamespaceURI(), list.item(i).getNodeName());
                    if (list.item(i).getNodeValue() != null) {
                        en.setNodeValue(list.item(i).getNodeValue());
                    }
                    Node n = XMLTools.copyNode(list.item(i), en);
                    dest.appendChild(n);
                }
            } else if (list.item(i) instanceof CDATASection) {
                CDATASection cd = dest.getOwnerDocument().createCDATASection(list.item(i).getNodeValue());
                dest.appendChild(cd);
            } else {
                Text tn = dest.getOwnerDocument().createTextNode(list.item(i).getNodeValue());
                dest.appendChild(tn);
            }
            ++i;
        }
        return dest;
    }

    public static Node insertNodeInto(Node source, Node dest) {
        Node n = dest.getOwnerDocument().importNode(source, true);
        dest.appendChild(n);
        return dest;
    }

    public static Element getFirstChildElement(Node node) {
        NodeList nl = node.getChildNodes();
        Element element = null;
        if (nl != null && nl.getLength() > 0) {
            int i = 0;
            while (i < nl.getLength()) {
                if (nl.item(i) instanceof Element) {
                    element = (Element)nl.item(i);
                    break;
                }
                ++i;
            }
        }
        return element;
    }

    @Deprecated
    public static Element getChildElement(Node node, String name) {
        NodeList nl = node.getChildNodes();
        Element element = null;
        Element childElement = null;
        if (nl != null && nl.getLength() > 0) {
            int i = 0;
            while (i < nl.getLength()) {
                if (nl.item(i) instanceof Element && (element = (Element)nl.item(i)).getNodeName().equals(name)) {
                    childElement = element;
                    break;
                }
                ++i;
            }
        }
        return childElement;
    }

    public static ElementList getChildElements(Node node) {
        NodeList children = node.getChildNodes();
        ElementList list = new ElementList();
        int i = 0;
        while (i < children.getLength()) {
            if (children.item(i).getNodeType() == 1) {
                list.elements.add((Element)children.item(i));
            }
            ++i;
        }
        return list;
    }

    public static void setNodeValue(Element target, String nodeValue) {
        NodeList nl = target.getChildNodes();
        int i = 0;
        while (i < nl.getLength()) {
            target.removeChild(nl.item(i));
            ++i;
        }
        Text text = target.getOwnerDocument().createTextNode(nodeValue);
        target.appendChild(text);
    }

    public static Element appendElement(Element element, URI namespaceURI, String name, String nodeValue) {
        String namespace = namespaceURI == null ? null : namespaceURI.toString();
        Element newElement = element.getOwnerDocument().createElementNS(namespace, name);
        if (nodeValue != null && !nodeValue.equals("")) {
            newElement.appendChild(element.getOwnerDocument().createTextNode(nodeValue));
        }
        element.appendChild(newElement);
        return newElement;
    }

    public static String getAsPrettyString(String xml) {
        try {
            Element e = XMLTools.getStringFragmentAsElement(xml);
            return new XMLFragment(e).getAsPrettyString();
        }
        catch (SAXException e) {
            LOG.logError("Unknown error", (Throwable)e);
        }
        catch (IOException e) {
            LOG.logError("Unknown error", (Throwable)e);
        }
        return xml;
    }

    public static Element getStringFragmentAsElement(String fragment) throws SAXException, IOException {
        StringBuffer xml = new StringBuffer("<?xml version=\"1.0\"?>");
        xml.append("<bogus ");
        Map map = CommonNamespaces.getNamespaceContext().getNamespaceMap();
        for (String pre : map.keySet()) {
            if (pre.equals("xmlns")) continue;
            xml.append("xmlns:").append(pre).append("='");
            xml.append(((URI)map.get(pre)).toString()).append("' ");
        }
        xml.append(">");
        xml.append(fragment);
        xml.append("</bogus>");
        StringReader in = new StringReader(xml.toString());
        XMLFragment doc = new XMLFragment((Reader)in, "http://www.systemid.org");
        return (Element)doc.getRootElement().getFirstChild();
    }

    public static Element importStringFragment(String fragment, Document doc) {
        try {
            Element e = XMLTools.getStringFragmentAsElement(fragment);
            return (Element)doc.importNode(e, true);
        }
        catch (SAXException e) {
            LOG.logError("Could not convert String to XML.", (Throwable)e);
        }
        catch (IOException e) {
            LOG.logError("Could not convert String to XML.", (Throwable)e);
        }
        return null;
    }

    public static String escape(String str) {
        XMLFragment doc = new XMLFragment(new QualifiedName("dummy"));
        doc.getRootElement().setAttribute("dummy", str);
        String s = doc.getAsString().substring(52);
        return s.substring(0, s.length() - 3);
    }
}

