/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.apache.xerces.parsers.DOMParser
 *  org.deegree.framework.xml.DOMPrinter
 */
package com.vividsolutions.wms;

import com.vividsolutions.wms.BoundingBox;
import com.vividsolutions.wms.Capabilities;
import com.vividsolutions.wms.MapLayer;
import com.vividsolutions.wms.MapStyle;
import com.vividsolutions.wms.WMService;
import com.vividsolutions.wms.util.XMLTools;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.deegree.framework.xml.DOMPrinter;
import org.saig.jump.lang.I18N;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Parser {
    private static final Logger LOGGER = Logger.getLogger(Parser.class);

    public Capabilities parseCapabilities(WMService service, InputStream inStream, String characterEncoding) throws IOException {
        Object[] values = this.getVersion(inStream, characterEncoding);
        service.setWmsVersion((String)values[0]);
        if (service.getVersion().startsWith("1.3")) {
            return this.parseCapabilities_1_3(service, (Document)values[1]);
        }
        if ("1.1.1".equals(service.getVersion()) || "1.1.0".equals(service.getVersion())) {
            return this.parseCapabilities_1_1_1(service, (Document)values[1]);
        }
        return this.parseCapabilities_1_0_0(service, (Document)values[1]);
    }

    public MapLayer wmsLayerFromNode(Node layerNode) {
        String value;
        String name = null;
        String title = null;
        LinkedList<String> srsList = new LinkedList<String>();
        LinkedList<MapLayer> subLayers = new LinkedList<MapLayer>();
        BoundingBox bbox = null;
        boolean queryable = false;
        ArrayList<MapStyle> styles = new ArrayList<MapStyle>();
        ArrayList<BoundingBox> boundingBoxList = new ArrayList<BoundingBox>();
        Node queryableNode = layerNode.getAttributes().getNamedItem("queryable");
        queryable = false;
        if (queryableNode != null && "1".equals(value = queryableNode.getNodeValue())) {
            queryable = true;
        }
        NodeList nl = layerNode.getChildNodes();
        int i = 0;
        while (i < nl.getLength()) {
            Node n = nl.item(i);
            try {
                if (n.getNodeType() == 1) {
                    if (n.getNodeName().equals("Name")) {
                        name = ((CharacterData)n.getFirstChild()).getData();
                    } else if (n.getNodeName().equals("Title")) {
                        title = ((CharacterData)n.getFirstChild()).getData();
                    } else if (n.getNodeName().equals("SRS") || n.getNodeName().equals("CRS")) {
                        String srsStr = ((CharacterData)n.getFirstChild()).getData();
                        String[] srss = StringUtils.split((String)srsStr);
                        int j = 0;
                        while (j < srss.length) {
                            srsList.add(srss[j]);
                            ++j;
                        }
                    } else if (n.getNodeName().equals("LatLonBoundingBox") || n.getNodeName().equals("EX_GeographicBoundingBox")) {
                        bbox = this.boundingBoxFromNode(n);
                        boundingBoxList.add(bbox);
                        BoundingBox bbox1 = new BoundingBox("EPSG:4326", bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
                        boundingBoxList.add(bbox1);
                    } else if (n.getNodeName().equals("BoundingBox")) {
                        bbox = this.boundingBoxFromNode(n);
                        boundingBoxList.add(bbox);
                    } else if (n.getNodeName().equals("Style")) {
                        String styleName = "";
                        String titleName = "";
                        String legendFormat = "";
                        String url = "";
                        NodeList nodeStyle = n.getChildNodes();
                        int k = 0;
                        while (k < nodeStyle.getLength()) {
                            Node n1 = nodeStyle.item(k);
                            if (n1.getNodeName().equals("Name")) {
                                styleName = ((CharacterData)n1.getFirstChild()).getData();
                            } else if (n1.getNodeName().equals("Title")) {
                                if (n1.getFirstChild() != null) {
                                    titleName = ((CharacterData)n1.getFirstChild()).getData();
                                }
                            } else if (n1.getNodeName().equals("LegendURL")) {
                                NodeList nodelegend = n1.getChildNodes();
                                int k1 = 0;
                                while (k1 < nodelegend.getLength()) {
                                    Node n2 = nodelegend.item(k1);
                                    if (n2.getNodeName().equals("Format")) {
                                        legendFormat = ((CharacterData)n2.getFirstChild()).getData();
                                    } else if (n2.getNodeName().equals("OnlineResource")) {
                                        url = n2.getAttributes().getNamedItem("xlink:href").getNodeValue();
                                    }
                                    ++k1;
                                }
                            }
                            ++k;
                        }
                        styles.add(new MapStyle(styleName, titleName, url, legendFormat));
                    } else if (n.getNodeName().equals("Layer")) {
                        subLayers.add(this.wmsLayerFromNode(n));
                    }
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)("Exception caught in wmsLayerFromNode(): " + e.toString()));
            }
            ++i;
        }
        if (!boundingBoxList.isEmpty()) {
            bbox = (BoundingBox)boundingBoxList.get(0);
        }
        return new MapLayer(name, title, srsList, subLayers, bbox, boundingBoxList, queryable, styles);
    }

    public BoundingBox boundingBoxFromNode(Node n) throws Exception {
        try {
            String srs = "";
            NamedNodeMap nm = n.getAttributes();
            double minx = Double.NEGATIVE_INFINITY;
            double miny = Double.NEGATIVE_INFINITY;
            double maxx = Double.POSITIVE_INFINITY;
            double maxy = Double.POSITIVE_INFINITY;
            if (n.getNodeName().equals("LatLonBoundingBox") || n.getNodeName().equals("EX_GeographicBoundingBox")) {
                srs = "LatLon";
            } else if (n.getNodeName().equals("BoundingBox")) {
                if (nm.getNamedItem("SRS") != null) {
                    srs = nm.getNamedItem("SRS").getNodeValue();
                } else if (nm.getNamedItem("CRS") != null) {
                    srs = nm.getNamedItem("CRS").getNodeValue();
                }
            }
            if (n.getNodeName().equals("EX_GeographicBoundingBox")) {
                NodeList childs = n.getChildNodes();
                int i = 0;
                while (i < childs.getLength()) {
                    Node currentNode = childs.item(i);
                    if (currentNode.getNodeType() == 1) {
                        String nodeName = currentNode.getNodeName();
                        String nodeValue = ((CharacterData)currentNode.getFirstChild()).getData();
                        if (nodeName.equalsIgnoreCase("westBoundLongitude")) {
                            minx = nodeValue.equals("inf") ? Double.NEGATIVE_INFINITY : Double.parseDouble(nodeValue);
                        } else if (nodeName.equalsIgnoreCase("eastBoundLongitude")) {
                            maxx = nodeValue.equals("inf") ? Double.NEGATIVE_INFINITY : Double.parseDouble(nodeValue);
                        } else if (nodeName.equalsIgnoreCase("southBoundLatitude")) {
                            miny = nodeValue.equals("inf") ? Double.NEGATIVE_INFINITY : Double.parseDouble(nodeValue);
                        } else if (nodeName.equalsIgnoreCase("northBoundLatitude")) {
                            maxy = nodeValue.equals("inf") ? Double.NEGATIVE_INFINITY : Double.parseDouble(nodeValue);
                        }
                    }
                    ++i;
                }
            } else {
                minx = nm.getNamedItem("minx").getNodeValue().equals("inf") ? Double.NEGATIVE_INFINITY : Double.parseDouble(nm.getNamedItem("minx").getNodeValue());
                miny = nm.getNamedItem("miny").getNodeValue().equals("inf") ? Double.NEGATIVE_INFINITY : Double.parseDouble(nm.getNamedItem("miny").getNodeValue());
                maxx = nm.getNamedItem("maxx").getNodeValue().equals("inf") ? Double.POSITIVE_INFINITY : Double.parseDouble(nm.getNamedItem("maxx").getNodeValue());
                maxy = nm.getNamedItem("maxy").getNodeValue().equals("inf") ? Double.POSITIVE_INFINITY : Double.parseDouble(nm.getNamedItem("maxy").getNodeValue());
            }
            if (srs.equals("LatLon")) {
                return new BoundingBox("EPSG:4326", minx, miny, maxx, maxy);
            }
            return new BoundingBox(srs, minx, miny, maxx, maxy);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            throw new Exception(String.valueOf(I18N.getString("com.vividsolutions.wms.Parser.invalid-bounding-box-element-node")) + ": " + e.toString());
        }
    }

    private Object[] getVersion(InputStream inStream, String characterEncoding) throws IOException {
        Document doc;
        String version = null;
        try {
            DOMParser parser = new DOMParser();
            parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            parser.setFeature("http://xml.org/sax/features/validation", false);
            InputSource source = new InputSource(inStream);
            parser.parse(source);
            doc = parser.getDocument();
        }
        catch (SAXException saxe) {
            throw new IOException(saxe.toString());
        }
        try {
            Node capsNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities");
            if (capsNode == null) {
                capsNode = XMLTools.simpleXPath(doc, "WMS_Capabilities");
            }
            version = capsNode.getAttributes().getNamedItem("version").getNodeValue();
        }
        catch (Exception e) {
            LOGGER.error((Object)e);
            throw new IOException("Maybe wrong Capabilities Version! ");
        }
        return new Object[]{version, doc};
    }

    private Capabilities parseCapabilities_1_0_0(WMService service, Document doc) throws IOException {
        Node getFeatureInfoOnlineResourceNode;
        MapLayer topLayer = null;
        String title = null;
        String description = null;
        String getMapUrl = null;
        String getFeatureInfoUrl = null;
        String capsString = null;
        LinkedList<String> formatList = new LinkedList<String>();
        LinkedList<String> infoFormatList = new LinkedList<String>();
        LinkedList<String> errorFormatList = new LinkedList<String>();
        Node capsNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities");
        capsString = DOMPrinter.nodeToString((Node)capsNode, (String)"");
        try {
            title = ((CharacterData)XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Service/Title").getFirstChild()).getData();
            description = ((CharacterData)XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Service/Abstract").getFirstChild()).getData();
        }
        catch (Exception e) {
            throw new IOException("Maybe wrong Capabilities Version! ");
        }
        Node formatNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Capability/Request/Map/Format");
        NodeList nl = formatNode.getChildNodes();
        int i = 0;
        while (i < nl.getLength()) {
            Node n = nl.item(i);
            if (n.getNodeType() == 1 && "Format".equals(n.getNodeName())) {
                formatList.add(n.getFirstChild().getNodeValue());
            }
            ++i;
        }
        Node infoFormatNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Capability/Request/GetFeatureInfo");
        if (infoFormatNode != null) {
            NodeList infoNodeList = infoFormatNode.getChildNodes();
            int i2 = 0;
            while (i2 < infoNodeList.getLength()) {
                Node n = infoNodeList.item(i2);
                if (n.getNodeType() == 1 && "Format".equals(n.getNodeName())) {
                    infoFormatList.add(n.getFirstChild().getNodeValue());
                }
                ++i2;
            }
        }
        Node exceptionFormatNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Capability/Exception");
        NodeList exceptionNodeList = exceptionFormatNode.getChildNodes();
        int i3 = 0;
        while (i3 < exceptionNodeList.getLength()) {
            Node n = exceptionNodeList.item(i3);
            if (n.getNodeType() == 1 && "Format".equals(n.getNodeName())) {
                errorFormatList.add(n.getFirstChild().getNodeValue());
            }
            ++i3;
        }
        topLayer = this.wmsLayerFromNode(XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Capability/Layer"));
        Node getMapOnlineResourceNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Capability/Request/GetMap/DCPType/HTTP/Get/OnlineResource");
        if (getMapOnlineResourceNode != null && StringUtils.isNotEmpty((String)(getMapUrl = this.extractUrlFromOnlineResourceNode(getMapOnlineResourceNode))) && !StringUtils.contains((String)getMapUrl, (char)'?')) {
            getMapUrl = String.valueOf(getMapUrl) + "?";
        }
        if ((getFeatureInfoOnlineResourceNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Capability/Request/GetFeatureInfo/DCPType/HTTP/Get/OnlineResource")) != null && StringUtils.isNotEmpty((String)(getFeatureInfoUrl = this.extractUrlFromOnlineResourceNode(getFeatureInfoOnlineResourceNode))) && !StringUtils.contains((String)getFeatureInfoUrl, (char)'?')) {
            getFeatureInfoUrl = String.valueOf(getFeatureInfoUrl) + "?";
        }
        return new Capabilities(service, title, topLayer, formatList, description, getMapUrl, getFeatureInfoUrl, infoFormatList, errorFormatList, capsString);
    }

    private Capabilities parseCapabilities_1_1_1(WMService service, Document doc) throws IOException {
        Node getFeatureInfoOnlineResourceNode;
        MapLayer topLayer = null;
        String title = null;
        String description = null;
        String getMapUrl = null;
        String getFeatureInfoUrl = null;
        String capsString = null;
        LinkedList<String> formatList = new LinkedList<String>();
        LinkedList<String> infoFormatList = new LinkedList<String>();
        LinkedList<String> errorFormatList = new LinkedList<String>();
        Node capsNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities");
        capsString = DOMPrinter.nodeToString((Node)capsNode, (String)"");
        try {
            CharacterData descriptionData;
            Node descriptionNode;
            CharacterData titleData;
            Node titleNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Service/Title");
            if (titleNode != null && (titleData = (CharacterData)titleNode.getFirstChild()) != null) {
                title = titleData.getData();
            }
            if ((descriptionNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Service/Abstract")) != null && (descriptionData = (CharacterData)descriptionNode.getFirstChild()) != null) {
                description = descriptionData.getData();
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        Node getMapNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Capability/Request/GetMap");
        NodeList getMapNodeList = getMapNode.getChildNodes();
        int i = 0;
        while (i < getMapNodeList.getLength()) {
            Node n = getMapNodeList.item(i);
            if (n.getNodeType() == 1 && "Format".equals(n.getNodeName())) {
                formatList.add(n.getFirstChild().getNodeValue());
            }
            ++i;
        }
        Node infoFormatNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Capability/Request/GetFeatureInfo");
        if (infoFormatNode != null) {
            NodeList infoNodeList = infoFormatNode.getChildNodes();
            int i2 = 0;
            while (i2 < infoNodeList.getLength()) {
                Node n = infoNodeList.item(i2);
                if (n.getNodeType() == 1 && "Format".equals(n.getNodeName())) {
                    infoFormatList.add(n.getFirstChild().getNodeValue());
                }
                ++i2;
            }
        }
        Node exceptionFormatNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Capability/Exception");
        NodeList exceptionNodeList = exceptionFormatNode.getChildNodes();
        int i3 = 0;
        while (i3 < exceptionNodeList.getLength()) {
            Node n = exceptionNodeList.item(i3);
            if (n.getNodeType() == 1 && "Format".equals(n.getNodeName())) {
                errorFormatList.add(n.getFirstChild().getNodeValue());
            }
            ++i3;
        }
        topLayer = this.wmsLayerFromNode(XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Capability/Layer"));
        Node getMapOnlineResourceNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Capability/Request/GetMap/DCPType/HTTP/Get/OnlineResource");
        if (getMapOnlineResourceNode != null && StringUtils.isNotEmpty((String)(getMapUrl = this.extractUrlFromOnlineResourceNode(getMapOnlineResourceNode))) && !StringUtils.contains((String)getMapUrl, (char)'?')) {
            getMapUrl = String.valueOf(getMapUrl) + "?";
        }
        if ((getFeatureInfoOnlineResourceNode = XMLTools.simpleXPath(doc, "WMT_MS_Capabilities/Capability/Request/GetFeatureInfo/DCPType/HTTP/Get/OnlineResource")) != null && StringUtils.isNotEmpty((String)(getFeatureInfoUrl = this.extractUrlFromOnlineResourceNode(getFeatureInfoOnlineResourceNode))) && !StringUtils.contains((String)getFeatureInfoUrl, (char)'?')) {
            getFeatureInfoUrl = String.valueOf(getFeatureInfoUrl) + "?";
        }
        return new Capabilities(service, title, topLayer, formatList, description, getMapUrl, getFeatureInfoUrl, infoFormatList, errorFormatList, capsString);
    }

    private String extractUrlFromOnlineResourceNode(Node n) {
        return n.getAttributes().getNamedItem("xlink:href").getNodeValue();
    }

    private Capabilities parseCapabilities_1_3(WMService service, Document doc) throws IOException {
        Node getFeatureInfoOnlineResourceNode;
        MapLayer topLayer = null;
        String title = null;
        String description = null;
        String getMapUrl = null;
        String getFeatureInfoUrl = null;
        String capsString = null;
        LinkedList<String> formatList = new LinkedList<String>();
        LinkedList<String> infoFormatList = new LinkedList<String>();
        LinkedList<String> errorFormatList = new LinkedList<String>();
        Node capsNode = XMLTools.simpleXPath(doc, "WMS_Capabilities");
        capsString = DOMPrinter.nodeToString((Node)capsNode, (String)"");
        try {
            CharacterData descriptionData;
            Node descriptionNode;
            CharacterData titleData;
            Node titleNode = XMLTools.simpleXPath(doc, "WMS_Capabilities/Service/Title");
            if (titleNode != null && (titleData = (CharacterData)titleNode.getFirstChild()) != null) {
                title = titleData.getData();
            }
            if ((descriptionNode = XMLTools.simpleXPath(doc, "WMS_Capabilities/Service/Abstract")) != null && (descriptionData = (CharacterData)descriptionNode.getFirstChild()) != null) {
                description = descriptionData.getData();
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        Node formatNode = XMLTools.simpleXPath(doc, "WMS_Capabilities/Capability/Request/GetMap");
        NodeList nl = formatNode.getChildNodes();
        int i = 0;
        while (i < nl.getLength()) {
            Node n = nl.item(i);
            if (n.getNodeType() == 1 && "Format".equals(n.getNodeName())) {
                formatList.add(n.getFirstChild().getNodeValue());
            }
            ++i;
        }
        Node infoFormatNode = XMLTools.simpleXPath(doc, "WMS_Capabilities/Capability/Request/GetFeatureInfo");
        if (infoFormatNode != null) {
            NodeList infoNodeList = infoFormatNode.getChildNodes();
            int i2 = 0;
            while (i2 < infoNodeList.getLength()) {
                Node n = infoNodeList.item(i2);
                if (n.getNodeType() == 1 && "Format".equals(n.getNodeName())) {
                    infoFormatList.add(n.getFirstChild().getNodeValue());
                }
                ++i2;
            }
        }
        Node exceptionFormatNode = XMLTools.simpleXPath(doc, "WMS_Capabilities/Capability/Exception");
        NodeList exceptionNodeList = exceptionFormatNode.getChildNodes();
        int i3 = 0;
        while (i3 < exceptionNodeList.getLength()) {
            Node n = exceptionNodeList.item(i3);
            if (n.getNodeType() == 1 && "Format".equals(n.getNodeName())) {
                errorFormatList.add(n.getFirstChild().getNodeValue());
            }
            ++i3;
        }
        topLayer = this.wmsLayerFromNode(XMLTools.simpleXPath(doc, "WMS_Capabilities/Capability/Layer"));
        Node getMapOnlineResourceNode = XMLTools.simpleXPath(doc, "WMS_Capabilities/Capability/Request/GetMap/DCPType/HTTP/Get/OnlineResource");
        if (getMapOnlineResourceNode != null && StringUtils.isNotEmpty((String)(getMapUrl = this.extractUrlFromOnlineResourceNode(getMapOnlineResourceNode))) && !StringUtils.contains((String)getMapUrl, (char)'?')) {
            getMapUrl = String.valueOf(getMapUrl) + "?";
        }
        if ((getFeatureInfoOnlineResourceNode = XMLTools.simpleXPath(doc, "WMS_Capabilities/Capability/Request/GetFeatureInfo/DCPType/HTTP/Get/OnlineResource")) != null && StringUtils.isNotEmpty((String)(getFeatureInfoUrl = this.extractUrlFromOnlineResourceNode(getFeatureInfoOnlineResourceNode))) && !StringUtils.contains((String)getFeatureInfoUrl, (char)'?')) {
            getFeatureInfoUrl = String.valueOf(getFeatureInfoUrl) + "?";
        }
        return new Capabilities(service, title, topLayer, formatList, description, getMapUrl, getFeatureInfoUrl, infoFormatList, errorFormatList, capsString);
    }
}

