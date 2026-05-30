/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.Arrays;
import java.util.List;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.selecting.CalculateSelectionPlugIn;

public class SelectFeaturesTool
extends SelectTool {
    public static final String NAME = I18N.getString("workbench.ui.cursortool.SelectFeaturesTool.name");
    public static final Icon ICON = IconLoader.icon("Select.gif");

    public SelectFeaturesTool() {
        super("SELECTED_FEATURES");
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
    public void activate(LayerViewPanel layerViewPanel) {
        super.activate(layerViewPanel);
        if (layerViewPanel != null && layerViewPanel.getSelectionManager() != null) {
            this.selection = layerViewPanel.getSelectionManager().getFeatureSelection();
        }
    }

    @Override
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        CalculateSelectionPlugIn calculateSelectionPlugIn = new CalculateSelectionPlugIn(NAME, this.wasShiftPressed(), EnvelopeUtil.toGeometry(this.getBoxInModelCoordinates()), this.selection, this.getLayerNameFilter());
        calculateSelectionPlugIn.execute(JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
    }

    protected List<Layerable> getLayerNameFilter() {
        return Arrays.asList(JUMPWorkbench.getFrameInstance().getContext().getWorkbench().getContext().getLayerNamePanel().getSelectedLayers());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext, CursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck(tool);
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        solucion.add(checkFactory.createSelectedLayersMustNotBeRasterCheck());
        solucion.add(checkFactory.createSelectedLayerMustBeActiveCheck());
        return solucion;
    }
}

