/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  javax.media.jai.util.Range
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.geotools.util.NumberRange
 */
package org.saig.core.renderer.style;

import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.styling.visitors.UpdateSizeValueStyleVisitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.media.jai.util.Range;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.geotools.util.NumberRange;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterAttributeExtractor;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.FilterUtil;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LabelExpressionUtil;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.relations.Relation;
import org.saig.core.renderer.style.SLDStyleFactory;
import org.saig.core.renderer.style.Style2D;
import org.saig.core.styling.Font;
import org.saig.core.styling.Halo;
import org.saig.core.styling.LabelPlacement;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.Mark;
import org.saig.core.styling.PointPlacement;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StyleAttributeExtractor;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;

public class RuleStyle {
    private static final Logger LOGGER = Logger.getLogger(RuleStyle.class);
    protected static final SLDStyleFactory styleFactory = new SLDStyleFactory();
    protected static final StyleFactory factory = StyleFactory.createStyleFactory();
    protected static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private List<Style2D> styles;
    private Map<Style2D, Symbolizer> symbols;
    private List<Boolean> squareStyles;
    private Filter filter;
    private boolean geometryFilter;
    private boolean hasPointSymbol;
    private boolean liteShape = false;
    private boolean hasFunctionExpressionWithGeometry = false;
    private RasterSymbolizer rasterSymbol;
    private List<TextSymbolizer> textSymbolizers;
    private List<String> labels;
    private Set<String> allLabels;
    private double factor;
    private List<IDecorator> decorators;

    public RuleStyle(Rule rule, NumberRange scaleRange, boolean loadSquare, double factor, double pxSize, Unit<Length> viewportLengthUnit) {
        this.factor = factor;
        this.initialize(rule, scaleRange, loadSquare, pxSize, viewportLengthUnit);
    }

    public RuleStyle(Rule rule, NumberRange scaleRange, boolean loadSquare, double factor, double pxSize, Unit<Length> viewportLengthUnit, FeatureSchema schema) {
        String pkAttrName;
        this.factor = factor;
        this.initialize(rule, scaleRange, loadSquare, pxSize, viewportLengthUnit);
        this.labels.addAll(this.getLabelsFromAttrCalculate(schema));
        this.labels.addAll(this.getLabelsOfAttrCalculateFromFilter(this.filter, schema));
        this.allLabels.addAll(this.getLabelsFromFilter(this.filter, schema));
        String geomAttrName = schema.getAttributeName(schema.getGeometryIndex());
        this.labels.remove(geomAttrName);
        this.allLabels.remove(geomAttrName);
        if (schema.getPrimaryKeyIndex() != -1 && StringUtils.isNotEmpty((String)(pkAttrName = schema.getAttributeName(schema.getPrimaryKeyIndex())))) {
            this.labels.remove(pkAttrName);
            this.allLabels.remove(pkAttrName);
        }
    }

