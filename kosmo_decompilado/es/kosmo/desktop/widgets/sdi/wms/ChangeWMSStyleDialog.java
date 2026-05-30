/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.sdi.wms;

import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import es.kosmo.desktop.widgets.sdi.wms.ChangeWMSStylePanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.saig.jump.lang.I18N;

public class ChangeWMSStyleDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private WMSLayer wmsLayer;

    public ChangeWMSStyleDialog(JFrame parent, boolean modal, WMSLayer wmsLayer) {
        super((Frame)parent, modal);
        this.wmsLayer = wmsLayer;
        this.initialize();
        this.pack();
        GUIUtil.centreOnWindow(this);
        this.setVisible(true);
    }

    private void initialize() {
        this.setTitle(I18N.getString("org.saig.jump.widgets.wms.ChangeWMSStyleDialog.Configure-WMS-service-style"));
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add((Component)new ChangeWMSStylePanel(this.wmsLayer), "Center");
        this.getContentPane().add((Component)this.getOKCancelPanel(), "South");
    }

    private JPanel getOKCancelPanel() {
        JPanel okPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton(I18N.getString("org.saig.jump.widgets.wms.ChangeWMSStyleDialog.Finish"));
        okButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                ChangeWMSStyleDialog.this.setVisible(false);
                ChangeWMSStyleDialog.this.wmsLayer.fireAppearanceChanged();
            }
        });
        okPanel.add(okButton);
        return okPanel;
    }
}

