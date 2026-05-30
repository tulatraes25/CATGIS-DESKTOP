/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.dataComponents;

public interface DataComponent<T> {
    public void refresh();

    public T getValue();

    public void clear();
}

