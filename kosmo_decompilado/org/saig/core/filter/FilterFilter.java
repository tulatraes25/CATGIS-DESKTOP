/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.log4j.Logger
 *  org.geotools.gml.GMLHandlerJTS
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.FeatureSchema;
import org.apache.log4j.Logger;
import org.geotools.gml.GMLHandlerJTS;
import org.saig.core.filter.AbstractFilter;
import org.saig.core.filter.DefaultExpression;
import org.saig.core.filter.ExpressionSAXParser;
import org.saig.core.filter.FilterHandler;
import org.saig.core.filter.FilterSAXParser;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LogicSAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public class FilterFilter
extends XMLFilterImpl
implements GMLHandlerJTS {
    private static final Logger LOGGER = Logger.getLogger(FilterFilter.class);
    private LogicSAXParser logicFactory;
    private FilterSAXParser filterFactory;
    private ExpressionSAXParser expressionFactory;
    private FilterHandler parent;
    private FeatureSchema schema;
    private boolean isLogicFilter = false;
    private boolean isFidFilter = false;
    protected boolean insideFilter = false;
    private boolean insideDistance = false;
    private String units;

    public FilterFilter(FilterHandler parent, FeatureSchema schema) {
        this.parent = parent;
        this.schema = schema;
        this.expressionFactory = new ExpressionSAXParser(schema);
        this.filterFactory = new FilterSAXParser();
        this.logicFactory = new LogicSAXParser();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        LOGGER.debug((Object)("found start element: " + localName));
        if (localName.equals("Filter")) {
            this.insideFilter = true;
            return;
        } else if (this.insideFilter) {
            short filterType = FilterFilter.convertType(localName);
            LOGGER.debug((Object)("types: (xml): " + localName + "; " + "(internal): " + filterType));
            if (!(filterType != -1 || localName.equals("UpperBoundary") || localName.equals("LowerBoundary") || localName.equals("Distance") || localName.endsWith("Member"))) {
                throw new SAXException("Attempted to construct illegal filter - I dont understand the tag: " + qName + ".  HINT: tags are case-sensitive!");
            }
            try {
                if (this.isFidFilter) {
                    if (filterType == 22) {
                        LOGGER.debug((Object)"sending attributes to existing FID filter");
                        this.filterFactory.setAttributes(atts);
                    } else {
                        this.isFidFilter = false;
                        LOGGER.debug((Object)("is fid (1): " + this.isFidFilter));
                        if (this.isLogicFilter) {
                            this.logicFactory.add(this.filterFactory.create());
                        } else {
                            this.parent.filter(this.filterFactory.create());
                        }
                    }
                }
                if (this.isFidFilter) return;
                LOGGER.debug((Object)"is logic?");
                if (AbstractFilter.isLogicFilter(filterType)) {
                    LOGGER.debug((Object)"found a logic filter start");
                    this.isLogicFilter = true;
                    this.logicFactory.start(filterType);
                    return;
                }
                if (AbstractFilter.isSimpleFilter(filterType)) {
                    LOGGER.debug((Object)"found a simple filter start");
                    this.filterFactory.start(filterType);
                    if (filterType == 20) {
                        LOGGER.debug((Object)"sending attributes for like filter");
                        this.filterFactory.setAttributes(atts);
                        return;
                    }
                    if (filterType != 22) return;
                    LOGGER.debug((Object)"sending attributes to new FID filter");
                    this.filterFactory.setAttributes(atts);
                    this.isFidFilter = true;
                    LOGGER.debug((Object)("is fid (3): " + this.isFidFilter));
                    return;
                }
                if (DefaultExpression.isExpression(filterType)) {
                    LOGGER.debug((Object)"found an expression filter start");
                    this.expressionFactory.start(localName, atts);
                    return;
                }
                if (!localName.equals("Distance")) return;
                LOGGER.debug((Object)"inside distance");
                if ("units".equals(atts.getLocalName(0))) {
                    this.units = atts.getValue(0);
                    LOGGER.debug((Object)("units = " + this.units));
                }
                this.insideDistance = true;
                return;
            }
            catch (IllegalFilterException ife) {
                throw new SAXException("Attempted to construct illegal filter: " + ife.getMessage(), ife);
            }
        } else {
            this.parent.startElement(namespaceURI, localName, qName, atts);
        }
    }

    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        String message = new String(chars, start, length);
        if (this.insideFilter) {
            try {
                if (this.insideDistance) {
                    LOGGER.debug((Object)("calling set distance on " + message + ", " + this.units));
                    this.filterFactory.setDistance(message, this.units);
                }
                LOGGER.debug((Object)("sending to expression factory: " + message));
                this.expressionFactory.message(message);
            }
            catch (IllegalFilterException ife) {
                throw new SAXException(ife);
            }
        } else {
            this.parent.characters(chars, start, length);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        LOGGER.debug((Object)("found start element: " + localName));
        if (localName.equals("Filter")) {
            if (this.isFidFilter && !localName.equals("FeatureId")) {
                this.isFidFilter = false;
                LOGGER.debug((Object)("is fid (2): " + this.isFidFilter));
                try {
                    if (this.isLogicFilter) {
                        this.logicFactory.add(this.filterFactory.create());
                    } else {
                        this.parent.filter(this.filterFactory.create());
                    }
                }
                catch (IllegalFilterException e) {
                    throw new SAXException("Attempted to construct illegal filter: " + e.getMessage());
                }
            }
            this.insideFilter = false;
            return;
        }
        if (this.insideFilter) {
            short filterType = FilterFilter.convertType(localName);
            try {
                if (AbstractFilter.isLogicFilter(filterType)) {
                    LOGGER.debug((Object)"found a logic filter end");
                    if (this.isFidFilter) {
                        this.logicFactory.add(this.filterFactory.create());
                        this.isFidFilter = false;
                    }
                    this.logicFactory.end(filterType);
                    if (!this.logicFactory.isComplete()) return;
                    LOGGER.debug((Object)"creating logic factory");
                    this.parent.filter(this.logicFactory.create());
                    return;
                }
                if (AbstractFilter.isSimpleFilter(filterType) && !this.isFidFilter) {
                    LOGGER.debug((Object)"found a simple filter end");
                    if (this.isLogicFilter) {
                        this.logicFactory.add(this.filterFactory.create());
                        return;
                    }
                    this.parent.filter(this.filterFactory.create());
                    return;
                }
                if (DefaultExpression.isExpression(filterType)) {
                    LOGGER.debug((Object)"found an expression filter end");
                    this.expressionFactory.end(localName);
                    if (!this.expressionFactory.isReady()) return;
                    LOGGER.debug((Object)"expression factory is ready");
                    this.filterFactory.expression(this.expressionFactory.create());
                    return;
                }
                if (!localName.equals("Distance")) return;
                this.insideDistance = false;
                return;
            }
            catch (IllegalFilterException e) {
                throw new SAXException("Attempted to construct illegal filter: " + e.getMessage());
            }
        } else {
            this.parent.endElement(namespaceURI, localName, qName);
        }
    }

    public void geometry(Geometry geometry) throws RuntimeException {
        try {
            LOGGER.debug((Object)("got geometry: " + geometry));
            this.expressionFactory.geometry(geometry);
            if (this.expressionFactory.isReady()) {
                LOGGER.debug((Object)"expression factory made expression and sent to filter factory");
                this.filterFactory.expression(this.expressionFactory.create());
            }
        }
        catch (IllegalFilterException ife) {
            LOGGER.debug((Object)("Had problems adding geometry: " + geometry.toString()));
            throw new RuntimeException("problem adding geometry to filter ", ife);
        }
    }

    protected static short convertType(String filterType) {
        if (filterType.equals("Or")) {
            return 1;
        }
        if (filterType.equals("And")) {
            return 2;
        }
        if (filterType.equals("Not")) {
            return 3;
        }
        if (filterType.equals("Equals")) {
            return 5;
        }
        if (filterType.equals("Disjoint")) {
            return 6;
        }
        if (filterType.equals("DWithin")) {
            return 24;
        }
        if (filterType.equals("Intersects")) {
            return 7;
        }
        if (filterType.equals("Touches")) {
            return 8;
        }
        if (filterType.equals("Crosses")) {
            return 9;
        }
        if (filterType.equals("Within")) {
            return 10;
        }
        if (filterType.equals("Contains")) {
            return 11;
        }
        if (filterType.equals("Overlaps")) {
            return 12;
        }
        if (filterType.equals("Beyond")) {
            return 13;
        }
        if (filterType.equals("BBOX")) {
            return 4;
        }
        if (filterType.equals("PropertyIsEqualTo")) {
            return 14;
        }
        if (filterType.equals("PropertyIsNotEqualTo")) {
            return 23;
        }
        if (filterType.equals("PropertyIsLessThan")) {
            return 15;
        }
        if (filterType.equals("PropertyIsGreaterThan")) {
            return 16;
        }
        if (filterType.equals("PropertyIsLessThanOrEqualTo")) {
            return 17;
        }
        if (filterType.equals("PropertyIsGreaterThanOrEqualTo")) {
            return 18;
        }
        if (filterType.equals("PropertyIsBetween")) {
            return 19;
        }
        if (filterType.equals("PropertyIsLike")) {
            return 20;
        }
        if (filterType.equals("PropertyIsNull")) {
            return 21;
        }
        if (filterType.equals("FeatureId")) {
            return 22;
        }
        if (filterType.equals("Add")) {
            return 105;
        }
        if (filterType.equals("Sub")) {
            return 106;
        }
        if (filterType.equals("Mul")) {
            return 107;
        }
        if (filterType.equals("Div")) {
            return 108;
        }
        if (filterType.equals("PropertyName")) {
            return 101;
        }
        if (filterType.equals("Literal")) {
            return 109;
        }
        if (filterType.equals("Function")) {
            return 114;
        }
        return -1;
    }
}

