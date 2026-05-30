/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.query.actions;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public abstract class InsertWKTStringAction
extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public InsertWKTStringAction() {
        super(I18N.getString("org.saig.jump.widgets.query.actions.InsertWKTStringAction.WKT-string"));
    }

    public InsertWKTStringAction(String name) {
        super(name);
    }

    public InsertWKTStringAction(String name, Icon icon) {
        super(name, icon);
    }
}

