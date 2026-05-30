/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.actions;

import java.awt.event.ActionEvent;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;
import org.saig.jump.widgets.print.elements.legend.LegendFrame;

public class AddLegend
extends PrintAction {
    public AddLegend(PrintLayoutFrame plf) {
        super(plf);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LegendFrame legendFrame = new LegendFrame(this.frame);
        this.frame.setGraphic(legendFrame);
        this.frame.getTaskFrame().getLayerManager().addLayerListener(legendFrame);
    }
}

