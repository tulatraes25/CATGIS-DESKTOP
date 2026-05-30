/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import java.awt.geom.Point2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeEllipse2D;

public class ShapeEllipse3D
extends ShapeEllipse2D
implements IShape3D {
    private static final long serialVersionUID = 1L;
    private double z;

    public ShapeEllipse3D(SAIGGeneralPath gpx, Point2D i, Point2D e, double d, double z) {
        super(gpx, i, e, d);
        this.z = z;
    }

    @Override
    public int getShapeType() {
        return 768;
    }

    @Override
    public double[] getZs() {
        return new double[]{this.z};
    }
}

