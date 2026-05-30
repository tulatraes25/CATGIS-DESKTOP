/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeMultiPoint2D;

public class ShapeMultipoint3D
extends ShapeMultiPoint2D
implements IShape3D {
    private static final long serialVersionUID = 1L;
    protected double[] z = null;

    public ShapeMultipoint3D(double[] x, double[] y, double[] z) {
        super(x, y);
        this.z = z;
    }

    @Override
    public IShape cloneShape() {
        return new ShapeMultipoint3D((double[])this.x.clone(), (double[])this.y.clone(), (double[])this.z.clone());
    }

    @Override
    public double[] getZs() {
        return this.z;
    }

    @Override
    public int getShapeType() {
        return 544;
    }
}

