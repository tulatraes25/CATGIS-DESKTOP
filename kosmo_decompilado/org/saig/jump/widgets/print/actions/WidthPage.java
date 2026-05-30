/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;

public class WidthPage
extends PrintAction {
    public WidthPage(PrintLayoutFrame parent) {
        super(parent);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        float dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        float fh = this.frame.getPrintLayoutPreviewPanel().getViewport().getWidth();
        float ph = (float)this.frame.getPageFormat().getWidth();
        float f = fh / ph;
        f = f * 72.0f / dpi;
        int fi = (int)(f * 100.0f);
        String zoomStr = String.valueOf(new Integer(fi).toString()) + "%";
        this.frame.getPrintLayoutToolBar().setZoom(zoomStr);
    }
}

