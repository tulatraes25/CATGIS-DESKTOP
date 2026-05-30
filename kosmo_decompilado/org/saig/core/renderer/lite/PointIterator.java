/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Point
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Point;
import java.awt.geom.AffineTransform;
import org.saig.core.renderer.lite.AbstractLiteIterator;

public class PointIterator
extends AbstractLiteIterator {
    private AffineTransform at;
    private Point point;
    private boolean done;

    public PointIterator(Point point, AffineTransform at) {
        if (at == null) {
            at = new AffineTransform();
        }
        this.at = at;
        this.point = point;
        this.done = false;
    }

    @Override
    public int getWindingRule() {
        return 0;
    }

    @Override
    public void next() {
        this.done = true;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public int currentSegment(double[] coords) {
        coords[0] = this.point.getX();
        coords[1] = this.point.getY();
        this.at.transform(coords, 0, coords, 0, 1);
        return 0;
    }
}

