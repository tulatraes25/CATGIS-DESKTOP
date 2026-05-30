/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.function;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;
import org.saig.core.filter.function.FilterFunctionUtils;

public class FilterFunction_lessEqualThan
extends FunctionExpressionImpl
implements FunctionExpression {
    private Expression[] args;

    @Override
    public String getName() {
        return "lessEqualThan";
    }

    @Override
    public int getArgCount() {
        return 2;
    }

    @Override
    public void setArgs(Expression[] args) {
        this.args = args;
    }

    @Override
    public Expression[] getArgs() {
        return this.args;
    }

    @Override
    public String toString() {
        String result = "lessEqualThan(";
        int t = 0;
        while (t < this.args.length) {
            result = String.valueOf(result) + this.args[t] + ",";
            ++t;
        }
        result = String.valueOf(result) + ")";
        return result;
    }

    @Override
    public Object getValue(Feature feature) {
        Object arg1;
        Object arg0;
        try {
            arg0 = this.args[0].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function lessEqualThan argument #0 - expected type Object");
        }
        try {
            arg1 = this.args[1].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function lessEqualThan argument #1 - expected type Object");
        }
        return new Boolean(FilterFunctionUtils.lessEqualThan(arg0, arg1));
    }
}

