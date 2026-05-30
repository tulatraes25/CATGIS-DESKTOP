/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;

public class SAIGGeneralPathIterator
implements PathIterator {
    int typeIdx = 0;
    int pointIdx = 0;
    SAIGGeneralPath path;
    AffineTransform affine;
    private static final int[] curvesize;

    static {
        int[] nArray = new int[5];
        nArray[0] = 2;
        nArray[1] = 2;
        nArray[2] = 4;
        nArray[3] = 6;
        curvesize = nArray;
    }

    public SAIGGeneralPathIterator(SAIGGeneralPath path) {
        this(path, null);
    }

    public SAIGGeneralPathIterator(SAIGGeneralPath path, AffineTransform at) {
        this.path = path;
        this.affine = at;
    }

    @Override
    public int getWindingRule() {
        return this.path.getWindingRule();
    }

    @Override
    public boolean isDone() {
        return this.typeIdx >= this.path.numTypes;
    }

    @Override
    public void next() {
        byte type = this.path.pointTypes[this.typeIdx++];
        this.pointIdx += curvesize[type];
    }

    @Override
    public int currentSegment(float[] coords) {
        byte type = this.path.pointTypes[this.typeIdx];
        int numCoords = curvesize[type];
        if (numCoords > 0 && this.affine != null) {
            this.affine.transform(this.path.pointCoords, this.pointIdx, coords, 0, numCoords / 2);
        } else {
            int i = 0;
            while (i < numCoords) {
                coords[i] = (float)this.path.pointCoords[this.pointIdx + i];
                ++i;
            }
        }
        return type;
    }

    @Override
    public int currentSegment(double[] coords) {
        byte type = this.path.pointTypes[this.typeIdx];
        int numCoords = curvesize[type];
        if (numCoords > 0 && this.affine != null) {
            this.affine.transform(this.path.pointCoords, this.pointIdx, coords, 0, numCoords / 2);
        } else {
            System.arraycopy(this.path.pointCoords, this.pointIdx, coords, 0, numCoords);
        }
        return type;
    }
}

