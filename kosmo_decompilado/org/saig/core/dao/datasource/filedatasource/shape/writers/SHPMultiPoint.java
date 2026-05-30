/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.writers;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import org.saig.core.dao.datasource.filedatasource.shape.ShapefileException;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPathIterator;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeMultiPoint2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHP;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHPShape;
import org.saig.jump.lang.I18N;

public class SHPMultiPoint
implements SHPShape {
    protected int m_type;
    protected int numpoints;
    protected Point2D[] points;
    protected double[] zs;

    public SHPMultiPoint() {
        this.m_type = 8;
    }

    public SHPMultiPoint(int type) throws ShapefileException {
        if (type != 8 && type != 28 && type != 18) {
            throw new ShapefileException(I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.writers.SHPMultiPoint.it-is-not-type-8-18-or-28"));
        }
        this.m_type = type;
    }

    @Override
    public int getShapeType() {
        return this.m_type;
    }

    @Override
    public IShapeGeometry read(MappedByteBuffer buffer, int type) {
        double minX = buffer.getDouble();
        double minY = buffer.getDouble();
        double maxX = buffer.getDouble();
        double maxY = buffer.getDouble();
        Rectangle2D.Double rec = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - maxY);
        int numpoints = buffer.getInt();
        ShapePoint2D[] p = new ShapePoint2D[numpoints];
        int t = 0;
        while (t < numpoints) {
            double x = buffer.getDouble();
            double y = buffer.getDouble();
            p[t] = new ShapePoint2D(x, y);
            ++t;
        }
        return (IShapeGeometry)((Object)new ShapeMultiPoint2D(p));
    }

    @Override
    public void write(ByteBuffer buffer, IShapeGeometry geometry) {
        int p = buffer.position();
        Rectangle2D box = geometry.getBounds2D();
        buffer.putDouble(box.getMinX());
        buffer.putDouble(box.getMinY());
        buffer.putDouble(box.getMaxX());
        buffer.putDouble(box.getMaxY());
        buffer.putInt(this.numpoints);
        int t = 0;
        int tt = this.numpoints;
        while (t < tt) {
            Point2D point = this.points[t];
            buffer.putDouble(point.getX());
            buffer.putDouble(point.getY());
            ++t;
        }
        if (this.m_type == 18) {
            double[] zExtreame = SHP.getZMinMax(this.zs);
            if (Double.isNaN(zExtreame[0])) {
                buffer.putDouble(0.0);
                buffer.putDouble(0.0);
            } else {
                buffer.putDouble(zExtreame[0]);
                buffer.putDouble(zExtreame[1]);
            }
            int t2 = 0;
            while (t2 < this.numpoints) {
                double z = this.zs[t2];
                if (Double.isNaN(z)) {
                    buffer.putDouble(0.0);
                } else {
                    buffer.putDouble(z);
                }
                ++t2;
            }
        }
        if (this.m_type == 28 || this.m_type == 18) {
            buffer.putDouble(-1.0E41);
            buffer.putDouble(-1.0E41);
            t = 0;
            while (t < this.numpoints) {
                buffer.putDouble(-1.0E41);
                ++t;
            }
        }
    }

    @Override
    public int getLength(IShapeGeometry fgeometry) {
        int length;
        if (this.m_type == 8) {
            length = this.numpoints * 16 + 40;
        } else if (this.m_type == 28) {
            length = this.numpoints * 16 + 40 + 16 + 8 * this.numpoints;
        } else if (this.m_type == 18) {
            length = this.numpoints * 16 + 40 + 16 + 8 * this.numpoints;
        } else {
            throw new IllegalStateException("Expected ShapeType of Arc, got " + this.m_type);
        }
        return length;
    }

    @Override
    public void obtainsPoints(IShapeGeometry g) {
        if (18 == this.m_type) {
            IShape shape = g.getShp();
            this.zs = shape instanceof IShape3D ? ((IShape3D)g.getShp()).getZs() : new double[shape.getGeneralPath().numCoords];
        }
        SAIGGeneralPathIterator theIterator = g.getGeneralPathXIterator();
        double[] theData = new double[6];
        ArrayList<Point2D.Double> ps = new ArrayList<Point2D.Double>();
        while (!theIterator.isDone()) {
            theIterator.currentSegment(theData);
            ps.add(new Point2D.Double(theData[0], theData[1]));
            theIterator.next();
        }
        this.points = ps.toArray(new Point2D.Double[0]);
        this.numpoints = this.points.length;
    }
}

