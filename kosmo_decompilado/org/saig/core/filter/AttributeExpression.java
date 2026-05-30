/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;

public interface AttributeExpression
extends Expression {
    public void setAttributePath(String var1) throws IllegalFilterException;

    @Override
    public Object getValue(Feature var1);

    public String getAttributePath();
}

