/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating.forms;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.navigation.status.IStatusForm;
import org.saig.jump.widgets.util.validating.forms.AbstractFormValidator;

public class NullComboBoxFormValidator
extends AbstractFormValidator {
    public static final String DEFAULT_MESSAGE = I18N.getString("org.saig.jump.widgets.util.validating.NullComboBoxValidator.It-is-a-required-field");
    public static final String NULL_VALUE = "----------";

    public NullComboBoxFormValidator(IStatusForm parent, JComboBox c) {
        super(parent, (JComponent)c, DEFAULT_MESSAGE);
    }

    public NullComboBoxFormValidator(IStatusForm parent, JComboBox c, String message) {
        super(parent, (JComponent)c, message);
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        JComboBox comboBox = (JComboBox)c;
        Object selectedItem = comboBox.getSelectedItem();
        return selectedItem != null && !selectedItem.equals("") && !selectedItem.equals(NULL_VALUE);
    }
}

