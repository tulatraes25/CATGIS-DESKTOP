/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.config;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.CategoryEventType;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.core.gui.swing.locale.TranslatableSelectionDialog;
import org.saig.jump.lang.I18N;

public class ChangeCategoryLanguagePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.config.ChangeCategoryLanguagePlugIn.Translate-category-name");
    public static final Icon ICON = IconLoader.icon("category_translate.png");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Category selectedCategory = context.getLayerNamePanel().getSelectedCategories().iterator().next();
        TranslatableSelectionDialog translatableSelectionDialog = new TranslatableSelectionDialog(JUMPWorkbench.getFrameInstance(), true, I18N.getMessage("org.saig.jump.plugin.config.ChangeCategoryLanguagePlugIn.Translations-for-the-category-{0}", new Object[]{selectedCategory.getName()}), selectedCategory);
        if (translatableSelectionDialog.isOk()) {
            selectedCategory.fireCategoryChanged(CategoryEventType.METADATA_CHANGED);
        }
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

    @Override
    public EnableCheck getCheck() {
        return ChangeCategoryLanguagePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck multiCheck = new MultiEnableCheck();
        multiCheck.add(checkFactory.createExactlyNCategoriesMustBeSelectedCheck(1));
        return multiCheck;
    }
}

