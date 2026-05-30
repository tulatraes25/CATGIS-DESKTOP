/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.zoom;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class ZoomToSelectedSegmentsPlugIn
extends ZoomToSelectedItemsPlugIn {
    public static final String NAME = I18N.getString("com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedSegmentsPlugIn.zoom");
    public static final Icon ICON = IconLoader.icon("ZoomSelectedSegments.gif");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        this.zoom(context.getLayerViewPanel().getSelectionManager().getSegmentSelection().getSelectedItems(), context.getLayerViewPanel());
        return true;
    }

    @Override
    public EnableCheck getCheck() {
        return ZoomToSelectedSegmentsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createAtLeastNSegmentsMustBeSelectedCheck(1));
    }

    public static /* bridge */ /* synthetic */ EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        return ZoomToSelectedSegmentsPlugIn.createEnableCheck(workbenchContext);
    }
}

