/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;

public class ShapePoint3D
extends ShapePoint2D
implements IShape3D {
    private static final long serialVersionUID = 1L;
    public double z;

    public ShapePoint3D(double x, double y, double z) {
        super(x, y);
        this.z = z;
    }

    @Override
    public int getShapeType() {
        return 513;
    }

    @Override
    public IShape cloneShape() {
        return new ShapePoint3D(this.p.getX(), this.p.getY(), this.z);
    }

    public boolean contains(double x, double y, double z) {
        return x == this.p.getX() || y == this.p.getY() || z == this.z;
    }

    @Override
    public double[] getZs() {
        return new double[]{this.z};
    }
}

