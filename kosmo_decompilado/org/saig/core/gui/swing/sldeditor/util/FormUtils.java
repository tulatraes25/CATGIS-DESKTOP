/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;

public class FormUtils {
    private static Insets defaultInsets = new Insets(3, 3, 3, 3);
    private static Dimension buttonDimension;
    private static Dimension colorButtonDimension;
    private static Dimension spinnerDimension;
    private static Dimension comboDimension;

    private FormUtils() {
    }

    public static Insets getDefaultInsets() {
        return defaultInsets;
    }

    public static void addRowInGBL(JComponent parent, int row, int startCol, JComponent component) {
        FormUtils.addRowInGBL(parent, row, startCol, component, true, true);
    }

    public static void addRowInGBL(JComponent parent, int row, int startCol, JComponent component, boolean fillRow, boolean insets) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol;
        gridBagConstraints.gridy = row;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        if (fillRow) {
            gridBagConstraints.gridwidth = 0;
            gridBagConstraints.weightx = 1.0;
        }
        if (insets) {
            gridBagConstraints.insets = FormUtils.getDefaultInsets();
        }
        parent.add((Component)component, gridBagConstraints);
    }

    public static void addRowInGBL(JComponent parent, int row, int startCol, JComponent component, boolean fillRow, boolean lastComponentInRow, boolean insets) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol;
        gridBagConstraints.gridy = row;
        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 17;
        if (fillRow) {
            gridBagConstraints.fill = 2;
            gridBagConstraints.weightx = 1.0;
        }
        if (lastComponentInRow) {
            gridBagConstraints.gridwidth = 0;
            gridBagConstraints.fill = 2;
            gridBagConstraints.weightx = 1.0;
        }
        if (insets) {
            gridBagConstraints.insets = FormUtils.getDefaultInsets();
        }
        parent.add((Component)component, gridBagConstraints);
    }

    public static void addRowInGBL(JComponent parent, int row, int startCol, JLabel label, JComponent component) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = FormUtils.getDefaultInsets();
        parent.add((Component)label, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol + 1;
        gridBagConstraints.gridy = row;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = FormUtils.getDefaultInsets();
        parent.add((Component)component, gridBagConstraints);
    }

    public static void addRowInGBL(JComponent parent, int row, int startCol, String label, JComponent component) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = FormUtils.getDefaultInsets();
        parent.add((Component)new JLabel(label), gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol + 1;
        gridBagConstraints.gridy = row;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = FormUtils.getDefaultInsets();
        parent.add((Component)component, gridBagConstraints);
    }

    public static void addRowInGBL(JComponent parent, int row, int startCol, JLabel label, JComponent component, boolean lastRowComponent) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = FormUtils.getDefaultInsets();
        parent.add((Component)label, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol + 1;
        gridBagConstraints.gridy = row;
        if (lastRowComponent) {
            gridBagConstraints.gridwidth = 0;
            gridBagConstraints.fill = 2;
            gridBagConstraints.weightx = 1.0;
        } else {
            gridBagConstraints.fill = 0;
        }
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = FormUtils.getDefaultInsets();
        parent.add((Component)component, gridBagConstraints);
    }

    public static void addRowInGBL(JComponent parent, int row, int startCol, String label, JComponent component, boolean lastRowComponent) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = FormUtils.getDefaultInsets();
        parent.add((Component)new JLabel(label), gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol + 1;
        gridBagConstraints.gridy = row;
        if (lastRowComponent) {
            gridBagConstraints.gridwidth = 0;
            gridBagConstraints.fill = 2;
            gridBagConstraints.weightx = 1.0;
        } else {
            gridBagConstraints.fill = 0;
        }
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = FormUtils.getDefaultInsets();
        parent.add((Component)component, gridBagConstraints);
    }

    public static void addRowInGBL(JComponent parent, int row, int startCol, JComponent label, JComponent component) {
        FormUtils.addRowInGBL(parent, row, startCol, label, component, 0.0, true);
    }

    public static void addRowInGBL(JComponent parent, int row, int startCol, JComponent label, JComponent component, boolean lastRowComponent) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = FormUtils.getDefaultInsets();
        parent.add((Component)label, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol + 1;
        gridBagConstraints.gridy = row;
        if (lastRowComponent) {
            gridBagConstraints.gridwidth = 0;
            gridBagConstraints.fill = 2;
            gridBagConstraints.weightx = 1.0;
        } else {
            gridBagConstraints.fill = 0;
        }
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = FormUtils.getDefaultInsets();
        parent.add((Component)component, gridBagConstraints);
    }

    public static void addRowInGBL(JComponent parent, int row, int startCol, JComponent label, JComponent component, double weigthy, boolean insets) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = weigthy > 0.0 ? 18 : 17;
        if (insets) {
            gridBagConstraints.insets = FormUtils.getDefaultInsets();
        }
        parent.add((Component)label, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol + 1;
        gridBagConstraints.gridy = row;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.weightx = 1.0;
        if (weigthy > 0.0) {
            gridBagConstraints.weighty = (float)weigthy;
            gridBagConstraints.fill = 1;
        }
        if (insets) {
            gridBagConstraints.insets = FormUtils.getDefaultInsets();
        }
        parent.add((Component)component, gridBagConstraints);
    }

    public static void addColInGBL(JComponent parent, int row, int startCol, JComponent label, JComponent component) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = 1;
        gridBagConstraints.gridx = startCol;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = 16;
        gridBagConstraints.insets = FormUtils.getDefaultInsets();
        parent.add((Component)label, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = startCol;
        gridBagConstraints.gridy = row + 1;
        gridBagConstraints.fill = 1;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = FormUtils.getDefaultInsets();
        parent.add((Component)component, gridBagConstraints);
    }

    public static void addFiller(JComponent parent, int row, int col, JComponent component, double weight, boolean insets) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = col;
        gridBagConstraints.gridy = row;
        gridBagConstraints.fill = 1;
        gridBagConstraints.weightx = weight;
        gridBagConstraints.weighty = weight;
        gridBagConstraints.gridwidth = 0;
        gridBagConstraints.gridheight = 0;
        if (insets) {
            gridBagConstraints.insets = FormUtils.getDefaultInsets();
        }
        parent.add((Component)component, gridBagConstraints);
    }

    public static void addFiller(JComponent parent, int row, int col, JComponent component) {
        FormUtils.addFiller(parent, row, col, component, 1000.0, true);
    }

    public static void addFiller(JComponent parent, int row, int col, JComponent component, boolean insets) {
        FormUtils.addFiller(parent, row, col, component, 1000.0, insets);
    }

    public static void addFiller(JComponent parent, int row, int col) {
        FormUtils.addFiller(parent, row, col, new JLabel(), false);
    }

    public static void addSingleRowWestComponent(JComponent parent, int row, JComponent component) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = 17;
        parent.add((Component)component, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.fill = 1;
        gridBagConstraints.gridwidth = 0;
        gridBagConstraints.weightx = 1.0;
        parent.add((Component)new JLabel(), gridBagConstraints);
    }

    public static Dimension getButtonDimension() {
        if (buttonDimension == null) {
            JLabel label = new JLabel("w");
            buttonDimension = label.getPreferredSize();
            FormUtils.buttonDimension.width = FormUtils.buttonDimension.height = (int)((double)Math.max(FormUtils.buttonDimension.width, FormUtils.buttonDimension.height) * 1.3);
        }
        return buttonDimension;
    }

    public static void forceButtonDimension(JButton button) {
        button.setPreferredSize(FormUtils.getButtonDimension());
        button.setMinimumSize(FormUtils.getButtonDimension());
        button.setMaximumSize(FormUtils.getButtonDimension());
    }

    public static Dimension getColorButtonDimension() {
        if (colorButtonDimension == null) {
            JLabel label = new JLabel("w");
            colorButtonDimension = label.getPreferredSize();
            FormUtils.colorButtonDimension.height = (int)((double)Math.max(FormUtils.colorButtonDimension.width, FormUtils.colorButtonDimension.height) * 1.3);
            FormUtils.colorButtonDimension.width = FormUtils.getComboDimension().width;
        }
        return colorButtonDimension;
    }

    public static Dimension getSpinnerDimension() {
        if (spinnerDimension == null) {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.1));
            spinnerDimension = spinner.getPreferredSize();
            FormUtils.spinnerDimension.width = FormUtils.getComboDimension().width;
        }
        return spinnerDimension;
    }

    public static Dimension getComboDimension() {
        if (comboDimension == null) {
            JComboBox<String> combo = new JComboBox<String>(new String[]{"abcdefg"});
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.1));
            comboDimension = combo.getPreferredSize();
        }
        return comboDimension;
    }

    public static void show(JComponent component) {
        JFrame frame = new JFrame("Testing component: " + component.getClass().getName());
        frame.setContentPane(component);
        frame.setDefaultCloseOperation(3);
        frame.pack();
        frame.show();
    }

    public static void show(JFrame frame) {
        frame.setTitle("Testing component: " + frame.getClass().getName());
        frame.setDefaultCloseOperation(3);
        frame.pack();
        frame.show();
    }

    public static Dimension getMaxDimension(Dimension d1, Dimension d2) {
        return new Dimension(Math.max(d1.width, d2.width), Math.max(d1.height, d2.height));
    }

    public static Window getWindowForComponent(Component parentComponent) {
        if (parentComponent == null) {
            return JOptionPane.getRootFrame();
        }
        if (parentComponent instanceof Frame) {
            return (Frame)parentComponent;
        }
        if (parentComponent instanceof Dialog) {
            return (Dialog)parentComponent;
        }
        return FormUtils.getWindowForComponent(parentComponent.getParent());
    }

    public static JLabel getTitleLabel(String title) {
        JLabel label = new JLabel(title);
        label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        return label;
    }

    public static JComponent getExpandableTitleLabel(String title, final JComponent[] managedComponents, boolean collapsed) {
        JLabel label = new JLabel(title);
        final Icon expandedIcon = (Icon)UIManager.get("Tree.expandedIcon");
        final Icon collapsedIcon = (Icon)UIManager.get("Tree.collapsedIcon");
        final JButton button = new JButton(collapsed ? collapsedIcon : expandedIcon);
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
        button.setFocusPainted(false);
        int i = 0;
        while (i < managedComponents.length) {
            managedComponents[i].setVisible(!collapsed);
            ++i;
        }
        button.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean visible = true;
                if (button.getIcon() == expandedIcon) {
                    button.setIcon(collapsedIcon);
                    visible = false;
                } else {
                    button.setIcon(expandedIcon);
                }
                int i = 0;
                while (i < managedComponents.length) {
                    managedComponents[i].setVisible(visible);
                    ++i;
                }
                FormUtils.getWindowForComponent(button).pack();
            }
        });
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        panel.setLayout(new BorderLayout());
        panel.add((Component)button, "West");
        panel.add(label);
        return panel;
    }

    public static void repackParentWindow(Component component) {
        Window window = FormUtils.getWindowForComponent(component);
        Dimension preferred = window.getPreferredSize();
        Dimension actual = window.getSize();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension newSize = new Dimension(actual);
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(window.getGraphicsConfiguration());
        Dimension freeScreen = new Dimension(screen.width - insets.left - insets.right, screen.height - insets.top - insets.bottom);
        if (actual.width < preferred.width) {
            newSize.width = preferred.width > freeScreen.width ? freeScreen.width : preferred.width;
        }
        if (actual.height < preferred.height) {
            newSize.height = preferred.height > freeScreen.height ? freeScreen.height : preferred.height;
        }
        if (!newSize.equals(actual)) {
            window.setSize(newSize);
        }
    }
}

