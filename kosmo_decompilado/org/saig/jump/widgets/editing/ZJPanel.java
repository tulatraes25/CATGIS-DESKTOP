/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.editing;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.AbstractSelectablePanel;
import org.saig.jump.widgets.util.NumberSpinner;

public class ZJPanel
extends AbstractSelectablePanel {
    private static final long serialVersionUID = 1L;
    private NumberSpinner zStepJSpinner;
    private NumberSpinner zSpinner;
    private JCheckBox useZCoordinateCheckBox;

    public ZJPanel() {
        super(I18N.getString("org.saig.jump.widgets.editing.ZJPanel.Use-Z"), true);
        this.initComponents();
    }

    @Override
    public void initComponents() {
        this.setLayout(new FlowLayout());
        this.useZCoordinateCheckBox = (JCheckBox)this.getSelectionComponent();
        this.zStepJSpinner = new NumberSpinner(1.0, 0.0, Double.MAX_VALUE, 0.01);
        this.zStepJSpinner.setPreferredSize(new Dimension(100, 20));
        this.zSpinner = new NumberSpinner(0.0, -1.7976931348623157E308, Double.MAX_VALUE, 1.0);
        this.zSpinner.setPreferredSize(new Dimension(100, 20));
        this.add(new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.editing.EditingPlugIn.Z-step")) + ":"));
        this.add(this.zStepJSpinner);
        this.add(new JLabel(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.editing.EditingPlugIn.Z")) + ":"));
        this.add(this.zSpinner);
    }

    public NumberSpinner getzStepJSpinner() {
        return this.zStepJSpinner;
    }

    public NumberSpinner getzSpinner() {
        return this.zSpinner;
    }

    public JCheckBox getUseZCoordinateCheckBox() {
        return this.useZCoordinateCheckBox;
    }

    @Override
    protected void refreshComponents(boolean enable) {
    }

    @Override
    protected void selectionStateChanged(boolean enable) {
    }
}

