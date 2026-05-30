/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class AddNewCategoryPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.AddNewCategoryPlugIn.name");
    private static final String NEW_CATEGORY_NAME = I18N.getString("workbench.ui.plugin.AddNewCategoryPlugIn.new-category");
    public static final Icon ICON = IconLoader.icon("category_new.png");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean execute(final PlugInContext context) throws Exception {
        final String categoryName = this.findNewCategoryName(context.getLayerManager());
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() {
                context.getLayerManager().addCategory(categoryName);
            }

            @Override
            public void unexecute() {
                Assert.isTrue((boolean)context.getLayerManager().getCategory(categoryName).isEmpty(), (String)I18N.getString("workbench.ui.plugin.AddNewCategoryPlugIn.this-can-happen-when-a-plugin-calls-to-reportnothingtoundoyet-but-adds-a-layer"));
                context.getLayerManager().removeIfEmpty(context.getLayerManager().getCategory(categoryName));
            }
        }, context);
        return true;
    }

    private String findNewCategoryName(LayerManager layerManager) {
        String newName;
        if (layerManager.getCategory(NEW_CATEGORY_NAME) == null) {
            return NEW_CATEGORY_NAME;
        }
        int i = 2;
        do {
            newName = String.valueOf(NEW_CATEGORY_NAME) + " (" + i + ")";
            ++i;
        } while (layerManager.getCategory(newName) != null);
        return newName;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return AddNewCategoryPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

