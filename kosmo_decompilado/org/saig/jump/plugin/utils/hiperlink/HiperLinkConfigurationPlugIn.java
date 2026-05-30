/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils.hiperlink;

import com.vividsolutions.jump.feature.HiperLink;
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
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.hiperlink.HiperLinkConfigurationDialog;

public class HiperLinkConfigurationPlugIn
extends AbstractPlugIn {
    public static final String NAME = String.valueOf(I18N.getString("org.saig.jump.plugin.utils.hiperlink.HiperLinkConfigurationPlugIn.hiperlink-configuration")) + "...";
    public static final Icon ICON = IconLoader.icon("rayo.gif");

    @Override
    public boolean execute(PlugInContext context) {
        Layerable[] layers = context.getLayerNamePanel().getSelectedLayers();
        if (layers != null) {
            Layer layer = (Layer)layers[0];
            HiperLinkConfigurationDialog dialog = new HiperLinkConfigurationDialog(context.getWorkbenchFrame(), context.getWorkbenchContext(), true, layer.getHiperLink());
            if (dialog.isOk()) {
                HiperLink hiperLink = dialog.getHiperLink();
                layer.setHiperLink(hiperLink);
            }
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

    @Override
    public EnableCheck getCheck() {
        return HiperLinkConfigurationPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck check = new MultiEnableCheck();
        EnableCheckFactory cf = new EnableCheckFactory(workbenchContext);
        check.add(cf.createTaskWindowMustBeActiveCheck());
        check.add(cf.createExactlyNLayersMustBeSelectedCheck(1));
        check.add(cf.createSelectedLayersMustBeNoInternals());
        check.add(cf.createSelectedLayersMustNotBeAppInternalSystemLayersCheck());
        check.add(cf.createSelectedLayersMustNotBeRasterCheck());
        check.add(cf.createSelectedLayerMustBeActiveCheck());
        return check;
    }
}

