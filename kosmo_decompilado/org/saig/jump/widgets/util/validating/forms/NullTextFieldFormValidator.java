/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.util.validating.forms;

import javax.swing.JComponent;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.navigation.status.IStatusForm;
import org.saig.jump.widgets.util.validating.forms.AbstractFormValidator;

public class NullTextFieldFormValidator
extends AbstractFormValidator {
    public static final String DEFAULT_MESSAGE = I18N.getString("org.saig.jump.widgets.util.validating.NullTextFieldValidator.It-is-a-required-field");

    public NullTextFieldFormValidator(IStatusForm parent, JTextField c) {
        super(parent, (JComponent)c, DEFAULT_MESSAGE);
    }

    public NullTextFieldFormValidator(IStatusForm parent, JTextField c, String message) {
        super(parent, (JComponent)c, message);
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        JTextField textField = (JTextField)c;
        return !StringUtils.isEmpty((String)textField.getText());
    }
}

