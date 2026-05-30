/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.styling.Gradient;
import org.saig.core.styling.AnchorPoint;
import org.saig.core.styling.ChannelSelection;
import org.saig.core.styling.ColorMap;
import org.saig.core.styling.ColorMapEntry;
import org.saig.core.styling.ContrastEnhancement;
import org.saig.core.styling.Displacement;
import org.saig.core.styling.Extent;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.FeatureTypeConstraint;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Font;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Halo;
import org.saig.core.styling.ILabelResolver;
import org.saig.core.styling.LabelPlacement;
import org.saig.core.styling.LinePlacement;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.Mark;
import org.saig.core.styling.NamedLayer;
import org.saig.core.styling.PointPlacement;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.RemoteOWS;
import org.saig.core.styling.Rule;
import org.saig.core.styling.SelectedChannelType;
import org.saig.core.styling.ShadedRelief;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyledLayer;
import org.saig.core.styling.StyledLayerDescriptor;
import org.saig.core.styling.Symbol;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.core.styling.UserLayer;
import org.saig.core.styling.WKTGraphic;

public interface StyleVisitor {
    public void visit(StyledLayerDescriptor var1);

    public void visit(StyledLayer var1);

    public void visit(UserLayer var1);

    public void visit(NamedLayer var1);

    public void visit(Style var1);

    public void visit(FeatureTypeStyle var1);

    public void visit(Rule var1);

    public void visit(Symbolizer var1);

    public void visit(PointSymbolizer var1);

    public void visit(LineSymbolizer var1);

    public void visit(PolygonSymbolizer var1);

    public void visit(TextSymbolizer var1);

    public void visit(RasterSymbolizer var1);

    public void visit(Fill var1);

    public void visit(Stroke var1);

    public void visit(Graphic var1);

    public void visit(Mark var1);

    public void visit(ExternalGraphic var1);

    public void visit(PointPlacement var1);

    public void visit(AnchorPoint var1);

    public void visit(Displacement var1);

    public void visit(LinePlacement var1);

    public void visit(Halo var1);

    public void visit(ColorMap var1);

    public void visit(ColorMapEntry var1);

    public void visit(FeatureTypeConstraint var1);

    public void visit(RemoteOWS var1);

    public void visit(ContrastEnhancement var1);

    public void visit(IDecorator var1);

    public void visit(ILabelResolver var1);

    public void visit(Font var1);

    public void visit(LabelPlacement var1);

    public void visit(ChannelSelection var1);

    public void visit(ShadedRelief var1);

    public void visit(Symbol var1);

    public void visit(WKTGraphic var1);

    public void visit(Extent var1);

    public void visit(SelectedChannelType var1);

    public void visit(Gradient var1);
}

