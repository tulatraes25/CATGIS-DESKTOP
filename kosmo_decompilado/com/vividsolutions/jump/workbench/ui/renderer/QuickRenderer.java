/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.SimpleRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;

public abstract class QuickRenderer
extends SimpleRenderer {
    public QuickRenderer(Object contentID, LayerViewPanel panel, double factor) {
        super(contentID, panel, factor);
    }

    @Override
    public Runnable createRunnable() {
        return null;
    }

    @Override
    public ThreadSafeImage getImage() {
        if (this.image != null) {
            return this.image;
        }
        this.rendering = true;
        try {
            this.cancelled = false;
            this.image = new ThreadSafeImage(this.panel);
            try {
                this.renderHook(this.image);
            }
            catch (Throwable t) {
                this.panel.getContext().handleThrowable(t);
            }
        }
        finally {
            this.rendering = false;
        }
        return this.image;
    }
}

