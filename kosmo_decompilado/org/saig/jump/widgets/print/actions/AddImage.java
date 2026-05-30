/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.actions;

import java.awt.event.ActionEvent;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;
import org.saig.jump.widgets.print.elements.image.ImageFrame;

public class AddImage
extends PrintAction {
    public AddImage(PrintLayoutFrame plf) {
        super(plf);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.frame.setGraphic(new ImageFrame(this.frame));
    }
}

