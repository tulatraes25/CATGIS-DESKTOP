/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.saig.core.filter.AbstractFilter;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LogicFilter;

public class LogicSAXParser {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");
    private static final FilterFactory FILTER_FACT = FilterFactory.createFilterFactory();
    private short logicType = (short)-1;
    private List<Filter> subFilters = new ArrayList<Filter>();
    private LogicSAXParser logicFactory = null;
    private boolean isComplete = false;

    public LogicSAXParser() {
        LOGGER.finer("made new logic factory");
    }

    public void start(short logicType) throws IllegalFilterException {
        LOGGER.finest("got a start element: " + logicType);
        if (this.logicType != -1) {
            if (this.logicFactory == null) {
                this.logicFactory = new LogicSAXParser();
            }
            this.logicFactory.start(logicType);
        } else {
            if (!AbstractFilter.isLogicFilter(logicType)) {
                throw new IllegalFilterException("Add logic filter type does not match declared type.");
            }
            this.logicType = logicType;
        }
    }

    public void end(short logicType) throws IllegalFilterException {
        LOGGER.finer("got an end element: " + logicType);
        if (this.logicFactory != null) {
            LOGGER.finer("sending end element to nested logic filter: " + logicType);
            this.logicFactory.end(logicType);
            if (this.logicFactory.isComplete()) {
                this.subFilters.add(this.logicFactory.create());
                this.logicFactory = null;
            }
        } else if (this.logicType == logicType) {
            LOGGER.finer("end element matched internal type: " + this.logicType);
            this.isComplete = true;
        } else {
            throw new IllegalFilterException("Logic Factory got an end message that it can't process.");
        }
    }

    public void add(Filter filter) {
        LOGGER.finer("added a filter: " + filter.toString());
        if (this.logicFactory != null) {
            LOGGER.finer("adding to nested logic filter: " + filter.toString());
            this.logicFactory.add(filter);
        } else {
            LOGGER.finer("added to sub filters: " + filter.toString());
            this.subFilters.add(filter);
        }
    }

    public Filter create() throws IllegalFilterException {
        LogicFilter filter = null;
        LOGGER.finer("creating a logic filter");
        if (this.isComplete()) {
            LOGGER.finer("filter is complete, with type: " + this.logicType);
            if (this.logicType == 3) {
                filter = FILTER_FACT.createLogicFilter(this.subFilters.get(0), this.logicType);
            } else {
                filter = FILTER_FACT.createLogicFilter(this.logicType);
                Iterator<Filter> iterator = this.subFilters.iterator();
                while (iterator.hasNext()) {
                    filter.addFilter(iterator.next());
                }
            }
            this.subFilters = new ArrayList<Filter>();
            this.logicType = (short)-1;
            this.isComplete = false;
            return filter;
        }
        throw new IllegalFilterException("Attempted to generate incomplete logic filter.");
    }

    public boolean isComplete() {
        return this.isComplete;
    }
}

