/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.CGAlgorithms
 *  com.vividsolutions.jts.geom.Coordinate
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
package org.geotools.data.oracle.sdo;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
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
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.geotools.data.oracle.sdo.AttributeList;
import org.geotools.data.oracle.sdo.CoordinateAccess;
import org.geotools.data.oracle.sdo.CoordinateAccessFactory;
import org.geotools.data.oracle.sdo.Coordinates;
import org.geotools.data.oracle.sdo.OrdinateList;
import org.saig.jump.lang.I18N;

public final class SDO {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle.sdo");
    public static final int SRID_NULL = -1;
    protected static GeometryFactory geomFact = new GeometryFactory();

    public static int gType(Geometry geom) {
        int d = SDO.D(geom) * 1000;
        int l = SDO.L(geom) * 100;
        int tt = SDO.TT(geom);
        return d + l + tt;
    }

    public static int D(Geometry geom) {
        CoordinateSequenceFactory f = geom.getFactory().getCoordinateSequenceFactory();
        if (f instanceof CoordinateAccessFactory) {
            return ((CoordinateAccessFactory)f).getDimension();
        }
        return Double.isNaN(geom.getCoordinate().z) ? 2 : 3;
    }

    public static int L(Geometry geom) {
        CoordinateSequenceFactory f = geom.getFactory().getCoordinateSequenceFactory();
        if (f instanceof CoordinateAccessFactory) {
            return ((CoordinateAccessFactory)f).getDimension();
        }
        return 0;
    }

    public static int TT(Geometry geom) {
        if (geom == null) {
            return 0;
        }
        if (geom instanceof Point) {
            return 1;
        }
        if (geom instanceof LineString) {
            return 2;
        }
        if (geom instanceof Polygon) {
            return 3;
        }
        if (geom instanceof MultiPoint) {
            return 5;
        }
        if (geom instanceof MultiLineString) {
            return 6;
        }
        if (geom instanceof MultiPolygon) {
            return 7;
        }
        if (geom instanceof GeometryCollection) {
            return 4;
        }
        throw new IllegalArgumentException(I18N.getMessage(SDO.class, "can-not-encode-{0}-{1}-as-{2}-limited-to-{3}-and-{4}", new Object[]{" JTS ", geom.getGeometryType(), " SDO_GTYPE (", " Point, Line, Polygon, GeometryCollection, MultiPoint, MultiLineString ", " MultiPolygon)"}));
    }

    public static int SRID(Geometry geom) {
        return geom.getFactory().getSRID();
    }

    public static double[] point(Geometry geom) {
        if (geom instanceof Point && SDO.L(geom) == 0) {
            Point point = (Point)geom;
            Coordinate coord = point.getCoordinate();
            return new double[]{coord.x, coord.y, coord.z};
        }
        return null;
    }

    public static int[] elemInfo(Geometry geom) {
        return SDO.elemInfo(geom, SDO.gType(geom));
    }

    public static int[] elemInfo(Geometry geom, int GTYPE) {
        LinkedList<Integer> list = new LinkedList<Integer>();
        SDO.elemInfo(list, geom, 1, GTYPE);
        return SDO.intArray(list);
    }

    private static void elemInfo(List<Integer> elemInfoList, Geometry geom, int STARTING_OFFSET, int GTYPE) {
        int tt = SDO.TT(geom);
        switch (tt) {
            case 1: {
                SDO.addElemInfo(elemInfoList, (Point)geom, STARTING_OFFSET);
                return;
            }
            case 2: {
                SDO.addElemInfo(elemInfoList, (LineString)geom, STARTING_OFFSET);
                return;
            }
            case 3: {
                SDO.addElemInfo(elemInfoList, (Polygon)geom, STARTING_OFFSET, GTYPE);
                return;
            }
            case 5: {
                SDO.addElemInfo(elemInfoList, (MultiPoint)geom, STARTING_OFFSET);
                return;
            }
            case 6: {
                SDO.addElemInfo(elemInfoList, (MultiLineString)geom, STARTING_OFFSET, GTYPE);
                return;
            }
            case 7: {
                SDO.addElemInfo(elemInfoList, (MultiPolygon)geom, STARTING_OFFSET, GTYPE);
                return;
            }
            case 4: {
                SDO.addElemInfo(elemInfoList, (GeometryCollection)geom, STARTING_OFFSET, GTYPE);
                return;
            }
        }
        throw new IllegalArgumentException(I18N.getMessage(SDO.class, "can-not-encode-{0}-{1}-as-{2}-limited-to-{3}-and-{4}", new Object[]{" JTS ", geom.getGeometryType(), " SDO_ELEM_INFO (", " Point, Line, Polygon, GeometryCollection, MultiPoint, MultiLineString ", " MultiPolygon)"}));
    }

    private static void addElemInfo(List<Integer> elemInfoList, Point point, int STARTING_OFFSET) {
        SDO.addInt(elemInfoList, STARTING_OFFSET);
        SDO.addInt(elemInfoList, 1);
        SDO.addInt(elemInfoList, 1);
    }

    private static void addElemInfo(List<Integer> elemInfoList, LineString line, int STARTING_OFFSET) {
        SDO.addInt(elemInfoList, STARTING_OFFSET);
        SDO.addInt(elemInfoList, 2);
        SDO.addInt(elemInfoList, 1);
    }

    private static void addElemInfo(List<Integer> elemInfoList, Polygon polygon, int STARTING_OFFSET, int GTYPE) {
        int HOLES = polygon.getNumInteriorRing();
        if (HOLES == 0) {
            SDO.addInt(elemInfoList, STARTING_OFFSET);
            SDO.addInt(elemInfoList, SDO.elemInfoEType((Geometry)polygon));
            SDO.addInt(elemInfoList, SDO.elemInfoInterpretation((Geometry)polygon, 1003));
            return;
        }
        int LEN = SDO.D(GTYPE) + SDO.L(GTYPE);
        int offset = STARTING_OFFSET;
        LineString ring = polygon.getExteriorRing();
        SDO.addInt(elemInfoList, offset);
        SDO.addInt(elemInfoList, SDO.elemInfoEType((Geometry)polygon));
        SDO.addInt(elemInfoList, SDO.elemInfoInterpretation((Geometry)polygon, 1003));
        offset += ring.getNumPoints() * LEN;
        int i = 1;
        while (i <= HOLES) {
            ring = polygon.getInteriorRingN(i - 1);
            SDO.addInt(elemInfoList, offset);
            SDO.addInt(elemInfoList, 2003);
            SDO.addInt(elemInfoList, SDO.elemInfoInterpretation((Geometry)ring, 2003));
            offset += ring.getNumPoints() * LEN;
            ++i;
        }
    }

    private static void addElemInfo(List<Integer> elemInfoList, MultiPoint points, int STARTING_OFFSET) {
        SDO.addInt(elemInfoList, STARTING_OFFSET);
        SDO.addInt(elemInfoList, 1);
        SDO.addInt(elemInfoList, SDO.elemInfoInterpretation((Geometry)points, 1));
    }

