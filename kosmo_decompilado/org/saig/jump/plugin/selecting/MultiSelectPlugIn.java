/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.selecting;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.List;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.selecting.MultiSelectDialog;

public class MultiSelectPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.selecting.MultiSelectPlugIn.name");
    public static final Icon ICON = IconLoader.icon("configMultiSelect.gif");
    private static Layer sourceLayer;
    private static Object[] targetLayers;
    private static String selectedOperation;

    @Override
    public boolean execute(PlugInContext context) {
        LayerViewPanel layerViewPanel = context.getLayerViewPanel();
        List<Layer> layers = layerViewPanel.getLayerManager().getLayers();
        MultiSelectDialog dialog = new MultiSelectDialog(context.getWorkbenchFrame(), layers, true);
        if (dialog.isOk()) {
            sourceLayer = dialog.getSourceLayer();
            if (dialog.getTargetLayers() != null) {
                targetLayers = dialog.getTargetLayers();
                selectedOperation = dialog.getSelectedOperation();
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

    public static Layer getSourceLayer() {
        return sourceLayer;
    }

    public static Object[] getTargetLayers() {
        return targetLayers;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNLayersMustExistCheck(2));
        return solucion;
    }

    public static String getSelectedOperation() {
        return selectedOperation;
    }

    @Override
    public EnableCheck getCheck() {
        return MultiSelectPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

