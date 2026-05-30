/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
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
 *  com.vividsolutions.jts.geom.PrecisionModel
 *  org.deegree.framework.log.ILogger
 *  org.deegree.framework.log.LoggerFactory
 *  org.deegree.model.spatialschema.Curve
 *  org.deegree.model.spatialschema.Geometry
 *  org.deegree.model.spatialschema.GeometryException
 *  org.deegree.model.spatialschema.GeometryFactory
 *  org.deegree.model.spatialschema.LineString
 *  org.deegree.model.spatialschema.MultiCurve
 *  org.deegree.model.spatialschema.MultiPoint
 *  org.deegree.model.spatialschema.MultiPointImpl
 *  org.deegree.model.spatialschema.MultiPrimitive
 *  org.deegree.model.spatialschema.MultiPrimitiveImpl
 *  org.deegree.model.spatialschema.MultiSurface
 *  org.deegree.model.spatialschema.MultiSurfaceImpl
 *  org.deegree.model.spatialschema.Point
 *  org.deegree.model.spatialschema.PointImpl
 *  org.deegree.model.spatialschema.PolygonImpl
 *  org.deegree.model.spatialschema.Position
 *  org.deegree.model.spatialschema.PositionImpl
 *  org.deegree.model.spatialschema.Primitive
 *  org.deegree.model.spatialschema.Surface
 *  org.deegree.model.spatialschema.SurfaceImpl
 *  org.deegree.model.spatialschema.SurfaceInterpolation
 *  org.deegree.model.spatialschema.SurfaceInterpolationImpl
 *  org.deegree.model.spatialschema.SurfacePatch
 */
package org.deegree.model.spatialschema;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.LineString;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiPointImpl;
import org.deegree.model.spatialschema.MultiPrimitive;
import org.deegree.model.spatialschema.MultiPrimitiveImpl;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.MultiSurfaceImpl;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.PointImpl;
import org.deegree.model.spatialschema.PolygonImpl;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.PositionImpl;
import org.deegree.model.spatialschema.Primitive;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfaceImpl;
import org.deegree.model.spatialschema.SurfaceInterpolation;
import org.deegree.model.spatialschema.SurfaceInterpolationImpl;
import org.deegree.model.spatialschema.SurfacePatch;

public class JTSAdapter {
    private static final ILogger LOG = LoggerFactory.getLogger(JTSAdapter.class);
    private static PrecisionModel pm = new PrecisionModel();
    private static com.vividsolutions.jts.geom.GeometryFactory jtsFactory = new com.vividsolutions.jts.geom.GeometryFactory(pm, 0);

    public static com.vividsolutions.jts.geom.Geometry export(Geometry gmObject) throws GeometryException {
        com.vividsolutions.jts.geom.Point geometry = null;
        if (gmObject instanceof Point) {
            geometry = JTSAdapter.export((Point)gmObject);
        } else if (gmObject instanceof MultiPoint) {
            geometry = JTSAdapter.export((MultiPoint)gmObject);
        } else if (gmObject instanceof Curve) {
            geometry = JTSAdapter.export((Curve)gmObject);
        } else if (gmObject instanceof MultiCurve) {
            geometry = JTSAdapter.export((MultiCurve)gmObject);
        } else if (gmObject instanceof Surface) {
            geometry = JTSAdapter.export((Surface)gmObject);
        } else if (gmObject instanceof MultiSurface) {
            geometry = JTSAdapter.export((MultiSurface)gmObject);
        } else if (gmObject instanceof MultiPrimitive) {
            geometry = JTSAdapter.export((MultiPrimitive)gmObject);
        } else {
            throw new GeometryException("JTSAdapter.export does not support type '" + gmObject.getClass().getName() + "'!");
        }
        return geometry;
    }

