/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.lite;

import java.awt.geom.PathIterator;

public abstract class AbstractLiteIterator
implements PathIterator {
    protected double[] dcoords = new double[2];

    @Override
    public int currentSegment(float[] coords) {
        int result = this.currentSegment(this.dcoords);
        coords[0] = (float)this.dcoords[0];
        coords[1] = (float)this.dcoords[1];
        return result;
    }
}

