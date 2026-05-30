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
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JLabel;
import org.apache.log4j.Logger;
import org.saig.core.renderer.LegendIconMaker;
import org.saig.core.renderer.Renderer;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StyleBuilder;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;

public class LegendRenderer {
    private static final Logger LOGGER = Logger.getLogger((String)"org.geotools.renderer");
    public static GeometryFactory gFac = new GeometryFactory();
    public static StyleFactory sFac = StyleFactory.createStyleFactory();
    public static int offset = 1;
    private static Renderer renderer = Renderer.getUniqueInstance();
    private static StyleBuilder styleBuilder = new StyleBuilder();
    private static FeatureSchema fFac = new FeatureSchema();

    static {
        fFac.addAttribute("legend", AttributeType.GEOMETRY);
    }

    public static Image makeLegend(int width, int height, Color background, List simbolos) {
        ArrayList<BasicFeature> features = new ArrayList<BasicFeature>();
        int i = 0;
        while (i < simbolos.size()) {
            BasicFeature feature = null;
            if (simbolos.get(i) instanceof PolygonSymbolizer) {
                Number lineWidth = new Integer(0);
                Stroke stroke = ((PolygonSymbolizer)simbolos.get(i)).getStroke();
                if (stroke != null && stroke.getWidth() != null) {
                    lineWidth = (Number)stroke.getWidth().getValue(null);
                }
                Coordinate[] c = new Coordinate[5];
                double marginForLineWidth = (double)((Number)lineWidth).intValue() / 2.0;
                c[0] = new Coordinate((double)offset + marginForLineWidth, (double)offset + marginForLineWidth);
                c[1] = new Coordinate((double)(width - offset) - marginForLineWidth, (double)offset + marginForLineWidth);
                c[2] = new Coordinate((double)(width - offset) - marginForLineWidth, (double)(height - offset) - marginForLineWidth);
                c[3] = new Coordinate((double)offset + marginForLineWidth, (double)(height - offset) - marginForLineWidth);
                c[4] = new Coordinate((double)offset + marginForLineWidth, (double)offset + marginForLineWidth);
                LinearRing r = null;
                try {
                    r = gFac.createLinearRing(c);
                }
                catch (TopologyException e) {
                    e.printStackTrace();
                    System.err.println("Topology Exception in GMLBox");
                }
                Polygon poly = gFac.createPolygon(r, null);
                feature = new BasicFeature(fFac);
                feature.setGeometry((Geometry)poly);
                features.add(feature);
                LOGGER.debug((Object)("feature = " + feature));
            } else if (simbolos.get(i) instanceof LineSymbolizer) {
                LOGGER.debug((Object)"building line");
                Coordinate[] c = new Coordinate[]{new Coordinate((double)offset, (double)offset), new Coordinate((double)(offset + width), (double)(offset + height))};
                LineString line = gFac.createLineString(c);
                feature = new BasicFeature(fFac);
                feature.setGeometry((Geometry)line);
                features.add(feature);
                LOGGER.debug((Object)("feature = " + feature));
            } else if (simbolos.get(i) instanceof PointSymbolizer) {
                LOGGER.debug((Object)"building point");
                Point p = gFac.createPoint(new Coordinate((double)offset + (double)width / 2.0, (double)offset + (double)height / 2.0));
                feature = new BasicFeature(fFac);
                feature.setGeometry((Geometry)p);
                features.add(feature);
                LOGGER.debug((Object)("feature = " + feature));
            }
            ++i;
        }
        ArrayList finalSymbols = new ArrayList();
        for (Symbolizer element : simbolos) {
            ArrayList<Symbolizer> finalSymbol = new ArrayList<Symbolizer>();
            finalSymbol.add(element);
            finalSymbols.add(finalSymbol);
        }
        BufferedImage image = new BufferedImage(width, height, 2);
        Graphics2D graphics = image.createGraphics();
        graphics.setBackground(background);
        graphics.setColor(background);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        renderer.setConcatTransforms(true);
        Rectangle screenSize = new Rectangle(0, 0, image.getWidth(), image.getHeight());
        Envelope envelope = new Envelope(0.0, (double)width, 0.0, (double)width);
        renderer.render(features, finalSymbols, envelope, screenSize, graphics, null, null, null);
        return image;
    }

    public static BufferedImage drawLegend(Layer layer, Color color) {
        return LegendRenderer.drawLegend(layer.getModelStyle().getSelectedFeatureTypeStyle(), layer.getName(), layer.isRaster(), color);
    }

