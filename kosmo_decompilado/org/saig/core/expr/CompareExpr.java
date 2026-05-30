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
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.IllegalFilterException;

class CompareExpr
extends AbstractExpr {
    Expr expr1;
    Expr expr2;
    short op;

    CompareExpr(Expr expr1, short op, Expr expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
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
        Expr eval1 = this.expr1.eval();
        Expr eval2 = this.expr2.eval();
        if (eval1 instanceof ResolvedExpr && eval2 instanceof ResolvedExpr) {
            Comparable value1 = CompareExpr.compare((ResolvedExpr)((Object)eval1));
            Comparable value2 = CompareExpr.compare((ResolvedExpr)((Object)eval2));
            int compare = value1 == null && value2 == null ? 0 : (value1 != null && value2 == null ? 1 : (value1 == null && value2 != null ? -1 : value1.compareTo(value2)));
            switch (this.op) {
                case 14: {
                    return new LiteralExpr(compare == 0);
                }
                case 16: {
                    return new LiteralExpr(compare == 1);
                }
                case 18: {
                    return new LiteralExpr(compare == 1 || compare == 0);
                }
                case 15: {
                    return new LiteralExpr(compare == -1);
                }
                case 17: {
                    return new LiteralExpr(compare == -1 || compare == 1);
                }
                case 23: {
                    return new LiteralExpr(compare == 1 || compare == -1);
                }
            }
        }
        if (eval1 == this.expr1 && eval2 == this.expr2) {
            return this;
        }
        return new CompareExpr(eval1, this.op, eval2);
    }

    @Override
    public Filter filter(FeatureSchema schema) throws IOException {
        try {
            CompareFilter compare = this.factory.createCompareFilter(this.op);
            Expression left = this.expr1.expression(schema);
            Expression right = this.expr2.expression(schema);
            compare.addLeftValue(left);
            compare.addRightValue(right);
            return compare;
        }
        catch (IllegalFilterException e) {
            return null;
        }
    }
}

