/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.ui.SplashPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JWindow;

public class SplashWindow
extends JWindow {
    public SplashWindow(SplashPanel panel) {
        this.getContentPane().add((Component)panel, "Center");
        this.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = panel.getPreferredSize();
        this.setLocation(screenSize.width / 2 - labelSize.width / 2, screenSize.height / 2 - labelSize.height / 2);
    }
}

