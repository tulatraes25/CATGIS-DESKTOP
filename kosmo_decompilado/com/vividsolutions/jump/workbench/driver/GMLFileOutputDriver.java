/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.driver;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.GMLWriter;
import com.vividsolutions.jump.workbench.driver.AbstractOutputDriver;
import com.vividsolutions.jump.workbench.driver.DriverManager;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.AbstractDriverPanel;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.GMLFileDriverPanel;
import java.io.File;
import org.saig.jump.lang.I18N;

public class GMLFileOutputDriver
extends AbstractOutputDriver {
    private GMLFileDriverPanel panel;
    private GMLWriter writer = new GMLWriter();

    @Override
    public void output(Layer layer) throws Exception {
        File selectedFile = this.panel.getGMLFile();
        String fname = selectedFile.getAbsolutePath();
        DriverProperties dp = new DriverProperties();
        dp.set("File", fname);
        dp.set("TemplateFile", this.panel.getTemplateFile().getAbsolutePath());
        this.writer.write((FeatureCollection)layer.getFeatureCollectionWrapper(), dp);
    }

    @Override
    public String toString() {
        return "GML 2.0";
    }

    @Override
    public AbstractDriverPanel getPanel() {
        return this.panel;
    }

    @Override
    public void initialize(DriverManager driverManager, ErrorHandler errorHandler) {
        super.initialize(driverManager, errorHandler);
        this.panel = new GMLFileDriverPanel(errorHandler);
        this.panel.setGMLFileMustExist(false);
        this.panel.setTemplateFileDescription(I18N.getString("workbench.driver.GMLFileOutputDriver.jcs-gml-output-template-file"));
        this.panel.addPossibleTemplateExtension(".jot");
        this.panel.addPossibleTemplateExtension("_output.xml");
    }
}

