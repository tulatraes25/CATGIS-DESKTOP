/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import java.util.Collections;
import java.util.Map;
import org.saig.core.filter.DefaultExpression;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.FunctionExpression;

public abstract class FunctionExpressionImpl
extends DefaultExpression
implements FunctionExpression {
    protected Expression[] args;

    @Override
    public short getType() {
        return 114;
    }

    @Override
    public abstract String getName();

    @Override
    public abstract int getArgCount();

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }

    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public void setArgs(Expression[] args) {
        this.args = args;
    }

    @Override
    public Expression[] getArgs() {
        return this.args;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getName());
        sb.append("(");
        if (this.args != null) {
            Expression[] expressionArray = this.args;
            int n = this.args.length;
            int n2 = 0;
            while (n2 < n) {
                Expression exp = expressionArray[n2];
                sb.append("[");
                sb.append(exp);
                sb.append("]");
                ++n2;
            }
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(")");
        return sb.toString();
    }
}

