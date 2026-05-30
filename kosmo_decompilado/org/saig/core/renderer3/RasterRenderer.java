/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.apache.log4j.Logger
 */
package org.saig.core.renderer3;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.apache.log4j.Logger;
import org.saig.core.dao.coverage.Coverage;
import org.saig.core.filter.Expression;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.renderer.RendererParameterWrapper;
import org.saig.core.renderer3.IG2dRenderer;
import org.saig.core.renderer3.IRenderer;
import org.saig.core.renderer3.RenderUtils;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.jump.lang.I18N;

public class RasterRenderer
implements IRenderer,
IG2dRenderer {
    private static final Logger LOGGER = Logger.getLogger(RasterRenderer.class);

    @Override
    public void render(Graphics2D g, int width, int height, Envelope envelope, Layerable layerToRender, double angle, double panelScale, boolean strategy, Unit<Length> mapUnits) {
        int realWidth = width;
        int realHeight = height;
        Layer layer = (Layer)layerToRender;
        try {
            Rectangle2D.Double view = new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(), envelope.getHeight());
            Envelope layerEnvelope = layer.getUltimateFeatureCollectionWrapper().getEnvelope();
            if (layerEnvelope.getWidth() == 0.0 || layerEnvelope.getHeight() == 0.0) {
                layerEnvelope.expandBy(0.05);
            }
            Rectangle2D layerView = new Rectangle2D.Double(layerEnvelope.getMinX(), layerEnvelope.getMinY(), layerEnvelope.getWidth() + 0.001, layerEnvelope.getHeight() + 0.001);
            if (layer.getCoordTrans() != null) {
                layerView = layer.getCoordTrans().convert(layerView);
            }
            Envelope viewTransf = null;
            FeatureIterator featuresIterator = null;
            if (layer.getCoordTrans() != null) {
                Rectangle2D viewConverted = layer.getCoordTrans().getInverted().convert((Rectangle2D)view);
                viewTransf = new Envelope(viewConverted.getMinX(), viewConverted.getMaxX(), viewConverted.getMinY(), viewConverted.getMaxY());
                featuresIterator = layer.getFeatureCollectionWrapper().queryIterator(viewTransf);
            } else {
                featuresIterator = layer.getFeatureCollectionWrapper().queryIterator(envelope);
                viewTransf = new Envelope(view.getMinX(), view.getMaxX(), view.getMinY(), view.getMaxY());
            }
            if (!featuresIterator.hasNext()) {
                return;
            }
            Feature rasterFeature = featuresIterator.next();
            Rule rule = layer.getModelStyle().getSelectedFeatureTypeStyle().getRules()[0];
            RasterSymbolizer simbolo = (RasterSymbolizer)layer.getModelStyle().getSelectedFeatureTypeStyle().getRules()[0].getSymbolizers()[0];
            if (angle != 0.0) {
                this.drawRotatedRaster(g, width, height, angle, panelScale, realWidth, realHeight, viewTransf, rasterFeature, rule, simbolo);
            } else {
                Rectangle2D.Double rect = RenderUtils.calculateProportionalImageRectangle(width, height, envelope);
                int imgWidth = (int)rect.width;
                int imgHeight = (int)rect.height;
                if (imgWidth == width && imgHeight == height) {
                    this.drawSimpleRaster(g, width, height, panelScale, viewTransf, rasterFeature, rule, simbolo);
                } else {
                    this.drawNotProportionalRaster(g, width, height, rect, panelScale, viewTransf, rasterFeature, rule, simbolo);
                }
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    private void drawSimpleRaster(Graphics2D g2, int width, int height, double panelScale, Envelope viewTransf, Feature rasterFeature, Rule rule, RasterSymbolizer simbolo) {
        RendererParameterWrapper renderPS = new RendererParameterWrapper(viewTransf, 0, 0, width, height);
        if ((rule.getMinScaleDenominator() <= panelScale || Double.isNaN(rule.getMinScaleDenominator())) && (rule.getMaxScaleDenominator() >= panelScale || Double.isNaN(rule.getMaxScaleDenominator()))) {
            this.renderRaster(g2, rasterFeature, simbolo, renderPS);
        }
    }

    private void drawNotProportionalRaster(Graphics2D g2, int widht, int height, Rectangle2D proportion, double panelScale, Envelope viewTransf, Feature rasterFeature, Rule rule, RasterSymbolizer simbolo) {
        RendererParameterWrapper renderPS = new RendererParameterWrapper(viewTransf, 0, 0, (int)proportion.getWidth(), (int)proportion.getHeight());
        if ((rule.getMinScaleDenominator() <= panelScale || Double.isNaN(rule.getMinScaleDenominator())) && (rule.getMaxScaleDenominator() >= panelScale || Double.isNaN(rule.getMaxScaleDenominator()))) {
            BufferedImage buf = new BufferedImage((int)proportion.getWidth(), (int)proportion.getHeight(), 1);
            this.renderRaster((Graphics2D)buf.getGraphics(), rasterFeature, simbolo, renderPS);
            g2.drawImage(buf, 0, 0, widht, height, 0, 0, buf.getWidth(), buf.getHeight(), null);
        }
    }

    private void drawRotatedRaster(Graphics2D g, int width, int height, double angle, double panelScale, int realWidth, int realHeight, Envelope viewTransf, Feature rasterFeature, Rule rule, RasterSymbolizer simbolo) {
        RendererParameterWrapper renderPS = new RendererParameterWrapper(viewTransf, 0, 0, (int)this.getRotatedWidht(width, height, angle), (int)this.getRotatedHeight(width, height, angle));
        if ((rule.getMinScaleDenominator() <= panelScale || Double.isNaN(rule.getMinScaleDenominator())) && (rule.getMaxScaleDenominator() >= panelScale || Double.isNaN(rule.getMaxScaleDenominator()))) {
            BufferedImage buf = new BufferedImage(renderPS.getWidth(), renderPS.getHeight(), 2);
            this.renderRaster((Graphics2D)buf.getGraphics(), rasterFeature, simbolo, renderPS);
            g.rotate(-angle, renderPS.getX() + realWidth / 2, renderPS.getY() + realHeight / 2);
            g.drawImage((Image)buf, renderPS.getX() - (renderPS.getWidth() - realWidth) / 2, renderPS.getY() - (renderPS.getHeight() - realHeight) / 2, null);
            buf = null;
        }
    }

    private Rectangle getRotatedWindowRectangle(int width, int height, double angle) {
        Point2D.Double p1 = new Point2D.Double(0.0, 0.0);
        Point2D.Double p2 = new Point2D.Double(0.0, height);
        Point2D.Double p3 = new Point2D.Double(width, 0.0);
        Point2D.Double p4 = new Point2D.Double(width, height);
        AffineTransform trans = new AffineTransform();
        trans.rotate(angle, width / 2, height / 2);
        trans.transform(p1, p1);
        trans.transform(p2, p2);
        trans.transform(p3, p3);
        trans.transform(p4, p4);
        int minx = (int)Math.min(Math.min(((Point2D)p1).getX(), ((Point2D)p2).getX()), Math.min(((Point2D)p3).getX(), ((Point2D)p4).getX()));
        int maxx = (int)Math.max(Math.max(((Point2D)p1).getX(), ((Point2D)p2).getX()), Math.max(((Point2D)p3).getX(), ((Point2D)p4).getX()));
        int miny = (int)Math.min(Math.min(((Point2D)p1).getY(), ((Point2D)p2).getY()), Math.min(((Point2D)p3).getY(), ((Point2D)p4).getY()));
        int maxy = (int)Math.max(Math.max(((Point2D)p1).getY(), ((Point2D)p2).getY()), Math.max(((Point2D)p3).getY(), ((Point2D)p4).getY()));
        return new Rectangle(minx, miny, maxx - minx, maxy - miny);
    }

    public double getRotatedWidht(int width, int height, double angle) {
        return this.getRotatedWindowRectangle(width, height, angle).getWidth();
    }

    public double getRotatedHeight(int width, int height, double angle) {
        return this.getRotatedWindowRectangle(width, height, angle).getHeight();
    }

    private void renderRaster(Graphics2D graphics, Feature feature, RasterSymbolizer symbolizer, RendererParameterWrapper renderPs) {
        int opacity;
        int alpha = opacity = this.getOpacity(symbolizer);
        Coverage coverage = (Coverage)feature.getAttribute("IMAGE");
        renderPs.setAlpha(alpha);
        coverage.getImage(graphics, renderPs);
        LOGGER.debug((Object)I18N.getString("com.vividsolutions.jump.workbench.ui.renderer.FeatureCollectionRenderer.Raster-rendered"));
    }

    private int getOpacity(RasterSymbolizer sym) {
        Expression exp = sym.getOpacity();
        if (exp == null) {
            return 255;
        }
        Object obj = exp.getValue(null);
        if (obj == null) {
            return 255;
        }
        Number num = null;
        if (obj instanceof Number) {
            num = (Number)obj;
        }
        if (num == null) {
            return 255;
        }
        return (int)(num.floatValue() * 255.0f);
    }

    @Override
    public void cancel() {
    }

    @Override
    public void render(Image image, Envelope envelope, Layerable layer, double angle, double panelScale, boolean strategy, Unit<Length> mapUnits, Map<Object, Object> renderingHints) {
        Graphics2D g2 = (Graphics2D)image.getGraphics();
        if (renderingHints != null) {
            g2.setRenderingHints(renderingHints);
        }
        this.render(g2, image.getWidth(null), image.getHeight(null), envelope, layer, angle, panelScale, strategy, mapUnits);
    }
}

