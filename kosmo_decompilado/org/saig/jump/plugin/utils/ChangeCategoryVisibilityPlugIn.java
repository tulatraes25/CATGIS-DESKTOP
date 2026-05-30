/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;

public class ChangeCategoryVisibilityPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.ChangeCategoryVisibilityPlugIn.visible");
    public static final Icon ICON = IconLoader.icon("Eye.gif");

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Collection<Category> selectedCategories = context.getLayerNamePanel().getSelectedCategories();
        boolean visibility = ChangeCategoryVisibilityPlugIn.checkVisibility(selectedCategories);
        for (Category cat : selectedCategories) {
            this.changeVisibility(cat, !visibility);
        }
        return true;
    }

    private void changeVisibility(Category category, boolean visibility) {
        for (Layerable layerable : category.getLayerables()) {
            if (layerable.isVisible() == visibility) continue;
            layerable.setVisible(visibility);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNCategoriesMustBeSelectedCheck(1)).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                ((JCheckBoxMenuItem)component).setSelected(ChangeCategoryVisibilityPlugIn.checkVisibility(workbenchContext.getLayerNamePanel().getSelectedCategories()));
                return null;
            }
        });
    }

    private static boolean checkVisibility(Collection categories) {
        boolean solucion = false;
        Iterator iter = categories.iterator();
        while (iter.hasNext() && !solucion) {
            Category cat = (Category)iter.next();
            Iterator<Layerable> iterator = cat.getLayerables().iterator();
            while (iterator.hasNext() && !solucion) {
                Layerable element = iterator.next();
                solucion = element.isVisible();
            }
        }
        return solucion;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return ChangeCategoryVisibilityPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

