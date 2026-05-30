/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import es.kosmo.core.styling.Gradient;
import es.kosmo.core.styling.LinearGradientImpl;
import es.kosmo.core.styling.RadialGradientImpl;
import java.awt.Color;
import java.net.URL;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.styling.AnchorPoint;
import org.saig.core.styling.AnchorPointImpl;
import org.saig.core.styling.ChannelSelection;
import org.saig.core.styling.ChannelSelectionImpl;
import org.saig.core.styling.ColorMap;
import org.saig.core.styling.ColorMapEntry;
import org.saig.core.styling.ColorMapEntryImpl;
import org.saig.core.styling.ColorMapImpl;
import org.saig.core.styling.ContrastEnhancement;
import org.saig.core.styling.Displacement;
import org.saig.core.styling.DisplacementImpl;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.ExternalGraphicImpl;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.FeatureTypeStyleImpl;
import org.saig.core.styling.Fill;
import org.saig.core.styling.FillImpl;
import org.saig.core.styling.Font;
import org.saig.core.styling.FontImpl;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.GraphicImpl;
import org.saig.core.styling.Halo;
import org.saig.core.styling.HaloImpl;
import org.saig.core.styling.LabelPlacement;
import org.saig.core.styling.LinePlacement;
import org.saig.core.styling.LinePlacementImpl;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.LineSymbolizerImpl;
import org.saig.core.styling.Mark;
import org.saig.core.styling.MarkImpl;
import org.saig.core.styling.NamedStyle;
import org.saig.core.styling.PointPlacement;
import org.saig.core.styling.PointPlacementImpl;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PointSymbolizerImpl;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.PolygonSymbolizerImpl;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.RasterSymbolizerImpl;
import org.saig.core.styling.Rule;
import org.saig.core.styling.RuleImpl;
import org.saig.core.styling.SelectedChannelType;
import org.saig.core.styling.SelectedChannelTypeImpl;
import org.saig.core.styling.ShadedRelief;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StrokeImpl;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.StyleImpl;
import org.saig.core.styling.StyledLayerDescriptor;
import org.saig.core.styling.StyledLayerDescriptorImpl;
import org.saig.core.styling.Symbol;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.core.styling.TextSymbolizerImpl;

