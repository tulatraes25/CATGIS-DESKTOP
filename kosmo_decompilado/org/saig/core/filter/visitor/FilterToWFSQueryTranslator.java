/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.lang.StringUtils
 *  org.deegree.model.crs.CRSFactory
 *  org.deegree.model.crs.UnknownCRSException
 *  org.deegree.model.spatialschema.Geometry
 *  org.deegree.model.spatialschema.GeometryException
 *  org.deegree.ogcwebservices.wfs.capabilities.FormatType
 */
package org.saig.core.filter.visitor;

import java.net.URI;
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
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
import org.saig.core.filter.function.FilterFunction_geomLength;
import org.saig.core.filter.function.FilterFunction_getX;
import org.saig.core.filter.function.FilterFunction_getY;
import org.saig.core.filter.visitor.AbstractFilterVisitor;
import org.saig.jump.lang.I18N;

public class FilterToWFSQueryTranslator
extends AbstractFilterVisitor {
    protected StringBuffer result = new StringBuffer();
    protected String featTypePrefix;
    protected String featTypeNamespace;
    protected FormatType format;
    protected CoordinateSystem selectedCRS;
    protected String currentOperator;

    public FilterToWFSQueryTranslator(String prefix, String namespace, FormatType formatType, URI selectedSRS) {
        this.featTypePrefix = !StringUtils.isEmpty((String)prefix) ? prefix : "";
        this.featTypeNamespace = !StringUtils.isEmpty((String)namespace) ? namespace : "";
        this.format = formatType;
        if (selectedSRS != null) {
            try {
                String epsgCode = GMLGeometryAdapter.transformCRSNameToEPSG(selectedSRS.toString());
                this.selectedCRS = CRSFactory.create((String)epsgCode);
                this.selectedCRS.setIdentifier(selectedSRS.toString());
            }
            catch (UnknownCRSException e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.selectedCRS = null;
            }
        } else {
            this.selectedCRS = null;
        }
    }

    public String translateFilter(Filter filter) {
        this.result = new StringBuffer();
        String[] filterTags = new String[]{"", ""};
        if (filter != null && !Filter.NONE.equals(filter)) {
            filterTags = FilterToWFSQueryTranslator.createStartStopTags("Filter");
            this.result.append(filterTags[0]);
            this.visit(filter);
            this.result.append(filterTags[1]);
        }
        return this.result.toString();
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
        String[] filterTags = FilterToWFSQueryTranslator.createStartStopTags("PropertyIsBetween");
        this.result.append(filterTags[0]);
        if (filter.getMiddleValue() != null) {
            filter.getMiddleValue().accept(this);
        }
        String[] lowerBoundaryTags = FilterToWFSQueryTranslator.createStartStopTags("LowerBoundary");
        this.result.append(lowerBoundaryTags[0]);
        if (filter.getLeftValue() != null) {
            filter.getLeftValue().accept(this);
        }
        this.result.append(lowerBoundaryTags[1]);
        String[] upperBoundaryTags = FilterToWFSQueryTranslator.createStartStopTags("UpperBoundary");
        this.result.append(upperBoundaryTags[0]);
        if (filter.getRightValue() != null) {
            filter.getRightValue().accept(this);
        }
        this.result.append(upperBoundaryTags[0]);
        this.result.append(filterTags[1]);
    }

    @Override
    public void visit(CompareFilter filter) {
        String operator = null;
        short filterType = filter.getFilterType();
        if (filterType == 14) {
            operator = "PropertyIsEqualTo";
        }
        if (filterType == 15) {
            operator = "PropertyIsLessThan";
        }
        if (filterType == 16) {
            operator = "PropertyIsGreaterThan";
        }
        if (filterType == 17) {
            operator = "PropertyIsLessThanOrEqualTo";
        }
        if (filterType == 18) {
            operator = "PropertyIsGreaterThanOrEqualTo";
        }
        if (filterType == 23) {
            operator = "PropertyIsNotEqualTo";
        }
        String[] filterTags = FilterToWFSQueryTranslator.createStartStopTags(operator);
        this.result.append(filterTags[0]);
        if (filter.getLeftValue() != null) {
            filter.getLeftValue().accept(this);
        }
        if (filter.getRightValue() != null) {
            filter.getRightValue().accept(this);
        }
        this.result.append(filterTags[1]);
    }

    @Override
    public void visit(GeometryFilter filter) {
        String operator = null;
        short filterType = filter.getFilterType();
        if (filterType == 5) {
            operator = "Equals";
        } else if (filterType == 6) {
            operator = "Disjoint";
        } else if (filterType == 7) {
            operator = this.format.getValue().equals(GMLGeometryAdapter.GML2_FORMAT_TYPE) ? "Intersects" : "Intersects";
        } else if (filterType == 9) {
            operator = "Crosses";
        } else if (filterType == 10) {
            operator = "Within";
        } else if (filterType == 11) {
            operator = "Contains";
        } else if (filterType == 12) {
            operator = "Overlaps";
        } else if (filterType == 13) {
            operator = "Beyond";
        } else if (filterType == 4) {
            operator = "BBOX";
        }
        String[] filterTags = FilterToWFSQueryTranslator.createStartStopTags(operator);
        this.result.append(filterTags[0]);
        if (filter.getLeftGeometry() != null) {
            filter.getLeftGeometry().accept(this);
        } else {
            this.result.append("null");
        }
        if (filter.getRightGeometry() != null) {
            this.currentOperator = operator;
            filter.getRightGeometry().accept(this);
            this.currentOperator = null;
        } else {
            this.result.append("null");
        }
        this.result.append(filterTags[1]);
    }

    @Override
    public void visit(LikeFilter filter) {
        this.result.append("<ogc:").append("PropertyIsLike").append(" wildCard=\"").append(filter.getWildcardMulti());
        this.result.append("\" singleChar=\"").append(filter.getWildcardSingle()).append("\" escape=\"");
        this.result.append(filter.getEscape()).append("\">");
        if (filter.getValue() != null) {
            filter.getValue().accept(this);
        }
        this.result.append("<ogc:Literal>").append(filter.getPattern()).append("</ogc:Literal>");
        this.result.append("</ogc:PropertyIsLike>");
    }

    @Override
    public void visit(LogicFilter filter) {
        String operator = "";
        short filterType = filter.getFilterType();
        if (filterType == 1) {
            operator = "Or";
        } else if (filterType == 2) {
            operator = "And";
        } else if (filterType == 3) {
            operator = "Not";
        }
        String[] filterTags = FilterToWFSQueryTranslator.createStartStopTags(operator);
        this.result.append(filterTags[0]);
        Iterator<Filter> it = filter.getFilterIterator();
        while (it.hasNext()) {
            Filter f = it.next();
            f.accept(this);
        }
        this.result.append(filterTags[1]);
    }

    @Override
    public void visit(NullFilter filter) {
        String[] filterTags = FilterToWFSQueryTranslator.createStartStopTags("PropertyIsNull");
        this.result.append(filterTags[0]);
        if (filter.getNullCheckValue() != null) {
            filter.getNullCheckValue().accept(this);
        }
        this.result.append(filterTags[1]);
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
        String[] filterTags = FilterToWFSQueryTranslator.createStartStopTags("PropertyName");
        this.result.append(filterTags[0]);
        if (!StringUtils.isEmpty((String)this.featTypePrefix)) {
            this.result.append(this.featTypePrefix);
            this.result.append(":");
        }
        this.result.append(expression.getAttributePath());
        this.result.append(filterTags[1]);
    }

    @Override
    public void visit(Expression expression) {
    }

    @Override
    public void visit(LiteralExpression expression) {
        String[] filterTags = null;
        StringBuffer expressionValue = null;
        switch (expression.getType()) {
            case 104: {
                filterTags = new String[]{"", ""};
                try {
                    com.vividsolutions.jts.geom.Geometry geom = (com.vividsolutions.jts.geom.Geometry)expression.getLiteral();
                    Geometry deegreeGeom = JTSAdapter.wrap(geom, this.selectedCRS);
                    if (!"BBOX".equals(this.currentOperator)) {
                        expressionValue = GMLGeometryAdapter.export(deegreeGeom, this.format.getValue());
                        break;
                    }
                    if (this.format.getValue().equals("GML2")) {
                        expressionValue = GMLGeometryAdapter.exportAsBox(deegreeGeom.getEnvelope());
                        break;
                    }
                    expressionValue = GMLGeometryAdapter.exportAsEnvelope(deegreeGeom.getEnvelope(), this.format.getValue());
                }
                catch (GeometryException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                break;
            }
            default: {
                filterTags = FilterToWFSQueryTranslator.createStartStopTags("Literal");
                expressionValue = new StringBuffer();
                expressionValue.append(expression.getLiteral().toString());
            }
        }
        this.result.append(filterTags[0]);
        this.result.append(expressionValue);
        this.result.append(filterTags[1]);
    }

    @Override
    public void visit(MathExpression expression) {
        String operation = "";
        short expressionType = expression.getType();
        switch (expressionType) {
            case 105: {
                operation = "Add";
                break;
            }
            case 106: {
                operation = "Sub";
                break;
            }
            case 107: {
                operation = "Mul";
                break;
            }
            case 108: {
                operation = "Div";
            }
        }
        String[] filterTags = FilterToWFSQueryTranslator.createStartStopTags(operation);
        this.result.append(filterTags[0]);
        if (expression.getLeftValue() != null) {
            expression.getLeftValue().accept(this);
        }
        if (expression.getRightValue() != null) {
            expression.getRightValue().accept(this);
        }
        this.result.append(filterTags[1]);
    }

    @Override
    public void visit(FunctionExpression expression) {
        this.result.append("[ ");
        if (expression instanceof FilterFunction_area) {
            this.visit((FilterFunction_area)expression);
        } else if (expression instanceof FilterFunction_geomLength) {
            this.visit((FilterFunction_geomLength)expression);
        } else if (expression instanceof FilterFunction_getX) {
            this.visit((FilterFunction_getX)expression);
        } else if (expression instanceof FilterFunction_getY) {
            this.visit((FilterFunction_getY)expression);
        } else {
            LOGGER.warn((Object)I18N.getMessage(this.getClass(), "filter-of-type-{0}-not-supported", new Object[]{expression.getClass()}));
        }
        this.result.append(" ]");
    }

    public void visit(FilterFunction_area expression) {
        this.result.append("area(");
        int t = 0;
        while (t < expression.getArgCount()) {
            expression.getArgs()[t].accept(this);
            if (t != expression.getArgCount() - 1) {
                this.result.append(",");
            }
            ++t;
        }
        this.result.append(")");
    }

    public void visit(FilterFunction_geomLength expression) {
        this.result.append("geomLength(");
        int t = 0;
        while (t < expression.getArgCount()) {
            expression.getArgs()[t].accept(this);
            if (t != expression.getArgCount() - 1) {
                this.result.append(",");
            }
            ++t;
        }
        this.result.append(")");
    }

    public void visit(FilterFunction_getX expression) {
        this.result.append("getX(");
        int t = 0;
        while (t < expression.getArgCount()) {
            expression.getArgs()[t].accept(this);
            if (t != expression.getArgCount() - 1) {
                this.result.append(",");
            }
            ++t;
        }
        this.result.append(")");
    }

    public void visit(FilterFunction_getY expression) {
        this.result.append("getY(");
        int t = 0;
        while (t < expression.getArgCount()) {
            expression.getArgs()[t].accept(this);
            if (t != expression.getArgCount() - 1) {
                this.result.append(",");
            }
            ++t;
        }
        this.result.append(")");
    }

    public static final String[] createStartStopTags(String tagName) {
        String[] tags = new String[]{"<ogc:" + tagName + ">", "</ogc:" + tagName + ">"};
        return tags;
    }
}

