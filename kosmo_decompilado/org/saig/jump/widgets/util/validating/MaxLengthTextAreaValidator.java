/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class MaxLengthTextAreaValidator
extends AbstractValidator {
    public static final String DEFAULT_MESSAGE = "La longitud del campo supera la longitud m\u00e1xima permitida ";
    private int maxLengthValue = 0;
    private String message = "";
    private boolean obligatorio;

    public MaxLengthTextAreaValidator(JDialog parent, JComponent c, int maximumValue) {
        super(parent, c, DEFAULT_MESSAGE + maximumValue);
        this.maxLengthValue = maximumValue;
    }

    public MaxLengthTextAreaValidator(JDialog parent, JComponent c, String message, int maximumValue) {
        super(parent, c, message);
        this.message = message;
        this.maxLengthValue = maximumValue;
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        JTextArea textArea = (JTextArea)c;
        int currentValue = 0;
        String value = "";
        value = textArea.getText().trim();
        if (value.equals("")) {
            return !this.obligatorio;
        }
        currentValue = value.length();
        return currentValue <= this.maxLengthValue;
    }

    public void setMaxLengthValue(int maxLengthValuee) {
        this.maxLengthValue = maxLengthValuee;
        if (this.message.equals("")) {
            this.setMessage(DEFAULT_MESSAGE + this.maxLengthValue);
        }
    }

    public boolean isObligatorio() {
        return this.obligatorio;
    }

    public void setObligatorio(boolean obligatorio) {
        this.obligatorio = obligatorio;
    }
}

