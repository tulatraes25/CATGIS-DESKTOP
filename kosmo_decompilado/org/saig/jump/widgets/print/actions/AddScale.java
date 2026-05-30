/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.actions;

import java.awt.event.ActionEvent;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;
import org.saig.jump.widgets.print.elements.scale.ScaleFrame;

public class AddScale
extends PrintAction {
    public AddScale(PrintLayoutFrame plf) {
        super(plf);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        this.frame.setGraphic(new ScaleFrame(this.frame));
    }
}

