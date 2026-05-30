/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.operation.distance.DistanceOp
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel;
import com.vividsolutions.jump.workbench.ui.snap.SnapPolicy;
import com.vividsolutions.jump.workbench.ui.snap.VisiblePointsAndLinesCache;
import java.awt.Color;
import org.saig.jump.lang.I18N;

public class SnapToFeaturesPolicy
implements SnapPolicy {
    public static final String NAME = I18N.getString("workbench.ui.snap.SnapOptionsPanel.snap-to-vertices-and-lines");
    public static final String ENABLED_KEY = String.valueOf(SnapToFeaturesPolicy.class.getName()) + " - ENABLED";
    private Color color;
    private GeometryFactory factory = new GeometryFactory();
    private Blackboard blackboard;

    public SnapToFeaturesPolicy(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.color = SnapOptionsPanel.getSnapToFeaturesColor();
    }

    public SnapToFeaturesPolicy() {
        this(new Blackboard());
        this.blackboard.put(ENABLED_KEY, true);
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
        double minDistance = Double.MAX_VALUE;
        Coordinate closestCoord = null;
        Point originalPoint = this.factory.createPoint(originalCoordinate);
        Geometry bufferedTransformedCursorLocation = originalPoint.buffer(units);
        if (toSpecificGeometry != null) {
            Geometry candidate = VisiblePointsAndLinesCache.toPointsAndLines(toSpecificGeometry);
            int i = 0;
            while (i < candidate.getNumGeometries()) {
                DistanceOp op;
                double distanceTemp;
                Geometry intersection;
                Geometry geomCand = candidate.getGeometryN(i);
                if (geomCand instanceof LinearRing) {
                    geomCand = this.factory.createLineString(((LinearRing)geomCand).getCoordinates());
                }
                if (!(intersection = geomCand.intersection(bufferedTransformedCursorLocation)).isEmpty() && (distanceTemp = (op = new DistanceOp(intersection, (Geometry)originalPoint)).distance()) < minDistance) {
                    Coordinate[] closestPoints = op.nearestPoints();
                    boolean encontrado = false;
                    int j = 0;
                    while (j < closestPoints.length && !encontrado) {
                        closestCoord = closestPoints[j];
                        minDistance = distanceTemp;
                        encontrado = true;
                        ++j;
                    }
                }
                ++i;
            }
        } else {
            for (Geometry candidate : VisiblePointsAndLinesCache.instance(panel).getTree().query(bufferedTransformedCursorLocation.getEnvelopeInternal())) {
                int i = 0;
                while (i < candidate.getNumGeometries()) {
                    DistanceOp op;
                    double distanceTemp;
                    Geometry intersection;
                    Geometry geomCand = candidate.getGeometryN(i);
                    if (geomCand instanceof LinearRing) {
                        geomCand = this.factory.createLineString(((LinearRing)geomCand).getCoordinates());
                    }
                    if (!(intersection = geomCand.intersection(bufferedTransformedCursorLocation)).isEmpty() && (distanceTemp = (op = new DistanceOp(intersection, (Geometry)originalPoint)).distance()) < minDistance) {
                        Coordinate[] closestPoints = op.nearestPoints();
                        boolean encontrado = false;
                        int j = 0;
                        while (j < closestPoints.length && !encontrado) {
                            closestCoord = closestPoints[j];
                            minDistance = distanceTemp;
                            encontrado = true;
                            ++j;
                        }
                    }
                    ++i;
                }
            }
        }
        this.color = SnapOptionsPanel.getSnapToFeaturesColor();
        return closestCoord;
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

