/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.GraphicsState;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.ImageObserver;
import org.saig.core.renderer.RenderingHintsManager;

public class ThreadSafeImage
implements Cloneable {
    private Image image = null;
    private Graphics2D graphics = null;
    private LayerViewPanel panel;
    private GraphicsState dummyGraphicsState = new GraphicsState(){

        @Override
        public void restore(Graphics2D g) {
        }
    };

    public ThreadSafeImage(LayerViewPanel panel) {
        this.panel = panel;
    }

    public synchronized Image getImage() {
        if (this.image == null) {
            this.image = this.panel.createBlankPanelImage();
        }
        return this.image;
    }

    public synchronized Graphics2D getGraphics() {
        if (this.graphics == null) {
            this.graphics = (Graphics2D)this.getImage().getGraphics();
            this.graphics.setRenderingHints(RenderingHintsManager.getRenderingHints());
        }
        return this.graphics;
    }

    public synchronized void copyTo(Graphics2D destination, ImageObserver imageObserver) {
        if (this.getImage() == null) {
            return;
        }
        destination.drawImage(this.getImage(), 0, 0, imageObserver);
    }

    private synchronized boolean isPanelReady() {
        if (this.panel.getSize().equals(new Dimension(0, 0))) {
            return false;
        }
        return this.getImage() != null;
    }

    public synchronized void draw(Drawer drawer) throws Exception {
        if (!this.isPanelReady()) {
            return;
        }
        Graphics2D g = (Graphics2D)this.image.getGraphics();
        g.setRenderingHints(RenderingHintsManager.getRenderingHints());
        drawer.draw(g);
    }

    public synchronized GraphicsState getGraphicsState() {
        if (!this.isPanelReady()) {
            return this.dummyGraphicsState;
        }
        return new GraphicsState(this.getGraphics());
    }

    public synchronized void setGraphicsState(GraphicsState gs) {
        if (!this.isPanelReady()) {
            return;
        }
        gs.restore(this.getGraphics());
    }

    public Object clone() {
        ThreadSafeImage clone = new ThreadSafeImage(this.panel);
        if (!clone.isPanelReady()) {
            return null;
        }
        this.copyTo(clone.getGraphics(), null);
        return clone;
    }

    public synchronized void dispose() {
        this.image = null;
    }

    public static interface Drawer {
        public void draw(Graphics2D var1) throws Exception;
    }
}

