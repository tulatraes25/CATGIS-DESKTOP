/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.saig.core.filter.AbstractFilter;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.GeometryDistanceFilter;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.NullFilter;
import org.xml.sax.Attributes;

public class FilterSAXParser {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");
    private static final FilterFactory FILTER_FACT = FilterFactory.createFilterFactory();
    private static final int NUM_LIKE_ATTS = 3;
    private Filter curFilter = null;
    private String curState = "uninitialized";
    private short filterType;
    private Map<String, String> attributes = new HashMap<String, String>();

    public void start(short filterType) throws IllegalFilterException {
        LOGGER.finest("starting filter type " + filterType);
        if (filterType == 22 && !this.curState.equals("fid")) {
            LOGGER.finer("creating the FID filter");
            this.curFilter = FILTER_FACT.createFidFilter();
        } else if (AbstractFilter.isGeometryDistanceFilter(filterType)) {
            this.curFilter = FILTER_FACT.createGeometryDistanceFilter(filterType);
        } else if (AbstractFilter.isGeometryFilter(filterType)) {
            this.curFilter = FILTER_FACT.createGeometryFilter(filterType);
        } else if (filterType == 19) {
            this.curFilter = FILTER_FACT.createBetweenFilter();
        } else if (filterType == 21) {
            this.curFilter = FILTER_FACT.createNullFilter();
        } else if (filterType == 20) {
            this.curFilter = FILTER_FACT.createLikeFilter();
        } else if (AbstractFilter.isCompareFilter(filterType)) {
            this.curFilter = FILTER_FACT.createCompareFilter(filterType);
        } else {
            throw new IllegalFilterException("Attempted to start a new filter with invalid type: " + filterType);
        }
        this.curState = FilterSAXParser.setInitialState(filterType);
        this.filterType = filterType;
        this.attributes = new HashMap<String, String>();
    }

    public void value(String message) throws IllegalFilterException {
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void expression(Expression expression) throws IllegalFilterException {
        if (this.filterType == 19) {
            if (this.curState.equals("attribute")) {
                ((BetweenFilter)this.curFilter).addMiddleValue(expression);
                this.curState = "LowerBoundary";
            } else if (this.curState.equals("LowerBoundary")) {
                ((BetweenFilter)this.curFilter).addLeftValue(expression);
                this.curState = "UpperBoundary";
            } else {
                if (!this.curState.equals("UpperBoundary")) throw new IllegalFilterException("Got expression for Between Filter in illegal state: " + this.curState);
                ((BetweenFilter)this.curFilter).addRightValue(expression);
                this.curState = "complete";
            }
        } else if (AbstractFilter.isCompareFilter(this.filterType)) {
            if (this.curState.equals("leftValue")) {
                ((CompareFilter)this.curFilter).addLeftValue(expression);
                this.curState = "rightValue";
            } else {
                if (!this.curState.equals("rightValue")) throw new IllegalFilterException("Got expression for Compare Filter in illegal state: " + this.curState);
                ((CompareFilter)this.curFilter).addRightValue(expression);
                this.curState = "complete";
            }
        } else if (this.filterType == 21) {
            if (!this.curState.equals("attribute")) throw new IllegalFilterException("Got expression for Null Filter in illegal state: " + this.curState);
            ((NullFilter)this.curFilter).setNullCheckValue(expression);
            this.curState = "complete";
        } else if (AbstractFilter.isGeometryFilter(this.filterType)) {
            if (this.curState.equals("leftValue")) {
                ((GeometryFilter)this.curFilter).addLeftGeometry(expression);
                this.curState = "rightValue";
            } else {
                if (!this.curState.equals("rightValue")) throw new IllegalFilterException("Got expression for Geometry Filter in illegal state: " + this.curState);
                ((GeometryFilter)this.curFilter).addRightGeometry(expression);
                this.curState = AbstractFilter.isGeometryDistanceFilter(this.filterType) ? "distance" : "complete";
                LOGGER.finer("expression called on geometry, curState = " + this.curState);
            }
        } else if (this.filterType == 20) {
            if (this.curState.equals("attribute")) {
                ((LikeFilter)this.curFilter).setValue(expression);
                this.curState = "pattern";
            } else {
                if (!this.curState.equals("pattern")) throw new IllegalFilterException("Got expression for Like Filter in illegal state: " + this.curState);
                if (this.attributes.size() != 3) {
                    throw new IllegalFilterException("Got wrong number of attributes (expecting 3): " + this.attributes.size() + "\n" + this.attributes);
                }
                String wildcard = this.attributes.get("wildCard");
                String singleChar = this.attributes.get("singleChar");
                String escapeChar = this.attributes.get("escape");
                LOGGER.fine("escape char is " + escapeChar);
                if (escapeChar == null) {
                    escapeChar = this.attributes.get("escapeChar");
                }
                LOGGER.fine("if null get new : " + escapeChar);
                ((LikeFilter)this.curFilter).setPattern(expression, wildcard, singleChar, escapeChar);
                this.curState = "complete";
            }
        }
        LOGGER.finer("current state (end): " + this.curState);
    }

    public Filter create() throws IllegalFilterException {
        if (this.isComplete()) {
            LOGGER.finer("complete called, state = " + this.curState);
            this.curState = "complete";
            return this.curFilter;
        }
        throw new IllegalFilterException("Got to the end state of an incomplete filter, current state is " + this.curState);
    }

    private static String setInitialState(short filterType) throws IllegalFilterException {
        if (filterType == 19 || filterType == 21 || filterType == 20) {
            return "attribute";
        }
        if (filterType == 22) {
            return "fid";
        }
        if (AbstractFilter.isCompareFilter(filterType) || AbstractFilter.isGeometryFilter(filterType)) {
            return "leftValue";
        }
        throw new IllegalFilterException("Filter type: " + filterType + " is not recognized");
    }

    public void setDistance(String distance, String units) throws IllegalFilterException {
        LOGGER.finer("set distance called, current state is " + this.curState);
        if (this.curState.equals("distance")) {
            try {
                double distDouble = Double.parseDouble(distance);
                ((GeometryDistanceFilter)this.curFilter).setDistance(distDouble);
                this.curState = "complete";
            }
            catch (NumberFormatException nfe) {
                throw new IllegalFilterException("could not parse distance: " + distance + " to a double");
            }
        } else {
            throw new IllegalFilterException("Got distance for Geometry Distance Filter in illegal state: " + this.curState + ", geometry and property should be set first");
        }
    }

    public void setAttributes(Attributes atts) {
        LOGGER.finer("got attribute: " + atts.getLocalName(0) + ", " + atts.getValue(0));
        LOGGER.finer("current state: " + this.curState);
        if (this.curState.equals("fid")) {
            LOGGER.finer("is a fid");
            ((FidFilter)this.curFilter).addFid(atts.getValue(0));
            LOGGER.finer("added fid");
        } else {
            int i = 0;
            while (i < atts.getLength()) {
                this.attributes.put(atts.getLocalName(i), atts.getValue(i));
                ++i;
            }
        }
    }

    private boolean isComplete() {
        return this.curState.equals("complete") || this.curState.equals("fid");
    }
}

