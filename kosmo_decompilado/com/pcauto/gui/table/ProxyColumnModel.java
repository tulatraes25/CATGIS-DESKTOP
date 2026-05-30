/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import com.pcauto.gui.table.EntityTableColumn;
import com.pcauto.gui.table.EntityTableColumnModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;

public class ProxyColumnModel
extends DefaultTableColumnModel
implements TableColumnModel,
TableColumnModelListener {
    private static final long serialVersionUID = 1L;
    protected EntityTableColumnModel origModel = null;

    public ProxyColumnModel() {
    }

    public ProxyColumnModel(EntityTableColumnModel m) {
        this();
        this.setModel(m);
    }

    public synchronized EntityTableColumnModel getModel() {
        return this.origModel;
    }

    public synchronized void setModel(EntityTableColumnModel m) {
        if (this.origModel != null) {
            while (this.getColumnCount() > 0) {
                this.removeColumn(this.getColumn(0));
            }
            this.origModel.removeColumnModelListener(this);
        }
        this.origModel = m;
        if (this.origModel != null) {
            this.origModel.addColumnModelListener(this);
            int j = 0;
            int i = 0;
            while (i < this.origModel.getColumnCount()) {
                if (!this.origModel.isHidden(i) && !this.origModel.isLocked(i)) {
                    this.addColumn(this.origModel.getColumn(i));
                    this.getColumn(j).setModelIndex(i);
                    this.getColumn(j).setHeaderValue(((EntityTableColumn)this.origModel.getColumn(i)).getName());
                    this.getColumn(j).setHeaderRenderer(((EntityTableColumn)this.origModel.getColumn(i)).getHeaderRenderer());
                    ++j;
                }
                ++i;
            }
        }
    }

    public void revalidate() {
        this.setModel(this.origModel);
    }

    protected int getRealColumnCount() {
        return this.getColumnCount();
    }

    @Override
    public void columnAdded(TableColumnModelEvent e) {
        super.fireColumnAdded(e);
    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
        super.fireColumnMarginChanged();
    }

    @Override
    public void columnMoved(TableColumnModelEvent e) {
        super.fireColumnMoved(e);
    }

    @Override
    public void columnRemoved(TableColumnModelEvent e) {
        super.fireColumnRemoved(e);
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
        super.fireColumnSelectionChanged(e);
    }
}