    private static void addElemInfo(List<Integer> elemInfoList, MultiLineString lines, int STARTING_OFFSET, int GTYPE) {
        int offset = STARTING_OFFSET;
        int LEN = SDO.D(GTYPE) + SDO.L(GTYPE);
        int i = 0;
        while (i < lines.getNumGeometries()) {
            LineString line = (LineString)lines.getGeometryN(i);
            SDO.addElemInfo(elemInfoList, line, offset);
            offset += line.getNumPoints() * LEN;
            ++i;
        }
    }

    private static void addElemInfo(List<Integer> elemInfoList, MultiPolygon polys, int STARTING_OFFSET, int GTYPE) {
        int offset = STARTING_OFFSET;
        int LEN = SDO.D(GTYPE) + SDO.L(GTYPE);
        int i = 0;
        while (i < polys.getNumGeometries()) {
            Polygon poly = (Polygon)polys.getGeometryN(i);
            SDO.addElemInfo(elemInfoList, poly, offset, GTYPE);
            offset = SDO.isRectangle(poly) ? (offset += 2 * LEN) : (offset += poly.getNumPoints() * LEN);
            ++i;
        }
    }

    private static void addElemInfo(List<Integer> elemInfoList, GeometryCollection geoms, int STARTING_OFFSET, int GTYPE) {
        int offset = STARTING_OFFSET;
        int LEN = SDO.D(GTYPE) + SDO.L(GTYPE);
        int i = 0;
        while (i < geoms.getNumGeometries()) {
            Geometry geom = geoms.getGeometryN(i);
            SDO.elemInfo(elemInfoList, geom, offset, GTYPE);
            offset = geom instanceof Polygon && SDO.isRectangle((Polygon)geom) ? (offset += 2 * LEN) : (offset += geom.getNumPoints() * LEN);
            ++i;
        }
    }

    protected static void addInts(List<Integer> list, int[] array) {
        int i = 0;
        while (i < array.length) {
            list.add(new Integer(array[i]));
            ++i;
        }
    }

    protected static void addInt(List<Integer> list, int i) {
        list.add(new Integer(i));
    }

    private static int[] intArray(List<Integer> list) {
        int[] array = new int[list.size()];
        int offset = 0;
        Iterator<Integer> i = list.iterator();
        while (i.hasNext()) {
            array[offset] = ((Number)i.next()).intValue();
            ++offset;
        }
        return array;
    }

    public static int elemInfoStartingOffset(Geometry geom) {
        return 1;
    }

    protected static int elemInfoEType(Geometry geom) {
        switch (SDO.TT(geom)) {
            case 0: {
                return 0;
            }
            case 1: {
                return 1;
            }
            case 2: {
                return 2;
            }
            case 3: {
                return SDO.isExterior((Polygon)geom) ? 1003 : 2003;
            }
        }
        throw new IllegalArgumentException(String.valueOf(I18N.getString(SDO.class, "unknown-encoding-of")) + " SDO_GTYPE");
    }

    public static int elemInfoInterpretation(Geometry geom) {
        return SDO.elemInfoInterpretation(geom, SDO.elemInfoEType(geom));
    }

    public static int elemInfoInterpretation(Geometry geom, int etype) {
        switch (etype) {
            case 0: {
                break;
            }
            case 1: {
                if (geom instanceof Point) {
                    return 1;
                }
                if (!(geom instanceof MultiPoint)) break;
                return ((MultiPoint)geom).getNumGeometries();
            }
            case 2: {
                if (SDO.isCurve((LineString)geom)) {
                    return 2;
                }
                return 1;
            }
            case 3: 
            case 1003: 
            case 2003: {
                if (geom instanceof Polygon) {
                    Polygon polygon = (Polygon)geom;
                    if (SDO.isCurve(polygon)) {
                        return 2;
                    }
                    if (SDO.isRectangle(polygon)) {
                        return 3;
                    }
                    if (SDO.isCircle(polygon)) {
                        return 4;
                    }
                }
                return 1;
            }
            case 4: {
                throw new IllegalArgumentException("JTS LineStrings " + I18N.getString(SDO.class, "are-not-composed-of-curves-and-lines") + ".");
            }
            case 5: 
            case 1005: 
            case 2005: {
                throw new IllegalArgumentException("JTS Polygons " + I18N.getString(SDO.class, "are-not-composed-of-curves-and-lines") + ".");
            }
        }
        throw new IllegalArgumentException(I18N.getMessage(SDO.class, "can-not-encode-{0}-{1}-as-{2}-limited-to-{3}-and-{4}", new Object[]{" JTS ", geom.getGeometryType(), " SDO_INTERPRETATION (", " Point, Line, Polygon, GeometryCollection, MultiPoint, MultiLineString ", " MultiPolygon)"}));
    }

    public static double[] ordinates(Geometry geom) {
        ArrayList<double[]> list = new ArrayList<double[]>();
        SDO.coordinates(list, geom);
        return SDO.ordinates(list, geom);
    }

    public static CoordinateSequence getCS(Geometry geom) {
        CoordinateSequence cs = null;
        switch (SDO.TT(geom)) {
            case 0: {
                break;
            }
            case 1: {
                return cs;
            }
            case 2: {
                cs = SDO.getLineStringCS((LineString)geom);
                return cs;
            }
            case 3: {
                return cs;
            }
            case 4: {
                return cs;
            }
            case 5: {
                return cs;
            }
            case 6: {
                return cs;
            }
            case 7: {
                return cs;
            }
        }
        throw new IllegalArgumentException(I18N.getMessage(SDO.class, "can-not-encode-{0}-{1}-as-{2}-limited-to-{3}-and-{4}", new Object[]{" JTS ", geom.getGeometryType(), "SDO_ORDINATRES (", " Point, Line, Polygon, GeometryCollection, MultiPoint, MultiLineString ", " MultiPolygon)"}));
    }

    private static CoordinateSequence getLineStringCS(LineString ls) {
        if (ls.getCoordinateSequence() instanceof CoordinateAccess) {
            CoordinateAccess ca = (CoordinateAccess)ls.getCoordinateSequence();
            return ca;
        }
        return null;
    }

    public static void coordinates(List<double[]> list, Geometry geom) {
        switch (SDO.TT(geom)) {
            case 0: {
                break;
            }
            case 1: {
                SDO.addCoordinates(list, (Point)geom);
                return;
            }
            case 2: {
                SDO.addCoordinates(list, (LineString)geom);
                return;
            }
            case 3: {
                SDO.addCoordinates(list, (Polygon)geom);
                return;
            }
            case 4: {
                SDO.addCoordinates(list, (GeometryCollection)geom);
                return;
            }
            case 5: {
                SDO.addCoordinates(list, (MultiPoint)geom);
                return;
            }
            case 6: {
                SDO.addCoordinates(list, (MultiLineString)geom);
                return;
            }
            case 7: {
                SDO.addCoordinates(list, (MultiPolygon)geom);
                return;
            }
        }
        throw new IllegalArgumentException(I18N.getMessage(SDO.class, "can-not-encode-{0}-{1}-as-{2}-limited-to-{3}-and-{4}", new Object[]{" JTS ", geom.getGeometryType(), "SDO_ORDINATRES (", " Point, Line, Polygon, GeometryCollection, MultiPoint, MultiLineString ", " MultiPolygon)"}));
    }

