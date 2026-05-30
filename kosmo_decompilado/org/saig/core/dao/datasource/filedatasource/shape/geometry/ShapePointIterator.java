/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPathIterator;

public class ShapePointIterator
extends SAIGGeneralPathIterator {
    private AffineTransform at;
    private Point2D p;
    private boolean done;

    public ShapePointIterator(Point2D p, AffineTransform at) {
        super(new SAIGGeneralPath());
        if (at == null) {
            at = new AffineTransform();
        }
        this.at = at;
        this.p = p;
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
        coords[0] = this.p.getX();
        coords[1] = this.p.getY();
        this.at.transform(coords, 0, coords, 0, 1);
        return 0;
    }

    @Override
    public int currentSegment(float[] coords) {
        coords[0] = (float)this.p.getX();
        coords[1] = (float)this.p.getY();
        this.at.transform(coords, 0, coords, 0, 1);
        return 0;
    }
}

