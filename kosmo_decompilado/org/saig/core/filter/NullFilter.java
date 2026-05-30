/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.IllegalFilterException;

public interface NullFilter
extends Filter {
    public void setNullCheckValue(Expression var1) throws IllegalFilterException;

    public Expression getNullCheckValue();

    @Override
    public boolean contains(Feature var1);
}