    private void initialize(Rule rule, NumberRange scaleRange, boolean loadSquare, double pxSize, Unit<Length> viewportLengthUnit) {
        this.styles = new ArrayList<Style2D>();
        this.symbols = new HashMap<Style2D, Symbolizer>();
        this.squareStyles = new ArrayList<Boolean>();
        this.textSymbolizers = new ArrayList<TextSymbolizer>();
        this.labels = new ArrayList<String>();
        this.allLabels = new HashSet<String>();
        this.decorators = new ArrayList<IDecorator>();
        Symbolizer[] ruleSymbols = rule.getSymbolizers();
        int i = 0;
        while (i < ruleSymbols.length) {
            if (ruleSymbols[i] != null && ruleSymbols[i].isActive()) {
                if (ruleSymbols[i] instanceof TextSymbolizer) {
                    LabelPlacement placement;
                    TextSymbolizer textSymbol = (TextSymbolizer)ruleSymbols[i];
                    String geomColName = textSymbol.getGeometryPropertyName();
                    if (geomColName != null) {
                        this.labels.add(geomColName);
                    }
                    List<String> labelsOfExpression = LabelExpressionUtil.getLabelsFromExpression(textSymbol.getLabel().toString());
                    this.labels.addAll(labelsOfExpression);
                    if (textSymbol.getHeightAttribute() != null) {
                        List<String> labelsOfHeight = LabelExpressionUtil.getLabelsFromExpression(textSymbol.getHeightAttribute().toString());
                        this.labels.addAll(labelsOfHeight);
                    }
                    if (textSymbol.getAttributeRotation() != null) {
                        List<String> labelsOfRotation = LabelExpressionUtil.getLabelsFromExpression(textSymbol.getAttributeRotation().toString());
                        this.labels.addAll(labelsOfRotation);
                    }
                    if ((placement = textSymbol.getLabelPlacement()) != null && placement instanceof PointPlacement && ((PointPlacement)placement).getRotation() != null) {
                        FilterAttributeExtractor attExtractor = new FilterAttributeExtractor();
                        attExtractor.visit(((PointPlacement)placement).getRotation());
                        Set<String> labelsOfRotation = attExtractor.getAttributeNameSet();
                        this.labels.addAll(labelsOfRotation);
                    }
                    if (this.factor > 1.0) {
                        Font originalFont = textSymbol.getFonts()[0];
                        Double size = new Double(((Number)originalFont.getFontSize().getValue(null)).doubleValue() * this.factor);
                        LiteralExpression fontSize = null;
                        try {
                            fontSize = filterFactory.createLiteralExpression(size);
                        }
                        catch (IllegalFilterException e) {
                            LOGGER.error((Object)"", (Throwable)e);
                        }
                        Font modelFont = factory.createFont(originalFont.getFontFamily(), originalFont.getFontStyle(), originalFont.getFontWeight(), fontSize);
                        Halo halo = null;
                        if (textSymbol.getHalo() != null) {
                            Double size_ = new Double(((Number)textSymbol.getHalo().getRadius().getValue(null)).doubleValue() * this.factor);
                            LiteralExpression haloSize = null;
                            try {
                                haloSize = filterFactory.createLiteralExpression(size_);
                            }
                            catch (IllegalFilterException e) {
                                LOGGER.error((Object)"", (Throwable)e);
                            }
                            halo = factory.createHalo(textSymbol.getHalo().getFill(), haloSize);
                        }
                        TextSymbolizer newTextSymbol = factory.createTextSymbolizer(textSymbol.getFill(), new Font[]{modelFont}, halo, textSymbol.getLabel(), textSymbol.getLabelPlacement(), null);
                        newTextSymbol.setAttributeRotation(textSymbol.getAttributeRotation());
                        newTextSymbol.setHeightAttribute(textSymbol.getHeightAttribute());
                        if (textSymbol.isScale()) {
                            newTextSymbol.setScale(true);
                            newTextSymbol.setScaleMinValue(textSymbol.getScaleMinValue());
                            newTextSymbol.setScaleMaxValue(textSymbol.getScaleMaxValue());
                        }
                        this.textSymbolizers.add(newTextSymbol);
                    } else {
                        this.textSymbolizers.add((TextSymbolizer)ruleSymbols[i]);
                    }
                    this.liteShape = true;
                } else if (ruleSymbols[i] instanceof RasterSymbolizer) {
                    this.rasterSymbol = (RasterSymbolizer)ruleSymbols[i];
                } else {
                    Symbolizer cloneSymbol = null;
                    UpdateSizeValueStyleVisitor visitor = new UpdateSizeValueStyleVisitor(ruleSymbols[i], this.factor, pxSize, viewportLengthUnit);
                    if (ruleSymbols[i] instanceof PointSymbolizer) {
                        PointSymbolizer pointSymbol = (PointSymbolizer)visitor.updateSymbolizer();
                        StyleAttributeExtractor sae = new StyleAttributeExtractor();
                        sae.visit(pointSymbol);
                        this.labels.addAll(sae.getAttributeNameSet());
                        if (pointSymbol.getGraphic().getExternalGraphics() == null) {
                            Mark[] marcas = pointSymbol.getGraphic().getMarks();
                            if (marcas.length > 0) {
                                if (this.checkAllSimpleSquareMarks(marcas)) {
                                    if (loadSquare) {
                                        this.liteShape = true;
                                    }
                                    this.hasPointSymbol = true;
                                    this.squareStyles.add(new Boolean(true));
                                } else {
                                    this.hasPointSymbol = true;
                                    this.liteShape = true;
                                    this.squareStyles.add(new Boolean(false));
                                }
                            } else {
                                this.hasPointSymbol = true;
                                this.liteShape = true;
                                this.squareStyles.add(new Boolean(false));
                            }
                        } else {
                            this.hasPointSymbol = true;
                            this.liteShape = true;
                            this.squareStyles.add(new Boolean(false));
                        }
                        cloneSymbol = pointSymbol;
                        this.decorators.addAll(((PointSymbolizer)ruleSymbols[i]).getDecorators());
                    } else {
                        this.squareStyles.add(new Boolean(false));
                        if (ruleSymbols[i] instanceof LineSymbolizer) {
                            LineSymbolizer lineSymbolizer = (LineSymbolizer)visitor.updateSymbolizer();
                            this.decorators.addAll(lineSymbolizer.getDecorators());
                            cloneSymbol = lineSymbolizer;
                        } else if (ruleSymbols[i] instanceof PolygonSymbolizer) {
                            PolygonSymbolizer polygonSymbolizer = (PolygonSymbolizer)visitor.updateSymbolizer();
                            this.decorators.addAll(polygonSymbolizer.getDecorators());
                            cloneSymbol = polygonSymbolizer;
                        }
                    }
                    Style2D style = null;
                    if (cloneSymbol != null) {
                        style = styleFactory.createStyle(null, cloneSymbol, (Range)scaleRange);
                        this.symbols.put(style, cloneSymbol);
                    } else {
                        style = styleFactory.createStyle(null, ruleSymbols[i], (Range)scaleRange);
                        this.symbols.put(style, ruleSymbols[i]);
                    }
                    this.styles.add(style);
                }
            }
            ++i;
        }
        this.filter = rule.getFilter();
        if (this.filter != null && FilterUtil.hasFunctionExpressionWithGeometry(this.filter)) {
            this.hasFunctionExpressionWithGeometry = true;
        }
        this.geometryFilter = rule.isGeometryFilter();
        this.allLabels.addAll(this.labels);
    }

