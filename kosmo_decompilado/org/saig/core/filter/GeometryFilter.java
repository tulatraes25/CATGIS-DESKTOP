/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.IllegalFilterException;

public interface GeometryFilter
extends Filter {
    public void addRightGeometry(Expression var1) throws IllegalFilterException;

    public void addLeftGeometry(Expression var1) throws IllegalFilterException;

    @Override
    public boolean contains(Feature var1);

    public Expression getRightGeometry();

    public Expression getLeftGeometry();
}

