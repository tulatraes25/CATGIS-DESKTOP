/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;

public class MaxFunction
extends FunctionExpressionImpl
implements FunctionExpression {
    private Expression expA;
    private Expression expB;
    private Expression[] args;

    @Override
    public Object getValue(Feature feature) {
        double first = ((Number)this.expA.getValue(feature)).doubleValue();
        double second = ((Number)this.expB.getValue(feature)).doubleValue();
        return new Double(Math.max(first, second));
    }

    @Override
    public int getArgCount() {
        return 2;
    }

    @Override
    public String getName() {
        return "Max";
    }

    @Override
    public void setArgs(Expression[] args) {
        this.expA = args[0];
        this.expB = args[1];
        this.args = args;
    }

    @Override
    public Expression[] getArgs() {
        return this.args;
    }

    @Override
    public String toString() {
        return "Max( " + this.expA + ", " + this.expB + ")";
    }
}

