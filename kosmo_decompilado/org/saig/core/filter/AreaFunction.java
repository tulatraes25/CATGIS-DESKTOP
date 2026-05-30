/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.geotools.algorithms.RobustGeometryProperties
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import java.util.Collections;
import java.util.Map;
import org.geotools.algorithms.RobustGeometryProperties;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.FunctionExpression;

public class AreaFunction
implements FunctionExpression {
    private Expression geom;
    private Expression[] args;
    private RobustGeometryProperties calc = new RobustGeometryProperties();

    @Override
    public short getType() {
        return 114;
    }

    @Override
    public Object getValue(Feature feature) {
        Geometry g = (Geometry)this.geom.getValue(feature);
        return new Double(this.calc.getArea(g));
    }

    @Override
    public int getArgCount() {
        return 1;
    }

    @Override
    public String getName() {
        return "Area";
    }

    @Override
    public void setArgs(Expression[] args) {
        this.geom = args[0];
        this.args = args;
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Expression[] getArgs() {
        return this.args;
    }

    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}

