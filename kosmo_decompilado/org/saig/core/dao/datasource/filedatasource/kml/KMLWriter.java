/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.commons.lang.StringUtils
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 *  org.dom4j.Document
 *  org.dom4j.DocumentHelper
 *  org.dom4j.Element
 *  org.dom4j.Namespace
 *  org.dom4j.io.OutputFormat
 *  org.dom4j.io.XMLWriter
 *  org.dom4j.tree.DefaultElement
 */
package org.saig.core.dao.datasource.filedatasource.kml;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import es.kosmo.core.crs.CrsRepositoryManager;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.gvsig.crs.ICrs;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Fill;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.Style;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.lang.I18N;

public class KMLWriter {
    public static final String KML_NAMESPACE = "http://earth.google.com/kml/2.2";
    public static final String OGC_KML_NAMESPACE = "http://www.opengis.net/kml/2.2";
    private static final String BASE_FEATURE_NAME = I18N.getString("org.saig.core.dao.datasource.filedatasource.kml.KMLWriter.Feature");
    private String filePath;
    private Style style;
    private IProjection dataProj;
    private ICoordTrans ct;
    private GeometryFactory geomFac = new GeometryFactory();
    private Map<String, Filter> ruleNameFilterMap;
    private Map<String, Expression> ruleNameLabelExpressionMap;
    private String otherRuleName;
    private static final String DOCUMENT_ELEMENT_NAME = "Document";
    private static final String SCHEMA_ELEMENT_NAME = "Schema";
    private static final String PLACEMARK_ELEMENT_NAME = "Placemark";
    private static final String EXTENDED_DATA_ELEMENT_NAME = "ExtendedData";
    private static final String SCHEMA_DATA_ELEMENT_NAME = "SchemaData";
    private static final String SIMPLE_DATA_ELEMENT_NAME = "SimpleData";
    private static final String POINT_ELEMENT_NAME = "Point";
    private static final String LINESTRING_ELEMENT_NAME = "LineString";
    private static final String LINEARRING_ELEMENT_NAME = "LinearRing";
    private static final String POLYGON_ELEMENT_NAME = "Polygon";
    private static final String MULTIGEOMETRY_ELEMENT_NAME = "MultiGeometry";
    private static final String OUTER_BOUNDARY_ELEMENT_NAME = "outerBoundaryIs";
    private static final String INNER_BOUNDARY_ELEMENT_NAME = "innerBoundaryIs";
    private static final String COORDINATES_ELEMENT_NAME = "coordinates";
    private static final String STYLE_ELEMENT_NAME = "Style";
    private static final String STYLEMAP_ELEMENT_NAME = "StyleMap";
    private static final String PAIR_ELEMENT_NAME = "Pair";
    private static final String LABEL_STYLE_ELEMENT_NAME = "LabelStyle";
    private static final String LINE_STYLE_ELEMENT_NAME = "LineStyle";
    private static final String POLYGON_STYLE_ELEMENT_NAME = "PolyStyle";
    private static final String COLOR_ELEMENT_NAME = "color";
    private static final String COLORMODE_ELEMENT_NAME = "colorMode";
    private static final String FILL_ELEMENT_NAME = "fill";
    private static final String OUTLINE_ELEMENT_NAME = "outline";
    private static final String WIDTH_ELEMENT_NAME = "width";
    private static final String DEFAULT_FILL_COLOR = "ff0000cc";
    private static final String DEFAULT_SELECTION_COLOR = "ff00ffff";
    private static final String KML_PROJECTION_CODE = "EPSG:4326";

    public KMLWriter(String path, Style currentStyle, IProjection proj) {
        this.filePath = path;
        this.style = currentStyle;
        this.dataProj = proj;
        this.ruleNameFilterMap = new HashMap<String, Filter>();
        this.ruleNameLabelExpressionMap = new HashMap<String, Expression>();
    }

    public void write(FeatureCollection fc) throws Exception {
        this.write(fc, Long.MAX_VALUE);
    }

