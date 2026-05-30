/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolygon2D;

public class ShapePolygon3D
extends ShapePolygon2D
implements IShape3D {
    private static final long serialVersionUID = 1L;
    protected double[] pZ = null;

    public ShapePolygon3D(SAIGGeneralPath gpx, double[] pZ) {
        super(gpx);
        this.pZ = pZ;
    }

    @Override
    public IShape cloneShape() {
        return new ShapePolygon3D((SAIGGeneralPath)this.gp.clone(), (double[])this.pZ.clone());
    }

    @Override
    public double[] getZs() {
        return this.pZ;
    }

    @Override
    public int getShapeType() {
        return 516;
    }
}

