/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.info;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.info.LayerInfoDialog;

public class LayerInfoPlugIn
extends AbstractPlugIn {
    public static String NAME = String.valueOf(I18N.getString("org.saig.jump.plugin.info.LayerInfoPlugIn.Layer-properties")) + "...";
    public static Icon ICON = IconLoader.icon("layerProperties.png");

    @Override
    public boolean execute(PlugInContext context) {
        this.reportNothingToUndoYet(context);
        Layerable[] layers = context.getLayerNamePanel().getSelectedLayers();
        Layerable selectedLayer = layers[0];
        boolean isWMS = selectedLayer instanceof WMSLayer;
        boolean isLayer = selectedLayer instanceof Layer;
        if (isWMS || isLayer) {
            LayerInfoDialog dialog = new LayerInfoDialog(JUMPWorkbench.getFrameInstance(), true, selectedLayer, context, isWMS);
            GUIUtil.centreOnScreen(dialog);
            dialog.setVisible(true);
        }
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(checkFactory.createTaskWindowMustBeActiveCheck());
        check.add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck());
        check.add(checkFactory.createExactlyNLayerablesMustBeSelectedCheck(1, Layerable.class));
        check.add(checkFactory.createSelectedLayerMustBeActiveCheck());
        return check;
    }

    @Override
    public EnableCheck getCheck() {
        return LayerInfoPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

