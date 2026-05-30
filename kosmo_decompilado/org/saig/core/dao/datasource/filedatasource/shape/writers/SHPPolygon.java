/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.writers;

import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import org.saig.core.dao.datasource.filedatasource.shape.ShapefileException;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolygon2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolygon3D;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHP;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHPMultiLine;
import org.saig.jump.lang.I18N;

public class SHPPolygon
extends SHPMultiLine {
    public SHPPolygon() {
        this.m_type = 5;
    }

    public SHPPolygon(int type) throws ShapefileException {
        if (type != 5 && type != 25 && type != 15) {
            throw new ShapefileException(I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.writers.SHPPolygon.it-is-not-type-5-15-or-25"));
        }
        this.m_type = type;
    }

    @Override
    public int getShapeType() {
        return this.m_type;
    }

    @Override
    public IShapeGeometry read(MappedByteBuffer buffer, int type) {
        if (this.m_type == 15) {
            return this.read3D(buffer, type);
        }
        double minX = buffer.getDouble();
        double minY = buffer.getDouble();
        double maxX = buffer.getDouble();
        double maxY = buffer.getDouble();
        Rectangle2D.Double rec = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - maxY);
        int numParts = buffer.getInt();
        int numPoints = buffer.getInt();
        int[] partOffsets = new int[numParts];
        int i = 0;
        while (i < numParts) {
            partOffsets[i] = buffer.getInt();
            ++i;
        }
        ShapePoint2D[] points = this.readPoints(buffer, numPoints);
        int offset = 0;
        int part = 0;
        while (part < numParts) {
            int start = partOffsets[part];
            int finish = part == numParts - 1 ? numPoints : partOffsets[part + 1];
            int length = finish - start;
            ShapePoint2D[] pointsPart = new ShapePoint2D[length];
            int i2 = 0;
            while (i2 < length) {
                pointsPart[i2] = points[offset++];
                ++i2;
            }
            ++part;
        }
        return (IShapeGeometry)((Object)new ShapePolygon2D(this.getGeneralPathX(points, partOffsets)));
    }

    private IShapeGeometry read3D(MappedByteBuffer buffer, int type) {
        double minX = buffer.getDouble();
        double minY = buffer.getDouble();
        double maxX = buffer.getDouble();
        double maxY = buffer.getDouble();
        Rectangle2D.Double rec = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - maxY);
        int numParts = buffer.getInt();
        int numPoints = buffer.getInt();
        int[] partOffsets = new int[numParts];
        int i = 0;
        while (i < numParts) {
            partOffsets[i] = buffer.getInt();
            ++i;
        }
        double[] zs = new double[numPoints];
        ShapePoint2D[] points = this.readPoints(buffer, numPoints);
        buffer.position(buffer.position() + 16);
        int t = 0;
        while (t < numPoints) {
            zs[t] = buffer.getDouble();
            ++t;
        }
        int offset = 0;
        int part = 0;
        while (part < numParts) {
            int start = partOffsets[part];
            int finish = part == numParts - 1 ? numPoints : partOffsets[part + 1];
            int length = finish - start;
            ShapePoint2D[] pointsPart = new ShapePoint2D[length];
            int i2 = 0;
            while (i2 < length) {
                pointsPart[i2] = points[offset++];
                ++i2;
            }
            ++part;
        }
        return (IShapeGeometry)((Object)new ShapePolygon3D(this.getGeneralPathX(points, partOffsets), zs));
    }

    private ShapePoint2D[] readPoints(MappedByteBuffer buffer, int numPoints) {
        ShapePoint2D[] points = new ShapePoint2D[numPoints];
        int t = 0;
        while (t < numPoints) {
            points[t] = new ShapePoint2D(buffer.getDouble(), buffer.getDouble());
            ++t;
        }
        return points;
    }

    @Override
    public void write(ByteBuffer buffer, IShapeGeometry geometry) {
        Rectangle2D rec = geometry.getBounds2D();
        buffer.putDouble(rec.getMinX());
        buffer.putDouble(rec.getMinY());
        buffer.putDouble(rec.getMaxX());
        buffer.putDouble(rec.getMaxY());
        int nparts = this.parts.length;
        int npoints = this.points.length;
        buffer.putInt(nparts);
        buffer.putInt(npoints);
        int t = 0;
        while (t < nparts) {
            buffer.putInt(this.parts[t]);
            ++t;
        }
        t = 0;
        while (t < this.points.length) {
            buffer.putDouble(this.points[t].getX());
            buffer.putDouble(this.points[t].getY());
            ++t;
        }
        if (this.m_type == 15) {
            double[] zExtreame = SHP.getZMinMax(this.zs);
            if (Double.isNaN(zExtreame[0])) {
                buffer.putDouble(0.0);
                buffer.putDouble(0.0);
            } else {
                buffer.putDouble(zExtreame[0]);
                buffer.putDouble(zExtreame[1]);
            }
            int t2 = 0;
            while (t2 < npoints) {
                double z = this.zs[t2];
                if (Double.isNaN(z)) {
                    buffer.putDouble(0.0);
                } else {
                    buffer.putDouble(z);
                }
                ++t2;
            }
        }
        if (this.m_type == 25 || this.m_type == 15) {
            buffer.putDouble(-1.0E41);
            buffer.putDouble(-1.0E41);
            t = 0;
            while (t < npoints) {
                buffer.putDouble(-1.0E41);
                ++t;
            }
        }
    }

    @Override
    public int getLength(IShapeGeometry fgeometry) {
        int length;
        int npoints = this.points.length;
        if (this.m_type == 15) {
            length = 44 + 4 * this.parts.length + 16 * npoints + 8 * npoints + 16 + 8 * npoints + 16;
        } else if (this.m_type == 25) {
            length = 44 + 4 * this.parts.length + 16 * npoints + 8 * npoints + 16;
        } else if (this.m_type == 5) {
            length = 44 + 4 * this.parts.length + 16 * npoints;
        } else {
            throw new IllegalStateException("Expected ShapeType of Polygon, got " + this.m_type);
        }
        return length;
    }
}

