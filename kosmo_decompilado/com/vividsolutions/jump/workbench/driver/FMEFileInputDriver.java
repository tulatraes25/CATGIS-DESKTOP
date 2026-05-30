/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.io.ParseException
 */
package com.vividsolutions.jump.workbench.driver;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.CompressedFile;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.FMEGMLReader;
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.workbench.driver.AbstractInputDriver;
import com.vividsolutions.jump.workbench.driver.DriverManager;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.AbstractDriverPanel;
import com.vividsolutions.jump.workbench.ui.BasicFileDriverPanel;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.WorkbenchFileFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.saig.jump.lang.I18N;

public class FMEFileInputDriver
extends AbstractInputDriver {
    private BasicFileDriverPanel panel;
    private DriverProperties dp = new DriverProperties();

    @Override
    public String toString() {
        return "FME GML";
    }

    @Override
    public void initialize(DriverManager driverManager, ErrorHandler errorHandler) {
        super.initialize(driverManager, errorHandler);
        this.panel = new BasicFileDriverPanel(errorHandler);
        this.panel.setFileMustExist(true);
        this.panel.setFileDescription("FME GML");
        this.panel.setFileFilter(new WorkbenchFileFilter("FME GML"));
    }

    @Override
    public void input(LayerManager layerManager, String categoryName) throws FileNotFoundException, IOException, com.vividsolutions.jump.io.ParseException, ParseException, IllegalParametersException, Exception {
        File selectedFile = this.panel.getSelectedFile();
        FMEGMLReader fmeReader = new FMEGMLReader();
        String name = selectedFile.getAbsolutePath();
        String extension = name.substring(name.length() - 3);
        this.dp = new DriverProperties();
        if (extension.equalsIgnoreCase("zip")) {
            this.dp.set("CompressedFile", name);
            String internalName = CompressedFile.getInternalZipFnameByExtension(".fme", name);
            if (internalName == null) {
                internalName = CompressedFile.getInternalZipFnameByExtension(".xml", name);
            }
            if (internalName == null) {
                internalName = CompressedFile.getInternalZipFnameByExtension(".gml", name);
            }
            if (internalName == null) {
                throw new Exception(I18N.getMessage("workbench.driver.FMEFileInputDriver.couldnt-find-a-fme-xml-gml-file-inside-the-zip-file-{0}", new Object[]{name}));
            }
            this.dp.set("File", internalName);
        } else if (extension.equalsIgnoreCase(".gz")) {
            this.dp.set("CompressedFile", name);
            this.dp.set("File", name);
        } else {
            this.dp.set("File", name);
        }
        FeatureCollection featureCollection = fmeReader.read(this.dp);
        Layer layer = layerManager.addLayer(categoryName, GUIUtil.nameWithoutExtension(selectedFile), featureCollection);
    }

    @Override
    public AbstractDriverPanel getPanel() {
        return this.panel;
    }
}

