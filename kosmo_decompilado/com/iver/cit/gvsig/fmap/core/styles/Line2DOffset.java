/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.cit.gvsig.fmap.core.FPolygon2D
 *  com.iver.cit.gvsig.fmap.core.GeneralPathX
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.LineSegment
 *  org.apache.log4j.Logger
 */
package com.iver.cit.gvsig.fmap.core.styles;

import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.styles.LineEquation;
import com.iver.cit.gvsig.fmap.core.styles.NotEnoughSegmentsToClosePathException;
import com.iver.cit.gvsig.fmap.core.styles.ParallelLinesCannotBeResolvedException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Hashtable;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;

public class Line2DOffset {
    public static SAIGGeneralPath offsetLine(Shape p, double offset) {
        if (Math.abs(offset) < 1.0) {
            return new SAIGGeneralPath(p);
        }
        PathIterator pi = p.getPathIterator(null);
        double[] dataCoords = new double[6];
        Coordinate from = null;
        Coordinate first = null;
        ArrayList<LineSegment> segments = new ArrayList<LineSegment>();
        SAIGGeneralPath offsetSegments = new SAIGGeneralPath();
        try {
            while (!pi.isDone()) {
                int type = pi.currentSegment(dataCoords);
                switch (type) {
                    case 0: {
                        if (from == null) {
                            first = from = new Coordinate(dataCoords[0], dataCoords[1]);
                            break;
                        }
                        try {
                            if (p instanceof FPolygon2D) {
                                offsetSegments.append((Shape)Line2DOffset.offsetAndConsumeClosedSegments(offset, segments), false);
                            } else {
                                offsetSegments.append((Shape)Line2DOffset.offsetAndConsumeSegments(offset, segments), false);
                            }
                        }
                        catch (NotEnoughSegmentsToClosePathException e) {
                            Logger.getLogger(Line2DOffset.class).error((Object)e.getMessage(), (Throwable)e);
                        }
                        segments.clear();
                        first = from = new Coordinate(dataCoords[0], dataCoords[1]);
                        break;
                    }
                    case 1: {
                        Coordinate to = new Coordinate(dataCoords[0], dataCoords[1]);
                        if (from.compareTo((Object)to) == 0) break;
                        LineSegment line = new LineSegment(from, to);
                        segments.add(line);
                        from = to;
                        break;
                    }
                    case 4: {
                        LineSegment line = new LineSegment(from, first);
                        segments.add(line);
                        try {
                            offsetSegments.append((Shape)Line2DOffset.offsetAndConsumeClosedSegments(offset, segments), false);
                        }
                        catch (NotEnoughSegmentsToClosePathException e) {
                            Logger.getLogger(Line2DOffset.class).error((Object)e.getMessage(), (Throwable)e);
                        }
                        segments.clear();
                        first = null;
                        from = null;
                    }
                }
                pi.next();
            }
            offsetSegments.append((Shape)Line2DOffset.offsetAndConsumeSegments(offset, segments), false);
            return offsetSegments;
        }
        catch (ParallelLinesCannotBeResolvedException e) {
            Logger.getLogger(Line2DOffset.class).error((Object)e.getMessage(), (Throwable)e);
            return new SAIGGeneralPath(p);
        }
    }

