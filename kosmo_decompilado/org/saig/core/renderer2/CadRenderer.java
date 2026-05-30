/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  javax.media.jai.util.Range
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.px.dxf.AcadColor
 *  org.geotools.util.NumberRange
 *  org.opengis.referencing.operation.MathTransform2D
 */
package org.saig.core.renderer2;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;
import javax.media.jai.util.Range;
import javax.swing.JLabel;
import org.cresques.cts.ICoordTrans;
import org.cresques.px.dxf.AcadColor;
import org.geotools.util.NumberRange;
import org.opengis.referencing.operation.MathTransform2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.renderer.lite.LabelCache;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.renderer.style.RuleStyle;
import org.saig.core.renderer2.DefaultRenderer;
import org.saig.core.styling.AnchorPoint;
import org.saig.core.styling.Displacement;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Halo;
import org.saig.core.styling.PointPlacement;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.TextSymbolizer;

public class CadRenderer
extends DefaultRenderer {
    private static final int CAD_DEFAULT_THICKNESS = 1;
    private static final int CAD_POINT_DEFAULT_THICKNESS = 3;
    protected int thickness;
    protected Color objColor;
    protected boolean isSolid;
    protected boolean isLine;
    protected boolean isPolygon;
    protected boolean isPoint;
    protected boolean isText;
    protected static Font baseFont = new JLabel().getFont();

    public CadRenderer(double factor) {
        super(factor);
    }

    @Override
    protected void process1Px(ThreadSafeImage image, Rectangle2D bounds, AffineTransform aft, List<RuleStyle> rulesWithFilter, Feature feature) throws IOException {
        Object color = feature.getAttribute("Color");
        this.draw1px(bounds, aft, AcadColor.getColor((int)((Number)color).intValue()).getRGB(), image);
    }

    /*
     * Handled impossible loop by duplicating code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    protected void paintOnlyQuery(Layer layer, ThreadSafeImage image, List<RuleStyle> rulesWithFilter, AffineTransform affineTransform, RuleStyle elseRule, final LabelCache labelCache, final MathTransform2D transform, final NumberRange scaleRange, Envelope envelope, boolean hasFilter, boolean hasTextSymbolizer, List<String> labels) throws Exception {
        double distance = this.getDistancia1Px(affineTransform);
        final ICoordTrans ct = layer.getCoordTrans();
        final String layerName = layer.getName();
        FeatureIterator iterator = null;
        try {
            try {
                if (layer.getCoordTrans() != null) {
                    Rectangle2D.Double view = new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(), envelope.getHeight());
                    Rectangle2D viewConverted = layer.getCoordTrans().getInverted().convert((Rectangle2D)view);
                    Envelope viewTransf = new Envelope(viewConverted.getMinX(), viewConverted.getMaxX(), viewConverted.getMinY(), viewConverted.getMaxY());
                    iterator = layer.getUltimateFeatureCollectionWrapper().queryIterator(viewTransf);
                    if (this.fcRenderer.cancelled) return;
                    if (!iterator.hasNext()) {
                        return;
                    }
                } else {
                    iterator = layer.getUltimateFeatureCollectionWrapper().queryIterator(envelope);
                    if (true) {
                        if (this.fcRenderer.cancelled) return;
                        if (!iterator.hasNext()) return;
                    }
                }
                do {
                    Feature feature;
                    Geometry featGeom;
                    this.isPoint = (featGeom = (feature = iterator.next()).getGeometry()) instanceof Point || featGeom instanceof MultiPoint;
                    this.isText = ((String)feature.getAttribute("Entity")).equalsIgnoreCase("text");
                    final IShapeGeometry geom = this.transformShapeToNewSR(layer.getCoordTrans(), feature.getGeometry(), transform);
                    final Rectangle2D bounds = geom.getBounds2D();
                    if (!(this.isPoint || this.isText || bounds.getWidth() > distance || bounds.getHeight() > distance)) {
                        IShape shp = geom.getPathShapeInt(this.viewPort.getModelToViewTransform());
                        this.process1Px(image, shp.getBounds2D(), affineTransform, rulesWithFilter, feature);
                    } else {
                        for (final RuleStyle style : rulesWithFilter) {
                            Filter ruleFilter = style.getFilter();
                            if (ruleFilter != null && (ruleFilter == null || !ruleFilter.contains(feature))) continue;
                            image.draw(new ThreadSafeImage.Drawer(){

                                @Override
                                public void draw(Graphics2D g) throws Exception {
                                    CadRenderer.this.drawShape(g, geom, style, labelCache, transform, scaleRange, feature, bounds, ct, CadRenderer.this.isLineLayer, CadRenderer.this.viewPort, layerName);
                                }
                            });
                        }
                    }
                    if (this.fcRenderer.cancelled) return;
                } while (iterator.hasNext());
                return;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (iterator == null) return;
                iterator.close(this.fcRenderer.cancelled);
                return;
            }
        }
        finally {
            if (iterator != null) {
                iterator.close(this.fcRenderer.cancelled);
            }
        }
    }

    @Override
    protected void paintOneQueryByFilter(Layer layer, ThreadSafeImage image, List<RuleStyle> rulesWithFilter, AffineTransform affineTransform, RuleStyle elseRule, LabelCache labelCache, MathTransform2D transform, NumberRange scaleRange, Envelope envelope, boolean hasTextSymbolizer) throws Exception {
        this.paintOnlyQuery(layer, image, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, envelope, true, true, null);
    }

    protected LiteShape2 drawShape(Graphics2D g, IShapeGeometry geom, RuleStyle ruleStyle, LabelCache labelCache, MathTransform2D transform, NumberRange scaleRange, Feature feature, Rectangle2D bounds, ICoordTrans ct, boolean isLineLayer, Viewport viewport, String layerId) throws IOException {
        IShape shp;
        block19: {
            LiteShape2 shape;
            block20: {
                block18: {
                    if (geom == null || ruleStyle == null || bounds == null || feature == null) {
                        return null;
                    }
                    Geometry featGeom = feature.getGeometry();
                    Object color = feature.getAttribute("Color");
                    if (color != null) {
                        this.objColor = AcadColor.getColor((int)((Number)color).intValue());
                    }
                    this.isSolid = ((String)feature.getAttribute("Entity")).equalsIgnoreCase("solid");
                    this.isLine = featGeom instanceof LineString || featGeom instanceof MultiLineString;
                    this.isPolygon = featGeom instanceof Polygon || featGeom instanceof MultiPolygon;
                    Object thicknessObj = feature.getAttribute("Thickness");
                    this.thickness = 1;
                    if (thicknessObj != null) {
                        this.thickness = ((Number)thicknessObj).intValue();
                        if (this.thickness == 0) {
                            this.thickness = 1;
                        }
                    }
                    shp = geom.getPathShapeInt(this.viewPort.getModelToViewTransform());
                    Rectangle2D shpBounds = shp.getBounds2D();
                    if (!this.isPoint || this.isText) break block18;
                    int scaledPointSize = this.thickness;
                    if (this.thickness == 1) {
                        scaledPointSize = (int)Math.floor((double)this.thickness * this.viewPort.getScale());
                    }
                    this.drawPoint(shpBounds, this.objColor, scaledPointSize);
                    return null;
                }
                if (!this.isText) break block19;
                shape = null;
                try {
                    shape = this.getTransformedShape(feature.getGeometry(), transform);
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                if (shape != null) break block20;
                return null;
            }
            TextSymbolizer textSymbol = this.getTextSymbolizer(this.layer.getFeatureSchema(), "Text", 3, Color.black, (Double)feature.getAttribute("RotationText"));
            double factor = 1.0;
            double scaleTextHeight = 1.0;
            Number textHeight = (Number)feature.getAttribute("HeightText");
            if (textSymbol.isScale()) {
                double max = textSymbol.getScaleMaxValue();
                double min = textSymbol.getScaleMinValue();
                factor = max - Math.tan(Math.atan((max - min) / this.layerWidth)) * this.viewWidth;
                scaleTextHeight = textHeight.floatValue() * (float)factor;
                if (scaleTextHeight < min) {
                    scaleTextHeight = min;
                }
            } else {
                scaleTextHeight = textHeight.doubleValue() * this.viewPort.getScale();
            }
            Number textRotation = (Number)feature.getAttribute("RotationText");
            labelCache.put(layerId, textSymbol, feature, shape, null, (Range)scaleRange, new Float(scaleTextHeight), this.objColor, new Double(textRotation.doubleValue()));
            return null;
        }
        try {
            Graphics2D g2 = this.image.getGraphics();
            if (this.isPolygon && this.isSolid) {
                if (this.objColor != null) {
                    g2.setPaint(this.objColor);
                }
                g2.fill(shp);
            }
            if (this.isPolygon || this.isLine) {
                g2.setPaint(this.objColor);
                g2.setStroke(new BasicStroke(this.thickness));
                g2.draw(shp);
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return null;
    }

    private TextSymbolizer getTextSymbolizer(FeatureSchema schema, String etq, int height, Color color, double rotationValue) {
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        StyleFactory factory = StyleFactory.createStyleFactory();
        AttributeExpression label = null;
        try {
            label = filterFactory.createAttributeExpression(schema, etq);
        }
        catch (IllegalFilterException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        Font jumpFont = baseFont.deriveFont(new AffineTransform());
        LiteralExpression fontFamily = filterFactory.createLiteralExpression(jumpFont.getFamily());
        boolean isBold = jumpFont.getStyle() == 1 || jumpFont.getStyle() > 2;
        boolean isItalic = jumpFont.getStyle() == 2 || jumpFont.getStyle() > 2;
        LiteralExpression fontWeight = null;
        fontWeight = isBold ? filterFactory.createLiteralExpression("bold") : filterFactory.createLiteralExpression("normal");
        LiteralExpression fontStyle = null;
        fontStyle = isItalic ? filterFactory.createLiteralExpression("italic") : filterFactory.createLiteralExpression("normal");
        LiteralExpression fontSize = filterFactory.createLiteralExpression(height);
        org.saig.core.styling.Font modelFont = factory.createFont(fontFamily, fontStyle, fontWeight, fontSize);
        Halo halo = null;
        LiteralExpression textColor = filterFactory.createLiteralExpression(Layer.decodeColor(color));
        Fill fillText = factory.createFill(textColor);
        Displacement offset = factory.createDisplacement(filterFactory.createLiteralExpression(0), filterFactory.createLiteralExpression(0));
        LiteralExpression rotation = filterFactory.createLiteralExpression(-rotationValue);
        AnchorPoint anchorPoint = factory.createAnchorPoint(filterFactory.createLiteralExpression(0), filterFactory.createLiteralExpression(0));
        PointPlacement labelPlac = factory.createPointPlacement(anchorPoint, offset, rotation);
        return factory.createTextSymbolizer(fillText, new org.saig.core.styling.Font[]{modelFont}, halo, label, labelPlac, null);
    }
}

