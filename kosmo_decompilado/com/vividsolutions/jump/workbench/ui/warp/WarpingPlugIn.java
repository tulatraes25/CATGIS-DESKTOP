/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.warp;

import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxPlugIn;
import com.vividsolutions.jump.workbench.ui.warp.DeleteIncrementalWarpingVectorTool;
import com.vividsolutions.jump.workbench.ui.warp.DeleteWarpingVectorTool;
import com.vividsolutions.jump.workbench.ui.warp.DrawIncrementalWarpingVectorTool;
import com.vividsolutions.jump.workbench.ui.warp.DrawWarpingVectorTool;
import com.vividsolutions.jump.workbench.ui.warp.WarpingPanel;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;

public class WarpingPlugIn
extends ToolboxPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.warp.WarpingPlugIn.name");
    public static final Icon ICON = IconLoader.icon("GoalFlag.gif");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    protected void initializeToolbox(ToolboxDialog toolbox) {
        WarpingPanel warpingPanel = new WarpingPanel(toolbox);
        toolbox.getCenterPanel().add((Component)warpingPanel, "Center");
        this.add(new DrawWarpingVectorTool(), false, toolbox, warpingPanel);
        this.add(new DeleteWarpingVectorTool(), false, toolbox, warpingPanel);
        toolbox.getToolBar().addSeparator();
        this.add(new DrawIncrementalWarpingVectorTool(warpingPanel), true, toolbox, warpingPanel);
        this.add(new DeleteIncrementalWarpingVectorTool(warpingPanel), true, toolbox, warpingPanel);
        toolbox.setInitialLocation(new GUIUtil.Location(20, true, 175, false));
    }

    private void add(CursorTool tool, final boolean incremental, ToolboxDialog toolbox, final WarpingPanel warpingPanel) {
        toolbox.add(tool, new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (incremental && warpingPanel.isWarpingIncrementally()) {
                    return null;
                }
                if (!incremental && !warpingPanel.isWarpingIncrementally()) {
                    return null;
                }
                return incremental ? I18N.getString("workbench.ui.warp.WarpingPlugIn.incremental-warping-must-be-enabled") : I18N.getString("workbench.ui.warp.WarpingPlugIn.incremental-warping-must-be-disabled");
            }
        });
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }
}

