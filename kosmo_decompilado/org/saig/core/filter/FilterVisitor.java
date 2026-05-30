/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LogicFilter;
import org.saig.core.filter.MathExpression;
import org.saig.core.filter.NullFilter;

public interface FilterVisitor {
    public void visit(Filter var1);

    public void visit(BetweenFilter var1);

    public void visit(CompareFilter var1);

    public void visit(GeometryFilter var1);

    public void visit(LikeFilter var1);

    public void visit(LogicFilter var1);

    public void visit(NullFilter var1);

    public void visit(FidFilter var1);

    public void visit(AttributeExpression var1);

    public void visit(Expression var1);

    public void visit(LiteralExpression var1);

    public void visit(MathExpression var1);

    public void visit(FunctionExpression var1);
}

