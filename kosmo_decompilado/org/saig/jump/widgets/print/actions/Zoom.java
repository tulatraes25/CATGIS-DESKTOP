/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import org.saig.jump.widgets.print.PreviewPanel;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;

public class Zoom
extends PrintAction {
    public Zoom(PrintLayoutFrame parent) {
        super(parent);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.frame.setActiveZoom(3);
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        String ft = (String)((JComboBox)e.getSource()).getSelectedItem();
        ft = ft.substring(0, ft.length() - 1);
        float f = new Float(ft).floatValue();
        f /= 100.0f;
        f = f / 72.0f * (float)dpi;
        this.frame.setZoomValue(f);
        this.frame.getPrintLayoutPreviewPanel().getPreviewPanel().getPage().zoom();
        this.frame.getPrintLayoutPreviewPanel().setPreview(new PreviewPanel(this.frame, this.frame.getPrintLayoutPreviewPanel().getPreviewPanel().getPage()));
    }
}