    public void write(FeatureCollection fc, long limit) throws Exception {
        ICrs targetProj = CrsRepositoryManager.getInstance().getCRS(KML_PROJECTION_CODE);
        this.ct = this.dataProj.getCT((IProjection)targetProj);
        Document doc = DocumentHelper.createDocument();
        Element root = this.writeRoot(doc);
        this.writeDocument(fc, root, limit);
        FileWriter fw = null;
        try {
            fw = new FileWriter(this.filePath);
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter xw = new XMLWriter((Writer)fw, format);
            xw.write(doc);
            xw.close();
        }
        finally {
            if (fw != null) {
                fw.close();
            }
        }
    }

    private Element writeRoot(Document doc) {
        DefaultElement kmlRoot = new DefaultElement("kml", new Namespace("", OGC_KML_NAMESPACE));
        doc.add((Element)kmlRoot);
        return kmlRoot;
    }

    private void writeDocument(FeatureCollection fc, Element root, long limit) throws Exception {
        Element docElement = root.addElement(DOCUMENT_ELEMENT_NAME);
        String fcName = "";
        if (StringUtils.isNotEmpty((String)fc.getName())) {
            fcName = fc.getName();
        }
        docElement.addElement("name").addText(fcName);
        docElement.addElement("open").addText("1");
        if (this.style != null) {
            this.writeStyles(docElement);
        }
        String schemaID = this.writeSchema(docElement, fc.getFeatureSchema());
        FeatureIterator fcIter = null;
        try {
            fcIter = fc.iterator();
            this.writeFeatures(docElement, fcIter, schemaID, limit);
        }
        finally {
            if (fcIter != null) {
                fcIter.close();
            }
        }
    }

    private void writeStyles(Element docElement) {
        FeatureTypeStyle featStyle = this.style.getSelectedFeatureTypeStyle();
        Rule[] ruleArray = featStyle.getRules();
        int n = ruleArray.length;
        int n2 = 0;
        while (n2 < n) {
            Rule currentRule = ruleArray[n2];
            String ruleName = currentRule.getName();
            Filter filter = currentRule.getFilter();
            if (filter == null) {
                filter = Filter.NONE;
            }
            Element styleElement = docElement.addElement(STYLE_ELEMENT_NAME);
            styleElement.addAttribute("id", ruleName);
            this.writeRule(styleElement, currentRule);
            if (!currentRule.isElseFilter()) {
                this.ruleNameFilterMap.put(ruleName, filter);
            } else {
                this.otherRuleName = ruleName;
            }
            ++n2;
        }
    }

    private void writeHighlightStyle(Element highlightStyleElement) {
    }

    private void writeRule(Element styleElement, Rule rule) {
        Symbolizer[] symbolizers = rule.getSymbolizers();
        int i = 0;
        while (i < symbolizers.length) {
            Symbolizer currentSymbol = symbolizers[i];
            if (currentSymbol instanceof PointSymbolizer) {
                PointSymbolizer pointSymbolizer = (PointSymbolizer)currentSymbol;
                this.writePointStyle(styleElement, pointSymbolizer);
            } else if (currentSymbol instanceof LineSymbolizer) {
                LineSymbolizer lineSymbolizer = (LineSymbolizer)currentSymbol;
                this.writeLineStyle(styleElement, lineSymbolizer);
            } else if (currentSymbol instanceof PolygonSymbolizer) {
                PolygonSymbolizer polygonSymbolizer = (PolygonSymbolizer)currentSymbol;
                this.writePolygonStyle(styleElement, polygonSymbolizer);
            } else if (currentSymbol instanceof TextSymbolizer) {
                TextSymbolizer textSymbolizer = (TextSymbolizer)currentSymbol;
                this.writeLabelStyle(styleElement, textSymbolizer);
                this.ruleNameLabelExpressionMap.put(rule.getName(), textSymbolizer.getLabel());
            }
            ++i;
        }
    }

    private void writePointStyle(Element styleElement, PointSymbolizer pointSymbolizer) {
    }

