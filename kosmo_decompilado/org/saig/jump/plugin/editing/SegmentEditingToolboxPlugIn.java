/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.RedoPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.UndoPlugIn;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxPlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedSegmentsPlugIn;
import java.awt.Dimension;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.config.ConfigPlugIn;
import org.saig.jump.plugin.editing.ClearSegmentSelectionPlugIn;
import org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn;
import org.saig.jump.plugin.editing.SegmentLateralDisplacementPlugIn;
import org.saig.jump.tools.editing.IncrementalMoveVertexTool;
import org.saig.jump.tools.editing.InsertMultipleVertexTool;
import org.saig.jump.tools.editing.ModifySegmentTool;
import org.saig.jump.tools.editing.SelectSingleFeatureSegmentsTool;
import org.saig.jump.widgets.config.ConfigDialog;
import org.saig.jump.widgets.config.ConfigSegmentsSelectionPanel;

public class SegmentEditingToolboxPlugIn
extends ToolboxPlugIn {
    private static final Logger LOGGER = Logger.getLogger(SegmentEditingToolboxPlugIn.class);
    public static final String NAME = I18N.getString(SegmentEditingToolboxPlugIn.class, "segments-editing");
    public static final Icon ICON = IconLoader.icon("SegmentEditingToolbox.png");

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    protected void initializeToolbox(ToolboxDialog toolbox) {
        toolbox.setIconImage(((ImageIcon)ICON).getImage());
        EnableCheckFactory checkFactory = new EnableCheckFactory(toolbox.getContext());
        SelectSingleFeatureSegmentsTool selectSingleFeatureSegmentsTool = new SelectSingleFeatureSegmentsTool();
        ZoomToSelectedSegmentsPlugIn zoomToSelectedSegmentsPlugIn = new ZoomToSelectedSegmentsPlugIn();
        ClearSegmentSelectionPlugIn clearSegmentSelectionPlugIn = new ClearSegmentSelectionPlugIn();
        DeleteSelectedSegmentsPlugIn deleteSelectedSegmentsPlugIn = new DeleteSelectedSegmentsPlugIn();
        SegmentLateralDisplacementPlugIn segmentLateralDisplacementPlugIn = new SegmentLateralDisplacementPlugIn();
        ModifySegmentTool modifySegmentTool = new ModifySegmentTool(checkFactory);
        IncrementalMoveVertexTool incrementalMoveVertexTool = new IncrementalMoveVertexTool(checkFactory);
        InsertMultipleVertexTool insertMultipleVertexTool = new InsertMultipleVertexTool(checkFactory);
        toolbox.add(selectSingleFeatureSegmentsTool, SelectSingleFeatureSegmentsTool.createEnableCheck(toolbox.getContext(), selectSingleFeatureSegmentsTool));
        toolbox.addPlugIn(zoomToSelectedSegmentsPlugIn);
        toolbox.addPlugIn(clearSegmentSelectionPlugIn);
        toolbox.addPlugIn(deleteSelectedSegmentsPlugIn);
        toolbox.addPlugIn(segmentLateralDisplacementPlugIn);
        toolbox.add(modifySegmentTool, ModifySegmentTool.createEnableCheck(toolbox.getContext(), modifySegmentTool));
        toolbox.getToolBar().addSeparator();
        toolbox.add(incrementalMoveVertexTool, IncrementalMoveVertexTool.createEnableCheck(toolbox.getContext(), incrementalMoveVertexTool));
        toolbox.add(insertMultipleVertexTool, InsertMultipleVertexTool.createEnableCheck(toolbox.getContext(), insertMultipleVertexTool));
        toolbox.getToolBar().addSeparator();
        UndoPlugIn undoPlugIn = new UndoPlugIn();
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)undoPlugIn.getIcon(), 20), undoPlugIn, undoPlugIn.getCheck(), toolbox.getContext());
        RedoPlugIn redoPlugIn = new RedoPlugIn();
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)redoPlugIn.getIcon(), 20), redoPlugIn, redoPlugIn.getCheck(), toolbox.getContext());
        toolbox.setInitialLocation(new GUIUtil.Location(20, true, 160, false));
        toolbox.setMinimumSize(new Dimension(200, 60));
        ConfigPlugIn.getDialog().addConfigPanel(new ConfigSegmentsSelectionPanel(toolbox.getContext().getBlackboard()), ConfigDialog.TOOLS_MAIN_CATEGORY_NAME, "Selecci\u00f3n de segmentos");
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return SegmentEditingToolboxPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public String getName() {
        return NAME;
    }
}

