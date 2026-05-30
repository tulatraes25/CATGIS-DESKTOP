/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.geotools.algorithms.classification.EqualClasses
 */
package org.saig.core.styling;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.core.styling.Gradient;
import java.awt.Color;
import java.net.URL;
import org.apache.log4j.Logger;
import org.geotools.algorithms.classification.EqualClasses;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.styling.AnchorPoint;
import org.saig.core.styling.ColorMap;
import org.saig.core.styling.ColorMapEntry;
import org.saig.core.styling.Displacement;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Font;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Halo;
import org.saig.core.styling.LabelPlacement;
import org.saig.core.styling.LinePlacement;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.Mark;
import org.saig.core.styling.PointPlacement;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.Symbol;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;

public class StyleBuilder {
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.core.styling.StyleBuilder");
    public static final String LINE_JOIN_MITER = "miter";
    public static final String LINE_JOIN_MITRE = "mitre";
    public static final String LINE_JOIN_ROUND = "round";
    public static final String LINE_JOIN_BEVEL = "bevel";
    public static final String LINE_CAP_BUTT = "butt";
    public static final String LINE_CAP_ROUND = "round";
    public static final String LINE_CAP_SQUARE = "square";
    public static final String MARK_SQUARE = "square";
    public static final String MARK_CIRCLE = "circle";
    public static final String MARK_TRIANGLE = "triangle";
    public static final String MARK_STAR = "star";
    public static final String MARK_CROSS = "cross";
    public static final String MARK_ARROW = "arrow";
    public static final String MARK_X = "x";
    public static final String FONT_STYLE_NORMAL = "normal";
    public static final String FONT_STYLE_ITALIC = "italic";
    public static final String FONT_STYLE_OBLIQUE = "oblique";
    public static final String FONT_WEIGHT_NORMAL = "normal";
    public static final String FONT_WEIGHT_BOLD = "bold";
    private StyleFactory sf;
    private FilterFactory ff;

    public StyleBuilder() {
        this.sf = StyleFactory.createStyleFactory();
        this.ff = FilterFactory.createFilterFactory();
    }

    public StyleBuilder(StyleFactory styleFactory) {
        this.sf = styleFactory;
        this.ff = FilterFactory.createFilterFactory();
    }

    public StyleBuilder(FilterFactory filterFactory) {
        this.ff = filterFactory;
        this.sf = StyleFactory.createStyleFactory();
    }

    public StyleBuilder(StyleFactory styleFactory, FilterFactory filterFactory) {
        this.sf = styleFactory;
        this.ff = filterFactory;
    }

    public StyleFactory getStyleFactory() {
        return this.sf;
    }

    public FilterFactory getFilterFactory() {
        return this.ff;
    }

    public Stroke createStroke() {
        return this.sf.getDefaultStroke();
    }

    public Stroke createStroke(double width) {
        return this.createStroke(Color.BLACK, width);
    }

    public Stroke createStroke(Color color) {
        return this.createStroke(color, 1.0);
    }

    public Stroke createStroke(Color color, double width) {
        return this.sf.createStroke(this.colorExpression(color), this.literalExpression(width));
    }

    public Stroke createStroke(Color color, double width, String lineJoin, String lineCap) {
        Stroke stroke = this.createStroke(color, width);
        stroke.setLineJoin(this.literalExpression(lineJoin));
        stroke.setLineCap(this.literalExpression(lineCap));
        return stroke;
    }

    public Stroke createStroke(Color color, double width, float[] dashArray) {
        Stroke stroke = this.createStroke(color, width);
        stroke.setDashArray(dashArray);
        return stroke;
    }

    public Stroke createStroke(Expression color, Expression width) {
        return this.sf.createStroke(color, width);
    }

    public Stroke createStroke(Color color, double width, double opacity) {
        return this.sf.createStroke(this.colorExpression(color), this.literalExpression(width), this.literalExpression(opacity));
    }

    public Stroke createStroke(Expression color, Expression width, Expression opacity) {
        return this.sf.createStroke(color, width, opacity);
    }

