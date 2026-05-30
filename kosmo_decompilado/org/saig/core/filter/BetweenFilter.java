/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.Expression;

public interface BetweenFilter
extends CompareFilter {
    @Override
    public boolean contains(Feature var1);

    public Expression getMiddleValue();

    public void addMiddleValue(Expression var1);
}

