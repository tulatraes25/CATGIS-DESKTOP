/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.CoordinateSequenceFactory
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 */
package org.geotools.geometry.jts;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.List;
import org.geotools.geometry.jts.Ordinates;

public class GeometryClipper {
    private static int RIGHT = 2;
    private static int TOP = 8;
    private static int BOTTOM = 4;
    private static int LEFT = 1;
    final double xmin;
    final double ymin;
    final double xmax;
    final double ymax;
    final Envelope bounds;

    public Envelope getBounds() {
        return this.bounds;
    }

    public GeometryClipper(Envelope bounds) {
        this.xmin = bounds.getMinX();
        this.ymin = bounds.getMinY();
        this.xmax = bounds.getMaxX();
        this.ymax = bounds.getMaxY();
        this.bounds = bounds;
    }

    public Geometry clip(Geometry g, boolean ensureValid) {
        if (g == null) {
            return null;
        }
        Envelope geomEnvelope = g.getEnvelopeInternal();
        if (geomEnvelope.isNull()) {
            return null;
        }
        if (this.bounds.contains(geomEnvelope)) {
            return g;
        }
        if (!this.bounds.intersects(geomEnvelope)) {
            return null;
        }
        if (g instanceof LineString) {
            return this.clipLineString((LineString)g);
        }
        if (g instanceof Polygon) {
            if (ensureValid) {
                GeometryFactory gf = g.getFactory();
                CoordinateSequenceFactory csf = gf.getCoordinateSequenceFactory();
                return g.intersection((Geometry)gf.createPolygon(this.buildBoundsString(gf, csf), null));
            }
            return this.clipPolygon((Polygon)g);
        }
        if (g instanceof GeometryCollection) {
            return this.clipCollection((GeometryCollection)g, ensureValid);
        }
        return g;
    }

    private int computeOutCode(double x, double y, double xmin, double ymin, double xmax, double ymax) {
        int code = 0;
        if (y > ymax) {
            code |= TOP;
        } else if (y < ymin) {
            code |= BOTTOM;
        }
        if (x > xmax) {
            code |= RIGHT;
        } else if (x < xmin) {
            code |= LEFT;
        }
        return code;
    }

    private double[] clipSegment(double[] segment) {
        double x0 = segment[0];
        double y0 = segment[1];
        double x1 = segment[2];
        double y1 = segment[3];
        int outcode0 = this.computeOutCode(x0, y0, this.xmin, this.ymin, this.xmax, this.ymax);
        int outcode1 = this.computeOutCode(x1, y1, this.xmin, this.ymin, this.xmax, this.ymax);
        int step = 0;
        do {
            double y;
            double x;
            int outcodeOut;
            if ((outcode0 | outcode1) == 0) {
                if (x0 == x1 && y0 == y1) {
                    return null;
                }
                segment[0] = x0;
                segment[1] = y0;
                segment[2] = x1;
                segment[3] = y1;
                return segment;
            }
            if ((outcode0 & outcode1) > 0) {
                return null;
            }
            int n = outcodeOut = outcode0 != 0 ? outcode0 : outcode1;
            if ((outcodeOut & TOP) > 0) {
                x = x0 + (x1 - x0) * (this.ymax - y0) / (y1 - y0);
                y = this.ymax;
            } else if ((outcodeOut & BOTTOM) > 0) {
                x = x0 + (x1 - x0) * (this.ymin - y0) / (y1 - y0);
                y = this.ymin;
            } else if ((outcodeOut & RIGHT) > 0) {
                y = y0 + (y1 - y0) * (this.xmax - x0) / (x1 - x0);
                x = this.xmax;
            } else {
                y = y0 + (y1 - y0) * (this.xmin - x0) / (x1 - x0);
                x = this.xmin;
            }
            if (outcodeOut == outcode0) {
                x0 = x;
                y0 = y;
                outcode0 = this.computeOutCode(x0, y0, this.xmin, this.ymin, this.xmax, this.ymax);
                continue;
            }
            x1 = x;
            y1 = y;
            outcode1 = this.computeOutCode(x1, y1, this.xmin, this.ymin, this.xmax, this.ymax);
        } while (++step < 5);
        throw new RuntimeException("Algorithm did not converge");
    }

