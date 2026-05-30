/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.expr;

import org.saig.core.expr.AbstractExpr;
import org.saig.core.expr.ArithmaticMathExpr;
import org.saig.core.expr.Expr;
import org.saig.core.expr.Exprs;
import org.saig.core.expr.MathExpr;

public class AbstractMathExpr
extends AbstractExpr
implements MathExpr {
    @Override
    public MathExpr add(MathExpr expr) {
        return new ArithmaticMathExpr(this, 105, expr);
    }

    @Override
    public MathExpr add(Number number) {
        return this.add(Exprs.literal(number));
    }

    @Override
    public MathExpr add(double number) {
        return this.add(Exprs.literal(number));
    }

    @Override
    public MathExpr subtract(MathExpr expr) {
        return new ArithmaticMathExpr(this, 106, expr);
    }

    @Override
    public MathExpr subtract(Number number) {
        return this.subtract(Exprs.literal(number));
    }

    @Override
    public MathExpr subtract(double number) {
        return this.subtract(Exprs.literal(number));
    }

    @Override
    public MathExpr divide(MathExpr expr) {
        return new ArithmaticMathExpr(this, 108, expr);
    }

    @Override
    public MathExpr divide(Number number) {
        return this.divide(Exprs.literal(number));
    }

    @Override
    public MathExpr divide(double number) {
        return this.divide(Exprs.literal(number));
    }

    @Override
    public MathExpr multiply(MathExpr expr) {
        return new ArithmaticMathExpr(this, 107, expr);
    }

    @Override
    public MathExpr multiply(Number number) {
        return this.multiply(Exprs.literal(number));
    }

    @Override
    public MathExpr multiply(double number) {
        return this.multiply(Exprs.literal(number));
    }

    @Override
    public Expr eq(Number number) {
        return this.eq(Exprs.literal(number));
    }

    @Override
    public Expr eq(double number) {
        return this.eq(Exprs.literal(number));
    }

    @Override
    public Expr gt(Number number) {
        return this.gt(Exprs.literal(number));
    }

    @Override
    public Expr gt(double number) {
        return this.gt(Exprs.literal(number));
    }

    @Override
    public Expr gte(Number number) {
        return this.gte(Exprs.literal(number));
    }

    @Override
    public Expr gte(double number) {
        return this.gte(Exprs.literal(number));
    }

    @Override
    public Expr lt(Number number) {
        return this.lt(Exprs.literal(number));
    }

    @Override
    public Expr lt(double number) {
        return this.lt(Exprs.literal(number));
    }

    @Override
    public Expr lte(Number number) {
        return this.lte(Exprs.literal(number));
    }

    @Override
    public Expr lte(double number) {
        return this.lte(Exprs.literal(number));
    }

    @Override
    public Expr ne(Number number) {
        return this.ne(Exprs.literal(number));
    }

    @Override
    public Expr ne(double number) {
        return this.ne(Exprs.literal(number));
    }

    @Override
    public Expr between(Number min, Number max) {
        return this.between(Exprs.literal(min), Exprs.literal(max));
    }

    @Override
    public Expr between(double min, double max) {
        return this.between(Exprs.literal(min), Exprs.literal(max));
    }
}

