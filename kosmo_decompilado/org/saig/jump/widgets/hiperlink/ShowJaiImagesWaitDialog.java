/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.hiperlink;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.saig.core.util.SwingWorker;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.hiperlink.ShowJAIImagesDialog;

public class ShowJaiImagesWaitDialog
extends JDialog {
    private ShowJAIImagesDialog dialog;

    public ShowJaiImagesWaitDialog(JFrame parent, boolean modal, final String path, final String relativePathDirectory) {
        super((Frame)parent, modal);
        this.getContentPane().setLayout(new BorderLayout());
        this.setTitle(I18N.getString("org.saig.jump.widgets.hiperlink.ShowJaiImagesWaitDialog.loading-hiperlink"));
        JLabel label = new JLabel();
        label.setIcon(IconLoader.icon("loading.gif"));
        label.setHorizontalAlignment(0);
        this.getContentPane().add((Component)label, "Center");
        this.setSize(new Dimension(200, 100));
        GUIUtil.centreOnWindow(this);
        SwingWorker worker = new SwingWorker(){

            @Override
            public Object construct() {
                try {
                    ShowJaiImagesWaitDialog.this.dialog = new ShowJAIImagesDialog(JUMPWorkbench.getFrameInstance(), true, path, relativePathDirectory);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    ShowJaiImagesWaitDialog.this.dispose();
                }
                return null;
            }

            @Override
            public void finished() {
                ShowJaiImagesWaitDialog.this.closeWindow();
                if (ShowJaiImagesWaitDialog.this.dialog.isImageOk()) {
                    ShowJaiImagesWaitDialog.this.dialog.setVisible(true);
                }
            }
        };
        worker.start();
    }

    void closeWindow() {
        this.dispose();
    }
}

