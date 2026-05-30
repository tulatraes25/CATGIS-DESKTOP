/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.function;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;
import org.saig.core.filter.function.FilterFunctionUtils;

public class FilterFunction_double2bool
extends FunctionExpressionImpl
implements FunctionExpression {
    private Expression[] args;

    @Override
    public String getName() {
        return "double2bool";
    }

    @Override
    public int getArgCount() {
        return 1;
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
        String result = "double2bool(";
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
        double arg0;
        try {
            arg0 = ((Number)this.args[0].getValue(feature)).doubleValue();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function double2bool argument #0 - expected type double");
        }
        return new Boolean(FilterFunctionUtils.double2bool(arg0));
    }
}

