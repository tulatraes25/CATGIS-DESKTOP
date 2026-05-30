/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.media.jai.util.Range
 *  org.apache.commons.collections.CollectionUtils
 *  org.cresques.cts.ICoordTrans
 *  org.geotools.referencing.operation.GeneralMatrix
 *  org.geotools.util.NumberRange
 *  org.opengis.referencing.operation.MathTransform2D
 *  org.opengis.referencing.operation.Matrix
 */
package org.saig.core.renderer2;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractSelectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import es.kosmo.core.renderer.decorators.IDecorator;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.media.jai.util.Range;
import org.apache.commons.collections.CollectionUtils;
import org.cresques.cts.ICoordTrans;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.util.NumberRange;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeFactory;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.renderer.Renderer;
import org.saig.core.renderer.lite.LabelCache;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.renderer.style.GraphicStyle2D;
import org.saig.core.renderer.style.LineStyle2D;
import org.saig.core.renderer.style.MarkStyle2D;
import org.saig.core.renderer.style.PolygonStyle2D;
import org.saig.core.renderer.style.RuleStyle;
import org.saig.core.renderer.style.Style2D;
import org.saig.core.renderer2.DefaultRenderer;
import org.saig.core.renderer2.ViewportDecoratorTransformerWrapper;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.RuleImpl;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.util.LayerUtil;

