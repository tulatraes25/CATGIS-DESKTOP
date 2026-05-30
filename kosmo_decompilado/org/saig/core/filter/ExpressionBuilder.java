/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.io.ParseException
 *  com.vividsolutions.jts.io.WKTReader
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.EmptyStackException;
import java.util.Stack;
import org.saig.core.filter.AbstractFilter;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LogicFilter;
import org.saig.core.filter.MathExpression;
import org.saig.core.filter.NullFilter;
import org.saig.core.filter.function.FilterFunction_area;
import org.saig.core.filter.function.FilterFunction_currentDate;
import org.saig.core.filter.function.FilterFunction_daysFrom;
import org.saig.core.filter.function.FilterFunction_geomLength;
import org.saig.core.filter.function.FilterFunction_getX;
import org.saig.core.filter.function.FilterFunction_getY;
import org.saig.core.filter.function.FilterFunction_strLength;
import org.saig.core.filter.parser.ExpressionException;
import org.saig.core.filter.parser.ExpressionParser;
import org.saig.core.filter.parser.ExpressionParserTreeConstants;
import org.saig.core.filter.parser.Node;
import org.saig.core.filter.parser.ParseException;
import org.saig.core.filter.parser.Token;
import org.saig.core.filter.parser.TokenMgrError;

public class ExpressionBuilder {
    public static Object parse(FeatureSchema schema, String input) throws ParseException {
        ExpressionCompiler c = new ExpressionCompiler(schema, input);
        try {
            c.CompilationUnit();
        }
        catch (TokenMgrError tme) {
            throw new ExpressionException(tme.getMessage(), c.getToken(0));
        }
        if (c.exception != null) {
            throw c.exception;
        }
        StackItem item = c.stack.peek();
        return item.built;
    }

    public static Object parse(String input) throws ParseException {
        return ExpressionBuilder.parse(null, input);
    }

    public static String getFormattedErrorMessage(ParseException pe, String input) {
        StringBuffer sb = new StringBuffer(input);
        sb.append('\n');
        Token t = pe.currentToken;
        while (t.next != null) {
            t = t.next;
        }
        int column = t.beginColumn - 1;
        int i = 0;
        while (i < column) {
            sb.append(' ');
            ++i;
        }
        sb.append('^').append('\n');
        sb.append(pe.getMessage());
        return sb.toString();
    }

