/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.CoordinateSequenceFactory
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory
 *  com.vividsolutions.jts.io.ParseException
 *  com.vividsolutions.jts.io.WKTReader
 *  org.apache.log4j.Logger
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jump.util.StringUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.geotools.data.oracle.sdo.SDO;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.FilterCapabilities;
import org.saig.core.filter.GeometryDistanceFilter;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.SQLEncoder;

public class SQLEncoderOracle
extends SQLEncoder {
    private static FilterCapabilities capabilities = null;
    private static final Logger LOGGER = Logger.getLogger(SQLEncoderOracle.class);
    private static final Map<Short, String> SDO_RELATE_MASK_MAP = new HashMap<Short, String>();
    private static final String SQL_WILD_MULTI = "%";
    private static final String SQL_WILD_SINGLE = "_";
    private static final String TOLERANCE = "0.001";
    private String escapedWildcardMulti = "\\.\\*";
    private String escapedWildcardSingle = "\\.\\?";
    private Map<String, Integer> srids;
    private String fidColumn;
    private String currentGeomColumnName = null;
    boolean inGeomFilter = false;

    static {
        SDO_RELATE_MASK_MAP.put(new Short(11), "contains");
        SDO_RELATE_MASK_MAP.put(new Short(9), "overlapbydisjoint");
        SDO_RELATE_MASK_MAP.put(new Short(5), "equal");
        SDO_RELATE_MASK_MAP.put(new Short(12), "overlapbyintersect");
        SDO_RELATE_MASK_MAP.put(new Short(8), "touch");
        SDO_RELATE_MASK_MAP.put(new Short(10), "inside");
        SDO_RELATE_MASK_MAP.put(new Short(6), "disjoint");
        SDO_RELATE_MASK_MAP.put(new Short(4), "anyinteract");
        SDO_RELATE_MASK_MAP.put(new Short(7), "anyinteract");
    }

    public SQLEncoderOracle(String fidColumn, int defaultSRID) {
        this(new HashMap<String, Integer>());
        this.fidColumn = fidColumn;
        this.srids.put(null, new Integer(defaultSRID));
        this.setSqlNameEscape("\"");
    }

    public SQLEncoderOracle(int defaultSRID) {
        this(null, new HashMap<String, Integer>());
        this.srids.put(null, new Integer(defaultSRID));
    }

    public SQLEncoderOracle(String fidColumn, Map<String, Integer> srids) {
        this.fidColumn = fidColumn;
        this.srids = srids;
        Set<String> geomCols = srids.keySet();
        if (geomCols.size() > 0) {
            this.currentGeomColumnName = geomCols.iterator().next();
        }
        LOGGER.debug((Object)("SQLEncoderOracle: Geometric Column is: " + this.currentGeomColumnName));
        this.setSqlNameEscape("\"");
    }

    public SQLEncoderOracle(Map<String, Integer> srids) {
        this(null, srids);
    }

    @Override
    protected FilterCapabilities createFilterCapabilities() {
        FilterCapabilities capabilities = super.createFilterCapabilities();
        capabilities.addType((short)4);
        capabilities.addType((short)11);
        capabilities.addType((short)9);
        capabilities.addType((short)6);
        capabilities.addType((short)5);
        capabilities.addType((short)7);
        capabilities.addType((short)12);
        capabilities.addType((short)8);
        capabilities.addType((short)10);
        capabilities.addType((short)24);
        capabilities.addType((short)13);
        capabilities.addType((short)22);
        capabilities.addType((short)20);
        return capabilities;
    }

    private void doSdoRelate(GeometryFilter geomFilter) throws IOException {
        String mask = SDO_RELATE_MASK_MAP.get(new Short(geomFilter.getFilterType()));
        Expression left = geomFilter.getLeftGeometry();
        Expression right = geomFilter.getRightGeometry();
        if ((left != null || this.currentGeomColumnName != null) && right != null && mask != null) {
            this.inGeomFilter = true;
            this.out.write("SDO_RELATE(");
            if (left != null) {
                left.accept(this);
            } else {
                this.out.write("\"" + this.currentGeomColumnName + "\"");
            }
            this.out.write(",");
            right.accept(this);
            this.out.write(",'mask=" + mask + " querytype=WINDOW') = 'TRUE' ");
            this.inGeomFilter = false;
        } else {
            LOGGER.warn((Object)"Invalid filter. Cannot have a Geometry filter with only one expression.");
        }
    }

    private void doSdoDistance(GeometryDistanceFilter geomFilter) throws IOException {
        String boolValue;
        Expression left = geomFilter.getLeftGeometry();
        Expression right = geomFilter.getRightGeometry();
        double distance = geomFilter.getDistance();
        boolean isDWithin = geomFilter.getFilterType() == 24;
        String string = boolValue = isDWithin ? "TRUE" : "FALSE";
        if (left != null && right != null) {
            this.inGeomFilter = true;
            this.out.write("SDO_WITHIN_DISTANCE(");
            left.accept(this);
            this.out.write(",");
            right.accept(this);
            this.out.write(",'distance=" + distance + "') = '" + boolValue + "' ");
            this.inGeomFilter = false;
        } else {
            LOGGER.warn((Object)"Invalid filter for DWithin. Cannot have a Geometry filter with only one expression.");
        }
    }

    public static String toSDOGeom(Geometry geometry, int srid) {
        if (Point.class.isAssignableFrom(geometry.getClass())) {
            return SQLEncoderOracle.toSDOGeom((Point)geometry, srid);
        }
        if (LineString.class.isAssignableFrom(geometry.getClass())) {
            return SQLEncoderOracle.toSDOGeom((LineString)geometry, srid);
        }
        if (Polygon.class.isAssignableFrom(geometry.getClass())) {
            return SQLEncoderOracle.toSDOGeom((Polygon)geometry, srid);
        }
        if (MultiPoint.class.isAssignableFrom(geometry.getClass())) {
            return SQLEncoderOracle.toSDOGeom((MultiPoint)geometry, srid);
        }
        if (MultiLineString.class.isAssignableFrom(geometry.getClass())) {
            return SQLEncoderOracle.toSDOGeom((MultiLineString)geometry, srid);
        }
        if (MultiPolygon.class.isAssignableFrom(geometry.getClass())) {
            return SQLEncoderOracle.toSDOGeom((MultiPolygon)geometry, srid);
        }
        LOGGER.warn((Object)("Got a literal geometry that I can't handle: " + geometry.getClass().getName()));
        return "";
    }

    private static String toSDOGeom(Point point, int srid) {
        if (SDO.D((Geometry)point) > 2) {
            LOGGER.warn((Object)(SDO.D((Geometry)point) + " dimensioned geometry provided." + " This encoder only supports 2D geometries. The query will be constructed as" + " a 2D query."));
        }
        StringBuffer buffer = new StringBuffer("MDSYS.SDO_GEOMETRY(");
        buffer.append(SDO.D((Geometry)point));
        buffer.append("001,");
        if (srid > 0) {
            LOGGER.debug((Object)("Using layer SRID: " + srid));
            buffer.append(srid);
        } else {
            LOGGER.debug((Object)"Using NULL SRID: ");
            buffer.append("NULL");
        }
        buffer.append(",MDSYS.SDO_POINT_TYPE(");
        buffer.append(point.getX());
        buffer.append(",");
        buffer.append(point.getY());
        buffer.append(",NULL),NULL,NULL)");
        return buffer.toString();
    }

    private static String toSDOGeom(LineString line, int srid) {
        if (SDO.D((Geometry)line) > 2) {
            LOGGER.warn((Object)(SDO.D((Geometry)line) + " dimensioned geometry provided." + " This encoder only supports 2D geometries. The query will be constructed as" + " a 2D query."));
        }
        StringBuffer buffer = new StringBuffer("MDSYS.SDO_GEOMETRY(");
        buffer.append(SDO.D((Geometry)line));
        buffer.append("002,");
        if (srid > 0) {
            LOGGER.debug((Object)("Using layer SRID: " + srid));
            buffer.append(srid);
        } else {
            LOGGER.debug((Object)"Using NULL SRID: ");
            buffer.append("NULL");
        }
        buffer.append(",NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),");
        buffer.append("MDSYS.SDO_ORDINATE_ARRAY(");
        Coordinate[] coordinates = line.getCoordinates();
        int i = 0;
        while (i < coordinates.length) {
            buffer.append(coordinates[i].x);
            buffer.append(",");
            buffer.append(coordinates[i].y);
            if (i != coordinates.length - 1) {
                buffer.append(",");
            }
            ++i;
        }
        buffer.append("))");
        return buffer.toString();
    }

    private static String toSDOGeom(Polygon polygon, int srid) {
        StringBuffer mainBuffer = new StringBuffer();
        StringBuffer coordinateBuffer = new StringBuffer();
        int coordinateCont = 1;
        if (SDO.D((Geometry)polygon) > 2) {
            LOGGER.warn((Object)(SDO.D((Geometry)polygon) + " dimensioned geometry provided." + " This encoder only supports 2D geometries. The query will be constructed as" + " a 2D query."));
        }
        if (polygon.getExteriorRing() != null) {
            mainBuffer.append("MDSYS.SDO_GEOMETRY(");
            mainBuffer.append(SDO.D((Geometry)polygon));
            mainBuffer.append("003,");
            if (srid > 0) {
                LOGGER.debug((Object)("Using layer SRID: " + srid));
                mainBuffer.append(srid);
            } else {
                LOGGER.debug((Object)"Using NULL SRID: ");
                mainBuffer.append("NULL");
            }
            mainBuffer.append(",NULL,");
            mainBuffer.append("MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1");
            coordinateBuffer.append("MDSYS.SDO_ORDINATE_ARRAY(");
            CoordinateSequence exterior = SDO.counterClockWise((CoordinateSequenceFactory)CoordinateArraySequenceFactory.instance(), polygon.getExteriorRing().getCoordinateSequence());
            int i = 0;
            int size = exterior.size();
            while (i < size) {
                Coordinate cur = exterior.getCoordinate(i);
                coordinateBuffer.append(cur.x);
                coordinateBuffer.append(",");
                coordinateBuffer.append(cur.y);
                coordinateCont += 2;
                if (i != size - 1) {
                    coordinateBuffer.append(",");
                }
                ++i;
            }
            if (polygon.getNumInteriorRing() > 0) {
                i = 0;
                while (i < polygon.getNumInteriorRing()) {
                    CoordinateSequence interiorNth = SDO.clockWise((CoordinateSequenceFactory)CoordinateArraySequenceFactory.instance(), polygon.getInteriorRingN(i).getCoordinateSequence());
                    mainBuffer.append(", " + coordinateCont + ",2003,1");
                    coordinateBuffer.append(",");
                    int j = 0;
                    int size2 = interiorNth.size();
                    while (j < size2) {
                        Coordinate cur = interiorNth.getCoordinate(j);
                        coordinateBuffer.append(cur.x);
                        coordinateBuffer.append(",");
                        coordinateBuffer.append(cur.y);
                        coordinateCont += 2;
                        if (j != size2 - 1) {
                            coordinateBuffer.append(",");
                        }
                        ++j;
                    }
                    ++i;
                }
            }
            mainBuffer.append("),");
            mainBuffer.append(coordinateBuffer);
            mainBuffer.append("))");
        } else {
            LOGGER.warn((Object)"No Exterior ring on polygon.  This encode only supports Polygons with exterior rings.");
        }
        return mainBuffer.toString();
    }

    private static String toSDOGeom(MultiPoint multiPoint, int srid) {
        if (SDO.D((Geometry)multiPoint) > 2) {
            LOGGER.warn((Object)(SDO.D((Geometry)multiPoint) + " dimensioned geometry provided." + " This encoder only supports 2D geometries. The query will be constructed as" + " a 2D query."));
        }
        StringBuffer mainBuffer = new StringBuffer("MDSYS.SDO_GEOMETRY(");
        mainBuffer.append(SDO.D((Geometry)multiPoint));
        mainBuffer.append("005,");
        if (srid > 0) {
            LOGGER.debug((Object)("Using layer SRID: " + srid));
            mainBuffer.append(srid);
        } else {
            LOGGER.debug((Object)"Using NULL SRID: ");
            mainBuffer.append("NULL");
        }
        mainBuffer.append(",NULL,MDSYS.SDO_ELEM_INFO_ARRAY(");
        StringBuffer coordinateBuffer = new StringBuffer();
        coordinateBuffer.append("MDSYS.SDO_ORDINATE_ARRAY(");
        int coordinateCont = 1;
        int i = 0;
        int size = multiPoint.getNumGeometries();
        while (i < size) {
            Point currentPoint = (Point)multiPoint.getGeometryN(i);
            mainBuffer.append(" " + coordinateCont + ",1,1");
            coordinateBuffer.append(currentPoint.getX());
            coordinateBuffer.append(",");
            coordinateBuffer.append(currentPoint.getY());
            coordinateCont += 2;
            if (i != size - 1) {
                mainBuffer.append(", ");
                coordinateBuffer.append(", ");
            }
            ++i;
        }
        mainBuffer.append("),");
        mainBuffer.append(coordinateBuffer);
        mainBuffer.append("))");
        return mainBuffer.toString();
    }

    private static String toSDOGeom(MultiLineString multiLineString, int srid) {
        if (SDO.D((Geometry)multiLineString) > 2) {
            LOGGER.warn((Object)(SDO.D((Geometry)multiLineString) + " dimensioned geometry provided." + " This encoder only supports 2D geometries. The query will be constructed as" + " a 2D query."));
        }
        StringBuffer mainBuffer = new StringBuffer("MDSYS.SDO_GEOMETRY(");
        mainBuffer.append(SDO.D((Geometry)multiLineString));
        mainBuffer.append("006,");
        if (srid > 0) {
            LOGGER.debug((Object)("Using layer SRID: " + srid));
            mainBuffer.append(srid);
        } else {
            LOGGER.debug((Object)"Using NULL SRID: ");
            mainBuffer.append("NULL");
        }
        mainBuffer.append(",NULL,MDSYS.SDO_ELEM_INFO_ARRAY(");
        StringBuffer coordinateBuffer = new StringBuffer();
        coordinateBuffer.append("MDSYS.SDO_ORDINATE_ARRAY(");
        int coordinateCont = 1;
        int i = 0;
        int size = multiLineString.getNumGeometries();
        while (i < size) {
            LineString currentLS = (LineString)multiLineString.getGeometryN(i);
            mainBuffer.append(" " + coordinateCont + ",2,1");
            Coordinate[] coords = currentLS.getCoordinates();
            int j = 0;
            int coordsSize = coords.length;
            while (j < coordsSize) {
                coordinateBuffer.append(coords[j].x);
                coordinateBuffer.append(",");
                coordinateBuffer.append(coords[j].y);
                coordinateCont += 2;
                if (j != coordsSize - 1) {
                    mainBuffer.append(", ");
                    coordinateBuffer.append(",");
                }
                ++j;
            }
            ++i;
        }
        mainBuffer.append("),");
        mainBuffer.append(coordinateBuffer);
        mainBuffer.append("))");
        return mainBuffer.toString();
    }

    private static String toSDOGeom(MultiPolygon multiPolygon, int srid) {
        StringBuffer mainBuffer = new StringBuffer();
        StringBuffer coordinateBuffer = new StringBuffer();
        int coordinateCont = 1;
        if (SDO.D((Geometry)multiPolygon) > 2) {
            LOGGER.warn((Object)(SDO.D((Geometry)multiPolygon) + " dimensioned geometry provided." + " This encoder only supports 2D geometries. The query will be constructed as" + " a 2D query."));
        }
        mainBuffer.append("MDSYS.SDO_GEOMETRY(");
        mainBuffer.append(SDO.D((Geometry)multiPolygon));
        mainBuffer.append("007,");
        if (srid > 0) {
            LOGGER.debug((Object)("Using layer SRID: " + srid));
            mainBuffer.append(srid);
        } else {
            LOGGER.debug((Object)"Using NULL SRID: ");
            mainBuffer.append("NULL");
        }
        mainBuffer.append(",NULL,");
        mainBuffer.append("MDSYS.SDO_ELEM_INFO_ARRAY(");
        coordinateBuffer.append("MDSYS.SDO_ORDINATE_ARRAY(");
        int i = 0;
        int size = multiPolygon.getNumGeometries();
        while (i < size) {
            Polygon currentPolygon = (Polygon)multiPolygon.getGeometryN(i);
            if (currentPolygon.getExteriorRing() != null) {
                mainBuffer.append(String.valueOf(coordinateCont) + ",1003,1");
                CoordinateSequence exterior = SDO.counterClockWise((CoordinateSequenceFactory)CoordinateArraySequenceFactory.instance(), currentPolygon.getExteriorRing().getCoordinateSequence());
                int j = 0;
                int polSize = exterior.size();
                while (j < polSize) {
                    Coordinate cur = exterior.getCoordinate(j);
                    coordinateBuffer.append(cur.x);
                    coordinateBuffer.append(",");
                    coordinateBuffer.append(cur.y);
                    coordinateCont += 2;
                    if (j != polSize - 1) {
                        coordinateBuffer.append(",");
                    }
                    ++j;
                }
                if (currentPolygon.getNumInteriorRing() > 0) {
                    j = 0;
                    while (j < currentPolygon.getNumInteriorRing()) {
                        CoordinateSequence interiorNth = SDO.clockWise((CoordinateSequenceFactory)CoordinateArraySequenceFactory.instance(), currentPolygon.getInteriorRingN(j).getCoordinateSequence());
                        mainBuffer.append(", " + coordinateCont + ",2003,1");
                        coordinateBuffer.append(",");
                        int k = 0;
                        int intSize = interiorNth.size();
                        while (k < intSize) {
                            Coordinate cur = interiorNth.getCoordinate(k);
                            coordinateBuffer.append(cur.x);
                            coordinateBuffer.append(",");
                            coordinateBuffer.append(cur.y);
                            coordinateCont += 2;
                            if (k != intSize - 1) {
                                coordinateBuffer.append(",");
                            }
                            ++k;
                        }
                        ++j;
                    }
                }
                if (i != size - 1) {
                    mainBuffer.append(", ");
                    coordinateBuffer.append(",");
                }
            } else {
                LOGGER.warn((Object)"No Exterior ring on polygon.  This encode only supports Polygons with exterior rings.");
            }
            ++i;
        }
        mainBuffer.append("),");
        mainBuffer.append(coordinateBuffer);
        mainBuffer.append("))");
        return mainBuffer.toString();
    }

    @Override
    public void visit(GeometryFilter geomFilter) {
        LOGGER.debug((Object)"Visiting a Geometry filter");
        try {
            short filterType = geomFilter.getFilterType();
            if (filterType == 24 || filterType == 13) {
                this.doSdoDistance((GeometryDistanceFilter)geomFilter);
            } else if (SDO_RELATE_MASK_MAP.get(new Short(geomFilter.getFilterType())) != null) {
                this.doSdoRelate(geomFilter);
            } else {
                LOGGER.warn((Object)("Unknown filter type: " + geomFilter.getFilterType()));
            }
        }
        catch (IOException e) {
            LOGGER.warn((Object)"IO Error exporting geometry filter");
        }
    }

    @Override
    public void visit(LikeFilter filter) {
        try {
            String pattern = filter.getPattern();
            pattern = pattern.replaceAll(this.escapedWildcardMulti, SQL_WILD_MULTI);
            pattern = pattern.replaceAll(this.escapedWildcardSingle, SQL_WILD_SINGLE);
            filter.getValue().accept(this);
            this.out.write(" LIKE ");
            pattern = StringUtil.replaceAll(pattern, "*", SQL_WILD_MULTI);
            pattern = StringUtil.replaceAll(pattern, "?", SQL_WILD_SINGLE);
            this.out.write("'" + pattern + "'");
            String esc = filter.getEscape();
            if (pattern.indexOf(esc) != -1) {
                this.out.write(" ESCAPE '" + esc + "'");
            }
        }
        catch (IOException ioe) {
            LOGGER.warn((Object)("Unable to export filter" + ioe));
        }
    }

    @Override
    public void visit(LiteralExpression literal) {
        if (literal.getType() == 104) {
            Geometry geometry = (Geometry)literal.getLiteral();
            try {
                int srid = -1;
                Integer sridO = this.srids.get(this.currentGeomColumnName);
                if (sridO == null) {
                    sridO = this.srids.get(null);
                }
                if (sridO != null) {
                    srid = sridO;
                }
                this.out.write(SQLEncoderOracle.toSDOGeom(geometry, srid));
            }
            catch (IOException e) {
                LOGGER.warn((Object)"IO Error exporting Literal Geometry");
            }
        } else {
            super.visit(literal);
        }
    }

    @Override
    public void visit(FidFilter filter) {
        if (this.fidColumn != null) {
            String[] fids = filter.getFids();
            LOGGER.debug((Object)("Exporting FID=" + Arrays.asList(fids)));
            int i = 0;
            while (i < fids.length) {
                try {
                    this.out.write(this.fidColumn);
                    this.out.write(" = '");
                    int pos = fids[i].indexOf(46);
                    if (pos != -1) {
                        this.out.write(fids[i].substring(pos + 1));
                    } else {
                        this.out.write(fids[i]);
                    }
                    this.out.write("'");
                    if (i < fids.length - 1) {
                        this.out.write(" OR ");
                    }
                }
                catch (IOException e) {
                    LOGGER.warn((Object)"IO Error exporting FID Filter.", (Throwable)e);
                }
                ++i;
            }
        } else {
            super.visit(filter);
        }
    }

    @Override
    public void visit(AttributeExpression ae) throws RuntimeException {
        super.visit(ae);
        if (this.inGeomFilter) {
            this.currentGeomColumnName = ae.getAttributePath();
        }
    }

    public static void main(String[] args) {
        GeometryFactory geomFact = new GeometryFactory();
        LOGGER.info((Object)"**********************************");
        LOGGER.info((Object)"Test n.1: Single 2D and 3D points");
        LOGGER.info((Object)"1) Single 2D point");
        Point point2D = geomFact.createPoint(new Coordinate(0.0434, 5.808));
        LOGGER.info((Object)("INPUT  -> " + point2D.toText()));
        LOGGER.info((Object)("OUTPUT -> " + SQLEncoderOracle.toSDOGeom(point2D, 23030)));
        LOGGER.info((Object)"2) Single 3D point");
        Point point3D = geomFact.createPoint(new Coordinate(0.0434, 5.808, 99999.9));
        LOGGER.info((Object)("INPUT  -> " + point3D.toText()));
        LOGGER.info((Object)("OUTPUT -> " + SQLEncoderOracle.toSDOGeom(point3D, 23030)));
        LOGGER.info((Object)"**********************************");
        LOGGER.info((Object)"**********************************");
        LOGGER.info((Object)"Test n.2: Single 2D and 3D linestrings");
        LOGGER.info((Object)"1) Single 2D linestring");
        Coordinate[] coords = new Coordinate[]{new Coordinate(0.49838, 3242.233), new Coordinate(3.49838, 11.0), new Coordinate(42322.49838, 33112.99), new Coordinate(243242.49838, 8888.1)};
        LineString lineString2D = geomFact.createLineString(coords);
        LOGGER.info((Object)("INPUT  -> " + lineString2D.toText()));
        LOGGER.info((Object)("OUTPUT -> " + SQLEncoderOracle.toSDOGeom(lineString2D, 23030)));
        LOGGER.info((Object)"2) Single 3D linestring");
        Coordinate[] coordsls3D = new Coordinate[]{new Coordinate(0.49838, 3242.233, 1.0), new Coordinate(3.49838, 11.0, 2.0), new Coordinate(42322.49838, 33112.99, 3.0), new Coordinate(243242.49838, 8888.1, 4.0)};
        LineString lineString3D = geomFact.createLineString(coordsls3D);
        LOGGER.info((Object)("INPUT  -> " + lineString3D.toText()));
        LOGGER.info((Object)("OUTPUT -> " + SQLEncoderOracle.toSDOGeom(lineString3D, 23030)));
        LOGGER.info((Object)"**********************************");
        LOGGER.info((Object)"**********************************");
        LOGGER.info((Object)"Test n.3: Single 2D and 3D linestrings");
        LOGGER.info((Object)"1) Single 2D polygon");
        LOGGER.info((Object)"2) Single 3D polygon");
        LOGGER.info((Object)"**********************************");
        LOGGER.info((Object)"**********************************");
        LOGGER.info((Object)"Test n.4: MultiPoints, 2D and 3D");
        LOGGER.info((Object)"1) 2D MultiPoint");
        LOGGER.info((Object)"2) 3D MultiPoint");
        LOGGER.info((Object)"**********************************");
        LOGGER.info((Object)"**********************************");
        LOGGER.info((Object)"Test n.5: MultiLineStrings, 2D and 3D");
        LOGGER.info((Object)"1) 2D MultiLineString");
        LOGGER.info((Object)"2) 3D MultiLineString");
        LOGGER.info((Object)"**********************************");
        LOGGER.info((Object)"**********************************");
        LOGGER.info((Object)"Test n.6: MultiPolygons, 2D and 3D");
        LOGGER.info((Object)"1) Single 2D MultiPolygon");
        WKTReader reader = new WKTReader(geomFact);
        String multipolygon2DWKT = "MULTIPOLYGON(((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2)),((6 3,9 2,9 4,6 3)))";
        try {
            Geometry multipolygon2D = reader.read(multipolygon2DWKT);
            LOGGER.info((Object)("INPUT  -> " + multipolygon2D.toText()));
            LOGGER.info((Object)("OUTPUT -> " + SQLEncoderOracle.toSDOGeom(multipolygon2D, 23030)));
        }
        catch (ParseException e) {
            LOGGER.warn((Object)"TEST FAILED");
            LOGGER.error((Object)"", (Throwable)e);
        }
        LOGGER.info((Object)"2) Single 3D MultiPolygon");
        LOGGER.info((Object)"**********************************");
        LOGGER.info((Object)"Tests ended");
    }
}