public class SelectionRenderer
extends DefaultRenderer {
    public SelectionRenderer(double factor) {
        super(factor);
    }

    /*
     * Unable to fully structure code
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void renderSelection(ThreadSafeImage image, final Layerable layer, final Viewport viewPort, Iterator<Feature> featuresIterator, Rule selectionRule, boolean paintHandles, PointSymbolizer vertexSymbolizer, AbstractSelectionRenderer parentRenderer) {
        t1 = System.currentTimeMillis();
        try {
            block13: {
                this.layer = (Layer)layer;
                this.layerWidth = this.layer.getUltimateFeatureCollectionWrapper().getEnvelope().getWidth();
                this.viewPort = viewPort;
                if (!this.checkCoordinates(this.layer, viewPort)) {
                    return;
                }
                this.viewWidth = viewPort.getEnvelopeInModelCoordinates().getWidth();
                this.isLineLayer = LayerUtil.isLinealLayer(this.layer);
                this.image = image;
                affineTransform = viewPort.getModelToViewTransform();
                transform = (MathTransform2D)Renderer.mathTransformFactory.createAffineTransform((Matrix)new GeneralMatrix(affineTransform));
                scaleDenominator = 1.0 / affineTransform.getScaleX();
                scaleRange = new NumberRange(scaleDenominator, scaleDenominator);
                ruleStyle = new RuleStyle(selectionRule, scaleRange, false, 1.0, viewPort.getEnvelopeInModelCoordinates().getWidth() / (double)viewPort.getPanel().getWidth(), viewPort.getPanel().getUserLengthUnit());
                if (paintHandles) break block13;
                iter = featuresIterator;
                if (true) ** GOTO lbl23
                do {
                    feature = iter.next();
                    geom = ShapeGeometryConverter.jts_to_igeometry(feature.getGeometry()).cloneGeometry();
                    image.draw(new ThreadSafeImage.Drawer(){

                        @Override
                        public void draw(Graphics2D g) throws Exception {
                            SelectionRenderer.this.drawShape(g, geom, ruleStyle, null, transform, scaleRange, feature, ((Layer)layer).getCoordTrans(), SelectionRenderer.this.isLineLayer, null, viewPort);
                        }
                    });
lbl23:
                    // 2 sources

                    if (!iter.hasNext()) return;
                } while (!parentRenderer.cancelled);
                return;
            }
            if (vertexSymbolizer == null) {
                SelectionRenderer.LOGGER.warn((Object)"No existe ning\u00fan s\u00edmbolo puntual para pintar el v\u00e9rtice");
                return;
            }
            try {
                block14: {
                    paintSquare = false;
                    if (vertexSymbolizer.getGraphic() != null && (graphic = vertexSymbolizer.getGraphic()).getMarks() != null && graphic.getMarks().length > 0) {
                        mark = graphic.getMarks()[0];
                        v0 = paintSquare = mark.getWellKnownName().toString().equalsIgnoreCase("square") != false && mark.getFill() != null;
                    }
                    if (paintSquare) break block14;
                    pointRule = new RuleImpl(new Symbolizer[]{vertexSymbolizer});
                    pointStyle = new RuleStyle(pointRule, scaleRange, false, 1.0, viewPort.getEnvelopeInModelCoordinates().getWidth() / (double)viewPort.getPanel().getWidth(), viewPort.getPanel().getUserLengthUnit());
                    iter = featuresIterator;
                    if (true) ** GOTO lbl45
                    do {
                        feature = iter.next();
                        geom = ShapeGeometryConverter.jts_to_igeometry(feature.getGeometry());
                        newStyle = (MarkStyle2D)SelectionRenderer.styleFactory.createStyle(feature, pointStyle.getSymbol(pointStyle.getStyles().get(0)), (Range)scaleRange);
                        image.draw(new ThreadSafeImage.Drawer(){

                            @Override
                            public void draw(Graphics2D g) throws Exception {
                                SelectionRenderer.this.drawShape(g, geom, ruleStyle, null, transform, scaleRange, feature, ((Layer)layer).getCoordTrans(), SelectionRenderer.this.isLineLayer, null, viewPort);
                                IShapeGeometry geom2 = ShapeGeometryConverter.jts_to_igeometry(feature.getGeometry());
                                SAIGGeneralPath path = geom2.getShp().getGeneralPath();
                                double[] coords = path.pointCoords;
                                int i = 0;
                                while (i < path.numCoords) {
                                    double x = coords[i];
                                    double y = coords[i + 1];
                                    ShapeGeometry currentPoint = ShapeFactory.createPoint2D(x, y);
                                    LiteShape2 shape = SelectionRenderer.this.getTransformedShape(ShapeGeometryConverter.java2d_to_jts(currentPoint.getShp()), transform);
                                    painter.paint(g, shape, newStyle, SelectionRenderer.this.factor == 1.0 ? SelectionRenderer.this.factor : SelectionRenderer.this.factor * 2.0);
                                    i += 2;
                                }
                            }
                        });
lbl45:
                        // 2 sources

                        if (!iter.hasNext()) return;
                    } while (!parentRenderer.cancelled);
                    return;
                }
                mark = vertexSymbolizer.getGraphic().getMarks()[0];
                size = Double.valueOf(mark.getSize().toString()).intValue();
                fillColor = Color.decode((String)mark.getFill().getColor().getValue(null));
                iter = featuresIterator;
                if (true) ** GOTO lbl58
                do {
                    feature = iter.next();
                    geom = ShapeGeometryConverter.jts_to_igeometry(feature.getGeometry());
                    image.draw(new ThreadSafeImage.Drawer(){

                        @Override
                        public void draw(Graphics2D g) throws Exception {
                            SelectionRenderer.this.drawShape(g, geom, ruleStyle, null, transform, scaleRange, feature, ((Layer)layer).getCoordTrans(), SelectionRenderer.this.isLineLayer, null, viewPort);
                            SAIGGeneralPath path = geom.getShp().getGeneralPath();
                            double[] coords = path.pointCoords;
                            int i = 0;
                            while (i < path.numCoords) {
                                double x = coords[i];
                                double y = coords[i + 1];
                                Rectangle2D.Double bounds = new Rectangle2D.Double(x, y, 1.0, 1.0);
                                SelectionRenderer.this.drawPoint(bounds, fillColor, size);
                                i += 2;
                            }
                        }
                    });
lbl58:
                    // 2 sources

                    if (!iter.hasNext()) return;
                } while (!parentRenderer.cancelled);
                return;
            }
            catch (Exception e) {
                SelectionRenderer.LOGGER.error((Object)"", (Throwable)e);
            }
            return;
        }
        finally {
            SelectionRenderer.LOGGER.debug((Object)("He tardado en pintar la capa " + layer.getName() + " " + (System.currentTimeMillis() - t1) + " ms"));
            this.dispose();
        }
    }

    protected LiteShape2 drawShape(Graphics2D g, IShapeGeometry geom, RuleStyle ruleStyle, LabelCache labelCache, MathTransform2D transform, NumberRange scaleRange, Feature feature, ICoordTrans ct, boolean isLineLayer, List<IDecorator> decorators, Viewport viewport) throws IOException {
        if (!this.layer.isCadLayer()) {
            return super.drawShape(g, geom, ruleStyle, labelCache, transform, scaleRange, feature, ct, isLineLayer, viewport);
        }
        if (geom == null || ruleStyle == null) {
            return null;
        }
        LiteShape2 shape = null;
        try {
            try {
                IShape shp = geom.getPathShapeInt(this.viewPort.getModelToViewTransform());
                int shpType = shp.getShapeType();
                Rectangle2D bounds = shp.getBounds2D();
                boolean isPointShape = this.isPointShape(shpType);
                boolean isLineShape = this.isLineShape(shpType);
                boolean isPolygonShape = this.isPolygonShape(shpType);
                for (Style2D style : ruleStyle.getStyles()) {
                    if (style instanceof MarkStyle2D) {
                        if (isPointShape) {
                            shape = this.drawMark(g, ruleStyle, (MarkStyle2D)style, feature, scaleRange, isLineLayer, transform, bounds, ct, shp);
                        }
                    } else if (style instanceof GraphicStyle2D) {
                        shape = this.drawGraphic(g, (GraphicStyle2D)style, ruleStyle, feature, scaleRange, isLineLayer, ct, transform);
                    } else if (style instanceof PolygonStyle2D) {
                        if (isPolygonShape) {
                            this.drawPolygon(g, (PolygonStyle2D)style, shp);
                        }
                    } else if (style instanceof LineStyle2D && isLineShape) {
                        this.drawLine(g, (LineStyle2D)style, shp);
                    }
                    if (!(style instanceof LineStyle2D) || !isPolygonShape) continue;
                    this.drawLine(g, (LineStyle2D)style, shp);
                }
            }
            catch (Exception e1) {
                LOGGER.error((Object)"", (Throwable)e1);
                if (CollectionUtils.isNotEmpty(decorators)) {
                    this.drawDecorators(g, new ViewportDecoratorTransformerWrapper(viewport), decorators, feature, ct, transform);
                }
            }
        }
        finally {
            if (CollectionUtils.isNotEmpty(decorators)) {
                this.drawDecorators(g, new ViewportDecoratorTransformerWrapper(viewport), decorators, feature, ct, transform);
            }
        }
        return shape;
    }

    protected boolean isPointShape(int shpType) {
        return shpType == 1 || shpType == 32 || shpType == 513 || shpType == 544;
    }

    protected boolean isLineShape(int shpType) {
        return shpType == 2 || shpType == 128 || shpType == 514 || shpType == 640;
    }

    protected boolean isPolygonShape(int shpType) {
        return shpType == 4 || shpType == 64 || shpType == 256 || shpType == 516 || shpType == 576 || shpType == 768;
    }
}