    private static GeneralPathX offsetAndConsumeSegments(double offset, ArrayList<LineSegment> segments) throws ParallelLinesCannotBeResolvedException {
        Hashtable<LineSegment, LineEquation> offsetLines = new Hashtable<LineSegment, LineEquation>();
        int segmentCount = segments.size();
        int i = 0;
        while (i < segmentCount) {
            LineSegment segment = segments.get(i);
            double theta = segment.angle();
            double xOffset = offset * Math.sin(theta);
            double yOffset = offset * Math.cos(theta);
            Coordinate p0 = segment.p0;
            double x0 = p0.x + xOffset;
            double y0 = p0.y - yOffset;
            Coordinate p1 = segment.p1;
            double x1 = p1.x + xOffset;
            double y1 = p1.y - yOffset;
            LineEquation offsetLine = new LineEquation(theta, x0, y0, x1, y1);
            offsetLines.put(segment, offsetLine);
            ++i;
        }
        Point2D.Double pIni = null;
        Point2D pEnd = null;
        GeneralPathX gpx = new GeneralPathX();
        int i2 = 0;
        while (i2 < segmentCount) {
            block6: {
                block5: {
                    LineSegment segment = segments.get(0);
                    LineEquation eq = (LineEquation)offsetLines.get(segment);
                    pIni = i2 == 0 ? new Point2D.Double(eq.x, eq.y) : pEnd;
                    if (i2 < segmentCount - 1) {
                        LineEquation eq1 = (LineEquation)offsetLines.get(segments.get(1));
                        try {
                            pEnd = eq.resolve(eq1);
                            break block5;
                        }
                        catch (ParallelLinesCannotBeResolvedException e) {
                            pEnd = new Point2D.Double(eq.xEnd, eq.yEnd);
                            gpx.append((Shape)new Line2D.Double(pIni, pEnd), true);
                            pIni = pEnd;
                            pEnd = new Point2D.Double(eq1.x, eq1.y);
                            segments.remove(0);
                            break block6;
                        }
                    }
                    pEnd = new Point2D.Double(eq.xEnd, eq.yEnd);
                }
                gpx.append((Shape)new Line2D.Double(pIni, pEnd), true);
                segments.remove(0);
            }
            ++i2;
        }
        return gpx;
    }

    private static GeneralPathX offsetAndConsumeClosedSegments(double offset, ArrayList<LineSegment> segments) throws ParallelLinesCannotBeResolvedException, NotEnoughSegmentsToClosePathException {
        int segmentCount = segments.size();
        if (segmentCount > 1) {
            Hashtable<LineSegment, LineEquation> offsetLines = new Hashtable<LineSegment, LineEquation>();
            int i = 0;
            while (i < segmentCount) {
                LineSegment segment = segments.get(i);
                double theta = segment.angle();
                double xOffset = offset * Math.sin(theta);
                double yOffset = offset * Math.cos(theta);
                Coordinate p0 = segment.p0;
                double x0 = p0.x + xOffset;
                double y0 = p0.y - yOffset;
                Coordinate p1 = segment.p1;
                double x1 = p1.x + xOffset;
                double y1 = p1.y - yOffset;
                LineEquation offsetLine = new LineEquation(theta, x0, y0, x1, y1);
                offsetLines.put(segment, offsetLine);
                ++i;
            }
            Point2D pIni = null;
            Point2D pEnd = null;
            Point2D firstP = null;
            GeneralPathX gpx = new GeneralPathX();
            int i2 = 0;
            while (i2 < segmentCount) {
                block11: {
                    block10: {
                        LineSegment segment = segments.get(0);
                        LineEquation eq = (LineEquation)offsetLines.get(segment);
                        if (i2 == 0) {
                            LineEquation eq0 = (LineEquation)offsetLines.get(segments.get(segmentCount - 1));
                            try {
                                pIni = eq0.resolve(eq);
                            }
                            catch (ParallelLinesCannotBeResolvedException e) {
                                pIni = new Point2D.Double(eq.x, eq.y);
                            }
                            firstP = pIni;
                        } else {
                            pIni = pEnd;
                        }
                        if (i2 < segmentCount - 1) {
                            LineEquation eq1 = (LineEquation)offsetLines.get(segments.get(1));
                            try {
                                pEnd = eq.resolve(eq1);
                                break block10;
                            }
                            catch (ParallelLinesCannotBeResolvedException e) {
                                pEnd = new Point2D.Double(eq.xEnd, eq.yEnd);
                                gpx.append((Shape)new Line2D.Double(pIni, pEnd), true);
                                pIni = pEnd;
                                pEnd = new Point2D.Double(eq1.x, eq1.y);
                                segments.remove(0);
                                break block11;
                            }
                        }
                        pEnd = new Point2D.Double(firstP.getX(), firstP.getY());
                    }
                    gpx.append((Shape)new Line2D.Double(pIni, pEnd), true);
                    segments.remove(0);
                }
                ++i2;
            }
            return gpx;
        }
        throw new NotEnoughSegmentsToClosePathException(segments);
    }
}

