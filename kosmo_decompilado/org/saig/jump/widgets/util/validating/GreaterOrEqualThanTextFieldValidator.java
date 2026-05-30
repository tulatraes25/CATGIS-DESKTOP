/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class GreaterOrEqualThanTextFieldValidator
extends AbstractValidator {
    public static final String DEFAULT_MESSAGE = String.valueOf(I18N.getString("org.saig.jump.widgets.util.validating.GreaterOrEqualThanTextFieldValidator.the-field-value-must-be-greater-or-equal-to")) + " ";
    private double minimumValue = 0.0;
    private String message = "";
    private boolean obligatorio = false;

    public GreaterOrEqualThanTextFieldValidator(JDialog parent, JComponent c, double minimumValue) {
        super(parent, c, String.valueOf(DEFAULT_MESSAGE) + minimumValue);
        this.minimumValue = minimumValue;
    }

    public GreaterOrEqualThanTextFieldValidator(JDialog parent, JComponent c, String message, double minimumValue) {
        super(parent, c, message);
        this.message = message;
        this.minimumValue = minimumValue;
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
            return !this.obligatorio;
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
        return currentValue >= this.minimumValue;
    }

    public void setMinimumValue(double newMinimumValue) {
        this.minimumValue = newMinimumValue;
        if (this.message.equals("")) {
            this.setMessage(String.valueOf(DEFAULT_MESSAGE) + this.minimumValue);
        }
    }

    public boolean isObligatorio() {
        return this.obligatorio;
    }

    public void setObligatorio(boolean obligatorio) {
        this.obligatorio = obligatorio;
    }
}

