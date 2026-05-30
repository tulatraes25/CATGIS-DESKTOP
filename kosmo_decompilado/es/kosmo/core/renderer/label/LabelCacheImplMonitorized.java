/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  javax.media.jai.util.Range
 */
package es.kosmo.core.renderer.label;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import es.kosmo.core.renderer.label.LabelCacheImpl;
import es.kosmo.core.renderer.label.LabelCacheItemWrapper;
import es.kosmo.core.renderer.label.LabelIndex;
import es.kosmo.core.renderer.label.LabelPainter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.media.jai.util.Range;
import org.geotools.geometry.jts.GeometryClipper;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.renderer.lite.LabelCacheItem;
import org.saig.core.renderer.lite.LabelDrawingResult;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.styling.TextSymbolizer;

public class LabelCacheImplMonitorized
extends LabelCacheImpl {
    protected Map<LabelCacheItemWrapper, Feature> item2feature;
    protected Map<LabelCacheItemWrapper, LabelDrawingResult> drawingResult;
    protected List<Rectangle> glyphs = new ArrayList<Rectangle>();

    public LabelCacheImplMonitorized(boolean overlapping, boolean repeated) {
        super(overlapping, repeated);
        this.item2feature = new HashMap<LabelCacheItemWrapper, Feature>();
        this.drawingResult = new HashMap<LabelCacheItemWrapper, LabelDrawingResult>();
    }

    @Override
    public void put(String layerId, TextSymbolizer symbolizer, Feature feature, LiteShape2 shape, LiteShape2 originalShape, Range scaleRange, Number textHeight, Color color, Number rotation, double factor) {
        this.needsOrdering = true;
        try {
            String label = this.getLabel(symbolizer, feature);
            if (label == null || label.length() == 0) {
                return;
            }
            double priorityValue = this.getPriority(symbolizer, feature);
            if (this.repeated) {
                LabelCacheItem item = this.buildLabelCacheItem(layerId, symbolizer, feature, shape, originalShape, scaleRange, textHeight, color, rotation, factor, label, priorityValue);
                if (item == null) {
                    return;
                }
                this.labelCacheNonGrouped.add(item);
                this.item2feature.put(new LabelCacheItemWrapper(item), feature);
            } else {
                LabelCacheItem lci = (LabelCacheItem)this.labelCache.get(label);
                if (lci == null) {
                    lci = this.buildLabelCacheItem(layerId, symbolizer, feature, shape, originalShape, scaleRange, textHeight, color, rotation, factor, label, priorityValue);
                    if (lci == null) {
                        return;
                    }
                    this.labelCache.put(label, lci);
                    this.item2feature.put(new LabelCacheItemWrapper(lci), feature);
                } else {
                    if (symbolizer.getPriority() != null && !(symbolizer.getPriority() instanceof LiteralExpression)) {
                        lci.setPriority(lci.getPriority() + priorityValue);
                    }
                    lci.getGeoms().add(shape.getGeometry());
                }
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    @Override
    protected void paintLabels(Graphics2D graphics, Rectangle displayArea) {
        if (!this.activeLayers.isEmpty()) {
            throw new IllegalStateException(this.activeLayers + " are layers that started rendering but have not completed," + " stop() or endLayer() must be called before end() is called");
        }
        LabelIndex glyphs = new LabelIndex();
        glyphs.reserveArea(this.reserved);
        displayArea = new Rectangle(displayArea);
        --displayArea.width;
        --displayArea.height;
        this.clipper = new GeometryClipper(new Envelope(displayArea.getMinX(), displayArea.getMaxX(), displayArea.getMinY(), displayArea.getMaxY()));
        List<LabelCacheItem> items = this.needsOrdering ? this.orderedLabels() : this.getActiveLabels();
        LabelPainter painter = new LabelPainter(graphics, this.labelRenderingMode);
        for (LabelCacheItem labelItem : items) {
            if (this.stop) {
                return;
            }
            painter.setLabel(labelItem);
            try {
                AffineTransform tempTransform = new AffineTransform();
                Geometry geom = labelItem.getGeometry();
                LabelDrawingResult result = null;
                if (geom instanceof Point || geom instanceof MultiPoint) {
                    result = this.paintPointLabel(painter, tempTransform, displayArea, glyphs);
                } else if (geom instanceof LineString && !(geom instanceof LinearRing) || geom instanceof MultiLineString) {
                    result = this.paintLineLabel(painter, tempTransform, displayArea, glyphs);
                } else if (geom instanceof Polygon || geom instanceof MultiPolygon || geom instanceof LinearRing) {
                    result = this.paintPolygonLabel(painter, tempTransform, displayArea, glyphs);
                }
                this.drawingResult.put(new LabelCacheItemWrapper(labelItem), result);
            }
            catch (Exception e) {
                System.out.println("Issues painting " + labelItem.getLabel());
                e.printStackTrace();
            }
        }
    }

    public List<Feature> getOverlappedFeatures() {
        ArrayList<Feature> overlapedFeats = new ArrayList<Feature>();
        Set<LabelCacheItemWrapper> keys = this.drawingResult.keySet();
        for (LabelCacheItemWrapper key : keys) {
            Feature feat;
            if (this.drawingResult.get(key).getType() != LabelDrawingResult.LabelDrawingResultType.OVERLAPPED || (feat = this.item2feature.get(key)) == null) continue;
            overlapedFeats.add(feat);
        }
        return overlapedFeats;
    }

    public List<Feature> getSuccessFeatures() {
        ArrayList<Feature> successFeats = new ArrayList<Feature>();
        Set<LabelCacheItemWrapper> keys = this.drawingResult.keySet();
        for (LabelCacheItemWrapper key : keys) {
            Feature feat;
            if (this.drawingResult.get(key).getType() != LabelDrawingResult.LabelDrawingResultType.SUCCEDED || (feat = this.item2feature.get(key)) == null) continue;
            successFeats.add(feat);
        }
        return successFeats;
    }

    public List<LabelCacheItemWrapper> getSuccessLabels() {
        ArrayList<LabelCacheItemWrapper> successLabels = new ArrayList<LabelCacheItemWrapper>();
        Set<LabelCacheItemWrapper> keys = this.drawingResult.keySet();
        for (LabelCacheItemWrapper key : keys) {
            if (this.drawingResult.get(key).getType() != LabelDrawingResult.LabelDrawingResultType.SUCCEDED) continue;
            successLabels.add(key);
        }
        return successLabels;
    }

    public void clearGlyphs() {
        this.glyphs.clear();
    }

    @Override
    public void stop() {
        this.stop = true;
        this.activeLayers.clear();
        this.labelCacheNonGrouped.clear();
        this.labelCache.clear();
    }

    public void clearReport() {
        this.item2feature.clear();
        this.drawingResult.clear();
    }

    public Feature getFeature(LabelCacheItemWrapper label) {
        return this.item2feature.get(label);
    }

    public LabelDrawingResult getDrawingResult(LabelCacheItemWrapper label) {
        return this.drawingResult.get(label);
    }
}

