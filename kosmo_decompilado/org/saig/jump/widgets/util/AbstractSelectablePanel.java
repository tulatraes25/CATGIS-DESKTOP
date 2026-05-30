/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.jump.widgets.util.ChangeFirerPanel;
import org.saig.jump.widgets.util.ComponentTitledBorder;

public abstract class AbstractSelectablePanel
extends ChangeFirerPanel {
    private static final long serialVersionUID = 1L;
    protected String borderTitle;
    protected final boolean isSelectable;
    protected AbstractButton selectionComponent;

    public abstract void initComponents();

    protected abstract void refreshComponents(boolean var1);

    protected abstract void selectionStateChanged(boolean var1);

    public AbstractSelectablePanel(String borderTitle, boolean isSelectable) {
        this(borderTitle, null, isSelectable, true);
    }

    public AbstractSelectablePanel(String borderTitle, boolean isSelectable, boolean isCheckBox) {
        this(borderTitle, null, isSelectable, isCheckBox);
    }

    public AbstractSelectablePanel(String borderTitle, Icon borderIcon, boolean isSelectable, boolean isCheckBox) {
        this.setLayout(new GridBagLayout());
        this.borderTitle = borderTitle;
        this.isSelectable = isSelectable;
        if (isSelectable) {
            this.selectionComponent = isCheckBox ? new JCheckBox(borderTitle, null) : new JRadioButton(borderTitle, null);
            this.selectionComponent.setForeground(UIManager.getColor("TitledBorder.titleColor"));
            this.selectionComponent.setFocusPainted(false);
            this.selectionComponent.addChangeListener(new ChangeListener(){

                @Override
                public void stateChanged(ChangeEvent e) {
                    boolean enable = AbstractSelectablePanel.this.selectionComponent.isSelected();
                    AbstractSelectablePanel.this.selectionStateChanged(enable);
                }
            });
            ComponentTitledBorder componentBorder = new ComponentTitledBorder(this.selectionComponent, this, BorderFactory.createTitledBorder(""));
            this.setBorder(componentBorder);
        } else {
            JLabel label = new JLabel(borderTitle, borderIcon, 2);
            label.setForeground(UIManager.getColor("TitledBorder.titleColor"));
            label.setOpaque(true);
            ComponentTitledBorder componentBorder = new ComponentTitledBorder(label, this, BorderFactory.createTitledBorder(""));
            this.setBorder(componentBorder);
        }
    }

    public String getBorderTitle() {
        return this.borderTitle;
    }

    public void setBorderTitle(String borderTitle) {
        this.borderTitle = borderTitle;
        this.selectionComponent.setText(borderTitle);
    }

    public void setSelectionComponentBoxToolTipText(String text) {
        if (this.isSelectable) {
            this.selectionComponent.setToolTipText(text);
        }
    }

    public AbstractButton getSelectionComponent() {
        return this.selectionComponent;
    }

    public void addCheckBoxActionListener(ActionListener actionListener) {
        if (this.isSelectable) {
            this.selectionComponent.addActionListener(actionListener);
        }
    }

    public void setSelected(boolean selected) {
        if (this.isSelectable) {
            this.selectionComponent.setSelected(selected);
            this.refreshComponents(selected);
        }
    }

    public boolean isSelected() {
        return !this.isSelectable || this.isSelectable && this.selectionComponent.isSelected();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.selectionComponent.setEnabled(enabled);
        if (this.selectionComponent.isSelected()) {
            this.refreshComponents(enabled);
        }
    }
}

