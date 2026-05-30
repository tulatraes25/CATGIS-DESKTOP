/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.util;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class WindowMinSizer {
    private Window window;
    private Dimension newDimension = new Dimension();

    public WindowMinSizer(Window window) {
        this.window = window;
        window.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentResized(ComponentEvent e) {
                WindowMinSizer.this.handleWindowResize();
            }
        });
    }

    private void handleWindowResize() {
        Dimension minimum = this.window.getMinimumSize();
        Dimension actual = this.window.getSize();
        this.newDimension.setSize(actual);
        if (actual.width < minimum.width) {
            this.newDimension.width = minimum.width;
        }
        if (actual.height < minimum.height) {
            this.newDimension.height = minimum.height;
        }
        if (!this.newDimension.equals(actual)) {
            this.window.setSize(this.newDimension);
        }
    }
}

