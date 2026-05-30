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
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

public class BasicFileDriverPanel
extends AbstractDriverPanel {
    private static final long serialVersionUID = 1L;
    BorderLayout borderLayout1 = new BorderLayout();
    OKCancelPanel okCancelPanel = new OKCancelPanel();
    JPanel centrePanel = new JPanel();
    JPanel innerCentrePanel = new JPanel();
    protected FileNamePanel fileNamePanel;
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    GridBagLayout gridBagLayout2 = new GridBagLayout();

    public BasicFileDriverPanel(ErrorHandler errorHandler) {
        this.fileNamePanel = new FileNamePanel(errorHandler);
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        this.setLayout(this.borderLayout1);
        this.innerCentrePanel.setLayout(this.gridBagLayout1);
        this.fileNamePanel.setUpperDescription("File Description Goes Here");
        this.centrePanel.setLayout(this.gridBagLayout2);
        this.add((Component)this.okCancelPanel, "South");
        this.add((Component)this.centrePanel, "Center");
        this.centrePanel.add((Component)this.innerCentrePanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 1, new Insets(10, 10, 10, 10), 0, 0));
        this.innerCentrePanel.add((Component)this.fileNamePanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
    }

    public void setFileMustExist(boolean fileMustExist) {
        this.fileNamePanel.setFileMustExist(fileMustExist);
    }

    @Override
    public String getValidationError() {
        if (!this.fileNamePanel.isInputValid()) {
            return this.fileNamePanel.getValidationError();
        }
        return null;
    }

    public File getSelectedFile() {
        return this.fileNamePanel.getSelectedFile();
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

    public void setFileDescription(String description) {
        this.fileNamePanel.setUpperDescription(description);
    }

    @Override
    public void setCache(DriverPanelCache cache) {
        super.setCache(cache);
        if (cache.get("FILE") != null) {
            this.fileNamePanel.setSelectedFile((File)cache.get("FILE"));
        }
    }

    @Override
    public DriverPanelCache getCache() {
        DriverPanelCache cache = super.getCache();
        cache.put("FILE", this.fileNamePanel.getSelectedFile());
        return cache;
    }

    public void setFileFilter(FileFilter filter) {
        this.fileNamePanel.setFileFilter(filter);
    }
}

