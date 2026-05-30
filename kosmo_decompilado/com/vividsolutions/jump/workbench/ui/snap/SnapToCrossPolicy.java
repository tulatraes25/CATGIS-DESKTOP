/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.Point
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel;
import com.vividsolutions.jump.workbench.ui.snap.SnapPolicy;
import com.vividsolutions.jump.workbench.ui.snap.VisiblePointsAndLinesCache;
import java.awt.Color;
import java.util.List;
import org.saig.jump.lang.I18N;

public class SnapToCrossPolicy
implements SnapPolicy {
    private static final int WARNING_MESSAGE_GAP_TIME = 20000;
    private static final int MAX_PROCESSING_TIME_IN_MS = 150;
    public static final String NAME = I18N.getString("com.vividsolutions.jump.workbench.ui.snap.SnapToCrossPolicy.Snap-to-intersections");
    public static final String ENABLED_KEY = String.valueOf(SnapToCrossPolicy.class.getName()) + " - ENABLED";
    public static long lastWarnTime = 0L;
    private Color color;
    private GeometryFactory factory = new GeometryFactory();
    private Blackboard blackboard;

    public SnapToCrossPolicy(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.color = SnapOptionsPanel.getSnapToCentroidColor();
    }

    public SnapToCrossPolicy() {
        this(new Blackboard());
        this.blackboard.put(ENABLED_KEY, true);
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public Coordinate snap(LayerViewPanel panel, Coordinate originalCoordinate, Geometry toSpecificGeometry) {
        if (!PersistentBlackboardPlugIn.get(this.blackboard).get(ENABLED_KEY, false)) {
            return null;
        }
        Object value = PersistentBlackboardPlugIn.get(this.blackboard).get("SNAP_BUFFER");
        double units = 10.0;
        if (value != null) {
            units = (Double)value;
        }
        double minDistance = units;
        Coordinate closestCoord = null;
        Point originalPoint = this.factory.createPoint(originalCoordinate);
        Geometry bufferedTransformedCursorLocation = originalPoint.buffer(units);
        if (toSpecificGeometry != null) {
            List query = VisiblePointsAndLinesCache.instance(panel).getTree().query(bufferedTransformedCursorLocation.getEnvelopeInternal());
            Geometry cand1 = VisiblePointsAndLinesCache.toPointsAndLines(toSpecificGeometry);
            query.remove(cand1);
            if (cand1.distance((Geometry)originalPoint) < minDistance) {
                for (Geometry cand2 : query) {
                    int i = 0;
                    while (i < cand2.getNumGeometries()) {
                        Geometry intersection;
                        Geometry currentGeom = cand2.getGeometryN(i);
                        if (currentGeom.distance((Geometry)originalPoint) < minDistance && !(intersection = cand1.intersection(currentGeom)).isEmpty()) {
                            Coordinate[] coordinates;
                            Coordinate[] coordinateArray = coordinates = intersection.getCoordinates();
                            int n = coordinates.length;
                            int n2 = 0;
                            while (n2 < n) {
                                Coordinate coord = coordinateArray[n2];
                                double candDistance = coord.distance(originalCoordinate);
                                if (candDistance < units && candDistance < minDistance) {
                                    minDistance = candDistance;
                                    closestCoord = coord;
                                }
                                ++n2;
                            }
                        }
                        ++i;
                    }
                }
            }
        } else {
            List query = VisiblePointsAndLinesCache.instance(panel).getTree().query(bufferedTransformedCursorLocation.getEnvelopeInternal());
            long t = System.currentTimeMillis();
            Geometry[] geoms = new Geometry[query.size()];
            geoms = query.toArray(geoms);
            int k = 0;
            boolean aborted = false;
            int i = 0;
            while (i < geoms.length && !aborted) {
                Geometry cand1 = geoms[i];
                if (cand1.distance((Geometry)originalPoint) < minDistance) {
                    int j = i + 1;
                    while (j < geoms.length && !aborted) {
                        long currentTimeMillis;
                        Geometry cand2 = geoms[j];
                        if (cand2.distance((Geometry)originalPoint) < minDistance) {
                            Geometry intersection = cand1.intersection(cand2);
                            ++k;
                            if (!intersection.isEmpty()) {
                                Coordinate[] coordinates;
                                Coordinate[] coordinateArray = coordinates = intersection.getCoordinates();
                                int n = coordinates.length;
                                int n3 = 0;
                                while (n3 < n) {
                                    Coordinate coord = coordinateArray[n3];
                                    double candDistance = coord.distance(originalCoordinate);
                                    if (candDistance < units && candDistance < minDistance) {
                                        minDistance = candDistance;
                                        closestCoord = coord;
                                    }
                                    ++n3;
                                }
                            }
                        }
                        if ((currentTimeMillis = System.currentTimeMillis()) - t > 150L) {
                            aborted = true;
                            if (currentTimeMillis - lastWarnTime > 20000L) {
                                JUMPWorkbench.getFrameInstance().warnUser(I18N.getString("com.vividsolutions.jump.workbench.ui.snap.SnapToCrossPolicy.The-snap-buffer-is-too-high-for-the-intersection-snap-try-to-reduce-it"));
                                lastWarnTime = currentTimeMillis;
                            }
                        }
                        ++j;
                    }
                }
                ++i;
            }
        }
        this.color = SnapOptionsPanel.getSnapToCrossColor();
        return closestCoord;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

