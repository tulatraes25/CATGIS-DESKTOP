/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.CGAlgorithms
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateArrays
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
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPathIterator;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeArc2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeArc3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeCircle2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeCircle3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeEllipse2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeEllipse3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeFactory;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeMultiPoint2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeMultipoint3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolygon2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolygon3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolyline2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolyline3D;
import org.saig.core.geometry.Arc;
import org.saig.core.geometry.Circle;
import org.saig.core.geometry.Ellipse;
import org.saig.jump.lang.I18N;

public class ShapeGeometryConverter {
    private static final Logger LOGGER = Logger.getLogger(ShapeGeometryConverter.class);
    public static final GeometryFactory geomFact = new GeometryFactory();
    public static final double FLATNESS = 0.8;

    public static boolean pointInList(Coordinate testPoint, Coordinate[] pointList) {
        int numpoints = Array.getLength(pointList);
        int t = 0;
        while (t < numpoints) {
            Coordinate p = pointList[t];
            if (testPoint.x == p.x && testPoint.y == p.y && (testPoint.z == p.z || testPoint.z != testPoint.z)) {
                return true;
            }
            ++t;
        }
        return false;
    }

    public static IShapeGeometry jts_to_igeometry(Geometry jtsGeometry) {
        IShape shape = ShapeGeometryConverter.jts_to_java2d(jtsGeometry);
        return ShapeFactory.createGeometry(shape);
    }

