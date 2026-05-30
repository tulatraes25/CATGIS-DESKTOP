/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.datatypes.UnknownTypeException
 *  org.deegree.framework.log.ILogger
 *  org.deegree.framework.log.LoggerFactory
 *  org.deegree.framework.util.CharsetUtils
 *  org.deegree.framework.util.TimeTools
 *  org.deegree.framework.xml.DOMPrinter
 *  org.deegree.framework.xml.ElementList
 *  org.deegree.framework.xml.XMLParsingException
 *  org.deegree.model.crs.UnknownCRSException
 *  org.deegree.model.feature.FeatureFactory
 *  org.deegree.model.feature.FeatureProperty
 *  org.deegree.model.feature.Messages
 *  org.deegree.model.feature.XLinkedFeatureProperty
 *  org.deegree.model.feature.schema.FeaturePropertyType
 *  org.deegree.model.feature.schema.FeatureType
 *  org.deegree.model.feature.schema.GMLSchemaDocument
 *  org.deegree.model.feature.schema.GeometryPropertyType
 *  org.deegree.model.feature.schema.MultiGeometryPropertyType
 *  org.deegree.model.feature.schema.PropertyType
 *  org.deegree.model.feature.schema.SimplePropertyType
 *  org.deegree.model.spatialschema.Geometry
 *  org.deegree.model.spatialschema.GeometryException
 *  org.deegree.ogcbase.CommonNamespaces
 *  org.deegree.ogcbase.GMLDocument
 */
