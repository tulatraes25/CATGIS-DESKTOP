/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.function;

import com.vividsolutions.jump.feature.Feature;
import java.util.Date;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;
import org.saig.core.filter.function.FilterFunctionUtils;

public class FilterFunction_daysFrom
extends FunctionExpressionImpl
implements FunctionExpression {
    public static final String NAME = "daysFrom";

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
        Date startDate = this.getDate(0, feature);
        Date endDate = this.getDate(1, feature);
        return new Integer(FilterFunctionUtils.daysFrom(startDate, endDate));
    }

    private Date getDate(int i, Feature feature) {
        try {
            Date date = (Date)this.args[i].getValue(feature);
            return date;
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Filter Function problem for function daysFrom argument #" + i + " - " + "Argument is not a valid date");
        }
    }
}

