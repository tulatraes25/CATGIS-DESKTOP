/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.utils;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import javax.swing.JInternalFrame;
import org.apache.log4j.Logger;
import org.saig.core.model.data.widgets.ViewTableFrame;
import org.saig.jump.lang.I18N;

public class SortTableTableModelPlugIn
extends AbstractPlugIn {
    public static final Logger LOGGER = Logger.getLogger(SortTableTableModelPlugIn.class);
    public static final String NAME_ASC = I18N.getString("org.saig.jump.plugin.utils.SortTableModelPlugIn.sort-ascending");
    public static final String NAME_DSC = I18N.getString("org.saig.jump.plugin.utils.SortTableModelPlugIn.sort-descending");
    public static final Icon ICON_ASC = IconLoader.icon("sort_asc.gif");
    public static final Icon ICON_DSC = IconLoader.icon("sort_desc.gif");
    private boolean ascending = true;

    public SortTableTableModelPlugIn(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        JInternalFrame frame = context.getActiveInternalFrame();
        if (frame instanceof ViewTableFrame) {
            ViewTableFrame viewTableFrame = (ViewTableFrame)frame;
            viewTableFrame.explicitSort(this.ascending);
        }
        return true;
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck();
    }

    @Override
    public EnableCheck getCheck() {
        return SortTableTableModelPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public String getName() {
        if (this.ascending) {
            return NAME_ASC;
        }
        return NAME_DSC;
    }

    @Override
    public Icon getIcon() {
        if (this.ascending) {
            return ICON_ASC;
        }
        return ICON_DSC;
    }
}

