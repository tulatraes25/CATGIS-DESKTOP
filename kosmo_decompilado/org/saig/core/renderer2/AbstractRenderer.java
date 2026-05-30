/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.Point
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  javax.media.jai.util.Range
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 *  org.geotools.referencing.operation.GeneralMatrix
 *  org.geotools.util.NumberRange
 *  org.opengis.referencing.operation.MathTransform
 *  org.opengis.referencing.operation.MathTransform2D
 *  org.opengis.referencing.operation.Matrix
 *  org.opengis.referencing.operation.NoninvertibleTransformException
 */
package org.saig.core.renderer2;

import com.iver.cit.gvsig.fmap.core.styles.Line2DOffset;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.FeatureCollectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import es.kosmo.core.renderer.ShapeClipper;
import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.renderer.decorators.IDecoratorPoint2DTransformer;
import es.kosmo.core.renderer.style.IconStyle2D;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.media.jai.util.Range;
import javax.swing.Icon;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.util.NumberRange;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeFactory;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolygon2D;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.FilterUtil;
import org.saig.core.filter.NoneFilter;
import org.saig.core.filter.NullFilter;
import org.saig.core.renderer.Renderer;
import org.saig.core.renderer.lite.DashedShape;
import org.saig.core.renderer.lite.Decimator;
import org.saig.core.renderer.lite.LabelCache;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.renderer.lite.StyledShapePainter;
import org.saig.core.renderer.style.GraphicStyle2D;
import org.saig.core.renderer.style.LineStyle2D;
import org.saig.core.renderer.style.MarkStyle2D;
import org.saig.core.renderer.style.PolygonStyle2D;
import org.saig.core.renderer.style.RuleStyle;
import org.saig.core.renderer.style.SLDStyleFactory;
import org.saig.core.renderer.style.Style2D;
import org.saig.core.renderer2.IRenderer;
import org.saig.core.renderer2.ViewportDecoratorTransformerWrapper;
import org.saig.core.styling.Font;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.util.MeasureUtils;

