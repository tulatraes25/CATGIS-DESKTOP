/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionDOMParser;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LogicFilter;
import org.saig.core.filter.NullFilter;
import org.saig.jump.lang.I18N;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class FilterDOMParser {
    private static final Logger LOGGER = Logger.getLogger(FilterDOMParser.class);
    private static final FilterFactory FILTER_FACT = FilterFactory.createFilterFactory();
    private static final int NUM_BETWEEN_CHILDREN = 3;
    private static Map<String, Integer> comparisions = new HashMap<String, Integer>();
    private static Map<String, Integer> spatial = new HashMap<String, Integer>();
    private static Map<String, Integer> logical = new HashMap<String, Integer>();

    static {
        comparisions.put("PropertyIsEqualTo", new Integer(14));
        comparisions.put("PropertyIsNotEqualTo", new Integer(23));
        comparisions.put("PropertyIsGreaterThan", new Integer(16));
        comparisions.put("PropertyIsGreaterThanOrEqualTo", new Integer(18));
        comparisions.put("PropertyIsLessThan", new Integer(15));
        comparisions.put("PropertyIsLessThanOrEqualTo", new Integer(17));
        comparisions.put("PropertyIsLike", new Integer(20));
        comparisions.put("PropertyIsNull", new Integer(21));
        comparisions.put("PropertyIsBetween", new Integer(19));
        comparisions.put("FeatureId", new Integer(22));
        spatial.put("Equals", new Integer(5));
        spatial.put("Disjoint", new Integer(6));
        spatial.put("Intersects", new Integer(7));
        spatial.put("Touches", new Integer(8));
        spatial.put("Crosses", new Integer(9));
        spatial.put("Within", new Integer(10));
        spatial.put("Contains", new Integer(11));
        spatial.put("Overlaps", new Integer(12));
        spatial.put("Beyond", new Integer(13));
        spatial.put("BBOX", new Integer(4));
        logical.put("And", new Integer(2));
        logical.put("Or", new Integer(1));
        logical.put("Not", new Integer(3));
    }

    private FilterDOMParser() {
    }

    public static Filter parseFilter(Node root, FeatureSchema schema) {
        LOGGER.debug((Object)("parsingFilter " + root.getLocalName()));
        if (root == null || root.getNodeType() != 1) {
            LOGGER.debug((Object)"bad node input ");
            return null;
        }
        LOGGER.debug((Object)("processing root " + root.getLocalName() + " " + root.getNodeName()));
        Node child = root;
        String childName = child.getLocalName();
        if (childName == null) {
            childName = child.getNodeName();
        }
        if (childName.indexOf(58) != -1) {
            childName = childName.substring(childName.indexOf(58) + 1);
        }
        LOGGER.debug((Object)("looking up " + childName));
        if (comparisions.containsKey(childName)) {
            CompareFilterImpl filter;
            short type;
            block48: {
                LOGGER.debug((Object)("a comparision filter " + childName));
                type = comparisions.get(childName).shortValue();
                filter = null;
                LOGGER.debug((Object)("type is " + type));
                if (type == 22) {
                    FidFilter fidFilter = FILTER_FACT.createFidFilter();
                    Element fidElement = (Element)child;
                    fidFilter.addFid(fidElement.getAttribute("fid"));
                    Node sibling = fidElement.getNextSibling();
                    while (sibling != null) {
                        LOGGER.debug((Object)"Parsing another FidFilter");
                        if (sibling.getNodeType() == 1) {
                            fidElement = (Element)sibling;
                            String fidElementName = fidElement.getLocalName();
                            if (fidElementName == null) {
                                fidElementName = fidElement.getNodeName();
                            }
                            if (fidElementName.indexOf(58) != -1) {
                                fidElementName = fidElementName.substring(fidElementName.indexOf(58) + 1);
                            }
                            if ("FeatureId".equals(fidElementName)) {
                                fidFilter.addFid(fidElement.getAttribute("fid"));
                            }
                        }
                        sibling = sibling.getNextSibling();
                    }
                    return fidFilter;
                }
                if (type == 19) {
                    BetweenFilter bfilter = FILTER_FACT.createBetweenFilter();
                    NodeList kids = child.getChildNodes();
                    if (kids.getLength() < 3) {
                        throw new IllegalFilterException("wrong number of children in Between filter: expected 3 got " + kids.getLength());
                    }
                    Node value = child.getFirstChild();
                    while (value.getNodeType() != 1) {
                        value = value.getNextSibling();
                    }
                    LOGGER.debug((Object)("add middle value -> " + value + "<-"));
                    bfilter.addMiddleValue(ExpressionDOMParser.parseExpression(value, schema));
                    int i = 0;
                    while (i < kids.getLength()) {
                        String kidName;
                        Node kid = kids.item(i);
                        String string = kidName = kid.getLocalName() != null ? kid.getLocalName() : kid.getNodeName();
                        if (kidName.indexOf(58) != -1) {
                            kidName = kidName.substring(kidName.indexOf(58) + 1);
                        }
                        if (kidName.equalsIgnoreCase("LowerBoundary")) {
                            value = kid.getFirstChild();
                            while (value.getNodeType() != 1) {
                                value = value.getNextSibling();
                            }
                            LOGGER.debug((Object)("add left value -> " + value + "<-"));
                            bfilter.addLeftValue(ExpressionDOMParser.parseExpression(value, schema));
                        }
                        if (kidName.equalsIgnoreCase("UpperBoundary")) {
                            value = kid.getFirstChild();
                            while (value.getNodeType() != 1) {
                                value = value.getNextSibling();
                            }
                            LOGGER.debug((Object)("add right value -> " + value + "<-"));
                            bfilter.addRightValue(ExpressionDOMParser.parseExpression(value, schema));
                        }
                        ++i;
                    }
                    return bfilter;
                }
                if (type != 20) break block48;
                String wildcard = null;
                String single = null;
                String escape = null;
                String pattern = null;
                Object value = null;
                NodeList map = child.getChildNodes();
                int i = 0;
                while (i < map.getLength()) {
                    Node kid = map.item(i);
                    if (kid != null && kid.getNodeType() == 1) {
                        String res;
                        String string = res = kid.getLocalName() != null ? kid.getLocalName() : kid.getNodeName();
                        if (res.indexOf(58) != -1) {
                            res = res.substring(res.indexOf(58) + 1);
                        }
                        if (res.equalsIgnoreCase("PropertyName")) {
                            value = ExpressionDOMParser.parseExpression(kid, schema);
                        }
                        if (res.equalsIgnoreCase("Literal")) {
                            pattern = ExpressionDOMParser.parseExpression(kid, schema).toString();
                        }
                    }
                    ++i;
                }
                NamedNodeMap kids = child.getAttributes();
                int i2 = 0;
                while (i2 < kids.getLength()) {
                    String res;
                    Node kid = kids.item(i2);
                    String string = res = kid.getLocalName() != null ? kid.getLocalName() : kid.getNodeName();
                    if (res.indexOf(58) != -1) {
                        res = res.substring(res.indexOf(58) + 1);
                    }
                    if (res.equalsIgnoreCase("wildCard")) {
                        wildcard = kid.getNodeValue();
                    }
                    if (res.equalsIgnoreCase("singleChar")) {
                        single = kid.getNodeValue();
                    }
                    if (res.equalsIgnoreCase("escapeChar") || res.equalsIgnoreCase("escape")) {
                        escape = kid.getNodeValue();
                    }
                    ++i2;
                }
                if (wildcard != null && single != null && escape != null && pattern != null) {
                    LikeFilter lfilter = FILTER_FACT.createLikeFilter();
                    LOGGER.debug((Object)("Building like filter " + value.toString() + "\n" + pattern + " " + wildcard + " " + single + " " + escape));
                    lfilter.setValue((Expression)value);
                    lfilter.setPattern(pattern, wildcard, single, escape);
                    return lfilter;
                }
                LOGGER.debug((Object)("Problem building like filter\n" + pattern + " " + wildcard + " " + single + " " + escape));
                return null;
            }
            try {
                if (type == 21) {
                    return FilterDOMParser.parseNullFilter(child, schema);
                }
                filter = new CompareFilterImpl(type);
                Node value = child.getFirstChild();
                while (value.getNodeType() != 1) {
                    value = value.getNextSibling();
                }
                LOGGER.debug((Object)("add left value -> " + value + "<-"));
                filter.addLeftValue(ExpressionDOMParser.parseExpression(value, schema));
                value = value.getNextSibling();
                while (value.getNodeType() != 1) {
                    value = value.getNextSibling();
                }
                int attrType = FilterDOMParser.guessAttributeTypeClass(filter.getLeftValue(), schema);
                LOGGER.debug((Object)("add right value -> " + value + "<-"));
                filter.addRightValue(ExpressionDOMParser.parseExpression(value, attrType, schema));
                return filter;
            }
            catch (IllegalFilterException ife) {
                LOGGER.warn((Object)("Unable to build filter: " + ife));
                return null;
            }
        }
        if (spatial.containsKey(childName)) {
            LOGGER.debug((Object)("a spatial filter " + childName));
            try {
                String valueName;
                short type = spatial.get(childName).shortValue();
                GeometryFilter filter = FILTER_FACT.createGeometryFilter(type);
                Node value = child.getFirstChild();
                while (value.getNodeType() != 1) {
                    value = value.getNextSibling();
                }
                LOGGER.debug((Object)("add left value -> " + value + "<-"));
                filter.addLeftGeometry(ExpressionDOMParser.parseExpression(value, schema));
                value = value.getNextSibling();
                while (value.getNodeType() != 1) {
                    value = value.getNextSibling();
                }
                LOGGER.debug((Object)("add right value -> " + value + "<-"));
                String string = valueName = value.getLocalName() != null ? value.getLocalName() : value.getNodeName();
                if (valueName.indexOf(58) != -1) {
                    valueName = valueName.substring(valueName.indexOf(58) + 1);
                }
                if (!valueName.equalsIgnoreCase("Literal") && !valueName.equalsIgnoreCase("propertyname")) {
                    Element literal = value.getOwnerDocument().createElement("literal");
                    literal.appendChild(value);
                    LOGGER.debug((Object)("Built new literal " + literal));
                    value = literal;
                }
                filter.addRightGeometry(ExpressionDOMParser.parseExpression(value, schema));
                return filter;
            }
            catch (IllegalFilterException ife) {
                LOGGER.warn((Object)("Unable to build filter: " + ife));
                return null;
            }
        }
        if (logical.containsKey(childName)) {
            LOGGER.debug((Object)("a logical filter " + childName));
            try {
                short type = logical.get(childName).shortValue();
                LOGGER.debug((Object)("logic type " + type));
                LogicFilter filter = FILTER_FACT.createLogicFilter(type);
                NodeList map = child.getChildNodes();
                int i = 0;
                while (i < map.getLength()) {
                    Node kid = map.item(i);
                    if (kid != null && kid.getNodeType() == 1) {
                        LOGGER.debug((Object)("adding to logic filter " + kid.getLocalName()));
                        filter.addFilter(FilterDOMParser.parseFilter(kid, schema));
                    }
                    ++i;
                }
                return filter;
            }
            catch (IllegalFilterException ife) {
                LOGGER.warn((Object)("Unable to build filter: " + ife));
                return null;
            }
        }
        LOGGER.warn((Object)("unknown filter " + root));
        return null;
    }

    private static int guessAttributeTypeClass(Expression leftValue, FeatureSchema schema) {
        boolean isAttrExpression = AttributeExpression.class.isAssignableFrom(leftValue.getClass());
        int attrType = -1;
        if (isAttrExpression) {
            AttributeExpression attrExpr = (AttributeExpression)leftValue;
            String attrName = attrExpr.getAttributePath();
            AttributeType attr = schema.getAttributeType(attrName);
            if (attr != null) {
                if (String.class.isAssignableFrom(attr.toJavaClass())) {
                    attrType = 103;
                }
            } else {
                LOGGER.warn((Object)I18N.getMessage(FilterDOMParser.class, "attribute-{0}-does-not-exist", new Object[]{attrName}));
            }
        }
        return attrType;
    }

    private static NullFilter parseNullFilter(Node nullNode, FeatureSchema schema) throws IllegalFilterException {
        LOGGER.info((Object)("parsing null node: " + nullNode));
        NullFilter nFilter = FILTER_FACT.createNullFilter();
        Node value = nullNode.getFirstChild();
        while (value.getNodeType() != 1) {
            value = value.getNextSibling();
        }
        LOGGER.debug((Object)("add null value -> " + value + "<-"));
        nFilter.setNullCheckValue(ExpressionDOMParser.parseExpression(value, schema));
        return nFilter;
    }
}

