/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.styling.Gradient;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterAttributeExtractor;
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
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.StyledLayer;
import org.saig.core.styling.StyledLayerDescriptor;
import org.saig.core.styling.Symbol;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.core.styling.UserLayer;
import org.saig.core.styling.WKTGraphic;

public class StyleAttributeExtractor
extends FilterAttributeExtractor
implements StyleVisitor {
    @Override
    public void visit(Style style) {
        FeatureTypeStyle[] ftStyles = style.getFeatureTypeStyles();
        int i = 0;
        while (i < ftStyles.length) {
            ftStyles[i].accept(this);
            ++i;
        }
    }

    @Override
    public void visit(Rule rule) {
        Symbolizer[] symbolizers;
        Filter filter = rule.getFilter();
        if (filter != null) {
            filter.accept(this);
        }
        if ((symbolizers = rule.getSymbolizers()) != null) {
            int i = 0;
            while (i < symbolizers.length) {
                Symbolizer symbolizer = symbolizers[i];
                symbolizer.accept(this);
                ++i;
            }
        }
        Graphic[] legendGraphics = rule.getLegendGraphic();
    }

    @Override
    public void visit(FeatureTypeStyle fts) {
        Rule[] rules = fts.getRules();
        int i = 0;
        while (i < rules.length) {
            Rule rule = rules[i];
            rule.accept(this);
            ++i;
        }
    }

    @Override
    public void visit(Fill fill) {
        if (fill.getBackgroundColor() != null) {
            fill.getBackgroundColor().accept(this);
        }
        if (fill.getColor() != null) {
            fill.getColor().accept(this);
        }
        if (fill.getGraphicFill() != null) {
            fill.getGraphicFill().accept(this);
        }
        if (fill.getOpacity() != null) {
            fill.getOpacity().accept(this);
        }
        if (fill.getGradientFill() != null) {
            fill.getGradientFill().accept(this);
        }
    }

    @Override
    public void visit(Stroke stroke) {
        if (stroke.getColor() != null) {
            stroke.getColor().accept(this);
        }
        if (stroke.getDashOffset() != null) {
            stroke.getDashOffset().accept(this);
        }
        if (stroke.getGraphicFill() != null) {
            stroke.getGraphicFill().accept(this);
        }
        if (stroke.getGraphicStroke() != null) {
            stroke.getGraphicStroke().accept(this);
        }
        if (stroke.getLineCap() != null) {
            stroke.getLineCap().accept(this);
        }
        if (stroke.getLineJoin() != null) {
            stroke.getLineJoin().accept(this);
        }
        if (stroke.getOpacity() != null) {
            stroke.getOpacity().accept(this);
        }
        if (stroke.getWidth() != null) {
            stroke.getWidth().accept(this);
        }
    }

    @Override
    public void visit(Symbolizer sym) {
        if (sym instanceof PointSymbolizer) {
            this.visit((PointSymbolizer)sym);
        }
        if (sym instanceof LineSymbolizer) {
            this.visit((LineSymbolizer)sym);
        }
        if (sym instanceof PolygonSymbolizer) {
            this.visit((PolygonSymbolizer)sym);
        }
        if (sym instanceof TextSymbolizer) {
            this.visit((TextSymbolizer)sym);
        }
        if (sym instanceof RasterSymbolizer) {
            this.visit((RasterSymbolizer)sym);
        }
    }

    @Override
    public void visit(RasterSymbolizer rs) {
        if (rs.getGeometryPropertyName() != null) {
            this.attributeNames.add(rs.getGeometryPropertyName());
        }
        if (rs.getImageOutline() != null) {
            rs.getImageOutline().accept(this);
        }
        if (rs.getOpacity() != null) {
            rs.getOpacity().accept(this);
        }
    }

    @Override
    public void visit(PointSymbolizer ps) {
        if (ps.getGeometryPropertyName() != null) {
            this.attributeNames.add(ps.getGeometryPropertyName());
        }
        if (ps.getGraphic() != null) {
            ps.getGraphic().accept(this);
        }
    }

    @Override
    public void visit(LineSymbolizer line) {
        if (line.getGeometryPropertyName() != null) {
            this.attributeNames.add(line.getGeometryPropertyName());
        }
        if (line.getStroke() != null) {
            line.getStroke().accept(this);
        }
    }

    @Override
    public void visit(PolygonSymbolizer poly) {
        if (poly.getGeometryPropertyName() != null) {
            this.attributeNames.add(poly.getGeometryPropertyName());
        }
        if (poly.getStroke() != null) {
            poly.getStroke().accept(this);
        }
        if (poly.getFill() != null) {
            poly.getFill().accept(this);
        }
    }

    @Override
    public void visit(TextSymbolizer text) {
        if (text.getGeometryPropertyName() != null) {
            this.attributeNames.add(text.getGeometryPropertyName());
        }
        if (text.getFill() != null) {
            text.getFill().accept(this);
        }
        if (text.getHalo() != null) {
            text.getHalo().accept(this);
        }
        if (text.getFonts() != null) {
            Font[] fonts = text.getFonts();
            int i = 0;
            while (i < fonts.length) {
                Font font = fonts[i];
                if (font.getFontFamily() != null) {
                    font.getFontFamily().accept(this);
                }
                if (font.getFontSize() != null) {
                    font.getFontSize().accept(this);
                }
                if (font.getFontStyle() != null) {
                    font.getFontStyle().accept(this);
                }
                if (font.getFontWeight() != null) {
                    font.getFontWeight().accept(this);
                }
                ++i;
            }
        }
        if (text.getHalo() != null) {
            text.getHalo().accept(this);
        }
        if (text.getLabel() != null) {
            text.getLabel().accept(this);
        }
        if (text.getLabelPlacement() != null) {
            text.getLabelPlacement().accept(this);
        }
        if (text.getHeightAttribute() != null) {
            text.getHeightAttribute().accept(this);
        }
        if (text.getGraphic() != null) {
            text.getGraphic().accept(this);
        }
        if (text.getPriority() != null) {
            text.getPriority().accept(this);
        }
    }

    @Override
    public void visit(Graphic gr) {
        if (gr.getSymbols() != null) {
            Symbol[] symbols = gr.getSymbols();
            int i = 0;
            while (i < symbols.length) {
                Symbol symbol = symbols[i];
                symbol.accept(this);
                ++i;
            }
        }
        if (gr.getOpacity() != null) {
            gr.getOpacity().accept(this);
        }
        if (gr.getRotation() != null) {
            gr.getRotation().accept(this);
        }
        if (gr.getSize() != null) {
            gr.getSize().accept(this);
        }
    }

    @Override
    public void visit(Mark mark) {
        if (mark.getFill() != null) {
            mark.getFill().accept(this);
        }
        if (mark.getStroke() != null) {
            mark.getStroke().accept(this);
        }
        if (mark.getRotation() != null) {
            mark.getRotation().accept(this);
        }
        if (mark.getSize() != null) {
            mark.getSize().accept(this);
        }
    }

    @Override
    public void visit(ExternalGraphic exgr) {
    }

    @Override
    public void visit(PointPlacement pp) {
        if (pp.getAnchorPoint() != null) {
            pp.getAnchorPoint().accept(this);
        }
        if (pp.getDisplacement() != null) {
            pp.getDisplacement().accept(this);
        }
        if (pp.getRotation() != null) {
            pp.getRotation().accept(this);
        }
    }

    @Override
    public void visit(AnchorPoint ap) {
        if (ap.getAnchorPointX() != null) {
            ap.getAnchorPointX().accept(this);
        }
        if (ap.getAnchorPointY() != null) {
            ap.getAnchorPointY().accept(this);
        }
    }

    @Override
    public void visit(Displacement dis) {
        if (dis.getDisplacementX() != null) {
            dis.getDisplacementX().accept(this);
        }
        if (dis.getDisplacementY() != null) {
            dis.getDisplacementY().accept(this);
        }
    }

    @Override
    public void visit(LinePlacement lp) {
        if (lp.getPerpendicularOffset() != null) {
            lp.getPerpendicularOffset().accept(this);
        }
    }

    @Override
    public void visit(Halo halo) {
        if (halo.getFill() != null) {
            halo.getFill().accept(this);
        }
        if (halo.getRadius() != null) {
            halo.getRadius().accept(this);
        }
    }

    @Override
    public void visit(ColorMap colorMap) {
    }

    @Override
    public void visit(ColorMapEntry colorMapEntry) {
    }

    @Override
    public void visit(StyledLayerDescriptor styledLayerDescriptor) {
    }

    @Override
    public void visit(UserLayer userLayer) {
    }

    @Override
    public void visit(FeatureTypeConstraint ftConstraint) {
    }

    @Override
    public void visit(NamedLayer namedLayer) {
    }

    @Override
    public void visit(ContrastEnhancement contrastEnhancement) {
    }

    @Override
    public void visit(StyledLayer sl) {
    }

    @Override
    public void visit(RemoteOWS remoteOWS) {
    }

    @Override
    public void visit(IDecorator decorator) {
    }

    @Override
    public void visit(ILabelResolver resolver) {
    }

    @Override
    public void visit(Font font) {
    }

    @Override
    public void visit(LabelPlacement placement) {
    }

    @Override
    public void visit(ChannelSelection channelSelection) {
    }

    @Override
    public void visit(ShadedRelief shadedRelief) {
    }

    @Override
    public void visit(Symbol symbol) {
    }

    @Override
    public void visit(WKTGraphic wktGraphic) {
    }

    @Override
    public void visit(Extent extent) {
    }

    @Override
    public void visit(SelectedChannelType channelType) {
    }

    @Override
    public void visit(Gradient gradient) {
    }
}

