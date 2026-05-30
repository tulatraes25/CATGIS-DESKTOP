/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.function;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;
import org.saig.core.filter.function.FilterFunctionUtils;

public class FilterFunction_strEqualsIgnoreCase
extends FunctionExpressionImpl
implements FunctionExpression {
    private Expression[] args;

    @Override
    public String getName() {
        return "strEqualsIgnoreCase";
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
        String result = "strEqualsIgnoreCase(";
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
        String arg1;
        String arg0;
        try {
            arg0 = this.args[0].getValue(feature).toString();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function strEqualsIgnoreCase argument #0 - expected type String");
        }
        try {
            arg1 = this.args[1].getValue(feature).toString();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function strEqualsIgnoreCase argument #1 - expected type String");
        }
        return new Boolean(FilterFunctionUtils.strEqualsIgnoreCase(arg0, arg1));
    }
}

