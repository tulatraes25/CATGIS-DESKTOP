/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterAttributeCalculatedVisitor;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LogicFilter;
import org.saig.core.filter.MathExpression;
import org.saig.core.filter.NullFilter;
import org.saig.core.model.feature.AttributeCalculate;

public class FilterAttributeCalculatedTranslator
implements FilterVisitor {
    private static final Logger LOGGER = Logger.getLogger(FilterAttributeCalculatedTranslator.class);
    private Filter translatedFilter;
    private FeatureSchema schemaFilter;
    private FilterAttributeCalculatedVisitor visitor;

    public FilterAttributeCalculatedTranslator(FeatureSchema schema) {
        this.schemaFilter = schema;
        this.visitor = new FilterAttributeCalculatedVisitor(schema);
    }

    public void clear() {
    }

    @Override
    public void visit(Filter filter) {
        this.visitor = new FilterAttributeCalculatedVisitor(this.schemaFilter);
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
        filter.accept(this.visitor);
        if (this.visitor.isCalculatedFilter()) {
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
        filter.accept(this.visitor);
        if (this.visitor.isCalculatedFilter()) {
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

    public Filter translate(LogicFilter filter) {
        ArrayList<Filter> newFilters = new ArrayList<Filter>();
        LogicFilter newLogicFilter = filter;
        Iterator<Filter> it = filter.getFilterIterator();
        while (it.hasNext()) {
            Filter f = it.next();
            f.accept(this.visitor);
            if (this.visitor.isCalculatedFilter()) {
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
            LOGGER.error((Object)"", (Throwable)e);
        }
        return newLogicFilter;
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
        if (filter.getLeftValue() != null && filter.getRightValue() != null) {
            AttributeExpression attrExpr = (AttributeExpression)filter.getLeftValue();
            LiteralExpression literalExpr = (LiteralExpression)filter.getRightValue();
            AttributeCalculate attrCalc = (AttributeCalculate)this.schemaFilter.getAttribute(attrExpr.getAttributePath());
            if (!literalExpr.getLiteral().equals("")) {
                Set<Object> keys = attrCalc.getKeysForFieldValue(attrCalc.getRelationFieldName(), literalExpr.getLiteral());
                String filterExpr = "";
                if (keys != null && keys.size() > 0) {
                    for (Object key : keys) {
                        filterExpr = String.valueOf(filterExpr) + attrCalc.getRelation().getSourceAttribute() + " = " + key + " OR ";
                    }
                    if (filterExpr.length() > 0) {
                        filterExpr = filterExpr.substring(0, filterExpr.length() - 4);
                        try {
                            newFilter = (CompareFilter)ExpressionBuilder.parse(this.schemaFilter, filterExpr);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        FilterFactory factory = FilterFactory.createFilterFactory();
                        newFilter = factory.createCompareFilter((short)14);
                        newFilter.addLeftValue(factory.createAttributeExpression(this.schemaFilter, attrCalc.getRelation().getSourceAttribute()));
                        newFilter.addRightValue(factory.createLiteralExpression(""));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    FilterFactory factory = FilterFactory.createFilterFactory();
                    newFilter = factory.createCompareFilter((short)14);
                    newFilter.addLeftValue(factory.createAttributeExpression(this.schemaFilter, attrCalc.getRelation().getSourceAttribute()));
                    newFilter.addRightValue(factory.createLiteralExpression(""));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (filter.getLeftValue() != null) {
            AttributeExpression attrExpr = (AttributeExpression)filter.getLeftValue();
            AttributeCalculate attrCalc = (AttributeCalculate)this.schemaFilter.getAttribute(attrExpr.getAttributePath());
            try {
                FilterFactory factory = FilterFactory.createFilterFactory();
                newFilter = factory.createCompareFilter((short)14);
                newFilter.addLeftValue(factory.createAttributeExpression(this.schemaFilter, attrCalc.getRelation().getSourceAttribute()));
                newFilter.addRightValue(null);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newFilter;
    }
}

