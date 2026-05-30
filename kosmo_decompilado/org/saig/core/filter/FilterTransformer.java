/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.geotools.gml.producer.GeometryTransformer$GeometryTranslator
 *  org.geotools.xml.transform.TransformerBase
 *  org.geotools.xml.transform.TransformerBase$TranslatorSupport
 *  org.geotools.xml.transform.Translator
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Geometry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.transform.TransformerException;
import org.geotools.gml.producer.GeometryTransformer;
import org.geotools.xml.transform.TransformerBase;
import org.geotools.xml.transform.Translator;
import org.saig.core.filter.AbstractFilter;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionBuilder;
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
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class FilterTransformer
extends TransformerBase {
    private static String defaultNamespace = "http://www.opengis.net/ogc";
    private static Map comparisions = new HashMap();
    private static Map spatial = new HashMap();
    private static Map logical = new HashMap();
    private static Map expressions = new HashMap();

    static {
        comparisions.put((short)14, "PropertyIsEqualTo");
        comparisions.put((short)16, "PropertyIsGreaterThan");
        comparisions.put((short)18, "PropertyIsGreaterThanOrEqualTo");
        comparisions.put((short)15, "PropertyIsLessThan");
        comparisions.put((short)17, "PropertyIsLessThanOrEqualTo");
        comparisions.put((short)20, "PropertyIsLike");
        comparisions.put((short)21, "PropertyIsNull");
        comparisions.put((short)19, "PropertyIsBetween");
        comparisions.put((short)23, "PropertyIsNotEqualTo");
        expressions.put((short)105, "Add");
        expressions.put((short)108, "Div");
        expressions.put((short)107, "Mul");
        expressions.put((short)106, "Sub");
        expressions.put((short)114, "Function");
        spatial.put((short)5, "Equals");
        spatial.put((short)6, "Disjoint");
        spatial.put((short)7, "Intersects");
        spatial.put((short)8, "Touches");
        spatial.put((short)9, "Crosses");
        spatial.put((short)10, "Within");
        spatial.put((short)11, "Contains");
        spatial.put((short)12, "Overlaps");
        spatial.put((short)13, "Beyond");
        spatial.put((short)4, "BBOX");
        spatial.put((short)24, "DWithin");
        logical.put((short)2, "And");
        logical.put((short)1, "Or");
        logical.put((short)3, "Not");
    }

    public String transform(Filter f) throws TransformerException {
        return super.transform((Object)f);
    }

    public Translator createTranslator(ContentHandler handler) {
        return new FilterTranslator(handler);
    }

    public static void main(String[] args) throws Exception {
        Filter f = (Filter)ExpressionBuilder.parse("LAYER = 'Prueba'");
        FilterTransformer transformer = new FilterTransformer();
        String XML = transformer.transform(f);
        System.err.println(XML);
    }

    public static class FilterTranslator
    extends TransformerBase.TranslatorSupport
    implements FilterVisitor {
        GeometryTransformer.GeometryTranslator geometryEncoder;
        protected boolean writeSimpleLiterals = false;
        protected boolean insideFilterFlag = false;

        public FilterTranslator(ContentHandler handler) {
            this(handler, false);
        }

        public FilterTranslator(ContentHandler handler, boolean writeSimpleLiterals) {
            super(handler, "ogc", defaultNamespace);
            this.writeSimpleLiterals = writeSimpleLiterals;
            this.geometryEncoder = new GeometryTransformer.GeometryTranslator(handler);
            this.addNamespaceDeclarations((TransformerBase.TranslatorSupport)this.geometryEncoder);
        }

        @Override
        public void visit(LogicFilter filter) {
            this.insideFilterFlag = true;
            filter.getFilterType();
            String type = (String)logical.get(filter.getFilterType());
            this.start(type);
            Iterator<Filter> list = filter.getFilterIterator();
            while (list.hasNext()) {
                ((AbstractFilter)list.next()).accept(this);
            }
            this.end(type);
            this.insideFilterFlag = false;
        }

        @Override
        public void visit(NullFilter filter) {
            this.insideFilterFlag = true;
            Expression expr = filter.getNullCheckValue();
            String type = (String)comparisions.get(filter.getFilterType());
            this.start(type);
            expr.accept(this);
            this.end(type);
            this.insideFilterFlag = false;
        }

        @Override
        public void visit(FidFilter filter) {
            this.insideFilterFlag = true;
            String[] fids = filter.getFids();
            int i = 0;
            while (i < fids.length) {
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "fid", "", "", fids[i]);
                this.element("FeatureId", null, atts);
                ++i;
            }
            this.insideFilterFlag = false;
        }

        @Override
        public void visit(Filter filter) {
            try {
                this.insideFilterFlag = true;
                this.contentHandler.startElement("", "!--", "!--", this.NULL_ATTS);
                this.chars("Unidentified Filter " + filter.getClass());
                this.contentHandler.endElement("", "--", "--");
                this.insideFilterFlag = false;
            }
            catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }

        @Override
        public void visit(LikeFilter filter) {
            this.insideFilterFlag = true;
            String wcm = filter.getWildcardMulti();
            String wcs = filter.getWildcardSingle();
            String esc = filter.getEscape();
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "wildCard", "", "", wcm);
            atts.addAttribute("", "singleChar", "", "", wcs);
            atts.addAttribute("", "escape", "", "", esc);
            this.start("PropertyIsLike", atts);
            this.encode(filter.getValue());
            this.element("Literal", filter.getPattern());
            this.end("PropertyIsLike");
            this.insideFilterFlag = false;
        }

        @Override
        public void visit(BetweenFilter filter) {
            this.insideFilterFlag = true;
            Expression left = filter.getLeftValue();
            Expression right = filter.getRightValue();
            Expression mid = filter.getMiddleValue();
            String type = (String)comparisions.get(filter.getFilterType());
            this.start(type);
            mid.accept(this);
            this.start("LowerBoundary");
            left.accept(this);
            this.end("LowerBoundary");
            this.start("UpperBoundary");
            right.accept(this);
            this.end("UpperBoundary");
            this.end(type);
            this.insideFilterFlag = false;
        }

        @Override
        public void visit(AttributeExpression expression) {
            this.element("PropertyName", expression.getAttributePath());
        }

        @Override
        public void visit(MathExpression expression) {
            String type = (String)expressions.get(expression.getType());
            this.start(type);
            this.encode(expression.getLeftValue());
            this.encode(expression.getRightValue());
            this.end(type);
        }

        @Override
        public void visit(FunctionExpression expression) {
            String type = (String)expressions.get(expression.getType());
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "name", "name", "", expression.getName());
            this.start(type, atts);
            Expression[] args = expression.getArgs();
            int i = 0;
            while (i < args.length) {
                args[i].accept(this);
                ++i;
            }
            this.end(type);
        }

        @Override
        public void visit(CompareFilter filter) {
            this.insideFilterFlag = true;
            Expression left = filter.getLeftValue();
            Expression right = filter.getRightValue();
            String type = (String)comparisions.get(filter.getFilterType());
            this.start(type);
            left.accept(this);
            right.accept(this);
            this.end(type);
            this.insideFilterFlag = false;
        }

        @Override
        public void visit(GeometryFilter filter) {
            this.insideFilterFlag = true;
            Expression left = filter.getLeftGeometry();
            Expression right = filter.getRightGeometry();
            String type = (String)spatial.get(filter.getFilterType());
            this.start(type);
            left.accept(this);
            right.accept(this);
            this.end(type);
            this.insideFilterFlag = false;
        }

        @Override
        public void visit(Expression expression) {
        }

        @Override
        public void visit(LiteralExpression expression) {
            Object value = expression.getLiteral();
            if (Geometry.class.isAssignableFrom(value.getClass())) {
                this.geometryEncoder.encode((Geometry)value);
            } else if (this.writeSimpleLiterals && !this.insideFilterFlag) {
                this.chars(value.toString());
            } else {
                this.element("Literal", value.toString());
            }
        }

        public void encode(Expression e) {
            e.accept(this);
        }

        public void encode(Filter f) {
            this.start("Filter");
            f.accept(this);
            this.end("Filter");
        }

        public void encode(Object o) {
            if (o instanceof Filter) {
                this.encode((Filter)o);
            } else if (o instanceof Expression) {
                this.encode((Expression)o);
            } else {
                throw new IllegalArgumentException("Cannot encode " + (o == null ? "null" : o.getClass().getName()));
            }
        }
    }
}

