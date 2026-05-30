/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print;

import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ElementsListModel<T>
implements ListModel {
    protected List<T> elementsList;
    protected ListDataListener l;

    public ElementsListModel(List<T> elementsList) {
        this.elementsList = elementsList;
    }

    @Override
    public int getSize() {
        return this.elementsList.size() + 1;
    }

    public T getElementAt(int i) {
        if (i == this.getSize() - 1) {
            return null;
        }
        return this.elementsList.get(this.elementsList.size() - (i + 1));
    }

    public void addElementAt(int i, T o) {
        this.elementsList.add(this.elementsList.size() - i, o);
        if (this.l != null) {
            this.l.contentsChanged(new ListDataEvent(this, 0, 0, this.elementsList.size()));
        }
    }

    public void removeElement(int i) {
        this.elementsList.remove(this.elementsList.size() - (i + 1));
        if (this.l != null) {
            this.l.contentsChanged(new ListDataEvent(this, 0, 0, this.elementsList.size()));
        }
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        this.l = l;
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        if (this.l.equals(l)) {
            this.l = null;
        }
    }

    public void contentsChanged() {
        if (this.l != null) {
            this.l.contentsChanged(new ListDataEvent(this, 0, 0, this.elementsList.size()));
        }
    }
}

