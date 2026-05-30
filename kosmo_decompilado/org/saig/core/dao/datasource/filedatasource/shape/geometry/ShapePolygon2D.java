/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import java.awt.geom.Rectangle2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolyline2D;

public class ShapePolygon2D
extends ShapePolyline2D {
    private static final long serialVersionUID = 1L;

    public ShapePolygon2D(SAIGGeneralPath gpx) {
        super(gpx);
    }

    @Override
    public int getShapeType() {
        return 4;
    }

    @Override
    public IShape cloneShape() {
        return new ShapePolygon2D((SAIGGeneralPath)this.gp.clone());
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return this.gp.intersects(r);
    }
}

