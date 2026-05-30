/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.plugins.selection;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.plugin.ClearSelectionPlugIn;
import javax.swing.Icon;

public abstract class AbstractClearLayerSelectionPlugIn
extends ClearSelectionPlugIn {
    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layer layer = context.getLayerManager().getLayer(this.getLayerName());
        if (layer != null) {
            context.getLayerViewPanel().getSelectionManager().unselectItems(layer);
        }
        return true;
    }

    public abstract String getLayerName();

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        return check;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        MultiEnableCheck check = AbstractClearLayerSelectionPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
        EnableCheckFactory checkFactory = new EnableCheckFactory(JUMPWorkbench.getFrameInstance().getContext());
        check.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheckInLayer(1, this.getLayerName()));
        return check;
    }
}

