/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.actions;

import java.awt.event.ActionEvent;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;
import org.saig.jump.widgets.print.elements.map.MapFrame;

public class AddMap
extends PrintAction {
    public AddMap(PrintLayoutFrame plf) {
        super(plf);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        MapFrame mapFrame = new MapFrame(this.frame);
        this.frame.setGraphic(mapFrame);
        this.frame.getTaskFrame().getLayerManager().addLayerListener(mapFrame);
        this.frame.getTaskFrame().getLayerViewPanel().getViewport().addListener(mapFrame);
    }
}

