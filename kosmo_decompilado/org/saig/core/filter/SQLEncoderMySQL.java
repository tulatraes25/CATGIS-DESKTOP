/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.io.WKTWriter
 *  org.apache.log4j.Logger
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jump.util.StringUtil;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.saig.core.filter.DefaultExpression;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterCapabilities;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.NullFilter;
import org.saig.core.filter.SQLEncoder;
import org.saig.core.filter.function.FilterFunction_area;
import org.saig.core.filter.function.FilterFunction_geomLength;
import org.saig.core.filter.function.FilterFunction_getX;
import org.saig.core.filter.function.FilterFunction_getY;

public class SQLEncoderMySQL
extends SQLEncoder
implements FilterVisitor {
    private static Logger LOGGER = Logger.getLogger(SQLEncoderMySQL.class);
    private static WKTWriter wkt = new WKTWriter();
    private FilterCapabilities capabilities = this.createFilterCapabilities();
    private int srid;
    private int geometryType;
    private String defaultGeom;
    private static final String SQL_WILD_MULTI = "%";
    private static final String SQL_WILD_SINGLE = "_";
    private String escapedWildcardMulti = "\\.\\*";
    private String escapedWildcardSingle = "\\.\\?";

    public SQLEncoderMySQL() {
        this.setSqlNameEscape("");
    }

    public SQLEncoderMySQL(int srid, int geometryType) {
        this();
        this.srid = srid;
        this.geometryType = geometryType;
    }

    @Override
    protected FilterCapabilities createFilterCapabilities() {
        FilterCapabilities capabilities = new FilterCapabilities();
        capabilities.addType((short)1);
        capabilities.addType((short)2);
        capabilities.addType((short)3);
        capabilities.addType((short)14);
        capabilities.addType((short)23);
        capabilities.addType((short)15);
        capabilities.addType((short)16);
        capabilities.addType((short)17);
        capabilities.addType((short)18);
        capabilities.addType((short)21);
        capabilities.addType((short)19);
        capabilities.addType((short)12345);
        capabilities.addType((short)-12345);
        capabilities.addType((short)4);
        capabilities.addType((short)22);
        capabilities.addType((short)20);
        return capabilities;
    }

    public void setSRID(int srid) {
        this.srid = srid;
    }

    public void setGeometryType(int geometryType) {
        this.geometryType = geometryType;
    }

    public void setDefaultGeometry(String name) {
        this.defaultGeom = name;
    }

    @Override
    public void visit(GeometryFilter filter) throws RuntimeException {
        LOGGER.debug((Object)"exporting GeometryFilter");
        System.out.println("exporting GeometryFilter");
        if (filter.getFilterType() == 4) {
            DefaultExpression left = (DefaultExpression)filter.getLeftGeometry();
            DefaultExpression right = (DefaultExpression)filter.getRightGeometry();
            try {
                this.out.write("MBRIntersects(");
                if (left == null) {
                    this.out.write(this.defaultGeom);
                } else {
                    left.accept(this);
                }
                this.out.write(", ");
                if (right == null) {
                    this.out.write(this.defaultGeom);
                } else {
                    right.accept(this);
                }
                this.out.write(")");
            }
            catch (IOException ioe) {
                LOGGER.warn((Object)("Unable to export filter" + ioe));
            }
        } else {
            LOGGER.warn((Object)"exporting unknown filter type, only bbox supported");
            throw new RuntimeException("Only BBox is currently supported");
        }
    }

    @Override
    public void visitLiteralGeometry(LiteralExpression expression) throws IOException {
        Geometry bbox = (Geometry)expression.getLiteral();
        String geomText = wkt.write(bbox);
        this.out.write("GeometryFromText('" + geomText + "', " + this.srid + ")");
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
    public void visit(NullFilter filter) throws RuntimeException {
        LOGGER.debug((Object)"exporting NullFilter");
        DefaultExpression expr = (DefaultExpression)filter.getNullCheckValue();
        try {
            this.out.write("'");
            expr.accept(this);
            this.out.write("'");
            this.out.write(" IS NULL ");
        }
        catch (IOException ioe) {
            throw new RuntimeException("io problem writing filter", ioe);
        }
    }

    @Override
    public void visit(FunctionExpression expression) throws UnsupportedOperationException {
        if (expression instanceof FilterFunction_area) {
            try {
                FilterFunction_area areaFunction = (FilterFunction_area)expression;
                Expression[] expr = areaFunction.getArgs();
                this.out.write("Area(");
                expr[0].accept(this);
                this.out.write(")");
            }
            catch (IOException ioe) {
                LOGGER.warn((Object)("Unable to export filter" + ioe));
                throw new RuntimeException("io error while writing", ioe);
            }
        } else if (expression instanceof FilterFunction_geomLength) {
            try {
                FilterFunction_geomLength geomLengthFunction = (FilterFunction_geomLength)expression;
                Expression[] expr = geomLengthFunction.getArgs();
                String lengthFunction = "";
                switch (this.geometryType) {
                    case 2: 
                    case 3: {
                        lengthFunction = "GLength";
                        break;
                    }
                    default: {
                        String message = "Function expression support not yet added:" + expression;
                        throw new UnsupportedOperationException(message);
                    }
                }
                this.out.write(String.valueOf(lengthFunction) + "(");
                expr[0].accept(this);
                this.out.write(")");
            }
            catch (IOException ioe) {
                LOGGER.warn((Object)("Unable to export filter" + ioe));
                throw new RuntimeException("io error while writing", ioe);
            }
        } else if (expression instanceof FilterFunction_getX) {
            try {
                FilterFunction_getX getXFunction = (FilterFunction_getX)expression;
                Expression[] expr = getXFunction.getArgs();
                String lengthFunction = "";
                switch (this.geometryType) {
                    case 1: {
                        lengthFunction = "X";
                        break;
                    }
                    default: {
                        String message = "Function expression support not yet added:" + expression;
                        throw new UnsupportedOperationException(message);
                    }
                }
                this.out.write(String.valueOf(lengthFunction) + "(");
                expr[0].accept(this);
                this.out.write(")");
            }
            catch (IOException ioe) {
                LOGGER.warn((Object)("Unable to export filter" + ioe));
                throw new RuntimeException("io error while writing", ioe);
            }
        } else if (expression instanceof FilterFunction_getY) {
            try {
                FilterFunction_getY getYFunction = (FilterFunction_getY)expression;
                Expression[] expr = getYFunction.getArgs();
                String lengthFunction = "";
                switch (this.geometryType) {
                    case 1: {
                        lengthFunction = "Y";
                        break;
                    }
                    default: {
                        String message = "Function expression support not yet added:" + expression;
                        throw new UnsupportedOperationException(message);
                    }
                }
                this.out.write(String.valueOf(lengthFunction) + "(");
                expr[0].accept(this);
                this.out.write(")");
            }
            catch (IOException ioe) {
                LOGGER.warn((Object)("Unable to export filter" + ioe));
                throw new RuntimeException("io error while writing", ioe);
            }
        } else {
            String message = "Function expression support not yet added.";
            throw new UnsupportedOperationException(message);
        }
    }
}

