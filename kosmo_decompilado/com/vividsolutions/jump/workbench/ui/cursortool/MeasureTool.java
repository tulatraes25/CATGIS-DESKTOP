/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  org.cresques.cts.GeoCalc
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.snap.SnapIndicatorTool;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.cresques.cts.GeoCalc;
import org.cresques.cts.IProjection;
import org.saig.core.util.UnitsManager;
import org.saig.jump.lang.I18N;

public class MeasureTool
extends MultiClickTool {
    public static final String NAME = I18N.getString("workbench.ui.cursortool.MeasureTool.name");
    public static final Icon ICON = IconLoader.icon("Ruler.gif");
    public static final Cursor CURSOR = MeasureTool.createCursor(IconLoader.icon("RulerCursor.gif").getImage());

    public MeasureTool() {
        super(false);
        this.allowSnapping();
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {
        super.activate(layerViewPanel);
        if (this.snapIndicatorTool == null) {
            this.snapIndicatorTool = new SnapIndicatorTool(this.getSnappingPolicies(JUMPWorkbench.getBlackboard()));
        } else {
            this.snapIndicatorTool.setNewPolicies(this.getSnappingPolicies(JUMPWorkbench.getBlackboard()));
        }
        this.snapIndicatorTool.activate(layerViewPanel);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.snapIndicatorTool.deactivate();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    @Override
    public void mouseLocationChanged(MouseEvent e) {
        try {
            if (this.isShapeOnScreen()) {
                ArrayList<Coordinate> currentCoordinates = new ArrayList<Coordinate>(this.getCoordinates());
                currentCoordinates.add(this.snap(e.getPoint()));
                this.display(currentCoordinates, this.getPanel());
            }
            super.mouseLocationChanged(e);
        }
        catch (Throwable t) {
            this.getPanel().getContext().handleThrowable(t);
        }
    }

    @Override
    protected void gestureFinished() throws NoninvertibleTransformException {
        this.reportNothingToUndoYet();
        this.display(this.getCoordinates(), this.getPanel());
    }

    private void display(List<Coordinate> coordinates, LayerViewPanel panel) throws NoninvertibleTransformException {
        double totalDistance = 0.0;
        double lastSegmentDistance = 0.0;
        IProjection proj = this.getWorkbench().getContext().getTask().getProjection();
        int i = 1;
        while (i < coordinates.size()) {
            Point2D.Double point1 = new Point2D.Double(coordinates.get((int)(i - 1)).x, coordinates.get((int)(i - 1)).y);
            Point2D.Double point2 = new Point2D.Double(coordinates.get((int)i).x, coordinates.get((int)i).y);
            lastSegmentDistance = MeasureTool.distanceWorld(point1, point2, proj);
            totalDistance += lastSegmentDistance;
            ++i;
        }
        this.display(totalDistance, lastSegmentDistance, panel);
    }

    private String convertFromMapUnitToUserUnit(double mapDistance) {
        return UnitsManager.convertDistanceValueToString(mapDistance, this.getPanel().getMapLengthUnit(), this.getPanel().getUserLengthUnit());
    }

    private void display(double totalDistance, double lastSegmentDistance, LayerViewPanel panel) {
        String statusMessage = String.valueOf(I18N.getString("workbench.ui.cursortool.MeasureTool.distance")) + " -> " + I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.MeasureTool.total") + ": " + this.convertFromMapUnitToUserUnit(totalDistance);
        statusMessage = String.valueOf(statusMessage) + " , " + I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.MeasureTool.segment") + ": " + this.convertFromMapUnitToUserUnit(lastSegmentDistance);
        panel.getContext().setStatusMessage(statusMessage);
    }

    public static double distanceWorld(Point2D pt1, Point2D pt2, IProjection proj) {
        double dist = -1.0;
        dist = pt1.distance(pt2);
        if (proj != null && !proj.isProjected()) {
            dist = new GeoCalc(proj).distanceVincenty(proj.toGeo(pt1), proj.toGeo(pt2));
        }
        return dist;
    }

    public static double distanceWorld(List<Coordinate> coordinates, IProjection proj) {
        double totalDistance = 0.0;
        int i = 1;
        while (i < coordinates.size()) {
            Point2D.Double point1 = new Point2D.Double(coordinates.get((int)(i - 1)).x, coordinates.get((int)(i - 1)).y);
            Point2D.Double point2 = new Point2D.Double(coordinates.get((int)i).x, coordinates.get((int)i).y);
            totalDistance += MeasureTool.distanceWorld(point1, point2, proj);
            ++i;
        }
        return totalDistance;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext, AbstractCursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck(tool);
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        return solucion;
    }
}