    public static Geometry wrap(com.vividsolutions.jts.geom.Geometry geometry, CoordinateSystem crs) throws GeometryException {
        Point gmObject = null;
        if (geometry instanceof com.vividsolutions.jts.geom.Point) {
            gmObject = JTSAdapter.wrap((com.vividsolutions.jts.geom.Point)geometry, crs);
        } else if (geometry instanceof com.vividsolutions.jts.geom.MultiPoint) {
            gmObject = JTSAdapter.wrap((com.vividsolutions.jts.geom.MultiPoint)geometry, crs);
        } else if (geometry instanceof com.vividsolutions.jts.geom.LineString) {
            gmObject = JTSAdapter.wrap((com.vividsolutions.jts.geom.LineString)geometry, crs);
        } else if (geometry instanceof MultiLineString) {
            gmObject = JTSAdapter.wrap((MultiLineString)geometry, crs);
        } else if (geometry instanceof Polygon) {
            gmObject = JTSAdapter.wrap((Polygon)geometry, crs);
        } else if (geometry instanceof MultiPolygon) {
            gmObject = JTSAdapter.wrap((MultiPolygon)geometry, crs);
        } else if (geometry instanceof GeometryCollection) {
            gmObject = JTSAdapter.wrap((GeometryCollection)geometry, crs);
        } else {
            throw new GeometryException("JTSAdapter.wrap does not support type '" + geometry.getClass().getName() + "'!");
        }
        return gmObject;
    }

    private static com.vividsolutions.jts.geom.Point export(Point gmPoint) {
        double z = gmPoint.getZ();
        Coordinate coord = Double.isNaN(z) ? new Coordinate(gmPoint.getX(), gmPoint.getY()) : new Coordinate(gmPoint.getX(), gmPoint.getY(), z);
        return jtsFactory.createPoint(coord);
    }

    private static com.vividsolutions.jts.geom.MultiPoint export(MultiPoint gmMultiPoint) {
        Point[] gmPoints = gmMultiPoint.getAllPoints();
        com.vividsolutions.jts.geom.Point[] points = new com.vividsolutions.jts.geom.Point[gmPoints.length];
        int i = 0;
        while (i < points.length) {
            points[i] = JTSAdapter.export(gmPoints[i]);
            ++i;
        }
        return jtsFactory.createMultiPoint(points);
    }

    private static com.vividsolutions.jts.geom.LineString export(Curve curve) throws GeometryException {
        LineString lineString = curve.getAsLineString();
        Coordinate[] coords = new Coordinate[lineString.getNumberOfPoints()];
        int i = 0;
        while (i < coords.length) {
            Position position = lineString.getPositionAt(i);
            coords[i] = new Coordinate(position.getX(), position.getY(), position.getZ());
            ++i;
        }
        return jtsFactory.createLineString(coords);
    }

    private static MultiLineString export(MultiCurve multi) throws GeometryException {
        Curve[] curves = multi.getAllCurves();
        com.vividsolutions.jts.geom.LineString[] lineStrings = new com.vividsolutions.jts.geom.LineString[curves.length];
        int i = 0;
        while (i < curves.length) {
            lineStrings[i] = JTSAdapter.export(curves[i]);
            ++i;
        }
        return jtsFactory.createMultiLineString(lineStrings);
    }

    public static LinearRing export(Position[] positions) {
        Coordinate[] coords = new Coordinate[Math.max(positions.length, 4)];
        int i = 0;
        while (i < positions.length) {
            coords[i] = new Coordinate(positions[i].getX(), positions[i].getY(), positions[i].getZ());
            ++i;
        }
        return jtsFactory.createLinearRing(coords);
    }

    private static Polygon export(Surface surface) {
        SurfacePatch patch = null;
        try {
            patch = surface.getSurfacePatchAt(0);
        }
        catch (GeometryException e) {
            LOG.logError("", (Throwable)e);
        }
        Position[] exteriorRing = patch.getExteriorRing();
        Position[][] interiorRings = patch.getInteriorRings();
        LinearRing shell = JTSAdapter.export(exteriorRing);
        LinearRing[] holes = new LinearRing[]{};
        if (interiorRings != null) {
            holes = new LinearRing[interiorRings.length];
        }
        int i = 0;
        while (i < holes.length) {
            holes[i] = JTSAdapter.export(interiorRings[i]);
            ++i;
        }
        return jtsFactory.createPolygon(shell, holes);
    }

    private static MultiPolygon export(MultiSurface msurface) {
        Surface[] surfaces = msurface.getAllSurfaces();
        Polygon[] polygons = new Polygon[surfaces.length];
        int i = 0;
        while (i < surfaces.length) {
            polygons[i] = JTSAdapter.export(surfaces[i]);
            ++i;
        }
        return jtsFactory.createMultiPolygon(polygons);
    }

