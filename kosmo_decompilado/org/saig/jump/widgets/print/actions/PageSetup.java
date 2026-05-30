/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.actions;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import org.saig.jump.widgets.print.Page;
import org.saig.jump.widgets.print.PreviewPanel;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;
import org.saig.jump.widgets.print.util.PageSetupDialog;

public class PageSetup
extends PrintAction {
    private PageSetupDialog dialog;

    public PageSetup(PrintLayoutFrame parent) {
        super(parent);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PageFormat newPageFormat;
        PageFormat oldPageFormat = this.frame.getPageFormat();
        if (this.dialog == null) {
            this.dialog = new PageSetupDialog(this.frame, true, this.frame.getPrinterJob());
            GUIUtil.centreOnWindow(this.dialog);
        }
        this.dialog.loadPageFormat(oldPageFormat);
        this.dialog.setVisible(true);
        if (this.dialog.wasOKPressed() && !(newPageFormat = this.dialog.getNewPageFormat()).equals(oldPageFormat)) {
            this.frame.setPageFormat(newPageFormat);
            Page page = this.frame.getPrintLayoutPreviewPanel().getPreviewPanel().getPage();
            page.resize(newPageFormat);
            this.frame.getPrintLayoutPreviewPanel().setPreview(new PreviewPanel(this.frame, page));
        }
    }
}

