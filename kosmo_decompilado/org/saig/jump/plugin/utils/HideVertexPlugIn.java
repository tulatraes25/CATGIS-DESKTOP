/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractSelectionRenderer;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class HideVertexPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.HideVertexPlugIn.show-hide-vertex");
    public static final Icon ICON_ENABLED = IconLoader.icon("GreenPinPushedIn.gif");
    public static final Icon ICON_DISABLED = IconLoader.icon("RedPinPushedIn.gif");
    private boolean alwaysOnTop = true;

    @Override
    public void initialize(PlugInContext context) throws Exception {
        this.alwaysOnTop = AbstractSelectionRenderer.paintingHandleOn;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        AbstractSelectionRenderer.paintingHandleOn = this.alwaysOnTop = !this.alwaysOnTop;
        this.refreshSelection(context);
        return true;
    }

    private void refreshSelection(PlugInContext context) {
        context.getLayerViewPanel().getRenderingManager().render("SELECTED_FEATURES");
        context.getLayerViewPanel().getRenderingManager().render("SELECTED_LINESTRINGS");
        context.getLayerViewPanel().getRenderingManager().render("SELECTED_PARTS");
        context.getLayerViewPanel().getRenderingManager().render("SELECTION_BACKGROUND");
        context.getLayerViewPanel().getRenderingManager().render("SELECTED_SEGMENTS");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        if (this.alwaysOnTop) {
            return ICON_ENABLED;
        }
        return ICON_DISABLED;
    }
}

