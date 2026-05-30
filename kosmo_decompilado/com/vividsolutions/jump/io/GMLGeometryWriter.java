/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.IOException;
import java.io.Writer;

public class GMLGeometryWriter {
    private final int INDENT_SIZE = 2;
    private static final String coordinateSeparator = ",";
    private static final String tupleSeparator = " ";
    private String linePrefix = null;
    private int maxCoordinatesPerLine = 10;
    private String srsName = null;
    private String gid = null;

    private static String stringOfChar(char ch, int count) {
        StringBuffer buf = new StringBuffer();
        int i = 0;
        while (i < count) {
            buf.append(ch);
            ++i;
        }
        return buf.toString();
    }

    public void setLinePrefix(String linePrefix) {
        this.linePrefix = linePrefix;
    }

    public void setSRSName(String srsName) {
        this.srsName = srsName;
    }

    public void setGID(String gid) {
        this.gid = gid;
    }

    public void setMaximumCoordinatesPerLine(int maxCoordinatesPerLine) {
        if (maxCoordinatesPerLine <= 0) {
            maxCoordinatesPerLine = 1;
            return;
        }
        this.maxCoordinatesPerLine = maxCoordinatesPerLine;
    }

    public String write(Geometry geom) {
        StringBuffer buf = new StringBuffer();
        this.write(geom, buf);
        return buf.toString();
    }

    public void write(Geometry geometry, Writer writer) throws IOException {
        writer.write(this.write(geometry));
    }

    public void write(Geometry g, StringBuffer buf) {
        this.writeGeometry(g, this.attributeString(), 0, buf);
    }

    private void writeGeometry(Geometry g, String attributes, int level, StringBuffer buf) {
        if (g instanceof Point) {
            this.writePoint((Point)g, attributes, level, buf);
        } else if (g instanceof LinearRing) {
            this.writeLinearRing((LinearRing)g, attributes, level, buf);
        } else if (g instanceof LineString) {
            this.writeLineString((LineString)g, attributes, level, buf);
        } else if (g instanceof Polygon) {
            this.writePolygon((Polygon)g, attributes, level, buf);
        } else if (g instanceof MultiPoint) {
            this.writeMultiPoint((MultiPoint)g, attributes, level, buf);
        } else if (g instanceof MultiLineString) {
            this.writeMultiLineString((MultiLineString)g, attributes, level, buf);
        } else if (g instanceof MultiPolygon) {
            this.writeMultiPolygon((MultiPolygon)g, attributes, level, buf);
        } else if (g instanceof GeometryCollection) {
            this.writeGeometryCollection((GeometryCollection)g, attributes, level, buf);
        }
    }

    private void startLine(StringBuffer buf, int level, String text) {
        if (this.linePrefix != null) {
            buf.append(this.linePrefix);
        }
        buf.append(GMLGeometryWriter.stringOfChar(' ', 2 * level));
        buf.append(text);
    }

    private String geometryTag(String geometryName, String attributes) {
        StringBuffer buf = new StringBuffer();
        buf.append("<gml:");
        buf.append(geometryName);
        if (attributes != null && attributes.length() > 0) {
            buf.append(tupleSeparator);
            buf.append(attributes);
        }
        buf.append(">");
        return buf.toString();
    }

    private String attributeString() {
        StringBuffer buf = new StringBuffer();
        if (this.gid != null) {
            buf.append(" gid='");
            buf.append(this.gid);
            buf.append("'");
        }
        if (this.srsName != null) {
            buf.append(" srsName='");
            buf.append(this.srsName);
            buf.append("'");
        }
        return buf.toString();
    }

    private void writePoint(Point p, String attributes, int level, StringBuffer buf) {
        this.startLine(buf, level, String.valueOf(this.geometryTag("Point", attributes)) + "\n");
        this.write(new Coordinate[]{p.getCoordinate()}, level + 1, buf);
        this.startLine(buf, level, "</gml:Point>\n");
    }

    private void writeLineString(LineString ls, String attributes, int level, StringBuffer buf) {
        this.startLine(buf, level, String.valueOf(this.geometryTag("LineString", attributes)) + "\n");
        this.write(ls.getCoordinates(), level + 1, buf);
        this.startLine(buf, level, "</gml:LineString>\n");
    }

    private void writeLinearRing(LinearRing lr, String attributes, int level, StringBuffer buf) {
        this.startLine(buf, level, String.valueOf(this.geometryTag("LinearRing", attributes)) + "\n");
        this.write(lr.getCoordinates(), level + 1, buf);
        this.startLine(buf, level, "</gml:LinearRing>\n");
    }

