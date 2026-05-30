/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  javax.media.jai.util.Range
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.renderer.style;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import es.kosmo.core.renderer.style.IconStyle2D;
import es.kosmo.core.styling.Gradient;
import es.kosmo.core.styling.LinearGradientImpl;
import es.kosmo.core.styling.RadialGradientImpl;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.media.jai.util.Range;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.renderer.lite.CustomGlyphRenderer;
import org.saig.core.renderer.lite.GlyphRenderer;
import org.saig.core.renderer.lite.SVGGlyphRenderer;
import org.saig.core.renderer.style.DynamicLineStyle2D;
import org.saig.core.renderer.style.DynamicPolygonStyle2D;
import org.saig.core.renderer.style.DynamicSymbolFactoryFinder;
import org.saig.core.renderer.style.ExternalGraphicFactory;
import org.saig.core.renderer.style.GraphicStyle2D;
import org.saig.core.renderer.style.ImageLoader;
import org.saig.core.renderer.style.LineStyle2D;
import org.saig.core.renderer.style.MarkFactory;
import org.saig.core.renderer.style.MarkStyle2D;
import org.saig.core.renderer.style.PolygonStyle2D;
import org.saig.core.renderer.style.RescaledIcon;
import org.saig.core.renderer.style.Style;
import org.saig.core.renderer.style.Style2D;
import org.saig.core.renderer.style.TextStyle2D;
import org.saig.core.renderer.style.WellKnownMarkFactory;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Halo;
import org.saig.core.styling.LabelPlacement;
import org.saig.core.styling.LinePlacement;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.Mark;
import org.saig.core.styling.PointPlacement;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.StyleAttributeExtractor;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.StyleFactoryImpl;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.core.util.DateFormatManager;
import org.saig.core.util.NumberFormatManager;
import org.w3c.dom.Document;

