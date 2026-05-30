/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.ui.RecursiveListener;
import java.awt.Component;
import java.awt.event.KeyListener;

public abstract class RecursiveKeyListener
extends RecursiveListener
implements KeyListener {
    public RecursiveKeyListener(Component component) {
        super(component);
    }

    @Override
    public void addListenerTo(Component comp) {
        comp.addKeyListener(this);
    }

    @Override
    public void removeListenerFrom(Component comp) {
        comp.removeKeyListener(this);
    }
}

