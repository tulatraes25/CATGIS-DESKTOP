/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.CoordinateSequenceFactory
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  org.geotools.filter.AttributeExpression
 *  org.geotools.filter.Expression
 *  org.geotools.filter.FidFilter
 *  org.geotools.filter.FilterCapabilities
 *  org.geotools.filter.FilterVisitor
 *  org.geotools.filter.GeometryDistanceFilter
 *  org.geotools.filter.GeometryFilter
 *  org.geotools.filter.LikeFilter
 *  org.geotools.filter.LiteralExpression
 *  org.geotools.filter.SQLEncoder
 */
package org.geotools.filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.geotools.data.oracle.sdo.SDO;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.GeometryDistanceFilter;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.SQLEncoder;
import org.saig.jump.lang.I18N;

public class SQLEncoderOracle
extends SQLEncoder {
    private static FilterCapabilities capabilities = null;
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter.SQLEncoderOracle");
    private static final Map<Short, String> SDO_RELATE_MASK_MAP = new HashMap<Short, String>();
    private static final String SQL_WILD_MULTI = "%";
    private static final String SQL_WILD_SINGLE = "_";
    private static final String TOLERANCE = "0.001";
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
        LOGGER.fine(I18N.getMessage(((Object)((Object)this)).getClass(), "sqlencoderoracle-geometric-columns-is-{0}", new Object[]{this.currentGeomColumnName}));
        this.setSqlNameEscape("\"");
    }

    public SQLEncoderOracle(Map<String, Integer> srids) {
        this(null, srids);
    }

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
                left.accept((FilterVisitor)this);
            } else {
                this.out.write("\"" + this.currentGeomColumnName + "\"");
            }
            this.out.write(",");
            right.accept((FilterVisitor)this);
            this.out.write(",'mask=" + mask + " querytype=WINDOW') = 'TRUE' ");
            this.inGeomFilter = false;
        } else {
            LOGGER.warning(I18N.getString(((Object)((Object)this)).getClass(), "invalid-filter-cannot-have-a-geometry-filter-with-only-one-expression"));
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
            left.accept((FilterVisitor)this);
            this.out.write(",");
            right.accept((FilterVisitor)this);
            this.out.write(",'distance=" + distance + "') = '" + boolValue + "' ");
            this.inGeomFilter = false;
        } else {
            LOGGER.warning(I18N.getString(((Object)((Object)this)).getClass(), "invalid-filter-for-dwithin-cannot-have-a-geometry-filter-with-only-one-expression"));
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
        if (MultiLineString.class.isAssignableFrom(geometry.getClass())) {
            return SQLEncoderOracle.toSDOGeom((MultiLineString)geometry, srid);
        }
        if (MultiPolygon.class.isAssignableFrom(geometry.getClass())) {
            return SQLEncoderOracle.toSDOGeom((MultiPolygon)geometry, srid);
        }
        LOGGER.warning(I18N.getMessage(SQLEncoderOracle.class, "got-a-literal-geometry-that-i-can-not-handle-{0}", new Object[]{geometry.getClass().getName()}));
        return "";
    }

    private static String toSDOGeom(MultiLineString line, int srid) {
        if (line.getNumGeometries() == 1) {
            return SQLEncoderOracle.toSDOGeom(line.getGeometryN(0), srid);
        }
        throw new UnsupportedOperationException(I18N.getString(SQLEncoderOracle.class, "can-not-encode-multilinestring-yet"));
    }

    private static String toSDOGeom(MultiPolygon polygon, int srid) {
        if (polygon.getNumGeometries() == 1) {
            return SQLEncoderOracle.toSDOGeom(polygon.getGeometryN(0), srid);
        }
        throw new UnsupportedOperationException(I18N.getString(SQLEncoderOracle.class, "can-not-encode-multipolygon-yet"));
    }

    private static String toSDOGeom(LineString line, int srid) {
        if (SDO.D((Geometry)line) > 2) {
            LOGGER.warning(I18N.getMessage(SQLEncoderOracle.class, "a-{0}-dimensioned-geometry-provided-this-encoder-only-supports-two-d-geometries-the-query-will-be-constructed-as-a-two-d-query", new Object[]{SDO.D((Geometry)line)}));
        }
        StringBuffer buffer = new StringBuffer("MDSYS.SDO_GEOMETRY(");
        buffer.append(SDO.D((Geometry)line));
        buffer.append("002,");
        if (srid > 0) {
            LOGGER.fine("Using layer SRID: " + srid);
            buffer.append(srid);
        } else {
            LOGGER.fine("Using NULL SRID: ");
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

    private static String toSDOGeom(Point point, int srid) {
        if (SDO.D((Geometry)point) > 2) {
            LOGGER.warning(I18N.getMessage(SQLEncoderOracle.class, "a-{0}-dimensioned-geometry-provided-this-encoder-only-supports-two-d-geometries-the-query-will-be-constructed-as-a-two-d-query", new Object[]{SDO.D((Geometry)point)}));
        }
        StringBuffer buffer = new StringBuffer("MDSYS.SDO_GEOMETRY(");
        buffer.append(SDO.D((Geometry)point));
        buffer.append("001,");
        if (srid > 0) {
            LOGGER.fine(I18N.getMessage(SQLEncoderOracle.class, "using-layer-srid-{0}", new Object[]{srid}));
            buffer.append(srid);
        } else {
            LOGGER.fine(I18N.getString(SQLEncoderOracle.class, "using-null-srid"));
            buffer.append("NULL");
        }
        buffer.append(",MDSYS.SDO_POINT_TYPE(");
        buffer.append(point.getX());
        buffer.append(",");
        buffer.append(point.getY());
        buffer.append(",NULL),NULL,NULL)");
        return buffer.toString();
    }

    private static String toSDOGeom(Polygon polygon, int srid) {
        StringBuffer buffer = new StringBuffer();
        if (SDO.D((Geometry)polygon) > 2) {
            LOGGER.warning(I18N.getMessage(SQLEncoderOracle.class, "a-{0}-dimensioned-geometry-provided-this-encoder-only-supports-two-d-geometries-the-query-will-be-constructed-as-a-two-d-query", new Object[]{SDO.D((Geometry)polygon)}));
        }
        if (polygon.getExteriorRing() != null) {
            buffer.append("MDSYS.SDO_GEOMETRY(");
            buffer.append(SDO.D((Geometry)polygon));
            buffer.append("003,");
            if (srid > 0) {
                LOGGER.fine(I18N.getMessage(SQLEncoderOracle.class, "using-layer-srid-{0}", new Object[]{srid}));
                buffer.append(srid);
            } else {
                LOGGER.fine(I18N.getString(SQLEncoderOracle.class, "using-null-srid"));
                buffer.append("NULL");
            }
            buffer.append(",NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1),");
            buffer.append("MDSYS.SDO_ORDINATE_ARRAY(");
            CoordinateSequenceFactory fact = polygon.getFactory().getCoordinateSequenceFactory();
            CoordinateSequence exterior = polygon.getExteriorRing().getCoordinateSequence();
            CoordinateSequence coordSeq = SDO.counterClockWise(fact, exterior);
            int i = 0;
            int size = coordSeq.size();
            while (i < size) {
                Coordinate cur = coordSeq.getCoordinate(i);
                buffer.append(cur.x);
                buffer.append(",");
                buffer.append(cur.y);
                if (i != size - 1) {
                    buffer.append(",");
                }
                ++i;
            }
            buffer.append("))");
        } else {
            LOGGER.warning(I18N.getString(SQLEncoderOracle.class, "no-exterior-ring-on-polygon-this-encode-only-supports-polygons-with-exterior-rings"));
        }
        if (polygon.getNumInteriorRing() > 0) {
            LOGGER.warning(I18N.getString(SQLEncoderOracle.class, "polygon-contains-interior-rings-these-rings-will-not-be-included-in-the-query"));
        }
        return buffer.toString();
    }

    public void visit(GeometryFilter geomFilter) {
        LOGGER.finer(I18N.getString(((Object)((Object)this)).getClass(), "visiting-a-geometry-filter"));
        try {
            short filterType = geomFilter.getFilterType();
            if (filterType == 24 || filterType == 13) {
                this.doSdoDistance((GeometryDistanceFilter)geomFilter);
            } else if (SDO_RELATE_MASK_MAP.get(new Short(geomFilter.getFilterType())) != null) {
                this.doSdoRelate(geomFilter);
            } else {
                LOGGER.warning(I18N.getMessage(((Object)((Object)this)).getClass(), "unknown-filter-type-{0}", new Object[]{geomFilter.getFilterType()}));
            }
        }
        catch (IOException e) {
            LOGGER.warning(I18N.getString(((Object)((Object)this)).getClass(), "io-error-exporting-geometry-filter"));
        }
    }

    public void visit(LikeFilter filter) {
        try {
            String pattern = filter.getPattern();
            String multi = "\\Q" + filter.getWildcardMulti() + "\\E";
            pattern = pattern.replaceAll(multi, SQL_WILD_MULTI);
            String single = "\\Q" + filter.getWildcardSingle() + "\\E";
            pattern = pattern.replaceAll(single, SQL_WILD_SINGLE);
            this.out.write("UPPER(");
            filter.getValue().accept((FilterVisitor)this);
            this.out.write(") LIKE ");
            this.out.write("UPPER('" + pattern + "')");
            String esc = filter.getEscape();
            if (pattern.indexOf(esc) != -1) {
                this.out.write(" ESCAPE '" + esc + "'");
            }
        }
        catch (IOException ioe) {
            LOGGER.warning(I18N.getMessage(((Object)((Object)this)).getClass(), "unable-to-export-filter-{0}", new Object[]{ioe}));
        }
    }

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
                LOGGER.warning(I18N.getString(((Object)((Object)this)).getClass(), "io-error-exporting-literal-geometry"));
            }
        } else {
            super.visit(literal);
        }
    }

    public void visit(FidFilter filter) {
        if (this.fidColumn != null) {
            String[] fids = filter.getFids();
            LOGGER.finer(String.valueOf(I18N.getString(((Object)((Object)this)).getClass(), "exporting-fid")) + Arrays.asList(fids));
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
                    LOGGER.warning(I18N.getString(((Object)((Object)this)).getClass(), "io-error-exporting-fid-filter"));
                }
                ++i;
            }
        } else {
            super.visit(filter);
        }
    }

    public void visit(AttributeExpression ae) throws RuntimeException {
        super.visit(ae);
        if (this.inGeomFilter) {
            this.currentGeomColumnName = ae.getAttributePath();
        }
    }
}