    public Fill createFill() {
        Fill f = this.sf.getDefaultFill();
        f.setColor(this.literalExpression("#808080"));
        f.setBackgroundColor(this.literalExpression("#808080"));
        f.setOpacity(this.literalExpression(1.0));
        return f;
    }

    public Fill createFill(Color fillColor) {
        if (fillColor == null) {
            return null;
        }
        return this.createFill(this.colorExpression(fillColor));
    }

    public Fill createFill(Expression fillColor) {
        return this.sf.createFill(fillColor);
    }

    public Fill createFill(Color fillColor, double opacity) {
        return this.sf.createFill(this.colorExpression(fillColor), this.literalExpression(opacity));
    }

    public Fill createFill(Expression color, Expression opacity) {
        return this.sf.createFill(color, opacity);
    }

    public Fill createFill(Color color, Color backgroundColor, double opacity, Graphic fill) {
        return this.sf.createFill(this.colorExpression(color), this.colorExpression(backgroundColor), this.literalExpression(opacity), fill);
    }

    public Fill createFill(Expression color, Expression backgroundColor, Expression opacity, Graphic fill) {
        return this.sf.createFill(color, backgroundColor, opacity, fill);
    }

    public String[] getWellKnownMarkNames() {
        return new String[]{"square", MARK_CIRCLE, MARK_TRIANGLE, MARK_STAR, MARK_CROSS, MARK_ARROW, MARK_X};
    }

    public Mark createMark(String wellKnownName) {
        Mark mark = this.sf.createMark();
        mark.setWellKnownName(this.literalExpression(wellKnownName));
        return mark;
    }

    public Mark createMark(String wellKnownName, Color fillColor, Color borderColor, double borderWidth) {
        Mark mark = this.sf.createMark();
        mark.setWellKnownName(this.literalExpression(wellKnownName));
        mark.setStroke(this.createStroke(borderColor, borderWidth));
        mark.setFill(this.createFill(fillColor));
        return mark;
    }

    public Mark createMark(String wellKnownName, Color borderColor, double borderWidth) {
        Mark mark = this.sf.createMark();
        mark.setWellKnownName(this.literalExpression(wellKnownName));
        mark.setStroke(this.createStroke(borderColor, borderWidth));
        return mark;
    }

    public Mark createMark(String wellKnownName, Color fillColor) {
        Mark mark = this.sf.createMark();
        mark.setWellKnownName(this.literalExpression(wellKnownName));
        mark.setFill(this.createFill(fillColor, 1.0));
        mark.setStroke(null);
        return mark;
    }

    public Mark createMark(String wellKnownName, Fill fill, Stroke stroke) {
        Mark mark = this.sf.createMark();
        mark.setWellKnownName(this.literalExpression(wellKnownName));
        mark.setStroke(stroke);
        mark.setFill(fill);
        return mark;
    }

    public Mark createMark(Expression wellKnownName, Fill fill, Stroke stroke) {
        Mark mark = this.sf.createMark();
        mark.setWellKnownName(wellKnownName);
        mark.setStroke(stroke);
        mark.setFill(fill);
        return mark;
    }

    public ExternalGraphic createExternalGraphic(String uri, String format) {
        return this.sf.createExternalGraphic(uri, format);
    }

    public ExternalGraphic createExternalGraphic(URL url, String format) {
        return this.sf.createExternalGraphic(url, format);
    }

    public Graphic createGraphic() {
        Graphic gr = this.sf.getDefaultGraphic();
        Mark mark = this.createMark("square", Color.decode("#808080"), Color.BLACK, 1.0);
        gr.setMarks(new Mark[]{mark});
        gr.setSize(this.literalExpression(6.0));
        return gr;
    }

    public Graphic createGraphic(ExternalGraphic externalGraphic, Mark mark, Symbol symbol) {
        Graphic gr = this.sf.getDefaultGraphic();
        if (symbol != null) {
            gr.setSymbols(new Symbol[]{symbol});
        } else {
            gr.setSymbols(new Symbol[0]);
        }
        if (externalGraphic != null) {
            gr.setExternalGraphics(new ExternalGraphic[]{externalGraphic});
        }
        if (mark != null) {
            gr.setMarks(new Mark[]{mark});
        } else {
            gr.setMarks(new Mark[0]);
        }
        return gr;
    }

