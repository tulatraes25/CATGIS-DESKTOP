/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opengis.util.Cloneable
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterVisitor;

public class NoneFilter
implements Filter,
Cloneable {
    @Override
    public final boolean contains(Feature f) {
        return true;
    }

    @Override
    public final Filter or(Filter f) {
        return this;
    }

    @Override
    public final Filter and(Filter f) {
        return f;
    }

    @Override
    public final Filter not() {
        return Filter.ALL;
    }

    @Override
    public final short getFilterType() {
        return 12345;
    }

    @Override
    public final void accept(FilterVisitor v) {
        v.visit(this);
    }

    public final String toString() {
        return "Filter.NONE";
    }

    public Object clone() {
        return new NoneFilter();
    }
}

