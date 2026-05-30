/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.CoordinateSequenceFactory
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  org.opengis.referencing.FactoryException
 *  org.opengis.referencing.operation.MathTransform
 *  org.opengis.referencing.operation.TransformException
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.saig.core.renderer.lite.Decimator;
import org.saig.core.renderer.lite.EmptyIterator;
import org.saig.core.renderer.lite.GeomCollectionIterator;
import org.saig.core.renderer.lite.LineIterator2;
import org.saig.core.renderer.lite.LiteCoordinateSequence;
import org.saig.core.renderer.lite.LiteCoordinateSequenceFactory;
import org.saig.core.renderer.lite.PointIterator;
import org.saig.core.renderer.lite.PolygonIterator;

public final class LiteShape2
implements Shape,
Cloneable {
    private Geometry geometry;
    private boolean generalize = false;
    private double maxDistance = 1.0;
    private LineIterator2 lineIterator = new LineIterator2();
    private GeomCollectionIterator collIterator = new GeomCollectionIterator();
    private EmptyIterator emptyiterator = new EmptyIterator();
    private static GeometryFactory geomFac;
    private MathTransform mathTransform;

    public LiteShape2(Geometry geom, MathTransform mathTransform, Decimator decimator, boolean generalize, double maxDistance) throws TransformException, FactoryException {
        this(geom, mathTransform, decimator, generalize);
        this.maxDistance = maxDistance;
    }

    public LiteShape2(Geometry geom, MathTransform mathTransform, Decimator decimator, boolean generalize) throws TransformException, FactoryException {
        this(geom, mathTransform, decimator, generalize, true);
    }

    public LiteShape2(Geometry geom, MathTransform mathTransform, Decimator decimator, boolean generalize, boolean clone) throws TransformException, FactoryException {
        if (geom != null) {
            this.geometry = !clone && geom.getFactory().getCoordinateSequenceFactory() instanceof LiteCoordinateSequenceFactory ? geom : (geom.getFactory().getCoordinateSequenceFactory() instanceof LiteCoordinateSequenceFactory ? this.cloneGeometryLCS(geom) : this.cloneGeometry(geom));
        }
        this.mathTransform = mathTransform;
        if (decimator != null) {
            decimator.decimateTransformGeneralize(this.geometry, this.mathTransform);
        } else {
            if (mathTransform != null && !mathTransform.isIdentity() && generalize) {
                new Decimator(mathTransform.inverse()).decimate(this.geometry);
            }
            if (this.geometry != null) {
                this.transformGeometry(this.geometry);
            }
        }
        this.generalize = false;
    }

    private final Geometry cloneGeometryLCS(Polygon geom) {
        LinearRing lr = (LinearRing)this.cloneGeometryLCS((LinearRing)geom.getExteriorRing());
        LinearRing[] rings = new LinearRing[geom.getNumInteriorRing()];
        int t = 0;
        while (t < rings.length) {
            rings[t] = (LinearRing)this.cloneGeometryLCS((LinearRing)geom.getInteriorRingN(t));
            ++t;
        }
        return this.getGeometryFactory().createPolygon(lr, rings);
    }

    private final Geometry cloneGeometryLCS(Point geom) {
        return this.getGeometryFactory().createPoint((CoordinateSequence)new LiteCoordinateSequence((LiteCoordinateSequence)geom.getCoordinateSequence()));
    }

    private final Geometry cloneGeometryLCS(LineString geom) {
        return this.getGeometryFactory().createLineString((CoordinateSequence)new LiteCoordinateSequence((LiteCoordinateSequence)geom.getCoordinateSequence()));
    }

    private final Geometry cloneGeometryLCS(LinearRing geom) {
        return this.getGeometryFactory().createLinearRing((CoordinateSequence)new LiteCoordinateSequence((LiteCoordinateSequence)geom.getCoordinateSequence()));
    }

    private final Geometry cloneGeometryLCS(Geometry geom) {
        if (geom instanceof LineString) {
            return this.cloneGeometryLCS((LineString)geom);
        }
        if (geom instanceof Polygon) {
            return this.cloneGeometryLCS((Polygon)geom);
        }
        if (geom instanceof Point) {
            return this.cloneGeometryLCS((Point)geom);
        }
        return this.cloneGeometryLCS((GeometryCollection)geom);
    }

    private final Geometry cloneGeometryLCS(GeometryCollection geom) {
        if (geom.getNumGeometries() == 0) {
            Geometry[] gs = new Geometry[]{};
            return this.getGeometryFactory().createGeometryCollection(gs);
        }
        ArrayList<Geometry> gs = new ArrayList<Geometry>(geom.getNumGeometries());
        int n = geom.getNumGeometries();
        int t = 0;
        while (t < n) {
            gs.add(t, this.cloneGeometryLCS(geom.getGeometryN(t)));
            ++t;
        }
        return this.getGeometryFactory().buildGeometry(gs);
    }

    private final Geometry cloneGeometry(Polygon geom) {
        LinearRing lr = (LinearRing)this.cloneGeometry((LinearRing)geom.getExteriorRing());
        LinearRing[] rings = new LinearRing[geom.getNumInteriorRing()];
        int t = 0;
        while (t < rings.length) {
            rings[t] = (LinearRing)this.cloneGeometry((LinearRing)geom.getInteriorRingN(t));
            ++t;
        }
        return this.getGeometryFactory().createPolygon(lr, rings);
    }

    private final Geometry cloneGeometry(Point geom) {
        return this.getGeometryFactory().createPoint((CoordinateSequence)new LiteCoordinateSequence(geom.getCoordinates()));
    }

    private final Geometry cloneGeometry(LineString geom) {
        return this.getGeometryFactory().createLineString((CoordinateSequence)new LiteCoordinateSequence(geom.getCoordinates()));
    }

    private final Geometry cloneGeometry(LinearRing geom) {
        return this.getGeometryFactory().createLinearRing((CoordinateSequence)new LiteCoordinateSequence(geom.getCoordinates()));
    }

    private final Geometry cloneGeometry(Geometry geom) {
        if (geom instanceof LineString) {
            return this.cloneGeometry((LineString)geom);
        }
        if (geom instanceof Polygon) {
            return this.cloneGeometry((Polygon)geom);
        }
        if (geom instanceof Point) {
            return this.cloneGeometry((Point)geom);
        }
        return this.cloneGeometry((GeometryCollection)geom);
    }

    private final Geometry cloneGeometry(GeometryCollection geom) {
        if (geom.getNumGeometries() == 0) {
            Geometry[] gs = new Geometry[]{};
            return this.getGeometryFactory().createGeometryCollection(gs);
        }
        ArrayList<Geometry> gs = new ArrayList<Geometry>(geom.getNumGeometries());
        int n = geom.getNumGeometries();
        int t = 0;
        while (t < n) {
            gs.add(this.cloneGeometry(geom.getGeometryN(t)));
            ++t;
        }
        return this.getGeometryFactory().buildGeometry(gs);
    }

    private void transformGeometry(Geometry geometry) throws TransformException, FactoryException {
        if (this.mathTransform == null || this.mathTransform.isIdentity()) {
            return;
        }
        if (geometry instanceof GeometryCollection) {
            GeometryCollection collection = (GeometryCollection)geometry;
            int i = 0;
            while (i < collection.getNumGeometries()) {
                this.transformGeometry(collection.getGeometryN(i));
                ++i;
            }
        } else if (geometry instanceof Point) {
            LiteCoordinateSequence seq = (LiteCoordinateSequence)((Point)geometry).getCoordinateSequence();
            double[] coords = seq.getArray();
            double[] newCoords = new double[coords.length];
            this.mathTransform.transform(coords, 0, newCoords, 0, seq.size());
            seq.setArray(newCoords);
        } else if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon)geometry;
            this.transformGeometry((Geometry)polygon.getExteriorRing());
            int i = 0;
            while (i < polygon.getNumInteriorRing()) {
                this.transformGeometry((Geometry)polygon.getInteriorRingN(i));
                ++i;
            }
        } else if (geometry instanceof LineString) {
            LiteCoordinateSequence seq = (LiteCoordinateSequence)((LineString)geometry).getCoordinateSequence();
            double[] coords = seq.getArray();
            this.mathTransform.transform(coords, 0, coords, 0, seq.size());
            seq.setArray(coords);
        }
    }

    private GeometryFactory getGeometryFactory() {
        if (geomFac == null) {
            geomFac = new GeometryFactory((CoordinateSequenceFactory)new LiteCoordinateSequenceFactory());
        }
        return geomFac;
    }

    public void setGeometry(Geometry g) throws TransformException, FactoryException {
        if (g != null) {
            this.geometry = this.getGeometryFactory().createGeometry(g);
            this.transformGeometry(this.geometry);
        }
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
        Envelope env = this.geometry.getEnvelopeInternal();
        return new Rectangle2D.Double(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        PathIterator pi = null;
        if (this.geometry.isEmpty()) {
            return this.emptyiterator;
        }
        if (this.geometry instanceof Point) {
            pi = new PointIterator((Point)this.geometry, at);
        }
        if (this.geometry instanceof Polygon) {
            pi = new PolygonIterator((Polygon)this.geometry, at, this.generalize, this.maxDistance);
        } else if (this.geometry instanceof LineString) {
            this.lineIterator.init((LineString)this.geometry, at);
            pi = this.lineIterator;
        } else if (this.geometry instanceof GeometryCollection) {
            this.collIterator.init((GeometryCollection)this.geometry, at, this.generalize, this.maxDistance);
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

    public MathTransform getMathTransform() {
        return this.mathTransform;
    }

    public Geometry getGeometry() {
        return this.geometry;
    }
}

