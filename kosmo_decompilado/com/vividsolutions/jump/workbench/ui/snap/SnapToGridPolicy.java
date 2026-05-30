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
import java.awt.Color;
import org.saig.jump.lang.I18N;

public class SnapToGridPolicy
implements SnapPolicy {
    public static final String NAME = I18N.getString("workbench.ui.snap.SnapOptionsPanel.snap-to-grid");
    private Color color;
    public static final String ENABLED_KEY = String.valueOf(SnapToGridPolicy.class.getName()) + " - ENABLED";
    public static final String GRID_SIZE_KEY = String.valueOf(SnapToGridPolicy.class.getName()) + " - GRID_SIZE";
    private Blackboard blackboard;
    private static GeometryFactory factory = new GeometryFactory();

    public SnapToGridPolicy(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.color = SnapOptionsPanel.getSnapToGridColor();
    }

    @Override
    public Coordinate snap(LayerViewPanel panel, Coordinate originalCoordinate, Geometry toSpecificGeometry) {
        Coordinate closest;
        if (!PersistentBlackboardPlugIn.get(this.blackboard).get(ENABLED_KEY, false)) {
            return null;
        }
        double gridSize = PersistentBlackboardPlugIn.get(this.blackboard).get(GRID_SIZE_KEY, 20.0);
        Object value = PersistentBlackboardPlugIn.get(this.blackboard).get("SNAP_BUFFER", new Double(10.0));
        double units = 10.0;
        if (value != null) {
            units = (Double)value;
        }
        if (originalCoordinate.distance(closest = new Coordinate((double)Math.round(originalCoordinate.x / gridSize) * gridSize, (double)Math.round(originalCoordinate.y / gridSize) * gridSize)) > units) {
            return null;
        }
        if (toSpecificGeometry != null && !toSpecificGeometry.contains((Geometry)factory.createPoint(closest))) {
            return null;
        }
        this.color = SnapOptionsPanel.getSnapToGridColor();
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

