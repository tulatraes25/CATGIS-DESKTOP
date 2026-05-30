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
import org.saig.core.filter.SQLEncoderPostgis;
import org.saig.core.filter.function.FilterFunction_area;
import org.saig.core.filter.function.FilterFunction_currentDate;
import org.saig.core.filter.function.FilterFunction_daysFrom;
import org.saig.core.filter.function.FilterFunction_geomLength;
import org.saig.core.filter.function.FilterFunction_getX;
import org.saig.core.filter.function.FilterFunction_getY;
import org.saig.core.filter.function.FilterFunction_strLength;

public class SQLEncoderPostgisGeos
extends SQLEncoderPostgis
implements FilterVisitor {
    private static Logger LOGGER = Logger.getLogger(SQLEncoderPostgisGeos.class);
    private static WKTWriter wkt = new WKTWriter();
    private FilterCapabilities capabils = new FilterCapabilities();
    private int srid;
    private int geometryType;
    private String defaultGeom;

    public SQLEncoderPostgisGeos() {
        this.capabils.addType((short)1);
        this.capabils.addType((short)2);
        this.capabils.addType((short)3);
        this.capabils.addType((short)14);
        this.capabils.addType((short)23);
        this.capabils.addType((short)15);
        this.capabils.addType((short)16);
        this.capabils.addType((short)17);
        this.capabils.addType((short)18);
        this.capabils.addType((short)21);
        this.capabils.addType((short)19);
        this.capabils.addType((short)20);
        this.capabils.addType((short)12345);
        this.capabils.addType((short)-12345);
        this.capabils.addType((short)22);
        this.capabils.addType((short)4);
        this.capabils.addType((short)5);
        this.capabils.addType((short)6);
        this.capabils.addType((short)7);
        this.capabils.addType((short)9);
        this.capabils.addType((short)10);
        this.capabils.addType((short)11);
        this.capabils.addType((short)12);
        this.capabils.addType((short)8);
    }

    public SQLEncoderPostgisGeos(int srid, int geometryType) {
        this();
        this.srid = srid;
        this.geometryType = geometryType;
    }

    @Override
    public FilterCapabilities getCapabilities() {
        return this.capabils;
    }

    @Override
    public void setSRID(int srid) {
        this.srid = srid;
    }

    @Override
    public void setGeometryType(int geometryType) {
        this.geometryType = geometryType;
    }

    @Override
    public void setDefaultGeometry(String name) {
        this.defaultGeom = name;
    }

    @Override
    public void visit(GeometryFilter filter) throws RuntimeException {
        short filterType = filter.getFilterType();
        DefaultExpression left = (DefaultExpression)filter.getLeftGeometry();
        DefaultExpression right = (DefaultExpression)filter.getRightGeometry();
        int literalGeometryCount = 0;
        if (left != null && left.getType() == 104) {
            ++literalGeometryCount;
        }
        if (right != null && right.getType() == 104) {
            ++literalGeometryCount;
        }
        boolean constrainBBOX = literalGeometryCount == 1;
        boolean onlyBbox = filterType == 4 && this.looseBbox;
        try {
            boolean bl = constrainBBOX = constrainBBOX && filterType != 6;
            if (constrainBBOX) {
                if (left == null) {
                    this.out.write("\"" + this.defaultGeom + "\"");
                } else {
                    left.accept(this);
                }
                this.out.write(" && ");
                if (right == null) {
                    this.out.write("\"" + this.defaultGeom + "\"");
                } else {
                    right.accept(this);
                }
                if (!onlyBbox) {
                    this.out.write(" AND ");
                }
            }
            String closingParenthesis = ")";
            if (!onlyBbox) {
                if (filterType == 5) {
                    this.out.write("equals");
                } else if (filterType == 6) {
                    this.out.write("NOT (intersects");
                    closingParenthesis = String.valueOf(closingParenthesis) + ")";
                } else if (filterType == 7) {
                    this.out.write("intersects");
                } else if (filterType == 9) {
                    this.out.write("crosses");
                } else if (filterType == 10) {
                    this.out.write("within");
                } else if (filterType == 11) {
                    this.out.write("contains");
                } else if (filterType == 12) {
                    this.out.write("overlaps");
                } else if (filterType == 4) {
                    this.out.write("intersects");
                } else if (filterType == 8) {
                    this.out.write("touches");
                } else {
                    throw new RuntimeException("does not support filter type " + filterType);
                }
                this.out.write("(");
                if (left == null) {
                    this.out.write("\"" + this.defaultGeom + "\"");
                } else {
                    left.accept(this);
                }
                this.out.write(", ");
                if (right == null) {
                    this.out.write("\"" + this.defaultGeom + "\"");
                } else {
                    right.accept(this);
                }
                this.out.write(closingParenthesis);
            }
        }
        catch (IOException ioe) {
            LOGGER.warn((Object)("Unable to export filter" + ioe));
            throw new RuntimeException("io error while writing", ioe);
        }
    }

    @Override
    public void visit(LiteralExpression expression) throws RuntimeException {
        try {
            if (expression.getType() == 104) {
                Geometry bbox = (Geometry)expression.getLiteral();
                String geomText = wkt.write(bbox);
                this.out.write("GeometryFromText('" + geomText + "', " + this.srid + ")");
            } else {
                super.visit(expression);
            }
        }
        catch (IOException ioe) {
            LOGGER.warn((Object)("Unable to export expresion" + ioe));
            throw new RuntimeException("io error while writing", ioe);
        }
    }

    @Override
    public void visit(LikeFilter filter) throws UnsupportedOperationException {
        try {
            String pattern = filter.getPattern();
            this.out.write("\"" + filter.getValue().toString() + "\"");
            if (pattern.contains("(?i)")) {
                pattern = StringUtil.replaceAll(pattern, "(?i)", "");
                this.out.write(" ILIKE ");
            } else {
                this.out.write(" LIKE ");
            }
            pattern = StringUtil.replaceAll(pattern, "*", "%");
            pattern = StringUtil.replaceAll(pattern, "?", "_");
            this.out.write("'" + pattern + "'");
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
                this.out.write("area(");
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
                        lengthFunction = "length";
                        break;
                    }
                    case 4: 
                    case 5: {
                        lengthFunction = "perimeter";
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
                        lengthFunction = "x";
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
                        lengthFunction = "y";
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
        } else if (expression instanceof FilterFunction_strLength) {
            try {
                FilterFunction_daysFrom daysFromFunction = (FilterFunction_daysFrom)expression;
                Expression[] expr = daysFromFunction.getArgs();
                this.out.write("char_length(");
                expr[0].accept(this);
                this.out.write(")");
            }
            catch (IOException ioe) {
                LOGGER.warn((Object)("Unable to export filter" + ioe));
                throw new RuntimeException("io error while writing", ioe);
            }
        } else if (expression instanceof FilterFunction_currentDate) {
            try {
                this.out.write("current_date");
            }
            catch (IOException ioe) {
                LOGGER.warn((Object)("Unable to export filter" + ioe));
                throw new RuntimeException("io error while writing", ioe);
            }
        } else if (expression instanceof FilterFunction_daysFrom) {
            try {
                FilterFunction_daysFrom daysFromFunction = (FilterFunction_daysFrom)expression;
                Expression[] expr = daysFromFunction.getArgs();
                this.out.write("EXTRACT(epoch FROM(");
                expr[1].accept(this);
                this.out.write(" - ");
                expr[0].accept(this);
                this.out.write("))");
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

