/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.sdi.wms;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.SelectFilePanel;

public class WMSConfigDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private JTextField urlTextField;
    private SelectFilePanel selectFilePanel;
    private boolean exitOk = false;

    public WMSConfigDialog(JFrame parent, boolean modal) {
        super((Frame)parent, modal);
        this.setTitle(I18N.getString("org.saig.jump.widgets.wms.WMSConfigDialog.Load-WMS-server-configuration"));
        this.setContentPane(this.getMainPanel());
        this.pack();
        GUIUtil.centreOnWindow(this);
        this.setVisible(true);
    }

    private JPanel getMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel urlLabel = new JLabel(I18N.getString("org.saig.jump.widgets.wms.WMSConfigDialog.WMS-server-URL"));
        this.urlTextField = new JTextField("http://localhost:8080/web-server/config");
        FormUtils.addRowInGBL((JComponent)panel, 0, 0, urlLabel, (JComponent)this.urlTextField);
        this.selectFilePanel = new SelectFilePanel(I18N.getString("org.saig.jump.widgets.wms.WMSConfigDialog.Proyect-files"), new String[]{"spr"});
        FormUtils.addRowInGBL(panel, 1, 0, this.selectFilePanel);
        FormUtils.addRowInGBL(panel, 2, 0, this.createOKcancelPanel());
        return panel;
    }

    private OKCancelPanel createOKcancelPanel() {
        final OKCancelPanel okCancelPanel = new OKCancelPanel();
        GridBagLayout gbPaneOKCancel = new GridBagLayout();
        okCancelPanel.setLayout(gbPaneOKCancel);
        okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (okCancelPanel.wasOKPressed()) {
                    WMSConfigDialog.this.exitOk = true;
                } else {
                    WMSConfigDialog.this.exitOk = false;
                }
                WMSConfigDialog.this.setVisible(false);
            }
        });
        return okCancelPanel;
    }

    public String getURLPath() {
        return this.urlTextField.getText();
    }

    public String getFilePath() {
        return this.selectFilePanel.getSelectedPath();
    }

    public boolean isExitOk() {
        return this.exitOk;
    }
}

