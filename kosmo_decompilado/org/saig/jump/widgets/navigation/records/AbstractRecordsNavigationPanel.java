/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.navigation.records;

import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import org.saig.core.model.data.Record;
import org.saig.jump.widgets.navigation.listener.DataModifiedListener;
import org.saig.jump.widgets.navigation.records.AbstractRecordsNavigationForm;
import org.saig.jump.widgets.util.IFormTable;

public abstract class AbstractRecordsNavigationPanel
extends JPanel
implements IFormTable {
    private List<DataModifiedListener> dataModifiedListeners = new ArrayList<DataModifiedListener>();
    protected AbstractRecordsNavigationForm parentForm;

    public AbstractRecordsNavigationPanel(LayoutManager lm, AbstractRecordsNavigationForm parent) {
        super(lm);
        this.parentForm = parent;
    }

    @Override
    public abstract void disable();

    public abstract void update(Record var1) throws Exception;

    public void addDataModifiedListener(DataModifiedListener listener) {
        this.dataModifiedListeners.add(listener);
    }

    public void removeDataModifiedListener(DataModifiedListener listener) {
        this.dataModifiedListeners.remove(listener);
    }

    public void fireDataModified() {
        Iterator<DataModifiedListener> it = this.dataModifiedListeners.iterator();
        while (it.hasNext()) {
            it.next().dataModified();
        }
    }

    public abstract void compact(boolean var1);

    public AbstractRecordsNavigationForm getParentForm() {
        return this.parentForm;
    }

    public boolean beforeSave() {
        return true;
    }
}

