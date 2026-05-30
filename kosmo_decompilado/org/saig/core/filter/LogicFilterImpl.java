/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opengis.util.Cloneable
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opengis.util.Cloneable;
import org.saig.core.filter.AbstractFilterImpl;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LogicFilter;

public class LogicFilterImpl
extends AbstractFilterImpl
implements LogicFilter,
Cloneable {
    private List<Filter> subFilters = new ArrayList<Filter>();

    public LogicFilterImpl(short filterType) throws IllegalFilterException {
        LOGGER.debug((Object)("filtertype " + filterType));
        if (!LogicFilterImpl.isLogicFilter(filterType)) {
            throw new IllegalFilterException("Attempted to create logic filter with non-logic type.");
        }
        this.filterType = filterType;
    }

    public LogicFilterImpl(Filter filter, short filterType) throws IllegalFilterException {
        if (!LogicFilterImpl.isLogicFilter(filterType)) {
            throw new IllegalFilterException("Attempted to create logic filter with non-logic type.");
        }
        this.filterType = filterType;
        this.subFilters.add(filter);
    }

    public LogicFilterImpl(Filter filter1, Filter filter2, short filterType) throws IllegalFilterException {
        if (!LogicFilterImpl.isLogicFilter(filterType)) {
            throw new IllegalFilterException("Attempted to create logic filter with non-logic type.");
        }
        this.filterType = filterType;
        this.subFilters.add(filter1);
        this.addFilter(filter2);
    }

    @Override
    public final void addFilter(Filter filter) throws IllegalFilterException {
        if (this.filterType == 3 && this.subFilters.size() != 0) {
            throw new IllegalFilterException("Attempted to add an more than one filter to a NOT filter.");
        }
        this.subFilters.add(filter);
    }

    @Override
    public Iterator<Filter> getFilterIterator() {
        return this.subFilters.iterator();
    }

    @Override
    public boolean contains(Feature feature) {
        Iterator<Filter> iterator = this.subFilters.iterator();
        boolean contains = false;
        if (!iterator.hasNext()) {
            return false;
        }
        if (this.filterType == 1) {
            contains = false;
            while (iterator.hasNext() && !contains) {
                boolean bl = contains = iterator.next().contains(feature) || contains;
            }
        } else if (this.filterType == 2) {
            contains = true;
            while (iterator.hasNext() && contains) {
                boolean bl = contains = iterator.next().contains(feature) && contains;
            }
        } else if (this.filterType == 3) {
            contains = !this.subFilters.get(0).contains(feature);
        }
        return contains;
    }

    @Override
    public Filter or(Filter filter) {
        if (this.filterType == 1) {
            this.subFilters.add(filter);
            return this;
        }
        return super.or(filter);
    }

    @Override
    public Filter and(Filter filter) {
        if (this.filterType == 2) {
            this.subFilters.add(filter);
            return this;
        }
        return super.and(filter);
    }

    @Override
    public Filter not() {
        if (this.filterType == 3) {
            return this.subFilters.get(0);
        }
        return super.not();
    }

    public List<Filter> getSubFilters() {
        return this.subFilters;
    }

    /*
     * Unable to fully structure code
     */
    public String toString() {
        block2: {
            block1: {
                returnString = "[";
                operator = "";
                iterator = this.subFilters.iterator();
                if (this.filterType != 1) break block1;
                operator = " OR ";
                ** GOTO lbl17
            }
            if (this.filterType != 2) break block2;
            operator = " AND ";
            ** GOTO lbl17
        }
        if (this.filterType != 3) ** GOTO lbl17
        return "NOT " + iterator.next().toString();
lbl-1000:
        // 1 sources

        {
            returnString = String.valueOf(returnString) + iterator.next().toString();
            if (!iterator.hasNext()) continue;
            returnString = String.valueOf(returnString) + operator;
lbl17:
            // 5 sources

            ** while (iterator.hasNext())
        }
lbl18:
        // 1 sources

        return String.valueOf(returnString) + "]";
    }

    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == this.getClass()) {
            LogicFilterImpl logFilter = (LogicFilterImpl)obj;
            LOGGER.debug((Object)("filter type match:" + (logFilter.getFilterType() == this.filterType)));
            LOGGER.debug((Object)("same size:" + (logFilter.getSubFilters().size() == this.subFilters.size()) + "; inner size: " + logFilter.getSubFilters().size() + "; outer size: " + this.subFilters.size()));
            LOGGER.debug((Object)("contains:" + logFilter.getSubFilters().containsAll(this.subFilters)));
            return logFilter.getFilterType() == this.filterType && logFilter.getSubFilters().size() == this.subFilters.size() && logFilter.getSubFilters().containsAll(this.subFilters);
        }
        return false;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + this.filterType;
        result = 37 * result + this.subFilters.hashCode();
        return result;
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        LogicFilterImpl clone = null;
        try {
            clone = new LogicFilterImpl(this.getFilterType());
        }
        catch (IllegalFilterException illegalFilterException) {
            // empty catch block
        }
        if (this.subFilters != null) {
            for (Filter currentFilter : this.subFilters) {
                clone.subFilters.add((Filter)((Cloneable)currentFilter).clone());
            }
        }
        return clone;
    }
}

