/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import java.util.Iterator;
import org.saig.core.filter.Filter;
import org.saig.core.filter.IllegalFilterException;

public interface LogicFilter
extends Filter {
    @Override
    public boolean contains(Feature var1);

    @Override
    public Filter not();

    @Override
    public Filter and(Filter var1);

    public Iterator<Filter> getFilterIterator();

    @Override
    public Filter or(Filter var1);

    public void addFilter(Filter var1) throws IllegalFilterException;
}

