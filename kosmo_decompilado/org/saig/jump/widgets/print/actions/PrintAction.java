/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.actions;

import java.awt.event.ActionListener;
import org.saig.jump.widgets.print.PrintLayoutFrame;

public abstract class PrintAction
implements ActionListener {
    protected PrintLayoutFrame frame;

    public PrintAction(PrintLayoutFrame printFrame) {
        this.frame = printFrame;
    }

    public void dispose() {
        this.frame = null;
    }
}

