/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.data.jdbc.fidmapper.FIDMapper
 */
package org.saig.core.filter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.saig.core.filter.AbstractFilter;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.DefaultExpression;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterCapabilities;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LogicFilter;
import org.saig.core.filter.MathExpression;
import org.saig.core.filter.NullFilter;
import org.saig.core.filter.SQLEncoderException;

public class SQLEncoder
implements FilterVisitor {
    protected static final String IO_ERROR = "io problem writing filter";
    private static FilterCapabilities capabilities = null;
    private static Logger LOGGER = Logger.getLogger("org.geotools.filter");
    private static Map<Integer, String> comparisions = new HashMap<Integer, String>();
    private static Map<Integer, String> spatial = new HashMap<Integer, String>();
    private static Map<Integer, String> logical = new HashMap<Integer, String>();
    private static Map<Integer, String> expressions = new HashMap<Integer, String>();
    private String sqlNameEscape = "";
    protected Writer out;
    protected FIDMapper mapper;

    static {
        comparisions.put(new Integer(14), "=");
        comparisions.put(new Integer(23), "!=");
        comparisions.put(new Integer(16), ">");
        comparisions.put(new Integer(18), ">=");
        comparisions.put(new Integer(15), "<");
        comparisions.put(new Integer(17), "<=");
        comparisions.put(new Integer(20), "LIKE");
        comparisions.put(new Integer(21), "IS NULL");
        comparisions.put(new Integer(19), "BETWEEN");
        expressions.put(new Integer(105), "+");
        expressions.put(new Integer(108), "/");
        expressions.put(new Integer(107), "*");
        expressions.put(new Integer(106), "-");
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
        logical.put(new Integer(2), "AND");
        logical.put(new Integer(1), "OR");
        logical.put(new Integer(3), "NOT");
    }

    public SQLEncoder() {
    }

    public SQLEncoder(Writer out, Filter filter) throws SQLEncoderException {
        if (this.getCapabilities().fullySupports(filter)) {
            this.out = out;
            try {
                out.write("WHERE ");
                filter.accept(this);
            }
            catch (IOException ioe) {
                LOGGER.warning("Unable to export filter: " + ioe);
                throw new SQLEncoderException("Problem writing filter: ", ioe);
            }
        } else {
            throw new SQLEncoderException("Filter type not supported");
        }
    }

    public void setFIDMapper(FIDMapper mapper) {
        this.mapper = mapper;
    }

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
        capabilities.addType((short)22);
        capabilities.addType((short)12345);
        capabilities.addType((short)-12345);
        return capabilities;
    }

    public void encode(Writer out, Filter filter) throws SQLEncoderException {
        if (this.getCapabilities().fullySupports(filter)) {
            this.out = out;
            try {
                out.write("WHERE ");
                filter.accept(this);
            }
            catch (IOException ioe) {
                LOGGER.warning("Unable to export filter" + ioe);
                throw new SQLEncoderException("Problem writing filter: ", ioe);
            }
        } else {
            throw new SQLEncoderException("Filter type not supported");
        }
    }

    public String encode(Filter filter) throws SQLEncoderException {
        StringWriter output = new StringWriter();
        this.encode(output, filter);
        return output.getBuffer().toString();
    }

    public synchronized FilterCapabilities getCapabilities() {
        if (capabilities == null) {
            capabilities = this.createFilterCapabilities();
        }
        return capabilities;
    }

    @Override
    public void visit(Filter filter) {
        try {
            if (filter.getFilterType() == 12345) {
                this.out.write("TRUE");
            } else if (filter.getFilterType() == -12345) {
                this.out.write("FALSE");
            } else {
                LOGGER.warning("exporting unknown filter type:" + filter.toString());
            }
        }
        catch (IOException ioe) {
            throw new RuntimeException(IO_ERROR, ioe);
        }
    }

    @Override
    public void visit(BetweenFilter filter) throws RuntimeException {
        LOGGER.finer("exporting BetweenFilter");
        DefaultExpression left = (DefaultExpression)filter.getLeftValue();
        DefaultExpression right = (DefaultExpression)filter.getRightValue();
        DefaultExpression mid = (DefaultExpression)filter.getMiddleValue();
        LOGGER.finer("Filter type id is " + filter.getFilterType());
        LOGGER.finer("Filter type text is " + comparisions.get(new Integer(filter.getFilterType())));
        try {
            mid.accept(this);
            this.out.write(" BETWEEN ");
            left.accept(this);
            this.out.write(" AND ");
            right.accept(this);
        }
        catch (IOException ioe) {
            throw new RuntimeException(IO_ERROR, ioe);
        }
    }

    @Override
    public void visit(LikeFilter filter) throws UnsupportedOperationException {
        String message = "Like Filter support not yet added.";
        throw new UnsupportedOperationException(message);
    }

    @Override
    public void visit(LogicFilter filter) throws RuntimeException {
        LOGGER.finer("exporting LogicFilter");
        String type = logical.get(new Integer(filter.getFilterType()));
        try {
            Iterator<Filter> list = filter.getFilterIterator();
            if (filter.getFilterType() == 3) {
                this.out.write(" NOT (");
                ((AbstractFilter)list.next()).accept(this);
                this.out.write(")");
            } else {
                this.out.write("(");
                while (list.hasNext()) {
                    ((AbstractFilter)list.next()).accept(this);
                    if (!list.hasNext()) continue;
                    this.out.write(" " + type + " ");
                }
                this.out.write(")");
            }
        }
        catch (IOException ioe) {
            throw new RuntimeException(IO_ERROR, ioe);
        }
    }

    @Override
    public void visit(CompareFilter filter) throws RuntimeException {
        LOGGER.finer("exporting SQL ComparisonFilter");
        DefaultExpression left = (DefaultExpression)filter.getLeftValue();
        DefaultExpression right = (DefaultExpression)filter.getRightValue();
        LOGGER.finer("Filter type id is " + filter.getFilterType());
        LOGGER.finer("Filter type text is " + comparisions.get(new Integer(filter.getFilterType())));
        String type = comparisions.get(new Integer(filter.getFilterType()));
        try {
            left.accept(this);
            this.out.write(" " + type + " ");
            right.accept(this);
        }
        catch (IOException ioe) {
            throw new RuntimeException(IO_ERROR, ioe);
        }
    }

    @Override
    public void visit(NullFilter filter) throws RuntimeException {
        LOGGER.finer("exporting NullFilter");
        DefaultExpression expr = (DefaultExpression)filter.getNullCheckValue();
        try {
            expr.accept(this);
            this.out.write(" IS NULL ");
        }
        catch (IOException ioe) {
            throw new RuntimeException(IO_ERROR, ioe);
        }
    }

    @Override
    public void visit(FidFilter filter) {
        if (this.mapper == null) {
            throw new RuntimeException("Must set a fid mapper before trying to encode FIDFilters");
        }
        String[] fids = filter.getFids();
        LOGGER.finer("Exporting FID=" + Arrays.asList(fids));
        String[] colNames = new String[this.mapper.getColumnCount()];
        int i = 0;
        while (i < colNames.length) {
            colNames[i] = this.mapper.getColumnName(i);
            ++i;
        }
        i = 0;
        while (i < fids.length) {
            try {
                Object[] attValues = this.mapper.getPKAttributes(fids[i]);
                this.out.write("(");
                int j = 0;
                while (j < attValues.length) {
                    this.out.write(colNames[j]);
                    this.out.write(" = '");
                    this.out.write(attValues[j].toString());
                    this.out.write("'");
                    if (j < attValues.length - 1) {
                        this.out.write(" AND ");
                    }
                    ++j;
                }
                this.out.write(")");
                if (i < fids.length - 1) {
                    this.out.write(" OR ");
                }
            }
            catch (IOException e) {
                LOGGER.warning("IO Error exporting FID Filter.");
            }
            ++i;
        }
    }

    @Override
    public void visit(AttributeExpression expression) throws RuntimeException {
        LOGGER.finer("exporting ExpressionAttribute");
        try {
            this.out.write(this.escapeName(expression.getAttributePath()));
        }
        catch (IOException ioe) {
            throw new RuntimeException("IO problems writing attribute exp", ioe);
        }
    }

    @Override
    public void visit(Expression expression) {
        LOGGER.warning("exporting unknown (default) expression");
    }

    @Override
    public void visit(LiteralExpression expression) throws RuntimeException {
        LOGGER.finer("exporting LiteralExpression");
        try {
            Object literal = expression.getLiteral();
            short type = expression.getType();
            switch (type) {
                case 101: 
                case 102: {
                    this.out.write(literal.toString());
                    break;
                }
                case 103: {
                    this.out.write("'" + literal + "'");
                    break;
                }
                case 104: {
                    this.visitLiteralGeometry(expression);
                    break;
                }
                default: {
                    throw new RuntimeException("type: " + type + "not supported");
                }
            }
        }
        catch (IOException ioe) {
            throw new RuntimeException("IO problems writing literal", ioe);
        }
    }

    protected void visitLiteralGeometry(LiteralExpression expression) throws IOException {
        throw new RuntimeException("Subclasses must implement this method in order to handle geometries");
    }

    @Override
    public void visit(GeometryFilter filter) {
        throw new RuntimeException("Subclasses must implement this method in order to handle geometries");
    }

    @Override
    public void visit(MathExpression expression) throws RuntimeException {
        LOGGER.finer("exporting Expression Math");
        String type = expressions.get(new Integer(expression.getType()));
        try {
            ((DefaultExpression)expression.getLeftValue()).accept(this);
            this.out.write(" " + type + " ");
            ((DefaultExpression)expression.getRightValue()).accept(this);
        }
        catch (IOException ioe) {
            throw new RuntimeException("IO problems writing expression", ioe);
        }
    }

    @Override
    public void visit(FunctionExpression expression) throws UnsupportedOperationException {
        String message = "Function expression support not yet added.";
        throw new UnsupportedOperationException(message);
    }

    public void setSqlNameEscape(String escape) {
        this.sqlNameEscape = escape;
    }

    public void setColnameEscape(String escape) {
        this.sqlNameEscape = escape;
    }

    protected String getColnameEscape() {
        return this.sqlNameEscape;
    }

    public String escapeName(String name) {
        return String.valueOf(this.sqlNameEscape) + name + this.sqlNameEscape;
    }
}

