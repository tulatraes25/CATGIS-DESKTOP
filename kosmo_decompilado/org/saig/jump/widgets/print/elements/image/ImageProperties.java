/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements.image;

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
import org.saig.jump.widgets.print.elements.ImageSelectionPanel;
import org.saig.jump.widgets.print.elements.image.ImageFrame;

public class ImageProperties
extends JFrame {
    private JPanel jContentPane = null;
    private OKCancelPanel okCancelPanel = new OKCancelPanel();
    private ImageSelectionPanel imageSelectionPanel = null;
    private ImageFrame imageFrame;

    public ImageProperties(ImageFrame imageFrame) {
        this.setIconImage(JUMPWorkbench.APP_ICON.getImage());
        this.imageFrame = imageFrame;
        this.initialize();
    }

    private void initialize() {
        this.setContentPane(this.getJContentPane());
        this.setName(I18N.getString("org.saig.jump.widgets.print.elements.image.ImageProperties.image-properties"));
        this.setTitle(I18N.getString("org.saig.jump.widgets.print.elements.image.ImageProperties.image-properties"));
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (ImageProperties.this.okCancelPanel.wasOKPressed()) {
                    ImageProperties.this.imageFrame.setImageSymbol(ImageProperties.this.imageSelectionPanel.getSelectedIcon());
                    ImageProperties.this.imageFrame.repaint();
                    ImageProperties.this.termine();
                } else {
                    ImageProperties.this.termine();
                }
            }
        });
        this.imageSelectionPanel = new ImageSelectionPanel(this.imageFrame.getImageSymbol());
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

