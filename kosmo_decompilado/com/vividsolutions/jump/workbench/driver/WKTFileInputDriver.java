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
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.io.WKTReader;
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

public class WKTFileInputDriver
extends AbstractInputDriver {
    private WKTReader reader = new WKTReader();
    private BasicFileDriverPanel panel;

    @Override
    public String toString() {
        return GUIUtil.wktDesc;
    }

    @Override
    public AbstractDriverPanel getPanel() {
        return this.panel;
    }

    @Override
    public void input(LayerManager layerManager, String categoryName) throws FileNotFoundException, IOException, com.vividsolutions.jump.io.ParseException, ParseException, IllegalParametersException, Exception {
        File selectedFile = this.panel.getSelectedFile();
        String layerName = GUIUtil.nameWithoutExtension(selectedFile);
        String fname = selectedFile.getAbsolutePath();
        String extension = fname.substring(fname.length() - 3);
        DriverProperties dp = new DriverProperties();
        if (extension.equalsIgnoreCase("zip")) {
            dp.set("CompressedFile", fname);
            String internalName = CompressedFile.getInternalZipFnameByExtension(".wkt", fname);
            if (internalName == null) {
                internalName = CompressedFile.getInternalZipFnameByExtension(".txt", fname);
            }
            if (internalName == null) {
                throw new Exception(I18N.getMessage("workbench.driver.WKTFileInputDriver.couldnt-find-a-wkt-txt-file-inside-the-zip-file-{0}", new Object[]{fname}));
            }
            dp.set("File", internalName);
        } else if (extension.equalsIgnoreCase(".gz")) {
            dp.set("CompressedFile", fname);
            dp.set("File", fname);
        } else {
            dp.set("File", fname);
        }
        FeatureCollection featureCollection = this.reader.read(dp);
        Layer layer = layerManager.addLayer(categoryName, layerName, featureCollection);
    }

    @Override
    public void initialize(DriverManager driverManager, ErrorHandler errorHandler) {
        super.initialize(driverManager, errorHandler);
        this.panel = new BasicFileDriverPanel(errorHandler);
        this.panel.setFileDescription(GUIUtil.wktDesc);
        this.panel.setFileFilter(new WorkbenchFileFilter(GUIUtil.wktDesc));
        this.panel.setFileMustExist(true);
    }
}

