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

public class JMLFileDriverPanel
extends AbstractDriverPanel {
    private static final String TEMPLATE_FILE_CACHE_KEY = "TEMPLATE_FILE";
    BorderLayout borderLayout1 = new BorderLayout();
    OKCancelPanel okCancelPanel = new OKCancelPanel();
    JPanel centrePanel = new JPanel();
    JPanel innerCentrePanel = new JPanel();
    FileNamePanel templateFileNamePanel;
    FileNamePanel jmlFileNamePanel;
    JLabel whitespaceLabel = new JLabel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    GridBagLayout gridBagLayout2 = new GridBagLayout();

    public JMLFileDriverPanel(ErrorHandler errorHandler) {
        this.templateFileNamePanel = new FileNamePanel(errorHandler);
        this.jmlFileNamePanel = new FileNamePanel(errorHandler);
        try {
            this.jbInit();
            this.jmlFileNamePanel.setFileMustExist(true);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setGMLFileMustExist(boolean gmlFileMustExist) {
        this.jmlFileNamePanel.setFileMustExist(gmlFileMustExist);
    }

    @Override
    public void setCache(DriverPanelCache cache) {
        super.setCache(cache);
        if (cache.get("FILE") != null) {
            this.jmlFileNamePanel.setSelectedFile((File)cache.get("FILE"));
        }
        if (cache.get(TEMPLATE_FILE_CACHE_KEY) != null) {
            this.templateFileNamePanel.setSelectedFile((File)cache.get(TEMPLATE_FILE_CACHE_KEY));
        }
    }

    @Override
    public String getValidationError() {
        if (!this.jmlFileNamePanel.isInputValid()) {
            return this.jmlFileNamePanel.getValidationError();
        }
        return null;
    }

    public File getJMLFile() {
        return this.jmlFileNamePanel.getSelectedFile();
    }

    public File getTemplateFile() {
        return this.templateFileNamePanel.getSelectedFile();
    }

    @Override
    public DriverPanelCache getCache() {
        DriverPanelCache cache = super.getCache();
        cache.put("FILE", this.jmlFileNamePanel.getSelectedFile());
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
        this.jmlFileNamePanel.setUpperDescription(I18N.getString("workbench.ui.JMLFileDriverPanel.jml-file"));
        this.centrePanel.setLayout(this.gridBagLayout2);
        this.add((Component)this.okCancelPanel, "South");
        this.add((Component)this.centrePanel, "Center");
        this.centrePanel.add((Component)this.innerCentrePanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 1, new Insets(10, 10, 10, 10), 0, 0));
        this.innerCentrePanel.add((Component)this.jmlFileNamePanel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.innerCentrePanel.add((Component)this.whitespaceLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
    }
}

