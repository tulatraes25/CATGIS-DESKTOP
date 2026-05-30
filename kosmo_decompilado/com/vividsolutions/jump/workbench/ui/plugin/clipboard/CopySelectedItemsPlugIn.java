/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.clipboard;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.CollectionOfFeaturesTransferable;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.DummyClipboardOwner;
import java.awt.Toolkit;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class CopySelectedItemsPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.clipboard.CopySelectedItemsPlugIn.name");
    public static final Icon ICON = IconLoader.icon("copy.gif");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new CollectionOfFeaturesTransferable(context.getWorkbenchContext().getLayerViewPanel().getSelectionManager().createFeaturesFromSelectedItems()), new DummyClipboardOwner());
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

    @Override
    public EnableCheck getCheck() {
        return CopySelectedItemsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithSelectionManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNItemsMustBeSelectedCheck(1));
    }
}

