/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.hiperlink;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.IOException;
import java.net.URL;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.log4j.Logger;
import org.saig.core.util.SwingWorker;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.hiperlink.ShowHTMLDialog;
import org.saig.jump.widgets.util.DialogFactory;

public class HTMLWaitDialog
extends JDialog {
    protected static final Logger LOGGER = Logger.getLogger((String)"org.saig.jump.widgets.print.util.PrintWaitDialog");
    public static boolean canceled = false;

    public HTMLWaitDialog(final JFrame frame, final URL url, final String title) {
        super((Frame)frame, true);
        this.getContentPane().setLayout(new BorderLayout());
        this.setTitle(I18N.getString("org.saig.jump.widgets.hiperlink.HTMLWaitDialog.loading-html"));
        JLabel label = new JLabel();
        label.setIcon(IconLoader.icon("loading.gif"));
        label.setHorizontalAlignment(0);
        this.getContentPane().add((Component)label, "Center");
        this.setSize(new Dimension(200, 100));
        GUIUtil.centreOnWindow(this);
        SwingWorker worker = new SwingWorker(){

            @Override
            public Object construct() {
                JEditorPane textArea = new JEditorPane();
                textArea.setEditable(false);
                textArea.addHyperlinkListener(new MyHyperlinkListener());
                try {
                    try {
                        textArea.setPage(url);
                        new ShowHTMLDialog(frame, false, textArea, title);
                    }
                    catch (Exception e) {
                        DialogFactory.showErrorDialog(frame, String.valueOf(I18N.getMessage("org.saig.jump.widgets.hiperlink.HTMLWaitDialog.an-error-has-been-produced-while-loading-the-page-{0}", new Object[]{url})) + ".\n" + I18N.getMessage("org.saig.jump.widgets.hiperlink.HTMLWaitDialog.the-error-is-{0}", new Object[]{e.getMessage()}), I18N.getString("org.saig.jump.widgets.hiperlink.HTMLWaitDialog.error"));
                        HTMLWaitDialog.this.setVisible(false);
                    }
                }
                finally {
                    HTMLWaitDialog.this.setVisible(false);
                }
                return frame;
            }

            @Override
            public void finished() {
                HTMLWaitDialog.this.closeWindow();
            }
        };
        worker.start();
    }

    public void closeWindow() {
        this.dispose();
    }

    class MyHyperlinkListener
    implements HyperlinkListener {
        MyHyperlinkListener() {
        }

        @Override
        public void hyperlinkUpdate(HyperlinkEvent evt) {
            if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                JEditorPane pane = (JEditorPane)evt.getSource();
                try {
                    pane.setPage(evt.getURL());
                    pane.repaint();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

