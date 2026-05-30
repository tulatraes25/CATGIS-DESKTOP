/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.geotools.factory.FactoryFinder
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import java.util.Iterator;
import org.geotools.factory.FactoryFinder;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.AttributeExpressionImpl;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.BBoxExpression;
import org.saig.core.filter.BBoxExpressionImpl;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.BetweenFilterImpl;
import org.saig.core.filter.CartesianDistanceFilter;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.EnvironmentVariable;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.FidFilterImpl;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.GeometryDistanceFilter;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.GeometryFilterImpl;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LikeFilterImpl;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.LogicFilter;
import org.saig.core.filter.LogicFilterImpl;
import org.saig.core.filter.MapScaleDenominatorImpl;
import org.saig.core.filter.MathExpression;
import org.saig.core.filter.MathExpressionImpl;
import org.saig.core.filter.NullFilter;
import org.saig.core.filter.NullFilterImpl;

public class FilterFactoryImpl
extends FilterFactory {
    public Filter createBBoxFilter(String attrName, Envelope envelope) throws IllegalFilterException {
        AttributeExpressionImpl2 attribute = new AttributeExpressionImpl2(attrName);
        LiteralExpressionImpl geometry = new LiteralExpressionImpl(EnvelopeUtil.toGeometry(envelope));
        GeometryFilterImpl geomFilter = new GeometryFilterImpl(4);
        geomFilter.addLeftGeometry(attribute);
        geomFilter.addRightGeometry(geometry);
        return geomFilter;
    }

    @Override
    public AttributeExpression createAttributeExpression(FeatureSchema schema) {
        return new AttributeExpressionImpl(schema);
    }

    @Override
    public AttributeExpression createAttributeExpression(FeatureSchema schema, String path) throws IllegalFilterException {
        return new AttributeExpressionImpl(schema, path);
    }

    @Override
    public AttributeExpression createAttributeExpression(String attrPath) throws IllegalFilterException {
        return new AttributeExpressionImpl2(attrPath);
    }

    @Override
    public BBoxExpression createBBoxExpression(Envelope env) throws IllegalFilterException {
        return new BBoxExpressionImpl(env);
    }

    @Override
    public BetweenFilter createBetweenFilter() throws IllegalFilterException {
        return new BetweenFilterImpl();
    }

    @Override
    public CompareFilter createCompareFilter(short type) throws IllegalFilterException {
        return new CompareFilterImpl(type);
    }

    @Override
    public FidFilter createFidFilter() {
        return new FidFilterImpl();
    }

    @Override
    public FidFilter createFidFilter(String fid) {
        return new FidFilterImpl(fid);
    }

    @Override
    public GeometryDistanceFilter createGeometryDistanceFilter(short filterType) throws IllegalFilterException {
        return new CartesianDistanceFilter(filterType);
    }

    @Override
    public GeometryFilter createGeometryFilter(short filterType) throws IllegalFilterException {
        return new GeometryFilterImpl(filterType);
    }

    @Override
    public LikeFilter createLikeFilter() {
        return new LikeFilterImpl();
    }

    @Override
    public LiteralExpression createLiteralExpression() {
        return new LiteralExpressionImpl();
    }

    @Override
    public LiteralExpression createLiteralExpression(Object o) throws IllegalFilterException {
        return new LiteralExpressionImpl(o);
    }

    @Override
    public LiteralExpression createLiteralExpression(int i) {
        return new LiteralExpressionImpl(i);
    }

    @Override
    public LiteralExpression createLiteralExpression(double d) {
        return new LiteralExpressionImpl(d);
    }

    @Override
    public LiteralExpression createLiteralExpression(String s) {
        return new LiteralExpressionImpl(s);
    }

    @Override
    public LogicFilter createLogicFilter(short filterType) throws IllegalFilterException {
        return new LogicFilterImpl(filterType);
    }

    @Override
    public LogicFilter createLogicFilter(Filter filter, short filterType) throws IllegalFilterException {
        return new LogicFilterImpl(filter, filterType);
    }

    @Override
    public LogicFilter createLogicFilter(Filter filter1, Filter filter2, short filterType) throws IllegalFilterException {
        return new LogicFilterImpl(filter1, filter2, filterType);
    }

    @Override
    public MathExpression createMathExpression() {
        return new MathExpressionImpl();
    }

    @Override
    public MathExpression createMathExpression(short expressionType) throws IllegalFilterException {
        return new MathExpressionImpl(expressionType);
    }

    @Override
    public FunctionExpression createFunctionExpression(String name) {
        return this.createFunctionExpression(name, null);
    }

    @Override
    public FunctionExpression createFunctionExpression(String name, Expression[] params) {
        int index = -1;
        index = name.indexOf("Function");
        if (index != -1) {
            name = name.substring(0, index);
        }
        name = name.toLowerCase().trim();
        char c = name.charAt(0);
        name = name.replaceFirst("" + c, "" + Character.toUpperCase(c));
        try {
            Iterator it = FactoryFinder.factories(FunctionExpression.class);
            String funName = "";
            FunctionExpression exp = null;
            while (funName != "found" && it.hasNext()) {
                FunctionExpression fe = (FunctionExpression)it.next();
                funName = fe.getName();
                if (!funName.equalsIgnoreCase(name)) continue;
                exp = fe;
                funName = "found";
                exp.setArgs(params);
            }
            return exp;
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to create class " + name + "Function", e);
        }
    }

    @Override
    public NullFilter createNullFilter() {
        return new NullFilterImpl();
    }

    @Override
    public EnvironmentVariable createEnvironmentVariable(String name) {
        if (name.equalsIgnoreCase("MapScaleDenominator")) {
            return new MapScaleDenominatorImpl();
        }
        throw new RuntimeException("Unknown environment variable:" + name);
    }
}