    private void writeLineStyle(Element styleElement, LineSymbolizer lineSymbolizer) {
        Element lineElement = styleElement.addElement(LINE_STYLE_ELEMENT_NAME);
        Stroke stroke = lineSymbolizer.getStroke();
        String color = this.getColorFromExpression(stroke.getColor(), stroke.getOpacity());
        lineElement.addElement(COLOR_ELEMENT_NAME).addText(color);
        lineElement.addElement(COLORMODE_ELEMENT_NAME).addText("normal");
        int width = ((Number)stroke.getWidth().getValue(null)).intValue();
        lineElement.addElement(WIDTH_ELEMENT_NAME).addText("" + width);
    }

    private String getColorFromExpression(Expression color, Expression opacity) {
        String colorHexString = (String)color.getValue(null);
        Color colorValue = Color.decode(colorHexString);
        double opacityValue = ((Number)opacity.getValue(null)).doubleValue();
        StringBuffer kmlColor = new StringBuffer();
        String opacityHex = Integer.toHexString((int)(opacityValue * 255.0));
        if (opacityHex.length() == 1) {
            opacityHex = "0" + opacityHex;
        }
        kmlColor.append(opacityHex);
        String blueHex = Integer.toHexString(colorValue.getBlue());
        if (blueHex.length() == 1) {
            blueHex = "0" + blueHex;
        }
        kmlColor.append(blueHex);
        String greenHex = Integer.toHexString(colorValue.getGreen());
        if (greenHex.length() == 1) {
            greenHex = "0" + greenHex;
        }
        kmlColor.append(greenHex);
        String redHex = Integer.toHexString(colorValue.getRed());
        if (redHex.length() == 1) {
            redHex = "0" + redHex;
        }
        kmlColor.append(redHex);
        return kmlColor.toString();
    }

    private void writePolygonStyle(Element styleElement, PolygonSymbolizer polygonSymbolizer) {
        boolean hasOutline;
        boolean hasFill = polygonSymbolizer.getFill() != null;
        boolean bl = hasOutline = polygonSymbolizer.getStroke() != null;
        if (hasOutline) {
            Element lineElement = styleElement.addElement(LINE_STYLE_ELEMENT_NAME);
            Stroke stroke = polygonSymbolizer.getStroke();
            String color = this.getColorFromExpression(stroke.getColor(), stroke.getOpacity());
            lineElement.addElement(COLOR_ELEMENT_NAME).addText(color);
            lineElement.addElement(COLORMODE_ELEMENT_NAME).addText("normal");
            int width = ((Number)stroke.getWidth().getValue(null)).intValue();
            lineElement.addElement(WIDTH_ELEMENT_NAME).addText("" + width);
        }
        Element polygonElement = styleElement.addElement(POLYGON_STYLE_ELEMENT_NAME);
        String color = DEFAULT_FILL_COLOR;
        if (hasFill) {
            Fill fill = polygonSymbolizer.getFill();
            Expression fillColor = fill.getColor();
            Expression opacity = fill.getOpacity();
            color = this.getColorFromExpression(fillColor, opacity);
        }
        polygonElement.addElement(COLOR_ELEMENT_NAME).addText(color);
        polygonElement.addElement(COLORMODE_ELEMENT_NAME).addText("normal");
        polygonElement.addElement(FILL_ELEMENT_NAME).addText(hasFill ? "1" : "0");
        polygonElement.addElement(OUTLINE_ELEMENT_NAME).addText(hasOutline ? "1" : "0");
    }

    private void writeLabelStyle(Element styleElement, TextSymbolizer textSymbolizer) {
        Element labelElement = styleElement.addElement(LABEL_STYLE_ELEMENT_NAME);
        labelElement.addElement(COLOR_ELEMENT_NAME).addText("ffffffff");
        labelElement.addElement(COLORMODE_ELEMENT_NAME).addText("normal");
    }

