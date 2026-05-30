/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 */
package com.vividsolutions.jump.workbench.ui.renderer.java2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.ShapeCollectionPathIterator;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.saig.core.util.I18NUnsupportedOperationException;

public class PolygonShape
implements Shape {
    private Polygon shell;
    private List<Polygon> holes = new ArrayList<Polygon>();

    public PolygonShape(Coordinate[] shellVertices, Collection<Coordinate[]> holeVerticesCollection) {
        this.shell = this.toPolygon(shellVertices);
        for (Coordinate[] holeVertices : holeVerticesCollection) {
            this.holes.add(this.toPolygon(holeVertices));
        }
    }

    private Polygon toPolygon(Coordinate[] coordinates) {
        Polygon polygon = new Polygon();
        int i = 0;
        while (i < coordinates.length) {
            polygon.addPoint((int)coordinates[i].x, (int)coordinates[i].y);
            ++i;
        }
        return polygon;
    }

    @Override
    public Rectangle getBounds() {
        Rectangle2D rectangle2D = this.getBounds2D();
        Rectangle rectangle = new Rectangle((int)rectangle2D.getX(), (int)rectangle2D.getY(), (int)rectangle2D.getWidth(), (int)rectangle2D.getHeight());
        return rectangle;
    }

    @Override
    public Rectangle2D getBounds2D() {
        return this.shell.getBounds2D();
    }

    @Override
    public boolean contains(double x, double y) {
        throw new I18NUnsupportedOperationException("Method contains() not yet implemented.");
    }

    @Override
    public boolean contains(Point2D p) {
        throw new UnsupportedOperationException("Method contains() not yet implemented.");
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
        ArrayList<Shape> rings = new ArrayList<Shape>();
        rings.add(this.shell);
        rings.addAll(this.holes);
        return new ShapeCollectionPathIterator(rings, at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return this.getPathIterator(at);
    }
}

