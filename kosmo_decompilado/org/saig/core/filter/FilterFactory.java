/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.geotools.factory.Factory
 *  org.geotools.factory.FactoryConfigurationError
 *  org.geotools.factory.FactoryFinder
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.Collections;
import java.util.Map;
import org.geotools.factory.Factory;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.factory.FactoryFinder;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.BBoxExpression;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.EnvironmentVariable;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.GeometryDistanceFilter;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LogicFilter;
import org.saig.core.filter.MathExpression;
import org.saig.core.filter.NullFilter;

public abstract class FilterFactory
implements Factory {
    private static FilterFactory factory = null;

    public static FilterFactory createFilterFactory() throws FactoryConfigurationError {
        if (factory == null) {
            factory = (FilterFactory)FactoryFinder.findFactory((String)"org.saig.core.filter.FilterFactory", (String)"org.saig.core.filter.FilterFactoryImpl");
        }
        return factory;
    }

    public abstract LogicFilter createLogicFilter(Filter var1, Filter var2, short var3) throws IllegalFilterException;

    public abstract LogicFilter createLogicFilter(short var1) throws IllegalFilterException;

    public abstract LogicFilter createLogicFilter(Filter var1, short var2) throws IllegalFilterException;

    public abstract BBoxExpression createBBoxExpression(Envelope var1) throws IllegalFilterException;

    public abstract LiteralExpression createLiteralExpression(int var1);

    public abstract MathExpression createMathExpression() throws IllegalFilterException;

    public abstract FidFilter createFidFilter();

    public abstract AttributeExpression createAttributeExpression(FeatureSchema var1, String var2) throws IllegalFilterException;

    public abstract AttributeExpression createAttributeExpression(String var1) throws IllegalFilterException;

    public abstract LiteralExpression createLiteralExpression(Object var1) throws IllegalFilterException;

    public abstract CompareFilter createCompareFilter(short var1) throws IllegalFilterException;

    public abstract LiteralExpression createLiteralExpression();

    public abstract LiteralExpression createLiteralExpression(String var1);

    public abstract LiteralExpression createLiteralExpression(double var1);

    public abstract AttributeExpression createAttributeExpression(FeatureSchema var1);

    public abstract MathExpression createMathExpression(short var1) throws IllegalFilterException;

    public abstract NullFilter createNullFilter();

    public abstract BetweenFilter createBetweenFilter() throws IllegalFilterException;

    public abstract GeometryFilter createGeometryFilter(short var1) throws IllegalFilterException;

    public abstract GeometryDistanceFilter createGeometryDistanceFilter(short var1) throws IllegalFilterException;

    public abstract FidFilter createFidFilter(String var1);

    public abstract LikeFilter createLikeFilter();

    public abstract FunctionExpression createFunctionExpression(String var1, Expression[] var2);

    public abstract FunctionExpression createFunctionExpression(String var1);

    public abstract EnvironmentVariable createEnvironmentVariable(String var1);

    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}

