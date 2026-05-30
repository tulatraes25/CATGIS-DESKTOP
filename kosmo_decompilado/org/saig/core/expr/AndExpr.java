/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.expr;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.io.IOException;
import org.saig.core.expr.AbstractExpr;
import org.saig.core.expr.Expr;
import org.saig.core.expr.Exprs;
import org.saig.core.expr.LiteralExpr;
import org.saig.core.expr.ResolvedExpr;
import org.saig.core.filter.Filter;

class AndExpr
extends AbstractExpr {
    Expr expr1;
    Expr expr2;

    AndExpr(Expr expr1, Expr expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    @Override
    public Expr eval() {
        Expr eval1 = this.expr1.eval();
        Expr eval2 = this.expr2.eval();
        if (eval1 instanceof ResolvedExpr && eval2 instanceof ResolvedExpr) {
            Object value1 = ((ResolvedExpr)((Object)eval1)).getValue();
            Object value2 = ((ResolvedExpr)((Object)eval1)).getValue();
            boolean and = Exprs.truth(value1) && Exprs.truth(value2);
            return new LiteralExpr(and);
        }
        if (eval1 == this.expr1 && eval2 == this.expr2) {
            return this;
        }
        return new AndExpr(eval1, eval2);
    }

    @Override
    public Filter filter(FeatureSchema schema) throws IOException {
        Filter filter1 = this.expr1.filter(schema);
        Filter filter2 = this.expr2.filter(schema);
        return filter1.and(filter2);
    }
}

