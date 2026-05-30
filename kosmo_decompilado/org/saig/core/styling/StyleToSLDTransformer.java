/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.geotools.xml.transform.TransformerBase
 *  org.geotools.xml.transform.TransformerBase$TranslatorSupport
 *  org.geotools.xml.transform.Translator
 */
package org.saig.core.styling;

import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.styling.Gradient;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.geotools.xml.transform.TransformerBase;
import org.geotools.xml.transform.Translator;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterTransformer;
import org.saig.core.filter.parser.ParseException;
import org.saig.core.renderer.style.SLDStyleFactory;
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
import org.saig.core.styling.NamedStyle;
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
import org.saig.jump.lang.I18N;
import org.saig.jump.util.MeasureUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class StyleToSLDTransformer
extends TransformerBase {
    private static final Logger LOGGER = Logger.getLogger(StyleToSLDTransformer.class);
    public static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";
    public static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";
    public static final String DEFAULT_ABSTRACT = "Abstract";
    private final Map<URI, String> uri2prefix;
    public boolean useSimpleLiterals = true;
    public boolean exportUOMAttributes = false;
    public String sldVersion = "1.0";

    public StyleToSLDTransformer() {
        this(true);
    }

    public StyleToSLDTransformer(boolean simpleLiterals) {
        this(null);
        this.useSimpleLiterals = simpleLiterals;
    }

    public StyleToSLDTransformer(String version, boolean simpleLiterals, boolean exportUOMAttrs) {
        this(null);
        this.sldVersion = version;
        this.useSimpleLiterals = simpleLiterals;
        this.exportUOMAttributes = !"1.0".equals(version) || exportUOMAttrs;
    }

    public StyleToSLDTransformer(Map<URI, String> nsBindings) {
        if (nsBindings == null || nsBindings.isEmpty()) {
            this.uri2prefix = new HashMap<URI, String>();
        } else {
            this.uri2prefix = new HashMap<URI, String>(nsBindings.size());
            int count = 0;
            for (Map.Entry<URI, String> e : nsBindings.entrySet()) {
                URI uri = e.getKey();
                String prefix = e.getValue();
                if (uri == null || prefix == null) continue;
                this.uri2prefix.put(uri, prefix.trim());
                ++count;
            }
            LOGGER.info((Object)I18N.getMessage(((Object)((Object)this)).getClass(), "added-{0}-namespace-entries-resulting-in-{1}-distinct-entries", new Object[]{count, this.uri2prefix.size()}));
        }
    }

    public Translator createTranslator(ContentHandler handler) {
        SLDTranslator result = new SLDTranslator(handler, this.useSimpleLiterals, this.exportUOMAttributes);
        if (!this.uri2prefix.isEmpty()) {
            for (Map.Entry<URI, String> e : this.uri2prefix.entrySet()) {
                URI uri = e.getKey();
                if (uri == null) continue;
                String prefix = e.getValue();
                String uriStr = String.valueOf(uri);
                result.getNamespaceSupport().declarePrefix(prefix, uriStr);
            }
        }
        return result;
    }

    static class SLDTranslator
    extends TransformerBase.TranslatorSupport
    implements StyleVisitor {
        FilterTransformer.FilterTranslator filterTranslator;
        boolean exportUOMAttributes = false;

        public SLDTranslator(ContentHandler handler) {
            this(handler, true, false);
        }

        public SLDTranslator(ContentHandler handler, boolean writeSimpleLiterals, boolean exportUOMAttrs) {
            super(handler, "sld", "http://www.opengis.net/sld");
            this.filterTranslator = new FilterTransformer.FilterTranslator(handler, writeSimpleLiterals);
            this.addNamespaceDeclarations(this.filterTranslator);
            this.exportUOMAttributes = exportUOMAttrs;
        }

        void element(String element, Expression e) {
            this.start(element);
            this.filterTranslator.encode(e);
            this.end(element);
        }

        void element(String element, Filter f) {
            this.start(element);
            this.filterTranslator.encode(f);
            this.end(element);
        }

        @Override
        public void visit(PointPlacement pp) {
            this.start("LabelPlacement");
            this.start("PointPlacement");
            pp.getAnchorPoint().accept(this);
            pp.getDisplacement().accept(this);
            this.element("Rotation", pp.getRotation());
            this.end("PointPlacement");
            this.end("LabelPlacement");
        }

        @Override
        public void visit(Stroke stroke) {
            this.start("Stroke");
            if (stroke.getGraphicFill() != null) {
                this.start("GraphicFill");
                stroke.getGraphicFill().accept(this);
                this.end("GraphicFill");
            }
            if (stroke.getGraphicStroke() != null) {
                this.start("GraphicStroke");
                stroke.getGraphicStroke().accept(this);
                this.end("GraphicStroke");
            }
            this.encodeCssParam("stroke", stroke.getColor());
            this.encodeCssParam("stroke-linecap", stroke.getLineCap());
            this.encodeCssParam("stroke-linejoin", stroke.getLineJoin());
            this.encodeCssParam("stroke-opacity", stroke.getOpacity());
            this.encodeCssParam("stroke-width", stroke.getWidth());
            this.encodeCssParam("stroke-dashoffset", stroke.getDashOffset());
            float[] dash = stroke.getDashArray();
            StringBuffer sb = new StringBuffer();
            int i = 0;
            while (i < dash.length) {
                sb.append(String.valueOf(dash[i]) + " ");
                ++i;
            }
            this.encodeCssParam("stroke-dasharray", sb.toString());
            this.end("Stroke");
        }

        @Override
        public void visit(LinePlacement lp) {
            this.start("LabelPlacement");
            this.start("LinePlacement");
            this.element("PerpendicularOffset", lp.getPerpendicularOffset());
            this.end("LinePlacement");
            this.end("LabelPlacement");
        }

        @Override
        public void visit(AnchorPoint ap) {
            this.start("AnchorPoint");
            this.element("AnchorPointX", ap.getAnchorPointX());
            this.element("AnchorPointY", ap.getAnchorPointY());
            this.end("AnchorPoint");
        }

        @Override
        public void visit(TextSymbolizer text) {
            if (text == null) {
                return;
            }
            if (StringUtils.isNotEmpty((String)text.getUnitsOfMeasurement())) {
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "uom", "uom", "", this.getUomFromUnits(text.getUnitsOfMeasurement()));
                this.start("TextSymbolizer", atts);
            } else {
                this.start("TextSymbolizer");
            }
            if (text.getGeometryPropertyName() != null) {
                this.encodeGeometryProperty(text.getGeometryPropertyName());
            }
            if (text.getLabel() != null) {
                this.element("Label", text.getLabel());
            }
            if (text.getFonts() != null && text.getFonts().length != 0) {
                this.start("Font");
                Font[] fonts = text.getFonts();
                int i = 0;
                while (i < fonts.length) {
                    this.encodeCssParam("font-family", fonts[i].getFontFamily());
                    ++i;
                }
                this.encodeCssParam("font-size", fonts[0].getFontSize());
                this.encodeCssParam("font-style", fonts[0].getFontStyle());
                this.encodeCssParam("font-weight", fonts[0].getFontWeight());
                try {
                    this.encodeCssParam("font-color", text.getFill() != null ? text.getFill().getColor() : (Expression)ExpressionBuilder.parse("#000000"));
                }
                catch (ParseException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                this.end("Font");
            }
            if (text.getLabelPlacement() != null) {
                text.getLabelPlacement().accept(this);
            }
            if (text.getHalo() != null) {
                text.getHalo().accept(this);
            }
            if (text.getFill() != null) {
                text.getFill().accept(this);
            }
            if (text.getOptions() != null) {
                this.encodeVendorOptions(text.getOptions());
            }
            if (text.getPriority() != null) {
                this.start("Priority");
                this.element("PropertyName", text.getPriority());
                this.end("Priority");
            }
            this.end("TextSymbolizer");
        }

        @Override
        public void visit(RasterSymbolizer raster) {
            if (raster == null) {
                return;
            }
            this.start("RasterSymbolizer");
            if (raster.getGeometryPropertyName() != null) {
                this.encodeGeometryProperty(raster.getGeometryPropertyName());
            }
            if (raster.getOpacity() != null) {
                this.start("Opacity");
                this.filterTranslator.encode(raster.getOpacity());
                this.end("Opacity");
            }
            if (raster.getOverlap() != null) {
                this.start("OverlapBehavior");
                this.filterTranslator.encode(raster.getOverlap());
                this.end("OverlapBehavior");
            }
            if (raster.getColorMap() != null) {
                raster.getColorMap().accept(this);
            }
            this.end("RasterSymbolizer");
        }

        @Override
        public void visit(ColorMap colorMap) {
            ColorMapEntry[] mapEntries = colorMap.getColorMapEntries();
            this.start("ColorMap");
            int i = 0;
            while (i < mapEntries.length) {
                mapEntries[i].accept(this);
                ++i;
            }
            this.end("ColorMap");
        }

        @Override
        public void visit(ColorMapEntry colorEntry) {
            if (colorEntry != null) {
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "color", "color", "", colorEntry.getColor().toString());
                if (colorEntry.getOpacity() != null) {
                    atts.addAttribute("", "opacity", "opacity", "", colorEntry.getOpacity().toString());
                }
                if (colorEntry.getQuantity() != null) {
                    atts.addAttribute("", "quantity", "quantity", "", colorEntry.getQuantity().toString());
                }
                if (colorEntry.getLabel() != null) {
                    atts.addAttribute("", "label", "label", "", colorEntry.getLabel());
                }
                this.element("ColorMapEntry", null, atts);
            }
        }

        @Override
        public void visit(Symbolizer sym) {
            try {
                this.contentHandler.startElement("", "!--", "!--", this.NULL_ATTS);
                this.chars("Unidentified Symbolizer " + sym.getClass());
                this.contentHandler.endElement("", "--", "--");
            }
            catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }

        @Override
        public void visit(PolygonSymbolizer poly) {
            if (StringUtils.isNotEmpty((String)poly.getUnitsOfMeasurement()) && this.exportUOMAttributes) {
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "uom", "uom", "", this.getUomFromUnits(poly.getUnitsOfMeasurement()));
                this.start("PolygonSymbolizer", atts);
            } else {
                this.start("PolygonSymbolizer");
            }
            this.encodeGeometryProperty(poly.getGeometryPropertyName());
            if (poly.getFill() != null) {
                poly.getFill().accept(this);
            }
            if (poly.getStroke() != null) {
                poly.getStroke().accept(this);
            }
            this.end("PolygonSymbolizer");
        }

        @Override
        public void visit(ExternalGraphic exgr) {
            this.start("ExternalGraphic");
            AttributesImpl atts = new AttributesImpl();
            try {
                atts.addAttribute(StyleToSLDTransformer.XMLNS_NAMESPACE, "xlink", "xmlns:xlink", "", StyleToSLDTransformer.XLINK_NAMESPACE);
                atts.addAttribute(StyleToSLDTransformer.XLINK_NAMESPACE, "type", "xlink:type", "", "simple");
                atts.addAttribute(StyleToSLDTransformer.XLINK_NAMESPACE, "xlink", "xlink:href", "", exgr.getLocation().toURI().toString());
            }
            catch (Exception murle) {
                throw new Error(I18N.getString(this.getClass(), "someone-coded-the-x-ling-namespace-wrong"));
            }
            this.element("OnlineResource", null, atts);
            this.element("Format", exgr.getFormat());
            this.end("ExternalGraphic");
        }

        @Override
        public void visit(LineSymbolizer line) {
            if (StringUtils.isNotEmpty((String)line.getUnitsOfMeasurement()) && this.exportUOMAttributes) {
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "uom", "uom", "", this.getUomFromUnits(line.getUnitsOfMeasurement()));
                this.start("LineSymbolizer", atts);
            } else {
                this.start("LineSymbolizer");
            }
            this.encodeGeometryProperty(line.getGeometryPropertyName());
            if (line.getStroke() != null) {
                line.getStroke().accept(this);
            }
            this.end("LineSymbolizer");
        }

        @Override
        public void visit(Fill fill) {
            this.start("Fill");
            if (fill.getGraphicFill() != null) {
                this.start("GraphicFill");
                fill.getGraphicFill().accept(this);
                this.end("GraphicFill");
            }
            this.encodeCssParam("fill", fill.getColor());
            this.encodeCssParam("fill-opacity", fill.getOpacity());
            this.end("Fill");
        }

        @Override
        public void visit(Rule rule) {
            Object[] sym;
            Object[] gr;
            this.start("Rule");
            if (StringUtils.isNotEmpty((String)rule.getName())) {
                this.element("Name", rule.getName());
            }
            if (StringUtils.isNotEmpty((String)rule.getTitle())) {
                this.element("Title", rule.getTitle());
            }
            if (StringUtils.isNotEmpty((String)rule.getAbstract())) {
                this.element(StyleToSLDTransformer.DEFAULT_ABSTRACT, rule.getAbstract());
            }
            if (!ArrayUtils.isEmpty((Object[])(gr = rule.getLegendGraphic()))) {
                int i = 0;
                while (i < gr.length) {
                    this.start("LegendGraphic");
                    gr[i].accept(this);
                    this.end("LegendGraphic");
                    ++i;
                }
            }
            if (rule.getFilter() != null) {
                this.filterTranslator.encode(rule.getFilter());
            }
            if (rule.isElseFilter()) {
                this.start("ElseFilter");
                this.end("ElseFilter");
            }
            if (rule.getMinScaleDenominator() != 0.0) {
                this.element("MinScaleDenominator", String.valueOf(rule.getMinScaleDenominator()));
            }
            if (rule.getMaxScaleDenominator() != Double.POSITIVE_INFINITY) {
                this.element("MaxScaleDenominator", String.valueOf(rule.getMaxScaleDenominator()));
            }
            if (!ArrayUtils.isEmpty((Object[])(sym = rule.getSymbolizers()))) {
                int i = 0;
                while (i < sym.length) {
                    sym[i].accept(this);
                    ++i;
                }
            }
            this.end("Rule");
        }

        @Override
        public void visit(Mark mark) {
            this.start("Mark");
            if (mark.getWellKnownName() != null) {
                if (SLDStyleFactory.wellKnownMarks.contains(mark.getWellKnownName())) {
                    this.element("WellKnownName", mark.getWellKnownName().toString());
                } else {
                    this.element("WellKnownName", mark.getWellKnownName().toString());
                }
            }
            if (mark.getFill() != null) {
                mark.getFill().accept(this);
            }
            if (mark.getStroke() != null) {
                mark.getStroke().accept(this);
            }
            this.end("Mark");
        }

        @Override
        public void visit(PointSymbolizer ps) {
            if (StringUtils.isNotEmpty((String)ps.getUnitsOfMeasurement()) && this.exportUOMAttributes) {
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "uom", "uom", "", this.getUomFromUnits(ps.getUnitsOfMeasurement()));
                this.start("PointSymbolizer", atts);
            } else {
                this.start("PointSymbolizer");
            }
            this.encodeGeometryProperty(ps.getGeometryPropertyName());
            ps.getGraphic().accept(this);
            this.end("PointSymbolizer");
        }

        @Override
        public void visit(Halo halo) {
            this.start("Halo");
            if (halo.getRadius() != null) {
                this.start("Radius");
                this.filterTranslator.encode(halo.getRadius());
                this.end("Radius");
            }
            if (halo.getFill() != null) {
                halo.getFill().accept(this);
            }
            this.end("Halo");
        }

        @Override
        public void visit(Graphic gr) {
            this.start("Graphic");
            this.encodeGeometryProperty(gr.getGeometryPropertyName());
            Symbol[] symbols = gr.getSymbols();
            int i = 0;
            while (i < symbols.length) {
                symbols[i].accept(this);
                ++i;
            }
            this.element("Opacity", gr.getOpacity());
            this.element("Size", gr.getSize());
            this.element("Rotation", gr.getRotation());
            this.end("Graphic");
        }

        @Override
        public void visit(StyledLayerDescriptor sld) {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "version", "version", "", "1.0.0");
            this.start("StyledLayerDescriptor", atts);
            if (sld.getName() != null && sld.getName().length() > 0) {
                this.element("Name", sld.getName());
            }
            if (sld.getTitle() != null && sld.getTitle().length() > 0) {
                this.element("Title", sld.getTitle());
            }
            if (sld.getAbstract() != null && sld.getAbstract().length() > 0) {
                this.element(StyleToSLDTransformer.DEFAULT_ABSTRACT, sld.getAbstract());
            }
            StyledLayer[] layers = sld.getStyledLayers();
            int i = 0;
            while (i < layers.length) {
                if (layers[i] instanceof NamedLayer) {
                    this.visit((NamedLayer)layers[i]);
                } else if (layers[i] instanceof UserLayer) {
                    this.visit((UserLayer)layers[i]);
                } else {
                    throw new IllegalArgumentException("StyledLayer '" + layers[i].getClass().toString() + "' not found");
                }
                ++i;
            }
            this.end("StyledLayerDescriptor");
        }

        @Override
        public void visit(NamedLayer layer) {
            this.start("NamedLayer");
            this.element("Name", layer.getName());
            Object[] lfc = layer.getLayerFeatureConstraints();
            this.start("LayerFeatureConstraints");
            if (!ArrayUtils.isEmpty((Object[])lfc)) {
                int i = 0;
                while (i < lfc.length) {
                    this.visit((FeatureTypeConstraint)lfc[i]);
                    ++i;
                }
            } else {
                this.element("FeatureTypeConstraint", "");
            }
            this.end("LayerFeatureConstraints");
            Style[] styles = layer.getStyles();
            int i = 0;
            while (i < styles.length) {
                this.visit(styles[i]);
                ++i;
            }
            this.end("NamedLayer");
        }

        @Override
        public void visit(UserLayer layer) {
            this.start("UserLayer");
            if (layer.getName() != null && layer.getName().length() > 0) {
                this.element("Name", layer.getName());
            }
            this.start("LayerFeatureConstraints");
            FeatureTypeConstraint[] lfc = layer.getLayerFeatureConstraints();
            if (lfc != null && lfc.length > 0) {
                int i = 0;
                while (i < lfc.length) {
                    this.visit(lfc[i]);
                    ++i;
                }
            } else {
                this.start("FeatureTypeConstraint");
                this.end("FeatureTypeConstraint");
            }
            this.end("LayerFeatureConstraints");
            Style[] styles = layer.getUserStyles();
            int i = 0;
            while (i < styles.length) {
                this.visit(styles[i]);
                ++i;
            }
            this.end("UserLayer");
        }

        @Override
        public void visit(RemoteOWS remoteOWS) {
            this.start("RemoteOWS");
            this.element("Service", remoteOWS.getService());
            this.element("OnlineResource", remoteOWS.getOnlineResource());
            this.end("RemoteOWS");
        }

        @Override
        public void visit(FeatureTypeConstraint ftc) {
            this.start("FeatureTypeConstraint");
            if (ftc != null) {
                this.element("FeatureTypeName", ftc.getFeatureTypeName());
                this.visit(ftc.getFilter());
            }
            this.end("FeatureTypeConstraint");
        }

        public void visit(Filter filter) {
        }

        @Override
        public void visit(Style style) {
            if (style instanceof NamedStyle) {
                this.start("NamedStyle");
                this.element("Name", style.getName());
                this.end("NamedStyle");
            } else {
                this.start("UserStyle");
                this.element("Name", style.getName());
                this.element("Title", style.getTitle());
                if (StringUtils.isNotEmpty((String)style.getAbstract())) {
                    this.element(StyleToSLDTransformer.DEFAULT_ABSTRACT, style.getAbstract());
                }
                FeatureTypeStyle[] fts = style.getFeatureTypeStyles();
                int i = 0;
                while (i < fts.length) {
                    this.visit(fts[i]);
                    ++i;
                }
                this.end("UserStyle");
            }
        }

        @Override
        public void visit(FeatureTypeStyle fts) {
            this.start("FeatureTypeStyle");
            if (StringUtils.isNotEmpty((String)fts.getName())) {
                this.element("Name", fts.getName());
            }
            if (StringUtils.isNotEmpty((String)fts.getTitle())) {
                this.element("Title", fts.getTitle());
            }
            if (StringUtils.isNotEmpty((String)fts.getAbstract())) {
                this.element(StyleToSLDTransformer.DEFAULT_ABSTRACT, fts.getAbstract());
            }
            if (StringUtils.isNotEmpty((String)fts.getFeatureTypeName())) {
                this.element("FeatureTypeName", fts.getFeatureTypeName());
            }
            Rule[] rules = fts.getRules();
            int i = 0;
            while (i < rules.length) {
                rules[i].accept(this);
                ++i;
            }
            this.end("FeatureTypeStyle");
        }

        @Override
        public void visit(Displacement dis) {
            this.start("Displacement");
            this.element("DisplacementX", dis.getDisplacementX());
            this.element("DisplacementY", dis.getDisplacementY());
            this.end("Displacement");
        }

        @Override
        public void visit(ContrastEnhancement contrastEnhancement) {
        }

        String getUomFromUnits(String unitsOfMeasurement) {
            if (unitsOfMeasurement.equals(MeasureUtils.AceptedLengthUnits.METER.getUnit().toString())) {
                return "http://www.opengeospatial.org/se/units/metre";
            }
            if (unitsOfMeasurement.equals(MeasureUtils.AceptedLengthUnits.FOOT.getUnit().toString())) {
                return "http://www.opengeospatial.org/se/units/foot";
            }
            if (unitsOfMeasurement.equals("pixel")) {
                return "http://www.opengeospatial.org/se/units/pixel";
            }
            return unitsOfMeasurement;
        }

        void encodeGeometryProperty(String name) {
            if (name == null || name.trim().length() == 0) {
                return;
            }
            this.start("Geometry");
            this.element("PropertyName", name);
            this.end("Geometry");
        }

        void encodeCssParam(String name, Expression expression) {
            if (expression == null) {
                return;
            }
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "name", "name", "", name);
            this.start("CssParameter", atts);
            this.filterTranslator.encode(expression);
            this.end("CssParameter");
        }

        void encodeCssParam(String name, String expression) {
            if (expression.length() == 0) {
                return;
            }
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "name", "name", "", name);
            this.start("CssParameter", atts);
            this.chars(expression);
            this.end("CssParameter");
        }

        void encodeVendorOptions(Map<String, String> options) {
            if (options != null) {
                for (String key : options.keySet()) {
                    String value = options.get(key);
                    this.encodeVendorOption(key, value);
                }
            }
        }

        void encodeVendorOption(String key, String value) {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "name", "name", "", key);
            this.start("VendorOption", atts);
            this.chars(value);
            this.end("VendorOption");
        }

        public void encode(Style[] styles) {
            try {
                this.contentHandler.startDocument();
                this.start("StyledLayerDescriptor", this.NULL_ATTS);
                this.start("NamedLayer", this.NULL_ATTS);
                int i = 0;
                int ii = styles.length;
                while (i < ii) {
                    styles[i].accept(this);
                    ++i;
                }
                this.end("NamedLayer");
                this.end("StyledLayerDescriptor");
                this.contentHandler.endDocument();
            }
            catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }

        public void encode(StyledLayerDescriptor sld) {
            try {
                this.contentHandler.startDocument();
                sld.accept(this);
                this.contentHandler.endDocument();
            }
            catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }

        public void encode(Object o) throws IllegalArgumentException {
            if (o instanceof StyledLayerDescriptor) {
                this.encode((StyledLayerDescriptor)o);
            } else if (o instanceof Style[]) {
                this.encode((Style[])o);
            } else {
                Class<?> c = o.getClass();
                try {
                    Method m = c.getMethod("accept", StyleVisitor.class);
                    m.invoke(o, this);
                }
                catch (NoSuchMethodException nsme) {
                    throw new IllegalArgumentException(I18N.getMessage(this.getClass(), "can-not-encode-{0}", new Object[]{o}));
                }
                catch (Exception e) {
                    throw new RuntimeException(I18N.getString(this.getClass(), "internal-transformation-exception"), e);
                }
            }
        }

        @Override
        public void visit(StyledLayer sl) {
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
}

