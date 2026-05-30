/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.IllegalFilterException;

public interface CompareFilter
extends Filter {
    public void addLeftValue(Expression var1) throws IllegalFilterException;

    public void addRightValue(Expression var1) throws IllegalFilterException;

    public Expression getLeftValue();

    public Expression getRightValue();

    @Override
    public boolean contains(Feature var1);
}

