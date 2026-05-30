/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.renderer;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;

public class ShapeClipper {
    private Rectangle2D clip;
    private AffineTransform transform;

    public ShapeClipper(Rectangle2D clip, AffineTransform transform) {
        this.clip = clip;
        this.transform = transform;
    }

    protected Point2D[][] getPoints(Shape s, boolean fill) {
        List unclippedpaths = this.getPointsUnclipped(s, fill);
        List paths = this.clipPaths(unclippedpaths, fill, 0);
        paths = this.clipPaths(paths, fill, 1);
        paths = this.clipPaths(paths, fill, 2);
        paths = this.clipPaths(paths, fill, 3);
        int i = 0;
        Point2D[][] pointsArray = new Point2D[paths.size()][];
        for (ArrayList path : paths) {
            pointsArray[i] = new Point2D[path.size()];
            pointsArray[i] = path.toArray(pointsArray[i]);
            ++i;
        }
        return pointsArray;
    }

    protected List clipPaths(List unclippedpaths, boolean fill, int side) {
        ArrayList paths = new ArrayList();
        ArrayList<Point2D> currentPath = null;
        Point2D lastPoint = null;
        Point2D firstPoint = null;
        Iterator pathIterator = unclippedpaths.iterator();
        boolean firstPointIn = false;
        while (pathIterator.hasNext()) {
            List path = (List)pathIterator.next();
            currentPath = new ArrayList<Point2D>();
            Point2D firstIPoint = null;
            Iterator it = path.iterator();
            if (it.hasNext()) {
                lastPoint = firstPoint = (Point2D)it.next();
                if (this.isPointInsideClip(firstPoint, side)) {
                    currentPath.add(firstPoint);
                    firstPointIn = true;
                } else {
                    firstPointIn = false;
                }
            }
            while (it.hasNext()) {
                Point2D newip;
                Point2D p = (Point2D)it.next();
                if (this.isPointInsideClip(lastPoint, side) && this.isPointInsideClip(p, side)) {
                    currentPath.add(p);
                } else if (this.isPointInsideClip(lastPoint, side) && !this.isPointInsideClip(p, side)) {
                    newip = this.clip(lastPoint, p, false, side);
                    currentPath.add(newip);
                } else if (!this.isPointInsideClip(lastPoint, side) && this.isPointInsideClip(p, side)) {
                    newip = this.clip(lastPoint, p, true, side);
                    if (!fill) {
                        if (currentPath.size() > 0) {
                            paths.add(currentPath);
                        }
                        currentPath = new ArrayList();
                        currentPath.add(newip);
                        currentPath.add(p);
                    } else {
                        currentPath.add(newip);
                        currentPath.add(p);
                        if (!firstPointIn && firstIPoint == null) {
                            firstIPoint = newip;
                        }
                    }
                }
                lastPoint = p;
            }
            if (!firstPointIn && fill && firstIPoint != null) {
                currentPath.add(firstIPoint);
            }
            if (currentPath.size() <= 0) continue;
            paths.add(currentPath);
        }
        return paths;
    }

    protected boolean isPointInsideClip(Point2D p, int side) {
        if (side == 0) {
            return p.getY() >= this.clip.getMinY();
        }
        if (side == 1) {
            return p.getX() <= this.clip.getMaxX();
        }
        if (side == 2) {
            return p.getY() <= this.clip.getMaxY();
        }
        if (side == 3) {
            return p.getX() >= this.clip.getMinX();
        }
        return false;
    }

    protected Point2D clip(Point2D p1, Point2D p2, boolean in, int side) {
        Point2D.Double vector = new Point2D.Double(p2.getX() - p1.getX(), p2.getY() - p1.getY());
        double delta = 0.0;
        switch (side) {
            case 3: {
                delta = this.calculateDelta(this.clip.getMinX(), p1.getX(), ((Point2D)vector).getX());
                break;
            }
            case 1: {
                delta = this.calculateDelta(this.clip.getMaxX(), p1.getX(), ((Point2D)vector).getX());
                break;
            }
            case 2: {
                delta = this.calculateDelta(this.clip.getMaxY(), p1.getY(), ((Point2D)vector).getY());
                break;
            }
            case 0: {
                delta = this.calculateDelta(this.clip.getMinY(), p1.getY(), ((Point2D)vector).getY());
            }
        }
        Point2D p = null;
        if (delta >= 0.0 && delta <= 1.0) {
            p = new Point2D.Double(p1.getX() + delta * ((Point2D)vector).getX(), p1.getY() + delta * ((Point2D)vector).getY());
        }
        return new Point2D.Double(p.getX(), p.getY());
    }

    protected double calculateDelta(double x, double p1, double v) {
        return (x - p1) / v;
    }

    protected List getPointsUnclipped(Shape s, boolean fill) {
        ArrayList paths = new ArrayList();
        ArrayList<Point2D.Double> currentPath = null;
        Point2D.Double startp = new Point2D.Double();
        boolean firstPointReached = false;
        PathIterator it = s.getPathIterator(this.transform);
        double[] points = new double[6];
        Point2D.Double p = null;
        block5: while (!it.isDone()) {
            int type = it.currentSegment(points);
            it.next();
            switch (type) {
                case 1: 
                case 2: 
                case 3: {
                    p = new Point2D.Double(points[0], points[1]);
                    currentPath.add(p);
                    if (firstPointReached) break;
                    startp = new Point2D.Double(((Point2D)p).getX(), ((Point2D)p).getY());
                    firstPointReached = true;
                    break;
                }
                case 0: {
                    currentPath = new ArrayList<Point2D.Double>();
                    p = new Point2D.Double(points[0], points[1]);
                    currentPath.add(p);
                    paths.add(currentPath);
                    if (firstPointReached) break;
                    startp = new Point2D.Double(((Point2D)p).getX(), ((Point2D)p).getY());
                    firstPointReached = true;
                    break;
                }
                case 4: {
                    if (!firstPointReached || startp.equals(p)) continue block5;
                    currentPath.add(startp);
                    break;
                }
            }
        }
        return paths;
    }

    public Shape getClippedShape(Shape shp, boolean fill) {
        Point2D[][] allPoints = this.getPoints(shp, fill);
        SAIGGeneralPath path = new SAIGGeneralPath();
        Point2D[][] point2DArray = allPoints;
        int n = allPoints.length;
        int n2 = 0;
        while (n2 < n) {
            Point2D[] points = point2DArray[n2];
            path.append(this.buildShapeFromPoints(points), false);
            ++n2;
        }
        return path;
    }

    private SAIGGeneralPath buildShapeFromPoints(Point2D[] points) {
        SAIGGeneralPath shp = new SAIGGeneralPath();
        Point2D p = points[0];
        shp.moveTo(p.getX(), p.getY());
        int i = 1;
        while (i < points.length) {
            shp.lineTo(points[i].getX(), points[i].getY());
            ++i;
        }
        return shp;
    }
}

