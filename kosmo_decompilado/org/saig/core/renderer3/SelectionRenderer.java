/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  javax.media.jai.util.Range
 *  org.apache.commons.collections.CollectionUtils
 *  org.cresques.cts.ICoordTrans
 *  org.geotools.referencing.operation.GeneralMatrix
 *  org.geotools.util.NumberRange
 *  org.opengis.referencing.operation.MathTransform2D
 *  org.opengis.referencing.operation.Matrix
 */
package org.saig.core.renderer3;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
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
import org.saig.core.renderer3.DecoratorTransformer;
import org.saig.core.renderer3.DefaultRenderer;
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
    public void renderSelection(final ThreadSafeImage image, int widht, int height, Envelope envelope, Layerable layerToRenderer, double angle, double panelScale, boolean oneQueryByFilter, Unit<Length> mapUnits, Iterator<Feature> featuresIterator, Rule selectionRule, boolean paintHandles, PointSymbolizer vertexSymbolizer, AbstractRenderer parentRenderer) {
        t1 = System.currentTimeMillis();
        try {
            block11: {
                this.layer = (Layer)layerToRenderer;
                this.layerWidth = this.layer.getUltimateFeatureCollectionWrapper().getEnvelope().getWidth();
                this.envelope = envelope;
                this.angle = angle;
                this.isLineLayer = LayerUtil.isLinealLayer(this.layer);
                this.width = widht;
                this.height = height;
                this.mapUnits = mapUnits;
                affineTransform = this.getModelToViewTransform();
                transform = (MathTransform2D)Renderer.mathTransformFactory.createAffineTransform((Matrix)new GeneralMatrix(affineTransform));
                scaleDenominator = 1.0 / affineTransform.getScaleX();
                scaleRange = new NumberRange(scaleDenominator, scaleDenominator);
                ruleStyle = new RuleStyle(selectionRule, scaleRange, false, 1.0, envelope.getWidth() / (double)this.width, mapUnits);
                if (paintHandles) break block11;
                iter = featuresIterator;
                if (true) ** GOTO lbl23
                do {
                    feature = iter.next();
                    geom = ShapeGeometryConverter.jts_to_igeometry(feature.getGeometry()).cloneGeometry();
                    image.draw(new ThreadSafeImage.Drawer(){

                        @Override
                        public void draw(Graphics2D g) throws Exception {
                            SelectionRenderer.this.drawShape(g, geom, ruleStyle, null, transform, scaleRange, feature, SelectionRenderer.this.layer.getCoordTrans(), SelectionRenderer.this.isLineLayer);
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
                block12: {
                    paintSquare = false;
                    if (vertexSymbolizer.getGraphic() != null && (graphic = vertexSymbolizer.getGraphic()).getMarks() != null && graphic.getMarks().length > 0) {
                        mark = graphic.getMarks()[0];
                        v0 = paintSquare = mark.getWellKnownName().toString().equalsIgnoreCase("square") != false && mark.getFill() != null;
                    }
                    if (paintSquare) break block12;
                    pointRule = new RuleImpl(new Symbolizer[]{vertexSymbolizer});
                    pointStyle = new RuleStyle(pointRule, scaleRange, false, 1.0, envelope.getWidth() / (double)this.width, mapUnits);
                    iter = featuresIterator;
                    if (true) ** GOTO lbl45
                    do {
                        feature = iter.next();
                        geom = ShapeGeometryConverter.jts_to_igeometry(feature.getGeometry());
                        newStyle = (MarkStyle2D)SelectionRenderer.styleFactory.createStyle(feature, pointStyle.getSymbol(pointStyle.getStyles().get(0)), (Range)scaleRange);
                        image.draw(new ThreadSafeImage.Drawer(){

                            @Override
                            public void draw(Graphics2D g) throws Exception {
                                SelectionRenderer.this.drawShape(g, geom, ruleStyle, null, transform, scaleRange, feature, SelectionRenderer.this.layer.getCoordTrans(), SelectionRenderer.this.isLineLayer);
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
                            SelectionRenderer.this.drawShape(g, geom, ruleStyle, null, transform, scaleRange, feature, SelectionRenderer.this.layer.getCoordTrans(), SelectionRenderer.this.isLineLayer);
                            SAIGGeneralPath path = geom.getShp().getGeneralPath();
                            double[] coords = path.pointCoords;
                            int i = 0;
                            while (i < path.numCoords) {
                                double x = coords[i];
                                double y = coords[i + 1];
                                Rectangle2D.Double bounds = new Rectangle2D.Double(x, y, 1.0, 1.0);
                                SelectionRenderer.this.drawPoint(image, (Rectangle2D)bounds, fillColor, size);
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
            SelectionRenderer.LOGGER.debug((Object)("He tardado en pintar la capa " + this.layer.getName() + " " + (System.currentTimeMillis() - t1) + " ms"));
            this.dispose();
        }
    }

    @Override
    protected LiteShape2 drawShape(Graphics2D g, IShapeGeometry geom, RuleStyle ruleStyle, LabelCache labelCache, MathTransform2D transform, NumberRange scaleRange, Feature feature, ICoordTrans ct, boolean isLineLayer) throws IOException {
        if (!this.layer.isCadLayer()) {
            return super.drawShape(g, geom, ruleStyle, labelCache, transform, scaleRange, feature, ct, isLineLayer);
        }
        if (geom == null || ruleStyle == null) {
            return null;
        }
        LiteShape2 shape = null;
        try {
            try {
                IShape shp = geom.getPathShapeInt(this.getModelToViewTransform());
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
                if (CollectionUtils.isNotEmpty(ruleStyle.getDecorators())) {
                    this.drawDecorators(g, new DecoratorTransformer(this.getModelToViewTransform()), ruleStyle.getDecorators(), feature, ct, transform);
                }
            }
        }
        finally {
            if (CollectionUtils.isNotEmpty(ruleStyle.getDecorators())) {
                this.drawDecorators(g, new DecoratorTransformer(this.getModelToViewTransform()), ruleStyle.getDecorators(), feature, ct, transform);
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

    protected void drawPoint(ThreadSafeImage image, Rectangle2D bounds, Color color, int size) throws IOException {
        if (color == null || size == 0) {
            return;
        }
        BufferedImage image2 = (BufferedImage)image.getImage();
        int pixX = (int)bounds.getMinX();
        int pixY = (int)bounds.getMinY();
        int size_ = Math.round((float)size / 2.0f);
        int j = pixX - size_;
        while (j <= pixX + size_) {
            int k = pixY - size_;
            while (k < pixY + size_) {
                if (j > 0 && j < image2.getWidth() && k > 0 && k < image2.getHeight()) {
                    image2.setRGB(j, k, color.getRGB());
                }
                ++k;
            }
            ++j;
        }
    }
}