    private boolean checkAllSimpleSquareMarks(Mark[] marksToCheck) {
        boolean ok = true;
        int i = 0;
        while (i < marksToCheck.length && ok) {
            Mark currentMark = marksToCheck[i];
            ok = currentMark.getWellKnownName().toString().equalsIgnoreCase("square") && currentMark.getFill() != null && currentMark.getFill().getGraphicFill() == null;
            Stroke markStroke = currentMark.getStroke();
            if (ok) {
                boolean bl = ok = markStroke == null || markStroke != null && markStroke.getColor() != null && currentMark.getFill().getColor() != null && currentMark.getFill().getColor().equals(markStroke.getColor());
            }
            if (ok && markStroke != null) {
                ok = markStroke.getDashArray() == null && markStroke.getGraphicFill() == null && markStroke.getGraphicStroke() == null;
            }
            ++i;
        }
        return ok;
    }

    public List<Style2D> getStyles() {
        return this.styles;
    }

    public Filter getFilter() {
        return this.filter;
    }

    public boolean isGeometryFilter() {
        return this.geometryFilter;
    }

    public List<TextSymbolizer> getTextSymbolizers() {
        return this.textSymbolizers;
    }

    public boolean hasPointSymbolizers() {
        return this.hasPointSymbol;
    }

    public boolean hasFunctionExpressionsWithGeometry() {
        return this.hasFunctionExpressionWithGeometry;
    }

