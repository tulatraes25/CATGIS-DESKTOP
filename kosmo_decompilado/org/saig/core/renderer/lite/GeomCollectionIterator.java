/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import org.saig.core.renderer.lite.AbstractLiteIterator;
import org.saig.core.renderer.lite.LineIterator;
import org.saig.core.renderer.lite.PointIterator;
import org.saig.core.renderer.lite.PolygonIterator;

public class GeomCollectionIterator
extends AbstractLiteIterator {
    private AffineTransform at;
    private GeometryCollection gc;
    private int currentGeom;
    private PathIterator currentIterator;
    private boolean done = false;
    private boolean generalize = false;
    private double maxDistance = 1.0;
    private LineIterator lineIterator = new LineIterator();

    public GeomCollectionIterator() {
    }

    public void init(GeometryCollection gc, AffineTransform at, boolean generalize, double maxDistance) {
        this.gc = gc;
        if (at == null) {
            at = new AffineTransform();
        }
        this.at = at;
        this.generalize = generalize;
        this.maxDistance = maxDistance;
        this.currentGeom = 0;
        this.done = false;
        this.currentIterator = this.getIterator(gc.getGeometryN(0));
    }

    public GeomCollectionIterator(GeometryCollection gc, AffineTransform at, boolean generalize, double maxDistance) {
        this.init(gc, at, generalize, maxDistance);
    }

    public void setMaxDistance(double distance) {
        this.maxDistance = distance;
    }

    public double getMaxDistance() {
        return this.maxDistance;
    }

    private AbstractLiteIterator getIterator(Geometry g) {
        AbstractLiteIterator pi = null;
        if (g instanceof Polygon) {
            Polygon p = (Polygon)g;
            pi = new PolygonIterator(p, this.at, this.generalize, this.maxDistance);
        } else if (g instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection)g;
            pi = new GeomCollectionIterator(gc, this.at, this.generalize, this.maxDistance);
        } else if (g instanceof LineString) {
            LineString ls = (LineString)g;
            this.lineIterator.init(ls, this.at, this.generalize, (float)this.maxDistance);
            pi = this.lineIterator;
        } else if (g instanceof LinearRing) {
            LinearRing lr = (LinearRing)g;
            this.lineIterator.init((LineString)lr, this.at, this.generalize, (float)this.maxDistance);
            pi = this.lineIterator;
        } else if (g instanceof Point) {
            Point p = (Point)g;
            pi = new PointIterator(p, this.at);
        }
        return pi;
    }

    @Override
    public int currentSegment(double[] coords) {
        return this.currentIterator.currentSegment(coords);
    }

    @Override
    public int currentSegment(float[] coords) {
        return this.currentIterator.currentSegment(coords);
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
        if (this.currentIterator.isDone()) {
            if (this.currentGeom < this.gc.getNumGeometries() - 1) {
                ++this.currentGeom;
                this.currentIterator = this.getIterator(this.gc.getGeometryN(this.currentGeom));
            } else {
                this.done = true;
            }
        } else {
            this.currentIterator.next();
        }
    }
}

