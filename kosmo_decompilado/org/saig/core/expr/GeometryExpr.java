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
import org.saig.core.expr.Expr;

public interface GeometryExpr
extends Expr {
    public GeometryExpr beyond(GeometryExpr var1, double var2);

    public GeometryExpr beyond(Geometry var1, double var2);

    public GeometryExpr beyond(Envelope var1, double var2);

    public GeometryExpr contains(GeometryExpr var1);

    public GeometryExpr contains(Geometry var1);

    public GeometryExpr contains(Envelope var1);

    public GeometryExpr crosses(GeometryExpr var1);

    public GeometryExpr crosses(Geometry var1);

    public GeometryExpr crosses(Envelope var1);

    public GeometryExpr disjoint(GeometryExpr var1);

    public GeometryExpr disjoint(Geometry var1);

    public GeometryExpr disjoint(Envelope var1);

    public GeometryExpr dwithin(GeometryExpr var1, double var2);

    public GeometryExpr dwithin(Geometry var1, double var2);

    public GeometryExpr dwithin(Envelope var1, double var2);

    public GeometryExpr equal(GeometryExpr var1);

    public GeometryExpr equal(Geometry var1);

    public GeometryExpr equal(Envelope var1);

    public GeometryExpr intersects(GeometryExpr var1);

    public GeometryExpr intersects(Geometry var1);

    public GeometryExpr intersects(Envelope var1);

    public GeometryExpr overlaps(GeometryExpr var1);

    public GeometryExpr overlaps(Geometry var1);

    public GeometryExpr overlaps(Envelope var1);

    public GeometryExpr touches(GeometryExpr var1);

    public GeometryExpr touches(Geometry var1);

    public GeometryExpr touches(Envelope var1);

    public GeometryExpr within(GeometryExpr var1);

    public GeometryExpr within(Geometry var1);

    public GeometryExpr within(Envelope var1);
}

