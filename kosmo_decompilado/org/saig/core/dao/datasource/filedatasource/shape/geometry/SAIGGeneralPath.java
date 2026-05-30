/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICoordTrans
 *  org.opengis.spatialschema.geometry.MismatchedDimensionException
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.cresques.cts.ICoordTrans;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPathIterator;
import sun.awt.geom.Crossings;
import sun.awt.geom.Curve;

public class SAIGGeneralPath
implements Shape,
Cloneable,
Serializable {
    private static final long serialVersionUID = 1L;
    public static final int WIND_EVEN_ODD = 0;
    public static final int WIND_NON_ZERO = 1;
    private static final byte SEG_MOVETO = 0;
    private static final byte SEG_LINETO = 1;
    private static final byte SEG_QUADTO = 2;
    private static final byte SEG_CUBICTO = 3;
    private static final byte SEG_CLOSE = 4;
    byte[] pointTypes;
    public double[] pointCoords;
    int numTypes;
    public int numCoords;
    int windingRule;
    static final int INIT_SIZE = 20;
    static final int EXPAND_MAX = 500;
    private double[] pz;
    private boolean _3D;

    public SAIGGeneralPath() {
        this(1, 20, 20);
    }

    public SAIGGeneralPath(int rule) {
        this(rule, 20, 20);
    }

    public SAIGGeneralPath(int rule, int initialCapacity) {
        this(rule, initialCapacity, initialCapacity);
    }

    SAIGGeneralPath(int rule, int initialTypes, int initialCoords) {
        this.setWindingRule(rule);
        this.pointTypes = new byte[initialTypes];
        this.pointCoords = new double[initialCoords * 2];
    }

    public SAIGGeneralPath(Shape s) {
        this(1, 20, 20);
        PathIterator pi = s.getPathIterator(null);
        this.setWindingRule(pi.getWindingRule());
        this.append(pi, false);
    }

    private void needRoom(int newTypes, int newCoords, boolean needMove) {
        Object[] arr;
        int grow;
        if (needMove && this.numTypes == 0) {
            throw new IllegalPathStateException("missing initial moveto in path definition");
        }
        int size = this.pointCoords.length;
        if (this.numCoords + newCoords > size) {
            grow = size;
            if (grow > 1000) {
                grow = 1000;
            }
            if (grow < newCoords) {
                grow = newCoords;
            }
            arr = new double[size + grow];
            System.arraycopy(this.pointCoords, 0, arr, 0, this.numCoords);
            this.pointCoords = arr;
        }
        if (this.numTypes + newTypes > (size = this.pointTypes.length)) {
            grow = size;
            if (grow > 500) {
                grow = 500;
            }
            if (grow < newTypes) {
                grow = newTypes;
            }
            arr = new byte[size + grow];
            System.arraycopy(this.pointTypes, 0, arr, 0, this.numTypes);
            this.pointTypes = (byte[])arr;
        }
    }

    public synchronized void moveTo(double x, double y) {
        this.needRoom(1, 2, false);
        this.pointTypes[this.numTypes++] = 0;
        this.pointCoords[this.numCoords++] = x;
        this.pointCoords[this.numCoords++] = y;
    }

    public synchronized void lineTo(double x, double y) {
        this.needRoom(1, 2, true);
        this.pointTypes[this.numTypes++] = 1;
        this.pointCoords[this.numCoords++] = x;
        this.pointCoords[this.numCoords++] = y;
    }

    public synchronized void quadTo(double x1, double y1, double x2, double y2) {
        this.needRoom(1, 4, true);
        this.pointTypes[this.numTypes++] = 2;
        this.pointCoords[this.numCoords++] = x1;
        this.pointCoords[this.numCoords++] = y1;
        this.pointCoords[this.numCoords++] = x2;
        this.pointCoords[this.numCoords++] = y2;
    }

    public synchronized void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        this.needRoom(1, 6, true);
        this.pointTypes[this.numTypes++] = 3;
        this.pointCoords[this.numCoords++] = x1;
        this.pointCoords[this.numCoords++] = y1;
        this.pointCoords[this.numCoords++] = x2;
        this.pointCoords[this.numCoords++] = y2;
        this.pointCoords[this.numCoords++] = x3;
        this.pointCoords[this.numCoords++] = y3;
    }

    public synchronized void closePath() {
        if (this.numTypes == 0 || this.pointTypes[this.numTypes - 1] != 4) {
            this.needRoom(1, 0, true);
            this.pointTypes[this.numTypes++] = 4;
        }
    }

    public void append(Shape s, boolean connect) {
        PathIterator pi = s.getPathIterator(null);
        this.append(pi, connect);
    }

    public void append(PathIterator pi, boolean connect) {
        double[] coords = new double[6];
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
                case 0: {
                    if (!connect || this.numTypes < 1 || this.numCoords < 2) {
                        this.moveTo(coords[0], coords[1]);
                        break;
                    }
                    if (this.pointTypes[this.numTypes - 1] != 4 && this.pointCoords[this.numCoords - 2] == coords[0] && this.pointCoords[this.numCoords - 1] == coords[1]) break;
                }
                case 1: {
                    this.lineTo(coords[0], coords[1]);
                    break;
                }
                case 2: {
                    this.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                }
                case 3: {
                    this.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;
                }
                case 4: {
                    this.closePath();
                }
            }
            pi.next();
            connect = false;
        }
    }

    public synchronized int getWindingRule() {
        return this.windingRule;
    }

    public void setWindingRule(int rule) {
        if (rule != 0 && rule != 1) {
            throw new IllegalArgumentException("winding rule must be WIND_EVEN_ODD or WIND_NON_ZERO");
        }
        this.windingRule = rule;
    }

    /*
     * Enabled aggressive block sorting
     */
    public synchronized Point2D getCurrentPoint() {
        if (this.numTypes < 1) return null;
        if (this.numCoords < 2) {
            return null;
        }
        int index = this.numCoords;
        if (this.pointTypes[this.numTypes - 1] != 4) return new Point2D.Double(this.pointCoords[index - 2], this.pointCoords[index - 1]);
        int i = this.numTypes - 2;
        while (i > 0) {
            switch (this.pointTypes[i]) {
                case 0: {
                    return new Point2D.Double(this.pointCoords[index - 2], this.pointCoords[index - 1]);
                }
                case 1: {
                    index -= 2;
                    break;
                }
                case 2: {
                    index -= 4;
                    break;
                }
                case 3: {
                    index -= 6;
                }
            }
            --i;
        }
        return new Point2D.Double(this.pointCoords[index - 2], this.pointCoords[index - 1]);
    }

    public synchronized void reset() {
        this.numCoords = 0;
        this.numTypes = 0;
    }

    public void transform(AffineTransform at) {
        at.transform(this.pointCoords, 0, this.pointCoords, 0, this.numCoords / 2);
    }

    public synchronized Shape createTransformedShape(AffineTransform at) {
        SAIGGeneralPath gp = (SAIGGeneralPath)this.clone();
        if (at != null) {
            gp.transform(at);
        }
        return gp;
    }

    @Override
    public Rectangle getBounds() {
        return this.getBounds2D().getBounds();
    }

    @Override
    public synchronized Rectangle2D getBounds2D() {
        double x1;
        double x2;
        double y1;
        double y2;
        int i = this.numCoords;
        if (i > 0) {
            y1 = y2 = this.pointCoords[--i];
            x1 = x2 = this.pointCoords[--i];
            while (i > 0) {
                double x;
                double y = this.pointCoords[--i];
                if ((x = this.pointCoords[--i]) < x1) {
                    x1 = x;
                }
                if (y < y1) {
                    y1 = y;
                }
                if (x > x2) {
                    x2 = x;
                }
                if (!(y > y2)) continue;
                y2 = y;
            }
        } else {
            y2 = 0.0;
            x2 = 0.0;
            y1 = 0.0;
            x1 = 0.0;
        }
        return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    public boolean contains(double x, double y) {
        if (this.numTypes < 2) {
            return false;
        }
        int cross = Curve.pointCrossingsForPath(this.getPathIterator(null), x, y);
        if (this.windingRule == 1) {
            return cross != 0;
        }
        return (cross & 1) != 0;
    }

    @Override
    public boolean contains(Point2D p) {
        return this.contains(p.getX(), p.getY());
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        Crossings c = Crossings.findCrossings(this.getPathIterator(null), x, y, x + w, y + h);
        return c != null && c.covers(y, y + h);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return this.contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        Crossings c = Crossings.findCrossings(this.getPathIterator(null), x, y, x + w, y + h);
        return c == null || !c.isEmpty();
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return this.intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new SAIGGeneralPathIterator(this, at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(this.getPathIterator(at), flatness);
    }

    public Object clone() {
        try {
            SAIGGeneralPath copy = (SAIGGeneralPath)super.clone();
            copy.pointTypes = (byte[])this.pointTypes.clone();
            copy.pointCoords = (double[])this.pointCoords.clone();
            return copy;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    SAIGGeneralPath(int windingRule, byte[] pointTypes, int numTypes, double[] pointCoords, int numCoords) {
        this.windingRule = windingRule;
        this.pointTypes = pointTypes;
        this.numTypes = numTypes;
        this.pointCoords = pointCoords;
        this.numCoords = numCoords;
    }

    public void flip() {
        byte[] pointTypesAux = new byte[this.numTypes];
        double[] pointCoordsAux = new double[this.numCoords];
        int i = 0;
        while (i < this.numTypes) {
            pointTypesAux[this.numTypes - i - 1] = this.pointTypes[i];
            ++i;
        }
        int numPoints = this.numCoords / 2;
        i = 0;
        while (i < numPoints) {
            pointCoordsAux[2 * (numPoints - i - 1)] = this.pointCoords[2 * i];
            pointCoordsAux[2 * (numPoints - i - 1) + 1] = this.pointCoords[2 * i + 1];
            ++i;
        }
        this.pointTypes = pointTypesAux;
        this.pointCoords = pointCoordsAux;
    }

    public void reProject(ICoordTrans ct) {
        Point2D pt = new Point2D.Double();
        int i = 0;
        while (i < this.numCoords) {
            pt.setLocation(this.pointCoords[i], this.pointCoords[i + 1]);
            try {
                pt = ct.convert(pt, null);
            }
            catch (MismatchedDimensionException e) {
                e.printStackTrace();
            }
            this.pointCoords[i] = pt.getX();
            this.pointCoords[i + 1] = pt.getY();
            i += 2;
        }
    }

    public boolean is_3D() {
        return this._3D;
    }

    public void set_3D(boolean _3d) {
        this._3D = _3d;
    }

    public double[] getPz() {
        return this.pz;
    }

    public void setPz(double[] pz) {
        this.pz = pz;
    }
}