public class StyleFactoryImpl
extends StyleFactory {
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();

    @Override
    public Style createStyle() {
        return new StyleImpl();
    }

    @Override
    public NamedStyle createNamedStyle() {
        return new NamedStyle();
    }

    @Override
    public PointSymbolizer createPointSymbolizer() {
        return new PointSymbolizerImpl();
    }

    @Override
    public PointSymbolizer createPointSymbolizer(Graphic graphic, String geometryPropertyName) {
        PointSymbolizerImpl pSymb = new PointSymbolizerImpl();
        pSymb.setGeometryPropertyName(geometryPropertyName);
        pSymb.setGraphic(graphic);
        return pSymb;
    }

    @Override
    public PolygonSymbolizer createPolygonSymbolizer() {
        return new PolygonSymbolizerImpl();
    }

    @Override
    public PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill, String geometryPropertyName) {
        PolygonSymbolizerImpl pSymb = new PolygonSymbolizerImpl();
        pSymb.setGeometryPropertyName(geometryPropertyName);
        pSymb.setStroke(stroke);
        pSymb.setFill(fill);
        return pSymb;
    }

    @Override
    public LineSymbolizer createLineSymbolizer() {
        return new LineSymbolizerImpl();
    }

    @Override
    public LineSymbolizer createLineSymbolizer(Stroke stroke, String geometryPropertyName) {
        LineSymbolizerImpl lSymb = new LineSymbolizerImpl();
        lSymb.setGeometryPropertyName(geometryPropertyName);
        lSymb.setStroke(stroke);
        return lSymb;
    }

    @Override
    public TextSymbolizer createTextSymbolizer() {
        return new TextSymbolizerImpl();
    }

    @Override
    public TextSymbolizer createTextSymbolizer(Fill fill, Font[] fonts, Halo halo, Expression label, LabelPlacement labelPlacement, String geometryPropertyName) {
        TextSymbolizerImpl tSymb = new TextSymbolizerImpl();
        tSymb.setFill(fill);
        tSymb.setFonts(fonts);
        tSymb.setGeometryPropertyName(geometryPropertyName);
        tSymb.setHalo(halo);
        tSymb.setLabel(label);
        tSymb.setLabelPlacement(labelPlacement);
        return tSymb;
    }

    @Override
    public FeatureTypeStyle createFeatureTypeStyle() {
        return new FeatureTypeStyleImpl();
    }

    @Override
    public FeatureTypeStyle createFeatureTypeStyle(Rule[] rules) {
        return new FeatureTypeStyleImpl(rules);
    }

    @Override
    public Rule createRule() {
        return new RuleImpl();
    }

    @Override
    public Stroke createStroke(Expression color, Expression width) {
        return this.createStroke(color, width, filterFactory.createLiteralExpression(1.0));
    }

    @Override
    public Stroke createStroke(Expression color, Expression width, Expression opacity) {
        return this.createStroke(color, width, opacity, filterFactory.createLiteralExpression("mitre"), filterFactory.createLiteralExpression("butt"), null, filterFactory.createLiteralExpression(0.0), null, null);
    }

    @Override
    public Stroke createStroke(Expression color, Expression width, Expression opacity, Expression lineJoin, Expression lineCap, float[] dashArray, Expression dashOffset, Graphic graphicFill, Graphic graphicStroke) {
        StrokeImpl stroke = new StrokeImpl();
        if (color == null) {
            throw new IllegalArgumentException("Color may not be null in a stroke");
        }
        stroke.setColor(color);
        if (width == null) {
            throw new IllegalArgumentException("Width may not be null in a stroke");
        }
        stroke.setWidth(width);
        if (opacity == null) {
            throw new IllegalArgumentException("Opacity may not be null in a stroke");
        }
        stroke.setOpacity(opacity);
        if (lineJoin == null) {
            throw new IllegalArgumentException("LineJoin may not be null in a stroke");
        }
        stroke.setLineJoin(lineJoin);
        if (lineCap == null) {
            throw new IllegalArgumentException("LineCap may not be null in a stroke");
        }
        stroke.setLineCap(lineCap);
        stroke.setDashArray(dashArray);
        stroke.setDashOffset(dashOffset);
        stroke.setGraphicFill(graphicFill);
        stroke.setGraphicStroke(graphicStroke);
        return stroke;
    }

    @Override
    public Fill createFill(Expression color, Expression backgroundColor, Expression opacity, Graphic graphicFill) {
        FillImpl fill = new FillImpl();
        if (color == null) {
            throw new IllegalArgumentException("Color may not be null in a fill");
        }
        fill.setColor(color);
        fill.setBackgroundColor(backgroundColor);
        if (opacity == null) {
            throw new IllegalArgumentException("Opacity may not be null in a fill");
        }
        fill.setOpacity(opacity);
        fill.setGraphicFill(graphicFill);
        return fill;
    }

    @Override
    public Fill createFill(Expression color, Expression opacity) {
        return this.createFill(color, null, opacity, null);
    }

    @Override
    public Fill createFill(Expression color) {
        return this.createFill(color, null, filterFactory.createLiteralExpression(1.0), null);
    }

    @Override
    public Mark createMark(Expression wellKnownName, Stroke stroke, Fill fill, Expression size, Expression rotation) {
        MarkImpl mark = new MarkImpl();
        if (wellKnownName == null) {
            throw new IllegalArgumentException("WellKnownName can not be null in mark");
        }
        mark.setWellKnownName(wellKnownName);
        mark.setStroke(stroke);
        mark.setFill(fill);
        if (size == null) {
            throw new IllegalArgumentException("Size can not be null in mark");
        }
        mark.setSize(size);
        if (rotation == null) {
            throw new IllegalArgumentException("Rotation can not be null in mark");
        }
        mark.setRotation(rotation);
        return mark;
    }

    @Override
    public Mark getSquareMark() {
        Mark mark = this.createMark(filterFactory.createLiteralExpression("Square"), this.getDefaultStroke(), this.getDefaultFill(), filterFactory.createLiteralExpression(6), filterFactory.createLiteralExpression(0));
        return mark;
    }

    @Override
    public Mark getCircleMark() {
        Mark mark = this.getDefaultMark();
        mark.setWellKnownName(filterFactory.createLiteralExpression("Circle"));
        return mark;
    }

    @Override
    public Mark getCrossMark() {
        Mark mark = this.getDefaultMark();
        mark.setWellKnownName(filterFactory.createLiteralExpression("Cross"));
        return mark;
    }

    @Override
    public Mark getXMark() {
        Mark mark = this.getDefaultMark();
        mark.setWellKnownName(filterFactory.createLiteralExpression("X"));
        return mark;
    }

    @Override
    public Mark getTriangleMark() {
        Mark mark = this.getDefaultMark();
        mark.setWellKnownName(filterFactory.createLiteralExpression("Triangle"));
        return mark;
    }

    @Override
    public Mark getStarMark() {
        Mark mark = this.getDefaultMark();
        mark.setWellKnownName(filterFactory.createLiteralExpression("Star"));
        return mark;
    }

    @Override
    public Mark createMark() {
        MarkImpl mark = new MarkImpl();
        return mark;
    }

    @Override
    public Graphic createGraphic(ExternalGraphic[] externalGraphics, Mark[] marks, Symbol[] symbols, Expression opacity, Expression size, Expression rotation) {
        GraphicImpl graphic = new GraphicImpl();
        graphic.setSymbols(symbols);
        if (externalGraphics != null) {
            graphic.setExternalGraphics(externalGraphics);
        }
        if (marks != null) {
            graphic.setMarks(marks);
        }
        if (opacity == null) {
            throw new IllegalArgumentException("Opacity can not be null in graphic");
        }
        graphic.setOpacity(opacity);
        if (size == null) {
            throw new IllegalArgumentException("Size can not be null in graphic");
        }
        graphic.setSize(size);
        if (rotation == null) {
            throw new IllegalArgumentException("Rotation can not be null in graphic");
        }
        graphic.setRotation(rotation);
        return graphic;
    }

    @Override
    public ExternalGraphic createExternalGraphic(String uri, String format) {
        ExternalGraphicImpl extg = new ExternalGraphicImpl();
        extg.setURI(uri);
        extg.setFormat(format);
        return extg;
    }

    @Override
    public ExternalGraphic createExternalGraphic(URL url, String format) {
        ExternalGraphicImpl extg = new ExternalGraphicImpl();
        extg.setLocation(url);
        extg.setFormat(format);
        return extg;
    }

    @Override
    public Font createFont(Expression fontFamily, Expression fontStyle, Expression fontWeight, Expression fontSize) {
        FontImpl font = new FontImpl();
        if (fontFamily == null) {
            throw new IllegalArgumentException("Null font family specified");
        }
        font.setFontFamily(fontFamily);
        if (fontSize == null) {
            throw new IllegalArgumentException("Null font size specified");
        }
        font.setFontSize(fontSize);
        if (fontStyle == null) {
            throw new IllegalArgumentException("Null font Style specified");
        }
        font.setFontStyle(fontStyle);
        if (fontWeight == null) {
            throw new IllegalArgumentException("Null font weight specified");
        }
        font.setFontWeight(fontWeight);
        return font;
    }

    @Override
    public LinePlacement createLinePlacement(Expression offset) {
        LinePlacementImpl linep = new LinePlacementImpl();
        linep.setPerpendicularOffset(offset);
        return linep;
    }

    @Override
    public PointPlacement createPointPlacement(AnchorPoint anchorPoint, Displacement displacement, Expression rotation) {
        PointPlacementImpl pointp = new PointPlacementImpl();
        pointp.setAnchorPoint(anchorPoint);
        pointp.setDisplacement(displacement);
        pointp.setRotation(rotation);
        return pointp;
    }

    @Override
    public AnchorPoint createAnchorPoint(Expression x, Expression y) {
        AnchorPointImpl anchorPoint = new AnchorPointImpl();
        anchorPoint.setAnchorPointX(x);
        anchorPoint.setAnchorPointY(y);
        return anchorPoint;
    }

    @Override
    public Displacement createDisplacement(Expression x, Expression y) {
        DisplacementImpl displacement = new DisplacementImpl();
        displacement.setDisplacementX(x);
        displacement.setDisplacementY(y);
        return displacement;
    }

    @Override
    public Halo createHalo(Fill fill, Expression radius) {
        HaloImpl halo = new HaloImpl();
        halo.setFill(fill);
        halo.setRadius(radius);
        return halo;
    }

    @Override
    public Fill getDefaultFill() {
        FillImpl fill = new FillImpl();
        try {
            fill.setColor(filterFactory.createLiteralExpression("#808080"));
            fill.setOpacity(filterFactory.createLiteralExpression(new Double(1.0)));
        }
        catch (IllegalFilterException ife) {
            throw new RuntimeException("Error creating fill", ife);
        }
        return fill;
    }

    @Override
    public LineSymbolizer getDefaultLineSymbolizer() {
        return this.createLineSymbolizer(this.getDefaultStroke(), null);
    }

    @Override
    public Mark getDefaultMark() {
        return this.getSquareMark();
    }

    @Override
    public PointSymbolizer getDefaultPointSymbolizer() {
        return this.createPointSymbolizer(this.getDefaultGraphic(), null);
    }

    @Override
    public PolygonSymbolizer getDefaultPolygonSymbolizer() {
        return this.createPolygonSymbolizer(this.getDefaultStroke(), this.getDefaultFill(), null);
    }

    @Override
    public Stroke getDefaultStroke() {
        try {
            Stroke stroke = this.createStroke(filterFactory.createLiteralExpression("#000000"), filterFactory.createLiteralExpression(new Integer(1)));
            stroke.setDashOffset(filterFactory.createLiteralExpression(new Integer(0)));
            stroke.setLineCap(filterFactory.createLiteralExpression("butt"));
            stroke.setLineJoin(filterFactory.createLiteralExpression("mitre"));
            stroke.setOpacity(filterFactory.createLiteralExpression(new Integer(1)));
            return stroke;
        }
        catch (IllegalFilterException ife) {
            throw new RuntimeException("Error creating stroke", ife);
        }
    }

    @Override
    public Style getDefaultStyle() {
        Style style = this.createStyle();
        return style;
    }

    @Override
    public TextSymbolizer getDefaultTextSymbolizer() {
        return this.createTextSymbolizer(this.getDefaultFill(), new Font[]{this.getDefaultFont()}, null, null, this.getDefaultPointPlacement(), "geometry:text");
    }

    @Override
    public Font getDefaultFont() {
        FontImpl font = new FontImpl();
        try {
            font.setFontSize(filterFactory.createLiteralExpression(new Integer(10)));
            font.setFontStyle(filterFactory.createLiteralExpression("normal"));
            font.setFontWeight(filterFactory.createLiteralExpression("normal"));
            font.setFontFamily(filterFactory.createLiteralExpression("Serif"));
        }
        catch (IllegalFilterException ife) {
            throw new RuntimeException("Error creating font", ife);
        }
        return font;
    }

    @Override
    public Graphic getDefaultGraphic() {
        GraphicImpl graphic = new GraphicImpl();
        try {
            graphic.setSize(filterFactory.createLiteralExpression(new Integer(6)));
            graphic.setOpacity(filterFactory.createLiteralExpression(new Double(1.0)));
            graphic.setRotation(filterFactory.createLiteralExpression(new Double(0.0)));
            graphic.addMark(this.getDefaultMark());
        }
        catch (IllegalFilterException ife) {
            throw new RuntimeException("Error creating graphic", ife);
        }
        return graphic;
    }

    @Override
    public PointPlacement getDefaultPointPlacement() {
        return this.createPointPlacement(this.createAnchorPoint(filterFactory.createLiteralExpression(0.5), filterFactory.createLiteralExpression(0.5)), this.createDisplacement(filterFactory.createLiteralExpression(0), filterFactory.createLiteralExpression(0)), filterFactory.createLiteralExpression(0));
    }

    @Override
    public RasterSymbolizer createRasterSymbolizer(String geometryPropertyName, Expression opacity, ChannelSelection channel, Expression overlap, ColorMap colorMap, ContrastEnhancement cenhancement, ShadedRelief relief, Symbolizer outline) {
        RasterSymbolizerImpl rastersym = new RasterSymbolizerImpl();
        if (geometryPropertyName != null) {
            rastersym.setGeometryPropertyName(geometryPropertyName);
        }
        if (opacity != null) {
            rastersym.setOpacity(opacity);
        }
        if (channel != null) {
            rastersym.setChannelSelection(channel);
        }
        if (overlap != null) {
            rastersym.setOverlap(overlap);
        }
        if (colorMap != null) {
            rastersym.setColorMap(colorMap);
        }
        if (cenhancement != null) {
            rastersym.setContrastEnhancement(cenhancement);
        }
        if (relief != null) {
            rastersym.setShadedRelief(relief);
        }
        if (outline != null) {
            rastersym.setImageOutline(outline);
        }
        return rastersym;
    }

    @Override
    public RasterSymbolizer getDefaultRasterSymbolizer() {
        return this.createRasterSymbolizer("geom", filterFactory.createLiteralExpression(1.0), null, null, null, null, null, null);
    }

    @Override
    public ChannelSelection createChannelSelection(SelectedChannelType[] channels) {
        ChannelSelectionImpl channelSel = new ChannelSelectionImpl();
        if (channels != null && channels.length > 0) {
            channelSel.setSelectedChannels(channels);
        }
        return channelSel;
    }

    @Override
    public ColorMap createColorMap() {
        return new ColorMapImpl();
    }

    @Override
    public ColorMapEntry createColorMapEntry() {
        return new ColorMapEntryImpl();
    }

    @Override
    public SelectedChannelType createSelectedChannelType(String name, ContrastEnhancement enhancement) {
        SelectedChannelTypeImpl sct = new SelectedChannelTypeImpl();
        sct.setChannelName(name);
        sct.setContrastEnhancement(enhancement);
        return sct;
    }

    @Override
    public StyledLayerDescriptor createStyledLayerDescriptor() {
        return new StyledLayerDescriptorImpl();
    }

    @Override
    public Gradient createLinearGradient(float startX, float startY, float endX, float endY, float[] fractions, Color[] colors, Gradient.GradientCycleMethod method) {
        LinearGradientImpl lg = new LinearGradientImpl();
        lg.setStartX(startX);
        lg.setStartY(startY);
        lg.setEndX(endX);
        lg.setEndY(endY);
        lg.setFractions(fractions);
        lg.setColors(colors);
        lg.setCycleMethod(method);
        return lg;
    }

    @Override
    public Gradient createRadialGradient(float centerX, float centerY, float radius, float[] fractions, Color[] colors, Gradient.GradientCycleMethod method) {
        RadialGradientImpl rg = new RadialGradientImpl();
        rg.setCenterX(centerX);
        rg.setCenterY(centerY);
        rg.setRadius(radius);
        rg.setFractions(fractions);
        rg.setColors(colors);
        rg.setCycleMethod(method);
        return rg;
    }
}

