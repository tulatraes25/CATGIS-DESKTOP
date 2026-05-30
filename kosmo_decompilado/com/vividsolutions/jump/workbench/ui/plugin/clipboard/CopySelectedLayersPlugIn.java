/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.clipboard;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.CollectionOfLayerablesTransferable;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.DummyClipboardOwner;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.LayerableClipboardPlugIn;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class CopySelectedLayersPlugIn
extends LayerableClipboardPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.clipboard.CopySelectedLayersPlugIn.name");
    public static final Icon ICON = IconLoader.icon("copyLayer.gif");

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
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new CollectionOfLayerablesTransferable(this.clone(context.getLayerNamePanel().selectedNodes(Layerable.class))), new DummyClipboardOwner());
        return true;
    }

    private Collection<Layerable> clone(Collection<Layerable> layerables) {
        ArrayList<Layerable> clones = new ArrayList<Layerable>();
        for (Layerable layerable : layerables) {
            if (!(layerable instanceof Layer) && !(layerable instanceof WMSLayer)) continue;
            clones.add(this.cloneLayerable(layerable));
        }
        return clones;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayerablesMustBeSelectedCheck(1, Layerable.class));
    }

    @Override
    public EnableCheck getCheck() {
        return CopySelectedLayersPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

