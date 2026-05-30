/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 */
package es.kosmo.core.dao.datasource.filedatasource.gml;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import es.kosmo.core.dao.datasource.filedatasource.gml.AbstractGmlGeometryConverter;
import es.kosmo.core.dao.datasource.filedatasource.gml.format.FormatUtil;

public class GMLGeometryConverter_2_1_2
extends AbstractGmlGeometryConverter {
    public GMLGeometryConverter_2_1_2(String crsName) {
        super(crsName);
    }

    @Override
    protected String getPointTemplate() {
        return "GmlPoint_2_1_2.template";
    }

    @Override
    protected String getLineStringTemplate() {
        return "GmlLineString_2_1_2.template";
    }

    @Override
    protected String getPolygonTemplate() {
        return "GmlPolygon_2_1_2.template";
    }

    @Override
    protected String getLinearRingTemplate() {
        return "GmlLinearRing_2_1_2.template";
    }

    @Override
    protected String getMultiPointTemplate() {
        return "GmlMultiPoint_2_1_2.template";
    }

    @Override
    protected String getMultiLineStringTemplate() {
        return "GmlMultiLineString_2_1_2.template";
    }

    @Override
    protected String getMultiPolygonTemplate() {
        return "GmlMultiPolygon_2_1_2.template";
    }

    @Override
    protected String point2Gml(Point point, String crsName) {
        String format = FormatUtil.getFormat(this.getPointTemplate());
        format = String.format(format, crsName, this.vertexList(point.getCoordinates()));
        return format;
    }

    @Override
    protected String lineString2Gml(LineString line, String crsName) {
        String format = FormatUtil.getFormat(this.getLineStringTemplate());
        Coordinate[] vertices = line.getCoordinates();
        format = String.format(format, crsName, this.vertexList(vertices));
        return format;
    }

    @Override
    protected String polygon2Gml(Polygon polygon, String crsName) {
        String format = FormatUtil.getFormat(this.getPolygonTemplate());
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
        format = String.format(format, crsName, exteriorRingStr, intRingsStr.toString());
        return format;
    }

    @Override
    protected String linearRing2Gml(LineString line) {
        String format = FormatUtil.getFormat(this.getLinearRingTemplate());
        Coordinate[] vertices = line.getCoordinates();
        format = String.format(format, this.vertexList(vertices));
        return format;
    }

    @Override
    protected String multiPoint2Gml(MultiPoint multipoint, String crsName) {
        String format = FormatUtil.getFormat(this.getMultiPointTemplate());
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
        format = String.format(format, crsName, strBuild.toString());
        return format;
    }

    @Override
    protected String multiLine2Gml(MultiLineString multilinestring, String crsName) {
        String format = FormatUtil.getFormat(this.getMultiLineStringTemplate());
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
        format = String.format(format, crsName, strBuild.toString());
        return format;
    }

    @Override
    protected String multiPolygon2Gml(MultiPolygon multipolygon, String crsName) {
        String format = FormatUtil.getFormat(this.getMultiPolygonTemplate());
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
        format = String.format(format, crsName, strBuild.toString());
        return format;
    }

    @Override
    protected String vertexList(Coordinate[] vertices) {
        StringBuilder verticesStr = new StringBuilder();
        Coordinate[] coordinateArray = vertices;
        int n = vertices.length;
        int n2 = 0;
        while (n2 < n) {
            Coordinate point = coordinateArray[n2];
            verticesStr.append(String.valueOf(NUMBER_FORMAT.format(point.x)) + "," + NUMBER_FORMAT.format(point.y));
            if (!Double.isNaN(point.z)) {
                verticesStr.append("," + NUMBER_FORMAT.format(point.z));
            }
            verticesStr.append(" ");
            ++n2;
        }
        String string = verticesStr.toString();
        return string;
    }
}

