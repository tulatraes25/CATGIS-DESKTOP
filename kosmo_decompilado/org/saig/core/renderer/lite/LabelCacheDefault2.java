/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.precision.EnhancedPrecisionOp
 *  javax.media.jai.util.Range
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;
import com.vividsolutions.jump.feature.Feature;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.media.jai.util.Range;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.renderer.lite.Decimator;
import org.saig.core.renderer.lite.LabelCacheDefault;
import org.saig.core.renderer.lite.LabelCacheItem;
import org.saig.core.renderer.lite.LabelDrawingResult;
import org.saig.core.renderer.lite.LineToLabel;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.renderer.lite.StyledShapePainter;
import org.saig.core.renderer.style.TextStyle2D;
import org.saig.core.styling.TextSymbolizer;
import org.saig.core.util.DateFormatManager;
import org.saig.core.util.NumberFormatManager;
import org.saig.jump.lang.I18N;

@Deprecated
public class LabelCacheDefault2
extends LabelCacheDefault {
    public double MIN_GOODNESS_FIT = 0.7;
    public double DEFAULT_PRIORITY = 1000.0;
    public boolean DEFAULT_GROUP = false;
    protected boolean outlineRenderingEnabled = false;
    protected Set<String> enabledLayers = new HashSet<String>();
    protected Set<String> activeLayers = new HashSet<String>();
    protected LabelCacheDefault.LineLengthComparator lineLengthComparator = new LabelCacheDefault.LineLengthComparator();
    protected boolean needsOrdering = false;

    public LabelCacheDefault2(boolean overlapping, boolean repeated) {
        super(overlapping, repeated);
    }

    @Override
    public void start() {
        this.stop = false;
    }

    @Override
    public void stop() {
        this.stop = true;
        this.activeLayers.clear();
    }

    public void clear() {
        if (!this.activeLayers.isEmpty()) {
            throw new IllegalStateException(I18N.getMessage(this.getClass(), "{0}-are-layers-that-started-rendering-but-have-not-completed-stop-or-enlayer-must-be-called-before-clear-is-called,", new Object[]{this.activeLayers}));
        }
        this.needsOrdering = true;
        this.labelCache.clear();
        this.labelCacheNonGrouped.clear();
        this.enabledLayers.clear();
    }

    public void clear(String layerId) {
        LabelCacheItem item;
        if (this.activeLayers.contains(layerId)) {
            throw new IllegalStateException(I18N.getMessage(this.getClass(), "{0}-is-still-rendering-end-the-layer-before-calling-clear", new Object[]{layerId}));
        }
        this.needsOrdering = true;
        Iterator<Object> iter = this.labelCache.values().iterator();
        while (iter.hasNext()) {
            item = (LabelCacheItem)iter.next();
            if (!item.getLayerIds().contains(layerId)) continue;
            iter.remove();
        }
        iter = this.labelCacheNonGrouped.iterator();
        while (iter.hasNext()) {
            item = (LabelCacheItem)iter.next();
            if (!item.getLayerIds().contains(layerId)) continue;
            iter.remove();
        }
        this.enabledLayers.remove(layerId);
    }

    public void disableLayer(String layerId) {
        this.needsOrdering = true;
        this.enabledLayers.remove(layerId);
    }

    @Override
    public void startLayer(String layerId) {
        this.enabledLayers.add(layerId);
        this.activeLayers.add(layerId);
    }

    public double getPriority(TextSymbolizer symbolizer, Feature feature) {
        if (symbolizer.getPriority() == null) {
            return this.DEFAULT_PRIORITY;
        }
        try {
            Double number = (Double)symbolizer.getPriority().getValue(feature);
            return number;
        }
        catch (Exception e) {
            return this.DEFAULT_PRIORITY;
        }
    }

    @Override
    public void put(String layerId, TextSymbolizer symbolizer, Feature feature, LiteShape2 shape, LiteShape2 originalShape, Range scaleRange, Number textHeight, Color color, Number rotation) {
        this.put(layerId, symbolizer, feature, shape, originalShape, scaleRange, textHeight, color, rotation, 1.0);
    }

    protected String getLabel(TextSymbolizer symbolizer, Feature feature) {
        Object labelValue = symbolizer.resolveLabel(feature);
        if (labelValue == null) {
            return null;
        }
        String label = null;
        label = labelValue instanceof Number ? NumberFormatManager.getFormattedValue((Number)labelValue) : (labelValue instanceof Date ? DateFormatManager.getDateTimeFormat().format((Date)labelValue) : labelValue.toString());
        label = label.trim();
        return label;
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
            } else {
                LabelCacheItem lci = (LabelCacheItem)this.labelCache.get(label);
                if (lci == null) {
                    lci = this.buildLabelCacheItem(layerId, symbolizer, feature, shape, originalShape, scaleRange, textHeight, color, rotation, factor, label, priorityValue);
                    if (lci == null) {
                        return;
                    }
                    this.labelCache.put(label, lci);
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
    public void endLayer(String layerId, Graphics2D graphics, Rectangle displayArea) {
        this.activeLayers.remove(layerId);
    }

    public List<LabelCacheItem> orderedLabels() {
        List<LabelCacheItem> al = this.getActiveLabels();
        Collections.sort(al);
        Collections.reverse(al);
        return al;
    }

    protected List<LabelCacheItem> getActiveLabels() {
        Collection c = this.labelCache.values();
        ArrayList<LabelCacheItem> al = new ArrayList<LabelCacheItem>();
        for (LabelCacheItem item : c) {
            if (!this.isActive(item.getLayerIds())) continue;
            al.add(item);
        }
        for (LabelCacheItem item : this.labelCacheNonGrouped) {
            if (!this.isActive(item.getLayerIds())) continue;
            al.add(item);
        }
        return al;
    }

    private boolean isActive(Set<String> layerIds) {
        for (String currentID : layerIds) {
            if (!this.enabledLayers.contains(currentID)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void end(Graphics2D graphics, Rectangle displayArea) {
        this.paintLabels(graphics, displayArea);
    }

    public LabelDrawingResult paintLabel(LabelCacheItem labelItem, Graphics2D graphics, Rectangle displayArea, List<Rectangle> glyphs, Geometry displayGeom) {
        boolean drawGlowing = false;
        try {
            labelItem.getTextStyle().setLabel(labelItem.getLabel());
            GlyphVector glyphVector = labelItem.getTextStyle().getTextGlyphVector(graphics);
            Geometry geom = labelItem.getGeometry();
            Geometry originalGeom = labelItem.getOriginalShape();
            if (originalGeom != null && !originalGeom.isValid()) {
                originalGeom = new GeometryFactory().createPoint(originalGeom.getCoordinate());
            }
            AffineTransform oldTransform = graphics.getTransform();
            AffineTransform tempTransform = new AffineTransform();
            Geometry representativeGeometry = null;
            if (geom instanceof Point || geom instanceof MultiPoint) {
                representativeGeometry = this.paintPointLabel(glyphVector, labelItem, tempTransform, displayGeom);
            } else if (geom instanceof LineString && !(geom instanceof LinearRing) || geom instanceof MultiLineString) {
                representativeGeometry = this.paintLineLabel(glyphVector, labelItem, tempTransform, displayGeom);
            } else if (geom instanceof Polygon || geom instanceof MultiPolygon || geom instanceof LinearRing) {
                representativeGeometry = this.paintPolygonLabel(glyphVector, labelItem, tempTransform, displayGeom);
            }
            Rectangle glyphBounds = glyphVector.getPixelBounds(null, 0.0f, 0.0f);
            glyphBounds = tempTransform.createTransformedShape(glyphBounds).getBounds();
            if (!displayArea.intersects(glyphBounds)) {
                return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OUTSCREEN);
            }
            RectangularShape shieldBounds = null;
            if (labelItem.getTextStyle().getGraphic() != null) {
                Rectangle area = labelItem.getTextStyle().getGraphicDimensions();
                Rectangle untransformedBounds = glyphVector.getPixelBounds(new FontRenderContext(new AffineTransform(), true, false), 0.0f, 0.0f);
                double[] shieldVerts = new double[]{-area.width / 2 + untransformedBounds.x - untransformedBounds.width / 2, -area.height / 2 + untransformedBounds.y - untransformedBounds.height / 2, area.width / 2, area.height / 2};
                tempTransform.transform(shieldVerts, 0, shieldVerts, 0, 2);
                shieldBounds = new Rectangle2D.Double(shieldVerts[0] + (double)(glyphBounds.width / 2), shieldVerts[1] + (double)(glyphBounds.height / 2), shieldVerts[2] - shieldVerts[0], shieldVerts[3] - shieldVerts[1]);
                if (!displayArea.contains((Rectangle2D)shieldBounds)) {
                    return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OUTSCREEN);
                }
            }
            int space = labelItem.getSpaceAround();
            int haloRadius = Math.round(labelItem.getTextStyle().getHaloFill() != null ? labelItem.getTextStyle().getHaloRadius() : 0.0f);
            if (space > 0 || !this.overlapping) {
                if (this.overlappingItems(glyphBounds, glyphs, space + haloRadius)) {
                    return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OVERLAPPED);
                }
                if (shieldBounds != null && this.overlappingItems(shieldBounds.getBounds(), glyphs, space)) {
                    return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OVERLAPPED);
                }
            }
            drawGlowing = representativeGeometry != null;
            try {
                this.drawLabelLine(graphics, labelItem, originalGeom, representativeGeometry);
                AffineTransform newTransform = new AffineTransform(oldTransform);
                newTransform.concatenate(tempTransform);
                graphics.setTransform(newTransform);
                if (labelItem.getTextStyle().getGraphic() != null) {
                    LiteShape2 tempShape = new LiteShape2((Geometry)this.geometryFactory.createPoint(new Coordinate((double)glyphBounds.width / 2.0, -1.0 * (double)glyphBounds.height / 2.0)), null, null, false, false);
                    labelItem.getTextStyle().getGraphic().setMinMaxScale(0.0, 10.0);
                    new StyledShapePainter().paint(graphics, tempShape, labelItem.getTextStyle().getGraphic(), 5.0);
                    graphics.setTransform(tempTransform);
                }
                Shape outline = glyphVector.getOutline();
                if (labelItem.getTextStyle().getHaloFill() != null && drawGlowing) {
                    graphics.setPaint(labelItem.getTextStyle().getHaloFill());
                    graphics.setComposite(labelItem.getTextStyle().getHaloComposite());
                    graphics.setStroke(new BasicStroke(2.0f * (float)haloRadius, 1, 1));
                    graphics.draw(outline);
                }
                Paint fill = labelItem.getTextStyle().getFill();
                Composite comp = labelItem.getTextStyle().getComposite();
                if (fill == null) {
                    fill = Color.BLACK;
                    comp = AlphaComposite.getInstance(3, 1.0f);
                }
                if (fill != null) {
                    graphics.setPaint(fill);
                    graphics.setComposite(comp);
                    if (this.outlineRenderingEnabled) {
                        graphics.fill(outline);
                    } else {
                        graphics.drawGlyphVector(glyphVector, 0.0f, 0.0f);
                    }
                    Rectangle bounds = glyphVector.getPixelBounds(new FontRenderContext(tempTransform, true, false), 0.0f, 0.0f);
                    int extraSpace = labelItem.getSpaceAround();
                    if (extraSpace >= 0) {
                        bounds = new Rectangle(bounds.x - extraSpace, bounds.y - extraSpace, bounds.width + extraSpace, bounds.height + extraSpace);
                        if (shieldBounds != null) {
                            bounds.add((Rectangle2D)shieldBounds);
                        }
                        bounds.grow(haloRadius, haloRadius);
                        glyphs.add(bounds);
                    }
                }
            }
            finally {
                graphics.setTransform(oldTransform);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.SUCCEDED);
    }

    protected void drawLabelLine(Graphics2D graphics, LabelCacheItem labelItem, Geometry originalGeom, Geometry representativeGeometry) {
        boolean lineToLabelExist;
        LineToLabel lineToLabel = labelItem.getLineToLabel();
        boolean bothGeometriesExist = originalGeom != null && representativeGeometry != null;
        boolean bl = lineToLabelExist = lineToLabel != null;
        if (bothGeometriesExist && lineToLabelExist) {
            graphics.setStroke(new BasicStroke((float)lineToLabel.getWidth(), 0, 0, 10.0f, lineToLabel.getDash(), 0.0f));
            graphics.setColor(lineToLabel.getColor());
            Coordinate c1 = representativeGeometry.isValid() ? representativeGeometry.getCentroid().getCoordinate() : representativeGeometry.getCoordinate();
            Coordinate c2 = originalGeom.getCentroid().getCoordinate();
            Line2D.Double line = new Line2D.Double(c1.x, c1.y, c2.x, c2.y);
            graphics.draw(line);
        }
    }

    protected void paintLabels(Graphics2D graphics, Rectangle displayArea) {
        if (!this.activeLayers.isEmpty()) {
            throw new IllegalStateException(I18N.getMessage(this.getClass(), "{0}-are-layers-that-started-rendering-but-have-not-completed-stop-or-enlayer-must-be-called-before-clear-is-called", new Object[]{this.activeLayers}));
        }
        ArrayList<Rectangle> glyphs = new ArrayList<Rectangle>();
        displayArea = new Rectangle(displayArea);
        --displayArea.width;
        --displayArea.height;
        Geometry displayGeom = this.geometryFactory.toGeometry(new Envelope(displayArea.getMinX(), displayArea.getMaxX(), displayArea.getMinY(), displayArea.getMaxY()));
        List<LabelCacheItem> items = this.needsOrdering ? this.orderedLabels() : this.getActiveLabels();
        for (LabelCacheItem labelItem : items) {
            if (this.stop) {
                return;
            }
            this.paintLabel(labelItem, graphics, displayArea, glyphs, displayGeom);
        }
    }

    protected double goodnessOfFit(GlyphVector glyphVector, AffineTransform tempTransform, Geometry representativeGeom) {
        if (representativeGeom instanceof Point) {
            return 1.0;
        }
        if (representativeGeom instanceof LineString) {
            return 1.0;
        }
        if (representativeGeom instanceof Polygon) {
            Rectangle glyphBounds = glyphVector.getPixelBounds(new FontRenderContext(tempTransform, true, false), 0.0f, 0.0f);
            try {
                Polygon p = this.simplifyPoly((Polygon)representativeGeom);
                int count = 0;
                int n = 10;
                double mindistance = glyphBounds.height;
                int t = 1;
                while (t < n + 1) {
                    Coordinate c = new Coordinate((double)glyphBounds.x + (double)glyphBounds.width * ((double)t / (double)(n + 1)), glyphBounds.getCenterY());
                    Point pp = this.geometryFactory.createPoint(c);
                    if (p.distance((Geometry)pp) < mindistance) {
                        ++count;
                    }
                    ++t;
                }
                return (double)count / (double)n;
            }
            catch (Exception e) {
                representativeGeom.geometryChanged();
                Envelope ePoly = representativeGeom.getEnvelopeInternal();
                Envelope eglyph = new Envelope((double)glyphBounds.x, (double)(glyphBounds.x + glyphBounds.width), (double)glyphBounds.y, (double)(glyphBounds.y + glyphBounds.height));
                Envelope inter = this.intersection(ePoly, eglyph);
                if (inter != null) {
                    return inter.getWidth() * inter.getHeight() / (eglyph.getWidth() * eglyph.getHeight());
                }
                return 0.0;
            }
        }
        return 0.0;
    }

    private Polygon simplifyPoly(Polygon polygon) {
        if (polygon.getNumInteriorRing() == 0) {
            return polygon;
        }
        LineString outer = polygon.getExteriorRing();
        if (outer.getStartPoint().distance((Geometry)outer.getEndPoint()) != 0.0) {
            ArrayList<Coordinate> clist = new ArrayList<Coordinate>(Arrays.asList(outer.getCoordinates()));
            clist.add(outer.getStartPoint().getCoordinate());
            outer = outer.getFactory().createLinearRing(clist.toArray(new Coordinate[clist.size()]));
        }
        LinearRing r = (LinearRing)outer;
        return outer.getFactory().createPolygon(r, null);
    }

    protected boolean overlappingItems(Rectangle bounds, List<Rectangle> glyphs, int extraSpace) {
        bounds = new Rectangle(bounds.x - extraSpace, bounds.y - extraSpace, bounds.width + extraSpace, bounds.height + extraSpace);
        for (Rectangle oldBounds : glyphs) {
            if (!oldBounds.intersects(bounds)) continue;
            return true;
        }
        return false;
    }

    @Override
    protected Geometry paintLineLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) {
        LineString line = this.getLineSetRepresentativeLocation(labelItem.getGeoms(), displayGeom);
        if (line == null) {
            return null;
        }
        TextStyle2D textStyle = labelItem.getTextStyle();
        this.paintLineStringLabel(glyphVector, line, textStyle, tempTransform);
        return line;
    }

    @Override
    protected void paintLineStringLabel(GlyphVector glyphVector, LineString line, TextStyle2D textStyle, AffineTransform tempTransform) {
        double rotation;
        Rectangle2D textBounds = glyphVector.getVisualBounds();
        Point centroid = this.middleLine(line, 0.5);
        tempTransform.translate(centroid.getX(), centroid.getY());
        double displacementX = 0.0;
        double displacementY = 0.0;
        double anchorX = textStyle.getAnchorX();
        double anchorY = textStyle.getAnchorY();
        if (textStyle.isPointPlacement()) {
            rotation = textStyle.getRotation();
            displacementX = textStyle.getAnchorX() * -textBounds.getWidth() + textStyle.getDisplacementX();
            displacementY = textStyle.getAnchorY() * textBounds.getHeight() - textStyle.getDisplacementY();
        } else {
            rotation = textStyle.isRotationStablished() ? textStyle.getRotation() : this.middleTheta(line, 0.5);
            displacementY -= (double)textStyle.getPerpendicularOffset();
            displacementX = -textBounds.getWidth() / 2.0;
            anchorX = 0.5;
            anchorY = 0.5;
        }
        if (rotation != rotation) {
            rotation = 0.0;
        }
        if (Double.isInfinite(rotation)) {
            rotation = 0.0;
        }
        tempTransform.rotate(rotation);
        tempTransform.translate(displacementX, displacementY);
    }

    @Override
    protected Geometry paintPointLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) {
        double rotation;
        Point point = this.getPointSetRepresentativeLocation(labelItem.getGeoms(), displayGeom);
        if (point == null) {
            return null;
        }
        TextStyle2D textStyle = labelItem.getTextStyle();
        Rectangle2D textBounds = glyphVector.getVisualBounds();
        tempTransform.translate(point.getX(), point.getY());
        double displacementX = 0.0;
        double displacementY = 0.0;
        displacementX = textStyle.getAnchorX() * -textBounds.getWidth() + textStyle.getDisplacementX();
        displacementY = textStyle.getAnchorY() * textBounds.getHeight() - textStyle.getDisplacementY();
        if (!textStyle.isPointPlacement()) {
            displacementY -= (double)textStyle.getPerpendicularOffset();
        }
        if ((rotation = textStyle.getRotation()) != rotation) {
            rotation = 0.0;
        }
        if (Double.isInfinite(rotation)) {
            rotation = 0.0;
        }
        tempTransform.rotate(rotation);
        tempTransform.translate(displacementX, displacementY);
        return point;
    }

    @Override
    protected Geometry paintPolygonLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) {
        double rotation;
        Point centroid;
        Polygon geom = this.getPolySetRepresentativeLocation(labelItem.getGeoms(), displayGeom);
        if (geom == null) {
            return null;
        }
        try {
            if (geom.getNumPoints() < 4) {
                centroid = this.geometryFactory.createPoint(geom.getCoordinate());
            } else {
                centroid = geom.getCentroid();
                if (!geom.contains((Geometry)centroid)) {
                    centroid = geom.getInteriorPoint();
                }
            }
        }
        catch (Exception e) {
            try {
                centroid = geom.getExteriorRing().getCentroid();
            }
            catch (Exception ee) {
                try {
                    centroid = geom.getFactory().createPoint(geom.getCoordinate());
                }
                catch (Exception eee) {
                    return null;
                }
            }
        }
        TextStyle2D textStyle = labelItem.getTextStyle();
        Rectangle2D textBounds = glyphVector.getVisualBounds();
        tempTransform.translate(centroid.getX(), centroid.getY());
        double displacementX = 0.0;
        double displacementY = 0.0;
        displacementX = textStyle.getAnchorX() * -textBounds.getWidth() + textStyle.getDisplacementX();
        displacementY = textStyle.getAnchorY() * textBounds.getHeight() - textStyle.getDisplacementY();
        if (!textStyle.isPointPlacement()) {
            displacementX = textStyle.getAnchorX() + -textBounds.getWidth() / 2.0 + textStyle.getDisplacementX();
            displacementY = textStyle.getAnchorY() + textBounds.getHeight() / 2.0 + textStyle.getDisplacementY();
            displacementY -= (double)textStyle.getPerpendicularOffset();
        }
        if ((rotation = textStyle.getRotation()) != rotation) {
            rotation = 0.0;
        }
        if (Double.isInfinite(rotation)) {
            rotation = 0.0;
        }
        tempTransform.rotate(rotation);
        tempTransform.translate(displacementX, displacementY);
        return geom;
    }

    @Override
    protected Point getPointSetRepresentativeLocation(List<Geometry> geoms, Geometry displayGeometry) {
        ArrayList<Point> pts = new ArrayList<Point>();
        for (Geometry g : geoms) {
            if (!(g instanceof Point) && !(g instanceof MultiPoint)) {
                g = g.getCentroid();
            }
            if (g instanceof Point) {
                if (!displayGeometry.intersects(g)) continue;
                pts.add((Point)g);
                continue;
            }
            if (!(g instanceof MultiPoint)) continue;
            int t = 0;
            while (t < g.getNumGeometries()) {
                Point gg = (Point)g.getGeometryN(t);
                if (displayGeometry.intersects((Geometry)gg)) {
                    pts.add(gg);
                }
                ++t;
            }
        }
        if (pts.size() == 0) {
            return null;
        }
        return (Point)pts.get(0);
    }

    @Override
    protected LineString getLineSetRepresentativeLocation(List<Geometry> geoms, Geometry displayGeometry) {
        ArrayList<LineString> lines = new ArrayList<LineString>();
        for (Geometry g : geoms) {
            if (!g.isValid() || !(g instanceof LineString) && !(g instanceof MultiLineString) && !(g instanceof Polygon) && !(g instanceof MultiPolygon)) continue;
            if (g instanceof Polygon || g instanceof MultiPolygon) {
                if ((g = g.getBoundary()) instanceof LineString || g instanceof MultiLineString) continue;
                continue;
            }
            if (g instanceof LineString) {
                if (g.getLength() == 0.0) continue;
                lines.add((LineString)g);
                continue;
            }
            int t = 0;
            while (t < g.getNumGeometries()) {
                LineString gg = (LineString)g.getGeometryN(t);
                lines.add(gg);
                ++t;
            }
        }
        if (lines.size() == 0) {
            return null;
        }
        Collection<LineString> merged = this.mergeLines(lines);
        ArrayList<LineString> clippedLines = new ArrayList<LineString>();
        Iterator<LineString> itLines = merged.iterator();
        Envelope displayGeomEnv = displayGeometry.getEnvelopeInternal();
        while (itLines.hasNext()) {
            LineString l = itLines.next();
            MultiLineString ll = this.clipLineString(l, (Polygon)displayGeometry, displayGeomEnv);
            if (ll == null || ll.isEmpty()) continue;
            int t = 0;
            while (t < ll.getNumGeometries()) {
                clippedLines.add((LineString)ll.getGeometryN(t));
                ++t;
            }
        }
        if (clippedLines.size() == 0) {
            return null;
        }
        double maxLen = -1.0;
        LineString maxLine = null;
        int t = 0;
        while (t < clippedLines.size()) {
            LineString cline = (LineString)clippedLines.get(t);
            if (cline.getLength() > maxLen) {
                maxLine = cline;
                maxLen = cline.getLength();
            }
            ++t;
        }
        return maxLine;
    }

    public MultiLineString clipLineString(LineString line, Polygon bbox, Envelope displayGeomEnv) {
        LineString clip = line;
        line.geometryChanged();
        if (displayGeomEnv.contains(line.getEnvelopeInternal())) {
            LineString[] lns = new LineString[]{clip};
            return line.getFactory().createMultiLineString(lns);
        }
        try {
            Decimator d = new Decimator(10.0, 10.0);
            d.decimate((Geometry)line);
            line.geometryChanged();
            clip = EnhancedPrecisionOp.intersection((Geometry)line, (Geometry)bbox);
        }
        catch (Exception e) {
            clip = line;
        }
        if (clip instanceof MultiLineString) {
            return (MultiLineString)clip;
        }
        if (clip instanceof LineString) {
            LineString[] lns = new LineString[]{clip};
            return line.getFactory().createMultiLineString(lns);
        }
        if (clip instanceof Point) {
            return null;
        }
        if (clip instanceof MultiPoint) {
            return null;
        }
        GeometryCollection gc = (GeometryCollection)clip;
        ArrayList<LineString> lns = new ArrayList<LineString>();
        int t = 0;
        while (t < gc.getNumGeometries()) {
            Geometry g = gc.getGeometryN(t);
            if (g instanceof LineString) {
                lns.add((LineString)g);
            }
            ++t;
        }
        if (lns.size() == 0) {
            return null;
        }
        return line.getFactory().createMultiLineString(lns.toArray(new LineString[1]));
    }

    @Override
    protected Polygon getPolySetRepresentativeLocation(List<Geometry> geoms, Geometry displayGeometry) {
        ArrayList<Polygon> polys = new ArrayList<Polygon>();
        for (Geometry g : geoms) {
            if (!(g instanceof Polygon) && !(g instanceof MultiPolygon)) continue;
            if (g instanceof Polygon) {
                polys.add((Polygon)g);
                continue;
            }
            int t = 0;
            while (t < g.getNumGeometries()) {
                Polygon gg = (Polygon)g.getGeometryN(t);
                polys.add(gg);
                ++t;
            }
        }
        if (polys.size() == 0) {
            return null;
        }
        ArrayList<Polygon> clippedPolys = new ArrayList<Polygon>();
        Iterator itPolys = polys.iterator();
        Envelope displayGeomEnv = displayGeometry.getEnvelopeInternal();
        while (itPolys.hasNext()) {
            Polygon p = (Polygon)itPolys.next();
            MultiPolygon pp = this.clipPolygon(p, (Polygon)displayGeometry, displayGeomEnv);
            if (pp == null || pp.isEmpty()) continue;
            int t = 0;
            while (t < pp.getNumGeometries()) {
                clippedPolys.add((Polygon)pp.getGeometryN(t));
                ++t;
            }
        }
        if (clippedPolys.size() == 0) {
            return null;
        }
        double maxSize = -1.0;
        Polygon maxPoly = null;
        int t = 0;
        while (t < clippedPolys.size()) {
            Polygon cpoly = (Polygon)clippedPolys.get(t);
            if (cpoly.getArea() > maxSize) {
                maxPoly = cpoly;
                maxSize = cpoly.getArea();
            }
            ++t;
        }
        return maxPoly;
    }

    public MultiPolygon clipPolygon(Polygon poly, Polygon bbox, Envelope displayGeomEnv) {
        Geometry clonedGeometry = (Geometry)poly.clone();
        Polygon clip = poly;
        poly.geometryChanged();
        if (displayGeomEnv.contains(poly.getEnvelopeInternal())) {
            Polygon[] polys = new Polygon[]{clip};
            return poly.getFactory().createMultiPolygon(polys);
        }
        try {
            Decimator d = new Decimator(10.0, 10.0);
            d.decimate(clonedGeometry);
            clonedGeometry.geometryChanged();
            clip = EnhancedPrecisionOp.intersection((Geometry)clonedGeometry, (Geometry)bbox);
        }
        catch (Exception e) {
            try {
                clip = EnhancedPrecisionOp.intersection((Geometry)poly, (Geometry)bbox);
            }
            catch (Exception e1) {
                clip = poly;
            }
        }
        if (clip instanceof MultiPolygon) {
            return (MultiPolygon)clip;
        }
        if (clip instanceof Polygon) {
            Polygon[] polys = new Polygon[]{clip};
            return poly.getFactory().createMultiPolygon(polys);
        }
        if (clip instanceof Point) {
            return null;
        }
        if (clip instanceof MultiPoint) {
            return null;
        }
        if (clip instanceof LineString) {
            return null;
        }
        if (clip instanceof MultiLineString) {
            return null;
        }
        GeometryCollection gc = (GeometryCollection)clip;
        ArrayList<Polygon> plys = new ArrayList<Polygon>();
        int t = 0;
        while (t < gc.getNumGeometries()) {
            Geometry g = gc.getGeometryN(t);
            if (g instanceof Polygon) {
                plys.add((Polygon)g);
            }
            ++t;
        }
        if (plys.size() == 0) {
            return null;
        }
        return poly.getFactory().createMultiPolygon(plys.toArray(new Polygon[1]));
    }

    protected double middleTheta(LineString l, double percent) {
        if (percent >= 1.0) {
            percent = 0.99;
        }
        if (percent <= 0.0) {
            percent = 0.01;
        }
        double len = l.getLength();
        double dist = percent * len;
        double running_sum_dist = 0.0;
        CoordinateSequence pts = l.getCoordinateSequence();
        int length = pts.size();
        Coordinate curr = new Coordinate();
        Coordinate next = new Coordinate();
        int i = 0;
        while (i < length - 1) {
            pts.getCoordinate(i, curr);
            pts.getCoordinate(i + 1, next);
            double segmentLen = curr.distance(next);
            if (running_sum_dist + segmentLen >= dist) {
                double dx = next.x - curr.x;
                double dy = next.y - curr.y;
                double slope = dy / dx;
                return Math.atan(slope);
            }
            running_sum_dist += segmentLen;
            ++i;
        }
        return 0.0;
    }

    @Override
    protected Point middleLine(LineString l, double percent) {
        if (percent >= 1.0) {
            percent = 0.99;
        }
        if (percent <= 0.0) {
            percent = 0.01;
        }
        double len = l.getLength();
        double dist = percent * len;
        double running_sum_dist = 0.0;
        Coordinate[] pts = l.getCoordinates();
        int length = pts.length;
        int i = 0;
        while (i < length - 1) {
            double segmentLen = pts[i].distance(pts[i + 1]);
            if (running_sum_dist + segmentLen >= dist) {
                double r = (dist - running_sum_dist) / segmentLen;
                Coordinate c = new Coordinate(pts[i].x + (pts[i + 1].x - pts[i].x) * r, pts[i].y + (pts[i + 1].y - pts[i].y) * r);
                return l.getFactory().createPoint(c);
            }
            running_sum_dist += segmentLen;
            ++i;
        }
        return l.getEndPoint();
    }

    public void processNodes(List edges, Hashtable nodes, ArrayList result) {
        int index = 0;
        while (index < edges.size()) {
            LineString ls2;
            LineString ls = (LineString)edges.get(index);
            Coordinate key = ls.getCoordinateN(0);
            ArrayList nodeList = (ArrayList)nodes.get(key);
            if (nodeList == null) {
                ++index;
                continue;
            }
            if (!nodeList.contains(ls)) {
                ++index;
                continue;
            }
            this.removeFromHash(nodes, ls);
            Coordinate key2 = ls.getCoordinateN(ls.getNumPoints() - 1);
            ArrayList nodeList2 = (ArrayList)nodes.get(key2);
            if (nodeList.size() == 0 && nodeList2.size() == 0) {
                result.add(ls);
                ++index;
                continue;
            }
            if (nodeList.size() > 0) {
                ls2 = this.getLongest(nodeList);
                ls = this.merge(ls, ls2);
                this.removeFromHash(nodes, ls2);
            }
            if (nodeList2.size() > 0) {
                ls2 = this.getLongest(nodeList2);
                ls = this.merge(ls, ls2);
                this.removeFromHash(nodes, ls2);
            }
            edges.set(index, ls);
            this.putInNodeHash(ls, nodes);
        }
    }

    public void removeFromHash(Hashtable nodes, LineString ls) {
        Coordinate key = ls.getCoordinateN(0);
        ArrayList nodeList = (ArrayList)nodes.get(key);
        if (nodeList != null) {
            nodeList.remove(ls);
        }
        if ((nodeList = (ArrayList)nodes.get(key = ls.getCoordinateN(ls.getNumPoints() - 1))) != null) {
            nodeList.remove(ls);
        }
    }

    public LineString getLongest(List<LineString> al) {
        if (al.size() == 1) {
            return al.get(0);
        }
        double maxLength = -1.0;
        LineString result = null;
        int size = al.size();
        int t = 0;
        while (t < size) {
            LineString l = al.get(t);
            if (l.getLength() > maxLength) {
                result = l;
                maxLength = l.getLength();
            }
            ++t;
        }
        return result;
    }

    public void putInNodeHash(LineString ls, Hashtable nodes) {
        Coordinate key = ls.getCoordinateN(0);
        ArrayList<LineString> nodeList = (ArrayList<LineString>)nodes.get(key);
        if (nodeList == null) {
            nodeList = new ArrayList<LineString>();
            nodeList.add(ls);
            nodes.put(key, nodeList);
        } else {
            nodeList.add(ls);
        }
        key = ls.getCoordinateN(ls.getNumPoints() - 1);
        nodeList = (ArrayList<LineString>)nodes.get(key);
        if (nodeList == null) {
            nodeList = new ArrayList<LineString>();
            nodeList.add(ls);
            nodes.put(key, nodeList);
        } else {
            nodeList.add(ls);
        }
    }

    private Envelope intersection(Envelope e1, Envelope e2) {
        Envelope r = e1.intersection(e2);
        if (r.getWidth() < 0.0) {
            return null;
        }
        if (r.getHeight() < 0.0) {
            return null;
        }
        return r;
    }

    public void enableLayer(String layerId) {
        this.needsOrdering = true;
        this.enabledLayers.add(layerId);
    }

    public boolean isOutlineRenderingEnabled() {
        return this.outlineRenderingEnabled;
    }

    public void setOutlineRenderingEnabled(boolean outlineRenderingEnabled) {
        this.outlineRenderingEnabled = outlineRenderingEnabled;
    }
}

