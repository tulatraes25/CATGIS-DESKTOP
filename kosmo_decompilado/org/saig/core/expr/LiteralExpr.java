/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.expr;

import com.vividsolutions.jump.feature.FeatureSchema;
import org.saig.core.expr.AbstractExpr;
import org.saig.core.expr.ResolvedExpr;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;

public class LiteralExpr
extends AbstractExpr
implements ResolvedExpr {
    Object literal;

    public LiteralExpr(boolean b) {
        this(b ? Boolean.TRUE : Boolean.FALSE);
    }

    public LiteralExpr(Object value) {
        this.literal = value;
    }

    @Override
    public Expression expression(FeatureSchema schema) {
        try {
            return this.factory.createLiteralExpression(this.literal);
        }
        catch (IllegalFilterException e) {
            return null;
        }
    }

    @Override
    public Object getValue() {
        return this.literal;
    }
}

