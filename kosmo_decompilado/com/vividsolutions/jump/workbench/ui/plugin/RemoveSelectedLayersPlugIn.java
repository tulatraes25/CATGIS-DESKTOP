/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.EditablePlugIn;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class RemoveSelectedLayersPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.RemoveSelectedLayersPlugIn.name");
    public static final Icon ICON = IconLoader.icon("deleteLayer.gif");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return RemoveSelectedLayersPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.remove(context.getLayerNamePanel().selectedNodes(Layerable.class).toArray(new Layerable[0]), context);
        context.getLayerViewPanel().repaint();
        return true;
    }

    public void remove(Layerable[] selectedLayers, PlugInContext context) {
        int i = 0;
        while (i < selectedLayers.length) {
            boolean check;
            Layerable layerable = selectedLayers[i];
            if (layerable instanceof Layer && !(check = EditablePlugIn.askCommitIfLayerIsEditable((Layer)layerable, context))) {
                return;
            }
            layerable.getLayerManager().remove(selectedLayers[i]);
            layerable.dispose();
            ++i;
        }
        System.gc();
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayerablesMustBeSelectedCheck(1, Layerable.class)).add(checkFactory.createSelectedLayersMustBeNoInternals());
    }
}

