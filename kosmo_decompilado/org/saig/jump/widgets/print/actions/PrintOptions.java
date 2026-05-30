/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.actions;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.event.ActionEvent;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;
import org.saig.jump.widgets.print.elements.PrintOptionsDialog;

public class PrintOptions
extends PrintAction {
    public static final int KEEP_ASPECT_RATIO = 0;
    public static final int KEEP_SIZE = 1;
    public static int printQuality = 100;
    public static int wmsPrintQuality = 75;
    public static int wmsGrid = 1000;
    private PrintOptionsDialog dialog;

    public PrintOptions(PrintLayoutFrame parent) {
        super(parent);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (this.dialog == null) {
            this.dialog = new PrintOptionsDialog(this.frame, I18N.getString("org.saig.jump.widgets.print.actions.PrintOptions.print-options"), true);
            GUIUtil.centreOnWindow(this.dialog);
        }
        this.dialog.setSeleccion(this.frame.getPage().getSizeSelection());
        this.dialog.setPrintQuality(printQuality);
        this.dialog.setWmsPrintQuality(wmsPrintQuality);
        this.dialog.setWmsGridQuality(wmsGrid);
        this.dialog.setVisible(true);
        if (this.dialog.wasOkPressed()) {
            this.frame.getPage().setSizeSelection(this.dialog.getSeleccion());
            printQuality = this.dialog.getPrintQuality();
            wmsPrintQuality = this.dialog.getWMSPrintQuality();
            wmsGrid = this.dialog.getWMSGridQuality();
        }
    }
}

