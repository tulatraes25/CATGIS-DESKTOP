/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class NullComboBoxValidator
extends AbstractValidator {
    public static final String DEFAULT_MESSAGE = I18N.getString("org.saig.jump.widgets.util.validating.NullComboBoxValidator.It-is-a-required-field");
    public static final String NULL_VALUE = "----------";

    public NullComboBoxValidator(JDialog parent, JComponent c) {
        super(parent, c, DEFAULT_MESSAGE);
    }

    public NullComboBoxValidator(JDialog parent, JComponent c, String message) {
        super(parent, c, message);
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        JComboBox comboBox = (JComboBox)c;
        Object selectedItem = comboBox.getSelectedItem();
        return comboBox.isPopupVisible() || selectedItem != null && !selectedItem.equals("") && !selectedItem.equals(NULL_VALUE);
    }
}

