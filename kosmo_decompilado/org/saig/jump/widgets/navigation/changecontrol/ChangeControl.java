/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.navigation.changecontrol;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.saig.core.gui.swing.dataComponents.layers.JLayerTextAreaScrollPane;
import org.saig.core.gui.swing.dataComponents.tables.JTableTextAreaScrollPane;
import org.saig.jump.widgets.navigation.changecontrol.ChangeControllable;

public class ChangeControl {
    private ChangeControllable form;
    private JComponent component;
    private boolean changeControlGenerated = false;

    public ChangeControl(ChangeControllable form, JComponent component) {
        this.form = form;
        this.component = component;
    }

    public void addChangeControl() {
        if (!this.changeControlGenerated) {
            this.analyzeComponent(this.component);
            this.changeControlGenerated = true;
        }
    }

    private void analyzeComponent(JComponent component) {
        if (component instanceof JScrollPane) {
            Component viewComponent = ((JScrollPane)component).getViewport().getView();
            this.analyzeComponent((JComponent)viewComponent);
        } else if (this.isContainer(component)) {
            Component[] componentArray = component.getComponents();
            int n = componentArray.length;
            int n2 = 0;
            while (n2 < n) {
                Component childComponent = componentArray[n2];
                this.analyzeComponent((JComponent)childComponent);
                ++n2;
            }
        } else {
            this.addChangeListener(component);
        }
    }

    private boolean isContainer(Component component) {
        return component instanceof JPanel || component instanceof JLayeredPane || component instanceof JRootPane || component instanceof JTabbedPane;
    }

    private void addChangeListener(Component component) {
        DocumentListener docListener = new DocumentListener(){

            @Override
            public void changedUpdate(DocumentEvent e) {
                ChangeControl.this.dataChanged();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                ChangeControl.this.dataChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                ChangeControl.this.dataChanged();
            }
        };
        ActionListener actionListener = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ChangeControl.this.dataChanged();
            }
        };
        ChangeListener changeListener = new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                ChangeControl.this.dataChanged();
            }
        };
        if (component instanceof JTextComponent) {
            JTextComponent c = (JTextComponent)component;
            c.getDocument().addDocumentListener(docListener);
        } else if (component instanceof JComboBox) {
            JComboBox c = (JComboBox)component;
            c.addActionListener(actionListener);
        } else if (component instanceof JCheckBox) {
            JCheckBox c = (JCheckBox)component;
            c.addActionListener(actionListener);
        } else if (component instanceof JRadioButton) {
            JRadioButton c = (JRadioButton)component;
            c.getModel().addChangeListener(changeListener);
        } else if (component instanceof JSpinner) {
            JSpinner c = (JSpinner)component;
            c.addChangeListener(changeListener);
        } else if (component instanceof JTextArea) {
            JTextArea c = (JTextArea)component;
            c.getDocument().addDocumentListener(docListener);
        } else if (component instanceof JLayerTextAreaScrollPane) {
            JLayerTextAreaScrollPane c = (JLayerTextAreaScrollPane)component;
            c.getTextArea().getDocument().addDocumentListener(docListener);
        } else if (component instanceof JTableTextAreaScrollPane) {
            JTableTextAreaScrollPane c = (JTableTextAreaScrollPane)component;
            c.getTextArea().getDocument().addDocumentListener(docListener);
        }
    }

    private void dataChanged() {
        if (!this.form.isRefreshing()) {
            this.form.setDataModified(true);
        }
    }
}

