/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.expr;

import org.saig.core.expr.Expr;

public interface MathExpr
extends Expr {
    public MathExpr add(MathExpr var1);

    public MathExpr add(Number var1);

    public MathExpr add(double var1);

    public MathExpr subtract(MathExpr var1);

    public MathExpr subtract(Number var1);

    public MathExpr subtract(double var1);

    public MathExpr divide(MathExpr var1);

    public MathExpr divide(Number var1);

    public MathExpr divide(double var1);

    public MathExpr multiply(MathExpr var1);

    public MathExpr multiply(Number var1);

    public MathExpr multiply(double var1);

    public Expr eq(Number var1);

    public Expr eq(double var1);

    public Expr gt(Number var1);

    public Expr gt(double var1);

    public Expr gte(Number var1);

    public Expr gte(double var1);

    public Expr lt(Number var1);

    public Expr lt(double var1);

    public Expr lte(Number var1);

    public Expr lte(double var1);

    public Expr ne(Number var1);

    public Expr ne(double var1);

    public Expr between(Number var1, Number var2);

    public Expr between(double var1, double var3);
}

