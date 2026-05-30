/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.expr;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.io.IOException;
import org.saig.core.expr.AbstractGeometryExpr;
import org.saig.core.expr.Expr;
import org.saig.core.expr.GeometryExpr;
import org.saig.core.expr.LiteralExpr;
import org.saig.core.expr.LiteralGeometryExpr;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.GeometryDistanceFilter;
import org.saig.core.filter.IllegalFilterException;

class TopoDistanceGeometryExpr
extends AbstractGeometryExpr {
    GeometryExpr expr1;
    GeometryExpr expr2;
    short op;
    double distance;

    TopoDistanceGeometryExpr(GeometryExpr expr1, short op, GeometryExpr expr2, double distance) {
        this.expr1 = expr1;
        this.expr2 = expr2;
        this.op = op;
        this.distance = distance;
    }

    @Override
    public Expr eval() {
        GeometryExpr eval1 = (GeometryExpr)this.expr1.eval();
        GeometryExpr eval2 = (GeometryExpr)this.expr2.eval();
        if (eval1 instanceof LiteralGeometryExpr && eval2 instanceof LiteralGeometryExpr) {
            Geometry geom1 = ((LiteralGeometryExpr)eval1).getGeometry();
            Geometry geom2 = ((LiteralGeometryExpr)eval1).getGeometry();
            switch (this.op) {
                case 24: {
                    return new LiteralExpr(geom1.isWithinDistance(geom2, this.distance));
                }
                case 13: {
                    return new LiteralExpr(!geom1.isWithinDistance(geom2, this.distance));
                }
            }
            return new LiteralExpr(false);
        }
        if (eval1 == this.expr1 && eval2 == this.expr2) {
            return this;
        }
        return new TopoDistanceGeometryExpr(eval1, this.op, eval2, this.distance);
    }

    @Override
    public Filter filter(FeatureSchema schema) throws IOException {
        try {
            GeometryDistanceFilter filter = this.factory.createGeometryDistanceFilter(this.op);
            Expression left = this.expr1.expression(schema);
            Expression right = this.expr2.expression(schema);
            filter.addLeftGeometry(left);
            filter.addRightGeometry(right);
            filter.setDistance(this.distance);
            return filter;
        }
        catch (IllegalFilterException e) {
            return null;
        }
    }
}

