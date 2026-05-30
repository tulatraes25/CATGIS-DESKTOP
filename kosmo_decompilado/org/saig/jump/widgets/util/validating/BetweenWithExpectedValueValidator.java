/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating;

import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class BetweenWithExpectedValueValidator
extends AbstractValidator {
    private final double minimumValue;
    private final double maximumValue;
    private double expectedVal = 0.0;
    private double maxDiff = 0.0;
    private Color prevColor;

    public void setExpectedValue(double expectedValue) {
        this.expectedVal = expectedValue;
    }

    public void setMaximumDifference(double maximumDifference) {
        this.maxDiff = maximumDifference;
    }

    public BetweenWithExpectedValueValidator(JDialog parent, JTextField textField, double minValue, double maxValue, double expectedValue, double maximumDifference) {
        super(parent, (JComponent)textField, I18N.getMessage("org.saig.jump.widgets.util.validating.BetweenValidator.Field-value-must-be-between-{0}-and-{1}", new Object[]{new Double(minValue), new Double(maxValue)}));
        this.minimumValue = minValue;
        this.maximumValue = maxValue;
        this.expectedVal = expectedValue;
        this.maxDiff = maximumDifference;
        this.prevColor = textField.getForeground();
    }

    public BetweenWithExpectedValueValidator(JDialog parent, JTextField textField, String message, double minValue, double maxValue, double expectedValue, double maximumDifference) {
        super(parent, (JComponent)textField, message);
        this.minimumValue = minValue;
        this.maximumValue = maxValue;
        this.expectedVal = expectedValue;
        this.maxDiff = maximumDifference;
        this.prevColor = textField.getForeground();
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        String value;
        double currentValue;
        block7: {
            JTextField textField = (JTextField)c;
            currentValue = 0.0;
            value = "";
            value = textField.getText().trim();
            if (!"".equals(value)) break block7;
            return true;
        }
        try {
            currentValue = Double.parseDouble(value);
        }
        catch (Exception e) {
            try {
                currentValue = Double.parseDouble(value.replace(',', '.'));
            }
            catch (NumberFormatException e1) {
                return false;
            }
        }
        if (Math.abs(currentValue - this.expectedVal) >= Math.abs(this.maxDiff)) {
            c.setForeground(Color.RED);
        } else {
            c.setForeground(this.prevColor);
        }
        return this.minimumValue <= currentValue && currentValue <= this.maximumValue;
    }
}

