/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.DefaultExpression;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.MapScaleDenominator;

public class MapScaleDenominatorImpl
extends DefaultExpression
implements MapScaleDenominator {
    @Override
    public Object getValue(Feature f) {
        return new Double(1.0);
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }

    public String toString() {
        return "sld:MapScaleDenominator";
    }
}

