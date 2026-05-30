/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.visitor;

import org.saig.core.filter.Expression;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.visitor.AbstractFilterVisitor;

public class FunctionExpressionVisitor
extends AbstractFilterVisitor {
    private boolean hasGeometry = false;

    @Override
    public void visit(FunctionExpression expression) {
        Expression[] args = expression.getArgs();
        if (args != null) {
            int i = 0;
            while (i < args.length) {
                if (args[i] != null) {
                    args[i].accept(this);
                }
                ++i;
            }
        }
        this.hasGeometry = true;
    }

    public boolean useGeometry() {
        return this.hasGeometry;
    }
}

