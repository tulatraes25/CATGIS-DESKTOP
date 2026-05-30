/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.lite;

public class ListenerList {
    private int capacity;
    private int size;
    private Object[] listeners = null;
    private static final Object[] EmptyArray = new Object[0];

    public ListenerList() {
        this(1);
    }

    public ListenerList(int capacity) {
        if (capacity < 1) {
            return;
        }
        this.capacity = capacity;
    }

    public void add(Object listener) {
        if (listener == null) {
            return;
        }
        if (this.size == 0) {
            this.listeners = new Object[this.capacity];
        } else {
            int i = 0;
            while (i < this.size) {
                if (this.listeners[i] == listener) {
                    return;
                }
                ++i;
            }
            if (this.size == this.listeners.length) {
                this.listeners = new Object[this.size * 2 + 1];
                System.arraycopy(this.listeners, 0, this.listeners, 0, this.size);
            }
        }
        this.listeners[this.size] = listener;
        ++this.size;
    }

    public void clear() {
        this.size = 0;
        this.listeners = null;
    }

    public Object[] getListeners() {
        if (this.size == 0) {
            return EmptyArray;
        }
        Object[] result = new Object[this.size];
        System.arraycopy(this.listeners, 0, result, 0, this.size);
        return result;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public void remove(Object listener) {
        if (listener == null) {
            return;
        }
        int i = 0;
        while (i < this.size) {
            if (this.listeners[i] == listener) {
                if (this.size == 1) {
                    this.listeners = null;
                    this.size = 0;
                } else {
                    System.arraycopy(this.listeners, i + 1, this.listeners, i, --this.size - i);
                    this.listeners[this.size] = null;
                }
                return;
            }
            ++i;
        }
    }

    public int size() {
        return this.size;
    }
}

