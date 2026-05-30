/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import java.awt.geom.Point2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeCircle2D;

public class ShapeCircle3D
extends ShapeCircle2D
implements IShape3D {
    private static final long serialVersionUID = 1L;
    private double z;

    public ShapeCircle3D(SAIGGeneralPath gpx, Point2D c, double r, double z) {
        super(gpx, c, r);
        this.z = z;
    }

    @Override
    public int getShapeType() {
        return 576;
    }

    @Override
    public double[] getZs() {
        return new double[]{this.z};
    }
}

