/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.function;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;
import org.saig.core.filter.function.FilterFunctionUtils;

public class FilterFunction_in6
extends FunctionExpressionImpl
implements FunctionExpression {
    private Expression[] args;

    @Override
    public String getName() {
        return "in6";
    }

    @Override
    public int getArgCount() {
        return 7;
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
        String result = "in6(";
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
        Object arg6;
        Object arg5;
        Object arg4;
        Object arg3;
        Object arg2;
        Object arg1;
        Object arg0;
        try {
            arg0 = this.args[0].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function in6 argument #0 - expected type Object");
        }
        try {
            arg1 = this.args[1].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function in6 argument #1 - expected type Object");
        }
        try {
            arg2 = this.args[2].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function in6 argument #2 - expected type Object");
        }
        try {
            arg3 = this.args[3].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function in6 argument #3 - expected type Object");
        }
        try {
            arg4 = this.args[4].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function in6 argument #4 - expected type Object");
        }
        try {
            arg5 = this.args[5].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function in6 argument #5 - expected type Object");
        }
        try {
            arg6 = this.args[6].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function in6 argument #6 - expected type Object");
        }
        return new Boolean(FilterFunctionUtils.in6(arg0, arg1, arg2, arg3, arg4, arg5, arg6));
    }
}

