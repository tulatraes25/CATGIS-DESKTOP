/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package org.saig.core.renderer2;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.FeatureCollectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.apache.log4j.Logger;
import org.saig.core.dao.coverage.Coverage;
import org.saig.core.filter.Expression;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.renderer.RendererParameterWrapper;
import org.saig.core.renderer2.IRenderer;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.jump.lang.I18N;

public class RasterRenderer
implements IRenderer {
    private static final Logger LOGGER = Logger.getLogger(RasterRenderer.class);

    @Override
    public void render(ThreadSafeImage image, Layerable layerToRenderer, Viewport viewPort, FeatureCollectionRenderer featureCollectionRenderer, boolean oneQueryByFilter) {
        final double angle = viewPort.getAngle();
        final int realWidth = viewPort.getPanel().getWidth();
        final int realHeight = viewPort.getPanel().getHeight();
        Layer layer = (Layer)layerToRenderer;
        try {
            Envelope envelope = viewPort.getEnvelopeInModelCoordinatesForQuery();
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
            final Feature rasterFeature = featuresIterator.next();
            Rule rule = layer.getModelStyle().getSelectedFeatureTypeStyle().getRules()[0];
            final RasterSymbolizer simbolo = (RasterSymbolizer)layer.getModelStyle().getSelectedFeatureTypeStyle().getRules()[0].getSymbolizers()[0];
            final RendererParameterWrapper renderPS = new RendererParameterWrapper(viewTransf, 0, 0, (int)viewPort.getRotatedWidht(), (int)viewPort.getRotatedHeight());
            if ((rule.getMinScaleDenominator() <= viewPort.getPanel().getScale() || Double.isNaN(rule.getMinScaleDenominator())) && (rule.getMaxScaleDenominator() >= viewPort.getPanel().getScale() || Double.isNaN(rule.getMaxScaleDenominator()))) {
                image.draw(new ThreadSafeImage.Drawer(){

                    @Override
                    public void draw(Graphics2D g) throws Exception {
                        BufferedImage buf = new BufferedImage(renderPS.getWidth(), renderPS.getHeight(), 2);
                        RasterRenderer.this.renderRaster(buf.createGraphics(), rasterFeature, simbolo, renderPS);
                        g.rotate(-angle, renderPS.getX() + realWidth / 2, renderPS.getY() + realHeight / 2);
                        g.drawImage((Image)buf, renderPS.getX() - (renderPS.getWidth() - realWidth) / 2, renderPS.getY() - (renderPS.getHeight() - realHeight) / 2, null);
                        buf = null;
                    }
                });
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
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
}

