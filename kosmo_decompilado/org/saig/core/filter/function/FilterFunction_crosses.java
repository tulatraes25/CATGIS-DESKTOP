/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.filter.function;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;
import org.saig.core.filter.function.FilterFunctionUtils;

public class FilterFunction_crosses
extends FunctionExpressionImpl
implements FunctionExpression {
    public static final String NAME = "crosses";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getArgCount() {
        return 2;
    }

    @Override
    public Object getValue(Feature feature) {
        Geometry arg1;
        Geometry arg0;
        try {
            arg0 = (Geometry)this.args[0].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function crosses argument #0 - expected type Geometry");
        }
        try {
            arg1 = (Geometry)this.args[1].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function crosses argument #1 - expected type Geometry");
        }
        return FilterFunctionUtils.crosses(arg0, arg1);
    }
}

