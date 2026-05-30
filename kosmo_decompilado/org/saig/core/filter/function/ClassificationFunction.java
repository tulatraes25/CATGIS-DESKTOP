/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.function;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;
import org.saig.core.filter.LiteralExpression;

public abstract class ClassificationFunction
extends FunctionExpressionImpl
implements FunctionExpression {
    int classNum;
    Expression expr;

    @Override
    public int getArgCount() {
        return 2;
    }

    public int getNumberOfClasses() {
        return this.classNum;
    }

    public void setNumberOfClasses(int i) {
        this.classNum = i;
    }

    public Expression getExpression() {
        return this.expr;
    }

    public void setExpression(Expression e) {
        this.expr = e;
    }

    @Override
    public Expression[] getArgs() {
        Expression[] ret = new Expression[2];
        ret[0] = this.expr;
        FilterFactory ff = FilterFactory.createFilterFactory();
        ret[1] = ff.createLiteralExpression(this.classNum);
        return ret;
    }

    @Override
    public abstract String getName();

    @Override
    public void setArgs(Expression[] args) {
        this.expr = args[0];
        this.classNum = ((Number)((LiteralExpression)args[1]).getLiteral()).intValue();
    }

    @Override
    public abstract Object getValue(Feature var1);
}

