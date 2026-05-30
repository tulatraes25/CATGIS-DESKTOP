/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.geotools.resources.i18n.Errors
 */
package org.saig.core.styling;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.geotools.resources.i18n.Errors;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionDOMParser;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterDOMParser;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.FilterFactoryImpl;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.styling.AnchorPoint;
import org.saig.core.styling.ChannelSelection;
import org.saig.core.styling.ColorMap;
import org.saig.core.styling.ColorMapEntry;
import org.saig.core.styling.ContrastEnhancement;
import org.saig.core.styling.ContrastEnhancementImpl;
import org.saig.core.styling.Displacement;
import org.saig.core.styling.Extent;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.FeatureTypeConstraint;
import org.saig.core.styling.FeatureTypeConstraintImpl;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Font;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Halo;
import org.saig.core.styling.LabelPlacement;
import org.saig.core.styling.LinePlacement;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.Mark;
import org.saig.core.styling.NamedLayer;
import org.saig.core.styling.NamedLayerImpl;
import org.saig.core.styling.NamedStyle;
import org.saig.core.styling.PointPlacement;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.RemoteOWS;
import org.saig.core.styling.RemoteOWSImpl;
import org.saig.core.styling.Rule;
import org.saig.core.styling.SelectedChannelType;
import org.saig.core.styling.SelectedChannelTypeImpl;
import org.saig.core.styling.ShadedRelief;
import org.saig.core.styling.ShadedReliefImpl;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.StyleFactoryImpl;
import org.saig.core.styling.StyledLayer;
import org.saig.core.styling.StyledLayerDescriptor;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.core.styling.UserLayerImpl;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.MeasureUtils;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SLDParser {
    private static final Logger LOGGER = Logger.getLogger(SLDParser.class);
    private static final String channelSelectionString = "ChannelSelection";
    private static final String graphicSt = "Graphic";
    private static final String geomString = "Geometry";
    private static final String fillSt = "Fill";
    private static final String opacityString = "Opacity";
    private static final String overlapBehaviorString = "OverlapBehavior";
    private static final String colorMapString = "ColorMap";
    private static final String colorMapOpacityString = "opacity";
    private static final String colorMapColorString = "color";
    private static final String contrastEnhancementString = "ContrastEnhancement";
    private static final String shadedReliefString = "ShadedRelief";
    private static final String imageOutlineString = "ImageOutline";
    private static final String colorMapQuantityString = "quantity";
    private static final String colorMapLabelString = "label";
    private FilterFactory ff;
    protected InputSource source;
    private Document dom;
    protected StyleFactory factory;
    private URL sourceUrl;
    private FeatureSchema schema;

    public SLDParser(StyleFactory factory, FeatureSchema schema) {
        this(factory, new FilterFactoryImpl(), schema);
    }

    public SLDParser(StyleFactory factory, FilterFactory filterFactory, FeatureSchema schema) {
        this.factory = factory;
        this.ff = filterFactory;
        this.schema = schema;
    }

    public SLDParser(StyleFactory factory, String filename) throws FileNotFoundException {
        this(factory, (FeatureSchema)null);
        File f = new File(filename);
        this.setInput(f);
    }

    public SLDParser(StyleFactory factory, File f, FeatureSchema schema) throws FileNotFoundException {
        this(factory, (FeatureSchema)null);
        this.setInput(f);
    }

    public SLDParser(StyleFactory factory, URL url) throws IOException {
        this(factory, (FeatureSchema)null);
        this.setInput(url);
    }

    public SLDParser(StyleFactory factory, InputStream s) {
        this(factory, (FeatureSchema)null);
        this.setInput(s);
    }

    public SLDParser(StyleFactory factory, Reader r) {
        this(factory, (FeatureSchema)null);
        this.setInput(r);
    }

    public void setInput(String filename) throws FileNotFoundException {
        File f = new File(filename);
        this.source = new InputSource(new FileInputStream(f));
        try {
            this.sourceUrl = f.toURI().toURL();
        }
        catch (MalformedURLException e) {
            LOGGER.warn((Object)I18N.getMessage(this.getClass(), "can-not-build-url-for-file-{0}", new Object[]{f.getAbsolutePath()}));
        }
    }

    public void setInput(File f) throws FileNotFoundException {
        this.source = new InputSource(new FileInputStream(f));
        try {
            this.sourceUrl = f.toURI().toURL();
        }
        catch (MalformedURLException e) {
            LOGGER.warn((Object)I18N.getMessage(this.getClass(), "can-not-build-url-for-file-{0}", new Object[]{f.getAbsolutePath()}));
        }
    }

    public void setInput(URL url) throws IOException {
        this.source = new InputSource(url.openStream());
        this.sourceUrl = url;
    }

    public void setInput(InputStream in) {
        this.source = new InputSource(in);
    }

    public void setInput(Reader in) {
        this.source = new InputSource(in);
    }

    public Style[] readXML() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            this.dom = db.parse(this.source);
        }
        catch (ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        }
        catch (SAXException se) {
            throw new RuntimeException(se);
        }
        catch (IOException ie) {
            throw new RuntimeException(ie);
        }
        return this.readDOM(this.dom);
    }

    public Style[] readDOM(Document document) {
        this.dom = document;
        NodeList nodes = this.findElements(document, "UserStyle");
        if (nodes == null) {
            return new Style[0];
        }
        int length = nodes.getLength();
        Style[] styles = new Style[length];
        int i = 0;
        while (i < length) {
            styles[i] = this.parseStyle(nodes.item(i));
            ++i;
        }
        return styles;
    }

    private NodeList findElements(Document document, String name) {
        NodeList nodes = document.getElementsByTagNameNS("*", name);
        if (nodes.getLength() == 0) {
            nodes = document.getElementsByTagName(name);
        }
        return nodes;
    }

    private NodeList findElements(Element element, String name) {
        NodeList nodes = element.getElementsByTagNameNS("*", name);
        if (nodes.getLength() == 0) {
            nodes = element.getElementsByTagName(name);
        }
        return nodes;
    }

    public StyledLayerDescriptor parseSLD() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            this.dom = db.parse(this.source);
            StyledLayerDescriptor sld = this.parseDescriptor(this.dom.getDocumentElement());
            return sld;
        }
        catch (ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        }
        catch (SAXException se) {
            throw new RuntimeException(se);
        }
        catch (IOException ie) {
            throw new RuntimeException(ie);
        }
    }

    public StyledLayerDescriptor parseDescriptor(Node root) {
        StyledLayerDescriptor sld = this.factory.createStyledLayerDescriptor();
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                StyledLayer layer;
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase("Name")) {
                    sld.setName(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("Title")) {
                    sld.setTitle(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("Abstract")) {
                    sld.setAbstract(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("NamedLayer")) {
                    layer = this.parseNamedLayer(child);
                    sld.addStyledLayer(layer);
                } else if (childName.equalsIgnoreCase("UserLayer")) {
                    layer = this.parseUserLayer(child);
                    sld.addStyledLayer(layer);
                }
            }
            ++i;
        }
        return sld;
    }

    private StyledLayer parseUserLayer(Node root) {
        UserLayerImpl layer = new UserLayerImpl();
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                } else if (childName.equalsIgnoreCase("UserStyle")) {
                    Style user = this.parseStyle(child);
                    layer.addUserStyle(user);
                } else if (childName.equalsIgnoreCase("Name")) {
                    String layerName = child.getFirstChild().getNodeValue();
                    layer.setName(layerName);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug((Object)I18N.getMessage(this.getClass(), "layer-name-{0}", new Object[]{layer.getName()}));
                    }
                } else if (childName.equalsIgnoreCase("RemoteOWS")) {
                    RemoteOWS remoteOws = this.parseRemoteOWS(child);
                    layer.setRemoteOWS(remoteOws);
                } else if (childName.equalsIgnoreCase("LayerFeatureConstraints")) {
                    layer.setLayerFeatureConstraints(this.parseLayerFeatureConstraints(child));
                }
            }
            ++i;
        }
        return layer;
    }

    private FeatureTypeConstraint[] parseLayerFeatureConstraints(Node root) {
        ArrayList<FeatureTypeConstraint> featureTypeConstraints = new ArrayList<FeatureTypeConstraint>();
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            FeatureTypeConstraint ftc;
            String childName;
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1 && (childName = child.getLocalName()).equalsIgnoreCase("FeatureTypeConstraint") && (ftc = this.parseFeatureTypeConstraint(child)) != null) {
                featureTypeConstraints.add(ftc);
            }
            ++i;
        }
        return featureTypeConstraints.toArray(new FeatureTypeConstraint[featureTypeConstraints.size()]);
    }

    private FeatureTypeConstraint parseFeatureTypeConstraint(Node root) {
        FeatureTypeConstraintImpl ftc = new FeatureTypeConstraintImpl();
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName.equalsIgnoreCase("FeatureTypeName")) {
                    ftc.setFeatureTypeName(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("Filter")) {
                    ftc.setFilter(this.parseFilter(child));
                }
            }
            ++i;
        }
        ftc.setExtents(new Extent[0]);
        if (ftc.getFeatureTypeName() == null) {
            return null;
        }
        return ftc;
    }

    private RemoteOWS parseRemoteOWS(Node root) {
        RemoteOWSImpl ows = new RemoteOWSImpl();
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName.equalsIgnoreCase("Service")) {
                    ows.setService(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("OnlineResource")) {
                    ows.setOnlineResource(this.parseOnlineResource(child));
                }
            }
            ++i;
        }
        return ows;
    }

    private NamedLayer parseNamedLayer(Node root) {
        NamedLayerImpl layer = new NamedLayerImpl();
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase("Name")) {
                    layer.setName(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("NamedStyle")) {
                    NamedStyle style = this.parseNamedStyle(child);
                    layer.addStyle(style);
                } else if (childName.equalsIgnoreCase("UserStyle")) {
                    Style user = this.parseStyle(child);
                    layer.addStyle(user);
                } else if (childName.equalsIgnoreCase("LayerFeatureConstraints")) {
                    throw new UnsupportedOperationException(I18N.getString(this.getClass(), "layerfeatureconstraints-pending-of-implementation"));
                }
            }
            ++i;
        }
        return layer;
    }

    public NamedStyle parseNamedStyle(Node n) {
        if (this.dom == null) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                this.dom = db.newDocument();
            }
            catch (ParserConfigurationException pce) {
                throw new RuntimeException(pce);
            }
        }
        NamedStyle style = this.factory.createNamedStyle();
        NodeList children = n.getChildNodes();
        int length = children.getLength();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getMessage(this.getClass(), "{0}-children-to-process", new Object[]{children.getLength()}));
        }
        int j = 0;
        while (j < length) {
            Node child = children.item(j);
            if (child != null && child.getNodeType() == 1 && child.getFirstChild() != null) {
                String childName;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug((Object)I18N.getMessage(this.getClass(), "processing-{0}", new Object[]{child.getLocalName()}));
                }
                if ((childName = child.getLocalName()) == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase("Name")) {
                    style.setName(child.getFirstChild().getNodeValue());
                }
            }
            ++j;
        }
        return style;
    }

    public Style parseStyle(Node n) {
        if (this.dom == null) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                this.dom = db.newDocument();
            }
            catch (ParserConfigurationException pce) {
                throw new RuntimeException(pce);
            }
        }
        Style style = this.factory.createStyle();
        NodeList children = n.getChildNodes();
        int length = children.getLength();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getMessage(this.getClass(), "{0}-children-to-process", new Object[]{children.getLength()}));
        }
        int j = 0;
        while (j < length) {
            Node child = children.item(j);
            if (child != null && child.getNodeType() == 1 && child.getFirstChild() != null) {
                String childName;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug((Object)I18N.getMessage(this.getClass(), "processing-{0}", new Object[]{child.getLocalName()}));
                }
                if ((childName = child.getLocalName()) == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase("Name")) {
                    style.setName(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("Title")) {
                    style.setTitle(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("Abstract")) {
                    style.setAbstract(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("IsDefault")) {
                    style.setDefault(Boolean.valueOf(child.getFirstChild().getNodeValue()));
                } else if (childName.equalsIgnoreCase("FeatureTypeStyle")) {
                    style.addFeatureTypeStyle(this.parseFeatureTypeStyle(child));
                }
            }
            ++j;
        }
        return style;
    }

    private FeatureTypeStyle parseFeatureTypeStyle(Node style) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)("Parsing featuretype style " + style.getLocalName()));
        }
        FeatureTypeStyle ft = this.factory.createFeatureTypeStyle();
        ArrayList<Rule> rules = new ArrayList<Rule>();
        ArrayList<String> sti = new ArrayList<String>();
        NodeList children = style.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                LOGGER.debug((Object)I18N.getMessage(this.getClass(), "processing-{0}", new Object[]{child.getLocalName()}));
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase("Name")) {
                    ft.setName(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("Title")) {
                    ft.setTitle(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("Abstract")) {
                    ft.setAbstract(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("FeatureTypeName")) {
                    ft.setFeatureTypeName(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("SemanticTypeIdentifier")) {
                    sti.add(child.getFirstChild().getNodeValue());
                } else if (childName.equalsIgnoreCase("Rule")) {
                    rules.add(this.parseRule(child));
                }
            }
            ++i;
        }
        ft.setRules(rules.toArray(new Rule[0]));
        return ft;
    }

    private Rule parseRule(Node ruleNode) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)("Parsing rule " + ruleNode.getLocalName()));
        }
        Rule rule = this.factory.createRule();
        ArrayList<Symbolizer> symbolizers = new ArrayList<Symbolizer>();
        NodeList children = ruleNode.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.indexOf(58) != -1) {
                    childName = childName.substring(childName.indexOf(58) + 1);
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug((Object)I18N.getMessage(this.getClass(), "processing-{0}", new Object[]{child.getLocalName()}));
                }
                if (childName.equalsIgnoreCase("Name")) {
                    if (child != null && child.getFirstChild() != null) {
                        rule.setName(child.getFirstChild().getNodeValue());
                    } else {
                        rule.setName("");
                    }
                } else if (childName.equalsIgnoreCase("Title")) {
                    if (child != null && child.getFirstChild() != null) {
                        rule.setTitle(child.getFirstChild().getNodeValue());
                    } else {
                        rule.setTitle("");
                    }
                } else if (childName.equalsIgnoreCase("Abstract")) {
                    if (child != null && child.getFirstChild() != null) {
                        rule.setAbstract(child.getFirstChild().getNodeValue());
                    } else {
                        rule.setAbstract("");
                    }
                } else if (childName.equalsIgnoreCase("MinScaleDenominator")) {
                    if (child != null && child.getFirstChild() != null) {
                        rule.setMinScaleDenominator(Double.parseDouble(child.getFirstChild().getNodeValue()));
                    } else {
                        rule.setMinScaleDenominator(0.0);
                    }
                } else if (childName.equalsIgnoreCase("MaxScaleDenominator")) {
                    if (child != null && child.getFirstChild() != null) {
                        rule.setMaxScaleDenominator(Double.parseDouble(child.getFirstChild().getNodeValue()));
                    } else {
                        rule.setMaxScaleDenominator(0.0);
                    }
                } else if (childName.equalsIgnoreCase("Filter")) {
                    Filter filter = this.parseFilter(child);
                    rule.setFilter(filter);
                } else if (childName.equalsIgnoreCase("ElseFilter")) {
                    rule.setElseFilter(true);
                } else if (childName.equalsIgnoreCase("LegendGraphic")) {
                    this.findElements((Element)child, graphicSt);
                    NodeList g = this.findElements((Element)child, graphicSt);
                    ArrayList<Graphic> legends = new ArrayList<Graphic>();
                    int l = g.getLength();
                    int k = 0;
                    while (k < l) {
                        legends.add(this.parseGraphic(g.item(k)));
                        ++k;
                    }
                    rule.setLegendGraphic(legends.toArray(new Graphic[0]));
                } else if (childName.equalsIgnoreCase("LineSymbolizer")) {
                    symbolizers.add(this.parseLineSymbolizer(child));
                } else if (childName.equalsIgnoreCase("PolygonSymbolizer")) {
                    symbolizers.add(this.parsePolygonSymbolizer(child));
                } else if (childName.equalsIgnoreCase("PointSymbolizer")) {
                    symbolizers.add(this.parsePointSymbolizer(child));
                } else if (childName.equalsIgnoreCase("TextSymbolizer")) {
                    symbolizers.add(this.parseTextSymbolizer(child));
                } else if (childName.equalsIgnoreCase("RasterSymbolizer")) {
                    symbolizers.add(this.parseRasterSymbolizer(child));
                }
            }
            ++i;
        }
        rule.setSymbolizers(symbolizers.toArray(new Symbolizer[0]));
        return rule;
    }

    private Filter parseFilter(Node child) {
        Node firstChild = child.getFirstChild();
        while (firstChild != null && firstChild.getNodeType() != 1) {
            firstChild = firstChild.getNextSibling();
        }
        Filter filter = FilterDOMParser.parseFilter(firstChild, this.schema);
        return filter;
    }

    private LineSymbolizer parseLineSymbolizer(Node root) {
        Node uom;
        LineSymbolizer symbol = this.factory.createLineSymbolizer();
        if (root.getAttributes().getLength() > 0 && (uom = root.getAttributes().getNamedItem("uom")) != null) {
            String units = uom.getNodeValue();
            symbol.setUnitsOfMeasurement(this.toUOMUnits(units));
        }
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase(geomString)) {
                    symbol.setGeometryPropertyName(this.parseGeometryName(child));
                } else if (childName.equalsIgnoreCase("Stroke")) {
                    symbol.setStroke(this.parseStroke(child));
                }
            }
            ++i;
        }
        return symbol;
    }

    private PolygonSymbolizer parsePolygonSymbolizer(Node root) {
        Node uom;
        PolygonSymbolizer symbol = this.factory.createPolygonSymbolizer();
        symbol.setFill(null);
        symbol.setStroke(null);
        if (root.getAttributes().getLength() > 0 && (uom = root.getAttributes().getNamedItem("uom")) != null) {
            String units = uom.getNodeValue();
            symbol.setUnitsOfMeasurement(this.toUOMUnits(units));
        }
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase(geomString)) {
                    symbol.setGeometryPropertyName(this.parseGeometryName(child));
                } else if (childName.equalsIgnoreCase("Stroke")) {
                    symbol.setStroke(this.parseStroke(child));
                } else if (childName.equalsIgnoreCase(fillSt)) {
                    symbol.setFill(this.parseFill(child));
                }
            }
            ++i;
        }
        return symbol;
    }

    private TextSymbolizer parseTextSymbolizer(Node root) {
        Node uom;
        TextSymbolizer symbol = this.factory.createTextSymbolizer();
        symbol.setFill(null);
        if (root.getAttributes().getLength() > 0 && (uom = root.getAttributes().getNamedItem("uom")) != null) {
            String units = uom.getNodeValue();
            symbol.setUnitsOfMeasurement(this.toUOMUnits(units));
        }
        ArrayList<Font> fonts = new ArrayList<Font>();
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase(geomString)) {
                    symbol.setGeometryPropertyName(this.parseGeometryName(child));
                } else if (childName.equalsIgnoreCase(fillSt)) {
                    symbol.setFill(this.parseFill(child));
                } else if (childName.equalsIgnoreCase("Label")) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug((Object)I18N.getMessage(this.getClass(), "parsing-label-{0}", new Object[]{child.getNodeValue()}));
                    }
                    symbol.setLabel(this.parseCssParameter(child, false));
                    if (symbol.getLabel() == null) {
                        LOGGER.warn((Object)I18N.getString(this.getClass(), "parsing-textsymbolizer-node-could-not-find-anything-in-the-label-element"));
                    }
                }
                if (childName.equalsIgnoreCase("Font")) {
                    fonts.add(this.parseFont(child, symbol));
                } else if (childName.equalsIgnoreCase("LabelPlacement")) {
                    symbol.setLabelPlacement(this.parseLabelPlacement(child));
                } else if (childName.equalsIgnoreCase("Halo")) {
                    symbol.setHalo(this.parseHalo(child));
                } else if (childName.equalsIgnoreCase(graphicSt)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug((Object)I18N.getString(this.getClass(), "parsing-non-standard-graphic-in-textsymbolizer"));
                    }
                    symbol.setGraphic(this.parseGraphic(child));
                } else if (childName.equalsIgnoreCase("priority")) {
                    symbol.setPriority(this.parseCssParameter(child));
                } else if (childName.equalsIgnoreCase("vendoroption")) {
                    this.parseVendorOption(symbol, child);
                }
            }
            ++i;
        }
        symbol.setFonts(fonts.toArray(new Font[0]));
        return symbol;
    }

    private String toUOMUnits(String units) {
        if (units.equals("http://www.opengeospatial.org/se/units/metre")) {
            return MeasureUtils.AceptedLengthUnits.METER.getUnit().toString();
        }
        if (units.equals("http://www.opengeospatial.org/se/units/foot")) {
            return MeasureUtils.AceptedLengthUnits.FOOT.getUnit().toString();
        }
        if (units.equals("http://www.opengeospatial.org/se/units/pixel")) {
            return "pixel";
        }
        return "pixel";
    }

    private void parseVendorOption(TextSymbolizer symbol, Node child) {
        String key = child.getAttributes().getNamedItem("name").getNodeValue();
        String value = child.getFirstChild().getNodeValue();
        symbol.addToOptions(key, value);
    }

    private RasterSymbolizer parseRasterSymbolizer(Node root) {
        RasterSymbolizer symbol = this.factory.getDefaultRasterSymbolizer();
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase(geomString)) {
                    symbol.setGeometryPropertyName(this.parseGeometryName(child));
                }
                if (childName.equalsIgnoreCase(opacityString)) {
                    try {
                        Expression opacity = this.parseParameterValueExpression(child, false);
                        symbol.setOpacity(opacity);
                    }
                    catch (Throwable e) {
                        LOGGER.warn((Object)e.getLocalizedMessage(), e);
                    }
                } else if (childName.equalsIgnoreCase(channelSelectionString)) {
                    symbol.setChannelSelection(this.parseChannelSelection(child));
                } else if (childName.equalsIgnoreCase(overlapBehaviorString)) {
                    try {
                        String overlapString = child.getFirstChild().getLocalName();
                        if (overlapString != null) {
                            symbol.setOverlap(this.ff.createLiteralExpression(overlapString));
                        }
                    }
                    catch (Throwable e) {
                        LOGGER.warn((Object)e.getLocalizedMessage(), e);
                    }
                } else if (childName.equalsIgnoreCase(colorMapString)) {
                    symbol.setColorMap(this.parseColorMap(child));
                } else if (childName.equalsIgnoreCase(contrastEnhancementString)) {
                    symbol.setContrastEnhancement(this.parseContrastEnhancement(child));
                } else if (childName.equalsIgnoreCase(shadedReliefString)) {
                    symbol.setShadedRelief(this.parseShadedRelief(child));
                } else if (childName.equalsIgnoreCase(imageOutlineString)) {
                    symbol.setImageOutline(this.parseLineSymbolizer(child));
                }
            }
            ++i;
        }
        return symbol;
    }

    Expression parseParameterValueExpression(Node root, boolean mixedText) {
        Expression expr = ExpressionDOMParser.parseExpression(root, this.schema);
        if (expr != null) {
            return expr;
        }
        NodeList children = root.getChildNodes();
        if (children.getLength() == 1 && root.getFirstChild() instanceof CharacterData) {
            Node textNode = root.getFirstChild();
            String text = textNode.getNodeValue();
            return this.ff.createLiteralExpression(text.trim());
        }
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        int index = 0;
        while (index < children.getLength()) {
            Node child = children.item(index);
            if (child instanceof CharacterData) {
                if (mixedText) {
                    String text = child.getNodeValue();
                    LiteralExpression childExpr = this.ff.createLiteralExpression(text);
                    expressionList.add(childExpr);
                }
            } else {
                Expression childExpr = ExpressionDOMParser.parseExpression(child, this.schema);
                if (childExpr != null) {
                    expressionList.add(childExpr);
                }
            }
            ++index;
        }
        if (expressionList.isEmpty()) {
            return null;
        }
        if (expressionList.size() == 1) {
            return (Expression)expressionList.get(0);
        }
        if (expressionList.size() == 2) {
            Expression[] expressionArray = expressionList.toArray(new Expression[0]);
            return this.ff.createFunctionExpression("strConcat", expressionArray);
        }
        Expression[] expressionArray = expressionList.toArray(new Expression[0]);
        return this.ff.createFunctionExpression("Concatenate", expressionArray);
    }

    private ColorMapEntry parseColorMapEntry(Node root) {
        ColorMapEntry symbol = this.factory.createColorMapEntry();
        NamedNodeMap atts = root.getAttributes();
        if (atts.getNamedItem(colorMapLabelString) != null) {
            symbol.setLabel(atts.getNamedItem(colorMapLabelString).getNodeValue());
        }
        if (atts.getNamedItem(colorMapColorString) != null) {
            symbol.setColor(this.ff.createLiteralExpression(atts.getNamedItem(colorMapColorString).getNodeValue()));
        }
        if (atts.getNamedItem(colorMapOpacityString) != null) {
            symbol.setOpacity(this.ff.createLiteralExpression(atts.getNamedItem(colorMapOpacityString).getNodeValue()));
        }
        if (atts.getNamedItem(colorMapQuantityString) != null) {
            symbol.setQuantity(this.ff.createLiteralExpression(atts.getNamedItem(colorMapQuantityString).getNodeValue()));
        }
        return symbol;
    }

    private ColorMap parseColorMap(Node root) {
        NamedNodeMap atts;
        Node typeAtt;
        ColorMap symbol = this.factory.createColorMap();
        if (root.hasAttributes() && (typeAtt = (atts = root.getAttributes()).getNamedItem("type")) != null) {
            String type = typeAtt.getNodeValue();
            if ("ramp".equalsIgnoreCase(type)) {
                symbol.setType(1);
            } else if ("intervals".equalsIgnoreCase(type)) {
                symbol.setType(2);
            } else if ("values".equalsIgnoreCase(type)) {
                symbol.setType(3);
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug((Object)Errors.format((int)42, (Object)"ColorMapType", (Object)type));
            }
        }
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase("ColorMapEntry")) {
                    symbol.addColorMapEntry(this.parseColorMapEntry(child));
                }
            }
            ++i;
        }
        return symbol;
    }

    private SelectedChannelType parseSelectedChannel(Node root) {
        SelectedChannelTypeImpl symbol = new SelectedChannelTypeImpl();
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                } else if (childName.equalsIgnoreCase("SourceChannelName")) {
                    if (child.getFirstChild() != null && child.getFirstChild().getNodeType() == 3) {
                        symbol.setChannelName(child.getFirstChild().getNodeValue());
                    }
                } else if (childName.equalsIgnoreCase(contrastEnhancementString)) {
                    symbol.setContrastEnhancement(this.parseContrastEnhancement(child));
                }
            }
            ++i;
        }
        return symbol;
    }

    private ChannelSelection parseChannelSelection(Node root) {
        ArrayList<SelectedChannelType> channels = new ArrayList<SelectedChannelType>();
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                } else if (childName.equalsIgnoreCase("GrayChannel")) {
                    channels.add(this.parseSelectedChannel(child));
                } else if (childName.equalsIgnoreCase("RedChannel")) {
                    channels.add(this.parseSelectedChannel(child));
                } else if (childName.equalsIgnoreCase("GreenChannel")) {
                    channels.add(this.parseSelectedChannel(child));
                } else if (childName.equalsIgnoreCase("BlueChannel")) {
                    channels.add(this.parseSelectedChannel(child));
                }
            }
            ++i;
        }
        ChannelSelection dap = this.factory.createChannelSelection(channels.toArray(new SelectedChannelType[channels.size()]));
        return dap;
    }

    private ContrastEnhancement parseContrastEnhancement(Node root) {
        ContrastEnhancementImpl symbol = new ContrastEnhancementImpl();
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase("Normalize")) {
                    symbol.setNormalize();
                } else if (childName.equalsIgnoreCase("Histogram")) {
                    symbol.setHistogram();
                } else if (childName.equalsIgnoreCase("Logarithmic")) {
                    symbol.setLogarithmic();
                } else if (childName.equalsIgnoreCase("Exponential")) {
                    symbol.setExponential();
                } else if (childName.equalsIgnoreCase("GammaValue")) {
                    try {
                        String gammaString = child.getFirstChild().getNodeValue();
                        symbol.setGammaValue(this.ff.createLiteralExpression(Double.parseDouble(gammaString)));
                    }
                    catch (Exception e) {
                        LOGGER.warn((Object)e.getLocalizedMessage(), (Throwable)e);
                    }
                }
            }
            ++i;
        }
        return symbol;
    }

    private ShadedRelief parseShadedRelief(Node root) {
        ShadedReliefImpl symbol = new ShadedReliefImpl();
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if ("BrightnessOnly".equalsIgnoreCase(childName)) {
                    symbol.setBrightnessOnly(Boolean.getBoolean(child.getFirstChild().getNodeValue()));
                } else if ("ReliefFactor".equalsIgnoreCase(childName)) {
                    try {
                        Expression relief = ExpressionDOMParser.parseExpression(child, this.schema);
                        symbol.setReliefFactor(relief);
                    }
                    catch (Exception e) {
                        LOGGER.warn((Object)e.getLocalizedMessage(), (Throwable)e);
                    }
                }
            }
            ++i;
        }
        return symbol;
    }

    private PointSymbolizer parsePointSymbolizer(Node root) {
        Node uom;
        PointSymbolizer symbol = this.factory.getDefaultPointSymbolizer();
        if (root.getAttributes().getLength() > 0 && (uom = root.getAttributes().getNamedItem("uom")) != null) {
            String units = uom.getNodeValue();
            symbol.setUnitsOfMeasurement(this.toUOMUnits(units));
        }
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase(geomString)) {
                    symbol.setGeometryPropertyName(this.parseGeometryName(child));
                } else if (childName.equalsIgnoreCase(graphicSt)) {
                    symbol.setGraphic(this.parseGraphic(child));
                }
            }
            ++i;
        }
        return symbol;
    }

    private Graphic parseGraphic(Node root) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getMessage(this.getClass(), "processing-graphic-{0}", new Object[]{root}));
        }
        Graphic graphic = this.factory.getDefaultGraphic();
        graphic.setMarks(null);
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase(geomString)) {
                    graphic.setGeometryPropertyName(this.parseGeometryName(child));
                } else if (childName.equalsIgnoreCase("ExternalGraphic")) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug((Object)I18N.getMessage(this.getClass(), "parsing-extgraphic-{0}", new Object[]{child}));
                    }
                    graphic.addExternalGraphic(this.parseExternalGraphic(child));
                } else if (childName.equalsIgnoreCase("Mark")) {
                    graphic.addMark(this.parseMark(child));
                } else if (childName.equalsIgnoreCase(opacityString)) {
                    graphic.setOpacity(this.parseCssParameter(child));
                } else if (childName.equalsIgnoreCase("size")) {
                    graphic.setSize(this.parseCssParameter(child));
                } else if (childName.equalsIgnoreCase("displacement")) {
                    graphic.setDisplacement(this.parseDisplacement(child));
                } else if (childName.equalsIgnoreCase("rotation")) {
                    graphic.setRotation(this.parseCssParameter(child));
                }
            }
            ++i;
        }
        return graphic;
    }

    private String parseGeometryName(Node root) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getString(this.getClass(), "parsing-geometryname"));
        }
        String ret = null;
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                ret = this.parseCssParameter(child).toString();
            }
            ++i;
        }
        return ret;
    }

    private Mark parseMark(Node root) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getString(this.getClass(), "parsing-mark"));
        }
        Mark mark = this.factory.createMark();
        mark.setFill(null);
        mark.setStroke(null);
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase("Stroke")) {
                    mark.setStroke(this.parseStroke(child));
                } else if (childName.equalsIgnoreCase(fillSt)) {
                    mark.setFill(this.parseFill(child));
                } else if (childName.equalsIgnoreCase("WellKnownName")) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug((Object)I18N.getMessage(this.getClass(), "setting-mark-to-{0}", new Object[]{child.getFirstChild().getNodeValue()}));
                    }
                    mark.setWellKnownName(this.parseCssParameter(child));
                }
            }
            ++i;
        }
        return mark;
    }

    private ExternalGraphic parseExternalGraphic(Node root) {
        URL url;
        HashMap<String, Object> paramList;
        String uri;
        String format;
        block14: {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug((Object)I18N.getString(this.getClass(), "processing-external-graphic"));
            }
            format = "";
            uri = "";
            paramList = new HashMap<String, Object>();
            NodeList children = root.getChildNodes();
            int length = children.getLength();
            int i = 0;
            while (i < length) {
                Node child = children.item(i);
                if (child != null && child.getNodeType() == 1) {
                    String childName = child.getLocalName();
                    if (childName == null) {
                        childName = child.getNodeName();
                    }
                    if (childName.equalsIgnoreCase("OnLineResource")) {
                        uri = this.parseOnlineResource(child);
                    }
                    if (childName.equalsIgnoreCase("format")) {
                        LOGGER.debug((Object)I18N.getMessage(this.getClass(), "format-child-is", new Object[]{child}));
                        LOGGER.debug((Object)I18N.getMessage(this.getClass(), "seting-extgraph-format-{0}", new Object[]{child.getFirstChild().getNodeValue()}));
                        format = child.getFirstChild().getNodeValue();
                    } else if (childName.equalsIgnoreCase("customProperty")) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug((Object)I18N.getMessage(this.getClass(), "custom-child-is-{0}", new Object[]{child}));
                        }
                        String propName = child.getAttributes().getNamedItem("name").getNodeValue();
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug((Object)I18N.getMessage(this.getClass(), "seting-custom-property-{0}-to-{1}", new Object[]{propName, child.getFirstChild().getNodeValue()}));
                        }
                        Expression value = this.parseCssParameter(child);
                        paramList.put(propName, value);
                    }
                }
                ++i;
            }
            url = null;
            try {
                url = new URL(uri);
            }
            catch (MalformedURLException mfe) {
                LOGGER.debug((Object)I18N.getMessage(this.getClass(), "looks-like-{0}-is-a-relative-path", new Object[]{uri}));
                if (this.sourceUrl == null) break block14;
                try {
                    url = new URL(this.sourceUrl, uri);
                }
                catch (MalformedURLException e) {
                    LOGGER.warn((Object)I18N.getMessage(this.getClass(), "can-not-parse-{0}-as-relative-to-{1}", new Object[]{uri, this.sourceUrl.toExternalForm()}));
                }
            }
        }
        ExternalGraphic extgraph = url == null ? this.factory.createExternalGraphic(uri, format) : this.factory.createExternalGraphic(url, format);
        extgraph.setCustomProperties(paramList);
        return extgraph;
    }

    private String parseOnlineResource(Node root) {
        Element param = (Element)root;
        NamedNodeMap map = param.getAttributes();
        int length = map.getLength();
        LOGGER.debug((Object)(String.valueOf(I18N.getString(this.getClass(), "attributes")) + map.toString()));
        int k = 0;
        while (k < length) {
            String res = map.item(k).getNodeValue();
            String name = map.item(k).getNodeName();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug((Object)I18N.getMessage(this.getClass(), "processing-attribute-{0}-equals-{1}", new Object[]{name, res}));
            }
            if (name.equalsIgnoreCase("xlink:href")) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug((Object)I18N.getMessage(this.getClass(), "seting-extgraph-uri-{0}", new Object[]{res}));
                }
                return res;
            }
            ++k;
        }
        return null;
    }

    private Stroke parseStroke(Node root) {
        Graphic g;
        String childName;
        Node child;
        int i;
        NodeList kids;
        Stroke stroke = this.factory.getDefaultStroke();
        NodeList list = this.findElements((Element)root, "GraphicFill");
        int length = list.getLength();
        if (length > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug((Object)I18N.getMessage(this.getClass(), "stroke-found-a-graphic-fill-{0}", new Object[]{list.item(0)}));
            }
            kids = list.item(0).getChildNodes();
            i = 0;
            while (i < kids.getLength()) {
                child = kids.item(i);
                if (child != null && child.getNodeType() == 1) {
                    childName = child.getLocalName();
                    if (childName == null) {
                        childName = child.getNodeName();
                    }
                    if (childName.equalsIgnoreCase(graphicSt)) {
                        g = this.parseGraphic(child);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug((Object)I18N.getMessage(this.getClass(), "setting-stroke-graphicfill-with-{0}", new Object[]{g}));
                        }
                        stroke.setGraphicFill(g);
                    }
                }
                ++i;
            }
        }
        if ((length = (list = this.findElements((Element)root, "GraphicStroke")).getLength()) > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug((Object)I18N.getMessage(this.getClass(), "stroke-found-a-graphic-stroke-{0}", new Object[]{list.item(0)}));
            }
            kids = list.item(0).getChildNodes();
            i = 0;
            while (i < kids.getLength()) {
                child = kids.item(i);
                if (child != null && child.getNodeType() == 1) {
                    childName = child.getLocalName();
                    if (childName == null) {
                        childName = child.getNodeName();
                    }
                    if (childName.equalsIgnoreCase(graphicSt)) {
                        g = this.parseGraphic(child);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug((Object)I18N.getMessage(this.getClass(), "setting-stroke-graphicstroke-with-{0}", new Object[]{g}));
                        }
                        stroke.setGraphicStroke(g);
                    }
                }
                ++i;
            }
        }
        list = this.findElements((Element)root, "CssParameter");
        length = list.getLength();
        int i2 = 0;
        while (i2 < length) {
            Node child2 = list.item(i2);
            if (child2 != null && child2.getNodeType() == 1) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug((Object)(String.valueOf(I18N.getString(this.getClass(), "now-i-am-processing")) + child2));
                }
                Element param = (Element)child2;
                NamedNodeMap map = param.getAttributes();
                int mapLength = map.getLength();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug((Object)I18N.getMessage(this.getClass(), "attributes-{0}", new Object[]{map.toString()}));
                }
                int k = 0;
                while (k < mapLength) {
                    String res = map.item(k).getNodeValue();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug((Object)I18N.getMessage(this.getClass(), "processing-attribute-{0}", new Object[]{res}));
                    }
                    if (res.equalsIgnoreCase("stroke")) {
                        stroke.setColor(this.parseCssParameter(child2));
                    } else if (res.equalsIgnoreCase("width") || res.equalsIgnoreCase("stroke-width")) {
                        stroke.setWidth(this.parseCssParameter(child2));
                    } else if (res.equalsIgnoreCase(opacityString) || res.equalsIgnoreCase("stroke-opacity")) {
                        stroke.setOpacity(this.parseCssParameter(child2));
                    } else if (res.equalsIgnoreCase("linecap") || res.equalsIgnoreCase("stroke-linecap")) {
                        stroke.setLineCap(this.parseCssParameter(child2));
                    } else if (res.equalsIgnoreCase("linejoin") || res.equalsIgnoreCase("stroke-linejoin")) {
                        stroke.setLineJoin(this.parseCssParameter(child2));
                    } else if (res.equalsIgnoreCase("dasharray") || res.equalsIgnoreCase("stroke-dasharray")) {
                        String dashString = child2.getFirstChild().getNodeValue();
                        StringTokenizer stok = null;
                        stok = dashString.contains(",") ? new StringTokenizer(dashString, ",") : new StringTokenizer(dashString, " ");
                        float[] dashes = new float[stok.countTokens()];
                        int l = 0;
                        while (l < dashes.length) {
                            dashes[l] = Float.parseFloat(stok.nextToken());
                            ++l;
                        }
                        stroke.setDashArray(dashes);
                    } else if (res.equalsIgnoreCase("dashoffset") || res.equalsIgnoreCase("stroke-dashoffset")) {
                        stroke.setDashOffset(this.parseCssParameter(child2));
                    }
                    ++k;
                }
            }
            ++i2;
        }
        return stroke;
    }

    private Fill parseFill(Node root) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getString(this.getClass(), "parsing-fill"));
        }
        Fill fill = this.factory.getDefaultFill();
        NodeList list = this.findElements((Element)root, "GraphicFill");
        int length = list.getLength();
        if (length > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug((Object)I18N.getMessage(this.getClass(), "fill-found-a-graphic-fill-{0}", new Object[]{list.item(0)}));
            }
            NodeList kids = list.item(0).getChildNodes();
            int i = 0;
            while (i < kids.getLength()) {
                Node child = kids.item(i);
                if (child != null && child.getNodeType() == 1) {
                    String childName = child.getLocalName();
                    if (childName == null) {
                        childName = child.getNodeName();
                    }
                    if (childName.equalsIgnoreCase(graphicSt)) {
                        Graphic g = this.parseGraphic(child);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug((Object)I18N.getMessage(this.getClass(), "setting-fill-graphic-with-{0}", new Object[]{g}));
                        }
                        fill.setGraphicFill(g);
                    }
                }
                ++i;
            }
        }
        list = this.findElements((Element)root, "CssParameter");
        length = list.getLength();
        int i = 0;
        while (i < length) {
            Node child = list.item(i);
            if (child != null && child.getNodeType() == 1) {
                Element param = (Element)child;
                NamedNodeMap map = param.getAttributes();
                int mapLength = map.getLength();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug((Object)I18N.getMessage(this.getClass(), "now-i-am-processing-{0}", new Object[]{child}));
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug((Object)I18N.getMessage(this.getClass(), "attributes-{0}", new Object[]{map.toString()}));
                }
                int k = 0;
                while (k < mapLength) {
                    String res = map.item(k).getNodeValue();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug((Object)I18N.getMessage(this.getClass(), "processing-attribute-{0}", new Object[]{res}));
                    }
                    if (res.equalsIgnoreCase(fillSt)) {
                        fill.setColor(this.parseCssParameter(child));
                    } else if (res.equalsIgnoreCase(opacityString) || res.equalsIgnoreCase("fill-opacity")) {
                        fill.setOpacity(this.parseCssParameter(child));
                    }
                    ++k;
                }
            }
            ++i;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)("fill graphic " + fill.getGraphicFill()));
        }
        return fill;
    }

    private Expression manageMixed(Expression left, Expression right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        FunctionExpression mixed = this.ff.createFunctionExpression("strConcat", new Expression[]{left, right});
        return mixed;
    }

    private Expression parseCssParameter(Node root) {
        return this.parseCssParameter(root, true);
    }

    private Expression parseCssParameter(Node root, boolean trimWhiteSpace) {
        Expression ret = null;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)("parsingCssParam " + root));
        }
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null) {
                if (child.getNodeType() == 3) {
                    String value = child.getNodeValue();
                    String string = value = value != null && trimWhiteSpace ? value.trim() : value;
                    if (value != null && value.length() != 0) {
                        Element literal = this.dom.createElement("literal");
                        Text text = this.dom.createTextNode(value);
                        literal.appendChild(text);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug((Object)I18N.getMessage(this.getClass(), "built-new-literal-{0}", new Object[]{literal}));
                        }
                        ret = this.manageMixed(ret, ExpressionDOMParser.parseExpression(literal, this.schema));
                    }
                } else if (child.getNodeType() == 1) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug((Object)I18N.getMessage(this.getClass(), "about-to-parse-{0}", new Object[]{child.getLocalName()}));
                    }
                    ret = this.manageMixed(ret, ExpressionDOMParser.parseExpression(child, this.schema));
                }
            }
            ++i;
        }
        if (ret == null && LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getString(this.getClass(), "no-children-in-cssparam"));
        }
        return ret;
    }

    private Font parseFont(Node root, TextSymbolizer textSymbol) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getString(this.getClass(), "parsing-font"));
        }
        Font font = this.factory.getDefaultFont();
        NodeList list = this.findElements((Element)root, "CssParameter");
        int length = list.getLength();
        int i = 0;
        while (i < length) {
            Node child = list.item(i);
            if (child != null && child.getNodeType() == 1) {
                Element param = (Element)child;
                NamedNodeMap map = param.getAttributes();
                int mapLength = map.getLength();
                int k = 0;
                while (k < mapLength) {
                    String res = map.item(k).getNodeValue();
                    if (res.equalsIgnoreCase("font-family")) {
                        font.setFontFamily(this.parseCssParameter(child));
                    } else if (res.equalsIgnoreCase("font-style")) {
                        font.setFontStyle(this.parseCssParameter(child));
                    } else if (res.equalsIgnoreCase("font-size")) {
                        font.setFontSize(this.parseCssParameter(child));
                    } else if (res.equalsIgnoreCase("font-weight")) {
                        font.setFontWeight(this.parseCssParameter(child));
                    } else if (res.equalsIgnoreCase("font-color")) {
                        Expression fontColor = this.parseCssParameter(child);
                        Fill fill = this.factory.getDefaultFill();
                        fill.setColor(fontColor);
                        textSymbol.setFill(fill);
                    }
                    ++k;
                }
            }
            ++i;
        }
        return font;
    }

    private LabelPlacement parseLabelPlacement(Node root) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getString(this.getClass(), "parsing-labelplacement"));
        }
        LabelPlacement ret = null;
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase("PointPlacement")) {
                    ret = this.parsePointPlacement(child);
                } else if (childName.equalsIgnoreCase("LinePlacement")) {
                    ret = this.parseLinePlacement(child);
                }
            }
            ++i;
        }
        return ret;
    }

    private PointPlacement parsePointPlacement(Node root) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getString(this.getClass(), "parsing-pointPlacement"));
        }
        Expression rotation = this.ff.createLiteralExpression(0.0);
        AnchorPoint ap = null;
        Displacement dp = null;
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase("AnchorPoint")) {
                    ap = this.parseAnchorPoint(child);
                } else if (childName.equalsIgnoreCase("Displacement")) {
                    dp = this.parseDisplacement(child);
                } else if (childName.equalsIgnoreCase("Rotation")) {
                    rotation = this.parseCssParameter(child);
                }
            }
            ++i;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getMessage(this.getClass(), "setting-anchorpoint-{0}", new Object[]{ap}));
            LOGGER.debug((Object)I18N.getMessage(this.getClass(), "setting-displacement-{0}", new Object[]{dp}));
        }
        PointPlacement dpp = this.factory.createPointPlacement(ap, dp, rotation);
        return dpp;
    }

    private LinePlacement parseLinePlacement(Node root) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getString(this.getClass(), "parsing-lineplacement"));
        }
        Expression offset = this.ff.createLiteralExpression(0.0);
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase("PerpendicularOffset")) {
                    offset = this.parseCssParameter(child);
                }
            }
            ++i;
        }
        LinePlacement dlp = this.factory.createLinePlacement(offset);
        return dlp;
    }

    private AnchorPoint parseAnchorPoint(Node root) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getString(this.getClass(), "parsing-anchorpoint"));
        }
        Expression x = null;
        Expression y = null;
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase("AnchorPointX")) {
                    x = this.parseCssParameter(child);
                } else if (childName.equalsIgnoreCase("AnchorPointY")) {
                    y = this.parseCssParameter(child);
                }
            }
            ++i;
        }
        AnchorPoint dap = this.factory.createAnchorPoint(x, y);
        return dap;
    }

    private Displacement parseDisplacement(Node root) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getString(this.getClass(), "parsing-displacment"));
        }
        Expression x = null;
        Expression y = null;
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase("DisplacementX")) {
                    x = this.parseCssParameter(child);
                }
                if (childName.equalsIgnoreCase("DisplacementY")) {
                    y = this.parseCssParameter(child);
                }
            }
            ++i;
        }
        Displacement dd = this.factory.createDisplacement(x, y);
        return dd;
    }

    private Halo parseHalo(Node root) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)I18N.getString(this.getClass(), "parsing-halo"));
        }
        Halo halo = this.factory.createHalo(this.factory.createFill(this.ff.createLiteralExpression("#FFFFFF")), this.ff.createLiteralExpression(1.0));
        NodeList children = root.getChildNodes();
        int length = children.getLength();
        int i = 0;
        while (i < length) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == 1) {
                String childName = child.getLocalName();
                if (childName == null) {
                    childName = child.getNodeName();
                }
                if (childName.equalsIgnoreCase(fillSt)) {
                    halo.setFill(this.parseFill(child));
                } else if (childName.equalsIgnoreCase("Radius")) {
                    halo.setRadius(this.parseCssParameter(child));
                }
            }
            ++i;
        }
        return halo;
    }

    public static void main(String[] args) {
        SLDParser parser = new SLDParser((StyleFactory)new StyleFactoryImpl(), null);
        try {
            parser.setInput(new File("D:/temp/Giuseppe Aruta/2008_05_18/SLD files/OpenJUMP/OpenJUMP_1.sld"));
            Style[] styles = parser.readXML();
            LOGGER.info((Object)("Se han leido " + styles.length + " estilos desde el fichero"));
        }
        catch (FileNotFoundException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }
}

