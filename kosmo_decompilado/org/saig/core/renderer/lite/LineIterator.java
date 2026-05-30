/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import java.awt.geom.AffineTransform;
import org.saig.core.renderer.lite.AbstractLiteIterator;

class LineIterator
extends AbstractLiteIterator {
    private AffineTransform at;
    private CoordinateSequence coordinates = null;
    private int currentCoord = 0;
    private float oldX = Float.NaN;
    private float oldY = Float.NaN;
    private boolean done = false;
    private boolean isClosed;
    private boolean generalize = false;
    private float maxDistance = 1.0f;
    private float xScale;
    private float yScale;
    private int coordinateCount;
    private static final AffineTransform NO_TRANSFORM = new AffineTransform();

    public LineIterator() {
    }

    public LineIterator(LineString ls, AffineTransform at, boolean generalize, float maxDistance) {
        this.init(ls, at, generalize, maxDistance);
    }

    public void init(LineString ls, AffineTransform at, boolean generalize, float maxDistance, float xScale, float yScale) {
        this.xScale = xScale;
        this.yScale = yScale;
        this._init(ls, at, generalize, maxDistance);
    }

    public void init(LineString ls, AffineTransform at, boolean generalize, float maxDistance) {
        if (at == null) {
            at = new AffineTransform();
        }
        this._init(ls, at, generalize, maxDistance);
        this.xScale = (float)Math.sqrt(at.getScaleX() * at.getScaleX() + at.getShearX() * at.getShearX());
        this.yScale = (float)Math.sqrt(at.getScaleY() * at.getScaleY() + at.getShearY() * at.getShearY());
    }

    private void _init(LineString ls, AffineTransform at, boolean generalize, float maxDistance) {
        if (at == null) {
            at = NO_TRANSFORM;
        }
        this.at = at;
        this.coordinates = ls.getCoordinateSequence();
        this.coordinateCount = this.coordinates.size();
        this.isClosed = ls instanceof LinearRing;
        this.generalize = generalize;
        this.maxDistance = maxDistance;
        this.done = false;
        this.currentCoord = 0;
        this.oldX = Float.NaN;
        this.oldY = Float.NaN;
    }

    public void setMaxDistance(float distance) {
        this.maxDistance = distance;
    }

    public double getMaxDistance() {
        return this.maxDistance;
    }

    @Override
    public int getWindingRule() {
        return 1;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public void next() {
        if (this.currentCoord == this.coordinateCount - 1 && !this.isClosed || this.currentCoord == this.coordinateCount && this.isClosed) {
            this.done = true;
        } else if (this.generalize) {
            if (Float.isNaN(this.oldX)) {
                ++this.currentCoord;
                this.oldX = (float)this.coordinates.getX(this.currentCoord);
                this.oldY = (float)this.coordinates.getY(this.currentCoord);
            } else {
                float distx = 0.0f;
                float disty = 0.0f;
                float x = 0.0f;
                float y = 0.0f;
                do {
                    ++this.currentCoord;
                    x = (float)this.coordinates.getX(this.currentCoord);
                    y = (float)this.coordinates.getY(this.currentCoord);
                    if (this.currentCoord >= this.coordinateCount) continue;
                    distx = Math.abs(x - this.oldX);
                    disty = Math.abs(y - this.oldY);
                } while (distx * this.xScale < this.maxDistance && disty * this.yScale < this.maxDistance && (!this.isClosed && this.currentCoord < this.coordinateCount - 1 || this.isClosed && this.currentCoord < this.coordinateCount));
                this.oldX = x;
                this.oldY = y;
            }
        } else {
            ++this.currentCoord;
        }
    }

    @Override
    public int currentSegment(double[] coords) {
        if (this.currentCoord == 0) {
            coords[0] = this.coordinates.getX(0);
            coords[1] = this.coordinates.getY(0);
            this.at.transform(coords, 0, coords, 0, 1);
            return 0;
        }
        if (this.currentCoord == this.coordinateCount && this.isClosed) {
            return 4;
        }
        coords[0] = this.coordinates.getX(this.currentCoord);
        coords[1] = this.coordinates.getY(this.currentCoord);
        this.at.transform(coords, 0, coords, 0, 1);
        return 1;
    }
}

