/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.apache.log4j.Logger;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterVisitor;

public abstract class AbstractFilter
implements Filter {
    protected static final Logger LOGGER = Logger.getLogger(AbstractFilter.class);
    protected short filterType;
    protected boolean permissiveConstruction = true;

    @Override
    public abstract boolean contains(Feature var1);

    protected static boolean isLogicFilter(short filterType) {
        return filterType == 1 || filterType == 2 || filterType == 3;
    }

    protected static boolean isMathFilter(short filterType) {
        return filterType == 15 || filterType == 16 || filterType == 17 || filterType == 18;
    }

    protected static boolean isCompareFilter(short filterType) {
        return AbstractFilter.isMathFilter(filterType) || filterType == 14 || filterType == 19 || filterType == 23;
    }

    protected static boolean isGeometryFilter(short filterType) {
        return filterType == 4 || filterType == 5 || filterType == 6 || filterType == 8 || filterType == 7 || filterType == 9 || filterType == 10 || filterType == 11 || filterType == 12 || filterType == 24 || filterType == 13;
    }

    protected static boolean isGeometryDistanceFilter(short filterType) {
        return filterType == 24 || filterType == 13;
    }

    protected static boolean isSimpleFilter(short filterType) {
        return AbstractFilter.isCompareFilter(filterType) || AbstractFilter.isGeometryFilter(filterType) || filterType == 21 || filterType == 22 || filterType == 20;
    }

    @Override
    public short getFilterType() {
        return this.filterType;
    }

    @Override
    public abstract void accept(FilterVisitor var1);
}