    private String writeSchema(Element docElement, FeatureSchema featureSchema) {
        Element schema = docElement.addElement(SCHEMA_ELEMENT_NAME);
        String schemaName = "schemaName";
        String schemaID = "schemaID";
        schema.addAttribute("name", schemaName);
        schema.addAttribute("targetId", schemaID);
        int geomIdx = featureSchema.getGeometryIndex();
        int i = 0;
        while (i < featureSchema.getAttributeCount()) {
            if (i != geomIdx) {
                Attribute currentAttr = featureSchema.getAttribute(i);
                Element simpleField = schema.addElement("SimpleField");
                simpleField.addAttribute("type", this.convertToKMLType(currentAttr.getType()));
                simpleField.addAttribute("name", currentAttr.getName());
                Element displayName = simpleField.addElement("displayName");
                displayName.addText(currentAttr.getPublicName());
            }
            ++i;
        }
        return schemaID;
    }

    private void writeFeatures(Element docElement, FeatureIterator fcIter, String schemaID, long limit) throws Exception {
        int cont = 0;
        while (fcIter.hasNext() && (long)cont < limit) {
            Feature feat = fcIter.next();
            Element placemark = docElement.addElement(PLACEMARK_ELEMENT_NAME);
            String styleID = this.getStyleIDForFeature(feat);
            this.writePlacemarkName(placemark, feat, cont++, styleID);
            placemark.addElement("visibility").addText("1");
            if (styleID != null) {
                placemark.addElement("styleUrl").addText("#" + styleID);
            }
            Element extendedData = placemark.addElement(EXTENDED_DATA_ELEMENT_NAME);
            Element schemaData = extendedData.addElement(SCHEMA_DATA_ELEMENT_NAME);
            schemaData.addAttribute("schemaUrl", "#" + schemaID);
            FeatureSchema featSchema = feat.getSchema();
            int geomIndex = featSchema.getGeometryIndex();
            int i = 0;
            while (i < featSchema.getAttributeCount()) {
                if (i != geomIndex) {
                    Attribute attr = featSchema.getAttribute(i);
                    Object value = feat.getAttribute(i);
                    Element simpleData = schemaData.addElement(SIMPLE_DATA_ELEMENT_NAME);
                    simpleData.addAttribute("name", attr.getName());
                    if (value != null) {
                        simpleData.addText(this.convertValueToKMLString(value, attr.getType()));
                    }
                }
                ++i;
            }
            this.writeGeometry(placemark, feat.getGeometry());
        }
    }

    private void writePlacemarkName(Element placemark, Feature feat, int cont, String styleID) {
        String placemarkName = null;
        if (this.ruleNameLabelExpressionMap.containsKey(styleID)) {
            Expression expr = this.ruleNameLabelExpressionMap.get(styleID);
            placemarkName = (String)expr.getValue(feat);
        }
        if (StringUtils.isEmpty(placemarkName)) {
            placemarkName = String.valueOf(BASE_FEATURE_NAME) + "_" + cont;
        }
        placemark.addElement("name").addText(placemarkName);
    }

    private String getStyleIDForFeature(Feature feat) {
        String styleIDName = null;
        Iterator<String> itRuleNames = this.ruleNameFilterMap.keySet().iterator();
        while (itRuleNames.hasNext() && styleIDName == null) {
            String ruleName = itRuleNames.next();
            Filter ruleFilter = this.ruleNameFilterMap.get(ruleName);
            if (!ruleFilter.contains(feat)) continue;
            styleIDName = ruleName;
        }
        if (StringUtils.isEmpty(styleIDName) && StringUtils.isNotEmpty((String)this.otherRuleName)) {
            styleIDName = this.otherRuleName;
        }
        return styleIDName;
    }

