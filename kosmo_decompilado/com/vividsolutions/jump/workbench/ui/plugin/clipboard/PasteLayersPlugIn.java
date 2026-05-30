/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.clipboard;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.CollectionOfLayerablesTransferable;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.LayerableClipboardPlugIn;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;

public class PasteLayersPlugIn
extends LayerableClipboardPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.clipboard.PasteLayersPlugIn.name");
    public static final Icon ICON = IconLoader.icon("pasteLayer.gif");

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
        Transferable transferable = GUIUtil.getContents(Toolkit.getDefaultToolkit().getSystemClipboard());
        if (!transferable.isDataFlavorSupported(CollectionOfLayerablesTransferable.COLLECTION_OF_LAYERABLES_FLAVOR)) {
            return false;
        }
        Collection layerables = (Collection)transferable.getTransferData(CollectionOfLayerablesTransferable.COLLECTION_OF_LAYERABLES_FLAVOR);
        Category selectedCategory = context.getLayerNamePanel().getSelectedCategories().iterator().next();
        for (Layerable layerable : layerables) {
            Layerable clone = this.cloneLayerable(layerable);
            clone.setLayerManager(context.getLayerManager());
            context.getLayerManager().addLayerable(selectedCategory.getName(), clone);
            clone.setName(context.getLayerManager().uniqueLayerName(clone.getName()));
        }
        return true;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNCategoriesMustBeSelectedCheck(1)).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Transferable transferable = GUIUtil.getContents(Toolkit.getDefaultToolkit().getSystemClipboard());
                if (transferable == null) {
                    return I18N.getString("workbench.ui.plugin.clipboard.PasteLayersPlugIn.clipboard-must-not-be-empty");
                }
                if (!transferable.isDataFlavorSupported(CollectionOfLayerablesTransferable.COLLECTION_OF_LAYERABLES_FLAVOR)) {
                    return I18N.getString("workbench.ui.plugin.clipboard.PasteLayersPlugIn.clipboard-contents-must-be-layers");
                }
                return null;
            }
        });
    }

    @Override
    public EnableCheck getCheck() {
        return PasteLayersPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

