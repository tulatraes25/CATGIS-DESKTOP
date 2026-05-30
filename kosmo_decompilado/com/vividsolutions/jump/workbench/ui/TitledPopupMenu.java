/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.ui.TrackedPopupMenu;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JSeparator;

public class TitledPopupMenu
extends TrackedPopupMenu {
    private static final long serialVersionUID = 1L;
    private JLabel titleLabel = new JLabel();

    public TitledPopupMenu() {
        this.titleLabel.setFont(new Font("Dialog", 3, 12));
        this.titleLabel.setHorizontalAlignment(0);
        this.add(this.titleLabel);
        this.addSeparator();
    }

    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }

    public void addSeparator(int i) {
        this.insert(new JSeparator(), i);
    }
}