    public static BufferedImage drawLegend(FeatureTypeStyle style, String layerName, boolean isRaster, Color color) {
        return LegendRenderer.drawLegend(style, layerName, isRaster, color, LocaleManager.getActiveLocale());
    }

    public static BufferedImage drawLegend(FeatureTypeStyle style, String layerName, boolean isRaster, Color color, Locale locale) {
        BasicStroke strokeLeyenda = new BasicStroke(1.0f);
        Font fontLabel = new Font("Arial", 0, 10);
        JLabel sampleLabel = new JLabel();
        sampleLabel.setFont(fontLabel);
        FontMetrics fm = sampleLabel.getFontMetrics(fontLabel);
        int x = 0;
        int y = 0;
        int ancho = 0;
        int alto = 0;
        if (!isRaster) {
            Rule[] rules = style.getRules();
            int i = 0;
            while (i < rules.length) {
                Rule rule = rules[i];
                Symbolizer[] symbols = rule.getSymbolizers();
                ArrayList<Symbolizer> symbolList = new ArrayList<Symbolizer>();
                int j = 0;
                while (j < symbols.length) {
                    if (!(symbols[j] instanceof TextSymbolizer)) {
                        symbolList.add(symbols[j]);
                    }
                    ++j;
                }
                if (!symbolList.isEmpty()) {
                    String nombreLeyenda = rule.getTitle(locale);
                    if (nombreLeyenda == null && (nombreLeyenda = rule.getName()) == null) {
                        nombreLeyenda = "";
                        LOGGER.warn((Object)I18N.getMessage("org.saig.core.renderer.LegendRenderer.there-is-not-any-title-for-the-rule-{0}-of-the-layer-{1}", new Object[]{rule.toString(), layerName}));
                    }
                    int anchoAux = 2 * fm.getHeight();
                    int j2 = 0;
                    while (j2 < nombreLeyenda.length()) {
                        char c = nombreLeyenda.charAt(j2);
                        int wc = fm.charWidth(c);
                        anchoAux += wc;
                        ++j2;
                    }
                    if (anchoAux > ancho) {
                        ancho = anchoAux;
                    }
                    alto += (int)Math.ceil((double)fm.getHeight() * 1.25);
                }
                ++i;
            }
        } else {
            LOGGER.warn((Object)"NO EXISTEN SIMBOLOS PARA DIBUJAR");
            return null;
        }
        if (alto == 0 || ancho == 0) {
            return null;
        }
        BufferedImage image = new BufferedImage(ancho, alto, 2);
        Graphics2D graphics = image.createGraphics();
        graphics.setBackground(color);
        graphics.setColor(color);
        graphics.fillRect(0, 0, ancho, alto);
        graphics.setFont(fontLabel);
        graphics.setStroke(strokeLeyenda);
        Rule[] rules = style.getRules();
        int i = 0;
        while (i < rules.length) {
            Rule rule = rules[i];
            Symbolizer[] symbols = rule.getSymbolizers();
            ArrayList<Symbolizer> symbolList = new ArrayList<Symbolizer>();
            int j = 0;
            while (j < symbols.length) {
                if (!(symbols[j] instanceof TextSymbolizer)) {
                    symbolList.add(symbols[j]);
                }
                ++j;
            }
            if (!symbolList.isEmpty()) {
                Symbolizer[] symbolsToRenderer = new Symbolizer[symbolList.size()];
                symbolList.toArray(symbolsToRenderer);
                Image image2 = LegendIconMaker.reallyMakeLegendIcon(fm.getHeight(), fm.getHeight(), color, symbolsToRenderer);
                String nombreLeyenda = rule.getTitle(locale);
                if (nombreLeyenda == null && (nombreLeyenda = rule.getName()) == null) {
                    nombreLeyenda = "";
                    LOGGER.warn((Object)I18N.getMessage("org.saig.core.renderer.LegendRenderer.there-is-not-any-title-for-the-rule-{0}-of-the-layer-{1}", new Object[]{rule.toString(), layerName}));
                }
                graphics.drawImage(image2, x, y, null);
                graphics.setStroke(strokeLeyenda);
                graphics.setColor(Color.BLACK);
                graphics.drawString(nombreLeyenda, x + 2 * fm.getHeight(), y + (int)((double)fm.getHeight() * 0.75));
                y += (int)Math.ceil((double)fm.getHeight() * 1.25);
            }
            ++i;
        }
        return image;
    }
}

