/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.FeatureCollectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import com.vividsolutions.jump.workbench.ui.renderer.style.StyleUtil;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.parser.ParseException;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Mark;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.RuleImpl;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.Symbolizer;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.util.LayerUtil;

public abstract class AbstractSelectionRenderer
extends FeatureCollectionRenderer
implements Style {
    public static final int HANDLE_WIDTH = 5;
    public static boolean paintingHandleOn = true;
    protected boolean enabled = true;
    protected Stroke handleStroke = new BasicStroke(1.0f);
    protected Color handleFillColor;
    protected Color handleLineColor = Color.black;
    protected Stroke lineStroke = new BasicStroke(2.0f);
    protected Color lineColor;
    protected Stroke fillStroke = new BasicStroke(1.0f);
    protected Color fillColor;
    protected boolean filling = true;
    protected PointSymbolizer vertexSymbolizer;
    private CollectionMap featureToSelectedItemsMap;
    private boolean paintingHandles;

    public AbstractSelectionRenderer(Object contentID, LayerViewPanel panel, Color color, boolean paintingHandles, boolean filling, double factor) {
        super(contentID, panel, factor);
        this.handleFillColor = color;
        this.lineColor = color;
        this.fillColor = GUIUtil.alphaColor(Color.white, 75);
        this.filling = filling;
        this.vertexSymbolizer = this.generateDefaultVertexSymbolizer();
    }

    protected PointSymbolizer generateDefaultVertexSymbolizer() {
        PointSymbolizer symb = SLDEditor.styleBuilder.createPointSymbolizer();
        Fill fill = SLDEditor.styleBuilder.createFill(this.handleFillColor);
        try {
            float alpha = new Float((float)this.handleFillColor.getAlpha() / 255.0f).floatValue();
            Expression opacity = (Expression)ExpressionBuilder.parse("" + alpha);
            fill.setOpacity(opacity);
        }
        catch (ParseException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        symb.getGraphic().getMarks()[0].setFill(fill);
        return symb;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public void setVertexSymbolizer(PointSymbolizer pointSymbolizer) {
        this.vertexSymbolizer = pointSymbolizer;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        this.filling = fillColor != null;
    }

    public void setHandleLineColor(Color handleFillColor) {
        this.handleFillColor = handleFillColor;
    }

    @Override
    public String getName() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Icon getIcon() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere();
            return null;
        }
    }

    @Override
    public void initialize(Layer layer) {
        this.paintingHandles = layer.isEditable() && paintingHandleOn;
    }

    @Override
    public void paint(Feature f, Graphics2D g, Viewport viewport) throws NoninvertibleTransformException {
        for (Geometry geometry : this.featureToSelectedItemsMap.getItems(f)) {
            this.paint(geometry, g, viewport);
        }
    }

    public void paint(Geometry geometry, Graphics2D g, Viewport viewport) throws NoninvertibleTransformException {
        Coordinate[] coordinates = geometry.getCoordinates();
        StyleUtil.paint(geometry, g, viewport, this.filling, this.fillStroke, this.fillColor, true, this.lineStroke, this.lineColor);
        if (this.paintingHandles) {
            AbstractSelectionRenderer.paintHandles(g, coordinates, this.handleStroke, this.handleFillColor, this.handleLineColor, this.panel.getViewport());
        }
    }

    @Override
    protected void renderHook(ThreadSafeImage image) throws Exception {
        List<Layer> layers = this.panel.getLayerManager().getLayers();
        layers.addAll(this.panel.getLayerManager().getHideLayers());
        for (Layer layer : layers) {
            this.featureToSelectedItemsMap = this.featureToSelectedItemsMap(layer);
            this.featureIterator = this.featureToSelectedItemsMap.keySet().iterator();
            this.renderFeatures(image, this.featureIterator, this, layer);
        }
    }

    protected abstract CollectionMap featureToSelectedItemsMap(Layer var1);

    protected Collection<Feature> featureToSelectedItemsMap(Layer layer, Envelope envelope) {
        return this.panel.getSelectionManager().getFeatureSelection().getFeatureToSelectedItemCollectionMap(layer, envelope);
    }

    private static Shape handle(Point2D point) {
        Rectangle2D.Double handle = new Rectangle2D.Double(0.0, 0.0, 5.0, 5.0);
        handle.x = point.getX() - 2.0;
        handle.y = point.getY() - 2.0;
        return handle;
    }

    public static void paintHandles(Graphics2D g, Coordinate[] coordinates, Stroke stroke, Color fillColor, Color lineColor, Viewport viewport) throws NoninvertibleTransformException {
        g.setStroke(stroke);
        g.setColor(fillColor);
        int i = 0;
        while (i < coordinates.length) {
            g.fill(AbstractSelectionRenderer.handle(viewport.toViewPoint(new Point2D.Double(coordinates[i].x, coordinates[i].y))));
            ++i;
        }
        g.setColor(lineColor);
        i = 0;
        while (i < coordinates.length) {
            g.draw(AbstractSelectionRenderer.handle(viewport.toViewPoint(new Point2D.Double(coordinates[i].x, coordinates[i].y))));
            ++i;
        }
    }

    protected void renderFeatures(ThreadSafeImage image, Iterator<Feature> featureIterator, final Style style, Layer layer) throws Exception {
        if (!layer.isVisible() || !style.isEnabled()) {
            return;
        }
        style.initialize(layer);
        while (featureIterator.hasNext()) {
            final Feature feature = featureIterator.next();
            if (this.cancelled) break;
            if (feature.getGeometry() == null || feature.getGeometry().isEmpty()) continue;
            image.draw(new ThreadSafeImage.Drawer(){

                @Override
                public void draw(Graphics2D g) throws Exception {
                    style.paint(feature, g, AbstractSelectionRenderer.this.panel.getViewport());
                }
            });
        }
    }

    protected Rule getSelectionRule(Layer layer) {
        RuleImpl rule = null;
        if (LayerUtil.isCADLayer(layer)) {
            Symbolizer pointSymb = this.createPointSymbolizer(this.lineColor);
            Symbolizer lineSymb = this.createLineSymbolizer();
            Symbolizer polygonSymb = this.createPolygonSymbolizer();
            rule = new RuleImpl(new Symbolizer[]{pointSymb, lineSymb, polygonSymb});
        } else {
            rule = LayerUtil.isPolygonLayer(layer) ? new RuleImpl(new Symbolizer[]{this.createPolygonSymbolizer()}) : (LayerUtil.isLinealLayer(layer) ? new RuleImpl(new Symbolizer[]{this.createLineSymbolizer()}) : new RuleImpl(new Symbolizer[]{this.createPointSymbolizer(this.lineColor)}));
        }
        return rule;
    }

    private Symbolizer createPointSymbolizer(Color color) {
        Graphic graphic = SLDEditor.styleBuilder.createGraphic();
        try {
            float alpha = new Float((float)color.getAlpha() / 255.0f).floatValue();
            Expression opacity = (Expression)ExpressionBuilder.parse("" + alpha);
            Mark mark = SLDEditor.styleBuilder.createMark("square", color);
            mark.getFill().setOpacity(opacity);
            graphic = SLDEditor.styleBuilder.createGraphic(null, mark, null);
        }
        catch (ParseException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return SLDEditor.styleBuilder.createPointSymbolizer(graphic);
    }

    private Symbolizer createLineSymbolizer() {
        float alpha = 1.0f;
        org.saig.core.styling.Stroke stroke = this.getStroke((BasicStroke)this.lineStroke, alpha, this.lineColor, SLDEditor.filterFactory, SLDEditor.styleFactory);
        return SLDEditor.styleBuilder.createLineSymbolizer(stroke);
    }

    private Symbolizer createPolygonSymbolizer() {
        float strokeAlpha = 1.0f;
        org.saig.core.styling.Stroke stroke = this.getStroke((BasicStroke)this.lineStroke, strokeAlpha, this.lineColor, SLDEditor.filterFactory, SLDEditor.styleFactory);
        Fill fill = null;
        if (this.fillColor != null) {
            fill = SLDEditor.styleBuilder.createFill(this.fillColor);
            try {
                float alpha = new Float((float)this.fillColor.getAlpha() / 255.0f).floatValue();
                Expression opacity = (Expression)ExpressionBuilder.parse("" + alpha);
                fill.setOpacity(opacity);
            }
            catch (ParseException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return SLDEditor.styleBuilder.createPolygonSymbolizer(stroke, fill);
    }

    private org.saig.core.styling.Stroke getStroke(BasicStroke jumpStroke, float alpha, Color color, FilterFactory filterFactory, StyleFactory factory) {
        LiteralExpression strokeColor = filterFactory.createLiteralExpression(Layer.decodeColor(color));
        LiteralExpression strokeWidth = filterFactory.createLiteralExpression(jumpStroke.getLineWidth());
        LiteralExpression strokeOpacity = filterFactory.createLiteralExpression(alpha);
        float[] dashArray = jumpStroke.getDashArray();
        int lineJoin_ = jumpStroke.getLineJoin();
        LiteralExpression lineJoin = null;
        if (lineJoin_ == 2) {
            lineJoin = filterFactory.createLiteralExpression("bevel");
        } else if (lineJoin_ == 0) {
            lineJoin = filterFactory.createLiteralExpression("mitre");
        } else if (lineJoin_ == 1) {
            lineJoin = filterFactory.createLiteralExpression("round");
        }
        LiteralExpression lineCap = null;
        int lineCap_ = jumpStroke.getEndCap();
        if (lineCap_ == 0) {
            lineCap = filterFactory.createLiteralExpression("butt");
        } else if (lineCap_ == 1) {
            lineCap = filterFactory.createLiteralExpression("round");
        } else if (lineCap_ == 2) {
            lineCap = filterFactory.createLiteralExpression("square");
        }
        LiteralExpression dashOffset = filterFactory.createLiteralExpression(jumpStroke.getDashPhase());
        return factory.createStroke(strokeColor, strokeWidth, strokeOpacity, lineJoin, lineCap, dashArray, dashOffset, null, null);
    }
}

