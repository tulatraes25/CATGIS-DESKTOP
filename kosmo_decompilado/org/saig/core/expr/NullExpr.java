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
import org.saig.core.filter.Filter;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.NullFilter;

class NullExpr
extends AbstractExpr {
    Expr expr;

    NullExpr(Expr expr) {
        this.expr = expr;
    }

    @Override
    public Expr eval() {
        Expr eval = this.expr.eval();
        if (eval instanceof ResolvedExpr) {
            Object value = ((ResolvedExpr)((Object)eval)).getValue();
            return new LiteralExpr(value != null);
        }
        if (eval == this.expr) {
            return this;
        }
        return new NullExpr(eval);
    }

    @Override
    public Filter filter(FeatureSchema schema) throws IOException {
        try {
            NullFilter nullFilter = this.factory.createNullFilter();
            nullFilter.setNullCheckValue(this.expr.expression(schema));
            return nullFilter;
        }
        catch (IllegalFilterException e) {
            return null;
        }
    }
}

