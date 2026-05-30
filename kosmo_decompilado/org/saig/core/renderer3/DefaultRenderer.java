/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.geotools.referencing.operation.GeneralMatrix
 *  org.geotools.util.NumberRange
 *  org.opengis.referencing.operation.MathTransform2D
 *  org.opengis.referencing.operation.Matrix
 */
package org.saig.core.renderer3;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.util.NumberRange;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;
import org.saig.core.renderer.Renderer;
import org.saig.core.renderer.lite.LabelCache;
import org.saig.core.renderer.style.RuleStyle;
import org.saig.core.renderer3.AbstractRenderer;
import org.saig.core.renderer3.IG2dRenderer;
import org.saig.core.renderer3.IRenderer;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.jump.util.LayerUtil;

public class DefaultRenderer
extends AbstractRenderer
implements IRenderer,
IG2dRenderer {
    protected LabelCache labelCache;

    public DefaultRenderer(double factor) {
        super(factor);
    }

    @Override
    public void render(Graphics2D g, int widht, int height, Envelope envelope, Layerable layerToRenderer, double angle, double panelScale, boolean oneQueryByFilter, Unit<Length> mapUnits) {
        block10: {
            long t1 = System.currentTimeMillis();
            try {
                this.layer = (Layer)layerToRenderer;
                this.layerWidth = this.layer.getUltimateFeatureCollectionWrapper().getEnvelope().getWidth();
                this.envelope = envelope;
                this.angle = angle;
                this.isLineLayer = LayerUtil.isLinealLayer(this.layer);
                this.width = widht;
                this.height = height;
                this.mapUnits = mapUnits;
                FeatureTypeStyle fts = this.getSelectedFeatureTypeStyle();
                AffineTransform affineTransform = this.getModelToViewTransform();
                MathTransform2D transform = (MathTransform2D)Renderer.mathTransformFactory.createAffineTransform((Matrix)new GeneralMatrix(affineTransform));
                double scaleDenominator = 1.0 / affineTransform.getScaleX();
                NumberRange scaleRange = new NumberRange(scaleDenominator, scaleDenominator);
                Object[] rules = this.getRulesInScale(fts.getRules(), scaleRange, this.layer, panelScale, envelope.getWidth() / (double)widht, mapUnits, this.factor);
                boolean hasFilter = (Boolean)rules[4];
                boolean hasTextSymbolizer = (Boolean)rules[5];
                Set labels = (Set)rules[6];
                List rulesWithFilter = (List)rules[0];
                if (rulesWithFilter.isEmpty() && rules[1] == null) {
                    return;
                }
                try {
                    this.labelCache = this.createLabelCache();
                    this.labelCache.start();
                    this.labelCache.startLayer(this.layer.getName());
                    RuleStyle elseRule = (RuleStyle)rules[1];
                    if (oneQueryByFilter) {
                        this.paintOneQueryByFilter(this.layer, g, rulesWithFilter, affineTransform, elseRule, this.labelCache, transform, scaleRange, this.getEnvelopeInModelCoordinatesForQuery(), hasTextSymbolizer);
                    } else {
                        ArrayList<String> labelsList = new ArrayList<String>();
                        labelsList.addAll(labels);
                        this.paintOnlyQuery(this.layer, g, rulesWithFilter, affineTransform, elseRule, this.labelCache, transform, scaleRange, this.getEnvelopeInModelCoordinatesForQuery(), hasFilter, hasTextSymbolizer, labelsList);
                    }
                    if (this.cancelled) {
                        this.labelCache.stop();
                        break block10;
                    }
                    Rectangle paintArea = new Rectangle(0, 0, widht, height);
                    this.labelCache.endLayer(this.layer.getName(), g, paintArea);
                    this.labelCache.end(g, paintArea);
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
            finally {
                LOGGER.debug((Object)("He tardado en pintar la capa " + this.layer.getName() + " " + (System.currentTimeMillis() - t1) + " ms"));
                this.dispose();
            }
        }
    }

    public LabelCache getLabelCache() {
        return this.labelCache;
    }

    @Override
    protected void dispose() {
        this.layer = null;
        this.labelCache = null;
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

