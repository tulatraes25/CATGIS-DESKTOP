/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.scalebar;

public class Unit
implements Comparable {
    private double modelValue;
    private String name;

    public Unit(String name, double modelValue) {
        this.name = name;
        this.modelValue = modelValue;
    }

    public String toString() {
        return this.getName();
    }

    public double getModelValue() {
        return this.modelValue;
    }

    public String getName() {
        return this.name;
    }

    public int compareTo(Object o) {
        Unit other = (Unit)o;
        if (this.modelValue == other.modelValue) {
            return 0;
        }
        if (this.modelValue < other.modelValue) {
            return -1;
        }
        return 1;
    }
}

