/*
 * Decompiled with CFR 0.152.
 */
package org.geotools.data.oracle.sdo;

import java.util.AbstractList;
import org.saig.jump.lang.I18N;

public class OrdinateList
extends AbstractList<Object> {
    final double[] ARRAY;
    final int OFFSET;
    final int LEN;
    final int SIZE;
    final int START;
    final int END;
    final int STEP;

    public OrdinateList(double[] array) {
        this(array, 0, 1);
    }

    public OrdinateList(double[] array, int offset, int len) {
        this(array, offset, len, 0, array.length);
    }

    public OrdinateList(double[] array, int offset, int len, int start, int end) {
        this.START = start;
        this.END = end;
        this.ARRAY = array;
        this.OFFSET = offset;
        this.LEN = len;
        this.SIZE = Math.abs(this.START - this.END) / this.LEN;
        int n = this.STEP = this.START < this.END ? this.LEN : -this.LEN;
        if (this.ARRAY.length % this.LEN != 0) {
            throw new IllegalArgumentException(I18N.getMessage("org.geotools.data.oracle.sdo.OrdinateList.you-have-requested-coordinates-of-{0}-ordinates-{1}-this-is-inconsistent-with-an-array-of-length-{2}", new Object[]{this.LEN, ". ", this.ARRAY.length}));
        }
    }

    @Override
    public Object get(int index) {
        return new Double(this.getDouble(index));
    }

    public double getDouble(int index) {
        this.rangeCheck(index);
        return this.ARRAY[this.START + this.STEP * index + this.OFFSET];
    }

    public double[] toDoubleArray() {
        double[] array = new double[this.size()];
        int i = 0;
        while (i < this.size()) {
            array[i] = this.getDouble(i);
            ++i;
        }
        return array;
    }

    private void rangeCheck(int index) {
        if (index >= this.SIZE) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.SIZE);
        }
    }

    @Override
    public int size() {
        return this.SIZE;
    }
}

