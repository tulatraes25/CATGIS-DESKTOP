/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.filedatasource.shape.writers;

import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.shape.ShapefileException;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPathIterator;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolyline2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolyline3D;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHP;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHPShape;
import org.saig.jump.lang.I18N;

public class SHPMultiLine
implements SHPShape {
    private static final Logger LOGGER = Logger.getLogger(SHPMultiLine.class);
    protected int m_type;
    protected int[] parts;
    protected ShapePoint2D[] points;
    protected double[] zs;

    public SHPMultiLine() {
        this.m_type = 3;
    }

    public SHPMultiLine(int type) throws ShapefileException {
        if (type != 3 && type != 23 && type != 13) {
            throw new ShapefileException(I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.writers.SHPMultiLine.it-is-not-type-3-13-or-23"));
        }
        this.m_type = type;
    }

    @Override
    public int getShapeType() {
        return this.m_type;
    }

    @Override
    public IShapeGeometry read(MappedByteBuffer buffer, int type) {
        if (type == 13) {
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
        ShapePoint2D[] points = new ShapePoint2D[numPoints];
        int t = 0;
        while (t < numPoints) {
            points[t] = new ShapePoint2D(buffer.getDouble(), buffer.getDouble());
            ++t;
        }
        double[] zs = new double[numPoints];
        buffer.position(buffer.position() + 16);
        int t2 = 0;
        while (t2 < numPoints) {
            zs[t2] = buffer.getDouble();
            ++t2;
        }
        return (IShapeGeometry)((Object)new ShapePolyline3D(this.getGeneralPathX(points, partOffsets), zs));
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
        ShapePoint2D[] points = new ShapePoint2D[numPoints];
        int t = 0;
        while (t < numPoints) {
            points[t] = new ShapePoint2D(buffer.getDouble(), buffer.getDouble());
            ++t;
        }
        return (IShapeGeometry)((Object)new ShapePolyline2D(this.getGeneralPathX(points, partOffsets)));
    }

    @Override
    public void write(ByteBuffer buffer, IShapeGeometry geometry) {
        Rectangle2D rec = geometry.getBounds2D();
        buffer.putDouble(rec.getMinX());
        buffer.putDouble(rec.getMinY());
        buffer.putDouble(rec.getMaxX());
        buffer.putDouble(rec.getMaxY());
        int numParts = this.parts.length;
        int npoints = this.points.length;
        buffer.putInt(numParts);
        buffer.putInt(npoints);
        int i = 0;
        while (i < numParts) {
            buffer.putInt(this.parts[i]);
            ++i;
        }
        int t = 0;
        while (t < npoints) {
            buffer.putDouble(this.points[t].getX());
            buffer.putDouble(this.points[t].getY());
            ++t;
        }
        if (this.m_type == 13) {
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
                double z = this.zs.length <= t2 ? this.zs[this.zs.length - 1] : this.zs[t2];
                if (Double.isNaN(z)) {
                    buffer.putDouble(0.0);
                } else {
                    buffer.putDouble(z);
                }
                ++t2;
            }
        }
        if (this.m_type == 23) {
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
        int numlines = this.parts.length;
        int numpoints = this.points.length;
        if (this.m_type == 3) {
            length = 44 + 4 * numlines + numpoints * 16;
        } else if (this.m_type == 23) {
            length = 44 + 4 * numlines + numpoints * 16 + 8 + 8 + 8 * numpoints;
        } else if (this.m_type == 13) {
            length = 44 + 4 * numlines + numpoints * 16 + 8 * numpoints + 8 + 8;
        } else {
            throw new IllegalStateException("Expected ShapeType of Arc, got " + this.m_type);
        }
        return length;
    }

    protected SAIGGeneralPath getGeneralPathX(ShapePoint2D[] po, int[] pa) {
        SAIGGeneralPath gPX = new SAIGGeneralPath(0, po.length);
        int j = 0;
        int i = 0;
        while (i < po.length) {
            if (i == pa[j]) {
                gPX.moveTo(po[i].getX(), po[i].getY());
                if (j < pa.length - 1) {
                    ++j;
                }
            } else {
                gPX.lineTo(po[i].getX(), po[i].getY());
            }
            ++i;
        }
        return gPX;
    }

    @Override
    public void obtainsPoints(IShapeGeometry g) {
        if (13 == this.m_type || 15 == this.m_type) {
            IShape shape = g.getShp();
            this.zs = shape instanceof IShape3D ? ((IShape3D)g.getShp()).getZs() : new double[shape.getGeneralPath().numCoords];
        }
        ArrayList<ShapePoint2D> arrayPoints = null;
        ArrayList<Integer> arrayParts = new ArrayList<Integer>();
        SAIGGeneralPathIterator theIterator = g.getGeneralPathXIterator();
        double[] theData = new double[6];
        int numParts = 0;
        while (!theIterator.isDone()) {
            int theType = theIterator.currentSegment(theData);
            switch (theType) {
                case 0: {
                    if (arrayPoints == null) {
                        arrayPoints = new ArrayList<ShapePoint2D>();
                        arrayParts.add(new Integer(0));
                    } else {
                        arrayParts.add(new Integer(arrayPoints.size()));
                    }
                    ++numParts;
                    arrayPoints.add(new ShapePoint2D(theData[0], theData[1]));
                    break;
                }
                case 1: {
                    arrayPoints.add(new ShapePoint2D(theData[0], theData[1]));
                    break;
                }
                case 2: {
                    LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.not-supported-here"));
                    break;
                }
                case 3: {
                    LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.not-supported-here"));
                    break;
                }
                case 4: {
                    ShapePoint2D firstPoint = (ShapePoint2D)arrayPoints.get(0);
                    arrayPoints.add(new ShapePoint2D(firstPoint.getX(), firstPoint.getY()));
                }
            }
            theIterator.next();
        }
        Integer[] integers = arrayParts.toArray(new Integer[0]);
        this.parts = new int[integers.length];
        int i = 0;
        while (i < integers.length) {
            this.parts[i] = integers[i];
            ++i;
        }
        this.points = arrayPoints.toArray(new ShapePoint2D[0]);
    }
}

