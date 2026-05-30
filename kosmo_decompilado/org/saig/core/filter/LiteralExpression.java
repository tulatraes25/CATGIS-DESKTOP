/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;

public interface LiteralExpression
extends Expression {
    public void setLiteral(Object var1) throws IllegalFilterException;

    @Override
    public Object getValue(Feature var1);

    @Override
    public short getType();

    public Object getLiteral();
}

