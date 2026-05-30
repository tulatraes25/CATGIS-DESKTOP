/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.addremove;

import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveListModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.tree.TreeModel;

public class TreeAddRemoveListModel<T>
implements AddRemoveListModel<T> {
    private TreeModel treeModel;

    public TreeAddRemoveListModel(TreeModel treeModel) {
        this.treeModel = treeModel;
    }

    public TreeModel getTreeModel() {
        return this.treeModel;
    }

    @Override
    public void add(T item) {
    }

    @Override
    public void setItems(Collection<T> items) {
    }

    @Override
    public List<T> getItems() {
        return new ArrayList();
    }

    @Override
    public void remove(Object item) {
    }

    @Override
    public void sort() {
    }

    @Override
    public boolean isSorted() {
        return false;
    }

    @Override
    public void setSorted(boolean sorted) {
    }
}

