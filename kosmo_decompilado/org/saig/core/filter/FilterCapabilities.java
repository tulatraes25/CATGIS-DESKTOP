/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.saig.core.filter.AbstractFilter;
import org.saig.core.filter.Filter;
import org.saig.core.filter.LogicFilter;

public class FilterCapabilities {
    private List<Short> supportTypes = new ArrayList<Short>();

    public void addType(short type) {
        this.supportTypes.add(new Short(type));
    }

    public boolean supports(short type) {
        return this.supportTypes.contains(new Short(type));
    }

    public boolean supports(Filter filter) {
        short filterType = filter.getFilterType();
        return this.supports(filterType);
    }

    public boolean fullySupports(Filter filter) {
        boolean supports = true;
        if (filter == null) {
            throw new IllegalArgumentException("Null filters can not be unpacked, did you mean Filter.NONE?");
        }
        short filterType = filter.getFilterType();
        if (AbstractFilter.isLogicFilter(filterType)) {
            Iterator<Filter> filters = ((LogicFilter)filter).getFilterIterator();
            Filter testFilter = null;
            while (filters.hasNext()) {
                testFilter = filters.next();
                if (this.fullySupports(testFilter)) continue;
                supports = false;
            }
        } else {
            supports = this.supports(filter);
        }
        return supports;
    }
}

