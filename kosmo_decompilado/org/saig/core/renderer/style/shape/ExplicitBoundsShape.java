/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style.shape;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ExplicitBoundsShape
implements Shape {
    private Shape shape;
    private Rectangle2D bounds = null;

    public ExplicitBoundsShape(Shape shape) {
        if (shape == null) {
            throw new IllegalArgumentException("Shape can't be null.");
        }
        this.shape = shape;
    }

    public void setBounds(Rectangle2D bounds) {
        this.bounds = bounds;
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return this.shape.contains(x, y, w, h);
    }

    @Override
    public boolean contains(double x, double y) {
        return this.shape.contains(x, y);
    }

    @Override
    public boolean contains(Point2D p) {
        return this.shape.contains(p);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return this.shape.contains(r);
    }

    @Override
    public Rectangle getBounds() {
        if (this.bounds != null) {
            return new Rectangle((int)this.bounds.getMinX(), (int)this.bounds.getMinY(), (int)this.bounds.getWidth(), (int)this.bounds.getHeight());
        }
        return this.shape.getBounds();
    }

    @Override
    public Rectangle2D getBounds2D() {
        if (this.bounds != null) {
            return this.bounds;
        }
        return this.shape.getBounds2D();
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return this.shape.getPathIterator(at, flatness);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return this.shape.getPathIterator(at);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return this.shape.intersects(x, y, w, h);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return this.shape.intersects(r);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ExplicitBoundsShape) {
            ExplicitBoundsShape other = (ExplicitBoundsShape)obj;
            boolean result = this.shape.equals(other.shape);
            if (this.bounds == null) {
                return result & other.bounds == null;
            }
            return result & this.bounds.equals(other.bounds);
        }
        if (obj instanceof Shape) {
            if (this.bounds == null) {
                return this.shape.equals(obj);
            }
            return false;
        }
        return super.equals(obj);
    }

    public int hashCode() {
        int hascode = this.shape.hashCode();
        if (this.bounds != null) {
            hascode += hascode * 37 + this.bounds.hashCode();
        }
        return hascode;
    }
}

