/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.toedter.calendar.JDateChooser
 */
package org.saig.jump.widgets.util.validating;

import com.toedter.calendar.JDateChooser;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JDialog;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public class NullDateChooserValidator
extends AbstractValidator {
    public static final String DEFAULT_MESSAGE = I18N.getString("org.saig.jump.widgets.util.validating.NullDateChooserValidator.It-is-a-required-field");

    public NullDateChooserValidator(JDialog parent, JDateChooser c) {
        super(parent, (JComponent)c, DEFAULT_MESSAGE);
    }

    public NullDateChooserValidator(JDialog parent, JDateChooser c, String message) {
        super(parent, (JComponent)c, message);
    }

    @Override
    protected boolean validationCriteria(JComponent c) {
        JDateChooser dateChooser = (JDateChooser)c;
        Date value = dateChooser.getDate();
        return value != null;
    }
}

