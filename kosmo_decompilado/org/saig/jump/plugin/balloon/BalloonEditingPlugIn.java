/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.balloon;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxPlugIn;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.balloon.DefineBalloonEditingScalePlugIn;
import org.saig.jump.plugin.balloon.GoToBalloonEditingScalePlugIn;
import org.saig.jump.plugin.balloon.SelectedFeaturePropertiesBalloonPlugIn;
import org.saig.jump.plugin.balloon.SelectedFeatureRecalculateExpressionBalloonPlugIn;
import org.saig.jump.tools.balloon.AddNewBalloonTool;
import org.saig.jump.tools.balloon.DeleteBalloonTool;
import org.saig.jump.tools.balloon.ModifyBalloonShapeTool;
import org.saig.jump.tools.balloon.PropertiesBalloonTool;

public class BalloonEditingPlugIn
extends ToolboxPlugIn {
    public static final String KEY = BalloonEditingPlugIn.class.getName();
    public static final String NAME = I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.editing.BalloonEditingPlugIn.balloon-editing-tools");
    public static final Icon ICON = IconLoader.icon("EditingToolbox.gif");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        JUMPWorkbench.getBlackboard().put(KEY, this);
    }

    @Override
    protected void initializeToolbox(ToolboxDialog toolbox) {
        AddNewBalloonTool addNewGlobeTool = new AddNewBalloonTool();
        toolbox.add(new QuasimodeTool(addNewGlobeTool).add(new QuasimodeTool.ModifierKeySpec(true, false, false), null), AddNewBalloonTool.createEnableCheck(toolbox.getContext(), addNewGlobeTool));
        ModifyBalloonShapeTool moveBalloonHeadTool = new ModifyBalloonShapeTool();
        toolbox.add(new QuasimodeTool(moveBalloonHeadTool).add(new QuasimodeTool.ModifierKeySpec(true, false, false), null), ModifyBalloonShapeTool.createEnableCheck(toolbox.getContext(), moveBalloonHeadTool));
        PropertiesBalloonTool propertiesBalloonTool = new PropertiesBalloonTool();
        toolbox.add(new QuasimodeTool(propertiesBalloonTool).add(new QuasimodeTool.ModifierKeySpec(true, false, false), null), PropertiesBalloonTool.createEnableCheck(toolbox.getContext(), propertiesBalloonTool));
        SelectedFeaturePropertiesBalloonPlugIn selectedFeaturePropertiesBalloonTool = new SelectedFeaturePropertiesBalloonPlugIn();
        toolbox.addPlugIn(selectedFeaturePropertiesBalloonTool);
        SelectedFeatureRecalculateExpressionBalloonPlugIn selectedFeatureRecalculateExpressionBalloonPlugIn = new SelectedFeatureRecalculateExpressionBalloonPlugIn();
        toolbox.addPlugIn(selectedFeatureRecalculateExpressionBalloonPlugIn);
        DefineBalloonEditingScalePlugIn defineEditBalloonEditingScalePlugIn = new DefineBalloonEditingScalePlugIn();
        toolbox.addPlugIn(defineEditBalloonEditingScalePlugIn);
        GoToBalloonEditingScalePlugIn goToBalloonEditingScalePlugIn = new GoToBalloonEditingScalePlugIn();
        toolbox.addPlugIn(goToBalloonEditingScalePlugIn);
        DeleteBalloonTool deleteBalloonTool = new DeleteBalloonTool();
        toolbox.add(new QuasimodeTool(deleteBalloonTool).add(new QuasimodeTool.ModifierKeySpec(true, false, false), null), DeleteBalloonTool.createEnableCheck(toolbox.getContext(), deleteBalloonTool));
        toolbox.setInitialLocation(new GUIUtil.Location(20, true, 20, false));
    }

    @Override
    public EnableCheck getCheck() {
        return BalloonEditingPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
    }
}

