/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.Iterator;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LogicFilter;
import org.saig.core.filter.MathExpression;
import org.saig.core.filter.NullFilter;
import org.saig.core.model.feature.AttributeCalculate;

public class FilterAttributeCalculatedVisitor
implements FilterVisitor {
    private boolean calculatedFilter = false;
    private FeatureSchema schemaFilter;

    public FilterAttributeCalculatedVisitor(FeatureSchema schema) {
        this.schemaFilter = schema;
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
        if (filter.getLeftValue() != null) {
            filter.getLeftValue().accept(this);
        }
        if (filter.getRightValue() != null) {
            filter.getRightValue().accept(this);
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
        Iterator<Filter> it = filter.getFilterIterator();
        while (it.hasNext()) {
            Filter f = it.next();
            f.accept(this);
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
        String attributeName = expression.getAttributePath();
        this.calculatedFilter = this.calculatedFilter || this.schemaFilter.getAttribute(attributeName) instanceof AttributeCalculate;
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

    public boolean isCalculatedFilter() {
        return this.calculatedFilter;
    }
}

