/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating.forms;

import javax.swing.JComponent;
import javax.swing.JTextField;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.navigation.status.IStatusForm;
import org.saig.jump.widgets.util.validating.forms.AbstractFormValidator;

public class StringLengthLessOrEqualThanTextFieldFormValidator
extends AbstractFormValidator {
    protected int numMaxChars = 0;
    public static final String DEFAULT_MESSAGE = I18N.getString(StringLengthLessOrEqualThanTextFieldFormValidator.class, "characters-number-in-field-must-be-lesser-than");

    public StringLengthLessOrEqualThanTextFieldFormValidator(IStatusForm parent, JComponent c, int numMaxChars) {
        super(parent, c, String.valueOf(DEFAULT_MESSAGE) + numMaxChars);
        this.numMaxChars = numMaxChars;
    }

    public StringLengthLessOrEqualThanTextFieldFormValidator(IStatusForm parent, JComponent c, int numMaxChars, String message) {
        super(parent, c, message);
        this.numMaxChars = numMaxChars;
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        JTextField textField = (JTextField)c;
        return textField.getText().trim().length() <= this.numMaxChars;
    }
}

