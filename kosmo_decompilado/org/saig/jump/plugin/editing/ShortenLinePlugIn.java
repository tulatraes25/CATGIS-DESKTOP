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
import org.saig.jump.tools.editing.ShortenLineTool;
import org.saig.jump.tools.editing.ShortenToClickedGeometryTool;
import org.saig.jump.tools.editing.ShortenToDrawnLineTool;
import org.saig.jump.widgets.util.DialogFactory;

public class ShortenLinePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.ShortenLinePlugIn.Shorten-line");
    public static final Icon ICON = IconLoader.icon("shortenLine.png");
    public static final String NEARBY_OPTION = I18N.getString("org.saig.jump.plugin.editing.ShortenLinePlugIn.Nearby");
    public static final String DRAWN_OPTION = I18N.getString("org.saig.jump.plugin.editing.ShortenLinePlugIn.Drawn");
    public static final String SELECTED_OPTION = I18N.getString("org.saig.jump.plugin.editing.ShortenLinePlugIn.Selected");
    public static final String CANCEL_OPTION = I18N.getString("org.saig.jump.plugin.editing.ShortenLinePlugIn.Cancel");
    protected KeyListener keyListener;
    protected ShortenLineTool slt;
    protected ShortenToDrawnLineTool stdlt;
    protected ShortenToClickedGeometryTool stcgt;

    public ShortenLinePlugIn() {
        this.createTools();
    }

    protected void createTools() {
        this.slt = new ShortenLineTool();
        this.stdlt = new ShortenToDrawnLineTool();
        this.stcgt = new ShortenToClickedGeometryTool();
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
        int n = this.showOptionDialog();
        if (n == 0) {
            context.getLayerViewPanel().setCurrentCursorTool(this.slt);
        } else if (n == 1) {
            context.getLayerViewPanel().setCurrentCursorTool(this.stdlt);
        } else if (n == 2) {
            context.getLayerViewPanel().setCurrentCursorTool(this.stcgt);
        }
        return n != 3;
    }

    protected int showOptionDialog() {
        Object[] options = new Object[]{NEARBY_OPTION, DRAWN_OPTION, SELECTED_OPTION, CANCEL_OPTION};
        Object option = DialogFactory.showOptionDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.jump.plugin.editing.ShortenLinePlugIn.Shorten-line-with")) + "...", I18N.getString("org.saig.jump.plugin.editing.ShortenLinePlugIn.Shorten-options"), options, options[0]);
        return ((Number)option).intValue();
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        solucion.add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
        solucion.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{3, 2, 15}));
        solucion.add(checkFactory.createExactlyNFeaturesMustBeSelectedCheck(new int[]{3, 2}, new int[]{9}, 1));
        return solucion;
    }

    @Override
    public EnableCheck getCheck() {
        return ShortenLinePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

