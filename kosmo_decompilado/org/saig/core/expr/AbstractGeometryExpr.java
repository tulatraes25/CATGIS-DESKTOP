/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.expr;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.saig.core.expr.AbstractExpr;
import org.saig.core.expr.Exprs;
import org.saig.core.expr.GeometryExpr;
import org.saig.core.expr.TopoDistanceGeometryExpr;
import org.saig.core.expr.TopoGeometryExpr;

public abstract class AbstractGeometryExpr
extends AbstractExpr
implements GeometryExpr {
    @Override
    public GeometryExpr beyond(GeometryExpr expr, double distance) {
        return new TopoDistanceGeometryExpr(this, 13, expr, distance);
    }

    @Override
    public GeometryExpr beyond(Geometry geometry, double distance) {
        return this.beyond(Exprs.literal(geometry), distance);
    }

    @Override
    public GeometryExpr beyond(Envelope extent, double distance) {
        return this.beyond(Exprs.literal(extent), distance);
    }

    @Override
    public GeometryExpr contains(GeometryExpr expr) {
        return new TopoGeometryExpr(this, 11, expr);
    }

    @Override
    public GeometryExpr contains(Geometry geometry) {
        return this.contains(Exprs.literal(geometry));
    }

    @Override
    public GeometryExpr contains(Envelope extent) {
        return this.contains(Exprs.literal(extent));
    }

    @Override
    public GeometryExpr crosses(GeometryExpr expr) {
        return new TopoGeometryExpr(this, 9, expr);
    }

    @Override
    public GeometryExpr crosses(Geometry geometry) {
        return this.crosses(Exprs.literal(geometry));
    }

    @Override
    public GeometryExpr crosses(Envelope extent) {
        return this.crosses(Exprs.literal(extent));
    }

    @Override
    public GeometryExpr disjoint(GeometryExpr expr) {
        return new TopoGeometryExpr(this, 6, expr);
    }

    @Override
    public GeometryExpr disjoint(Geometry geometry) {
        return this.disjoint(Exprs.literal(geometry));
    }

    @Override
    public GeometryExpr disjoint(Envelope extent) {
        return this.disjoint(Exprs.literal(extent));
    }

    @Override
    public GeometryExpr dwithin(GeometryExpr expr, double distance) {
        return new TopoDistanceGeometryExpr(this, 24, expr, distance);
    }

    @Override
    public GeometryExpr dwithin(Geometry geometry, double distance) {
        return this.dwithin(Exprs.literal(geometry), distance);
    }

    @Override
    public GeometryExpr dwithin(Envelope extent, double distance) {
        return this.dwithin(Exprs.literal(extent), distance);
    }

    @Override
    public GeometryExpr equal(GeometryExpr expr) {
        return new TopoGeometryExpr(this, 5, expr);
    }

    @Override
    public GeometryExpr equal(Geometry geometry) {
        return this.equal(Exprs.literal(geometry));
    }

    @Override
    public GeometryExpr equal(Envelope extent) {
        return this.equal(Exprs.literal(extent));
    }

    @Override
    public GeometryExpr intersects(GeometryExpr expr) {
        return new TopoGeometryExpr(this, 7, expr);
    }

    @Override
    public GeometryExpr intersects(Geometry geometry) {
        return this.intersects(Exprs.literal(geometry));
    }

    @Override
    public GeometryExpr intersects(Envelope extent) {
        return this.intersects(Exprs.literal(extent));
    }

    @Override
    public GeometryExpr overlaps(GeometryExpr expr) {
        return new TopoGeometryExpr(this, 12, expr);
    }

    @Override
    public GeometryExpr overlaps(Geometry geometry) {
        return this.overlaps(Exprs.literal(geometry));
    }

    @Override
    public GeometryExpr overlaps(Envelope extent) {
        return this.overlaps(Exprs.literal(extent));
    }

    @Override
    public GeometryExpr touches(GeometryExpr expr) {
        return new TopoGeometryExpr(this, 8, expr);
    }

    @Override
    public GeometryExpr touches(Geometry geometry) {
        return this.touches(Exprs.literal(geometry));
    }

    @Override
    public GeometryExpr touches(Envelope extent) {
        return this.touches(Exprs.literal(extent));
    }

    @Override
    public GeometryExpr within(GeometryExpr expr) {
        return new TopoGeometryExpr(this, 10, expr);
    }

    @Override
    public GeometryExpr within(Geometry geometry) {
        return this.within(Exprs.literal(geometry));
    }

    @Override
    public GeometryExpr within(Envelope extent) {
        return this.within(Exprs.literal(extent));
    }
}