    private boolean outside(double x0, double y0, double x1, double y1) {
        int outcode1;
        int outcode0 = this.computeOutCode(x0, y0, this.xmin, this.ymin, this.xmax, this.ymax);
        return (outcode0 & (outcode1 = this.computeOutCode(x1, y1, this.xmin, this.ymin, this.xmax, this.ymax))) > 0;
    }

    private boolean contained(double x, double y) {
        return x > this.xmin && x < this.xmax && y > this.ymin && y < this.ymax;
    }

    private Geometry clipPolygon(Polygon polygon) {
        GeometryFactory gf = polygon.getFactory();
        LinearRing exterior = (LinearRing)polygon.getExteriorRing();
        LinearRing shell = this.polygonClip(exterior);
        if (shell == null || shell.isEmpty()) {
            return null;
        }
        ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
        int i = 0;
        while (i < polygon.getNumInteriorRing()) {
            LinearRing hole = (LinearRing)polygon.getInteriorRingN(i);
            if ((hole = this.polygonClip(hole)) != null && !hole.isEmpty()) {
                holes.add(hole);
            }
            ++i;
        }
        return gf.createPolygon(shell, holes.toArray(new LinearRing[holes.size()]));
    }

    private LinearRing polygonClip(LinearRing ring) {
        double INFINITY = Double.MAX_VALUE;
        CoordinateSequence cs = ring.getCoordinateSequence();
        Ordinates out = new Ordinates();
        int i = 0;
        while (i < cs.size() - 1) {
            double tOut2;
            double tOut1;
            double yOut;
            double yIn;
            double xOut;
            double xIn;
            double x0 = cs.getOrdinate(i, 0);
            double x1 = cs.getOrdinate(i + 1, 0);
            double y0 = cs.getOrdinate(i, 1);
            double y1 = cs.getOrdinate(i + 1, 1);
            double deltaX = x1 - x0;
            double deltaY = y1 - y0;
            if (deltaX > 0.0 || deltaX == 0.0 && x0 > this.xmax) {
                xIn = this.xmin;
                xOut = this.xmax;
            } else {
                xIn = this.xmax;
                xOut = this.xmin;
            }
            if (deltaY > 0.0 || deltaY == 0.0 && y0 > this.ymax) {
                yIn = this.ymin;
                yOut = this.ymax;
            } else {
                yIn = this.ymax;
                yOut = this.ymin;
            }
            double tOutX = deltaX != 0.0 ? (xOut - x0) / deltaX : (x0 <= this.xmax && this.xmin <= x0 ? Double.MAX_VALUE : -1.7976931348623157E308);
            double tOutY = deltaY != 0.0 ? (yOut - y0) / deltaY : (y0 <= this.ymax && this.ymin <= y0 ? Double.MAX_VALUE : -1.7976931348623157E308);
            if (tOutX < tOutY) {
                tOut1 = tOutX;
                tOut2 = tOutY;
            } else {
                tOut1 = tOutY;
                tOut2 = tOutX;
            }
            if (tOut2 > 0.0) {
                double tIn2;
                double tIn1;
                double tInY;
                double tInX = deltaX != 0.0 ? (xIn - x0) / deltaX : -1.7976931348623157E308;
                if (tInX < (tInY = deltaY != 0.0 ? (yIn - y0) / deltaY : -1.7976931348623157E308)) {
                    tIn1 = tInX;
                    tIn2 = tInY;
                } else {
                    tIn1 = tInY;
                    tIn2 = tInX;
                }
                if (tOut1 < tIn2) {
                    if (0.0 < tOut1 && tOut1 <= 1.0) {
                        if (tInX < tInY) {
                            out.add(xOut, yIn);
                        } else {
                            out.add(xIn, yOut);
                        }
                    }
                } else if (0.0 < tOut1 && tIn2 <= 1.0) {
                    if (0.0 <= tIn2) {
                        if (tInX > tInY) {
                            out.add(xIn, y0 + tInX * deltaY);
                        } else {
                            out.add(x0 + tInY * deltaX, yIn);
                        }
                    }
                    if (1.0 >= tOut1) {
                        if (tOutX < tOutY) {
                            out.add(xOut, y0 + tOutX * deltaY);
                        } else {
                            out.add(x0 + tOutY * deltaX, yOut);
                        }
                    } else {
                        out.add(x1, y1);
                    }
                }
                if (0.0 < tOut2 && tOut2 <= 1.0) {
                    out.add(xOut, yOut);
                }
            }
            ++i;
        }
        if (out.size() < 3) {
            return null;
        }
        if (out.getOrdinate(0, 0) != out.getOrdinate(out.size() - 1, 0) || out.getOrdinate(0, 1) != out.getOrdinate(out.size() - 1, 1)) {
            out.add(out.getOrdinate(0, 0), out.getOrdinate(0, 1));
        } else if (out.size() == 3) {
            return null;
        }
        return ring.getFactory().createLinearRing(out.toCoordinateSequence(ring.getFactory().getCoordinateSequenceFactory()));
    }

