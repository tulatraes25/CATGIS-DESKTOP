/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel;
import com.vividsolutions.jump.workbench.ui.snap.SnapPolicy;
import com.vividsolutions.jump.workbench.ui.snap.VisiblePointsAndLinesCache;
import java.awt.Color;
import org.saig.jump.lang.I18N;

public class MidPointSnapPolicy
implements SnapPolicy {
    public static final String NAME = I18N.getString("com.vividsolutions.jump.workbench.ui.snap.MidPointSnapPolicy.Snap-to-segment-mid-points");
    private Color color;
    private Blackboard blackboard;
    private GeometryFactory factory = new GeometryFactory();
    public static final String ENABLED_KEY = String.valueOf(MidPointSnapPolicy.class.getName()) + " - ENABLED";

    public MidPointSnapPolicy(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.color = SnapOptionsPanel.getSnapToMidPointColor();
    }

    public MidPointSnapPolicy() {
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
        Geometry bufferedTransformedCursorLocation = this.factory.createPoint(originalCoordinate).buffer(units);
        Coordinate closest = null;
        Coordinate aux = null;
        double distance = Double.MAX_VALUE;
        if (toSpecificGeometry != null) {
            Geometry candidate = VisiblePointsAndLinesCache.toPointsAndLines(toSpecificGeometry);
            int i = 0;
            while (i < candidate.getNumGeometries()) {
                Coordinate[] points = candidate.getGeometryN(i).getCoordinates();
                int k = 0;
                while (k < points.length - 1) {
                    aux = new Coordinate((points[k].x + points[k + 1].x) / 2.0, (points[k].y + points[k + 1].y) / 2.0);
                    double auxd = aux.distance(originalCoordinate);
                    if (auxd < units && distance > auxd) {
                        distance = auxd;
                        closest = aux;
                    }
                    ++k;
                }
                ++i;
            }
        } else {
            for (Geometry candidate : VisiblePointsAndLinesCache.instance(panel).getTree().query(bufferedTransformedCursorLocation.getEnvelopeInternal())) {
                int i = 0;
                while (i < candidate.getNumGeometries()) {
                    Coordinate[] points = candidate.getGeometryN(i).getCoordinates();
                    int k = 0;
                    while (k < points.length - 1) {
                        aux = new Coordinate((points[k].x + points[k + 1].x) / 2.0, (points[k].y + points[k + 1].y) / 2.0);
                        double auxd = aux.distance(originalCoordinate);
                        if (auxd < units && distance > auxd) {
                            distance = auxd;
                            closest = aux;
                        }
                        ++k;
                    }
                    ++i;
                }
            }
        }
        this.color = SnapOptionsPanel.getSnapToMidPointColor();
        return closest;
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

