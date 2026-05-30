/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import java.awt.geom.GeneralPath;

public abstract class DrawnMarkStep {
    private int type;
    private Object[] params;

    public DrawnMarkStep(int type, Object[] params) {
        this.type = type;
        this.params = params;
    }

    public int getType() {
        return this.type;
    }

    public Object[] getParams() {
        return this.params;
    }

    public Object getParam(int i) {
        return this.params[i];
    }

    public abstract void paint(GeneralPath var1);
}

