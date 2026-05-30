/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.writers;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import org.saig.core.dao.datasource.filedatasource.shape.ShapefileException;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPathIterator;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint3D;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHPShape;
import org.saig.jump.lang.I18N;

public class SHPPoint
implements SHPShape {
    private int m_type;
    private ShapePoint2D point;
    private double z;

    public SHPPoint(int type) throws ShapefileException {
        if (type != 1 && type != 21 && type != 11) {
            throw new ShapefileException(I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.writers.SHPPoint.it-is-not-a-point-1-11-or-21"));
        }
        this.m_type = type;
    }

    public SHPPoint() {
        this.m_type = 1;
    }

    @Override
    public int getShapeType() {
        return this.m_type;
    }

    @Override
    public IShapeGeometry read(MappedByteBuffer buffer, int type) {
        double x = buffer.getDouble();
        double y = buffer.getDouble();
        double z = Double.NaN;
        if (this.m_type == 21) {
            buffer.getDouble();
        }
        if (this.m_type == 11) {
            z = buffer.getDouble();
            return (IShapeGeometry)((Object)new ShapePoint3D(x, y, z));
        }
        return (IShapeGeometry)((Object)new ShapePoint2D(x, y));
    }

    @Override
    public void write(ByteBuffer buffer, IShapeGeometry geometry) {
        buffer.putDouble(this.point.getX());
        buffer.putDouble(this.point.getY());
        if (this.m_type == 11) {
            if (Double.isNaN(this.z)) {
                buffer.putDouble(0.0);
            } else {
                buffer.putDouble(this.z);
            }
        }
        if (this.m_type == 11 || this.m_type == 21) {
            buffer.putDouble(-1.0E41);
        }
    }

    @Override
    public int getLength(IShapeGeometry fgeometry) {
        int length;
        if (this.m_type == 1) {
            length = 20;
        } else if (this.m_type == 21) {
            length = 28;
        } else if (this.m_type == 11) {
            length = 36;
        } else {
            throw new IllegalStateException("Expected ShapeType of Point, got" + this.m_type);
        }
        return length;
    }

    @Override
    public void obtainsPoints(IShapeGeometry g) {
        if (11 == this.m_type) {
            IShape shape = g.getShp();
            this.z = shape instanceof IShape3D ? ((IShape3D)g.getShp()).getZs()[0] : 0.0;
        }
        SAIGGeneralPathIterator theIterator = g.getGeneralPathXIterator();
        double[] theData = new double[6];
        while (!theIterator.isDone()) {
            theIterator.currentSegment(theData);
            this.point = new ShapePoint2D(theData[0], theData[1]);
            theIterator.next();
        }
    }
}

