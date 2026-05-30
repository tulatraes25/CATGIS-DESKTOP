/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.factory.Factory
 *  org.geotools.factory.FactoryConfigurationError
 *  org.geotools.factory.FactoryFinder
 */
package org.saig.core.styling;

import es.kosmo.core.styling.Gradient;
import java.awt.Color;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import org.geotools.factory.Factory;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.factory.FactoryFinder;
import org.saig.core.filter.Expression;
import org.saig.core.styling.AnchorPoint;
import org.saig.core.styling.ChannelSelection;
import org.saig.core.styling.ColorMap;
import org.saig.core.styling.ColorMapEntry;
import org.saig.core.styling.ContrastEnhancement;
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
import org.saig.core.styling.NamedStyle;
import org.saig.core.styling.PointPlacement;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.SelectedChannelType;
import org.saig.core.styling.ShadedRelief;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyledLayerDescriptor;
import org.saig.core.styling.Symbol;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;

public abstract class StyleFactory
implements Factory {
    private static StyleFactory factory = null;

    public static StyleFactory createStyleFactory() throws FactoryConfigurationError {
        if (factory == null) {
            factory = (StyleFactory)FactoryFinder.findFactory((String)"org.saig.core.styling.StyleFactory", (String)"org.saig.core.styling.StyleFactoryImpl");
        }
        return factory;
    }

    public abstract TextSymbolizer createTextSymbolizer(Fill var1, Font[] var2, Halo var3, Expression var4, LabelPlacement var5, String var6);

    public abstract ExternalGraphic createExternalGraphic(URL var1, String var2);

    public abstract ExternalGraphic createExternalGraphic(String var1, String var2);

    public abstract AnchorPoint createAnchorPoint(Expression var1, Expression var2);

    public abstract Displacement createDisplacement(Expression var1, Expression var2);

    public abstract PointSymbolizer createPointSymbolizer();

    public abstract Mark createMark(Expression var1, Stroke var2, Fill var3, Expression var4, Expression var5);

    public abstract Mark getCircleMark();

    public abstract Mark getXMark();

    public abstract Mark getStarMark();

    public abstract Mark getSquareMark();

    public abstract Mark getCrossMark();

    public abstract Mark getTriangleMark();

    public abstract FeatureTypeStyle createFeatureTypeStyle(Rule[] var1);

    public abstract LinePlacement createLinePlacement(Expression var1);

    public abstract PolygonSymbolizer createPolygonSymbolizer();

    public abstract Halo createHalo(Fill var1, Expression var2);

    public abstract Fill createFill(Expression var1, Expression var2, Expression var3, Graphic var4);

    public abstract LineSymbolizer createLineSymbolizer();

    public abstract PointSymbolizer createPointSymbolizer(Graphic var1, String var2);

    public abstract Style createStyle();

    public abstract NamedStyle createNamedStyle();

    public abstract Fill createFill(Expression var1, Expression var2);

    public abstract Fill createFill(Expression var1);

    public abstract TextSymbolizer createTextSymbolizer();

    public abstract PointPlacement createPointPlacement(AnchorPoint var1, Displacement var2, Expression var3);

    public abstract Stroke createStroke(Expression var1, Expression var2);

    public abstract Stroke createStroke(Expression var1, Expression var2, Expression var3);

    public abstract Stroke createStroke(Expression var1, Expression var2, Expression var3, Expression var4, Expression var5, float[] var6, Expression var7, Graphic var8, Graphic var9);

    public abstract Rule createRule();

    public abstract LineSymbolizer createLineSymbolizer(Stroke var1, String var2);

    public abstract FeatureTypeStyle createFeatureTypeStyle();

    public abstract Graphic createGraphic(ExternalGraphic[] var1, Mark[] var2, Symbol[] var3, Expression var4, Expression var5, Expression var6);

    public abstract Font createFont(Expression var1, Expression var2, Expression var3, Expression var4);

    public abstract Mark createMark();

    public abstract PolygonSymbolizer createPolygonSymbolizer(Stroke var1, Fill var2, String var3);

    public abstract RasterSymbolizer createRasterSymbolizer(String var1, Expression var2, ChannelSelection var3, Expression var4, ColorMap var5, ContrastEnhancement var6, ShadedRelief var7, Symbolizer var8);

    public abstract RasterSymbolizer getDefaultRasterSymbolizer();

    public abstract ChannelSelection createChannelSelection(SelectedChannelType[] var1);

    public abstract SelectedChannelType createSelectedChannelType(String var1, ContrastEnhancement var2);

    public abstract ColorMap createColorMap();

    public abstract ColorMapEntry createColorMapEntry();

    public abstract Style getDefaultStyle();

    public abstract Stroke getDefaultStroke();

    public abstract Fill getDefaultFill();

    public abstract Mark getDefaultMark();

    public abstract PointSymbolizer getDefaultPointSymbolizer();

    public abstract PolygonSymbolizer getDefaultPolygonSymbolizer();

    public abstract LineSymbolizer getDefaultLineSymbolizer();

    public abstract TextSymbolizer getDefaultTextSymbolizer();

    public abstract Graphic getDefaultGraphic();

    public abstract Font getDefaultFont();

    public abstract PointPlacement getDefaultPointPlacement();

    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }

    public abstract StyledLayerDescriptor createStyledLayerDescriptor();

    public abstract Gradient createLinearGradient(float var1, float var2, float var3, float var4, float[] var5, Color[] var6, Gradient.GradientCycleMethod var7);

    public abstract Gradient createRadialGradient(float var1, float var2, float var3, float[] var4, Color[] var5, Gradient.GradientCycleMethod var6);
}

