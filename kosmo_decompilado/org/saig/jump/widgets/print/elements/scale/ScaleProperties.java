/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements.scale;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.elements.map.MapFrame;
import org.saig.jump.widgets.print.elements.scale.ScaleFrame;

public class ScaleProperties
extends JFrame {
    private JPanel jContentPane = null;
    private OKCancelPanel okCancelPanel = new OKCancelPanel();
    private ScaleFrame scaleFrame;
    private JPanel viewSelectionPanel;
    private JComboBox viewSelectionComboBox;

    public ScaleProperties(ScaleFrame scaleFrame) {
        this.setIconImage(JUMPWorkbench.APP_ICON.getImage());
        this.scaleFrame = scaleFrame;
        this.initialize();
    }

    private void initialize() {
        this.setContentPane(this.getJContentPane());
        this.setName(I18N.getString("org.saig.jump.widgets.print.elements.scale.ScaleProperties.scale-properties"));
        this.setTitle(I18N.getString("org.saig.jump.widgets.print.elements.scale.ScaleProperties.scale-properties"));
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (ScaleProperties.this.okCancelPanel.wasOKPressed()) {
                    MapFrame selectedMapFrame = (MapFrame)ScaleProperties.this.viewSelectionComboBox.getSelectedItem();
                    if (!selectedMapFrame.getName().equals(ScaleProperties.this.scaleFrame.getAssociatedMapFrameName())) {
                        ScaleProperties.this.scaleFrame.refreshAssociatedMapFrame(selectedMapFrame);
                        ScaleProperties.this.scaleFrame.repaint();
                    }
                    ScaleProperties.this.termine();
                } else {
                    ScaleProperties.this.termine();
                }
            }
        });
        this.getContentPane().add((Component)this.getViewSelectionPanel(), "Center");
        this.getContentPane().add((Component)this.okCancelPanel, "South");
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    private JPanel getViewSelectionPanel() {
        if (this.viewSelectionPanel == null) {
            this.viewSelectionPanel = new JPanel();
            this.viewSelectionPanel.setLayout(new GridBagLayout());
            this.viewSelectionPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.scale.ScaleProperties.associated-view")));
            this.viewSelectionPanel.setMinimumSize(new Dimension(300, 50));
            this.viewSelectionPanel.setPreferredSize(new Dimension(300, 50));
            FormUtils.addRowInGBL(this.viewSelectionPanel, 0, 0, this.getViewSelectionComboBox());
        }
        return this.viewSelectionPanel;
    }

    private JComboBox getViewSelectionComboBox() {
        if (this.viewSelectionComboBox == null) {
            this.viewSelectionComboBox = new JComboBox();
            this.viewSelectionComboBox.setMinimumSize(new Dimension(200, 20));
            this.viewSelectionComboBox.setPreferredSize(new Dimension(200, 20));
            List<MapFrame> availableMapElements = this.scaleFrame.getParent().getAllMapElements();
            this.viewSelectionComboBox.removeAllItems();
            for (MapFrame element : availableMapElements) {
                this.viewSelectionComboBox.addItem(element);
            }
            this.viewSelectionComboBox.setSelectedItem(this.scaleFrame.getAssociatedMapFrame());
        }
        return this.viewSelectionComboBox;
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

