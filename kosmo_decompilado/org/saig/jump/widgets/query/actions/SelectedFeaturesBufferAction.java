/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.query.actions;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public abstract class SelectedFeaturesBufferAction
extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public SelectedFeaturesBufferAction() {
        super(I18N.getString("org.saig.jump.widgets.query.actions.SelectedFeaturesBufferAction.Selected-features-buffer"));
    }

    public SelectedFeaturesBufferAction(String name) {
        super(name);
    }

    public SelectedFeaturesBufferAction(String name, Icon icon) {
        super(name, icon);
    }
}

