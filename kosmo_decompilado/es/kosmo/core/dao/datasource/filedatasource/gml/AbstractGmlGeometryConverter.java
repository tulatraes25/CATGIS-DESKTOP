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
import java.text.NumberFormat;
import java.util.Locale;

public abstract class AbstractGmlGeometryConverter {
    protected static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);
    protected String crsName;

    static {
        NUMBER_FORMAT.setGroupingUsed(false);
    }

    protected AbstractGmlGeometryConverter(String name) {
        this.crsName = name;
    }

    protected abstract String getPointTemplate();

    protected abstract String getLineStringTemplate();

    protected abstract String getPolygonTemplate();

    protected abstract String getLinearRingTemplate();

    protected abstract String getMultiPointTemplate();

    protected abstract String getMultiLineStringTemplate();

    protected abstract String getMultiPolygonTemplate();

    public String geom2Gml(Geometry geom) {
        String gml = "";
        if (geom instanceof Point) {
            gml = this.point2Gml((Point)geom, this.crsName);
        } else if (geom instanceof LineString) {
            gml = this.lineString2Gml((LineString)geom, this.crsName);
        } else if (geom instanceof Polygon) {
            gml = this.polygon2Gml((Polygon)geom, this.crsName);
        } else if (geom instanceof MultiPoint) {
            gml = this.multiPoint2Gml((MultiPoint)geom, this.crsName);
        } else if (geom instanceof MultiLineString) {
            gml = this.multiLine2Gml((MultiLineString)geom, this.crsName);
        } else if (geom instanceof MultiPolygon) {
            gml = this.multiPolygon2Gml((MultiPolygon)geom, this.crsName);
        }
        return gml;
    }

    protected String point2Gml(Point point, String crsName) {
        String format = FormatUtil.getFormat(this.getPointTemplate());
        String dimension = this.dimensionValue((Geometry)point);
        format = String.format(format, crsName, dimension, dimension, this.vertexList(point.getCoordinates()));
        return format;
    }

    protected String lineString2Gml(LineString line, String crsName) {
        String format = FormatUtil.getFormat(this.getLineStringTemplate());
        String dimension = this.dimensionValue((Geometry)line);
        Coordinate[] vertices = line.getCoordinates();
        format = String.format(format, crsName, dimension, dimension, this.vertexList(vertices));
        return format;
    }

    protected String polygon2Gml(Polygon polygon, String crsName) {
        String format = FormatUtil.getFormat(this.getPolygonTemplate());
        String dimension = this.dimensionValue((Geometry)polygon);
        LineString exteriorRing = polygon.getExteriorRing();
        String exteriorRingStr = this.linearRing2Gml(exteriorRing);
        StringBuilder intRingsStr = new StringBuilder();
        int n = polygon.getNumInteriorRing();
        int i = 0;
        while (i < n) {
            LineString linearRing = polygon.getInteriorRingN(i);
            intRingsStr.append("<gml:interior>");
            intRingsStr.append(this.linearRing2Gml(linearRing));
            intRingsStr.append("</gml:interior>");
            ++i;
        }
        format = String.format(format, crsName, dimension, exteriorRingStr, intRingsStr.toString());
        return format;
    }

    protected String multiPoint2Gml(MultiPoint multipoint, String crsName) {
        String format = FormatUtil.getFormat(this.getMultiPointTemplate());
        String dimension = this.dimensionValue((Geometry)multipoint);
        StringBuilder strBuild = new StringBuilder();
        int n = multipoint.getNumGeometries();
        int i = 0;
        while (i < n) {
            Point point = (Point)multipoint.getGeometryN(i);
            strBuild.append("<gml:pointMember>");
            strBuild.append(this.point2Gml(point, crsName));
            strBuild.append("</gml:pointMember>");
            ++i;
        }
        format = String.format(format, crsName, dimension, strBuild.toString());
        return format;
    }

    protected String multiLine2Gml(MultiLineString multilinestring, String crsName) {
        String format = FormatUtil.getFormat(this.getMultiLineStringTemplate());
        String dimension = this.dimensionValue((Geometry)multilinestring);
        StringBuilder strBuild = new StringBuilder();
        int n = multilinestring.getNumGeometries();
        int i = 0;
        while (i < n) {
            LineString line = (LineString)multilinestring.getGeometryN(i);
            strBuild.append("<gml:lineStringMember>");
            strBuild.append(this.lineString2Gml(line, crsName));
            strBuild.append("</gml:lineStringMember>");
            ++i;
        }
        format = String.format(format, crsName, dimension, strBuild.toString());
        return format;
    }

    protected String multiPolygon2Gml(MultiPolygon multipolygon, String crsName) {
        String format = FormatUtil.getFormat(this.getMultiPolygonTemplate());
        String dimension = this.dimensionValue((Geometry)multipolygon);
        StringBuilder strBuild = new StringBuilder();
        int n = multipolygon.getNumGeometries();
        int i = 0;
        while (i < n) {
            Polygon poly = (Polygon)multipolygon.getGeometryN(i);
            strBuild.append("<gml:polygonMember>");
            strBuild.append(this.polygon2Gml(poly, crsName));
            strBuild.append("</gml:polygonMember>");
            ++i;
        }
        format = String.format(format, crsName, dimension, strBuild.toString());
        return format;
    }

    protected String linearRing2Gml(LineString line) {
        String format = FormatUtil.getFormat(this.getLinearRingTemplate());
        Coordinate[] vertices = line.getCoordinates();
        format = String.format(format, this.dimensionValue((Geometry)line), this.vertexList(vertices));
        return format;
    }

    protected String vertexList(Coordinate[] vertices) {
        StringBuilder verticesStr = new StringBuilder();
        Coordinate[] coordinateArray = vertices;
        int n = vertices.length;
        int n2 = 0;
        while (n2 < n) {
            Coordinate point = coordinateArray[n2];
            verticesStr.append(String.valueOf(NUMBER_FORMAT.format(point.x)) + " " + NUMBER_FORMAT.format(point.y) + " ");
            if (!Double.isNaN(point.z)) {
                verticesStr.append(String.valueOf(NUMBER_FORMAT.format(point.z)) + " ");
            }
            ++n2;
        }
        String string = verticesStr.toString();
        return string;
    }

    protected String dimensionValue(Geometry geom) {
        String dimValue = "2";
        ZCoordinateCountFilter filter = new ZCoordinateCountFilter();
        geom.apply((CoordinateFilter)filter);
        if (filter.getCount() > 0) {
            dimValue = "3";
        }
        return dimValue;
    }
}

