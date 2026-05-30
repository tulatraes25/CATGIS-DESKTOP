/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.validating.forms;

import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import org.saig.jump.widgets.navigation.status.IStatusForm;
import org.saig.jump.widgets.util.validating.AbstractValidator;

public abstract class AbstractFormValidator
extends AbstractValidator {
    private IStatusForm parent;

    private AbstractFormValidator(JComponent c, String message) {
        super(c, message);
    }

    public AbstractFormValidator(IStatusForm parent, JComponent c, String message) {
        this(c, message);
        this.parent = parent;
    }

    @Override
    protected abstract boolean validationCriteria(JComponent var1);

    @Override
    public boolean verify(JComponent c) {
        if (!this.validationCriteria(c)) {
            c.requestFocus();
            this.parent.warnUser(this.getMessage());
            return false;
        }
        this.parent.clearStatusMessage();
        return true;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        this.parent.clearStatusMessage();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}

