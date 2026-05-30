/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JList;

public class SortedList<T>
extends JList {
    private static final long serialVersionUID = 1L;
    private Comparator<T> comparator;

    public SortedList(Comparator<T> comparator) {
        this(new DefaultListModel(), comparator);
    }

    public SortedList(Vector<T> listData, Comparator<T> compare) {
        this(new DefaultListModel(), compare);
        DefaultListModel model = (DefaultListModel)this.getModel();
        int size = listData.size();
        int x = 0;
        while (x < size) {
            model.addElement(listData.elementAt(x));
            ++x;
        }
    }

    public SortedList(T[] listData, Comparator<T> compare) {
        this(new DefaultListModel(), compare);
        DefaultListModel model = (DefaultListModel)this.getModel();
        int size = listData.length;
        int x = 0;
        while (x < size) {
            model.addElement(listData[x]);
            ++x;
        }
    }

    public SortedList(DefaultListModel listData, Comparator<T> compare) {
        this.setModel(listData);
        this.comparator = compare;
    }

    public void add(T element, boolean bSort) {
        DefaultListModel model = (DefaultListModel)this.getModel();
        if (!bSort) {
            model.addElement(element);
        } else {
            int insertionPoint = this.findInsertionPoint(element);
            model.insertElementAt(element, insertionPoint);
        }
    }

    public void removeAllElements() {
        DefaultListModel model = (DefaultListModel)this.getModel();
        model.removeAllElements();
    }

    private int findInsertionPoint(T element) {
        DefaultListModel model = (DefaultListModel)this.getModel();
        int size = model.getSize();
        Vector list = new Vector();
        int x = 0;
        while (x < size) {
            Object o = model.get(x);
            list.addElement(o);
            ++x;
        }
        int insertionPoint = Collections.binarySearch(list, element, this.comparator);
        if (insertionPoint < 0) {
            insertionPoint = -(insertionPoint + 1);
        }
        return insertionPoint;
    }

    public void sort() {
        DefaultListModel model = (DefaultListModel)this.getModel();
        int size = model.getSize();
        Vector list = new Vector();
        int x = 0;
        while (x < size) {
            Object o = model.get(x);
            list.addElement(o);
            ++x;
        }
        Collections.sort(list, this.comparator);
        x = 0;
        while (x < size) {
            if (model.getElementAt(x) != list.elementAt(x)) {
                model.set(x, list.elementAt(x));
            }
            ++x;
        }
    }
}

