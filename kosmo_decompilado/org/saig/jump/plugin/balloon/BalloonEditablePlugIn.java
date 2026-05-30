/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.balloon;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.Collection;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.balloon.BalloonEditingPlugIn;

public class BalloonEditablePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.BalloonEditablePlugIn.editing-tools");
    public static final Icon ICON = IconLoader.icon("Draw.gif");
    private BalloonEditingPlugIn editingPlugIn = new BalloonEditingPlugIn();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Collection<Layer> col = context.getWorkbenchContext().getLayerNamePanel().getLayerManager().getEditableLayers();
        TextBalloonLayer selectedLayer = (TextBalloonLayer)context.getSelectedLayer(0);
        for (Layer currentLayer : context.getLayerManager().getLayers()) {
            currentLayer.setEditable(false);
        }
        if (!this.editingPlugIn.getToolbox(context.getWorkbenchContext()).isVisible()) {
            this.editingPlugIn.execute(context);
        }
        this.resetCursorTool();
        return true;
    }

    private void resetCursorTool() {
        CursorTool tool = JUMPWorkbench.getFrameInstance().getToolBar().getDefaultEditingCursorTool();
        AbstractButton boton = JUMPWorkbench.getFrameInstance().getToolBar().getButton(tool.getClass());
        boton.doClick();
    }
}

