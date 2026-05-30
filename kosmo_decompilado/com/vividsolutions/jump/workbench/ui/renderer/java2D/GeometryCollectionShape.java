/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.java2D;

import com.vividsolutions.jump.workbench.ui.renderer.java2D.ShapeCollectionPathIterator;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.saig.core.util.I18NUnsupportedOperationException;

public class GeometryCollectionShape
implements Shape {
    private List<Shape> shapes = new ArrayList<Shape>();

    public void add(Shape shape) {
        this.shapes.add(shape);
    }

    @Override
    public Rectangle getBounds() {
        throw new I18NUnsupportedOperationException("Method getBounds() not yet implemented.");
    }

    @Override
    public Rectangle2D getBounds2D() {
        Rectangle2D rectangle = null;
        for (Shape shape : this.shapes) {
            if (rectangle == null) {
                rectangle = shape.getBounds2D();
                continue;
            }
            rectangle.add(shape.getBounds2D());
        }
        return rectangle;
    }

    @Override
    public boolean contains(double x, double y) {
        throw new I18NUnsupportedOperationException("Method contains() not yet implemented.");
    }

    @Override
    public boolean contains(Point2D p) {
        throw new I18NUnsupportedOperationException("Method contains() not yet implemented.");
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        throw new I18NUnsupportedOperationException("Method intersects() not yet implemented.");
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        throw new I18NUnsupportedOperationException("Method intersects() not yet implemented.");
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        throw new I18NUnsupportedOperationException("Method contains() not yet implemented.");
    }

    @Override
    public boolean contains(Rectangle2D r) {
        throw new I18NUnsupportedOperationException("Method contains() not yet implemented.");
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new ShapeCollectionPathIterator(this.shapes, at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return this.getPathIterator(at);
    }
}

