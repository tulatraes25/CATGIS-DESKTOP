/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometry;

public class ShapeGeometry3D
extends ShapeGeometry
implements IShapeGeometry3D {
    public ShapeGeometry3D(IShape shp) {
        super(shp);
    }

    @Override
    public double[] getZs() {
        return ((IShape3D)this.shp).getZs();
    }
}

