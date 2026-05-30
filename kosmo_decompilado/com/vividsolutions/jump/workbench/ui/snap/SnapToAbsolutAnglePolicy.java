/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineSegment
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel;
import com.vividsolutions.jump.workbench.ui.snap.SnapPolicy;
import com.vividsolutions.jump.workbench.ui.snap.algorithms.AngleAlgs;
import java.awt.Color;
import java.util.List;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.KosmoDesktopUtils;

public class SnapToAbsolutAnglePolicy
implements SnapPolicy {
    private static double[] ANGLES = new double[]{0.0, 0.7853981633974483, 1.5707963267948966, 2.356194490192345, Math.PI, 3.9269908169872414, 4.71238898038469, 5.497787143782138, Math.PI * 2};
    private Color color;
    public static final String ENABLED_KEY = String.valueOf(SnapToAbsolutAnglePolicy.class.getName()) + " - ENABLED";
    public static final String NAME = I18N.getString("com.vividsolutions.jump.workbench.ui.snap.SnapToAbsolutAnglePolicy.Snap-to-absolute-angles");
    private Blackboard blackboard;
    private static AngleAlgs angleAlgs = new AngleAlgs();

    public SnapToAbsolutAnglePolicy(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.color = SnapOptionsPanel.getSnapToPerpendicularColor();
    }

    @Override
    public Coordinate snap(LayerViewPanel panel, Coordinate originalPoint, Geometry toSpecificGeometry) {
        if (panel == null) {
            return null;
        }
        if (!PersistentBlackboardPlugIn.get(this.blackboard).get(ENABLED_KEY, false)) {
            return null;
        }
        MultiClickTool mct = this.getCurrentMultiClickTool(panel);
        if (mct == null) {
            return null;
        }
        if (mct.getCoordinates().size() < 1) {
            return null;
        }
        Coordinate lastC = this.getLastClickedPoint(mct);
        if (lastC == null) {
            return null;
        }
        LineSegment originalSegment = new LineSegment(lastC, originalPoint);
        double len = originalSegment.getLength();
        double angle = angleAlgs.clipAngle(originalSegment.angle(), ANGLES);
        double x = len * Math.cos(angle);
        double y = len * Math.sin(angle);
        this.color = SnapOptionsPanel.getSnapToAbsolutAngleColor();
        return new Coordinate(lastC.x + x, lastC.y + y);
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
        List<Coordinate> snappedClicksHistory = mct.getCoordinates();
        if (snappedClicksHistory.size() > 0) {
            Coordinate coordinate = snappedClicksHistory.get(snappedClicksHistory.size() - 1);
            return coordinate;
        }
        return null;
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