    private static void addCoordinates(List<double[]> list, CoordinateSequence sequence) {
        if (sequence instanceof CoordinateAccess) {
            CoordinateAccess access = (CoordinateAccess)sequence;
            int i = 0;
            while (i < access.size()) {
                list.add(SDO.ordinateArray(access, i));
                ++i;
            }
        } else {
            int i = 0;
            while (i < sequence.size()) {
                list.add(SDO.ordinateArray(sequence.getCoordinate(i)));
                ++i;
            }
        }
    }

    private static double[] ordinateArray(Coordinate coord) {
        return new double[]{coord.x, coord.y, coord.z};
    }

    private static double[] ordinateArray(CoordinateAccess access, int index) {
        int D = access.getDimension();
        int L = access.getNumAttributes();
        int LEN = D + L;
        double[] ords = new double[LEN];
        int i = 0;
        while (i < LEN) {
            ords[i] = access.getOrdinate(index, i);
            ++i;
        }
        return ords;
    }

    protected static double[] doubleOrdinateArray(CoordinateAccess access, int index) {
        int D = access.getDimension();
        int L = access.getNumAttributes();
        int LEN = D + L;
        double[] ords = new double[LEN];
        int i = 0;
        while (i < LEN) {
            ords[i] = access.getOrdinate(index, i);
            ++i;
        }
        return ords;
    }

    private static void addCoordinates(List<double[]> list, Point point) {
        SDO.addCoordinates(list, point.getCoordinateSequence());
    }

    private static void addCoordinates(List<double[]> list, LineString line) {
        SDO.addCoordinates(list, line.getCoordinateSequence());
    }

    private static void addCoordinates(List<double[]> list, Polygon polygon) {
        switch (SDO.elemInfoInterpretation((Geometry)polygon)) {
            case 4: {
                break;
            }
            case 3: {
                SDO.addCoordinatesInterpretation3(list, polygon);
                break;
            }
            case 2: {
                break;
            }
            case 1: {
                SDO.addCoordinatesInterpretation1(list, polygon);
            }
        }
    }

    private static void addCoordinatesInterpretation3(List<double[]> list, Polygon poly) {
        Envelope e = poly.getEnvelopeInternal();
        list.add(new double[]{e.getMinX(), e.getMinY()});
        list.add(new double[]{e.getMaxX(), e.getMaxY()});
    }

    private static void addCoordinatesInterpretation1(List<double[]> list, Polygon polygon) {
        int holes = polygon.getNumInteriorRing();
        SDO.addCoordinates(list, SDO.counterClockWise(polygon.getFactory().getCoordinateSequenceFactory(), polygon.getExteriorRing().getCoordinateSequence()));
        int i = 0;
        while (i < holes) {
            SDO.addCoordinates(list, SDO.clockWise(polygon.getFactory().getCoordinateSequenceFactory(), polygon.getInteriorRingN(i).getCoordinateSequence()));
            ++i;
        }
    }

    private static void addCoordinates(List<double[]> list, MultiPoint points) {
        int i = 0;
        while (i < points.getNumGeometries()) {
            SDO.addCoordinates(list, (Point)points.getGeometryN(i));
            ++i;
        }
    }

    private static void addCoordinates(List<double[]> list, MultiLineString lines) {
        int i = 0;
        while (i < lines.getNumGeometries()) {
            SDO.addCoordinates(list, (LineString)lines.getGeometryN(i));
            ++i;
        }
    }

    private static void addCoordinates(List<double[]> list, MultiPolygon polys) {
        int i = 0;
        while (i < polys.getNumGeometries()) {
            SDO.addCoordinates(list, (Polygon)polys.getGeometryN(i));
            ++i;
        }
    }

    private static void addCoordinates(List<double[]> list, GeometryCollection geoms) {
        int i = 0;
        while (i < geoms.getNumGeometries()) {
            Geometry geom = geoms.getGeometryN(i);
            if (geom instanceof Point) {
                SDO.addCoordinates(list, (Point)geom);
            } else if (geom instanceof LineString) {
                SDO.addCoordinates(list, (LineString)geom);
            } else if (geom instanceof Polygon) {
                SDO.addCoordinates(list, (Polygon)geom);
            } else if (geom instanceof MultiPoint) {
                SDO.addCoordinates(list, (MultiPoint)geom);
            } else if (geom instanceof MultiLineString) {
                SDO.addCoordinates(list, (MultiLineString)geom);
            } else if (geom instanceof MultiPolygon) {
                SDO.addCoordinates(list, (MultiPolygon)geom);
            } else if (geom instanceof GeometryCollection) {
                SDO.addCoordinates(list, (GeometryCollection)geom);
            }
            ++i;
        }
    }

    public static double[] ordinateArray(CoordinateSequence coords, int ordinate) {
        if (coords instanceof CoordinateAccess) {
            CoordinateAccess access = (CoordinateAccess)coords;
            return access.toOrdinateArray(ordinate);
        }
        int LENGTH = coords.size();
        double[] array = new double[LENGTH];
        if (ordinate == 0) {
            int i = 0;
            while (i < LENGTH) {
                Coordinate c = coords.getCoordinate(i);
                array[i] = c != null ? c.x : Double.NaN;
                ++i;
            }
        } else if (ordinate == 1) {
            int i = 0;
            while (i < LENGTH) {
                Coordinate c = coords.getCoordinate(i);
                array[i] = c != null ? c.y : Double.NaN;
                ++i;
            }
        } else if (ordinate == 2) {
            int i = 0;
            while (i < LENGTH) {
                Coordinate c = coords.getCoordinate(i);
                array[i] = c != null ? c.z : Double.NaN;
                ++i;
            }
        } else {
            int i = 0;
            while (i < LENGTH) {
                array[i] = Double.NaN;
                ++i;
            }
        }
        return array;
    }

    public static double[] ordinateArray(Coordinate[] array, int ordinate) {
        if (array == null) {
            return null;
        }
        int LENGTH = array.length;
        double[] ords = new double[LENGTH];
        if (ordinate == 0) {
            int i = 0;
            while (i < LENGTH) {
                Coordinate c = array[i];
                ords[i] = c != null ? c.x : Double.NaN;
                ++i;
            }
        } else if (ordinate == 1) {
            int i = 0;
            while (i < LENGTH) {
                Coordinate c = array[i];
                ords[i] = c != null ? c.y : Double.NaN;
                ++i;
            }
        } else if (ordinate == 2) {
            int i = 0;
            while (i < LENGTH) {
                Coordinate c = array[i];
                ords[i] = c != null ? c.z : Double.NaN;
                ++i;
            }
        } else {
            int i = 0;
            while (i < LENGTH) {
                ords[i] = Double.NaN;
                ++i;
            }
        }
        return ords;
    }

