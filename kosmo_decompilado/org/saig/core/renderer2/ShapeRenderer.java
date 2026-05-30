/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 *  org.geotools.referencing.operation.GeneralMatrix
 *  org.geotools.resources.geometry.XRectangle2D
 *  org.geotools.util.NumberRange
 *  org.opengis.referencing.operation.MathTransform2D
 *  org.opengis.referencing.operation.Matrix
 */
package org.saig.core.renderer2;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.FeatureCollectionRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import es.kosmo.core.renderer.label.LabelCacheImpl;
import es.kosmo.core.styling.visitors.DecoratorsFinderVisitor;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.util.NumberRange;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeConnection;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeFactory;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterUtil;
import org.saig.core.filter.NoneFilter;
import org.saig.core.filter.NullFilter;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.renderer.Renderer;
import org.saig.core.renderer.lite.LabelCache;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.renderer.style.LineStyle2D;
import org.saig.core.renderer.style.MarkStyle2D;
import org.saig.core.renderer.style.PolygonStyle2D;
import org.saig.core.renderer.style.RuleStyle;
import org.saig.core.renderer.style.Style2D;
import org.saig.core.renderer2.AbstractRenderer;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Rule;
import org.saig.jump.util.LayerUtil;

public class ShapeRenderer
extends AbstractRenderer {
    private static final Logger LOGGER = Logger.getLogger(ShapeRenderer.class);
    private ICoordTrans ct;
    private ShapeConnection con;

    public ShapeRenderer(double factor) {
        super(factor);
    }

    @Override
    public void render(ThreadSafeImage image, Layerable layerToRenderer, Viewport viewPort, FeatureCollectionRenderer featureCollectionRenderer, boolean oneQueryByFilter) {
        block18: {
            this.layer = (Layer)layerToRenderer;
            try {
                ShapeFileDataSource ds = (ShapeFileDataSource)((FeatureCollectionOnDemand)this.layer.getUltimateFeatureCollectionWrapper()).getDataAccesor();
                this.layerWidth = this.layer.getUltimateFeatureCollectionWrapper().getEnvelope().getWidth();
                this.fcRenderer = featureCollectionRenderer;
                this.image = image;
                this.viewPort = viewPort;
                if (!this.checkCoordinates(this.layer, viewPort)) {
                    return;
                }
                try {
                    LabelCacheImpl labelCache;
                    block17: {
                        this.viewWidth = viewPort.getEnvelopeInModelCoordinates().getWidth();
                        this.ct = this.layer.getCoordTrans();
                        this.isLineLayer = LayerUtil.isLinealLayer(this.layer);
                        Envelope envelope = viewPort.getEnvelopeInModelCoordinatesForQuery();
                        Rectangle2D.Double view = new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(), envelope.getHeight());
                        Envelope layerEnvelope = this.layer.getUltimateFeatureCollectionWrapper().getEnvelope();
                        if (layerEnvelope.getWidth() == 0.0 || layerEnvelope.getHeight() == 0.0) {
                            layerEnvelope.expandBy(0.05);
                        }
                        Rectangle2D layerView = new Rectangle2D.Double(layerEnvelope.getMinX(), layerEnvelope.getMinY(), layerEnvelope.getWidth() + 0.001, layerEnvelope.getHeight() + 0.001);
                        if (this.layer.getCoordTrans() != null) {
                            layerView = this.ct.convert(layerView);
                        }
                        this.con = ds.getConnection();
                        FeatureTypeStyle estilo = this.layer.getModelStyle().getSelectedFeatureTypeStyle();
                        AffineTransform affineTransform = viewPort.getModelToViewTransform();
                        MathTransform2D transform = (MathTransform2D)Renderer.mathTransformFactory.createAffineTransform((Matrix)new GeneralMatrix(affineTransform));
                        double scaleDenominator = 1.0 / affineTransform.getScaleX();
                        NumberRange scaleRange = new NumberRange(scaleDenominator, scaleDenominator);
                        boolean hasDecorators = this.hasDecorators(this.layer);
                        Object[] rules = this.getRulesInScale(estilo.getRules(), scaleRange, this.factor);
                        List rulesWithFilter = (List)rules[0];
                        labelCache = new LabelCacheImpl(this.layer.isOverlapping(), this.layer.isRepeated());
                        boolean hasFeature = (Boolean)rules[2] != false || hasDecorators || this.layer.isVersionable();
                        boolean loadFeatureGeometry = (Boolean)rules[3] != false || hasDecorators;
                        labelCache.start();
                        labelCache.startLayer(this.layer.getName());
                        RuleStyle elseRule = (RuleStyle)rules[1];
                        boolean contains = view.contains(layerView);
                        try {
                            try {
                                this.con.open();
                                if (contains || ds.hasEditableFeatures() || ds.getSpatialIndex() == null) {
                                    this.paintAll(ds, contains, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, hasFeature, loadFeatureGeometry, view, this.layer.getName());
                                } else {
                                    this.paintAllWithSpatialIndex(ds, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, hasFeature, loadFeatureGeometry, view, this.layer.getName());
                                }
                                List<Feature> editableFeatures = ds.getEditableFeatures();
                                this.paintAll(editableFeatures.iterator(), rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, hasFeature, loadFeatureGeometry, view, this.layer.getName());
                            }
                            catch (Exception e) {
                                LOGGER.error((Object)"", (Throwable)e);
                                this.con.close();
                                break block17;
                            }
                        }
                        catch (Throwable throwable) {
                            this.con.close();
                            throw throwable;
                        }
                        this.con.close();
                    }
                    if (this.fcRenderer.cancelled) {
                        labelCache.stop();
                        break block18;
                    }
                    final Rectangle paintArea = new Rectangle(0, 0, viewPort.getPanel().getWidth(), viewPort.getPanel().getHeight());
                    image.draw(new ThreadSafeImage.Drawer(){

                        @Override
                        public void draw(Graphics2D g) throws Exception {
                            labelCache.endLayer(ShapeRenderer.this.layer.getName(), g, paintArea);
                            labelCache.end(g, paintArea);
                        }
                    });
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
            finally {
                this.dispose();
            }
        }
    }

    private void paintAll(ShapeFileDataSource ds, boolean contains, List<RuleStyle> rulesWithFilter, AffineTransform affineTransform, RuleStyle elseRule, LabelCache labelCache, MathTransform2D transform, NumberRange scaleRange, boolean hasFeature, boolean loadFeatureGeometry, Rectangle2D extent, String layerId) throws Exception {
        double distance = this.getDistancia1Px(affineTransform);
        if (!ds.hasEditableFeatures()) {
            if (ds.getShapeType() == 1 || ds.getShapeType() == 11) {
                int i = 0;
                while (i < ds.iterableRows() && !this.fcRenderer.cancelled) {
                    Rectangle2D bounds = this.con.getShapeBounds(i);
                    if (bounds != null) {
                        if (this.ct != null) {
                            bounds = this.ct.convert(bounds);
                        }
                        if (contains || XRectangle2D.intersectInclusive((Rectangle2D)extent, (Rectangle2D)bounds)) {
                            this.processPoint(ds, i, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, hasFeature, loadFeatureGeometry, layerId);
                        }
                    }
                    ++i;
                }
            } else if (ds.getShapeType() == 8 || ds.getShapeType() == 18) {
                int i = 0;
                while (i < ds.iterableRows() && !this.fcRenderer.cancelled) {
                    Rectangle2D bounds = this.con.getShapeBounds(i);
                    if (bounds != null) {
                        if (this.ct != null) {
                            bounds = this.ct.convert(bounds);
                        }
                        if (contains || XRectangle2D.intersectInclusive((Rectangle2D)extent, (Rectangle2D)bounds)) {
                            ShapeGeometry geom = this.con.getShape(i);
                            SAIGGeneralPath points = geom.getShp().getGeneralPath();
                            int j = 0;
                            while (j < points.numCoords / 2) {
                                int k = j * 2;
                                ShapeGeometry point = ShapeFactory.createPoint2D(points.pointCoords[k], points.pointCoords[k + 1]);
                                this.processPoint(ds, i, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, hasFeature, loadFeatureGeometry, point, layerId);
                                ++j;
                            }
                        }
                    }
                    ++i;
                }
            } else {
                int i = 0;
                while (i < ds.getShapeCount() && !this.fcRenderer.cancelled) {
                    Rectangle2D bounds = this.con.getShapeBounds(i);
                    if (bounds != null) {
                        if (this.ct != null) {
                            bounds = this.ct.convert(bounds);
                        }
                        if (contains || XRectangle2D.intersectInclusive((Rectangle2D)extent, (Rectangle2D)bounds)) {
                            if (bounds.getWidth() > distance || bounds.getHeight() > distance) {
                                this.processShape(ds, i, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, hasFeature, loadFeatureGeometry, layerId);
                            } else {
                                this.process1Px(this.image.getGraphics(), bounds, affineTransform, rulesWithFilter, elseRule, hasFeature, loadFeatureGeometry, i, ds);
                            }
                        }
                    }
                    ++i;
                }
            }
        } else if (ds.getShapeType() == 1 || ds.getShapeType() == 11) {
            int i = 0;
            while (i < ds.iterableRows() && !this.fcRenderer.cancelled) {
                Rectangle2D bounds;
                if (i < ds.getShapeCount() && !ds.isUpdatedOrDeleted(i) && (bounds = this.con.getShapeBounds(i)) != null) {
                    if (this.ct != null) {
                        bounds = this.ct.convert(bounds);
                    }
                    if (contains || XRectangle2D.intersectInclusive((Rectangle2D)extent, (Rectangle2D)bounds)) {
                        this.processPoint(ds, i, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, hasFeature, loadFeatureGeometry, layerId);
                    }
                }
                ++i;
            }
        } else if (ds.getShapeType() == 8 || ds.getShapeType() == 18) {
            int i = 0;
            while (i < ds.iterableRows() && !this.fcRenderer.cancelled) {
                Rectangle2D bounds;
                if (i < ds.getShapeCount() && !ds.isUpdatedOrDeleted(i) && (bounds = this.con.getShapeBounds(i)) != null) {
                    if (this.ct != null) {
                        bounds = this.ct.convert(bounds);
                    }
                    if (contains || XRectangle2D.intersectInclusive((Rectangle2D)extent, (Rectangle2D)bounds)) {
                        ShapeGeometry geom = this.con.getShape(i);
                        SAIGGeneralPath points = geom.getShp().getGeneralPath();
                        int j = 0;
                        while (j < points.numCoords / 2) {
                            int k = j * 2;
                            ShapeGeometry point = ShapeFactory.createPoint2D(points.pointCoords[k], points.pointCoords[k + 1]);
                            this.processPoint(ds, i, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, hasFeature, loadFeatureGeometry, point, layerId);
                            ++j;
                        }
                    }
                }
                ++i;
            }
        } else {
            int i = 0;
            while (i < ds.iterableRows() && !this.fcRenderer.cancelled) {
                Rectangle2D bounds;
                if (i < ds.getShapeCount() && !ds.isUpdatedOrDeleted(i) && (bounds = this.con.getShapeBounds(i)) != null) {
                    if (this.ct != null) {
                        bounds = this.ct.convert(bounds);
                    }
                    if (contains || XRectangle2D.intersectInclusive((Rectangle2D)extent, (Rectangle2D)bounds)) {
                        if (bounds.getWidth() > distance || bounds.getHeight() > distance) {
                            this.processShape(ds, i, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, hasFeature, loadFeatureGeometry, layerId);
                        } else {
                            this.process1Px(this.image.getGraphics(), bounds, affineTransform, rulesWithFilter, elseRule, hasFeature, loadFeatureGeometry, i, ds);
                        }
                    }
                }
                ++i;
            }
        }
    }

    private void paintAllWithSpatialIndex(ShapeFileDataSource ds, List<RuleStyle> rulesWithFilter, AffineTransform affineTransform, RuleStyle elseRule, LabelCache labelCache, MathTransform2D transform, NumberRange scaleRange, boolean hasFeature, boolean loadFeatureGeometry, Rectangle2D extent, String layerId) throws Exception {
        double distance = this.getDistancia1Px(affineTransform);
        Rectangle2D queryExtent = extent;
        if (this.ct != null) {
            queryExtent = this.ct.getInverted().convert(extent);
        }
        List candidatos = ds.getSpatialIndex().query(queryExtent);
        if (ds.getShapeType() == 1 || ds.getShapeType() == 11) {
            Iterator iter = candidatos.iterator();
            while (iter.hasNext() && !this.fcRenderer.cancelled) {
                int index = ((Number)iter.next()).intValue();
                Rectangle2D bounds = this.con.getShapeBounds(index);
                if (bounds == null) continue;
                if (this.ct != null) {
                    bounds = this.ct.convert(bounds);
                }
                if (!XRectangle2D.intersectInclusive((Rectangle2D)extent, (Rectangle2D)bounds)) continue;
                this.processPoint(ds, index, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, hasFeature, loadFeatureGeometry, layerId);
            }
        } else if (ds.getShapeType() == 8 || ds.getShapeType() == 18) {
            Iterator iter = candidatos.iterator();
            while (iter.hasNext() && !this.fcRenderer.cancelled) {
                int index = ((Number)iter.next()).intValue();
                Rectangle2D bounds = this.con.getShapeBounds(index);
                if (bounds == null) continue;
                if (this.ct != null) {
                    bounds = this.ct.convert(bounds);
                }
                if (!XRectangle2D.intersectInclusive((Rectangle2D)extent, (Rectangle2D)bounds)) continue;
                ShapeGeometry geom = this.con.getShape(index);
                SAIGGeneralPath points = geom.getShp().getGeneralPath();
                int j = 0;
                while (j < points.numCoords / 2) {
                    int k = j * 2;
                    ShapeGeometry point = ShapeFactory.createPoint2D(points.pointCoords[k], points.pointCoords[k + 1]);
                    this.processPoint(ds, index, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, hasFeature, loadFeatureGeometry, point, layerId);
                    ++j;
                }
            }
        } else {
            Iterator iter = candidatos.iterator();
            while (iter.hasNext() && !this.fcRenderer.cancelled) {
                int index = ((Number)iter.next()).intValue();
                Rectangle2D bounds = this.con.getShapeBounds(index);
                if (bounds == null) continue;
                if (this.ct != null) {
                    bounds = this.ct.convert(bounds);
                }
                if (!XRectangle2D.intersectInclusive((Rectangle2D)extent, (Rectangle2D)bounds)) continue;
                if (bounds.getWidth() > distance || bounds.getHeight() > distance) {
                    this.processShape(ds, index, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, hasFeature, loadFeatureGeometry, layerId);
                    continue;
                }
                this.process1Px(this.image.getGraphics(), bounds, affineTransform, rulesWithFilter, elseRule, hasFeature, loadFeatureGeometry, index, ds);
            }
        }
    }

    /*
     * Unable to fully structure code
     */
    private void paintAll(Iterator<Feature> it, List<RuleStyle> rulesWithFilter, AffineTransform affineTransform, RuleStyle elseRule, LabelCache labelCache, MathTransform2D transform, NumberRange scaleRange, boolean hasFeature, boolean loadFeatureGeometry, Rectangle2D extent, String layerId) throws Exception {
        block6: {
            block5: {
                distance = this.getDistancia1Px(affineTransform);
                geomType = this.layer.getUltimateFeatureCollectionWrapper().getFeatureSchema().getGeometryType();
                if (geomType != 1) break block5;
                while (!this.fcRenderer.cancelled && it.hasNext()) {
                    feat = it.next();
                    if (feat.getGeometry().isEmpty()) continue;
                    geom = ShapeGeometryConverter.jts_to_igeometry(feat.getGeometry());
                    this.processPoint(layerId, feat, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, geom);
                }
                break block6;
            }
            if (geomType != 8) ** GOTO lbl35
            while (!this.fcRenderer.cancelled && it.hasNext()) {
                feat = it.next();
                if (feat.getGeometry().isEmpty()) continue;
                geom = ShapeGeometryConverter.jts_to_igeometry(feat.getGeometry());
                points = geom.getShp().getGeneralPath();
                j = 0;
                while (j < points.numCoords / 2) {
                    k = j * 2;
                    point = ShapeFactory.createPoint2D(points.pointCoords[k], points.pointCoords[k + 1]);
                    this.processPoint(layerId, feat, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, point);
                    ++j;
                }
            }
            break block6;
lbl-1000:
            // 1 sources

            {
                feat = it.next();
                if (feat.getGeometry().isEmpty()) continue;
                geom = ShapeGeometryConverter.jts_to_igeometry(feat.getGeometry());
                bounds = geom.getBounds2D();
                if (bounds.getWidth() > distance || bounds.getHeight() > distance) {
                    this.processShape(layerId, feat, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, geom);
                    continue;
                }
                this.process1Px(this.image.getGraphics(), bounds, affineTransform, rulesWithFilter, elseRule, feat);
lbl35:
                // 4 sources

                ** while (!this.fcRenderer.cancelled && it.hasNext())
            }
        }
    }

    private void processPoint(final String layerId, final Feature feat, List<RuleStyle> rulesWithFilter, AffineTransform affineTransform, final RuleStyle elseRule, final LabelCache labelCache, final MathTransform2D transform, final NumberRange scaleRange, IShapeGeometry geom) throws Exception {
        if (this.ct != null) {
            geom = geom.cloneGeometry();
            geom.reProject(this.ct);
        }
        final IShapeGeometry geom2 = geom;
        boolean check = false;
        if (elseRule != null && elseRule.getFilter().contains(feat)) {
            this.image.draw(new ThreadSafeImage.Drawer(){

                @Override
                public void draw(Graphics2D g) throws Exception {
                    LiteShape2 shape = ShapeRenderer.this.drawShape(g, geom2.cloneGeometry(), elseRule, labelCache, transform, scaleRange, feat, ShapeRenderer.this.ct, ShapeRenderer.this.isLineLayer, ShapeRenderer.this.viewPort);
                    ShapeRenderer.this.processTextSymbolizer(layerId, elseRule, shape, labelCache, feat, ShapeRenderer.this.layerWidth, ShapeRenderer.this.viewWidth, scaleRange, transform, ShapeRenderer.this.ct);
                }
            });
            check = true;
        }
        Iterator<RuleStyle> iter = rulesWithFilter.iterator();
        while (iter.hasNext() && !check) {
            final RuleStyle ruleStyle = iter.next();
            Filter filter = ruleStyle.getFilter();
            if (filter != null && !filter.contains(feat)) continue;
            this.image.draw(new ThreadSafeImage.Drawer(){

                @Override
                public void draw(Graphics2D g) throws Exception {
                    LiteShape2 shape = ShapeRenderer.this.drawShape(g, geom2.cloneGeometry(), ruleStyle, labelCache, transform, scaleRange, feat, ShapeRenderer.this.ct, ShapeRenderer.this.isLineLayer, ShapeRenderer.this.viewPort);
                    ShapeRenderer.this.processTextSymbolizer(layerId, ruleStyle, shape, labelCache, feat, ShapeRenderer.this.layerWidth, ShapeRenderer.this.viewWidth, scaleRange, transform, ShapeRenderer.this.ct);
                }
            });
        }
    }

    private void processPoint(ShapeFileDataSource ds, int index, List<RuleStyle> rulesWithFilter, AffineTransform affineTransform, RuleStyle elseRule, LabelCache labelCache, MathTransform2D transform, NumberRange scaleRange, boolean hasFeature, boolean loadFeature, String layerId) throws Exception {
        this.processPoint(ds, index, rulesWithFilter, affineTransform, elseRule, labelCache, transform, scaleRange, hasFeature, loadFeature, null, layerId);
    }

    private void processPoint(ShapeFileDataSource ds, int index, List<RuleStyle> rulesWithFilter, AffineTransform affineTransform, final RuleStyle elseRule, final LabelCache labelCache, final MathTransform2D transform, final NumberRange scaleRange, boolean hasFeature, boolean loadFeature, ShapeGeometry geom, final String layerId) throws Exception {
        if (geom == null) {
            geom = this.con.getShape(index);
        }
        Feature feature = null;
        if (hasFeature) {
            feature = this.con.readFeature(index, geom, loadFeature, this.layer.getFeatureSchema());
        }
        final Feature feat2 = feature;
        if (this.ct != null) {
            geom = (ShapeGeometry)geom.cloneGeometry();
            geom.reProject(this.ct);
        }
        final ShapeGeometry geom2 = geom;
        boolean check = false;
        if (elseRule != null && elseRule.getFilter().contains(feature)) {
            this.image.draw(new ThreadSafeImage.Drawer(){

                @Override
                public void draw(Graphics2D g) throws Exception {
                    LiteShape2 shape = ShapeRenderer.this.drawShape(g, geom2.cloneGeometry(), elseRule, labelCache, transform, scaleRange, feat2, ShapeRenderer.this.ct, ShapeRenderer.this.isLineLayer, ShapeRenderer.this.viewPort);
                    ShapeRenderer.this.processTextSymbolizer(layerId, elseRule, shape, labelCache, feat2, ShapeRenderer.this.layerWidth, ShapeRenderer.this.viewWidth, scaleRange, transform, ShapeRenderer.this.ct);
                }
            });
            check = true;
        }
        Iterator<RuleStyle> iter = rulesWithFilter.iterator();
        while (iter.hasNext() && !check) {
            final RuleStyle ruleStyle = iter.next();
            Filter filter = ruleStyle.getFilter();
            if (filter != null && !filter.contains(feature)) continue;
            this.image.draw(new ThreadSafeImage.Drawer(){

                @Override
                public void draw(Graphics2D g) throws Exception {
                    LiteShape2 shape = ShapeRenderer.this.drawShape(g, geom2.cloneGeometry(), ruleStyle, labelCache, transform, scaleRange, feat2, ShapeRenderer.this.ct, ShapeRenderer.this.isLineLayer, ShapeRenderer.this.viewPort);
                    ShapeRenderer.this.processTextSymbolizer(layerId, ruleStyle, shape, labelCache, feat2, ShapeRenderer.this.layerWidth, ShapeRenderer.this.viewWidth, scaleRange, transform, ShapeRenderer.this.ct);
                }
            });
        }
    }

    private void processShape(ShapeFileDataSource ds, int index, List<RuleStyle> rulesWithFilter, AffineTransform affineTransform, final RuleStyle elseRule, final LabelCache labelCache, final MathTransform2D transform, final NumberRange scaleRange, boolean hasFeature, boolean loadFeature, final String layerId) throws Exception {
        boolean check = false;
        ShapeGeometry geom = this.con.getShape(index);
        Feature feature = null;
        if (hasFeature) {
            feature = this.con.readFeature(index, geom, loadFeature, this.layer.getFeatureSchema());
        }
        final Feature feat = feature;
        if (this.ct != null) {
            geom = (ShapeGeometry)geom.cloneGeometry();
            geom.reProject(this.ct);
        }
        final ShapeGeometry geom2 = geom;
        if (elseRule != null && elseRule.getFilter().contains(feature)) {
            this.image.draw(new ThreadSafeImage.Drawer(){

                @Override
                public void draw(Graphics2D g) throws Exception {
                    LiteShape2 shape = ShapeRenderer.this.drawShape(g, geom2.cloneGeometry(), elseRule, labelCache, transform, scaleRange, feat, ShapeRenderer.this.ct, ShapeRenderer.this.isLineLayer, ShapeRenderer.this.viewPort);
                    ShapeRenderer.this.processTextSymbolizer(layerId, elseRule, shape, labelCache, feat, ShapeRenderer.this.layerWidth, ShapeRenderer.this.viewWidth, scaleRange, transform, ShapeRenderer.this.ct);
                }
            });
            check = true;
        }
        Iterator<RuleStyle> iter = rulesWithFilter.iterator();
        while (iter.hasNext() && !check) {
            final RuleStyle ruleStyle = iter.next();
            Filter filter = ruleStyle.getFilter();
            if (filter != null && !filter.contains(feat)) continue;
            this.image.draw(new ThreadSafeImage.Drawer(){

                @Override
                public void draw(Graphics2D g) throws Exception {
                    LiteShape2 shape = ShapeRenderer.this.drawShape(g, geom2.cloneGeometry(), ruleStyle, labelCache, transform, scaleRange, feat, ShapeRenderer.this.ct, ShapeRenderer.this.isLineLayer, ShapeRenderer.this.viewPort);
                    ShapeRenderer.this.processTextSymbolizer(layerId, ruleStyle, shape, labelCache, feat, ShapeRenderer.this.layerWidth, ShapeRenderer.this.viewWidth, scaleRange, transform, ShapeRenderer.this.ct);
                }
            });
        }
    }

    private void processShape(final String layerId, final Feature feat, List<RuleStyle> rulesWithFilter, AffineTransform affineTransform, final RuleStyle elseRule, final LabelCache labelCache, final MathTransform2D transform, final NumberRange scaleRange, IShapeGeometry geom) throws Exception {
        boolean check = false;
        if (this.ct != null) {
            geom = geom.cloneGeometry();
            geom.reProject(this.ct);
        }
        final IShapeGeometry geom2 = geom;
        if (elseRule != null && elseRule.getFilter().contains(feat)) {
            this.image.draw(new ThreadSafeImage.Drawer(){

                @Override
                public void draw(Graphics2D g) throws Exception {
                    LiteShape2 shape = ShapeRenderer.this.drawShape(g, geom2.cloneGeometry(), elseRule, labelCache, transform, scaleRange, feat, ShapeRenderer.this.ct, ShapeRenderer.this.isLineLayer, ShapeRenderer.this.viewPort);
                    ShapeRenderer.this.processTextSymbolizer(layerId, elseRule, shape, labelCache, feat, ShapeRenderer.this.layerWidth, ShapeRenderer.this.viewWidth, scaleRange, transform, ShapeRenderer.this.ct);
                }
            });
            check = true;
        }
        Iterator<RuleStyle> iter = rulesWithFilter.iterator();
        while (iter.hasNext() && !check) {
            final RuleStyle ruleStyle = iter.next();
            Filter filter = ruleStyle.getFilter();
            if ((filter != null || elseRule != null) && (filter == null || !filter.contains(feat))) continue;
            this.image.draw(new ThreadSafeImage.Drawer(){

                @Override
                public void draw(Graphics2D g) throws Exception {
                    LiteShape2 shape = ShapeRenderer.this.drawShape(g, geom2.cloneGeometry(), ruleStyle, labelCache, transform, scaleRange, feat, ShapeRenderer.this.ct, ShapeRenderer.this.isLineLayer, ShapeRenderer.this.viewPort);
                    ShapeRenderer.this.processTextSymbolizer(layerId, ruleStyle, shape, labelCache, feat, ShapeRenderer.this.layerWidth, ShapeRenderer.this.viewWidth, scaleRange, transform, ShapeRenderer.this.ct);
                }
            });
        }
    }

    private Object[] getRulesInScale(Rule[] rules, NumberRange scaleRange, double factor) {
        RuleStyle elseRule = null;
        ArrayList<RuleStyle> rulesInScale = new ArrayList<RuleStyle>();
        boolean hasfeature = false;
        boolean loadFeature = false;
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
                if ((rule.getMinScaleDenominator() <= this.viewPort.getPanel().getScale() || Double.isNaN(rule.getMinScaleDenominator())) && (rule.getMaxScaleDenominator() >= this.viewPort.getPanel().getScale() || Double.isNaN(rule.getMaxScaleDenominator()))) {
                    RuleStyle ruleStyle = new RuleStyle(rule, scaleRange, false, factor, this.viewPort.getPixelSize(), this.viewPort.getPanel().getUserLengthUnit(), this.layer.getFeatureSchema());
                    if (rule.isElseFilter()) {
                        elseRule = ruleStyle;
                    } else {
                        rulesInScale.add(ruleStyle);
                    }
                    if (ruleStyle.hasTextSymbolizers() || ruleStyle.hasPointSymbolizers() || ruleStyle.hasFunctionExpressionsWithGeometry()) {
                        hasfeature = true;
                        loadFeature = true;
                    }
                    if (ruleStyle.getFilter() != null) {
                        hasfeature = true;
                        if (ruleStyle.isGeometryFilter()) {
                            loadFeature = true;
                        }
                    }
                }
            }
            ++i;
        }
        if (elseRule != null) {
            FeatureSchema schema = this.layer.getUltimateFeatureCollectionWrapper().getFeatureSchema();
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
        return new Object[]{rulesInScale, elseRule, new Boolean(hasfeature), new Boolean(loadFeature)};
    }

    private void process1Px(Graphics2D g2, Rectangle2D bounds, AffineTransform aft, List<RuleStyle> rulesWithFilter, RuleStyle elseRule, boolean hasFeature, boolean loadOnlyGeometry, int index, ShapeFileDataSource ds) throws IOException {
        boolean check = false;
        hasFeature = hasFeature || elseRule != null;
        loadOnlyGeometry = loadOnlyGeometry && elseRule == null;
        Feature feature = null;
        if (hasFeature) {
            ShapeGeometry geom = null;
            if (loadOnlyGeometry) {
                geom = this.con.getShape(index);
            }
            try {
                feature = this.con.readFeature(index, geom, loadOnlyGeometry, this.layer.getFeatureSchema());
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        if (elseRule != null && elseRule.getFilter().contains(feature)) {
            this.paint1Px(elseRule, bounds, aft);
            check = true;
        }
        Iterator<RuleStyle> iter = rulesWithFilter.iterator();
        while (iter.hasNext() && !check) {
            RuleStyle ruleStyle = iter.next();
            Filter filter = ruleStyle.getFilter();
            if (filter == null) {
                this.paint1Px(ruleStyle, bounds, aft);
                continue;
            }
            if (!filter.contains(feature)) continue;
            this.paint1Px(ruleStyle, bounds, aft);
        }
    }

    private void process1Px(Graphics2D g2, Rectangle2D bounds, AffineTransform aft, List<RuleStyle> rulesWithFilter, RuleStyle elseRule, Feature feature) throws IOException {
        boolean check = false;
        if (elseRule != null && elseRule.getFilter().contains(feature)) {
            this.paint1Px(elseRule, bounds, aft);
            check = true;
        }
        Iterator<RuleStyle> iter = rulesWithFilter.iterator();
        while (iter.hasNext() && !check) {
            RuleStyle ruleStyle = iter.next();
            Filter filter = ruleStyle.getFilter();
            if (filter != null && !filter.contains(feature)) continue;
            this.paint1Px(ruleStyle, bounds, aft);
        }
    }

    private void paint1Px(RuleStyle ruleStyle, Rectangle2D bounds, AffineTransform aft) {
        for (Style2D style : ruleStyle.getStyles()) {
            LineStyle2D ls2d;
            Paint paint;
            if (style instanceof MarkStyle2D) {
                MarkStyle2D ms2d = (MarkStyle2D)style;
                Color color = (Color)ms2d.getFill();
                this.draw1px(bounds, aft, color.getRGB());
                continue;
            }
            if (style instanceof PolygonStyle2D) {
                PolygonStyle2D polygonStyle2D = (PolygonStyle2D)style;
                if (polygonStyle2D.getFill() != null) {
                    paint = polygonStyle2D.getFill();
                    if (paint instanceof TexturePaint || paint instanceof MultipleGradientPaint) continue;
                    this.draw1px(bounds, aft, ((Color)paint).getRGB());
                    continue;
                }
                LineStyle2D ls2d2 = (LineStyle2D)style;
                if (ls2d2.getStroke() == null) continue;
                Paint paint2 = ls2d2.getContour();
                if (paint2 instanceof TexturePaint || paint2 instanceof MultipleGradientPaint || ls2d2.getGraphicStroke() != null) {
                    this.draw1px(bounds, aft, Color.darkGray.getRGB());
                    continue;
                }
                this.draw1px(bounds, aft, ((Color)paint2).getRGB());
                continue;
            }
            if (!(style instanceof LineStyle2D) || (ls2d = (LineStyle2D)style).getStroke() == null) continue;
            paint = ls2d.getContour();
            if (paint instanceof TexturePaint || paint instanceof MultipleGradientPaint || ls2d.getGraphicStroke() != null) {
                this.draw1px(bounds, aft, Color.darkGray.getRGB());
                continue;
            }
            this.draw1px(bounds, aft, ((Color)paint).getRGB());
        }
    }

    private void draw1px(Rectangle2D bounds, AffineTransform aft, int rgb) {
        Point2D.Double pOrig = new Point2D.Double(bounds.getMinX(), bounds.getMinY());
        Point2D pDest = aft.transform(pOrig, null);
        Point2D pDest2 = this.image.getGraphics().getTransform().transform(pDest, null);
        int pixX = (int)pDest2.getX();
        int pixY = (int)pDest2.getY();
        BufferedImage imag2 = (BufferedImage)this.image.getImage();
        if (pixX > 0 && pixX < imag2.getWidth() && pixY > 0 && pixY < imag2.getHeight()) {
            imag2.setRGB(pixX, pixY, rgb);
        }
    }

    @Override
    public void dispose() {
        this.fcRenderer = null;
        this.image = null;
        this.viewPort = null;
        this.ct = null;
        this.layer = null;
    }

    private boolean hasDecorators(Layer layer) {
        DecoratorsFinderVisitor visitor = new DecoratorsFinderVisitor(true);
        visitor.visit(layer.getModelStyle());
        return visitor.hasDecorators();
    }
}