    public Graphic createGraphic(ExternalGraphic externalGraphic, Mark mark, Symbol symbol, double opacity, double size, double rotation) {
        ExternalGraphic[] egs = null;
        Mark[] marks = null;
        Symbol[] symbols = null;
        if (externalGraphic != null) {
            egs = new ExternalGraphic[]{externalGraphic};
        }
        if (mark != null) {
            marks = new Mark[]{mark};
        }
        if (symbol != null) {
            symbols = new Symbol[]{symbol};
        }
        return this.createGraphic(egs, marks, symbols, this.literalExpression(opacity), this.literalExpression(size), this.literalExpression(rotation));
    }

    public Graphic createGraphic(ExternalGraphic[] externalGraphics, Mark[] marks, Symbol[] symbols, double opacity, double size, double rotation) {
        return this.createGraphic(externalGraphics, marks, symbols, this.literalExpression(opacity), this.literalExpression(size), this.literalExpression(rotation));
    }

    public Graphic createGraphic(ExternalGraphic[] externalGraphics, Mark[] marks, Symbol[] symbols, Expression opacity, Expression size, Expression rotation) {
        Symbol[] s;
        Mark[] m;
        ExternalGraphic[] exg = externalGraphics;
        if (exg == null) {
            exg = new ExternalGraphic[]{};
        }
        if ((m = marks) == null) {
            m = new Mark[]{};
        }
        if ((s = symbols) == null) {
            s = new Symbol[]{};
        }
        return this.sf.createGraphic(exg, m, s, opacity, size, rotation);
    }

    public AnchorPoint createAnchorPoint(double x, double y) {
        return this.sf.createAnchorPoint(this.literalExpression(x), this.literalExpression(y));
    }

    public AnchorPoint createAnchorPoint(Expression x, Expression y) {
        return this.sf.createAnchorPoint(x, y);
    }

    public Displacement createDisplacement(double x, double y) {
        return this.sf.createDisplacement(this.literalExpression(x), this.literalExpression(y));
    }

    public Displacement createDisplacement(Expression x, Expression y) {
        return this.sf.createDisplacement(x, y);
    }

    public PointPlacement createPointPlacement() {
        return this.sf.getDefaultPointPlacement();
    }

    public PointPlacement createPointPlacement(double anchorX, double anchorY, double rotation) {
        AnchorPoint anchorPoint = this.createAnchorPoint(anchorX, anchorY);
        return this.sf.createPointPlacement(anchorPoint, null, this.literalExpression(rotation));
    }

    public PointPlacement createPointPlacement(double anchorX, double anchorY, double displacementX, double displacementY, double rotation) {
        AnchorPoint anchorPoint = this.createAnchorPoint(anchorX, anchorY);
        Displacement displacement = this.createDisplacement(displacementX, displacementY);
        return this.sf.createPointPlacement(anchorPoint, displacement, this.literalExpression(rotation));
    }

    public PointPlacement createPointPlacement(AnchorPoint anchorPoint, Displacement displacement, Expression rotation) {
        return this.sf.createPointPlacement(anchorPoint, displacement, rotation);
    }

    public LinePlacement createLinePlacement(double offset) {
        return this.sf.createLinePlacement(this.literalExpression(offset));
    }

    public LinePlacement createLinePlacement(Expression offset) {
        return this.sf.createLinePlacement(offset);
    }

    public Font createFont(java.awt.Font font) {
        Expression family = this.literalExpression(font.getFamily());
        Expression weight = font.isBold() ? this.literalExpression(FONT_WEIGHT_BOLD) : this.literalExpression("normal");
        Expression style = font.isItalic() ? this.literalExpression(FONT_STYLE_ITALIC) : this.literalExpression("normal");
        return this.sf.createFont(family, style, weight, this.literalExpression(font.getSize2D()));
    }

    public Font createFont(String fontFamily, double fontSize) {
        Expression family = this.literalExpression(fontFamily);
        Expression style = this.literalExpression("normal");
        Expression weight = this.literalExpression("normal");
        return this.sf.createFont(family, style, weight, this.literalExpression(fontSize));
    }

