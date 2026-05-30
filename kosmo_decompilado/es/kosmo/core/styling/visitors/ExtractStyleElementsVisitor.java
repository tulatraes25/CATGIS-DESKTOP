/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.styling.visitors;

import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.styling.visitors.AbstractStyleVisitor;
import java.util.ArrayList;
import java.util.List;
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

public class ExtractStyleElementsVisitor<T>
extends AbstractStyleVisitor {
    protected List<T> elements = new ArrayList<T>();
    private Class<T> clazz;

    public ExtractStyleElementsVisitor(Class<T> tClass) {
        this.clazz = tClass;
    }

    public List<T> getElements() {
        return this.elements;
    }

    @Override
    public void visit(StyledLayerDescriptor styledLayerDescriptor) {
        if (styledLayerDescriptor == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(styledLayerDescriptor.getClass())) {
            this.elements.add(styledLayerDescriptor);
        } else {
            super.visit(styledLayerDescriptor);
        }
    }

    @Override
    public void visit(StyledLayer sl) {
        if (sl == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(sl.getClass())) {
            this.elements.add(sl);
        } else {
            super.visit(sl);
        }
    }

    @Override
    public void visit(UserLayer userLayer) {
        if (userLayer == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(userLayer.getClass())) {
            this.elements.add(userLayer);
        } else {
            super.visit(userLayer);
        }
    }

    @Override
    public void visit(NamedLayer namedLayer) {
        if (namedLayer == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(namedLayer.getClass())) {
            this.elements.add(namedLayer);
        } else {
            super.visit(namedLayer);
        }
    }

    @Override
    public void visit(Style style) {
        if (style == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(style.getClass())) {
            this.elements.add(style);
        } else {
            super.visit(style);
        }
    }

    @Override
    public void visit(FeatureTypeStyle fts) {
        if (fts == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(fts.getClass())) {
            this.elements.add(fts);
        } else {
            super.visit(fts);
        }
    }

    @Override
    public void visit(Rule rule) {
        if (rule == null || !rule.isEnabled()) {
            return;
        }
        if (this.clazz.isAssignableFrom(rule.getClass())) {
            this.elements.add(rule);
        } else {
            super.visit(rule);
        }
    }

    @Override
    public void visit(Symbolizer sym) {
        if (sym == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(sym.getClass())) {
            this.elements.add(sym);
        } else {
            super.visit(sym);
        }
    }

    @Override
    public void visit(PointSymbolizer ps) {
        if (ps == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(ps.getClass())) {
            this.elements.add(ps);
        } else {
            super.visit(ps);
        }
    }

    @Override
    public void visit(LineSymbolizer line) {
        if (line == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(line.getClass())) {
            this.elements.add(line);
        } else {
            super.visit(line);
        }
    }

    @Override
    public void visit(PolygonSymbolizer poly) {
        if (poly == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(poly.getClass())) {
            this.elements.add(poly);
        } else {
            super.visit(poly);
        }
    }

    @Override
    public void visit(TextSymbolizer text) {
        if (text == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(text.getClass())) {
            this.elements.add(text);
        } else {
            super.visit(text);
        }
    }

    @Override
    public void visit(RasterSymbolizer raster) {
        if (raster == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(raster.getClass())) {
            this.elements.add(raster);
        } else {
            super.visit(raster);
        }
    }

    @Override
    public void visit(Fill fill) {
        if (fill == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(fill.getClass())) {
            this.elements.add(fill);
        } else {
            super.visit(fill);
        }
    }

    @Override
    public void visit(Stroke stroke) {
        if (stroke == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(stroke.getClass())) {
            this.elements.add(stroke);
        } else {
            super.visit(stroke);
        }
    }

    @Override
    public void visit(Graphic gr) {
        if (gr == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(gr.getClass())) {
            this.elements.add(gr);
        } else {
            super.visit(gr);
        }
    }

    @Override
    public void visit(Mark mark) {
        if (mark == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(mark.getClass())) {
            this.elements.add(mark);
        } else {
            super.visit(mark);
        }
    }

    @Override
    public void visit(ExternalGraphic exgr) {
        if (exgr == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(exgr.getClass())) {
            this.elements.add(exgr);
        } else {
            super.visit(exgr);
        }
    }

    @Override
    public void visit(PointPlacement pp) {
        if (pp == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(pp.getClass())) {
            this.elements.add(pp);
        } else {
            super.visit(pp);
        }
    }

    @Override
    public void visit(AnchorPoint ap) {
        if (ap == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(ap.getClass())) {
            this.elements.add(ap);
        } else {
            super.visit(ap);
        }
    }

    @Override
    public void visit(Displacement dis) {
        if (dis == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(dis.getClass())) {
            this.elements.add(dis);
        } else {
            super.visit(dis);
        }
    }

    @Override
    public void visit(LinePlacement lp) {
        if (lp == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(lp.getClass())) {
            this.elements.add(lp);
        } else {
            super.visit(lp);
        }
    }

    @Override
    public void visit(Halo halo) {
        if (halo == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(halo.getClass())) {
            this.elements.add(halo);
        } else {
            super.visit(halo);
        }
    }

    @Override
    public void visit(ColorMap colorMap) {
        if (colorMap == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(colorMap.getClass())) {
            this.elements.add(colorMap);
        } else {
            super.visit(colorMap);
        }
    }

    @Override
    public void visit(ColorMapEntry colorMapEntry) {
        if (colorMapEntry == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(colorMapEntry.getClass())) {
            this.elements.add(colorMapEntry);
        } else {
            super.visit(colorMapEntry);
        }
    }

    @Override
    public void visit(FeatureTypeConstraint ftConstraint) {
        if (ftConstraint == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(ftConstraint.getClass())) {
            this.elements.add(ftConstraint);
        } else {
            super.visit(ftConstraint);
        }
    }

    @Override
    public void visit(RemoteOWS remoteOWS) {
        if (remoteOWS == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(remoteOWS.getClass())) {
            this.elements.add(remoteOWS);
        } else {
            super.visit(remoteOWS);
        }
    }

    @Override
    public void visit(ContrastEnhancement contrastEnhancement) {
        if (contrastEnhancement == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(contrastEnhancement.getClass())) {
            this.elements.add(contrastEnhancement);
        } else {
            super.visit(contrastEnhancement);
        }
    }

    @Override
    public void visit(IDecorator decorator) {
        if (decorator == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(decorator.getClass())) {
            this.elements.add(decorator);
        } else {
            super.visit(decorator);
        }
    }

    @Override
    public void visit(ILabelResolver resolver) {
        if (resolver == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(resolver.getClass())) {
            this.elements.add(resolver);
        } else {
            super.visit(resolver);
        }
    }

    @Override
    public void visit(Font font) {
        if (font == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(font.getClass())) {
            this.elements.add(font);
        } else {
            super.visit(font);
        }
    }

    @Override
    public void visit(LabelPlacement placement) {
        if (placement == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(placement.getClass())) {
            this.elements.add(placement);
        } else {
            super.visit(placement);
        }
    }

    @Override
    public void visit(ChannelSelection channelSelection) {
        if (channelSelection == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(channelSelection.getClass())) {
            this.elements.add(channelSelection);
        } else {
            super.visit(channelSelection);
        }
    }

    @Override
    public void visit(ShadedRelief shadedRelief) {
        if (shadedRelief == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(shadedRelief.getClass())) {
            this.elements.add(shadedRelief);
        } else {
            super.visit(shadedRelief);
        }
    }

    @Override
    public void visit(Symbol symbol) {
        if (symbol == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(symbol.getClass())) {
            this.elements.add(symbol);
        } else {
            super.visit(symbol);
        }
    }

    @Override
    public void visit(WKTGraphic wktGraphic) {
        if (wktGraphic == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(wktGraphic.getClass())) {
            this.elements.add(wktGraphic);
        } else {
            super.visit(wktGraphic);
        }
    }

    @Override
    public void visit(Extent extent) {
        if (extent == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(extent.getClass())) {
            this.elements.add(extent);
        } else {
            super.visit(extent);
        }
    }

    @Override
    public void visit(SelectedChannelType channelType) {
        if (channelType == null) {
            return;
        }
        if (this.clazz.isAssignableFrom(channelType.getClass())) {
            this.elements.add(channelType);
        } else {
            super.visit(channelType);
        }
    }
}

