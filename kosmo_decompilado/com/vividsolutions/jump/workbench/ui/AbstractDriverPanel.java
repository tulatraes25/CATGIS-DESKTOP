/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.DriverPanelCache;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;

public abstract class AbstractDriverPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    BorderLayout borderLayout = new BorderLayout();

    public abstract void addActionListener(ActionListener var1);

    public abstract void removeActionListener(ActionListener var1);

    public abstract boolean wasOKPressed();

    public boolean isInputValid() {
        return this.getValidationError() == null;
    }

    public void setCache(DriverPanelCache cache) {
        Assert.isTrue((cache != null ? 1 : 0) != 0);
    }

    public DriverPanelCache getCache() {
        return new DriverPanelCache();
    }

    public String getValidationError() {
        return null;
    }
}