    LinearRing buildBoundsString(GeometryFactory gf, CoordinateSequenceFactory csf) {
        CoordinateSequence cs = csf.create(5, 2);
        cs.setOrdinate(0, 0, this.xmin);
        cs.setOrdinate(0, 1, this.ymin);
        cs.setOrdinate(1, 0, this.xmin);
        cs.setOrdinate(1, 1, this.ymax);
        cs.setOrdinate(2, 0, this.xmax);
        cs.setOrdinate(2, 1, this.ymax);
        cs.setOrdinate(3, 0, this.xmax);
        cs.setOrdinate(3, 1, this.ymin);
        cs.setOrdinate(4, 0, this.xmin);
        cs.setOrdinate(4, 1, this.ymin);
        return gf.createLinearRing(cs);
    }

    private Geometry clipCollection(GeometryCollection gc, boolean ensureValid) {
        if (gc.getNumGeometries() == 1) {
            return this.clip(gc.getGeometryN(0), ensureValid);
        }
        ArrayList<Geometry> result = new ArrayList<Geometry>(gc.getNumGeometries());
        int i = 0;
        while (i < gc.getNumGeometries()) {
            Geometry clipped = this.clip(gc.getGeometryN(i), ensureValid);
            if (clipped != null) {
                result.add(clipped);
            }
            ++i;
        }
        if (result.size() == 0) {
            return null;
        }
        if (result.size() == 1) {
            return (Geometry)result.get(0);
        }
        this.flattenCollection(result);
        if (gc instanceof MultiPoint) {
            return gc.getFactory().createMultiPoint(result.toArray(new Point[result.size()]));
        }
        if (gc instanceof MultiLineString) {
            return gc.getFactory().createMultiLineString(result.toArray(new LineString[result.size()]));
        }
        if (gc instanceof MultiPolygon) {
            return gc.getFactory().createMultiPolygon(result.toArray(new Polygon[result.size()]));
        }
        return gc.getFactory().createGeometryCollection(result.toArray(new Geometry[result.size()]));
    }

    private void flattenCollection(List<Geometry> result) {
        int i = 0;
        while (i < result.size()) {
            Geometry g = result.get(i);
            if (g instanceof GeometryCollection) {
                GeometryCollection gc = (GeometryCollection)g;
                int j = 0;
                while (j < gc.getNumGeometries()) {
                    result.add(gc.getGeometryN(j));
                    ++j;
                }
                result.remove(i);
                continue;
            }
            ++i;
        }
    }

