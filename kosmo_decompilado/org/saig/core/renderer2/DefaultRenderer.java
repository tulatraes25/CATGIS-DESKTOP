/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.referencing.operation.GeneralMatrix
 *  org.geotools.util.NumberRange
 *  org.opengis.referencing.operation.MathTransform2D
 *  org.opengis.referencing.operation.Matrix
 */
package org.saig.core.renderer2;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.FeatureCollectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import es.kosmo.core.renderer.label.LabelCacheImpl;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.util.NumberRange;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;
import org.saig.core.renderer.Renderer;
import org.saig.core.renderer.style.RuleStyle;
import org.saig.core.renderer2.AbstractRenderer;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.jump.util.LayerUtil;

public class DefaultRenderer
extends AbstractRenderer {
    public DefaultRenderer(double factor) {
        super(factor);
    }

    @Override
    public void render(ThreadSafeImage image, Layerable layerToRenderer, Viewport viewPort, FeatureCollectionRenderer featureCollectionRenderer, boolean oneQueryByFilter) {
        block12: {
            long t1 = System.currentTimeMillis();
            try {
                this.layer = (Layer)layerToRenderer;
                this.layerWidth = this.layer.getUltimateFeatureCollectionWrapper().getEnvelope().getWidth();
                this.fcRenderer = featureCollectionRenderer;
                this.viewPort = viewPort;
                if (!this.checkCoordinates(this.layer, viewPort)) {
                    return;
                }
                this.viewWidth = viewPort.getEnvelopeInModelCoordinates().getWidth();
                this.isLineLayer = LayerUtil.isLinealLayer(this.layer);
                this.image = image;
                FeatureTypeStyle estilo = this.layer.getModelStyle().getSelectedFeatureTypeStyle();
                AffineTransform affineTransform = viewPort.getModelToViewTransform();
                MathTransform2D transform = (MathTransform2D)Renderer.mathTransformFactory.createAffineTransform((Matrix)new GeneralMatrix(affineTransform));
                double scaleDenominator = 1.0 / affineTransform.getScaleX();
                NumberRange scaleRange = new NumberRange(scaleDenominator, scaleDenominator);
                Object[] rules = this.getRulesInScale(estilo.getRules(), scaleRange, this.layer, viewPort, this.factor);
                boolean hasFilter = (Boolean)rules[4];
                boolean hasTextSymbolizer = (Boolean)rules[5];
                Set labels = (Set)rules[6];
                List rulesWithFilter = (List)rules[0];
                if (rulesWithFilter.isEmpty() && rules[1] == null) {
                    return;
                }
                try {
                    final LabelCacheImpl labelCache = new LabelCacheImpl(this.layer.isOverlapping(), this.layer.isRepeated());
                    labelCache.start();
                    labelCache.startLayer(this.layer.getName());
                    RuleStyle elseRule = (RuleStyle)rules[1];
                    if (oneQueryByFilter) {
                        this.paintOneQueryByFilter(this.layer, image, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, viewPort.getEnvelopeInModelCoordinatesForQuery(), hasTextSymbolizer);
                    } else {
                        ArrayList<String> labelsList = new ArrayList<String>();
                        labelsList.addAll(labels);
                        this.paintOnlyQuery(this.layer, image, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, viewPort.getEnvelopeInModelCoordinatesForQuery(), hasFilter, hasTextSymbolizer, labelsList);
                    }
                    if (this.fcRenderer.cancelled) {
                        labelCache.stop();
                        break block12;
                    }
                    final Rectangle paintArea = new Rectangle(0, 0, viewPort.getPanel().getWidth(), viewPort.getPanel().getHeight());
                    image.draw(new ThreadSafeImage.Drawer(){

                        @Override
                        public void draw(Graphics2D g) throws Exception {
                            labelCache.endLayer(DefaultRenderer.this.layer.getName(), g, paintArea);
                            labelCache.end(g, paintArea);
                        }
                    });
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

    @Override
    protected void dispose() {
        this.fcRenderer = null;
        this.viewPort = null;
        this.layer = null;
        this.image = null;
    }
}

