/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.apache.log4j.Logger;
import org.saig.core.util.SwingWorker;

public abstract class AbstractWaitDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    protected static final Logger LOGGER = Logger.getLogger(AbstractWaitDialog.class);
    protected String errorMessage = "";

    public AbstractWaitDialog(JFrame parent, String title) {
        super((Frame)parent, true);
        this.getContentPane().setLayout(new BorderLayout());
        this.setTitle(title);
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
                    AbstractWaitDialog.this.methodToPerform();
                    return null;
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    AbstractWaitDialog.this.errorMessage = e.getMessage();
                    AbstractWaitDialog.this.dispose();
                    return null;
                }
            }

            @Override
            public void finished() {
                AbstractWaitDialog.this.closeWindow();
            }
        };
        worker.start();
    }

    void closeWindow() {
        this.dispose();
    }

    protected abstract void methodToPerform() throws Exception;

    public String getErrorMessage() {
        return this.errorMessage;
    }
}

