/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.OrderTranslatorEntityList;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class ProxyListSelectionModel
extends DefaultListSelectionModel
implements ListSelectionModel,
ListSelectionListener {
    private static final long serialVersionUID = 1L;
    ListSelectionModel baseModel = null;
    OrderTranslatorEntityList proxyList = null;

    public ProxyListSelectionModel() {
    }

    public ProxyListSelectionModel(ListSelectionModel m, OrderTranslatorEntityList p) {
        this.setSelectionModel(m);
        this.setOrderTranslatorEntityList(p);
    }

    public ListSelectionModel getSelectionModel() {
        return this.baseModel;
    }

    public void setSelectionModel(ListSelectionModel m) {
        this.baseModel = m;
        this.baseModel.addListSelectionListener(this);
    }

    public OrderTranslatorEntityList getOrderTranslatorEntityList() {
        return this.proxyList;
    }

    public void setOrderTranslatorEntityList(OrderTranslatorEntityList p) {
        this.proxyList = p;
    }

    @Override
    public void setAnchorSelectionIndex(int anchor) {
        this.baseModel.setAnchorSelectionIndex(this.proxyList.getViewIndex(anchor));
    }

    @Override
    public void setSelectionInterval(int anchor, int lead) {
        this.baseModel.setSelectionInterval(this.proxyList.getViewIndex(anchor), this.proxyList.getViewIndex(lead));
    }

    @Override
    public void setValueIsAdjusting(boolean adjusting) {
        this.baseModel.setValueIsAdjusting(adjusting);
    }

    @Override
    public int getLeadSelectionIndex() {
        return this.proxyList.getEntityIndex(this.baseModel.getLeadSelectionIndex());
    }

    @Override
    public void removeIndexInterval(int anchor, int lead) {
        this.baseModel.removeIndexInterval(this.proxyList.getViewIndex(anchor), this.proxyList.getEntityIndex(lead));
    }

    @Override
    public void setLeadSelectionIndex(int lead) {
        this.baseModel.setLeadSelectionIndex(this.proxyList.getViewIndex(lead));
    }

    @Override
    public boolean getValueIsAdjusting() {
        return this.baseModel.getValueIsAdjusting();
    }

    @Override
    public void clearSelection() {
        this.baseModel.clearSelection();
    }

    @Override
    public boolean isSelectedIndex(int index) {
        return this.baseModel.isSelectedIndex(this.proxyList.getViewIndex(index));
    }

    @Override
    public void addSelectionInterval(int anchor, int lead) {
        this.baseModel.addSelectionInterval(this.proxyList.getViewIndex(anchor), this.proxyList.getEntityIndex(lead));
    }

    @Override
    public void removeSelectionInterval(int anchor, int lead) {
        this.baseModel.removeSelectionInterval(this.proxyList.getViewIndex(anchor), this.proxyList.getEntityIndex(lead));
    }

    @Override
    public void setSelectionMode(int method) {
        this.baseModel.setSelectionMode(method);
    }

    @Override
    public int getAnchorSelectionIndex() {
        return this.proxyList.getEntityIndex(this.baseModel.getAnchorSelectionIndex());
    }

    @Override
    public void insertIndexInterval(int start, int length, boolean before) {
        this.baseModel.insertIndexInterval(this.proxyList.getViewIndex(start), this.proxyList.getViewIndex(length), before);
    }

    @Override
    public int getSelectionMode() {
        return this.baseModel.getSelectionMode();
    }

    @Override
    public boolean isSelectionEmpty() {
        return this.baseModel.isSelectionEmpty();
    }

    @Override
    public int getMinSelectionIndex() {
        if (this.baseModel.isSelectionEmpty()) {
            return -1;
        }
        int min = this.proxyList.getCount();
        int i = 0;
        while (i <= this.proxyList.getCount()) {
            if (this.baseModel.isSelectedIndex(i) && this.proxyList.getEntityIndex(i) < min) {
                min = this.proxyList.getEntityIndex(i);
            }
            ++i;
        }
        return min;
    }

    @Override
    public int getMaxSelectionIndex() {
        if (this.baseModel.isSelectionEmpty()) {
            return -1;
        }
        int max = -1;
        int i = 0;
        while (i <= this.proxyList.getCount()) {
            if (this.baseModel.isSelectedIndex(i) && this.proxyList.getEntityIndex(i) > max) {
                max = this.proxyList.getEntityIndex(i);
            }
            ++i;
        }
        return max;
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        this.fireValueChanged(evt.getFirstIndex(), evt.getLastIndex(), evt.getValueIsAdjusting());
    }
}

