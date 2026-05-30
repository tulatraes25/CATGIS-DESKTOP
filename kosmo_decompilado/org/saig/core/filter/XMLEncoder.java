/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import org.saig.core.filter.AbstractFilter;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LogicFilter;
import org.saig.core.filter.MathExpression;
import org.saig.core.filter.NullFilter;

public class XMLEncoder
implements FilterVisitor {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");
    private static Map<Integer, String> comparisions = new HashMap<Integer, String>();
    private static Map<Integer, String> spatial = new HashMap<Integer, String>();
    private static Map<Integer, String> logical = new HashMap<Integer, String>();
    private static Map<Integer, String> expressions = new HashMap<Integer, String>();
    private Writer out;

    static {
        comparisions.put(new Integer(14), "PropertyIsEqualTo");
        comparisions.put(new Integer(16), "PropertyIsGreaterThan");
        comparisions.put(new Integer(18), "PropertyIsGreaterThanOrEqualTo");
        comparisions.put(new Integer(15), "PropertyIsLessThan");
        comparisions.put(new Integer(17), "PropertyIsLessThanOrEqualTo");
        comparisions.put(new Integer(20), "PropertyIsLike");
        comparisions.put(new Integer(21), "PropertyIsNull");
        comparisions.put(new Integer(19), "PropertyIsBetween");
        expressions.put(new Integer(105), "Add");
        expressions.put(new Integer(108), "Div");
        expressions.put(new Integer(107), "Mul");
        expressions.put(new Integer(106), "Sub");
        expressions.put(new Integer(114), "Function");
        spatial.put(new Integer(5), "Equals");
        spatial.put(new Integer(6), "Disjoint");
        spatial.put(new Integer(7), "Intersects");
        spatial.put(new Integer(8), "Touches");
        spatial.put(new Integer(9), "Crosses");
        spatial.put(new Integer(10), "Within");
        spatial.put(new Integer(11), "Contains");
        spatial.put(new Integer(12), "Overlaps");
        spatial.put(new Integer(13), "Beyond");
        spatial.put(new Integer(4), "BBOX");
        logical.put(new Integer(2), "And");
        logical.put(new Integer(1), "Or");
        logical.put(new Integer(3), "Not");
    }

    public XMLEncoder(Writer out) {
        this.out = out;
    }

    public XMLEncoder(Writer out, Filter filter) {
        this.out = out;
        try {
            this.encode(filter);
        }
        catch (IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    public void encode(Filter filter) throws IOException {
        this.out.write("<Filter>\n");
        filter.accept(this);
        this.out.write("</Filter>\n");
    }

    public void encode(Expression expression) {
        expression.accept(this);
    }

    @Override
    public void visit(Filter filter) {
        LOGGER.warning("exporting unknown filter type:" + filter.toString());
    }

    @Override
    public void visit(BetweenFilter filter) {
        LOGGER.finer("exporting BetweenFilter");
        Expression left = filter.getLeftValue();
        Expression right = filter.getRightValue();
        Expression mid = filter.getMiddleValue();
        LOGGER.finer("Filter type id is " + filter.getFilterType());
        LOGGER.finer("Filter type text is " + comparisions.get(new Integer(filter.getFilterType())));
        String type = comparisions.get(new Integer(filter.getFilterType()));
        try {
            this.out.write("<" + type + ">\n");
            mid.accept(this);
            this.out.write("<LowerBoundary>\n");
            left.accept(this);
            this.out.write("</LowerBoundary>\n<UpperBoundary>\n");
            right.accept(this);
            this.out.write("</UpperBoundary>\n");
            this.out.write("</" + type + ">\n");
        }
        catch (IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    @Override
    public void visit(LikeFilter filter) {
        LOGGER.finer("exporting like filter");
        try {
            String wcm = filter.getWildcardMulti();
            String wcs = filter.getWildcardSingle();
            String esc = filter.getEscape();
            this.out.write("<PropertyIsLike wildCard=\"" + wcm + "\" singleChar=\"" + wcs + "\" escape=\"" + esc + "\">\n");
            filter.getValue().accept(this);
            this.out.write("<Literal>" + filter.getPattern() + "</Literal>\n");
            this.out.write("</PropertyIsLike>\n");
        }
        catch (IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    @Override
    public void visit(LogicFilter filter) {
        LOGGER.finer("exporting LogicFilter");
        filter.getFilterType();
        String type = logical.get(new Integer(filter.getFilterType()));
        try {
            this.out.write("<" + type + ">\n");
            Iterator<Filter> list = filter.getFilterIterator();
            while (list.hasNext()) {
                ((AbstractFilter)list.next()).accept(this);
            }
            this.out.write("</" + type + ">\n");
        }
        catch (IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    @Override
    public void visit(CompareFilter filter) {
        LOGGER.finer("exporting ComparisonFilter");
        Expression left = filter.getLeftValue();
        Expression right = filter.getRightValue();
        LOGGER.finer("Filter type id is " + filter.getFilterType());
        LOGGER.finer("Filter type text is " + comparisions.get(new Integer(filter.getFilterType())));
        String type = comparisions.get(new Integer(filter.getFilterType()));
        try {
            this.out.write("<" + type + ">\n");
            LOGGER.fine("exporting left expression " + left);
            left.accept(this);
            LOGGER.fine("exporting right expression " + right);
            right.accept(this);
            this.out.write("</" + type + ">\n");
        }
        catch (IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    @Override
    public void visit(GeometryFilter filter) {
        LOGGER.finer("exporting GeometryFilter");
        Expression left = filter.getLeftGeometry();
        Expression right = filter.getRightGeometry();
        String type = spatial.get(new Integer(filter.getFilterType()));
        try {
            this.out.write("<" + type + ">\n");
            left.accept(this);
            right.accept(this);
            this.out.write("</" + type + ">\n");
        }
        catch (IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    @Override
    public void visit(NullFilter filter) {
        LOGGER.finer("exporting NullFilter");
        Expression expr = filter.getNullCheckValue();
        String type = comparisions.get(new Integer(filter.getFilterType()));
        try {
            this.out.write("<" + type + ">\n");
            expr.accept(this);
            this.out.write("</" + type + ">\n");
        }
        catch (IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    @Override
    public void visit(FidFilter filter) {
        LOGGER.finer("exporting FidFilter");
        String[] fids = filter.getFids();
        int i = 0;
        while (i < fids.length) {
            try {
                this.out.write("<FeatureId fid=\"" + fids[i] + "\"/>");
            }
            catch (IOException ioe) {
                LOGGER.warning("Unable to export filter" + ioe);
            }
            ++i;
        }
    }

    @Override
    public void visit(AttributeExpression expression) {
        LOGGER.finer("exporting ExpressionAttribute");
        try {
            this.out.write("<PropertyName>" + expression.getAttributePath() + "</PropertyName>\n");
        }
        catch (IOException ioe) {
            LOGGER.finer("Unable to export expresion: " + ioe);
        }
    }

    @Override
    public void visit(Expression expression) {
        LOGGER.warning("exporting unknown (default) expression");
    }

    @Override
    public void visit(LiteralExpression expression) {
        LOGGER.finer("exporting LiteralExpression");
        try {
            Object value = expression.getLiteral();
            if (Geometry.class.isAssignableFrom(value.getClass())) {
                GeometryEncoder encoder = new GeometryEncoder(this.out);
                encoder.encode((Geometry)value);
            } else {
                this.out.write("<Literal>" + value + "</Literal>\n");
            }
        }
        catch (IOException ioe) {
            LOGGER.warning("Unable to export expresion" + ioe);
        }
    }

    @Override
    public void visit(MathExpression expression) {
        LOGGER.finer("exporting Expression Math");
        String type = expressions.get(new Integer(expression.getType()));
        try {
            this.out.write("<" + type + ">\n");
            expression.getLeftValue().accept(this);
            expression.getRightValue().accept(this);
            this.out.write("</" + type + ">\n");
        }
        catch (IOException ioe) {
            LOGGER.warning("Unable to export expresion: " + ioe);
        }
    }

    @Override
    public void visit(FunctionExpression expression) {
        LOGGER.finer("exporting Expression Math");
        String type = expressions.get(new Integer(expression.getType()));
        try {
            this.out.write("<" + type + " name = " + expression.getName() + ">\n");
            Expression[] args = expression.getArgs();
            int i = 0;
            while (i < args.length) {
                args[i].accept(this);
                ++i;
            }
            this.out.write("</" + type + ">\n");
        }
        catch (IOException ioe) {
            LOGGER.warning("Unable to export expresion: " + ioe);
        }
    }

    static class GeometryEncoder {
        private String srs = "epsg:4326";
        private PrintWriter out;

        public GeometryEncoder(Writer out) {
            this.out = new PrintWriter(out);
        }

        public GeometryEncoder(String srs, Writer out) {
            this.out = new PrintWriter(out);
            this.srs = srs;
        }

        public void encode(Geometry geom) {
            Class<?> geomType = geom.getClass();
            if (Point.class.isAssignableFrom(geomType)) {
                this.encode((Point)geom);
            } else if (LineString.class.isAssignableFrom(geomType)) {
                this.encode((LineString)geom);
            } else if (Polygon.class.isAssignableFrom(geomType)) {
                this.encode((Polygon)geom);
            } else if (MultiPoint.class.isAssignableFrom(geomType)) {
                this.encode((MultiPoint)geom);
            } else if (MultiLineString.class.isAssignableFrom(geomType)) {
                this.encode((MultiLineString)geom);
            } else if (MultiPolygon.class.isAssignableFrom(geomType)) {
                this.encode((MultiPolygon)geom);
            } else if (GeometryCollection.class.isAssignableFrom(geomType)) {
                this.encode((GeometryCollection)geom);
            }
        }

        public void encode(Coordinate[] coords) {
            this.out.print("<gml:coordinates>");
            int i = 0;
            while (i < coords.length) {
                this.out.print(String.valueOf(coords[i].x) + "," + coords[i].y);
                this.out.print(i < coords.length - 1 ? " " : "");
                ++i;
            }
            this.out.println("</gml:coordinates>");
        }

        public void encode(Point point) {
            this.out.println("<gml:Point srsName=\"" + this.srs + "\">");
            this.encode(point.getCoordinates());
            this.out.println("</gml:Point>");
        }

        public void encode(LineString line) {
            this.out.println("<gml:LineString srsName=\"" + this.srs + "\">");
            this.encode(line.getCoordinates());
            this.out.println("</gml:LineString>");
        }

        public void encode(Polygon polygon) {
            this.out.println("<gml:Polygon srsName=\"" + this.srs + "\">");
            this.out.println("<gml:outerBoundaryIs>");
            this.encode(polygon.getExteriorRing());
            this.out.println("</gml:outerBoundaryIs>");
            int i = 0;
            while (i < polygon.getNumInteriorRing()) {
                this.out.println("<gml:innerBoundaryIs>");
                this.encode(polygon.getInteriorRingN(i));
                this.out.println("</gml:innerBoundaryIs>");
                ++i;
            }
            this.out.println("</gml:Polygon>");
        }

        public void encode(MultiPoint mpoint) {
            this.out.println("<gml:MultiPoint srsName=\"" + this.srs + "\">\n");
            int i = 0;
            while (i < mpoint.getNumGeometries()) {
                this.encode((Point)mpoint.getGeometryN(i));
                ++i;
            }
            this.out.println("</gml:MultiPoint>\n");
        }

        public void encode(MultiLineString mline) {
            this.out.println("<gml:MultiLineString srsName=\"" + this.srs + "\">\n");
            int i = 0;
            while (i < mline.getNumGeometries()) {
                this.encode((LineString)mline.getGeometryN(i));
                ++i;
            }
            this.out.println("</gml:MultiLineString>\n");
        }

        public void encode(MultiPolygon mpolygon) {
            this.out.println("<gml:MultiPolygon srsName=\"" + this.srs + "\">\n");
            int i = 0;
            while (i < mpolygon.getNumGeometries()) {
                this.encode((Polygon)mpolygon.getGeometryN(i));
                ++i;
            }
            this.out.println("</gml:MultiPolygon>\n");
        }

        public void encode(GeometryCollection geomcoll) {
            this.out.println("<gml:MultiGeometry srsName=\"" + this.srs + "\">\n");
            int i = 0;
            while (i < geomcoll.getNumGeometries()) {
                this.encode(geomcoll.getGeometryN(i));
                ++i;
            }
            this.out.println("</gml:MultiGeometry>\n");
        }
    }
}