    private void writeGeometry(Element placemark, Geometry geometry) {
        if (geometry instanceof GeometryCollection) {
            this.writeMultiGeometry(placemark, geometry);
        } else if (geometry instanceof Point) {
            Point point = (Point)geometry;
            this.writePoint(placemark, point);
        } else if (geometry instanceof LinearRing) {
            LinearRing linearRing = (LinearRing)geometry;
            this.writeLinearRing(placemark, linearRing);
        } else if (geometry instanceof LineString) {
            LineString lineString = (LineString)geometry;
            this.writeLineString(placemark, lineString);
        } else if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon)geometry;
            this.writePolygon(placemark, polygon);
        }
    }

    private void writeMultiGeometry(Element placemark, Geometry geometry) {
        Element multiGeomElement = placemark.addElement(MULTIGEOMETRY_ELEMENT_NAME);
        int i = 0;
        while (i < geometry.getNumGeometries()) {
            this.writeGeometry(multiGeomElement, geometry.getGeometryN(i));
            ++i;
        }
    }

    private void writePoint(Element placemark, Point point) {
        Element pointElement = placemark.addElement(POINT_ELEMENT_NAME);
        pointElement.addElement("extrude").addText("0");
        pointElement.addElement("altitudeMode").addText("clampToGround");
        this.writeCoordinates(pointElement, point.getCoordinateSequence());
    }

    private void writeLinearRing(Element placemark, LinearRing linearRing) {
        Element linearRingElement = placemark.addElement(LINEARRING_ELEMENT_NAME);
        linearRingElement.addElement("extrude").addText("0");
        linearRingElement.addElement("tessellate").addText("0");
        linearRingElement.addElement("altitudeMode").addText("clampToGround");
        this.writeCoordinates(linearRingElement, linearRing.getCoordinateSequence());
    }

    private void writeLineString(Element placemark, LineString lineString) {
        Element lineElement = placemark.addElement(LINESTRING_ELEMENT_NAME);
        lineElement.addElement("extrude").addText("0");
        lineElement.addElement("tessellate").addText("0");
        lineElement.addElement("altitudeMode").addText("clampToGround");
        this.writeCoordinates(lineElement, lineString.getCoordinateSequence());
    }

    private void writePolygon(Element placemark, Polygon polygon) {
        Element polygonElement = placemark.addElement(POLYGON_ELEMENT_NAME);
        polygonElement.addElement("extrude").addText("0");
        polygonElement.addElement("tessellate").addText("0");
        polygonElement.addElement("altitudeMode").addText("clampToGround");
        LineString extRing = polygon.getExteriorRing();
        Element outerBoundaryElement = polygonElement.addElement(OUTER_BOUNDARY_ELEMENT_NAME);
        this.writeLinearRing(outerBoundaryElement, this.geomFac.createLinearRing(extRing.getCoordinates()));
        if (polygon.getNumInteriorRing() > 0) {
            Element innerBoundaryElement = polygonElement.addElement(INNER_BOUNDARY_ELEMENT_NAME);
            int i = 0;
            while (i < polygon.getNumInteriorRing()) {
                LineString innerRing = polygon.getInteriorRingN(i);
                this.writeLinearRing(innerBoundaryElement, this.geomFac.createLinearRing(innerRing.getCoordinates()));
                ++i;
            }
        }
    }

    private void writeCoordinates(Element geomElement, CoordinateSequence sequence) {
        Element coordinatesElement = geomElement.addElement(COORDINATES_ELEMENT_NAME);
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        while (i < sequence.size()) {
            if (i != 0) {
                buffer.append("\n");
            }
            Coordinate coord = sequence.getCoordinate(i);
            Point2D pt = new Point2D.Double();
            pt.setLocation(coord.x, coord.y);
            pt = this.ct.convert(pt, null);
            buffer.append(pt.getX());
            buffer.append(",");
            buffer.append(pt.getY());
            if (!Double.isNaN(coord.z)) {
                buffer.append(",");
                buffer.append(coord.z);
            }
            ++i;
        }
        coordinatesElement.addText(buffer.toString());
    }

    private String convertValueToKMLString(Object value, AttributeType type) {
        return (String)FeatureUtil.getGoodAttribute(AttributeType.STRING, value);
    }

    private String convertToKMLType(AttributeType type) {
        String conversion = null;
        conversion = AttributeType.isString(type) ? "string" : (AttributeType.isNumeric(type) ? (type.equals(AttributeType.INTEGER) ? "int" : (type.equals(AttributeType.FLOAT) || type.equals(AttributeType.LONG) ? "float" : "double")) : (AttributeType.isDate(type) ? "string" : (type.equals(AttributeType.BOOLEAN) ? "bool" : "string")));
        return conversion;
    }
}

