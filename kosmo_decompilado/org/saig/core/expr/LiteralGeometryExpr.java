/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LinearRing
 */
package org.saig.core.expr;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jump.feature.FeatureSchema;
import org.saig.core.expr.AbstractGeometryExpr;
import org.saig.core.expr.ResolvedExpr;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;

public class LiteralGeometryExpr
extends AbstractGeometryExpr
implements ResolvedExpr {
    static final GeometryFactory geomFactory = new GeometryFactory();
    Geometry geom;

    public LiteralGeometryExpr(Envelope extent) {
        Coordinate[] points;
        points = new Coordinate[]{new Coordinate(extent.getMinX(), extent.getMinY()), new Coordinate(extent.getMinX(), extent.getMaxY()), new Coordinate(extent.getMaxX(), extent.getMaxY()), new Coordinate(extent.getMaxX(), extent.getMinY()), points[0]};
        LinearRing ring = geomFactory.createLinearRing(points);
        this.geom = geomFactory.createPolygon(ring, new LinearRing[0]);
    }

    public LiteralGeometryExpr(Geometry geom) {
        this.geom = geom;
    }

    @Override
    public Expression expression(FeatureSchema schema) {
        try {
            return this.factory.createLiteralExpression(this.geom);
        }
        catch (IllegalFilterException e) {
            return null;
        }
    }

    @Override
    public Object getValue() {
        return this.geom;
    }

    public Geometry getGeometry() {
        return this.geom;
    }
}