    public Font createFont(String fontFamily, boolean italic, boolean bold, double fontSize) {
        Expression family = this.literalExpression(fontFamily);
        Expression weight = bold ? this.literalExpression(FONT_WEIGHT_BOLD) : this.literalExpression("normal");
        Expression style = italic ? this.literalExpression(FONT_STYLE_ITALIC) : this.literalExpression("normal");
        return this.sf.createFont(family, style, weight, this.literalExpression(fontSize));
    }

    public Font createFont(Expression fontFamily, Expression fontStyle, Expression fontWeight, Expression fontSize) {
        return this.sf.createFont(fontFamily, fontStyle, fontWeight, fontSize);
    }

    public Halo createHalo() {
        return this.sf.createHalo(this.createFill(Color.WHITE), this.literalExpression(1));
    }

    public Halo createHalo(Color color, double radius) {
        return this.sf.createHalo(this.createFill(color), this.literalExpression(radius));
    }

    public Halo createHalo(Color color, double opacity, double radius) {
        return this.sf.createHalo(this.createFill(color, opacity), this.literalExpression(radius));
    }

    public Halo createHalo(Fill fill, double radius) {
        return this.sf.createHalo(fill, this.literalExpression(radius));
    }

    public Halo createHalo(Fill fill, Expression radius) {
        return this.sf.createHalo(fill, radius);
    }

    public LineSymbolizer createLineSymbolizer() {
        return this.sf.getDefaultLineSymbolizer();
    }

    public LineSymbolizer createLineSymbolizer(double width) {
        return this.createLineSymbolizer(this.createStroke(width), null);
    }

    public LineSymbolizer createLineSymbolizer(Color color) {
        return this.createLineSymbolizer(this.createStroke(color), null);
    }

    public LineSymbolizer createLineSymbolizer(Color color, double width) {
        return this.createLineSymbolizer(this.createStroke(color, width), null);
    }

    public LineSymbolizer createLineSymbolizer(Color color, double width, String geometryPropertyName) {
        return this.createLineSymbolizer(this.createStroke(color, width), geometryPropertyName);
    }

    public LineSymbolizer createLineSymbolizer(Stroke stroke) {
        return this.sf.createLineSymbolizer(stroke, null);
    }

    public LineSymbolizer createLineSymbolizer(Stroke stroke, String geometryPropertyName) {
        return this.sf.createLineSymbolizer(stroke, geometryPropertyName);
    }

    public PolygonSymbolizer createPolygonSymbolizer() {
        PolygonSymbolizer ps = this.sf.createPolygonSymbolizer();
        ps.setFill(this.createFill());
        ps.setStroke(this.createStroke());
        return ps;
    }

    public PolygonSymbolizer createPolygonSymbolizer(Color fillColor) {
        return this.createPolygonSymbolizer(null, this.createFill(fillColor));
    }

    public PolygonSymbolizer createPolygonSymbolizer(Color fillColor, Color borderColor, double borderWidth) {
        return this.createPolygonSymbolizer(this.createStroke(borderColor, borderWidth), this.createFill(fillColor));
    }

    public PolygonSymbolizer createPolygonSymbolizer(Color borderColor, double borderWidth) {
        return this.createPolygonSymbolizer(this.createStroke(borderColor, borderWidth), null);
    }

