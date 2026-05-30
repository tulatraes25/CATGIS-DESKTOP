/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.geom.TopologyException
 *  org.apache.log4j.Logger
 */
package org.saig.core.renderer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.SymbolizerUtils;
import org.saig.core.renderer.Renderer;
import org.saig.core.renderer.RenderingHintsManager;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.Mark;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleBuilder;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;

public class LegendIconMaker {
    private static final Logger LOGGER = Logger.getLogger(LegendIconMaker.class);
    public static final ImageIcon ICON_TEXT_SYMBOL = IconLoader.icon("texto.png");
    public static final ImageIcon ICON_RASTER_SYMBOL = IconLoader.icon("raster.png");
    private static final int MIN_ICON_SIZE = 8;
    public static GeometryFactory gFac = new GeometryFactory();
    public static StyleFactory sFac = StyleFactory.createStyleFactory();
    public static int offset = 0;
    private static Renderer renderer = Renderer.getUniqueInstance();
    private static StyleBuilder styleBuilder = new StyleBuilder();
    private static final int ICON_CACHE_SIZE = 30;
    private static Map<IconDescriptor, Icon> iconCache = new LinkedHashMap<IconDescriptor, Icon>(16, 0.75f, true){
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return this.size() > 30;
        }
    };
    private static FeatureSchema fFac = new FeatureSchema();

    static {
        fFac.addAttribute("legend", AttributeType.GEOMETRY);
        fFac.addAttribute("text", AttributeType.STRING);
    }

    private LegendIconMaker() {
    }

    public static Icon makeLegendIcon(int iconWidth, Color background, Rule rule, Feature sample) {
        return LegendIconMaker.makeLegendIcon(iconWidth, background, rule.getSymbolizers(), sample);
    }

    public static Icon makeLegendIcon(int iconWidth, Rule rule, Feature sample) {
        return LegendIconMaker.makeLegendIcon(iconWidth, new Color(0, 0, 0, 0), rule, sample);
    }

    public static Icon makeLegendIcon(int iconWidth, Color background, Symbolizer[] syms, Feature sample) {
        return LegendIconMaker.makeLegendIcon(iconWidth, iconWidth, background, syms, sample, true);
    }

    public static Icon makeLegendIcon(int iconWidth, int iconHeight, Color background, Symbolizer[] syms, Feature sample, boolean cacheIcon) {
        IconDescriptor descriptor = new IconDescriptor(iconWidth, iconHeight, background, syms, sample);
        Icon icon = iconCache.get(descriptor);
        if (icon == null) {
            icon = new ImageIcon(LegendIconMaker.reallyMakeLegendIcon(iconWidth, iconHeight, background, syms));
            if (cacheIcon) {
                iconCache.put(descriptor, icon);
            }
        }
        return icon;
    }

    public static Image reallyMakeLegendIcon(int iconWidth, int iconHeight, Color background, Symbolizer[] symbolizers) {
        ArrayList<BasicFeature> features = new ArrayList<BasicFeature>();
        int i = 0;
        while (i < symbolizers.length) {
            BasicFeature feature = null;
            if (symbolizers[i] instanceof PolygonSymbolizer) {
                Number lineWidth = new Integer(0);
                Stroke stroke = ((PolygonSymbolizer)symbolizers[i]).getStroke();
                if (stroke != null && stroke.getWidth() != null) {
                    lineWidth = (Number)stroke.getWidth().getValue(null);
                }
                Coordinate[] c = new Coordinate[5];
                double marginForLineWidth = (double)((Number)lineWidth).intValue() / 2.0;
                c[0] = new Coordinate((double)offset + marginForLineWidth, (double)offset + marginForLineWidth);
                c[1] = new Coordinate((double)(iconWidth - offset) - marginForLineWidth, (double)offset + marginForLineWidth);
                c[2] = new Coordinate((double)(iconWidth - offset) - marginForLineWidth, (double)(iconHeight - offset) - marginForLineWidth);
                c[3] = new Coordinate((double)offset + marginForLineWidth, (double)(iconHeight - offset) - marginForLineWidth);
                c[4] = new Coordinate((double)offset + marginForLineWidth, (double)offset + marginForLineWidth);
                LinearRing r = null;
                try {
                    r = gFac.createLinearRing(c);
                }
                catch (TopologyException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                Polygon poly = gFac.createPolygon(r, null);
                feature = new BasicFeature(fFac);
                feature.setGeometry((Geometry)poly);
                features.add(feature);
            } else if (symbolizers[i] instanceof LineSymbolizer) {
                Coordinate[] c = new Coordinate[]{new Coordinate((double)offset, (double)offset), new Coordinate((double)(offset + iconWidth), (double)(offset + iconHeight))};
                LineString line = gFac.createLineString(c);
                feature = new BasicFeature(fFac);
                feature.setGeometry((Geometry)line);
                features.add(feature);
            } else if (symbolizers[i] instanceof PointSymbolizer) {
                PointSymbolizer pointSymbolizer = SymbolizerUtils.styleCloner.clone((PointSymbolizer)symbolizers[i]);
                Graphic graphic = pointSymbolizer.getGraphic();
                Mark[] marks = graphic.getMarks();
                Object g_size_value = graphic.getSize().getValue(feature);
                Object l_size_value = null;
                double g_size = 0.0;
                double l_size = 0.0;
                boolean reducir = false;
                if (marks.length > 0 && marks[0].getStroke() != null && (l_size_value = marks[0].getStroke().getWidth().getValue(feature)) instanceof Number) {
                    l_size = ((Number)l_size_value).doubleValue();
                }
                if (g_size_value instanceof Number) {
                    g_size = ((Number)g_size_value).doubleValue();
                    boolean bl = reducir = g_size + 2.0 * l_size > (double)iconWidth;
                }
                if (reducir) {
                    double line_resize = l_size * (double)iconWidth / g_size;
                    graphic.setSize(styleBuilder.literalExpression((double)iconWidth - (line_resize + 1.0)));
                    if (line_resize != 0.0) {
                        Stroke stroke = (Stroke)marks[0].getStroke().clone();
                        stroke.setWidth(styleBuilder.literalExpression(line_resize));
                        marks[0].setStroke(stroke);
                    }
                    pointSymbolizer.setGraphic(graphic);
                    symbolizers[i] = pointSymbolizer;
                }
                if (g_size < 8.0 && pointSymbolizer.getUnitsOfMeasurement() != null && !pointSymbolizer.getUnitsOfMeasurement().equals("pixel")) {
                    graphic.setSize(styleBuilder.literalExpression(8));
                    pointSymbolizer.setGraphic(graphic);
                    symbolizers[i] = pointSymbolizer;
                }
                Point p = gFac.createPoint(new Coordinate((double)offset + (double)iconWidth / 2.0, (double)offset + (double)iconHeight / 2.0 + 1.0));
                feature = new BasicFeature(fFac);
                feature.setGeometry((Geometry)p);
                features.add(feature);
            } else if (symbolizers[i] instanceof TextSymbolizer) {
                if (symbolizers.length == 1) {
                    return ICON_TEXT_SYMBOL.getImage().getScaledInstance(iconWidth, iconHeight, 4);
                }
            } else if (symbolizers[i] instanceof RasterSymbolizer && symbolizers.length == 1) {
                return ICON_RASTER_SYMBOL.getImage().getScaledInstance(iconWidth, iconHeight, 4);
            }
            ++i;
        }
        ArrayList finalSymbols = new ArrayList();
        int i2 = 0;
        while (i2 < symbolizers.length) {
            Symbolizer element = symbolizers[i2];
            if (!(element instanceof TextSymbolizer)) {
                ArrayList<Symbolizer> finalSymbol = new ArrayList<Symbolizer>();
                finalSymbol.add(element);
                finalSymbols.add(finalSymbol);
            }
            ++i2;
        }
        FeatureTypeStyle fts = styleBuilder.createFeatureTypeStyle("", styleBuilder.createRule(symbolizers));
        fts.setFeatureTypeName("Prueba");
        Style s = styleBuilder.createStyle();
        s.addFeatureTypeStyle(fts);
        BufferedImage image = new BufferedImage(iconWidth, iconHeight, 2);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHints(RenderingHintsManager.getRenderingHints());
        graphics.setBackground(background);
        graphics.setColor(background);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        renderer.setConcatTransforms(true);
        Rectangle screenSize = new Rectangle(0, 0, image.getWidth(), image.getHeight());
        Envelope envelope = new Envelope(0.0, (double)iconWidth, 0.0, (double)iconWidth);
        renderer.render(features, finalSymbols, envelope, screenSize, graphics, null, null, null);
        return image;
    }

    public static void reallyMakeLegendIcon(int iconWidth, int iconHeight, Color background, Symbolizer[] symbolizers, Graphics2D g, int x, int y) {
        ArrayList<BasicFeature> features = new ArrayList<BasicFeature>();
        int i = 0;
        while (i < symbolizers.length) {
            BasicFeature feature = null;
            if (symbolizers[i] instanceof PolygonSymbolizer) {
                Number lineWidth = new Integer(0);
                Stroke stroke = ((PolygonSymbolizer)symbolizers[i]).getStroke();
                if (stroke != null && stroke.getWidth() != null) {
                    lineWidth = (Number)stroke.getWidth().getValue(null);
                }
                Coordinate[] c = new Coordinate[5];
                double marginForLineWidth = (double)((Number)lineWidth).intValue() / 2.0;
                c[0] = new Coordinate((double)offset + marginForLineWidth, (double)offset + marginForLineWidth);
                c[1] = new Coordinate((double)(iconWidth - offset) - marginForLineWidth, (double)offset + marginForLineWidth);
                c[2] = new Coordinate((double)(iconWidth - offset) - marginForLineWidth, (double)(iconHeight - offset) - marginForLineWidth);
                c[3] = new Coordinate((double)offset + marginForLineWidth, (double)(iconHeight - offset) - marginForLineWidth);
                c[4] = new Coordinate((double)offset + marginForLineWidth, (double)offset + marginForLineWidth);
                LinearRing r = null;
                try {
                    r = gFac.createLinearRing(c);
                }
                catch (TopologyException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                Polygon poly = gFac.createPolygon(r, null);
                feature = new BasicFeature(fFac);
                feature.setGeometry((Geometry)poly);
                features.add(feature);
            } else if (symbolizers[i] instanceof LineSymbolizer) {
                Coordinate[] c = new Coordinate[]{new Coordinate((double)offset, (double)offset), new Coordinate((double)(offset + iconWidth), (double)(offset + iconHeight))};
                LineString line = gFac.createLineString(c);
                feature = new BasicFeature(fFac);
                feature.setGeometry((Geometry)line);
                features.add(feature);
            } else if (symbolizers[i] instanceof PointSymbolizer) {
                Point p = gFac.createPoint(new Coordinate((double)offset + (double)iconWidth / 2.0, (double)offset + (double)iconHeight / 2.0));
                feature = new BasicFeature(fFac);
                feature.setGeometry((Geometry)p);
                features.add(feature);
            }
            ++i;
        }
        ArrayList finalSymbols = new ArrayList();
        int i2 = 0;
        while (i2 < symbolizers.length) {
            Symbolizer element = symbolizers[i2];
            if (!(element instanceof TextSymbolizer)) {
                ArrayList<Symbolizer> finalSymbol = new ArrayList<Symbolizer>();
                finalSymbol.add(element);
                finalSymbols.add(finalSymbol);
            }
            ++i2;
        }
        FeatureTypeStyle fts = styleBuilder.createFeatureTypeStyle("", styleBuilder.createRule(symbolizers));
        fts.setFeatureTypeName("Prueba");
        Style s = styleBuilder.createStyle();
        s.addFeatureTypeStyle(fts);
        Rectangle screenSize = new Rectangle(x, y, iconWidth, iconHeight);
        Envelope envelope = new Envelope(0.0, (double)iconWidth, 0.0, (double)iconWidth);
        renderer.render(features, finalSymbols, envelope, screenSize, g, null, null, null);
    }

    private static class IconDescriptor {
        private int iconHeight;
        private int iconWidth;
        private Color background;
        private Symbolizer[] symbolizers;
        private Feature sample;

        public IconDescriptor(int iconWidth, int iconHeight, Color background, Symbolizer[] symbolizers, Feature sample) {
            this.iconWidth = iconWidth;
            this.iconHeight = iconHeight;
            this.background = background;
            this.symbolizers = symbolizers;
            this.sample = sample;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof IconDescriptor)) {
                return false;
            }
            IconDescriptor other = (IconDescriptor)obj;
            if (other.iconWidth != this.iconWidth || other.iconHeight != this.iconHeight) {
                return false;
            }
            if (!(this.background == null && other.background == null || other.background.equals(this.background))) {
                return false;
            }
            if (this.symbolizers == null && other.symbolizers != null || this.symbolizers != null && other.symbolizers == null || this.symbolizers.length != other.symbolizers.length) {
                return false;
            }
            int i = 0;
            while (i < this.symbolizers.length) {
                if (this.symbolizers[i] == null || other.symbolizers[i] == null || !this.symbolizers[i].equals(other.symbolizers[i])) {
                    return false;
                }
                ++i;
            }
            return this.sample == null && other.sample == null || this.sample != null && other.sample != null && this.sample.equals(other.sample);
        }

        public int hashCode() {
            return ((((17 + this.symbolizersHashCode()) * 37 + this.iconWidth) * 37 + this.iconHeight) * 37 + (this.background != null ? this.background.hashCode() : 0)) * 37 + (this.sample == null ? 0 : this.sample.hashCode());
        }

        private int symbolizersHashCode() {
            int hash = 17;
            int i = 0;
            while (i < this.symbolizers.length) {
                if (this.symbolizers[i] != null) {
                    hash = (hash + this.symbolizers[i].hashCode()) * 37;
                }
                ++i;
            }
            return hash;
        }
    }
}

