/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateFilter
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 */
package es.kosmo.core.dao.datasource.filedatasource.gml;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import es.kosmo.core.dao.datasource.filedatasource.gml.format.FormatUtil;
import es.kosmo.core.geometry.filters.ZCoordinateCountFilter;

public class GmlConverter {
    public static String geom2Gml(Geometry geom, String crsName) {
        String gml = "";
        if (geom instanceof Point) {
            gml = GmlConverter.point2Gml((Point)geom, crsName);
        } else if (geom instanceof LineString) {
            gml = GmlConverter.lineString2Gml((LineString)geom, crsName);
        } else if (geom instanceof Polygon) {
            gml = GmlConverter.polygon2Gml((Polygon)geom, crsName);
        } else if (geom instanceof MultiPoint) {
            gml = GmlConverter.multiPoint2Gml((MultiPoint)geom, crsName);
        } else if (geom instanceof MultiLineString) {
            gml = GmlConverter.multiLine2Gml((MultiLineString)geom, crsName);
        } else if (geom instanceof MultiPolygon) {
            gml = GmlConverter.multiPolygon2Gml((MultiPolygon)geom, crsName);
        }
        return gml;
    }

    private static String point2Gml(Point point, String crsName) {
        String format = FormatUtil.getFormat("GmlPoint.template");
        String dimension = GmlConverter.dimensionValue((Geometry)point);
        format = String.format(format, crsName, dimension, dimension, GmlConverter.verticesList(point.getCoordinates()));
        return format;
    }

    private static String lineString2Gml(LineString line, String crsName) {
        String format = FormatUtil.getFormat("GmlLineString.template");
        String dimension = GmlConverter.dimensionValue((Geometry)line);
        Coordinate[] vertices = line.getCoordinates();
        format = String.format(format, crsName, dimension, dimension, GmlConverter.verticesList(vertices));
        return format;
    }

    private static String polygon2Gml(Polygon polygon, String crsName) {
        String format = FormatUtil.getFormat("GmlPolygon.template");
        String dimension = GmlConverter.dimensionValue((Geometry)polygon);
        LineString exteriorRing = polygon.getExteriorRing();
        String exteriorRingStr = GmlConverter.linearRing2Gml(exteriorRing);
        StringBuilder intRingsStr = new StringBuilder();
        int n = polygon.getNumInteriorRing();
        int i = 0;
        while (i < n) {
            LineString linearRing = polygon.getInteriorRingN(i);
            intRingsStr.append("<gml:interior>");
            intRingsStr.append(GmlConverter.linearRing2Gml(linearRing));
            intRingsStr.append("</gml:interior>");
            ++i;
        }
        format = String.format(format, crsName, dimension, exteriorRingStr, intRingsStr.toString());
        return format;
    }

    private static String multiPoint2Gml(MultiPoint multipoint, String crsName) {
        String format = FormatUtil.getFormat("GmlMultiPoint.template");
        String dimension = GmlConverter.dimensionValue((Geometry)multipoint);
        StringBuilder strBuild = new StringBuilder();
        int n = multipoint.getNumGeometries();
        int i = 0;
        while (i < n) {
            Point point = (Point)multipoint.getGeometryN(i);
            strBuild.append("<gml:pointMember>");
            strBuild.append(GmlConverter.point2Gml(point, crsName));
            strBuild.append("</gml:pointMember>");
            ++i;
        }
        format = String.format(format, crsName, dimension, strBuild.toString());
        return format;
    }

    private static String multiLine2Gml(MultiLineString multilinestring, String crsName) {
        String format = FormatUtil.getFormat("GmlMultiLineString.template");
        String dimension = GmlConverter.dimensionValue((Geometry)multilinestring);
        StringBuilder strBuild = new StringBuilder();
        int n = multilinestring.getNumGeometries();
        int i = 0;
        while (i < n) {
            LineString line = (LineString)multilinestring.getGeometryN(i);
            strBuild.append("<gml:lineStringMember>");
            strBuild.append(GmlConverter.lineString2Gml(line, crsName));
            strBuild.append("</gml:lineStringMember>");
            ++i;
        }
        format = String.format(format, crsName, dimension, strBuild.toString());
        return format;
    }

    private static String multiPolygon2Gml(MultiPolygon multipolygon, String crsName) {
        String format = FormatUtil.getFormat("GmlMultiPolygon.template");
        String dimension = GmlConverter.dimensionValue((Geometry)multipolygon);
        StringBuilder strBuild = new StringBuilder();
        int n = multipolygon.getNumGeometries();
        int i = 0;
        while (i < n) {
            Polygon poly = (Polygon)multipolygon.getGeometryN(i);
            strBuild.append("<gml:polygonMember>");
            strBuild.append(GmlConverter.polygon2Gml(poly, crsName));
            strBuild.append("</gml:polygonMember>");
            ++i;
        }
        format = String.format(format, crsName, dimension, strBuild.toString());
        return format;
    }

    private static String linearRing2Gml(LineString line) {
        String format = FormatUtil.getFormat("GmlLinearRing.template");
        Coordinate[] vertices = line.getCoordinates();
        format = String.format(format, GmlConverter.dimensionValue((Geometry)line), GmlConverter.verticesList(vertices));
        return format;
    }

    private static String verticesList(Coordinate[] vertices) {
        StringBuilder verticesStr = new StringBuilder();
        Coordinate[] coordinateArray = vertices;
        int n = vertices.length;
        int n2 = 0;
        while (n2 < n) {
            Coordinate point = coordinateArray[n2];
            verticesStr.append(String.valueOf(point.x) + " " + point.y + " ");
            if (!Double.isNaN(point.z)) {
                verticesStr.append(String.valueOf(point.z) + " ");
            }
            ++n2;
        }
        String string = verticesStr.toString();
        return string;
    }

    private static String dimensionValue(Geometry geom) {
        String dimValue = "2";
        ZCoordinateCountFilter filter = new ZCoordinateCountFilter();
        geom.apply((CoordinateFilter)filter);
        if (filter.getCount() > 0) {
            dimValue = "3";
        }
        return dimValue;
    }
}

