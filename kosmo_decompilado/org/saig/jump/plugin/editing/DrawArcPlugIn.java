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
import javax.swing.ImageIcon;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.DrawArcTool;
import org.saig.jump.widgets.tools.editing.ArcDialog;

public class DrawArcPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.DrawArcPlugIn.Draw-arc");
    public static final ImageIcon ICON = IconLoader.icon("drawArc.png");
    protected QuasimodeTool quasimodeTool;
    protected DrawArcTool dcp = new DrawArcTool();

    public DrawArcPlugIn() {
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
        return DrawArcPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(checkFactory.createTaskWindowMustBeActiveCheck());
        check.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        check.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{3, 2, 15}));
        return check;
    }

    protected void setDefaultOption(ArcDialog dialog) {
        dialog.getJrbindicarRaton().doClick();
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        ArcDialog cd = new ArcDialog(JUMPWorkbench.getFrameInstance());
        this.setDefaultOption(cd);
        cd.setVisible(true);
        if (!cd.cancelado) {
            if (cd.raton) {
                context.getLayerViewPanel().setCurrentCursorTool(this.quasimodeTool);
            } else if (cd.absoluto) {
                double x = cd.x;
                double y = cd.y;
                double r = cd.r;
                double a1 = cd.a1 * Math.PI / 180.0;
                double a2 = cd.a2 * Math.PI / 180.0;
                this.quasimodeTool.activate(context.getLayerViewPanel());
                this.dcp.save(new Coordinate(x, y), r, a1, a2, context.getLayerViewPanel());
                this.quasimodeTool.deactivate();
            }
        }
        return true;
    }
}

