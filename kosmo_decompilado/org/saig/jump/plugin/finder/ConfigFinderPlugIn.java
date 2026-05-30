/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.finder;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.finder.ConfigFinderController;
import org.saig.jump.widgets.finder.ConfigFinderJDialog;
import org.saig.jump.widgets.finder.FinderWindowManager;

public class ConfigFinderPlugIn
extends AbstractPlugIn {
    public static String NAME = I18N.getString("org.saig.jump.plugin.finder.ConfigFinderPlugIn.Configure-finder");
    public static Icon ICON = IconLoader.icon("finderConfig.png");

    public Layer getSelectedLayer() {
        Layerable[] layers = JUMPWorkbench.getFrameInstance().getContext().getLayerNamePanel().getSelectedLayers();
        if (layers.length > 0) {
            return (Layer)layers[0];
        }
        return null;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Layer layer = this.getSelectedLayer();
        if (layer != null) {
            ConfigFinderJDialog dialog = FinderWindowManager.getConFinderJDialog();
            ConfigFinderController controller = FinderWindowManager.getConfigFinderController();
            controller.refresh(layer);
            GUIUtil.centreOnScreen(dialog);
            dialog.setVisible(true);
            if (controller.wasOkPressed()) {
                layer.setFinderFields(controller.getResult());
            }
        }
        return true;
    }

    @Override
    public EnableCheck getCheck() {
        return ConfigFinderPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
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
}

