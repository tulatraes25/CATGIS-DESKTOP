/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.addremove;

import com.vividsolutions.jump.workbench.ui.addremove.AddRemovePanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class ButtonCustomAddRemovePanel<T>
extends AddRemovePanel<T> {
    private static final long serialVersionUID = 1L;

    public ButtonCustomAddRemovePanel(boolean showingUpDownButtons) {
        super(showingUpDownButtons);
    }

    public ButtonCustomAddRemovePanel(boolean showingUpDownButtons, boolean showingAllToRightLeftButtons) {
        super(showingUpDownButtons, showingAllToRightLeftButtons);
    }

    public ButtonCustomAddRemovePanel(boolean showingUpButton, boolean showingDownButton, boolean showingAddAllButton, boolean showingRemoveAllButton) {
        super(showingUpButton, showingDownButton, showingAddAllButton, showingRemoveAllButton);
    }

    public JButton getToLeftButton() {
        return this.removeButton;
    }

    public JButton getAllToLeftButton() {
        return this.removeAllButton;
    }

    public JButton getToRightButton() {
        return this.addButton;
    }

    public JButton getAllToRightButton() {
        return this.addAllButton;
    }

    @Override
    public void addButton_actionPerformed(ActionEvent e) {
        super.addButton_actionPerformed(e);
    }

    @Override
    public void addAllButton_actionPerformed(ActionEvent e) {
        super.addAllButton_actionPerformed(e);
    }

    @Override
    public void removeAllButton_actionPerformed(ActionEvent e) {
        super.removeAllButton_actionPerformed(e);
    }

    @Override
    public void removeButton_actionPerformed(ActionEvent e) {
        super.removeButton_actionPerformed(e);
    }

    public void removeAllButtonActionListeners() {
        this.removeAllActionListeners(this.getAllToLeftButton());
        this.removeAllActionListeners(this.getAllToRightButton());
        this.removeAllActionListeners(this.getToLeftButton());
        this.removeAllActionListeners(this.getToRightButton());
    }

    private void removeAllActionListeners(JButton button) {
        ActionListener[] listeners = button.getActionListeners();
        int i = 0;
        while (i < listeners.length) {
            ActionListener currentListener = listeners[i];
            button.removeActionListener(currentListener);
            ++i;
        }
    }
}

