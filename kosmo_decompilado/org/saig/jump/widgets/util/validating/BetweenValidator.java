/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class BetweenValidator
extends AbstractValidator {
    private double minimumValue = 0.0;
    private double maximumValue = 0.0;

    public BetweenValidator(JDialog parent, JTextField textField, double minValue, double maxValue) {
        super(parent, (JComponent)textField, I18N.getMessage("org.saig.jump.widgets.util.validating.BetweenValidator.Field-value-must-be-between-{0}-and-{1}", new Object[]{new Double(minValue), new Double(maxValue)}));
        this.minimumValue = minValue;
        this.maximumValue = maxValue;
    }

    public BetweenValidator(JDialog parent, JTextField textField, String message, double minValue, double maxValue) {
        super(parent, (JComponent)textField, message);
        this.minimumValue = minValue;
        this.maximumValue = maxValue;
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        String value;
        double currentValue;
        block5: {
            JTextField textField = (JTextField)c;
            currentValue = 0.0;
            value = "";
            value = textField.getText().trim();
            if (!value.equals("")) break block5;
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
        return this.minimumValue <= currentValue && currentValue <= this.maximumValue;
    }
}

