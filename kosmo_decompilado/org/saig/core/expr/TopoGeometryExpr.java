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
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.IllegalFilterException;

class TopoGeometryExpr
extends AbstractGeometryExpr {
    GeometryExpr expr1;
    GeometryExpr expr2;
    short op;

    TopoGeometryExpr(GeometryExpr expr1, short op, GeometryExpr expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
        this.op = op;
    }

    @Override
    public Expr eval() {
        GeometryExpr eval1 = (GeometryExpr)this.expr1.eval();
        GeometryExpr eval2 = (GeometryExpr)this.expr2.eval();
        if (eval1 instanceof LiteralGeometryExpr && eval2 instanceof LiteralGeometryExpr) {
            Geometry geom1 = ((LiteralGeometryExpr)eval1).getGeometry();
            Geometry geom2 = ((LiteralGeometryExpr)eval1).getGeometry();
            switch (this.op) {
                case 11: {
                    return new LiteralExpr(geom1.contains(geom2));
                }
                case 9: {
                    return new LiteralExpr(!geom1.crosses(geom2));
                }
                case 6: {
                    return new LiteralExpr(!geom1.disjoint(geom2));
                }
                case 5: {
                    return new LiteralExpr(!geom1.equals(geom2));
                }
                case 7: {
                    return new LiteralExpr(!geom1.intersects(geom2));
                }
                case 12: {
                    return new LiteralExpr(!geom1.overlaps(geom2));
                }
                case 8: {
                    return new LiteralExpr(!geom1.touches(geom2));
                }
                case 10: {
                    return new LiteralExpr(!geom1.within(geom2));
                }
            }
            return new LiteralExpr(false);
        }
        if (eval1 == this.expr1 && eval2 == this.expr2) {
            return this;
        }
        return new TopoGeometryExpr(eval1, this.op, eval2);
    }

    @Override
    public Filter filter(FeatureSchema schema) throws IOException {
        try {
            GeometryFilter filter = this.factory.createGeometryFilter(this.op);
            Expression left = this.expr1.expression(schema);
            Expression right = this.expr2.expression(schema);
            filter.addLeftGeometry(left);
            filter.addRightGeometry(right);
            return filter;
        }
        catch (IllegalFilterException e) {
            return null;
        }
    }
}