@Deprecated
public abstract class AbstractRenderer
implements IRenderer {
    protected static final Logger LOGGER = Logger.getLogger(AbstractRenderer.class);
    protected static final SLDStyleFactory styleFactory = new SLDStyleFactory();
    protected static final StyledShapePainter painter = new StyledShapePainter();
    protected FilterFactory filterFactory = FilterFactory.createFilterFactory();
    protected static Canvas imgObserver = new Canvas();
    protected FeatureCollectionRenderer fcRenderer;
    protected boolean isLineLayer;
    protected Layer layer;
    protected Viewport viewPort;
    protected double layerWidth;
    protected double viewWidth;
    private Map<MathTransform2D, Decimator> decimators = new HashMap<MathTransform2D, Decimator>();
    protected ThreadSafeImage image;
    protected double factor;

    public AbstractRenderer(double factor) {
        this.factor = factor;
    }

    @Override
    public abstract void render(ThreadSafeImage var1, Layerable var2, Viewport var3, FeatureCollectionRenderer var4, boolean var5);

    protected abstract void dispose();

    protected void draw1px(Rectangle2D bounds, AffineTransform aft, int rgb, ThreadSafeImage image) {
        Point2D.Double pOrig = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
        Point2D pDest = aft.transform(pOrig, null);
        Point2D pDest2 = image.getGraphics().getTransform().transform(pDest, null);
        int pixX = (int)pDest2.getX();
        int pixY = (int)pDest2.getY();
        BufferedImage imag2 = (BufferedImage)image.getImage();
        if (pixX > 0 && pixX < imag2.getWidth() && pixY > 0 && pixY < imag2.getHeight()) {
            imag2.setRGB(pixX, pixY, rgb);
        }
    }

    protected void drawPoint(Rectangle2D bounds, Color color, int size) throws IOException {
        if (color == null || size == 0) {
            return;
        }
        BufferedImage image2 = (BufferedImage)this.image.getImage();
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

    protected LiteShape2 drawShape(Graphics2D g, IShapeGeometry geom, RuleStyle ruleStyle, LabelCache labelCache, MathTransform2D transform, NumberRange scaleRange, Feature feature, ICoordTrans ct, boolean isLineLayer, Viewport viewport) throws IOException {
        if (geom == null || ruleStyle == null) {
            return null;
        }
        LiteShape2 shape = null;
        try {
            try {
                IShape shp = geom.getPathShapeInt(this.viewPort.getModelToViewTransform());
                Rectangle2D bounds = shp.getBounds2D();
                for (Style2D style : ruleStyle.getStyles()) {
                    if (style instanceof MarkStyle2D) {
                        shape = this.drawMark(g, ruleStyle, (MarkStyle2D)style, feature, scaleRange, isLineLayer, transform, bounds, ct, shp);
                    } else if (style instanceof GraphicStyle2D) {
                        shape = this.drawGraphic(g, (GraphicStyle2D)style, ruleStyle, feature, scaleRange, isLineLayer, ct, transform);
                    } else if (style instanceof PolygonStyle2D) {
                        this.drawPolygon(g, (PolygonStyle2D)style, shp);
                    }
                    if (!(style instanceof LineStyle2D)) continue;
                    this.drawLine(g, (LineStyle2D)style, shp);
                }
            }
            catch (Exception e1) {
                LOGGER.error((Object)"", (Throwable)e1);
                if (CollectionUtils.isNotEmpty(ruleStyle.getDecorators())) {
                    this.drawDecorators(g, new ViewportDecoratorTransformerWrapper(viewport), ruleStyle.getDecorators(), feature, ct, transform);
                }
            }
        }
        finally {
            if (CollectionUtils.isNotEmpty(ruleStyle.getDecorators())) {
                this.drawDecorators(g, new ViewportDecoratorTransformerWrapper(viewport), ruleStyle.getDecorators(), feature, ct, transform);
            }
        }
        return shape;
    }

    protected LiteShape2 getTransformedShape(Geometry g, MathTransform2D transform) throws Exception {
        LiteShape2 shape = new LiteShape2(g, (MathTransform)transform, this.getDecimator(transform), false);
        return shape;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected LiteShape2 drawMark(Graphics2D g, RuleStyle ruleStyle, MarkStyle2D style, Feature feature, NumberRange scaleRange, boolean isLineLayer, MathTransform2D transform, Rectangle2D bounds, ICoordTrans ct, IShape shp) {
        LiteShape2 shape = null;
        try {
            if (!ruleStyle.loadLitleShape(style)) {
                Symbolizer symbol = ruleStyle.getSymbol(style);
                MarkStyle2D newStyle = (MarkStyle2D)styleFactory.createStyle(feature, symbol, (Range)scaleRange);
                Geometry geometry = this.getSelectedGeometry(feature, symbol);
                boolean notGeometryExist = geometry == null || geometry.isEmpty();
                boolean geometryIsRenderable = !(geometry instanceof Point) && !(geometry instanceof MultiPoint) && !(geometry instanceof LineString) && !(geometry instanceof MultiLineString);
                if (notGeometryExist) return null;
                if (geometryIsRenderable) {
                    return null;
                }
                if (isLineLayer) {
                    Point centroide = geometry.getCentroid();
                    if (ct != null) {
                        IShapeGeometry geom = ShapeGeometryConverter.jts_to_igeometry((Geometry)centroide);
                        geom.reProject(ct);
                        shape = this.getTransformedShape(ShapeGeometryConverter.java2d_to_jts(geom.getShp()), transform);
                    } else {
                        shape = this.getTransformedShape((Geometry)centroide, transform);
                    }
                    if (geometry.getNumGeometries() == 1) {
                        LineString lineStr = (LineString)geometry.getGeometryN(1);
                        LineSegment segment = new LineSegment(lineStr.getStartPoint().getCoordinate(), lineStr.getEndPoint().getCoordinate());
                        float angle = (float)segment.angle();
                        newStyle.setRotation(angle);
                    }
                } else if (ct != null) {
                    IShapeGeometry geom = ShapeGeometryConverter.jts_to_igeometry(geometry);
                    geom.reProject(ct);
                    shape = this.getTransformedShape(ShapeGeometryConverter.java2d_to_jts(geom.getShp()), transform);
                } else {
                    shape = this.getTransformedShape(geometry, transform);
                }
                if (!symbol.getUnitsOfMeasurement().equals("pixel")) {
                    painter.paint(g, shape, newStyle, this.factor);
                    return shape;
                }
                painter.paint(g, shape, newStyle, this.factor == 1.0 ? this.factor : this.factor * 2.0);
                return shape;
            }
            MarkStyle2D ms2d = style;
            Color color = (Color)ms2d.getFill();
            SAIGGeneralPath points = shp.getGeneralPath();
            int j = 0;
            while (true) {
                if (j >= points.numCoords / 2) {
                    return shape;
                }
                int k = j * 2;
                ShapeGeometry point = ShapeFactory.createPoint2D(points.pointCoords[k], points.pointCoords[k + 1]);
                this.drawPoint(point.getBounds2D(), color, ms2d.getSize());
                ++j;
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return shape;
    }

    protected LiteShape2 drawGraphic(Graphics2D g, GraphicStyle2D style, RuleStyle ruleStyle, Feature feature, NumberRange scaleRange, boolean isLineLayer, ICoordTrans ct, MathTransform2D transform) {
        Geometry geometry;
        Style2D newStyle;
        LiteShape2 shape;
        block10: {
            boolean geometryIsRenderable;
            shape = null;
            Symbolizer symbol = ruleStyle.getSymbol(style);
            newStyle = styleFactory.createStyle(feature, symbol, (Range)scaleRange);
            geometry = this.getSelectedGeometry(feature, symbol);
            boolean notGeometryExist = geometry == null || geometry.isEmpty();
            boolean bl = geometryIsRenderable = !(geometry instanceof Point) && !(geometry instanceof MultiPoint) && !(geometry instanceof LineString) && !(geometry instanceof MultiLineString);
            if (!notGeometryExist && !geometryIsRenderable) break block10;
            return null;
        }
        try {
            if (isLineLayer) {
                Point centroide = geometry.getCentroid();
                if (ct != null) {
                    IShapeGeometry geom = ShapeGeometryConverter.jts_to_igeometry((Geometry)centroide);
                    geom.reProject(ct);
                    shape = this.getTransformedShape(ShapeGeometryConverter.java2d_to_jts(geom.getShp()), transform);
                } else {
                    shape = this.getTransformedShape((Geometry)centroide, transform);
                }
                if (geometry.getNumGeometries() == 1) {
                    LineString lineStr = (LineString)geometry.getGeometryN(1);
                    LineSegment segment = new LineSegment(lineStr.getStartPoint().getCoordinate(), lineStr.getEndPoint().getCoordinate());
                    float angle = (float)segment.angle();
                    ((GraphicStyle2D)newStyle).setRotation(angle);
                }
            } else if (ct != null) {
                IShapeGeometry geom = ShapeGeometryConverter.jts_to_igeometry(geometry);
                geom.reProject(ct);
                shape = this.getTransformedShape(ShapeGeometryConverter.java2d_to_jts(geom.getShp()), transform);
            } else {
                shape = this.getTransformedShape(geometry, transform);
            }
            ((GraphicStyle2D)newStyle).setRotation(((GraphicStyle2D)newStyle).getRotation() * -1.0f);
            painter.paint(g, shape, newStyle, 1.0);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return shape;
    }

    private Geometry getSelectedGeometry(Feature feature, Symbolizer symbol) {
        Geometry geometry;
        if (symbol != null && symbol instanceof PointSymbolizer) {
            PointSymbolizer psym = (PointSymbolizer)symbol;
            String geometryPropertyName = psym.getGeometryPropertyName();
            if (geometryPropertyName != null && feature.getSchema().hasAttribute(geometryPropertyName)) {
                geometry = (Geometry)feature.getAttribute(geometryPropertyName);
                if (geometry == null) {
                    return null;
                }
            } else {
                geometry = feature.getGeometry();
            }
        } else {
            geometry = feature.getGeometry();
        }
        return geometry;
    }

    protected void drawPolygon(Graphics2D g2, PolygonStyle2D style, IShape shp) {
        if (style.getFill() != null) {
            Paint paint = this.getPaint(style.getFill(), g2.getTransform(), shp.getBounds2D());
            g2.setPaint(paint);
            g2.setComposite(style.getFillComposite());
            g2.fill(shp);
        }
    }

    protected Paint getPaint(Paint paint, AffineTransform at, Rectangle2D anchor) {
        Paint newPaint;
        if (paint instanceof TexturePaint) {
            TexturePaint tp = (TexturePaint)paint;
            BufferedImage image = tp.getImage();
            Rectangle2D rect = tp.getAnchorRect();
            double width = rect.getWidth() * at.getScaleX();
            double height = rect.getHeight() * at.getScaleY();
            Rectangle2D.Double scaledRect = new Rectangle2D.Double(0.0, 0.0, width, height);
            newPaint = new TexturePaint(image, scaledRect);
        } else if (paint instanceof LinearGradientPaint) {
            double anchorHeight;
            LinearGradientPaint lgp = (LinearGradientPaint)paint;
            double anchorWidth = anchor.getWidth();
            if (anchorWidth == 0.0) {
                anchorWidth = 1.0;
            }
            if ((anchorHeight = anchor.getHeight()) == 0.0) {
                anchorHeight = 1.0;
            }
            Point2D.Double p1 = new Point2D.Double(anchorWidth / 250.0 * lgp.getStartPoint().getX() + anchor.getMinX(), anchorHeight / 250.0 * lgp.getStartPoint().getY() + anchor.getMinY());
            Point2D.Double p2 = new Point2D.Double(anchorWidth / 250.0 * lgp.getEndPoint().getX() + anchor.getMinX(), anchorHeight / 250.0 * lgp.getEndPoint().getY() + anchor.getMinY());
            newPaint = new LinearGradientPaint((float)((Point2D)p1).getX(), (float)((Point2D)p1).getY(), (float)((Point2D)p2).getX(), (float)((Point2D)p2).getY(), lgp.getFractions(), lgp.getColors(), lgp.getCycleMethod());
        } else if (paint instanceof RadialGradientPaint) {
            double anchorRadius = Math.max(anchor.getWidth(), anchor.getHeight());
            if (anchorRadius == 0.0) {
                anchorRadius = 1.0;
            }
            RadialGradientPaint rgp = (RadialGradientPaint)paint;
            Point2D.Double center = new Point2D.Double(anchor.getWidth() / 250.0 * rgp.getCenterPoint().getX() + anchor.getMinX(), anchor.getHeight() / 250.0 * rgp.getCenterPoint().getY() + anchor.getMinY());
            newPaint = new RadialGradientPaint((float)((Point2D)center).getX(), (float)((Point2D)center).getY(), (float)(anchorRadius / 250.0 * (double)rgp.getRadius()), rgp.getFractions(), rgp.getColors(), rgp.getCycleMethod());
        } else {
            newPaint = paint;
        }
        return newPaint;
    }

    protected void drawLine(Graphics2D g2, LineStyle2D style, IShape shp) {
        LineStyle2D ls2d = style;
        if (ls2d.getStroke() != null) {
            if (ls2d.getGraphicStroke() != null) {
                this.drawWithGraphicsStroke(g2, this.dashShape(this.clip(shp, g2), ls2d.getStroke()), ls2d.getGraphicStroke());
            } else {
                Paint paint = this.getPaint(ls2d.getContour(), g2.getTransform(), shp.getBounds2D());
                Stroke stroke = ls2d.getScaledStroke(this.factor);
                double offset = ls2d.getOffset();
                if (offset != 0.0) {
                    shp = new ShapePolygon2D(Line2DOffset.offsetLine(shp, offset));
                }
                g2.setPaint(paint);
                g2.setStroke(stroke);
                g2.setComposite(ls2d.getContourComposite());
                g2.draw(this.clip(shp, g2));
            }
        }
    }

    protected Shape clip(IShape shp, Graphics2D g2) {
        Rectangle2D.Double view = new Rectangle2D.Double(0.0, 0.0, this.viewPort.getPanel().getWidth(), this.viewPort.getPanel().getHeight());
        ShapeClipper clipper = new ShapeClipper(view, new AffineTransform());
        return clipper.getClippedShape(shp, false);
    }

    protected Shape dashShape(Shape shape, Stroke stroke) {
        if (!(stroke instanceof BasicStroke)) {
            return shape;
        }
        BasicStroke bs = (BasicStroke)stroke;
        if (bs.getDashArray() == null || bs.getDashArray().length == 0) {
            return shape;
        }
        return new DashedShape(shape, bs.getDashArray(), bs.getDashPhase());
    }

    protected void drawDecorators(Graphics2D g, IDecoratorPoint2DTransformer viewport, List<IDecorator> decorators, Feature feature, ICoordTrans ct, MathTransform2D transform) {
        for (IDecorator element : decorators) {
            try {
                element.paint(feature, g, viewport, ct, 0.0, null);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
    }

    public void processTextSymbolizer(String layerId, RuleStyle ruleStyle, LiteShape2 shape, LabelCache labelCache, Feature feature, double layerWidth, double viewWidth, NumberRange scaleRange, MathTransform2D transform, ICoordTrans ct) {
        if (ruleStyle.hasTextSymbolizers()) {
            if (shape == null) {
                try {
                    if (ct != null) {
                        IShapeGeometry geom = ShapeGeometryConverter.jts_to_igeometry(feature.getGeometry());
                        geom.reProject(ct);
                        shape = this.getTransformedShape(ShapeGeometryConverter.java2d_to_jts(geom.getShp()), transform);
                    } else {
                        shape = this.getTransformedShape(feature.getGeometry(), transform);
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    return;
                }
            }
            FeatureSchema schema = feature.getSchema();
            for (TextSymbolizer textSymbol : ruleStyle.getTextSymbolizers()) {
                boolean unitsAreNotPixels;
                double min;
                double max;
                String geomColName = textSymbol.getGeometryPropertyName();
                LiteShape2 currentShape = null;
                LiteShape2 originalShape = null;
                String geometryAttribute = feature.getSchema().getAttributeName(feature.getSchema().getGeometryIndex());
                if (textSymbol.getOption("lineToLabel") != null && geomColName != null && !geomColName.equals(geometryAttribute)) {
                    try {
                        originalShape = this.getTransformedShape(feature.getGeometry(), transform);
                    }
                    catch (Exception e1) {
                        LOGGER.error((Object)"", (Throwable)e1);
                    }
                } else {
                    originalShape = null;
                }
                if (geomColName != null && feature.getSchema().hasAttribute(geomColName) && !geomColName.equals(schema.getAttributeName(schema.getGeometryIndex()))) {
                    Geometry labelGeom = (Geometry)feature.getAttribute(geomColName);
                    if (labelGeom == null || labelGeom.isEmpty()) {
                        if (textSymbol.getOption("ignoreLabelIfGeometriesAreNull") != null) {
                            return;
                        }
                        labelGeom = feature.getGeometry();
                        originalShape = null;
                    }
                    try {
                        if (ct != null) {
                            IShapeGeometry geom = ShapeGeometryConverter.jts_to_igeometry(labelGeom);
                            geom.reProject(ct);
                            currentShape = this.getTransformedShape(ShapeGeometryConverter.java2d_to_jts(geom.getShp()), transform);
                        }
                        currentShape = this.getTransformedShape(labelGeom, transform);
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        return;
                    }
                } else {
                    currentShape = shape;
                }
                double factor = 1.0;
                if (textSymbol.isScale() && (factor = (max = textSymbol.getScaleMaxValue()) - Math.tan(Math.atan((max - (min = textSymbol.getScaleMinValue())) / layerWidth)) * viewWidth) < min) {
                    factor = min;
                }
                Number height = null;
                if (textSymbol.getHeightAttribute() != null) {
                    Number textHeight = (Number)textSymbol.getHeightAttribute().getValue(feature);
                    if (textHeight != null) {
                        height = (double)Float.valueOf(textHeight.floatValue() * (float)factor).floatValue() * this.viewPort.getScale();
                    } else if (textSymbol.isScale()) {
                        height = Float.valueOf((float)factor);
                    }
                } else if (textSymbol.isScale()) {
                    height = Float.valueOf((float)factor);
                }
                String units = textSymbol.getUnitsOfMeasurement();
                boolean bl = unitsAreNotPixels = units != null && !units.equals("pixel");
                if (unitsAreNotPixels) {
                    Font[] fonts;
                    if (height == null && (fonts = textSymbol.getFonts()).length > 0) {
                        height = (Number)fonts[0].getFontSize().getValue(null);
                    }
                    if (height != null) {
                        double pxSize = this.viewPort.getEnvelopeInModelCoordinates().getWidth() / (double)this.viewPort.getPanel().getWidth();
                        Unit<Length> userLengthUnit = this.viewPort.getPanel().getUserLengthUnit();
                        Unit<Length> heightUnits = MeasureUtils.getLenghtUnitByName(units);
                        double transformLenght = MeasureUtils.transformLenght(height.doubleValue(), heightUnits, userLengthUnit);
                        if ((height = Double.valueOf(transformLenght / pxSize)).doubleValue() < 1.0) continue;
                    }
                }
                Number rotation = null;
                if (textSymbol.getAttributeRotation() != null) {
                    rotation = (Number)textSymbol.getAttributeRotation().getValue(feature);
                }
                double f = this.factor;
                if (this.factor != 1.0) {
                    boolean hasHeightAttribute = textSymbol.getHeightAttribute() != null;
                    boolean notUnitsInPixel = hasHeightAttribute || unitsAreNotPixels;
                    f = notUnitsInPixel ? 1.0 : this.factor * 2.0;
                }
                labelCache.put(layerId, textSymbol, feature, currentShape, originalShape, (Range)scaleRange, height, null, rotation, f);
            }
        }
    }

    protected void drawWithGraphicsStroke(Graphics2D graphics, Shape shape, Style2D graphicStroke) {
        Envelope envelope = this.viewPort.getEnvelopeInModelCoordinatesForQuery();
        Shape gClip = graphics.getClip();
        try {
            try {
                double imageSize;
                AffineTransform affineTransform = this.viewPort.getModelToViewTransform();
                MathTransform2D transform = (MathTransform2D)Renderer.mathTransformFactory.createAffineTransform((Matrix)new GeneralMatrix(affineTransform));
                graphics.setClip(this.getTransformedShape(EnvelopeUtil.toGeometry(envelope), transform));
                PathIterator pi = shape.getPathIterator(null);
                double[] coords = new double[4];
                if (graphicStroke instanceof MarkStyle2D) {
                    imageSize = ((MarkStyle2D)graphicStroke).getSize();
                } else if (graphicStroke instanceof IconStyle2D) {
                    imageSize = ((IconStyle2D)graphicStroke).getIcon().getIconWidth();
                } else {
                    GraphicStyle2D gs = (GraphicStyle2D)graphicStroke;
                    imageSize = gs.getImage().getWidth() - gs.getBorder();
                }
                double[] first = new double[2];
                double[] previous = new double[2];
                int type = pi.currentSegment(coords);
                first[0] = coords[0];
                first[1] = coords[1];
                previous[0] = coords[0];
                previous[1] = coords[1];
                pi.next();
                double remainder = imageSize / 2.0;
                while (!pi.isDone()) {
                    type = pi.currentSegment(coords);
                    switch (type) {
                        case 0: {
                            first[0] = coords[0];
                            first[1] = coords[1];
                            remainder = imageSize / 2.0;
                            break;
                        }
                        case 4: {
                            coords[0] = first[0];
                            coords[1] = first[1];
                            remainder = imageSize / 2.0;
                        }
                        case 1: {
                            double dx = coords[0] - previous[0];
                            double dy = coords[1] - previous[1];
                            double len = Math.sqrt(dx * dx + dy * dy);
                            if (len < remainder) {
                                remainder -= len;
                                break;
                            }
                            double theta = Math.atan2(dx, dy);
                            dx = Math.sin(theta) * imageSize;
                            dy = Math.cos(theta) * imageSize;
                            double rotation = -(theta - 1.5707963267948966);
                            double x = previous[0] + Math.sin(theta) * remainder;
                            double y = previous[1] + Math.cos(theta) * remainder;
                            double dist = 0.0;
                            dist = remainder;
                            while (dist < len) {
                                this.renderGraphicsStroke(graphics, x, y, graphicStroke, rotation, 1.0f);
                                x += dx;
                                y += dy;
                                dist += imageSize;
                            }
                            remainder = dist - len;
                            break;
                        }
                        default: {
                            LOGGER.warn((Object)"default branch reached in drawWithGraphicStroke");
                        }
                    }
                    previous[0] = coords[0];
                    previous[1] = coords[1];
                    pi.next();
                }
            }
            catch (Exception ex) {
                LOGGER.error((Object)"", (Throwable)ex);
                graphics.setClip(gClip);
            }
        }
        finally {
            graphics.setClip(gClip);
        }
    }

    protected void renderGraphicsStroke(Graphics2D graphics, double x, double y, Style2D style, double rotation, float opacity) {
        graphics.setComposite(AlphaComposite.getInstance(3, opacity));
        if (style instanceof GraphicStyle2D) {
            BufferedImage image = ((GraphicStyle2D)style).getImage();
            this.renderImage(graphics, x, y, image, rotation, opacity);
        } else if (style instanceof MarkStyle2D) {
            MarkStyle2D ms2d = (MarkStyle2D)style;
            Shape transformedShape = ms2d.getTransformedShape((float)x, (float)y, (float)rotation);
            if (transformedShape != null) {
                if (ms2d.getFill() != null) {
                    graphics.setPaint(ms2d.getFill());
                    graphics.fill(transformedShape);
                }
                if (ms2d.getContour() != null) {
                    graphics.setPaint(ms2d.getContour());
                    graphics.setStroke(ms2d.getStroke());
                    graphics.draw(transformedShape);
                }
            }
        } else if (style instanceof IconStyle2D) {
            IconStyle2D icons = (IconStyle2D)style;
            Icon icon = icons.getIcon();
            AffineTransform markAT = new AffineTransform(graphics.getTransform());
            markAT.translate(x, y);
            markAT.rotate(rotation);
            double dx = (double)(-icon.getIconWidth()) / 2.0;
            double dy = (double)(-icon.getIconHeight()) / 2.0;
            markAT.translate(dx, dy);
            AffineTransform temp = graphics.getTransform();
            try {
                graphics.setTransform(markAT);
                icon.paintIcon(null, graphics, 0, 0);
            }
            finally {
                graphics.setTransform(temp);
            }
        }
    }

    private void renderImage(Graphics2D graphics, double x, double y, BufferedImage image, double rotation, float opacity) {
        AffineTransform markAT = new AffineTransform();
        markAT.translate(x, y);
        markAT.rotate(rotation);
        markAT.translate((double)(-image.getWidth()) / 2.0, (double)(-image.getHeight()) / 2.0);
        graphics.setComposite(AlphaComposite.getInstance(3, opacity));
        Object interpolation = graphics.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        if (interpolation == null) {
            interpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        }
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawRenderedImage(image, markAT);
        }
        finally {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
        }
    }

    protected Object[] getRulesInScale(Rule[] rules, NumberRange scaleRange, Layer layer, Viewport viewPort, double factor) {
        HashSet<String> labels = new HashSet<String>();
        boolean hasFilter = false;
        RuleStyle elseRule = null;
        ArrayList<RuleStyle> rulesInScale = new ArrayList<RuleStyle>();
        boolean hasfeature = false;
        boolean loadFeature = false;
        boolean hasTextSymbolizer = false;
        Filter elseFilter = null;
        int i = 0;
        while (i < rules.length) {
            Rule rule = rules[i];
            if (rule.isEnabled()) {
                if (elseFilter == null) {
                    if (rule.getFilter() != null) {
                        elseFilter = rule.getFilter().not();
                    }
                } else if (rule.getFilter() != null) {
                    elseFilter = elseFilter.and(rule.getFilter().not());
                }
                if ((rule.getMinScaleDenominator() <= viewPort.getPanel().getScale() || Double.isNaN(rule.getMinScaleDenominator())) && (rule.getMaxScaleDenominator() >= viewPort.getPanel().getScale() || Double.isNaN(rule.getMaxScaleDenominator()))) {
                    RuleStyle ruleStyle = new RuleStyle(rule, scaleRange, false, factor, viewPort.getPixelSize(), viewPort.getPanel().getUserLengthUnit(), layer.getFeatureSchema());
                    if (rule.isElseFilter()) {
                        elseRule = ruleStyle;
                    } else {
                        rulesInScale.add(ruleStyle);
                    }
                    if (ruleStyle.hasTextSymbolizers() || ruleStyle.hasPointSymbolizers()) {
                        hasfeature = true;
                        loadFeature = true;
                        if (ruleStyle.hasTextSymbolizers()) {
                            hasTextSymbolizer = true;
                        }
                    }
                    if (ruleStyle.getFilter() != null) {
                        hasFilter = true;
                        hasfeature = true;
                        if (ruleStyle.isGeometryFilter()) {
                            loadFeature = true;
                        }
                    }
                    labels.addAll(ruleStyle.getAllLabels());
                }
            }
            ++i;
        }
        if (elseRule != null) {
            FeatureSchema schema = layer.getUltimateFeatureCollectionWrapper().getFeatureSchema();
            if (elseFilter != null) {
                Set<String> nullFields = FilterUtil.getLabelsFromFilter(elseFilter, schema);
                Iterator<String> iter = nullFields.iterator();
                while (iter.hasNext()) {
                    try {
                        NullFilter nullFilter = this.filterFactory.createNullFilter();
                        AttributeExpression attribute = this.filterFactory.createAttributeExpression(iter.next());
                        nullFilter.setNullCheckValue(attribute);
                        elseFilter = elseFilter.or(nullFilter);
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                }
                elseRule.setFilter(elseFilter, schema);
            } else {
                elseRule.setFilter(new NoneFilter(), schema);
            }
        }
        Collections.reverse(rulesInScale);
        return new Object[]{rulesInScale, elseRule, new Boolean(hasfeature), new Boolean(loadFeature), new Boolean(hasFilter), new Boolean(hasTextSymbolizer), labels};
    }

    protected double getDistancia1Px(AffineTransform trans) {
        try {
            AffineTransform at = trans.createInverse();
            java.awt.Point pPixel = new java.awt.Point(1, 1);
            Point2D.Float pProv = new Point2D.Float();
            at.deltaTransform(pPixel, pProv);
            return pProv.x;
        }
        catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected void paintOneQueryByFilter(Layer layer, ThreadSafeImage image, List<RuleStyle> rulesWithFilter, AffineTransform affineTransform, final RuleStyle elseRule, final LabelCache labelCache, final MathTransform2D transform, final NumberRange scaleRange, Envelope envelope, boolean hasTextSymbolizer) throws Exception {
        block13: {
            ct = layer.getCoordTrans();
            viewTransf = layer.getTransformedEnvelope(envelope);
            layerName = layer.getName();
            if (elseRule == null) break block13;
            filterElse = elseRule.getFilter();
            iterator = null;
            try {
                iterator = layer.getUltimateFeatureCollectionWrapper().queryOnlyGeometryIterator(filterElse, viewTransf, elseRule.getLabels());
                if (!hasTextSymbolizer) ** GOTO lbl19
                while (!this.fcRenderer.cancelled && iterator.hasNext()) {
                    feature = iterator.next();
                    geom = this.transformShapeToNewSR(layer.getCoordTrans(), feature.getGeometry(), transform);
                    image.draw(new ThreadSafeImage.Drawer(){

                        @Override
                        public void draw(Graphics2D g) throws Exception {
                            LiteShape2 shape = AbstractRenderer.this.drawShape(g, geom, elseRule, labelCache, transform, scaleRange, feature, ct, AbstractRenderer.this.isLineLayer, AbstractRenderer.this.viewPort);
                            AbstractRenderer.this.processTextSymbolizer(layerName, elseRule, shape, labelCache, feature, AbstractRenderer.this.layerWidth, AbstractRenderer.this.viewWidth, scaleRange, transform, ct);
                        }
                    });
                }
                break block13;
lbl-1000:
                // 1 sources

                {
                    feature = iterator.next();
                    geom = this.transformShapeToNewSR(layer.getCoordTrans(), feature.getGeometry(), transform);
                    image.draw(new ThreadSafeImage.Drawer(){

                        @Override
                        public void draw(Graphics2D g) throws Exception {
                            AbstractRenderer.this.drawShape(g, geom, elseRule, labelCache, transform, scaleRange, feature, ct, AbstractRenderer.this.isLineLayer, AbstractRenderer.this.viewPort);
                        }
                    });
lbl19:
                    // 2 sources

                    ** while (!this.fcRenderer.cancelled && iterator.hasNext())
                }
lbl20:
                // 1 sources

            }
            finally {
                if (iterator != null) {
                    iterator.close(this.fcRenderer.cancelled);
                }
            }
        }
        iter = rulesWithFilter.iterator();
        while (!this.fcRenderer.cancelled && iter.hasNext()) {
            ruleStyle = iter.next();
            filter = ruleStyle.getFilter();
            iterator = null;
            try {
                iterator = layer.getUltimateFeatureCollectionWrapper().queryOnlyGeometryIterator(filter, viewTransf, ruleStyle.getLabels());
                if (!hasTextSymbolizer) ** GOTO lbl43
                while (!this.fcRenderer.cancelled && iterator.hasNext()) {
                    feature = iterator.next();
                    geom = this.transformShapeToNewSR(layer.getCoordTrans(), feature.getGeometry(), transform);
                    image.draw(new ThreadSafeImage.Drawer(){

                        @Override
                        public void draw(Graphics2D g) throws Exception {
                            LiteShape2 shape = AbstractRenderer.this.drawShape(g, geom, ruleStyle, labelCache, transform, scaleRange, feature, ct, AbstractRenderer.this.isLineLayer, AbstractRenderer.this.viewPort);
                            AbstractRenderer.this.processTextSymbolizer(layerName, ruleStyle, shape, labelCache, feature, AbstractRenderer.this.layerWidth, AbstractRenderer.this.viewWidth, scaleRange, transform, ct);
                        }
                    });
                }
                continue;
lbl-1000:
                // 1 sources

                {
                    feature = iterator.next();
                    geom = this.transformShapeToNewSR(layer.getCoordTrans(), feature.getGeometry(), transform);
                    image.draw(new ThreadSafeImage.Drawer(){

                        @Override
                        public void draw(Graphics2D g) throws Exception {
                            AbstractRenderer.this.drawShape(g, geom, ruleStyle, labelCache, transform, scaleRange, feature, ct, AbstractRenderer.this.isLineLayer, AbstractRenderer.this.viewPort);
                        }
                    });
lbl43:
                    // 2 sources

                    ** while (!this.fcRenderer.cancelled && iterator.hasNext())
                }
lbl44:
                // 1 sources

            }
            finally {
                if (iterator != null) {
                    iterator.close(this.fcRenderer.cancelled);
                }
            }
        }
    }

    /*
     * Unable to fully structure code
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected void paintOnlyQuery(Layer layer, ThreadSafeImage image, List<RuleStyle> rulesWithFilter, AffineTransform affineTransform, final RuleStyle elseRule, final LabelCache labelCache, final MathTransform2D transform, final NumberRange scaleRange, Envelope envelope, boolean hasFilter, boolean hasTextSymbolizer, List<String> labels) throws Exception {
        ct = layer.getCoordTrans();
        layerName = layer.getName();
        iterator = null;
        try {
            try {
                block21: {
                    block19: {
                        block20: {
                            block18: {
                                viewTransf = layer.getTransformedEnvelope(envelope);
                                iterator = layer.getUltimateFeatureCollectionWrapper().queryOnlyGeometryIterator(viewTransf, labels);
                                if (!hasFilter) break block18;
                                if (!hasTextSymbolizer) ** GOTO lbl42
                                if (true) ** GOTO lbl25
                                do {
                                    feature = iterator.next();
                                    geom = this.transformShapeToNewSR(layer.getCoordTrans(), feature.getGeometry(), transform);
                                    check = false;
                                    if (elseRule != null && elseRule.getFilter().contains(feature)) {
                                        check = true;
                                        image.draw(new ThreadSafeImage.Drawer(){

                                            @Override
                                            public void draw(Graphics2D g) throws Exception {
                                                LiteShape2 shape = AbstractRenderer.this.drawShape(g, geom.cloneGeometry(), elseRule, labelCache, transform, scaleRange, feature, ct, AbstractRenderer.this.isLineLayer, AbstractRenderer.this.viewPort);
                                                AbstractRenderer.this.processTextSymbolizer(layerName, elseRule, shape, labelCache, feature, AbstractRenderer.this.layerWidth, AbstractRenderer.this.viewWidth, scaleRange, transform, ct);
                                            }
                                        });
                                    }
                                    itRules = rulesWithFilter.iterator();
                                    while (itRules.hasNext() && !check) {
                                        style = itRules.next();
                                        ruleFilter = style.getFilter();
                                        if (ruleFilter != null && (ruleFilter == null || !ruleFilter.contains(feature))) continue;
                                        image.draw(new ThreadSafeImage.Drawer(){

                                            @Override
                                            public void draw(Graphics2D g) throws Exception {
                                                LiteShape2 shape = AbstractRenderer.this.drawShape(g, geom.cloneGeometry(), style, labelCache, transform, scaleRange, feature, ct, AbstractRenderer.this.isLineLayer, AbstractRenderer.this.viewPort);
                                                AbstractRenderer.this.processTextSymbolizer(layerName, style, shape, labelCache, feature, AbstractRenderer.this.layerWidth, AbstractRenderer.this.viewWidth, scaleRange, transform, ct);
                                            }
                                        });
                                    }
lbl25:
                                    // 2 sources

                                    if (this.fcRenderer.cancelled) return;
                                } while (iterator.hasNext());
                                return;
lbl-1000:
                                // 1 sources

                                {
                                    feature = iterator.next();
                                    geom = this.transformShapeToNewSR(layer.getCoordTrans(), feature.getGeometry(), transform);
                                    check = false;
                                    if (elseRule != null && elseRule.getFilter().contains(feature)) {
                                        image.draw(new ThreadSafeImage.Drawer(){

                                            @Override
                                            public void draw(Graphics2D g) throws Exception {
                                                AbstractRenderer.this.drawShape(g, geom.cloneGeometry(), elseRule, labelCache, transform, scaleRange, feature, ct, AbstractRenderer.this.isLineLayer, AbstractRenderer.this.viewPort);
                                            }
                                        });
                                        check = true;
                                    }
                                    itRules = rulesWithFilter.iterator();
                                    while (itRules.hasNext() && !check) {
                                        style = itRules.next();
                                        ruleFilter = style.getFilter();
                                        if (ruleFilter != null && (ruleFilter == null || !ruleFilter.contains(feature))) continue;
                                        check = true;
                                        image.draw(new ThreadSafeImage.Drawer(){

                                            @Override
                                            public void draw(Graphics2D g) throws Exception {
                                                AbstractRenderer.this.drawShape(g, geom.cloneGeometry(), style, labelCache, transform, scaleRange, feature, ct, AbstractRenderer.this.isLineLayer, AbstractRenderer.this.viewPort);
                                            }
                                        });
                                    }
lbl42:
                                    // 2 sources

                                    if (this.fcRenderer.cancelled) return;
                                    ** while (iterator.hasNext())
                                }
lbl44:
                                // 1 sources

                                return;
                            }
                            if (!hasTextSymbolizer) break block19;
                            if (rulesWithFilter.size() <= 0) return;
                            if (rulesWithFilter.size() <= 1) break block20;
                            if (true) ** GOTO lbl56
                            do {
                                feature = iterator.next();
                                geom = this.transformShapeToNewSR(layer.getCoordTrans(), feature.getGeometry(), transform);
                                for (final RuleStyle style : rulesWithFilter) {
                                    image.draw(new ThreadSafeImage.Drawer(){

                                        @Override
                                        public void draw(Graphics2D g) throws Exception {
                                            LiteShape2 shape = AbstractRenderer.this.drawShape(g, geom.cloneGeometry(), style, labelCache, transform, scaleRange, feature, ct, AbstractRenderer.this.isLineLayer, AbstractRenderer.this.viewPort);
                                            AbstractRenderer.this.processTextSymbolizer(layerName, style, shape, labelCache, feature, AbstractRenderer.this.layerWidth, AbstractRenderer.this.viewWidth, scaleRange, transform, ct);
                                        }
                                    });
                                }
lbl56:
                                // 2 sources

                                if (this.fcRenderer.cancelled) return;
                            } while (iterator.hasNext());
                            return;
                        }
                        style = rulesWithFilter.get(0);
                        if (true) ** GOTO lbl66
                        do {
                            feature = iterator.next();
                            geom = this.transformShapeToNewSR(layer.getCoordTrans(), feature.getGeometry(), transform);
                            image.draw(new ThreadSafeImage.Drawer(){

                                @Override
                                public void draw(Graphics2D g) throws Exception {
                                    LiteShape2 shape = AbstractRenderer.this.drawShape(g, geom.cloneGeometry(), style, labelCache, transform, scaleRange, feature, ct, AbstractRenderer.this.isLineLayer, AbstractRenderer.this.viewPort);
                                    AbstractRenderer.this.processTextSymbolizer(layerName, style, shape, labelCache, feature, AbstractRenderer.this.layerWidth, AbstractRenderer.this.viewWidth, scaleRange, transform, ct);
                                }
                            });
lbl66:
                            // 2 sources

                            if (this.fcRenderer.cancelled) return;
                        } while (iterator.hasNext());
                        return;
                    }
                    if (rulesWithFilter.size() <= 0) return;
                    if (rulesWithFilter.size() <= 1) break block21;
                    if (true) ** GOTO lbl79
                    do {
                        feature = iterator.next();
                        geom = this.transformShapeToNewSR(layer.getCoordTrans(), feature.getGeometry(), transform);
                        for (final RuleStyle style : rulesWithFilter) {
                            image.draw(new ThreadSafeImage.Drawer(){

                                @Override
                                public void draw(Graphics2D g) throws Exception {
                                    AbstractRenderer.this.drawShape(g, geom.cloneGeometry(), style, labelCache, transform, scaleRange, feature, ct, AbstractRenderer.this.isLineLayer, AbstractRenderer.this.viewPort);
                                }
                            });
                        }
lbl79:
                        // 2 sources

                        if (this.fcRenderer.cancelled) return;
                    } while (iterator.hasNext());
                    return;
                }
                style = rulesWithFilter.get(0);
                if (true) ** GOTO lbl89
                do {
                    feature = iterator.next();
                    geom = this.transformShapeToNewSR(layer.getCoordTrans(), feature.getGeometry(), transform);
                    image.draw(new ThreadSafeImage.Drawer(){

                        @Override
                        public void draw(Graphics2D g) throws Exception {
                            AbstractRenderer.this.drawShape(g, geom.cloneGeometry(), style, labelCache, transform, scaleRange, feature, ct, AbstractRenderer.this.isLineLayer, AbstractRenderer.this.viewPort);
                        }
                    });
lbl89:
                    // 2 sources

                    if (this.fcRenderer.cancelled) return;
                } while (iterator.hasNext());
                return;
            }
            catch (Exception e) {
                AbstractRenderer.LOGGER.error((Object)"", (Throwable)e);
                if (iterator == null) return;
                iterator.close(this.fcRenderer.cancelled);
            }
            return;
        }
        finally {
            if (iterator != null) {
                iterator.close(this.fcRenderer.cancelled);
            }
        }
    }

    protected void processFeature(final String layerName, final Feature feature, ThreadSafeImage image, final RuleStyle style, final LabelCache labelCache, final MathTransform2D transform, final NumberRange scaleRange, final ICoordTrans ct) throws Exception {
        final IShapeGeometry geom = this.transformShapeToNewSR(this.layer.getCoordTrans(), feature.getGeometry(), transform);
        image.draw(new ThreadSafeImage.Drawer(){

            @Override
            public void draw(Graphics2D g) throws Exception {
                LiteShape2 shape = AbstractRenderer.this.drawShape(g, geom, style, labelCache, transform, scaleRange, feature, ct, AbstractRenderer.this.isLineLayer, AbstractRenderer.this.viewPort);
                AbstractRenderer.this.processTextSymbolizer(layerName, style, shape, labelCache, feature, AbstractRenderer.this.layerWidth, AbstractRenderer.this.viewWidth, scaleRange, transform, ct);
            }
        });
    }

    protected void process1Px(ThreadSafeImage image, Rectangle2D bounds, AffineTransform aft, List<RuleStyle> rulesWithFilter, Feature feature) throws IOException {
        for (RuleStyle ruleStyle : rulesWithFilter) {
            Filter filter = ruleStyle.getFilter();
            if (filter != null && !filter.contains(feature)) continue;
            this.paint1Px(ruleStyle, bounds, aft, image);
        }
    }

    protected void paint1Px(RuleStyle ruleStyle, Rectangle2D bounds, AffineTransform aft, ThreadSafeImage image) {
        for (Style2D style : ruleStyle.getStyles()) {
            LineStyle2D ls2d;
            Paint paint;
            Color color = null;
            if (style instanceof MarkStyle2D) {
                MarkStyle2D ms2d = (MarkStyle2D)style;
                color = (Color)ms2d.getFill();
            } else if (style instanceof PolygonStyle2D) {
                PolygonStyle2D polygonStyle2D = (PolygonStyle2D)style;
                if (polygonStyle2D.getFill() != null) {
                    paint = polygonStyle2D.getFill();
                    if (paint instanceof TexturePaint || paint instanceof MultipleGradientPaint) continue;
                    color = (Color)paint;
                } else {
                    LineStyle2D ls2d2 = (LineStyle2D)style;
                    if (ls2d2.getStroke() != null) {
                        Paint paint2 = ls2d2.getContour();
                        color = paint2 instanceof TexturePaint || paint2 instanceof MultipleGradientPaint || ls2d2.getGraphicStroke() != null ? Color.darkGray : (Color)paint2;
                    }
                }
            } else if (style instanceof LineStyle2D && (ls2d = (LineStyle2D)style).getStroke() != null) {
                paint = ls2d.getContour();
                color = paint instanceof TexturePaint || paint instanceof MultipleGradientPaint || ls2d.getGraphicStroke() != null ? Color.darkGray : (Color)paint;
            }
            if (color == null) continue;
            this.draw1px(bounds, aft, color.getRGB(), image);
        }
    }

    protected IShapeGeometry transformShapeToNewSR(ICoordTrans ct, Geometry featGeom, MathTransform2D transform) throws Exception {
        IShapeGeometry geom = ShapeGeometryConverter.jts_to_igeometry(featGeom);
        if (ct != null) {
            geom = geom.cloneGeometry();
            geom.reProject(ct);
        }
        return geom;
    }

    private Decimator getDecimator(MathTransform2D mathTransform) throws org.opengis.referencing.operation.NoninvertibleTransformException {
        Decimator decimator = this.decimators.get(mathTransform);
        if (decimator == null) {
            decimator = mathTransform != null && !mathTransform.isIdentity() ? new Decimator(mathTransform.inverse(), new Rectangle()) : new Decimator(null, new Rectangle());
            this.decimators.put(mathTransform, decimator);
        }
        return decimator;
    }

    protected boolean checkCoordinates(Layer layer, Viewport viewport) throws Exception {
        return true;
    }
}

