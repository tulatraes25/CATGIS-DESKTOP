/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class MaxLengthTextFieldValidator
extends AbstractValidator {
    public static final String DEFAULT_MESSAGE = I18N.getString(MaxLengthTextFieldValidator.class, "field-length-exceed-allowed-maximum-length");
    private int maxLengthValue = 0;
    private String message = "";
    private boolean obligatorio;

    public MaxLengthTextFieldValidator(JDialog parent, JComponent c, int maximumValue) {
        super(parent, c, String.valueOf(DEFAULT_MESSAGE) + maximumValue);
        this.maxLengthValue = maximumValue;
    }

    public MaxLengthTextFieldValidator(JDialog parent, JComponent c, String message, int maximumValue) {
        super(parent, c, message);
        this.message = message;
        this.maxLengthValue = maximumValue;
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        JTextField textField = (JTextField)c;
        int currentValue = 0;
        String value = "";
        value = textField.getText().trim();
        if (value.equals("")) {
            return !this.obligatorio;
        }
        currentValue = value.length();
        return currentValue <= this.maxLengthValue;
    }

    public void setMaxLengthValue(int maxLengthValuee) {
        this.maxLengthValue = maxLengthValuee;
        if (this.message.equals("")) {
            this.setMessage(String.valueOf(DEFAULT_MESSAGE) + this.maxLengthValue);
        }
    }

    public boolean isObligatorio() {
        return this.obligatorio;
    }

    public void setObligatorio(boolean obligatorio) {
        this.obligatorio = obligatorio;
    }
}

