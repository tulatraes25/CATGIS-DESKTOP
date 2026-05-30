/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.util.Assert;
import java.util.ArrayList;
import java.util.List;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

public class UndoableEditReceiver {
    private UndoManager undoManager = new UndoManager();
    private List<UndoableEdit> newUndoableEdits = new ArrayList<UndoableEdit>();
    private int transactions = 0;
    private boolean nothingToUndoReported = false;
    private boolean irreversibleChangeReported = false;
    private boolean undoManagerCouldUndoAtStart = false;
    private List<Listener> listeners = new ArrayList<Listener>();

    public void startReceiving() {
        ++this.transactions;
        this.setNothingToUndoReported(false);
        this.irreversibleChangeReported = false;
        this.undoManagerCouldUndoAtStart = this.undoManager.canUndo();
        this.newUndoableEdits.clear();
    }

    public void reportNothingToUndoYet() {
        Assert.isTrue((boolean)this.isReceiving());
        this.setNothingToUndoReported(true);
    }

    public void reportIrreversibleChange() {
        Assert.isTrue((boolean)this.isReceiving());
        this.irreversibleChangeReported = true;
    }

    public void stopReceiving() {
        --this.transactions;
        try {
            if (this.newUndoableEdits.isEmpty() && !this.wasNothingToUndoReported() || this.irreversibleChangeReported) {
                this.undoManager.discardAllEdits();
                return;
            }
            for (UndoableEdit undoableEdit : this.newUndoableEdits) {
                this.undoManager.addEdit(undoableEdit);
            }
        }
        finally {
            this.fireUndoHistoryChanged();
            if (this.undoManagerCouldUndoAtStart && !this.undoManager.canUndo()) {
                this.fireUndoHistoryTruncated();
            }
        }
    }

    private void fireUndoHistoryTruncated() {
        for (Listener listener : this.listeners) {
            listener.undoHistoryTruncated();
        }
    }

    private void fireUndoHistoryChanged() {
        for (Listener listener : this.listeners) {
            listener.undoHistoryChanged();
        }
    }

    public void add(Listener listener) {
        this.listeners.add(listener);
    }

    public void receive(UndoableEdit undoableEdit) {
        Assert.isTrue((boolean)this.isReceiving());
        this.newUndoableEdits.add(undoableEdit);
    }

    public UndoManager getUndoManager() {
        return this.undoManager;
    }

    private void setNothingToUndoReported(boolean nothingToUndoReported) {
        this.nothingToUndoReported = nothingToUndoReported;
    }

    private boolean wasNothingToUndoReported() {
        return this.nothingToUndoReported;
    }

    public boolean isReceiving() {
        return this.transactions > 0;
    }

    public static interface Listener {
        public void undoHistoryChanged();

        public void undoHistoryTruncated();
    }
}

