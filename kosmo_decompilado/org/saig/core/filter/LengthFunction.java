/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FunctionExpressionImpl;

public class LengthFunction
extends FunctionExpressionImpl {
    private AttributeExpression ae;

    @Override
    public String getName() {
        return "length";
    }

    @Override
    public void setArgs(Expression[] args) {
        this.ae = (AttributeExpression)args[0];
    }

    @Override
    public int getArgCount() {
        return this.ae == null ? 0 : 1;
    }

    @Override
    public Expression[] getArgs() {
        return null;
    }

    @Override
    public Object getValue(Feature feature) {
        return new Integer(this.ae.getValue(feature).toString().length());
    }

    @Override
    public String toString() {
        return "Length [" + this.ae.toString() + "]";
    }
}

