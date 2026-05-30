/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.DrawCircleRatioTool;
import org.saig.jump.tools.editing.DrawCircleTool;
import org.saig.jump.widgets.tools.editing.CircleDialog;

public class DrawCirclePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.DrawCirclePlugIn.Draw-circle");
    public static final Icon ICON = IconLoader.icon("drawCircle.png");
    public static final int NUM_VERTICES = 48;
    protected QuasimodeTool quasimodeTool;
    protected DrawCircleTool dcp = new DrawCircleTool();

    public DrawCirclePlugIn() {
        this.quasimodeTool = QuasimodeTool.addStandardQuasimodes(this.dcp);
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
        return DrawCirclePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(checkFactory.createTaskWindowMustBeActiveCheck());
        check.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        check.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{3, 2, 5, 4, 15}));
        return check;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        CircleDialog cd = new CircleDialog(JUMPWorkbench.getFrameInstance());
        cd.setVisible(true);
        if (!cd.cancelado) {
            if (cd.raton) {
                context.getLayerViewPanel().setCurrentCursorTool(this.quasimodeTool);
            } else if (cd.radio) {
                double ratio = cd.r1;
                DrawCircleRatioTool crt = new DrawCircleRatioTool(ratio);
                context.getLayerViewPanel().setCurrentCursorTool(crt);
            } else if (cd.absoluto) {
                double x = cd.x;
                double y = cd.y;
                double r = cd.r2;
                this.quasimodeTool.activate(context.getLayerViewPanel());
                this.dcp.doCircle(new Coordinate(x, y), r);
                this.quasimodeTool.deactivate();
            }
        }
        return true;
    }
}

