/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.gui.swing.sldeditor.util;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import org.apache.log4j.Logger;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Halo;
import org.saig.core.styling.LinePlacement;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.Mark;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.lang.I18N;

public class SymbolizerUtils
implements SLDEditor {
    private static final Logger LOGGER = Logger.getLogger(SymbolizerUtils.class);

    private SymbolizerUtils() {
    }

    public static SymbolizerEditor getDefaultSymbolizerEditor(FeatureSchema ft) {
        int type = ft.getGeometryType();
        if (type == 5 || type == 4) {
            return symbolizerEditorFactory.createPolygonSymbolizerEditor(ft);
        }
        if (type == 3 || type == 2) {
            return symbolizerEditorFactory.createLineSymbolizerEditor(ft);
        }
        if (type == 1 || type == 8) {
            return symbolizerEditorFactory.createPointSymbolizerEditor(ft);
        }
        throw new IllegalArgumentException(I18N.getString("org.saig.core.gui.swing.sldeditor.util.SymbolizerUtils.unsupported-geometry-type"));
    }

    public static Symbolizer getDefaultSymbolizer(FeatureSchema ft) {
        int type = ft.getGeometryType();
        if (type == 5 || type == 4) {
            return styleBuilder.createPolygonSymbolizer();
        }
        if (type == 3 || type == 2) {
            return styleBuilder.createLineSymbolizer();
        }
        if (type == 1 || type == 8) {
            return styleBuilder.createPointSymbolizer();
        }
        return styleBuilder.createRasterSymbolizer();
    }

    public static String getSymbolizerName(Symbolizer s) {
        if (s instanceof LineSymbolizer) {
            return I18N.getString("org.saig.core.gui.swing.sldeditor.util.SymbolizerUtils.line");
        }
        if (s instanceof PointSymbolizer) {
            return I18N.getString("org.saig.core.gui.swing.sldeditor.util.SymbolizerUtils.point");
        }
        if (s instanceof PolygonSymbolizer) {
            return I18N.getString("org.saig.core.gui.swing.sldeditor.util.SymbolizerUtils.polygon");
        }
        if (s instanceof TextSymbolizer) {
            return I18N.getString("org.saig.core.gui.swing.sldeditor.util.SymbolizerUtils.text");
        }
        throw new IllegalArgumentException(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.util.SymbolizerUtils.unsupported-symbolizer")) + s.getClass().getName());
    }

    public static SymbolizerEditor getSymbolizerEditor(Symbolizer s, FeatureSchema ft) {
        SymbolizerEditor symbolizerEditor = null;
        if (s instanceof LineSymbolizer) {
            symbolizerEditor = symbolizerEditorFactory.createLineSymbolizerEditor(ft);
        } else if (s instanceof PointSymbolizer) {
            symbolizerEditor = symbolizerEditorFactory.createPointSymbolizerEditor(ft);
        } else if (s instanceof PolygonSymbolizer) {
            symbolizerEditor = symbolizerEditorFactory.createPolygonSymbolizerEditor(ft);
        } else if (s instanceof TextSymbolizer) {
            symbolizerEditor = symbolizerEditorFactory.createTextSymbolizerEditor(ft);
        } else if (s instanceof RasterSymbolizer) {
            symbolizerEditor = symbolizerEditorFactory.createRasterSymbolizerEditor(ft);
        } else {
            throw new IllegalArgumentException(I18N.getString("org.saig.core.gui.swing.sldeditor.util.SymbolizerUtils.unsupported-symbolizer"));
        }
        symbolizerEditor.setSymbolizer(s);
        return symbolizerEditor;
    }

    public static Symbolizer getTextSymbolizer(FeatureSchema schema, String etq, int height, Color color) {
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        StyleFactory factory = StyleFactory.createStyleFactory();
        AttributeExpression label = null;
        try {
            label = filterFactory.createAttributeExpression(schema, etq);
        }
        catch (IllegalFilterException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        Font jumpFont = new JLabel().getFont();
        LiteralExpression fontFamily = filterFactory.createLiteralExpression(jumpFont.getFamily());
        boolean isBold = jumpFont.getStyle() == 1 || jumpFont.getStyle() > 2;
        boolean isItalic = jumpFont.getStyle() == 2 || jumpFont.getStyle() > 2;
        LiteralExpression fontWeight = null;
        fontWeight = isBold ? filterFactory.createLiteralExpression("bold") : filterFactory.createLiteralExpression("normal");
        LiteralExpression fontStyle = null;
        fontStyle = isItalic ? filterFactory.createLiteralExpression("italic") : filterFactory.createLiteralExpression("normal");
        LiteralExpression fontSize = filterFactory.createLiteralExpression(height);
        org.saig.core.styling.Font modelFont = factory.createFont(fontFamily, fontStyle, fontWeight, fontSize);
        Halo halo = null;
        LiteralExpression textColor = filterFactory.createLiteralExpression(Layer.decodeColor(color));
        Fill fillText = factory.createFill(textColor);
        LiteralExpression offset = filterFactory.createLiteralExpression(1);
        LinePlacement labelPlac = factory.createLinePlacement(offset);
        return factory.createTextSymbolizer(fillText, new org.saig.core.styling.Font[]{modelFont}, halo, label, labelPlac, null);
    }

    public static Symbolizer getPolygonSymbolizer(Color color, int size) {
        StyleFactory factory = StyleFactory.createStyleFactory();
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        BasicStroke jumpStroke = new BasicStroke(1.0f);
        int jAlpha = 255;
        float alpha = new Float((float)jAlpha / 255.0f).floatValue();
        Stroke stroke = SymbolizerUtils.getStroke(jumpStroke, alpha, color, size, filterFactory, factory);
        Fill fill = SymbolizerUtils.getFill(color, alpha, filterFactory, factory);
        return factory.createPolygonSymbolizer(stroke, fill, null);
    }

    public static Symbolizer getLineSymbolizer(Color color, int size) {
        StyleFactory factory = StyleFactory.createStyleFactory();
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        BasicStroke jumpStroke = new BasicStroke(1.0f);
        int jAlpha = 255;
        float alpha = new Float((float)jAlpha / 255.0f).floatValue();
        Stroke stroke = SymbolizerUtils.getStroke(jumpStroke, alpha, color, size, filterFactory, factory);
        return factory.createLineSymbolizer(stroke, null);
    }

    public static Symbolizer getPointSymbolizer(Color color, int thickness) {
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        StyleFactory factory = StyleFactory.createStyleFactory();
        BasicStroke jumpStroke = new BasicStroke(1.0f);
        int jAlpha = 255;
        float alpha = new Float((float)jAlpha / 255.0f).floatValue();
        Stroke stroke = SymbolizerUtils.getStroke(jumpStroke, alpha, color, thickness, filterFactory, factory);
        Fill fill = SymbolizerUtils.getFill(color, alpha, filterFactory, factory);
        LiteralExpression markType = filterFactory.createLiteralExpression("square");
        LiteralExpression markSize = filterFactory.createLiteralExpression(4);
        LiteralExpression markRotation = filterFactory.createLiteralExpression(0);
        Mark mark = factory.createMark(markType, stroke, fill, markSize, markRotation);
        LiteralExpression opacity = filterFactory.createLiteralExpression(alpha);
        Graphic graphic = factory.createGraphic(null, new Mark[]{mark}, null, opacity, markSize, markRotation);
        return factory.createPointSymbolizer(graphic, null);
    }

    private static Fill getFill(Color color, float opacity, FilterFactory filterFactory, StyleFactory factory) {
        LiteralExpression fillColor = filterFactory.createLiteralExpression(Layer.decodeColor(color));
        LiteralExpression fillOpacity = filterFactory.createLiteralExpression(opacity);
        return factory.createFill(fillColor, fillColor, fillOpacity, null);
    }

    private static Stroke getStroke(BasicStroke jumpStroke, float alpha, Color color, int lineWidth, FilterFactory filterFactory, StyleFactory factory) {
        LiteralExpression strokeColor = filterFactory.createLiteralExpression(Layer.decodeColor(color));
        LiteralExpression strokeWidth = filterFactory.createLiteralExpression(lineWidth);
        LiteralExpression strokeOpacity = filterFactory.createLiteralExpression(alpha);
        float[] dashArray = jumpStroke.getDashArray();
        int lineJoin_ = jumpStroke.getLineJoin();
        LiteralExpression lineJoin = null;
        if (lineJoin_ == 2) {
            lineJoin = filterFactory.createLiteralExpression("bevel");
        } else if (lineJoin_ == 0) {
            lineJoin = filterFactory.createLiteralExpression("mitre");
        } else if (lineJoin_ == 1) {
            lineJoin = filterFactory.createLiteralExpression("round");
        }
        LiteralExpression lineCap = null;
        int lineCap_ = jumpStroke.getEndCap();
        if (lineCap_ == 0) {
            lineCap = filterFactory.createLiteralExpression("butt");
        } else if (lineCap_ == 1) {
            lineCap = filterFactory.createLiteralExpression("round");
        } else if (lineCap_ == 2) {
            lineCap = filterFactory.createLiteralExpression("square");
        }
        LiteralExpression dashOffset = filterFactory.createLiteralExpression(jumpStroke.getDashPhase());
        return factory.createStroke(strokeColor, strokeWidth, strokeOpacity, lineJoin, lineCap, dashArray, dashOffset, null, null);
    }
}