    public boolean hasTextSymbolizers() {
        return !this.textSymbolizers.isEmpty();
    }

    public boolean loadLitleShape(Style2D style) {
        int index = this.styles.lastIndexOf(style);
        if (index == -1) {
            return true;
        }
        if (CollectionUtils.isNotEmpty(this.labels) && this.hasPointSymbol) {
            return false;
        }
        return this.squareStyles.get(index);
    }

    public boolean isLiteShape() {
        return this.liteShape;
    }

    public RasterSymbolizer getRasterSymbol() {
        return this.rasterSymbol;
    }

    public Symbolizer getSymbol(Style2D style) {
        return this.symbols.get(style);
    }

    public void setFilter(Filter filter, FeatureSchema schema) {
        this.filter = filter;
        this.labels.addAll(this.getLabelsOfAttrCalculateFromFilter(filter, schema));
        this.allLabels.addAll(this.getLabelsFromFilter(filter, schema));
    }

    public List<String> getLabels() {
        return this.labels;
    }

    public Set<String> getAllLabels() {
        return this.allLabels;
    }

    private Set<String> getLabelsFromAttrCalculate(FeatureSchema schema) {
        HashSet<String> labels = new HashSet<String>();
        for (String label : this.getLabels()) {
            Attribute attr = schema.getAttribute(label);
            if (attr == null || !attr.isCalculated()) continue;
            Relation<?> rel = ((AttributeCalculate)attr).getRelation();
            labels.add(rel.getSourceAttribute());
        }
        return labels;
    }

    private Set<String> getLabelsFromFilter(Filter filter, FeatureSchema schema) {
        String[] comparators = new String[]{" =", " >=", " <=", " <", " >", " !=", " IS", " LIKE"};
        HashSet<String> labels = new HashSet<String>();
        if (filter == null) {
            return labels;
        }
        String filterStr = filter.toString();
        int i = 0;
        while (i < schema.getAttributeCount()) {
            int j;
            boolean check;
            String name;
            Attribute attr = schema.getAttribute(i);
            if (i != schema.getGeometryIndex() && !attr.isCalculated()) {
                name = attr.getName();
                check = false;
                j = 0;
                while (j < comparators.length && !check) {
                    if (filterStr.indexOf(String.valueOf(name) + comparators[j]) != -1) {
                        labels.add(name);
                        check = true;
                    }
                    ++j;
                }
            } else if (attr.isCalculated()) {
                name = attr.getName();
                check = false;
                j = 0;
                while (j < comparators.length && !check) {
                    if (filterStr.indexOf(String.valueOf(name) + comparators[j]) != -1) {
                        labels.add(((AttributeCalculate)attr).getRelation().getSourceAttribute());
                        check = true;
                    }
                    ++j;
                }
            }
            ++i;
        }
        return labels;
    }

    private Set<String> getLabelsOfAttrCalculateFromFilter(Filter filter, FeatureSchema schema) {
        String[] comparators = new String[]{" =", " >=", " <=", " <", " >", " !=", " IS", " LIKE"};
        HashSet<String> labels = new HashSet<String>();
        if (filter == null) {
            return labels;
        }
        String filterStr = filter.toString();
        int i = 0;
        while (i < schema.getAttributeCount()) {
            Attribute attr = schema.getAttribute(i);
            if (i != schema.getGeometryIndex() && attr.isCalculated()) {
                String name = attr.getName();
                boolean check = false;
                int j = 0;
                while (j < comparators.length && !check) {
                    if (filterStr.indexOf(String.valueOf(name) + comparators[j]) != -1) {
                        labels.add(name);
                        labels.add(((AttributeCalculate)attr).getRelation().getSourceAttribute());
                        check = true;
                    }
                    ++j;
                }
            }
            ++i;
        }
        return labels;
    }

    public double getFactor() {
        return this.factor;
    }

    public List<IDecorator> getDecorators() {
        return this.decorators;
    }
}