    public static Geometry java2d_to_jts(IShape shp) {
        if (shp.isEmpty()) {
            return geomFact.buildGeometry(new ArrayList());
        }
        Point geoJTS = null;
        ArrayList<Coordinate> arrayCoords = null;
        Coordinate firstCoord = null;
        Coordinate lastCoord = null;
        int numParts = 0;
        double[] theData = new double[6];
        switch (shp.getShapeType()) {
            case 1: {
                ShapePoint2D p = (ShapePoint2D)shp;
                Coordinate coord = new Coordinate(p.getX(), p.getY());
                geoJTS = geomFact.createPoint(coord);
                break;
            }
            case 513: {
                ShapePoint3D p3D = (ShapePoint3D)shp;
                Coordinate coord = new Coordinate(p3D.getX(), p3D.getY(), p3D.getZs()[0]);
                geoJTS = geomFact.createPoint(coord);
                break;
            }
            case 32: {
                ShapeMultiPoint2D mp = (ShapeMultiPoint2D)shp;
                Coordinate[] puntos = new Coordinate[mp.getNumPoints()];
                int i = 0;
                while (i < mp.getNumPoints()) {
                    puntos[i] = new Coordinate(mp.getPoint(i).getX(), mp.getPoint(i).getY());
                    ++i;
                }
                geoJTS = geomFact.createMultiPoint(puntos);
                break;
            }
            case 544: {
                ShapeMultipoint3D mp3D = (ShapeMultipoint3D)shp;
                Coordinate[] puntos3D = new Coordinate[mp3D.getNumPoints()];
                int i = 0;
                while (i < mp3D.getNumPoints()) {
                    puntos3D[i] = new Coordinate(mp3D.getPoint(i).getX(), mp3D.getPoint(i).getY(), mp3D.getZs()[i]);
                    ++i;
                }
                geoJTS = geomFact.createMultiPoint(puntos3D);
                break;
            }
            case 2: 
            case 128: {
                LineString lin;
                ArrayList<LineString> arrayLines = new ArrayList<LineString>();
                PathIterator theIterator = shp.getPathIterator(null, 0.8);
                while (!theIterator.isDone()) {
                    int theType = theIterator.currentSegment(theData);
                    switch (theType) {
                        case 0: {
                            if (arrayCoords == null) {
                                arrayCoords = new ArrayList<Coordinate>();
                            } else {
                                lin = geomFact.createLineString(CoordinateArrays.toCoordinateArray((Collection)arrayCoords));
                                arrayLines.add(lin);
                                arrayCoords = new ArrayList();
                            }
                            ++numParts;
                            arrayCoords.add(new Coordinate(theData[0], theData[1]));
                            break;
                        }
                        case 1: {
                            arrayCoords.add(new Coordinate(theData[0], theData[1]));
                            break;
                        }
                        case 2: {
                            LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.not-supported-here"));
                            break;
                        }
                        case 3: {
                            LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.not-supported-here"));
                            break;
                        }
                        case 4: {
                            firstCoord = (Coordinate)arrayCoords.get(0);
                            arrayCoords.add(new Coordinate(firstCoord.x, firstCoord.y));
                        }
                    }
                    theIterator.next();
                }
                lin = geomFact.createLineString(CoordinateArrays.toCoordinateArray(arrayCoords));
                if (numParts > 1) {
                    arrayLines.add(lin);
                    geoJTS = geomFact.createMultiLineString(GeometryFactory.toLineStringArray(arrayLines));
                    break;
                }
                geoJTS = lin;
                break;
            }
            case 514: 
            case 640: {
                LineString lin;
                ArrayList<LineString> arrayLines = new ArrayList<LineString>();
                PathIterator theIterator = shp.getPathIterator(null, 0.8);
                double[] pz = ((ShapePolyline3D)shp).pZ;
                int k = 0;
                while (!theIterator.isDone()) {
                    int theType = theIterator.currentSegment(theData);
                    switch (theType) {
                        case 0: {
                            if (arrayCoords == null) {
                                arrayCoords = new ArrayList();
                            } else {
                                lin = geomFact.createLineString(CoordinateArrays.toCoordinateArray(arrayCoords));
                                arrayLines.add(lin);
                                arrayCoords = new ArrayList();
                            }
                            ++numParts;
                            arrayCoords.add(new Coordinate(theData[0], theData[1], pz[k]));
                            break;
                        }
                        case 1: {
                            arrayCoords.add(new Coordinate(theData[0], theData[1], pz[k]));
                            break;
                        }
                        case 2: {
                            LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.not-supported-here"));
                            break;
                        }
                        case 3: {
                            LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.not-supported-here"));
                            break;
                        }
                        case 4: {
                            firstCoord = (Coordinate)arrayCoords.get(0);
                            arrayCoords.add(new Coordinate(firstCoord.x, firstCoord.y, pz[k]));
                        }
                    }
                    ++k;
                    theIterator.next();
                }
                lin = geomFact.createLineString(CoordinateArrays.toCoordinateArray((Collection)arrayCoords));
                if (numParts > 1) {
                    arrayLines.add(lin);
                    geoJTS = geomFact.createMultiLineString(GeometryFactory.toLineStringArray(arrayLines));
                    break;
                }
                geoJTS = lin;
                break;
            }
            case 4: 
            case 64: 
            case 256: {
                LinearRing ring;
                ArrayList arrayLines = new ArrayList();
                ArrayList<LinearRing> shells = new ArrayList<LinearRing>();
                ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
                Coordinate[] points = null;
                PathIterator theIterator = shp.getPathIterator(null, 0.8);
                while (!theIterator.isDone()) {
                    int theType = theIterator.currentSegment(theData);
                    switch (theType) {
                        case 0: {
                            if (arrayCoords == null) {
                                arrayCoords = new ArrayList();
                            } else {
                                points = CoordinateArrays.toCoordinateArray(arrayCoords);
                                try {
                                    ring = geomFact.createLinearRing(points);
                                    if (CGAlgorithms.isCCW((Coordinate[])points)) {
                                        holes.add(ring);
                                    } else {
                                        shells.add(ring);
                                    }
                                }
                                catch (Exception e) {
                                    LOGGER.error((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.caught-topology-exception-in-GMLLinearRingHandler"));
                                    return null;
                                }
                                arrayCoords = new ArrayList();
                            }
                            ++numParts;
                            arrayCoords.add(new Coordinate(theData[0], theData[1]));
                            break;
                        }
                        case 1: {
                            arrayCoords.add(new Coordinate(theData[0], theData[1]));
                            break;
                        }
                        case 2: {
                            LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.seg-quadto-not-supported-here"));
                            break;
                        }
                        case 3: {
                            LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.seg-cubicto-not-supported-here"));
                            break;
                        }
                        case 4: {
                            firstCoord = (Coordinate)arrayCoords.get(0);
                            arrayCoords.add(new Coordinate(firstCoord.x, firstCoord.y));
                        }
                    }
                    theIterator.next();
                }
                firstCoord = (Coordinate)arrayCoords.get(0);
                if (!firstCoord.equals2D(lastCoord = (Coordinate)arrayCoords.get(arrayCoords.size() - 1))) {
                    arrayCoords.add((Coordinate)arrayCoords.get(0));
                }
                points = CoordinateArrays.toCoordinateArray(arrayCoords);
                try {
                    ring = geomFact.createLinearRing(points);
                    if (CGAlgorithms.isCCW((Coordinate[])points)) {
                        holes.add(ring);
                    } else {
                        shells.add(ring);
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)(String.valueOf(I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.caught-topology-exception-in-GMLLinearRingHandler")) + ": " + e.getMessage()));
                    return null;
                }
                ArrayList holesForShells = new ArrayList(shells.size());
                int i = 0;
                while (i < shells.size()) {
                    holesForShells.add(new ArrayList());
                    ++i;
                }
                i = 0;
                while (i < holes.size()) {
                    LinearRing testRing = (LinearRing)holes.get(i);
                    LinearRing minShell = null;
                    Envelope minEnv = null;
                    Envelope testEnv = testRing.getEnvelopeInternal();
                    Coordinate testPt = testRing.getCoordinateN(0);
                    LinearRing tryRing = null;
                    int j = 0;
                    while (j < shells.size()) {
                        tryRing = (LinearRing)shells.get(j);
                        Envelope tryEnv = tryRing.getEnvelopeInternal();
                        if (minShell != null) {
                            minEnv = minShell.getEnvelopeInternal();
                        }
                        boolean isContained = false;
                        Coordinate[] coordList = tryRing.getCoordinates();
                        if (tryEnv.contains(testEnv) && (CGAlgorithms.isPointInRing((Coordinate)testPt, (Coordinate[])coordList) || ShapeGeometryConverter.pointInList(testPt, coordList))) {
                            isContained = true;
                        }
                        if (isContained && (minShell == null || minEnv.contains(tryEnv))) {
                            minShell = tryRing;
                        }
                        ++j;
                    }
                    if (minShell == null) {
                        LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.polygon-found-with-a-hole-thats-not-inside-a-shell"));
                        Coordinate[] cs = testRing.getCoordinates();
                        Coordinate[] reversed = new Coordinate[cs.length];
                        int pointIndex = 0;
                        int z = cs.length - 1;
                        while (z >= 0) {
                            reversed[pointIndex] = cs[z];
                            ++pointIndex;
                            --z;
                        }
                        LinearRing newRing = geomFact.createLinearRing(reversed);
                        shells.add(newRing);
                        holesForShells.add(new ArrayList());
                    } else {
                        ((List)holesForShells.get(shells.indexOf(minShell))).add(testRing);
                    }
                    ++i;
                }
                Polygon[] polygons = new Polygon[shells.size()];
                int i2 = 0;
                while (i2 < shells.size()) {
                    polygons[i2] = geomFact.createPolygon((LinearRing)shells.get(i2), ((List)holesForShells.get(i2)).toArray(new LinearRing[0]));
                    ++i2;
                }
                if (polygons.length == 1) {
                    return polygons[0];
                }
                holesForShells = null;
                shells = null;
                holes = null;
                geoJTS = geomFact.createMultiPolygon(polygons);
                break;
            }
            case 516: 
            case 576: 
            case 768: {
                LinearRing ring;
                ArrayList arrayLines = new ArrayList();
                ArrayList<LinearRing> shells = new ArrayList<LinearRing>();
                ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
                Coordinate[] points = null;
                double[] pz = ((ShapePolygon3D)shp).pZ;
                PathIterator theIterator = shp.getPathIterator(null, 0.8);
                int k = 0;
                while (!theIterator.isDone()) {
                    int theType = theIterator.currentSegment(theData);
                    switch (theType) {
                        case 0: {
                            if (arrayCoords == null) {
                                arrayCoords = new ArrayList();
                            } else {
                                points = CoordinateArrays.toCoordinateArray(arrayCoords);
                                try {
                                    ring = geomFact.createLinearRing(points);
                                    if (CGAlgorithms.isCCW((Coordinate[])points)) {
                                        holes.add(ring);
                                    } else {
                                        shells.add(ring);
                                    }
                                }
                                catch (Exception e) {
                                    LOGGER.error((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.caught-topology-exception-in-GMLLinearRingHandler"));
                                    return null;
                                }
                                arrayCoords = new ArrayList();
                            }
                            ++numParts;
                            arrayCoords.add(new Coordinate(theData[0], theData[1], pz[k]));
                            break;
                        }
                        case 1: {
                            arrayCoords.add(new Coordinate(theData[0], theData[1], pz[k]));
                            break;
                        }
                        case 2: {
                            LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.seg-quadto-not-supported-here"));
                            break;
                        }
                        case 3: {
                            LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.seg-cubicto-not-supported-here"));
                            break;
                        }
                        case 4: {
                            Coordinate firs = (Coordinate)arrayCoords.get(0);
                            arrayCoords.add(new Coordinate(firs.x, firs.y, pz[k]));
                        }
                    }
                    theIterator.next();
                    ++k;
                }
                firstCoord = (Coordinate)arrayCoords.get(0);
                if (!firstCoord.equals3D(lastCoord = (Coordinate)arrayCoords.get(arrayCoords.size() - 1))) {
                    arrayCoords.add((Coordinate)arrayCoords.get(0));
                }
                while (arrayCoords.size() <= 3) {
                    arrayCoords.add((Coordinate)arrayCoords.get(0));
                }
                points = CoordinateArrays.toCoordinateArray(arrayCoords);
                try {
                    ring = geomFact.createLinearRing(points);
                    if (CGAlgorithms.isCCW((Coordinate[])points)) {
                        holes.add(ring);
                    } else {
                        shells.add(ring);
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.caught-topology-exception-in-GMLLinearRingHandler"));
                    return null;
                }
                ArrayList holesForShells = new ArrayList(shells.size());
                int i = 0;
                while (i < shells.size()) {
                    holesForShells.add(new ArrayList());
                    ++i;
                }
                i = 0;
                while (i < holes.size()) {
                    LinearRing testRing = (LinearRing)holes.get(i);
                    LinearRing minShell = null;
                    Envelope minEnv = null;
                    Envelope testEnv = testRing.getEnvelopeInternal();
                    Coordinate testPt = testRing.getCoordinateN(0);
                    LinearRing tryRing = null;
                    int j = 0;
                    while (j < shells.size()) {
                        tryRing = (LinearRing)shells.get(j);
                        Envelope tryEnv = tryRing.getEnvelopeInternal();
                        if (minShell != null) {
                            minEnv = minShell.getEnvelopeInternal();
                        }
                        boolean isContained = false;
                        Coordinate[] coordList = tryRing.getCoordinates();
                        if (tryEnv.contains(testEnv) && (CGAlgorithms.isPointInRing((Coordinate)testPt, (Coordinate[])coordList) || ShapeGeometryConverter.pointInList(testPt, coordList))) {
                            isContained = true;
                        }
                        if (isContained && (minShell == null || minEnv.contains(tryEnv))) {
                            minShell = tryRing;
                        }
                        ++j;
                    }
                    if (minShell == null) {
                        LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.polygon-found-with-a-hole-thats-not-inside-a-shell"));
                        Coordinate[] cs = testRing.getCoordinates();
                        Coordinate[] reversed = new Coordinate[cs.length];
                        int pointIndex = 0;
                        int z = cs.length - 1;
                        while (z >= 0) {
                            reversed[pointIndex] = cs[z];
                            ++pointIndex;
                            --z;
                        }
                        LinearRing newRing = geomFact.createLinearRing(reversed);
                        shells.add(newRing);
                        holesForShells.add(new ArrayList());
                    } else {
                        ((List)holesForShells.get(shells.indexOf(minShell))).add(testRing);
                    }
                    ++i;
                }
                Polygon[] polygons = new Polygon[shells.size()];
                i = 0;
                while (i < shells.size()) {
                    polygons[i] = geomFact.createPolygon((LinearRing)shells.get(i), ((List)holesForShells.get(i)).toArray(new LinearRing[0]));
                    ++i;
                }
                if (polygons.length == 1) {
                    return polygons[0];
                }
                holesForShells = null;
                shells = null;
                holes = null;
                geoJTS = geomFact.createMultiPolygon(polygons);
            }
        }
        return geoJTS;
    }

    public static IShape jts_to_java2d(Geometry geo) {
        IShape shpNew = null;
        try {
            if (geo.isEmpty()) {
                shpNew = null;
            }
            if (geo instanceof Point) {
                Point p = (Point)geo;
                shpNew = Double.isNaN(p.getCoordinate().z) ? new ShapePoint2D(p.getX(), p.getY()) : new ShapePoint3D(p.getX(), p.getY(), p.getCoordinate().z);
            } else if (geo instanceof MultiPoint) {
                shpNew = new ShapeMultiPoint2D(ShapeGeometryConverter.toShape((MultiPoint)geo));
            } else if (geo instanceof Circle) {
                Circle circle = (Circle)geo;
                shpNew = !circle.is3D() ? new ShapeCircle2D(ShapeGeometryConverter.toShape(circle), circle.getCentro(), circle.getRadio()) : new ShapeCircle3D(ShapeGeometryConverter.toShape(circle), circle.getCentro(), circle.getRadio(), circle.getZ());
            } else if (geo instanceof Ellipse) {
                Ellipse ellipse = (Ellipse)geo;
                shpNew = !ellipse.is3D() ? new ShapeEllipse2D(ShapeGeometryConverter.toShape(ellipse), ellipse.getStartPoint(), ellipse.getEndPoint(), ellipse.getDistancia()) : new ShapeEllipse3D(ShapeGeometryConverter.toShape(ellipse), ellipse.getStartPoint(), ellipse.getEndPoint(), ellipse.getDistancia(), ellipse.getZ());
            } else if (geo instanceof Polygon) {
                Polygon pol = (Polygon)geo;
                Coordinate c = pol.getCoordinates()[0];
                if (Double.isNaN(c.z)) {
                    shpNew = new ShapePolygon2D(ShapeGeometryConverter.toShape((Polygon)geo));
                } else {
                    double[] zs = new double[pol.getNumPoints()];
                    Coordinate[] coords = pol.getCoordinates();
                    int i = 0;
                    while (i < coords.length) {
                        zs[i] = coords[i].z;
                        ++i;
                    }
                    shpNew = new ShapePolygon3D(ShapeGeometryConverter.toShape((Polygon)geo), zs);
                }
            } else if (geo instanceof MultiPolygon) {
                MultiPolygon multiPol = (MultiPolygon)geo;
                Coordinate c = multiPol.getCoordinates()[0];
                if (Double.isNaN(c.z)) {
                    shpNew = new ShapePolygon2D(ShapeGeometryConverter.toShape((MultiPolygon)geo));
                } else {
                    double[] zs = new double[multiPol.getNumPoints()];
                    Coordinate[] coords = multiPol.getCoordinates();
                    int i = 0;
                    while (i < coords.length) {
                        zs[i] = coords[i].z;
                        ++i;
                    }
                    shpNew = new ShapePolygon3D(ShapeGeometryConverter.toShape((MultiPolygon)geo), zs);
                }
            } else if (geo instanceof Arc) {
                Arc arco = (Arc)geo;
                shpNew = !arco.is3D() ? new ShapeArc2D(ShapeGeometryConverter.toShape((LineString)geo), arco.getArcStartPoint(), arco.getIntermediatePoint(), arco.geArcEndPoint()) : new ShapeArc3D(ShapeGeometryConverter.toShape((LineString)geo), arco.getArcStartPoint(), arco.getIntermediatePoint(), arco.geArcEndPoint(), arco.getZs());
            } else if (geo instanceof LineString) {
                LineString line = (LineString)geo;
                Coordinate c = line.getCoordinateN(0);
                if (Double.isNaN(c.z)) {
                    shpNew = new ShapePolyline2D(ShapeGeometryConverter.toShape((LineString)geo));
                } else {
                    double[] zs = new double[line.getNumPoints()];
                    Coordinate[] coords = line.getCoordinates();
                    int i = 0;
                    while (i < coords.length) {
                        zs[i] = coords[i].z;
                        ++i;
                    }
                    shpNew = new ShapePolyline3D(ShapeGeometryConverter.toShape((LineString)geo), zs);
                }
            } else if (geo instanceof MultiLineString) {
                MultiLineString line = (MultiLineString)geo;
                Coordinate c = line.getCoordinates()[0];
                if (Double.isNaN(c.z)) {
                    shpNew = new ShapePolyline2D(ShapeGeometryConverter.toShape((MultiLineString)geo));
                } else {
                    double[] zs = new double[line.getNumPoints()];
                    Coordinate[] coords = line.getCoordinates();
                    int i = 0;
                    while (i < coords.length) {
                        zs[i] = coords[i].z;
                        ++i;
                    }
                    shpNew = new ShapePolyline3D(ShapeGeometryConverter.toShape((MultiLineString)geo), zs);
                }
            }
            return shpNew;
        }
        catch (NoninvertibleTransformException e) {
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
    }

    private static SAIGGeneralPath toShape(Polygon p) {
        Coordinate coord;
        SAIGGeneralPath resul = new SAIGGeneralPath();
        int i = 0;
        while (i < p.getExteriorRing().getNumPoints()) {
            coord = p.getExteriorRing().getCoordinateN(i);
            if (i == 0) {
                resul.moveTo(coord.x, coord.y);
            } else {
                resul.lineTo(coord.x, coord.y);
            }
            ++i;
        }
        int j = 0;
        while (j < p.getNumInteriorRing()) {
            LineString hole = p.getInteriorRingN(j);
            int k = 0;
            while (k < hole.getNumPoints()) {
                coord = hole.getCoordinateN(k);
                if (k == 0) {
                    resul.moveTo(coord.x, coord.y);
                } else {
                    resul.lineTo(coord.x, coord.y);
                }
                ++k;
            }
            ++j;
        }
        return resul;
    }

    private static SAIGGeneralPath toShape(MultiLineString mls) throws NoninvertibleTransformException {
        SAIGGeneralPath path = new SAIGGeneralPath();
        int i = 0;
        while (i < mls.getNumGeometries()) {
            LineString lineString = (LineString)mls.getGeometryN(i);
            path.append(ShapeGeometryConverter.toShape(lineString), false);
            ++i;
        }
        return path;
    }

    private static ShapePoint2D[] toShape(MultiPoint mp) throws NoninvertibleTransformException {
        ShapePoint2D[] points = new ShapePoint2D[mp.getNumGeometries()];
        int i = 0;
        while (i < mp.getNumGeometries()) {
            Point currentPoint = (Point)mp.getGeometryN(i);
            points[i] = ShapeGeometryConverter.toShape(currentPoint);
            ++i;
        }
        return points;
    }

    private static SAIGGeneralPath toShape(LineString lineString) throws NoninvertibleTransformException {
        SAIGGeneralPath shape = new SAIGGeneralPath();
        ShapePoint2D viewPoint = ShapeGeometryConverter.coordinate2ShapePoint2D(lineString.getCoordinateN(0));
        shape.moveTo(viewPoint.getX(), viewPoint.getY());
        int i = 1;
        while (i < lineString.getNumPoints()) {
            viewPoint = ShapeGeometryConverter.coordinate2ShapePoint2D(lineString.getCoordinateN(i));
            shape.lineTo(viewPoint.getX(), viewPoint.getY());
            ++i;
        }
        return shape;
    }

    private static ShapePoint2D toShape(Point point) throws NoninvertibleTransformException {
        ShapePoint2D viewPoint = ShapeGeometryConverter.coordinate2ShapePoint2D(point.getCoordinate());
        return viewPoint;
    }

    private static SAIGGeneralPath toShape(MultiPolygon mp) throws NoninvertibleTransformException {
        SAIGGeneralPath path = new SAIGGeneralPath();
        int i = 0;
        while (i < mp.getNumGeometries()) {
            Polygon polygon = (Polygon)mp.getGeometryN(i);
            path.append(ShapeGeometryConverter.toShape(polygon), false);
            ++i;
        }
        return path;
    }

    public static ShapePoint2D coordinate2ShapePoint2D(Coordinate coord) {
        return new ShapePoint2D(coord.x, coord.y);
    }

    public static SAIGGeneralPath toShape(Geometry geometry) throws NoninvertibleTransformException, IllegalArgumentException {
        if (geometry.isEmpty()) {
            return new SAIGGeneralPath();
        }
        if (geometry instanceof Polygon) {
            return ShapeGeometryConverter.toShape((Polygon)geometry);
        }
        if (geometry instanceof MultiPolygon) {
            return ShapeGeometryConverter.toShape((MultiPolygon)geometry);
        }
        if (geometry instanceof LineString) {
            return ShapeGeometryConverter.toShape((LineString)geometry);
        }
        if (geometry instanceof MultiLineString) {
            return ShapeGeometryConverter.toShape((MultiLineString)geometry);
        }
        if (geometry instanceof GeometryCollection) {
            return ShapeGeometryConverter.toShape((Geometry)((GeometryCollection)geometry));
        }
        throw new IllegalArgumentException(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.unrecognized-geometry-class-{0}", new Object[]{geometry.getClass()}));
    }

    public static SAIGGeneralPath transformToInts(SAIGGeneralPath gp, AffineTransform at) {
        SAIGGeneralPath newGp = new SAIGGeneralPath();
        int numParts = 0;
        double[] theData = new double[6];
        Point2D.Double ptDst = new Point2D.Double();
        Point2D.Double ptSrc = new Point2D.Double();
        boolean bFirst = true;
        int antX = -1;
        int antY = -1;
        PathIterator theIterator = gp.getPathIterator(null);
        while (!theIterator.isDone()) {
            int theType = theIterator.currentSegment(theData);
            switch (theType) {
                case 0: {
                    ++numParts;
                    ((Point2D)ptSrc).setLocation(theData[0], theData[1]);
                    at.transform(ptSrc, ptDst);
                    antX = (int)((Point2D)ptDst).getX();
                    antY = (int)((Point2D)ptDst).getY();
                    newGp.moveTo(antX, antY);
                    bFirst = true;
                    break;
                }
                case 1: {
                    ((Point2D)ptSrc).setLocation(theData[0], theData[1]);
                    at.transform(ptSrc, ptDst);
                    int xInt = (int)((Point2D)ptDst).getX();
                    int yInt = (int)((Point2D)ptDst).getY();
                    if (!bFirst && xInt == antX && yInt == antY) break;
                    newGp.lineTo(xInt, yInt);
                    antX = xInt;
                    antY = yInt;
                    bFirst = false;
                    break;
                }
                case 2: {
                    LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.not-supported-here"));
                    break;
                }
                case 3: {
                    LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.not-supported-here"));
                    break;
                }
                case 4: {
                    newGp.closePath();
                }
            }
            theIterator.next();
        }
        return newGp;
    }

    public static IShape transformToInts(IShapeGeometry gp, AffineTransform at) {
        SAIGGeneralPath newGp = new SAIGGeneralPath();
        double[] theData = new double[6];
        double[] aux = new double[6];
        int numParts = 0;
        Point2D.Double ptDst = new Point2D.Double();
        Point2D.Double ptSrc = new Point2D.Double();
        boolean bFirst = true;
        int antX = -1;
        int antY = -1;
        SAIGGeneralPathIterator theIterator = gp.getGeneralPathXIterator();
        int numSegmentsAdded = 0;
        while (!theIterator.isDone()) {
            int theType = theIterator.currentSegment(theData);
            switch (theType) {
                case 0: {
                    ++numParts;
                    ((Point2D)ptSrc).setLocation(theData[0], theData[1]);
                    at.transform(ptSrc, ptDst);
                    antX = (int)((Point2D)ptDst).getX();
                    antY = (int)((Point2D)ptDst).getY();
                    newGp.moveTo(antX, antY);
                    ++numSegmentsAdded;
                    bFirst = true;
                    break;
                }
                case 1: {
                    ((Point2D)ptSrc).setLocation(theData[0], theData[1]);
                    at.transform(ptSrc, ptDst);
                    int xInt = (int)((Point2D)ptDst).getX();
                    int yInt = (int)((Point2D)ptDst).getY();
                    if (!bFirst && xInt == antX && yInt == antY) break;
                    newGp.lineTo(xInt, yInt);
                    antX = xInt;
                    antY = yInt;
                    bFirst = false;
                    ++numSegmentsAdded;
                    break;
                }
                case 2: {
                    at.transform(theData, 0, aux, 0, 2);
                    newGp.quadTo(aux[0], aux[1], aux[2], aux[3]);
                    ++numSegmentsAdded;
                    break;
                }
                case 3: {
                    at.transform(theData, 0, aux, 0, 3);
                    newGp.curveTo(aux[0], aux[1], aux[2], aux[3], aux[4], aux[5]);
                    ++numSegmentsAdded;
                    break;
                }
                case 4: {
                    if (numSegmentsAdded < 3) {
                        newGp.lineTo(antX, antY);
                    }
                    newGp.closePath();
                }
            }
            theIterator.next();
        }
        IShape shp = null;
        switch (gp.getGeometryType()) {
            case 1: 
            case 513: {
                shp = new ShapePoint2D(((Point2D)ptDst).getX(), ((Point2D)ptDst).getY());
                break;
            }
            case 2: 
            case 128: 
            case 514: 
            case 640: {
                shp = new ShapePolyline2D(newGp);
                break;
            }
            case 4: 
            case 64: 
            case 256: 
            case 516: 
            case 576: 
            case 768: {
                shp = new ShapePolygon2D(newGp);
                break;
            }
            case 32: {
                PathIterator itPath = newGp.getPathIterator(null);
                int numCoords = newGp.numCoords / 2;
                double[] x = new double[numCoords];
                double[] y = new double[numCoords];
                int cont = 0;
                while (!itPath.isDone()) {
                    itPath.currentSegment(theData);
                    x[cont] = theData[0];
                    y[cont] = theData[1];
                    ++cont;
                    itPath.next();
                }
                shp = new ShapeMultiPoint2D(x, y);
            }
        }
        return shp;
    }

    public static Rectangle2D convertEnvelopeToRectangle2D(Envelope jtsR) {
        Rectangle2D.Double r = new Rectangle2D.Double(jtsR.getMinX(), jtsR.getMinY(), jtsR.getWidth(), jtsR.getHeight());
        return r;
    }

    public static Envelope convertRectangle2DtoEnvelope(Rectangle2D r) {
        Envelope e = new Envelope(r.getX(), r.getX() + r.getWidth(), r.getY(), r.getY() + r.getHeight());
        return e;
    }

    public static IShapeGeometry getExteriorPolygon(Coordinate[] coordinates) {
        Coordinate[] vs = new Coordinate[coordinates.length];
        if (CGAlgorithms.isCCW((Coordinate[])coordinates)) {
            int i = vs.length - 1;
            while (i >= 0) {
                vs[i] = coordinates[i];
                --i;
            }
        } else {
            vs = coordinates;
        }
        LinearRing ring = geomFact.createLinearRing(vs);
        try {
            return ShapeFactory.createPolygon2D(ShapeGeometryConverter.toShape((LineString)ring));
        }
        catch (NoninvertibleTransformException e) {
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
    }

    public static boolean isCCW(Point2D[] points) {
        Coordinate[] vs = new Coordinate[points.length];
        int i = points.length - 1;
        while (i >= 0) {
            vs[i] = new Coordinate(points[i].getX(), points[i].getY());
            --i;
        }
        return CGAlgorithms.isCCW((Coordinate[])vs);
    }

    public static boolean isCCW(ShapePolygon2D pol) {
        Geometry jtsGeom = ShapeGeometryConverter.java2d_to_jts(pol);
        if (jtsGeom.getNumGeometries() == 1) {
            Coordinate[] coords = jtsGeom.getCoordinates();
            return CGAlgorithms.isCCW((Coordinate[])coords);
        }
        return false;
    }

    public static IShapeGeometry getHole(Coordinate[] coordinates) {
        Coordinate[] vs = new Coordinate[coordinates.length];
        if (CGAlgorithms.isCCW((Coordinate[])coordinates)) {
            vs = coordinates;
        } else {
            int i = vs.length - 1;
            while (i >= 0) {
                vs[i] = coordinates[i];
                --i;
            }
        }
        LinearRing ring = geomFact.createLinearRing(vs);
        try {
            return ShapeFactory.createPolygon2D(ShapeGeometryConverter.toShape((LineString)ring));
        }
        catch (NoninvertibleTransformException e) {
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
    }

    public static Shape getExteriorPolygon(SAIGGeneralPath gp) {
        Area area = new Area(gp);
        area.isSingular();
        return area;
    }

    public static IShapeGeometry getNotHolePolygon(ShapePolygon2D pol) {
        ArrayList<Coordinate> arrayCoords = null;
        int numParts = 0;
        double[] theData = new double[6];
        ArrayList<LinearRing> shells = new ArrayList<LinearRing>();
        ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
        Coordinate[] points = null;
        PathIterator theIterator = pol.getPathIterator(null, 0.8);
        while (!theIterator.isDone()) {
            int theType = theIterator.currentSegment(theData);
            switch (theType) {
                case 0: {
                    if (arrayCoords == null) {
                        arrayCoords = new ArrayList<Coordinate>();
                    } else {
                        points = CoordinateArrays.toCoordinateArray((Collection)arrayCoords);
                        try {
                            LinearRing ring = geomFact.createLinearRing(points);
                            if (CGAlgorithms.isCCW((Coordinate[])points)) {
                                holes.add(ring);
                            } else {
                                shells.add(ring);
                            }
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.caught-topology-exception-in-GMLLinearRingHandler"));
                            return null;
                        }
                        arrayCoords = new ArrayList();
                    }
                    ++numParts;
                    arrayCoords.add(new Coordinate(theData[0], theData[1]));
                    break;
                }
                case 1: {
                    arrayCoords.add(new Coordinate(theData[0], theData[1]));
                    break;
                }
                case 2: {
                    LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.seg-quadto-not-supported-here"));
                    break;
                }
                case 3: {
                    LOGGER.debug((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter.seg-cubicto-not-supported-here"));
                    break;
                }
                case 4: {
                    Coordinate firstCoord = (Coordinate)arrayCoords.get(0);
                    arrayCoords.add(new Coordinate(firstCoord.x, firstCoord.y));
                }
            }
            theIterator.next();
        }
        arrayCoords.add((Coordinate)arrayCoords.get(0));
        Coordinate[] coords = CoordinateArrays.toCoordinateArray((Collection)arrayCoords);
        if (numParts == 1) {
            return ShapeGeometryConverter.getExteriorPolygon(coords);
        }
        return ShapeFactory.createGeometry(pol);
    }
}

