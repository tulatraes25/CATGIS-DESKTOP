/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import java.util.Iterator;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterCapabilities;
import org.saig.core.filter.LogicFilter;

public class SQLUnpacker {
    private FilterPair pair;
    private FilterCapabilities capabilities;

    public SQLUnpacker(FilterCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public void unPackAND(Filter filter) {
        this.pair = this.doUnPack(filter, (short)2);
    }

    public void unPackOR(Filter filter) {
        this.pair = this.doUnPack(filter, (short)1);
    }

    public Filter getUnSupported() {
        return this.pair.getUnSupported();
    }

    public Filter getSupported() {
        return this.pair.getSupported();
    }

    private FilterPair doUnPack(Filter filter, short splitType) {
        Filter subSup = null;
        Filter subUnSup = null;
        Filter retSup = null;
        Filter retUnSup = null;
        if (filter == null) {
            return new FilterPair(null, null);
        }
        if (this.capabilities.fullySupports(filter)) {
            retSup = filter;
        } else {
            short type = filter.getFilterType();
            if (type == splitType && this.capabilities.supports(splitType)) {
                Iterator<Filter> filters = ((LogicFilter)filter).getFilterIterator();
                while (filters.hasNext()) {
                    FilterPair subPair = this.doUnPack(filters.next(), splitType);
                    subSup = subPair.getSupported();
                    subUnSup = subPair.getUnSupported();
                    retSup = this.combineFilters(retSup, subSup, splitType);
                    retUnSup = this.combineFilters(retUnSup, subUnSup, splitType);
                }
            } else if (type == 3 && this.capabilities.supports((short)3)) {
                Iterator<Filter> filters = ((LogicFilter)filter).getFilterIterator();
                FilterPair subPair = this.doUnPack(filters.next(), splitType);
                subSup = subPair.getSupported();
                subUnSup = subPair.getUnSupported();
                if (subSup != null) {
                    retSup = subSup.not();
                }
                if (subUnSup != null) {
                    retUnSup = subUnSup.not();
                }
            } else {
                retUnSup = filter;
            }
        }
        FilterPair retPair = new FilterPair(retSup, retUnSup);
        return retPair;
    }

    private Filter combineFilters(Filter filter1, Filter filter2, short splitType) {
        Filter retFilter = filter1 != null ? (filter2 != null ? (splitType == 2 ? filter1.and(filter2) : filter1.or(filter2)) : filter1) : (filter2 != null ? filter2 : null);
        return retFilter;
    }

    private class FilterPair {
        private Filter supported;
        private Filter unSupported;

        public FilterPair(Filter supported, Filter unSupported) {
            this.supported = supported;
            this.unSupported = unSupported;
        }

        public Filter getSupported() {
            return this.supported;
        }

        public Filter getUnSupported() {
            return this.unSupported;
        }
    }
}

