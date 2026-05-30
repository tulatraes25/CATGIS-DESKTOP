/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.actions;

import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;
import org.saig.jump.widgets.print.util.PrintWaitDialog;

public class Print
extends PrintAction {
    public Print(PrintLayoutFrame parent) {
        super(parent);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PageFormat pfSinMargenes = new PageFormat();
        Paper paper = new Paper();
        paper.setSize(this.frame.getPageFormat().getPaper().getWidth(), this.frame.getPageFormat().getPaper().getHeight());
        paper.setImageableArea(0.0, 0.0, this.frame.getPageFormat().getPaper().getWidth(), this.frame.getPageFormat().getPaper().getHeight());
        pfSinMargenes.setOrientation(this.frame.getPageFormat().getOrientation());
        pfSinMargenes.setPaper(paper);
        this.frame.getPrinterJob().setPrintable((Printable)((Object)this.frame.getPrintLayoutPreviewPanel().getPreviewPanel().getPage().getPageForPrint()), pfSinMargenes);
        boolean accion = this.frame.getPrinterJob().printDialog();
        if (accion) {
            PrintWaitDialog dialog = new PrintWaitDialog(this.frame);
            dialog.setVisible(true);
        }
    }
}

