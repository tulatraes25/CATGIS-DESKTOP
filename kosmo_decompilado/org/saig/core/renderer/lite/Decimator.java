/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  org.opengis.referencing.operation.MathTransform
 *  org.opengis.referencing.operation.TransformException
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Rectangle;
import org.geotools.referencing.operation.matrix.AffineTransform2D;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.saig.core.renderer.lite.LiteCoordinateSequence;

public final class Decimator {
    private double spanx = -1.0;
    private double spany = -1.0;

    public Decimator(MathTransform screenToWorld, Rectangle paintArea) {
        if (screenToWorld != null) {
            double[] original = new double[]{(double)paintArea.x + (double)paintArea.width / 2.0, (double)paintArea.y + (double)paintArea.height / 2.0, (double)paintArea.x + (double)paintArea.width / 2.0 + 1.0, (double)paintArea.y + (double)paintArea.height / 2.0 + 1.0};
            double[] coords = new double[4];
            try {
                screenToWorld.transform(original, 0, coords, 0, 2);
            }
            catch (TransformException e) {
                return;
            }
            this.spanx = Math.abs(coords[0] - coords[2]) * 0.8;
            this.spany = Math.abs(coords[1] - coords[3]) * 0.8;
        } else {
            this.spanx = 1.0;
            this.spany = 1.0;
        }
    }

    public Decimator(MathTransform screenToWorld) {
        this(screenToWorld, new Rectangle());
    }

    public Decimator(double spanx, double spany) {
        this.spanx = spanx;
        this.spany = spany;
    }

