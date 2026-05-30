/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements.north;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.elements.NorthSelectionPanel;
import org.saig.jump.widgets.print.elements.north.NorthFrame;

public class NorthProperties
extends JFrame {
    private JPanel jContentPane = null;
    private OKCancelPanel okCancelPanel = new OKCancelPanel();
    private NorthSelectionPanel imageSelectionPanel = null;
    private NorthFrame northFrame;

    public NorthProperties(NorthFrame northFrame) {
        this.setIconImage(JUMPWorkbench.APP_ICON.getImage());
        this.northFrame = northFrame;
        this.initialize();
    }

    private void initialize() {
        this.setContentPane(this.getJContentPane());
        this.setName(I18N.getString("org.saig.jump.widgets.print.elements.north.NorthProperties.north-properties"));
        this.setTitle(I18N.getString("org.saig.jump.widgets.print.elements.north.NorthProperties.north-properties"));
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (NorthProperties.this.okCancelPanel.wasOKPressed()) {
                    NorthProperties.this.northFrame.setNorthSymbol(NorthProperties.this.imageSelectionPanel.getSelectedIcon());
                    NorthProperties.this.northFrame.getParent().setNorthRotation(NorthProperties.this.imageSelectionPanel.getSelectedAngle());
                    NorthProperties.this.northFrame.repaint();
                    NorthProperties.this.termine();
                } else {
                    NorthProperties.this.termine();
                }
            }
        });
        this.imageSelectionPanel = new NorthSelectionPanel(this.northFrame.getNorthSymbol(), this.northFrame.getParent().getNorthRotation());
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add((Component)this.imageSelectionPanel, "Center");
        this.getContentPane().add((Component)this.okCancelPanel, "South");
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    private JPanel getJContentPane() {
        if (this.jContentPane == null) {
            this.jContentPane = new JPanel();
            this.jContentPane.setLayout(new BorderLayout());
        }
        return this.jContentPane;
    }

    private void termine() {
        this.dispose();
    }
}

