/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.ui.AbstractDriverPanel;
import com.vividsolutions.jump.workbench.ui.DriverPanelCache;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.FileNamePanel;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFileFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.jump.lang.I18N;

public class GMLFileDriverPanel
extends AbstractDriverPanel {
    private static final String TEMPLATE_FILE_CACHE_KEY = "TEMPLATE_FILE";
    BorderLayout borderLayout1 = new BorderLayout();
    OKCancelPanel okCancelPanel = new OKCancelPanel();
    JPanel centrePanel = new JPanel();
    JPanel innerCentrePanel = new JPanel();
    FileNamePanel templateFileNamePanel;
    JLabel whitespaceLabel = new JLabel();
    FileNamePanel gmlFileNamePanel;
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    private ArrayList possibleTemplateExtensions = new ArrayList();

    public GMLFileDriverPanel(ErrorHandler errorHandler) {
        this.templateFileNamePanel = new FileNamePanel(errorHandler);
        this.gmlFileNamePanel = new FileNamePanel(errorHandler);
        try {
            this.jbInit();
            this.gmlFileNamePanel.setFileMustExist(true);
            this.templateFileNamePanel.setFileMustExist(true);
            this.gmlFileNamePanel.addBrowseListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    GMLFileDriverPanel.this.findPossibleTemplateFile();
                }
            });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setGMLFileMustExist(boolean gmlFileMustExist) {
        this.gmlFileNamePanel.setFileMustExist(gmlFileMustExist);
    }

    public void setTemplateFileDescription(String description) {
        this.templateFileNamePanel.setUpperDescription(description);
    }

    @Override
    public void setCache(DriverPanelCache cache) {
        super.setCache(cache);
        if (cache.get("FILE") != null) {
            this.gmlFileNamePanel.setSelectedFile((File)cache.get("FILE"));
        }
        if (cache.get(TEMPLATE_FILE_CACHE_KEY) != null) {
            this.templateFileNamePanel.setSelectedFile((File)cache.get(TEMPLATE_FILE_CACHE_KEY));
        }
    }

    public void addPossibleTemplateExtension(String extension) {
        this.possibleTemplateExtensions.add(extension);
    }

    @Override
    public String getValidationError() {
        if (!this.gmlFileNamePanel.isInputValid()) {
            return this.gmlFileNamePanel.getValidationError();
        }
        if (!this.templateFileNamePanel.isInputValid()) {
            return this.templateFileNamePanel.getValidationError();
        }
        return null;
    }

    public File getGMLFile() {
        return this.gmlFileNamePanel.getSelectedFile();
    }

    public File getTemplateFile() {
        return this.templateFileNamePanel.getSelectedFile();
    }

    @Override
    public DriverPanelCache getCache() {
        DriverPanelCache cache = super.getCache();
        cache.put("FILE", this.gmlFileNamePanel.getSelectedFile());
        cache.put(TEMPLATE_FILE_CACHE_KEY, this.templateFileNamePanel.getSelectedFile());
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
        this.templateFileNamePanel.setUpperDescription("Template File Description Goes Here");
        this.whitespaceLabel.setText(" ");
        this.gmlFileNamePanel.setUpperDescription(I18N.getString("workbench.ui.GMLFileDriverPanel.gml-file"));
        this.gmlFileNamePanel.setFileFilter(new WorkbenchFileFilter("GML"));
        this.centrePanel.setLayout(this.gridBagLayout2);
        this.add((Component)this.okCancelPanel, "South");
        this.add((Component)this.centrePanel, "Center");
        this.centrePanel.add((Component)this.innerCentrePanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 1, new Insets(10, 10, 10, 10), 0, 0));
        this.innerCentrePanel.add((Component)this.gmlFileNamePanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.innerCentrePanel.add((Component)this.templateFileNamePanel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.innerCentrePanel.add((Component)this.whitespaceLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
    }

    private void findPossibleTemplateFile() {
        String gmlFile = this.gmlFileNamePanel.getSelectedFile().toString();
        if (gmlFile.length() < "a.aaa".length()) {
            return;
        }
        for (String extension : this.possibleTemplateExtensions) {
            File templateFile = new File(String.valueOf(gmlFile.substring(0, gmlFile.length() - ".aaa".length())) + extension);
            if (!templateFile.exists()) continue;
            this.templateFileNamePanel.setSelectedFile(templateFile);
            return;
        }
    }
}

