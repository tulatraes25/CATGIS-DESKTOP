/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.expr;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.io.IOException;
import org.saig.core.expr.AbstractExpr;
import org.saig.core.expr.Expr;
import org.saig.core.expr.LiteralExpr;
import org.saig.core.expr.ResolvedExpr;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.IllegalFilterException;

class BetweenExpr
extends AbstractExpr {
    Expr expr;
    Expr min;
    Expr max;

    BetweenExpr(Expr min, Expr expr, Expr max) {
        this.expr = expr;
        this.min = min;
        this.max = max;
    }

    static final Comparable compare(ResolvedExpr expr) {
        Object value = expr.getValue();
        if (value instanceof Comparable) {
            return (Comparable)value;
        }
        return null;
    }

    @Override
    public Expr eval() {
        Expr evalMin = this.min.eval();
        Expr eval = this.expr.eval();
        Expr evalMax = this.max.eval();
        if (evalMin instanceof ResolvedExpr && eval instanceof ResolvedExpr && evalMax instanceof ResolvedExpr) {
            boolean gt;
            Comparable lower = BetweenExpr.compare((ResolvedExpr)((Object)evalMin));
            Comparable value = BetweenExpr.compare((ResolvedExpr)((Object)eval));
            Comparable upper = BetweenExpr.compare((ResolvedExpr)((Object)evalMax));
            if (value == null) {
                return new LiteralExpr(false);
            }
            boolean bl = lower == null ? true : (gt = value.compareTo(lower) >= 0);
            boolean lt = upper == null ? true : value.compareTo(upper) <= 0;
            return new LiteralExpr(gt && lt);
        }
        if (evalMin == this.min && eval == this.expr && evalMax == this.max) {
            return this;
        }
        return new BetweenExpr(evalMin, eval, evalMax);
    }

    @Override
    public Filter filter(FeatureSchema schema) throws IOException {
        try {
            BetweenFilter between = this.factory.createBetweenFilter();
            Expression expression = this.expr.expression(schema);
            Expression left = this.min.expression(schema);
            Expression right = this.max.expression(schema);
            between.addMiddleValue(expression);
            between.addLeftValue(left);
            between.addRightValue(right);
            return between;
        }
        catch (IllegalFilterException e) {
            return null;
        }
    }
}

