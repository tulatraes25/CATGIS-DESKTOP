/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.function;

import com.vividsolutions.jump.feature.Feature;
import java.util.Date;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;

public class FilterFunction_currentDate
extends FunctionExpressionImpl
implements FunctionExpression {
    public static final String NAME = "currentDate";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getArgCount() {
        return 0;
    }

    @Override
    public Object getValue(Feature feature) {
        return new Date(System.currentTimeMillis());
    }
}

