/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public abstract class ChangeFirerPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    protected List<ChangeListener> listeners = new ArrayList<ChangeListener>();

    protected void firePanelChanged() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : this.listeners) {
            listener.stateChanged(event);
        }
    }

    public void addChangeListener(ChangeListener changeListener) {
        this.listeners.add(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        this.listeners.remove(changeListener);
    }
}

