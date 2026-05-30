/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jump.workbench.model.Category;
import java.util.ArrayList;
import java.util.List;

public class CategoryCollection {
    private String name = "";
    private List<Category> categories = new ArrayList<Category>();

    public List<Category> getCategories() {
        return this.categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String newName) {
        this.name = newName;
    }
}