    public static double[] ordinateArray(List<Coordinate> list, int ordinate) {
        if (list == null) {
            return null;
        }
        int LENGTH = list.size();
        double[] ords = new double[LENGTH];
        if (ordinate == 0) {
            int i = 0;
            while (i < LENGTH) {
                Coordinate c = list.get(i);
                ords[i] = c != null ? c.x : Double.NaN;
                ++i;
            }
        } else if (ordinate == 1) {
            int i = 0;
            while (i < LENGTH) {
                Coordinate c = list.get(i);
                ords[i] = c != null ? c.y : Double.NaN;
                ++i;
            }
        } else if (ordinate == 2) {
            int i = 0;
            while (i < LENGTH) {
                Coordinate c = list.get(i);
                ords[i] = c != null ? c.z : Double.NaN;
                ++i;
            }
        } else {
            int i = 0;
            while (i < LENGTH) {
                ords[i] = Double.NaN;
                ++i;
            }
        }
        return ords;
    }

    public static double[] ordinates(List<double[]> list, Geometry geom) {
        LOGGER.finest(String.valueOf(I18N.getString(SDO.class, "ordinates")) + "D:" + SDO.D(geom));
        LOGGER.finest(String.valueOf(I18N.getString(SDO.class, "ordinates")) + "L:" + SDO.L(geom));
        if (SDO.D(geom) == 3) {
            return SDO.ordinates3d(list, SDO.L(geom));
        }
        return SDO.ordinates2d(list, SDO.L(geom));
    }

    public static double[] ordinates2d(List<double[]> list) {
        int NUMBER2 = list.size();
        int LEN = 2;
        double[] array = new double[NUMBER2 * 2];
        int offset = 0;
        int i = 0;
        while (i < NUMBER2) {
            double[] ords = list.get(i);
            if (ords != null) {
                array[offset++] = ords[0];
                array[offset++] = ords[1];
            } else {
                array[offset++] = Double.NaN;
                array[offset++] = Double.NaN;
            }
            ++i;
        }
        return array;
    }

    public static double[] ordinates3d(List<double[]> list) {
        int NUMBER2 = list.size();
        int LEN = 3;
        double[] array = new double[NUMBER2 * 3];
        int offset = 0;
        int i = 0;
        while (i < NUMBER2) {
            double[] ords = list.get(i);
            if (ords != null) {
                array[offset++] = ords[0];
                array[offset++] = ords[1];
                array[offset++] = ords[2];
            } else {
                array[offset++] = Double.NaN;
                array[offset++] = Double.NaN;
                array[offset++] = Double.NaN;
            }
            ++i;
        }
        return array;
    }

    public static double[] ordinates2d(List<double[]> list, int L) {
        if (L == 0) {
            return SDO.ordinates2d(list);
        }
        int NUMBER2 = list.size();
        int LEN = 2 + L;
        double[] array = new double[NUMBER2 * LEN];
        int i = 0;
        while (i < NUMBER2) {
            double[] ords = list.get(i);
            int j = 0;
            while (j < LEN) {
                array[i * LEN + j] = ords[j];
                ++j;
            }
            ++i;
        }
        return array;
    }

    public static double[] ordinates3d(List<double[]> list, int L) {
        if (L == 0) {
            return SDO.ordinates3d(list);
        }
        int NUMBER2 = list.size();
        int LEN = 3 + L;
        double[] array = new double[NUMBER2 * LEN];
        int i = 0;
        while (i < NUMBER2) {
            double[] ords = list.get(i);
            int j = 0;
            while (j < LEN) {
                array[i * LEN + j] = ords[j];
                ++j;
            }
            ++i;
        }
        return array;
    }

    public static CoordinateSequence counterClockWise(CoordinateSequenceFactory factory, CoordinateSequence ring) {
        if (CGAlgorithms.isCCW((Coordinate[])ring.toCoordinateArray())) {
            return ring;
        }
        return Coordinates.reverse(factory, ring);
    }

    public static CoordinateSequence clockWise(CoordinateSequenceFactory factory, CoordinateSequence ring) {
        if (!CGAlgorithms.isCCW((Coordinate[])ring.toCoordinateArray())) {
            return ring;
        }
        return Coordinates.reverse(factory, ring);
    }

    protected static Coordinate[] reverse(Coordinate[] ring) {
        int length = ring.length;
        Coordinate[] reverse = new Coordinate[length];
        int i = 0;
        while (i < length) {
            reverse[i] = ring[length - i - 1];
            ++i;
        }
        return reverse;
    }

    private static boolean isExterior(Polygon poly) {
        return true;
    }

    private static boolean isCircle(Polygon polygon) {
        return false;
    }

    private static boolean isRectangle(Polygon polygon) {
        if (polygon.getFactory().getSRID() != -1) {
            return false;
        }
        if (SDO.L((Geometry)polygon) != 0) {
            return false;
        }
        Coordinate[] coords = polygon.getCoordinates();
        if (coords.length != 5) {
            return false;
        }
        if (coords[0] == null || coords[1] == null || coords[2] == null || coords[3] == null) {
            return false;
        }
        if (!coords[0].equals2D(coords[4])) {
            return false;
        }
        double x1 = coords[0].x;
        double y1 = coords[0].y;
        double x2 = coords[1].x;
        double y2 = coords[1].y;
        double x3 = coords[2].x;
        double y3 = coords[2].y;
        double x4 = coords[3].x;
        double y4 = coords[3].y;
        if (x1 == x4 && y1 == y2 && x3 == x2 && y3 == y4) {
            return true;
        }
        return x1 == x2 && y1 == y4 && x3 == x4 && y3 == y2;
    }

    private static boolean isCurve(Polygon polygon) {
        return false;
    }

    private static boolean isCurve(LineString lineString) {
        return false;
    }

    private static CoordinateSequence subList(CoordinateSequenceFactory factory, CoordinateSequence coords, int GTYPE, int[] elemInfo, int triplet) {
        int STARTING_OFFSET = SDO.STARTING_OFFSET(elemInfo, triplet);
        int ENDING_OFFSET = SDO.STARTING_OFFSET(elemInfo, triplet + 1);
        if (STARTING_OFFSET == 1 && ENDING_OFFSET == -1) {
            return coords;
        }
        int LEN = SDO.D(GTYPE) + SDO.L(GTYPE);
        int start = (STARTING_OFFSET - 1) / LEN;
        int end = ENDING_OFFSET != -1 ? (ENDING_OFFSET - 1) / LEN : coords.size();
        return SDO.subList(factory, coords, start, end);
    }

