/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.actions;

import java.awt.event.ActionEvent;
import org.saig.jump.widgets.print.ElementsViewerDialog;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;

public class ShowElementsDialog
extends PrintAction {
    public ShowElementsDialog(PrintLayoutFrame parent) {
        super(parent);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (this.frame.getElementsViewerDialog() == null) {
            this.frame.setElementsViewerDialog(new ElementsViewerDialog(this.frame));
        } else {
            this.frame.getElementsViewerDialog().setVisible(!this.frame.getElementsViewerDialog().isVisible());
        }
    }
}

