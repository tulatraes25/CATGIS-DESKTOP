/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class NumberSpinner
extends JSpinner {
    private static final long serialVersionUID = 1L;

    public NumberSpinner(double defaultValue, double minValue, double maxValue, double step) {
        SpinnerNumberModel model = new SpinnerNumberModel(defaultValue, minValue, maxValue, step);
        this.setFont(new JLabel().getFont());
        this.setModel(model);
    }

    public NumberSpinner(int defaultValue, int minValue, int maxValue, int step) {
        SpinnerNumberModel model = new SpinnerNumberModel(defaultValue, minValue, maxValue, step);
        this.setFont(new JLabel().getFont());
        this.setModel(model);
    }

    public void setDefaultValue(double defaultValue) {
        this.getModel().setValue(new Double(defaultValue));
    }

    public void setDefaultValue(int defaultValue) {
        this.getModel().setValue(defaultValue);
    }

    public void setMinValue(double minValue) {
        ((SpinnerNumberModel)this.getModel()).setMinimum(new Double(minValue));
    }

    public void setMaxValue(double maxValue) {
        ((SpinnerNumberModel)this.getModel()).setMaximum(new Double(maxValue));
    }

    public void setStep(double step) {
        ((SpinnerNumberModel)this.getModel()).setStepSize(new Double(step));
    }

    public void updateModel(double defaultValue, double minValue, double maxValue, double step) {
        SpinnerNumberModel model = new SpinnerNumberModel(defaultValue, minValue, maxValue, step);
        this.setModel(model);
    }

    public int getIntValue() {
        return ((Number)this.getValue()).intValue();
    }

    public double getDoubleValue() {
        return ((Number)this.getValue()).doubleValue();
    }
}

