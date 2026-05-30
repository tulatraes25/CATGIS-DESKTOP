/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.expr;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.io.IOException;
import org.saig.core.expr.AbstractMathExpr;
import org.saig.core.expr.Expr;
import org.saig.core.expr.LiteralMathExpr;
import org.saig.core.expr.MathExpr;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.MathExpression;

class ArithmaticMathExpr
extends AbstractMathExpr {
    MathExpr expr1;
    MathExpr expr2;
    short op;

    ArithmaticMathExpr(MathExpr expr1, short op, MathExpr expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
        this.op = op;
    }

    @Override
    public Expr eval() {
        MathExpr eval1 = (MathExpr)this.expr1.eval();
        MathExpr eval2 = (MathExpr)this.expr2.eval();
        if (eval1 instanceof LiteralMathExpr && eval2 instanceof LiteralMathExpr) {
            double number1 = ((LiteralMathExpr)eval1).toDouble();
            double number2 = ((LiteralMathExpr)eval2).toDouble();
            switch (this.op) {
                case 105: {
                    return new LiteralMathExpr(number1 + number2);
                }
                case 106: {
                    return new LiteralMathExpr(number1 - number2);
                }
                case 107: {
                    return new LiteralMathExpr(number1 * number2);
                }
                case 108: {
                    return new LiteralMathExpr(number1 / number2);
                }
            }
        }
        if (eval1 == this.expr1 && eval2 == this.expr2) {
            return this;
        }
        return new ArithmaticMathExpr(eval1, this.op, eval2);
    }

    @Override
    public Expression expression(FeatureSchema schema) throws IOException {
        try {
            MathExpression math = this.factory.createMathExpression(this.op);
            Expression left = this.expr1.expression(schema);
            Expression right = this.expr2.expression(schema);
            math.addLeftValue(left);
            math.addRightValue(right);
            return math;
        }
        catch (IllegalFilterException e) {
            return null;
        }
    }
}

