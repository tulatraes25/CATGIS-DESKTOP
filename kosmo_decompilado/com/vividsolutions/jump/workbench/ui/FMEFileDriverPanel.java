/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.ui.AbstractDriverPanel;
import com.vividsolutions.jump.workbench.ui.DriverPanelCache;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.FileNamePanel;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.jump.lang.I18N;

public class FMEFileDriverPanel
extends AbstractDriverPanel {
    BorderLayout borderLayout1 = new BorderLayout();
    OKCancelPanel okCancelPanel = new OKCancelPanel();
    JPanel centrePanel = new JPanel();
    JPanel innerCentrePanel = new JPanel();
    JLabel whitespaceLabel = new JLabel();
    FileNamePanel fmeFileNamePanel;
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    GridBagLayout gridBagLayout2 = new GridBagLayout();

    public FMEFileDriverPanel(ErrorHandler errorHandler) {
        this.fmeFileNamePanel = new FileNamePanel(errorHandler);
        try {
            this.jbInit();
            this.fmeFileNamePanel.setFileMustExist(true);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setGMLFileMustExist(boolean gmlFileMustExist) {
        this.fmeFileNamePanel.setFileMustExist(gmlFileMustExist);
    }

    @Override
    public void setCache(DriverPanelCache cache) {
        super.setCache(cache);
        if (cache.get("FILE") != null) {
            this.fmeFileNamePanel.setSelectedFile((File)cache.get("FILE"));
        }
    }

    @Override
    public String getValidationError() {
        if (!this.fmeFileNamePanel.isInputValid()) {
            return this.fmeFileNamePanel.getValidationError();
        }
        return null;
    }

    public File getFMEFile() {
        return this.fmeFileNamePanel.getSelectedFile();
    }

    @Override
    public DriverPanelCache getCache() {
        DriverPanelCache cache = super.getCache();
        cache.put("FILE", this.fmeFileNamePanel.getSelectedFile());
        return cache;
    }

    @Override
    public void addActionListener(ActionListener l) {
        this.okCancelPanel.addActionListener(l);
    }

    @Override
    public void removeActionListener(ActionListener l) {
        this.okCancelPanel.removeActionListener(l);
    }

    @Override
    public boolean wasOKPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    void jbInit() throws Exception {
        this.setLayout(this.borderLayout1);
        this.innerCentrePanel.setLayout(this.gridBagLayout1);
        this.whitespaceLabel.setText(" ");
        this.fmeFileNamePanel.setUpperDescription(I18N.getString("workbench.ui.FMEFileDriverPanel.file"));
        this.centrePanel.setLayout(this.gridBagLayout2);
        this.add((Component)this.okCancelPanel, "South");
        this.add((Component)this.centrePanel, "Center");
        this.centrePanel.add((Component)this.innerCentrePanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 1, new Insets(10, 10, 10, 10), 0, 0));
        this.innerCentrePanel.add((Component)this.fmeFileNamePanel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.innerCentrePanel.add((Component)this.whitespaceLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
    }
}

