/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.actions;

import java.awt.event.ActionEvent;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;

public class Delete
extends PrintAction {
    public Delete(PrintLayoutFrame plf) {
        super(plf);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        this.frame.getPrintLayoutPreviewPanel().getPreviewPanel().getPage().remove(this.frame.getSelectedComponent());
        this.frame.setSelectedComponent(null);
    }
}

