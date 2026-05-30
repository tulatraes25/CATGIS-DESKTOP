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
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DelegatingTool;
import com.vividsolutions.jump.workbench.ui.cursortool.LeftClickFilter;
import com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.VerticesInFencePlugIn;
import com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel;
import com.vividsolutions.jump.workbench.ui.snap.SnapPolicy;
import com.vividsolutions.jump.workbench.ui.snap.VisiblePointsAndLinesCache;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import org.saig.jump.lang.I18N;

public class SnapToVerticesPolicy
implements SnapPolicy {
    public static final String NAME = I18N.getString("workbench.ui.snap.SnapOptionsPanel.snap-to-vertices");
    private Color color;
    private GeometryFactory factory = new GeometryFactory();
    private Blackboard blackboard;
    public static final String ENABLED_KEY = String.valueOf(SnapToVerticesPolicy.class.getName()) + " - ENABLED";
    public static final String FIRST_CANDIDATE = String.valueOf(SnapToVerticesPolicy.class.getName()) + " - FIRST CANDIDATE";

    public SnapToVerticesPolicy(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.color = SnapOptionsPanel.getSnapToVerticesColor();
    }

    @Override
    public Coordinate snap(LayerViewPanel panel, Coordinate originalPoint, Geometry toSpecificGeometry) {
        if (!PersistentBlackboardPlugIn.get(this.blackboard).get(ENABLED_KEY, false)) {
            return null;
        }
        Object value = PersistentBlackboardPlugIn.get(this.blackboard).get("SNAP_BUFFER", new Double(10.0));
        double units = 10.0;
        if (value != null) {
            units = (Double)value;
        }
        Geometry bufferedTransformedCursorLocation = this.factory.createPoint(originalPoint).buffer(units);
        HashSet<Coordinate> vertices = new HashSet<Coordinate>();
        if (toSpecificGeometry != null) {
            Geometry candidate = VisiblePointsAndLinesCache.toPointsAndLines(toSpecificGeometry);
            vertices.addAll(VerticesInFencePlugIn.verticesInFence(candidate, bufferedTransformedCursorLocation, true).getCoordinates());
        } else {
            for (Geometry pointsAndLines : VisiblePointsAndLinesCache.instance(panel).getTree().query(bufferedTransformedCursorLocation.getEnvelopeInternal())) {
                vertices.addAll(VerticesInFencePlugIn.verticesInFence(pointsAndLines, bufferedTransformedCursorLocation, true).getCoordinates());
            }
        }
        Coordinate coord = this.calculateFirstCoordinate(panel, originalPoint, units);
        if (coord != null) {
            vertices.add(coord);
        }
        if (vertices.isEmpty()) {
            return null;
        }
        this.color = SnapOptionsPanel.getSnapToVerticesColor();
        return CoordUtil.closest(vertices, originalPoint);
    }

    private Coordinate calculateFirstCoordinate(LayerViewPanel panel, Coordinate originalPoint, double units) {
        Coordinate solution = null;
        boolean firstIsCandidate = PersistentBlackboardPlugIn.get(this.blackboard).get(FIRST_CANDIDATE, false);
        if (!firstIsCandidate) {
            return solution;
        }
        CursorTool currentCursorTool = panel.getCurrentCursorTool();
        if (currentCursorTool != null) {
            Coordinate coord;
            List<Coordinate> coords;
            if (currentCursorTool instanceof QuasimodeTool) {
                LeftClickFilter filter;
                QuasimodeTool tool = (QuasimodeTool)currentCursorTool;
                currentCursorTool = tool.getDefaultTool() instanceof LeftClickFilter ? ((filter = (LeftClickFilter)tool.getDefaultTool()).getWrappee() instanceof DelegatingTool ? ((DelegatingTool)filter.getWrappee()).getDelegate() : filter.getWrappee()) : tool.getDefaultTool();
            }
            if (currentCursorTool instanceof MultiClickTool && (coords = ((MultiClickTool)currentCursorTool).getCoordinates()).size() > 0 && originalPoint.distance(coord = coords.get(0)) <= units) {
                solution = coord;
            }
        }
        return solution;
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

