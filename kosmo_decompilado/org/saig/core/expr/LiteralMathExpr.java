/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.expr;

import com.vividsolutions.jump.feature.FeatureSchema;
import org.saig.core.expr.AbstractMathExpr;
import org.saig.core.expr.ResolvedExpr;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;

public class LiteralMathExpr
extends AbstractMathExpr
implements ResolvedExpr {
    Number number;

    public LiteralMathExpr(int number) {
        this.number = new Integer(number);
    }

    public LiteralMathExpr(double number) {
        this.number = new Double(number);
    }

    public LiteralMathExpr(Number number) {
        this.number = number;
    }

    @Override
    public Expression expression(FeatureSchema schema) {
        try {
            return this.factory.createLiteralExpression(this.number);
        }
        catch (IllegalFilterException e) {
            return null;
        }
    }

    @Override
    public Object getValue() {
        return this.number;
    }

    public Number getNumber() {
        return this.number;
    }

    public double toDouble() {
        if (this.number == null) {
            return Double.NaN;
        }
        return this.number.doubleValue();
    }
}

