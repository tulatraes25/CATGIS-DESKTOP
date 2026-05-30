/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.MultiPoint
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.Collection;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.DrawCircleThreePointsTool;

public class DrawCircleThreePointsPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.DrawCircleThreePointsPlugIn.Draw-circle-from-three-points");
    public static final Icon ICON = IconLoader.icon("threePointsCircle.png");
    protected DrawCircleThreePointsTool dctpt = null;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return DrawCircleThreePointsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(checkFactory.createTaskWindowMustBeActiveCheck());
        check.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        check.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{3, 2, 5, 4, 15}));
        return check;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        SelectionManager selMan;
        Collection<Geometry> points;
        this.reportNothingToUndoYet(context);
        if (this.dctpt == null) {
            this.dctpt = new DrawCircleThreePointsTool();
        }
        if ((points = (selMan = context.getLayerViewPanel().getSelectionManager()).getSelectedItems()).size() != 3) {
            context.getLayerViewPanel().setCurrentCursorTool(this.dctpt);
        } else {
            Object[] pointsArr = points.toArray();
            if (pointsArr[0] instanceof MultiPoint) {
                Coordinate a = ((MultiPoint)pointsArr[0]).getCoordinate();
                Coordinate b = ((MultiPoint)pointsArr[1]).getCoordinate();
                Coordinate c = ((MultiPoint)pointsArr[2]).getCoordinate();
                this.dctpt.activate(context.getLayerViewPanel());
                this.dctpt.doCircle(a, b, c);
                this.dctpt.deactivate();
            } else {
                Coordinate a = ((Geometry)pointsArr[0]).getCentroid().getCoordinate();
                Coordinate b = ((Geometry)pointsArr[1]).getCentroid().getCoordinate();
                Coordinate c = ((Geometry)pointsArr[2]).getCentroid().getCoordinate();
                this.dctpt.activate(context.getLayerViewPanel());
                this.dctpt.doCircle(a, b, c);
                this.dctpt.deactivate();
            }
        }
        return true;
    }
}

