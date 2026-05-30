/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.geom.prep.PreparedGeometry
 *  com.vividsolutions.jts.geom.prep.PreparedGeometryFactory
 *  com.vividsolutions.jts.operation.linemerge.LineMerger
 *  org.apache.commons.collections.CollectionUtils
 */
package es.kosmo.core.renderer.label;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import es.kosmo.core.renderer.label.LabelIndex;
import es.kosmo.core.renderer.label.LabelPainter;
import es.kosmo.core.renderer.label.LineStringCursor;
import es.kosmo.core.renderer.label.TextStyle2DExt;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.geotools.geometry.jts.GeometryClipper;
import org.saig.core.renderer.lite.LabelCacheDefault2;
import org.saig.core.renderer.lite.LabelCacheItem;
import org.saig.core.renderer.lite.LabelDrawingResult;
import org.saig.core.renderer.style.TextStyle2D;

public class LabelCacheImpl
extends LabelCacheDefault2 {
    protected GeometryClipper clipper;
    protected GeometryFactory gf = new GeometryFactory();
    public static double MIN_CURVED_DELTA = 0.05235987755982988;

    public LabelCacheImpl(boolean overlapping, boolean repeated) {
        super(overlapping, repeated);
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
        LabelPainter painter = this.buildLabelPainter(graphics);
        for (LabelCacheItem labelItem : items) {
            if (this.stop) {
                return;
            }
            painter.setLabel(labelItem);
            try {
                AffineTransform tempTransform = new AffineTransform();
                Geometry geom = labelItem.getGeometry();
                if (geom instanceof Point || geom instanceof MultiPoint) {
                    this.paintPointLabel(painter, tempTransform, displayArea, glyphs);
                    continue;
                }
                if (geom instanceof LineString && !(geom instanceof LinearRing) || geom instanceof MultiLineString) {
                    this.paintLineLabel(painter, tempTransform, displayArea, glyphs);
                    continue;
                }
                if (!(geom instanceof Polygon) && !(geom instanceof MultiPolygon) && !(geom instanceof LinearRing)) continue;
                this.paintPolygonLabel(painter, tempTransform, displayArea, glyphs);
            }
            catch (Exception e) {
                System.out.println("Issues painting " + labelItem.getLabel());
                e.printStackTrace();
            }
        }
    }

    protected LabelPainter buildLabelPainter(Graphics2D graphics) {
        return new LabelPainter(graphics, this.labelRenderingMode);
    }

    protected LabelDrawingResult paintPointLabel(LabelPainter painter, AffineTransform tempTransform, Rectangle displayArea, LabelIndex glyphs) throws Exception {
        int startAngle;
        LabelCacheItem labelItem = painter.getLabel();
        Point point = this.getPointSetRepresentativeLocation(labelItem.getGeoms(), displayArea);
        if (point == null) {
            return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OUTSCREEN);
        }
        TextStyle2D ts = labelItem.getTextStyle();
        double step = painter.getAscent() > 2.0 ? painter.getAscent() : 2.0;
        double radius = Math.sqrt(ts.getDisplacementX() * ts.getDisplacementX() + ts.getDisplacementY() * ts.getDisplacementY());
        AffineTransform tx = new AffineTransform(tempTransform);
        LabelDrawingResult result = this.paintPointLabelInternal(painter, tx, displayArea, glyphs, labelItem, point, ts);
        if (result.getType() == LabelDrawingResult.LabelDrawingResultType.SUCCEDED) {
            return result;
        }
        TextStyle2D cloned = new TextStyle2D(ts);
        int angle = startAngle = this.getClosestStandardAngle(ts.getDisplacementX(), ts.getDisplacementY());
        while (radius <= (double)labelItem.getMaxDisplacement()) {
            int offset = 45;
            while (offset <= 360) {
                double dx = radius * Math.cos(Math.toRadians(angle));
                double dy = radius * Math.sin(Math.toRadians(angle));
                int normAngle = angle % 360;
                if (normAngle < 0) {
                    normAngle += 360;
                }
                double[] anchorPointCandidates = normAngle < 90 || normAngle > 270 ? RIGHT_ANCHOR_CANDIDATES : (normAngle > 90 && normAngle < 270 ? LEFT_ANCHOR_CANDIDATES : MID_ANCHOR_CANDIDATES);
                int i = 0;
                while (i < anchorPointCandidates.length) {
                    double ax = anchorPointCandidates[i];
                    double ay = anchorPointCandidates[i + 1];
                    cloned.setAnchorX(ax);
                    cloned.setAnchorY(ay);
                    cloned.setDisplacementX(dx);
                    cloned.setDisplacementY(dy);
                    tx = new AffineTransform(tempTransform);
                    result = this.paintPointLabelInternal(painter, tx, displayArea, glyphs, labelItem, point, cloned);
                    if (result.getType().equals((Object)LabelDrawingResult.LabelDrawingResultType.SUCCEDED)) {
                        return result;
                    }
                    i += 2;
                }
                angle = angle <= startAngle ? (angle += offset) : (angle -= offset);
                offset += 45;
            }
            radius += step;
        }
        return result;
    }

    Point getPointSetRepresentativeLocation(List<Geometry> geoms, Rectangle displayArea) {
        ArrayList<Point> pts = new ArrayList<Point>();
        for (Geometry g : geoms) {
            if (!(g instanceof Point) && !(g instanceof MultiPoint)) {
                g = g.getCentroid();
            }
            if (g instanceof Point) {
                Point point = (Point)g;
                if (!displayArea.contains(point.getX(), point.getY())) continue;
                pts.add(point);
                continue;
            }
            if (!(g instanceof MultiPoint)) continue;
            int t = 0;
            while (t < g.getNumGeometries()) {
                Point gg = (Point)g.getGeometryN(t);
                if (displayArea.contains(gg.getX(), gg.getY())) {
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

    private LabelDrawingResult paintPointLabelInternal(LabelPainter painter, AffineTransform tempTransform, Rectangle displayArea, LabelIndex glyphs, LabelCacheItem labelItem, Point point, TextStyle2D textStyle) throws Exception {
        this.setupPointTransform(tempTransform, point, textStyle, painter);
        Rectangle2D transformed = tempTransform.createTransformedShape(painter.getFullLabelBounds()).getBounds2D();
        if (!displayArea.contains(transformed)) {
            return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OUTSCREEN);
        }
        if (labelItem.isConflictResolutionEnabled() && glyphs.labelsWithinDistance(transformed, labelItem.getSpaceAround())) {
            return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OVERLAPPED);
        }
        painter.setAnchorPoint(point);
        painter.paintStraightLabel(tempTransform);
        if (labelItem.isConflictResolutionEnabled()) {
            glyphs.addLabel(labelItem, transformed);
        }
        return new LabelDrawingResult((Geometry)point, LabelDrawingResult.LabelDrawingResultType.SUCCEDED);
    }

    private void setupPointTransform(AffineTransform tempTransform, Point centroid, TextStyle2D textStyle, LabelPainter painter) {
        tempTransform.translate(centroid.getX(), centroid.getY());
        double rotation = textStyle.getRotation();
        if (Double.isNaN(rotation) || Double.isInfinite(rotation)) {
            rotation = 0.0;
        }
        tempTransform.rotate(rotation);
        Rectangle2D textBounds = painter.getLabelBounds();
        double displacementX = textStyle.getAnchorX() * -textBounds.getWidth() + textStyle.getDisplacementX();
        double displacementY = textStyle.getAnchorY() * textBounds.getHeight() - textStyle.getDisplacementY() - textBounds.getHeight() + painter.getLineHeight();
        tempTransform.translate(displacementX, displacementY);
        textStyle.setTextBounds(textBounds);
        textStyle.setLineHeight(painter.getLineHeight());
    }

    protected LabelDrawingResult paintLineLabel(LabelPainter painter, AffineTransform originalTransform, Rectangle displayArea, LabelIndex paintedBounds) throws Exception {
        LabelCacheItem labelItem = painter.getLabel();
        List<LineString> lines = this.getLineSetRepresentativeLocation(labelItem.getGeoms(), displayArea, labelItem.removeGroupOverlaps());
        if (CollectionUtils.isEmpty(lines)) {
            return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OUTSCREEN);
        }
        if (!labelItem.labelAllGroup() && lines.size() > 1) {
            lines = Collections.singletonList(lines.get(0));
        }
        Rectangle2D textBounds = painter.getFullLabelBounds();
        double step = painter.getAscent() > 2.0 ? painter.getAscent() : 2.0;
        int space = labelItem.getSpaceAround();
        int haloRadius = Math.round(labelItem.getTextStyle().getHaloFill() != null ? labelItem.getTextStyle().getHaloRadius() : 0.0f);
        int extraSpace = space + haloRadius;
        int labelDistance = labelItem.getRepeat();
        int minDistance = labelItem.getMinGroupDistance();
        LabelIndex groupLabels = new LabelIndex();
        double labelOffset = labelItem.getMaxDisplacement();
        boolean allowOverruns = labelItem.allowOverruns();
        double maxAngleDeltaInRadians = Math.toRadians(labelItem.getMaxAngleDelta());
        int labelCount = 0;
        LabelDrawingResult result = null;
        for (LineString line : lines) {
            int i;
            double[] labelPositions;
            if (labelItem.isFollowLineEnabled()) {
                line = this.decimateLineString(line, step);
            }
            double lineStringLength = line.getLength();
            if ((!allowOverruns || labelItem.isFollowLineEnabled()) && line.getLength() < textBounds.getWidth()) {
                return labelCount > 0 ? new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OVERLAPPED) : new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OUTSCREEN);
            }
            if (labelDistance > 0 && (double)labelDistance < lineStringLength / 2.0) {
                labelPositions = new double[(int)(lineStringLength / (double)labelDistance)];
                labelPositions[0] = lineStringLength / 2.0;
                double offset = labelDistance;
                i = 1;
                while (i < labelPositions.length) {
                    labelPositions[i] = labelPositions[i - 1] + offset;
                    double signum = Math.signum(offset);
                    offset = -1.0 * signum * (Math.abs(offset) + (double)labelDistance);
                    ++i;
                }
            } else {
                labelPositions = new double[]{lineStringLength / 2.0};
            }
            LineStringCursor cursor = new LineStringCursor(line);
            AffineTransform tx = new AffineTransform();
            i = 0;
            while (i < labelPositions.length) {
                cursor.moveTo(labelPositions[i]);
                Coordinate centroid = cursor.getCurrentPosition();
                LineString cut = null;
                double currOffset = 0.0;
                boolean painted = false;
                while (Math.abs(currOffset) <= labelOffset * 2.0 && !painted) {
                    boolean drawLine;
                    Rectangle2D labelEnvelope;
                    tx.setToIdentity();
                    double maxAngleChange = 0.0;
                    double startOrdinate = cursor.getCurrentOrdinate() - textBounds.getWidth() / 2.0;
                    double endOrdinate = cursor.getCurrentOrdinate() + textBounds.getWidth() / 2.0;
                    if (labelItem.isFollowLineEnabled()) {
                        maxAngleChange = cursor.getMaxAngleChange(startOrdinate, endOrdinate);
                        cut = cursor.getSubLineString(startOrdinate, endOrdinate);
                        if (maxAngleChange < MIN_CURVED_DELTA) {
                            this.setupLineTransform(painter, cursor, centroid, tx, true);
                            labelEnvelope = tx.createTransformedShape(textBounds).getBounds2D();
                        } else {
                            labelEnvelope = this.getCurvedLabelBounds(cut, textBounds.getHeight() / 2.0);
                            painter.getLabel().getTextStyle().setTextBounds(labelEnvelope);
                        }
                    } else {
                        this.setupLineTransform(painter, cursor, centroid, tx, false);
                        labelEnvelope = tx.createTransformedShape(textBounds).getBounds2D();
                    }
                    boolean isInsideDisplayArea = displayArea.contains(labelEnvelope);
                    boolean bl = drawLine = isInsideDisplayArea && (!labelItem.isConflictResolutionEnabled() || !paintedBounds.labelsWithinDistance(labelEnvelope, extraSpace)) && !groupLabels.labelsWithinDistance(labelEnvelope, minDistance);
                    if (drawLine) {
                        if (labelItem.isFollowLineEnabled()) {
                            if (startOrdinate > 0.0 && endOrdinate <= cursor.getLineStringLength() && maxAngleChange < maxAngleDeltaInRadians) {
                                if (maxAngleChange < MIN_CURVED_DELTA) {
                                    painter.setAnchorPoint(this.gf.createPoint(cursor.getCurrentPosition()));
                                    painter.paintStraightLabel(tx);
                                    if (result == null) {
                                        result = new LabelDrawingResult((Geometry)cut, LabelDrawingResult.LabelDrawingResultType.SUCCEDED);
                                    } else {
                                        result.addAnchorPoint((Geometry)cut);
                                    }
                                } else {
                                    painter.paintCurvedLabel(cursor);
                                    if (result == null) {
                                        result = new LabelDrawingResult((Geometry)cut, LabelDrawingResult.LabelDrawingResultType.SUCCEDED);
                                    } else {
                                        result.addAnchorPoint((Geometry)cut);
                                    }
                                }
                                painted = true;
                            }
                        } else if (allowOverruns || startOrdinate > 0.0 && endOrdinate <= cursor.getLineStringLength()) {
                            Point anchorPoint = this.gf.createPoint(cursor.getCurrentPosition());
                            painter.setAnchorPoint(anchorPoint);
                            painter.paintStraightLabel(tx);
                            painted = true;
                            if (result == null) {
                                result = new LabelDrawingResult((Geometry)anchorPoint, LabelDrawingResult.LabelDrawingResultType.SUCCEDED);
                            } else {
                                result.addAnchorPoint((Geometry)anchorPoint);
                            }
                        }
                    } else if (result != null) {
                        LabelDrawingResult labelDrawingResult = result = isInsideDisplayArea ? new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OVERLAPPED) : new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OUTSCREEN);
                    }
                    if (painted) {
                        ++labelCount;
                        groupLabels.addLabel(labelItem, labelEnvelope);
                        if (!labelItem.isConflictResolutionEnabled()) continue;
                        paintedBounds.addLabel(labelItem, labelEnvelope);
                        continue;
                    }
                    double signum = Math.signum(currOffset);
                    currOffset = signum == 0.0 ? step : -1.0 * signum * (Math.abs(currOffset) + step);
                    cursor.moveRelative(currOffset);
                    cursor.getCurrentPosition(centroid);
                }
                ++i;
            }
        }
        if (result == null) {
            result = new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OUTSCREEN);
        }
        return result;
    }

    List<LineString> getLineSetRepresentativeLocation(List<Geometry> geoms, Rectangle displayArea, boolean removeOverlaps) {
        ArrayList<LineString> lines = new ArrayList<LineString>();
        for (Geometry g : geoms) {
            this.accumulateLineStrings(g, lines);
        }
        if (lines.size() == 0) {
            return null;
        }
        ArrayList<LineString> clippedLines = new ArrayList<LineString>();
        for (LineString ls : lines) {
            MultiLineString ll = this.clipLineString(ls);
            if (ll == null || ll.isEmpty()) continue;
            int t = 0;
            while (t < ll.getNumGeometries()) {
                clippedLines.add((LineString)ll.getGeometryN(t));
                ++t;
            }
        }
        if (removeOverlaps) {
            ArrayList<LineString> cleanedLines = new ArrayList<LineString>();
            ArrayList<Geometry> bufferCache = new ArrayList<Geometry>();
            Iterator iterator = clippedLines.iterator();
            while (iterator.hasNext()) {
                LineString ls;
                LineString g = ls = (LineString)iterator.next();
                int i = 0;
                while (i < cleanedLines.size()) {
                    LineString cleaned = (LineString)cleanedLines.get(i);
                    if (g.getEnvelopeInternal().intersects(cleaned.getEnvelopeInternal())) {
                        Geometry buffer = (Geometry)bufferCache.get(i);
                        if (buffer == null) {
                            buffer = cleaned.buffer(2.0);
                            bufferCache.set(i, buffer);
                        }
                        g = g.difference(buffer);
                    }
                    ++i;
                }
                int added = this.accumulateLineStrings((Geometry)g, cleanedLines);
                int i2 = 0;
                while (i2 < added) {
                    bufferCache.add(null);
                    ++i2;
                }
            }
            clippedLines = cleanedLines;
        }
        if (clippedLines == null || clippedLines.size() == 0) {
            return null;
        }
        Collection merged = this.mergeLines(clippedLines);
        if (merged.size() == 0) {
            return null;
        }
        Collections.sort(merged, new LineLengthComparator());
        return merged;
    }

    private int accumulateLineStrings(Geometry g, List<LineString> lines) {
        if (!(g instanceof LineString || g instanceof MultiLineString || g instanceof Polygon || g instanceof MultiPolygon)) {
            return 0;
        }
        if ((g instanceof Polygon || g instanceof MultiPolygon) && !((g = g.getBoundary()) instanceof LineString) && !(g instanceof MultiLineString)) {
            return 0;
        }
        if (g instanceof LineString) {
            if (g.getLength() != 0.0) {
                lines.add((LineString)g);
                return 1;
            }
            return 0;
        }
        if (g instanceof MultiLineString) {
            int t = 0;
            while (t < g.getNumGeometries()) {
                LineString gg = (LineString)g.getGeometryN(t);
                lines.add(gg);
                ++t;
            }
            return g.getNumGeometries();
        }
        int count = 0;
        int t = 0;
        while (t < g.getNumGeometries()) {
            count += this.accumulateLineStrings(g.getGeometryN(t), lines);
            ++t;
        }
        return count;
    }

    private LineString decimateLineString(LineString line, double step) {
        Coordinate[] inputCoordinates = line.getCoordinates();
        ArrayList<Coordinate> simplified = new ArrayList<Coordinate>();
        Coordinate prev = inputCoordinates[0];
        simplified.add(prev);
        int i = 1;
        while (i < inputCoordinates.length) {
            Coordinate curr = inputCoordinates[i];
            if (Math.abs(curr.x - prev.x) > step || Math.abs(curr.y - prev.y) > step) {
                simplified.add(curr);
                prev = curr;
            }
            ++i;
        }
        if (simplified.size() == 1) {
            simplified.add(inputCoordinates[inputCoordinates.length - 1]);
        }
        Coordinate[] newCoords = simplified.toArray(new Coordinate[simplified.size()]);
        return line.getFactory().createLineString(newCoords);
    }

    private void setupLineTransform(LabelPainter painter, LineStringCursor cursor, Coordinate centroid, AffineTransform tempTransform, boolean followLine) {
        double rotation;
        tempTransform.translate(centroid.x, centroid.y);
        TextStyle2D textStyle = painter.getLabel().getTextStyle();
        double anchorX = textStyle.getAnchorX();
        double anchorY = textStyle.getAnchorY();
        double displacementX = 0.0;
        double displacementY = 0.0;
        if (textStyle.isPointPlacement() && !followLine) {
            rotation = textStyle.getRotation();
        } else {
            rotation = painter.getLabel().isForceLeftToRightEnabled() ? cursor.getLabelOrientation() : cursor.getCurrentAngle();
            displacementY -= (double)textStyle.getPerpendicularOffset();
            anchorX = 0.5;
            anchorY = painter.getLinePlacementYAnchor();
        }
        Rectangle2D textBounds = painter.getLabelBounds();
        displacementX = anchorX * -textBounds.getWidth() + textStyle.getDisplacementX();
        displacementY += anchorY * textBounds.getHeight() - textStyle.getDisplacementY();
        if (Double.isNaN(rotation) || Double.isInfinite(rotation)) {
            rotation = 0.0;
        }
        tempTransform.rotate(rotation);
        tempTransform.translate(displacementX, displacementY);
        textStyle.setRotation(rotation);
        textStyle.setTextBounds(textBounds);
        textStyle.setLineHeight(painter.getLineHeight());
        textStyle.setLinePlacementYAnchor(painter.getLinePlacementYAnchor());
    }

    private void setupPolygonTransform(AffineTransform tempTransform, Point centroid, TextStyle2D textStyle, LabelPainter painter) {
        tempTransform.translate(centroid.getX(), centroid.getY());
        double rotation = textStyle.getRotation();
        if (Double.isNaN(rotation) || Double.isInfinite(rotation)) {
            rotation = 0.0;
        }
        tempTransform.rotate(rotation);
        Rectangle2D textBounds = painter.getLabelBounds();
        double displacementX = textStyle.getAnchorX() * -textBounds.getWidth() + textStyle.getDisplacementX();
        double displacementY = textStyle.getAnchorY() * textBounds.getHeight() - textStyle.getDisplacementY() - textBounds.getHeight() + painter.getLineHeight();
        tempTransform.translate(displacementX, displacementY);
        textStyle.setTextBounds(textBounds);
        textStyle.setLineHeight(painter.getLineHeight());
    }

    private Rectangle2D getCurvedLabelBounds(LineString line, double bufferSize) {
        Envelope e = line.getEnvelopeInternal();
        e.expandBy(bufferSize);
        return new Rectangle2D.Double(e.getMinX(), e.getMinY(), e.getWidth(), e.getHeight());
    }

    protected List<LineString> mergeLines(Collection<LineString> lines) {
        LineMerger lm = new LineMerger();
        lm.add(lines);
        ArrayList<LineString> merged = new ArrayList<LineString>(lm.getMergedLineStrings());
        if (merged.size() == 0) {
            return null;
        }
        if (merged.size() == 1) {
            return merged;
        }
        HashMap<Coordinate, List<LineString>> nodes = new HashMap<Coordinate, List<LineString>>(merged.size() * 2);
        for (LineString ls : merged) {
            this.putInNodeHash(ls.getCoordinateN(0), ls, nodes);
            this.putInNodeHash(ls.getCoordinateN(ls.getNumPoints() - 1), ls, nodes);
        }
        ArrayList<LineString> merged_list = new ArrayList<LineString>(merged);
        Collections.sort(merged_list, this.lineLengthComparator);
        return this.processNodes(merged_list, nodes);
    }

    public List<LineString> processNodes(List<LineString> edges, Map<Coordinate, List<LineString>> nodes) {
        ArrayList<LineString> result = new ArrayList<LineString>();
        int index = 0;
        while (index < edges.size()) {
            LineString ls2;
            LineString ls = edges.get(index);
            Coordinate key = ls.getCoordinateN(0);
            List<LineString> nodeList = nodes.get(key);
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
            List<LineString> nodeList2 = nodes.get(key2);
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
            this.putInNodeHash(ls.getCoordinateN(0), ls, nodes);
            this.putInNodeHash(ls.getCoordinateN(ls.getNumPoints() - 1), ls, nodes);
        }
        return result;
    }

    public void removeFromHash(Map<Coordinate, List<LineString>> nodes, LineString ls) {
        Coordinate key = ls.getCoordinateN(0);
        List<LineString> nodeList = nodes.get(key);
        if (nodeList != null) {
            nodeList.remove(ls);
        }
        if ((nodeList = nodes.get(key = ls.getCoordinateN(ls.getNumPoints() - 1))) != null) {
            nodeList.remove(ls);
        }
    }

    private void putInNodeHash(Coordinate node, LineString ls, Map<Coordinate, List<LineString>> nodes) {
        List<LineString> nodeList = nodes.get(node);
        if (nodeList == null) {
            nodeList = new ArrayList<LineString>();
            nodeList.add(ls);
            nodes.put(node, nodeList);
        } else {
            nodeList.add(ls);
        }
    }

    public MultiLineString clipLineString(LineString line) {
        Geometry g;
        block5: {
            LineString clip = line;
            line.geometryChanged();
            if (this.clipper.getBounds().contains(line.getEnvelopeInternal())) {
                LineString[] lns = new LineString[]{clip};
                return line.getFactory().createMultiLineString(lns);
            }
            try {
                g = this.clipper.clip((Geometry)line, false);
                if (g != null) break block5;
                return null;
            }
            catch (Exception e) {
                return line.getFactory().createMultiLineString(new LineString[]{line});
            }
        }
        if (g instanceof LineString) {
            return line.getFactory().createMultiLineString(new LineString[]{(LineString)g});
        }
        return (MultiLineString)g;
    }

    protected LabelDrawingResult paintPolygonLabel(LabelPainter painter, AffineTransform tempTransform, Rectangle displayArea, LabelIndex glyphs) throws Exception {
        double step;
        AffineTransform tx;
        LabelDrawingResult result;
        Point centroid;
        LabelCacheItem labelItem = painter.getLabel();
        Polygon geom = this.getPolySetRepresentativeLocation(labelItem.getGeoms(), displayArea);
        if (geom == null) {
            return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OUTSCREEN);
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
                    return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OUTSCREEN);
                }
            }
        }
        PreparedGeometry pg = PreparedGeometryFactory.prepare((Geometry)geom);
        if (!pg.contains((Geometry)centroid)) {
            Envelope env = geom.getEnvelopeInternal();
            double step2 = 5.0;
            int steps = (int)Math.round((env.getMaxX() - env.getMinX()) / step2);
            Coordinate c = new Coordinate();
            Point pp = this.gf.createPoint(c);
            c.y = centroid.getY();
            int max = -1;
            int maxIdx = -1;
            int containCounter = -1;
            int i = 0;
            while (i < steps) {
                c.x = env.getMinX() + step2 * (double)i;
                pp.geometryChanged();
                if (!pg.contains((Geometry)pp)) {
                    containCounter = 0;
                } else if (i == 0) {
                    containCounter = 1;
                } else if (++containCounter > max) {
                    max = containCounter;
                    maxIdx = i;
                }
                ++i;
            }
            if (maxIdx != -1) {
                int midIdx = max > 1 ? maxIdx - max / 2 : maxIdx;
                c.x = env.getMinX() + step2 * (double)midIdx;
                pp.geometryChanged();
                centroid = pp;
            } else {
                return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OUTSCREEN);
            }
        }
        TextStyle2DExt textStyle = new TextStyle2DExt(labelItem);
        if (labelItem.getMaxDisplacement() > 0) {
            textStyle.setDisplacementX(0.0);
            textStyle.setDisplacementY(0.0);
            textStyle.setAnchorX(0.5);
            textStyle.setAnchorY(0.5);
        }
        if ((result = this.paintPolygonLabelInternal(painter, tx = new AffineTransform(tempTransform), displayArea, glyphs, labelItem, pg, centroid, textStyle)).getType().equals((Object)LabelDrawingResult.LabelDrawingResultType.SUCCEDED)) {
            return result;
        }
        double radius = step = painter.getAscent() > 2.0 ? painter.getAscent() : 2.0;
        Coordinate c = new Coordinate(centroid.getCoordinate());
        Coordinate cc = centroid.getCoordinate();
        Point testPoint = centroid.getFactory().createPoint(c);
        while (radius < (double)labelItem.getMaxDisplacement()) {
            int angle = 0;
            while (angle < 360) {
                double dx = Math.cos(Math.toRadians(angle)) * radius;
                double dy = Math.sin(Math.toRadians(angle)) * radius;
                c.x = cc.x + dx;
                c.y = cc.y + dy;
                testPoint.geometryChanged();
                if (pg.contains((Geometry)testPoint)) {
                    textStyle.setDisplacementX(dx);
                    textStyle.setDisplacementY(dy);
                    tx = new AffineTransform(tempTransform);
                    result = this.paintPolygonLabelInternal(painter, tx, displayArea, glyphs, labelItem, pg, centroid, textStyle);
                    if (result.getType().equals((Object)LabelDrawingResult.LabelDrawingResultType.SUCCEDED)) {
                        return result;
                    }
                }
                angle += 45;
            }
            radius += step;
        }
        return result;
    }

    Polygon getPolySetRepresentativeLocation(List<Geometry> geoms, Rectangle displayArea) {
        ArrayList<Polygon> polys = new ArrayList<Polygon>();
        Geometry displayGeometry = this.gf.toGeometry(this.toEnvelope(displayArea));
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
        Envelope displayGeomEnv = displayGeometry.getEnvelopeInternal();
        for (Polygon p : polys) {
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
            double area = cpoly.getArea();
            if (area > maxSize) {
                maxPoly = cpoly;
                maxSize = area;
            }
            ++t;
        }
        if (maxSize > 0.0) {
            return maxPoly;
        }
        return null;
    }

    private Envelope toEnvelope(Rectangle2D bounds) {
        return new Envelope(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
    }

    private LabelDrawingResult paintPolygonLabelInternal(LabelPainter painter, AffineTransform tempTransform, Rectangle displayArea, LabelIndex glyphs, LabelCacheItem labelItem, PreparedGeometry pg, Point centroid, TextStyle2DExt textStyle) throws Exception {
        AffineTransform original = new AffineTransform(tempTransform);
        this.setupPolygonTransform(tempTransform, centroid, textStyle, painter);
        Rectangle2D transformed = tempTransform.createTransformedShape(painter.getFullLabelBounds()).getBounds2D();
        if (!displayArea.contains(transformed) || labelItem.isConflictResolutionEnabled() && glyphs.labelsWithinDistance(transformed, labelItem.getSpaceAround())) {
            if (textStyle.flipRotation(pg.getGeometry())) {
                tempTransform.setTransform(original);
                this.setupPointTransform(tempTransform, centroid, textStyle, painter);
                transformed = tempTransform.createTransformedShape(painter.getFullLabelBounds()).getBounds2D();
                if (!displayArea.contains(transformed)) {
                    textStyle.flipRotation(pg.getGeometry());
                    return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OUTSCREEN);
                }
                if (labelItem.isConflictResolutionEnabled() && glyphs.labelsWithinDistance(transformed, labelItem.getSpaceAround())) {
                    textStyle.flipRotation(pg.getGeometry());
                    return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OVERLAPPED);
                }
            } else {
                if (!displayArea.contains(transformed)) {
                    return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OUTSCREEN);
                }
                return new LabelDrawingResult(LabelDrawingResult.LabelDrawingResultType.OVERLAPPED);
            }
        }
        painter.setAnchorPoint(centroid);
        painter.paintStraightLabel(tempTransform);
        if (labelItem.isConflictResolutionEnabled()) {
            glyphs.addLabel(labelItem, transformed);
        }
        return new LabelDrawingResult((Geometry)centroid, LabelDrawingResult.LabelDrawingResultType.SUCCEDED);
    }

    private double goodnessOfFit(LabelPainter painter, AffineTransform transform, PreparedGeometry representativeGeom) {
        if (representativeGeom.getGeometry() instanceof Point) {
            return 1.0;
        }
        if (representativeGeom.getGeometry() instanceof LineString) {
            return 1.0;
        }
        if (representativeGeom.getGeometry() instanceof Polygon) {
            Rectangle2D glyphBounds = painter.getFullLabelBounds();
            try {
                int count = 0;
                int n = 10;
                Coordinate c = new Coordinate();
                Point pp = this.gf.createPoint(c);
                double[] gp = new double[2];
                double[] tp = new double[2];
                int i = 1;
                while (i < painter.getLineCount() + 1) {
                    gp[1] = glyphBounds.getY() + glyphBounds.getHeight() * ((double)i / (double)(painter.getLineCount() + 1));
                    int j = 1;
                    while (j < n + 1) {
                        gp[0] = glyphBounds.getX() + glyphBounds.getWidth() * ((double)j / (double)(n + 1));
                        transform.transform(gp, 0, tp, 0, 1);
                        c.x = tp[0];
                        c.y = tp[1];
                        pp.geometryChanged();
                        if (representativeGeom.contains((Geometry)pp)) {
                            ++count;
                        }
                        ++j;
                    }
                    ++i;
                }
                return (double)count / (double)(n * painter.getLineCount());
            }
            catch (Exception e) {
                Geometry g = representativeGeom.getGeometry();
                g.geometryChanged();
                Envelope ePoly = g.getEnvelopeInternal();
                Envelope eglyph = this.toEnvelope(transform.createTransformedShape(glyphBounds).getBounds2D());
                Envelope inter = this.intersection(ePoly, eglyph);
                if (inter != null) {
                    return inter.getWidth() * inter.getHeight() / (eglyph.getWidth() * eglyph.getHeight());
                }
                return 0.0;
            }
        }
        return 0.0;
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

    private final class LineLengthComparator
    implements Comparator<LineString> {
        private LineLengthComparator() {
        }

        @Override
        public int compare(LineString o1, LineString o2) {
            return Double.compare(o2.getLength(), o1.getLength());
        }
    }
}

