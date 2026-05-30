/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;

public abstract class SimpleRenderer
extends AbstractRenderer {
    public SimpleRenderer(Object contentID, LayerViewPanel panel, double factor) {
        super(contentID, panel, factor);
    }

    @Override
    protected void renderHook(ThreadSafeImage image) throws Exception {
        image.draw(new ThreadSafeImage.Drawer(){

            @Override
            public void draw(Graphics2D g) throws NoninvertibleTransformException {
                SimpleRenderer.this.paint(g);
            }
        });
    }

    protected abstract void paint(Graphics2D var1) throws NoninvertibleTransformException;
}

