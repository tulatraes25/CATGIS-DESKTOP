/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.sdi.wms;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import es.kosmo.desktop.widgets.sdi.wms.ViewWMSLegendDialog;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.images.PrintIconLoader;

public class ViewWMSLegendPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.wms.ViewWMSLegendPlugIn.name");
    public static final Icon ICON = PrintIconLoader.icon("addLegend.gif");

    @Override
    public boolean execute(PlugInContext context) {
        Layerable[] layers = context.getLayerNamePanel().getSelectedLayers();
        if (layers.length > 0) {
            WMSLayer layer = (WMSLayer)layers[0];
            new ViewWMSLegendDialog(JUMPWorkbench.getFrameInstance(), false, layer);
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
        return ViewWMSLegendPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createExactlyNLayerablesMustBeSelectedCheck(1, WMSLayer.class));
        solucion.add(checkFactory.createSelectedLayerMustBeActiveCheck());
        return solucion;
    }
}

