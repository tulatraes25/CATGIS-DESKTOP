/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Polygon
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.geom.AffineTransform;
import org.saig.core.renderer.lite.AbstractLiteIterator;

class PolygonIterator
extends AbstractLiteIterator {
    private AffineTransform at;
    private LineString[] rings;
    private int currentRing = 0;
    private int currentCoord = 0;
    private CoordinateSequence coords = null;
    private Coordinate oldCoord = null;
    private boolean done = false;
    private boolean generalize = false;
    private double maxDistance = 1.0;
    private double xScale;
    private double yScale;

    public PolygonIterator(Polygon p, AffineTransform at) {
        int numInteriorRings = p.getNumInteriorRing();
        this.rings = new LineString[numInteriorRings + 1];
        this.rings[0] = p.getExteriorRing();
        int i = 0;
        while (i < numInteriorRings) {
            this.rings[i + 1] = p.getInteriorRingN(i);
            ++i;
        }
        if (at == null) {
            at = new AffineTransform();
        }
        this.at = at;
        this.xScale = Math.sqrt(at.getScaleX() * at.getScaleX() + at.getShearX() * at.getShearX());
        this.yScale = Math.sqrt(at.getScaleY() * at.getScaleY() + at.getShearY() * at.getShearY());
        this.coords = this.rings[0].getCoordinateSequence();
    }

    public PolygonIterator(Polygon p, AffineTransform at, boolean generalize) {
        this(p, at);
        this.generalize = generalize;
    }

    public PolygonIterator(Polygon p, AffineTransform at, boolean generalize, double maxDistance) {
        this(p, at, generalize);
        this.maxDistance = maxDistance;
    }

    public void setMaxDistance(double distance) {
        this.maxDistance = distance;
    }

    public double getMaxDistance() {
        return this.maxDistance;
    }

    @Override
    public int currentSegment(double[] coords) {
        if (this.currentCoord == 0) {
            coords[0] = this.coords.getX(0);
            coords[1] = this.coords.getY(0);
            this.transform(coords, 0, coords, 0, 1);
            return 0;
        }
        if (this.currentCoord == this.coords.size()) {
            return 4;
        }
        coords[0] = this.coords.getX(this.currentCoord);
        coords[1] = this.coords.getY(this.currentCoord);
        this.transform(coords, 0, coords, 0, 1);
        return 1;
    }

    protected void transform(double[] src, int index, double[] dest, int destIndex, int numPoints) {
        this.at.transform(src, index, dest, destIndex, numPoints);
    }

    @Override
    public int getWindingRule() {
        return 0;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public void next() {
        if (this.currentCoord == this.coords.size()) {
            if (this.currentRing < this.rings.length - 1) {
                this.currentCoord = 0;
                ++this.currentRing;
                this.coords = this.rings[this.currentRing].getCoordinateSequence();
            } else {
                this.done = true;
            }
        } else if (this.generalize) {
            if (this.oldCoord == null) {
                ++this.currentCoord;
                this.oldCoord = this.coords.getCoordinate(this.currentCoord);
            } else {
                double distx = 0.0;
                double disty = 0.0;
                do {
                    ++this.currentCoord;
                    if (this.currentCoord >= this.coords.size()) continue;
                    distx = Math.abs(this.coords.getX(this.currentCoord) - this.oldCoord.x);
                    disty = Math.abs(this.coords.getY(this.currentCoord) - this.oldCoord.y);
                } while (distx * this.xScale < this.maxDistance && disty * this.yScale < this.maxDistance && this.currentCoord < this.coords.size());
                this.oldCoord = this.currentCoord < this.coords.size() ? this.coords.getCoordinate(this.currentCoord) : null;
            }
        } else {
            ++this.currentCoord;
        }
    }
}

