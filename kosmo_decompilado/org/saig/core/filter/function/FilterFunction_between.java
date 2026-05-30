/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.function;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;
import org.saig.core.filter.function.FilterFunctionUtils;

public class FilterFunction_between
extends FunctionExpressionImpl
implements FunctionExpression {
    public static final String NAME = "between";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getArgCount() {
        return 3;
    }

    @Override
    public Object getValue(Feature feature) {
        Object arg2;
        Object arg1;
        Object arg0;
        try {
            arg0 = this.args[0].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function between argument #0 - expected type Object");
        }
        try {
            arg1 = this.args[1].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function between argument #1 - expected type Object");
        }
        try {
            arg2 = this.args[2].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function between argument #2 - expected type Object");
        }
        return FilterFunctionUtils.between(arg0, arg1, arg2);
    }
}

