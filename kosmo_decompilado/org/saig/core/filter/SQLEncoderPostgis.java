/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.io.WKTWriter
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import java.io.IOException;
import java.util.logging.Logger;
import org.saig.core.filter.DefaultExpression;
import org.saig.core.filter.FilterCapabilities;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.SQLEncoder;

public class SQLEncoderPostgis
extends SQLEncoder
implements FilterVisitor {
    private static Logger LOGGER = Logger.getLogger("org.geotools.filter");
    private static WKTWriter wkt = new WKTWriter();
    private FilterCapabilities capabilities = this.createFilterCapabilities();
    private int srid;
    private int geometryType;
    private String defaultGeom;
    protected boolean looseBbox = false;

    public SQLEncoderPostgis() {
        this.setSqlNameEscape("\"");
    }

    public SQLEncoderPostgis(boolean looseBbox) {
        this();
        this.looseBbox = looseBbox;
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
        return capabilities;
    }

    public SQLEncoderPostgis(int srid) {
        this(true);
        this.srid = srid;
    }

    public void setLooseBbox(boolean isLooseBbox) {
        this.looseBbox = isLooseBbox;
    }

    public boolean isLooseBbox() {
        return this.looseBbox;
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
        LOGGER.finer("exporting GeometryFilter");
        if (filter.getFilterType() == 4) {
            DefaultExpression left = (DefaultExpression)filter.getLeftGeometry();
            DefaultExpression right = (DefaultExpression)filter.getRightGeometry();
            try {
                if (!this.looseBbox) {
                    this.out.write("NOT disjoint(");
                }
                if (left == null) {
                    this.out.write("\"" + this.defaultGeom + "\"");
                } else {
                    left.accept(this);
                }
                if (!this.looseBbox) {
                    this.out.write(", ");
                } else {
                    this.out.write(" && ");
                }
                if (right == null) {
                    this.out.write("\"" + this.defaultGeom + "\"");
                } else {
                    right.accept(this);
                }
                if (!this.looseBbox) {
                    this.out.write(")");
                }
            }
            catch (IOException ioe) {
                LOGGER.warning("Unable to export filter" + ioe);
            }
        } else {
            LOGGER.warning("exporting unknown filter type, only bbox supported");
            throw new RuntimeException("Only BBox is currently supported");
        }
    }

    @Override
    public void visitLiteralGeometry(LiteralExpression expression) throws IOException {
        Geometry bbox = (Geometry)expression.getLiteral();
        String geomText = wkt.write(bbox);
        this.out.write("GeometryFromText('" + geomText + "', " + this.srid + ")");
    }
}

