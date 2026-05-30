/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.model.filterencoding.capabilities.Function
 *  org.deegree.model.filterencoding.capabilities.Operator
 *  org.deegree.model.filterencoding.capabilities.SpatialOperator
 *  org.deegree.ogcwebservices.getcapabilities.UnknownOperatorNameException
 */
package org.deegree.model.filterencoding.capabilities;

import org.deegree.model.filterencoding.capabilities.Function;
import org.deegree.model.filterencoding.capabilities.Operator;
import org.deegree.model.filterencoding.capabilities.SpatialOperator;
import org.deegree.ogcwebservices.getcapabilities.UnknownOperatorNameException;

public class OperatorFactory100 {
    public static final String OPERATOR_LOGICAL_OPERATORS = "Logical_Operators";
    public static final String OPERATOR_SIMPLE_ARITHMETIC = "Simple_Arithmetic";
    public static final String OPERATOR_FUNCTIONS = "Functions";
    public static final String OPERATOR_SIMPLE_COMPARISONS = "Simple_Comparisons";
    public static final String OPERATOR_LIKE = "Like";
    public static final String OPERATOR_BETWEEN = "Between";
    public static final String OPERATOR_NULL_CHECK = "NullCheck";
    public static final String OPERATOR_BBOX = "BBOX";
    public static final String OPERATOR_EQUALS = "Equals";
    public static final String OPERATOR_DISJOINT = "Disjoint";
    public static final String OPERATOR_INTERSECT = "Intersect";
    public static final String OPERATOR_TOUCHES = "Touches";
    public static final String OPERATOR_CROSSES = "Crosses";
    public static final String OPERATOR_WITHIN = "Within";
    public static final String OPERATOR_CONTAINS = "Contains";
    public static final String OPERATOR_OVERLAPS = "Overlaps";
    public static final String OPERATOR_BEYOND = "Beyond";
    public static final String OPERATOR_DWITHIN = "DWithin";

    public static SpatialOperator createSpatialOperator(String name) throws UnknownOperatorNameException {
        if (name.equals(OPERATOR_BBOX) || name.equals(OPERATOR_EQUALS) || name.equals(OPERATOR_DISJOINT) || name.equals(OPERATOR_INTERSECT) || name.equals(OPERATOR_TOUCHES) || name.equals(OPERATOR_CROSSES) || name.equals(OPERATOR_WITHIN) || name.equals(OPERATOR_CONTAINS) || name.equals(OPERATOR_OVERLAPS) || name.equals(OPERATOR_BEYOND) || name.equals(OPERATOR_DWITHIN)) {
            return new SpatialOperator(name);
        }
        throw new UnknownOperatorNameException("'" + name + "' is no known spatial operator.");
    }

    public static Operator createComparisonOperator(String name) throws UnknownOperatorNameException {
        if (name.equals(OPERATOR_SIMPLE_COMPARISONS) || name.equals(OPERATOR_LIKE) || name.equals(OPERATOR_BETWEEN) || name.equals(OPERATOR_NULL_CHECK)) {
            return new Operator(name);
        }
        throw new UnknownOperatorNameException("'" + name + "' is no known comparison operator.");
    }

    public static Operator createArithmeticOperator(String name) throws UnknownOperatorNameException {
        if (name.equals(OPERATOR_SIMPLE_ARITHMETIC) || name.equals(OPERATOR_FUNCTIONS)) {
            return new Operator(name);
        }
        throw new UnknownOperatorNameException("'" + name + "' is no known arithmetic operator.");
    }

    public static Function createArithmeticFunction(String name, int argumentCount) {
        return new Function(name, argumentCount);
    }
}

