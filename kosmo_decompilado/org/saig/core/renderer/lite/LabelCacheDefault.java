/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
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
 *  com.vividsolutions.jts.operation.linemerge.LineMerger
 *  com.vividsolutions.jts.precision.EnhancedPrecisionOp
 *  javax.media.jai.util.Range
 *  org.apache.log4j.Logger
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Coordinate;
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
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.StringUtil;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.media.jai.util.Range;
import org.apache.log4j.Logger;
import org.saig.core.renderer.lite.LabelCache;
import org.saig.core.renderer.lite.LabelCacheItem;
import org.saig.core.renderer.lite.LineToLabel;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.renderer.style.SLDStyleFactory;
import org.saig.core.renderer.style.TextStyle2D;
import org.saig.core.styling.TextSymbolizer;
import org.saig.core.util.ColorUtil;

public class LabelCacheDefault
implements LabelCache {
    protected static final Logger LOGGER = Logger.getLogger(LabelCacheDefault.class);
    protected LabelRenderingMode labelRenderingMode = LabelRenderingMode.STRING;
    protected static final double[] RIGHT_ANCHOR_CANDIDATES = new double[]{0.0, 0.5, 0.0, 0.0, 0.0, 1.0};
    protected static final double[] MID_ANCHOR_CANDIDATES = new double[]{0.5, 0.5, 0.0, 0.5, 1.0, 0.5};
    protected static final double[] LEFT_ANCHOR_CANDIDATES = new double[]{1.0, 0.5, 1.0, 0.0, 1.0, 1.0};
    public static final double SQUARE = Math.sqrt(2.0);
    protected Map<String, LabelCacheItem> labelCache = new HashMap<String, LabelCacheItem>();
    protected List<LabelCacheItem> labelCacheNonGrouped = new ArrayList<LabelCacheItem>();
    protected List<Rectangle2D> reserved = new ArrayList<Rectangle2D>();
    protected SLDStyleFactory styleFactory = new SLDStyleFactory();
    protected boolean stop = false;
    protected GeometryFactory geometryFactory = new GeometryFactory();
    protected boolean overlapping;
    protected boolean repeated;

    public LabelCacheDefault(boolean overlapping, boolean repeated) {
        this.overlapping = overlapping;
        this.repeated = repeated;
    }

    @Override
    public void stop() {
        this.stop = true;
    }

    @Override
    public void start() {
        this.stop = false;
    }

    @Override
    public void startLayer(String layerId) {
    }

    @Override
    public void put(String layerId, TextSymbolizer symbolizer, Feature feature, LiteShape2 shape, LiteShape2 originalShape, Range scaleRange, Number textHeight, Color color, Number rotation, double factor) {
        try {
            Font newFont;
            TextStyle2D textStyle = (TextStyle2D)this.styleFactory.createStyle(feature, symbolizer, scaleRange);
            if (textStyle == null) {
                return;
            }
            String label = textStyle.getLabel();
            if (label == null) {
                return;
            }
            if ((label = label.trim()).length() == 0) {
                return;
            }
            if (rotation != null) {
                double rotationD = rotation.doubleValue();
                if (rotationD != 0.0) {
                    rotationD *= -1.0;
                }
                textStyle.setRotation(rotationD *= Math.PI / 180);
            }
            if (textHeight != null) {
                newFont = textStyle.getFont().deriveFont((float)((double)textHeight.floatValue() * factor));
                textStyle.setFont(newFont);
            } else {
                newFont = textStyle.getFont().deriveFont((float)((double)textStyle.getFont().getSize() * factor));
                textStyle.setFont(newFont);
            }
            if (symbolizer.getHeightAttribute() != null) {
                textStyle.setFontSizeScale(true);
            }
            if (color != null) {
                textStyle.setFill(color);
            }
            if (!this.repeated) {
                if (!this.labelCache.containsKey(label)) {
                    this.labelCache.put(label, new LabelCacheItem(layerId, textStyle, shape, originalShape, label));
                } else {
                    LabelCacheItem item = this.labelCache.get(textStyle.getLabel());
                    item.getGeoms().add(shape.getGeometry());
                }
            } else {
                this.labelCacheNonGrouped.add(new LabelCacheItem(layerId, textStyle, shape, originalShape, label));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endLayer(String layerId, Graphics2D graphics, Rectangle displayArea) {
    }

    @Override
    public void end(Graphics2D graphics, Rectangle displayArea) {
        ArrayList<Rectangle> glyphs = new ArrayList<Rectangle>();
        if (!this.repeated) {
            Iterator<String> labelIter = this.labelCache.keySet().iterator();
            while (labelIter.hasNext() && !this.stop) {
                LabelCacheItem labelItem = this.labelCache.get(labelIter.next());
                this.processItem(labelItem, graphics, displayArea, glyphs);
            }
            this.labelCache.clear();
        } else {
            for (LabelCacheItem labelItem : this.labelCacheNonGrouped) {
                this.processItem(labelItem, graphics, displayArea, glyphs);
            }
            this.labelCacheNonGrouped.clear();
        }
    }

    protected void processItem(LabelCacheItem labelItem, Graphics2D graphics, Rectangle displayArea, List<Rectangle> glyphs) {
        boolean drawGlowing = false;
        Geometry representativeGeometry = null;
        try {
            Geometry displayGeom = this.geometryFactory.toGeometry(new Envelope(displayArea.getMinX(), displayArea.getMaxX(), displayArea.getMinY(), displayArea.getMaxY()));
            AffineTransform oldTransform = graphics.getTransform();
            AffineTransform tempTransform = new AffineTransform(oldTransform);
            GlyphVector glyphVector = labelItem.getTextStyle().getTextGlyphVector(graphics);
            if (labelItem.getGeometry() instanceof Point || labelItem.getGeometry() instanceof MultiPoint) {
                representativeGeometry = this.paintPointLabel(glyphVector, labelItem, tempTransform, displayGeom);
            }
            if (labelItem.getGeometry() instanceof LineString && !(labelItem.getGeometry() instanceof LinearRing) || labelItem.getGeometry() instanceof MultiLineString) {
                try {
                    representativeGeometry = this.paintLineLabel(glyphVector, labelItem, tempTransform, displayGeom);
                }
                catch (Exception e) {
                    LOGGER.error((Object)e);
                }
            }
            if (labelItem.getGeometry() instanceof Polygon || labelItem.getGeometry() instanceof MultiPolygon || labelItem.getGeometry() instanceof LinearRing) {
                representativeGeometry = this.paintPolygonLabel(glyphVector, labelItem, tempTransform, displayGeom);
            }
            if (!this.overlapping && this.overlappingItems(glyphVector, tempTransform, glyphs)) {
                return;
            }
            drawGlowing = representativeGeometry != null;
            try {
                graphics.setTransform(tempTransform);
                if (labelItem.getTextStyle().getHaloFill() != null && drawGlowing) {
                    graphics.setPaint(labelItem.getTextStyle().getHaloFill());
                    graphics.setComposite(labelItem.getTextStyle().getHaloComposite());
                    graphics.fill(labelItem.getTextStyle().getHaloShape(graphics));
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
                    graphics.drawGlyphVector(glyphVector, 0.0f, 0.0f);
                    glyphs.add(glyphVector.getPixelBounds(new FontRenderContext(tempTransform, true, false), 0.0f, 0.0f));
                }
            }
            finally {
                graphics.setTransform(oldTransform);
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)e);
        }
    }

    @Override
    public void print(Graphics2D graphics, Rectangle displayArea) {
        ArrayList<Rectangle> glyphs = new ArrayList<Rectangle>();
        Iterator<String> labelIter = this.labelCache.keySet().iterator();
        while (labelIter.hasNext() && !this.stop) {
            try {
                boolean drawGlowing = false;
                LabelCacheItem labelItem = this.labelCache.get(labelIter.next());
                Geometry displayGeom = this.geometryFactory.toGeometry(new Envelope(displayArea.getMinX(), displayArea.getMaxX(), displayArea.getMinY(), displayArea.getMaxY()));
                Geometry representativeGeometry = null;
                AffineTransform oldTransform = graphics.getTransform();
                AffineTransform tempTransform = new AffineTransform(oldTransform);
                GlyphVector glyphVector = labelItem.getTextStyle().getTextGlyphVector(graphics);
                if (labelItem.getGeometry() instanceof Point || labelItem.getGeometry() instanceof MultiPoint) {
                    representativeGeometry = this.paintPointLabel(glyphVector, labelItem, tempTransform, displayGeom);
                }
                if (labelItem.getGeometry() instanceof LineString && !(labelItem.getGeometry() instanceof LinearRing) || labelItem.getGeometry() instanceof MultiLineString) {
                    try {
                        representativeGeometry = this.paintLineLabel(glyphVector, labelItem, tempTransform, displayGeom);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (labelItem.getGeometry() instanceof Polygon || labelItem.getGeometry() instanceof MultiPolygon || labelItem.getGeometry() instanceof LinearRing) {
                    representativeGeometry = this.paintPolygonLabel(glyphVector, labelItem, tempTransform, displayGeom);
                }
                if (this.overlappingItems(glyphVector, tempTransform, glyphs)) continue;
                drawGlowing = representativeGeometry != null;
                try {
                    graphics.setTransform(tempTransform);
                    if (labelItem.getTextStyle().getHaloFill() != null && drawGlowing) {
                        graphics.setPaint(labelItem.getTextStyle().getHaloFill());
                        graphics.setComposite(labelItem.getTextStyle().getHaloComposite());
                        graphics.fill(labelItem.getTextStyle().getHaloShape(graphics));
                    }
                    Paint fill = labelItem.getTextStyle().getFill();
                    Composite comp = labelItem.getTextStyle().getComposite();
                    if (fill == null) {
                        fill = Color.BLACK;
                        comp = AlphaComposite.getInstance(3, 1.0f);
                    }
                    if (fill == null) continue;
                    graphics.setPaint(fill);
                    graphics.setComposite(comp);
                    graphics.drawGlyphVector(glyphVector, 0.0f, 0.0f);
                    glyphs.add(glyphVector.getPixelBounds(new FontRenderContext(tempTransform, true, false), 0.0f, 0.0f));
                }
                finally {
                    graphics.setTransform(oldTransform);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean overlappingItems(GlyphVector glyphVector, AffineTransform tempTransform, List<Rectangle> glyphs) {
        for (Rectangle oldBounds : glyphs) {
            if (!oldBounds.intersects(glyphVector.getPixelBounds(new FontRenderContext(tempTransform, true, false), 0.0f, 0.0f))) continue;
            return true;
        }
        return false;
    }

    protected Geometry paintLineLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) {
        LineString line = this.getLineSetRepresentativeLocation(labelItem.getGeoms(), displayGeom);
        if (line == null) {
            return null;
        }
        TextStyle2D textStyle = labelItem.getTextStyle();
        this.paintLineStringLabel(glyphVector, line, textStyle, tempTransform);
        return line;
    }

    protected void paintLineStringLabel(GlyphVector glyphVector, LineString line, TextStyle2D textStyle, AffineTransform tempTransform) {
        double rotation = textStyle.getRotation();
        Rectangle2D textBounds = glyphVector.getVisualBounds();
        Point centroid = this.middleLine(line, 0.5);
        tempTransform.translate(centroid.getX(), centroid.getY());
        double displacementX = 0.0;
        double displacementY = 0.0;
        if (!textStyle.isPointPlacement()) {
            double offset = textStyle.getDisplacementY();
            displacementY = offset > 0.0 ? -offset : (offset < 0.0 ? -offset + textBounds.getHeight() : textBounds.getHeight() / 2.0);
            displacementX = -textBounds.getWidth() / 2.0;
        } else {
            displacementX = textStyle.getAnchorX() + -textBounds.getWidth() / 2.0 + textStyle.getDisplacementX();
            displacementY = textStyle.getAnchorY() + textBounds.getHeight() / 2.0 + textStyle.getDisplacementY();
        }
        tempTransform.rotate(rotation);
        tempTransform.translate(displacementX, displacementY);
    }

    protected Geometry paintPointLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) {
        Point point = this.getPointSetRepresentativeLocation(labelItem.getGeoms(), displayGeom);
        if (point == null) {
            return null;
        }
        TextStyle2D textStyle = labelItem.getTextStyle();
        Rectangle2D textBounds = glyphVector.getVisualBounds();
        tempTransform.translate(point.getX(), point.getY());
        double displacementX = 0.0;
        double displacementY = 0.0;
        if (!textStyle.isPointPlacement()) {
            double offset = textStyle.getDisplacementY();
            displacementY = offset > 0.0 ? -offset : (offset < 0.0 ? -offset + textBounds.getHeight() : textBounds.getHeight() / 2.0);
            displacementX = -textBounds.getWidth() / 2.0;
        } else {
            displacementX = textStyle.getAnchorX() * -textBounds.getWidth() + textStyle.getDisplacementX();
            displacementY = textStyle.getAnchorY() * textBounds.getHeight() + textStyle.getDisplacementY();
        }
        tempTransform.rotate(textStyle.getRotation());
        tempTransform.translate(displacementX, displacementY);
        return point;
    }

    protected Geometry paintPolygonLabel(GlyphVector glyphVector, LabelCacheItem labelItem, AffineTransform tempTransform, Geometry displayGeom) {
        Point centroid;
        Polygon geom = this.getPolySetRepresentativeLocation(labelItem.getGeoms(), displayGeom);
        if (geom == null) {
            return geom;
        }
        try {
            centroid = geom.getCentroid();
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
        if (!textStyle.isPointPlacement()) {
            double offset = textStyle.getDisplacementY();
            displacementY = offset > 0.0 ? -offset : (offset < 0.0 ? -offset + textBounds.getHeight() : textBounds.getHeight() / 2.0);
            displacementX = -textBounds.getWidth() / 2.0;
        } else {
            displacementX = textStyle.getAnchorX() + -textBounds.getWidth() / 2.0 + textStyle.getDisplacementX();
            displacementY = textStyle.getAnchorY() + textBounds.getHeight() / 2.0 + textStyle.getDisplacementY();
        }
        tempTransform.rotate(textStyle.getRotation());
        tempTransform.translate(displacementX, displacementY);
        return geom;
    }

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

    protected LineString getLineSetRepresentativeLocation(List<Geometry> geoms, Geometry displayGeometry) {
        ArrayList<LineString> lines = new ArrayList<LineString>();
        for (Geometry g : geoms) {
            if (!(g instanceof LineString) && !(g instanceof MultiLineString) && !(g instanceof Polygon) && !(g instanceof MultiPolygon) || (g instanceof Polygon || g instanceof MultiPolygon) && !((g = g.getBoundary()) instanceof LineString) && !(g instanceof MultiLineString)) continue;
            if (g instanceof LineString) {
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
        for (LineString l : merged) {
            MultiLineString ll = this.clipLineString(l, (Polygon)displayGeometry);
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

    public MultiLineString clipLineString(LineString line, Polygon bbox) {
        LineString clip = line;
        try {
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
        for (Polygon p : polys) {
            MultiPolygon pp = this.clipPolygon(p, (Polygon)displayGeometry);
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

    public MultiPolygon clipPolygon(Polygon poly, Polygon bbox) {
        Polygon clip = poly;
        try {
            clip = EnhancedPrecisionOp.intersection((Geometry)poly, (Geometry)bbox);
        }
        catch (Exception e) {
            clip = poly;
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
        int i = 0;
        while (i < pts.length - 1) {
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

    protected Collection<LineString> mergeLines(Collection<LineString> lines) {
        LineMerger lm = new LineMerger();
        lm.add(lines);
        Collection merged = lm.getMergedLineStrings();
        if (merged.size() == 0) {
            return null;
        }
        if (merged.size() == 1) {
            return merged;
        }
        List<Object> mylines = new ArrayList(merged);
        boolean keep_going = true;
        while (keep_going) {
            keep_going = false;
            Collections.sort(mylines, new LineLengthComparator());
            int t = 0;
            while (t < mylines.size()) {
                LineString major = (LineString)mylines.get(t);
                if (major != null) {
                    int i = t + 1;
                    while (i < mylines.size()) {
                        LineString merge;
                        LineString minor = (LineString)mylines.get(i);
                        if (minor != null && (merge = this.merge(major, minor)) != null) {
                            keep_going = true;
                            mylines.set(i, null);
                            mylines.set(t, merge);
                            major = merge;
                        }
                        ++i;
                    }
                }
                ++t;
            }
            mylines = this.removeNulls(mylines);
        }
        return this.removeNulls(mylines);
    }

    protected List removeNulls(List l) {
        ArrayList al = new ArrayList();
        for (Object o : l) {
            if (o == null) continue;
            al.add(o);
        }
        return al;
    }

    protected LineString reverse(LineString l) {
        List<Coordinate> clist = Arrays.asList(l.getCoordinates());
        Collections.reverse(clist);
        return l.getFactory().createLineString(clist.toArray(new Coordinate[1]));
    }

    protected LineString merge(LineString major, LineString minor) {
        Coordinate major_s = major.getCoordinateN(0);
        Coordinate major_e = major.getCoordinateN(major.getNumPoints() - 1);
        Coordinate minor_s = minor.getCoordinateN(0);
        Coordinate minor_e = minor.getCoordinateN(minor.getNumPoints() - 1);
        if (major_s.equals2D(minor_s)) {
            return this.mergeSimple(this.reverse(minor), major);
        }
        if (major_s.equals2D(minor_e)) {
            return this.mergeSimple(minor, major);
        }
        if (major_e.equals2D(minor_s)) {
            return this.mergeSimple(major, minor);
        }
        if (major_e.equals2D(minor_e)) {
            return this.mergeSimple(major, this.reverse(minor));
        }
        return null;
    }

    protected LineString mergeSimple(LineString l1, LineString l2) {
        ArrayList<Coordinate> clist = new ArrayList<Coordinate>(Arrays.asList(l1.getCoordinates()));
        clist.addAll(Arrays.asList(l2.getCoordinates()));
        return l1.getFactory().createLineString(clist.toArray(new Coordinate[1]));
    }

    @Override
    public void put(String layerId, TextSymbolizer symbolizer, Feature feature, LiteShape2 shape, LiteShape2 originalShape, Range scaleRange, Number textHeight, Color color, Number rotation) {
        this.put(layerId, symbolizer, feature, shape, originalShape, scaleRange, textHeight, color, rotation, 1.0);
    }

    protected Enum getEnumOption(TextSymbolizer symbolizer, String optionName, Enum defaultValue) {
        String value = symbolizer.getOption(optionName);
        if (value == null) {
            return defaultValue;
        }
        try {
            Object enumValue = Enum.valueOf(defaultValue.getDeclaringClass(), value.toUpperCase());
            return enumValue;
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    protected int getIntOption(TextSymbolizer symbolizer, String optionName, int defaultValue) {
        String value = symbolizer.getOption(optionName);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    protected double getDoubleOption(TextSymbolizer symbolizer, String optionName, double defaultValue) {
        String value = symbolizer.getOption(optionName);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    protected boolean getBooleanOption(TextSymbolizer symbolizer, String optionName, boolean defaultValue) {
        String value = symbolizer.getOption(optionName);
        if (value == null) {
            return defaultValue;
        }
        return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1");
    }

    protected int[] getGraphicMargin(TextSymbolizer symbolizer) {
        String value = symbolizer.getOption("graphic-margin");
        if (value == null) {
            return null;
        }
        String[] values = value.trim().split("\\s+");
        if (values.length == 0) {
            return null;
        }
        if (values.length > 4) {
            throw new IllegalArgumentException("The graphic margin is to be specified with 1, 2 or 4 values");
        }
        int[] parsed = new int[values.length];
        int i = 0;
        while (i < parsed.length) {
            parsed[i] = Integer.parseInt(values[i]);
            ++i;
        }
        if (parsed.length == 4) {
            return parsed;
        }
        if (parsed.length == 3) {
            return new int[]{parsed[0], parsed[1], parsed[2], parsed[1]};
        }
        if (parsed.length == 2) {
            return new int[]{parsed[0], parsed[1], parsed[0], parsed[1]};
        }
        return new int[]{parsed[0], parsed[0], parsed[0], parsed[0]};
    }

    public void put(Rectangle2D area) {
        this.reserved.add(area);
    }

    public LabelRenderingMode getLabelRenderingMode() {
        return this.labelRenderingMode;
    }

    public void setLabelRenderingMode(LabelRenderingMode mode) {
        this.labelRenderingMode = mode;
    }

    protected int getClosestStandardAngle(double x, double y) {
        double angle = Math.toDegrees(Math.atan2(y, x));
        return (int)Math.round(angle / 45.0) * 45;
    }

    protected LabelCacheItem buildLabelCacheItem(String layerId, TextSymbolizer symbolizer, Feature feature, LiteShape2 shape, LiteShape2 originalShape, Range scaleRange, Number textHeight, Color color, Number rotation, double factor, String label, double priorityValue) {
        TextStyle2D textStyle = this.getTextStyle2D(feature, symbolizer, scaleRange, color, textHeight, factor);
        if (textStyle == null) {
            return null;
        }
        textStyle.setHaloRadius((float)((double)textStyle.getHaloRadius() * factor));
        LabelCacheItem item = new LabelCacheItem(layerId, textStyle, shape, originalShape, label);
        item.setPriority(priorityValue);
        item.setSpaceAround(this.getIntOption(symbolizer, "spaceAround", 0));
        item.setAutoWrap(this.getIntOption(symbolizer, "autoWrap", 0));
        item.setFollowLineEnabled(this.getBooleanOption(symbolizer, "followLine", false));
        double maxAngleDelta = this.getDoubleOption(symbolizer, "maxAngleDelta", 22.5);
        item.setMaxAngleDelta(maxAngleDelta);
        item.setRepeat(this.getIntOption(symbolizer, "repeat", 0));
        item.setMaxDisplacement(this.getIntOption(symbolizer, "maxDisplacement", 0));
        item.setMinGroupDistance(this.getIntOption(symbolizer, "minGroupDistance", -1));
        item.setLabelAllGroup(this.getBooleanOption(symbolizer, "labelAllGroup", false));
        item.setRemoveGroupOverlaps(this.getBooleanOption(symbolizer, "removeOverlaps", false));
        item.setAllowOverruns(this.getBooleanOption(symbolizer, "allowOverruns", true));
        item.setForceLeftToRightEnabled(this.getBooleanOption(symbolizer, "forceLeftToRight", true));
        item.setConflictResolutionEnabled(!this.overlapping);
        item.setGoodnessOfFit(this.getDoubleOption(symbolizer, "goodnessOfFit", 0.5));
        item.setPolygonAlign((TextSymbolizer.PolygonAlignOptions)this.getEnumOption(symbolizer, "polygonAlign", TextSymbolizer.DEFAULT_POLYGONALIGN));
        item.setGraphicsResize((TextSymbolizer.GraphicResize)this.getEnumOption(symbolizer, "graphic-resize", TextSymbolizer.DEFAULT_GRAPHIC_RESIZE));
        item.setGraphicMargin(this.getGraphicMargin(symbolizer));
        LineToLabel lineToLabel = new LineToLabel();
        lineToLabel.setWidth(this.getDoubleOption(symbolizer, "lineToLabelWidth", 1.0));
        lineToLabel.setColor(this.getColorOption(symbolizer, "lineToLabelColor", TextSymbolizer.DEFAULT_LINE_TO_LABEL_COLOR));
        lineToLabel.setDash(this.getDashOption(symbolizer, "lineToLabelDash", TextSymbolizer.DEFAULT_LINE_TO_LABEL_DASH));
        lineToLabel.setLineToLabelEndingAnchorOption((TextSymbolizer.LineToLabelEndingAnchorOptions)this.getEnumOption(symbolizer, "lineToLabelEndingAnchor", TextSymbolizer.DEFAULT_LINE_TO_LABEL_ENDING_ANCHOR));
        item.setLineToLabel(lineToLabel);
        return item;
    }

    protected TextStyle2D getTextStyle2D(Feature feature, TextSymbolizer symbolizer, Range scaleRange, Color color, Number textHeight, double factor) {
        TextStyle2D textStyle = (TextStyle2D)this.styleFactory.createStyle(feature, symbolizer, scaleRange);
        if (textStyle == null) {
            return null;
        }
        if (textHeight != null) {
            Font newFont = textStyle.getFont().deriveFont((float)((double)textHeight.floatValue() * factor));
            textStyle.setFont(newFont);
        } else {
            Font newFont = textStyle.getFont().deriveFont((float)((double)textStyle.getFont().getSize() * factor));
            textStyle.setFont(newFont);
        }
        if (symbolizer.getHeightAttribute() != null) {
            textStyle.setFontSizeScale(true);
        }
        if (color != null) {
            textStyle.setFill(color);
        }
        return textStyle;
    }

    protected Color getColorOption(TextSymbolizer symbolizer, String optionName, Color defaultValue) {
        String value = symbolizer.getOption(optionName);
        if (value == null) {
            return defaultValue;
        }
        try {
            return ColorUtil.fromHex(value);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    protected float[] getDashOption(TextSymbolizer symbolizer, String optionName, float[] defaultValue) {
        String value = symbolizer.getOption(optionName);
        if (value == null) {
            return defaultValue;
        }
        try {
            return StringUtil.floatArrayFromString(value);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    public static enum LabelRenderingMode {
        STRING,
        OUTLINE,
        ADAPTIVE;

    }

    protected class LineLengthComparator
    implements Comparator<LineString> {
        protected LineLengthComparator() {
        }

        @Override
        public int compare(LineString o1, LineString o2) {
            return Double.compare(o2.getLength(), o1.getLength());
        }
    }
}

