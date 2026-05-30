/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.query.actions;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public abstract class CurrentViewEnvelopeBufferAction
extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public CurrentViewEnvelopeBufferAction() {
        super(I18N.getString("org.saig.jump.widgets.query.actions.CurrentViewEnvelopeBufferAction.Current-view-envelope-buffer"));
    }

    public CurrentViewEnvelopeBufferAction(String name) {
        super(name);
    }

    public CurrentViewEnvelopeBufferAction(String name, Icon icon) {
        super(name, icon);
    }
}

