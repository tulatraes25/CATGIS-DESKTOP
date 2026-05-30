/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.ThreeCirclesTriangleTool;
import org.saig.jump.tools.editing.TwoCirclesTriangleTool;
import org.saig.jump.widgets.tools.editing.TriangleDialog;

public class TrianglePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.TrianglePlugIn.Triangle");
    public static final ImageIcon ICON = IconLoader.icon("traingulacion.gif");
    private ThreeCirclesTriangleTool ttt = new ThreeCirclesTriangleTool();
    private TwoCirclesTriangleTool tctt = new TwoCirclesTriangleTool();

    @Override
    public void initialize(PlugInContext context) {
        this.reportNothingToUndoYet(context);
    }

    @Override
    public boolean execute(PlugInContext context) {
        this.reportNothingToUndoYet(context);
        TriangleDialog td = new TriangleDialog(context.getWorkbenchFrame());
        td.setVisible(true);
        if (td.twoCircles) {
            this.tctt.setRatio(td.rat1, td.rat2);
            context.getLayerViewPanel().setCurrentCursorTool(this.tctt);
        } else {
            this.ttt.setRatios(td.rat1, td.rat2, td.rat3);
            context.getLayerViewPanel().setCurrentCursorTool(this.ttt);
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

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        solucion.add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
        solucion.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{1, 8, 15}));
        return solucion;
    }

    @Override
    public EnableCheck getCheck() {
        return TrianglePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

