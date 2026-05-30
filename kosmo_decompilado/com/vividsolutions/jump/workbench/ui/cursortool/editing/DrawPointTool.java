/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.Point
 */
package com.vividsolutions.jump.workbench.ui.cursortool.editing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.snap.SnapIndicatorTool;
import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class DrawPointTool
extends NClickTool {
    public static final String NAME = I18N.getString("workbench.ui.cursortool.editing.DrawPointTool.name");
    public static final Icon ICON = IconLoader.icon("DrawPoint.gif");
    protected FeatureDrawingUtil featureDrawingUtil;

    protected DrawPointTool(FeatureDrawingUtil featureDrawingUtil) {
        super(1);
        this.featureDrawingUtil = featureDrawingUtil;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected Shape getShape() throws NoninvertibleTransformException {
        return null;
    }

    public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy) {
        FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(layerNamePanelProxy);
        return featureDrawingUtil.prepare(new DrawPointTool(featureDrawingUtil), true);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        this.execute(this.featureDrawingUtil.createAddCommand((Geometry)this.getPoint(), this.isRollingBackInvalidEdits(), this.getPanel(), this));
    }

    protected Point getPoint() throws NoninvertibleTransformException {
        return geomFac.createPoint(this.getCoordinates().get(0));
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext, CursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck(tool);
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1)).add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{1, 8, 15}));
        return solucion;
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
}

