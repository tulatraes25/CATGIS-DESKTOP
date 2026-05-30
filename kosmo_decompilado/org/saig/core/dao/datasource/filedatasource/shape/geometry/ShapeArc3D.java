/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import java.awt.geom.Point2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeArc2D;

public class ShapeArc3D
extends ShapeArc2D
implements IShape3D {
    private static final long serialVersionUID = 1L;
    private double[] zs;

    public ShapeArc3D(SAIGGeneralPath gpx, Point2D i, Point2D c, Point2D e, double[] z) {
        super(gpx, i, c, e);
        this.zs = z;
    }

    @Override
    public double[] getZs() {
        return this.zs;
    }

    @Override
    public int getShapeType() {
        return 640;
    }
}