    private static CoordinateSequence subList(CoordinateSequenceFactory factory, CoordinateSequence coords, int start, int end) {
        if (start == 0 && end == coords.size()) {
            return coords;
        }
        return Coordinates.subList(factory, coords, start, end);
    }

    private static LinearRing[] toInteriorRingArray(List<LinearRing> list) {
        return (LinearRing[])SDO.toArray(list, LinearRing.class);
    }

    private static LineString[] toLineStringArray(List<LineString> list) {
        return (LineString[])SDO.toArray(list, LineString.class);
    }

    private static Polygon[] toPolygonArray(List<Polygon> list) {
        return (Polygon[])SDO.toArray(list, Polygon.class);
    }

    private static Geometry[] toGeometryArray(List<Geometry> list) {
        return (Geometry[])SDO.toArray(list, Geometry.class);
    }

    private static <T> Object toArray(List<T> list, Class<?> type) {
        if (list == null) {
            return null;
        }
        Object array = Array.newInstance(type, list.size());
        int index = 0;
        Iterator<T> i = list.iterator();
        while (i.hasNext()) {
            Array.set(array, index, i.next());
            ++index;
        }
        return array;
    }

    public static int D(int GTYPE) {
        return GTYPE / 1000;
    }

    public static int L(int GTYPE) {
        return (GTYPE - SDO.D(GTYPE) * 1000) / 100;
    }

    public static int TT(int GTYPE) {
        return GTYPE - SDO.D(GTYPE) * 1000 - SDO.L(GTYPE) * 100;
    }

    private static int STARTING_OFFSET(int[] elemInfo, int triplet) {
        if (triplet * 3 + 0 >= elemInfo.length) {
            return -1;
        }
        return elemInfo[triplet * 3 + 0];
    }

    private static void ensure(String condition, int min, int actual, int max) {
        if (min > actual || actual > max) {
            String msg = MessageFormat.format(condition, new Integer(min), new Integer(actual), new Integer(max));
            throw new IllegalArgumentException(msg);
        }
    }

    private static void ensure(String condition, int actual, int[] set) {
        if (set == null) {
            return;
        }
        int i = 0;
        while (i < set.length) {
            if (set[i] == actual) {
                return;
            }
            ++i;
        }
        StringBuffer array = new StringBuffer();
        int i2 = 0;
        while (i2 < set.length) {
            array.append(set[i2]);
            if (i2 < set.length) {
                array.append(",");
            }
            ++i2;
        }
        String msg = MessageFormat.format(condition, new Integer(actual), array);
        throw new IllegalArgumentException(msg);
    }

    private static int ordinateSize(CoordinateSequence coords, int GTYPE) {
        if (coords == null) {
            return 0;
        }
        return coords.size() * SDO.D(GTYPE);
    }

    private static int ETYPE(int[] elemInfo, int triplet) {
        if (triplet * 3 + 1 >= elemInfo.length) {
            return -1;
        }
        return elemInfo[triplet * 3 + 1];
    }

    private static int INTERPRETATION(int[] elemInfo, int triplet) {
        if (triplet * 3 + 2 >= elemInfo.length) {
            return -1;
        }
        return elemInfo[triplet * 3 + 2];
    }

    public static Coordinate[] asCoordinates(double[] ordinates) {
        return SDO.asCoordiantes(ordinates, 2);
    }

    public static Coordinate[] asCoordiantes(double[] ordinates, int d) {
        int length = ordinates.length / d;
        Coordinate[] coords = new Coordinate[length];
        int i = 0;
        while (i < length) {
            coords[i] = new Coordinate(ordinates[i * d], ordinates[i * d + 1]);
            ++i;
        }
        return coords;
    }

    public static CoordinateSequence coordinates(CoordinateSequenceFactory f, int GTYPE, double[] ordinates) {
        if (ordinates == null || ordinates.length == 0) {
            return f.create(new Coordinate[0]);
        }
        int D = SDO.D(GTYPE);
        int L = SDO.L(GTYPE);
        int TT2 = SDO.TT(GTYPE);
        if (D == 2 && L == 0 && TT2 == 1 && ordinates.length == 3) {
            return f.create(new Coordinate[]{new Coordinate(ordinates[0], ordinates[1], ordinates[2])});
        }
        int LEN = D + L;
        if (ordinates.length % LEN != 0) {
            throw new IllegalArgumentException(I18N.getMessage(SDO.class, "dimension-{0}-{1}-and-{2}-{3}-denote-coordinates-of-{4}-ordinates-{5}-this-can-not-be-resolved-with-an-ordinate-array-of-length-{6}", new Object[]{" D:", D, " L:", L, LEN, ". ", ordinates.length}));
        }
        int LENGTH = ordinates.length / LEN;
        OrdinateList x = new OrdinateList(ordinates, 0, LEN);
        OrdinateList y = new OrdinateList(ordinates, 1, LEN);
        OrdinateList z = null;
        if (D == 3) {
            z = new OrdinateList(ordinates, 2, LEN);
        }
        if (L != 0) {
            OrdinateList[] m = new OrdinateList[L];
            int i = 0;
            while (i < L) {
                m[i] = new OrdinateList(ordinates, D + i, LEN);
                ++i;
            }
            return SDO.coordiantes(f, x, y, z, m);
        }
        return SDO.coordiantes(f, x, y, z);
    }

    public static CoordinateSequence coordiantes(CoordinateSequenceFactory f, OrdinateList x, OrdinateList y, OrdinateList z) {
        int LENGTH = x.size();
        Coordinate[] array = new Coordinate[LENGTH];
        if (z != null) {
            int i = 0;
            while (i < LENGTH) {
                array[i] = new Coordinate(x.getDouble(i), y.getDouble(i), z.getDouble(i));
                ++i;
            }
        } else {
            int i = 0;
            while (i < LENGTH) {
                array[i] = new Coordinate(x.getDouble(i), y.getDouble(i));
                ++i;
            }
        }
        return f.create(array);
    }

    public static CoordinateSequence coordiantes(CoordinateSequenceFactory f, AttributeList x, AttributeList y, AttributeList z) {
        int LENGTH = x.size();
        Coordinate[] array = new Coordinate[LENGTH];
        if (z != null) {
            int i = 0;
            while (i < LENGTH) {
                array[i] = new Coordinate(x.getDouble(i), y.getDouble(i), z.getDouble(i));
                ++i;
            }
        } else {
            int i = 0;
            while (i < LENGTH) {
                array[i] = new Coordinate(x.getDouble(i), y.getDouble(i));
                ++i;
            }
        }
        return f.create(array);
    }

    public static CoordinateSequence coordiantes(CoordinateSequenceFactory f, OrdinateList x, OrdinateList y, OrdinateList z, OrdinateList[] m) {
        int L;
        int D = z != null ? 3 : 2;
        int n = L = m != null ? m.length : 0;
        if (f instanceof CoordinateAccess && L != 0) {
            CoordinateAccessFactory factory = (CoordinateAccessFactory)f;
            double[][] xyz = new double[D][];
            double[][] measures = new double[L][];
            xyz[0] = x.toDoubleArray();
            xyz[1] = y.toDoubleArray();
            if (D == 3) {
                xyz[2] = z.toDoubleArray();
            }
            int i = 0;
            while (i < L) {
                measures[i] = m[i].toDoubleArray();
                ++i;
            }
            return factory.create(xyz, (Object[])measures);
        }
        return SDO.coordiantes(f, x, y, z);
    }

