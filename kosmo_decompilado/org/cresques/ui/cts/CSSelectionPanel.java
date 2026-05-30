/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 *  org.cresques.i18n.Messages
 *  org.cresques.ui.LoadableComboBox
 *  org.cresques.ui.cts.CSSelectionModel
 */
package org.cresques.ui.cts;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.cresques.cts.IProjection;
import org.cresques.i18n.Messages;
import org.cresques.ui.LoadableComboBox;
import org.cresques.ui.cts.CSSelectionModel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;

public class CSSelectionPanel
extends JPanel {
    private static final long serialVersionUID = -3370601314380922368L;
    private LoadableComboBox datumComboBox = null;
    private LoadableComboBox projComboBox = null;
    private LoadableComboBox huseComboBox = null;
    private String tit;
    private CSSelectionModel model;

    public CSSelectionPanel(String tit) {
        if (tit == null && (tit = Messages.getText((String)"reference_system")) == null) {
            tit = "Reference System";
        }
        this.tit = tit;
        this.setModel(new CSSelectionModel());
        this.initialize();
    }

    private void initialize() {
        this.setPreferredSize(new Dimension(295, 170));
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createCompoundBorder(null, BorderFactory.createTitledBorder(null, Messages.getText((String)"reference_system"), 0, 0, null, null)));
        FormUtils.addRowInGBL((JComponent)this, 0, 0, String.valueOf(Messages.getText((String)"datum")) + ":", (JComponent)this.getDatumComboBox());
        FormUtils.addRowInGBL((JComponent)this, 1, 0, String.valueOf(Messages.getText((String)"projection")) + ":", (JComponent)this.getProjComboBox());
        FormUtils.addRowInGBL((JComponent)this, 2, 0, String.valueOf(Messages.getText((String)"zone")) + ":", (JComponent)this.getHuseComboBox());
        this.setHuseComboBoxEnabled(false);
    }

    public void setModel(CSSelectionModel model) {
        this.model = model;
        this.getHuseComboBox().loadData(model.getZoneList());
        this.getDatumComboBox().loadData(model.getDatumList());
        this.getProjComboBox().loadData(model.getProjectionList());
    }

    private void setHuseComboBoxEnabled(boolean enabled) {
        this.getHuseComboBox().setEnabled(enabled);
    }

    private void setDatumComboBoxEnabled(boolean enabled) {
        this.getDatumComboBox().setEnabled(enabled);
    }

    public void setProjection(IProjection proj) {
        this.model.setProjection(proj);
        this.setDatumComboBoxEnabled(true);
        this.getDatumComboBox().setSelectedIndex(this.model.getSelectedDatum());
        this.getProjComboBox().removeAllItems();
        this.getProjComboBox().loadData(this.model.getProjectionList());
        this.model.setProjection(proj);
        this.getProjComboBox().setSelectedIndex(this.model.getSelectedProj());
        this.model.setProjection(proj);
        if (this.model.getSelectedZone() >= 0) {
            this.setHuseComboBoxEnabled(true);
            this.getHuseComboBox().removeAllItems();
            this.getHuseComboBox().loadData(this.model.getZoneList());
            this.model.setProjection(proj);
            this.getHuseComboBox().setSelectedIndex(this.model.getSelectedZone());
        } else {
            this.setHuseComboBoxEnabled(false);
            this.getHuseComboBox().setSelectedIndex(0);
        }
    }

    private LoadableComboBox getDatumComboBox() {
        if (this.datumComboBox == null) {
            this.datumComboBox = new LoadableComboBox();
            this.datumComboBox.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent e) {
                    CSSelectionPanel.this.model.setSelectedDatum(e.getItem());
                    CSSelectionPanel.this.getProjComboBox().removeAllItems();
                    CSSelectionPanel.this.getProjComboBox().loadData(CSSelectionPanel.this.model.getProjectionList());
                }
            });
        }
        return this.datumComboBox;
    }

    private LoadableComboBox getProjComboBox() {
        if (this.projComboBox == null) {
            this.projComboBox = new LoadableComboBox();
            this.projComboBox.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent e) {
                    CSSelectionPanel.this.model.setSelectedProj(e.getItem());
                    if (CSSelectionPanel.this.model.getSelectedProjType() == CSSelectionModel.TRANSVERSAL) {
                        CSSelectionPanel.this.setHuseComboBoxEnabled(true);
                        CSSelectionPanel.this.getHuseComboBox().removeAllItems();
                        CSSelectionPanel.this.getHuseComboBox().loadData(CSSelectionPanel.this.model.getZoneList());
                    } else {
                        CSSelectionPanel.this.setHuseComboBoxEnabled(false);
                    }
                }
            });
        }
        return this.projComboBox;
    }

    private LoadableComboBox getHuseComboBox() {
        if (this.huseComboBox == null) {
            this.huseComboBox = new LoadableComboBox();
            this.huseComboBox.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent e) {
                    CSSelectionPanel.this.model.setSelectedZone(e.getItem());
                }
            });
        }
        return this.huseComboBox;
    }

    public IProjection getProjection() {
        return this.model.getProjection();
    }
}

