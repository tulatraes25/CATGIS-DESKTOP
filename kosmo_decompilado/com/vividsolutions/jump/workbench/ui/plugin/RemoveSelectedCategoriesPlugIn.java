/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.util.OrderedMap;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.undo.UndoManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class RemoveSelectedCategoriesPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.RemoveSelectedCategoriesPlugIn.name");
    public static final Icon ICON = IconLoader.icon("category_remove.png");

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
        return RemoveSelectedCategoriesPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.execute(this.toCategorySpecToLayerablesMap(this.toOrderedCategories(context.getLayerNamePanel().getSelectedCategories())), RemoveSelectedCategoriesPlugIn.pickUnselectedCategory(context.getLayerNamePanel(), context.getLayerManager()), context);
        return true;
    }

    private static Category pickUnselectedCategory(LayerNamePanel layerNamePanel, LayerManager layerManager) {
        Collection<Category> selectedCategories = layerNamePanel.getSelectedCategories();
        Category workingCategory = layerManager.getCategory(StandardCategoryNames.WORKING);
        if (workingCategory != null && !selectedCategories.contains(workingCategory)) {
            return workingCategory;
        }
        for (Category category : layerManager.getCategories()) {
            if (selectedCategories.contains(category)) continue;
            return category;
        }
        return null;
    }

    private List<Category> toOrderedCategories(Collection<Category> unorderedCategories) {
        ArrayList<Category> orderedCategories = new ArrayList<Category>(unorderedCategories);
        Collections.sort(orderedCategories, new Comparator<Category>(){

            @Override
            public int compare(Category c1, Category c2) {
                return new Integer(c1.getLayerManager().indexOf(c1)).compareTo(new Integer(c2.getLayerManager().indexOf(c2)));
            }
        });
        return orderedCategories;
    }

    private OrderedMap<CategorySpec, List<Layerable>> toCategorySpecToLayerablesMap(List<Category> selectedCategoriesInOrder) {
        OrderedMap<CategorySpec, List<Layerable>> map = new OrderedMap<CategorySpec, List<Layerable>>();
        for (Category category : selectedCategoriesInOrder) {
            map.put(new CategorySpec(category.getName(), category.getLayerManager().indexOf(category)), new ArrayList<Layerable>(category.getLayerables()));
        }
        return map;
    }

    private void execute(OrderedMap<CategorySpec, List<Layerable>> originalCategorySpecToLayerablesMap, Category newCategory, PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        for (CategorySpec originalCategorySpec : originalCategorySpecToLayerablesMap.keyList()) {
            List<Layerable> layers = originalCategorySpecToLayerablesMap.get(originalCategorySpec);
            for (Layerable layerable : layers) {
                int option;
                if (layerable instanceof Layer && (option = this.askCommitIfLayerIsEditable((Layer)layerable, context)) == 2) {
                    return;
                }
                context.getLayerManager().remove(layerable);
                layerable.dispose();
            }
            context.getLayerManager().removeIfEmpty(context.getLayerManager().getCategory(originalCategorySpec.name));
        }
    }

    private int askCommitIfLayerIsEditable(Layer layer, PlugInContext context) {
        int option = -1;
        if (layer.isFeatureCollectionModified()) {
            option = DialogFactory.showYesNoCancelDialog(context.getWorkbenchFrame(), I18N.getMessage("workbench.ui.plugin.EditablePlugIn.do-you-want-to-save-the-changes-made-to-the-layer-{0}", new Object[]{layer.getName()}), I18N.getString("workbench.ui.plugin.EditablePlugIn.save-changes"));
            if (option == 2) {
                return option;
            }
            if (option == 0) {
                try {
                    layer.getFeatureCollectionWrapper().getUltimateWrappee().commit();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    DialogFactory.showErrorDialog(context.getWorkbenchFrame(), String.valueOf(I18N.getMessage("workbench.ui.plugin.EditablePlugIn.an-unexpected-error-have-been-produced-while-saving-the-changes-in-the-layer-{0}", new Object[]{layer.getName()})) + ".\n" + e.getMessage(), I18N.getString("workbench.ui.plugin.EditablePlugIn.error-saving-changes"));
                    return option;
                }
            } else {
                layer.getFeatureCollectionWrapper().getUltimateWrappee().rollBack();
                UndoManager undoManager = ((LayerManagerProxy)((Object)context.getWorkbenchContext().getWorkbench().getFrame().getActiveInternalFrame())).getLayerManager().getUndoableEditReceiver().getUndoManager();
                while (undoManager.canUndo()) {
                    try {
                        undoManager.undo();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                undoManager.discardAllEdits();
            }
            layer.setFeatureCollectionModified(false);
            layer.setEditable(false);
            SelectionManager selectManager = context.getWorkbenchContext().getLayerViewPanel().getSelectionManager();
            if (selectManager != null) {
                selectManager.unselectItems(layer);
            }
        }
        return option;
    }

    public static MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNCategoriesMustBeSelectedCheck(1)).add(checkFactory.createSelectedCategoriesMustNotContainInternalLayersCheck()).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return RemoveSelectedCategoriesPlugIn.pickUnselectedCategory(workbenchContext.getLayerNamePanel(), workbenchContext.getLayerManager()) == null ? I18N.getString("workbench.ui.plugin.RemoveSelectedCategoriesPlugIn.at-least-1-category-must-be-left-unselected") : null;
            }
        });
    }

    private static class CategorySpec {
        private int index;
        private String name;

        public CategorySpec(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }
}

