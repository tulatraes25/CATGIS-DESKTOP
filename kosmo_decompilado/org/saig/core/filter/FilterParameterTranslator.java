/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.filter;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.FilterUtil;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LogicFilter;
import org.saig.core.filter.MathExpression;
import org.saig.core.filter.NullFilter;
import org.saig.jump.widgets.util.DialogFactory;

public class FilterParameterTranslator
implements FilterVisitor {
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.core.filter.FilterParameterTranslator");
    private Filter translatedFilter;

    public void clear() {
    }

    @Override
    public void visit(Filter filter) {
        if (filter instanceof BetweenFilter) {
            this.visit((BetweenFilter)filter);
        } else if (filter instanceof CompareFilter) {
            this.visit((CompareFilter)filter);
        } else if (filter instanceof GeometryFilter) {
            this.visit((GeometryFilter)filter);
        } else if (filter instanceof LikeFilter) {
            this.visit((LikeFilter)filter);
        } else if (filter instanceof LogicFilter) {
            this.visit((LogicFilter)filter);
        } else if (filter instanceof NullFilter) {
            this.visit((NullFilter)filter);
        } else if (filter instanceof FidFilter) {
            this.visit((FidFilter)filter);
        }
    }

    @Override
    public void visit(BetweenFilter filter) {
        if (filter.getLeftValue() != null) {
            filter.getLeftValue().accept(this);
        }
        if (filter.getRightValue() != null) {
            filter.getRightValue().accept(this);
        }
        if (filter.getMiddleValue() != null) {
            filter.getMiddleValue().accept(this);
        }
    }

    @Override
    public void visit(CompareFilter filter) {
        if (FilterUtil.hasParameters(filter)) {
            this.translatedFilter = this.translate(filter);
        }
    }

    @Override
    public void visit(GeometryFilter filter) {
        if (filter.getLeftGeometry() != null) {
            filter.getLeftGeometry().accept(this);
        }
        if (filter.getRightGeometry() != null) {
            filter.getRightGeometry().accept(this);
        }
    }

    @Override
    public void visit(LikeFilter filter) {
        if (filter.getValue() != null) {
            filter.getValue().accept(this);
        }
    }

    @Override
    public void visit(LogicFilter filter) {
        if (FilterUtil.hasParameters(filter)) {
            this.translatedFilter = this.translate(filter);
        }
    }

    @Override
    public void visit(NullFilter filter) {
        if (filter.getNullCheckValue() != null) {
            filter.getNullCheckValue().accept(this);
        }
    }

    @Override
    public void visit(FidFilter filter) {
    }

    @Override
    public void visit(AttributeExpression expression) {
    }

    @Override
    public void visit(Expression expression) {
        if (expression instanceof AttributeExpression) {
            this.visit((AttributeExpression)expression);
        } else if (expression instanceof LiteralExpression) {
            this.visit((LiteralExpression)expression);
        } else if (expression instanceof MathExpression) {
            this.visit((MathExpression)expression);
        } else if (expression instanceof FunctionExpression) {
            this.visit((FunctionExpression)expression);
        }
    }

    @Override
    public void visit(LiteralExpression expression) {
    }

    @Override
    public void visit(MathExpression expression) {
        if (expression.getLeftValue() != null) {
            expression.getLeftValue().accept(this);
        }
        if (expression.getRightValue() != null) {
            expression.getRightValue().accept(this);
        }
    }

    @Override
    public void visit(FunctionExpression expression) {
        Expression[] args = expression.getArgs();
        int i = 0;
        while (i < args.length) {
            if (args[i] != null) {
                args[i].accept(this);
            }
            ++i;
        }
    }

    public Filter getTranslatedFilter() {
        return this.translatedFilter;
    }

    public Filter translate(Filter f) {
        if (f instanceof CompareFilter) {
            return this.translate((CompareFilter)f);
        }
        if (f instanceof LogicFilter) {
            return this.translate((LogicFilter)f);
        }
        return f;
    }

    public Filter translate(CompareFilter filter) {
        CompareFilter newFilter = filter;
        if (filter.getLeftValue() != null) {
            Expression rightExpr = filter.getRightValue();
            Expression translateExpression = this.translate(rightExpr);
            try {
                newFilter.addRightValue(translateExpression);
            }
            catch (IllegalFilterException e) {
                LOGGER.error((Object)"Error al traducir el filtro", (Throwable)e);
            }
        }
        return newFilter;
    }

    private Expression translate(Expression expression) {
        if (expression instanceof AttributeExpression) {
            return this.translate((AttributeExpression)expression);
        }
        if (expression instanceof LiteralExpression) {
            return this.translate((LiteralExpression)expression);
        }
        if (expression instanceof MathExpression) {
            return this.translate((MathExpression)expression);
        }
        if (expression instanceof FunctionExpression) {
            return this.translate((FunctionExpression)expression);
        }
        return expression;
    }

    private Expression translate(LiteralExpression expression) {
        if (expression.getLiteral().equals("\"$_PARAM_$\"")) {
            DialogFactory.showInformationDialog(JUMPWorkbench.getFrameInstance(), "Solicitar par\u00e1metro", "Dame el valor del par\u00e1metro");
            return expression;
        }
        return expression;
    }

    private Expression translate(MathExpression expression) {
        return expression;
    }

    private Expression translate(FunctionExpression expression) {
        return expression;
    }

    private Expression translate(AttributeExpression expression) {
        return expression;
    }

    public Filter translate(LogicFilter filter) {
        ArrayList<Filter> newFilters = new ArrayList<Filter>();
        LogicFilter newLogicFilter = filter;
        Iterator<Filter> it = filter.getFilterIterator();
        while (it.hasNext()) {
            Filter f = it.next();
            if (FilterUtil.hasParameters(f)) {
                Filter newFilter = this.translate(f);
                newFilters.add(newFilter);
                continue;
            }
            newFilters.add(f);
        }
        try {
            newLogicFilter = FilterFactory.createFilterFactory().createLogicFilter(filter.getFilterType());
            for (Filter element : newFilters) {
                newLogicFilter.addFilter(element);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return newLogicFilter;
    }
}

