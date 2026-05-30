/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import java.util.ArrayList;
import java.util.List;

public class InputChangedFirer {
    private List<InputChangedListener> inputChangedListeners = new ArrayList<InputChangedListener>();

    public void add(InputChangedListener listener) {
        this.inputChangedListeners.add(listener);
    }

    public void remove(InputChangedListener listener) {
        this.inputChangedListeners.remove(listener);
    }

    public void fire() {
        for (InputChangedListener listener : this.inputChangedListeners) {
            listener.inputChanged();
        }
    }
}