    public final void decimateTransformGeneralize(Geometry geometry, MathTransform transform) throws TransformException {
        if (geometry instanceof GeometryCollection) {
            GeometryCollection collection = (GeometryCollection)geometry;
            int length = collection.getNumGeometries();
            int i = 0;
            while (i < length) {
                this.decimateTransformGeneralize(collection.getGeometryN(i), transform);
                ++i;
            }
        } else if (geometry instanceof Point) {
            LiteCoordinateSequence seq = (LiteCoordinateSequence)((Point)geometry).getCoordinateSequence();
            this.decimateTransformGeneralize(seq, transform);
        } else if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon)geometry;
            this.decimateTransformGeneralize((Geometry)polygon.getExteriorRing(), transform);
            int length = polygon.getNumInteriorRing();
            int i = 0;
            while (i < length) {
                this.decimateTransformGeneralize((Geometry)polygon.getInteriorRingN(i), transform);
                ++i;
            }
        } else if (geometry instanceof LineString) {
            LiteCoordinateSequence seq = (LiteCoordinateSequence)((LineString)geometry).getCoordinateSequence();
            this.decimateTransformGeneralize(seq, transform);
        }
    }

    public final void decimate(Geometry geom) {
        block6: {
            block7: {
                block5: {
                    if (this.spanx == -1.0) {
                        return;
                    }
                    if (geom instanceof MultiPoint) {
                        return;
                    }
                    if (!(geom instanceof GeometryCollection)) break block5;
                    GeometryCollection collection = (GeometryCollection)geom;
                    int numGeometries = collection.getNumGeometries();
                    int i = 0;
                    while (i < numGeometries) {
                        this.decimate(collection.getGeometryN(i));
                        ++i;
                    }
                    break block6;
                }
                if (!(geom instanceof LineString)) break block7;
                LineString line = (LineString)geom;
                LiteCoordinateSequence seq = (LiteCoordinateSequence)line.getCoordinateSequence();
                if (this.decimateOnEnvelope((Geometry)line, seq)) {
                    return;
                }
                this.decimate((Geometry)line, seq);
                break block6;
            }
            if (!(geom instanceof Polygon)) break block6;
            Polygon line = (Polygon)geom;
            this.decimate((Geometry)line.getExteriorRing());
            int numRings = line.getNumInteriorRing();
            int i = 0;
            while (i < numRings) {
                this.decimate((Geometry)line.getInteriorRingN(i));
                ++i;
            }
        }
    }

    private boolean decimateOnEnvelope(Geometry geom, LiteCoordinateSequence seq) {
        Envelope env = geom.getEnvelopeInternal();
        if (env.getWidth() <= this.spanx && env.getHeight() <= this.spany) {
            if (geom instanceof LinearRing) {
                this.decimateRingFully(seq);
                return true;
            }
            double[] coords = seq.getArray();
            int dim = seq.getDimension();
            double[] newcoords = new double[dim * 2];
            int i = 0;
            while (i < dim) {
                newcoords[i] = coords[i];
                newcoords[dim + i] = coords[coords.length - dim + i];
                ++i;
            }
            seq.setArray(newcoords);
            return true;
        }
        return false;
    }

    private void decimateRingFully(LiteCoordinateSequence seq) {
        double[] coords = seq.getArray();
        int dim = seq.getDimension();
        if (seq.size() <= 4) {
            return;
        }
        double[] newcoords = new double[dim * 4];
        int i = 0;
        while (i < dim) {
            newcoords[i] = coords[i];
            newcoords[dim + i] = coords[dim + i];
            newcoords[dim * 2 + i] = coords[coords.length - dim * 2 + i];
            newcoords[dim * 3 + i] = coords[coords.length - dim + i];
            ++i;
        }
        seq.setArray(newcoords);
    }

    private final void decimateTransformGeneralize(LiteCoordinateSequence seq, MathTransform transform) throws TransformException {
        int ncoords = seq.size();
        double[] coords = seq.getArray();
        if (ncoords < 2) {
            if (ncoords == 1) {
                if (transform != null) {
                    transform.transform(coords, 0, coords, 0, 1);
                    seq.setArray(coords);
                }
                return;
            }
            return;
        }
        int actualCoords = 1;
        double lastX = coords[0];
        double lastY = coords[1];
        int t = 1;
        while (t < ncoords - 1) {
            double x = coords[t * 2];
            double y = coords[t * 2 + 1];
            if (Math.abs(x - lastX) > this.spanx || Math.abs(y - lastY) > this.spany) {
                coords[actualCoords * 2] = x;
                coords[actualCoords * 2 + 1] = y;
                lastX = x;
                lastY = y;
                ++actualCoords;
            }
            ++t;
        }
        coords[actualCoords * 2] = coords[(ncoords - 1) * 2];
        coords[actualCoords * 2 + 1] = coords[(ncoords - 1) * 2 + 1];
        ++actualCoords;
        if (transform != null && !transform.isIdentity()) {
            transform.transform(coords, 0, coords, 0, actualCoords);
        }
        int actualCoordsGen = 1;
        if (!(transform instanceof AffineTransform2D)) {
            int t2 = 1;
            while (t2 < actualCoords - 1) {
                double x = coords[t2 * 2];
                double y = coords[t2 * 2 + 1];
                if (Math.abs(x - lastX) > 0.75 || Math.abs(y - lastY) > 0.75) {
                    coords[actualCoordsGen * 2] = x;
                    coords[actualCoordsGen * 2 + 1] = y;
                    lastX = x;
                    lastY = y;
                    ++actualCoordsGen;
                }
                ++t2;
            }
            coords[actualCoordsGen * 2] = coords[(actualCoords - 1) * 2];
            coords[actualCoordsGen * 2 + 1] = coords[(actualCoords - 1) * 2 + 1];
            ++actualCoordsGen;
        } else {
            actualCoordsGen = actualCoords;
        }
        if (actualCoordsGen * 2 < coords.length) {
            double[] seqDouble = new double[2 * actualCoordsGen];
            System.arraycopy(coords, 0, seqDouble, 0, actualCoordsGen * 2);
            seq.setArray(seqDouble);
        } else {
            seq.setArray(coords);
        }
    }

    private void decimate(Geometry g, LiteCoordinateSequence seq) {
        double[] coords = seq.getArray();
        int dim = seq.getDimension();
        int numDoubles = coords.length;
        int readDoubles = 0;
        int currentDoubles = 0;
        while (currentDoubles < numDoubles) {
            if (currentDoubles >= dim && currentDoubles < numDoubles - dim) {
                double prevx = coords[readDoubles - dim];
                double currx = coords[currentDoubles];
                double diffx = Math.abs(prevx - currx);
                double prevy = coords[readDoubles - dim + 1];
                double curry = coords[currentDoubles + 1];
                double diffy = Math.abs(prevy - curry);
                if (diffx > this.spanx || diffy > this.spany) {
                    readDoubles = this.copyCoordinate(coords, dim, readDoubles, currentDoubles);
                }
            } else {
                readDoubles = this.copyCoordinate(coords, dim, readDoubles, currentDoubles);
            }
            currentDoubles += dim;
        }
        if (g instanceof LinearRing && readDoubles < dim * 4) {
            this.decimateRingFully(seq);
        } else if (readDoubles < numDoubles) {
            double[] newCoords = new double[readDoubles];
            System.arraycopy(coords, 0, newCoords, 0, readDoubles);
            seq.setArray(newCoords);
        }
    }

    private int copyCoordinate(double[] coords, int dimension, int readDoubles, int currentDoubles) {
        int i = 0;
        while (i < dimension) {
            coords[readDoubles + i] = coords[currentDoubles + i];
            ++i;
        }
        return readDoubles += dimension;
    }
}

