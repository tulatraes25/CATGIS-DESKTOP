/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.AbstractBasicFeature;
import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.LiteralExpressionImpl;

public class LabelExpression
extends LiteralExpressionImpl {
    public LabelExpression() {
    }

    public LabelExpression(String expr) {
        super(expr);
    }

    @Override
    public Object getValue(Feature feature) throws IllegalArgumentException {
        return ((AbstractBasicFeature)feature).getExpression((String)this.literal);
    }

    public String getExpression() {
        return (String)this.literal;
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
}