    public static CoordinateSequence coordiantes(CoordinateSequenceFactory f, AttributeList x, AttributeList y, AttributeList z, AttributeList[] m) {
        int L;
        int D = z != null ? 3 : 2;
        int n = L = m != null ? m.length : 0;
        if (f instanceof CoordinateAccess && L != 0) {
            CoordinateAccessFactory factory = (CoordinateAccessFactory)f;
            double[][] xyz = new double[D][];
            Object[] measures = new Object[L];
            xyz[0] = x.toDoubleArray();
            xyz[1] = y.toDoubleArray();
            if (D == 3) {
                xyz[2] = z.toDoubleArray();
            }
            int i = 0;
            while (i < L) {
                measures[i] = m[i].toObjectArray();
                ++i;
            }
            return factory.create(xyz, measures);
        }
        return SDO.coordiantes(f, x, y, z);
    }

    public static Geometry create(GeometryFactory gf, int GTYPE, int SRID, double[] point, int[] elemInfo, double[] ordinates) {
        CoordinateSequence coords;
        int L = SDO.L(GTYPE);
        int TT2 = SDO.TT(GTYPE);
        if (L == 0 && TT2 == 1 && point != null && elemInfo == null) {
            coords = SDO.coordinates(gf.getCoordinateSequenceFactory(), GTYPE, point);
            elemInfo = new int[]{1, 1, 1};
        } else {
            coords = SDO.coordinates(gf.getCoordinateSequenceFactory(), GTYPE, ordinates);
        }
        return SDO.create(gf, GTYPE, SRID, elemInfo, 0, coords, -1);
    }

    public static Geometry create(GeometryFactory gf, int GTYPE, int SRID, int[] elemInfo, int triplet, CoordinateSequence coords, int N) {
        switch (SDO.TT(GTYPE)) {
            case 1: {
                return SDO.createPoint(gf, GTYPE, SRID, elemInfo, triplet, coords);
            }
            case 2: {
                return SDO.createLine(gf, GTYPE, SRID, elemInfo, triplet, coords);
            }
            case 3: {
                return SDO.createPolygon(gf, GTYPE, SRID, elemInfo, triplet, coords);
            }
            case 5: {
                return SDO.createMultiPoint(gf, GTYPE, SRID, elemInfo, triplet, coords);
            }
            case 6: {
                return SDO.createMultiLine(gf, GTYPE, SRID, elemInfo, triplet, coords, N);
            }
            case 7: {
                return SDO.createMultiPolygon(gf, GTYPE, SRID, elemInfo, triplet, coords, N);
            }
            case 4: {
                return SDO.createCollection(gf, GTYPE, SRID, elemInfo, triplet, coords, N);
            }
        }
        LOGGER.warning(I18N.getMessage(SDO.class, "can-not-represent-provided-{0}-{1}-{2}-using-jts-geometry", new Object[]{" SDO STRUCT (GTYPE =", GTYPE, ") "}));
        return geomFact.buildGeometry(new ArrayList());
    }

    private static Point createPoint(GeometryFactory gf, int GTYPE, int SRID, int[] elemInfo, int element, CoordinateSequence coords) {
        int STARTING_OFFSET = SDO.STARTING_OFFSET(elemInfo, element);
        int etype = SDO.ETYPE(elemInfo, element);
        int INTERPRETATION = SDO.INTERPRETATION(elemInfo, element);
        if (STARTING_OFFSET < 1 || STARTING_OFFSET > coords.size()) {
            throw new IllegalArgumentException("ELEM_INFO STARTING_OFFSET " + STARTING_OFFSET + " " + I18N.getString(SDO.class, "inconsistent-with") + " ORDINATES " + I18N.getString(SDO.class, "length") + " " + coords.size());
        }
        if (etype != 1) {
            throw new IllegalArgumentException("ETYPE " + etype + " " + I18N.getString(SDO.class, "inconsistent-with-expected") + "POINT");
        }
        if (INTERPRETATION != 1) {
            LOGGER.warning(I18N.getMessage(SDO.class, "could-not-create-{0}-with-{1}-{2}-{3}-we-only-expect-one-for-a-single-point", new Object[]{" JTS Point ", " INTERPRETATION ", INTERPRETATION, " - "}));
            return null;
        }
        Point point = new Point(SDO.subList(gf.getCoordinateSequenceFactory(), coords, GTYPE, elemInfo, element), gf);
        point.setSRID(SRID);
        return point;
    }

    private static LineString createLine(GeometryFactory gf, int GTYPE, int SRID, int[] elemInfo, int triplet, CoordinateSequence coords) {
        int STARTING_OFFSET = SDO.STARTING_OFFSET(elemInfo, triplet);
        int etype = SDO.ETYPE(elemInfo, triplet);
        int INTERPRETATION = SDO.INTERPRETATION(elemInfo, triplet);
        if (etype != 2) {
            return null;
        }
        if (INTERPRETATION != 1) {
            LOGGER.warning(I18N.getMessage(SDO.class, "could-not-create-{0}-with-{1}-{2}-{3}-we-only-expect-one-for-straight-edges", new Object[]{" JTS LineString ", " INTERPRETATION ", INTERPRETATION, " - "}));
            return null;
        }
        if (INTERPRETATION != 1) {
            throw new IllegalArgumentException("ELEM_INFO INTERPRETATION " + INTERPRETATION + " " + I18N.getString(SDO.class, "not-supported-by") + " JTS LineString.  " + I18N.getString(SDO.class, "straight-edges") + "( ELEM_INFO INTERPRETAION 1) " + I18N.getString(SDO.class, "is-supported"));
        }
        LineString line = new LineString(SDO.subList(gf.getCoordinateSequenceFactory(), coords, GTYPE, elemInfo, triplet), gf);
        line.setSRID(SRID);
        return line;
    }

