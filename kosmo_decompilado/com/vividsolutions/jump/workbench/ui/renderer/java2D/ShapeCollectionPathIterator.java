/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.java2D;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.Collection;
import java.util.Iterator;
import org.saig.core.util.I18NUnsupportedOperationException;

public class ShapeCollectionPathIterator
implements PathIterator {
    private Iterator<Shape> shapeIterator;
    private PathIterator currentPathIterator = new PathIterator(){

        @Override
        public int getWindingRule() {
            throw new I18NUnsupportedOperationException();
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public void next() {
        }

        @Override
        public int currentSegment(float[] coords) {
            throw new I18NUnsupportedOperationException();
        }

        @Override
        public int currentSegment(double[] coords) {
            throw new I18NUnsupportedOperationException();
        }
    };
    private AffineTransform affineTransform;
    private boolean done = false;

    public ShapeCollectionPathIterator(Collection<Shape> shapes, AffineTransform affineTransform) {
        this.shapeIterator = shapes.iterator();
        this.affineTransform = affineTransform;
        this.next();
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
        this.currentPathIterator.next();
        if (this.currentPathIterator.isDone() && !this.shapeIterator.hasNext()) {
            this.done = true;
            return;
        }
        if (this.currentPathIterator.isDone()) {
            this.currentPathIterator = this.shapeIterator.next().getPathIterator(this.affineTransform);
        }
    }

    @Override
    public int currentSegment(float[] coords) {
        return this.currentPathIterator.currentSegment(coords);
    }

    @Override
    public int currentSegment(double[] coords) {
        return this.currentPathIterator.currentSegment(coords);
    }
}

