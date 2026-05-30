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

public class FilterFunction_bufferWithSegments
extends FunctionExpressionImpl
implements FunctionExpression {
    public static final String NAME = "bufferWithSegments";

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
        int arg2;
        double arg1;
        Geometry arg0;
        try {
            arg0 = (Geometry)this.args[0].getValue(feature);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function bufferWithSegments argument #0 - expected type Geometry");
        }
        try {
            arg1 = ((Number)this.args[1].getValue(feature)).doubleValue();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function bufferWithSegments argument #1 - expected type double");
        }
        try {
            arg2 = ((Number)this.args[2].getValue(feature)).intValue();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function bufferWithSegments argument #2 - expected type int");
        }
        return FilterFunctionUtils.bufferWithSegments(arg0, arg1, arg2);
    }
}

