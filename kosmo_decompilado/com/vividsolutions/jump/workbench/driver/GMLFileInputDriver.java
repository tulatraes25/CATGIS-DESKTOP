/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.driver;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.CompressedFile;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.GMLReader;
import com.vividsolutions.jump.workbench.driver.AbstractInputDriver;
import com.vividsolutions.jump.workbench.driver.DriverManager;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.AbstractDriverPanel;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.GMLFileDriverPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.io.File;
import org.saig.jump.lang.I18N;

public class GMLFileInputDriver
extends AbstractInputDriver {
    private GMLFileDriverPanel panel;
    private GMLReader reader = new GMLReader();

    @Override
    public void input(LayerManager layerManager, String categoryName) throws Exception {
        String internalName;
        File selectedFile = this.panel.getGMLFile();
        String layerName = GUIUtil.nameWithoutExtension(selectedFile);
        String fname = selectedFile.getAbsolutePath();
        DriverProperties dp = new DriverProperties();
        String extension_gml = fname.substring(fname.length() - 3);
        String extension_template = this.panel.getTemplateFile().getAbsolutePath().substring(this.panel.getTemplateFile().getAbsolutePath().length() - 3);
        if (extension_gml.equalsIgnoreCase("zip")) {
            dp.set("CompressedFile", fname);
            internalName = CompressedFile.getInternalZipFnameByExtension(".gml", fname);
            if (internalName == null) {
                internalName = CompressedFile.getInternalZipFnameByExtension(".xml", fname);
            }
            if (internalName == null) {
                throw new Exception(I18N.getMessage("workbench.driver.GMLFileInputDriver.couldnt-find-a-xml-gml-file-inside-the-zip-file-{0}", new Object[]{fname}));
            }
            dp.set("File", internalName);
        } else if (extension_gml.equalsIgnoreCase(".gz")) {
            dp.set("CompressedFile", fname);
            dp.set("File", fname);
        } else {
            dp.set("File", fname);
        }
        if (extension_template.equalsIgnoreCase("zip")) {
            dp.set("CompressedFileTemplate", this.panel.getTemplateFile().getAbsolutePath());
            internalName = CompressedFile.getInternalZipFnameByExtension("_input.xml", this.panel.getTemplateFile().getAbsolutePath());
            if (internalName == null) {
                internalName = CompressedFile.getInternalZipFnameByExtension(".input", this.panel.getTemplateFile().getAbsolutePath());
            }
            if (internalName == null) {
                internalName = CompressedFile.getInternalZipFnameByExtension(".template", this.panel.getTemplateFile().getAbsolutePath());
            }
            if (internalName == null) {
                throw new Exception(I18N.getMessage("workbench.driver.GMLFileInputDriver.couldnt-find-a-input.xml-input-or-template-file-inside-the-zip-file-{0}", new Object[]{this.panel.getTemplateFile().getAbsolutePath()}));
            }
            dp.set("TemplateFile", internalName);
        } else if (extension_template.equalsIgnoreCase(".gz")) {
            dp.set("CompressedFileTemplate", this.panel.getTemplateFile().getAbsolutePath());
            dp.set("TemplateFile", this.panel.getTemplateFile().getAbsolutePath());
        } else {
            dp.set("TemplateFile", this.panel.getTemplateFile().getAbsolutePath());
        }
        FeatureCollection featureCollection = this.reader.read(dp);
        Layer layer = layerManager.addLayer(categoryName, layerName, featureCollection);
    }

    @Override
    public String toString() {
        return "GML 2.0";
    }

    @Override
    public void initialize(DriverManager driverManager, ErrorHandler errorHandler) {
        super.initialize(driverManager, errorHandler);
        this.panel = new GMLFileDriverPanel(errorHandler);
        this.panel.setGMLFileMustExist(true);
        this.panel.setTemplateFileDescription(I18N.getString("workbench.driver.GMLFileInputDriver.jcs-gml-input-template-file"));
        this.panel.addPossibleTemplateExtension(".jit");
        this.panel.addPossibleTemplateExtension("_input.xml");
        this.panel.addPossibleTemplateExtension(".gz");
        this.panel.addPossibleTemplateExtension(".zip");
    }

    @Override
    public AbstractDriverPanel getPanel() {
        return this.panel;
    }
}

