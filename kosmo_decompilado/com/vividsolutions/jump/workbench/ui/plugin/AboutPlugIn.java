/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.AboutDialog;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class AboutPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.AboutPlugIn.name");
    public static final Icon ICON = IconLoader.icon("blank.png");

    @Override
public boolean execute(PlugInContext context) throws Exception {
    this.reportNothingToUndoYet(context);
    System.out.println("Prueba AboutPlugIn OK");
    AboutDialog.instance(context.getWorkbenchContext()).setVisible(true);
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
}