    public PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill) {
        return this.createPolygonSymbolizer(stroke, fill, null);
    }

    public PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill, String geometryPropertyName) {
        return this.sf.createPolygonSymbolizer(stroke, fill, geometryPropertyName);
    }

    public PointSymbolizer createPointSymbolizer() {
        return this.sf.getDefaultPointSymbolizer();
    }

    public PointSymbolizer createPointSymbolizer(Graphic graphic) {
        PointSymbolizer ps = this.sf.createPointSymbolizer();
        ps.setGraphic(graphic);
        return ps;
    }

    public PointSymbolizer createPointSymbolizer(Graphic graphic, String geometryPropertyName) {
        return this.sf.createPointSymbolizer(graphic, geometryPropertyName);
    }

    public TextSymbolizer createTextSymbolizer() {
        TextSymbolizer ts = this.sf.createTextSymbolizer();
        ts.setFill(this.createFill(Color.BLACK));
        ts.setLabel(this.literalExpression("Label"));
        ts.setFonts(new Font[]{this.createFont("Lucida Sans", 10.0)});
        return ts;
    }

    public TextSymbolizer createTextSymbolizer(FeatureSchema fs) throws IllegalFilterException {
        int geomIndex = fs.getGeometryIndex();
        int attrNoGeomIndex = 0;
        if (geomIndex != 0 || fs.getAttributeCount() != 1) {
            attrNoGeomIndex = 1;
        }
        return this.createTextSymbolizer(this.createFill(Color.BLACK), new Font[]{this.createFont("Lucida Sans", 10.0)}, null, this.attributeExpression(fs.getAttributeName(attrNoGeomIndex)), null, null);
    }

    public TextSymbolizer createTextSymbolizer(Color color, Font font, String attributeName) throws IllegalFilterException {
        return this.createTextSymbolizer(this.createFill(color), new Font[]{font}, null, this.attributeExpression(attributeName), null, null);
    }

    public TextSymbolizer createTextSymbolizer(Color color, Font[] fonts, String attributeName) throws IllegalFilterException {
        return this.createTextSymbolizer(this.createFill(color), fonts, null, this.attributeExpression(attributeName), null, null);
    }

    public TextSymbolizer createStaticTextSymbolizer(Color color, Font font, String label) {
        return this.createTextSymbolizer(this.createFill(color), new Font[]{font}, null, this.literalExpression(label), null, null);
    }

    public TextSymbolizer createStaticTextSymbolizer(Color color, Font[] fonts, String label) {
        return this.createTextSymbolizer(this.createFill(color), fonts, null, this.literalExpression(label), null, null);
    }

    public TextSymbolizer createTextSymbolizer(Fill fill, Font[] fonts, Halo halo, Expression label, LabelPlacement labelPlacement, String geometryPropertyName) {
        TextSymbolizer ts = this.sf.createTextSymbolizer();
        if (fill != null) {
            ts.setFill(fill);
        }
        if (halo != null) {
            ts.setHalo(halo);
        }
        if (label != null) {
            ts.setLabel(label);
        }
        if (labelPlacement != null) {
            ts.setLabelPlacement(labelPlacement);
        }
        if (geometryPropertyName != null) {
            ts.setGeometryPropertyName(geometryPropertyName);
        }
        if (fonts != null) {
            ts.setFonts(fonts);
        }
        return ts;
    }

    public FeatureTypeStyle createFeatureTypeStyle(Symbolizer symbolizer) {
        return this.createFeatureTypeStyle(null, symbolizer, Double.NaN, Double.NaN);
    }

    public Rule createRule(Symbolizer symbolizer) {
        return this.createRule(symbolizer, Double.NaN, Double.NaN);
    }

    public Rule createRule(Symbolizer[] symbolizers) {
        return this.createRule(symbolizers, Double.NaN, Double.NaN);
    }

    public Rule createRule(Symbolizer symbolizer, double minScaleDenominator, double maxScaleDenominator) {
        return this.createRule(new Symbolizer[]{symbolizer}, Double.NaN, Double.NaN);
    }

    public Rule createRule(Symbolizer[] symbolizers, double minScaleDenominator, double maxScaleDenominator) {
        Rule r = this.sf.createRule();
        r.setSymbolizers(symbolizers);
        if (!Double.isNaN(maxScaleDenominator)) {
            r.setMaxScaleDenominator(maxScaleDenominator);
        } else {
            r.setMaxScaleDenominator(Double.MAX_VALUE);
        }
        if (!Double.isNaN(minScaleDenominator)) {
            r.setMinScaleDenominator(minScaleDenominator);
        } else {
            r.setMinScaleDenominator(0.0);
        }
        return r;
    }

    public FeatureTypeStyle createFeatureTypeStyle(Symbolizer symbolizer, double minScaleDenominator, double maxScaleDenominator) {
        return this.createFeatureTypeStyle(null, symbolizer, minScaleDenominator, maxScaleDenominator);
    }

    public FeatureTypeStyle createFeatureTypeStyle(Symbolizer[] symbolizers, double minScaleDenominator, double maxScaleDenominator) {
        return this.createFeatureTypeStyle(null, symbolizers, minScaleDenominator, maxScaleDenominator);
    }

    public FeatureTypeStyle createFeatureTypeStyle(String featureTypeStyleName, Symbolizer symbolizer) {
        return this.createFeatureTypeStyle(featureTypeStyleName, symbolizer, Double.NaN, Double.NaN);
    }

    public FeatureTypeStyle createFeatureTypeStyle(String featureTypeStyleName, Symbolizer[] symbolizers) {
        return this.createFeatureTypeStyle(featureTypeStyleName, symbolizers, Double.NaN, Double.NaN);
    }

    public FeatureTypeStyle createFeatureTypeStyle(String featureTypeStyleName, Symbolizer symbolizer, double minScaleDenominator, double maxScaleDenominator) {
        return this.createFeatureTypeStyle(featureTypeStyleName, new Symbolizer[]{symbolizer}, minScaleDenominator, maxScaleDenominator);
    }

    public FeatureTypeStyle createFeatureTypeStyle(String featureTypeStyleName, Symbolizer[] symbolizers, double minScaleDenominator, double maxScaleDenominator) {
        Rule r = this.createRule(symbolizers, minScaleDenominator, maxScaleDenominator);
        FeatureTypeStyle fts = this.sf.createFeatureTypeStyle();
        fts.setRules(new Rule[]{r});
        if (featureTypeStyleName != null) {
            fts.setFeatureTypeName(featureTypeStyleName);
        }
        return fts;
    }

    public FeatureTypeStyle createFeatureTypeStyle(String featureTypeStyleName, Rule r) {
        FeatureTypeStyle fts = this.sf.createFeatureTypeStyle();
        fts.setRules(new Rule[]{r});
        if (featureTypeStyleName != null) {
            fts.setFeatureTypeName(featureTypeStyleName);
        }
        return fts;
    }

    public FeatureTypeStyle createFeatureTypeStyle(String featureTypeStyleName, Rule[] rules) {
        FeatureTypeStyle fts = this.sf.createFeatureTypeStyle();
        fts.setRules(rules);
        if (featureTypeStyleName != null) {
            fts.setFeatureTypeName(featureTypeStyleName);
        }
        return fts;
    }

    public Style createStyle(Symbolizer symbolizer) {
        return this.createStyle(null, symbolizer, Double.NaN, Double.NaN);
    }

    public Style createStyle(Symbolizer symbolizer, double minScaleDenominator, double maxScaleDenominator) {
        return this.createStyle(null, symbolizer, minScaleDenominator, maxScaleDenominator);
    }

    public Style createStyle(String featureTypeStyleName, Symbolizer symbolizer) {
        return this.createStyle(featureTypeStyleName, symbolizer, Double.NaN, Double.NaN);
    }

    public Style createStyle(String featureTypeStyleName, Symbolizer symbolizer, double minScaleDenominator, double maxScaleDenominator) {
        FeatureTypeStyle fts = this.createFeatureTypeStyle(featureTypeStyleName, symbolizer, minScaleDenominator, maxScaleDenominator);
        Style style = this.sf.createStyle();
        style.addFeatureTypeStyle(fts);
        style.setSelectedFeatureTypeStyle(fts);
        return style;
    }

    public Style createStyle() {
        return this.sf.createStyle();
    }

    public Expression colorExpression(Color color) {
        if (color == null) {
            return null;
        }
        String redCode = Integer.toHexString(color.getRed());
        String greenCode = Integer.toHexString(color.getGreen());
        String blueCode = Integer.toHexString(color.getBlue());
        if (redCode.length() == 1) {
            redCode = "0" + redCode;
        }
        if (greenCode.length() == 1) {
            greenCode = "0" + greenCode;
        }
        if (blueCode.length() == 1) {
            blueCode = "0" + blueCode;
        }
        String colorCode = "#" + redCode + greenCode + blueCode;
        return this.ff.createLiteralExpression(colorCode.toUpperCase());
    }

    public Expression literalExpression(double value) {
        return this.ff.createLiteralExpression(value);
    }

    public Expression literalExpression(int value) {
        return this.ff.createLiteralExpression(value);
    }

    public Expression literalExpression(String value) {
        LiteralExpression result = null;
        if (value != null) {
            result = this.ff.createLiteralExpression(value);
        }
        return result;
    }

    public Expression literalExpression(Object value) throws IllegalFilterException {
        LiteralExpression result = null;
        if (value != null) {
            result = this.ff.createLiteralExpression(value);
        }
        return result;
    }

    public Expression attributeExpression(String attributeName) throws IllegalFilterException {
        AttributeExpression attribute = this.ff.createAttributeExpression((FeatureSchema)null);
        attribute.setAttributePath(attributeName);
        return attribute;
    }

    public Style buildClassifiedStyle(FeatureCollection fc, String name, String[] colors, FeatureSchema schema) throws IllegalFilterException {
        double[] values;
        String geomName;
        AttributeExpression value;
        block12: {
            value = this.ff.createAttributeExpression(schema, name);
            geomName = schema.getAttributeName(schema.getGeometryIndex());
            int size = 0;
            try {
                size = fc.size();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            values = new double[size];
            FeatureIterator it = null;
            try {
                try {
                    it = fc.iterator();
                    int count = 0;
                    while (it.hasNext()) {
                        Feature f = it.next();
                        values[count++] = ((Number)f.getAttribute(name)).doubleValue();
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    if (it != null) {
                        it.close();
                    }
                    break block12;
                }
            }
            catch (Throwable throwable) {
                if (it != null) {
                    it.close();
                }
                throw throwable;
            }
            if (it != null) {
                it.close();
            }
        }
        EqualClasses ec = new EqualClasses(colors.length, values);
        double[] breaks = ec.getBreaks();
        Style ret = this.createStyle();
        Rule[] rules = new Rule[colors.length + 1];
        CompareFilter cf1 = this.ff.createCompareFilter((short)15);
        cf1.addLeftValue(value);
        cf1.addRightValue(this.ff.createLiteralExpression(breaks[0]));
        LOGGER.debug((Object)cf1.toString());
        rules[0] = this.sf.createRule();
        rules[0].setFilter(cf1);
        Color c = this.createColor(colors[0]);
        PolygonSymbolizer symb1 = this.createPolygonSymbolizer(c, Color.black, 1.0);
        rules[0].setSymbolizers(new Symbolizer[]{symb1});
        LOGGER.debug((Object)("added low class " + breaks[0] + " " + colors[0]));
        int i = 1;
        while (i < colors.length - 1) {
            rules[i] = this.sf.createRule();
            BetweenFilter cf = this.ff.createBetweenFilter();
            cf.addLeftValue(this.ff.createLiteralExpression(breaks[i - 1]));
            cf.addRightValue(this.ff.createLiteralExpression(breaks[i]));
            cf.addMiddleValue(value);
            LOGGER.debug((Object)cf.toString());
            c = this.createColor(colors[i]);
            LOGGER.debug((Object)("color " + c.toString()));
            PolygonSymbolizer symb = this.createPolygonSymbolizer(c, Color.black, 1.0);
            rules[i].setSymbolizers(new Symbolizer[]{symb});
            rules[i].setFilter(cf);
            LOGGER.debug((Object)("added class " + breaks[i - 1] + "->" + breaks[i] + " " + colors[i]));
            ++i;
        }
        CompareFilter cf2 = this.ff.createCompareFilter((short)18);
        cf2.addLeftValue(value);
        cf2.addRightValue(this.ff.createLiteralExpression(breaks[colors.length - 2]));
        LOGGER.debug((Object)cf2.toString());
        rules[colors.length - 1] = this.sf.createRule();
        rules[colors.length - 1].setFilter(cf2);
        rules[colors.length - 1].setName(geomName);
        c = this.createColor(colors[colors.length - 1]);
        PolygonSymbolizer symb2 = this.createPolygonSymbolizer(c, Color.black, 1.0);
        rules[colors.length - 1].setSymbolizers(new Symbolizer[]{symb2});
        LOGGER.debug((Object)("added upper class " + breaks[colors.length - 2] + "  " + colors[colors.length - 1]));
        rules[colors.length] = this.sf.createRule();
        PolygonSymbolizer elsePoly = this.createPolygonSymbolizer(Color.black, 1.0);
        rules[colors.length].setSymbolizers(new Symbolizer[]{elsePoly});
        rules[colors.length].setElseFilter(true);
        FeatureTypeStyle ft = this.sf.createFeatureTypeStyle(rules);
        ft.setFeatureTypeName("feature");
        ft.setName(name);
        ret.addFeatureTypeStyle(ft);
        return ret;
    }

    public Symbolizer buildSymbolizer(Color borderColor, double borderWidth, int geometryType) throws Exception {
        return this.buildSymbolizer(borderColor, null, borderWidth, geometryType);
    }

    public Symbolizer buildSymbolizer(Color borderColor, Color fillColor, double borderWidth, int geometryType) throws Exception {
        Symbolizer symb = null;
        switch (geometryType) {
            case 1: 
            case 8: {
                Mark mark = this.createMark("square", fillColor, borderColor, borderWidth);
                Graphic graphic = this.createGraphic(null, mark, null);
                graphic.setSize(new LiteralExpressionImpl(borderWidth));
                symb = this.createPointSymbolizer(graphic);
                break;
            }
            case 2: 
            case 3: {
                symb = this.createLineSymbolizer(borderColor, borderWidth);
                break;
            }
            case 4: 
            case 5: {
                symb = this.createPolygonSymbolizer(fillColor, borderColor, borderWidth);
                break;
            }
            default: {
                throw new Exception("Tipo de geometria desconocida " + geometryType);
            }
        }
        return symb;
    }

    public Filter buildFilterFromValue(String attrName, Object key) throws Exception {
        AttributeExpression attribute = this.ff.createAttributeExpression(attrName);
        LiteralExpression literal = this.ff.createLiteralExpression(key);
        CompareFilter filter = this.ff.createCompareFilter((short)14);
        filter.addLeftValue(attribute);
        filter.addRightValue(literal);
        return filter;
    }

    private Color createColor(String text) {
        int i = Integer.decode("0x" + text);
        return Color.decode("" + i);
    }

    public RasterSymbolizer createRasterSymbolizer() {
        return this.sf.getDefaultRasterSymbolizer();
    }

    public RasterSymbolizer createRasterSymbolizer(ColorMap colorMap, double opacity) {
        RasterSymbolizer rs = this.sf.getDefaultRasterSymbolizer();
        rs.setColorMap(colorMap);
        rs.setOpacity(this.literalExpression(opacity));
        return rs;
    }

    public ColorMap createColorMap(double[] quantities, Color[] colors, int type) {
        ColorMap colorMap = this.sf.createColorMap();
        colorMap.setType(type);
        if (quantities == null || colors == null || quantities.length != colors.length) {
            throw new IllegalArgumentException("Quantities and colors arrays should be not null and have the same size");
        }
        int i = 0;
        while (i < colors.length) {
            colorMap.addColorMapEntry(this.createColorMapEntry(quantities[i], colors[i]));
            ++i;
        }
        return colorMap;
    }

    private ColorMapEntry createColorMapEntry(double quantity, Color color) {
        ColorMapEntry entry = this.sf.createColorMapEntry();
        entry.setQuantity(this.literalExpression(quantity));
        entry.setColor(this.colorExpression(color));
        entry.setOpacity(this.literalExpression((double)color.getAlpha() / 255.0));
        entry.setLabel(String.valueOf(quantity));
        return entry;
    }

    public Gradient createLinearGradient(float startX, float startY, float endX, float endY, float[] fractions, Color[] colors, Gradient.GradientCycleMethod method) {
        return this.sf.createLinearGradient(startX, startY, endX, endY, fractions, colors, method);
    }

    public Gradient createRadialGradient(float centerX, float centerY, float radius, float[] fractions, Color[] colors, Gradient.GradientCycleMethod method) {
        return this.sf.createRadialGradient(centerX, centerY, radius, fractions, colors, method);
    }
}

