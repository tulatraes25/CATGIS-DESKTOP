/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.function;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;
import org.saig.core.filter.function.FilterFunctionUtils;

public class FilterFunction_if_then_else
extends FunctionExpressionImpl
implements FunctionExpression {
    private Expression[] args;

    @Override
    public String getName() {
        return "if_then_else";
    }

    @Override
    public int getArgCount() {
        return 3;
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
        String result = "if_then_else(";
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
        Object arg2;
        Object arg1;
        boolean arg0;
        try {
            arg0 = (Boolean)this.args[0].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function if_then_else argument #0 - expected type boolean");
        }
        try {
            arg1 = this.args[1].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function if_then_else argument #1 - expected type Object");
        }
        try {
            arg2 = this.args[2].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function if_then_else argument #2 - expected type Object");
        }
        return FilterFunctionUtils.if_then_else(arg0, arg1, arg2);
    }
}

