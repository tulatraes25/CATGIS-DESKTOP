/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import javax.swing.Icon;
import javax.swing.JPanel;

public abstract class OptionsPanel
extends JPanel {
    private static final long serialVersionUID = 1L;

    @Override
    public abstract String getName();

    public abstract Icon getIcon();

    public abstract void init();

    public abstract String validateInput();

    public abstract void okPressed();
}

