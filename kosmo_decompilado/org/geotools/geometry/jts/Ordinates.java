/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.CoordinateSequenceFactory
 */
package org.geotools.geometry.jts;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;

class Ordinates {
    int curr;
    double[] ordinates;

    public Ordinates() {
        this.ordinates = new double[10];
        this.curr = -1;
    }

    public Ordinates(int capacity) {
        this.ordinates = new double[capacity];
        this.curr = -1;
    }

    public CoordinateSequence toCoordinateSequence(CoordinateSequenceFactory csfac) {
        CoordinateSequence cs = csfac.create(this.size(), 2);
        int i = 0;
        while (i <= this.curr) {
            cs.setOrdinate(i, 0, this.ordinates[i * 2]);
            cs.setOrdinate(i, 1, this.ordinates[i * 2 + 1]);
            ++i;
        }
        return cs;
    }

    int size() {
        return this.curr + 1;
    }

    void add(double x, double y) {
        ++this.curr;
        if (this.curr * 2 + 1 >= this.ordinates.length) {
            int newSize = this.ordinates.length * 3 / 2;
            if (newSize < 10) {
                newSize = 10;
            }
            double[] resized = new double[newSize];
            System.arraycopy(this.ordinates, 0, resized, 0, this.ordinates.length);
            this.ordinates = resized;
        }
        this.ordinates[this.curr * 2] = x;
        this.ordinates[this.curr * 2 + 1] = y;
    }

    void clear() {
        this.curr = -1;
    }

    double getOrdinate(int coordinate, int ordinate) {
        return this.ordinates[coordinate * 2 + ordinate];
    }

    public void init(CoordinateSequence cs) {
        this.clear();
        int i = 0;
        while (i < cs.size()) {
            this.add(cs.getOrdinate(i, 0), cs.getOrdinate(i, 1));
            ++i;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Ordinates[");
        int i = 0;
        while (i <= this.curr) {
            sb.append(this.ordinates[i * 2]);
            sb.append(" ");
            sb.append(this.ordinates[i * 2 + 1]);
            if (i < this.curr) {
                sb.append(";");
            }
            ++i;
        }
        sb.append("]");
        return sb.toString();
    }
}