    Geometry clipLineString(LineString line) {
        double y0;
        ArrayList<LineString> clipped = new ArrayList<LineString>();
        GeometryFactory gf = line.getFactory();
        CoordinateSequenceFactory csf = gf.getCoordinateSequenceFactory();
        CoordinateSequence coords = line.getCoordinateSequence();
        Ordinates ordinates = new Ordinates(coords.size());
        double x0 = coords.getX(0);
        boolean prevInside = this.contained(x0, y0 = coords.getY(0));
        if (prevInside) {
            ordinates.add(x0, y0);
        }
        double[] segment = new double[4];
        int size = coords.size();
        int i = 1;
        while (i < size) {
            double[] clippedSegment;
            double y1;
            double x1 = coords.getX(i);
            boolean inside = this.contained(x1, y1 = coords.getY(i));
            if (inside == prevInside) {
                if (inside) {
                    ordinates.add(x1, y1);
                } else if (!this.outside(x0, y0, x1, y1)) {
                    segment[0] = x0;
                    segment[1] = y0;
                    segment[2] = x1;
                    segment[3] = y1;
                    clippedSegment = this.clipSegment(segment);
                    if (clippedSegment != null) {
                        CoordinateSequence cs = csf.create(2, 2);
                        cs.setOrdinate(0, 0, clippedSegment[0]);
                        cs.setOrdinate(0, 1, clippedSegment[1]);
                        cs.setOrdinate(1, 0, clippedSegment[2]);
                        cs.setOrdinate(1, 1, clippedSegment[3]);
                        clipped.add(gf.createLineString(cs));
                    }
                }
            } else {
                segment[0] = x0;
                segment[1] = y0;
                segment[2] = x1;
                segment[3] = y1;
                clippedSegment = this.clipSegment(segment);
                if (clippedSegment != null) {
                    if (prevInside) {
                        ordinates.add(clippedSegment[2], clippedSegment[3]);
                    } else {
                        ordinates.add(clippedSegment[0], clippedSegment[1]);
                        ordinates.add(clippedSegment[2], clippedSegment[3]);
                    }
                    if (prevInside) {
                        clipped.add(gf.createLineString(ordinates.toCoordinateSequence(csf)));
                        ordinates.clear();
                    }
                } else {
                    prevInside = false;
                }
            }
            prevInside = inside;
            x0 = x1;
            y0 = y1;
            ++i;
        }
        if (ordinates.size() > 1) {
            clipped.add(gf.createLineString(ordinates.toCoordinateSequence(csf)));
        }
        if (line.isClosed() && clipped.size() > 1) {
            CoordinateSequence cs0 = ((LineString)clipped.get(0)).getCoordinateSequence();
            CoordinateSequence cs1 = ((LineString)clipped.get(clipped.size() - 1)).getCoordinateSequence();
            if (cs0.getOrdinate(0, 0) == cs1.getOrdinate(cs1.size() - 1, 0) && cs0.getOrdinate(0, 1) == cs1.getOrdinate(cs1.size() - 1, 1)) {
                CoordinateSequence cs = csf.create(cs0.size() + cs1.size() - 1, 2);
                int i2 = 0;
                while (i2 < cs1.size()) {
                    cs.setOrdinate(i2, 0, cs1.getOrdinate(i2, 0));
                    cs.setOrdinate(i2, 1, cs1.getOrdinate(i2, 1));
                    ++i2;
                }
                i2 = 1;
                while (i2 < cs0.size()) {
                    cs.setOrdinate(i2 + cs1.size() - 1, 0, cs0.getOrdinate(i2, 0));
                    cs.setOrdinate(i2 + cs1.size() - 1, 1, cs0.getOrdinate(i2, 1));
                    ++i2;
                }
                clipped.remove(0);
                clipped.remove(clipped.size() - 1);
                clipped.add(gf.createLineString(cs));
            }
        }
        if (clipped.size() > 1) {
            return gf.createMultiLineString(clipped.toArray(new LineString[clipped.size()]));
        }
        if (clipped.size() == 1) {
            return (Geometry)clipped.get(0);
        }
        return null;
    }
}

