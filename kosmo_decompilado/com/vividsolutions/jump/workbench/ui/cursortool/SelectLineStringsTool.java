/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class SelectLineStringsTool
extends SelectTool {
    public static final String NAME = I18N.getString("workbench.ui.cursortool.SelectLineStringsTool.name");
    public static final Icon ICON = IconLoader.icon("SelectLineString.gif");

    public SelectLineStringsTool() {
        super("SELECTED_LINESTRINGS");
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {
        super.activate(layerViewPanel);
        this.selection = layerViewPanel.getSelectionManager().getLineStringSelection();
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext, AbstractCursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck(tool);
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        solucion.add(checkFactory.createSelectedLayersMustNotBeRasterCheck());
        solucion.add(checkFactory.createSelectedLayerMustBeActiveCheck());
        return solucion;
    }
}