    private void writePolygon(Polygon p, String attributes, int level, StringBuffer buf) {
        this.startLine(buf, level, String.valueOf(this.geometryTag("Polygon", attributes)) + "\n");
        this.startLine(buf, level, "  <gml:outerBoundaryIs>\n");
        this.writeLinearRing((LinearRing)p.getExteriorRing(), null, level + 1, buf);
        this.startLine(buf, level, "  </gml:outerBoundaryIs>\n");
        int t = 0;
        while (t < p.getNumInteriorRing()) {
            this.startLine(buf, level, "  <gml:innerBoundaryIs>\n");
            this.writeLinearRing((LinearRing)p.getInteriorRingN(t), null, level + 1, buf);
            this.startLine(buf, level, "  </gml:innerBoundaryIs>\n");
            ++t;
        }
        this.startLine(buf, level, "</gml:Polygon>\n");
    }

    private void writeMultiPoint(MultiPoint mp, String attributes, int level, StringBuffer buf) {
        this.startLine(buf, level, String.valueOf(this.geometryTag("MultiPoint", attributes)) + "\n");
        int t = 0;
        while (t < mp.getNumGeometries()) {
            this.startLine(buf, level, "  <gml:pointMember>\n");
            this.writePoint((Point)mp.getGeometryN(t), null, level + 1, buf);
            this.startLine(buf, level, "  </gml:pointMember>\n");
            ++t;
        }
        this.startLine(buf, level, "</gml:MultiPoint>\n");
    }

    private void writeMultiLineString(MultiLineString mls, String attributes, int level, StringBuffer buf) {
        this.startLine(buf, level, String.valueOf(this.geometryTag("MultiLineString", attributes)) + "\n");
        int t = 0;
        while (t < mls.getNumGeometries()) {
            this.startLine(buf, level, "  <gml:lineStringMember>\n");
            this.writeLineString((LineString)mls.getGeometryN(t), null, level + 1, buf);
            this.startLine(buf, level, "  </gml:lineStringMember>\n");
            ++t;
        }
        this.startLine(buf, level, "</gml:MultiLineString>\n");
    }

    private void writeMultiPolygon(MultiPolygon mp, String attributes, int level, StringBuffer buf) {
        this.startLine(buf, level, String.valueOf(this.geometryTag("MultiPolygon", attributes)) + "\n");
        int t = 0;
        while (t < mp.getNumGeometries()) {
            this.startLine(buf, level, "  <gml:polygonMember>\n");
            this.writePolygon((Polygon)mp.getGeometryN(t), null, level + 1, buf);
            this.startLine(buf, level, "  </gml:polygonMember>\n");
            ++t;
        }
        this.startLine(buf, level, "</gml:MultiPolygon>\n");
    }

    private void writeGeometryCollection(GeometryCollection gc, String attributes, int level, StringBuffer buf) {
        this.startLine(buf, level, String.valueOf(this.geometryTag("MultiGeometry", attributes)) + "\n");
        int t = 0;
        while (t < gc.getNumGeometries()) {
            this.startLine(buf, level, "  <gml:geometryMember>\n");
            this.writeGeometry(gc.getGeometryN(t), null, level + 1, buf);
            this.startLine(buf, level, "  </gml:geometryMember>\n");
            ++t;
        }
        this.startLine(buf, level, "</gml:MultiGeometry>\n");
    }

    private void write(Coordinate[] coords, int level, StringBuffer buf) {
        this.startLine(buf, level, "<gml:coordinates>");
        int dim = 2;
        if (coords.length > 0 && !Double.isNaN(coords[0].z)) {
            dim = 3;
        }
        boolean isNewLine = false;
        int i = 0;
        while (i < coords.length) {
            if (isNewLine) {
                this.startLine(buf, level, "  ");
                isNewLine = false;
            }
            if (dim == 2) {
                buf.append(coords[i].x);
                buf.append(coordinateSeparator);
                buf.append(coords[i].y);
            } else if (dim == 3) {
                buf.append(coords[i].x);
                buf.append(coordinateSeparator);
                buf.append(coords[i].y);
                buf.append(coordinateSeparator);
                buf.append(coords[i].z);
            }
            buf.append(tupleSeparator);
            if ((i + 1) % this.maxCoordinatesPerLine == 0 && i < coords.length - 1) {
                buf.append("\n");
                isNewLine = true;
            }
            ++i;
        }
        buf.append("</gml:coordinates>\n");
    }
}

