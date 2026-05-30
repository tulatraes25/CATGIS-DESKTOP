/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.HashSet;
import java.util.Set;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.FilterParameterTranslator;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.visitor.FunctionExpressionVisitor;
import org.saig.core.model.feature.Attribute;

public class FilterUtil {
    public static final String PARAM_STRING = "\"$_PARAM_$\"";
    public static final String FILTER_GEOMETRY_BBOX = "bbox";
    public static final String FILTER_GEOMETRY_EQUALS = "equals";
    public static final String FILTER_GEOMETRY_DISJOINT = "disjoint";
    public static final String FILTER_GEOMETRY_INTERSECTS = "intersects";
    public static final String FILTER_GEOMETRY_TOUCHES = "touches";
    public static final String FILTER_GEOMETRY_CROSSES = "crosses";
    public static final String FILTER_GEOMETRY_WITHIN = "within";
    public static final String FILTER_GEOMETRY_CONTAINS = "contains";
    public static final String FILTER_GEOMETRY_OVERLAPS = "overlaps";
    public static final String FILTER_UNKNOW = "Filtro desconocido";

    public static boolean hasParameters(Filter filter) {
        return filter.toString().indexOf(PARAM_STRING) != -1;
    }

    public static Filter transformParameters(Filter filter) {
        Filter solutionFilter = filter;
        if (filter == null || !FilterUtil.hasParameters(filter)) {
            return null;
        }
        FilterParameterTranslator translator = new FilterParameterTranslator();
        solutionFilter.accept(translator);
        solutionFilter = translator.getTranslatedFilter();
        return solutionFilter;
    }

    public static String getFilterName(short filterType) {
        String filterName = "";
        switch (filterType) {
            case 4: {
                filterName = FILTER_GEOMETRY_BBOX;
                break;
            }
            case 5: {
                filterName = FILTER_GEOMETRY_EQUALS;
                break;
            }
            case 6: {
                filterName = FILTER_GEOMETRY_DISJOINT;
                break;
            }
            case 7: {
                filterName = FILTER_GEOMETRY_INTERSECTS;
                break;
            }
            case 8: {
                filterName = FILTER_GEOMETRY_TOUCHES;
                break;
            }
            case 9: {
                filterName = FILTER_GEOMETRY_CROSSES;
                break;
            }
            case 10: {
                filterName = FILTER_GEOMETRY_WITHIN;
                break;
            }
            case 11: {
                filterName = FILTER_GEOMETRY_CONTAINS;
                break;
            }
            case 12: {
                filterName = FILTER_GEOMETRY_OVERLAPS;
                break;
            }
            default: {
                filterName = FILTER_UNKNOW;
            }
        }
        return filterName;
    }

    public static Filter fromName(String filterName) throws IllegalFilterException {
        GeometryFilter newFilter = null;
        if (filterName.equalsIgnoreCase(FILTER_GEOMETRY_BBOX)) {
            newFilter = FilterFactory.createFilterFactory().createGeometryFilter((short)4);
        } else if (filterName.equalsIgnoreCase(FILTER_GEOMETRY_EQUALS)) {
            newFilter = FilterFactory.createFilterFactory().createGeometryFilter((short)5);
        } else if (filterName.equalsIgnoreCase(FILTER_GEOMETRY_DISJOINT)) {
            newFilter = FilterFactory.createFilterFactory().createGeometryFilter((short)6);
        } else if (filterName.equalsIgnoreCase(FILTER_GEOMETRY_INTERSECTS)) {
            newFilter = FilterFactory.createFilterFactory().createGeometryFilter((short)7);
        } else if (filterName.equalsIgnoreCase(FILTER_GEOMETRY_TOUCHES)) {
            newFilter = FilterFactory.createFilterFactory().createGeometryFilter((short)8);
        } else if (filterName.equalsIgnoreCase(FILTER_GEOMETRY_CROSSES)) {
            newFilter = FilterFactory.createFilterFactory().createGeometryFilter((short)9);
        } else if (filterName.equalsIgnoreCase(FILTER_GEOMETRY_WITHIN)) {
            newFilter = FilterFactory.createFilterFactory().createGeometryFilter((short)10);
        } else if (filterName.equalsIgnoreCase(FILTER_GEOMETRY_CONTAINS)) {
            newFilter = FilterFactory.createFilterFactory().createGeometryFilter((short)11);
        } else if (filterName.equalsIgnoreCase(FILTER_GEOMETRY_OVERLAPS)) {
            newFilter = FilterFactory.createFilterFactory().createGeometryFilter((short)12);
        }
        return newFilter;
    }

    public static boolean hasFunctionExpressionWithGeometry(Filter f) {
        FunctionExpressionVisitor visitor = new FunctionExpressionVisitor();
        visitor.visit(f);
        return visitor.useGeometry();
    }

    public static Set<String> getLabelsFromFilter(Filter filter, FeatureSchema schema) {
        String[] comparators = new String[]{" =", " >=", " <=", " <", " >", " !=", " IS", " LIKE"};
        HashSet<String> labels = new HashSet<String>();
        if (filter == null) {
            return labels;
        }
        String filterStr = filter.toString();
        int i = 0;
        while (i < schema.getAttributeCount()) {
            Attribute attr = schema.getAttribute(i);
            if (i != schema.getGeometryIndex()) {
                String name = attr.getName();
                boolean check = false;
                int j = 0;
                while (j < comparators.length && !check) {
                    if (filterStr.indexOf(String.valueOf(name) + comparators[j]) != -1) {
                        labels.add(name);
                        check = true;
                    }
                    ++j;
                }
            }
            ++i;
        }
        return labels;
    }
}