public class SLDStyleFactory {
    private static final Logger LOGGER = Logger.getLogger(SLDStyleFactory.class);
    private static final int MAX_RASTERIZATION_SIZE = 512;
    private static final Map<String, Integer> joinLookup = new HashMap<String, Integer>();
    private static final Map<String, Integer> capLookup = new HashMap<String, Integer>();
    private static final Map<String, Integer> fontStyleLookup = new HashMap<String, Integer>();
    public static final String SQUARE_WKN_KEY = "square";
    public static final String TRIANGLE_WKN_KEY = "triangle";
    public static final String CROSS_WKN_KEY = "cross";
    public static final String CIRCLE_WKN_KEY = "circle";
    public static final String STAR_WKN_KEY = "star";
    public static final String X_WKN_KEY = "x";
    public static final String ARROW_WKN_KEY = "arrow";
    public static final String HATCH_WKN_KEY = "hatch";
    private static Set<String> fontFamilies = null;
    private static Map<String, Font> loadedFonts = new HashMap<String, Font>();
    public static Set<String> wellKnownMarks = new HashSet<String>();
    static Set<String> supportedGraphicFormats = null;
    static ImageLoader imageLoader = new ImageLoader();
    private static List<GlyphRenderer> glyphRenderers = new ArrayList<GlyphRenderer>();
    WeakHashMap<URL, Document> svgGlyphs = new WeakHashMap();
    WeakHashMap<SymbolizerKey, Style2D> dynamicSymbolizers = new WeakHashMap();
    WeakHashMap<SymbolizerKey, Style2D> staticSymbolizers = new WeakHashMap();
    private long hits;
    private long requests;
    private boolean vectorRenderingEnabled = false;
    private double mapScaleDenominator = Double.NaN;
    RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);

    static {
        joinLookup.put("miter", new Integer(0));
        joinLookup.put("mitre", new Integer(0));
        joinLookup.put("bevel", new Integer(2));
        joinLookup.put("round", new Integer(1));
        capLookup.put("butt", new Integer(0));
        capLookup.put("round", new Integer(1));
        capLookup.put(SQUARE_WKN_KEY, new Integer(2));
        fontStyleLookup.put("normal", new Integer(0));
        fontStyleLookup.put("italic", new Integer(2));
        fontStyleLookup.put("oblique", new Integer(2));
        fontStyleLookup.put("bold", new Integer(1));
        wellKnownMarks.add(StringUtils.capitalize((String)SQUARE_WKN_KEY));
        wellKnownMarks.add(StringUtils.capitalize((String)TRIANGLE_WKN_KEY));
        wellKnownMarks.add(StringUtils.capitalize((String)CROSS_WKN_KEY));
        wellKnownMarks.add(StringUtils.capitalize((String)CIRCLE_WKN_KEY));
        wellKnownMarks.add(StringUtils.capitalize((String)STAR_WKN_KEY));
        wellKnownMarks.add(StringUtils.capitalize((String)X_WKN_KEY));
        wellKnownMarks.add(StringUtils.capitalize((String)ARROW_WKN_KEY));
        wellKnownMarks.add(StringUtils.capitalize((String)HATCH_WKN_KEY));
        wellKnownMarks.add(SQUARE_WKN_KEY);
        wellKnownMarks.add(TRIANGLE_WKN_KEY);
        wellKnownMarks.add(CROSS_WKN_KEY);
        wellKnownMarks.add(CIRCLE_WKN_KEY);
        wellKnownMarks.add(STAR_WKN_KEY);
        wellKnownMarks.add(X_WKN_KEY);
        wellKnownMarks.add(ARROW_WKN_KEY);
        wellKnownMarks.add(HATCH_WKN_KEY);
        glyphRenderers.add(new CustomGlyphRenderer());
        try {
            glyphRenderers.add(new SVGGlyphRenderer());
        }
        catch (Exception e) {
            LOGGER.warn((Object)("Will not support SVG External Graphics " + e));
        }
    }

    public RenderingHints getRenderingHints() {
        return this.renderingHints;
    }

    public void setRenderingHints(RenderingHints renderingHints) {
        if (renderingHints == null) {
            return;
        }
        this.renderingHints = renderingHints;
    }

    public boolean isVectorRenderingEnabled() {
        return this.vectorRenderingEnabled;
    }

    public void setVectorRenderingEnabled(boolean vectorRenderingEnabled) {
        this.vectorRenderingEnabled = vectorRenderingEnabled;
    }

    public double getHitRatio() {
        return (double)this.hits / (double)this.requests;
    }

    public long getHits() {
        return this.hits;
    }

    public long getRequests() {
        return this.requests;
    }

    public Style2D createStyle(Feature f, Symbolizer symbolizer, Range scaleRange) {
        Style2D style = null;
        SymbolizerKey key = new SymbolizerKey(symbolizer, scaleRange);
        style = this.staticSymbolizers.get(key);
        ++this.requests;
        if (style != null) {
            ++this.hits;
        } else {
            style = this.createStyleInternal(f, symbolizer, scaleRange);
            if (this.dynamicSymbolizers.containsKey(key)) {
                return style;
            }
            StyleAttributeExtractor sae = new StyleAttributeExtractor();
            sae.visit(symbolizer);
            Set<String> nameSet = sae.getAttributeNameSet();
            if (CollectionUtils.isEmpty(nameSet)) {
                this.staticSymbolizers.put(key, style);
            } else {
                this.dynamicSymbolizers.put(key, null);
            }
        }
        return style;
    }

    private Style2D createStyleInternal(Feature f, Symbolizer symbolizer, Range scaleRange) {
        Style2D style = null;
        if (symbolizer instanceof PolygonSymbolizer) {
            style = this.createPolygonStyle(f, (PolygonSymbolizer)symbolizer, scaleRange);
        } else if (symbolizer instanceof LineSymbolizer) {
            style = this.createLineStyle(f, (LineSymbolizer)symbolizer, scaleRange);
        } else if (symbolizer instanceof PointSymbolizer) {
            style = this.createPointStyle(f, (PointSymbolizer)symbolizer, scaleRange);
        } else if (symbolizer instanceof TextSymbolizer) {
            style = this.createTextStyle(f, (TextSymbolizer)symbolizer, scaleRange);
        }
        return style;
    }

    public Style2D createDynamicStyle(Feature f, Symbolizer symbolizer, Range scaleRange) {
        Style2D style = null;
        if (symbolizer instanceof PolygonSymbolizer) {
            style = this.createDynamicPolygonStyle(f, (PolygonSymbolizer)symbolizer, scaleRange);
        } else if (symbolizer instanceof LineSymbolizer) {
            style = this.createDynamicLineStyle(f, (LineSymbolizer)symbolizer, scaleRange);
        } else {
            throw new UnsupportedOperationException("This kind of symbolizer is not yet supported");
        }
        return style;
    }

    Style2D createPolygonStyle(Feature feature, PolygonSymbolizer symbolizer, Range scaleRange) {
        PolygonStyle2D style = new PolygonStyle2D();
        this.setScaleRange(style, scaleRange);
        style.setStroke(this.getStroke(symbolizer.getStroke(), feature));
        style.setGraphicStroke(this.getGraphicStroke(symbolizer.getStroke(), feature, scaleRange));
        style.setContour(this.getStrokePaint(symbolizer.getStroke(), feature));
        style.setContourComposite(this.getStrokeComposite(symbolizer.getStroke(), feature));
        this.setPolygonStyleFill(feature, style, symbolizer, scaleRange);
        return style;
    }

    void setPolygonStyleFill(Feature feature, PolygonStyle2D style, PolygonSymbolizer symbolizer, Range scaleRange) {
        Fill fill = symbolizer.getFill();
        if (fill == null) {
            return;
        }
        if (fill.getGraphicFill() != null) {
            double size = this.evalToDouble(fill.getGraphicFill().getSize(), feature, 0.0);
            if (this.isVectorRenderingEnabled() || size > 512.0) {
                Style2D style2DFill = this.createPointStyle(feature, fill.getGraphicFill(), scaleRange, false);
                style.setGraphicFill(style2DFill);
                return;
            }
            if (symbolizer.getFill().getGraphicFill().getExternalGraphics() != null && symbolizer.getFill().getGraphicFill().getExternalGraphics().length > 0 && symbolizer.getFill().getGraphicFill().getExternalGraphics()[0].getCustomProperties() != null) {
                style.setWktFillName((String)symbolizer.getFill().getGraphicFill().getExternalGraphics()[0].getCustomProperties().get("wktFill"));
                style.setWktFillColor((Color)symbolizer.getFill().getGraphicFill().getExternalGraphics()[0].getCustomProperties().get("wktFillColor"));
            }
        }
        style.setFill(this.getPaint(symbolizer.getFill(), feature));
        style.setFillComposite(this.getComposite(symbolizer.getFill(), feature));
    }

    Style2D createDynamicPolygonStyle(Feature feature, PolygonSymbolizer symbolizer, Range scaleRange) {
        DynamicPolygonStyle2D style = new DynamicPolygonStyle2D(feature, symbolizer);
        this.setScaleRange(style, scaleRange);
        return style;
    }

    Style2D createLineStyle(Feature feature, LineSymbolizer symbolizer, Range scaleRange) {
        LineStyle2D style = new LineStyle2D();
        this.setScaleRange(style, scaleRange);
        style.setStroke(this.getStroke(symbolizer.getStroke(), feature));
        style.setGraphicStroke(this.getGraphicStroke(symbolizer.getStroke(), feature, scaleRange));
        style.setContour(this.getStrokePaint(symbolizer.getStroke(), feature));
        style.setContourComposite(this.getStrokeComposite(symbolizer.getStroke(), feature));
        if (symbolizer.getOffset() != null) {
            style.setOffset(((Number)symbolizer.getOffset().getValue(feature)).doubleValue());
        }
        return style;
    }

    Style2D createDynamicLineStyle(Feature feature, LineSymbolizer symbolizer, Range scaleRange) {
        DynamicLineStyle2D style = new DynamicLineStyle2D(feature, symbolizer);
        this.setScaleRange(style, scaleRange);
        if (symbolizer.getOffset() != null) {
            style.setOffset(((Number)symbolizer.getOffset().getValue(feature)).doubleValue());
        }
        return style;
    }

    Style2D createPointStyle(Feature feature, PointSymbolizer symbolizer, Range scaleRange) {
        return this.createPointStyle(feature, symbolizer.getGraphic(), scaleRange, false);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    Style2D createPointStyle(Feature feature, Graphic sldGraphic, Range scaleRange, boolean forceVector) {
        Style2D retval = null;
        float opacity = this.evalOpacity(sldGraphic.getOpacity(), feature);
        AlphaComposite composite = AlphaComposite.getInstance(3, opacity);
        float displacementX = 0.0f;
        float displacementY = 0.0f;
        if (sldGraphic.getDisplacement() != null) {
            displacementX = this.evalToFloat(sldGraphic.getDisplacement().getDisplacementX(), feature, 0.0f);
            displacementY = this.evalToFloat(sldGraphic.getDisplacement().getDisplacementY(), feature, 0.0f);
        }
        double size = 0.0;
        try {
            if (sldGraphic.getSize() != null) {
                size = this.evalToDouble(sldGraphic.getSize(), feature, 6.0);
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        float rotation = (float)((double)this.evalToFloat(sldGraphic.getRotation(), feature, 0.0f) * Math.PI / 180.0);
        Object[] symbols = sldGraphic.getSymbols();
        if (ArrayUtils.isEmpty((Object[])symbols)) {
            return null;
        }
        Object[] objectArray = symbols;
        int n = symbols.length;
        int n2 = 0;
        while (n2 < n) {
            block18: {
                Object symbol;
                block19: {
                    symbol = objectArray[n2];
                    if (!(symbol instanceof ExternalGraphic)) break block19;
                    ExternalGraphic eg = (ExternalGraphic)symbol;
                    if (this.vectorRenderingEnabled || forceVector || size > 512.0) {
                        Icon icon = this.getIcon(eg, feature, -1.0);
                        if (icon != null) {
                            if (icon instanceof ImageIcon) {
                                GraphicStyle2D g2d = this.getGraphicStyle(eg, feature, size, 1);
                                if (g2d != null) {
                                    g2d.setRotation(rotation);
                                    g2d.setOpacity(opacity);
                                    retval = g2d;
                                    break;
                                }
                                break block18;
                            } else {
                                if ((double)icon.getIconHeight() != size && size != 0.0) {
                                    double scale = size / (double)icon.getIconHeight();
                                    icon = new RescaledIcon(icon, scale);
                                }
                                retval = new IconStyle2D(icon, feature, displacementX, displacementY, rotation, composite);
                                break;
                            }
                        }
                        break block18;
                    } else {
                        GraphicStyle2D g2d = this.getGraphicStyle(eg, feature, size, 1);
                        if (g2d != null) {
                            g2d.setRotation(rotation);
                            g2d.setOpacity(opacity);
                            retval = g2d;
                            break;
                        }
                    }
                    break block18;
                }
                if (symbol instanceof Mark) {
                    Mark mark = (Mark)symbol;
                    Shape shape = this.getShape(mark, feature);
                    if (shape == null) {
                        throw new IllegalArgumentException("The specified mark " + mark.getWellKnownName() + " was not found!");
                    }
                    MarkStyle2D ms2d = new MarkStyle2D();
                    ms2d.setShape(shape);
                    ms2d.setFill(this.getPaint(mark.getFill(), feature));
                    ms2d.setFillComposite(this.getComposite(mark.getFill(), feature));
                    ms2d.setStroke(this.getStroke(mark.getStroke(), feature));
                    ms2d.setContour(this.getStrokePaint(mark.getStroke(), feature));
                    ms2d.setContourComposite(this.getStrokeComposite(mark.getStroke(), feature));
                    if (size <= 0.0) {
                        size = 16.0;
                    }
                    ms2d.setSize((int)size);
                    ms2d.setRotation(rotation);
                    retval = ms2d;
                    break;
                }
            }
            ++n2;
        }
        if (retval != null) {
            this.setScaleRange(retval, scaleRange);
        }
        return retval;
    }

    Style2D createTextStyle(Feature feature, TextSymbolizer symbolizer, Range scaleRange) {
        Graphic graphicShield;
        Number newRotation;
        Object obj;
        TextStyle2D ts2d = new TextStyle2D();
        this.setScaleRange(ts2d, scaleRange);
        String geomName = symbolizer.getGeometryPropertyName();
        Geometry geom = this.findGeometry(feature, geomName);
        if (geom == null || geom.isEmpty()) {
            geom = feature.getGeometry();
        }
        if ((obj = symbolizer.resolveLabel(feature)) == null) {
            return null;
        }
        String label = null;
        label = obj instanceof Number ? NumberFormatManager.getFormattedValue((Number)obj) : (obj instanceof Date ? DateFormatManager.getDateTimeFormat().format((Date)obj) : obj.toString());
        if ((label = label.trim()).length() == 0) {
            return null;
        }
        ts2d.setLabel(label);
        org.saig.core.styling.Font[] fonts = symbolizer.getFonts();
        Font javaFont = this.getFont(feature, fonts);
        ts2d.setFont(javaFont);
        LabelPlacement placement = symbolizer.getLabelPlacement();
        double anchorX = 0.0;
        double anchorY = 0.0;
        double rotation = 0.0;
        double dispX = 0.0;
        double dispY = 0.0;
        if (placement instanceof PointPlacement) {
            PointPlacement p = (PointPlacement)placement;
            ts2d.setPointPlacement(true);
            anchorX = ((Number)p.getAnchorPoint().getAnchorPointX().getValue(feature)).doubleValue();
            anchorY = ((Number)p.getAnchorPoint().getAnchorPointY().getValue(feature)).doubleValue();
            dispX = ((Number)p.getDisplacement().getDisplacementX().getValue(feature)).doubleValue();
            dispY = ((Number)p.getDisplacement().getDisplacementY().getValue(feature)).doubleValue();
            rotation = 0.0;
            if (((PointPlacement)placement).getRotation() != null) {
                try {
                    newRotation = (Number)((PointPlacement)placement).getRotation().getValue(feature);
                    rotation = newRotation != null && newRotation.floatValue() != 0.0f ? (double)((float)((double)newRotation.floatValue() * Math.PI / 180.0)) : 0.0;
                    ts2d.setRotationStablished(true);
                }
                catch (Exception e1) {
                    rotation = 0.0;
                }
            }
        } else if (placement instanceof LinePlacement) {
            int offset = ((Number)((LinePlacement)placement).getPerpendicularOffset().getValue(feature)).intValue();
            if (((LinePlacement)placement).getAttributeRotation() != null) {
                try {
                    newRotation = (Number)((LinePlacement)placement).getAttributeRotation().getValue(feature);
                    rotation = newRotation != null && newRotation.floatValue() != 0.0f ? (double)((float)((double)newRotation.floatValue() * Math.PI / 180.0)) : 0.0;
                    ts2d.setRotationStablished(true);
                }
                catch (Exception e1) {
                    rotation = 0.0;
                }
            }
            ts2d.setPerpendicularOffset(offset);
            ts2d.setPointPlacement(false);
        }
        ts2d.setAnchorX(anchorX);
        ts2d.setAnchorY(anchorY);
        ts2d.setRotation((float)rotation);
        ts2d.setDisplacementX(dispX);
        ts2d.setDisplacementY(dispY);
        ts2d.setFill(this.getPaint(symbolizer.getFill(), feature));
        ts2d.setComposite(this.getComposite(symbolizer.getFill(), feature));
        Halo halo = symbolizer.getHalo();
        if (halo != null) {
            ts2d.setHaloFill(this.getPaint(halo.getFill(), feature));
            ts2d.setHaloComposite(this.getComposite(halo.getFill(), feature));
            ts2d.setHaloRadius(((Number)halo.getRadius().getValue(feature)).floatValue());
        }
        if ((graphicShield = symbolizer.getGraphic()) != null) {
            StyleFactoryImpl sfac = new StyleFactoryImpl();
            PointSymbolizer p = ((StyleFactory)sfac).createPointSymbolizer();
            p.setGraphic(graphicShield);
            Style2D shieldStyle = this.createPointStyle(feature, p, scaleRange);
            ts2d.setGraphic(shieldStyle);
        }
        return ts2d;
    }

    private Geometry findGeometry(Feature feature, String geomName) {
        Geometry geom = null;
        geom = geomName == null ? feature.getGeometry() : (feature.getSchema().hasAttribute(geomName) ? (Geometry)feature.getAttribute(geomName) : feature.getGeometry());
        return geom;
    }

    private synchronized Font getFont(Feature feature, org.saig.core.styling.Font[] fonts) {
        if (fontFamilies == null) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            fontFamilies = new HashSet<String>();
            List<String> f = Arrays.asList(ge.getAvailableFontFamilyNames());
            fontFamilies.addAll(f);
        }
        Font javaFont = null;
        int styleCode = 0;
        int size = 6;
        String requestedFont = "";
        int k = 0;
        while (k < fonts.length) {
            block17: {
                String reqWeight;
                String reqStyle;
                requestedFont = fonts[k].getFontFamily().getValue(feature).toString();
                if (loadedFonts.containsKey(requestedFont)) {
                    javaFont = loadedFonts.get(requestedFont);
                    reqStyle = (String)fonts[k].getFontStyle().getValue(feature);
                    styleCode = fontStyleLookup.containsKey(reqStyle) ? fontStyleLookup.get(reqStyle) : 0;
                    reqWeight = (String)fonts[k].getFontWeight().getValue(feature);
                    if (reqWeight.equalsIgnoreCase("Bold")) {
                        styleCode |= 1;
                    }
                    size = ((Number)fonts[k].getFontSize().getValue(feature)).intValue();
                    return javaFont.deriveFont(styleCode, size);
                }
                if (fontFamilies.contains(requestedFont)) {
                    reqStyle = (String)fonts[k].getFontStyle().getValue(feature);
                    styleCode = fontStyleLookup.containsKey(reqStyle) ? fontStyleLookup.get(reqStyle) : 0;
                    reqWeight = (String)fonts[k].getFontWeight().getValue(feature);
                    if (reqWeight.equalsIgnoreCase("Bold")) {
                        styleCode |= 1;
                    }
                    size = ((Number)fonts[k].getFontSize().getValue(feature)).intValue();
                    javaFont = new Font(requestedFont, styleCode, size);
                    loadedFonts.put(requestedFont, javaFont);
                    return javaFont;
                }
                InputStream is = null;
                if (requestedFont.startsWith("http") || requestedFont.startsWith("file:")) {
                    try {
                        URL url = new URL(requestedFont);
                        is = url.openStream();
                    }
                    catch (MalformedURLException mue) {
                        LOGGER.error((Object)("Bad url in SLDStyleFactory " + requestedFont + "\n" + mue));
                    }
                    catch (IOException ioe) {
                        LOGGER.error((Object)("IO error in SLDStyleFactory " + requestedFont + "\n" + ioe));
                    }
                } else {
                    File file = new File(requestedFont);
                    try {
                        is = new FileInputStream(file);
                    }
                    catch (FileNotFoundException fileNotFoundException) {
                        // empty catch block
                    }
                }
                if (is != null) {
                    try {
                        javaFont = Font.createFont(0, is);
                    }
                    catch (FontFormatException ffe) {
                        break block17;
                    }
                    catch (IOException ioe) {
                        LOGGER.error((Object)("IO error in SLDStyleFactory " + requestedFont + "\n" + ioe));
                        break block17;
                    }
                    loadedFonts.put(requestedFont, javaFont);
                    return javaFont;
                }
            }
            ++k;
        }
        return new Font("Serif", 0, 12);
    }

    void setScaleRange(Style style, Range scaleRange) {
        double min = ((Number)((Object)scaleRange.getMinValue())).doubleValue();
        double max = ((Number)((Object)scaleRange.getMaxValue())).doubleValue();
        style.setMinMaxScale(min, max);
    }

    private Style2D getGraphicStroke(org.saig.core.styling.Stroke stroke, Feature feature, Range scaleRange) {
        if (stroke == null || stroke.getGraphicStroke() == null) {
            return null;
        }
        return this.createPointStyle(feature, stroke.getGraphicStroke(), scaleRange, false);
    }

    private Stroke getStroke(org.saig.core.styling.Stroke stroke, Feature feature) {
        if (stroke == null) {
            return null;
        }
        String joinType = this.evaluateExpression(stroke.getLineJoin(), feature, "mitre");
        int joinCode = joinLookup.containsKey(joinType) ? joinLookup.get(joinType) : 0;
        String capType = this.evaluateExpression(stroke.getLineCap(), feature, SQUARE_WKN_KEY);
        int capCode = capLookup.containsKey(capType) ? capLookup.get(capType) : 2;
        float[] dashes = stroke.getDashArray();
        float width = ((Number)stroke.getWidth().getValue(feature)).floatValue();
        float dashOffset = ((Number)stroke.getDashOffset().getValue(feature)).floatValue();
        if ((double)width < 1.5) {
            width = 0.0f;
        }
        BasicStroke stroke2d = dashes != null && dashes.length > 0 ? new BasicStroke(width, capCode, joinCode, 1.0f, dashes, dashOffset) : new BasicStroke(width, capCode, joinCode, 1.0f);
        return stroke2d;
    }

    private Paint getStrokePaint(org.saig.core.styling.Stroke stroke, Feature feature) {
        if (stroke == null) {
            return null;
        }
        Paint contourPaint = Color.decode((String)stroke.getColor().getValue(feature));
        Graphic gr = stroke.getGraphicFill();
        if (gr != null) {
            contourPaint = this.getTexturePaint(gr, feature);
        }
        return contourPaint;
    }

    private Composite getStrokeComposite(org.saig.core.styling.Stroke stroke, Feature feature) {
        if (stroke == null) {
            return null;
        }
        float opacity = ((Number)stroke.getOpacity().getValue(feature)).floatValue();
        AlphaComposite composite = AlphaComposite.getInstance(3, opacity);
        return composite;
    }

    protected Paint getPaint(Fill fill, Feature feature) {
        Graphic gr;
        if (fill == null) {
            return null;
        }
        Paint fillPaint = null;
        if (fill.getColor() != null) {
            fillPaint = Color.decode((String)fill.getColor().getValue(feature));
            float opacity = ((Number)fill.getOpacity().getValue(feature)).floatValue();
            fillPaint = new Color(((Color)fillPaint).getRed(), ((Color)fillPaint).getGreen(), ((Color)fillPaint).getBlue(), (int)(opacity * 255.0f));
        }
        if ((gr = fill.getGraphicFill()) != null) {
            fillPaint = this.getTexturePaint(gr, feature);
        }
        if (fill.getGradientFill() != null) {
            fillPaint = this.fromGradient(fill.getGradientFill());
        }
        return fillPaint;
    }

    public MultipleGradientPaint fromGradient(Gradient gr) {
        MultipleGradientPaint p;
        switch (gr.getType()) {
            case RADIAL: {
                RadialGradientImpl rg = (RadialGradientImpl)gr;
                p = new RadialGradientPaint(rg.getCenterX(), rg.getCenterY(), rg.getRadius(), rg.getFractions(), rg.getColors(), this.convertToCycleMethod(rg.getCycleMethod()));
                break;
            }
            default: {
                LinearGradientImpl lg = (LinearGradientImpl)gr;
                p = new LinearGradientPaint(lg.getStartX(), lg.getStartY(), lg.getEndX(), lg.getEndY(), lg.getFractions(), lg.getColors(), this.convertToCycleMethod(lg.getCycleMethod()));
            }
        }
        return p;
    }

    private MultipleGradientPaint.CycleMethod convertToCycleMethod(Gradient.GradientCycleMethod method) {
        MultipleGradientPaint.CycleMethod c;
        switch (method) {
            case REFLECT: {
                c = MultipleGradientPaint.CycleMethod.REFLECT;
                break;
            }
            case REPEAT: {
                c = MultipleGradientPaint.CycleMethod.REPEAT;
                break;
            }
            default: {
                c = MultipleGradientPaint.CycleMethod.NO_CYCLE;
            }
        }
        return c;
    }

    protected Composite getComposite(Fill fill, Feature feature) {
        if (fill == null || fill.getOpacity() == null) {
            return null;
        }
        float opacity = ((Number)fill.getOpacity().getValue(feature)).floatValue();
        AlphaComposite composite = AlphaComposite.getInstance(3, opacity);
        return composite;
    }

    public TexturePaint getTexturePaint(Graphic gr, Feature feature) {
        int iSizeY;
        int iSizeX;
        double graphicSize = this.evalToDouble(gr.getSize(), feature, -1.0);
        GraphicStyle2D gs = null;
        ExternalGraphic[] externalGraphicArray = gr.getExternalGraphics();
        int n = externalGraphicArray.length;
        int n2 = 0;
        while (n2 < n) {
            ExternalGraphic eg = externalGraphicArray[n2];
            gs = this.getGraphicStyle(eg, feature, graphicSize, 1);
            if (gs != null) break;
            ++n2;
        }
        BufferedImage image = null;
        if (gs != null) {
            image = gs.getImage();
            iSizeX = image.getWidth() - gs.getBorder();
            iSizeY = image.getHeight() - gs.getBorder();
        } else {
            Mark mark = this.getMark(gr, feature);
            if (mark == null) {
                return null;
            }
            Shape shape = this.getShape(mark, feature);
            if (shape == null) {
                return null;
            }
            Rectangle2D shapeBounds = shape.getBounds2D();
            double shapeAspectRatio = shapeBounds.getHeight() > 0.0 && shapeBounds.getWidth() > 0.0 ? shapeBounds.getWidth() / shapeBounds.getHeight() : 1.0;
            double size = this.evalToDouble(gr.getSize(), feature, 16.0);
            double sizeX = size * shapeAspectRatio;
            double sizeY = size;
            image = new BufferedImage((int)Math.ceil(sizeX * 3.0), (int)Math.ceil(sizeY * 3.0), 2);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHints(this.renderingHints);
            double rotation = this.evalToDouble(gr.getRotation(), feature, 0.0);
            int i = -1;
            while (i < 2) {
                int j = -1;
                while (j < 2) {
                    double tx = sizeX * 1.5 + sizeX * (double)i;
                    double ty = sizeY * 1.5 + sizeY * (double)j;
                    this.fillDrawMark(g2d, tx, ty, mark, size, rotation, feature);
                    ++j;
                }
                ++i;
            }
            g2d.dispose();
            iSizeX = (int)Math.floor(sizeX);
            iSizeY = (int)Math.floor(sizeY);
            image = image.getSubimage(iSizeX, iSizeY, iSizeX + 1, iSizeY + 1);
        }
        Rectangle2D.Double rect = new Rectangle2D.Double(0.0, 0.0, iSizeX, iSizeY);
        TexturePaint imagePaint = new TexturePaint(image, rect);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug((Object)("applied TexturePaint " + imagePaint));
        }
        return imagePaint;
    }

    private Mark getMark(Graphic graphic, Feature feature) {
        Mark[] marks = graphic.getMarks();
        int i = 0;
        while (i < marks.length) {
            String name = marks[i].getWellKnownName().getValue(feature).toString();
            if (wellKnownMarks.contains(name) || WellKnownMarkFactory.existsMark(name)) {
                Mark mark = marks[i];
                return mark;
            }
            ++i;
        }
        Mark mark = null;
        return mark;
    }

    private void fillDrawMark(Graphics2D g2d, double tx, double ty, Mark mark, double size, double rotation, Feature feature) {
        if (mark == null) {
            return;
        }
        Shape originalShape = this.getShape(mark, feature);
        AffineTransform markAT = new AffineTransform();
        markAT.translate(tx, ty);
        markAT.rotate(rotation);
        markAT.scale(size, -size);
        Shape shape = markAT.createTransformedShape(originalShape);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        if (mark.getFill() != null) {
            g2d.setPaint(this.getPaint(mark.getFill(), feature));
            g2d.setComposite(this.getComposite(mark.getFill(), feature));
            g2d.fill(shape);
        }
        if (mark.getStroke() != null) {
            g2d.setPaint(this.getStrokePaint(mark.getStroke(), feature));
            g2d.setComposite(this.getStrokeComposite(mark.getStroke(), feature));
            g2d.setStroke(this.getStroke(mark.getStroke(), feature));
            g2d.draw(shape);
        }
    }

    private String evaluateExpression(Expression e, Feature feature, String defaultValue) {
        String result = defaultValue;
        if (e != null && (result = (String)e.getValue(feature)) == null) {
            result = defaultValue;
        }
        return result;
    }

    public static int lookUpJoin(String joinType) {
        if (joinLookup.containsKey(joinType)) {
            return joinLookup.get(joinType);
        }
        return 0;
    }

    public static int lookUpCap(String capType) {
        if (capLookup.containsKey(capType)) {
            return capLookup.get(capType);
        }
        return 2;
    }

    public double getMapScaleDenominator() {
        return this.mapScaleDenominator;
    }

    public void setMapScaleDenominator(double mapScaleDenominator) {
        this.mapScaleDenominator = mapScaleDenominator;
    }

    private float evalOpacity(Expression opacityExpr, Feature feature) {
        return this.evalToFloat(opacityExpr, feature, 1.0f);
    }

    private float evalToFloat(Expression exp, Feature f, float fallback) {
        if (exp == null) {
            return fallback;
        }
        try {
            if (exp.getValue(f) == null) {
                return fallback;
            }
        }
        catch (Exception ex) {
            return fallback;
        }
        Float fo = Float.valueOf(Float.parseFloat(exp.getValue(f).toString()));
        if (fo != null) {
            return fo.floatValue();
        }
        return fallback;
    }

    private double evalToDouble(Expression exp, Feature f, double fallback) {
        if (exp == null) {
            return fallback;
        }
        try {
            if (exp.getValue(f) == null) {
                return fallback;
            }
        }
        catch (Exception ex) {
            return fallback;
        }
        Double d = Double.parseDouble(exp.getValue(f).toString());
        if (d != null) {
            return d;
        }
        return fallback;
    }

    private Icon getIcon(ExternalGraphic eg, Feature feature, double size) {
        String strLocation;
        if (eg == null) {
            return null;
        }
        try {
            strLocation = eg.getLocation().toExternalForm();
        }
        catch (MalformedURLException e) {
            LOGGER.info((Object)"Malformed URL processing external graphic", (Throwable)e);
            return null;
        }
        LiteralExpression location = FilterFactory.createFilterFactory().createLiteralExpression(strLocation);
        Iterator<ExternalGraphicFactory> it = DynamicSymbolFactoryFinder.getExternalGraphicFactories();
        while (it.hasNext()) {
            ExternalGraphicFactory egf = it.next();
            try {
                Icon icon;
                String format = null;
                if (eg.getFormat() != null) {
                    format = eg.getFormat();
                }
                if ((icon = egf.getIcon(feature, location, format, this.toImageSize(size))) == null) continue;
                return icon;
            }
            catch (Exception e) {
                LOGGER.warn((Object)"Error occurred evaluating external graphic", (Throwable)e);
            }
        }
        return null;
    }

    int toImageSize(double size) {
        if (size == -1.0) {
            return -1;
        }
        if (size > 0.0 && size < 0.5) {
            return 1;
        }
        return (int)Math.round(size);
    }

    private GraphicStyle2D getGraphicStyle(ExternalGraphic eg, Feature feature, double size, int border) {
        Icon icon = this.getIcon(eg, feature, this.toImageSize(size));
        if (icon != null) {
            ImageIcon img;
            if (icon instanceof ImageIcon && (img = (ImageIcon)icon).getImage() instanceof BufferedImage) {
                BufferedImage image = (BufferedImage)img.getImage();
                return new GraphicStyle2D(image, 0.0f, 0.0f);
            }
            BufferedImage result = new BufferedImage(icon.getIconWidth() + border * 2, icon.getIconHeight() + border * 2, 6);
            Graphics2D g = (Graphics2D)result.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            icon.paintIcon(null, g, 1, 1);
            g.dispose();
            return new GraphicStyle2D(result, 0.0f, 0.0f, border);
        }
        return null;
    }

    private Shape getShape(Mark mark, Feature feature) {
        if (mark == null) {
            return null;
        }
        Expression name = mark.getWellKnownName();
        Iterator<MarkFactory> it = DynamicSymbolFactoryFinder.getMarkFactories();
        while (it.hasNext()) {
            MarkFactory factory = it.next();
            try {
                Shape shape = factory.getShape(null, name, feature);
                if (shape == null) continue;
                return shape;
            }
            catch (Exception e) {
                LOGGER.warn((Object)"Exception while scanning for the appropriate mark factory", (Throwable)e);
            }
        }
        return null;
    }

    private static class SymbolizerKey {
        private Symbolizer symbolizer;
        private double minScale;
        private double maxScale;

        public SymbolizerKey(Symbolizer symbolizer, Range scaleRange) {
            this.symbolizer = symbolizer;
            this.minScale = ((Number)((Object)scaleRange.getMinValue())).doubleValue();
            this.maxScale = ((Number)((Object)scaleRange.getMaxValue())).doubleValue();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof SymbolizerKey)) {
                return false;
            }
            SymbolizerKey other = (SymbolizerKey)obj;
            return other.symbolizer == this.symbolizer && other.minScale == this.minScale && other.maxScale == this.maxScale;
        }

        public int hashCode() {
            return ((17 + this.symbolizer.hashCode()) * 37 + this.doubleHash(this.minScale)) * 37 + this.doubleHash(this.maxScale);
        }

        private int doubleHash(double value) {
            long bits = Double.doubleToLongBits(value);
            return (int)(bits ^ bits >>> 32);
        }
    }
}

