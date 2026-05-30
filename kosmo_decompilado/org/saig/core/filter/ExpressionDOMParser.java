/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.geom.TopologyException
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.IllegalFilterException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class ExpressionDOMParser {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");
    private static final FilterFactory FILTER_FACT = FilterFactory.createFilterFactory();
    private static GeometryFactory gfac = new GeometryFactory();
    private static final int GML_BOX = 1;
    private static final int GML_POLYGON = 2;
    private static final int GML_LINESTRING = 3;
    private static final int GML_POINT = 4;
    private static final int NUM_BOX_COORDS = 5;

    private ExpressionDOMParser() {
    }

    public static Expression parseExpression(Node root, FeatureSchema schema) {
        return ExpressionDOMParser.parseExpression(root, -1, schema);
    }

    /*
     * Unable to fully structure code
     */
    public static Expression parseExpression(Node root, int type, FeatureSchema schema) {
        block68: {
            ExpressionDOMParser.LOGGER.finer("parsingExpression " + root.getLocalName());
            if (root == null || root.getNodeType() != 1) {
                ExpressionDOMParser.LOGGER.finer("bad node input ");
                return null;
            }
            ExpressionDOMParser.LOGGER.finer("processing root " + root.getLocalName());
            child = root;
            v0 = childName = child.getLocalName() != null ? child.getLocalName() : child.getNodeName();
            if (childName.indexOf(58) != -1) {
                childName = childName.substring(childName.indexOf(58) + 1);
            }
            if (childName.equalsIgnoreCase("Literal")) {
                ExpressionDOMParser.LOGGER.finer("processing literal " + child);
                kidList = child.getChildNodes();
                ExpressionDOMParser.LOGGER.finest("literal elements (" + kidList.getLength() + ") " + kidList.toString());
                if (kidList.getLength() == 0) {
                    return ExpressionDOMParser.FILTER_FACT.createLiteralExpression("");
                }
                i = 0;
                while (i < kidList.getLength()) {
                    kid = kidList.item(i);
                    ExpressionDOMParser.LOGGER.finest("kid " + i + " " + kid);
                    if (kid == null) {
                        ExpressionDOMParser.LOGGER.finest("Skipping ");
                    } else {
                        if (kid.getNodeValue() == null) {
                            ExpressionDOMParser.LOGGER.finer("node " + kid.getNodeValue() + " namespace " + kid.getNamespaceURI());
                            ExpressionDOMParser.LOGGER.fine("a literal gml string?");
                            try {
                                geom = ExpressionDOMParser.parseGML(kid);
                                if (geom != null) {
                                    ExpressionDOMParser.LOGGER.finer("built a " + geom.getGeometryType() + " from gml");
                                    ExpressionDOMParser.LOGGER.finer("\tpoints: " + geom.getNumPoints());
                                } else {
                                    ExpressionDOMParser.LOGGER.finer("got a null geometry back from gml parser");
                                }
                                return ExpressionDOMParser.FILTER_FACT.createLiteralExpression(geom);
                            }
                            catch (IllegalFilterException ife) {
                                ExpressionDOMParser.LOGGER.warning("Problem building GML/JTS object: " + ife);
                                return null;
                            }
                        }
                        if (kid.getNodeValue().trim().length() == 0) {
                            ExpressionDOMParser.LOGGER.finest("empty text element");
                        } else {
                            nodeValue = kid.getNodeValue();
                            ExpressionDOMParser.LOGGER.finer("processing " + nodeValue);
                            try {
                                switch (type) {
                                    case 103: {
                                        return ExpressionDOMParser.FILTER_FACT.createLiteralExpression(nodeValue);
                                    }
                                    case 102: {
                                        try {
                                            intLit = new Integer(nodeValue);
                                            ExpressionDOMParser.LOGGER.finer("An integer");
                                            return ExpressionDOMParser.FILTER_FACT.createLiteralExpression(intLit);
                                        }
                                        catch (NumberFormatException e) {
                                            return ExpressionDOMParser.FILTER_FACT.createLiteralExpression(nodeValue);
                                        }
                                    }
                                    case 101: {
                                        try {
                                            doubleLit = new Double(nodeValue);
                                            ExpressionDOMParser.LOGGER.finer("A double");
                                            return ExpressionDOMParser.FILTER_FACT.createLiteralExpression(doubleLit);
                                        }
                                        catch (NumberFormatException e) {
                                            return ExpressionDOMParser.FILTER_FACT.createLiteralExpression(nodeValue);
                                        }
                                    }
                                }
                                try {
                                    intLit = new Integer(nodeValue);
                                    ExpressionDOMParser.LOGGER.finer("An integer");
                                    return ExpressionDOMParser.FILTER_FACT.createLiteralExpression(intLit);
                                }
                                catch (NumberFormatException intLit) {
                                    try {
                                        doubleLit = new Double(nodeValue);
                                        ExpressionDOMParser.LOGGER.finer("A double");
                                        return ExpressionDOMParser.FILTER_FACT.createLiteralExpression(doubleLit);
                                    }
                                    catch (NumberFormatException doubleLit) {
                                        ExpressionDOMParser.LOGGER.finer("defaulting to string");
                                        return ExpressionDOMParser.FILTER_FACT.createLiteralExpression(nodeValue);
                                    }
                                }
                            }
                            catch (IllegalFilterException ife) {
                                ExpressionDOMParser.LOGGER.finer("Unable to build expression " + ife);
                                return null;
                            }
                        }
                    }
                    ++i;
                }
            }
            if (childName.equalsIgnoreCase("add")) {
                try {
                    ExpressionDOMParser.LOGGER.fine("processing an Add");
                    math = ExpressionDOMParser.FILTER_FACT.createMathExpression((short)105);
                    value = child.getFirstChild();
                    while (value.getNodeType() != 1) {
                        value = value.getNextSibling();
                    }
                    ExpressionDOMParser.LOGGER.finer("add left value -> " + value + "<-");
                    math.addLeftValue(ExpressionDOMParser.parseExpression(value, schema));
                    value = value.getNextSibling();
                    while (value.getNodeType() != 1) {
                        value = value.getNextSibling();
                    }
                    ExpressionDOMParser.LOGGER.finer("add right value -> " + value + "<-");
                    math.addRightValue(ExpressionDOMParser.parseExpression(value, schema));
                    return math;
                }
                catch (IllegalFilterException ife) {
                    ExpressionDOMParser.LOGGER.warning("Unable to build expression " + ife);
                    return null;
                }
            }
            if (childName.equalsIgnoreCase("sub")) {
                try {
                    math = ExpressionDOMParser.FILTER_FACT.createMathExpression((short)106);
                    value = child.getFirstChild();
                    while (value.getNodeType() != 1) {
                        value = value.getNextSibling();
                    }
                    ExpressionDOMParser.LOGGER.finer("add left value -> " + value + "<-");
                    math.addLeftValue(ExpressionDOMParser.parseExpression(value, schema));
                    value = value.getNextSibling();
                    while (value.getNodeType() != 1) {
                        value = value.getNextSibling();
                    }
                    ExpressionDOMParser.LOGGER.finer("add right value -> " + value + "<-");
                    math.addRightValue(ExpressionDOMParser.parseExpression(value, schema));
                    return math;
                }
                catch (IllegalFilterException ife) {
                    ExpressionDOMParser.LOGGER.warning("Unable to build expression " + ife);
                    return null;
                }
            }
            if (childName.equalsIgnoreCase("mul")) {
                try {
                    math = ExpressionDOMParser.FILTER_FACT.createMathExpression((short)107);
                    value = child.getFirstChild();
                    while (value.getNodeType() != 1) {
                        value = value.getNextSibling();
                    }
                    ExpressionDOMParser.LOGGER.finer("add left value -> " + value + "<-");
                    math.addLeftValue(ExpressionDOMParser.parseExpression(value, schema));
                    value = value.getNextSibling();
                    while (value.getNodeType() != 1) {
                        value = value.getNextSibling();
                    }
                    ExpressionDOMParser.LOGGER.finer("add right value -> " + value + "<-");
                    math.addRightValue(ExpressionDOMParser.parseExpression(value, schema));
                    return math;
                }
                catch (IllegalFilterException ife) {
                    ExpressionDOMParser.LOGGER.warning("Unable to build expression " + ife);
                    return null;
                }
            }
            if (childName.equalsIgnoreCase("div")) {
                try {
                    math = ExpressionDOMParser.FILTER_FACT.createMathExpression((short)108);
                    value = child.getFirstChild();
                    while (value.getNodeType() != 1) {
                        value = value.getNextSibling();
                    }
                    ExpressionDOMParser.LOGGER.finer("add left value -> " + value + "<-");
                    math.addLeftValue(ExpressionDOMParser.parseExpression(value, schema));
                    value = value.getNextSibling();
                    while (value.getNodeType() != 1) {
                        value = value.getNextSibling();
                    }
                    ExpressionDOMParser.LOGGER.finer("add right value -> " + value + "<-");
                    math.addRightValue(ExpressionDOMParser.parseExpression(value, schema));
                    return math;
                }
                catch (IllegalFilterException ife) {
                    ExpressionDOMParser.LOGGER.warning("Unable to build expression " + ife);
                    return null;
                }
            }
            if (childName.equalsIgnoreCase("PropertyName")) {
                try {
                    attribute = ExpressionDOMParser.FILTER_FACT.createAttributeExpression(child.getFirstChild().getNodeValue());
                    return attribute;
                }
                catch (IllegalFilterException ife) {
                    ExpressionDOMParser.LOGGER.warning("Unable to build expression: " + ife);
                    return null;
                }
            }
            if (!childName.equalsIgnoreCase("Function")) break block68;
            func = null;
            param = (Element)child;
            map = param.getAttributes();
            funcName = null;
            k = 0;
            while (k < map.getLength()) {
                res = map.item(k).getNodeValue();
                name = map.item(k).getLocalName();
                if (name == null) {
                    name = map.item(k).getNodeName();
                }
                if (name.indexOf(58) != -1) {
                    name = name.substring(name.indexOf(58) + 1);
                }
                ExpressionDOMParser.LOGGER.fine("attribute " + name + " with value of " + res);
                if (name.equalsIgnoreCase("name")) {
                    funcName = res;
                    func = ExpressionDOMParser.FILTER_FACT.createFunctionExpression(res);
                }
                ++k;
            }
            if (func == null) {
                if (funcName != null) {
                    ExpressionDOMParser.LOGGER.severe("failed to create instance of function " + funcName);
                } else {
                    ExpressionDOMParser.LOGGER.severe("failed to find a function name in " + child);
                }
                return null;
            }
            argCount = func.getArgCount();
            args = new Expression[argCount];
            value = child.getFirstChild();
            i = 0;
            ** GOTO lbl197
            {
                value = value.getNextSibling();
                do {
                    if (value.getNodeType() != 1) continue block43;
                    args[i] = ExpressionDOMParser.parseExpression(value, schema);
                    value = value.getNextSibling();
                    ++i;
lbl197:
                    // 2 sources

                } while (i < argCount);
            }
            func.setArgs(args);
            return func;
        }
        if (child.getNodeType() == 3) {
            ExpressionDOMParser.LOGGER.finer("processing a text node " + root.getNodeValue());
            nodeValue = root.getNodeValue();
            ExpressionDOMParser.LOGGER.finer("Text name " + nodeValue);
            try {
                try {
                    intLiteral = new Integer(nodeValue);
                    return ExpressionDOMParser.FILTER_FACT.createLiteralExpression(intLiteral);
                }
                catch (NumberFormatException intLiteral) {
                    try {
                        doubleLit = new Double(nodeValue);
                        return ExpressionDOMParser.FILTER_FACT.createLiteralExpression(doubleLit);
                    }
                    catch (NumberFormatException doubleLit) {
                        return ExpressionDOMParser.FILTER_FACT.createLiteralExpression(nodeValue);
                    }
                }
            }
            catch (IllegalFilterException ife) {
                ExpressionDOMParser.LOGGER.finer("Unable to build expression " + ife);
            }
        }
        return null;
    }

    public static Geometry parseGML(Node root) {
        LOGGER.finer("processing gml " + root);
        int type = 0;
        Node child = root;
        String childName = child.getNodeName();
        if (childName == null) {
            childName = child.getLocalName();
        }
        if (!childName.startsWith("gml:")) {
            childName = "gml:" + childName;
        }
        if (childName.equalsIgnoreCase("gml:box")) {
            type = 1;
            List<Coordinate> coordList = ExpressionDOMParser.parseCoords(child);
            Envelope env = new Envelope();
            int i = 0;
            while (i < coordList.size()) {
                env.expandToInclude(coordList.get(i));
                ++i;
            }
            Coordinate[] coords = new Coordinate[]{new Coordinate(env.getMinX(), env.getMinY()), new Coordinate(env.getMinX(), env.getMaxY()), new Coordinate(env.getMaxX(), env.getMaxY()), new Coordinate(env.getMaxX(), env.getMinY()), new Coordinate(env.getMinX(), env.getMinY())};
            LinearRing ring = null;
            try {
                ring = gfac.createLinearRing(coords);
            }
            catch (TopologyException tope) {
                LOGGER.fine("Topology Exception in GMLBox" + (Object)((Object)tope));
                return null;
            }
            return gfac.createPolygon(ring, null);
        }
        if (childName.equalsIgnoreCase("gml:polygon")) {
            LOGGER.finer("polygon");
            type = 2;
            LinearRing outer = null;
            ArrayList<LinearRing> inner = new ArrayList<LinearRing>();
            NodeList kids = root.getChildNodes();
            int i = 0;
            while (i < kids.getLength()) {
                Node kid = kids.item(i);
                LOGGER.finer("doing " + kid);
                String kidName = kid.getNodeName();
                if (kidName == null) {
                    kidName = child.getLocalName();
                }
                if (!kidName.startsWith("gml:")) {
                    kidName = "gml:" + kidName;
                }
                if (kidName.equalsIgnoreCase("gml:outerBoundaryIs")) {
                    outer = (LinearRing)ExpressionDOMParser.parseGML(kid);
                }
                if (kidName.equalsIgnoreCase("gml:innerBoundaryIs")) {
                    inner.add((LinearRing)ExpressionDOMParser.parseGML(kid));
                }
                ++i;
            }
            if (inner.size() > 0) {
                return gfac.createPolygon(outer, inner.toArray(new LinearRing[0]));
            }
            return gfac.createPolygon(outer, null);
        }
        if (childName.equalsIgnoreCase("gml:outerBoundaryIs") || childName.equalsIgnoreCase("gml:innerBoundaryIs")) {
            LOGGER.finer("Boundary layer");
            NodeList kids = ((Element)child).getElementsByTagName("gml:LinearRing");
            if (kids.getLength() == 0) {
                kids = ((Element)child).getElementsByTagName("LinearRing");
            }
            return ExpressionDOMParser.parseGML(kids.item(0));
        }
        if (childName.equalsIgnoreCase("gml:linearRing")) {
            LOGGER.finer("LinearRing");
            List<Coordinate> coordList = ExpressionDOMParser.parseCoords(child);
            LinearRing ring = null;
            try {
                ring = gfac.createLinearRing(coordList.toArray(new Coordinate[0]));
            }
            catch (TopologyException te) {
                LOGGER.finer("Topology Exception build linear ring: " + (Object)((Object)te));
                return null;
            }
            return ring;
        }
        if (childName.equalsIgnoreCase("gml:linestring")) {
            LOGGER.finer("linestring");
            type = 3;
            List<Coordinate> coordList = ExpressionDOMParser.parseCoords(child);
            LineString line = null;
            line = gfac.createLineString(coordList.toArray(new Coordinate[0]));
            return line;
        }
        if (childName.equalsIgnoreCase("gml:point")) {
            LOGGER.finer("point");
            type = 4;
            List<Coordinate> coordList = ExpressionDOMParser.parseCoords(child);
            Point point = null;
            point = gfac.createPoint(coordList.get(0));
            return point;
        }
        if (childName.toLowerCase().startsWith("gml:multiPolygon")) {
            LOGGER.finer("MultiPolygon");
            ArrayList<Polygon> multi = new ArrayList<Polygon>();
            NodeList kids = child.getChildNodes();
            int i = 0;
            while (i < kids.getLength()) {
                multi.add((Polygon)ExpressionDOMParser.parseGML(kids.item(i)));
                ++i;
            }
            return gfac.createMultiPolygon(multi.toArray(new Polygon[0]));
        }
        return null;
    }

    public static List<Coordinate> parseCoords(Node root) {
        LOGGER.finer("parsing coordinate(s) " + root);
        ArrayList<Coordinate> clist = new ArrayList<Coordinate>();
        NodeList kids = root.getChildNodes();
        int i = 0;
        while (i < kids.getLength()) {
            Node child = kids.item(i);
            LOGGER.finer("doing " + child);
            String childName = child.getNodeName();
            if (childName == null) {
                childName = child.getLocalName();
            }
            if (!childName.startsWith("gml:")) {
                childName = "gml:" + childName;
            }
            if (childName.equalsIgnoreCase("gml:coord")) {
                Coordinate c = new Coordinate();
                NodeList grandChildren = child.getChildNodes();
                int t = 0;
                while (t < grandChildren.getLength()) {
                    Node grandChild = grandChildren.item(t);
                    String grandChildName = grandChild.getNodeName();
                    if (grandChildName == null) {
                        grandChildName = grandChild.getLocalName();
                    }
                    if (!grandChildName.startsWith("gml:")) {
                        grandChildName = "gml:" + grandChildName;
                    }
                    if (grandChildName.equalsIgnoreCase("gml:x")) {
                        c.x = Double.parseDouble(grandChild.getChildNodes().item(0).getNodeValue().trim());
                    } else if (grandChildName.equalsIgnoreCase("gml:y")) {
                        c.y = Double.parseDouble(grandChild.getChildNodes().item(0).getNodeValue().trim());
                    } else if (grandChildName.equalsIgnoreCase("gml:z")) {
                        c.z = Double.parseDouble(grandChild.getChildNodes().item(0).getNodeValue().trim());
                    }
                    ++t;
                }
                clist.add(c);
            }
            if (childName.equalsIgnoreCase("gml:coordinates")) {
                LOGGER.finer("coordinates " + child.getFirstChild().getNodeValue());
                NodeList grandKids = child.getChildNodes();
                int k = 0;
                while (k < grandKids.getLength()) {
                    Node grandKid = grandKids.item(k);
                    if (grandKid.getNodeValue() != null && grandKid.getNodeValue().trim().length() != 0) {
                        String outer = grandKid.getNodeValue().trim();
                        StringTokenizer ost = new StringTokenizer(outer, " ");
                        while (ost.hasMoreTokens()) {
                            String internal = ost.nextToken();
                            StringTokenizer ist = new StringTokenizer(internal, ",");
                            double xCoord = Double.parseDouble(ist.nextToken());
                            double yCoord = Double.parseDouble(ist.nextToken());
                            double zCoord = Double.NaN;
                            if (ist.hasMoreTokens()) {
                                zCoord = Double.parseDouble(ist.nextToken());
                            }
                            clist.add(new Coordinate(xCoord, yCoord, zCoord));
                        }
                    }
                    ++k;
                }
            }
            ++i;
        }
        return clist;
    }
}

