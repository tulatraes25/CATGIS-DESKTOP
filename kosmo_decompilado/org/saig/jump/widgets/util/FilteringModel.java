/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class FilteringModel<T>
extends AbstractListModel
implements DocumentListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FilteringModel.class);
    protected List<T> list;
    protected List<T> filteredList;
    protected String lastFilter = "";

    public FilteringModel() {
        this.list = new ArrayList<T>();
        this.filteredList = new ArrayList<T>();
    }

    public FilteringModel(Collection<T> elements) {
        this.list = new ArrayList<T>(elements);
        this.filteredList = new ArrayList<T>(elements);
    }

    public void addElement(T element) {
        this.list.add(element);
        this.filter(this.lastFilter);
    }

    public void filter(String search) {
        this.filteredList.clear();
        for (T element : this.list) {
            if (!this.elementContainString(element, search)) continue;
            this.filteredList.add(element);
        }
        this.fireContentsChanged(this, 0, this.getSize());
    }

    protected boolean elementContainString(T element, String search) {
        boolean encontrado = false;
        if (element != null) {
            encontrado = StringUtils.contains((String)element.toString(), (String)search);
        }
        return encontrado;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    @Override
    public T getElementAt(int index) {
        T returnValue = index < this.filteredList.size() ? (T)this.filteredList.get(index) : null;
        return returnValue;
    }

    @Override
    public int getSize() {
        return this.filteredList.size();
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        this.executeFilter(event.getDocument());
    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        this.executeFilter(event.getDocument());
    }

    private void executeFilter(Document doc) {
        try {
            this.lastFilter = doc.getText(0, doc.getLength());
            this.filter(this.lastFilter);
        }
        catch (BadLocationException ble) {
            LOGGER.error((Object)"", (Throwable)ble);
        }
    }

    public List<T> getFullList() {
        return Collections.unmodifiableList(this.list);
    }

    public List<T> getFilteredList() {
        return Collections.unmodifiableList(this.filteredList);
    }
}

