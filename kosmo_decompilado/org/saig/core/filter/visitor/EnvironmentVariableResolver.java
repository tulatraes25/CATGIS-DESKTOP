/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.visitor;

import java.util.ArrayList;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.MapScaleDenominator;
import org.saig.core.filter.parser.ParseException;
import org.saig.core.filter.visitor.AbstractFilterVisitor;

public class EnvironmentVariableResolver {
    public Filter resolve(Filter filter, double mapScale) throws ParseException {
        String input = filter.toString();
        input = input.replaceAll("sld:MapScaleDenominator", "" + mapScale);
        Filter output = (Filter)ExpressionBuilder.parse(input);
        return output;
    }

    public Expression resolve(Expression exp, double mapScale) throws ParseException {
        String input = exp.toString();
        input = input.replaceAll("sld:MapScaleDenominator", "" + mapScale);
        Expression output = (Expression)ExpressionBuilder.parse(input);
        return output;
    }

    public boolean needsResolving(Filter f) {
        final ArrayList parts = new ArrayList();
        new AbstractFilterVisitor(){

            @Override
            public void visit(Expression expression) {
                if (expression instanceof MapScaleDenominator) {
                    parts.add(expression);
                }
            }
        }.visit(f);
        return parts.size() > 0;
    }
}

