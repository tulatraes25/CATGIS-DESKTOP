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
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel;
import com.vividsolutions.jump.workbench.ui.snap.SnapPolicy;
import com.vividsolutions.jump.workbench.ui.snap.VisiblePointsAndLinesCache;
import java.awt.Color;
import org.saig.jump.lang.I18N;

public class SnapToCentroidPolicy
implements SnapPolicy {
    public static final String NAME = I18N.getString("com.vividsolutions.jump.workbench.ui.snap.SnapToCentroidPolicy.snap-to-centroids");
    public static final String ENABLED_KEY = String.valueOf(SnapToCentroidPolicy.class.getName()) + " - ENABLED";
    private Color color;
    private GeometryFactory factory = new GeometryFactory();
    private Blackboard blackboard;

    public SnapToCentroidPolicy(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.color = SnapOptionsPanel.getSnapToCentroidColor();
    }

    public SnapToCentroidPolicy() {
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
        double minDistance = Double.MAX_VALUE;
        Coordinate closestCoord = null;
        Point originalPoint = this.factory.createPoint(originalCoordinate);
        Geometry bufferedTransformedCursorLocation = originalPoint.buffer(units);
        if (toSpecificGeometry != null) {
            Geometry candidate = VisiblePointsAndLinesCache.toPointsAndLines(toSpecificGeometry);
            int i = 0;
            while (i < candidate.getNumGeometries()) {
                Geometry geomCand = candidate.getGeometryN(i);
                Coordinate can = geomCand.getCentroid().getCoordinate();
                double candDistance = can.distance(originalCoordinate);
                if (candDistance < units && candDistance < minDistance) {
                    minDistance = candDistance;
                    closestCoord = can;
                }
                ++i;
            }
        } else {
            for (Geometry candidate : VisiblePointsAndLinesCache.instance(panel).getTree().query(bufferedTransformedCursorLocation.getEnvelopeInternal())) {
                int i = 0;
                while (i < candidate.getNumGeometries()) {
                    Geometry geomCand = candidate.getGeometryN(i);
                    Coordinate can = geomCand.getCentroid().getCoordinate();
                    double candDistance = can.distance(originalCoordinate);
                    if (candDistance < units && candDistance < minDistance) {
                        minDistance = candDistance;
                        closestCoord = can;
                    }
                    ++i;
                }
            }
        }
        this.color = SnapOptionsPanel.getSnapToCentroidColor();
        return closestCoord;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

