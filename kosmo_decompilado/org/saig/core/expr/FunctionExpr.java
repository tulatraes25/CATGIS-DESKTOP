/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.expr;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.io.IOException;
import org.saig.core.expr.AbstractExpr;
import org.saig.core.expr.Expr;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FunctionExpression;

public class FunctionExpr
extends AbstractExpr {
    Expr[] expr;
    String name;

    public FunctionExpr(String name, Expr expr) {
        this.name = name;
        this.expr = new Expr[]{expr};
    }

    public FunctionExpr(String name, Expr expr1, Expr expr2) {
        this.name = name;
        this.expr = new Expr[]{expr1, expr2};
    }

    public FunctionExpr(String name, Expr[] expr) {
        this.name = name;
        this.expr = expr;
    }

    @Override
    public Expression expression(FeatureSchema schema) throws IOException {
        Expression[] args = new Expression[this.expr.length];
        int i = 0;
        while (i < this.expr.length) {
            args[i] = this.expr[i].expression(schema);
            ++i;
        }
        FunctionExpression fn = this.factory.createFunctionExpression(this.name, args);
        return fn;
    }
}

