/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.util.validating;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class NullTextFieldValidator
extends AbstractValidator {
    public static final String DEFAULT_MESSAGE = I18N.getString("org.saig.jump.widgets.util.validating.NullTextFieldValidator.It-is-a-required-field");

    public NullTextFieldValidator(JDialog parent, JComponent c) {
        super(parent, c, DEFAULT_MESSAGE);
    }

    public NullTextFieldValidator(JDialog parent, JComponent c, String message) {
        super(parent, c, message);
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        JTextField textField = (JTextField)c;
        return !StringUtils.isEmpty((String)textField.getText());
    }
}

