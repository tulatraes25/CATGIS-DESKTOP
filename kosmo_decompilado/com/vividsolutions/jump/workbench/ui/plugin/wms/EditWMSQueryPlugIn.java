/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.wms;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPanel;
import com.vividsolutions.wms.MapLayer;
import javax.swing.Icon;
import javax.swing.JLabel;
import org.saig.jump.lang.I18N;

public class EditWMSQueryPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.wms.EditWMSQueryPlugIn.Edit-WMS-query");
    public static final Icon ICON = IconLoader.icon("world_edit.png");

    public MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayerablesMustBeSelectedCheck(1, Layerable.class)).add(checkFactory.createSelectedLayerMustBeActiveCheck());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        WMSLayer layer = context.getLayerNamePanel().selectedNodes(WMSLayer.class).iterator().next();
        MultiInputDialog dialog = new MultiInputDialog(context.getWorkbenchFrame(), I18N.getString("ui.plugin.wms.EditWMSQueryPlugIn.edit-wms-query"), true);
        dialog.setInset(5);
        dialog.setSideBarImage(IconLoader.icon("toolImages/EditWMSLayer.jpg"));
        dialog.setSideBarDescription(I18N.getString("ui.plugin.wms.EditWMSQueryPlugIn.this-dialog-enables-you-to-change-the-layers-being-retrieved-from-a-web-map-server"));
        EditWMSQueryPanel panel = new EditWMSQueryPanel(layer.getService(), layer.getLayerNames(), layer.getSrs(), layer.getTime(), layer.getFormat(), layer.getAlpha(), layer.getInformationFormat(), layer.getExceptionFormat(), layer.isUseDeclaredCapabilitiesURLs(), dialog);
        dialog.addRow(I18N.getString("ui.plugin.wms.EditWMSQueryPlugIn.chosen-layers"), new JLabel(""), panel, panel.getEnableChecks(), null);
        dialog.pack();
        dialog.setVisible(true);
        if (dialog.wasOKPressed()) {
            layer.removeAllLayerNames();
            for (MapLayer mapLayer : panel.getChosenMapLayers()) {
                layer.addLayerName(mapLayer.getName());
            }
            layer.setFormat(panel.getFormat());
            layer.setSrs(panel.getSRS());
            layer.setTime(panel.getTime());
            layer.setTransparent(panel.isTransparent());
            layer.setAlpha(panel.getAlpha());
            layer.setInformationFormat(panel.getInformationFormat());
            layer.setInformationFeatureCount(panel.getInformationFeatureCount());
            layer.setExceptionFormat(panel.getExceptionFormat());
            layer.setUseDeclaredCapabilitiesURLs(panel.getUseDeclaredCapabilitiesURLs());
            layer.setVendorParameters(panel.getVendorParameters());
            layer.setAxisOrder(panel.getAxisOrder());
            layer.fireAppearanceChanged();
            return true;
        }
        return false;
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
        return this.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

