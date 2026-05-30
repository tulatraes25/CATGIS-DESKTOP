/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.addremove;

import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveListModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;

public class DefaultAddRemoveListModel<T>
implements AddRemoveListModel<T> {
    private DefaultListModel listModel;
    private boolean sorted = false;

    public DefaultAddRemoveListModel(DefaultListModel listModel) {
        this(listModel, false);
    }

    public DefaultAddRemoveListModel(DefaultListModel listModel, boolean sorted) {
        this.listModel = listModel;
        this.sorted = sorted;
    }

    public ListModel getListModel() {
        return this.listModel;
    }

    @Override
    public void add(T item) {
        this.listModel.addElement(item);
        if (this.sorted) {
            this.sort();
        }
    }

    private void setItemsWithoutSorting(Collection<T> items) {
        this.listModel.clear();
        Iterator<T> i = items.iterator();
        while (i.hasNext()) {
            this.listModel.addElement(i.next());
        }
    }

    public void clear() {
        this.listModel.clear();
    }

    @Override
    public void setItems(Collection<T> items) {
        this.setItemsWithoutSorting(items);
        if (this.sorted) {
            this.sort();
        }
    }

    @Override
    public void sort() {
        ArrayList<T> items = new ArrayList<T>(this.getItems());
        Collections.sort(items);
        this.setItemsWithoutSorting(items);
    }

    @Override
    public List<T> getItems() {
        return Arrays.asList(this.listModel.toArray());
    }

    @Override
    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }

    @Override
    public void remove(T item) {
        this.listModel.removeElement(item);
    }

    @Override
    public boolean isSorted() {
        return this.sorted;
    }
}

