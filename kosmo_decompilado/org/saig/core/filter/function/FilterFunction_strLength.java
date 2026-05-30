/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.function;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;
import org.saig.core.filter.function.FilterFunctionUtils;

public class FilterFunction_strLength
extends FunctionExpressionImpl
implements FunctionExpression {
    public static final String NAME = "strLength";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getArgCount() {
        return 1;
    }

    @Override
    public Object getValue(Feature feature) {
        String arg0;
        try {
            arg0 = this.args[0].getValue(feature).toString();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function strLength argument #0 - expected type String");
        }
        return FilterFunctionUtils.strLength(arg0);
    }
}