package org.deegree.model.feature;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.Messages;
import org.deegree.model.feature.XLinkedFeatureProperty;
import org.deegree.model.feature.schema.FeaturePropertyType;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GMLSchema;
import org.deegree.model.feature.schema.GMLSchemaDocument;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.model.feature.schema.MultiGeometryPropertyType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.feature.schema.SimplePropertyType;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.GMLDocument;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class GMLFeatureDocument
extends GMLDocument {
    private static final long serialVersionUID = -7626943858143104276L;
    private static final ILogger LOG = LoggerFactory.getLogger(GMLFeatureDocument.class);
    private static String FID = "fid";
    private static String GMLID = "id";
    private static URI GMLNS = CommonNamespaces.GMLNS;
    private static String GMLID_NS = CommonNamespaces.GMLNS.toString();
    private static QualifiedName PROP_NAME_BOUNDED_BY = new QualifiedName("boundedBy", GMLNS);
    private static QualifiedName PROP_NAME_DESCRIPTION = new QualifiedName("description", GMLNS);
    private static QualifiedName PROP_NAME_NAME = new QualifiedName("name", GMLNS);
    private static QualifiedName PROP_NAME_WKB_GEOM = new QualifiedName("wkbGeom", GMLNS);
    private static QualifiedName TYPE_NAME_BOX = new QualifiedName("Box", GMLNS);
    private static QualifiedName TYPE_NAME_LINESTRING = new QualifiedName("LineString", GMLNS);
    private static QualifiedName TYPE_NAME_MULTIGEOMETRY = new QualifiedName("MultiGeometry", GMLNS);
    private static QualifiedName TYPE_NAME_MULTILINESTRING = new QualifiedName("MultiLineString", GMLNS);
    private static QualifiedName TYPE_NAME_MULTIPOINT = new QualifiedName("MultiPoint", GMLNS);
    private static QualifiedName TYPE_NAME_MULTIPOLYGON = new QualifiedName("MultiPolygon", GMLNS);
    private static QualifiedName TYPE_NAME_POINT = new QualifiedName("Point", GMLNS);
    private static QualifiedName TYPE_NAME_POLYGON = new QualifiedName("Polygon", GMLNS);
    private static QualifiedName TYPE_NAME_SURFACE = new QualifiedName("Surface", GMLNS);
    private static QualifiedName TYPE_NAME_CURVE = new QualifiedName("Curve", GMLNS);
    private static QualifiedName TYPE_NAME_MULTISURFACE = new QualifiedName("MultiSurface", GMLNS);
    private static QualifiedName TYPE_NAME_MULTICURVE = new QualifiedName("MultiCurve", GMLNS);
    protected Map<URI, GMLSchema> gmlSchemaMap;
    protected Map<String, Feature> featureMap = new HashMap<String, Feature>();
    protected Collection<XLinkedFeatureProperty> xlinkPropertyList = new ArrayList<XLinkedFeatureProperty>();
    private boolean guessSimpleTypes = false;

    public GMLFeatureDocument() {
    }

    public GMLFeatureDocument(boolean guessSimpleTypes) {
        this.guessSimpleTypes = guessSimpleTypes;
    }

    public void setSchemas(Map<URI, GMLSchema> gmlSchemaMap) {
        this.gmlSchemaMap = gmlSchemaMap;
    }

    public Feature parseFeature() throws XMLParsingException, UnknownCRSException {
        return this.parseFeature((String)null);
    }

    public Feature parseFeature(String defaultSRS) throws XMLParsingException, UnknownCRSException {
        Feature feature = this.parseFeature(this.getRootElement(), defaultSRS);
        this.resolveXLinkReferences();
        return feature;
    }

    protected Feature parseFeature(Element element) throws XMLParsingException, UnknownCRSException {
        return this.parseFeature(element, null);
    }

    protected Feature parseFeature(Element element, String srsName) throws XMLParsingException, UnknownCRSException {
        Feature feature = null;
        String fid = this.parseFeatureId(element);
        FeatureType ft = this.getFeatureType(element);
        srsName = XMLTools.getNodeAsString(element, "gml:boundedBy/*[1]/@srsName", nsContext, srsName);
        ElementList childList = XMLTools.getChildElements(element);
        ArrayList<FeatureProperty> propertyList = new ArrayList<FeatureProperty>(childList.getLength());
        int i = 0;
        while (i < childList.getLength()) {
            Element propertyElement = childList.item(i);
            QualifiedName propertyName = this.getQualifiedName(propertyElement);
            if (!PROP_NAME_BOUNDED_BY.equals((Object)propertyName) && !PROP_NAME_WKB_GEOM.equals((Object)propertyName)) {
                if (PROP_NAME_NAME.equals((Object)propertyName) || PROP_NAME_DESCRIPTION.equals((Object)propertyName)) {
                    FeatureProperty property;
                    String s = XMLTools.getStringValue(propertyElement);
                    if (s != null) {
                        s = s.trim();
                    }
                    if ((property = this.createSimpleProperty(s, propertyName, 12)) != null) {
                        propertyList.add(property);
                    }
                } else {
                    try {
                        FeatureProperty property = this.parseProperty(childList.item(i), ft, srsName);
                        if (property != null) {
                            propertyList.add(property);
                        }
                    }
                    catch (XMLParsingException xmle) {
                        LOG.logInfo("An error occurred while trying to parse feature with fid: " + fid);
                        throw xmle;
                    }
                }
            }
            ++i;
        }
        FeatureProperty[] featureProperties = propertyList.toArray(new FeatureProperty[propertyList.size()]);
        feature = FeatureFactory.createFeature((String)fid, (FeatureType)ft, (FeatureProperty[])featureProperties);
        if (!"".equals(fid)) {
            if (this.featureMap.containsKey(fid)) {
                String msg = Messages.format((String)"ERROR_FEATURE_ID_NOT_UNIQUE", (Object[])new Object[]{fid});
                throw new XMLParsingException(msg);
            }
            this.featureMap.put(fid, feature);
        }
        return feature;
    }

    public FeatureProperty parseProperty(Element propertyElement, FeatureType ft) throws XMLParsingException, UnknownCRSException {
        return this.parseProperty(propertyElement, ft, null);
    }

    public FeatureProperty parseProperty(Element propertyElement, FeatureType ft, String srsName) throws XMLParsingException, UnknownCRSException {
        FeatureProperty property = null;
        QualifiedName propertyName = this.getQualifiedName(propertyElement);
        PropertyType propertyType = ft.getProperty(propertyName);
        if (propertyType == null) {
            throw new XMLParsingException(Messages.format((String)"ERROR_NO_PROPERTY_TYPE", (Object[])new Object[]{propertyName}));
        }
        if (propertyType instanceof SimplePropertyType) {
            int typeCode = propertyType.getType();
            String s = null;
            if (typeCode == 11019) {
                Element child = XMLTools.getRequiredElement(propertyElement, "*", nsContext);
                s = DOMPrinter.nodeToString((Node)child, (String)CharsetUtils.getSystemCharset());
            } else {
                s = XMLTools.getStringValue(propertyElement).trim();
            }
            property = this.createSimpleProperty(s, propertyName, typeCode);
        } else if (propertyType instanceof GeometryPropertyType) {
            Element contentElement = XMLTools.getFirstChildElement(propertyElement);
            if (contentElement == null) {
                String msg = Messages.format((String)"ERROR_PROPERTY_NO_CHILD", (Object[])new Object[]{propertyName, "geometry"});
                throw new XMLParsingException(msg);
            }
            property = this.createGeometryProperty(contentElement, propertyName, srsName);
        } else {
            if (propertyType instanceof MultiGeometryPropertyType) {
                throw new XMLParsingException("Handling of MultiGeometryPropertyType not implemented in GMLFeatureDocument yet.");
            }
            if (propertyType instanceof FeaturePropertyType) {
                List<Node> childElements = XMLTools.getNodes(propertyElement, "*", nsContext);
                switch (childElements.size()) {
                    case 0: {
                        Text xlinkHref = (Text)XMLTools.getNode(propertyElement, "@xlink:href/text()", nsContext);
                        if (xlinkHref == null) {
                            String msg = Messages.format((String)"ERROR_INVALID_FEATURE_PROPERTY", (Object[])new Object[]{propertyName});
                            throw new XMLParsingException(msg);
                        }
                        String href = xlinkHref.getData();
                        if (!href.startsWith("#")) {
                            String msg = Messages.format((String)"ERROR_EXTERNAL_XLINK_NOT_SUPPORTED", (Object[])new Object[]{href});
                            throw new XMLParsingException(msg);
                        }
                        String fid = href.substring(1);
                        property = new XLinkedFeatureProperty(propertyName, fid);
                        this.xlinkPropertyList.add((XLinkedFeatureProperty)property);
                        break;
                    }
                    case 1: {
                        Feature propertyValue = this.parseFeature((Element)childElements.get(0), srsName);
                        property = FeatureFactory.createFeatureProperty((QualifiedName)propertyName, (Object)propertyValue);
                        break;
                    }
                    default: {
                        String string = Messages.format((String)"ERROR_INVALID_FEATURE_PROPERTY2", (Object[])new Object[]{propertyName, childElements.size()});
                    }
                }
            }
        }
        return property;
    }

    protected void resolveXLinkReferences() throws XMLParsingException {
        for (XLinkedFeatureProperty xlinkProperty : this.xlinkPropertyList) {
            String fid = xlinkProperty.getTargetFeatureId();
            Feature targetFeature = this.featureMap.get(fid);
            if (targetFeature == null) {
                String msg = Messages.format((String)"ERROR_XLINK_NOT_RESOLVABLE", (Object[])new Object[]{fid});
                throw new XMLParsingException(msg);
            }
            xlinkProperty.setValue((Object)targetFeature);
        }
    }

    private FeatureProperty createSimpleProperty(String s, QualifiedName propertyName, int typeCode) throws XMLParsingException {
        Object propertyValue = null;
        switch (typeCode) {
            case 12: 
            case 11019: {
                propertyValue = s;
                break;
            }
            case 4: 
            case 5: {
                try {
                    propertyValue = new Integer(s);
                    break;
                }
                catch (NumberFormatException e) {
                    String msg = Messages.format((String)"ERROR_CONVERTING_PROPERTY", (Object[])new Object[]{s, propertyName, "-", "Integer"});
                    throw new XMLParsingException(msg);
                }
            }
            case 2: 
            case 8: {
                try {
                    propertyValue = new Double(s);
                    break;
                }
                catch (NumberFormatException e) {
                    String msg = Messages.format((String)"ERROR_CONVERTING_PROPERTY", (Object[])new Object[]{s, propertyName, "-", "Double"});
                    throw new XMLParsingException(msg);
                }
            }
            case 3: 
            case 6: {
                try {
                    propertyValue = new Float(s);
                    break;
                }
                catch (NumberFormatException e) {
                    String msg = Messages.format((String)"ERROR_CONVERTING_PROPERTY", (Object[])new Object[]{s, propertyName, "-", "Float"});
                    throw new XMLParsingException(msg);
                }
            }
            case 16: {
                propertyValue = new Boolean(s);
                break;
            }
            case 91: 
            case 93: {
                try {
                    propertyValue = TimeTools.createCalendar((String)s).getTime();
                    break;
                }
                catch (NumberFormatException e) {
                    String msg = Messages.format((String)"ERROR_CONVERTING_PROPERTY", (Object[])new Object[]{s, propertyName, "-", "Date/Timestamp"});
                    throw new XMLParsingException(msg);
                }
            }
            default: {
                String typeString = "" + typeCode;
                try {
                    typeString = Types.getTypeNameForSQLTypeCode(typeCode);
                }
                catch (UnknownTypeException e) {
                    LOG.logError("No type name for code: " + typeCode);
                }
                String msg = Messages.format((String)"ERROR_UNHANDLED_TYPE", (Object[])new Object[]{typeString});
                LOG.logError(msg);
                throw new XMLParsingException(msg);
            }
        }
        FeatureProperty property = FeatureFactory.createFeatureProperty((QualifiedName)propertyName, (Object)propertyValue);
        return property;
    }

    private FeatureProperty createGeometryProperty(Element contentElement, QualifiedName propertyName, String srsName) throws XMLParsingException {
        Geometry propertyValue = null;
        try {
            propertyValue = GMLGeometryAdapter.wrap(contentElement, srsName);
        }
        catch (GeometryException e) {
            LOG.logError(e.getMessage(), (Throwable)e);
            String msg = Messages.format((String)"ERROR_CONVERTING_GEOMETRY_PROPERTY", (Object[])new Object[]{propertyName, "-", e.getMessage()});
            throw new XMLParsingException(msg);
        }
        FeatureProperty property = FeatureFactory.createFeatureProperty((QualifiedName)propertyName, (Object)propertyValue);
        return property;
    }

    public Map<URI, GMLSchema> getGMLSchemas() throws XMLParsingException, UnknownCRSException {
        if (this.gmlSchemaMap == null) {
            this.gmlSchemaMap = new HashMap<URI, GMLSchema>();
            Map schemaMap = this.getAttachedSchemas();
            for (URI nsURI : schemaMap.keySet()) {
                URL schemaURL = (URL)schemaMap.get(nsURI);
                GMLSchemaDocument schemaDocument = new GMLSchemaDocument();
                LOG.logDebug("Retrieving schema document for namespace '" + nsURI + "' from URL '" + schemaURL + "'.");
                try {
                    schemaDocument.load(schemaURL);
                    GMLSchema gmlSchema = schemaDocument.parseGMLSchema();
                    this.gmlSchemaMap.put(nsURI, gmlSchema);
                }
                catch (IOException e) {
                    String msg = Messages.format((String)"ERROR_RETRIEVING_SCHEMA", (Object[])new Object[]{schemaURL, e.getMessage()});
                    throw new XMLParsingException(msg);
                }
                catch (SAXException e) {
                    String msg = Messages.format((String)"ERROR_SCHEMA_NOT_XML", (Object[])new Object[]{schemaURL, e.getMessage()});
                    throw new XMLParsingException(msg);
                }
                catch (XMLParsingException e) {
                    String msg = Messages.format((String)"ERROR_SCHEMA_PARSING1", (Object[])new Object[]{schemaURL, e.getMessage()});
                    throw new XMLParsingException(msg);
                }
            }
        }
        return this.gmlSchemaMap;
    }

    protected GMLSchema getSchemaForNamespace(URI ns) throws XMLParsingException, UnknownCRSException {
        Map<URI, GMLSchema> gmlSchemaMap = this.getGMLSchemas();
        GMLSchema schema = gmlSchemaMap.get(ns);
        return schema;
    }

    protected FeatureType getFeatureType(QualifiedName ftName) throws XMLParsingException, UnknownCRSException {
        FeatureType featureType = null;
        if (this.gmlSchemaMap != null) {
            GMLSchema schema = this.getSchemaForNamespace(ftName.getNamespace());
            if (schema == null) {
                String msg = Messages.format((String)"ERROR_SCHEMA_NO_SCHEMA_FOR_NS", (Object[])new Object[]{ftName.getNamespace()});
                throw new XMLParsingException(msg);
            }
            featureType = schema.getFeatureType(ftName);
            if (featureType == null) {
                String msg = Messages.format((String)"ERROR_SCHEMA_FEATURE_TYPE_UNKNOWN", (Object[])new Object[]{ftName});
                throw new XMLParsingException(msg);
            }
        }
        return featureType;
    }

    protected String parseFeatureId(Element featureElement) {
        String fid = featureElement.getAttributeNS(GMLID_NS, GMLID);
        if (fid.length() == 0) {
            fid = featureElement.getAttribute(FID);
        }
        return fid;
    }

    private FeatureType getFeatureType(Element element) throws XMLParsingException, UnknownCRSException {
        QualifiedName ftName = this.getQualifiedName(element);
        FeatureType featureType = this.getFeatureType(ftName);
        if (featureType == null) {
            LOG.logDebug("Feature type '" + ftName + "' is not defined in schema. Generating feature type dynamically.");
            featureType = this.generateFeatureType(element);
        }
        return featureType;
    }

    private FeatureType generateFeatureType(Element element) throws XMLParsingException {
        ElementList el = XMLTools.getChildElements(element);
        ArrayList<PropertyType> propertyList = new ArrayList<PropertyType>(el.getLength());
        int i = 0;
        while (i < el.getLength()) {
            PropertyType propertyType;
            Element propertyElement = el.item(i);
            QualifiedName propertyName = this.getQualifiedName(propertyElement);
            if (!(propertyName.equals((Object)PROP_NAME_BOUNDED_BY) || propertyName.equals((Object)PROP_NAME_NAME) || propertyName.equals((Object)PROP_NAME_DESCRIPTION) || propertyList.contains(propertyType = this.determinePropertyType(propertyElement, propertyName)))) {
                propertyList.add(propertyType);
            }
            ++i;
        }
        PropertyType[] properties = new PropertyType[propertyList.size()];
        properties = propertyList.toArray(properties);
        QualifiedName ftName = this.getQualifiedName(element);
        FeatureType featureType = FeatureFactory.createFeatureType((QualifiedName)ftName, (boolean)false, (PropertyType[])properties);
        return featureType;
    }

    private PropertyType determinePropertyType(Element propertyElement, QualifiedName propertyName) throws XMLParsingException {
        Object pt = null;
        ElementList childList = XMLTools.getChildElements(propertyElement);
        Attr xlink = (Attr)XMLTools.getNode(propertyElement, "@xlink:href", nsContext);
        String skipParsing = XMLTools.getNodeAsString(propertyElement, "@deegreewfs:skipParsing", nsContext, "false");
        if ("true".equals(skipParsing)) {
            pt = FeatureFactory.createSimplePropertyType((QualifiedName)propertyName, (int)11019, (int)0, (int)-1);
            return pt;
        }
        if (childList.getLength() == 0 && xlink == null) {
            String value = XMLTools.getStringValue(propertyElement);
            if (value != null) {
                value = value.trim();
            }
            pt = this.guessSimplePropertyType(value, propertyName);
        } else {
            QualifiedName elementName;
            pt = xlink != null ? FeatureFactory.createFeaturePropertyType((QualifiedName)propertyName, (int)0, (int)-1) : (this.isGeometry(elementName = this.getQualifiedName(childList.item(0))) ? FeatureFactory.createGeometryPropertyType((QualifiedName)propertyName, (QualifiedName)elementName, (int)0, (int)-1) : FeatureFactory.createFeaturePropertyType((QualifiedName)propertyName, (int)0, (int)-1));
        }
        return pt;
    }

    private SimplePropertyType guessSimplePropertyType(String value, QualifiedName propertyName) {
        int typeCode = 12;
        if (this.guessSimpleTypes) {
            try {
                Integer.parseInt(value);
                typeCode = 4;
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
            if (typeCode == 12) {
                try {
                    Double.parseDouble(value);
                    typeCode = 2;
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
        }
        SimplePropertyType propertyType = FeatureFactory.createSimplePropertyType((QualifiedName)propertyName, (int)typeCode, (int)0, (int)-1);
        return propertyType;
    }

    private boolean isGeometry(QualifiedName elementName) {
        boolean isGeometry = false;
        if (TYPE_NAME_BOX.equals((Object)elementName) || TYPE_NAME_LINESTRING.equals((Object)elementName) || TYPE_NAME_MULTIGEOMETRY.equals((Object)elementName) || TYPE_NAME_MULTILINESTRING.equals((Object)elementName) || TYPE_NAME_MULTIPOINT.equals((Object)elementName) || TYPE_NAME_MULTIPOLYGON.equals((Object)elementName) || TYPE_NAME_POINT.equals((Object)elementName) || TYPE_NAME_POLYGON.equals((Object)elementName) || TYPE_NAME_SURFACE.equals((Object)elementName) || TYPE_NAME_MULTISURFACE.equals((Object)elementName) || TYPE_NAME_CURVE.equals((Object)elementName) || TYPE_NAME_MULTICURVE.equals((Object)elementName)) {
            isGeometry = true;
        }
        return isGeometry;
    }
}

