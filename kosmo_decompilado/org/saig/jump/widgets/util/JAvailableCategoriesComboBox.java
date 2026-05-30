/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.LayerManager;
import java.util.List;
import java.util.TreeSet;
import javax.swing.JComboBox;

public class JAvailableCategoriesComboBox
extends JComboBox {
    private static final long serialVersionUID = 1L;
    private LayerManager layerManager;

    public JAvailableCategoriesComboBox(LayerManager layerManager) {
        this.layerManager = layerManager;
        this.refresh();
    }

    private void refresh() {
        List<Category> categories = this.layerManager.getCategories();
        TreeSet<String> categoryNames = new TreeSet<String>();
        for (Category currentCategory : categories) {
            categoryNames.add(currentCategory.getName());
        }
        this.removeAllItems();
        for (String currentCategoryName : categoryNames) {
            this.addItem(currentCategoryName);
        }
    }

    @Override
    public Object getSelectedItem() {
        Category selectedItem = null;
        Object categoryName = super.getSelectedItem();
        if (categoryName != null) {
            selectedItem = this.layerManager.getCategory((String)categoryName);
        }
        return selectedItem;
    }

    @Override
    public void setSelectedItem(Object selectedItem) {
        if (selectedItem == null || !(selectedItem instanceof Category)) {
            super.setSelectedItem(selectedItem);
        } else {
            super.setSelectedItem(((Category)selectedItem).getName());
        }
    }
}