    private static Polygon createPolygon(GeometryFactory gf, int GTYPE, int SRID, int[] elemInfo, int triplet, CoordinateSequence coords) throws IllegalArgumentException {
        int etype;
        int STARTING_OFFSET = SDO.STARTING_OFFSET(elemInfo, triplet);
        int eTYPE = SDO.ETYPE(elemInfo, triplet);
        int INTERPRETATION = SDO.INTERPRETATION(elemInfo, triplet);
        SDO.ensure("ELEM_INFO STARTING_OFFSET {1} must be in the range {0}..{1} of COORDINATES", 1, STARTING_OFFSET, SDO.ordinateSize(coords, GTYPE));
        if (1 > STARTING_OFFSET || STARTING_OFFSET > SDO.ordinateSize(coords, GTYPE)) {
            throw new IllegalArgumentException("ELEM_INFO STARTING_OFFSET " + STARTING_OFFSET + I18N.getString(SDO.class, "inconsistent-with-coordinates-length") + SDO.ordinateSize(coords, GTYPE));
        }
        SDO.ensure("ETYPE {0} must be expected POLYGON or POLYGON_EXTERIOR (one of {1})", eTYPE, new int[]{3, 1003});
        if (eTYPE != 3 && eTYPE != 1003) {
            throw new IllegalArgumentException("ETYPE " + eTYPE + I18N.getString(SDO.class, "inconsistent-with-expected-polygon-or-polygon-exterior"));
        }
        if (INTERPRETATION != 1 && INTERPRETATION != 3) {
            LOGGER.warning(I18N.getMessage(SDO.class, "could-not-create-{0}-with-{1}-{2}-{3}-we-can-only-support-one-for-straight-edges-and-three-for-rectangule", new Object[]{" JTS Polygon with", " INTERPRETATION ", INTERPRETATION, " - "}));
            return null;
        }
        LinearRing exteriorRing = SDO.createLinearRing(gf, GTYPE, SRID, elemInfo, triplet, coords);
        LinkedList<LinearRing> rings = new LinkedList<LinearRing>();
        int i = triplet + 1;
        while ((etype = SDO.ETYPE(elemInfo, i)) != -1) {
            if (etype == 2003) {
                rings.add(SDO.createLinearRing(gf, GTYPE, SRID, elemInfo, i, coords));
            } else {
                LinearRing ring;
                if (etype != 3 || !CGAlgorithms.isCCW((Coordinate[])(ring = SDO.createLinearRing(gf, GTYPE, SRID, elemInfo, i, coords)).getCoordinates())) break;
                rings.add(ring);
            }
            ++i;
        }
        Polygon poly = gf.createPolygon(exteriorRing, SDO.toInteriorRingArray(rings));
        poly.setSRID(SRID);
        poly.normalize();
        return poly;
    }

    private static LinearRing createLinearRing(GeometryFactory gf, int GTYPE, int SRID, int[] elemInfo, int triplet, CoordinateSequence coords) {
        LinearRing ring;
        int STARTING_OFFSET = SDO.STARTING_OFFSET(elemInfo, triplet);
        int eTYPE = SDO.ETYPE(elemInfo, triplet);
        int INTERPRETATION = SDO.INTERPRETATION(elemInfo, triplet);
        int LENGTH = coords.size() * SDO.D(GTYPE);
        if (STARTING_OFFSET < 1 || STARTING_OFFSET > LENGTH) {
            throw new IllegalArgumentException("ELEM_INFO STARTING_OFFSET " + STARTING_OFFSET + " " + I18N.getString(SDO.class, "inconsistent-with") + " ORDINATES " + I18N.getString(SDO.class, "length") + " " + coords.size());
        }
        if (eTYPE != 3 && eTYPE != 1003 && eTYPE != 2003) {
            throw new IllegalArgumentException("ETYPE " + eTYPE + " " + I18N.getString(SDO.class, "inconsistent-with-expected") + " POLYGON, POLYGON_EXTERIOR " + I18N.getString(SDO.class, "or") + " POLYGON_INTERIOR");
        }
        if (INTERPRETATION != 1 && INTERPRETATION != 3) {
            LOGGER.warning(I18N.getMessage(SDO.class, "could-not-create-{0}-with-{1}-{2}-{3}-{4}-we-can-only-support-one-for-straight-edges", new Object[]{" LinearRing ", " ", "INTERPRETATION ", INTERPRETATION, " - "}));
            return null;
        }
        if (INTERPRETATION == 1) {
            ring = gf.createLinearRing(SDO.subList(gf.getCoordinateSequenceFactory(), coords, GTYPE, elemInfo, triplet));
        } else if (INTERPRETATION == 3) {
            CoordinateSequence ext = SDO.subList(gf.getCoordinateSequenceFactory(), coords, GTYPE, elemInfo, triplet);
            Coordinate min = ext.getCoordinate(0);
            Coordinate max = ext.getCoordinate(1);
            ring = gf.createLinearRing(new Coordinate[]{min, new Coordinate(max.x, min.y), max, new Coordinate(min.x, max.y), min});
        } else {
            throw new IllegalArgumentException("ELEM_INFO INTERPRETAION " + elemInfo[2] + I18N.getString(SDO.class, "not-supported-for") + " JTS Polygon Linear Rings. ELEM_INFO INTERPRETAION " + I18N.getString(SDO.class, "one-and-three-are-supported"));
        }
        ring.setSRID(SRID);
        return ring;
    }

    private static MultiPoint createMultiPoint(GeometryFactory gf, int GTYPE, int SRID, int[] elemInfo, int triplet, CoordinateSequence coords) {
        int STARTING_OFFSET = SDO.STARTING_OFFSET(elemInfo, triplet);
        int eTYPE = SDO.ETYPE(elemInfo, triplet);
        int INTERPRETATION = SDO.INTERPRETATION(elemInfo, triplet);
        if (STARTING_OFFSET < 1 || STARTING_OFFSET > coords.size()) {
            throw new IllegalArgumentException("ELEM_INFO STARTING_OFFSET " + STARTING_OFFSET + " " + I18N.getString(SDO.class, "inconsistent-with") + " ORDINATES " + I18N.getString(SDO.class, "length") + " " + coords.size());
        }
        if (eTYPE != 1) {
            throw new IllegalArgumentException("ETYPE " + eTYPE + I18N.getString(SDO.class, "inconsistent-with-expected-point"));
        }
        if (INTERPRETATION < 1) {
            LOGGER.warning(I18N.getMessage(SDO.class, "could-not-create-{0}-with-{1}-{2}-{3}-representing-the-number-of-points", new Object[]{" MultiPoint ", " INTERPRETATION ", INTERPRETATION, " - "}));
            return null;
        }
        int LEN = SDO.D(GTYPE) + SDO.L(GTYPE);
        int start = (STARTING_OFFSET - 1) / LEN;
        int end = start + INTERPRETATION;
        MultiPoint points = gf.createMultiPoint(SDO.subList(gf.getCoordinateSequenceFactory(), coords, start, end));
        points.setSRID(SRID);
        return points;
    }

