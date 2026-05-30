/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 */
package es.kosmo.core.styling.visitors;

import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.styling.Gradient;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
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

public class AbstractStyleVisitor
implements StyleVisitor {
    @Override
    public void visit(StyledLayerDescriptor styledLayerDescriptor) {
        if (styledLayerDescriptor == null) {
            return;
        }
        Object[] styledLayers = styledLayerDescriptor.getStyledLayers();
        if (!ArrayUtils.isEmpty((Object[])styledLayers)) {
            Object[] objectArray = styledLayers;
            int n = styledLayers.length;
            int n2 = 0;
            while (n2 < n) {
                Object sl = objectArray[n2];
                if (sl != null) {
                    sl.accept(this);
                }
                ++n2;
            }
        }
    }

    @Override
    public void visit(StyledLayer sl) {
        if (sl instanceof UserLayer) {
            ((UserLayer)sl).accept(this);
        } else if (sl instanceof NamedLayer) {
            ((NamedLayer)sl).accept(this);
        }
    }

    @Override
    public void visit(UserLayer userLayer) {
        Object[] styles;
        int n;
        Object[] ftConstraints;
        if (userLayer == null) {
            return;
        }
        if (userLayer.getRemoteOWS() != null) {
            userLayer.getRemoteOWS().accept(this);
        }
        if (!ArrayUtils.isEmpty((Object[])(ftConstraints = userLayer.getLayerFeatureConstraints()))) {
            Object[] objectArray = ftConstraints;
            n = ftConstraints.length;
            int n2 = 0;
            while (n2 < n) {
                Object ftConstraint = objectArray[n2];
                if (ftConstraint != null) {
                    ftConstraint.accept(this);
                }
                ++n2;
            }
        }
        if (!ArrayUtils.isEmpty((Object[])(styles = userLayer.getUserStyles()))) {
            Object[] objectArray = styles;
            int n3 = styles.length;
            n = 0;
            while (n < n3) {
                Object style = objectArray[n];
                style.accept(this);
                ++n;
            }
        }
    }

    @Override
    public void visit(NamedLayer namedLayer) {
        Object[] styles;
        int n;
        if (namedLayer == null) {
            return;
        }
        Object[] ftConstraints = namedLayer.getLayerFeatureConstraints();
        if (!ArrayUtils.isEmpty((Object[])ftConstraints)) {
            Object[] objectArray = ftConstraints;
            n = ftConstraints.length;
            int n2 = 0;
            while (n2 < n) {
                Object ftConstraint = objectArray[n2];
                if (ftConstraint != null) {
                    ftConstraint.accept(this);
                }
                ++n2;
            }
        }
        if (!ArrayUtils.isEmpty((Object[])(styles = namedLayer.getStyles()))) {
            Object[] objectArray = styles;
            int n3 = styles.length;
            n = 0;
            while (n < n3) {
                Object style = objectArray[n];
                if (style != null) {
                    style.accept(this);
                }
                ++n;
            }
        }
    }

    @Override
    public void visit(Style style) {
        if (style == null) {
            return;
        }
        Object[] ftsArray = style.getFeatureTypeStyles();
        if (!ArrayUtils.isEmpty((Object[])ftsArray)) {
            Object[] objectArray = ftsArray;
            int n = ftsArray.length;
            int n2 = 0;
            while (n2 < n) {
                Object fts = objectArray[n2];
                if (fts != null) {
                    fts.accept(this);
                }
                ++n2;
            }
        }
    }

    @Override
    public void visit(FeatureTypeStyle fts) {
        if (fts == null) {
            return;
        }
        Object[] rules = fts.getRules();
        if (!ArrayUtils.isEmpty((Object[])rules)) {
            Object[] objectArray = rules;
            int n = rules.length;
            int n2 = 0;
            while (n2 < n) {
                Object rule = objectArray[n2];
                if (rule != null) {
                    rule.accept(this);
                }
                ++n2;
            }
        }
    }

    @Override
    public void visit(Rule rule) {
        Object[] symbols;
        int n;
        if (rule == null) {
            return;
        }
        Object[] legends = rule.getLegendGraphic();
        if (!ArrayUtils.isEmpty((Object[])legends)) {
            Object[] objectArray = legends;
            n = legends.length;
            int n2 = 0;
            while (n2 < n) {
                Object legend = objectArray[n2];
                if (legend != null) {
                    legend.accept(this);
                }
                ++n2;
            }
        }
        if (!ArrayUtils.isEmpty((Object[])(symbols = rule.getSymbolizers()))) {
            Object[] objectArray = symbols;
            int n3 = symbols.length;
            n = 0;
            while (n < n3) {
                Object symbol = objectArray[n];
                if (symbol != null) {
                    symbol.accept(this);
                }
                ++n;
            }
        }
    }

    @Override
    public void visit(Symbolizer sym) {
        if (sym == null) {
            return;
        }
        if (sym instanceof PointSymbolizer) {
            ((PointSymbolizer)sym).accept(this);
        } else if (sym instanceof LineSymbolizer) {
            ((LineSymbolizer)sym).accept(this);
        } else if (sym instanceof PolygonSymbolizer) {
            ((PolygonSymbolizer)sym).accept(this);
        } else if (sym instanceof TextSymbolizer) {
            ((TextSymbolizer)sym).accept(this);
        } else if (sym instanceof RasterSymbolizer) {
            ((RasterSymbolizer)sym).accept(this);
        }
    }

    @Override
    public void visit(PointSymbolizer ps) {
        List<IDecorator> decorators;
        if (ps == null) {
            return;
        }
        if (ps.getGraphic() != null) {
            ps.getGraphic().accept(this);
        }
        if (CollectionUtils.isNotEmpty(decorators = ps.getDecorators())) {
            for (IDecorator decorator : decorators) {
                this.visit(decorator);
            }
        }
    }

    @Override
    public void visit(LineSymbolizer line) {
        List<IDecorator> decorators;
        if (line == null) {
            return;
        }
        if (line.getStroke() != null) {
            line.getStroke().accept(this);
        }
        if (CollectionUtils.isNotEmpty(decorators = line.getDecorators())) {
            for (IDecorator decorator : decorators) {
                this.visit(decorator);
            }
        }
    }

    @Override
    public void visit(PolygonSymbolizer poly) {
        List<IDecorator> decorators;
        if (poly == null) {
            return;
        }
        if (poly.getStroke() != null) {
            poly.getStroke().accept(this);
        }
        if (poly.getFill() != null) {
            poly.getFill().accept(this);
        }
        if (CollectionUtils.isNotEmpty(decorators = poly.getDecorators())) {
            for (IDecorator decorator : decorators) {
                this.visit(decorator);
            }
        }
    }

    @Override
    public void visit(TextSymbolizer text) {
        Object[] fonts;
        if (text == null) {
            return;
        }
        if (text.getFill() != null) {
            text.getFill().accept(this);
        }
        if (!ArrayUtils.isEmpty((Object[])(fonts = text.getFonts()))) {
            Object[] objectArray = fonts;
            int n = fonts.length;
            int n2 = 0;
            while (n2 < n) {
                Object font = objectArray[n2];
                if (font != null) {
                    font.accept(this);
                }
                ++n2;
            }
        }
        if (text.getGraphic() != null) {
            text.getGraphic().accept(this);
        }
        if (text.getHalo() != null) {
            text.getHalo().accept(this);
        }
        if (text.getLabelPlacement() != null) {
            text.getLabelPlacement().accept(this);
        }
        if (text.getLabelResolver() != null) {
            this.visit(text.getLabelResolver());
        }
    }

    @Override
    public void visit(RasterSymbolizer raster) {
        if (raster == null) {
            return;
        }
        if (raster.getChannelSelection() != null) {
            raster.getChannelSelection().accept(this);
        }
        if (raster.getColorMap() != null) {
            raster.getColorMap().accept(this);
        }
        if (raster.getContrastEnhancement() != null) {
            raster.getContrastEnhancement().accept(this);
        }
        if (raster.getImageOutline() != null) {
            raster.getImageOutline().accept(this);
        }
        if (raster.getShadedRelief() != null) {
            raster.getShadedRelief().accept(this);
        }
    }

    @Override
    public void visit(Fill fill) {
        if (fill == null) {
            return;
        }
        if (fill.getGraphicFill() != null) {
            fill.getGraphicFill().accept(this);
        }
    }

    @Override
    public void visit(Stroke stroke) {
        if (stroke == null) {
            return;
        }
        if (stroke.getGraphicFill() != null) {
            stroke.getGraphicFill().accept(this);
        }
        if (stroke.getGraphicStroke() != null) {
            stroke.getGraphicStroke().accept(this);
        }
    }

    @Override
    public void visit(Graphic gr) {
        Object[] symbols;
        Object[] marks;
        int n;
        Object[] externals;
        if (gr == null) {
            return;
        }
        if (gr.getDisplacement() != null) {
            gr.getDisplacement().accept(this);
        }
        if (!ArrayUtils.isEmpty((Object[])(externals = gr.getExternalGraphics()))) {
            Object[] objectArray = externals;
            n = externals.length;
            int n2 = 0;
            while (n2 < n) {
                Object external = objectArray[n2];
                if (external != null) {
                    external.accept(this);
                }
                ++n2;
            }
        }
        if (!ArrayUtils.isEmpty((Object[])(marks = gr.getMarks()))) {
            Object[] objectArray = marks;
            int n3 = marks.length;
            n = 0;
            while (n < n3) {
                Object mark = objectArray[n];
                if (mark != null) {
                    mark.accept(this);
                }
                ++n;
            }
        }
        if (!ArrayUtils.isEmpty((Object[])(symbols = gr.getSymbols()))) {
            Object[] objectArray = symbols;
            int n4 = symbols.length;
            int n5 = 0;
            while (n5 < n4) {
                Object symbol = objectArray[n5];
                if (symbol != null) {
                    symbol.accept(this);
                }
                ++n5;
            }
        }
    }

    @Override
    public void visit(Mark mark) {
        if (mark == null) {
            return;
        }
        if (mark.getFill() != null) {
            mark.getFill().accept(this);
        }
        if (mark.getStroke() != null) {
            mark.getStroke().accept(this);
        }
    }

    @Override
    public void visit(ExternalGraphic exgr) {
        if (exgr == null) {
            return;
        }
        if (exgr.getWKTGraphic() != null) {
            this.visit(exgr.getWKTGraphic());
        }
    }

    @Override
    public void visit(PointPlacement pp) {
        if (pp == null) {
            return;
        }
        if (pp.getDisplacement() != null) {
            pp.getDisplacement().accept(this);
        }
        if (pp.getAnchorPoint() != null) {
            pp.getAnchorPoint().accept(this);
        }
    }

    @Override
    public void visit(AnchorPoint ap) {
    }

    @Override
    public void visit(Displacement dis) {
    }

    @Override
    public void visit(LinePlacement lp) {
    }

    @Override
    public void visit(Halo halo) {
        if (halo == null) {
            return;
        }
        if (halo.getFill() != null) {
            halo.getFill().accept(this);
        }
    }

    @Override
    public void visit(ColorMap colorMap) {
        if (colorMap == null) {
            return;
        }
        Object[] entries = colorMap.getColorMapEntries();
        if (!ArrayUtils.isEmpty((Object[])entries)) {
            Object[] objectArray = entries;
            int n = entries.length;
            int n2 = 0;
            while (n2 < n) {
                Object entry = objectArray[n2];
                if (entry != null) {
                    entry.accept(this);
                }
                ++n2;
            }
        }
    }

    @Override
    public void visit(ColorMapEntry colorMapEntry) {
    }

    @Override
    public void visit(FeatureTypeConstraint ftConstraint) {
        if (ftConstraint == null) {
            return;
        }
        Object[] extents = ftConstraint.getExtents();
        if (!ArrayUtils.isEmpty((Object[])extents)) {
            Object[] objectArray = extents;
            int n = extents.length;
            int n2 = 0;
            while (n2 < n) {
                Object extent = objectArray[n2];
                if (extent != null) {
                    extent.accept(this);
                }
                ++n2;
            }
        }
    }

    @Override
    public void visit(RemoteOWS remoteOWS) {
    }

    @Override
    public void visit(ContrastEnhancement contrastEnhancement) {
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
        if (placement == null) {
            return;
        }
        if (placement instanceof PointPlacement) {
            ((PointPlacement)placement).accept(this);
        } else if (placement instanceof LinePlacement) {
            ((LinePlacement)placement).accept(this);
        }
    }

    @Override
    public void visit(ChannelSelection channelSelection) {
        Object[] selectedChannelTypes;
        int n;
        Object[] rgbChannelTypes;
        if (channelSelection == null) {
            return;
        }
        SelectedChannelType grayChannelType = channelSelection.getGrayChannel();
        if (grayChannelType != null) {
            grayChannelType.accept(this);
        }
        if (!ArrayUtils.isEmpty((Object[])(rgbChannelTypes = channelSelection.getRGBChannels()))) {
            Object[] objectArray = rgbChannelTypes;
            n = rgbChannelTypes.length;
            int n2 = 0;
            while (n2 < n) {
                Object rgbChannelType = objectArray[n2];
                if (rgbChannelType != null) {
                    rgbChannelType.accept(this);
                }
                ++n2;
            }
        }
        if (!ArrayUtils.isEmpty((Object[])(selectedChannelTypes = channelSelection.getSelectedChannels()))) {
            Object[] objectArray = selectedChannelTypes;
            int n3 = selectedChannelTypes.length;
            n = 0;
            while (n < n3) {
                Object selectedChannelType = objectArray[n];
                if (selectedChannelType != null) {
                    selectedChannelType.accept(this);
                }
                ++n;
            }
        }
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
        if (channelType == null) {
            return;
        }
        ContrastEnhancement contrast = channelType.getContrastEnhancement();
        if (contrast != null) {
            contrast.accept(this);
        }
    }

    @Override
    public void visit(Gradient gradient) {
    }
}

