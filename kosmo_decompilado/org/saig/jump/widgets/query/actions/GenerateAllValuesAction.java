/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.query.actions;

import javax.swing.AbstractAction;
import javax.swing.Icon;

public abstract class GenerateAllValuesAction
extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public GenerateAllValuesAction() {
        super("...");
    }

    public GenerateAllValuesAction(String name) {
        super(name);
    }

    public GenerateAllValuesAction(String name, Icon icon) {
        super(name, icon);
    }
}

