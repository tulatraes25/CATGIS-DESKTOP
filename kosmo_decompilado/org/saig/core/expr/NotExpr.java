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

class NotExpr
extends AbstractExpr {
    Expr expr;

    NotExpr(Expr expr) {
        this.expr = expr;
    }

    @Override
    public Expr eval() {
        Expr eval = this.expr.eval();
        if (eval instanceof ResolvedExpr) {
            Object value = ((ResolvedExpr)((Object)eval)).getValue();
            boolean not = !Exprs.truth(value);
            return new LiteralExpr(not);
        }
        if (eval == this.expr) {
            return this;
        }
        return new NotExpr(eval);
    }

    @Override
    public Filter filter(FeatureSchema schema) throws IOException {
        return this.expr.filter(schema).not();
    }
}

