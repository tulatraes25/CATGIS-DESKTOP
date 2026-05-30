/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.impl.PackedCoordinateSequence
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;
import org.saig.jump.lang.I18N;

public class LiteCoordinateSequence
extends PackedCoordinateSequence {
    private double[] coords;
    private int size;

    public LiteCoordinateSequence(double[] coords) {
        this.dimension = 2;
        if (coords.length % this.dimension != 0) {
            throw new IllegalArgumentException(I18N.getString(((Object)((Object)this)).getClass(), "packed-array-does-not-contain-an-integral-number-of-coordinates"));
        }
        this.coords = coords;
        this.size = coords.length / this.dimension;
    }

    public LiteCoordinateSequence(float[] coordinates) {
        this.coords = new double[coordinates.length];
        this.dimension = 2;
        this.size = this.coords.length / this.dimension;
        int i = 0;
        while (i < coordinates.length) {
            this.coords[i] = coordinates[i];
            ++i;
        }
    }

    public LiteCoordinateSequence(Coordinate[] coordinates) {
        if (coordinates == null) {
            coordinates = new Coordinate[]{};
        }
        this.dimension = 2;
        this.coords = new double[coordinates.length * this.dimension];
        int i = 0;
        while (i < coordinates.length) {
            this.coords[i * this.dimension] = coordinates[i].x;
            if (this.dimension >= 2) {
                this.coords[i * this.dimension + 1] = coordinates[i].y;
            }
            ++i;
        }
        this.size = coordinates.length;
    }

    public LiteCoordinateSequence(int size, int dimension) {
        if (dimension != 2) {
            throw new IllegalArgumentException(I18N.getString(((Object)((Object)this)).getClass(), "this-type-of-sequence-is-always-two-dimensional"));
        }
        this.dimension = 2;
        this.coords = new double[size * this.dimension];
        this.size = this.coords.length / dimension;
    }

    public LiteCoordinateSequence(LiteCoordinateSequence seq) {
        this.dimension = seq.dimension;
        this.size = seq.size;
        double[] orig = seq.getArray();
        this.coords = new double[orig.length];
        System.arraycopy(orig, 0, this.coords, 0, this.coords.length);
    }

    public Coordinate getCoordinateInternal(int i) {
        double x = this.coords[i * this.dimension];
        double y = this.coords[i * this.dimension + 1];
        double z = this.dimension == 2 ? Double.NaN : this.coords[i * this.dimension + 2];
        return new Coordinate(x, y, z);
    }

    public int size() {
        return this.size;
    }

    public Object clone() {
        double[] clone = new double[this.coords.length];
        System.arraycopy(this.coords, 0, clone, 0, this.coords.length);
        return new LiteCoordinateSequence(clone);
    }

    public double getOrdinate(int index, int ordinate) {
        return this.coords[index * this.dimension + ordinate];
    }

    public double getX(int index) {
        return this.coords[index * this.dimension];
    }

    public double getY(int index) {
        return this.coords[index * this.dimension + 1];
    }

    public void setOrdinate(int index, int ordinate, double value) {
        this.coordRef = null;
        this.coords[index * this.dimension + ordinate] = value;
    }

    public Envelope expandEnvelope(Envelope env) {
        int i = 0;
        while (i < this.coords.length) {
            env.expandToInclude(this.coords[i], this.coords[i + 1]);
            i += this.dimension;
        }
        return env;
    }

    public double[] getArray() {
        return this.coords;
    }

    public void setArray(double[] coords2) {
        this.coords = coords2;
        this.size = this.coords.length / this.dimension;
        this.coordRef = null;
    }

    public double[] getXYArray() {
        if (this.dimension == 2) {
            return this.coords;
        }
        int n = this.size();
        double[] result = new double[n * 2];
        int t = 0;
        while (t < n) {
            result[t * 2] = this.getOrdinate(t, 0);
            result[t * 2 + 1] = this.getOrdinate(t, 1);
            ++t;
        }
        return result;
    }
}

