/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.visitor;

import java.util.Iterator;
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
import org.saig.core.filter.function.FilterFunction_area;
import org.saig.core.filter.function.FilterFunction_currentDate;
import org.saig.core.filter.function.FilterFunction_daysFrom;
import org.saig.core.filter.function.FilterFunction_geomLength;
import org.saig.core.filter.function.FilterFunction_getX;
import org.saig.core.filter.function.FilterFunction_getY;
import org.saig.core.filter.function.FilterFunction_strLength;
import org.saig.core.filter.visitor.AbstractFilterVisitor;
import org.saig.jump.lang.I18N;

public class FilterToStringTranslator
extends AbstractFilterVisitor {
    protected StringBuffer result = new StringBuffer();

    public String translateFilter(Filter filter) {
        this.result = new StringBuffer();
        this.visit(filter);
        return this.result.toString();
    }

    @Override
    public void visit(Filter filter) {
        this.result.append("[ ");
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
        this.result.append(" ]");
    }

    @Override
    public void visit(BetweenFilter filter) {
        this.result.append(" between (");
        if (filter.getLeftValue() != null) {
            filter.getLeftValue().accept(this);
        }
        this.result.append(",");
        if (filter.getMiddleValue() != null) {
            filter.getMiddleValue().accept(this);
        }
        this.result.append(",");
        if (filter.getRightValue() != null) {
            filter.getRightValue().accept(this);
        }
        this.result.append(") ");
    }

    @Override
    public void visit(CompareFilter filter) {
        this.result.append(" [ ");
        if (filter.getLeftValue() != null) {
            filter.getLeftValue().accept(this);
        }
        String operator = null;
        short filterType = filter.getFilterType();
        if (filterType == 14) {
            operator = " = ";
        }
        if (filterType == 15) {
            operator = " < ";
        }
        if (filterType == 16) {
            operator = " > ";
        }
        if (filterType == 17) {
            operator = " <= ";
        }
        if (filterType == 18) {
            operator = " >= ";
        }
        if (filterType == 23) {
            operator = " != ";
        }
        this.result.append(" " + operator + " ");
        if (filter.getRightValue() != null) {
            filter.getRightValue().accept(this);
        }
        this.result.append(" ] ");
    }

    @Override
    public void visit(GeometryFilter filter) {
        String operator = null;
        short filterType = filter.getFilterType();
        if (filterType == 5) {
            operator = " equals ";
        } else if (filterType == 6) {
            operator = " disjoint ";
        } else if (filterType == 7) {
            operator = " intersects ";
        } else if (filterType == 9) {
            operator = " crosses ";
        } else if (filterType == 10) {
            operator = " within ";
        } else if (filterType == 11) {
            operator = " contains ";
        } else if (filterType == 12) {
            operator = " overlaps ";
        } else if (filterType == 13) {
            operator = " beyond ";
        } else if (filterType == 4) {
            operator = " bbox ";
        }
        this.result.append(" [ ");
        this.result.append(operator);
        this.result.append("(");
        if (filter.getLeftGeometry() != null) {
            filter.getLeftGeometry().accept(this);
        } else {
            this.result.append("null");
        }
        this.result.append(",");
        if (filter.getRightGeometry() != null) {
            filter.getRightGeometry().accept(this);
        } else {
            this.result.append("null");
        }
        this.result.append(") ]");
    }

    @Override
    public void visit(LikeFilter filter) {
        this.result.append("like (");
        if (filter.getValue() != null) {
            filter.getValue().accept(this);
        }
        this.result.append(",'" + filter.getPattern() + "')");
    }

    @Override
    public void visit(LogicFilter filter) {
        this.result.append("[ ");
        String operator = "";
        short filterType = filter.getFilterType();
        if (filterType == 1) {
            operator = " OR ";
        } else if (filterType == 2) {
            operator = " AND ";
        } else if (filterType == 3) {
            operator = "NOT ";
        }
        if (filterType == 3) {
            this.result.append(operator);
            this.result.append("[ ");
            Iterator<Filter> it = filter.getFilterIterator();
            while (it.hasNext()) {
                Filter f = it.next();
                f.accept(this);
            }
            this.result.append(" ]");
        } else {
            Iterator<Filter> it = filter.getFilterIterator();
            while (it.hasNext()) {
                Filter f = it.next();
                f.accept(this);
                if (!it.hasNext()) continue;
                this.result.append(" " + operator + " ");
            }
        }
        this.result.append(" ]");
    }

    @Override
    public void visit(NullFilter filter) {
        this.result.append("[ isNull(");
        if (filter.getNullCheckValue() != null) {
            filter.getNullCheckValue().accept(this);
        }
        this.result.append(") ] ");
    }

    @Override
    public void visit(FidFilter filter) {
        this.result.append("[ ");
        String[] fids = filter.getFids();
        int i = 0;
        while (i < fids.length - 1) {
            String currentFid = fids[i];
            this.result.append(String.valueOf(currentFid) + ",");
            ++i;
        }
        if (fids.length > 0) {
            this.result.append(fids[fids.length - 1]);
        }
        this.result.append(" ]");
    }

    @Override
    public void visit(AttributeExpression expression) {
        this.result.append(expression.getAttributePath());
    }

    @Override
    public void visit(Expression expression) {
    }

    @Override
    public void visit(LiteralExpression expression) {
        if (expression.getType() == 103) {
            this.result.append("'" + expression.getLiteral() + "'");
        } else {
            this.result.append(expression.getLiteral().toString());
        }
    }

    @Override
    public void visit(MathExpression expression) {
        String operation;
        short expressionType = expression.getType();
        switch (expressionType) {
            case 105: {
                operation = " + ";
                break;
            }
            case 106: {
                operation = " - ";
                break;
            }
            case 107: {
                operation = " * ";
                break;
            }
            case 108: {
                operation = " / ";
                break;
            }
            default: {
                operation = " ? ";
            }
        }
        this.result.append("[ ");
        if (expression.getLeftValue() != null) {
            expression.getLeftValue().accept(this);
        }
        this.result.append(" " + operation + " ");
        if (expression.getRightValue() != null) {
            expression.getRightValue().accept(this);
        }
        this.result.append(" ]");
    }

    @Override
    public void visit(FunctionExpression expression) {
        this.result.append("[ ");
        if (expression instanceof FilterFunction_area) {
            this.visitFunction(expression);
        } else if (expression instanceof FilterFunction_geomLength) {
            this.visitFunction(expression);
        } else if (expression instanceof FilterFunction_getX) {
            this.visitFunction(expression);
        } else if (expression instanceof FilterFunction_getY) {
            this.visitFunction(expression);
        } else if (expression instanceof FilterFunction_strLength) {
            this.visitFunction(expression);
        } else if (expression instanceof FilterFunction_daysFrom) {
            this.visitFunction(expression);
        } else if (expression instanceof FilterFunction_currentDate) {
            this.visitFunction(expression);
        } else {
            LOGGER.warn((Object)I18N.getMessage(this.getClass(), "filter-type-{0}-not-supported", new Object[]{expression.getClass()}));
        }
        this.result.append(" ]");
    }

    protected void visitFunction(FunctionExpression functExpr) {
        this.result.append(String.valueOf(functExpr.getName()) + "(");
        int t = 0;
        while (t < functExpr.getArgCount()) {
            functExpr.getArgs()[t].accept(this);
            if (t != functExpr.getArgCount() - 1) {
                this.result.append(",");
            }
            ++t;
        }
        this.result.append(")");
    }
}