    static class ExpressionCompiler
    extends ExpressionParser
    implements ExpressionParserTreeConstants {
        Stack<StackItem> stack = new Stack();
        FilterFactory factory = FilterFactory.createFilterFactory();
        ExpressionException exception = null;
        String input;
        FeatureSchema schema;
        WKTReader reader;

        ExpressionCompiler(FeatureSchema schema, String input) {
            super(new StringReader(input));
            this.input = input;
            this.schema = schema;
        }

        StackItem popStack() {
            return this.stack.pop();
        }

        Expression expression() throws ExpressionException {
            StackItem item = null;
            try {
                item = this.popStack();
                return (Expression)item.built;
            }
            catch (ClassCastException cce) {
                throw new ExpressionException("Expecting Expression, but found Filter", item.token);
            }
            catch (EmptyStackException ese) {
                throw new ExpressionException("No items on stack", this.getToken(0));
            }
        }

        Filter filter() throws ExpressionException {
            StackItem item = null;
            try {
                item = this.popStack();
                return (Filter)item.built;
            }
            catch (ClassCastException cce) {
                throw new ExpressionException("Expecting Filter, but found Expression", item.token);
            }
            catch (EmptyStackException ese) {
                throw new ExpressionException("No items on stack", this.getToken(0));
            }
        }

        double doubleValue() throws ExpressionException {
            try {
                return ((Number)this.expression().getValue(null)).doubleValue();
            }
            catch (ClassCastException cce) {
                throw new ExpressionException("Expected double", this.getToken(0));
            }
        }

        int intValue() throws ExpressionException {
            try {
                return ((Number)this.expression().getValue(null)).intValue();
            }
            catch (ClassCastException cce) {
                throw new ExpressionException("Expected int", this.getToken(0));
            }
        }

        String stringValue() throws ExpressionException {
            return this.expression().getValue(null).toString();
        }

        @Override
        public void jjtreeOpenNodeScope(Node n) {
        }

        @Override
        public void jjtreeCloseNodeScope(Node n) throws ParseException {
            try {
                Object built = this.buildObject(n);
                if (built == null) {
                    throw new RuntimeException("INTERNAL ERROR : Node " + n + " resulted in null build");
                }
                this.stack.push(new StackItem(built, this.getToken(0)));
            }
            finally {
                n.dispose();
            }
        }

        String token() {
            return this.getToken((int)0).image;
        }

        MathExpression mathExpression(short type) throws ExpressionException {
            try {
                MathExpression e = this.factory.createMathExpression(type);
                Expression right = this.expression();
                Expression left = this.expression();
                e.addLeftValue(left);
                e.addRightValue(right);
                return e;
            }
            catch (IllegalFilterException ife) {
                throw new ExpressionException("Exception building MathExpression", this.getToken(0), ife);
            }
        }

        LogicFilter logicFilter(short type) throws ExpressionException {
            try {
                Filter right = this.filter();
                Filter left = this.filter();
                return this.factory.createLogicFilter(left, right, type);
            }
            catch (IllegalFilterException ife) {
                throw new ExpressionException("Exception building LogicFilter", this.getToken(0), ife);
            }
        }

        CompareFilter compareFilter(short type) throws ExpressionException {
            try {
                CompareFilter f = this.factory.createCompareFilter(type);
                Expression right = this.expression();
                Expression left = this.expression();
                f.addLeftValue(left);
                f.addRightValue(right);
                return f;
            }
            catch (IllegalFilterException ife) {
                throw new ExpressionException("Exception building CompareFilter", this.getToken(0), ife);
            }
        }

        CompareFilter betweenFilter() throws ExpressionException {
            try {
                BetweenFilter f = this.factory.createBetweenFilter();
                Expression right = this.expression();
                Expression middle = this.expression();
                Expression left = this.expression();
                f.addLeftValue(left);
                f.addMiddleValue(middle);
                f.addRightValue(right);
                return f;
            }
            catch (IllegalFilterException ife) {
                throw new ExpressionException("Exception building CompareFilter", this.getToken(0), ife);
            }
        }

        Object buildObject(Node n) throws ExpressionException {
            switch (n.getType()) {
                case 16: {
                    return this.factory.createLiteralExpression(Integer.parseInt(this.token()));
                }
                case 17: {
                    return this.factory.createLiteralExpression(Double.parseDouble(this.token()));
                }
                case 20: {
                    return this.factory.createLiteralExpression(n.getToken().image);
                }
                case 15: {
                    try {
                        return this.factory.createAttributeExpression(this.schema, this.token());
                    }
                    catch (IllegalFilterException ife) {
                        throw new ExpressionException("Exception building AttributeExpression", this.getToken(0), ife);
                    }
                }
                case 21: {
                    return this.parseFunction(n);
                }
                case 10: {
                    return this.mathExpression((short)105);
                }
                case 11: {
                    return this.mathExpression((short)106);
                }
                case 12: {
                    return this.mathExpression((short)107);
                }
                case 13: {
                    return this.mathExpression((short)108);
                }
                case 1: {
                    return this.logicFilter((short)1);
                }
                case 2: {
                    return this.logicFilter((short)2);
                }
                case 14: {
                    return this.filter().not();
                }
                case 5: {
                    return this.betweenFilter();
                }
                case 8: {
                    return this.compareFilter((short)17);
                }
                case 6: {
                    return this.compareFilter((short)15);
                }
                case 9: {
                    return this.compareFilter((short)18);
                }
                case 7: {
                    return this.compareFilter((short)16);
                }
                case 3: {
                    return this.compareFilter((short)14);
                }
                case 4: {
                    return this.compareFilter((short)23);
                }
                case 22: {
                    Token end = n.getToken();
                    while (end.next != null) {
                        end = end.next;
                    }
                    return this.geometry(n.getToken(), end);
                }
                case 18: 
                case 19: {
                    throw new ExpressionException("Unsupported syntax", this.getToken(0));
                }
            }
            return null;
        }

        LiteralExpression geometry(Token start, Token end) throws ExpressionException {
            if (this.reader == null) {
                this.reader = new WKTReader();
            }
            String wktGeom = this.input.substring(start.beginColumn - 1, end.endColumn);
            try {
                Geometry g = this.reader.read(wktGeom);
                return this.factory.createLiteralExpression(g);
            }
            catch (com.vividsolutions.jts.io.ParseException e) {
                throw new ExpressionException(e.getMessage(), start);
            }
            catch (Exception e) {
                throw new ExpressionException("Error building WKT Geometry", start, e);
            }
        }

        Object parseFunction(Node n) throws ExpressionException {
            String function = n.getToken().image;
            if ("box".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 4) {
                    throw new ExpressionException("Bounding Box filter requires 4 arguments", this.getToken(0));
                }
                double d4 = this.doubleValue();
                double d3 = this.doubleValue();
                double d2 = this.doubleValue();
                double d1 = this.doubleValue();
                try {
                    return this.factory.createBBoxExpression(new Envelope(d1, d2, d3, d4));
                }
                catch (IllegalFilterException ife) {
                    throw new ExpressionException("Exception building BBoxExpression", this.getToken(0), ife);
                }
            }
            if ("id".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 1) {
                    throw new ExpressionException("Feature ID filter requires 1 argument", this.getToken(0));
                }
                return this.factory.createFidFilter(this.stringValue());
            }
            if ("between".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 3) {
                    throw new ExpressionException("Between filter requires 3 arguments", this.getToken(0));
                }
                Expression two = this.expression();
                Expression att = this.expression();
                Expression one = this.expression();
                try {
                    BetweenFilter b = this.factory.createBetweenFilter();
                    b.addLeftValue(one);
                    b.addMiddleValue(att);
                    b.addRightValue(two);
                    return b;
                }
                catch (IllegalFilterException ife) {
                    throw new ExpressionException("Exception building BetweenFilter", this.getToken(0), ife);
                }
            }
            if ("like".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 2) {
                    throw new ExpressionException("Like filter requires at least two arguments", this.getToken(0));
                }
                LikeFilter f = this.factory.createLikeFilter();
                f.setPattern(this.stringValue(), "*", ".?", "\\");
                try {
                    f.setValue(this.expression());
                }
                catch (IllegalFilterException ife) {
                    throw new ExpressionException("Exception building LikeFilter", this.getToken(0), ife);
                }
                return f;
            }
            if ("null".equalsIgnoreCase(function) || "isNull".equalsIgnoreCase(function)) {
                NullFilter nf = this.factory.createNullFilter();
                Expression e = this.expression();
                try {
                    if (e instanceof LiteralExpression) {
                        e = this.factory.createAttributeExpression(this.schema, ((LiteralExpression)e).getValue(null).toString());
                    }
                    nf.setNullCheckValue(e);
                }
                catch (IllegalFilterException ife) {
                    throw new ExpressionException("Exception building NullFilter", this.getToken(0), ife);
                }
                return nf;
            }
            if ("area".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 1) {
                    throw new ExpressionException("Area function requires only one argument", this.getToken(0));
                }
                FilterFunction_area areaFunction = new FilterFunction_area();
                Expression e = this.expression();
                areaFunction.setArgs(new Expression[]{e});
                return areaFunction;
            }
            if ("geomLength".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 1) {
                    throw new ExpressionException("GeomLength function requires only one argument", this.getToken(0));
                }
                FilterFunction_geomLength geomLengthFunction = new FilterFunction_geomLength();
                Expression e = this.expression();
                geomLengthFunction.setArgs(new Expression[]{e});
                return geomLengthFunction;
            }
            if ("getX".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 1) {
                    throw new ExpressionException("GetX function requires only one argument", this.getToken(0));
                }
                FilterFunction_getX getXFunction = new FilterFunction_getX();
                Expression e = this.expression();
                getXFunction.setArgs(new Expression[]{e});
                return getXFunction;
            }
            if ("getY".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 1) {
                    throw new ExpressionException("GetY function requires only one argument", this.getToken(0));
                }
                FilterFunction_getY getYFunction = new FilterFunction_getY();
                Expression e = this.expression();
                getYFunction.setArgs(new Expression[]{e});
                return getYFunction;
            }
            if ("strLength".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 1) {
                    throw new ExpressionException("StrLength function requires only one argument", this.getToken(0));
                }
                FilterFunction_strLength strLengthFuncion = new FilterFunction_strLength();
                Expression e = this.expression();
                strLengthFuncion.setArgs(new Expression[]{e});
                return strLengthFuncion;
            }
            if ("currentDate".equalsIgnoreCase(function)) {
                FilterFunction_currentDate currentDateFunction = new FilterFunction_currentDate();
                return currentDateFunction;
            }
            if ("daysFrom".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 2) {
                    throw new ExpressionException("DaysFrom function requires only two arguments", this.getToken(0));
                }
                FilterFunction_daysFrom daysFromFunction = new FilterFunction_daysFrom();
                Expression two = this.expression();
                Expression one = this.expression();
                daysFromFunction.setArgs(new Expression[]{one, two});
                return daysFromFunction;
            }
            short geometryFilterType = this.lookupGeometryFilter(function);
            if (geometryFilterType >= 0) {
                return this.buildGeometryFilter(geometryFilterType);
            }
            throw new ExpressionException("Function " + function + " doesn't exist", this.getToken(0));
        }

        short lookupGeometryFilter(String name) {
            Field[] f = AbstractFilter.class.getFields();
            name = name.toUpperCase();
            int i = 0;
            int ii = f.length;
            while (i < ii) {
                if (f[i].getName().endsWith(name)) {
                    try {
                        return f[i].getShort(null);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                ++i;
            }
            return -1;
        }

        GeometryFilter buildGeometryFilter(short type) throws ExpressionException {
            Expression right = this.expression();
            Expression left = this.expression();
            try {
                GeometryFilter f = this.factory.createGeometryFilter(type);
                f.addLeftGeometry(left);
                f.addRightGeometry(right);
                return f;
            }
            catch (IllegalFilterException ife) {
                throw new ExpressionException("Exception building GeometryFilter", this.getToken(0), ife);
            }
        }
    }

    static class StackItem {
        Object built;
        Token token;

        StackItem(Object b, Token t) {
            this.built = b;
            this.token = t;
        }
    }
}

