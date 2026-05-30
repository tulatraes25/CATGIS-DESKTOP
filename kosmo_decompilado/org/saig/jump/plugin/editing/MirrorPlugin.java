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
import javax.swing.JOptionPane;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.MirrorLineTool;
import org.saig.jump.tools.editing.MirrorSegmentTool;

public class MirrorPlugin
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.MirrorPlugin.Symmetry");
    public static final Icon ICON = IconLoader.icon("symmetry.png");
    protected MirrorLineTool mrt = null;
    protected MirrorSegmentTool mst = null;

    public MirrorPlugin() {
        this.createTools();
    }

    protected void createTools() {
        this.mrt = new MirrorLineTool();
        this.mst = new MirrorSegmentTool();
    }

    @Override
    public boolean execute(PlugInContext context) {
        this.reportNothingToUndoYet(context);
        Object[] options = new Object[]{I18N.getString("org.saig.jump.plugin.editing.MirrorPlugin.Draw"), I18N.getString("org.saig.jump.plugin.editing.MirrorPlugin.Select"), I18N.getString("org.saig.jump.plugin.editing.MirrorPlugin.Cancel")};
        int n = JOptionPane.showOptionDialog(context.getWorkbenchFrame(), I18N.getString("org.saig.jump.plugin.editing.MirrorPlugin.Do-you-want-to-draw-the-symmetry-axis-or-select-it-from-another-geometry"), I18N.getString("org.saig.jump.plugin.editing.MirrorPlugin.Symmetry-options"), 1, 3, null, options, options[2]);
        if (n == 0) {
            context.getLayerViewPanel().setCurrentCursorTool(this.mrt);
        } else if (n == 1) {
            context.getLayerViewPanel().setCurrentCursorTool(this.mst);
        }
        return n != 2;
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
        solucion.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1));
        return solucion;
    }

    @Override
    public EnableCheck getCheck() {
        return MirrorPlugin.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

