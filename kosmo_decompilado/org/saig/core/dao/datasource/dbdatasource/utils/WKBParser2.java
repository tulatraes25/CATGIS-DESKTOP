/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
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
package org.saig.core.dao.datasource.dbdatasource.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.geom.PathIterator;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;

public class WKBParser2 {
    private static final Logger LOGGER = Logger.getLogger(WKBParser2.class);
    private boolean gHaveM;
    private boolean gHaveZ;
    private boolean gHaveS;
    private static GeometryFactory geomFact = new GeometryFactory();

    public synchronized Geometry parse(byte[] value) {
        ByteBuffer buf = ByteBuffer.wrap(value);
        return this.parseGeometry(buf);
    }

    protected void parseTypeAndSRID(ByteBuffer data) {
        byte endian = data.get();
        int typeword = data.getInt();
        int realtype = typeword & 0x1FFFFFFF;
        this.gHaveZ = (typeword & Integer.MIN_VALUE) != 0;
        this.gHaveM = (typeword & 0x40000000) != 0;
        this.gHaveS = (typeword & 0x20000000) != 0;
        int srid = -1;
        if (this.gHaveS) {
            srid = data.getInt();
        }
    }

    protected Geometry parseGeometry(ByteBuffer data) {
        byte endian = data.get();
        if (endian == 1) {
            data.order(ByteOrder.LITTLE_ENDIAN);
        }
        int typeword = data.getInt();
        int realtype = typeword & 0x1FFFFFFF;
        boolean haveZ = (typeword & Integer.MIN_VALUE) != 0;
        boolean haveM = (typeword & 0x40000000) != 0;
        boolean haveS = (typeword & 0x20000000) != 0;
        int srid = -1;
        if (haveS) {
            srid = data.getInt();
        }
        MultiPoint result1 = null;
        switch (realtype) {
            case 1: {
                result1 = this.parsePoint(data, haveZ, haveM);
                break;
            }
            case 2: {
                result1 = this.parseLineString(data, haveZ, haveM);
                break;
            }
            case 3: {
                result1 = this.parsePolygon(data, haveZ, haveM);
                break;
            }
            case 4: {
                result1 = this.parseMultiPoint(data);
                break;
            }
            case 5: {
                result1 = this.parseMultiLineString(data);
                break;
            }
            case 6: {
                result1 = this.parseMultiPolygon(data);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown Geometry Type!");
            }
        }
        return result1;
    }

    private Point parsePoint(ByteBuffer data, boolean haveZ, boolean haveM) {
        Point result;
        double X = data.getDouble();
        double Y = data.getDouble();
        if (haveZ) {
            double Z = data.getDouble();
            result = geomFact.createPoint(new Coordinate(X, Y, Z));
        } else {
            result = geomFact.createPoint(new Coordinate(X, Y));
        }
        if (haveM) {
            LOGGER.warn((Object)"M no soportado. (WKBParser de Kosmo, dentro de parsePoint)");
        }
        return result;
    }

    private Coordinate parseCoordinate(ByteBuffer data, boolean haveZ, boolean haveM) {
        Coordinate result;
        double X = data.getDouble();
        double Y = data.getDouble();
        if (haveZ) {
            double Z = data.getDouble();
            result = new Coordinate(X, Y, Z);
        } else {
            result = new Coordinate(X, Y);
        }
        if (haveM) {
            LOGGER.warn((Object)"M no soportado. (WKBParser de Kosmo, dentro de parsePoint)");
        }
        return result;
    }

    private Coordinate[] parsePointArray(ByteBuffer data, boolean haveZ, boolean haveM) {
        int count = data.getInt();
        Coordinate[] result = new Coordinate[count];
        int i = 0;
        while (i < count) {
            result[i] = this.parseCoordinate(data, haveZ, haveM);
            ++i;
        }
        return result;
    }

    private MultiPoint parseMultiPoint(ByteBuffer data) {
        Coordinate[] points = new Coordinate[data.getInt()];
        int i = 0;
        while (i < points.length) {
            this.parseTypeAndSRID(data);
            points[i] = this.parseCoordinate(data, this.gHaveZ, this.gHaveM);
            ++i;
        }
        return geomFact.createMultiPoint(points);
    }

    private LineString parseLineString(ByteBuffer data, boolean haveZ, boolean haveM) {
        Coordinate[] points = this.parsePointArray(data, haveZ, haveM);
        return geomFact.createLineString(points);
    }

    private LinearRing parseLinearRing(ByteBuffer data, boolean haveZ, boolean haveM) {
        Coordinate[] points = this.parsePointArray(data, haveZ, haveM);
        return geomFact.createLinearRing(points);
    }

    private Polygon parsePolygon(ByteBuffer data, boolean haveZ, boolean haveM) {
        int count = data.getInt();
        LinearRing extLinearRing = null;
        LinearRing[] rings = null;
        if (count > 1) {
            rings = new LinearRing[count - 1];
        }
        int i = 0;
        while (i < count) {
            if (i == 0) {
                extLinearRing = this.parseLinearRing(data, haveZ, haveM);
            } else {
                rings[i - 1] = this.parseLinearRing(data, haveZ, haveM);
            }
            ++i;
        }
        return geomFact.createPolygon(extLinearRing, rings);
    }

    private MultiLineString parseMultiLineString(ByteBuffer data) {
        int count = data.getInt();
        LineString[] lines = new LineString[count];
        int i = 0;
        while (i < count) {
            this.parseTypeAndSRID(data);
            lines[i] = this.parseLineString(data, this.gHaveZ, this.gHaveM);
            ++i;
        }
        return geomFact.createMultiLineString(lines);
    }

    private MultiPolygon parseMultiPolygon(ByteBuffer data) {
        int count = data.getInt();
        Polygon[] polys = new Polygon[count];
        int i = 0;
        while (i < count) {
            this.parseTypeAndSRID(data);
            polys[i] = this.parsePolygon(data, this.gHaveZ, this.gHaveM);
            ++i;
        }
        return geomFact.createMultiPolygon(polys);
    }

    protected SAIGGeneralPath getGeneralPathX(IShape[] geometries) {
        SAIGGeneralPath shape = new SAIGGeneralPath();
        int i = 0;
        while (i < geometries.length) {
            IShape shp = geometries[i];
            PathIterator theIterator = shp.getPathIterator(null);
            double[] theData = new double[6];
            while (!theIterator.isDone()) {
                int theType = theIterator.currentSegment(theData);
                switch (theType) {
                    case 0: {
                        shape.moveTo(theData[0], theData[1]);
                        break;
                    }
                    case 1: {
                        shape.lineTo(theData[0], theData[1]);
                        break;
                    }
                    case 2: {
                        shape.quadTo(theData[0], theData[1], theData[2], theData[3]);
                        break;
                    }
                    case 3: {
                        shape.curveTo(theData[0], theData[1], theData[2], theData[3], theData[4], theData[5]);
                        break;
                    }
                    case 4: {
                        if (i != geometries.length - 1) break;
                        shape.closePath();
                    }
                }
                theIterator.next();
            }
            ++i;
        }
        return shape;
    }
}

