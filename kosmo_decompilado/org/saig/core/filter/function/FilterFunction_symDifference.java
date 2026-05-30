/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.filter.function;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;
import org.saig.core.filter.function.FilterFunctionUtils;

public class FilterFunction_symDifference
extends FunctionExpressionImpl
implements FunctionExpression {
    private Expression[] args;

    @Override
    public String getName() {
        return "symDifference";
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
        String result = "symDifference(";
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
        Geometry arg1;
        Geometry arg0;
        try {
            arg0 = (Geometry)this.args[0].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function symDifference argument #0 - expected type Geometry");
        }
        try {
            arg1 = (Geometry)this.args[1].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function symDifference argument #1 - expected type Geometry");
        }
        return FilterFunctionUtils.symDifference(arg0, arg1);
    }
}

