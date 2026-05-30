/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;

public class ChangeDataSourceInMemoryPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.ChangeDataSourceInMemoryPlugIn.Layer-in-memory");
    public static final Icon ICON = IconLoader.icon("blank.png");

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
        return ChangeDataSourceInMemoryPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        return true;
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustBeNoInternals()).add(checkFactory.createSelectedLayersMustNotBeAppInternalSystemLayersCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck()).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayersMustNotBeFromMemoryCheck()).add(checkFactory.createSelectedLayerIsNotCAD()).add(checkFactory.createSelectedLayersMustNotBeWMSLayersCheck()).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                ((JCheckBoxMenuItem)component).setSelected(((Layer)workbenchContext.getLayerNamePanel().getSelectedLayers()[0]).isMemory());
                return null;
            }
        }).add(checkFactory.createSelectedLayersMustNotBeEditableCheck());
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        Layer layer = (Layer)context.getSelectedLayer(0);
        if (!layer.isMemory()) {
            monitor.report(I18N.getMessage("org.saig.jump.plugin.utils.ChangeDataSourceInMemoryPlugIn.Inserting-layer-{0}-into-memory", new Object[]{layer.getName()}));
        } else {
            monitor.report(I18N.getMessage("org.saig.jump.plugin.utils.ChangeDataSourceInMemoryPlugIn.Extracting-layer-{0}-from-memory", new Object[]{layer.getName()}));
        }
        layer.setMemory(!layer.isMemory());
    }
}

