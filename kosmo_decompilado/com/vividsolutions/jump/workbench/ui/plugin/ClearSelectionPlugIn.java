/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.config.ConfigPlugIn;
import org.saig.jump.widgets.config.ConfigDialog;
import org.saig.jump.widgets.config.ConfigSelectionPanel;

public class ClearSelectionPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.ClearSelectionPlugIn.name");
    public static final Icon ICON = IconLoader.icon("deleteSelection.gif");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        ConfigPlugIn.getDialog().addConfigPanel(new ConfigSelectionPanel(context.getWorkbenchContext().getBlackboard()), ConfigDialog.TOOLS_MAIN_CATEGORY_NAME, ConfigSelectionPanel.NAME);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        context.getLayerViewPanel().getSelectionManager().clear();
        return true;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustExistCheck(1)).add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1));
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return ClearSelectionPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

