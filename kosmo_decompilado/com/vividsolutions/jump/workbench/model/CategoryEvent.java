/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.CategoryEventType;

public class CategoryEvent {
    private Category category;
    private CategoryEventType type;
    private int categoryIndex;

    public CategoryEvent(Category category, CategoryEventType type, int index) {
        this.category = category;
        this.type = type;
        this.categoryIndex = index;
    }

    public CategoryEventType getType() {
        return this.type;
    }

    public Category getCategory() {
        return this.category;
    }

    public int getCategoryIndex() {
        return this.categoryIndex;
    }
}

