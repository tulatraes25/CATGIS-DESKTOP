/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.operation.valid.IsValidOp
 */
package com.vividsolutions.jump.workbench.ui.cursortool.editing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.snap.SnapIndicatorTool;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class DrawLineStringTool
extends MultiClickTool {
    public static final String NAME = I18N.getString("workbench.ui.cursortool.editing.DrawLineStringTool.name");
    public static final Icon ICON = IconLoader.icon("DrawLineString.gif");
    protected FeatureDrawingUtil featureDrawingUtil;

    protected DrawLineStringTool(FeatureDrawingUtil featureDrawingUtil) {
        this(featureDrawingUtil, true);
    }

    protected DrawLineStringTool(FeatureDrawingUtil featureDrawingUtil, boolean check) {
        super(check);
        this.featureDrawingUtil = featureDrawingUtil;
    }

    public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy) {
        FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(layerNamePanelProxy);
        return featureDrawingUtil.prepare(new DrawLineStringTool(featureDrawingUtil), true);
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
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        String message = DrawLineStringTool.createEnableCheck(this.getWorkbench().getContext(), this).check(null);
        if (message != null) {
            this.getWorkbench().getFrame().warnUser(message);
            return;
        }
        if (!this.checkLineString()) {
            return;
        }
        this.execute(this.featureDrawingUtil.createAddCommand((Geometry)this.getLineString(), this.isRollingBackInvalidEdits(), this.getPanel(), this));
    }

    protected LineString getLineString() throws Exception {
        return geomFac.createLineString(this.toArray(this.getCoordinates()));
    }

    protected boolean checkLineString() throws Exception {
        if (this.getCoordinates().size() < 2) {
            this.getPanel().getContext().warnUser(I18N.getString("workbench.ui.cursortool.editing.DrawLineStringTool.the-linestring-must-have-at-least-2-points"));
            return false;
        }
        IsValidOp isValidOp = new IsValidOp((Geometry)this.getLineString());
        if (!isValidOp.isValid()) {
            this.getPanel().getContext().warnUser(isValidOp.getValidationError().getMessage());
            if (this.isRollingBackInvalidEdits()) {
                return false;
            }
        }
        return true;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext, AbstractCursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck(tool);
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1)).add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{3, 2, 15}));
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

