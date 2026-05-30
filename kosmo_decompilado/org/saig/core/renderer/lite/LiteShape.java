/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateSequenceFactory
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.saig.core.renderer.lite.AbstractLiteIterator;
import org.saig.core.renderer.lite.GeomCollectionIterator;
import org.saig.core.renderer.lite.LineIterator;
import org.saig.core.renderer.lite.PointIterator;
import org.saig.core.renderer.lite.PolygonIterator;

public class LiteShape
implements Shape,
Cloneable {
    private Geometry geometry;
    private AffineTransform affineTransform = null;
    private boolean generalize = false;
    private double maxDistance = 1.0;
    private LineIterator lineIterator = new LineIterator();
    private GeomCollectionIterator collIterator = new GeomCollectionIterator();
    private float xScale;
    private float yScale;
    private GeometryFactory geomFac;

    public LiteShape(Geometry geom, AffineTransform at, boolean generalize, double maxDistance) {
        this(geom, at, generalize);
        this.maxDistance = maxDistance;
    }

    public LiteShape(Geometry geom, AffineTransform at, boolean generalize) {
        if (geom != null) {
            this.geometry = this.getGeometryFactory().createGeometry(geom);
        }
        this.affineTransform = at;
        this.generalize = generalize;
        if (at == null) {
            this.xScale = 1.0f;
            this.yScale = 1.0f;
            return;
        }
        this.xScale = (float)Math.sqrt(at.getScaleX() * at.getScaleX() + at.getShearX() * at.getShearX());
        this.yScale = (float)Math.sqrt(at.getScaleY() * at.getScaleY() + at.getShearY() * at.getShearY());
    }

    private GeometryFactory getGeometryFactory() {
        if (this.geomFac == null) {
            this.geomFac = new GeometryFactory((CoordinateSequenceFactory)new PackedCoordinateSequenceFactory());
        }
        return this.geomFac;
    }

    public void setGeometry(Geometry g) {
        this.geometry = (Geometry)g.clone();
    }

    @Override
    public boolean contains(Rectangle2D r) {
        Geometry rect = this.rectangleToGeometry(r);
        return this.geometry.contains(rect);
    }

    @Override
    public boolean contains(Point2D p) {
        Coordinate coord = new Coordinate(p.getX(), p.getY());
        Point point = this.geometry.getFactory().createPoint(coord);
        return this.geometry.contains((Geometry)point);
    }

    @Override
    public boolean contains(double x, double y) {
        Coordinate coord = new Coordinate(x, y);
        Point point = this.geometry.getFactory().createPoint(coord);
        return this.geometry.contains((Geometry)point);
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        Geometry rect = this.createRectangle(x, y, w, h);
        return this.geometry.contains(rect);
    }

    @Override
    public Rectangle getBounds() {
        double y2;
        double x2;
        Coordinate[] coords = this.geometry.getEnvelope().getCoordinates();
        double x1 = x2 = coords[0].x;
        double y1 = y2 = coords[0].y;
        int i = 1;
        while (i < 3) {
            double x = coords[i].x;
            double y = coords[i].y;
            if (x < x1) {
                x1 = x;
            }
            if (x > x2) {
                x2 = x;
            }
            if (y < y1) {
                y1 = y;
            }
            if (y > y2) {
                y2 = y;
            }
            ++i;
        }
        x1 = Math.ceil(x1);
        x2 = Math.floor(x2);
        y1 = Math.ceil(y1);
        y2 = Math.floor(y2);
        return new Rectangle((int)x1, (int)y1, (int)(x2 - x1), (int)(y2 - y1));
    }

    @Override
    public Rectangle2D getBounds2D() {
        double y2;
        double x2;
        Coordinate[] coords = this.geometry.getEnvelope().getCoordinates();
        double x1 = x2 = coords[0].x;
        double y1 = y2 = coords[0].y;
        int i = 1;
        while (i < 3) {
            double x = coords[i].x;
            double y = coords[i].y;
            if (x < x1) {
                x1 = x;
            }
            if (x > x2) {
                x2 = x;
            }
            if (y < y1) {
                y1 = y;
            }
            if (y > y2) {
                y2 = y;
            }
            ++i;
        }
        return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        AbstractLiteIterator pi = null;
        AffineTransform combined = null;
        if (this.affineTransform == null) {
            combined = at;
        } else if (at == null || at.isIdentity()) {
            combined = this.affineTransform;
        } else {
            combined = new AffineTransform(this.affineTransform);
            combined.concatenate(at);
        }
        if (this.geometry instanceof Point) {
            pi = new PointIterator((Point)this.geometry, combined);
        }
        if (this.geometry instanceof Polygon) {
            pi = new PolygonIterator((Polygon)this.geometry, combined, this.generalize, this.maxDistance);
        } else if (this.geometry instanceof LinearRing) {
            this.lineIterator.init((LineString)((LinearRing)this.geometry), combined, this.generalize, (float)this.maxDistance);
            pi = this.lineIterator;
        } else if (this.geometry instanceof LineString) {
            if (combined == this.affineTransform) {
                this.lineIterator.init((LineString)this.geometry, combined, this.generalize, (float)this.maxDistance, this.xScale, this.yScale);
            } else {
                this.lineIterator.init((LineString)this.geometry, combined, this.generalize, (float)this.maxDistance);
            }
            pi = this.lineIterator;
        } else if (this.geometry instanceof GeometryCollection) {
            this.collIterator.init((GeometryCollection)this.geometry, combined, this.generalize, this.maxDistance);
            pi = this.collIterator;
        }
        return pi;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return this.getPathIterator(at);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        Geometry rect = this.rectangleToGeometry(r);
        return this.geometry.intersects(rect);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        Geometry rect = this.createRectangle(x, y, w, h);
        return this.geometry.intersects(rect);
    }

    private Geometry rectangleToGeometry(Rectangle2D r) {
        return this.createRectangle(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
    }

    private Geometry createRectangle(double x, double y, double w, double h) {
        Coordinate[] coords = new Coordinate[]{new Coordinate(x, y), new Coordinate(x, y + h), new Coordinate(x + w, y + h), new Coordinate(x + w, y), new Coordinate(x, y)};
        LinearRing lr = this.geometry.getFactory().createLinearRing(coords);
        return this.geometry.getFactory().createPolygon(lr, null);
    }

    public AffineTransform getAffineTransform() {
        return this.affineTransform;
    }

    public Geometry getGeometry() {
        return this.geometry;
    }
}