    private static GeometryCollection export(MultiPrimitive multi) throws GeometryException {
        Primitive[] primitives = multi.getAllPrimitives();
        com.vividsolutions.jts.geom.Geometry[] geometries = new com.vividsolutions.jts.geom.Geometry[primitives.length];
        int i = 0;
        while (i < primitives.length) {
            geometries[i] = JTSAdapter.export((Geometry)primitives[i]);
            ++i;
        }
        return jtsFactory.createGeometryCollection(geometries);
    }

    private static Point wrap(com.vividsolutions.jts.geom.Point point, CoordinateSystem crs) {
        Coordinate coord = point.getCoordinate();
        return Double.isNaN(coord.z) ? new PointImpl(coord.x, coord.y, crs) : new PointImpl(coord.x, coord.y, coord.z, crs);
    }

    private static MultiPoint wrap(com.vividsolutions.jts.geom.MultiPoint multi, CoordinateSystem crs) {
        Point[] gmPoints = new Point[multi.getNumGeometries()];
        int i = 0;
        while (i < gmPoints.length) {
            gmPoints[i] = JTSAdapter.wrap((com.vividsolutions.jts.geom.Point)multi.getGeometryN(i), crs);
            ++i;
        }
        return new MultiPointImpl(gmPoints, null);
    }

    private static Curve wrap(com.vividsolutions.jts.geom.LineString line, CoordinateSystem crs) throws GeometryException {
        Coordinate[] coords = line.getCoordinates();
        Position[] positions = new Position[coords.length];
        int i = 0;
        while (i < coords.length) {
            positions[i] = new PositionImpl(coords[i].x, coords[i].y);
            ++i;
        }
        return GeometryFactory.createCurve((Position[])positions, (CoordinateSystem)crs);
    }

    private static MultiCurve wrap(MultiLineString multi, CoordinateSystem crs) throws GeometryException {
        Curve[] curves = new Curve[multi.getNumGeometries()];
        int i = 0;
        while (i < curves.length) {
            curves[i] = JTSAdapter.wrap((com.vividsolutions.jts.geom.LineString)multi.getGeometryN(i), crs);
            ++i;
        }
        return GeometryFactory.createMultiCurve((Curve[])curves);
    }

    private static Surface wrap(Polygon polygon, CoordinateSystem crs) throws GeometryException {
        Position[] exteriorRing = JTSAdapter.createGMPositions(polygon.getExteriorRing());
        Position[][] interiorRings = new Position[polygon.getNumInteriorRing()][];
        int i = 0;
        while (i < interiorRings.length) {
            interiorRings[i] = JTSAdapter.createGMPositions(polygon.getInteriorRingN(i));
            ++i;
        }
        PolygonImpl patch = new PolygonImpl((SurfaceInterpolation)new SurfaceInterpolationImpl(), exteriorRing, (Position[][])interiorRings, crs);
        return new SurfaceImpl((SurfacePatch)patch);
    }

    private static MultiSurface wrap(MultiPolygon multiPolygon, CoordinateSystem crs) throws GeometryException {
        Surface[] surfaces = new Surface[multiPolygon.getNumGeometries()];
        int i = 0;
        while (i < surfaces.length) {
            surfaces[i] = JTSAdapter.wrap((Polygon)multiPolygon.getGeometryN(i), crs);
            ++i;
        }
        return new MultiSurfaceImpl(surfaces);
    }

    private static MultiPrimitive wrap(GeometryCollection collection, CoordinateSystem crs) throws GeometryException {
        MultiPrimitiveImpl multi = new MultiPrimitiveImpl(null);
        int i = 0;
        while (i < collection.getNumGeometries()) {
            multi.add(JTSAdapter.wrap(collection.getGeometryN(i), crs));
            ++i;
        }
        return multi;
    }

    private static Position[] createGMPositions(com.vividsolutions.jts.geom.LineString line) {
        Coordinate[] coords = line.getCoordinates();
        Position[] positions = new Position[coords.length];
        int i = 0;
        while (i < coords.length) {
            positions[i] = new PositionImpl(coords[i].x, coords[i].y);
            ++i;
        }
        return positions;
    }
}

