/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel;
import com.vividsolutions.jump.workbench.ui.snap.SnapPolicy;
import com.vividsolutions.jump.workbench.ui.snap.VisiblePointsAndLinesCache;
import com.vividsolutions.jump.workbench.ui.snap.algorithms.TangentSnapAlgs;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.KosmoDesktopUtils;

public class SnapToTangentPolicy
implements SnapPolicy {
    private static final Logger LOGGER = Logger.getLogger(SnapToTangentPolicy.class);
    private Color color;
    public static final String ENABLED_KEY = String.valueOf(SnapToTangentPolicy.class.getName()) + " - ENABLED";
    public static final String NAME = I18N.getString("com.vividsolutions.jump.workbench.ui.snap.SnapToTangentPolicy.Snap-to-tangent");
    private Blackboard blackboard;
    private static GeometryFactory factory = new GeometryFactory();
    private static TangentSnapAlgs tangentAlgs = new TangentSnapAlgs();

    public SnapToTangentPolicy(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.color = SnapOptionsPanel.getSnapToTangentColor();
    }

    @Override
    public Coordinate snap(LayerViewPanel panel, Coordinate originalPoint, Geometry toSpecificGeometry) {
        if (panel == null) {
            return null;
        }
        if (!PersistentBlackboardPlugIn.get(this.blackboard).get(ENABLED_KEY, false)) {
            return null;
        }
        ArrayList<Coordinate> vertices = new ArrayList<Coordinate>();
        MultiClickTool mct = this.getCurrentMultiClickTool(panel);
        if (mct == null) {
            return null;
        }
        Coordinate lastClickedPoint = this.getLastClickedPoint(mct);
        if (lastClickedPoint == null) {
            return null;
        }
        Object value = PersistentBlackboardPlugIn.get(this.blackboard).get("SNAP_BUFFER", new Double(10.0));
        double units = 10.0;
        if (value != null) {
            units = (Double)value;
        }
        Geometry bufferedTransformedCursorLocation = factory.createPoint(originalPoint).buffer(units);
        if (toSpecificGeometry != null) {
            Coordinate[] tangentPoints = tangentAlgs.getTangentPoints(toSpecificGeometry, lastClickedPoint);
            if (tangentPoints != null) {
                vertices.addAll(Arrays.asList(tangentPoints));
            }
        } else {
            for (Geometry pointsAndLines : VisiblePointsAndLinesCache.instance(panel).getTree().query(bufferedTransformedCursorLocation.getEnvelopeInternal())) {
                Coordinate[] tangentPoints = tangentAlgs.getTangentPoints(pointsAndLines, lastClickedPoint);
                if (tangentPoints == null) continue;
                vertices.addAll(Arrays.asList(tangentPoints));
            }
        }
        if (vertices.isEmpty()) {
            return null;
        }
        this.color = SnapOptionsPanel.getSnapToTangentColor();
        return CoordUtil.closest(vertices, originalPoint);
    }

    private MultiClickTool getCurrentMultiClickTool(LayerViewPanel panel) {
        CursorTool currentCursorTool = KosmoDesktopUtils.getLastNotDelegatingCursorTool(panel.getCurrentCursorTool());
        if (currentCursorTool == null) {
            return null;
        }
        MultiClickTool mct = null;
        if (!(currentCursorTool instanceof MultiClickTool)) {
            return null;
        }
        mct = (MultiClickTool)currentCursorTool;
        return mct;
    }

    private Coordinate getLastClickedPoint(MultiClickTool mct) {
        List<Coordinate> coordinates = mct.getCoordinates();
        if (coordinates == null) {
            return null;
        }
        if (coordinates.isEmpty()) {
            return null;
        }
        return coordinates.get(coordinates.size() - 1);
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

