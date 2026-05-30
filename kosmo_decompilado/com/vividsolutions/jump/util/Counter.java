/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.util;

public class Counter {
    private int value = 0;

    public Counter() {
    }

    public Counter(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public void increment() {
        ++this.value;
    }
}

