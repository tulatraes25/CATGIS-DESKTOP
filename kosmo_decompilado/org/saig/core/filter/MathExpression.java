/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;

public interface MathExpression
extends Expression {
    @Override
    public Object getValue(Feature var1);

    public void addRightValue(Expression var1) throws IllegalFilterException;

    @Override
    public short getType();

    public void setType(short var1);

    public Expression getLeftValue();

    public Expression getRightValue();

    public void addLeftValue(Expression var1) throws IllegalFilterException;
}

