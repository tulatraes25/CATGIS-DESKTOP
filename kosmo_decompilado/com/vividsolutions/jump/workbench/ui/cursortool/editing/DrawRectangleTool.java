/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.cursortool.editing;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.RectangleTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.snap.SnapIndicatorTool;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class DrawRectangleTool
extends RectangleTool {
    public static final String NAME = I18N.getString("workbench.ui.cursortool.editing.DrawRectangleTool.name");
    public static final Icon ICON = IconLoader.icon("DrawRectangle.gif");
    private FeatureDrawingUtil featureDrawingUtil;
    private SnapIndicatorTool snapIndicatorTool;

    protected DrawRectangleTool(FeatureDrawingUtil featureDrawingUtil) {
        this.featureDrawingUtil = featureDrawingUtil;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy) {
        FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(layerNamePanelProxy);
        return featureDrawingUtil.prepare(new DrawRectangleTool(featureDrawingUtil), true);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    protected boolean check() {
        String message = DrawRectangleTool.createEnableCheck(this.getWorkbench().getContext(), this).check(null);
        if (message != null) {
            this.getWorkbench().getFrame().warnUser(message);
            return false;
        }
        return !this.getModelSource().equals((Object)this.getModelDestination());
    }

    @Override
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        if (!this.check()) {
            return;
        }
        this.featureDrawingUtil.drawRing(this.getRectangle(), this.isRollingBackInvalidEdits(), this, this.getPanel());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext, AbstractCursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck(tool);
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1)).add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{5, 4, 15}));
        solucion.add(checkFactory.createAngleOfTheActiveViewMustBe(0.0));
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

