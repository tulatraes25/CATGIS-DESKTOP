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
import java.awt.event.KeyListener;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.ExtendRectLineTool;
import org.saig.jump.tools.editing.ExtendToClickedGeometryTool;
import org.saig.jump.tools.editing.ExtendToDrawnLineTool;
import org.saig.jump.widgets.util.DialogFactory;

public class ExtendLinePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.ExtendLinePlugIn.Extend-line");
    public static final Icon ICON = IconLoader.icon("extentLine.png");
    public static final String NEARBY_OPTION = I18N.getString("org.saig.jump.plugin.editing.ExtendLinePlugIn.Nearby");
    public static final String DRAWN_OPTION = I18N.getString("org.saig.jump.plugin.editing.ExtendLinePlugIn.Drawn");
    public static final String SELECTED_OPTION = I18N.getString("org.saig.jump.plugin.editing.ExtendLinePlugIn.Selected");
    public static final String CANCEL_OPTION = I18N.getString("org.saig.jump.plugin.editing.ExtendLinePlugIn.Cancel");
    protected KeyListener keyListener;
    protected ExtendRectLineTool elt;
    protected ExtendToDrawnLineTool etdlt;
    protected ExtendToClickedGeometryTool etcgt;

    public ExtendLinePlugIn() {
        this.createTools();
    }

    public void createTools() {
        this.elt = new ExtendRectLineTool();
        this.etdlt = new ExtendToDrawnLineTool();
        this.etcgt = new ExtendToClickedGeometryTool();
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
    public boolean execute(PlugInContext context) {
        this.reportNothingToUndoYet(context);
        int selectedOption = this.showOptionDialog();
        if (selectedOption == 0) {
            context.getLayerViewPanel().setCurrentCursorTool(this.elt);
        } else if (selectedOption == 1) {
            context.getLayerViewPanel().setCurrentCursorTool(this.etdlt);
        } else if (selectedOption == 2) {
            context.getLayerViewPanel().setCurrentCursorTool(this.etcgt);
        }
        return selectedOption != 3;
    }

    protected int showOptionDialog() {
        Object[] options = new Object[]{NEARBY_OPTION, DRAWN_OPTION, SELECTED_OPTION, CANCEL_OPTION};
        Object option = DialogFactory.showOptionDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.jump.plugin.editing.ExtendLinePlugIn.Extend-line-with")) + "...", I18N.getString("org.saig.jump.plugin.editing.ExtendLinePlugIn.Extend-options"), options, options[0]);
        return ((Number)option).intValue();
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        solucion.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{3, 2, 15}));
        solucion.add(checkFactory.createExactlyNFeaturesMustBeSelectedCheck(new int[]{3, 2}, new int[]{9}, 1));
        return solucion;
    }

    @Override
    public EnableCheck getCheck() {
        return ExtendLinePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