    private static MultiLineString createMultiLine(GeometryFactory gf, int GTYPE, int SRID, int[] elemInfo, int triplet, CoordinateSequence coords, int N) {
        int etype;
        int STARTING_OFFSET = SDO.STARTING_OFFSET(elemInfo, triplet);
        int eTYPE = SDO.ETYPE(elemInfo, triplet);
        int INTERPRETATION = SDO.INTERPRETATION(elemInfo, triplet);
        int LENGTH = coords.size() * SDO.D(GTYPE);
        if (STARTING_OFFSET < 1 || STARTING_OFFSET > LENGTH) {
            throw new IllegalArgumentException("ELEM_INFO STARTING_OFFSET " + STARTING_OFFSET + " " + I18N.getString(SDO.class, "inconsistent-with") + " ORDINATES " + I18N.getString(SDO.class, "length") + " " + coords.size());
        }
        if (eTYPE != 2) {
            throw new IllegalArgumentException("ETYPE " + eTYPE + " " + I18N.getString(SDO.class, "inconsistent-with-expected") + " LINE");
        }
        if (INTERPRETATION != 1) {
            LOGGER.warning(I18N.getMessage(SDO.class, "could-not-create-{0}-with-{1}-{2}-{3}-we-can-only-represent-one-for-straight-edges", new Object[]{" MultiLineString ", " INTERPRETATION ", INTERPRETATION, " - "}));
            return null;
        }
        int LEN = SDO.D(GTYPE) + SDO.L(GTYPE);
        int endTriplet = N != -1 ? triplet + N : elemInfo.length / 3;
        LinkedList<LineString> list = new LinkedList<LineString>();
        int i = triplet;
        while (i < endTriplet && (etype = SDO.ETYPE(elemInfo, i)) != -1) {
            if (etype != 2) break;
            list.add(SDO.createLine(gf, GTYPE, SRID, elemInfo, i, coords));
            ++i;
        }
        MultiLineString lines = gf.createMultiLineString(SDO.toLineStringArray(list));
        lines.setSRID(SRID);
        return lines;
    }

    private static MultiPolygon createMultiPolygon(GeometryFactory gf, int GTYPE, int SRID, int[] elemInfo, int triplet, CoordinateSequence coords, int N) {
        int etype;
        int STARTING_OFFSET = SDO.STARTING_OFFSET(elemInfo, triplet);
        int eTYPE = SDO.ETYPE(elemInfo, triplet);
        int INTERPRETATION = SDO.INTERPRETATION(elemInfo, triplet);
        int LENGTH = coords.size() * SDO.D(GTYPE);
        if (STARTING_OFFSET < 1 || STARTING_OFFSET > LENGTH) {
            throw new IllegalArgumentException("ELEM_INFO STARTING_OFFSET " + STARTING_OFFSET + " " + I18N.getString(SDO.class, "inconsistent-with") + " ORDINATES " + I18N.getString(SDO.class, "length") + " " + coords.size());
        }
        if (eTYPE != 3 && eTYPE != 1003) {
            throw new IllegalArgumentException("ETYPE " + eTYPE + " " + I18N.getString(SDO.class, "inconsistent-with-expected") + " POLYGON " + I18N.getString(SDO.class, "or") + " POLYGON_EXTERIOR");
        }
        if (INTERPRETATION != 1 && INTERPRETATION != 3) {
            LOGGER.warning(I18N.getMessage(SDO.class, "could-not-create-{0}-with-{1}-{2}-{3}-we-can-only-represent-one-for-straight-edges-or-three-for-rectangle", new Object[]{" MultiPolygon ", " INTERPRETATION ", INTERPRETATION, " - "}));
            return null;
        }
        int LEN = SDO.D(GTYPE) + SDO.L(GTYPE);
        int endTriplet = N != -1 ? triplet + N : elemInfo.length / 3 + 1;
        LinkedList<Polygon> list = new LinkedList<Polygon>();
        int i = triplet;
        while (i < endTriplet && (etype = SDO.ETYPE(elemInfo, i)) != -1) {
            if (etype != 3 && etype != 1003) break;
            Polygon poly = SDO.createPolygon(gf, GTYPE, SRID, elemInfo, i, coords);
            i += poly.getNumInteriorRing();
            list.add(poly);
            ++i;
        }
        MultiPolygon polys = gf.createMultiPolygon(SDO.toPolygonArray(list));
        polys.setSRID(SRID);
        polys.normalize();
        return polys;
    }

    private static GeometryCollection createCollection(GeometryFactory gf, int GTYPE, int SRID, int[] elemInfo, int triplet, CoordinateSequence coords, int N) {
        int STARTING_OFFSET = SDO.STARTING_OFFSET(elemInfo, triplet);
        int eTYPE = SDO.ETYPE(elemInfo, triplet);
        int INTERPRETATION = SDO.INTERPRETATION(elemInfo, triplet);
        int LENGTH = coords.size() * SDO.D(GTYPE);
        if (STARTING_OFFSET < 1 || STARTING_OFFSET > LENGTH) {
            throw new IllegalArgumentException("ELEM_INFO STARTING_OFFSET " + STARTING_OFFSET + " " + I18N.getString(SDO.class, "inconsistent-with") + " ORDINATES " + I18N.getString(SDO.class, "length") + " " + coords.size());
        }
        int LEN = SDO.D(GTYPE) + SDO.L(GTYPE);
        int endTriplet = N != -1 ? triplet + N : elemInfo.length / 3 + 1;
        LinkedList<Geometry> list = new LinkedList<Geometry>();
        int i = triplet;
        block7: while (i < endTriplet) {
            LineString geom;
            int etype = SDO.ETYPE(elemInfo, i);
            int interpretation = SDO.INTERPRETATION(elemInfo, i);
            switch (etype) {
                case -1: {
                    break block7;
                }
                case 1: {
                    if (interpretation == 1) {
                        geom = SDO.createPoint(gf, GTYPE, SRID, elemInfo, i, coords);
                        break;
                    }
                    if (interpretation > 1) {
                        geom = SDO.createMultiPoint(gf, GTYPE, SRID, elemInfo, i, coords);
                        break;
                    }
                    throw new IllegalArgumentException("ETYPE.POINT " + I18N.getString(SDO.class, "requires") + " INTERPRETATION >= 1");
                }
                case 2: {
                    geom = SDO.createLine(gf, GTYPE, SRID, elemInfo, i, coords);
                    break;
                }
                case 3: 
                case 1003: {
                    geom = SDO.createPolygon(gf, GTYPE, SRID, elemInfo, i, coords);
                    i += ((Polygon)geom).getNumInteriorRing();
                    break;
                }
                case 2003: {
                    throw new IllegalArgumentException("ETYPE 2003 (Polygon Interior) " + I18N.getString(SDO.class, "no-expected-in-a") + " GeometryCollection" + "(2003 " + I18N.getString(SDO.class, "is-used-to-represent-polygon-holes") + ", " + I18N.getString(SDO.class, "in-a") + " 1003 " + I18N.getString(SDO.class, "polygon-exterior") + ")");
                }
                default: {
                    throw new IllegalArgumentException("ETYPE " + etype + " " + I18N.getString(SDO.class, "not-representable-as-a") + " JTS Geometry." + "(" + I18N.getString(SDO.class, "custom-and-compound-straight-and-curved-geometries-not-supported") + ")");
                }
            }
            list.add((Geometry)geom);
            ++i;
        }
        GeometryCollection geoms = gf.createGeometryCollection(SDO.toGeometryArray(list));
        geoms.setSRID(SRID);
        return geoms;
    }
}

