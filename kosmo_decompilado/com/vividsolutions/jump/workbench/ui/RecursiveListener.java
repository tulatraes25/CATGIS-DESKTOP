/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

public abstract class RecursiveListener
implements ContainerListener {
    public RecursiveListener(Component component) {
        this.listenTo(component);
    }

    @Override
    public void componentRemoved(ContainerEvent evt) {
        Component comp = evt.getChild();
        this.ignore(comp);
    }

    @Override
    public void componentAdded(ContainerEvent evt) {
        Component comp = evt.getChild();
        this.listenTo(comp);
    }

    public void listenTo(Component comp) {
        this.addListenerTo(comp);
        if (comp instanceof Container) {
            Container container = (Container)comp;
            container.addContainerListener(this);
            Component[] components = container.getComponents();
            int i = 0;
            while (i < container.getComponentCount()) {
                this.listenTo(components[i]);
                ++i;
            }
        }
    }

    public void ignore(Component comp) {
        this.removeListenerFrom(comp);
        if (comp instanceof Container) {
            Container container = (Container)comp;
            container.removeContainerListener(this);
            Component[] components = container.getComponents();
            int i = 0;
            while (i < container.getComponentCount()) {
                this.ignore(components[i]);
                ++i;
            }
        }
    }

    public abstract void addListenerTo(Component var1);

    public abstract void removeListenerFrom(Component var1);
}

