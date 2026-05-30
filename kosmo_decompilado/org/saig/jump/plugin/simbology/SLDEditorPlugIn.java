/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.simbology;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.style.ChangeStylesPlugIn;
import javax.swing.Icon;
import org.saig.core.styling.Rule;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.simbology.AbstractSLDEditorPlugIn;

public class SLDEditorPlugIn
extends AbstractSLDEditorPlugIn {
    public static final String NAME = String.valueOf(I18N.getString("org.saig.jump.plugin.editing.SLDEditorPlugIn.name")) + "...";
    public static final Icon ICON = IconLoader.icon("advancedStyleEditor.png");

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    protected Layer getLayer(PlugInContext context) {
        return (Layer)context.getSelectedLayer(0);
    }

    @Override
    protected Rule getRule(PlugInContext context) {
        return null;
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
        return ChangeStylesPlugIn.createEnableCheck(workbenchContext);
    }

    @Override
    public EnableCheck getCheck() {
        return SLDEditorPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

